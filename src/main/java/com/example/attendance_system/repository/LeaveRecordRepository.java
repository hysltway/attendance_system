package com.example.attendance_system.repository;

import com.example.attendance_system.entity.LeaveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假记录数据访问接口
 */
@Repository
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, Long> {

    /**
     * 根据员工编号查询请假记录
     * @param employeeNo 员工编号
     * @return 请假记录列表
     */
    List<LeaveRecord> findByEmployeeNo(String employeeNo);
    
    /**
     * 根据员工编号和状态查询请假记录
     * @param employeeNo 员工编号
     * @param status 审批状态
     * @return 请假记录列表
     */
    List<LeaveRecord> findByEmployeeNoAndStatus(String employeeNo, Integer status);
    
    /**
     * 检查指定员工在指定日期是否有已批准的请假记录
     * @param employeeNo 员工编号
     * @param date 日期
     * @return 是否有已批准的请假记录
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LeaveRecord l " +
           "WHERE l.employeeNo = :employeeNo AND l.status = 1 " +
           "AND :date BETWEEN l.startDate AND l.endDate")
    boolean hasApprovedLeave(@Param("employeeNo") String employeeNo, @Param("date") LocalDate date);
    
    /**
     * 查询指定日期有已批准请假的所有员工编号
     * @param date 日期
     * @return 员工编号列表
     */
    @Query("SELECT DISTINCT l.employeeNo FROM LeaveRecord l " +
           "WHERE l.status = 1 AND :date BETWEEN l.startDate AND l.endDate")
    List<String> findEmployeesOnLeave(@Param("date") LocalDate date);
} 