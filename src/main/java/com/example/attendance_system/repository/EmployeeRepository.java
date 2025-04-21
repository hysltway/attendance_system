package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 员工数据访问接口
 * Repository  表示该类专注于和数据库交互，
 * 属于数据访问层（Data Access Object，简称 DAO）
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // 这是一个员工数据访问接口，继承自JpaRepository
    // JpaRepository<Employee, Long>表示这个接口操作Employee实体，主键类型是Long
    // 它提供了基本的CRUD操作方法，如save(), findById(), findAll(), delete()等
    // 通过继承JpaRepository，我们无需编写基本的数据库操作代码

    /**
     * 根据员工编号查询员工
     *
     * @param employeeNo 员工编号
     * @return 员工对象
     */
    Employee findByEmployeeNo(String employeeNo);

    /**
     * 检查手机号是否已存在
     *
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据员工状态查询员工列表
     *
     * @param status 员工状态：1-在职，0-离职
     * @return 员工列表
     */
    List<Employee> findByStatus(Integer status);

    /**
     * 根据员工状态统计员工数量
     *
     * @param status 员工状态：1-在职，0-离职
     * @return 员工数量
     */
    int countByStatus(Integer status);

    /**
     * 根据员工姓名模糊查询
     *
     * @param name 员工姓名（部分）
     * @return 匹配的员工列表
     */
    List<Employee> findByNameContaining(String name);

    /**
     * 根据员工姓名模糊查询（分页）
     *
     * @param name     员工姓名（部分）
     * @param pageable 分页参数
     * @return 匹配的员工分页列表
     */
    Page<Employee> findByNameContaining(String name, Pageable pageable);

    /**
     * 根据部门ID列表查询员工（分页）
     *
     * @param departmentIds 部门ID列表
     * @param pageable      分页参数
     * @return 匹配的员工分页列表
     */
    Page<Employee> findByDepartmentIdIn(List<Long> departmentIds, Pageable pageable);

    /**
     * 根据员工姓名和部门ID列表查询员工（分页）
     *
     * @param name          员工姓名（部分）
     * @param departmentIds 部门ID列表
     * @param pageable      分页参数
     * @return 匹配的员工分页列表
     */
    Page<Employee> findByNameContainingAndDepartmentIdIn(String name, List<Long> departmentIds, Pageable pageable);

    /**
     * 统计指定部门下的员工数量
     *
     * @param departmentId 部门ID
     * @return 员工数量
     */
    int countByDepartmentId(Long departmentId);

    /**
     * 根据部门ID查询员工列表（分页）
     *
     * @param departmentId 部门ID
     * @param pageable     分页参数
     * @return 匹配的员工分页列表
     */
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
} 