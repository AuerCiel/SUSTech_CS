package decompressor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Decoder {
    
    // 输入被压缩的文件，输出解码还原后的原始 byte[]
    public byte[] decodeFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             DataInputStream dis = new DataInputStream(fis)//这个包装流可以精确读取int，byte，或者传入一个byte数组要求读满。非常方便
              ) {

            // 按照约定的结构读取 Header
            int contentSize = dis.readInt();
            byte padding = dis.readByte();
            int treeSize = dis.readInt();

            // 读取树结构内容并建树
            byte[] treeBytes = new byte[treeSize];
            dis.readFully(treeBytes);//读满树的数组
            HuffmanTree tree = new HuffmanTree();
            tree.buildTreeFromHeader(treeBytes);

            // 读取压缩 Payload，并交由树进行解码
            byte[] payload = new byte[contentSize];
            dis.readFully(payload);

            return tree.decode(payload, padding);
        }
    }
}