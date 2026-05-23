package compressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Reader {
    private byte padding;
    private byte[] serializedTree;   //就是编码之后的树

    // 输入是一个文件，输出是编码后的byte数组
    public byte[] readAndEncode(File file) throws IOException {
        //读出源文件，变成byte数组对象
        byte[] sourceBytes = Files.readAllBytes(file.toPath());
        if (sourceBytes.length == 0) return new byte[0];

        //输入源文件的byte数组，构建霍夫曼树，并且构建编码后的霍夫曼树
        HuffmanTree tree = new HuffmanTree();
        tree.buildTree(sourceBytes);
        this.serializedTree = tree.getSerializedTree();

        //将字节转换为byte流，并打包成新的 byte 数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();//本质上是一个byte数组，并且提供了很多操作这个数组的API。数组扩容的开销到了100MB才会很明显
        int currentByte = 0;//当前byte的值
        int bitCount = 0;//记录当前处理了几个bit

        for (byte b : sourceBytes) {
            //先从源byte数组取出一个byte，然后利用树获取它的Huffman码/
            String code = tree.getCodes().get(b);
            for (char c : code.toCharArray()) {
                //然后一位位把霍夫曼码写入当前byte。
                currentByte = (currentByte << 1) | (c - '0');
                //二进制拼接操作。
                // c - '0'是为了把字符 '0'/'1' 转成数字 0/1。
                //“ |”是按位或，把新比特拼进去
                bitCount++;
                if (bitCount == 8) {
                    bos.write(currentByte);//这一步其实不是IO，只是写到了stream内部的数组罢了。开销很小
                    currentByte = 0;
                    bitCount = 0;
                }
            }
        }

        //处理最后不足8位的情况（计算 Padding 并补齐）
        if (bitCount > 0) {
            this.padding = (byte) (8 - bitCount);
            currentByte = currentByte << padding; // 尾部补零
            bos.write(currentByte);
        } else {
            this.padding = 0;
        }

        return bos.toByteArray();
    }

    public byte getPadding() {
        return padding;
    }

    public byte[] getSerializedTree() {
        return serializedTree;
    }
}