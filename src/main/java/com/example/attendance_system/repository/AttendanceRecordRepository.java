package com.example.attendance_system.repository;

import com.example.attendance_system.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤记录数据访问接口
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 根据员工编号查询考勤记录
     * @param employeeNo 员工编号
     * @return 考勤记录列表
     */
    List<AttendanceRecord> findByEmployeeNo(String employeeNo);

    /**
     * 根据员工编号和时间范围查询考勤记录
     * @param employeeNo 员工编号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 考勤记录列表
     */
    List<AttendanceRecord> findByEmployeeNoAndCheckTimeBetween(
            String employeeNo, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询员工在指定日期是否已有打卡记录
     * @param employeeNo 员工编号
     * @param startOfDay 当天开始时间
     * @param endOfDay 当天结束时间
     * @param checkType 打卡类型
     * @return 记录数量
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.employeeNo = :employeeNo " +
            "AND a.checkTime BETWEEN :startOfDay AND :endOfDay " +
            "AND a.checkType = :checkType")
    long countTodayAttendance(
            @Param("employeeNo") String employeeNo,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("checkType") Integer checkType);
    
    /**
     * 分页查询员工的异常考勤记录（状态不为1-正常的记录）
     * @param employeeNo 员工编号
     * @param pageable 分页参数
     * @return 异常考勤记录分页结果
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.employeeNo = :employeeNo AND a.status != 1 ORDER BY a.checkTime DESC")
    Page<AttendanceRecord> findExceptionRecords(@Param("employeeNo") String employeeNo, Pageable pageable);

    /**
     * 根据员工编号和时间范围查询考勤记录数量
     * @param employeeNo 员工编号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 考勤记录数量
     */
    long countByEmployeeNoAndCheckTimeBetween(String employeeNo, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询所有已提交申诉但未处理的异常考勤记录（submittedToAdmin为true）
     * @param pageable 分页参数
     * @return 异常考勤记录分页结果
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.submittedToAdmin = true ORDER BY a.checkTime DESC")
    Page<AttendanceRecord> findAllExceptionAppeals(Pageable pageable);
} 