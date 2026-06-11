.text
.globl _start

_start:
    li t0, 1          # t0 = 1
    # 连续产生 RAW (Read-After-Write) 数据冒险
    add t1, t0, t0    # t1 依赖 t0，t1 = 2 (从 EX/MEM 旁路转发)
    add t2, t1, t1    # t2 依赖 t1，t2 = 4 (从 EX/MEM 旁路转发)
    add t3, t2, t1    # t3 依赖 t2 和 t1，t3 = 4 + 2 = 6 (同时从 MEM/WB 和 EX/MEM 转发)
    
wait_here:
    j wait_here