package OJ;

import java.util.Scanner;

public class OJ_9 {
    static long[][] dp;
    static node_9[] nodes;
    static boolean[][] calculated;
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        //初始化头尾节点
        nodes = new node_9[n+2];
        node_9 head = new node_9();
        node_9 tail = new node_9();
        head.index = 0;
        tail.index = n+1;
        head.value = 1;
        tail.value = 1;
        nodes[0] = head;
        nodes[n+1] = tail;



        //读入链表
        for(int i=1;i<=n;i++){
            long value = in.nextInt();
            node_9 node = new node_9();
            node.value = value;
            node.index = i;
            nodes[i] = node;
        }

        //定义dp【l】【r】为开区间（l，r）内删掉node的最大收益。不包括第l，r号node
        dp = new long[n+2][n+2];
        calculated = new boolean[n+2][n+2];

        
        cal_dp(0,n+1);
        System.out.println(dp[0][n+1]);

    }
    public static void cal_dp(int left,int right){
        //base case:左右内恰好有一个
        node_9 left_node = nodes[left];
        node_9 right_node = nodes[right];
        if(left+1 == right){
            dp[left][right] = 0;
            calculated[left][right] = true;
            return;
        }

        //一般情况；
        long max = -1;
        for(int i=left+1;i<right;i++){
            //先计算左半边：
            //没有值就进入递归计算
            if(!calculated[left][i]){
                cal_dp(left,i);
            }
            if (!calculated[i][right]){
                cal_dp(i,right);
            }

            long cur = left_node.value*right_node.value*nodes[i].value;
            long cur_max = cur+dp[i][right]+dp[left][i];
            if(max<cur_max){
                max = cur_max;
            }
        }

        dp[left][right] = max;
        calculated[left][right] = true;
    }

}

class node_9 {
    long index;
    long value;
}

