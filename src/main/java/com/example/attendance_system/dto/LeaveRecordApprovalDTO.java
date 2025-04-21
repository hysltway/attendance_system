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
    @Schema(description = "请假记录ID（必填）", required = true, example = "10001")
    private Long id;
    
    /**
     * 审批人员工编号
     */
    @Schema(description = "审批人员工编号（必填）", required = true, example = "ADMIN001")
    private String approverNo;
    
    /**
     * 审批状态：1-已批准，2-已拒绝
     */
    @Schema(description = "审批状态（必填）：1=批准，2=拒绝", required = true, example = "1")
    private Integer status;
    
    /**
     * 审批备注
     */
    @Schema(description = "审批备注或意见", example = "批准，请休息好再回来工作")
    private String approvalRemark;
} 