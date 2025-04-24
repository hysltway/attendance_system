package com.example.attendance_system.config;

import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.HolidayService;
import com.example.attendance_system.service.LeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤数据初始化器
 * 用于系统启动时检查并补充生成缺失的旷工记录
 */
@Slf4j
@Configuration
public class AttendanceInitializer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private HolidayService holidayService;
    @Autowired
    private LeaveService leaveService;
    @Value("${app.attendance.missing-records-check-days:7}")
    private int missingRecordsCheckDays;

    /**
     * 创建命令行运行器，在应用启动时检查并补充生成缺失的旷工记录
     *
     * @return CommandLineRunner实例
     */
    @Bean
    public CommandLineRunner checkMissingAttendanceRecords() {
        return args -> {
            log.info("开始检查缺失的旷工记录，检查天数: {}...", missingRecordsCheckDays);

            // 获取当前日期
            LocalDate today = LocalDate.now();

            // 检查从前N天到昨天的考勤记录，天数通过配置文件指定
            for (int i = missingRecordsCheckDays; i >= 1; i--) {
                LocalDate checkDate = today.minusDays(i);
                String dateStr = checkDate.format(DATE_FORMATTER);

                // 检查是否是工作日
                if (!holidayService.isWorkday(checkDate)) {
                    log.info("{}不是工作日，跳过旷工检查", dateStr);
                    continue;
                }

                log.info("检查{}的缺失旷工记录", dateStr);

                // 设置当天时间范围
                LocalDateTime startOfDay = checkDate.atStartOfDay();
                LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);

                // 查询所有在职员工
                List<Employee> allActiveEmployees = employeeRepository.findByStatus(1);

                // 获取当天请假的员工列表
                List<String> employeesOnLeave = leaveService.getEmployeesOnLeave(checkDate);

                int absentCount = 0;
                List<AttendanceRecord> missingRecords = new ArrayList<>();

                for (Employee employee : allActiveEmployees) {
                    String employeeNo = employee.getEmployeeNo();

                    // 如果员工当天请假，则跳过检查
                    if (employeesOnLeave.contains(employeeNo)) {
                        continue;
                    }

                    // 查询员工当天的考勤记录
                    long attendanceCount = attendanceRecordRepository.countByEmployeeNoAndCheckTimeBetween(
                            employeeNo, startOfDay, endOfDay);

                    // 如果没有考勤记录，则补充旷工记录
                    if (attendanceCount == 0) {
                        log.info("在职员工{}(姓名:{})在{}没有任何打卡记录，补充旷工记录",
                                employeeNo, employee.getName(), dateStr);

                        // 创建旷工记录
                        AttendanceRecord absentRecord = new AttendanceRecord();
                        absentRecord.setEmployeeNo(employeeNo);
                        // 设置为当天上班时间09:00作为打卡时间
                        absentRecord.setCheckTime(checkDate.atTime(9, 0));
                        absentRecord.setCheckType(1); // 上班打卡
                        absentRecord.setCheckMethod(3); // 系统自动生成
                        absentRecord.setStatus(4); // 旷工
                        absentRecord.setReason("整天未打卡，系统启动时自动补充旷工记录");

                        missingRecords.add(absentRecord);
                        absentCount++;
                    }
                }

                // 批量保存旷工记录
                if (!missingRecords.isEmpty()) {
                    attendanceRecordRepository.saveAll(missingRecords);
                    log.info("日期{}：成功补充{}条旷工记录", dateStr, missingRecords.size());
                } else {
                    log.info("日期{}：没有需要补充的旷工记录", dateStr);
                }
            }

            log.info("缺失旷工记录检查完成");
        };
    }
} 