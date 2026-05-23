package practice5;

import java.util.Scanner;

public class interval_schedule {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        interval[] min_heap = new interval[n+1];

        for(int i=1;i<=n;i++){
            interval a = new interval();
            a.begin = in.nextInt();  // 读取开始时间
            a.end = in.nextInt();    // 读取结束时间
            min_heap[i] = a;
        }

        build_min_heap(min_heap);
        int counter = 0;
        int size = n;
        int cur_end = -1;

        while(size>0){
            interval a = pop(min_heap,size);
            size--;

            if(a.begin>=cur_end){
                counter++;
                cur_end=a.end;

            }
        }


        System.out.println(counter);

    }


    public static void build_min_heap(interval[] min_heap){
        int size = min_heap.length - 1; // 实际元素数量
        int pointer = size / 2;  // 最后一个非叶子节点位置

        // 从后往前，逐个向下调整
        for(int i = pointer; i >= 1; i--){
            siftDown(min_heap, i, size);
        }
    }
    public static void siftDown(interval[] heap, int i, int size){
        interval temp = heap[i]; // 先把当前节点存起来

        // 左孩子 = 2*i
        for(int j = 2*i; j <= size; j = 2*j){
            // 找到左右孩子里 end 更小的那个
            if(j+1 <= size && heap[j+1].end < heap[j].end){
                j++;
            }

            // 如果孩子更小，就往上移
            if(heap[j].end < temp.end){
                heap[i] = heap[j];
                i = j;
            }else{
                break; // 位置正确，停止
            }
        }

        heap[i] = temp; // 最后把原来节点放下
    }

    // 弹出堆顶（end 最小的 interval）
    // 返回弹出的元素；同时 heap 长度-1（通过 size 控制，数组不真缩）
    public static interval pop(interval[] min_heap, int size) {
        // 堆空
        if (size < 1) {
            return null;
        }

        // 保存堆顶
        interval top = min_heap[1];

        // 最后一个元素放到堆顶
        min_heap[1] = min_heap[size];

        // 新大小 = size - 1
        int newSize = size - 1;

        // 向下调整堆顶
        siftDown(min_heap, 1, newSize);

        // 返回弹出的元素
        return top;
    }

}

class interval {
    int begin = -1;
    int end = -1;

    // 构造方法方便创建
    public interval(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }
    public interval(){

    }
}
