`timescale 1ns / 1ps

module PL_Control(
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

module PL_ImmGen(
    input  [31:0] inst,
    output [31:0] immI, immS, immB, immU, immJ
);
    assign immI = {{20{inst[31]}}, inst[31:20]};
    assign immS = {{20{inst[31]}}, inst[31:25], inst[11:7]};
    assign immB = {{20{inst[31]}}, inst[7], inst[30:25], inst[11:8], 1'b0};
    assign immU = {inst[31:12], 12'b0};
    assign immJ = {{12{inst[31]}}, inst[19:12], inst[20], inst[30:21], 1'b0};
endmodule

module PL_RegFile(
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
    
    assign rdata1 = (rs1 == 0) ? 32'd0 : ((we && rs1 == rd) ? wdata : regs[rs1]);
    assign rdata2 = (rs2 == 0) ? 32'd0 : ((we && rs2 == rd) ? wdata : regs[rs2]);
    assign dbg_reg_data = (dbg_reg_addr == 0) ? 32'd0 : regs[dbg_reg_addr];
    
    always @(posedge clk) begin
        if (we && rd != 0) begin
            regs[rd] <= wdata;
        end
    end
endmodule

module PL_ALU_Ctrl(
    input        is_r, is_i, is_load, is_store, is_lui, is_auipc, is_jal, is_jalr,
    input  [2:0] funct3,
    input        funct7_5,
    output reg [3:0] alu_op
);
    always @(*) begin
        if (is_lui) alu_op = 4'b1111; // PASS_B
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

module PL_ALU(
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

module PL_BranchUnit(
    input  [31:0] valA, valB,
    input  [2:0]  funct3,
    input         is_branch, is_jal, is_jalr,
    output reg    branch_taken
);
    always @(*) begin
        branch_taken = 0;
        if (is_jal | is_jalr) branch_taken = 1;
        else if (is_branch) begin
            case (funct3)
                3'b000: branch_taken = (valA == valB);
                3'b001: branch_taken = (valA != valB);
                3'b100: branch_taken = ($signed(valA) < $signed(valB));
                3'b101: branch_taken = ($signed(valA) >= $signed(valB));
                3'b110: branch_taken = (valA < valB);
                3'b111: branch_taken = (valA >= valB);
                default: branch_taken = 0;
            endcase
        end
    end
endmodule

module PL_LSU_Ctrl(
    input         mem_read, mem_write, run,
    input  [2:0]  funct3,
    input  [31:0] alu_out, r2_data, dmem_rdata,
    output        dmem_en,
    output [3:0]  dmem_we,
    output [31:0] dmem_wdata,
    output [31:0] load_data
);
    assign dmem_en = (mem_read | mem_write) & run;
    
    assign dmem_wdata = (funct3==3'b000) ? {4{r2_data[7:0]}} : 
                        (funct3==3'b001) ? {2{r2_data[15:0]}} : r2_data;
                        
    assign dmem_we = (mem_write & run) ? 
                     ((funct3==3'b000) ? (4'b0001 << alu_out[1:0]) : 
                      (funct3==3'b001) ? (4'b0011 << (alu_out[1:0]&2'b10)) : 
                      4'b1111) : 4'b0000;
                      
    wire [31:0] shifted_rd = dmem_rdata >> (8 * alu_out[1:0]);
    assign load_data = (funct3==3'b000) ? {{24{shifted_rd[7]}}, shifted_rd[7:0]} : 
                       (funct3==3'b100) ? {24'd0, shifted_rd[7:0]} :
                       (funct3==3'b001) ? {{16{shifted_rd[15]}}, shifted_rd[15:0]} : 
                       (funct3==3'b101) ? {16'd0, shifted_rd[15:0]} : shifted_rd;
endmodule

module PL_ForwardingUnit(
    input  [4:0] ID_EX_Rs1, ID_EX_Rs2,
    input  [4:0] EX_MEM_Rd, MEM_WB_Rd,
    input        EX_MEM_RegWrite, MEM_WB_RegWrite,
    output [1:0] forwardA, forwardB
);

    assign forwardA = (EX_MEM_RegWrite && EX_MEM_Rd != 0 && EX_MEM_Rd == ID_EX_Rs1) ? 2'b10 :
                      (MEM_WB_RegWrite && MEM_WB_Rd != 0 && MEM_WB_Rd == ID_EX_Rs1) ? 2'b01 : 2'b00;

    assign forwardB = (EX_MEM_RegWrite && EX_MEM_Rd != 0 && EX_MEM_Rd == ID_EX_Rs2) ? 2'b10 :
                      (MEM_WB_RegWrite && MEM_WB_Rd != 0 && MEM_WB_Rd == ID_EX_Rs2) ? 2'b01 : 2'b00;
endmodule

module PL_HazardUnit(
    input        ID_EX_MemRead,
    input  [4:0] ID_EX_Rd,
    input  [4:0] IF_ID_Rs1, IF_ID_Rs2,
    output       lw_stall
);

    assign lw_stall = ID_EX_MemRead && (ID_EX_Rd != 0) &&
                      ((ID_EX_Rd == IF_ID_Rs1) || (ID_EX_Rd == IF_ID_Rs2));
endmodule

module PipelinedCPU (
    input         clk, rst, run,
    input  [31:0] inst_rdata, dmem_rdata,
    output [31:0] inst_addr, dmem_addr, dmem_wdata,
    output [3:0]  dmem_we,
    output        dmem_en,
    input  [4:0]  dbg_reg_addr,
    output [31:0] dbg_reg_data, dbg_pc
);

    reg [31:0] pc;
    assign inst_addr = pc;
    assign dbg_pc = pc;

    reg [31:0] IF_ID_PC, IF_ID_Inst;
    
    reg [31:0] ID_EX_PC, ID_EX_R1, ID_EX_R2, ID_EX_Imm;
    reg [4:0]  ID_EX_Rs1, ID_EX_Rs2, ID_EX_Rd;
    reg [3:0]  ID_EX_ALUOp;
    reg [2:0]  ID_EX_Funct3;
    reg ID_EX_ALUSrc, ID_EX_MemRead, ID_EX_MemWrite, ID_EX_RegWrite, ID_EX_MemtoReg;
    reg ID_EX_IsBranch, ID_EX_IsJal, ID_EX_IsJalr, ID_EX_IsLui, ID_EX_IsAuipc, ID_EX_Funct7_5;

    reg [31:0] EX_MEM_ALUOut, EX_MEM_R2;
    reg [4:0]  EX_MEM_Rd;
    reg [2:0]  EX_MEM_Funct3;
    reg EX_MEM_MemRead, EX_MEM_MemWrite, EX_MEM_RegWrite, EX_MEM_MemtoReg;

    reg [31:0] MEM_WB_ALUOut, MEM_WB_MemData;
    reg [4:0]  MEM_WB_Rd;
    reg MEM_WB_RegWrite, MEM_WB_MemtoReg;

    wire lw_stall;
    wire branch_taken;
    wire [31:0] branch_target;
    wire [31:0] wb_data_fwd;

    always @(posedge clk) begin
        if (rst) pc <= 0;
        else if (run) begin
            if (branch_taken) pc <= branch_target;
            else if (!lw_stall) pc <= pc + 4;
        end
    end

    always @(posedge clk) begin
        if (rst || (run && branch_taken)) begin
            IF_ID_PC <= 0; IF_ID_Inst <= 32'h00000013; // NOP (addi x0, x0, 0)
        end else if (run && !lw_stall) begin
            IF_ID_PC <= pc; IF_ID_Inst <= inst_rdata;
        end
    end

    wire [6:0] opcode = IF_ID_Inst[6:0];
    wire [4:0] rs1 = IF_ID_Inst[19:15], rs2 = IF_ID_Inst[24:20], rd = IF_ID_Inst[11:7];

    wire is_r, is_i, is_ld, is_st, is_br, is_lui, is_auipc, is_jal, is_jalr;
    PL_Control u_ctrl (.opcode(opcode), .is_r(is_r), .is_i(is_i), .is_load(is_ld), .is_store(is_st),
                       .is_branch(is_br), .is_lui(is_lui), .is_auipc(is_auipc), .is_jal(is_jal), .is_jalr(is_jalr));

    wire [31:0] immI, immS, immB, immU, immJ;
    PL_ImmGen u_imm (.inst(IF_ID_Inst), .immI(immI), .immS(immS), .immB(immB), .immU(immU), .immJ(immJ));

    wire [31:0] imm = is_st ? immS : is_br ? immB : (is_lui|is_auipc) ? immU : is_jal ? immJ : immI;
    wire [31:0] rdata1, rdata2;
    

    PL_RegFile u_regfile (.clk(clk), .we(MEM_WB_RegWrite & run), .rs1(rs1), .rs2(rs2), .rd(MEM_WB_Rd),
                          .dbg_reg_addr(dbg_reg_addr), .wdata(wb_data_fwd), .rdata1(rdata1), .rdata2(rdata2),
                          .dbg_reg_data(dbg_reg_data));

    PL_HazardUnit u_hazard (.ID_EX_MemRead(ID_EX_MemRead), .ID_EX_Rd(ID_EX_Rd), .IF_ID_Rs1(rs1), .IF_ID_Rs2(rs2),
                            .lw_stall(lw_stall));

    always @(posedge clk) begin
        if (rst || (run && (lw_stall || branch_taken))) begin
            ID_EX_PC<=0; ID_EX_R1<=0; ID_EX_R2<=0; ID_EX_Imm<=0; ID_EX_Rs1<=0; ID_EX_Rs2<=0; ID_EX_Rd<=0;
            ID_EX_ALUOp<=0; ID_EX_Funct3<=0; ID_EX_ALUSrc<=0; ID_EX_MemRead<=0; ID_EX_MemWrite<=0;
            ID_EX_RegWrite<=0; ID_EX_MemtoReg<=0; ID_EX_IsBranch<=0; ID_EX_IsJal<=0; ID_EX_IsJalr<=0; 
            ID_EX_IsLui<=0; ID_EX_IsAuipc<=0; ID_EX_Funct7_5<=0;
        end else if (run) begin
            ID_EX_PC<=IF_ID_PC; ID_EX_R1<=rdata1; ID_EX_R2<=rdata2; ID_EX_Imm<=imm; 
            ID_EX_Rs1<=rs1; ID_EX_Rs2<=rs2; ID_EX_Rd<=rd; ID_EX_Funct3<=IF_ID_Inst[14:12]; ID_EX_Funct7_5<=IF_ID_Inst[30];
            
            ID_EX_ALUSrc   <= (is_i | is_ld | is_st | is_lui | is_auipc | is_jal | is_jalr);
            ID_EX_MemRead  <= is_ld; 
            ID_EX_MemWrite <= is_st; 
            ID_EX_RegWrite <= (is_r | is_i | is_ld | is_lui | is_auipc | is_jal | is_jalr);
            ID_EX_MemtoReg <= is_ld; 
            ID_EX_IsBranch <= is_br; 
            ID_EX_IsJal    <= is_jal; 
            ID_EX_IsJalr   <= is_jalr; 
            ID_EX_IsLui    <= is_lui; 
            ID_EX_IsAuipc  <= is_auipc;
            ID_EX_ALUOp    <= (is_r | is_i) ? 4'b0001 : 4'b0000;
        end
    end

    wire [1:0] forwardA, forwardB;
    PL_ForwardingUnit u_forward (.ID_EX_Rs1(ID_EX_Rs1), .ID_EX_Rs2(ID_EX_Rs2),
                                 .EX_MEM_Rd(EX_MEM_Rd), .MEM_WB_Rd(MEM_WB_Rd),
                                 .EX_MEM_RegWrite(EX_MEM_RegWrite), .MEM_WB_RegWrite(MEM_WB_RegWrite),
                                 .forwardA(forwardA), .forwardB(forwardB));

    wire [31:0] valA = (forwardA==2'b10) ? EX_MEM_ALUOut : (forwardA==2'b01) ? wb_data_fwd : ID_EX_R1;
    wire [31:0] valB = (forwardB==2'b10) ? EX_MEM_ALUOut : (forwardB==2'b01) ? wb_data_fwd : ID_EX_R2;

    wire [31:0] aluA = (ID_EX_IsAuipc | ID_EX_IsJal | ID_EX_IsBranch) ? ID_EX_PC : valA;
    wire [31:0] aluB = ID_EX_ALUSrc ? ID_EX_Imm : valB;

    wire [3:0] alu_op;
    PL_ALU_Ctrl u_alu_ctrl (.is_r(ID_EX_ALUOp==4'b0001 && !ID_EX_ALUSrc), .is_i(ID_EX_ALUOp==4'b0001 && ID_EX_ALUSrc), 
                            .is_load(ID_EX_MemRead), .is_store(ID_EX_MemWrite), 
                            .is_lui(ID_EX_IsLui), .is_auipc(ID_EX_IsAuipc), .is_jal(ID_EX_IsJal), .is_jalr(ID_EX_IsJalr),
                            .funct3(ID_EX_Funct3), .funct7_5(ID_EX_Funct7_5), .alu_op(alu_op));

    wire [31:0] alu_res_raw;
    PL_ALU u_alu (.A(aluA), .B(aluB), .alu_op(alu_op), .res(alu_res_raw));
    wire [31:0] alu_res = (ID_EX_IsJal | ID_EX_IsJalr) ? (ID_EX_PC + 4) : alu_res_raw;

    PL_BranchUnit u_bu (.valA(valA), .valB(valB), .funct3(ID_EX_Funct3),
                        .is_branch(ID_EX_IsBranch), .is_jal(ID_EX_IsJal), .is_jalr(ID_EX_IsJalr),
                        .branch_taken(branch_taken));

    assign branch_target = ID_EX_IsJalr ? ((valA + ID_EX_Imm) & ~32'h1) : (ID_EX_PC + ID_EX_Imm);

    always @(posedge clk) begin
        if (rst) begin
            EX_MEM_ALUOut<=0; EX_MEM_R2<=0; EX_MEM_Rd<=0; EX_MEM_Funct3<=0;
            EX_MEM_MemRead<=0; EX_MEM_MemWrite<=0; EX_MEM_RegWrite<=0; EX_MEM_MemtoReg<=0;
        end else if (run) begin
            EX_MEM_ALUOut<=alu_res; EX_MEM_R2<=valB; EX_MEM_Rd<=ID_EX_Rd; EX_MEM_Funct3<=ID_EX_Funct3;
            EX_MEM_MemRead<=ID_EX_MemRead; EX_MEM_MemWrite<=ID_EX_MemWrite; 
            EX_MEM_RegWrite<=ID_EX_RegWrite; EX_MEM_MemtoReg<=ID_EX_MemtoReg;
        end
    end

    wire [31:0] mem_rdata_fmt;
    PL_LSU_Ctrl u_lsu (.mem_read(EX_MEM_MemRead), .mem_write(EX_MEM_MemWrite), .run(run),
                       .funct3(EX_MEM_Funct3), .alu_out(EX_MEM_ALUOut), .r2_data(EX_MEM_R2), .dmem_rdata(dmem_rdata),
                       .dmem_en(dmem_en), .dmem_we(dmem_we), .dmem_wdata(dmem_wdata), .load_data(mem_rdata_fmt));

    assign dmem_addr = EX_MEM_ALUOut;

    always @(posedge clk) begin
        if (rst) begin
            MEM_WB_ALUOut<=0; MEM_WB_MemData<=0; MEM_WB_Rd<=0; MEM_WB_RegWrite<=0; MEM_WB_MemtoReg<=0;
        end else if (run) begin
            MEM_WB_ALUOut<=EX_MEM_ALUOut; MEM_WB_MemData<=mem_rdata_fmt; MEM_WB_Rd<=EX_MEM_Rd;
            MEM_WB_RegWrite<=EX_MEM_RegWrite; MEM_WB_MemtoReg<=EX_MEM_MemtoReg;
        end
    end

    assign wb_data_fwd = MEM_WB_MemtoReg ? MEM_WB_MemData : MEM_WB_ALUOut;

endmodule