`timescale 1ns / 1ps

module Keyboard_Ctrl (
    input  wire        clk,
    input  wire        rst,
    input  wire        ps2_clk,
    input  wire        ps2_data,
    input  wire [31:0] cpu_addr,
    output wire [31:0] cpu_rdata
);

    reg [2:0] ps2_clk_sync;
    reg [1:0] ps2_data_sync;
    always @(posedge clk) begin
        if (rst) begin
            ps2_clk_sync <= 3'b111;
            ps2_data_sync <= 2'b11;
        end else begin
            ps2_clk_sync <= {ps2_clk_sync[1:0], ps2_clk};
            ps2_data_sync <= {ps2_data_sync[0], ps2_data};
        end
    end
    wire ps2_clk_neg = (ps2_clk_sync[2:1] == 2'b10);
    wire ps2_data_in = ps2_data_sync[1];


    reg [19:0] timeout_cnt;
    always @(posedge clk) begin
        if (rst || ps2_clk_neg) timeout_cnt <= 0;
        else if (timeout_cnt < 20'd600_000) timeout_cnt <= timeout_cnt + 1;
    end
    wire timeout = (timeout_cnt == 20'd500_000);

    reg [3:0] bit_cnt;
    reg [7:0] shift_reg;
    reg [7:0] rx_data;
    reg       rx_valid;

    always @(posedge clk) begin
        if (rst) begin
            bit_cnt  <= 0;
            rx_valid <= 0;
        end else begin
            rx_valid <= 0;
            if (timeout) bit_cnt <= 0;
            else if (ps2_clk_neg) begin
                if (bit_cnt == 0) begin
                    if (ps2_data_in == 0) bit_cnt <= bit_cnt + 1;
                end else if (bit_cnt < 9) begin
                    shift_reg[bit_cnt-1] <= ps2_data_in;
                    bit_cnt <= bit_cnt + 1;
                end else if (bit_cnt == 9) begin
                    bit_cnt <= bit_cnt + 1;
                end else if (bit_cnt == 10) begin
                    if (ps2_data_in == 1) begin
                        rx_data  <= shift_reg;
                        rx_valid <= 1;
                    end
                    bit_cnt <= 0;
                end
            end
        end
    end

    localparam S_IDLE    = 2'd0;
    localparam S_EXT     = 2'd1;
    localparam S_BREAK   = 2'd2;
    localparam S_EXT_BRK = 2'd3;

    reg [1:0] state;
    reg [255:0] key_state;

    always @(posedge clk) begin
        if (rst) begin
            state     <= S_IDLE;
            key_state <= 256'b0;
        end else begin
            if (timeout) state <= S_IDLE;
            else if (rx_valid) begin
                case (state)
                    S_IDLE: begin
                        if (rx_data == 8'hE0)      state <= S_EXT;
                        else if (rx_data == 8'hF0) state <= S_BREAK;
                        else                       key_state[rx_data] <= 1'b1;
                    end
                    S_EXT: begin
                        if (rx_data == 8'hF0)      state <= S_EXT_BRK;
                        else begin
                            key_state[rx_data] <= 1'b1;
                            state <= S_IDLE;
                        end
                    end
                    S_BREAK: begin
                        key_state[rx_data] <= 1'b0;
                        state <= S_IDLE;
                    end
                    S_EXT_BRK: begin
                        key_state[rx_data] <= 1'b0;
                        state <= S_IDLE;
                    end
                endcase
            end
        end
    end

    assign cpu_rdata = {31'd0, key_state[cpu_addr[9:2]]};

endmodule