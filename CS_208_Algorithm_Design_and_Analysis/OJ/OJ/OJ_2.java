package OJ;

import java.util.*;

public class OJ_2 {
    static node_gate[] node_gates;
    static node_gate[] node_outputs;
    static operation[] operations;
    static ArrayList<node_gate> node_input;
    static int n;
    static int k;
    static int t;
    static int current_logic_nodes;//永远指向node_gates数组里面，下一个可以加入新node的位置
    static int calculation_timestamp = 0;

    public static void main(String[] args) {
        //初始化方法，完成数据读取
        init();

        //取指令+执行指令+检测是否成环+最终输出
        int op_pointer = 0;
        while(op_pointer< operations.length){
            //先取指令
            operation op_to_perform = operations[op_pointer];
            op_pointer++;
            //判断类型,并且执行
            if(op_to_perform.type.equals("ADD")){
                //执行add类型
                perform_add(op_to_perform);
                calculate_output();
                print_output();
            }else if(op_to_perform.type.equals("DELETE")){
                //执行delete类型

                //会保证要删除的node一定存在，不是output，且output不会被使用。
                //也就是说，必定是leaf节点
                //采用软删除吧
                perform_delete(op_to_perform);
                calculate_output();
                print_output();
            }else {
                //执行modify类型
                boolean b = perform_modify(op_to_perform);
                if(b){
                    System.out.println("Loop");
                }else {
                    calculate_output();
                    print_output();
                }
            }

        }


    }
    static void perform_add(operation op){
        node_gate add = new node_gate();
        node_gates[current_logic_nodes] = add;
        add.index = current_logic_nodes;
        current_logic_nodes++;
        add.type="GATE";
        add.logic=op.gate_type;
        add.src_1=op.src_1;
        add.src_2=op.src_2;

    }
    static void perform_delete(operation op){
        node_gate node = node_gates[op.gate_id_of_delete];
        node.is_deleted = true;
    }
    // 重写 perform_modify：检测成环
    static boolean perform_modify(operation op) {
        int gate_id = op.gate_id_of_modify;
        int new_src = op.new_src;

        // 环路检测：从 new_src 向上追溯，看是否能到达 gate_id
        // 如果 new_src 已经依赖了 gate_id，那么再让 gate_id 指向 new_src 就会成环
        if (can_reach(new_src, gate_id)) {
            return true;
        }

        // 不成环，执行修改
        node_gate node = node_gates[gate_id];
        if (op.input_index == 0) {
            node.src_1 = new_src;
        } else {
            node.src_2 = new_src;
        }
        return false;
    }

    // 辅助方法：判断 start 节点是否依赖于 target 节点 (向上追溯)
    static boolean can_reach(int start_id, int target_id) {
        if (start_id == -1) return false;
        if (start_id == target_id) return true;


        //开队列，集合，进行BFS
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        queue.add(start_id);
        visited.add(start_id);

        while (!queue.isEmpty()) {
            int curr_id = queue.poll();
            if (curr_id == target_id) return true;

            node_gate curr = node_gates[curr_id];
            // 检查当前节点的输入源
            int[] sources = {curr.src_1, curr.src_2};
            for (int s : sources) {
                if (s != -1 && !visited.contains(s)) {
                    visited.add(s);
                    queue.add(s);
                }
            }
        }
        return false;
    }

    // 重写 calculate_output：使用记忆化搜索 (O(N))
    static void calculate_output() {
        // 增加全局计算时间戳，避免重复计算
        calculation_timestamp++;

        // 对每个输出节点进行递归求值
        for (node_gate nodeOutput : node_outputs) {
            nodeOutput.output_value = recursive_eval(nodeOutput.index);
        }
    }

    // 记忆化搜索核心递归
    static int recursive_eval(int node_id) {
        if (node_id == -1) return 0;//递归的时候，有些src指针指向的node不存在。这里就是处理方法
        node_gate n = node_gates[node_id];

        // 如果是 INPUT 类型，直接返回其值
        if (n.type.equals("INPUT")) {
            return n.input_value;
        }

        // 如果在本次计算周期内已经算过了，直接返回缓存值
        // 需要在 OJ.node_gate 类中增加一个 int last_calc_time = 0; 字段
        if (n.output_value != -1 && n.last_calc_time == calculation_timestamp) {
            return n.output_value;
        }

        // 递归计算输入源的值
        int v1 = recursive_eval(n.src_1);
        int res = 0;

        if (n.logic.equals("NOT")) {
            res = (v1 == 1) ? 0 : 1;
        } else {
            int v2 = recursive_eval(n.src_2);
            switch (n.logic) {
                case "AND": res = v1 & v2; break;
                case "OR":  res = v1 | v2; break;
                case "XOR": res = v1 ^ v2; break;
            }
        }

        // 更新缓存和时间戳
        n.output_value = res;
        n.last_calc_time = calculation_timestamp;
        return res;
    }

    static void print_output() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < node_outputs.length; i++) {
            sb.append(node_outputs[i].output_value);
            if (i != node_outputs.length - 1) sb.append(" ");

        }
        System.out.println(sb);

    }


    static void init() {
        // 1. 使用快读替代 Scanner
        FastReader in = new FastReader(System.in);//System.in是fd=0，也就是专门指向键盘输入的inputStream

        // 2. 读取第一行三个参数
        n = in.nextInt();
        k = in.nextInt();
        t = in.nextInt();
        current_logic_nodes = n;

        node_gates = new node_gate[n + t];
        node_input = new ArrayList<>();

        // 3. 读取节点定义
        for (int i = 0; i < n; i++) {
            node_gate node = new node_gate();
            node.index = i;

            String type = in.next(); // 读入 INPUT 或 GATE
            if (type.equals("INPUT")) {
                node.type = "INPUT";
                node.input_value = in.nextInt();
                node_input.add(node);
            } else {
                node.type = "GATE";
                node.logic = in.next(); // 读入 AND/OR/XOR/NOT
                node.src_1 = in.nextInt();
                if (!node.logic.equals("NOT")) {
                    node.src_2 = in.nextInt();
                }
            }
            node_gates[i] = node;
        }

        // 4. 读取输出端口 ID
        node_outputs = new node_gate[k];
        for (int i = 0; i < k; i++) {
            node_outputs[i] = node_gates[in.nextInt()];
            node_outputs[i].is_output = true;
        }

        // 5. 读取操作指令
        operations = new operation[t];
        for (int i = 0; i < t; i++) {
            operation op = new operation();
            String type = in.next();
            op.type = type;

            if (type.equals("ADD")) {
                op.gate_type = in.next();
                op.src_1 = in.nextInt();
                if (!op.gate_type.equals("NOT")) {
                    op.src_2 = in.nextInt();
                }
            } else if (type.equals("DELETE")) {
                op.gate_id_of_delete = in.nextInt();
            } else if (type.equals("MODIFY")) {
                op.gate_id_of_modify = in.nextInt();
                op.input_index = in.nextInt();
                op.new_src = in.nextInt();
            }
            operations[i] = op;
        }
    }


}


class operation {
    String type;

    //values for add OJ.operation
    String gate_type;
    int src_1=-1;
    int src_2=-1;

    //values for delete
    int gate_id_of_delete;


    //values for modify
    int gate_id_of_modify;
    int input_index;
    int new_src;
}

class node_gate{
    String type;
    String logic;
    int src_1=-1;   //=================初始值设为-1，表示不指向任何node
    int src_2=-1;
    int input_value;//如果是input value，输入值就放在这里
    int output_value = -1; //如果是-1，表示这个node的输出没计算过。

    boolean is_output = false;
    int index;
    boolean is_deleted = false;

    int last_calc_time = 0;
}


