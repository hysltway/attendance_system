package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门数据访问接口
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    /**
     * 根据部门编码查询部门
     *
     * @param code 部门编码
     * @return 部门对象
     */
    Department findByCode(String code);

    /**
     * 根据部门名称查询部门
     *
     * @param name 部门名称
     * @return 部门对象
     */
    Department findByName(String name);

    /**
     * 根据部门名称模糊查询部门
     *
     * @param name 部门名称（部分）
     * @return 匹配的部门列表
     */
    List<Department> findByNameContaining(String name);

    /**
     * 根据部门名称模糊查询部门（分页）
     *
     * @param name     部门名称（部分）
     * @param pageable 分页参数
     * @return 匹配的部门分页列表
     */
    Page<Department> findByNameContaining(String name, Pageable pageable);

    /**
     * 根据上级部门ID查询子部门
     *
     * @param parentId 上级部门ID
     * @return 子部门列表
     */
    List<Department> findByParentId(Long parentId);

    /**
     * 根据上级部门ID查询子部门数量
     *
     * @param parentId 上级部门ID
     * @return 子部门数量
     */
    int countByParentId(Long parentId);
} 