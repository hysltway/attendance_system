package com.example.attendance_system.service;

import com.example.attendance_system.entity.LeaveRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假服务接口
 */
public interface LeaveService {

    /**
     * 提交请假申请
     * @param leaveRecord 请假记录
     * @return 请假记录
     */
    LeaveRecord submitLeaveApplication(LeaveRecord leaveRecord);
    
    /**
     * 审批请假申请
     * @param id 请假记录ID
     * @param approverNo 审批人员工编号
     * @param status 审批状态：1-已批准，2-已拒绝
     * @param remark 审批备注
     * @return 审批后的请假记录
     */
    LeaveRecord approveLeaveApplication(Long id, String approverNo, Integer status, String remark);
    
    /**
     * 获取员工请假记录
     * @param employeeNo 员工编号
     * @return 请假记录列表
     */
    List<LeaveRecord> getEmployeeLeaveRecords(String employeeNo);
    
    /**
     * 获取待审批的请假记录
     * @return 待审批的请假记录列表
     */
    List<LeaveRecord> getPendingLeaveRecords();
    
    /**
     * 检查员工在指定日期是否请假
     * @param employeeNo 员工编号
     * @param date 日期
     * @return 是否请假
     */
    boolean isEmployeeOnLeave(String employeeNo, LocalDate date);
    
    /**
     * 获取指定日期请假的员工列表
     * @param date 日期
     * @return 请假员工编号列表
     */
    List<String> getEmployeesOnLeave(LocalDate date);
} 