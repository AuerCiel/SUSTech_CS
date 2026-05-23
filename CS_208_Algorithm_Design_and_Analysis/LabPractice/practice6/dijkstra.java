package practice6;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;


public class dijkstra {

    static int[] dist;
    static MinHeap min_heap;
    static node_6[] nodes;


    public static void main(String[] args) {

        //initialization();
        initialization("c:/Users/Akira/Desktop/CS_208_ass/src/practice6/1.in");


        //初始化heap,dist
        min_heap = new MinHeap(nodes.length);
        min_heap.add(nodes[0]);
        nodes[0].cur_cost=0;

        dist = new int[nodes.length];
        Arrays.fill(dist, -1);
        dist[0]=0;



        double a = System.currentTimeMillis();
        //核心算法：
        while (!min_heap.isEmpty()){
            node_6 parent = min_heap.poll();
            parent.is_settled = true;

            //处理node
            for(int i=0;i<parent.child.size();i++){
                node_6 child = parent.child.get(i);
                int cost_to_this_child = parent.cost_of_path.get(i);

                if(child.is_settled){
                    //跳过已经处理过的node
                    continue;
                }else if(dist[child.index]==-1){
                    //首次入队的node

                    dist[child.index] = parent.cur_cost+cost_to_this_child;
                    child.cur_cost = parent.cur_cost+cost_to_this_child;

                    min_heap.add(child);

                }else {
                    //非首次入队的node
                    if(parent.cur_cost+cost_to_this_child<child.cur_cost){
                        child.cur_cost = parent.cur_cost+cost_to_this_child;
                        dist[child.index] = parent.cur_cost+cost_to_this_child;
                        int index =  min_heap.getIndex(child);
                        min_heap.siftDown(index);
                        min_heap.siftUp(index);

                    }
                }

            }
        }
        //法二：可以不进行上浮和下调
        //懒删除：直接把更优的cur-cost放入堆。旧的肯定比这个更优的慢出来。
        //而且就算弹出来了旧的cur-cost，也可以访问node的is——settle信息。如果已经settle，那就不更改。空间最大n方
        //但是我们把node的连接信息储存到了node里面。所以如果直接复制一个node再放入堆，开销很大
        //所以我可以新建一个state。储存node可能的cost。一个属性是对应哪个node，一个属性是cur-cost。用这个类的对象建堆。

        //打印答案
        double b = System.currentTimeMillis();
        for(int i=0;i< dist.length;i++){

            //每二十个打印一次换行
            if(i%20==0){
                System.out.println();
            }

            //打印答案
            System.out.print(dist[i]+" ");
        }
        System.out.println();
        System.out.println(b-a);






    }


    //读取键盘输入的初始化
    public static void initialization(){
        Scanner in = new Scanner(System.in);


        int n = in.nextInt();
        int m = in.nextInt();



        //初始化所有node
        nodes = new node_6[n];
        for(int i=0;i<n;i++){
            node_6 node = new node_6();
            node.index = i;

            nodes[i] = node;
        }


        //读入edges
        for(int i=0;i<m;i++){
            node_6 a = nodes[in.nextInt()];
            node_6 b = nodes[in.nextInt()];
            int weight = in.nextInt();

            //connect each other
            a.child.add(b);
            a.cost_of_path.add(weight);

            b.child.add(a);
            b.cost_of_path.add(weight);
        }
    }
    //读入指定文件的输入
    public static void initialization(String filename){
        try{
            BufferedReader br = new BufferedReader(new java.io.FileReader(filename));

            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());



            nodes = new node_6[n];
            for(int i=0;i<n;i++){
                node_6 node = new node_6();
                node.index = i;
                nodes[i] = node;
            }

            for(int i=0;i<m;i++){
                st = new StringTokenizer(br.readLine());
                int a_idx = Integer.parseInt(st.nextToken());
                int b_idx = Integer.parseInt(st.nextToken());
                int weight = Integer.parseInt(st.nextToken());

                node_6 a = nodes[a_idx];
                node_6 b = nodes[b_idx];

                a.child.add(b);
                a.cost_of_path.add(weight);

                b.child.add(a);
                b.cost_of_path.add(weight);
            }

            br.close();
        }catch(IOException e){
            System.out.println("文件读取失败");
        }
    }






}



class node_6 implements Comparable<node_6>{
    int cur_cost  = 2147483647;
    int index = -1;
    ArrayList<node_6> child = new ArrayList<>();
    ArrayList<Integer> cost_of_path = new ArrayList<>();
    boolean is_settled = false;

    @Override
    public int compareTo(node_6 o) {
        return Integer.compare(this.cur_cost, o.cur_cost);
    }
}

class MinHeap {
    public node_6[] heap;    // 堆数组
    public int size;         // 当前元素个数
    public int[] pos;        // pos[index] = 该node在堆里的下标

    // 初始化指定大小的堆
    public MinHeap(int capacity) {
        heap = new node_6[capacity + 1]; // 堆从1开始
        pos = new int[capacity];         // 记录每个node的位置
        size = 0;

        // 初始位置都为 -1（表示不在堆里）
        for (int i = 0; i < capacity; i++)
            pos[i] = -1;
    }

    // 交换两个位置，并更新 pos 表
    public void swap(int i, int j) {
        node_6 a = heap[i];
        node_6 b = heap[j];

        heap[i] = b;
        heap[j] = a;

        // 更新位置表
        pos[a.index] = j;
        pos[b.index] = i;
    }

    // 上浮
    public void siftUp(int i) {
        while (i > 1) {
            int p = i / 2;
            if (heap[i].cur_cost < heap[p].cur_cost) {
                swap(i, p);
                i = p;
            } else break;
        }
    }

    // 下沉
    public void siftDown(int i) {
        while (true) {
            int left = i * 2;
            int right = i * 2 + 1;
            int min = i;

            if (left <= size && heap[left].cur_cost < heap[min].cur_cost) min = left;
            if (right <= size && heap[right].cur_cost < heap[min].cur_cost) min = right;

            if (min == i) break;
            swap(i, min);
            i = min;
        }
    }

    // 添加节点
    public void add(node_6 node) {
        size++;
        heap[size] = node;
        pos[node.index] = size;
        siftUp(size);
    }

    // 弹出最小节点
    public node_6 poll() {
        node_6 top = heap[1];
        swap(1, size);
        pos[top.index] = -1;
        size--;
        siftDown(1);
        return top;
    }

    // 获取这个 node 在堆里的下标
    public int getIndex(node_6 node) {
        return pos[node.index];
    }

    // 判断是否在堆里
    public boolean contains(node_6 node) {
        return pos[node.index] != -1;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
