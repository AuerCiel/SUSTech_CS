import java.util.ArrayList;

public class brute_force {
    //传入的是一个node数组，输出最小距离
    ArrayList<node> nodes;
    double res = Double.MAX_VALUE;

    //构造方法
    brute_force(ArrayList<node> nodes){
        this.nodes=nodes;
        if(nodes.size()==1){
           System.out.println("只有一个点，无效输入");
           return;}

        find_closest();
    }

    //find
    public void find_closest(){
        for(int i=0;i<nodes.size();i++){
            for(int j = i+1;j<nodes.size();j++){
                node a = nodes.get(i);
                node b = nodes.get(j);

                double dis =  find_closest_pair.find_dis(a,b);
                if(dis<res){
                    res = dis;
                }
            }
        }
    }

}
