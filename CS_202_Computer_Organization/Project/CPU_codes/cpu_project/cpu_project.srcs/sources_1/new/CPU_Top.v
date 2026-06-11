`timescale 1ns / 1ps

module CPU_Top (
    input         clk, rst, run, mode_sel,
    input  [31:0] inst_rdata, dmem_rdata,
    output [31:0] inst_addr, dmem_addr, dmem_wdata,
    output [3:0]  dmem_we,
    output        dmem_en,
    input  [4:0]  dbg_reg_addr,
    output [31:0] dbg_reg_data, dbg_pc
);
    wire [31:0] sc_inst_addr, sc_dmem_addr, sc_dmem_wdata, sc_dbg_reg_data, sc_dbg_pc;
    wire [3:0]  sc_dmem_we;
    wire        sc_dmem_en;

    wire [31:0] pl_inst_addr, pl_dmem_addr, pl_dmem_wdata, pl_dbg_reg_data, pl_dbg_pc;
    wire [3:0]  pl_dmem_we;
    wire        pl_dmem_en;

    SingleCycleCPU u_sc(
        .clk(clk), .rst(rst | mode_sel), .run(run & ~mode_sel),
        .inst_rdata(inst_rdata), .inst_addr(sc_inst_addr),
        .dmem_rdata(dmem_rdata), .dmem_addr(sc_dmem_addr), .dmem_wdata(sc_dmem_wdata),
        .dmem_we(sc_dmem_we), .dmem_en(sc_dmem_en),
        .dbg_reg_addr(dbg_reg_addr), .dbg_reg_data(sc_dbg_reg_data), .dbg_pc(sc_dbg_pc)
    );

    PipelinedCPU u_pl(
        .clk(clk), .rst(rst | ~mode_sel), .run(run & mode_sel),
        .inst_rdata(inst_rdata), .inst_addr(pl_inst_addr),
        .dmem_rdata(dmem_rdata), .dmem_addr(pl_dmem_addr), .dmem_wdata(pl_dmem_wdata),
        .dmem_we(pl_dmem_we), .dmem_en(pl_dmem_en),
        .dbg_reg_addr(dbg_reg_addr), .dbg_reg_data(pl_dbg_reg_data), .dbg_pc(pl_dbg_pc)
    );

    assign inst_addr    = mode_sel ? pl_inst_addr    : sc_inst_addr;
    assign dmem_addr    = mode_sel ? pl_dmem_addr    : sc_dmem_addr;
    assign dmem_wdata   = mode_sel ? pl_dmem_wdata   : sc_dmem_wdata;
    assign dmem_we      = mode_sel ? pl_dmem_we      : sc_dmem_we;
    assign dmem_en      = mode_sel ? pl_dmem_en      : sc_dmem_en;
    assign dbg_reg_data = mode_sel ? pl_dbg_reg_data : sc_dbg_reg_data;
    assign dbg_pc       = mode_sel ? pl_dbg_pc       : sc_dbg_pc;

endmodule