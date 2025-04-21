package com.example.attendance_system.service;

import com.example.attendance_system.entity.Holiday;

import java.time.LocalDate;
import java.util.List;

/**
 * 节假日服务接口
 */
public interface HolidayService {

    /**
     * 判断指定日期是否为工作日
     * 工作日定义：不是周末，不是法定节假日，或者是调休工作日
     *
     * @param date 日期
     * @return 是否为工作日
     */
    boolean isWorkday(LocalDate date);

    /**
     * 从外部API同步指定年份的节假日数据
     *
     * @param year 年份
     * @return 同步的节假日数量
     */
    int syncHolidaysFromAPI(int year);

    /**
     * 添加节假日
     *
     * @param holiday 节假日信息
     * @return 添加的节假日
     */
    Holiday addHoliday(Holiday holiday);

    /**
     * 更新节假日
     *
     * @param holiday 节假日信息
     * @return 更新的节假日
     */
    Holiday updateHoliday(Holiday holiday);

    /**
     * 删除节假日
     *
     * @param id 节假日ID
     */
    void deleteHoliday(Long id);

    /**
     * 获取指定年份的所有节假日
     *
     * @param year 年份
     * @return 节假日列表
     */
    List<Holiday> getHolidaysByYear(int year);

    /**
     * 检查指定日期是否为法定节假日
     *
     * @param date 日期
     * @return 是否为法定节假日
     */
    boolean isPublicHoliday(LocalDate date);

    /**
     * 检查指定日期是否为调休工作日
     *
     * @param date 日期
     * @return 是否为调休工作日
     */
    boolean isTransferWorkday(LocalDate date);
} 