package com.example.attendance_system.repository;

import com.example.attendance_system.entity.LeaveRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假记录数据访问接口
 */
@Repository
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, Long> {

    /**
     * 根据员工编号查询请假记录
     *
     * @param employeeNo 员工编号
     * @return 请假记录列表
     */
    List<LeaveRecord> findByEmployeeNo(String employeeNo);

    /**
     * 根据员工编号查询请假记录（分页）
     *
     * @param employeeNo 员工编号
     * @param pageable   分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> findByEmployeeNo(String employeeNo, Pageable pageable);

    /**
     * 根据员工编号和状态查询请假记录
     *
     * @param employeeNo 员工编号
     * @param status     审批状态
     * @return 请假记录列表
     */
    List<LeaveRecord> findByEmployeeNoAndStatus(String employeeNo, Integer status);

    /**
     * 根据员工编号和状态查询请假记录（分页）
     *
     * @param employeeNo 员工编号
     * @param status     审批状态
     * @param pageable   分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> findByEmployeeNoAndStatus(String employeeNo, Integer status, Pageable pageable);

    /**
     * 根据状态查询请假记录（分页）
     *
     * @param status   审批状态
     * @param pageable 分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> findByStatus(Integer status, Pageable pageable);

    /**
     * 排除指定员工后，查询所有请假记录（分页）
     *
     * @param employeeNo 要排除的员工编号
     * @param pageable   分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> findByEmployeeNoNot(String employeeNo, Pageable pageable);

    /**
     * 排除指定员工后，根据状态查询请假记录（分页）
     *
     * @param status     审批状态
     * @param employeeNo 要排除的员工编号
     * @param pageable   分页参数
     * @return 分页请假记录
     */
    Page<LeaveRecord> findByStatusAndEmployeeNoNot(Integer status, String employeeNo, Pageable pageable);

    /**
     * 检查指定员工在指定日期是否有已批准的请假记录
     *
     * @param employeeNo 员工编号
     * @param date       日期
     * @return 是否有已批准的请假记录
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LeaveRecord l " +
            "WHERE l.employeeNo = :employeeNo AND l.status = 1 " +
            "AND :date BETWEEN l.startDate AND l.endDate")
    boolean hasApprovedLeave(@Param("employeeNo") String employeeNo, @Param("date") LocalDate date);

    /**
     * 查询指定员工在指定日期范围内是否有重叠的请假记录（包括待审批和已批准的）
     *
     * @param employeeNo 员工编号
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 是否有重叠的请假记录
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LeaveRecord l " +
            "WHERE l.employeeNo = :employeeNo AND (l.status = 0 OR l.status = 1) " +
            "AND NOT (l.endDate < :startDate OR l.startDate > :endDate)")
    boolean hasOverlappingLeave(@Param("employeeNo") String employeeNo,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期有已批准请假的所有员工编号
     *
     * @param date 日期
     * @return 员工编号列表
     */
    @Query("SELECT DISTINCT l.employeeNo FROM LeaveRecord l " +
            "WHERE l.status = 1 AND :date BETWEEN l.startDate AND l.endDate")
    List<String> findEmployeesOnLeave(@Param("date") LocalDate date);

    /**
     * 根据员工姓名查询请假记录（排除指定员工）
     *
     * @param employeeName      员工姓名
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable          分页参数
     * @return 分页请假记录
     */
    @Query("SELECT lr FROM LeaveRecord lr JOIN Employee e ON lr.employeeNo = e.employeeNo " +
            "WHERE e.name LIKE %:employeeName% AND lr.employeeNo != :excludeEmployeeNo")
    Page<LeaveRecord> findByEmployeeNameContainingAndEmployeeNoNot(
            @Param("employeeName") String employeeName,
            @Param("excludeEmployeeNo") String excludeEmployeeNo,
            Pageable pageable);

    /**
     * 根据员工姓名和状态查询请假记录（排除指定员工）
     *
     * @param employeeName      员工姓名
     * @param status            审批状态
     * @param excludeEmployeeNo 要排除的员工编号
     * @param pageable          分页参数
     * @return 分页请假记录
     */
    @Query("SELECT lr FROM LeaveRecord lr JOIN Employee e ON lr.employeeNo = e.employeeNo " +
            "WHERE e.name LIKE %:employeeName% AND lr.status = :status AND lr.employeeNo != :excludeEmployeeNo")
    Page<LeaveRecord> findByEmployeeNameContainingAndStatusAndEmployeeNoNot(
            @Param("employeeName") String employeeName,
            @Param("status") Integer status,
            @Param("excludeEmployeeNo") String excludeEmployeeNo,
            Pageable pageable);

    /**
     * 根据员工编号和分页参数查询所有请假记录（按创建时间降序排序）
     *
     * @param employeeNo 员工编号
     * @param pageable   分页参数
     * @return 请假记录分页结果
     */
    Page<LeaveRecord> findByEmployeeNoOrderByCreatedTimeDesc(String employeeNo, Pageable pageable);

    /**
     * 根据员工编号和状态查询所有请假记录（按创建时间降序排序）
     *
     * @param employeeNo 员工编号
     * @param status     审批状态（0-待审批，1-已批准，2-已拒绝）
     * @param pageable   分页参数
     * @return 请假记录分页结果
     */
    Page<LeaveRecord> findByEmployeeNoAndStatusOrderByCreatedTimeDesc(
            String employeeNo, Integer status, Pageable pageable);

    /**
     * 查询指定日期范围内的已批准请假记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 请假记录列表
     */
    @Query("SELECT l FROM LeaveRecord l WHERE l.status = 1 AND" +
            "((l.startDate BETWEEN :startDate AND :endDate) OR " +
            "(l.endDate BETWEEN :startDate AND :endDate) OR " +
            "(l.startDate <= :startDate AND l.endDate >= :endDate))")
    List<LeaveRecord> findApprovedLeaveByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计指定状态的请假记录数量
     *
     * @param status 审批状态（0-待审批，1-已批准，2-已拒绝）
     * @return 记录数量
     */
    int countByStatus(Integer status);

    /**
     * 统计指定状态列表和时间范围内的请假记录数量
     *
     * @param statusList 审批状态列表
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 记录数量
     */
    int countByStatusInAndApprovalTimeBetween(
            List<Integer> statusList, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定状态列表和时间范围内的请假记录
     *
     * @param statusList 审批状态列表
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 请假记录列表
     */
    List<LeaveRecord> findByStatusInAndApprovalTimeBetween(
            List<Integer> statusList, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查指定员工在指定日期范围内是否有请假记录
     *
     * @param employeeNo 员工编号
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 是否有重叠的请假记录
     */
    @Query("SELECT COUNT(l) > 0 FROM LeaveRecord l WHERE l.employeeNo = :employeeNo AND l.status <> 2 AND " +
            "((l.startDate BETWEEN :startDate AND :endDate) OR " +
            "(l.endDate BETWEEN :startDate AND :endDate) OR " +
            "(l.startDate <= :startDate AND l.endDate >= :endDate))")
    boolean existsOverlappingLeave(
            @Param("employeeNo") String employeeNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
} 