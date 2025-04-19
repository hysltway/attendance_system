package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 人脸识别考勤打卡数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceRecognitionDTO {
    /**
     * 打卡状态：success-成功，error-失败
     */
    private String status;
    
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 员工姓名
     */
    private String name;
    
    /**
     * 打卡时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    private Integer checkMethod;
    
    /**
     * 人脸相似度（匹配得分）
     */
    private Double similarity;
    
    /**
     * 人脸相似度阈值
     */
    private Double similarityThreshold;
} 