package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考勤记录分页数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤记录分页结果")
public class AttendanceRecordPageDTO {
    /**
     * 当前页码
     */
    @Schema(description = "当前页码（从1开始）", example = "1")
    private Integer current;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", example = "10")
    private Integer size;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "125")
    private Long total;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "13")
    private Integer pages;

    /**
     * 考勤统计 - 出勤天数
     */
    @Schema(description = "考勤统计 - 出勤天数", example = "21")
    private Integer attendanceDays;

    /**
     * 考勤统计 - 迟到次数
     */
    @Schema(description = "考勤统计 - 迟到次数", example = "2")
    private Integer lateTimes;

    /**
     * 考勤统计 - 早退次数
     */
    @Schema(description = "考勤统计 - 早退次数", example = "1")
    private Integer earlyLeaveTimes;

    /**
     * 考勤统计 - 缺勤天数
     */
    @Schema(description = "考勤统计 - 缺勤天数", example = "0")
    private Integer absentDays;

    /**
     * 考勤统计 - 加班次数
     */
    @Schema(description = "考勤统计 - 加班次数", example = "3")
    private Integer overtimeTimes;

    /**
     * 考勤记录列表
     */
    @Schema(description = "当前页的考勤记录列表")
    private List<AttendanceRecordDTO> records;
} 