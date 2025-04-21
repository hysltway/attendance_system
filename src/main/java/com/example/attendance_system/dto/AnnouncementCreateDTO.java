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
    @Schema(description = "公告标题（必填）", required = true, example = "关于五一假期的通知")
    private String title;
    
    /**
     * 公告正文，支持富文本（必填）
     */
    @Schema(description = "公告正文，支持富文本（必填）", required = true, example = "请大家注意假期安排与值班人员...")
    private String content;
    
    /**
     * 目标范围（必填）：all=全体员工，partial=指定部门
     */
    @Schema(description = "目标范围（必填）：all=全体员工，partial=指定部门", required = true, example = "all")
    private String targetScope;
    
    /**
     * 指定部门ID列表（仅当targetScope为partial时必填）
     */
    @Schema(description = "指定部门ID列表（仅当targetScope为partial时必填）", example = "[\"dept001\", \"dept002\"]")
    private List<String> departments;
    
    /**
     * 发布类型（必填）：immediate=立即发布，scheduled=定时发布
     */
    @Schema(description = "发布类型（必填）：immediate=立即发布，scheduled=定时发布", required = true, example = "immediate")
    private String publishType;
    
    /**
     * 预定发布时间（仅当publishType为scheduled时必填）
     */
    @Schema(description = "预定发布时间（仅当publishType为scheduled时必填）", example = "2025-04-22 09:00:00")
    private LocalDateTime publishTime;
    
    /**
     * 公告开始生效时间（必填）
     */
    @Schema(description = "公告开始生效时间（必填）", required = true, example = "2025-04-21 08:00:00")
    private LocalDateTime validFrom;
    
    /**
     * 公告失效时间（必填），过期后员工端不再展示
     */
    @Schema(description = "公告失效时间（必填），过期后员工端不再展示", required = true, example = "2025-04-28 18:00:00")
    private LocalDateTime validTo;
    
    /**
     * 发布人（系统自动带入）
     */
    @Schema(description = "发布人（系统自动带入）", example = "admin01")
    private String publisher;
} 