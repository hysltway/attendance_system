package com.example.attendance_system.service.impl;

import com.example.attendance_system.entity.Department;
import com.example.attendance_system.repository.DepartmentRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 分页查询部门列表
     * @param current 当前页码
     * @param size 每页记录数
     * @param name 部门名称（模糊匹配）
     * @return 部门分页列表
     */
    @Override
    public Page<Department> getDepartmentListPage(Integer current, Integer size, String name) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(current - 1, size);
        
        // 查询条件
        Page<Department> departmentPage;
        
        if (name == null || name.isEmpty()) {
            // 无查询条件，查询所有部门
            departmentPage = departmentRepository.findAll(pageable);
        } else {
            // 按名称查询
            departmentPage = departmentRepository.findByNameContaining(name, pageable);
        }
        
        // 计算每个部门的员工数量
        departmentPage.getContent().forEach(department -> {
            // 计算并设置部门员工数量
            int employeeCount = employeeRepository.countByDepartmentId(department.getId());
            department.setEmployeeCount(employeeCount);
        });
        
        return departmentPage;
    }

    /**
     * 创建部门
     * @param department 部门信息
     * @return 创建后的部门信息
     */
    @Override
    @Transactional
    public Department createDepartment(Department department) {
        // 检查部门名称是否已存在
        Department existingDepartment = departmentRepository.findByName(department.getName());
        if (existingDepartment != null) {
            throw new IllegalArgumentException("部门名称已存在");
        }
        
        // 设置默认状态
        if (department.getStatus() == null) {
            department.setStatus(1); // 默认启用
        }
        
        // 保存部门信息
        return departmentRepository.save(department);
    }

    /**
     * 更新部门信息
     * @param department 部门信息
     * @return 更新后的部门信息
     */
    @Override
    @Transactional
    public Department updateDepartment(Department department) {
        // 检查部门是否存在
        Department existingDepartment = departmentRepository.findById(department.getId())
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        
        // 检查部门名称是否已被其他部门使用
        Department departmentWithSameName = departmentRepository.findByName(department.getName());
        if (departmentWithSameName != null && !departmentWithSameName.getId().equals(department.getId())) {
            throw new IllegalArgumentException("部门名称已被其他部门使用");
        }
        
        // 更新部门信息
        existingDepartment.setName(department.getName());
        existingDepartment.setCode(department.getCode());
        existingDepartment.setDescription(department.getDescription());
        existingDepartment.setStatus(department.getStatus());
        
        // 保存更新后的部门信息
        return departmentRepository.save(existingDepartment);
    }

    /**
     * 删除部门
     * @param id 部门ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteDepartment(String id) {
        try {
            // 解析部门ID
            Long departmentId = Long.parseLong(id);
            
            // 检查部门是否存在
            boolean exists = departmentRepository.existsById(departmentId);
            if (!exists) {
                throw new IllegalArgumentException("部门不存在");
            }
            
            // 检查是否有员工使用该部门
            if (hasEmployees(departmentId)) {
                throw new IllegalArgumentException("部门下仍有员工，无法删除");
            }
            
            // 删除部门
            departmentRepository.deleteById(departmentId);
            
            return true;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("部门ID格式不正确");
        }
    }

    /**
     * 获取部门信息
     * @param id 部门ID
     * @return 部门信息
     */
    @Override
    public Department getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        
        return department;
    }

    /**
     * 根据部门名称获取部门信息
     * @param name 部门名称
     * @return 部门信息
     */
    @Override
    public Department getDepartmentByName(String name) {
        Department department = departmentRepository.findByName(name);
        return department;
    }

    /**
     * 检查部门是否存在
     * @param id 部门ID
     * @return 是否存在
     */
    @Override
    public boolean existsById(Long id) {
        return departmentRepository.existsById(id);
    }

    /**
     * 检查部门是否有关联员工
     * @param id 部门ID
     * @return 是否有关联员工
     */
    @Override
    public boolean hasEmployees(Long id) {
        // 统计使用该部门的员工数量
        int count = employeeRepository.countByDepartmentId(id);
        return count > 0;
    }
} 