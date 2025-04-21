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
    @Schema(description = "员工编号（必填，系统唯一标识，格式为'EMP'开头加年份和顺序号，如EMP2025001）", 
           required = true, 
           example = "EMP2025001", 
           pattern = "EMP\\d{7}")
    private String employeeNo;
    
    /**
     * 密码
     */
    @Schema(description = "密码（必填，8-20位字符，区分大小写，连续5次错误将锁定账号30分钟）", 
           required = true, 
           example = "Password123", 
           minLength = 8, 
           maxLength = 20)
    private String password;
} 