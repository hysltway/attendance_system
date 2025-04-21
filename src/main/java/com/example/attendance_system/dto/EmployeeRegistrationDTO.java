package com.example.attendance_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工注册表单DTO
 * 用于接收前端提交的员工注册信息
 */
@Data
@Schema(description = "员工注册请求体")
public class EmployeeRegistrationDTO {
    @Schema(description = "员工编号（必填）", required = true, example = "EMP2025001")
    private String employeeNo;
    
    @Schema(description = "员工姓名（必填）", required = true, example = "张三")
    private String name;
    
    @Schema(description = "性别（必填）：1=男，2=女", required = true, example = "1")
    private Integer gender;
    
    @Schema(description = "手机号码（必填）", required = true, example = "13812345678")
    private String phoneNumber;
    
    @Schema(description = "电子邮箱", example = "zhangsan@example.com")
    private String email;
    
    @Schema(description = "部门ID（必填）", required = true, example = "1001")
    private Long departmentId;
    
    @Schema(description = "职位（必填）", required = true, example = "软件工程师")
    private String position;
    
    @Schema(description = "入职日期（必填，格式：yyyy-MM-dd）", required = true, example = "2025-04-01")
    private LocalDate hireDate;
    
    @Schema(description = "密码（必填，长度8-20位）", required = true, example = "password123")
    private String password;
}