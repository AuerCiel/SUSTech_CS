package decompressor;

import java.io.ByteArrayOutputStream;

public class HuffmanTree {
    //节点内部没必要储存霍夫曼编码。树的结构本身就包含了霍夫曼编码的信息
    static class Node {
        public Byte data;//存的是当前节点的数据
        public Node left;
        public Node right;

        public Node(Byte data) {
            this.data = data;
        }
    }

    private Node root;
    private int pointer = 0; // 反序列化时的字节指针

    // 根据压缩文件Header中的树结构重构树
    public void buildTreeFromHeader(byte[] treeBytes) {
        pointer = 0;
        root = deserialize(treeBytes);
    }

    private Node deserialize(byte[] treeBytes) {
        if (pointer >= treeBytes.length) return null;
        
        byte type = treeBytes[pointer++];
        if (type == 0) {
            Node node = new Node(null); // 内部节点
            node.left = deserialize(treeBytes);
            node.right = deserialize(treeBytes);
            return node;
        } else {
            return new Node(treeBytes[pointer++]); // 叶子节点
        }
    }

    // 利用霍夫曼树进行二进制级别的解码
    public byte[] decode(byte[] payload, byte padding) {
        if (payload.length == 0) return new byte[0];//防止文件位空的情况

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Node current = root;

        for (int i = 0; i < payload.length; i++) {
            byte b = payload[i];
            //如果是最后一个 byte，需要去掉无用的 padding 位
            int validBits = (i == payload.length - 1) ? (8 - padding) : 8;
            
            //从高位到低位逐位读取 (1 byte = 8 bits)
            for (int j = 7; j >= 8 - validBits; j--) {
                int bit = (b >> j) & 1; // 提取指定位
                
                if (bit == 0) {
                    current = current.left;
                } else {
                    current = current.right;
                }

                // 到达叶子节点，输出原字节，并将指针重置回根节点
                if (current.data != null) {
                    bos.write(current.data);
                    current = root;
                }
            }
        }
        return bos.toByteArray();
    }
}