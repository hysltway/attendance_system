package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工时统计数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursDTO {
    
    /**
     * 平均上班时长（小时）
     */
    private Double averageWorkingHours;
    
    /**
     * 平均加班时长（小时）
     */
    private Double averageOvertimeHours;
    
    /**
     * 平均请假时长（小时）
     */
    private Double averageLeaveHours;
    
    /**
     * 最长工作时长（小时）
     */
    private Double maxWorkingHours;
    
    /**
     * 最短工作时长（小时）
     */
    private Double minWorkingHours;
    
    /**
     * 当日出勤率
     */
    private Double attendanceRate;
    
    /**
     * 超时工作员工比例
     */
    private Double overtimeEmployeeRate;
    
    /**
     * 有效工作时长（扣除休息时间）
     */
    private Double effectiveWorkingHours;
    
    /**
     * 统计周期（日/周/月）
     */
    private String period;
} 