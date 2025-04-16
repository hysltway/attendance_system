package com.example.attendance_system.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工注册表单DTO
 * 用于接收前端提交的员工注册信息
 */
@Data
public class EmployeeRegistrationDTO {
    private String employeeNo;
    private String name;
    private Integer gender;
    private String phoneNumber;
    private String email;
    private Long departmentId;
    private String position;
    private LocalDate hireDate;
    private String password;
}