import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class GenerateTestCase {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        
        String filename = "bigCases/" + n + "test.txt";
        
        try (FileWriter writer = new FileWriter(filename)) {
            // 写入 n
            writer.write(n + "\n");
            // 生成并写入男生名字
            ArrayList<String> boys = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                boys.add("boy" + i);
            }
            for (int i = 0; i < boys.size(); i++) {
                writer.write(boys.get(i));
                if (i < boys.size() - 1) {
                    writer.write(" ");
                }
            }
            writer.write("\n");
            
            // 生成并写入女生名字
            ArrayList<String> girls = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                girls.add("girl" + i);
            }
            for (int i = 0; i < girls.size(); i++) {
                writer.write(girls.get(i));
                if (i < girls.size() - 1) {
                    writer.write(" ");
                }
            }
            writer.write("\n");
            
            // 生成并写入男生偏好列表
            for (int i = 0; i < n; i++) {
                ArrayList<String> shuffled = new ArrayList<>(girls);//生成一个新的list，存入所有girls名字
                Collections.shuffle(shuffled);//把这个list打乱，就是一个合格的preferenceList了
                for (int j = 0; j < shuffled.size(); j++) {
                    writer.write(shuffled.get(j));
                    if (j < shuffled.size() - 1) {
                        writer.write(" ");
                    }
                }
                writer.write("\n");
            }
            
            // 生成并写入女生偏好列表
            for (int i = 0; i < n; i++) {
                ArrayList<String> shuffled = new ArrayList<>(boys);
                Collections.shuffle(shuffled);
                for (int j = 0; j < shuffled.size(); j++) {
                    writer.write(shuffled.get(j));
                    if (j < shuffled.size() - 1) {
                        writer.write(" ");
                    }
                }
                writer.write("\n");
            }
            
            System.out.println("测试样例生成完成: " + filename);
        } catch (IOException e) {
            System.out.println(n+"规模的测试样例生成失败");
        }
    }
}