package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告DTO，用于返回公告信息
 */
@Data
@Schema(description = "公告详细信息")
public class AnnouncementDTO {
    /**
     * 公告ID
     */
    @Schema(description = "公告ID", example = "1001")
    private Long id;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题", example = "关于五一假期的通知")
    private String title;

    /**
     * 公告正文
     */
    @Schema(description = "公告正文（支持富文本）", example = "<p>根据国家法定节假日安排，现将公司五一假期安排通知如下...</p>")
    private String content;

    /**
     * 公告摘要（前50字或截断内容）
     */
    @Schema(description = "公告摘要（前50字或截断内容）", example = "根据国家法定节假日安排，现将公司五一假期安排通知如下...")
    private String summary;

    /**
     * 目标范围：all（全体员工）或 partial（指定部门）
     */
    @Schema(description = "目标范围：all=全体员工，partial=指定部门", example = "all")
    private String targetScope;

    /**
     * 指定部门ID列表
     */
    @Schema(description = "指定部门ID列表（当targetScope为partial时有值）", example = "[\"dept001\", \"dept002\"]")
    private List<String> departments;

    /**
     * 指定部门名称列表（便于前端展示）
     */
    @Schema(description = "指定部门名称列表（便于前端展示）", example = "[\"研发部\", \"市场部\"]")
    private List<String> departmentNames;

    /**
     * 发布类型：immediate（立即）或 scheduled（定时）
     */
    @Schema(description = "发布类型：immediate=立即发布，scheduled=定时发布", example = "immediate")
    private String publishType;

    /**
     * 预定发布时间
     */
    @Schema(description = "预定发布时间（当publishType为scheduled时有值）", example = "2025-04-22 09:00:00")
    private LocalDateTime publishTime;

    /**
     * 公告生效开始时间
     */
    @Schema(description = "公告生效开始时间", example = "2025-04-21 08:00:00")
    private LocalDateTime validFrom;

    /**
     * 公告失效时间
     */
    @Schema(description = "公告失效时间，过期后员工端不再展示", example = "2025-04-28 18:00:00")
    private LocalDateTime validTo;

    /**
     * 发布人
     */
    @Schema(description = "发布人姓名或ID", example = "admin01")
    private String publisher;

    /**
     * 公告状态：draft-草稿，published-已发布，expired-已过期
     */
    @Schema(description = "公告状态：draft=草稿，published=已发布，expired=已过期", example = "published")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-04-20 15:30:00")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "最后更新时间", example = "2025-04-20 16:45:00")
    private LocalDateTime updatedTime;

    /**
     * 是否已读（用于员工端）
     */
    @Schema(description = "当前员工是否已读（仅员工端接口返回）", example = "true")
    private Boolean isRead;
} 