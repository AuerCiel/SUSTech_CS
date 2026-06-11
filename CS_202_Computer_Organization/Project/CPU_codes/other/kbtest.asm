.text
    .globl _start

_start:
    # 1. 设置 MMIO 的基地址
    lui a0, 0x60000      # a0 = 0x60000000 (键盘 MMIO 基地址)
    lui a1, 0x80000      # a1 = 0x80000000 (外设 MMIO 基地址)

loop:
    # 2. 读取键盘的最后 4 个字节数据 (偏移量 0x400)
    lw t0, 0x400(a0)     # t0 = *0x60000400

    # 3. 将其写入到数码管的 MMIO 地址 (偏移量 0x00C)
    sw t0, 0x00C(a1)     # *0x8000000C = t0

    # 4. 无条件跳转，死循环
    j loop