package compressor;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Writer {
    
    // 将 Header 与压缩内容一并写入文件
    public void writeCompressedFile(byte[] payload, byte padding, byte[] treeBytes, String destPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destPath);
             DataOutputStream dos = new DataOutputStream(fos)) {
             
            // 按照约定的头部结构写入
            dos.writeInt(payload.length);   // 4 bytes: 压缩内容的长度
            dos.writeByte(padding);         // 1 byte:  Padding 数
            dos.writeInt(treeBytes.length); // 4 bytes: 树结构占用的字节数
            
            // 写入树结构
            dos.write(treeBytes);
            // 写入压缩后的内容（包含补零的最后字节）
            dos.write(payload);
        }
    }
}