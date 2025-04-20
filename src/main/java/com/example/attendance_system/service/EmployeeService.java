package com.example.attendance_system.service;

import com.example.attendance_system.dto.EmployeeInfoUpdateAuditDTO;
import com.example.attendance_system.dto.EmployeeInfoUpdateDTO;
import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.dto.LoginDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;

import java.util.List;

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
    
    /**
     * 员工登录
     * @param loginDTO 登录信息
     * @return 登录成功的员工信息
     */
    Employee login(LoginDTO loginDTO);
    
    /**
     * 获取员工信息
     * @param employeeNo 员工编号
     * @return 员工信息
     */
    Employee getEmployeeInfo(String employeeNo);
    
    /**
     * 提交员工信息更新请求
     * @param updateDTO 员工信息更新DTO
     * @return 更新请求
     */
    EmployeeInfoUpdateRequest submitInfoUpdateRequest(EmployeeInfoUpdateDTO updateDTO);
    
    /**
     * 查询员工信息更新请求
     * @param employeeNo 员工编号
     * @return 请求列表
     */
    List<EmployeeInfoUpdateRequest> getInfoUpdateRequests(String employeeNo);
    
    /**
     * 查询所有待审核的员工信息更新请求
     * @return 请求列表
     */
    List<EmployeeInfoUpdateRequest> getPendingInfoUpdateRequests();
    
    /**
     * 查询所有待审核的员工信息更新请求，排除指定员工
     * @param excludeEmployeeNo 要排除的员工编号
     * @return 请求列表
     */
    List<EmployeeInfoUpdateRequest> getPendingInfoUpdateRequestsExcludeEmployee(String excludeEmployeeNo);
    
    /**
     * 审核员工信息更新请求
     * @param auditDTO 审核信息
     * @return 更新后的请求
     */
    EmployeeInfoUpdateRequest auditInfoUpdateRequest(EmployeeInfoUpdateAuditDTO auditDTO);
} 