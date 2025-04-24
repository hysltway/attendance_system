package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假记录数据传输对象
 * 用于前后端交互
 */
@Data
@Schema(description = "请假记录详细信息")
public class LeaveRecordDTO {
    /**
     * 主键ID
     */
    @Schema(description = "请假记录ID", example = "10001")
    private Long id;

    /**
     * 员工编号
     */
    @Schema(description = "员工编号", example = "EMP2025001")
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    /**
     * 请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假，7-其他
     */
    @Schema(description = "请假类型：1=事假，2=病假，3=年假，4=婚假，5=产假，6=丧假，7=其他", example = "2")
    private Integer leaveType;

    /**
     * 请假类型描述
     */
    @Schema(description = "请假类型中文描述", example = "病假")
    private String leaveTypeDesc;

    /**
     * 请假开始日期
     */
    @Schema(description = "请假开始日期", example = "2025-05-01")
    private LocalDate startDate;

    /**
     * 请假结束日期
     */
    @Schema(description = "请假结束日期", example = "2025-05-03")
    private LocalDate endDate;

    /**
     * 请假原因
     */
    @Schema(description = "请假原因", example = "因身体不适需要休息")
    private String reason;

    /**
     * 审批状态：0-待审批，1-已批准，2-已拒绝
     */
    @Schema(description = "审批状态：0=待审批，1=已批准，2=已拒绝", example = "1")
    private Integer status;

    /**
     * 审批状态描述
     */
    @Schema(description = "审批状态中文描述", example = "已批准")
    private String statusDesc;

    /**
     * 审批人员工编号
     */
    @Schema(description = "审批人员工编号", example = "ADMIN001")
    private String approverNo;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间", example = "2025-04-25 14:30:00")
    private LocalDateTime approvalTime;

    /**
     * 审批备注
     */
    @Schema(description = "审批备注或意见", example = "批准，请休息好再回来工作")
    private String approvalRemark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间（申请提交时间）", example = "2025-04-24 10:15:00")
    private LocalDateTime createdTime;
} 