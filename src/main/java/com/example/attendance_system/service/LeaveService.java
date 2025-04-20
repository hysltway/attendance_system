package com.example.attendance_system.service;

import com.example.attendance_system.entity.LeaveRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * 获取员工请假记录（不分页）
     * @param employeeNo 员工编号
     * @return 请假记录列表
     */
    List<LeaveRecord> getEmployeeLeaveRecords(String employeeNo);
    
    /**
     * 获取员工请假记录（分页）
     * @param employeeNo 员工编号
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getEmployeeLeaveRecords(String employeeNo, Pageable pageable);
    
    /**
     * 根据状态获取员工请假记录（分页）
     * @param employeeNo 员工编号
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getEmployeeLeaveRecordsByStatus(String employeeNo, Integer status, Pageable pageable);
    
    /**
     * 获取所有请假记录（分页）
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getAllLeaveRecords(Pageable pageable);
    
    /**
     * 获取所有请假记录（分页），排除指定员工
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getAllLeaveRecordsExcludeEmployee(String excludeEmployeeNo, Pageable pageable);
    
    /**
     * 根据状态获取所有请假记录（分页）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getLeaveRecordsByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据状态获取所有请假记录（分页），排除指定员工
     * @param status 状态
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getLeaveRecordsByStatusExcludeEmployee(Integer status, String excludeEmployeeNo, Pageable pageable);
    
    /**
     * 根据员工编号获取所有请假记录（分页）
     * @param employeeNo 员工编号
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getLeaveRecordsByEmployeeNo(String employeeNo, Pageable pageable);
    
    /**
     * 根据员工编号和状态获取所有请假记录（分页）
     * @param employeeNo 员工编号
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getLeaveRecordsByEmployeeNoAndStatus(String employeeNo, Integer status, Pageable pageable);
    
    /**
     * 检查是否与已有请假记录重叠
     * @param employeeNo 员工编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 是否重叠
     */
    boolean hasOverlappingLeave(String employeeNo, LocalDate startDate, LocalDate endDate);
    
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
    
    /**
     * 根据员工姓名模糊查询请假记录（分页），排除指定员工
     * @param employeeName 员工姓名（部分）
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getLeaveRecordsByEmployeeNameExcludeEmployee(String employeeName, String excludeEmployeeNo, Pageable pageable);
    
    /**
     * 根据员工姓名和状态模糊查询请假记录（分页），排除指定员工
     * @param employeeName 员工姓名（部分）
     * @param status 状态
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> getLeaveRecordsByEmployeeNameAndStatusExcludeEmployee(String employeeName, Integer status, String excludeEmployeeNo, Pageable pageable);
} 