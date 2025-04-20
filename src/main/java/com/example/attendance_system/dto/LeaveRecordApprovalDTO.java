package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 请假审批数据传输对象
 * 用于管理员审批请假申请
 */
@Data
public class LeaveRecordApprovalDTO {
    /**
     * 请假记录ID
     */
    private Long id;
    
    /**
     * 审批人员工编号
     */
    private String approverNo;
    
    /**
     * 审批状态：1-已批准，2-已拒绝
     */
    private Integer status;
    
    /**
     * 审批备注
     */
    private String approvalRemark;
} 