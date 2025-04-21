package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 部门实体类
 * 对应数据库表t_department
 */
@Data
@Entity
@Table(name = "t_department")
public class Department {
    /**
     * 部门ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 部门名称
     */
    @Column(length = 50, nullable = false)
    private String name;
    
    /**
     * 部门编码
     */
    @Column(length = 20, unique = true)
    private String code;
    
    /**
     * 部门描述
     */
    @Column(length = 200)
    private String description;
    
    /**
     * 父部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 状态：1-正常，0-停用
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
} 