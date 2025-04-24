package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 员工阅读公告请求DTO
 */
@Data
public class AnnouncementEmployeeReadDTO {
    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 公告ID
     */
    private Long announcementId;
} 