import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        int choice = -1;
        while (true) {

            System.out.println("手动模式，请输入0");
            System.out.println("自动模式，请输入1");
            System.out.println("退出程序，请输入2");
            System.out.println("\n\n\n\n");

            choice = in.nextInt();
            if (choice == 0) {
                //读入node
                int n = in.nextInt();
                ArrayList<node> nodes = new ArrayList<>(n);

                for (int i = 0; i < n; i++) {
                    node a = new node();
                    a.x = in.nextDouble();
                    a.y = in.nextDouble();
                    nodes.add(a);
                }

                brute_force brute_force = new brute_force(nodes);
                find_closest_pair find_closest_pair = new find_closest_pair(nodes);

                double res_1 = brute_force.res;
                double res_2 = find_closest_pair.res;

                System.out.println("暴力计算结果：" + res_1);
                System.out.println("分治计算结果" + res_2);


            } else if (choice == 1) {
                readAndSolve("C:\\Users\\Akira\\Desktop\\CS_208_project_3\\small_cases");
                System.out.println("==================================================");
                System.out.println("\n");
                readAndSolve("C:\\Users\\Akira\\Desktop\\CS_208_project_3\\big_cases");
                System.out.println("==================================================");
            }else if (choice==2){
                break;
            }else  {
                System.out.println("未知指令，重新输入");
            }
        }
        System.out.println("执行完毕，退出程序");
    }

    static void readAndSolve(String s){
        File directory = new File(s);
        File[] testcases = directory.listFiles();

        //开始逐个读取文件
        if(testcases!=null){
            for(File test : testcases){

                try(BufferedReader reader = new BufferedReader(new FileReader(test) )){
                    //先读入第一行
                    String line_1 = reader.readLine();
                    int[] number = new int[]{Integer.parseInt(line_1.trim())};
                    ArrayList<node> nodes = new ArrayList<>(number[0]);

                    //开始循环读入点
                    while (true){
                        //一行行读
                        String line = reader.readLine();
                        if(line==null)break;
                        String[] parts = line.split("\\s+");
                        node a = new node();
                        a.x = Double.parseDouble(parts[0]);
                        a.y = Double.parseDouble(parts[1]);
                        nodes.add(a);

                    }

                    System.out.println(test.getName()+"运行结果：");
                    double c = System.currentTimeMillis();
                    if(number[0]<30000){
                        brute_force brute_force = new brute_force(nodes);
                        double res_1 = brute_force.res;
                        System.out.println("暴力计算结果：" + res_1);

                    }
                    double a = System.currentTimeMillis();
                    find_closest_pair find_closest_pair = new find_closest_pair(nodes);
                    double b = System.currentTimeMillis();


                    double res_2 = find_closest_pair.res;


                    System.out.println("分治计算结果：" + res_2);
                    System.out.println("暴力计算用时：" + (a-c));
                    System.out.println("分治计算用时：" + (b-a));
                    System.out.println("===========================");
                    System.out.println("\n");


                }catch (IOException IO_e){
                    System.out.println(test.getName()+"读取失败");
                }
            }
        }
        else {
        System.out.println("测试文件为空");
        }
    }

}
