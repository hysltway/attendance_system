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
    @Schema(description = "员工编号（必填，系统唯一标识，格式为'EMP'开头加年份后3位数字，如EMP2025001）", required = true, example = "EMP2025001", pattern = "EMP\\d{7}")
    private String employeeNo;
    
    @Schema(description = "员工姓名（必填，2-20个字符，不能包含特殊字符）", required = true, example = "张三", minLength = 2, maxLength = 20)
    private String name;
    
    @Schema(description = "性别（必填）：1=男，2=女，不支持其他值", required = true, example = "1", minimum = "1", maximum = "2")
    private Integer gender;
    
    @Schema(description = "手机号码（必填，11位中国大陆手机号，系统将用于接收通知）", required = true, example = "13812345678", pattern = "^1[3-9]\\d{9}$")
    private String phoneNumber;
    
    @Schema(description = "电子邮箱（选填，格式需符合邮箱规范，用于接收系统通知和重置密码）", example = "zhangsan@example.com")
    private String email;
    
    @Schema(description = "部门ID（必填，必须是系统中已存在的有效部门ID）", required = true, example = "1001")
    private Long departmentId;
    
    @Schema(description = "职位（必填，2-30个字符，描述员工在公司中的具体职位）", required = true, example = "软件工程师", minLength = 2, maxLength = 30)
    private String position;
    
    @Schema(description = "入职日期（必填，格式：yyyy-MM-dd，不能是未来日期，最早可追溯至10年前）", required = true, example = "2025-04-01")
    private LocalDate hireDate;
    
    @Schema(description = "密码（必填，长度8-20位，必须包含字母和数字，不能包含特殊字符，区分大小写）", required = true, example = "Password123", minLength = 8, maxLength = 20)
    private String password;
}