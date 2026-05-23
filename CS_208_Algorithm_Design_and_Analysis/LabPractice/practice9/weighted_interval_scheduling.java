package practice9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class weighted_interval_scheduling {
    public static void main(String[] args) {
        FastReader1 in = new FastReader1(System.in);

        //储存所有工作
        int size = in.nextInt();
        ArrayList<job> jobs = new ArrayList<>(size);

        //读入工作
        for(int i=0;i<size;i++){
            job a = new job();
            a.start = in.nextInt();
            a.end = in.nextInt();
            a.weight = in.nextInt();

            jobs.add(a);
        }



        jobs.sort(null); // null = 使用自然排序（job实现的 Comparable）

        //找p【i】
        int[] p = new int[size];
        for (int i = 0; i < size; i++) {
            p[i] = findPredecessor(jobs, i);
        }

        //开始动态规划
        int[] dp = new int[size + 1];
        dp[0] = 0; // 边界条件

        //记录每一步选中的job
        boolean[] selected = new boolean[size + 1];

        for (int i = 1; i <= size; i++) {
            job curr = jobs.get(i - 1);
            int choose = curr.weight + dp[p[i - 1] + 1]; // 选当前工作
            int notChoose = dp[i - 1];                 // 不选当前工作

            if (choose > notChoose) {
                dp[i] = choose;
                selected[i] = true; //选了第 i 个工作
            } else {
                dp[i] = notChoose;
                selected[i] = false; //没选
            }

        }

        //回溯选中的job
        int i = size;
        while (i > 0) {
            if (selected[i]) {
                // 选了：标记这个 job，并跳到前驱
                jobs.get(i - 1).is = true;
                i = p[i - 1] + 1;
            } else {
                // 没选：看前一个
                i--;
            }
        }

        // 输出最终答案
        System.out.println(dp[size]);
        for(job a : jobs){
            if(a.is){
                System.out.println("("+a.start+"-"+a.end+")"+a.weight);
            }
        }


    }
    private static int findPredecessor(ArrayList<job> jobs, int i) {
        int target = jobs.get(i).start;
        int left = 0, right = i - 1;
        int res = -1;

        while (left <= right) {
            int mid = (left + right) / 2;
            if (jobs.get(mid).end <= target) {
                res = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return res;
    }
}

class job implements Comparable<job>{
    int weight;
    int start;
    int end;
    boolean is = false;

    @Override
    public int compareTo(job o) {
        return Integer.compare(this.end,o.end);
    }
}
class FastReader1 {
    BufferedReader br;
    StringTokenizer st;

    public FastReader1(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is));
    }

    public String next() {
        while (st == null || !st.hasMoreElements()) {
            try {
                String line = br.readLine();
                if (line == null) return null;
                st = new StringTokenizer(line);
            } catch (IOException e) {
                return null;
            }
        }
        return st.nextToken();
    }

    public int nextInt() {
        String s = next();
        if (s == null) return -1;
        return Integer.parseInt(s);
    }
}
