package com.example.attendance_system.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假记录数据传输对象
 * 用于前后端交互
 */
@Data
public class LeaveRecordDTO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 员工姓名
     */
    private String employeeName;
    
    /**
     * 请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假，7-其他
     */
    private Integer leaveType;
    
    /**
     * 请假类型描述
     */
    private String leaveTypeDesc;
    
    /**
     * 请假开始日期
     */
    private LocalDate startDate;
    
    /**
     * 请假结束日期
     */
    private LocalDate endDate;
    
    /**
     * 请假原因
     */
    private String reason;
    
    /**
     * 审批状态：0-待审批，1-已批准，2-已拒绝
     */
    private Integer status;
    
    /**
     * 审批状态描述
     */
    private String statusDesc;
    
    /**
     * 审批人员工编号
     */
    private String approverNo;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;
    
    /**
     * 审批备注
     */
    private String approvalRemark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
} 