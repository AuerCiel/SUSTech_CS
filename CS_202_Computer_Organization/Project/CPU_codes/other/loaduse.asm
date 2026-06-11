.text
.globl _start

_start:
    lui gp, 0x4       # gp = 0x4000 (Data Memory)
    li t0, 0x66       # t0 = 0x66
    sw t0, 0(gp)      # 将 0x66 写入内存

    lw t1, 0(gp)      # t1 从内存加载数据
    # 下面这条指令依赖 t1，由于 t1 在 MEM 阶段才能读出，必须产生 Load-Use Stall
    add t2, t1, t0    # t2 = 0x66 + 0x66 = 0xCC 
    
wait_here:
    j wait_here