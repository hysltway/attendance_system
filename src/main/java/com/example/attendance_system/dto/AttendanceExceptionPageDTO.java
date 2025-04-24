package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 异常考勤信息分页查询结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤异常分页结果")
public class AttendanceExceptionPageDTO {
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
    @Schema(description = "总记录数", example = "45")
    private Long total;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "5")
    private Integer pages;

    /**
     * 异常考勤记录列表
     */
    @Schema(description = "当前页的考勤异常记录列表")
    private List<AttendanceExceptionDTO> records;
} 