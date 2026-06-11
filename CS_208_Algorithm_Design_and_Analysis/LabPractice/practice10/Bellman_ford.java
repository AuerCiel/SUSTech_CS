package practice10;

import java.util.Arrays;
import java.util.Scanner;

public class Bellman_ford {
    static int n;
    static int m;
    static long[] pre;//用来储存path的。比如pre【i】=j，就表示最终的最短路径，是从第j号node走到第i号node的
    static long[] dist;//dist[i]表示当前走到i号node的临时最小dist
    static edge[] edges;
   

    public static void main(String[] args) {
        init();

        //开始进行n-1轮循环
        for(int i=0;i<n-1;i++){
            boolean is = false;
            for(edge edge : edges){
                //如果起点当前还没有被到达，就不讨论。直接减枝条
                if(dist[edge.start]==Integer.MAX_VALUE){
                    continue;
                }
                //检查是否符合更新条件
                if(dist[edge.start]+edge.weight<dist[edge.end]){
                    dist[edge.end] = dist[edge.start]+edge.weight;
                    pre[edge.end] = edge.start;
                    is = true;
                }
            }
            if(!is){
                break;
            }
        }

        //检测是否有负环
        for(int i=0;i<1;i++){
            for(edge edge : edges){
                //如果起点当前还没有被到达，就不讨论。直接减枝条
                if(dist[edge.start]<Integer.MAX_VALUE){
                    //检查是否符合更新条件
                    if(dist[edge.start]+edge.weight<dist[edge.end]){
                        System.out.println(-1);
                        return;
                    }
                }
            }
        }

        //打印答案
        //输出 1 ~ n-1 的距离
        for (int i = 0; i < n; i++) {
            System.out.print(dist[i] + " ");
        }
        System.out.println();

        //输出 1 ~ n-1 的父节点
        for (int i = 0 ; i < n; i++) {
            System.out.print(pre[i] + " ");
        }
    }

    private static void init(){
        Scanner in = new Scanner(System.in);

        n = in.nextInt();
        m = in.nextInt();


        edges = new edge[m];
        for(int i=0;i<m;i++){
            edges[i] = new edge();
            edges[i].start = in.nextInt();
            edges[i].end = in.nextInt();
            edges[i].weight = in.nextInt();
        }


        //dist[i]的值，表示index为i的node，当前到达它的最短路径是多少
        //pre[i]的值，表示index为i的node，当前到达它的上一个node的index是多少
        //并且初始化
        pre = new long[n];
        dist = new long[n];
        Arrays.fill(pre,-1);
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[0] = 0;

    }

    private static void init1() {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("c:\\Users\\Akira\\Desktop\\CS_208_ass\\src\\practice10\\1.in"))) {
            String line = reader.readLine();
            if (line == null) return;
            java.util.StringTokenizer st = new java.util.StringTokenizer(line);

            n = Integer.parseInt(st.nextToken());
            m = Integer.parseInt(st.nextToken());

            edges = new edge[m];
            for (int i = 0; i < m; i++) {
                line = reader.readLine();
                if (line == null) break;
                st = new java.util.StringTokenizer(line);
                edges[i] = new edge();
                edges[i].start = Integer.parseInt(st.nextToken());
                edges[i].end = Integer.parseInt(st.nextToken());
                edges[i].weight = Integer.parseInt(st.nextToken());
            }

            pre = new long[n];
            dist = new long[n];
            Arrays.fill(pre, -1);
            Arrays.fill(dist, Integer.MAX_VALUE);
            dist[0] = 0;

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}


class edge{
    int start = -1;
    int end = -1;
    int weight = 0;
}
