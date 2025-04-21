package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异常考勤信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤异常信息")
public class AttendanceExceptionDTO {
    /**
     * 记录ID
     */
    @Schema(description = "记录ID", example = "12345")
    private Long id;

    /**
     * 打卡日期，格式为 yyyy-MM-dd
     */
    @Schema(description = "打卡日期（格式：yyyy-MM-dd）", example = "2025-05-01")
    private String date;

    /**
     * 打卡时间，完整时间
     */
    @Schema(description = "打卡时间（完整时间，包含时分秒）", example = "2025-05-01 09:15:30")
    private LocalDateTime checkTime;

    /**
     * 打卡类型：1-上班打卡，2-下班打卡，3-外出打卡，4-返回打卡
     */
    @Schema(description = "打卡类型：1=上班打卡，2=下班打卡，3=外出打卡，4=返回打卡", example = "1")
    private Integer checkType;

    /**
     * 打卡类型描述
     */
    @Schema(description = "打卡类型描述", example = "上班打卡")
    private String checkTypeText;

    /**
     * 异常类型编号：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    @Schema(description = "考勤状态：1=正常，2=迟到，3=早退，4=旷工，5=加班", example = "2")
    private Integer checkStatus;

    /**
     * 异常类型中文描述（如"迟到"、"早退"）
     */
    @Schema(description = "考勤状态描述", example = "迟到")
    private String checkTypeDesc;

    /**
     * 异常原因
     */
    @Schema(description = "异常原因", example = "交通拥堵")
    private String reason;

    /**
     * 处理状态编号：0-未处理，1-已处理
     */
    @Schema(description = "处理状态：0=未处理，1=已处理", example = "0")
    private Integer status;

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
     * 申诉说明
     */
    @Schema(description = "申诉说明", example = "因为公交车故障导致延误")
    private String explanation;

    /**
     * 管理员备注
     */
    @Schema(description = "管理员备注", example = "已核实情况属实")
    private String remark;

    /**
     * 是否已提交申诉
     */
    @Schema(description = "是否已提交申诉", example = "true")
    private Boolean submittedToAdmin;

    /**
     * 是否已被处理
     */
    @Schema(description = "是否已被处理", example = "false")
    private Boolean processedByAdmin;

    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    @Schema(description = "打卡方式：1=人脸识别，2=管理员录入，3=系统自动生成", example = "1")
    private Integer checkMethod;

    /**
     * 打卡方式描述
     */
    @Schema(description = "打卡方式描述", example = "人脸识别")
    private String checkMethodText;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-05-01 09:30:00")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-05-01 14:20:00")
    private LocalDateTime updatedTime;
} 