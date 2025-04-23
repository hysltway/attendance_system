package com.example.attendance_system.blockchain;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块链中的区块
 * 每个区块包含多个考勤记录数据，通过前一个区块的哈希值链接
 */
@Data
public class Block {
    // 区块的哈希值
    private String hash;
    // 前一个区块的哈希值
    private String previousHash;
    // 区块中存储的数据（考勤记录JSON字符串列表）
    private List<String> data;
    // 区块创建时间
    private LocalDateTime timestamp;
    // 工作量证明的随机数
    private int nonce;
    
    /**
     * 创建新区块
     * @param previousHash 前一个区块的哈希值
     */
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.data = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
        this.nonce = 0;
        this.hash = calculateHash();
    }
    
    /**
     * 计算区块的哈希值
     * 使用SHA-256算法，基于区块的所有属性计算
     * @return 区块的哈希值
     */
    public String calculateHash() {
        String dataToHash = previousHash + timestamp.toString() + 
                            Integer.toString(nonce) + data.toString();
        MessageDigest digest;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        StringBuilder buffer = new StringBuilder();
        for (byte b : bytes) {
            buffer.append(String.format("%02x", b));
        }
        return buffer.toString();
    }
    
    /**
     * 挖矿方法 - 工作量证明
     * 通过不断调整nonce值，使得计算的哈希值前difficulty个字符为0
     * @param difficulty 难度值
     */
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("区块已挖矿完成: " + hash);
    }
    
    /**
     * 向区块中添加数据
     * @param record 考勤记录JSON字符串
     */
    public void addData(String record) {
        data.add(record);
    }
} 