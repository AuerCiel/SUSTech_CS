.text
.globl _start

_start:
    # ----------------------------------------------------
    # 1. 初始环境配置
    # ----------------------------------------------------
    lui s0, 0x70000          # s0 = VRAM_BASE (0x7000_0000)
    
    li  s1, 10               # s1 = 初始坐标 X
    li  s2, 10               # s2 = 初始坐标 Y
    li  s3, 2                # s3 = X轴速度 dX (每次移动2像素)
    li  s4, 2                # s4 = Y轴速度 dY (每次移动2像素)
    
    li  s5, 0                # s5 = 背景色 (纯黑 0x0000)
    addi s6, zero, 0x7E0     # s6 = 前景色 (纯绿 0x07E0)

    # ----------------------------------------------------
    # 2. 初始全屏清屏 (画黑色背景)
    # ----------------------------------------------------
    lui t0, 0x4B             # 320*240*4 = 307200 = 0x4B000 字节
    add t0, s0, t0           # t0 = 显存结束地址
    add a0, s0, zero         # a0 = 起始地址
clear_screen:
    sw  s5, 0(a0)
    addi a0, a0, 4
    blt a0, t0, clear_screen

    # ----------------------------------------------------
    # 3. 核心动画主循环
    # ----------------------------------------------------
main_loop:
    # --- 步骤 A: 擦除旧方块 (在旧位置画背景色) ---
    add a0, zero, s1         # 参数1: x
    add a1, zero, s2         # 参数2: y
    add a2, zero, s5         # 参数3: 颜色 (黑)
    jal ra, draw_square      # 调用画方块函数

    # --- 步骤 B: 更新坐标 ---
    add s1, s1, s3           # X = X + dX
    add s2, s2, s4           # Y = Y + dY

    # --- 步骤 C: X轴边界碰撞检测 ---
    # 屏幕宽320, 方块宽20, 最大合法X = 300
    li  t0, 300
    blt s1, t0, check_x_low  # 如果 X < 300，检查下界
    li  s1, 300              # 越界修正 X = 300
    sub s3, zero, s3         # dX = -dX (反弹)
    j   check_y
check_x_low:
    bge s1, zero, check_y    # 如果 X >= 0，正常
    li  s1, 0                # 越界修正 X = 0
    sub s3, zero, s3         # dX = -dX (反弹)

    # --- 步骤 D: Y轴边界碰撞检测 ---
    # 屏幕高240, 方块高20, 最大合法Y = 220
check_y:
    li  t0, 220
    blt s2, t0, check_y_low  # 如果 Y < 220，检查下界
    li  s2, 220              # 越界修正 Y = 220
    sub s4, zero, s4         # dY = -dY (反弹)
    j   draw_new
check_y_low:
    bge s2, zero, draw_new   # 如果 Y >= 0，正常
    li  s2, 0                # 越界修正 Y = 0
    sub s4, zero, s4         # dY = -dY (反弹)

    # --- 步骤 E: 在新坐标绘制方块 ---
draw_new:
    li t1, 0x8000000C
    slli t0, s1, 16
    or t0, t0, s2
    sw t0, 0(t1)
    add a0, zero, s1         # 参数1: x
    add a1, zero, s2         # 参数2: y
    add a2, zero, s6         # 参数3: 颜色 (绿)
    jal ra, draw_square

    # --- 步骤 F: 控制帧率的死循环延时 ---
    # 根据 50MHz 时钟估算，调整 t0 控制移动快慢
    li t0, 1000000
delay:
    addi t0, t0, -1
    bne  t0, zero, delay

    j main_loop              # 进入下一帧！


# ====================================================================
# 函数: draw_square
# 描述: 在 (x, y) 处画一个 20x20 的实心方块
# 传入参数: a0 = x, a1 = y, a2 = color
# 使用的临时寄存器: t1~t5
# ====================================================================
draw_square:
    li  t0, 20               # t0 = 外层行计数器 (高度 h = 20)
    
    # 核心算法：计算显存指针起点
    # addr = VRAM_BASE + (Y * 320 + X) * 4
    # 因为没有乘法器，我们用移位加法: Y * 320 = Y * 256 + Y * 64 = (Y<<8) + (Y<<6)
    slli t4, a1, 8           # t4 = Y * 256
    slli t5, a1, 6           # t5 = Y * 64
    add  t1, t4, t5          # t1 = Y * 320
    add  t1, t1, a0          # t1 = Y * 320 + X
    slli t1, t1, 2           # t1 = (Y * 320 + X) * 4 (转为字节偏移)
    add  t1, s0, t1          # t1 = 最终在 VRAM 中的起点绝对地址

ds_y_loop:
    li  t2, 20               # t2 = 内层列计数器 (宽度 w = 20)
    add t3, zero, t1         # t3 = 当前行正在渲染的指针

ds_x_loop:
    sw  a2, 0(t3)            # 把颜色写进显存
    addi t3, t3, 4           # 移动到右边一个像素
    addi t2, t2, -1          # w--
    bne  t2, zero, ds_x_loop # 画完一行了吗？
    
    # 画完一行，跳到下一行起点
    # 下一行起点 = 当前起点 + 320个像素 * 4字节 = t1 + 1280
    addi t1, t1, 1280
    addi t0, t0, -1          # h--
    bne  t0, zero, ds_y_loop # 画完 20 行了吗？
    
    # 函数返回
    jalr zero, 0(ra)