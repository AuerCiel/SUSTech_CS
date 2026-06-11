`timescale 1ns / 1ps

module VGA_Ctrl(
    input  wire clk_25m,
    input  wire rst,
    input  wire [15:0] pixel_data,
    output wire [18:0] vga_addr_next,
    output wire h_sync,
    output wire v_sync,
    output wire [3:0] vga_r,
    output wire [3:0] vga_g,
    output wire [3:0] vga_b
);

    localparam H_SYNC  = 96,  H_BACK  = 48, H_DISP  = 640, H_FRONT = 16, H_TOTAL = 800;
    localparam V_SYNC  = 2,   V_BACK  = 33, V_DISP  = 480, V_FRONT = 10, V_TOTAL = 525;

    reg [9:0] h_cnt = 0;
    reg [9:0] v_cnt = 0;

    always @(posedge clk_25m) begin
        if (rst) begin
            h_cnt <= 0; v_cnt <= 0;
        end else begin
            if (h_cnt == H_TOTAL - 1) begin
                h_cnt <= 0;
                if (v_cnt == V_TOTAL - 1) v_cnt <= 0;
                else v_cnt <= v_cnt + 1;
            end else begin
                h_cnt <= h_cnt + 1;
            end
        end
    end

    assign h_sync = (h_cnt < H_SYNC) ? 1'b0 : 1'b1;
    assign v_sync = (v_cnt < V_SYNC) ? 1'b0 : 1'b1;


    wire valid = (h_cnt >= H_SYNC + H_BACK) && (h_cnt < H_SYNC + H_BACK + H_DISP) &&
                 (v_cnt >= V_SYNC + V_BACK) && (v_cnt < V_SYNC + V_BACK + V_DISP);


    wire [9:0] next_h = (h_cnt == H_TOTAL - 1) ? 0 : h_cnt + 1;
    wire [9:0] next_v = (h_cnt == H_TOTAL - 1) ? ((v_cnt == V_TOTAL - 1) ? 0 : v_cnt + 1) : v_cnt;
    wire next_valid = (next_h >= H_SYNC + H_BACK) && (next_h < H_SYNC + H_BACK + H_DISP) &&
                      (next_v >= V_SYNC + V_BACK) && (next_v < V_SYNC + V_BACK + V_DISP);
    
    wire [9:0] next_x = next_h - (H_SYNC + H_BACK);
    wire [9:0] next_y = next_v - (V_SYNC + V_BACK);
    
    wire [8:0] logical_x = next_x[9:1];
    wire [8:0] logical_y = next_y[9:1];
    

    assign vga_addr_next = next_valid ? (logical_y * 320 + logical_x) : 19'd0;


    assign vga_r = valid ? pixel_data[15:12] : 4'd0;
    assign vga_g = valid ? pixel_data[10:7]  : 4'd0;
    assign vga_b = valid ? pixel_data[4:1]   : 4'd0;

endmodule