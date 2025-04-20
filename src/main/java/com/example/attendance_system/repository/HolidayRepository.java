package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 节假日数据访问接口
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    /**
     * 根据日期查询节假日信息
     * @param date 日期
     * @return 节假日信息
     */
    Optional<Holiday> findByHolidayDate(LocalDate date);
    
    /**
     * 根据年份查询所有节假日
     * @param year 年份
     * @return 节假日列表
     */
    List<Holiday> findByYear(Integer year);
    
    /**
     * 根据年份和类型查询节假日
     * @param year 年份
     * @param type 类型
     * @return 节假日列表
     */
    List<Holiday> findByYearAndType(Integer year, String type);
    
    /**
     * 检查指定日期是否为法定节假日
     * @param date 日期
     * @return 是否为法定节假日
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h " +
           "WHERE h.holidayDate = :date AND h.type = 'public_holiday'")
    boolean isPublicHoliday(@Param("date") LocalDate date);
    
    /**
     * 检查指定日期是否为调休工作日
     * @param date 日期
     * @return 是否为调休工作日
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h " +
           "WHERE h.holidayDate = :date AND h.type = 'transfer_workday'")
    boolean isTransferWorkday(@Param("date") LocalDate date);
} 