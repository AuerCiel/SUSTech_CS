.text
.globl _start

_start:
    lui gp, 0x80000   # gp = 0x8000_0000 (MMIO LED 地址)
    li t0, 100000001         # 循环上限 N = 11 (因为我们用 blt 小于判断)
    li t1, 0          # sum = 0 (存放累加结果)
    li t2, 1          # i = 1 (计数器)
    sw t2, 8(gp)

loop:
    # 1. 数据冒险: t1 和 t2 都是上一轮刚更新的值
    add t1, t1, t2    # sum = sum + i
    
    # 2. 数据冒险: 依赖 t2
    addi t2, t2, 1    # i++
    
    # 3. 数据冒险 (依赖 t2) + 控制冒险 (Branch 跳转)
    blt t2, t0, loop  # 如果 i < 11，跳转回 loop
    
    # 当 i == 11 时退出循环，把 0x37(55) 输出到 LED
    sw t1, 12(gp)      # LED = 0x0037
    
wait_here:
    j wait_here       # 控制冒险: 无条件跳转死循环