package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 考勤记录实体类
 * 对应数据库表t_attendance_record
 */
@Data
@Entity
@Table(name = "t_attendance_record")
public class AttendanceRecord {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 员工编号
     */
    @Column(name = "employee_no", length = 20, nullable = false)
    private String employeeNo;
    
    /**
     * 打卡时间
     */
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;
    
    /**
     * 打卡类型：1-上班打卡，2-下班打卡，3-外出打卡，4-返回打卡
     */
    @Column(name = "check_type", nullable = false)
    private Integer checkType;
    
    /**
     * 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     */
    @Column(name = "check_method", nullable = false)
    private Integer checkMethod;
    
    /**
     * 打卡状态：1-正常，2-迟到，3-早退，4-旷工，5-加班
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 备注
     */
    @Column(length = 200)
    private String remark;
    
    /**
     * 异常描述（自动生成）
     */
    @Column(length = 200)
    private String reason;
    
    /**
     * 员工申诉说明
     */
    @Column(length = 500)
    private String explanation;
    
    /**
     * 是否提交管理员处理：true-是，false-否
     */
    @Column(name = "submitted_to_admin")
    private Boolean submittedToAdmin = false;
    
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