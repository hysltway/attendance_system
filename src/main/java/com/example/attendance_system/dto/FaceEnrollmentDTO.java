package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 人脸录入数据传输对象
 */
@Data
@Schema(description = "人脸特征录入参数")
public class FaceEnrollmentDTO {
    /**
     * 员工编号
     */
    @Schema(description = "员工编号（必填，必须是系统中已存在的有效员工，且该员工尚未录入人脸信息）", required = true, example = "EMP2025001", pattern = "EMP\\d{7}")
    private String employeeNo;

    /**
     * Base64编码的人脸图像
     * 前端可能会传输带前缀的Base64，例如："data:image/jpeg;base64,/9j/4AAQSkZJRgABA..."
     */
    @Schema(description = "Base64编码的人脸图像（必填，支持JPG/JPEG/PNG格式，分辨率建议不低于640x480，文件大小不超过2MB，可包含'data:image/jpeg;base64,'等前缀）",
            required = true,
            example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...")
    private String faceImageBase64;
} 