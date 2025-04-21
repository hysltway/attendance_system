package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 请假审批数据传输对象
 * 用于管理员审批请假申请
 */
@Data
@Schema(description = "请假审批参数")
public class LeaveRecordApprovalDTO {
    /**
     * 请假记录ID
     */
    @Schema(description = "请假记录ID（必填，必须是系统中状态为'待审批'的有效请假申请ID）",
            required = true,
            example = "10001")
    private Long id;

    /**
     * 审批人员工编号
     */
    @Schema(description = "审批人员工编号（必填，必须是有审批权限的管理员编号，系统会自动校验当前登录用户权限）",
            required = true,
            example = "ADMIN001",
            pattern = "(ADMIN|EMP)\\d+")
    private String approverNo;

    /**
     * 审批状态：1-已批准，2-已拒绝
     */
    @Schema(description = "审批状态（必填）：1=批准（同意员工请假申请），2=拒绝（不同意请假申请），不支持其他值",
            required = true,
            example = "1",
            allowableValues = {"1", "2"},
            minimum = "1",
            maximum = "2")
    private Integer status;

    /**
     * 审批备注
     */
    @Schema(description = "审批备注或意见（选填，当拒绝申请时建议填写拒绝理由，不超过200字符）",
            example = "批准，请休息好再回来工作",
            maxLength = 200)
    private String approvalRemark;
} 