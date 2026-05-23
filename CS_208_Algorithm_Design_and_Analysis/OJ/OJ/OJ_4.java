package OJ;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class OJ_4 {
    public static void main(String[] args) {
        FastReader in = new FastReader(System.in);
        int n = in.nextInt();   //总材料数
        int m = in.nextInt();   //总edge数

        //所有node的集合
        node_4[] nodes = new node_4[n];


        for(int i=0;i<n;i++){
            nodes[i] = new node_4();
            //读入每个材料的合成费用
            nodes[i].brewing_time= in.nextInt();
            nodes[i].node_index = i+1;
        }


        for(int i=0;i<n;i++){
            //读入每个材料为后续带来的额外费用
            nodes[i].caused_time= in.nextInt();
        }


        //读入m个edge
        for(int i=0;i<m;i++){
            node_4 parent = nodes[in.nextInt()-1];
            node_4 child = nodes[in.nextInt()-1];

            parent.next_materials.add(child);
            child.next_materials.add(parent);  //理论上来说，child是不需要回到parent的。因为这样子一定会导致负担和总时间都变得更大。state无效
        }

        //完成读入，算法初始化
        PriorityQueue<node_4_state> states = new PriorityQueue<>();
        node_4_state init = new node_4_state();
        init.node_index = 1; // 必须明确设置为 1
        states.add(init);
        nodes[0].accepted_states.add(new state_record(0,0));


        //答案
        long ans = Long.MAX_VALUE;

        while (!states.isEmpty()){
            node_4_state cur = states.poll();

            //如果达到了n，尝试更新ans
            if (cur.node_index == n) {
                if (cur.cur_total_time < ans) {
                    ans = cur.cur_total_time;
                }
                continue; // 到达终点后不需要再往后走邻居了
            }


            //状态转移.遍历这个state对应的node的邻居。然后尝试添加状态
            for(node_4 neighbor : nodes[cur.node_index-1].next_materials ){
                // 下一个状态的负担
                int next_burden = cur.cur_total_caused_time + neighbor.caused_time;
                // 耗时是：旧时间 + 邻居的基础酿造时间 + 之前累积的负担
                long next_total = cur.cur_total_time + neighbor.brewing_time + cur.cur_total_caused_time;

                //检查将要插入的状态是否合格
                boolean is = node_4.is_accepted(neighbor,next_burden,next_total);
                if(is){
                    //如果合格，新建state，然后加入record和states队列
                    node_4_state state = new node_4_state();
                    state.node_index=neighbor.node_index;
                    state.cur_total_time = next_total;
                    state.cur_total_caused_time = next_burden;
                    //入队
                    states.add(state);


                }
            }

        }

        System.out.println(ans);

    }

}

class node_4 {
    int node_index =1;
    int brewing_time = -1;      //合成本身需要的时间
    int caused_time = -1;       //合成它会给后面每一步增加的时间
    ArrayList<node_4> next_materials = new ArrayList<>();

    List<state_record> accepted_states = new ArrayList<>();//记录到达这个node的，不互相支配的状态

    //检查将要插入的state能不呢被接收
    public static boolean is_accepted(node_4 targetNode, int newS, long newTime) {
        List<state_record> records = targetNode.accepted_states;
        for (int i = 0; i < records.size(); i++) {
            state_record r = records.get(i);
            if (r.cur_total_caused_time_of_state <= newS && r.cur_total_time_of_state <= newTime) {
                return false;
            }
            if (newS <= r.cur_total_caused_time_of_state && newTime <= r.cur_total_time_of_state) {
                records.remove(i);
                i--;
            }
        }
        // 在这里添加，保证逻辑闭环
        records.add(new state_record(newS, newTime));
        return true;
    }
}

class node_4_state implements Comparable<node_4_state> {
    //用来记录可能的状态。
    //就是当前的合成到了哪一步？累计b是多少？累计总时间是多少？
    int node_index = 0;  //表示合成到了哪个材料
    int cur_total_caused_time =0;  //表示合成到了这个材料的当前状态的累计“负担”
    long cur_total_time =0; // 走到当前状态，总共用了多少时间

    @Override
    public int compareTo(node_4_state o) {
        return Long.compare(this.cur_total_time,o.cur_total_time);
    }
}

class state_record{
    //用来记录可以到达某一个节点的所有状态的性能。也就是负重和总花费。
    //如果想要新加入的state，负重和总开销，都比一个state大，就不可以加入heap
    int cur_total_caused_time_of_state;
    long cur_total_time_of_state;

    state_record(int a,long b){
        this.cur_total_caused_time_of_state=a;
        this.cur_total_time_of_state=b;

    }
}

