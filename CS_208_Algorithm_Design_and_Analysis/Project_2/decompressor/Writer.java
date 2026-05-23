package decompressor;

import java.io.FileOutputStream;
import java.io.IOException;

public class Writer {
    // 写入还原后的原始文件
    public void writeDecodedFile(byte[] decodedData, String destPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destPath)) {
            fos.write(decodedData);
        }
    }
}