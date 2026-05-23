package OJ;

import java.util.*;

public class OJ_1 {

    static int n, m, k;
    static List<pair> conflicts = new ArrayList<>();    //记录pair


    public static void main(String[] args) {
        Scanner  in = new Scanner(System.in);

        n =  in.nextInt();   //有多少个物品
        m =  in.nextInt();   //一次最多搬多少个
        k =  in.nextInt();   //有多少对危险情况
        
        //初始化conflict，并且开始记录
        conflicts.clear();
        for(int i=0;i<k;i++){
            pair danger = new pair();
            danger.material_1=in.nextInt();
            danger.material_2=in.nextInt();
            conflicts.add(danger);
        }


        //初始化full————定义当前输入下的final states
        int full = (1 << n) - 1;

        //创建队列，储存initial情况.
        Queue<state> cur_states = new LinkedList<>();
        state initial = new state();
        initial.state_code = 0;
        cur_states.add(initial);

        //创建一个set，储存已经存在的states_code。每次创建新的states_code，先检查有没有存在。
        //只要是进入过队列的，都认为已经存在
        boolean[] existed = new boolean[(1 << n) * 2];
        //加入初始态
        existed[0] = true;

        //开始执行核心逻辑
        while(!cur_states.isEmpty()){

            //取出queue的第一个元素
            state cur = cur_states.poll();

            //检测当前state是否是最终states
            if(cur.state_code==full){
                System.out.println(cur.cur_steps);
                return;
            }

            //查找当前节点的下一堆合法node有哪些
            ArrayList<state> valid = states_code_generator(cur,existed);

            //全部入队
            cur_states.addAll(valid);
        }

        //如果前面的循环没有终止整个程序，说明没有解决方法
        System.out.println(-1);
    }

    static public ArrayList<state> states_code_generator(state cur, boolean[] existed) {
        //传入的cur是当前node
        //下面的list，用来储存当前node可以到达的下一个node
        ArrayList<state> valid_states = new ArrayList<>();


        //提取当前states的codes，还要女孩的position
        int cur_mask = cur.state_code;
        int pos = cur.girl_position;


        //创建一个list，储存index。表示当前可以般第index号元素
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int bit = (cur_mask >> i) & 1;//取出第i个bit
            if (bit == pos) { // 如果这个bit和当前的pos一样，说明这个bit的材料可以搬走
                candidates.add(i);
            }
        }

        int size = candidates.size();

        //我们已经知道了有size个元素可以搬运-------不考虑去重，合法性检查的情况下
        //我们不需要遍历size个元素构成的集合的所有子集，复杂度太高了，2的size次方
        //我们只需要遍历，size里面由0~m个元素构成的子集一共有多少个。毕竟只能最多移动m个元素
        for (int s = 0; s < (1 << size); s++) {


            //我们把s看成是一个二进制数，用它来映射，从这些可搬运元素中，搬运哪些。
            //比如可搬运元素是 1，9，11
            //那么s=100，表示搬运1，s=010，表示搬运第二个可搬运元素，也就是搬运9.。。。以此类推
            //下面这里，是数我们当前的s这串二进制码，有多少个1，也就是搬运多少个可搬运元素
            //如果大于最大可搬运量m，就跳过不考虑这种情况
            if (Integer.bitCount(s) > m) continue;


            //这里根据s的值，修改原来的state_code，来生成新的code
            int new_mask = cur_mask;
            for (int j = 0; j < size; j++) {
                if (((s >> j) & 1) == 1) {      //阅读当前s的code，看哪个物品要般
                    int item = candidates.get(j);   //找出要般的物品在原码的index

                    // 翻转该物品位置，也就是般
                    new_mask ^= (1 << item);
                }
            }

            //维护下一步女孩的位置。不是1就是0
            int new_pos = 1 - pos;

            //如果不合法，就跳过当前情况
            if (!isValid(new_mask, new_pos)) continue;

            //去除重复状态
            int hash = (new_mask << 1) | new_pos;
            if (existed[hash]) continue;
            existed[hash] = true;

            // 构造新状态
            state next = new state();
            next.state_code = new_mask;
            next.girl_position = new_pos;
            next.cur_steps = cur.cur_steps + 1;

            valid_states.add(next);
        }
        return valid_states;
    }



    //传入当前的musk，判断是否合法
    static boolean isValid(int mask, int girl_pos) {

        // 判断哪一侧是“无人看管”
        int unattended_side = 1 - girl_pos;

        //遍历所有的不合法情况，判断code是否合法
        for (pair p : conflicts) {
            int a = p.material_1;
            int b = p.material_2;

            int posA = (mask >> a) & 1;
            int posB = (mask >> b) & 1;

            // 如果两个危险物品都在无人看管的一侧
            if (posA == unattended_side && posB == unattended_side) {
                return false;
            }
        }

        return true;
    }

}
class pair{
    int material_1=-1;
    int material_2=-1;
}

class state{
    //states 定义：LSB是girl的位置。0就在pile，1表示在box
    //MSB是第0位material的位置。0就在pile，1就在box
    //MSB左一位就是第1个material位置
    int state_code = 0;
    int cur_steps = 0;                      //计数器封装在node内部，不在外部维护
    int girl_position = 0;

}

