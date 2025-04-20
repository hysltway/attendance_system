package com.example.attendance_system.repository;

import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 员工信息更新请求数据访问接口
 */
@Repository
public interface EmployeeInfoUpdateRequestRepository extends JpaRepository<EmployeeInfoUpdateRequest, Long> {
    
    /**
     * 根据员工编号查询员工信息更新请求
     * @param employeeNo 员工编号
     * @return 员工信息更新请求列表
     */
    List<EmployeeInfoUpdateRequest> findByEmployeeNoOrderByCreatedTimeDesc(String employeeNo);
    
    /**
     * 根据员工编号和状态查询员工信息更新请求
     * @param employeeNo 员工编号
     * @param status 状态
     * @return 员工信息更新请求列表
     */
    List<EmployeeInfoUpdateRequest> findByEmployeeNoAndStatusOrderByCreatedTimeDesc(String employeeNo, Integer status);
    
    /**
     * 查询所有待审核的员工信息更新请求
     * @param status 状态
     * @return 员工信息更新请求列表
     */
    List<EmployeeInfoUpdateRequest> findByStatusOrderByCreatedTimeAsc(Integer status);
    
    /**
     * 查询所有待审核的员工信息更新请求，排除指定员工
     * @param status 状态
     * @param employeeNo 要排除的员工编号
     * @return 员工信息更新请求列表
     */
    List<EmployeeInfoUpdateRequest> findByStatusAndEmployeeNoNotOrderByCreatedTimeAsc(Integer status, String employeeNo);
    
    /**
     * 按员工编号查询待审核的员工信息更新请求，排除指定员工
     * @param status 状态
     * @param employeeNo 员工编号
     * @param excludeEmployeeNo 要排除的员工编号
     * @return 员工信息更新请求列表
     */
    List<EmployeeInfoUpdateRequest> findByStatusAndEmployeeNoAndEmployeeNoNotOrderByCreatedTimeAsc(Integer status, String employeeNo, String excludeEmployeeNo);
    
    /**
     * 按员工姓名模糊查询待审核的员工信息更新请求，排除指定员工
     * @param status 状态
     * @param name 员工姓名（部分）
     * @param excludeEmployeeNo 要排除的员工编号
     * @return 员工信息更新请求列表
     */
    List<EmployeeInfoUpdateRequest> findByStatusAndNameContainingAndEmployeeNoNotOrderByCreatedTimeAsc(Integer status, String name, String excludeEmployeeNo);
    
    /**
     * 查询所有待审核的员工信息更新请求（分页）
     * @param status 状态
     * @param pageable 分页参数
     * @return 员工信息更新请求分页结果
     */
    Page<EmployeeInfoUpdateRequest> findByStatusOrderByCreatedTimeAsc(Integer status, Pageable pageable);
    
    /**
     * 查询所有待审核的员工信息更新请求，排除指定员工（分页）
     * @param status 状态
     * @param employeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 员工信息更新请求分页结果
     */
    Page<EmployeeInfoUpdateRequest> findByStatusAndEmployeeNoNotOrderByCreatedTimeAsc(Integer status, String employeeNo, Pageable pageable);
    
    /**
     * 按员工编号查询待审核的员工信息更新请求，排除指定员工（分页）
     * @param status 状态
     * @param employeeNo 员工编号
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 员工信息更新请求分页结果
     */
    Page<EmployeeInfoUpdateRequest> findByStatusAndEmployeeNoAndEmployeeNoNotOrderByCreatedTimeAsc(Integer status, String employeeNo, String excludeEmployeeNo, Pageable pageable);
    
    /**
     * 按员工姓名模糊查询待审核的员工信息更新请求，排除指定员工（分页）
     * @param status 状态
     * @param name 员工姓名（部分）
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 员工信息更新请求分页结果
     */
    Page<EmployeeInfoUpdateRequest> findByStatusAndNameContainingAndEmployeeNoNotOrderByCreatedTimeAsc(Integer status, String name, String excludeEmployeeNo, Pageable pageable);
} 