package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告阅读状态DTO
 */
@Data
@Schema(description = "公告阅读状态信息")
public class AnnouncementReadStatusDTO {
    /**
     * 员工编号
     */
    @Schema(description = "员工编号", example = "EMP2025001")
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "研发部")
    private String departmentName;

    /**
     * 阅读状态：read-已读，unread-未读
     */
    @Schema(description = "阅读状态：read=已读，unread=未读", example = "read")
    private String status;

    /**
     * 阅读时间
     */
    @Schema(description = "阅读时间（已读时才有值）", example = "2025-04-22 10:15:20")
    private LocalDateTime readTime;
} 