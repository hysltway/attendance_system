package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工信息更新DTO
 */
@Data
@Schema(description = "员工信息更新参数")
public class EmployeeInfoUpdateDTO {
    /**
     * 员工编号
     */
    @Schema(description = "员工编号（必填，用于标识要更新的员工，必须是系统中已存在的有效员工，且当前用户有权限修改）",
            required = true,
            example = "EMP2025001",
            pattern = "EMP\\d{7}")
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名（选填，若提供则更新，2-20个字符，不能包含特殊字符；若为空则保持原值不变）",
            example = "张三",
            minLength = 2,
            maxLength = 20)
    private String name;

    /**
     * 手机号
     */
    @Schema(description = "手机号码（选填，若提供则更新，必须是11位中国大陆手机号；若为空则保持原值不变）",
            example = "13812345678",
            pattern = "^1[3-9]\\d{9}$")
    private String phoneNumber;

    /**
     * 邮箱
     */
    @Schema(description = "电子邮箱（选填，若提供则更新，必须符合邮箱格式规范；若为空则保持原值不变）",
            example = "zhangsan@example.com")
    private String email;
} 