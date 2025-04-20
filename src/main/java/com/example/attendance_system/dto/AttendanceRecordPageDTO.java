package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考勤记录分页数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordPageDTO {
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
     * 考勤记录列表
     */
    private List<AttendanceRecordDTO> records;
} 