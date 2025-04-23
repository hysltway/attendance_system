package com.example.attendance_system.service.impl;

import com.example.attendance_system.blockchain.Block;
import com.example.attendance_system.blockchain.Blockchain;
import com.example.attendance_system.dto.AttendanceRecordDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.service.BlockchainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 区块链服务实现类
 */
@Service
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    private Blockchain blockchain;
    private final ObjectMapper objectMapper;
    
    // 记录哪些考勤记录已经上链，以及它们在哪个区块中
    private final Map<Long, String> recordBlockMapping = new ConcurrentHashMap<>();
    
    @Value("${blockchain.file.path:blockchain.json}")
    private String blockchainFilePath;
    
    @Value("${blockchain.difficulty:4}")
    private int difficulty;

    public BlockchainServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.blockchain = new Blockchain(4); // 默认难度为4
        // 注意：不要在构造函数中调用loadBlockchain()，因为@Value还未被注入
    }
    
    /**
     * 初始化方法，在所有属性注入完成后执行
     */
    @PostConstruct
    public void init() {
        log.info("初始化区块链服务...");
        loadBlockchain(); // 尝试从文件加载区块链数据
    }

    @Override
    public Blockchain getBlockchain() {
        return blockchain;
    }

    @Override
    public Block addBlock(Object data) {
        log.info("添加新区块到区块链，数据: {}", data);
        // 创建新区块
        Block newBlock = new Block(blockchain.getLatestBlock().getHash());
        // 如果数据是JSON字符串，直接添加
        if (data instanceof String) {
            newBlock.addData((String) data);
        } else {
            // 尝试将对象转换为JSON字符串
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                newBlock.addData(jsonData);
            } catch (Exception e) {
                log.error("转换数据为JSON失败", e);
                newBlock.addData(data.toString());
            }
        }
        
        // 添加区块到区块链
        blockchain.addBlock(newBlock);
        saveBlockchain(); // 保存区块链
        return newBlock;
    }

    @Override
    public Block addAttendanceRecord(AttendanceRecordDTO record) {
        log.info("添加考勤记录到区块链: {}", record);
        Block block = addBlock(record);
        
        // 记录区块映射关系
        if (record.getId() != null) {
            recordBlockMapping.put(record.getId(), block.getHash());
        }
        
        return block;
    }

    @Override
    public Block addAttendanceRecords(List<AttendanceRecordDTO> records) {
        log.info("批量添加考勤记录到区块链, 记录数: {}", records.size());
        Block block = addBlock(records);
        
        // 记录区块映射关系
        for (AttendanceRecordDTO record : records) {
            if (record.getId() != null) {
                recordBlockMapping.put(record.getId(), block.getHash());
            }
        }
        
        return block;
    }

    @Override
    public boolean verifyBlockchain() {
        log.info("验证区块链有效性");
        return blockchain.isChainValid();
    }

    @Override
    public boolean saveBlockchain() {
        try {
            log.info("保存区块链数据到文件: {}", blockchainFilePath);
            objectMapper.writeValue(new File(blockchainFilePath), blockchain);
            return true;
        } catch (IOException e) {
            log.error("保存区块链数据失败", e);
            return false;
        }
    }

    @Override
    public boolean loadBlockchain() {
        // 检查属性是否已注入
        if (blockchainFilePath == null) {
            log.warn("区块链文件路径未配置，使用默认值");
            blockchainFilePath = "blockchain.json";
        }
        
        File file = new File(blockchainFilePath);
        log.info("尝试从文件加载区块链数据: {}", file.getAbsolutePath());
        
        if (!file.exists()) {
            log.info("区块链文件不存在，使用新的区块链实例");
            blockchain = new Blockchain(difficulty);
            return false;
        }
        
        try {
            log.info("从文件加载区块链数据: {}", blockchainFilePath);
            blockchain = objectMapper.readValue(file, Blockchain.class);
            
            // 重建记录区块映射
            rebuildRecordBlockMapping();
            
            return true;
        } catch (IOException e) {
            log.error("加载区块链数据失败", e);
            blockchain = new Blockchain(difficulty); // 如果加载失败，创建新的区块链
            return false;
        }
    }
    
    /**
     * 重建记录区块映射
     */
    private void rebuildRecordBlockMapping() {
        recordBlockMapping.clear();
        
        for (Block block : blockchain.getChain()) {
            // 跳过创世区块
            if (block.getPreviousHash() == null || block.getPreviousHash().isEmpty()) {
                continue;
            }
            
            // 解析区块数据
            Object data = block.getData();
            try {
                if (data instanceof List) {
                    // 批量记录
                    List<?> records = (List<?>) data;
                    for (Object obj : records) {
                        if (obj instanceof AttendanceRecordDTO) {
                            AttendanceRecordDTO dto = (AttendanceRecordDTO) obj;
                            if (dto.getId() != null) {
                                recordBlockMapping.put(dto.getId(), block.getHash());
                            }
                        }
                    }
                } else if (data instanceof AttendanceRecordDTO) {
                    // 单条记录
                    AttendanceRecordDTO dto = (AttendanceRecordDTO) data;
                    if (dto.getId() != null) {
                        recordBlockMapping.put(dto.getId(), block.getHash());
                    }
                }
            } catch (Exception e) {
                log.error("解析区块数据失败", e);
            }
        }
    }
    
    @Override
    public int uploadHistoricalRecords(int batchSize) {
        int totalRecords = 0;
        int page = 0;
        
        log.info("开始上传历史考勤记录到区块链，每批次{}条", batchSize);
        
        while (true) {
            Pageable pageable = PageRequest.of(page, batchSize);
            Page<AttendanceRecord> records = attendanceRecordRepository.findAll(pageable);
            
            if (records.isEmpty()) {
                break;
            }
            
            List<AttendanceRecordDTO> dtoList = new ArrayList<>();
            for (AttendanceRecord record : records.getContent()) {
                // 转换为DTO
                AttendanceRecordDTO dto = convertToDTO(record);
                dtoList.add(dto);
            }
            
            // 添加到区块链
            addAttendanceRecords(dtoList);
            
            totalRecords += dtoList.size();
            page++;
            
            log.info("已上传{}条历史考勤记录到区块链", totalRecords);
        }
        
        log.info("历史考勤记录上传完成，共上传{}条记录", totalRecords);
        return totalRecords;
    }
    
    /**
     * 转换实体为DTO
     */
    private AttendanceRecordDTO convertToDTO(AttendanceRecord record) {
        // 这里简单实现，实际项目中可能需要使用MapStruct等工具
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        // 员工编号
        dto.setEmployeeNo(record.getEmployeeNo());
        // 打卡时间
        dto.setCheckTime(record.getCheckTime());
        // 打卡类型
        dto.setCheckType(record.getCheckType());
        // 位置信息
        dto.setLocation(record.getRemark()); // 使用remark字段作为location
        // 备注
        dto.setRemark(record.getRemark());
        return dto;
    }
    
    @Override
    public boolean verifyAttendanceRecord(AttendanceRecord record) {
        // 如果记录未上链，返回false
        if (record.getId() == null || !recordBlockMapping.containsKey(record.getId())) {
            log.info("考勤记录未上链，ID: {}", record.getId());
            return false;
        }
        
        // 获取记录所在的区块哈希
        String blockHash = recordBlockMapping.get(record.getId());
        
        // 查找对应区块
        Block targetBlock = null;
        for (Block block : blockchain.getChain()) {
            if (block.getHash().equals(blockHash)) {
                targetBlock = block;
                break;
            }
        }
        
        if (targetBlock == null) {
            log.error("无法找到考勤记录对应的区块，记录ID: {}, 区块哈希: {}", record.getId(), blockHash);
            return false;
        }
        
        // 将记录转换为DTO
        AttendanceRecordDTO recordDTO = convertToDTO(record);
        
        // 在区块中查找记录
        Object blockData = targetBlock.getData();
        if (blockData instanceof List) {
            // 批量记录
            List<?> records = (List<?>) blockData;
            for (Object obj : records) {
                if (obj instanceof AttendanceRecordDTO) {
                    AttendanceRecordDTO dto = (AttendanceRecordDTO) obj;
                    if (dto.getId().equals(recordDTO.getId())) {
                        // 比较记录内容
                        return compareRecords(dto, recordDTO);
                    }
                }
            }
        } else if (blockData instanceof AttendanceRecordDTO) {
            // 单条记录
            AttendanceRecordDTO dto = (AttendanceRecordDTO) blockData;
            if (dto.getId().equals(recordDTO.getId())) {
                // 比较记录内容
                return compareRecords(dto, recordDTO);
            }
        }
        
        log.error("在区块中未找到考勤记录，记录ID: {}", record.getId());
        return false;
    }
    
    /**
     * 比较两个考勤记录是否一致
     */
    private boolean compareRecords(AttendanceRecordDTO block, AttendanceRecordDTO current) {
        if (!block.getId().equals(current.getId())) return false;
        if (block.getUserId() != null && current.getUserId() != null && !block.getUserId().equals(current.getUserId())) return false;
        if (block.getCheckTime() != null && current.getCheckTime() != null && !block.getCheckTime().equals(current.getCheckTime())) return false;
        if (block.getCheckType() != null && current.getCheckType() != null && !block.getCheckType().equals(current.getCheckType())) return false;
        
        // 位置和备注可能为空
        if (block.getLocation() != null && current.getLocation() != null && !block.getLocation().equals(current.getLocation())) return false;
        if (block.getRemark() != null && current.getRemark() != null && !block.getRemark().equals(current.getRemark())) return false;
        
        return true;
    }
} 