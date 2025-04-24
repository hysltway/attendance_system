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
    @Schema(description = "员工编号（必填，必须是系统中已存在的有效员工，且必须是当前登录用户或其下属）",
            required = true,
            example = "EMP2025001",
            pattern = "EMP\\d{7}")
    private String employeeNo;

    /**
     * 异常考勤记录ID
     */
    @Schema(description = "异常考勤记录ID（必填，必须是与员工关联的且状态为未处理的异常考勤记录，申诉只能在异常发生后7天内提交）",
            required = true,
            example = "12345")
    private Long recordId;

    /**
     * 异常说明 / 申诉理由
     */
    @Schema(description = "异常说明/申诉理由（必填，10-500个字符，需详细说明异常原因，若有证据可在备注中说明，有助于快速审批通过）",
            required = true,
            example = "因为公交车故障导致延误，已向部门主管口头报备。此情况已与张经理确认，可联系确认情况。",
            minLength = 10,
            maxLength = 500)
    private String explanation;
} 