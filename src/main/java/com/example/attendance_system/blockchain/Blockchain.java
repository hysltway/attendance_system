package com.example.attendance_system.blockchain;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块链管理类
 * 管理区块集合，提供添加区块和验证区块链的功能
 */
@Data
public class Blockchain {
    // 区块链
    private List<Block> chain;
    // 挖矿难度
    private int difficulty;
    
    /**
     * 创建区块链
     * @param difficulty 挖矿难度
     */
    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        // 创建创世区块
        createGenesisBlock();
    }
    
    /**
     * 创建创世区块
     * 区块链的第一个区块，没有前一个区块
     */
    private void createGenesisBlock() {
        Block genesisBlock = new Block("0");
        genesisBlock.addData("创世区块 - 考勤系统区块链初始化");
        genesisBlock.mineBlock(difficulty);
        chain.add(genesisBlock);
        System.out.println("创世区块已创建: " + genesisBlock.getHash());
    }
    
    /**
     * 获取区块链中的最新区块
     * @return 最新区块
     */
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }
    
    /**
     * 添加新区块到区块链
     * @param newBlock 新区块
     */
    public void addBlock(Block newBlock) {
        newBlock.setPreviousHash(getLatestBlock().getHash());
        newBlock.setHash(newBlock.calculateHash());
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
    }
    
    /**
     * 验证区块链的有效性
     * 检查每个区块的哈希值和与前一个区块的链接是否正确
     * @return 区块链是否有效
     */
    public boolean isChainValid() {
        // 检查每个区块
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);
            
            // 检查当前区块的哈希值是否正确
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("区块 " + i + " 的哈希值无效");
                return false;
            }
            
            // 检查当前区块的previousHash是否正确指向前一个区块
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("区块 " + i + " 与前一个区块的链接无效");
                return false;
            }
        }
        
        return true;
    }
} 