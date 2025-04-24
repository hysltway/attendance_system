package com.example.attendance_system.service;

import com.example.attendance_system.blockchain.Block;
import com.example.attendance_system.blockchain.Blockchain;
import com.example.attendance_system.dto.AttendanceRecordDTO;
import com.example.attendance_system.entity.AttendanceRecord;

import java.util.List;

/**
 * 区块链服务接口
 * 定义了区块链操作的方法
 */
public interface BlockchainService {

    /**
     * 获取当前区块链实例
     * @return Blockchain 区块链实例
     */
    Blockchain getBlockchain();
    
    /**
     * 添加新区块到区块链
     * @param data 区块数据
     * @return 添加的区块
     */
    Block addBlock(Object data);
    
    /**
     * 添加考勤记录到区块链
     * @param record 考勤记录
     * @return 添加的区块
     */
    Block addAttendanceRecord(AttendanceRecordDTO record);
    
    /**
     * 批量添加考勤记录到区块链
     * @param records 考勤记录列表
     * @return 添加的区块
     */
    Block addAttendanceRecords(List<AttendanceRecordDTO> records);
    
    /**
     * 验证区块链是否有效
     * @return 是否有效
     */
    boolean verifyBlockchain();
    
    /**
     * 保存区块链数据到文件
     * @return 是否保存成功
     */
    boolean saveBlockchain();
    
    /**
     * 从文件加载区块链数据
     * @return 是否加载成功
     */
    boolean loadBlockchain();
    
    /**
     * 上传历史考勤记录到区块链
     * @param batchSize 每批次处理的记录数
     * @return 上传的记录总数
     */
    int uploadHistoricalRecords(int batchSize);
    
    /**
     * 验证考勤记录是否已上链并且未被篡改
     * @param record 考勤记录
     * @return 验证结果
     */
    boolean verifyAttendanceRecord(AttendanceRecord record);
} 