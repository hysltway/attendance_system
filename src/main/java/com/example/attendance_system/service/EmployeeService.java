package com.example.attendance_system.service;

import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.entity.Employee;

/**
 * 员工服务接口
 */
public interface EmployeeService {
    /**
     * 注册员工
     * @param registrationDTO 员工注册信息
     * @return 注册成功的员工信息
     */
    Employee registerEmployee(EmployeeRegistrationDTO registrationDTO);
} 