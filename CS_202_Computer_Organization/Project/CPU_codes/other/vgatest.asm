.text
.globl _start

_start:
    lui gp, 0x70000
    lui t0, 0x12C
    add t0, gp, t0

    li t1, 0x0000F800
    li t2, 0x0000FFE0

fill_loop:
    srli t4, t3, 12
    add t4, t4, t1
    sw t4, 0(gp)
    addi gp, gp, 4
    addi t3, t3, 1
    blt gp, t0, fill_loop
    li t5, 0
    li t6, 10000000

wait_loop:
    addi t5, t5, 1
    blt t5, t6, wait_loop
    xor t1, t1, t2
    lui gp, 0x70000
    li t3, 0
    j fill_loop
