package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 公告分页查询DTO
 */
@Data
public class AnnouncementPageQueryDTO {
    /**
     * 公告标题关键词
     */
    private String title;
    
    /**
     * 公告状态：published-已发布，draft-待发布
     */
    private String status;
    
    /**
     * 发布时间开始（yyyy-MM-dd）
     */
    private String startDate;
    
    /**
     * 发布时间结束（yyyy-MM-dd）
     */
    private String endDate;
    
    /**
     * 当前页码
     */
    private Integer current = 1;
    
    /**
     * 每页条数
     */
    private Integer size = 10;
} 