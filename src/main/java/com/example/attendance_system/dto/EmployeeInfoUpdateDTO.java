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
    @Schema(description = "员工编号（必填）", required = true, example = "EMP2025001")
    private String employeeNo;
    
    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String name;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号码", example = "13812345678")
    private String phoneNumber;
    
    /**
     * 邮箱
     */
    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    private String email;
} 