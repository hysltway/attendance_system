package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 人脸录入数据传输对象
 */
@Data
public class FaceEnrollmentDTO {
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * Base64编码的人脸图像
     * 前端可能会传输带前缀的Base64，例如："data:image/jpeg;base64,/9j/4AAQSkZJRgABA..."
     */
    private String faceImageBase64;
} 