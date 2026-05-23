package practice8;

import java.util.Scanner;

public class inverse_pair {
    static int[] numbers;
    static int total;
    static int count_pair =0;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        total = in.nextInt();

        numbers = new int[total];
        for(int i=0;i<total;i++){
            numbers[i] = in.nextInt();
        }

        //暴力方法
        double begin = System.currentTimeMillis();
        brute_force();
        double end = System.currentTimeMillis();
        System.out.println("暴力计算："+count_pair+"  耗时："+(end-begin)+"毫秒"+"\n\n\n");

        //重新初始化计数器
        count_pair = 0;
        begin = System.currentTimeMillis();
        mergeSort(0,total-1);
        end = System.currentTimeMillis();
        System.out.println("merge计算："+count_pair+"  耗时："+(end-begin)+"毫秒");

    }
    //暴力
    public static void brute_force(){
        for(int i=0;i<total;i++){
            for(int j=i+1;j<total;j++){
                int a = numbers[i];
                int b = numbers[j];
                if(a>b){
                    count_pair++;
                }
            }
        }
    }

    //归并排序
    public static void mergeSort(int left, int right) {
        if (left >= right) return;

        int mid = (left + right) / 2;
        mergeSort(left, mid);
        mergeSort(mid + 1, right);
        merge(left, mid, right);  // 合并 + 统计逆序对
    }


    public static void merge(int left, int mid, int right) {
        int j = mid + 1;
        int l = left; //不能修改参数 left

        while (l <= mid && j <= right) {
            if (numbers[l] <= numbers[j]) {
                l++;
            } else {
                int val = numbers[j];

                // 右移元素
                for (int k = j; k > l; k--) {
                    numbers[k] = numbers[k - 1];
                }
                numbers[l] = val;

                // 统计逆序对
                count_pair += mid - l + 1;

                // 指针更新
                l++;
                mid++;
                j++;
            }
        }
    }
}
