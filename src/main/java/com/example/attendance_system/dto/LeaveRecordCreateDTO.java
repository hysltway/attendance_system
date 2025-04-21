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
    @Schema(description = "员工编号（必填）", required = true, example = "EMP2025001")
    private String employeeNo;
    
    /**
     * 请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假，7-其他
     */
    @Schema(description = "请假类型（必填）：1=事假，2=病假，3=年假，4=婚假，5=产假，6=丧假，7=其他", required = true, example = "2")
    private Integer leaveType;
    
    /**
     * 请假开始日期
     */
    @Schema(description = "请假开始日期（必填，格式：yyyy-MM-dd）", required = true, example = "2025-05-01")
    private LocalDate startDate;
    
    /**
     * 请假结束日期
     */
    @Schema(description = "请假结束日期（必填，格式：yyyy-MM-dd）", required = true, example = "2025-05-03")
    private LocalDate endDate;
    
    /**
     * 请假原因
     */
    @Schema(description = "请假原因（必填）", required = true, example = "因身体不适需要休息")
    private String reason;
} 