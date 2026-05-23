package OJ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class OJ_10 {
    static node_10[] nodes;
    public static void main(String[] args) {
        FastReader10 in = new FastReader10(System.in);
        int n = in.nextInt();//总材料个数
        int m = in.nextInt();//一开始有的材料数
        int k = in.nextInt();//反应数
        int target = in.nextInt();//目标合成物。

        //维护一个dist，表示合成到某一个材料的当前最小天数
        int[] dis = new int[n];
        Arrays.fill(dis, Integer.MAX_VALUE);

        //读入所有node
        nodes = new node_10[n];
        for(int i=0;i<n;i++){
            node_10 node = new node_10();
            node.index = i+1;
            node.pre_time = in.nextInt();
            nodes[i] = node;
        }

        //读入初始材料
        for (int i=0;i<m;i++){
            int index = in.nextInt();
            dis[index-1] = 0;
            nodes[i].available = true;
        }

        //开始读入反应,通过邻接表的形式储存reaction
        for(int i=0;i<k;i++){
            node_10 a = nodes[in.nextInt()-1];
            node_10 b = nodes[in.nextInt()-1];
            node_10 target_node = nodes[in.nextInt()-1];

            a.another_source.add(b);
            b.another_source.add(a);
            a.target_nodes.add(target_node);
            b.target_nodes.add(target_node);
        }

        Queue<node_10> queue = new ArrayDeque<>();
        boolean successfully_added = queue.addAll(Arrays.asList(nodes));

        while(!queue.isEmpty()){
            //取出队首node，然后开始松弛
            node_10 father = queue.poll();
            father.is_in_queue = false;

            //取出相关反应
            for(int i=0;i<father.target_nodes.size();i++){
                node_10 another = father.another_source.get(i);
                node_10 target_node = father.target_nodes.get(i);

                //尝试松弛：当且仅当两个元素，当前可用，才进行松弛
                if(dis[father.index-1]!=Integer.MAX_VALUE&&dis[another.index-1]!=Integer.MAX_VALUE){
                    int new_time = Math.max(dis[father.index-1],dis[another.index-1])+Math.max(father.pre_time, another.pre_time);
                    //比当前快，才更新dis数组。然后顺便把速度更快的target_node再次入队，松弛之后的
                    if(new_time<dis[target_node.index-1]){
                        dis[target_node.index-1] = new_time;
                        //防止重复入队，性能损耗
                        if(!target_node.is_in_queue){
                            queue.add(target_node);
                        }
                    }
                }
            }
        }


        System.out.println(dis[target-1]);
    }


}

class node_10{
    int index;
    int pre_time;
    boolean available = false;
    boolean is_in_queue = true;

    //一个ArrayList储存另一个source：
    ArrayList<node_10> another_source = new ArrayList<>();
    //一个ArrayList储存target
    ArrayList<node_10> target_nodes = new ArrayList<>();
}



class FastReader10 {
    BufferedReader br;
    StringTokenizer st;

    public FastReader10(InputStream is) {
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