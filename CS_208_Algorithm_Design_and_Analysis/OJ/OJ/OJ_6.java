package OJ;


public class OJ_6 {
    static int ans=0;
    static int max,min;
    public static void main(String[] args) {
        FastReader in = new FastReader(System.in);
        min = in.nextInt();
        max = in.nextInt();
        //总数
        int n = in.nextInt();
        int[] num = new int[n];
        int[] pre = new int[n+1];   //前缀和数组
        // 读入数组
        for(int i = 0; i < n; i++){
            num[i] = in.nextInt();
            pre[i+1] = pre[i]+num[i];
        }

        merge(0,n,pre);

        System.out.println(ans);

    }

    //用来分割的方法
    public static void merge(int left, int right, int[] pre) {
        if (left >= right) return;

        //先把前面归并解决了，再解决当前层
        int mid = left + (right - left) / 2;
        merge(left, mid, pre);
        merge(mid + 1, right, pre);

        sort(left, mid, right, pre);
    }

    //实际执行
    public static void sort(int left, int mid, int right, int[] pre) {
        //统计——————利用两个指针在左半部分滑动，找出符合条件的 pre[i] 范围
        int low = left;
        int high = left;

        for (int j = mid + 1; j <= right; j++) {
            //找出 pre[j] - pre[i] >= min 的最小 low
            while (low <= mid && pre[j] - pre[low] > max) {
                low++;
            }
            //找出 pre[j] - pre[i] <= max 的最大 high
            while (high <= mid && pre[j] - pre[high] >= min) {
                high++;
            }
            ans += (high - low);
        }

        //排序————常规归并排序逻辑，确保数组有序以供上一层递归使用
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;

        while (i <= mid && j <= right) {
            if (pre[i] <= pre[j]) temp[k++] = pre[i++];
            else temp[k++] = pre[j++];
        }
        while (i <= mid) temp[k++] = pre[i++];
        while (j <= right) temp[k++] = pre[j++];

        System.arraycopy(temp, 0, pre, left, temp.length);
    }

}





