public class node implements Comparable<node>{
    double x;
    double y;
    static boolean is_x = true;

    @Override
    public int compareTo(node o) {
        if(is_x){
            return Double.compare(this.x,o.x);
        }else{
            return Double.compare(this.y,o.y);
        }
    }
}