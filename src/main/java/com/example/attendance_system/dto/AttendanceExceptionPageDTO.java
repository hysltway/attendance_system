package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 异常考勤信息分页查询结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceExceptionPageDTO {
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
     * 异常考勤记录列表
     */
    private List<AttendanceExceptionDTO> records;
} 