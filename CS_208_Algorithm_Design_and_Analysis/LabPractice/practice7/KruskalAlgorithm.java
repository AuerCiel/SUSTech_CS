package practice7;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Scanner;


public class KruskalAlgorithm {
    static node_7[] nodes;
    static PriorityQueue<edge_7> edges;


    public static void main(String[] args) {
        //init();
        String a = "a";init(a);


        int answer = 0;




        //一个parent数组，维护每个node当前父类
        int[] parent = new int[nodes.length];
        for(int i=0;i<parent.length;i++){
            parent[i]=i;
        }


        //取出一条边，看看两个node所属set情况：
        //  1.都在同一个：跳过这条边
        //  2.不在同一个：合并两个集合
        while(!edges.isEmpty()){
            edge_7 cur_edge = edges.poll();
            int left_set = find(cur_edge.left.index,parent);
            int right_set = find(cur_edge.right.index,parent);

            //不一样就合并，且加上答案
            if(left_set!=right_set){
                answer = answer+cur_edge.weight;
                union(left_set,right_set,parent);
            }

        }

        System.out.println(answer);


    }

    public static int find(int i,int[] parent) {
        if (parent[i] == i) return i;
        return parent[i] = find(parent[i],parent); // 这一行就是灵魂！
    }

    public static void union(int left,int right,int[] parent){
        parent[left] = right;
    }



    //读取控制台输入
    public static void init(){
        Scanner scanner = new Scanner(System.in);

        int n = scanner.nextInt();
        int m = scanner.nextInt();

        nodes = new node_7[n];
        for(int i=0;i<n;i++){
            nodes[i] = new node_7();
            nodes[i].index = i;
        }

        edges =  new PriorityQueue<>(m);
        for(int i=0;i<m;i++){
            int left = scanner.nextInt();
            int right = scanner.nextInt();
            int weight = scanner.nextInt();

            edge_7 edge = new edge_7();
            edge.left = nodes[left];
            edge.right = nodes[right];
            edge.weight = weight;

            edges.add(edge);
        }

        scanner.close();
    }
    //读取文件
    public static void init(String pathname)  {
        pathname = "c:/Users/Akira/Desktop/CS_208_ass/src/practice7/1.in";

        //读取文件
        File file = new File(pathname);
        try  {
            FileReader in = new FileReader(file);
            BufferedReader br = new BufferedReader(in);

            //先读第一行
            String line1 = br.readLine();
            int[] nums = Arrays.stream(line1.split("\\s+")).mapToInt(Integer::parseInt).toArray();
            int n = nums[0];
            int m = nums[1];


            //初始化数组
            nodes = new node_7[n];
            for(int i=0;i<n;i++){
                nodes[i] = new node_7();
                nodes[i].index = i;
            }

            //读m个edge
            edges =  new PriorityQueue<>(m);
            for(int i=0;i<m;i++){
                String line = br.readLine();
                int[] edge_info = Arrays.stream(line.split("\\s+")).mapToInt(Integer::parseInt).toArray();

                //初始化这条边，加入优先队列
                edge_7 edge = new edge_7();
                edge.left = nodes[edge_info[0]];
                edge.right = nodes[edge_info[1]];
                edge.weight = edge_info[2];

                edges.add(edge);
            }

        }catch (Exception e){
            System.out.println("读取失败");
        }
    }
}




class node_7{
    int index = -1;
    int cur_master = -1;
}

class edge_7 implements Comparable<edge_7>{
    node_7 left;
    node_7 right;
    int weight = -1;
    int index = -1;

    @Override
    public int compareTo(edge_7 o) {
        return Integer.compare(this.weight, o.weight);
    }
}

