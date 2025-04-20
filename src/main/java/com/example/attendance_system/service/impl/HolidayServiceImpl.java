package com.example.attendance_system.service.impl;

import com.example.attendance_system.entity.Holiday;
import com.example.attendance_system.repository.HolidayRepository;
import com.example.attendance_system.service.HolidayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 节假日服务实现类
 */
@Slf4j
@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${app.holiday.api-base-url:https://unpkg.com/holiday-calendar/data}")
    private String apiBaseUrl;
    
    @Value("${app.holiday.default-region:CN}")
    private String defaultRegion;
    
    @Override
    public boolean isWorkday(LocalDate date) {
        // 首先检查是否为法定节假日
        if (isPublicHoliday(date)) {
            return false; // 法定节假日不是工作日
        }
        
        // 检查是否为调休工作日
        if (isTransferWorkday(date)) {
            return true; // 调休工作日是工作日
        }
        
        // 非节假日和调休工作日，则周一至周五是工作日
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    @Override
    @Transactional
    public int syncHolidaysFromAPI(int year) {
        String url = String.format("%s/%s/%d.json", apiBaseUrl, defaultRegion, year);
        log.info("开始从API同步{}年节假日数据，URL: {}", year, url);
        
        try {
            // 调用外部API
            String jsonResponse = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            int count = 0;
            JsonNode datesNode = rootNode.get("dates");
            if (datesNode != null && datesNode.isArray()) {
                for (JsonNode dateNode : datesNode) {
                    // 解析节假日信息
                    String dateStr = dateNode.get("date").asText();
                    String nameCN = dateNode.get("name_cn").asText();
                    String nameEN = dateNode.get("name_en").asText();
                    String type = dateNode.get("type").asText();
                    
                    // 转换日期格式
                    LocalDate holidayDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                    
                    // 查找是否已存在该日期的记录
                    Optional<Holiday> existingHoliday = holidayRepository.findByHolidayDate(holidayDate);
                    
                    if (existingHoliday.isPresent()) {
                        // 更新已有记录
                        Holiday holiday = existingHoliday.get();
                        holiday.setNameCN(nameCN);
                        holiday.setNameEN(nameEN);
                        holiday.setType(type);
                        // 设置兼容字段
                        holiday.setDate(holidayDate);
                        holiday.setName(nameCN);
                        holidayRepository.save(holiday);
                    } else {
                        // 创建新记录
                        Holiday holiday = new Holiday();
                        holiday.setHolidayDate(holidayDate);
                        holiday.setNameCN(nameCN);
                        holiday.setNameEN(nameEN);
                        holiday.setType(type);
                        holiday.setYear(year);
                        holiday.setRegion(defaultRegion);
                        // 设置兼容字段
                        holiday.setDate(holidayDate);
                        holiday.setName(nameCN);
                        // 设置备注信息
                        holiday.setRemark(type.equals("public_holiday") ? "法定节假日" : "调休工作日");
                        holidayRepository.save(holiday);
                        count++;
                    }
                }
            }
            log.info("成功从API同步了{}年的{}个节假日", year, count);
            return count;
        } catch (Exception e) {
            log.error("从API同步{}年节假日数据时发生错误", year, e);
            throw new RuntimeException("同步节假日数据失败", e);
        }
    }

    @Override
    @Transactional
    public Holiday addHoliday(Holiday holiday) {
        // 确保date字段与holidayDate保持一致
        holiday.setDate(holiday.getHolidayDate());
        // 确保name字段与nameCN保持一致
        holiday.setName(holiday.getNameCN());
        return holidayRepository.save(holiday);
    }

    @Override
    @Transactional
    public Holiday updateHoliday(Holiday holiday) {
        if (holiday.getId() == null) {
            throw new IllegalArgumentException("更新节假日需要指定ID");
        }
        // 确保date字段与holidayDate保持一致
        holiday.setDate(holiday.getHolidayDate());
        // 确保name字段与nameCN保持一致
        holiday.setName(holiday.getNameCN());
        return holidayRepository.save(holiday);
    }

    @Override
    @Transactional
    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }

    @Override
    public List<Holiday> getHolidaysByYear(int year) {
        return holidayRepository.findByYear(year);
    }

    @Override
    public boolean isPublicHoliday(LocalDate date) {
        return holidayRepository.isPublicHoliday(date);
    }

    @Override
    public boolean isTransferWorkday(LocalDate date) {
        return holidayRepository.isTransferWorkday(date);
    }
} 