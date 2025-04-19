package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异常考勤信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceExceptionDTO {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 打卡日期，格式为 yyyy-MM-dd
     */
    private String date;
    
    /**
     * 打卡时间，完整时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 打卡类型：1-上班打卡，2-下班打卡，3-外出打卡，4-返回打卡
     */
    private Integer checkType;
    
    /**
     * 打卡类型描述
     */
    private String checkTypeText;
    
    /**
     * 异常类型编号：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    private Integer checkStatus;
    
    /**
     * 异常类型中文描述（如"迟到"、"早退"）
     */
    private String checkTypeDesc;
    
    /**
     * 异常原因
     */
    private String reason;
    
    /**
     * 处理状态编号：0-未处理，1-已处理
     */
    private Integer status;
    
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 申诉说明
     */
    private String explanation;
    
    /**
     * 管理员备注
     */
    private String remark;
    
    /**
     * 是否已提交申诉
     */
    private Boolean submittedToAdmin;
    
    /**
     * 是否已被处理
     */
    private Boolean processedByAdmin;
    
    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    private Integer checkMethod;
    
    /**
     * 打卡方式描述
     */
    private String checkMethodText;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 