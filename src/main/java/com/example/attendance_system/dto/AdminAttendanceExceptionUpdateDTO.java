package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员更新异常考勤记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAttendanceExceptionUpdateDTO {
    /**
     * 异常考勤记录ID
     */
    private Long recordId;
    
    /**
     * 异常类型编号：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    private Integer status;
    
    /**
     * 备注/描述
     */
    private String remark;
} 