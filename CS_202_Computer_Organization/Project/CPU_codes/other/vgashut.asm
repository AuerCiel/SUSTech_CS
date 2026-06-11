.text
.globl _start

_start:
    lui gp, 0x70000
    lui t0, 0x12C
    add t0, gp, t0

    li t1, 0x00000000
    li t2, 0

fill_loop:
    sw t1, 0(gp)
    addi gp, gp, 4
    blt gp, t0, fill_loop
    li t5, 0
    li t6, 10000000

wait_loop:
    addi t5, t5, 1
    
    blt t5, t6, wait_loop
    
    addi t2, t2, 1
    andi t2, t2, 63
    
    li t1, 0
    or t1, t1, t2
    slli t4, t2, 6
    or t1, t1, t4
    slli t4, t2, 11
    or t1, t1, t4
    
    lui t3, 0x80000
    
    addi t3, t3, 12
    
    sw t1, 0(t3)
    
    add gp, x0, x0
    
    lui gp, 0x70000
    j fill_loop
