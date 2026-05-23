import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class matching {
    //如果通过读取文件的方式测试：需要声明下面两个变量：
    File txt_file_to_analyze; //相对路径：在src文件夹下的main方法，相对路径举例：C:\Users\Akira\Desktop\CS_208_project_1\a，我只输入了a
    String path_of_testCase;

    //算法用到的变量
    int pair_of_couple = 0; //有多少对男女要匹配
    HashMap<String,girl> girls_map; //查询表for女生
    HashMap<String,boy> boys_map; //查询表for男生
    boy[] index_of_boy; //所有男生按照输入顺序储存
    girl[] index_of_girl; //所有女生按照输入顺序储存
    List<List<girl>> all_stable_matches; //存储所有稳定匹配。之需要储存girls的顺序就ok。因为在当前list，女生的index就代表了她对象是哪个男生



    //构造方法，一个matching实例对应一组stable matching。
    //读入文件，并且完成初始化
    matching(String path) {
        this.path_of_testCase = path;
        txt_file_to_analyze = new File(path);
        //读取文件，并且初始化类
        this.initialization();
    }

    //如果希望从控制台读取测试样例，就使用空参构造
    matching(){
        String console = "console";
        this.initialization(console);
    }

    //配对完了之后打印答案
    public void print_answer(){
        for(boy boy:index_of_boy){
            System.out.println(boy.name+" "+boy.cur_lover.name);
        }
    }

    //打印所有稳定匹配————————————专门给暴力算法的
    public void print_all_stable_matches(){
        System.out.println("找到 " + all_stable_matches.size() + " 个稳定匹配：");
        for(int i=0; i<all_stable_matches.size(); i++){
            //逐个匹配打印
            System.out.println("匹配 " + (i+1) + ":");
            List<girl> match = all_stable_matches.get(i);//取出当前的match
            for(int j=0; j<match.size(); j++){
                boy b = index_of_boy[j];
                girl g = match.get(j);
                System.out.println(b.name+" "+g.name);
            }
            System.out.println();
        }
    }

    //核心算法逻辑
    public void find_stable_match(){
        //算法核心是，每次找一个单身的男生，像女生求爱。
        //女生单身，就接受
        //不单身，就选更喜欢的
        //被甩了的男生重新入队
        //循环反复，直到没有单身的男生为止
        //创建队列，储存当前所有单身男
        Queue<boy> single_boys = new ArrayDeque<>(pair_of_couple);
        Collections.addAll(single_boys,index_of_boy);

        //执行算法,直到没有男生单身
        while(!single_boys.isEmpty()){

            //当前要分配的boy
            boy boy_to_match = single_boys.poll();

            while (boy_to_match.cur_lover==null){

                //他当前没追过且最喜欢的女生
                int cur_preferred_girl = boy_to_match.pointer;
                girl girl_to_pursuit = boy_to_match.preference_list[cur_preferred_girl];
                //维护指针
                boy_to_match.pointer++;
                //每一次提出propose，不论追到没有，都会维护指针。

                //尝试追这个女生
                //女生单身，直接分配
                if(girl_to_pursuit.cur_lover==null){
                    boy_to_match.cur_lover = girl_to_pursuit;
                    girl_to_pursuit.cur_lover=boy_to_match;
                }else{
                    //女生不单身，就开始雄竞
                    //原配分数：
                    int old_lover=girl_to_pursuit.preference_list.get(girl_to_pursuit.cur_lover);
                    int new_lover=girl_to_pursuit.preference_list.get(boy_to_match);
                    //比较分数
                    if(old_lover<new_lover){
                        //原配获胜
                        continue;
                    }else {
                        //当前男生获胜
                        //原配继续单身
                        single_boys.add(girl_to_pursuit.cur_lover);
                        //这里不需要给原配更新指针。因为原配只能通过提出propose获得女生。
                        //每次propose不论成功与否，pointer都已经指向了下一个女生
                        girl_to_pursuit.cur_lover.cur_lover=null;

                        //重新分配
                        boy_to_match.cur_lover = girl_to_pursuit;
                        girl_to_pursuit.cur_lover=boy_to_match;
                    }
                }
            }
        }
    }

    //Brute-force算法逻辑
    public void find_stable_match_brute_force() {
        int n = pair_of_couple;
        int[] permutation = new int[n];//我们用一个int数组反映男女生的映射关系。第i位的值，表示index是i的男生，分配的是哪个index的女生
        for (int i = 0; i < n; i++) {
            permutation[i] = i;
        }
        //清空之前的结果
        all_stable_matches.clear();
        //生成所有排列并收集稳定匹配
        generatePermutations(permutation, 0, n);

        //设置第一个稳定匹配为当前匹配，也就是我们这个matching class里面分配的girls，boys的cur_lover指针，是根据第一个可能的matching指定的
        if (!all_stable_matches.isEmpty()) {
            List<girl> firstMatch = all_stable_matches.get(0);
            for (int i = 0; i < n; i++) {
                boy b = index_of_boy[i];
                girl g = firstMatch.get(i);
                b.cur_lover = g;
                g.cur_lover = b;
            }
        }
    }

    //生成所有排列并检查稳定性
    private void generatePermutations(int[] permutation, int start, int n) {
        if (start == n) {
            if (isStable(permutation)) {
                //保存稳定匹配
                List<girl> match = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    match.add(index_of_girl[permutation[i]]);//逐个把int类型映射的女生装进去
                }
                all_stable_matches.add(match);
            }
        } else {
            for (int i = start; i < n; i++) {
                swap(permutation, start, i);
                generatePermutations(permutation, start + 1, n);
                swap(permutation, start, i);
            }
        }
    }


    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    //检查匹配是否稳定
    private boolean isStable(int[] permutation) {
        int n = pair_of_couple;
        //创建女生到当前男生的映射
        HashMap<girl, boy> girlToBoy = new HashMap<>();
        for (int i = 0; i < n; i++) {
            boy b = index_of_boy[i];                //由permutation数组的index和对应的值，建立映射
            girl g = index_of_girl[permutation[i]];
            girlToBoy.put(g, b);
        }
        
        //检查所有可能的阻塞对
        for (int i = 0; i < n; i++) {
            boy b = index_of_boy[i];
            girl currentGirl = index_of_girl[permutation[i]];
            
            //检查男生b更喜欢的女生
            for (int j = 0; j < n; j++) {
                girl g = b.preference_list[j];
                if (g == currentGirl) {
                    //已经检查到当前伴侣，后面的女生优先级更低，无需继续检查
                    break;
                }
                
                //检查女生g是否更喜欢男生b而不是当前伴侣
                boy currentBoyOfG = girlToBoy.get(g);
                int gPrefersB = g.preference_list.get(b);
                int gPrefersCurrent = g.preference_list.get(currentBoyOfG);
                
                if (gPrefersB < gPrefersCurrent) {
                    //发现阻塞对，匹配不稳定
                    return false;
                }
            }
        }
        
        //没有发现阻塞对，匹配稳定
        return true;
    }

    //从文件处读测试cases
    public void initialization() {

        try(BufferedReader br = new BufferedReader(new FileReader(txt_file_to_analyze))){

            //先读取测试样例大小
            int n = Integer.parseInt(br.readLine());
            this.pair_of_couple = n;

            //因为一开始的容量是0.75*初始设置的n，大于这个的时候会触发扩容。
            //我们这里不想要扩容，就一次性开好
            girls_map = new HashMap<>((int)(n / 0.75f) + 1);
            boys_map = new HashMap<>((int)(n / 0.75f) + 1);

            //创建index映射boy，girl的表。因为读取preference的时候，是根据第x行，来定位是哪个boy的
            index_of_boy = new boy[pair_of_couple];
            index_of_girl = new girl[pair_of_couple];
            //初始化存储所有稳定匹配的列表
            all_stable_matches = new ArrayList<>();

            //下一行是男生有哪些,初始化男生们
            String line_of_boys = br.readLine();
            String[] boys_names = line_of_boys.split(" ");

            for(int i = 0;i<pair_of_couple;i++){
                //创建这些boy的对象
                boy boy = new boy();
                boy.name=boys_names[i];
                boy.pair_of_people=pair_of_couple;

                //将boy放入查询表
                boys_map.put(boy.name,boy);
                index_of_boy[i] = boy;
            }

            //读入并且初始化girls
            String line_of_girls = br.readLine();
            String[] girls_name = line_of_girls.split(" ");

            for(int i=0;i<pair_of_couple;i++){
                //创建girl的对象
                girl girl = new girl();
                girl.name=girls_name[i];
                girl.pair_of_people=pair_of_couple;

                //将girl放入查询表
                girls_map.put(girl.name,girl);
                index_of_girl[i] = girl;
            }

            //读入接下来的n行，是boy的preference list
            for(int i=0;i<pair_of_couple;i++){

                //先找出是哪个boy的偏好list
                boy boy = index_of_boy[i];

                //初始化它的preferenceList
                boy.preference_list = new girl[pair_of_couple];

                //读取偏好表
                String line = br.readLine();
                String[] preference = line.split(" ");

                for(int j=0;j<pair_of_couple;j++){
                    girl girl = girls_map.get(preference[j]);
                    boy.preference_list[j]=girl;
                }
            }

            //读取接下来的n行，初始化girl的偏好
            for(int i=0;i<pair_of_couple;i++){

                //找出是哪个girl的偏好表
                girl girl= index_of_girl[i];

                //初始化她的preferenceList
                girl.preference_list = new HashMap<>((int)(n / 0.75f) + 1);

                //read in preference list and initialize it
                String line = br.readLine();
                String[] preference = line.split(" ");

                for(int j=0;j<pair_of_couple;j++){
                    boy boy = boys_map.get(preference[j]);
                    girl.preference_list.put(boy,j);
                }
            }

            System.out.println("完成"+path_of_testCase+"测试样例的初始化");

        }catch (Exception e){
            System.out.println("文件读取失败");
        }
    }
    //从控制台读取testCases
    public void initialization(String console) {
        Scanner scanner = new Scanner(System.in);
        
        //先读取测试样例大小
        int n = scanner.nextInt();
        scanner.nextLine(); // 消费换行符
        this.pair_of_couple = n;

        //初始化 HashMap
        girls_map = new HashMap<>((int)(n / 0.75f) + 1);
        boys_map = new HashMap<>((int)(n / 0.75f) + 1);

        //创建 index
        index_of_boy = new boy[pair_of_couple];
        index_of_girl = new girl[pair_of_couple];
        //初始化存储所有稳定匹配的列表
        all_stable_matches = new ArrayList<>();

        //读取男生
        String line_of_boys = scanner.nextLine();
        String[] boys_names = line_of_boys.split(" ");

        for(int i = 0;i<pair_of_couple;i++){
            boy boy = new boy();
            boy.name=boys_names[i];
            boy.pair_of_people=pair_of_couple;

            boys_map.put(boy.name,boy);
            index_of_boy[i] = boy;
        }

        //读取女生
        String line_of_girls = scanner.nextLine();
        String[] girls_name = line_of_girls.split(" ");

        for(int i=0;i<pair_of_couple;i++){
            girl girl = new girl();
            girl.name=girls_name[i];
            girl.pair_of_people=pair_of_couple;

            girls_map.put(girl.name,girl);
            index_of_girl[i] = girl;
        }

        //读取 boy preference
        for(int i=0;i<pair_of_couple;i++){

            boy boy = index_of_boy[i];
            boy.preference_list = new girl[pair_of_couple];

            String line = scanner.nextLine();
            String[] preference = line.split(" ");

            for(int j=0;j<pair_of_couple;j++){
                girl girl = girls_map.get(preference[j]);
                boy.preference_list[j]=girl;
            }
        }

        //读取 girl preference
        for(int i=0;i<pair_of_couple;i++){

            girl girl= index_of_girl[i];
            girl.preference_list = new HashMap<>((int)(n / 0.75f) + 1);

            String line = scanner.nextLine();
            String[] preference = line.split(" ");

            for(int j=0;j<pair_of_couple;j++){
                boy boy = boys_map.get(preference[j]);
                girl.preference_list.put(boy,j);
            }
        }

        System.out.println("完成控制台测试样例的初始化");
    }
}


class boy{
    int pair_of_people = 0;
    String name =null;
    girl[] preference_list;
    int pointer = 0; //指向当前最喜欢的，且没追过的女生
    girl cur_lover = null; //当前的老婆
}

class girl{
    int pair_of_people = 0;
    String name = null;
    HashMap<boy,Integer> preference_list; //需要根据男孩，查询这个女生喜欢他的程度
    boy cur_lover = null;
}

