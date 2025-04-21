package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "管理员处理考勤异常参数")
public class AdminAttendanceExceptionUpdateDTO {
    /**
     * 异常考勤记录ID
     */
    @Schema(description = "异常考勤记录ID（必填）", required = true, example = "12345")
    private Long recordId;
    
    /**
     * 异常类型编号：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    @Schema(description = "考勤状态（必填）：1=正常，2=迟到，3=早退，4=旷工，5=加班", required = true, example = "1")
    private Integer status;
    
    /**
     * 备注/描述
     */
    @Schema(description = "管理员处理备注", example = "经核实情况属实，更正为正常考勤")
    private String remark;
} 