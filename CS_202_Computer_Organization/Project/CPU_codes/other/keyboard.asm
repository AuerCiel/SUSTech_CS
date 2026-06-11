.text
.globl _start

_start:
    # ----------------------------------------------------
    # 初始化
    # ----------------------------------------------------
    lui s0, 0x70000          # s0 = VRAM 基址 (0x7000_0000)
    lui s7, 0x60000          # s7 = 键盘状态基址 (0x6000_0000)
    
    li  s1, 150              # s1 = 方块 X 坐标 (初始中心)
    li  s2, 110              # s2 = 方块 Y 坐标 (初始中心)
    
    li  s5, 0                # s5 = 黑色 (背景)
    addi s6, zero, 0x7E0     # s6 = 绿色 (方块)

main_loop:
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s5
    jal ra, draw_square

check_W:
    lw  t0, 0x74(s7)
    li s1, 150
    li s2, 90
    beq t0, zero, clear_W
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s6
    jal ra, draw_square
    j check_S

clear_W:
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s5
    jal ra, draw_square

check_S:
    lw  t0, 0x6C(s7)
    li s1, 150
    li s2, 110
    beq t0, zero, clear_S
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s6
    jal ra, draw_square
    j check_A

clear_S:
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s5
    jal ra, draw_square

check_A:
    lw  t0, 0x70(s7)
    li s1, 130
    li s2, 110
    beq t0, zero, clear_A
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s6
    jal ra, draw_square
    j check_D

clear_A:
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s5
    jal ra, draw_square

check_D:
    lw  t0, 0x8C(s7)
    li s1, 170
    li s2, 110
    beq t0, zero, clear_D
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s6
    jal ra, draw_square
    li t0, 1000000          
    j delay

clear_D:
    add a0, zero, s1
    add a1, zero, s2
    add a2, zero, s5
    jal ra, draw_square

    li t0, 1000000          
delay:
    addi t0, t0, -1
    bne  t0, zero, delay

    j main_loop

draw_square:
    li  t0, 20               # 高 20
    slli t4, a1, 8           # Y * 256
    slli t5, a1, 6           # Y * 64
    add  t1, t4, t5          # Y * 320
    add  t1, t1, a0          # Y * 320 + X
    slli t1, t1, 2           # 乘以 4 字节
    add  t1, s0, t1          # 加上 VRAM_BASE
ds_y_loop:
    li  t2, 20               # 宽 20
    add t3, zero, t1
ds_x_loop:
    sw  a2, 0(t3)
    addi t3, t3, 4
    addi t2, t2, -1
    bne  t2, zero, ds_x_loop
    addi t1, t1, 1280        # 下一行
    addi t0, t0, -1
    bne  t0, zero, ds_y_loop
    jalr zero, 0(ra)