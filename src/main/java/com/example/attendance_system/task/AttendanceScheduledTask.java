package com.example.attendance_system.task;

import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.HolidayService;
import com.example.attendance_system.service.LeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考勤定时任务
 */
@Slf4j
@Component
public class AttendanceScheduledTask {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private HolidayService holidayService;
    
    @Autowired
    private LeaveService leaveService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 每天晚上23点检查旷工情况
     * 检查当天所有在职员工是否有打卡记录，如果没有且不是节假日/请假，则记为旷工
     */
    @Scheduled(cron = "0 0 23 * * ?")
    @Transactional
    public void checkAbsenteeism() {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);
        log.info("开始执行旷工检查任务，日期：{}", todayStr);
        
        // 检查今天是否是工作日
        if (!holidayService.isWorkday(today)) {
            log.info("今天{}不是工作日，跳过旷工检查", todayStr);
            return;
        }
        
        // 设置当天时间范围
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 直接查询所有在职员工（状态为1-在职）
        List<Employee> allActiveEmployees = employeeRepository.findByStatus(1);
        log.info("总在职员工数：{}", allActiveEmployees.size());
        
        // 获取今天请假的员工列表
        List<String> employeesOnLeave = leaveService.getEmployeesOnLeave(today);
        log.info("今天请假的员工数：{}", employeesOnLeave.size());
        
        int absentCount = 0;
        
        for (Employee employee : allActiveEmployees) {
            String employeeNo = employee.getEmployeeNo();
            
            // 如果员工今天请假，则跳过检查
            if (employeesOnLeave.contains(employeeNo)) {
                log.debug("员工{}今天请假，跳过旷工检查", employeeNo);
                continue;
            }
            
            // 查询员工今天的考勤记录
            long attendanceCount = attendanceRecordRepository.countByEmployeeNoAndCheckTimeBetween(
                    employeeNo, startOfDay, endOfDay);
            
            // 如果没有考勤记录，则记为旷工
            if (attendanceCount == 0) {
                log.info("在职员工{}(姓名:{})今天没有任何打卡记录，记为旷工", 
                        employeeNo, employee.getName());
                
                // 创建旷工记录
                AttendanceRecord absentRecord = new AttendanceRecord();
                absentRecord.setEmployeeNo(employeeNo);
                // 设置为当天上班时间09:00作为打卡时间
                absentRecord.setCheckTime(today.atTime(9, 0));
                absentRecord.setCheckType(1); // 上班打卡
                absentRecord.setCheckMethod(3); // 系统自动生成
                absentRecord.setStatus(4); // 旷工
                absentRecord.setReason("整天未打卡，系统自动记为旷工");
                
                // 保存旷工记录
                attendanceRecordRepository.save(absentRecord);
                absentCount++;
            }
        }
        
        log.info("旷工检查任务完成，日期：{}，检测到{}名旷工员工", todayStr, absentCount);
    }
    
    /**
     * 每月1号0点同步下一个月的节假日数据
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void syncNextMonthHolidays() {
        // 获取下个月的年份
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        int year = nextMonth.getYear();
        
        log.info("开始同步{}年的节假日数据", year);
        try {
            int count = holidayService.syncHolidaysFromAPI(year);
            log.info("成功同步{}年的{}个节假日", year, count);
        } catch (Exception e) {
            log.error("同步{}年节假日数据失败", year, e);
        }
    }
} 