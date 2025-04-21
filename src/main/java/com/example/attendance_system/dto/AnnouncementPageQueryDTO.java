package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公告分页查询DTO
 */
@Data
@Schema(description = "公告分页查询参数")
public class AnnouncementPageQueryDTO {
    /**
     * 公告标题关键词（模糊匹配）
     */
    @Schema(description = "公告标题关键词（模糊匹配）", example = "通知")
    private String title;
    
    /**
     * 公告状态：published=已发布，draft=待发布，expired=已过期
     */
    @Schema(description = "公告状态：published=已发布，draft=待发布，expired=已过期", example = "published")
    private String status;
    
    /**
     * 发布时间开始（yyyy-MM-dd）
     */
    @Schema(description = "发布时间开始（yyyy-MM-dd）", example = "2025-04-01")
    private String startDate;
    
    /**
     * 发布时间结束（yyyy-MM-dd）
     */
    @Schema(description = "发布时间结束（yyyy-MM-dd）", example = "2025-04-30")
    private String endDate;
    
    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "当前页码（从1开始）", example = "1")
    private Integer current = 1;
    
    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
} 