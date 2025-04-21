package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 员工信息更新审核DTO
 */
@Data
public class EmployeeInfoUpdateAuditDTO {
    /**
     * 请求ID
     */
    private Long requestId;

    /**
     * 审核状态：1-通过，2-拒绝
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String adminComment;

    /**
     * 管理员编号（审核人）
     */
    private String adminNo;
} 