package com.example.attendance_system.config;

import com.example.attendance_system.entity.Holiday;
import com.example.attendance_system.service.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/**
 * 节假日数据初始化器
 */
@Slf4j
@Configuration
public class HolidayInitializer {

    @Autowired
    private HolidayService holidayService;
    
    /**
     * 创建命令行运行器，在应用启动时初始化节假日数据
     * 优化为只在数据库中没有当年节假日数据时才进行同步
     * @return CommandLineRunner实例
     */
    @Bean
    public CommandLineRunner initHolidayData() {
        return args -> {
            log.info("检查节假日数据...");
            
            // 获取当前年份
            int currentYear = LocalDate.now().getYear();
            
            try {
                // 检查当前年份的节假日数据是否存在
                List<Holiday> currentYearHolidays = holidayService.getHolidaysByYear(currentYear);
                if (currentYearHolidays == null || currentYearHolidays.isEmpty()) {
                    log.info("数据库中不存在{}年的节假日数据，开始同步...", currentYear);
                    int currentYearCount = holidayService.syncHolidaysFromAPI(currentYear);
                    log.info("已同步{}年的{}个节假日", currentYear, currentYearCount);
                } else {
                    log.info("数据库中已存在{}年的{}个节假日数据，无需同步", currentYear, currentYearHolidays.size());
                }
                
                log.info("节假日数据检查完成");
            } catch (Exception e) {
                log.error("检查节假日数据失败", e);
                // 不阻止应用启动，只记录错误
            }
        };
    }
} 