package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 请假申请创建数据传输对象
 * 用于员工提交请假申请
 */
@Data
@Schema(description = "请假申请创建请求体")
public class LeaveRecordCreateDTO {
    /**
     * 员工编号
     */
    @Schema(description = "员工编号（必填，必须是系统中已存在的有效员工）", required = true, example = "EMP2025001")
    private String employeeNo;
    
    /**
     * 请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假，7-其他
     */
    @Schema(description = "请假类型（必填）：1=事假（普通请假），2=病假（需提供证明），3=年假（每年固定额度），4=婚假（需提供证明），5=产假（需提供证明），6=丧假（直系亲属），7=其他（特殊情况）", required = true, example = "2", minimum = "1", maximum = "7")
    private Integer leaveType;
    
    /**
     * 请假开始日期
     */
    @Schema(description = "请假开始日期（必填，格式：yyyy-MM-dd，不能是过去的日期，最早可从当天开始请假）", required = true, example = "2025-05-01")
    private LocalDate startDate;
    
    /**
     * 请假结束日期
     */
    @Schema(description = "请假结束日期（必填，格式：yyyy-MM-dd，必须大于等于开始日期，单次请假最长不超过90天）", required = true, example = "2025-05-03")
    private LocalDate endDate;
    
    /**
     * 请假原因
     */
    @Schema(description = "请假原因（必填，5-200个字符，详细说明请假事由，有助于审批通过）", required = true, example = "因感冒发烧，需要居家休息治疗，预计三天后康复", minLength = 5, maxLength = 200)
    private String reason;
} 