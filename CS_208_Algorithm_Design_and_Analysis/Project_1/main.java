import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {
        System.out.println("您希望怎么进行样例测试？");
        System.out.println("——————从控制台输入测试样例，请按0");
        System.out.println("——————测试smallCases文件，请按1");
        System.out.println("——————测试bigCases的文件，请按2");

        Scanner in = new Scanner(System.in);
        
        int way_to_test = in.nextInt();
        
        //希望读取控制台输入
        if(way_to_test==0){
            //空参构造，并且在空参构造方法的内部进行初始化。监听用户输入
            matching matching = new matching();
            
            //运行算法
            matching.find_stable_match();
            matching.print_answer();

        } else if (way_to_test == 1) {//测试smallCases，并且和暴力算法进行验证


            File testCasesDir = new File("smallCases");
            File[] testFiles = testCasesDir.listFiles((dir, name) -> name.endsWith(".txt"));
            
            if (testFiles != null) {
                Arrays.sort(testFiles);//确保按顺序执行测试文件。排序方式是最小字典序
                
                for (File testFile : testFiles) {
                    
                    System.out.println("测试文件: " + testFile.getName() );
                    // 使用lab算法
                    matching matching = new matching(testFile.getPath());
                    matching.find_stable_match();
                    System.out.println("lab算法结果:");
                    matching.print_answer();
                    
                    // 使用暴力算法
                    matching bruteForceMatching = new matching(testFile.getPath());
                    bruteForceMatching.find_stable_match_brute_force();
                    System.out.println("暴力算法结果:");
                    bruteForceMatching.print_all_stable_matches();
                    
                    // 验证结果是否一致
                    boolean resultsMatch = false;
                    // 获取传统算法的匹配结果
                    List<String> traditionalMatch = new ArrayList<>();
                    for (int i = 0; i < matching.index_of_boy.length; i++) {
                        traditionalMatch.add(matching.index_of_boy[i].cur_lover.name);
                    }
                    // 检查传统算法的结果是否在暴力算法找到的稳定匹配中
                    for (List<girl> match : bruteForceMatching.all_stable_matches) {
                        List<String> currentMatch = new ArrayList<>();
                        for (int i = 0; i < match.size(); i++) {
                            currentMatch.add(match.get(i).name);
                        }
                        if (traditionalMatch.equals(currentMatch)) {
                            resultsMatch = true;
                            break;
                        }
                    }
                    
                    if (resultsMatch) {
                        System.out.println("验证结果: 合格");
                    } else {
                        System.out.println("验证结果: 不合格");
                    }
                    
                    System.out.println();
                    System.out.println();

                }
            } else {
                System.out.println("未找到测试文件");
            }
        }else if(way_to_test==2){//测试bigCases


            File testCasesDir = new File("bigCases");
            File[] testFiles = testCasesDir.listFiles((dir, name) -> name.endsWith(".txt"));

            if (testFiles != null) {
                Arrays.sort(testFiles);//确保按顺序执行测试文件。排序方式是最小字典序

                for (File testFile : testFiles) {

                    System.out.println("测试文件: " + testFile.getName() );


                    matching matching = new matching(testFile.getPath());
                    double a = System.nanoTime();
                    matching.find_stable_match();
                    double b = System.nanoTime();


                    System.out.println("总计用时："+(b-a));
                    System.out.println();
                    System.out.println();


                }
            } else {
                System.out.println("未找到测试文件");
            }
        }
    }
}