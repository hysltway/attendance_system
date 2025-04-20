package com.example.attendance_system.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 请假申请创建数据传输对象
 * 用于员工提交请假申请
 */
@Data
public class LeaveRecordCreateDTO {
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假，7-其他
     */
    private Integer leaveType;
    
    /**
     * 请假开始日期
     */
    private LocalDate startDate;
    
    /**
     * 请假结束日期
     */
    private LocalDate endDate;
    
    /**
     * 请假原因
     */
    private String reason;
} 