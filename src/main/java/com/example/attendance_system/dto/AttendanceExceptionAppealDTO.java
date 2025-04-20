package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤异常申诉DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceExceptionAppealDTO {
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 异常考勤记录ID
     */
    private Long recordId;
    
    /**
     * 异常说明 / 申诉理由
     */
    private String explanation;
} 