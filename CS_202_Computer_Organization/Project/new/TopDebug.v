`timescale 1ns / 1ps

module TopDebug (
    input         clk,         // 100MHz 
    input         rst_n,       // Active-low reset button
    input         uart_rxd,
    output        uart_txd,
    input  [7:0]  sw,
    input  [4:0]  btn,
    output [15:0] led,
    output [7:0]  seg_n,
    output [7:0]  seg_n1,
    output [7:0]  seg_an,
    inout  [15:0] sram_d,
    output [18:0] sram_a,
    output        sram_ce_n,
    output        sram_oe_n,
    output        sram_we_n,
    output        sram_ub_n,
    output        sram_lb_n,
    output [3:0]  vga_r,
    output [3:0]  vga_g,
    output [3:0]  vga_b,
    output        vga_hs,
    output        vga_vs,
    input         ps2_clk,
    input         ps2_data,
    output        audio_pwm,
    output        audio_sd
);
    reg [1:0] clk_div_cnt = 0;
    always @(posedge clk) begin
        clk_div_cnt <= clk_div_cnt + 1;
    end
    wire clk_25m = clk_div_cnt[1];
    wire clk_50m = clk_div_cnt[0];
    wire cpu_clk; // = (sw[0]) ? clk_50m : clk_25m;
    
    BUFGMUX u_clk_mux (
        .O(cpu_clk),
        .I0(clk_25m),
        .I1(clk_50m),
        .S(sw[0])
    );

    wire [7:0] rx_data, tx_data;
    wire rx_valid, tx_start, tx_busy;
    
    // connect to UART utilties

    UartRx uUartRx(.clk(clk), .rst_n(rst_n), .rx_pin(uart_rxd), .rx_data(rx_data), .rx_valid(rx_valid));
    UartTx uUartTx(.clk(clk), .rst_n(rst_n), .tx_data(tx_data), .tx_start(tx_start), .tx_busy(tx_busy), .tx_pin(uart_txd));

    wire cpu_halt, cpu_step, cpu_reset;
    wire [4:0] dbg_reg_addr;
    wire [31:0] dbg_reg_data, dbg_pc;
    wire inst_dbg_en, inst_wr_en, dmem_dbg_en, dmem_wr_en;
    wire [31:0] inst_dbg_addr, inst_wr_data, inst_rd_data;
    wire [31:0] dmem_dbg_addr, dmem_wr_data, dmem_rd_data;

    DebugController uDebugCtrl(
        .clk(clk), .rst_n(rst_n),
        .rx_data(rx_data), .rx_valid(rx_valid), .tx_data(tx_data), .tx_start(tx_start), .tx_busy(tx_busy),
        .cpu_halt(cpu_halt), .cpu_step(cpu_step), .cpu_reset(cpu_reset),
        .dbg_reg_addr(dbg_reg_addr), .dbg_reg_data(dbg_reg_data),
        .inst_dbg_en(inst_dbg_en), .inst_wr_en(inst_wr_en), .inst_dbg_addr(inst_dbg_addr), 
        .inst_wr_data(inst_wr_data), .inst_rd_data(inst_rd_data),
        .dmem_dbg_en(dmem_dbg_en), .dmem_wr_en(dmem_wr_en), .dmem_dbg_addr(dmem_dbg_addr), 
        .dmem_wr_data(dmem_wr_data), .dmem_rd_data(dmem_rd_data),
        .dbg_pc(dbg_pc)
    );

    reg [5:0] rst_stretch = 0;
    reg cpu_reset_ext = 0;
    always @(posedge clk) begin
        if (cpu_reset) rst_stretch <= 5'd31;
        else if (rst_stretch > 0) rst_stretch <= rst_stretch - 1;
        cpu_reset_ext <= (rst_stretch > 0);
    end // extend signal length

    reg rst_n_sync1 = 1, rst_n_sync2 = 1;
    reg rst_sync1 = 0, rst_sync2 = 0;
    reg halt_sync1 = 0, halt_sync2 = 0;
    reg step_sync1 = 0, step_sync2 = 0, step_sync3 = 0;

    always @(posedge cpu_clk) begin
        rst_n_sync1 <= rst_n;         rst_n_sync2 <= rst_n_sync1;
        rst_sync1   <= cpu_reset_ext; rst_sync2   <= rst_sync1;
        halt_sync1  <= cpu_halt;      halt_sync2  <= halt_sync1;
        step_sync1  <= cpu_step;      step_sync2  <= step_sync1; 
        step_sync3  <= step_sync2;
    end

    // wait 2 cycles to avoid bad data

    wire cpu_sys_rst = ~rst_n_sync2 | rst_sync2;
    wire cpu_run = !halt_sync2 | (step_sync2 & !step_sync3);

    // detect step signal posedge
    // step signal has been streched in DebugCtrl

    wire [31:0] cpu_inst_addr, cpu_dmem_addr, cpu_dmem_wdata, cpu_dmem_rdata;
    wire [3:0]  cpu_dmem_we;
    wire cpu_dmem_en;

    CPU_Top u_cpu (
        .clk(cpu_clk), .rst(cpu_sys_rst), .run(cpu_run), .mode_sel(sw[0]),
        .inst_rdata(inst_rd_data), .inst_addr(cpu_inst_addr),
        .dmem_rdata(cpu_dmem_rdata), .dmem_addr(cpu_dmem_addr),
        .dmem_wdata(cpu_dmem_wdata), .dmem_we(cpu_dmem_we), .dmem_en(cpu_dmem_en),
        .dbg_reg_addr(dbg_reg_addr), .dbg_reg_data(dbg_reg_data), .dbg_pc(dbg_pc)
    );
    
    wire [31:0] imem_addr = inst_dbg_en ? inst_dbg_addr : cpu_inst_addr;
    wire        imem_we   = inst_dbg_en ? inst_wr_en    : 1'b0;
    wire [31:0] imem_din  = inst_dbg_en ? inst_wr_data  : 32'd0;

    InstructionMemory #(.MEM_SIZE(32768)) u_imem(
        .clk(clk),
        .we(imem_we), .addr(imem_addr), .wdata(imem_din), .rdata(inst_rd_data)
    );
    wire is_mmio = (cpu_dmem_addr[31:28] == 4'h8) && cpu_dmem_en;
    wire is_kb_mmio = (cpu_dmem_addr[31:24] == 8'h60) && cpu_dmem_en;
    wire [31:0] kb_rdata;
    wire [3:0] actual_cpu_dmem_we = (is_mmio | is_kb_mmio) ? 4'b0000 : cpu_dmem_we;

    wire [31:0] dmem_addr = dmem_dbg_en ? dmem_dbg_addr : cpu_dmem_addr;
    wire [3:0]  dmem_we   = dmem_dbg_en ? {4{dmem_wr_en}} : actual_cpu_dmem_we;
    wire [31:0] dmem_din  = dmem_dbg_en ? dmem_wr_data  : cpu_dmem_wdata;
    wire [31:0] dmem_dout;
    wire is_sram_acc = (cpu_dmem_addr >= 32'h7000_0000 && cpu_dmem_addr < 32'h7020_0000) && cpu_dmem_en;
    wire is_audio_acc = (cpu_dmem_addr >= 32'h5000_0000 && cpu_dmem_addr < 32'h5000_0004) && cpu_dmem_en;
    wire [3:0] dmem_wena = dmem_we & {4{~is_sram_acc}} & {4{~is_audio_acc}};

    DataMemory #(.MEM_SIZE(32768)) u_dmem(
        .clk(clk),
        .we(dmem_wena), .addr(dmem_addr), .wdata(dmem_din), .rdata(dmem_dout)
    );

    reg [15:0] led_reg = 0;
    reg [31:0] seg_reg = 0;
    assign led = led_reg;
    
    reg [31:0] cpu_audio_freq = 0;
    reg cpu_audio_en = 0;

    always @(posedge cpu_clk) begin
        if (cpu_sys_rst) begin
            led_reg <= 0; seg_reg <= 0;
        end else if (cpu_run && is_mmio && (|cpu_dmem_we)) begin
            if (cpu_dmem_addr[7:0] == 8'h08) led_reg <= cpu_dmem_wdata[15:0];
            if (cpu_dmem_addr[7:0] == 8'h0C) seg_reg <= cpu_dmem_wdata;
        end
    end // MMIO LED and Segtube
    
    always @(posedge cpu_clk) begin
        if (cpu_sys_rst) begin
            cpu_audio_freq <= 0; cpu_audio_en <= 0;
        end else if (cpu_run && is_audio_acc && (|cpu_dmem_we)) begin
            if (cpu_dmem_addr[7:0] == 8'h00) cpu_audio_en <= cpu_dmem_wdata[0];
            if (cpu_dmem_addr[7:0] == 8'h04) cpu_audio_freq <= cpu_dmem_wdata;
        end
    end // MMIO audio control (write only)

    reg [31:0] mmio_rdata;
    always @(*) begin
        if (cpu_dmem_addr[7:0] == 8'h00) mmio_rdata = {25'd0, sw[7:1]}; // switch
        else if (cpu_dmem_addr[7:0] == 8'h04) mmio_rdata = {27'd0, btn}; // button status
        else if (cpu_dmem_addr[7:0] == 8'h08) mmio_rdata = {16'd0, led_reg};
        else if (cpu_dmem_addr[7:0] == 8'h0C) mmio_rdata = seg_reg;
        else mmio_rdata = 32'd0;
    end

    assign dmem_rd_data   = dmem_dout;
    assign seg_n1 = seg_n;

    MMIO_Display u_display(.clk(cpu_clk), .rst(cpu_sys_rst), .seg_data(seg_reg), .seg_n(seg_n), .seg_an(seg_an));

    reg [18:0] sram_a_reg;
    reg [15:0] sram_d_out;
    reg sram_we_n_reg = 1;
    reg sram_oe_n_reg = 1;

    assign sram_a    = sram_a_reg;
    assign sram_ce_n = 0;
    assign sram_ub_n = 0;
    assign sram_lb_n = 0;
    assign sram_we_n = sram_we_n_reg;
    assign sram_oe_n = sram_oe_n_reg;
    assign sram_d    = !sram_we_n_reg ? sram_d_out : 16'hZ;

    wire [18:0] vga_addr_next;
    reg  [15:0] vga_pixel_data;
    reg  [15:0] cpu_sram_rdata;

    always @(posedge clk) begin 
        case (clk_div_cnt)
            2'b11: begin 
                sram_we_n_reg <= 1;
                if (is_sram_acc && !(|cpu_dmem_we)) cpu_sram_rdata <= sram_d;
                
                sram_a_reg <= vga_addr_next; 
                sram_oe_n_reg <= 0;
            end
            
            2'b00: begin 

            end
            
            2'b01: begin 
                vga_pixel_data <= sram_d;
                
                sram_a_reg <= cpu_dmem_addr[20:2]; 
                if (is_sram_acc && (|cpu_dmem_we)) begin
                    sram_we_n_reg <= 0;
                    sram_d_out <= cpu_dmem_wdata[15:0]; 
                    sram_oe_n_reg <= 1;
                end else begin
                    sram_we_n_reg <= 1; 
                    sram_oe_n_reg <= 0;
                end
            end
            
            2'b10: begin 

            end
        endcase
    end
    
    wire vga_enab = (~sw[7]) | cpu_sys_rst;

    VGA_Ctrl u_vga(
        .clk_25m(clk_25m), .rst(vga_enab), .pixel_data(vga_pixel_data),
        .vga_addr_next(vga_addr_next), .h_sync(vga_hs), .v_sync(vga_vs),
        .vga_r(vga_r), .vga_g(vga_g), .vga_b(vga_b)
    );
    
    Keyboard_Ctrl u_kb_ctrl (
        .clk(clk),
        .rst(cpu_sys_rst),
        .ps2_clk(ps2_clk),
        .ps2_data(ps2_data),
        .cpu_addr(cpu_dmem_addr),
        .cpu_rdata(kb_rdata)
    );
    
    Audio_Ctrl u_au_ctrl (
        .clk(clk),
        .rst(cpu_sys_rst),
        .audio_en(cpu_audio_en),
        .audio_freq(cpu_audio_freq),
        .audio_pwm(audio_pwm)
    );

    assign cpu_dmem_rdata = is_sram_acc ? {16'd0, cpu_sram_rdata} : 
                            is_kb_mmio  ? kb_rdata :
                            is_mmio     ? mmio_rdata : dmem_dout;
    
    // 0x80000000 switch
    // 0x80000004 buttion
    // 0x80000008 led
    // 0x8000000C seg
    // 0x70000000 ~ vga
    // 0x60000000 ~ keyboard
    // 0x50000000 0x50000004 audio_en, audio_freq

endmodule