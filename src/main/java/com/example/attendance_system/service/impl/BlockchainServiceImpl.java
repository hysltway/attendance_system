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
        try {
            log.info("初始化区块链服务...");
            boolean loaded = loadBlockchain(); // 尝试从文件加载区块链数据
            
            if (loaded) {
                log.info("成功从文件加载区块链数据，当前链长度: {}", blockchain.getChain().size());
                log.info("考勤记录映射表中包含 {} 条记录", recordBlockMapping.size());
            } else {
                log.warn("无法从文件加载区块链数据，已创建新的区块链实例");
                // 确保创建了新的区块链实例
                if (blockchain == null) {
                    blockchain = new Blockchain(difficulty);
                }
                saveBlockchain(); // 保存空区块链，确保文件存在
            }
        } catch (Exception e) {
            log.error("初始化区块链服务失败", e);
            // 确保创建了新的区块链实例
            blockchain = new Blockchain(difficulty);
        }
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
        log.info("开始重建考勤记录区块映射...");
        
        int totalRecords = 0;
        for (Block block : blockchain.getChain()) {
            // 跳过创世区块
            if (block.getPreviousHash() == null || block.getPreviousHash().equals("0")) {
                log.debug("跳过创世区块，哈希值: {}", block.getHash());
                continue;
            }
            
            // 解析区块数据
            List<String> dataList = block.getData();
            if (dataList == null || dataList.isEmpty()) {
                log.debug("区块数据为空，跳过。区块哈希: {}", block.getHash());
                continue;
            }
            
            try {
                for (String dataStr : dataList) {
                    if (dataStr == null || dataStr.isEmpty()) {
                        continue;
                    }
                    
                    // 尝试将JSON字符串解析为对象
                    try {
                        // 尝试解析为AttendanceRecordDTO列表
                        if (dataStr.startsWith("[")) {
                            List<?> records = objectMapper.readValue(dataStr, List.class);
                            for (Object obj : records) {
                                processRecordObject(obj, block.getHash());
                                totalRecords++;
                            }
                        } 
                        // 尝试解析为单个AttendanceRecordDTO
                        else {
                            Object record = objectMapper.readValue(dataStr, Object.class);
                            processRecordObject(record, block.getHash());
                            totalRecords++;
                        }
                    } catch (Exception e) {
                        log.warn("解析区块数据JSON失败，尝试其他格式解析。区块哈希: {}，错误: {}", block.getHash(), e.getMessage());
                        // 如果不是有效的JSON，尝试直接处理字符串
                        if (dataStr.contains("id") && dataStr.contains(":")) {
                            try {
                                // 简单解析包含ID的字符串
                                String idPattern = "\"id\"\\s*:\\s*(\\d+)";
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(idPattern);
                                java.util.regex.Matcher matcher = pattern.matcher(dataStr);
                                if (matcher.find()) {
                                    String idStr = matcher.group(1);
                                    Long recordId = Long.valueOf(idStr);
                                    recordBlockMapping.put(recordId, block.getHash());
                                    log.debug("通过正则表达式提取到记录ID {} 映射到区块 {}", recordId, block.getHash());
                                    totalRecords++;
                                }
                            } catch (Exception ex) {
                                log.error("通过正则表达式提取记录ID失败，字符串: {}", dataStr, ex);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("处理区块数据失败，区块哈希: " + block.getHash(), e);
            }
        }
        
        log.info("考勤记录区块映射重建完成，共映射 {} 条记录", recordBlockMapping.size());
    }
    
    /**
     * 处理记录对象，提取ID并建立映射
     * @param obj 记录对象
     * @param blockHash 区块哈希
     */
    private void processRecordObject(Object obj, String blockHash) {
        try {
            // 处理Map类型
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                if (map.containsKey("id")) {
                    Object idValue = map.get("id");
                    if (idValue != null) {
                        Long recordId = null;
                        if (idValue instanceof Number) {
                            recordId = ((Number) idValue).longValue();
                        } else {
                            recordId = Long.valueOf(idValue.toString());
                        }
                        recordBlockMapping.put(recordId, blockHash);
                        log.debug("映射记录ID {} 到区块 {}", recordId, blockHash);
                    }
                }
            } 
            // 处理AttendanceRecordDTO类型
            else if (obj instanceof AttendanceRecordDTO) {
                AttendanceRecordDTO dto = (AttendanceRecordDTO) obj;
                if (dto.getId() != null) {
                    recordBlockMapping.put(dto.getId(), blockHash);
                    log.debug("映射记录ID {} 到区块 {}", dto.getId(), blockHash);
                }
            }
            // 处理其他类型的对象，尝试使用反射获取ID字段
            else {
                try {
                    // 使用反射获取id字段
                    java.lang.reflect.Field idField = obj.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idValue = idField.get(obj);
                    if (idValue != null) {
                        Long recordId = null;
                        if (idValue instanceof Number) {
                            recordId = ((Number) idValue).longValue();
                        } else {
                            recordId = Long.valueOf(idValue.toString());
                        }
                        recordBlockMapping.put(recordId, blockHash);
                        log.debug("通过反射映射记录ID {} 到区块 {}", recordId, blockHash);
                    }
                } catch (Exception e) {
                    log.debug("对象不包含id字段或无法访问", e);
                }
            }
        } catch (Exception e) {
            log.warn("处理记录对象失败: {}", e.getMessage());
        }
    }
    
    /**
     * 根据哈希查找区块
     * @param hash 区块哈希
     * @return 区块
     */
    private Block findBlockByHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return null;
        }
        
        for (Block block : blockchain.getChain()) {
            if (block.getHash().equals(hash)) {
                return block;
            }
        }
        
        return null;
    }
    
    @Override
    public int uploadHistoricalRecords(int batchSize) {
        int totalRecords = 0;
        int page = 0;
        int skippedRecords = 0;
        
        log.info("开始上传历史考勤记录到区块链，每批次{}条", batchSize);
        
        while (true) {
            Pageable pageable = PageRequest.of(page, batchSize);
            Page<AttendanceRecord> records = attendanceRecordRepository.findAll(pageable);
            
            if (records.isEmpty()) {
                break;
            }
            
            List<AttendanceRecordDTO> dtoList = new ArrayList<>();
            for (AttendanceRecord record : records.getContent()) {
                // 检查记录是否已经存在于区块链中
                if (record.getId() != null && recordBlockMapping.containsKey(record.getId())) {
                    // 记录已存在，跳过
                    skippedRecords++;
                    continue;
                }
                
                // 转换为DTO并添加到上传列表
                AttendanceRecordDTO dto = convertToDTO(record);
                dtoList.add(dto);
            }
            
            // 如果有记录需要上传，则添加到区块链
            if (!dtoList.isEmpty()) {
                addAttendanceRecords(dtoList);
                totalRecords += dtoList.size();
                log.info("已上传{}条历史考勤记录到区块链", totalRecords);
            }
            
            page++;
        }
        
        log.info("历史考勤记录上传完成，共上传{}条记录，跳过{}条已存在记录", totalRecords, skippedRecords);
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
        if (record == null || record.getId() == null) {
            log.warn("无法验证空记录或没有ID的记录");
            return false;
        }
        
        log.debug("开始验证考勤记录是否上链, ID: {}", record.getId());
        
        // 将实体转换为DTO
        AttendanceRecordDTO recordDTO = convertToDTO(record);
        
        // 检查记录ID是否在映射中
        if (!recordBlockMapping.containsKey(record.getId())) {
            log.debug("考勤记录 ID:{} 不在记录区块映射中，未上链", record.getId());
            return false;
        }
        
        // 获取包含此记录的区块哈希
        String blockHash = recordBlockMapping.get(record.getId());
        if (blockHash == null || blockHash.isEmpty()) {
            log.warn("考勤记录 ID:{} 在映射中但区块哈希为空", record.getId());
            return false;
        }
        
        // 根据哈希查找区块
        Block block = findBlockByHash(blockHash);
        if (block == null) {
            log.warn("找不到哈希为 {} 的区块，记录 ID:{}", blockHash, record.getId());
            return false;
        }
        
        // 检查区块中是否包含此记录
        try {
            List<String> dataList = block.getData();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("区块 {} 数据为空，记录 ID:{}", blockHash, record.getId());
                return false;
            }
            
            for (String dataStr : dataList) {
                try {
                    // 尝试解析为列表
                    if (dataStr.startsWith("[")) {
                        List<?> records = objectMapper.readValue(dataStr, List.class);
                        for (Object obj : records) {
                            if (isRecordMatch(obj, record.getId())) {
                                log.debug("考勤记录 ID:{} 在区块 {} 中找到，已上链", record.getId(), blockHash);
                                return true;
                            }
                        }
                    } 
                    // 尝试解析为单个对象
                    else {
                        Object obj = objectMapper.readValue(dataStr, Object.class);
                        if (isRecordMatch(obj, record.getId())) {
                            log.debug("考勤记录 ID:{} 在区块 {} 中找到，已上链", record.getId(), blockHash);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析区块数据失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("验证考勤记录时出错，记录ID: " + record.getId() + ", 区块哈希: " + blockHash, e);
            return false;
        }
        
        log.warn("考勤记录 ID:{} 在映射中存在，但在区块 {} 中未找到对应数据", record.getId(), blockHash);
        return false;
    }

    /**
     * 判断数据对象是否匹配指定ID
     * @param obj 数据对象
     * @param recordId 记录ID
     * @return 是否匹配
     */
    private boolean isRecordMatch(Object obj, Long recordId) {
        if (obj == null || recordId == null) {
            return false;
        }
        
        try {
            // 处理Map类型
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                if (map.containsKey("id") && map.get("id") != null) {
                    Long id = null;
                    Object idValue = map.get("id");
                    if (idValue instanceof Number) {
                        id = ((Number) idValue).longValue();
                    } else {
                        id = Long.valueOf(idValue.toString());
                    }
                    return recordId.equals(id);
                }
            } 
            // 处理AttendanceRecordDTO类型
            else if (obj instanceof AttendanceRecordDTO) {
                AttendanceRecordDTO dto = (AttendanceRecordDTO) obj;
                return dto.getId() != null && dto.getId().equals(recordId);
            }
            // 尝试使用反射获取id字段
            else {
                try {
                    java.lang.reflect.Field idField = obj.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idValue = idField.get(obj);
                    if (idValue != null) {
                        Long id = null;
                        if (idValue instanceof Number) {
                            id = ((Number) idValue).longValue();
                        } else {
                            id = Long.valueOf(idValue.toString());
                        }
                        return recordId.equals(id);
                    }
                } catch (Exception e) {
                    // 忽略反射异常
                }
            }
        } catch (Exception e) {
            log.debug("检查记录匹配时出错: {}", e.getMessage());
        }
        
        return false;
    }

    /**
     * 验证考勤记录DTO是否已上链并且未被篡改
     * @param record 考勤记录DTO
     * @return 验证结果
     */
    public boolean verifyAttendanceRecordDTO(AttendanceRecordDTO record) {
        if (record == null || record.getId() == null) {
            log.warn("无法验证空记录或没有ID的记录");
            return false;
        }
        
        log.debug("开始验证考勤记录DTO是否上链, ID: {}", record.getId());
        
        // 检查记录ID是否在映射中
        if (!recordBlockMapping.containsKey(record.getId())) {
            log.debug("考勤记录 ID:{} 不在记录区块映射中，未上链", record.getId());
            return false;
        }
        
        // 获取包含此记录的区块哈希
        String blockHash = recordBlockMapping.get(record.getId());
        if (blockHash == null || blockHash.isEmpty()) {
            log.warn("考勤记录 ID:{} 在映射中但区块哈希为空", record.getId());
            return false;
        }
        
        // 根据哈希查找区块
        Block block = findBlockByHash(blockHash);
        if (block == null) {
            log.warn("找不到哈希为 {} 的区块，记录 ID:{}", blockHash, record.getId());
            return false;
        }
        
        // 检查区块中是否包含此记录
        try {
            List<String> dataList = block.getData();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("区块 {} 数据为空，记录 ID:{}", blockHash, record.getId());
                return false;
            }
            
            for (String dataStr : dataList) {
                try {
                    // 尝试解析为列表
                    if (dataStr.startsWith("[")) {
                        List<?> records = objectMapper.readValue(dataStr, List.class);
                        for (Object obj : records) {
                            if (isRecordMatch(obj, record.getId())) {
                                log.debug("考勤记录 ID:{} 在区块 {} 中找到，已上链", record.getId(), blockHash);
                                return true;
                            }
                        }
                    } 
                    // 尝试解析为单个对象
                    else {
                        Object obj = objectMapper.readValue(dataStr, Object.class);
                        if (isRecordMatch(obj, record.getId())) {
                            log.debug("考勤记录 ID:{} 在区块 {} 中找到，已上链", record.getId(), blockHash);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析区块数据失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("验证考勤记录时出错，记录ID: " + record.getId() + ", 区块哈希: " + blockHash, e);
            return false;
        }
        
        log.warn("考勤记录 ID:{} 在映射中存在，但在区块 {} 中未找到对应数据", record.getId(), blockHash);
        return false;
    }
} 