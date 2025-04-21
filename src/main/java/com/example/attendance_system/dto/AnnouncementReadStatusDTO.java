package com.example.attendance_system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告阅读状态DTO
 */
@Data
public class AnnouncementReadStatusDTO {
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 员工姓名
     */
    private String employeeName;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 阅读状态：read-已读，unread-未读
     */
    private String status;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
} 