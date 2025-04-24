package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 人脸识别考勤打卡数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "人脸识别打卡响应结果")
public class FaceRecognitionDTO {
    /**
     * 打卡状态：success-成功，error-失败
     */
    @Schema(description = "打卡状态：success=成功，error=失败", example = "success")
    private String status;

    /**
     * 员工编号
     */
    @Schema(description = "员工编号", example = "EMP2025001")
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String name;

    /**
     * 打卡时间戳
     */
    @Schema(description = "打卡时间戳（精确到秒）", example = "2025-05-01 09:02:35")
    private LocalDateTime timestamp;

    /**
     * 返回消息
     */
    @Schema(description = "返回消息，成功时为打卡成功提示，失败时为错误原因", example = "打卡成功，上班打卡已记录")
    private String message;

    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    @Schema(description = "打卡方式：1=人脸识别，2=管理员录入，3=系统自动生成", example = "1")
    private Integer checkMethod;

    /**
     * 人脸相似度（匹配得分）
     */
    @Schema(description = "人脸相似度（匹配得分），范围0.0~1.0，越高越相似", example = "0.92")
    private Double similarity;

    /**
     * 人脸相似度阈值
     */
    @Schema(description = "人脸相似度阈值，低于此值视为识别失败", example = "0.8")
    private Double similarityThreshold;
} 