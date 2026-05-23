package OJ;

import java.util.Scanner;

class OJ_3{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 读取 n
        int n = sc.nextInt();

        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }

        //特殊情况——如果只有 1 天
        if (n == 1) {
            System.out.println(1);
            return;
        }


        int workDays = 1;      // 初始第 0 天必须上班
        int currentEnd = 0;    // 当前这一班次能支撑到的最远日期
        int farthest = 0;      // 在当前范围内，下一次上班能跳到的最远日期

        //预设第一步：从第 0 天开始
        currentEnd = nums[0];
        workDays++; // 既然 n > 1，且第 0 天不能直接到终点的话，至少需要第二次工作

        // 如果第 0 天就能直接覆盖到最后一天
        if (currentEnd >= n - 1) {
            System.out.println(2);
            return;
        }

        // 遍历数组寻找最少的跳跃次数
        // 注意：我们只需要遍历到 n-2 即可，因为我们要到达的是 n-1
        for (int i = 1; i < n - 1; i++) {
            // 更新当前能跳到的最远位置
            farthest = Math.max(farthest, i + nums[i]);

            // 如果到达了当前班次支撑的极限
            if (i == currentEnd) {
                workDays++;             // 必须再上一次班
                currentEnd = farthest;  // 下一次上班能撑到的新边界

                //如果新边界已经覆盖了最后一天，直接退出
                if (currentEnd >= n - 1) {
                    break;
                }
            }
        }

        System.out.println(workDays);

    }
}

