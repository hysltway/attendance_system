package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 公告实体类
 * 对应数据库表t_announcement
 */
@Data
@Entity
@Table(name = "t_announcement")
public class Announcement {
    /**
     * 公告ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 公告标题
     */
    @Column(length = 100, nullable = false)
    private String title;

    /**
     * 公告内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 目标范围：all-全体员工，partial-指定部门
     */
    @Column(name = "target_scope", length = 20, nullable = false)
    private String targetScope;

    /**
     * 指定部门ID列表，JSON格式存储
     */
    @Column(name = "departments", columnDefinition = "TEXT")
    private String departments;

    /**
     * 发布类型：immediate-立即发布，scheduled-定时发布
     */
    @Column(name = "publish_type", length = 20, nullable = false)
    private String publishType;

    /**
     * 预定发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 公告生效开始时间
     */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    /**
     * 公告失效时间
     */
    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    /**
     * 发布人
     */
    @Column(length = 50, nullable = false)
    private String publisher;

    /**
     * 公告状态：draft-草稿，published-已发布，expired-已过期
     */
    @Column(length = 20, nullable = false)
    private String status;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

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