package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 异常类型编号：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    private Integer checkType;
    
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
} 