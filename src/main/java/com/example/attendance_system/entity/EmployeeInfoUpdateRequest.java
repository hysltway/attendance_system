package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 员工信息更新请求实体类
 * 对应数据库表t_employee_info_update_request
 */
@Data
@Entity
@Table(name = "t_employee_info_update_request")
public class EmployeeInfoUpdateRequest {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", nullable = false, length = 20)
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Column(length = 30)
    private String name;

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
     * 审核状态：0-待审核，1-已通过，2-已拒绝
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 审核意见
     */
    @Column(name = "admin_comment", length = 200)
    private String adminComment;

    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private LocalDateTime auditTime;

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