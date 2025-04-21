package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤记录数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤记录详细信息")
public class AttendanceRecordDTO {
    /**
     * 记录ID
     */
    @Schema(description = "考勤记录ID", example = "1001")
    private Long id;

    /**
     * 员工编号
     */
    @Schema(description = "员工编号", example = "EMP2025001")
    private String employeeNo;

    /**
     * 打卡时间
     */
    @Schema(description = "打卡时间（原始时间戳）", example = "2025-05-01 08:55:23")
    private LocalDateTime checkTime;

    /**
     * 打卡时间（格式化后的字符串，格式：yyyy-MM-dd HH:mm）
     */
    @Schema(description = "打卡时间（格式化后的字符串，格式：yyyy-MM-dd HH:mm）", example = "2025-05-01 08:55")
    private String checkTimeStr;

    /**
     * 打卡类型：1-上班打卡，2-下班打卡，3-外出打卡，4-返回打卡
     */
    @Schema(description = "打卡类型：1=上班打卡，2=下班打卡，3=外出打卡，4=返回打卡", example = "1")
    private Integer checkType;

    /**
     * 打卡类型描述
     */
    @Schema(description = "打卡类型中文描述", example = "上班打卡")
    private String checkTypeDesc;

    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    @Schema(description = "打卡方式：1=人脸识别，2=管理员录入，3=系统自动生成", example = "1")
    private Integer checkMethod;

    /**
     * 打卡方式描述
     */
    @Schema(description = "打卡方式中文描述", example = "人脸识别")
    private String checkMethodDesc;

    /**
     * 打卡状态：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    @Schema(description = "打卡状态：1=正常，2=迟到，3=早退，4=旷工，5=加班", example = "1")
    private Integer status;

    /**
     * 打卡状态描述
     */
    @Schema(description = "打卡状态中文描述", example = "正常")
    private String statusDesc;

    /**
     * 备注
     */
    @Schema(description = "备注信息，一般为管理员填写", example = "因会议原因延迟打卡")
    private String remark;

    /**
     * 异常描述（自动生成）
     */
    @Schema(description = "异常描述，系统根据考勤规则自动生成", example = "迟到15分钟")
    private String reason;

    /**
     * 员工申诉说明
     */
    @Schema(description = "员工申诉说明，员工对异常考勤的解释", example = "因公交车延误导致迟到")
    private String explanation;

    /**
     * 是否提交管理员处理：true-是，false-否
     */
    @Schema(description = "是否已提交管理员处理：true=是，false=否", example = "true")
    private Boolean submittedToAdmin;

    /**
     * 是否已被管理员处理过：true-是，false-否
     */
    @Schema(description = "是否已被管理员处理：true=是，false=否", example = "false")
    private Boolean processedByAdmin;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-05-01 08:55:23")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "最后更新时间", example = "2025-05-01 10:30:15")
    private LocalDateTime updatedTime;
} 