package practice11;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Ford_Fulkerson {

    static ArrayList<Edge>[] graph;
    static boolean[] visited;

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        //
        // Scanner in = getScanner();

        int n = in.nextInt();
        int m = in.nextInt();
        graph = new ArrayList[n];//邻接表集合

        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();//第i个node存的边。都放到这里了
        }
        for (int i = 0; i < m; i++) {

            int u = in.nextInt();
            int v = in.nextInt();
            int w = in.nextInt();

            addEdge(u, v, w);
        }
        System.out.println(maxFlow(n - 1));
    }
    //加边。。。。还以为要用多少次。。。
    //u是起点，v是终点
    static void addEdge(int u, int v, int cap) {
        // 正向边,从u到v
        Edge forward = new Edge(v, cap, graph[v].size());
        // 反向边
        Edge backward = new Edge(u, 0, graph[u].size());
        graph[u].add(forward);
        graph[v].add(backward);
    }

    //DFS寻找增广路径。返回值是这条路径可以承受的最大flow增量
    static int dfs(int u, int t, int flow) {
        // 到达汇点
        if (u == t) {
            return flow;
        }
        visited[u] = true;
        for (Edge e : graph[u]) {
            //还有剩余容量并且没访问过
            if (e.capacity > 0 && !visited[e.to]) {
                int pushed = dfs(
                        e.to,
                        t,
                        Math.min(flow, e.capacity)
                );
                // 找到增广路径
                if (pushed > 0) {
                    // 更新正向边
                    e.capacity -= pushed;
                    // 更新反向边
                    graph[e.to]
                            .get(e.rev)
                            .capacity += pushed;
                    return pushed;
                }
            }
        }
        return 0;
    }

    static int maxFlow(int t) {
        int flow = 0;
        while (true) {

            visited = new boolean[graph.length];
            int pushed = dfs(0, t, Integer.MAX_VALUE);
            // 找不到增广路径
            if (pushed == 0) {
                break;
            }
            flow += pushed;
        }
        return flow;
    }

    static Scanner getScanner(){
        Scanner in = null;
        try {
            in = new Scanner(new File("c:\\Users\\Akira\\Desktop\\CS_208_ass\\src\\practice11\\1.in"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return in;
    }

    static class Edge {
        int to;
        int capacity;
        int rev;

        Edge(int to, int capacity, int rev) {
            this.to = to;
            this.capacity = capacity;
            this.rev = rev;
        }
    }
}
