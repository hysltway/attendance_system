package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤记录数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDTO {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 打卡时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 打卡类型：1-上班打卡，2-下班打卡，3-外出打卡，4-返回打卡
     */
    private Integer checkType;
    
    /**
     * 打卡类型描述
     */
    private String checkTypeDesc;
    
    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    private Integer checkMethod;
    
    /**
     * 打卡方式描述
     */
    private String checkMethodDesc;
    
    /**
     * 打卡状态：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    private Integer status;
    
    /**
     * 打卡状态描述
     */
    private String statusDesc;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 异常描述（自动生成）
     */
    private String reason;
    
    /**
     * 员工申诉说明
     */
    private String explanation;
    
    /**
     * 是否提交管理员处理：true-是，false-否
     */
    private Boolean submittedToAdmin;
    
    /**
     * 是否已被管理员处理过：true-是，false-否
     */
    private Boolean processedByAdmin;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 