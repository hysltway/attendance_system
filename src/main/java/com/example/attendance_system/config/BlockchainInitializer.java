package com.example.attendance_system.config;

import com.example.attendance_system.dto.AttendanceRecordDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.service.BlockchainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块链数据初始化器
 * 用于系统启动时检查昨天的考勤记录是否已上传到区块链，如果没有则上传
 */
@Slf4j
@Configuration
public class BlockchainInitializer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private BlockchainService blockchainService;
    
    @Value("${blockchain.batch-size:20}")
    private int batchSize;
    
    /**
     * 创建命令行运行器，在应用启动时检查并上传昨天的考勤记录到区块链
     *
     * @return CommandLineRunner实例
     */
    @Bean
    public CommandLineRunner checkAndUploadAttendanceRecordsToBlockchain() {
        return args -> {
            log.info("开始检查昨天考勤记录是否已上传到区块链...");

            // 获取昨天的日期
            LocalDate yesterday = LocalDate.now().minusDays(1);
            String yesterdayStr = yesterday.format(DATE_FORMATTER);
            
            // 设置昨天的时间范围
            LocalDateTime startOfDay = yesterday.atStartOfDay();
            LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);
            
            // 查询昨天的所有考勤记录
            List<AttendanceRecord> yesterdayRecords = attendanceRecordRepository.findAllByCheckTimeBetween(
                    startOfDay, endOfDay);
            
            if (yesterdayRecords.isEmpty()) {
                log.info("昨天({})没有考勤记录，无需上传到区块链", yesterdayStr);
                return;
            }
            
            log.info("昨天({})共有{}条考勤记录需要检查", yesterdayStr, yesterdayRecords.size());
            
            // 检查每条记录是否已上传到区块链
            List<AttendanceRecordDTO> recordsToUpload = new ArrayList<>();
            int alreadyUploadedCount = 0;
            
            for (AttendanceRecord record : yesterdayRecords) {
                // 使用ID直接检查记录是否已经在区块链中
                if (record.getId() != null && blockchainService.getBlockchain() != null && 
                    blockchainService.verifyAttendanceRecord(record)) {
                    alreadyUploadedCount++;
                    continue;
                }
                
                // 转换为DTO
                AttendanceRecordDTO dto = convertToDTO(record);
                recordsToUpload.add(dto);
            }
            
            if (recordsToUpload.isEmpty()) {
                log.info("昨天({})的所有{}条考勤记录已上传到区块链，无需重复上传", 
                    yesterdayStr, alreadyUploadedCount);
                return;
            }
            
            log.info("昨天({})共有{}条考勤记录需要上传到区块链，{}条记录已存在", 
                yesterdayStr, recordsToUpload.size(), alreadyUploadedCount);
            
            // 批量上传到区块链
            if (recordsToUpload.size() <= batchSize) {
                // 如果记录数小于批次大小，直接上传
                blockchainService.addAttendanceRecords(recordsToUpload);
                log.info("成功上传{}条考勤记录到区块链", recordsToUpload.size());
            } else {
                // 如果记录数超过批次大小，分批上传
                int totalBatches = (recordsToUpload.size() + batchSize - 1) / batchSize; // 计算总批次数
                
                for (int i = 0; i < totalBatches; i++) {
                    int fromIndex = i * batchSize;
                    int toIndex = Math.min((i + 1) * batchSize, recordsToUpload.size());
                    
                    List<AttendanceRecordDTO> batchRecords = recordsToUpload.subList(fromIndex, toIndex);
                    blockchainService.addAttendanceRecords(batchRecords);
                    
                    log.info("成功上传第{}批考勤记录到区块链，共{}条", (i + 1), batchRecords.size());
                }
                
                log.info("分批上传完成，总共上传了{}条考勤记录", recordsToUpload.size());
            }
            
            log.info("昨天考勤记录上传区块链检查完成");
        };
    }
    
    /**
     * 将AttendanceRecord转换为AttendanceRecordDTO
     */
    private AttendanceRecordDTO convertToDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setEmployeeNo(record.getEmployeeNo());
        dto.setCheckTime(record.getCheckTime());
        dto.setCheckType(record.getCheckType());
        dto.setCheckMethod(record.getCheckMethod());
        dto.setStatus(record.getStatus());
        dto.setLocation(record.getRemark()); // 使用备注作为位置信息
        dto.setRemark(record.getRemark());
        return dto;
    }
} 