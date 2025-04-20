package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 员工信息更新DTO
 */
@Data
public class EmployeeInfoUpdateDTO {
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 员工姓名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phoneNumber;
    
    /**
     * 邮箱
     */
    private String email;
} 