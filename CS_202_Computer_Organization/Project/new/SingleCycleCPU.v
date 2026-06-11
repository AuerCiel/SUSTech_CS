`timescale 1ns / 1ps

module SC_Control(
    input  [6:0] opcode,
    output is_r, is_i, is_load, is_store, is_branch, is_lui, is_auipc, is_jal, is_jalr
);
    assign is_r      = (opcode == 7'h33);
    assign is_i      = (opcode == 7'h13);
    assign is_load   = (opcode == 7'h03);
    assign is_store  = (opcode == 7'h23);
    assign is_branch = (opcode == 7'h63);
    assign is_lui    = (opcode == 7'h37);
    assign is_auipc  = (opcode == 7'h17);
    assign is_jal    = (opcode == 7'h6F);
    assign is_jalr   = (opcode == 7'h67);
endmodule

module SC_ImmGen(
    input  [31:0] inst,
    output [31:0] immI, immS, immB, immU, immJ
);
    assign immI = {{20{inst[31]}}, inst[31:20]};
    assign immS = {{20{inst[31]}}, inst[31:25], inst[11:7]};
    assign immB = {{20{inst[31]}}, inst[7], inst[30:25], inst[11:8], 1'b0};
    assign immU = {inst[31:12], 12'b0};
    assign immJ = {{12{inst[31]}}, inst[19:12], inst[20], inst[30:21], 1'b0};
endmodule

module SC_RegFile(
    input         clk,
    input         we,
    input  [4:0]  rs1, rs2, rd, dbg_reg_addr,
    input  [31:0] wdata,
    output [31:0] rdata1, rdata2, dbg_reg_data
);
    reg [31:0] regs [0:31];
    integer i;
    initial begin
        for (i=0; i<32; i=i+1) regs[i] = 0;
        regs[2] = 32'h00008000;
    end
    
    assign rdata1 = (rs1 == 0) ? 32'd0 : regs[rs1];
    assign rdata2 = (rs2 == 0) ? 32'd0 : regs[rs2];
    assign dbg_reg_data = (dbg_reg_addr == 0) ? 32'd0 : regs[dbg_reg_addr];
    
    always @(posedge clk) begin
        if (we && rd != 0) begin
            regs[rd] <= wdata;
        end
    end
endmodule

module SC_ALU_Ctrl(
    input        is_r, is_i, is_load, is_store, is_lui, is_auipc, is_jal, is_jalr,
    input  [2:0] funct3,
    input        funct7_5,
    output reg [3:0] alu_op
);
    always @(*) begin
        if (is_lui) alu_op = 4'b1111;
        else if (is_jal | is_jalr) alu_op = 4'b0000;
        else if (is_load | is_store | is_auipc) alu_op = 4'b0000; 
        else begin
            case (funct3)
                3'b000: alu_op = (is_r && funct7_5) ? 4'b1000 : 4'b0000; // SUB / ADD
                3'b001: alu_op = 4'b0001; // SLL
                3'b010: alu_op = 4'b0010; // SLT
                3'b011: alu_op = 4'b0011; // SLTU
                3'b100: alu_op = 4'b0100; // XOR
                3'b101: alu_op = funct7_5 ? 4'b1101 : 4'b0101; // SRA / SRL
                3'b110: alu_op = 4'b0110; // OR
                3'b111: alu_op = 4'b0111; // AND
            endcase
        end
    end
endmodule

module SC_ALU(
    input  [31:0] A, B,
    input  [3:0]  alu_op,
    output reg [31:0] res
);
    wire [31:0] sra_mask = ~(32'hFFFFFFFF >> B[4:0]);
    wire [31:0] alu_sra  = (A >> B[4:0]) | (A[31] ? sra_mask : 32'd0);

    always @(*) begin
        case(alu_op)
            4'b0000: res = A + B;
            4'b1000: res = A - B;
            4'b0001: res = A << B[4:0];
            4'b0010: res = ($signed(A) < $signed(B)) ? 32'd1 : 32'd0;
            4'b0011: res = (A < B) ? 32'd1 : 32'd0;
            4'b0100: res = A ^ B;
            4'b0101: res = A >> B[4:0];
            4'b1101: res = alu_sra;
            4'b0110: res = A | B;
            4'b0111: res = A & B;
            4'b1111: res = B;
            default: res = A + B;
        endcase
    end
endmodule

module SC_BranchUnit(
    input  [31:0] rdata1, rdata2,
    input  [2:0]  funct3,
    input         is_branch, is_jal, is_jalr,
    output reg    branch_taken
);
    always @(*) begin
        branch_taken = 0;
        if (is_jal | is_jalr) branch_taken = 1;
        else if (is_branch) begin
            case (funct3)
                3'b000: branch_taken = (rdata1 == rdata2);
                3'b001: branch_taken = (rdata1 != rdata2);
                3'b100: branch_taken = ($signed(rdata1) < $signed(rdata2));
                3'b101: branch_taken = ($signed(rdata1) >= $signed(rdata2));
                3'b110: branch_taken = (rdata1 < rdata2);
                3'b111: branch_taken = (rdata1 >= rdata2);
                default: branch_taken = 0;
            endcase
        end
    end
endmodule

module SC_LSU_Ctrl(
    input         is_load, is_store, run,
    input  [2:0]  funct3,
    input  [31:0] alu_out, rdata2, dmem_rdata,
    output        dmem_en,
    output [3:0]  dmem_we,
    output [31:0] dmem_wdata,
    output [31:0] load_data
);
    assign dmem_en = (is_load | is_store) & run;
    
    assign dmem_wdata = (funct3==3'b000) ? {4{rdata2[7:0]}} : 
                        (funct3==3'b001) ? {2{rdata2[15:0]}} : rdata2;
                        
    assign dmem_we = (is_store & run) ? 
                     ((funct3==3'b000) ? (4'b0001 << alu_out[1:0]) : 
                      (funct3==3'b001) ? (4'b0011 << (alu_out[1:0]&2'b10)) : 
                      4'b1111) : 4'b0000;
                      
    wire [31:0] shifted_rd = dmem_rdata >> (8 * alu_out[1:0]);
    assign load_data = (funct3==3'b000) ? {{24{shifted_rd[7]}}, shifted_rd[7:0]} : 
                       (funct3==3'b100) ? {24'd0, shifted_rd[7:0]} :
                       (funct3==3'b001) ? {{16{shifted_rd[15]}}, shifted_rd[15:0]} : 
                       (funct3==3'b101) ? {16'd0, shifted_rd[15:0]} : shifted_rd;
endmodule

module SingleCycleCPU (
    input         clk, rst, run,
    input  [31:0] inst_rdata, dmem_rdata,
    output [31:0] inst_addr, dmem_addr, dmem_wdata,
    output [3:0]  dmem_we,
    output        dmem_en,
    input  [4:0]  dbg_reg_addr,
    output [31:0] dbg_reg_data, dbg_pc
);
    reg [31:0] pc;
    wire [31:0] inst = inst_rdata;
    assign inst_addr = pc;
    assign dbg_pc = pc;

    wire [6:0] opcode = inst[6:0];
    wire [2:0] funct3 = inst[14:12];
    wire [6:0] funct7 = inst[31:25];
    wire [4:0] rs1 = inst[19:15], rs2 = inst[24:20], rd = inst[11:7];

    wire is_r, is_i, is_load, is_store, is_branch, is_lui, is_auipc, is_jal, is_jalr;
    SC_Control u_ctrl (
        .opcode(opcode),
        .is_r(is_r), .is_i(is_i), .is_load(is_load), .is_store(is_store), 
        .is_branch(is_branch), .is_lui(is_lui), .is_auipc(is_auipc), 
        .is_jal(is_jal), .is_jalr(is_jalr)
    );

    wire [31:0] immI, immS, immB, immU, immJ;
    SC_ImmGen u_immgen (
        .inst(inst),
        .immI(immI), .immS(immS), .immB(immB), .immU(immU), .immJ(immJ)
    );

    wire reg_we = (is_r | is_i | is_load | is_lui | is_auipc | is_jal | is_jalr) & run & (rd != 0);
    wire [31:0] wb_data;
    wire [31:0] rdata1, rdata2;
    SC_RegFile u_regfile (
        .clk(clk), .we(reg_we), 
        .rs1(rs1), .rs2(rs2), .rd(rd), .dbg_reg_addr(dbg_reg_addr),
        .wdata(wb_data), 
        .rdata1(rdata1), .rdata2(rdata2), .dbg_reg_data(dbg_reg_data)
    );

    wire [3:0] alu_op;
    SC_ALU_Ctrl u_alu_ctrl (
        .is_r(is_r), .is_i(is_i), .is_load(is_load), .is_store(is_store), 
        .is_lui(is_lui), .is_auipc(is_auipc), .is_jal(is_jal), .is_jalr(is_jalr),
        .funct3(funct3), .funct7_5(funct7[5]),
        .alu_op(alu_op)
    );

    wire [31:0] opA = (is_auipc | is_jal | is_branch) ? pc : rdata1;
    wire [31:0] opB = (is_r) ? rdata2 : 
                      (is_store ? immS : 
                      (is_branch ? immB : 
                      (is_lui | is_auipc ? immU : 
                      (is_jal ? immJ : immI))));
    wire [31:0] alu_out;
    SC_ALU u_alu (
        .A(opA), .B(opB), .alu_op(alu_op), .res(alu_out)
    );

    wire branch_taken;
    SC_BranchUnit u_branch (
        .rdata1(rdata1), .rdata2(rdata2), .funct3(funct3),
        .is_branch(is_branch), .is_jal(is_jal), .is_jalr(is_jalr),
        .branch_taken(branch_taken)
    );

    wire [31:0] load_data;
    SC_LSU_Ctrl u_lsu (
        .is_load(is_load), .is_store(is_store), .run(run),
        .funct3(funct3), .alu_out(alu_out), .rdata2(rdata2), .dmem_rdata(dmem_rdata),
        .dmem_en(dmem_en), .dmem_we(dmem_we), .dmem_wdata(dmem_wdata), .load_data(load_data)
    );

    assign dmem_addr = alu_out;


    assign wb_data = is_load ? load_data : 
                     (is_jal | is_jalr) ? (pc + 4) : alu_out;


    wire [31:0] next_pc = branch_taken ? ((is_jalr) ? (rdata1 + immI)&~32'h1 : (pc + opB)) : (pc + 4);


    always @(posedge clk) begin
        if (rst) pc <= 32'h0;
        else if (run) pc <= next_pc;
    end
endmodule