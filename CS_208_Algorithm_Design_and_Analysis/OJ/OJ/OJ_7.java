package OJ;

import java.util.*;
public class OJ_7 {

    static final int BASE = 1000; // 压缩：3位一组


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int a = in.nextInt();
        int b = in.nextInt();
        int c = in.nextInt();

        // 小根堆：按长度排序
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x.length));

        //计算 P(i) 并转为大整数
        for (int i = 1; i <= n; i++) {
            long val = (long) a * i * i + (long) b * i + c;
            pq.add(toArray(val));
        }

        //最优顺序合并
        while (pq.size() > 1) {
            int[] A = pq.poll();
            int[] B = pq.poll();
            pq.add(karatsuba(A, B));
        }

        //输出
        int[] res = pq.poll();
        if (res != null) {
            System.out.print(res[res.length - 1]);
            for (int i = res.length - 2; i >= 0; i--) {
                System.out.printf("%03d", res[i]); // BASE=1000
            }
        }
    }


    //Karatsuba
    //递归入口
    //计算公式：
    //x = a * BASE^m + b
    //y = c * BASE^m + d
    //
    //其中：
    //- a, b 是 x 的高位和低位
    //- c, d 是 y 的高位和低位
    //- m 是分割位置（通常为长度的一半）
    //- BASE 是进制（例如 10、1000 等）
    //
    //步骤：
    //1. 计算三个子乘积：
    //   z0 = b * d
    //   z2 = a * c
    //   z1 = (a + b) * (c + d) - z0 - z2
    //2. 合并结果：
    //x * y = z2 * BASE^(2*m) + z1 * BASE^m + z0
    static int[] karatsuba(int[] A, int[] B) {
        return karatsuba(A, 0, A.length, B, 0, B.length);
    }
    //从这里开始真正拆分上下层，然后递归
    //递归的每一层，返回上一层的都是一个新数组
    static int[] karatsuba(int[] A, int aStart, int aLen,
                           int[] B, int bStart, int bLen) {

        //小规模直接普通乘法
        if (Math.min(aLen, bLen) < 32) {
            return schoolMul(A, aStart, aLen, B, bStart, bLen);
        }

        int half = Math.min(aLen, bLen) / 2;

        //大规模就递归
        //拆分（不复制，用区间）————计算几个乘法的结果
        int[] z0 = karatsuba(A, aStart, half, B, bStart, half);
        int[] z2 = karatsuba(A, aStart + half, aLen - half,
                B, bStart + half, bLen - half);

        int[] aSum = add(A, aStart, half, A, aStart + half, aLen - half);
        int[] bSum = add(B, bStart, half, B, bStart + half, bLen - half);

        int[] z1 = karatsuba(aSum, 0, aSum.length, bSum, 0, bSum.length);
        z1 = sub(sub(z1, z0), z2);

        //合并
        int[] res = new int[aLen + bLen];
        addTo(res, z0, 0);
        addTo(res, z1, half);
        addTo(res, z2, 2 * half);

        return trim(res);
    }

    //简单乘法————暴力相乘就可以了
    static int[] schoolMul(int[] A, int aStart, int aLen,
                           int[] B, int bStart, int bLen) {

        //乘法结果最大可能就是两个长度和
        int[] res = new int[aLen + bLen];

        //a的每一位，和b相乘
        for (int i = 0; i < aLen; i++) {
            //取出a的一位，从最小位开始
            long carry = 0;
            //和b逐位相乘
            for (int j = 0; j < bLen; j++) {
                long cur = res[i + j]
                        + (long) A[aStart + i] * B[bStart + j]
                        + carry;
                res[i + j] = (int) (cur % BASE);
                carry = cur / BASE;
            }
            int pos = i + bLen;
            while (carry > 0) {
                long cur = res[pos] + carry;
                res[pos] = (int) (cur % BASE);
                carry = cur / BASE;
                pos++;
            }
        }
        return trim(res);
    }

    static int[] add(int[] A, int aStart, int aLen,
                     int[] B, int bStart, int bLen) {

        int len = Math.max(aLen, bLen);
        int[] res = new int[len + 1];

        int carry = 0;
        for (int i = 0; i < len; i++) {
            //处理加数，高位补0
            int x = i < aLen ? A[aStart + i] : 0;
            int y = i < bLen ? B[bStart + i] : 0;
            int sum = x + y + carry;
            res[i] = sum % BASE;
            carry = sum / BASE;
        }
        //剩余进位
        if (carry > 0) res[len] = carry;
        return trim(res);
    }
    //必须保证A大于B
    static int[] sub(int[] A, int[] B) {
        int[] res = new int[A.length];
        int borrow = 0;

        for (int i = 0; i < A.length; i++) {
            int x = A[i] - borrow;
            int y = i < B.length ? B[i] : 0;//如果B已经没有位数了，直接当成0处理就可以了
            if (x < y) {
                x += BASE;
                borrow = 1;
            } else borrow = 0;
            res[i] = x - y;
        }
        return trim(res);
    }

    //移位再相加
    static void addTo(int[] res, int[] A, int offset) {
        int carry = 0;
        int i = 0;

        // 先处理数组部分
        for (; i < A.length; i++) {
            long cur = res[i + offset] + A[i] + carry;
            res[i + offset] = (int) (cur % BASE);
            carry = (int) (cur / BASE);
        }

        // 再处理剩余进位
        while (carry > 0) {
            long cur = res[i + offset] + carry;
            res[i + offset] = (int) (cur % BASE);
            carry = (int) (cur / BASE);
            i++;
        }
    }

    //工具方法如下：
    static int[] trim(int[] a) {
        int i = a.length - 1;
        while (i > 0 && a[i] == 0) i--;
        // 如果本来就没多余0，直接返回原数组
        if (i == a.length - 1) return a;
        return Arrays.copyOf(a, i + 1);
    }

    //把一个long类型变成数组，也就6n操作
    static int[] toArray(long v) {
        if (v == 0) return new int[]{0};

        int[] tmp = new int[10]; // long最多 ~10^18，BASE=1000，最多6位，这里留冗余
        int idx = 0;

        while (v > 0) {
            tmp[idx++] = (int) (v % BASE);
            v /= BASE;
        }

        return Arrays.copyOf(tmp, idx);
    }
}



