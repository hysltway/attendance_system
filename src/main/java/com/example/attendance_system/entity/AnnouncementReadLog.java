package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 公告阅读记录实体类
 * 对应数据库表t_announcement_read_log
 */
@Data
@Entity
@Table(name = "t_announcement_read_log")
public class AnnouncementReadLog {
    /**
     * 记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 公告ID
     */
    @Column(name = "announcement_id", nullable = false)
    private Long announcementId;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", length = 50, nullable = false)
    private String employeeNo;

    /**
     * 阅读时间
     */
    @CreationTimestamp
    @Column(name = "read_time", updatable = false)
    private LocalDateTime readTime;

    /**
     * 员工姓名（冗余存储，便于查询）
     */
    @Column(name = "employee_name", length = 50)
    private String employeeName;

    /**
     * 部门ID（冗余存储，便于查询）
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门名称（冗余存储，便于查询）
     */
    @Column(name = "department_name", length = 50)
    private String departmentName;
} 