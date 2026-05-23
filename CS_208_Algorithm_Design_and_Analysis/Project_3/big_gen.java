import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class big_gen {
    private static final int BATCH_SIZE = 8192;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Random random = new Random();

        System.out.println("输入样例的 index：");
        int index = in.nextInt();

        System.out.println("输入希望生成的 node 数量：");
        long num = in.nextLong();

        System.out.println("输入 x 轴左边界 右边界（空格分隔）：");
        double left_bound = -num;
        double right_bound = +num;

        System.out.println("输入 y 轴下边界 上边界（空格分隔）：");
        double lower_bound = -num;
        double upper_bound = +num;

        System.out.println("开始生成");
        String folderPath = "C:\\Users\\Akira\\Desktop\\CS_208_project_3\\big_cases";
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        String fileName = "case_" + index + ".txt";
        File file = new File(folder, fileName);

        //正式开始生成并且写入
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            double a = System.currentTimeMillis();
            bw.write(num + "\n");

            StringBuilder sb = new StringBuilder();
            double xRange = right_bound - left_bound;
            double yRange = upper_bound - lower_bound;

            for (long i = 0; i < num; i++) {
                double x = left_bound + xRange * random.nextDouble();
                double y = lower_bound + yRange * random.nextDouble();
                sb.append(x).append(' ').append(y).append('\n');

                if ((i + 1) % BATCH_SIZE == 0) {
                    bw.write(sb.toString());
                    sb.setLength(0);
                }
            }

            if (sb.length() > 0) {
                bw.write(sb.toString());
            }
            double b = System.currentTimeMillis();
            System.out.println("生成完成！");
            System.out.println("文件路径：" + file.getAbsolutePath());
            System.out.println("点数：" + num);
            System.out.println("总计耗时： "+(b-a)+"  毫秒");

        } catch (IOException e) {
            System.out.println("文件写入失败");
            e.printStackTrace();
        }


        in.close();
    }
}
