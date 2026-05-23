package practice5;

import java.util.*;

public class interval_partition {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        //读入数据
        int n = sc.nextInt();
        Job[] jobs = new Job[n];

        for (int i = 0; i < n; i++) {
            double begin = sc.nextDouble();
            double end = sc.nextDouble();
            jobs[i] = new Job(begin, end);
        }

        //执行计算，打印答案
        int ans = minClassrooms(jobs);
        System.out.println(ans);
    }

    public static int minClassrooms(Job[] jobs) {
        if (jobs == null || jobs.length == 0) return 0;

        // 按开始时间排序
        Arrays.sort(jobs, (a, b) -> Double.compare(a.begin, b.begin));//谁开始时间早，谁排在前面
        MinHeap heap = new MinHeap(jobs.length);
        for (Job curr : jobs) {
            if (!heap.isEmpty() && curr.begin >= heap.peek()) {
                heap.removeMin();
            }
            heap.add(curr.end);
        }

        return heap.size;
    }
}


class Job {
    double begin;
    double end;

    public Job(double begin, double end) {
        this.begin = begin;
        this.end = end;
    }
}


class MinHeap {
    double[] heap;
    int size;

    public MinHeap(int capacity) {
        heap = new double[capacity + 1];
        size = 0;
    }

    public void add(double val) {
        size++;
        heap[size] = val;
        siftUp(size);
    }

    public double peek() {
        return heap[1];
    }

    public void removeMin() {
        heap[1] = heap[size];
        size--;
        siftDown(1);
    }

    private void siftUp(int i) {
        while (i > 1) {
            int parent = i / 2;
            if (heap[i] < heap[parent]) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    private void siftDown(int i) {
        while (2 * i <= size) {
            int left = 2 * i;
            int right = 2 * i + 1;
            int min = left;

            if (right <= size && heap[right] < heap[left]) {
                min = right;
            }

            if (heap[i] <= heap[min]) break;

            swap(i, min);
            i = min;
        }
    }

    private void swap(int i, int j) {
        double temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}


