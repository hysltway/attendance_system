package com.example.attendance_system.config;

import com.example.attendance_system.service.HolidayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

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
     * @return CommandLineRunner实例
     */
    @Bean
    public CommandLineRunner initHolidayData() {
        return args -> {
            log.info("开始初始化节假日数据...");
            
            // 获取当前年份和下一年份
            int currentYear = LocalDate.now().getYear();
            int nextYear = currentYear + 1;
            
            try {
                // 同步当前年份的节假日数据
                int currentYearCount = holidayService.syncHolidaysFromAPI(currentYear);
                log.info("已同步{}年的{}个节假日", currentYear, currentYearCount);
                
                // 同步下一年份的节假日数据
                int nextYearCount = holidayService.syncHolidaysFromAPI(nextYear);
                log.info("已同步{}年的{}个节假日", nextYear, nextYearCount);
                
                log.info("节假日数据初始化完成");
            } catch (Exception e) {
                log.error("初始化节假日数据失败", e);
                // 不阻止应用启动，只记录错误
            }
        };
    }
} 