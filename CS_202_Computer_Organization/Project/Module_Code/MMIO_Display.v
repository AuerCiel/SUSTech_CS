`timescale 1ns / 1ps

module MMIO_Display (
    input clk,          // 25MHz
    input rst,
    input [31:0] seg_data,
    output reg [7:0] seg_n, // A~G, DP
    output reg [7:0] seg_an // DN0~DN1
);

    reg [14:0] scan_cnt;
    always @(posedge clk) begin
        if (rst) scan_cnt <= 0;
        else scan_cnt <= scan_cnt + 1;
    end
    wire [2:0] scan_idx = scan_cnt[14:12];

    reg [3:0] hex_val;
    always @(*) begin
        case (scan_idx)
            3'd0: hex_val = seg_data[3:0];
            3'd1: hex_val = seg_data[7:4];
            3'd2: hex_val = seg_data[11:8];
            3'd3: hex_val = seg_data[15:12];
            3'd4: hex_val = seg_data[19:16];
            3'd5: hex_val = seg_data[23:20];
            3'd6: hex_val = seg_data[27:24];
            3'd7: hex_val = seg_data[31:28];
        endcase
    end

    always @(*) begin
        case (scan_idx)
            3'd7: seg_an = 8'b00000001;
            3'd6: seg_an = 8'b00000010;
            3'd5: seg_an = 8'b00000100;
            3'd4: seg_an = 8'b00001000;
            3'd3: seg_an = 8'b00010000;
            3'd2: seg_an = 8'b00100000;
            3'd1: seg_an = 8'b01000000;
            3'd0: seg_an = 8'b10000000;
        endcase
    end

    always @(*) begin
        case (hex_val)
            4'h0: seg_n = 8'b00111111;
            4'h1: seg_n = 8'b00000110;
            4'h2: seg_n = 8'b01011011;
            4'h3: seg_n = 8'b01001111;
            4'h4: seg_n = 8'b01100110;
            4'h5: seg_n = 8'b01101101;
            4'h6: seg_n = 8'b01111101;
            4'h7: seg_n = 8'b00000111;
            4'h8: seg_n = 8'b01111111;
            4'h9: seg_n = 8'b01101111;
            4'ha: seg_n = 8'b01110111;
            4'hb: seg_n = 8'b01111100;
            4'hc: seg_n = 8'b00111001;
            4'hd: seg_n = 8'b01011110;
            4'he: seg_n = 8'b01111001;
            4'hf: seg_n = 8'b01110001;
        endcase
    end
endmodule