package com.example.attendance_system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建公告请求DTO
 */
@Data
public class AnnouncementCreateDTO {
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告正文（富文本或纯文本）
     */
    private String content;
    
    /**
     * 目标范围：all（全体员工）或 partial（指定部门）
     */
    private String targetScope;
    
    /**
     * 指定部门ID列表（仅当targetScope为partial时需要）
     */
    private List<String> departments;
    
    /**
     * 发布类型：immediate（立即）或 scheduled（定时）
     */
    private String publishType;
    
    /**
     * 预定发布时间（仅定时发布时填写）
     */
    private LocalDateTime publishTime;
    
    /**
     * 公告开始生效时间
     */
    private LocalDateTime validFrom;
    
    /**
     * 公告失效时间，过期后员工端不再展示
     */
    private LocalDateTime validTo;
    
    /**
     * 发布人姓名或ID
     */
    private String publisher;
} 