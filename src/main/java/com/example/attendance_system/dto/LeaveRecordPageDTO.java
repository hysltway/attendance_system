package com.example.attendance_system.dto;

import lombok.Data;

import java.util.List;

/**
 * 请假记录分页数据传输对象
 * 用于分页展示请假记录
 */
@Data
public class LeaveRecordPageDTO {
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页记录数
     */
    private Integer size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 请假记录列表
     */
    private List<LeaveRecordDTO> records;
} 