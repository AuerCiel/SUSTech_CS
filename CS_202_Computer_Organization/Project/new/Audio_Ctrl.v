`timescale 1ns / 1ps


module Audio_Ctrl(
    input        clk,
    input        rst,
    input        audio_en,
    input [31:0] audio_freq,
    output       audio_pwm
);

    reg [31:0] counter;
    reg audio_out;

    always @(posedge clk or negedge rst) begin
        if (!rst) begin
            counter <= 0;
            audio_out <= 0;
        end else if (audio_freq == 0) begin
            audio_out <= 0;
        end else if (counter >= audio_freq) begin
            counter <= 0;
            audio_out <= ~audio_out;
        end else begin
            counter <= counter + 1;
        end
    end
    
    assign audio_pwm = audio_out & audio_en;

endmodule
