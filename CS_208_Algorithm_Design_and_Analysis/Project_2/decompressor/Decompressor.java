package decompressor;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

public class Decompressor {
    public static void main(String[] args) {
        // 你可以手动修改这两个文件夹的路径
        String sourceDirPath = "C:\\Users\\Akira\\Desktop\\CS_208_project_2\\output";
        String targetDirPath = "C:\\Users\\Akira\\Desktop\\CS_208_project_2\\output";

        File sourceDir = new File(sourceDirPath);
        File targetDir = new File(targetDirPath);

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        //为了鲁棒性，检查了是不是文件夹类型，是不是空的，文件是不是文件，文件是不是空的
        if (sourceDir.isDirectory()) {
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".huff")) {
                        System.out.println("正在解压: " + file.getName());
                        try {
                            Decoder decoder = new Decoder();
                            byte[] originalData = decoder.decodeFile(file);

                            Writer writer = new Writer();
                            // 去掉 ".huff" 后缀还原名字
                            String originalName = file.getName().substring(0, file.getName().length() - 5);
                            String destPath = targetDirPath + File.separator + originalName;

                            writer.writeDecodedFile(originalData, destPath);
                            System.out.println("解压完成 -> " + destPath);


                        } catch (EOFException e){//这一段专门处理empty文件
                            System.err.println(file.getName() +  "警告：文件为空");
                            String originalName = file.getName().substring(0, file.getName().length() - 5);
                            String destPath = targetDirPath + File.separator + originalName;
                            try {
                                new File(destPath).createNewFile();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            System.out.println("解压完成 -> " + destPath);
                        } catch (Exception e) {
                            System.err.println("解压 " + file.getName() + " 时发生错误:");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.err.println("压缩文件夹不存在或路径错误。");
        }
    }
}