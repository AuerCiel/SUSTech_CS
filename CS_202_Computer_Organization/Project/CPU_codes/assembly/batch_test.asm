.text
.globl _start

_start:
    lui s0, 0x70000
    li  s1, 128
    li  s2, 88
    li  s3, 0
    li  s5, 0
    lui t0, 0x4B
    add t0, s0, t0
    add a0, s0, zero
clear_screen:
    sw  s5, 0(a0)
    addi a0, a0, 4
    blt a0, t0, clear_screen
    slli t4, s2, 8
    slli t5, s2, 6
    add  t1, t4, t5
    add  t1, t1, s1
    slli t1, t1, 2
    add  t1, s0, t1
    li  t0, 64

draw_y_loop:
    li  t2, 64
    add t3, zero, t1

draw_x_loop:
    lw  a2, 0(s3)
    sw  a2, 0(t3)
    addi s3, s3, 4
    addi t3, t3, 4
    addi t2, t2, -1
    bne  t2, zero, draw_x_loop
    
    addi t1, t1, 1280
    addi t0, t0, -1
    bne  t0, zero, draw_y_loop
end:
    j start

start:
    lui gp, 0x4
    lw t0, 0(gp)
    lw t1, 4(gp)
    lw t2, 8(gp)
check_0:
    li t3, 0
    bne t0, t3, check_1
    and s3, t1, t2
    j end_test

check_1:
    li t3, 1
    bne t0, t3, check_2
    sll s3, t1, t2
    j end_test

check_2:
    li t3, 2
    bne t0, t3, check_3
    sra s3, t1, t2
    j end_test

check_3:
    li t3, 3
    bne t0, t3, check_4
    lui a0, 0x12345
    add s3, t1, a0
    j end_test

check_4:
    li t3, 4
    bne t0, t3, check_5
    jal a0, target_4
target_4:
    auipc a1, 0x12345
    sub s3, a1, a0
    add s3, s3, t1
    j end_test

check_5:
    li t3, 5
    bne t0, t3, check_6
    jal ra, func_5
    add s3, t1, t2
    j end_test
func_5:
    jalr zero, 0(ra)

check_6:
    li t3, 6
    bne t0, t3, check_7
    li s3, 1
    li a0, 1
    li a1, 0
    li a2, 1
    beq t1, zero, end_test
    beq t1, a2, end_test
fib_loop:
    add s3, a0, a1
    mv a1, a0
    mv a0, s3
    addi a2, a2, 1
    blt a2, t1, fib_loop
    j end_test

check_7:
    li t3, 7
    bne t0, t3, check_8
    li s3, 0
    li a0, 8
cnt_loop:
    andi a1, t1, 1
    add s3, s3, a1
    srli t1, t1, 1
    addi a0, a0, -1
    bne a0, zero, cnt_loop
    j end_test

check_8:
    li t3, 8
    bne t0, t3, check_9
    slli a0, t1, 17
    srli a0, a0, 27
    li a1, 0x3FF
    and a1, t1, a1
    li a2, 31
    beq a0, zero, exp_0
    beq a0, a2, exp_31
    li s3, 3
    j end_test
exp_0:
    beq a1, zero, is_0
    li s3, 4
    j end_test
is_0:
    li s3, 0
    j end_test
exp_31:
    beq a1, zero, is_inf
    li s3, 2
    j end_test
is_inf:
    li s3, 1
    j end_test

check_9:
    li t3, 9
    bne t0, t3, end_test
    srli a0, t1, 15
    slli a1, t1, 17
    srli a1, a1, 27
    li a2, 0x3FF
    and a2, t1, a2
    li a3, 0x400
    or a2, a2, a3
    li a3, 21
    sub a3, a3, a1
    srl a2, a2, a3
    beq a0, zero, is_pos
    sub a2, zero, a2
is_pos:
    li a3, 0xFF
    and s3, a2, a3
end_test:
    sw s3, 12(gp)
wait_here:
    j wait_here
