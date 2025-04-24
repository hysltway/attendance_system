package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 请假记录分页数据传输对象
 * 用于分页展示请假记录
 */
@Data
@Schema(description = "请假记录分页结果")
public class LeaveRecordPageDTO {
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
    @Schema(description = "总记录数", example = "85")
    private Long total;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "9")
    private Integer pages;

    /**
     * 请假记录列表
     */
    @Schema(description = "当前页的请假记录列表")
    private List<LeaveRecordDTO> records;
} 