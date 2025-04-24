package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 节假日实体类
 * 对应数据库表t_holiday
 */
@Data
@Entity
@Table(name = "t_holiday")
public class Holiday {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 日期（旧字段，保留兼容）
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 日期
     */
    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    /**
     * 节假日名称（旧字段，保留兼容）
     */
    @Column(name = "name", length = 50)
    private String name;

    /**
     * 节假日名称（中文）
     */
    @Column(name = "name_cn", length = 50)
    private String nameCN;

    /**
     * 节假日名称（英文）
     */
    @Column(name = "name_en", length = 50)
    private String nameEN;

    /**
     * 备注
     */
    @Column(name = "remark", length = 200)
    private String remark;

    /**
     * 类型：public_holiday-法定节假日，transfer_workday-调休工作日
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * 年份
     */
    @Column(nullable = false)
    private Integer year;

    /**
     * 地区（如：CN-中国）
     */
    @Column(length = 10)
    private String region;

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