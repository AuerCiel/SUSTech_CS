package practice3;

import java.util.ArrayList;
import java.util.Scanner;

public class flood_fill_test {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        int row_count = in.nextInt();
        int col_count = in.nextInt();

        //create a list to hold all nodes
        node_A[][] nodes = new node_A[row_count][col_count];


        //create node and link them
        for(int i=0;i<row_count;i++){
            for(int j=0;j<col_count;j++){

                //initialize the node
                node_A a = new node_A();
                a.color = in.nextInt();
                a.col=j;
                a.row=i;
                nodes[i][j] = a;
            }
        }


        //receive the requirement
        int starting_row = in.nextInt();
        int starting_col = in.nextInt();
        int flooding_color = in.nextInt();

        node_A flooding_node = nodes[starting_row][starting_col];
        int required_color = flooding_node.color;           //发大水的点的颜色


        //create a query,and initialize it
        node_A[] query = new node_A[row_count*col_count+1];
        int rear=0; //point to the first element
        int tail=1; //point to the last element's next position
        query[0]=flooding_node;
        flooding_node.is_in_query=true;
        flooding_node.is_flooded=true;


        //start to BFS
        while(rear<tail&&tail<row_count*col_count+1){

            //get the rear element
            node_A father = query[rear];
            rear++;

            //if its colour is not required color,then skip it
            if(father.color!=required_color){
                continue;
            }else{
                father.is_flooded = true;
            }

            //find its neighbors
            ArrayList<node_A> neighbours = find_neighbour(father,nodes);

            //add its neighbour to the query
            for(node_A a:neighbours){
                if(!a.is_in_query){
                    query[tail] = a;
                    tail++;
                    a.is_in_query = true;
                }
            }
        }


        //print the answer
        System.out.println();
        for(int i=0;i<row_count;i++){
            for(int j=0;j<col_count;j++){

                //change to the flooding_color
                node_A a = nodes[i][j];
                if(a.is_flooded){
                    a.color=flooding_color;
                }


                //print the node
                System.out.print(a.color);
                System.out.print(" ");
            }
            System.out.println();
        }

    }

    public static ArrayList<node_A> find_neighbour(node_A start, node_A[][] nodes){

        int col = start.col;
        int row = start.row;

        ArrayList<node_A> neighbours = new ArrayList<>();

        //find up
        if(row>0){
            node_A up = nodes[row-1][col];
            neighbours.add(up);
        }
        //find down
        if(row<nodes.length-1){
            node_A down = nodes[row+1][col];
            neighbours.add(down);
        }
        //find left
        if(col>0){
            node_A left = nodes[row][col-1];
            neighbours.add(left);
        }
        //find right
        if(col<nodes[0].length-1){
            node_A right = nodes[row][col+1];
            neighbours.add(right);
        }


        return neighbours;
    }

}

class node_A{
    int row;
    int col;
    int color;
    boolean is_flooded = false;
    boolean is_in_query = false;

}