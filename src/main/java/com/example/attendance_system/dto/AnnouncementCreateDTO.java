package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建公告请求DTO
 */
@Data
@Schema(description = "公告创建请求体")
public class AnnouncementCreateDTO {
    /**
     * 公告标题（必填）
     */
    @Schema(description = "公告标题（必填，2-50个字符）", required = true, example = "关于五一假期的通知", minLength = 2, maxLength = 50)
    private String title;

    /**
     * 公告正文，支持富文本（必填）
     */
    @Schema(description = "公告正文内容（必填，支持HTML富文本格式，最大50000字符）", required = true, example = "<p>根据国家法定节假日安排，现将公司五一假期安排通知如下...</p>", maxLength = 50000)
    private String content;

    /**
     * 目标范围（必填）：all=全体员工，partial=指定部门
     */
    @Schema(description = "目标范围（必填）：all=全体员工（所有部门可见），partial=指定部门（仅选定部门可见）", required = true, example = "all", allowableValues = {"all", "partial"})
    private String targetScope;

    /**
     * 指定部门ID列表（仅当targetScope为partial时必填）
     */
    @Schema(description = "指定部门ID列表（当targetScope为partial时必填，否则可为空；部门ID必须是已存在的有效部门）", example = "[\"dept001\", \"dept002\"]")
    private List<String> departments;

    /**
     * 发布类型（必填）：immediate=立即发布，scheduled=定时发布
     */
    @Schema(description = "发布类型（必填）：immediate=立即发布（创建后即发布），scheduled=定时发布（到指定时间自动发布）", required = true, example = "immediate", allowableValues = {"immediate", "scheduled"})
    private String publishType;

    /**
     * 预定发布时间（仅当publishType为scheduled时必填）
     */
    @Schema(description = "预定发布时间（当publishType为scheduled时必填，必须是未来时间，格式：yyyy-MM-dd HH:mm:ss）", example = "2025-04-22 09:00:00")
    private LocalDateTime publishTime;

    /**
     * 公告开始生效时间（必填）
     */
    @Schema(description = "公告开始生效时间（必填，可以是当前或未来时间，格式：yyyy-MM-dd HH:mm:ss）", required = true, example = "2025-04-21 08:00:00")
    private LocalDateTime validFrom;

    /**
     * 公告失效时间（必填），过期后员工端不再展示
     */
    @Schema(description = "公告失效时间（必填，必须晚于生效开始时间，格式：yyyy-MM-dd HH:mm:ss，过期后员工端不再展示此公告）", required = true, example = "2025-04-28 18:00:00")
    private LocalDateTime validTo;

    /**
     * 发布人（系统自动带入）
     */
    @Schema(description = "发布人ID或姓名（系统会自动从当前登录用户获取，可不填）", example = "admin01")
    private String publisher;
} 