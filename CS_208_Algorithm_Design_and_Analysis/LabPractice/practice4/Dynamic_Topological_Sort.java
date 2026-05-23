package practice4;

import java.util.*;

//动态拓扑排序问题定义：
//如果某一个图变化频繁，经常加node或者减node，那么如何高效的维护这个图每次变化的拓扑排序？
//这里给出一个已有的DAG，讨论一下加入一条新边应该如何调整
public class Dynamic_Topological_Sort {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int original_nodes = in.nextInt();
        int original_edges = in.nextInt();

        //initialize the nodes
        node_d[] nodes = new node_d[original_nodes];
        for(int i=0;i<original_nodes;i++){
            nodes[i] = new node_d();
            nodes[i].index=i;
        }

        //read in the original edges and link the nodes
        for(int i=0;i<original_edges;i++){
            int parent = in.nextInt();
            int child = in.nextInt();

            node_d parent_node = nodes[parent];
            node_d child_node = nodes[child];

            //link them
            parent_node.children.add(child_node);
            child_node.parents.add(parent_node);
        }

        //edge to input
        edge edge_to_insert = new edge();
        edge_to_insert.parent = nodes[in.nextInt()];
        edge_to_insert.child = nodes[in.nextInt()];

        //read in original valid topological order
        ArrayList<node_d> legal_order = new ArrayList<>();
        for(int i=0;i<original_nodes;i++){
            legal_order.add(nodes[in.nextInt()]);
        }

        //换行一下，输出更美观
        System.out.println();

        //建立位置映射-------------我们希望知道，index为x的node，当前合法排序的第几个位置
        int[] pos = new int[original_nodes];
        for(int i=0;i<legal_order.size();i++){
            pos[legal_order.get(i).index] = i;
        }

        int u = edge_to_insert.parent.index;
        int v = edge_to_insert.child.index;

        // 如果已经合法，直接输出原排序，不执行下面代码。直接return
        if(pos[u] < pos[v]){
            System.out.println("B:");
            System.out.println("F:");
            System.out.println("I:");
            System.out.print("UPDATED ");
            for(node_d n: legal_order) System.out.print(n.index + " ");
            return;
        }


        //如果插入后，不合法，就开始执行调整.
        //计算 F
        Set<node_d> F = new LinkedHashSet<>(); //集合，不允许重复元素，并且保持插入顺序
        Queue<node_d> q = new LinkedList<>();
        q.add(edge_to_insert.child);
        F.add(edge_to_insert.child);

        //从child开始，它本应该成为father。所以要从他开始，找拓扑序在它后面的nodes
        while(!q.isEmpty()){
            node_d cur = q.poll();
            for(node_d nxt: cur.children){
                if(!F.contains(nxt) && pos[nxt.index] <= pos[u]){
                    //contain防止重复元素加入，并且位置在u之前。因为只讨论v，u之间这些受影响的nodes。只有这些nodes的拓扑序受影响了
                    F.add(nxt);
                    q.add(nxt);
                }
            }
        }

        //计算 B
        Set<node_d> B = new LinkedHashSet<>();
        q.clear();                          //不要忘了初始化一下队列。。。。。。这里卡了半天
        q.add(edge_to_insert.parent);
        B.add(edge_to_insert.parent);

        while(!q.isEmpty()){
            node_d cur = q.poll();
            for(node_d pre: cur.parents){
                if(!B.contains(pre) && pos[pre.index] >= pos[v]){
                    B.add(pre);
                    q.add(pre);
                }
            }
        }

        //输出 B 
        System.out.print("B:");
        for(node_d n: B) System.out.print(" " + n.index);
        System.out.println();

        //输出 F 
        System.out.print("F:");
        for(node_d n: F) System.out.print(" " + n.index);
        System.out.println();

        //判环 
        Set<node_d> inter = new HashSet<>(B);
        inter.retainAll(F);

        if(!inter.isEmpty()){   //成环就直接pass，不需要下面这些了
            System.out.println("I:");
            System.out.println("CYCLE");
            return;
        }

        // 计算 I ————————虽然在原排序里面处于【v，u】区间，但是和这两个节点没有连接的nodes。无关成员，放哪都不影响拓扑序
        ArrayList<node_d> I = new ArrayList<>();
        int left = pos[v];
        int right = pos[u];

        for(int i=left;i<=right;i++){
            node_d cur = legal_order.get(i);
            if(!B.contains(cur) && !F.contains(cur)){
                I.add(cur);
            }
        }

        //  输出 I 
        System.out.print("I:");
        for(node_d n: I) System.out.print(" " + n.index);
        System.out.println();

        //创建一个新的arraylist，存入新的排序
        ArrayList<node_d> newSegment = new ArrayList<>();

        for(int i=left;i<=right;i++){
            node_d cur = legal_order.get(i);
            if(B.contains(cur)) newSegment.add(cur);
        }
        for(int i=left;i<=right;i++){
            node_d cur = legal_order.get(i);
            if(I.contains(cur)) newSegment.add(cur);
        }
        for(int i=left;i<=right;i++){
            node_d cur = legal_order.get(i);
            if(F.contains(cur)) newSegment.add(cur);
        }

        //在原有的【v，u】区间，逐个放入新排序
        int idx = 0;//记一下放入了多少个nodes
        for(int i=left;i<=right;i++){
            legal_order.set(i, newSegment.get(idx++));//逐个node放入，覆盖原来区间的排序
        }

        //   输出结果   
        System.out.print("UPDATED "); 
        for(node_d n: legal_order){
            System.out.print(n.index + " ");
        }





    }
}

class node_d{
    int index = 0;
    ArrayList<node_d> parents = new ArrayList<>();
    ArrayList<node_d> children = new ArrayList<>();

}

class edge{
    node_d parent;
    node_d child;
}