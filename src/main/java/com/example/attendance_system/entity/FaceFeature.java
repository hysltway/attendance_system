package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 人脸特征实体类
 * 对应数据库表t_face_feature
 */
@Data
@Entity
@Table(name = "t_face_feature")
public class FaceFeature {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的员工编号
     */
    @Column(name = "employee_no", length = 20, unique = true, nullable = false)
    private String employeeNo;

    /**
     * 人脸特征向量，存储128维特征数据的JSON字符串
     */
    @Column(name = "feature_vector", columnDefinition = "TEXT", nullable = false)
    private String featureVector;

    /**
     * 特征提取时间
     */
    @Column(name = "extraction_time")
    private LocalDateTime extractionTime;

    /**
     * 状态：1-有效，0-无效
     */
    @Column(nullable = false)
    private Integer status = 1;

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