`timescale 1ns / 1ps

module InstructionMemory #(parameter MEM_SIZE = 32768) (
    input clk, we,
    input  [31:0] addr, wdata,
    output reg [31:0] rdata
);
    (* ram_style = "block" *) reg [31:0] mem [0:(MEM_SIZE/4)-1];
    initial $readmemh("batch_test.hex", mem);
    
    always @(posedge clk) begin
        if (we) mem[addr[14:2]] <= wdata;
        rdata <= mem[addr[14:2]];
    end
endmodule

module DataMemory #(parameter MEM_SIZE = 32768) (
    input clk,
    input  [3:0]  we,
    input  [31:0] addr, wdata,
    output reg [31:0] rdata
);
    (* ram_style = "block" *) reg [31:0] mem [0:(MEM_SIZE/4)-1];
    initial $readmemh("image_data.hex", mem);
    
    always @(posedge clk) begin
        if (we[0]) mem[addr[14:2]][7:0]   <= wdata[7:0];
        if (we[1]) mem[addr[14:2]][15:8]  <= wdata[15:8];
        if (we[2]) mem[addr[14:2]][23:16] <= wdata[23:16];
        if (we[3]) mem[addr[14:2]][31:24] <= wdata[31:24];
        rdata <= mem[addr[14:2]];
    end
endmodule