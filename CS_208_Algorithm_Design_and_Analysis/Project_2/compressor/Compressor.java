package compressor;

import java.io.File;


public class Compressor {
    public static void main(String[] args) {
        // 你可以手动修改这两个文件夹的路径
        String sourceDirPath = "C:\\Users\\Akira\\Desktop\\CS_208_project_2\\testcases";
        String targetDirPath = "C:\\Users\\Akira\\Desktop\\CS_208_project_2\\output";

        File sourceDir = new File(sourceDirPath);
        File targetDir = new File(targetDirPath);

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        //为了
        if (sourceDir.isDirectory()) {
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            System.out.println("正在压缩: " + file.getName());
                            Reader reader = new Reader();
                            byte[] compressedPayload = reader.readAndEncode(file);

                            Writer writer = new Writer();
                            String destPath = targetDirPath + File.separator + file.getName() + ".huff";

                            writer.writeCompressedFile(
                                    compressedPayload,
                                    reader.getPadding(),
                                    reader.getSerializedTree(),
                                    destPath
                            );
                            System.out.println("压缩完成 -> " + file.getName()+"\n\n");
                        }catch (NullPointerException e) {
                            System.err.println(file.getName() + "警告：文件为空"+"\n\n");
                        }
                        catch (Exception e) {
                            System.err.println("压缩 " + file.getName() + " 时发生错误:");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.err.println("源文件夹不存在或路径错误。");
        }
    }
}