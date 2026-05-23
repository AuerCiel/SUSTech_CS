package practice4;

import java.util.ArrayList;
import java.util.Scanner;

public class topological_ordering {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        //初始化nodes
        node_c[] nodes = new node_c[sc.nextInt()];
        for(int i=0;i<nodes.length;i++){
            node_c c = new node_c();
            c.index=i;
            nodes[i] = c;
        }



        //read in edges, and connect the nodes
        int edges = sc.nextInt();
        for(int i=0;i<edges;i++){
            node_c father = nodes[sc.nextInt()];
            node_c child  = nodes[sc.nextInt()];

            //维护每一个node的edge指向，入度，
            father.children.add(child);
            child.in_degree++;

        }

        //找出所有入度为0的node，入队
        node_c[] queue = new node_c[nodes.length+1];
        int rear = 0;
        int tail = 0;
        for(node_c a:nodes){
            if(a.in_degree==0) {
                queue[tail] = a;
                tail++;
                a.is_in = true;
            }
        }

        //初始化拓扑序列
        node_c[] answer = new node_c[nodes.length];
        int answer_tail = 0;


        //遍历queue，并且适当放入topological队列
        while(rear<tail){
            //取出队首元素
            node_c parent = queue[rear];
            rear++;

            //加入answer
            answer[answer_tail] = parent;
            answer_tail++;

            //遍历children
            for(node_c child:parent.children){

                if(child.in_degree==1){
                    child.in_degree--;
                    queue[tail] = child;
                    tail++;
                    child.is_in=true;
                }else {
                    child.in_degree--;
                }
            }
        }

        //检查是否有环,再打印答案
        if(tail!=nodes.length){
            System.out.println("CYCLE");
        }else {
            for(node_c a:answer){
                System.out.print(a.index);
                System.out.print(' ');
            }
        }
    }

}
class node_c{
    int index=0;
    ArrayList<node_c> children = new ArrayList<>();
    boolean is_in = false;
    int in_degree = 0;

}
