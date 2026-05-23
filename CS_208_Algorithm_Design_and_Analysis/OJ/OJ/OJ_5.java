package OJ;


import java.util.ArrayList;
import java.util.List;


public class OJ_5 {
    static node_5[] node5s;
    static ArrayList<edge_5> wormholes;
    static  List<edge_5> edge5s;

    public static void main(String[] args) {
        //read in data
        FastReader in = new FastReader(System.in);

        int n = in.nextInt();
        int m = in.nextInt();
        int k = in.nextInt();


        //read node
        node5s = new node_5[n];
        for (int i = 0; i < n; i++) {
            node_5 node = new node_5();
            node.index = i;
            node5s[i] = node;
        }


        //read edge and connect nodes
        //and select wormhole edges and select the minimum capability of the wormholes
        int min = Integer.MAX_VALUE;
        wormholes = new ArrayList<>(m);        //存放虫洞
        edge5s = new ArrayList<>();            //存放所有边，包括虫洞
        for (int i = 0; i < m; i++) {
            edge_5 edge = new edge_5();

            edge.parent = node5s[in.nextInt()];
            edge.child = node5s[in.nextInt()];
            edge.capability = in.nextInt();

            if (in.nextInt() == 1) {
                edge.is_wormhole = true;
                wormholes.add(edge);
                //reserve min value                 //维护虫洞的最小边权
                if (edge.capability < min) {
                    min = edge.capability;
                }
            }


            edge5s.add(edge);   //存入edges数组
        }

        long left = 0;
        long right = min;   // 上界就是 wormhole 的最小值
        long ans = -1;

        // 先判断是否能连通（所有边都用）。不连通直接退
        if (!canConnectAll(n)) {
            System.out.println(-1);
            return;
        }

        while (left <= right) {
            long mid = (left + right) >> 1;

            if (check(mid, k, n)) {
                ans = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        System.out.println(ans);




    }
    static boolean check(long T, int k, int n) {

        DSU dsu = new DSU(n);

        int count = 0;      //选够n-1条，才合格
        long used = 0;

        //wormhole（必须选）
        for (edge_5 e : wormholes) {
            dsu.union(e.parent.index, e.child.index);
            count++;
        }


        //桶（cost 最大约 30）
        ArrayList<edge_5>[] buckets =  new ArrayList[32];
        for (int i = 0; i < 32; i++) {
            buckets[i] = new ArrayList<>();
        }

        //分类边（free直接用，need进桶）
        for (edge_5 e : edge5s) {
            if (e.is_wormhole) continue;//虫洞已经选过了，不处理

            if (e.capability >= T) {
                // free边直接尝试union
                if (dsu.union(e.parent.index, e.child.index)) {
                    count++;
                    if (count == n - 1) return true;
                }
            } else {
                // 计算cost（升级次数）
                long cur = e.capability;
                int cnt = 0;
                while (cur < T) {
                    cur <<= 1;
                    cnt++;
                }
                buckets[cnt].add(e);
            }
        }

        //按cost从小到大处理
        for (int c = 0; c < 32; c++) {
            for (edge_5 e : buckets[c]) {
                if (dsu.union(e.parent.index, e.child.index)) {
                    used += c;
                    count++;

                    // 剪枝
                    if (used > k) return false;
                    if (count == n - 1) return true;
                }
            }
        }

        return count == n - 1 && used <= k;
    }

    //并查集对象
    static class DSU {
        int[] parent;

        DSU(int n) {
            parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        //传入一个边的两端node的index，判断是不是在同一个集合。是就返回false，不选这个edge。不是就union，返回true，表示选这个edge
        boolean union(int a, int b) {
            int pa = find(a);
            int pb = find(b);
            if (pa == pb) return false;
            parent[pa] = pb;
            return true;
        }
    }
    //判断图的连通性
    static boolean canConnectAll(int n) {
        DSU dsu = new DSU(n);

        for (edge_5 e : edge5s) {
            dsu.union(e.parent.index, e.child.index);
        }

        int root = dsu.find(0);
        for (int i = 1; i < n; i++) {
            if (dsu.find(i) != root) return false;
        }
        return true;
    }

}




class node_5{
    int index;

}

class edge_5{
    node_5 parent;
    node_5 child;
    int capability;
    boolean is_wormhole;
    
}



