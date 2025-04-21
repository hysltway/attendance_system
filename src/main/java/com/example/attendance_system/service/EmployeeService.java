package com.example.attendance_system.service;

import com.example.attendance_system.dto.EmployeeInfoUpdateAuditDTO;
import com.example.attendance_system.dto.EmployeeInfoUpdateDTO;
import com.example.attendance_system.dto.EmployeeInfoUpdatePageDTO;
import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.dto.LoginDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import org.springframework.data.domain.Page;

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
     * @param employeeNo 员工编号筛选（可选）
     * @param name 员工姓名筛选（可选）
     * @return 请求列表
     */
    List<EmployeeInfoUpdateRequest> getPendingInfoUpdateRequestsExcludeEmployee(String excludeEmployeeNo, String employeeNo, String name);
    
    /**
     * 审核员工信息更新请求
     * @param auditDTO 审核信息
     * @return 更新后的请求
     */
    EmployeeInfoUpdateRequest auditInfoUpdateRequest(EmployeeInfoUpdateAuditDTO auditDTO);
    
    /**
     * 查询所有待审核的员工信息更新请求，排除指定员工（分页）
     * @param excludeEmployeeNo 要排除的员工编号
     * @param employeeNo 员工编号筛选（可选）
     * @param name 员工姓名筛选（可选）
     * @param current 当前页码（从1开始）
     * @param size 每页记录数
     * @return 请求分页结果
     */
    EmployeeInfoUpdatePageDTO getPendingInfoUpdateRequestsExcludeEmployeePage(String excludeEmployeeNo, String employeeNo, String name, Integer current, Integer size);
    
    /**
     * 分页查询员工列表
     * @param current 当前页码
     * @param size 每页记录数
     * @param name 员工姓名（模糊匹配）
     * @param department 部门名称（精确或模糊匹配）
     * @return 员工分页列表
     */
    Page<Employee> getEmployeeListPage(Integer current, Integer size, String name, String department);
    
    /**
     * 更新员工信息
     * @param employee 员工信息
     * @return 更新后的员工信息
     */
    Employee updateEmployee(Employee employee);
    
    /**
     * 删除员工（逻辑删除）
     * @param employeeId 员工ID
     * @return 是否删除成功
     */
    boolean deleteEmployee(String employeeId);
    
    /**
     * 创建员工
     * @param employee 员工信息
     * @return 创建后的员工信息
     */
    Employee createEmployee(Employee employee);
} 