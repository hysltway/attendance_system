package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 登录数据传输对象
 */
@Data
public class LoginDTO {
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 密码
     */
    private String password;
} 