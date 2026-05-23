package practice3;

import java.util.ArrayList;
import java.util.Scanner;

public class Bipartite_Graph_Testing {
    //给定一个图，让你判断是不是二分图
    static boolean isBipartite = true;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int numberOfNodes = in.nextInt();
        int numberOfEdge = in.nextInt();

        //initialize nodes
        node_B[] nodes = new node_B[numberOfNodes];
        for(int i=0;i<numberOfNodes;i++){
            nodes[i] = new node_B();
            nodes[i].index=i;
        }


        //read in edges,and linked the nodes accordingly
        for (int i=0;i<numberOfEdge;i++){
            int a = in.nextInt();
            int b = in.nextInt();

            node_B a_node = nodes[a];
            node_B b_node = nodes[b];

            a_node.linked_nodes.add(b_node);
            b_node.linked_nodes.add(a_node);
        }

        //from above we have initialize the whole graph.
        //The first node is node[0]
        for (int i = 0; i < numberOfNodes; i++) {
            if (nodes[i].color == 0) {   // 0 = 未染色
                dfs(nodes[i], 1);        // 染成颜色 1
            }
        }


        // 输出结果
        System.out.println();
        if (isBipartite) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }


    }

    public static void dfs(node_B current, int color) {
        // 当前节点染色
        current.color = color;

        // 遍历所有邻居
        for (node_B neighbor : current.linked_nodes) {

            if (neighbor.color == 0) {
                // 邻居未染色 → 染相反颜色
                dfs(neighbor, 3 - color);  
            } else if (neighbor.color == current.color) {
                // 邻居和自己颜色一样 → 不是二分图
                isBipartite = false;
                return;
            }
        }
    }
}

class node_B{
    int index = 0;
    ArrayList<node_B> linked_nodes = new ArrayList<>();
    int color = 0;
}