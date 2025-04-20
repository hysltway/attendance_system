package com.example.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假记录实体类
 * 对应数据库表t_leave_record
 */
@Data
@Entity
@Table(name = "t_leave_record")
public class LeaveRecord {
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
     * 请假类型：1-事假，2-病假，3-年假，4-婚假，5-产假，6-丧假，7-其他
     */
    @Column(name = "leave_type", nullable = false)
    private Integer leaveType;
    
    /**
     * 请假开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    /**
     * 请假结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    /**
     * 请假原因
     */
    @Column(length = 500)
    private String reason;
    
    /**
     * 审批状态：0-待审批，1-已批准，2-已拒绝
     */
    @Column(nullable = false)
    private Integer status = 0;
    
    /**
     * 审批人员工编号
     */
    @Column(name = "approver_no", length = 20)
    private String approverNo;
    
    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;
    
    /**
     * 审批备注
     */
    @Column(name = "approval_remark", length = 200)
    private String approvalRemark;
    
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