package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 * 对应数据库表t_employee
 */
@Data
@Entity
@Table(name = "t_employee")
public class Employee {
    /**
     * 主键ID
     * GenerationType.IDENTITY 表示数据库负责生成主键，这通常是通过设置一个自增列来完成的。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 员工编号，唯一标识
     * 格式：mm/yy/xxxx
     * mm：入职月份
     * yy：入职年份
     * xxxx：随机生成的数字
     * 员工注册时自动生成，总长度为8位
     */
    @Column(name = "employee_no", length = 20, unique = true, nullable = false)
    private String employeeNo;
    
    /**
     * 员工姓名
     */
    @Column(length = 30, nullable = false)
    private String name;
    
    /**
     * 性别：0-女，1-男
     */
    @Column(nullable = false)
    private Integer gender;
    
    /**
     * 手机号
     */
    @Column(length = 20)
    private String phoneNumber;
    
    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;
    
    /**
     * 所属部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;
    
    /**
     * 职位
     */
    @Column(length = 50)
    private String position;
    
    /**
     * 入职日期
     */
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    /**
     * 状态：1-在职，0-离职
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 是否为管理员：true-是，false-否
     */
    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;
    
    /**
     * 密码
     */
    @Column(length = 100, nullable = false)
    private String password;
    
    /**
     * 创建时间，自动填充
     */
    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间，自动更新
     */
    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
} 