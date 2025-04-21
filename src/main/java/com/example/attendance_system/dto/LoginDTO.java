package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录数据传输对象
 */
@Data
@Schema(description = "登录请求参数")
public class LoginDTO {
    /**
     * 员工编号
     */
    @Schema(description = "员工编号（必填）", required = true, example = "EMP2025001")
    private String employeeNo;
    
    /**
     * 密码
     */
    @Schema(description = "密码（必填）", required = true, example = "password123")
    private String password;
} 