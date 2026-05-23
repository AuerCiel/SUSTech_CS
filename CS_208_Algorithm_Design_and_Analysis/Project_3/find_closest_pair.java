
import java.util.ArrayList;


//设计接口：
//传入一组node对象，找出最近的两个node
//输出是一个res
public class find_closest_pair {
    ArrayList<node> nodes;
    ArrayList<node> nodes_y; // 储存按照y排序的node
    node[] temp_array;      // 归并排序使用的临时数组
    node[] strip_array;     // 复用内存以储存 strip 区域的 node
    double res = Double.MAX_VALUE;


    //构造方法
    find_closest_pair(ArrayList<node> nodes){
        this.nodes = nodes;
        if(nodes.size()==1){
            System.out.println("只有一个点，无效输入");
            return;
        }
        //按照x轴位置排列
        nodes.sort(null);
        
        // 初始化nodes_y，初始状态和nodes一致（按x排序）
        this.nodes_y = new ArrayList<>(nodes);
        this.temp_array = new node[nodes.size()];
        this.strip_array = new node[nodes.size()];
        
        res = divide(0, nodes.size()-1);

    }


    //divide
    public double divide (int left,int right){

        //base cases:   处理三个node，两个node的情况。right 和 left都是真实指针。
        if(left+1 == right){
            double d = find_dis(nodes.get(left),nodes.get(right));
            // 对nodes_y进行y轴排序
            node a = nodes_y.get(left), b = nodes_y.get(right);
            if (a.y > b.y) {
                nodes_y.set(left, b);
                nodes_y.set(right, a);
            }
            return d;

        } else if (right == left+2) {
            //三个node，两两计算距离
            double dis_1 = find_dis(nodes.get(left), nodes.get(right));
            double dis_2 = find_dis(nodes.get(left), nodes.get(right-1));
            double dis_3 = find_dis(nodes.get(left+1), nodes.get(right));
            double d = Math.min(dis_1,Math.min(dis_2,dis_3));
            
            // 对三个点进行y轴排序 (直接比较交换)
            node a = nodes_y.get(left), b = nodes_y.get(left+1), c = nodes_y.get(right);
            if (a.y > b.y) { node t = a; a = b; b = t; }
            if (a.y > c.y) { node t = a; a = c; c = t; }
            if (b.y > c.y) { node t = b; b = c; c = t; }
            nodes_y.set(left, a); nodes_y.set(left+1, b); nodes_y.set(right, c);
            
            return d;
        }


        //找左半边
        int mid = left + (right - left)/2;//指向中间的node
        double dis_1 = divide(left,mid);
        //找右半边
        double dis_2 = divide(mid+1, right);
        
        //归并 nodes_y[left...mid] 和 nodes_y[mid+1...right]
        merge(left, mid, right);
        
        //找两边中的更小值。
        double cur_dis = Math.min(dis_1,dis_2);
        //找跨边的最小距离
        double dis_3 = find_cross(left,right,mid,cur_dis);

        return Math.min(cur_dis,dis_3);
    }

    private void merge(int left, int mid, int right) {
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            if (nodes_y.get(i).y <= nodes_y.get(j).y) {
                temp_array[k++] = nodes_y.get(i++);
            } else {
                temp_array[k++] = nodes_y.get(j++);
            }
        }
        while (i <= mid) temp_array[k++] = nodes_y.get(i++);
        while (j <= right) temp_array[k++] = nodes_y.get(j++);
        
        for (int m = left; m <= right; m++) {
            nodes_y.set(m, temp_array[m]);
        }
    }

    public double find_cross(int left,int right,int mid,double cur_dis){
        double cur_ans = Double.MAX_VALUE;
        double mid_line = nodes.get(mid).x;

        // 取出所有在中间讨论区的node。
        // 由于 nodes_y[left...right] 已经在 divide 过程中按 y 轴排好序了，
        // 我们直接遍历这个区间，筛选出 x 轴距离中线小于 cur_dis 的点即可。
        int count = 0;
        for (int i = left; i <= right; i++) {
            if (Math.abs(nodes_y.get(i).x - mid_line) < cur_dis) {
                strip_array[count++] = nodes_y.get(i);
            }
        }

        // 此时 strip_array[0...count-1] 已经是按 y 轴从小到大排序的了
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                // 如果 y 轴坐标差超过 cur_dis，则无需继续比较（剪枝）
                if (strip_array[j].y - strip_array[i].y >= cur_dis) break;
                double dis = find_dis(strip_array[i], strip_array[j]);
                if (dis < cur_ans) {
                    cur_ans = dis;
                }
            }
        }
        return cur_ans;
    }


    static public double find_dis(node left,node right){
        double dx = left.x - right.x;
        double dy = left.y - right.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

}





