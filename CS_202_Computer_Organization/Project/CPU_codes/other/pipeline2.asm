.text
.globl _start

_start:
    lui gp, 0x80000          # gp = 0x8000_0000
    li s1, 1                 # 初始化第一个灯

main_loop:
    sw s1, 8(gp)             # 更新底部 16 个小 LED
    sw s1, 12(gp)            # 更新顶部 8 个大数码管

    # 配置延时参数 (约 0.1 秒的周期数)
    # 循环次数 0x40000 (262,144)
    lui t0, 0x40             

delay_loop:
    # -----------------------------------------------------------------
    # 【高能预警】：极限 RAW 数据依赖连环计算，全靠 Forwarding 救场
    # -----------------------------------------------------------------
    addi t0, t0, -1          # t0 减 1
    add  t3, zero, t0        # t3 依赖 t0 (EX/MEM 前馈)
    xor  t4, t3, t0          # t4 依赖 t3 (EX/MEM 前馈) 和 t0 (MEM/WB 前馈)
    or   t5, t4, t3          # t5 依赖 t4 (EX/MEM 前馈) 和 t3 (MEM/WB 前馈)
    and  t6, t5, t0          # t6 依赖 t5 (EX/MEM 前馈)
    slli t3, t6, 1           # t3 依赖 t6 (EX/MEM 前馈)
    srli t3, t3, 1           # t3 依赖 t3 (EX/MEM 前馈)
    add  t4, t3, t3          # t4 双倍依赖 t3 (两路 EX/MEM 前馈)
    
    # 循环跳转判断
    bne  t0, zero, delay_loop 
    # -----------------------------------------------------------------

    # 点亮下一个灯
    slli s1, s1, 1           
    li t2, 0x10000           
    bne s1, t2, main_loop    
    
    # 如果跑完 16 个灯，重置并无限循环
    li s1, 1                 
    j main_loop