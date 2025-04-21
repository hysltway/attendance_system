package com.example.attendance_system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告DTO，用于返回公告信息
 */
@Data
public class AnnouncementDTO {
    /**
     * 公告ID
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告正文
     */
    private String content;
    
    /**
     * 公告摘要（前50字或截断内容）
     */
    private String summary;
    
    /**
     * 目标范围：all（全体员工）或 partial（指定部门）
     */
    private String targetScope;
    
    /**
     * 指定部门ID列表
     */
    private List<String> departments;
    
    /**
     * 指定部门名称列表（便于前端展示）
     */
    private List<String> departmentNames;
    
    /**
     * 发布类型：immediate（立即）或 scheduled（定时）
     */
    private String publishType;
    
    /**
     * 预定发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 公告生效开始时间
     */
    private LocalDateTime validFrom;
    
    /**
     * 公告失效时间
     */
    private LocalDateTime validTo;
    
    /**
     * 发布人
     */
    private String publisher;
    
    /**
     * 公告状态：draft-草稿，published-已发布，expired-已过期
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 是否已读（用于员工端）
     */
    private Boolean isRead;
} 