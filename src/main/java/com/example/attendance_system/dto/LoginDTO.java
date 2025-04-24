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
     * 账号 - 可以是员工编号、手机号或邮箱
     */
    @Schema(description = "账号（必填，可以是员工编号、手机号或邮箱）",
            required = true,
            example = "10250001 或 13812345678 或 example@company.com")
    private String account;

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