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
    @Schema(description = "异常考勤记录ID（必填，必须是系统中已存在且状态为待处理的异常考勤记录）", 
            required = true, 
            example = "12345")
    private Long recordId;
    
    /**
     * 异常类型编号：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    @Schema(description = "考勤状态（必填）：1=正常（确认为正常考勤，取消异常标记），2=迟到（确认为迟到，影响绩效），3=早退（确认为早退，影响绩效），4=旷工（确认为旷工，严重违规），5=加班（确认为加班，可能产生加班费）", 
            required = true, 
            example = "1", 
            allowableValues = {"1", "2", "3", "4", "5"}, 
            minimum = "1", 
            maximum = "5")
    private Integer status;
    
    /**
     * 备注/描述
     */
    @Schema(description = "管理员处理备注（选填，但建议填写处理原因，尤其是当修改了考勤状态时，将显示在员工的考勤记录中，不超过200字符）", 
            example = "经核实情况属实，公司班车确实当日延误，更正为正常考勤", 
            maxLength = 200)
    private String remark;
} 