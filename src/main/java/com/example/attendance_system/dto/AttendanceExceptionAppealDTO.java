package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "考勤异常申诉参数")
public class AttendanceExceptionAppealDTO {
    /**
     * 员工编号
     */
    @Schema(description = "员工编号（必填）", required = true, example = "EMP2025001")
    private String employeeNo;
    
    /**
     * 异常考勤记录ID
     */
    @Schema(description = "异常考勤记录ID（必填）", required = true, example = "12345")
    private Long recordId;
    
    /**
     * 异常说明 / 申诉理由
     */
    @Schema(description = "异常说明/申诉理由（必填）", required = true, example = "因为公交车故障导致延误，已向部门主管口头报备")
    private String explanation;
} 