package compressor;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanTree {
    
    // 树的节点类
    public static class Node implements Comparable<Node> {
        public Byte data; //数据（内部节点为null）————————就是编码前的byte
        public int weight; //权重（频次）
        public Node left;
        public Node right;

        public Node(Byte data, int weight) {
            this.data = data;
            this.weight = weight;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.weight, o.weight);
        }
    }

    private Node root;
    private final Map<Byte, String> huffmanCodes = new HashMap<>();//构建映射，方便快速根据byte值查出相应的huffman编码

    //根据原始字节数据构建霍夫曼树
    public void buildTree(byte[] sourceData) {
        if (sourceData == null || sourceData.length == 0) return;

        //统计频次
        Map<Byte, Integer> freqMap = new HashMap<>();
        for (byte b : sourceData) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);//有就加一，没有就创建然后赋默认值0
        }

        //将叶子节点放入优先队列
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));//创建node
        }

        // 处理特殊情况：文件中只有一种字符——————创建一个空根，然后唯一的node作为root唯一的child
        if (pq.size() == 1) {
            root = new Node(null, pq.peek().weight);
            root.left = pq.poll();
        } else {
            //构建树
            while (pq.size() > 1) {
                Node left = pq.poll();
                Node right = pq.poll();
                if(right==null)break;//防止黄色警报————————————我知道根本不可能right是null
                Node parent = new Node(null, left.weight + right.weight);
                parent.left = left;
                parent.right = right;
                pq.add(parent);
            }
            root = pq.poll();//剩下唯一一个node就是root
        }

        //生成编码表
        generateCodes(root, "", new StringBuilder());
    }

    //递归地访问树的每一个节点。leaf节点作为base情况
    private void generateCodes(Node node, String code, StringBuilder sb) {
        StringBuilder currentPath = new StringBuilder(sb).append(code);
        if (node != null) {//huffman树的每一个内部节点必然有两个child。其实不可能遍历到空的node。。。。just in case
            if (node.data == null) {
                generateCodes(node.left, "0", currentPath);
                generateCodes(node.right, "1", currentPath);
            } else {
                huffmanCodes.put(node.data, currentPath.toString());//表示到了leaf。不再进行遍历。存入map
            }
        }
    }



    // 前序遍历序列化树结构: 0表示内部节点, 1表示叶子节点(后接1 byte数据)
    public byte[] getSerializedTree() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializePreOrder(root, bos);
        return bos.toByteArray();
    }
    //真正前序遍历的部分————————一个内部node只占据一个byte，一个leaf占据两个byte
    private void serializePreOrder(Node node, ByteArrayOutputStream bos) {
        if (node == null) return;
        if (node.data == null) {
            bos.write(0); // 内部节点
            serializePreOrder(node.left, bos);
            serializePreOrder(node.right, bos);
        } else {
            bos.write(1); // 叶子节点
            bos.write(node.data);
        }
    }

    public Map<Byte, String> getCodes() {
        return huffmanCodes;
    }
}