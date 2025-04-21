package com.example.attendance_system.service;

import com.example.attendance_system.entity.Department;
import org.springframework.data.domain.Page;

/**
 * 部门服务接口
 */
public interface DepartmentService {
    
    /**
     * 分页查询部门列表
     * @param current 当前页码
     * @param size 每页记录数
     * @param name 部门名称（模糊匹配）
     * @return 部门分页列表
     */
    Page<Department> getDepartmentListPage(Integer current, Integer size, String name);
    
    /**
     * 创建部门
     * @param department 部门信息
     * @return 创建后的部门信息
     */
    Department createDepartment(Department department);
    
    /**
     * 更新部门信息
     * @param department 部门信息
     * @return 更新后的部门信息
     */
    Department updateDepartment(Department department);
    
    /**
     * 删除部门
     * @param id 部门ID
     * @return 是否删除成功
     */
    boolean deleteDepartment(String id);
    
    /**
     * 获取部门信息
     * @param id 部门ID
     * @return 部门信息
     */
    Department getDepartmentById(Long id);
    
    /**
     * 根据部门名称获取部门信息
     * @param name 部门名称
     * @return 部门信息
     */
    Department getDepartmentByName(String name);
    
    /**
     * 检查部门是否存在
     * @param id 部门ID
     * @return 是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查部门是否有关联员工
     * @param id 部门ID
     * @return 是否有关联员工
     */
    boolean hasEmployees(Long id);
} 