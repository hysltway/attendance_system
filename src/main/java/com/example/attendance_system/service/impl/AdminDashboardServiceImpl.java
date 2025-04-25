package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.AdminDashboardDTO;
import com.example.attendance_system.dto.WorkingHoursDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.entity.Department;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.LeaveRecord;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.repository.DepartmentRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.repository.LeaveRecordRepository;
import com.example.attendance_system.service.AdminDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员仪表盘服务实现类
 */
@Service
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private LeaveRecordRepository leaveRecordRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    @Deprecated
    public AdminDashboardDTO getDashboardData(Integer timeGranularity) {
        if (timeGranularity == null) {
            timeGranularity = 1; // 默认为日
        }

        // 构建仪表盘数据
        AdminDashboardDTO dashboardDTO = new AdminDashboardDTO();

        // 获取基础指标统计
        dashboardDTO.setSummary(getSummaryData());

        // 获取考勤趋势图数据
        dashboardDTO.setAttendanceTrend(getAttendanceTrendData(timeGranularity));

        // 获取考勤状态分布数据
        dashboardDTO.setStatusDistribution(getStatusDistributionData());

        // 获取部门考勤对比数据
        dashboardDTO.setDepartmentComparison(getDepartmentComparisonData());

        // 获取考勤时间热力图数据
        dashboardDTO.setTimeHeatmap(getTimeHeatmapData());

        // 获取异常预警数据
        dashboardDTO.setAbnormalWarning(getAbnormalWarningData());

        // 获取审批统计数据
        dashboardDTO.setApprovalStatistics(getApprovalStatisticsData());

        return dashboardDTO;
    }
    
    @Override
    public AdminDashboardDTO.SummaryDTO getDashboardSummary() {
        return getSummaryData();
    }
    
    @Override
    public AdminDashboardDTO.AttendanceTrendDTO getAttendanceTrend(Integer timeGranularity) {
        if (timeGranularity == null) {
            timeGranularity = 1; // 默认为日
        }
        
        if (timeGranularity != 1 && timeGranularity != 3) {
            timeGranularity = 1; // 只支持日(1)和月(3)两种粒度
        }
        
        return getAttendanceTrendData(timeGranularity);
    }
    
    @Override
    public AdminDashboardDTO.StatusDistributionDTO getStatusDistribution() {
        return getStatusDistributionData();
    }
    
    @Override
    public AdminDashboardDTO.DepartmentComparisonDTO getDepartmentComparison() {
        return getDepartmentComparisonData();
    }
    
    @Override
    public AdminDashboardDTO.TimeHeatmapDTO getTimeHeatmap() {
        return getTimeHeatmapData();
    }
    
    @Override
    public AdminDashboardDTO.AbnormalWarningDTO getAbnormalWarning() {
        return getAbnormalWarningData();
    }
    
    @Override
    public Map<String, Object> getWorkingHours() {
        // 创建结果Map
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前日期
        LocalDate today = LocalDate.now();
        
        // 获取日统计数据
        WorkingHoursDTO dailyStats = calculateWorkingHours(today, today, "日");
        result.put("daily", dailyStats);
        
        // 获取周统计数据
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        WorkingHoursDTO weeklyStats = calculateWorkingHours(startOfWeek, today, "周");
        result.put("weekly", weeklyStats);
        
        // 获取月统计数据
        LocalDate startOfMonth = today.withDayOfMonth(1);
        WorkingHoursDTO monthlyStats = calculateWorkingHours(startOfMonth, today, "月");
        result.put("monthly", monthlyStats);
        
        return result;
    }
    
    /**
     * 实现接口方法，获取审批统计数据
     *
     * @return 审批统计数据
     */
    @Override
    public AdminDashboardDTO.ApprovalStatisticsDTO getApprovalStatistics() {
        return getApprovalStatisticsData();
    }
    
    /**
     * 计算指定日期范围内的工时统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param period    统计周期（日/周/月）
     * @return 工时统计数据
     */
    private WorkingHoursDTO calculateWorkingHours(LocalDate startDate, LocalDate endDate, String period) {
        // 设置时间范围
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 获取活跃员工数量
        List<Employee> activeEmployees = employeeRepository.findByStatus(1);
        int totalEmployeeCount = activeEmployees.size();
        
        // 查询时间范围内的考勤记录
        List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findAllByCheckTimeBetween(startDateTime, endDateTime);
        
        // 查询时间范围内的请假记录
        List<LeaveRecord> leaveRecords = leaveRecordRepository.findApprovedLeaveByDateRange(startDate, endDate);
        
        // 计算平均工作时长
        double totalWorkingHours = 0.0;
        double totalOvertimeHours = 0.0;
        double maxWorkingHours = 0.0;
        double minWorkingHours = Double.MAX_VALUE;
        int attendanceCount = 0;
        int overtimeEmployeeCount = 0;
        
        // 处理不同员工的工作时长
        Map<String, List<AttendanceRecord>> employeeAttendanceMap = attendanceRecords.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getEmployeeNo));
        
        for (Map.Entry<String, List<AttendanceRecord>> entry : employeeAttendanceMap.entrySet()) {
            double employeeWorkingHours = calculateEmployeeWorkingHours(entry.getValue());
            
            if (employeeWorkingHours > 0) {
                attendanceCount++;
                totalWorkingHours += employeeWorkingHours;
                
                // 计算加班时长 (假设标准工作时长为8小时)
                double overtimeHours = Math.max(0, employeeWorkingHours - 8.0);
                totalOvertimeHours += overtimeHours;
                
                if (overtimeHours > 0) {
                    overtimeEmployeeCount++;
                }
                
                // 更新最大和最小工作时长
                maxWorkingHours = Math.max(maxWorkingHours, employeeWorkingHours);
                minWorkingHours = Math.min(minWorkingHours, employeeWorkingHours);
            }
        }
        
        // 计算请假时长
        double totalLeaveHours = leaveRecords.stream()
                .mapToDouble(record -> {
                    // 假设每天请假8小时
                    long daysBetween = record.getEndDate().toEpochDay() - 
                            record.getStartDate().toEpochDay() + 1;
                    return daysBetween * 8.0;
                })
                .sum();
        
        // 计算平均值
        double averageWorkingHours = attendanceCount > 0 ? totalWorkingHours / attendanceCount : 0;
        double averageOvertimeHours = attendanceCount > 0 ? totalOvertimeHours / attendanceCount : 0;
        double averageLeaveHours = totalEmployeeCount > 0 ? totalLeaveHours / totalEmployeeCount : 0;
        double effectiveWorkingHours = Math.max(0, averageWorkingHours - 1.0); // 假设平均休息1小时
        
        // 计算出勤率
        double attendanceRate = totalEmployeeCount > 0 ? (double) attendanceCount / totalEmployeeCount : 0;
        
        // 计算加班员工比例
        double overtimeEmployeeRate = attendanceCount > 0 ? (double) overtimeEmployeeCount / attendanceCount : 0;
        
        // 如果没有记录，设置最小工作时长为0
        if (minWorkingHours == Double.MAX_VALUE) {
            minWorkingHours = 0.0;
        }
        
        // 构建并返回结果
        return WorkingHoursDTO.builder()
                .averageWorkingHours(roundToOneDecimal(averageWorkingHours))
                .averageOvertimeHours(roundToOneDecimal(averageOvertimeHours))
                .averageLeaveHours(roundToOneDecimal(averageLeaveHours))
                .maxWorkingHours(roundToOneDecimal(maxWorkingHours))
                .minWorkingHours(roundToOneDecimal(minWorkingHours))
                .attendanceRate(roundToOneDecimal(attendanceRate * 100)) // 转为百分比
                .overtimeEmployeeRate(roundToOneDecimal(overtimeEmployeeRate * 100)) // 转为百分比
                .effectiveWorkingHours(roundToOneDecimal(effectiveWorkingHours))
                .period(period)
                .build();
    }
    
    /**
     * 计算员工的工作时长
     *
     * @param records 员工的考勤记录
     * @return 工作时长（小时）
     */
    private double calculateEmployeeWorkingHours(List<AttendanceRecord> records) {
        if (records.size() < 2) {
            return 0.0;
        }
        
        // 按时间排序
        records.sort(Comparator.comparing(AttendanceRecord::getCheckTime));
        
        // 获取第一次打卡（上班）和最后一次打卡（下班）
        LocalDateTime firstCheckIn = records.get(0).getCheckTime();
        LocalDateTime lastCheckOut = records.get(records.size() - 1).getCheckTime();
        
        // 计算工作时长（小时）
        double hours = (lastCheckOut.getHour() - firstCheckIn.getHour()) +
                (lastCheckOut.getMinute() - firstCheckIn.getMinute()) / 60.0;
        
        return Math.max(0, hours);
    }
    
    /**
     * 四舍五入到小数点后一位
     *
     * @param value 原始值
     * @return 四舍五入后的值
     */
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }

    /**
     * 获取基础指标统计数据
     */
    private AdminDashboardDTO.SummaryDTO getSummaryData() {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 设置今天的时间范围
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        // 设置昨天的时间范围
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(23, 59, 59);

        // 获取所有在职员工
        List<Employee> activeEmployees = employeeRepository.findByStatus(1);
        int totalEmployeeCount = activeEmployees.size();

        // 今日出勤数据
        List<AttendanceRecord> todayRecords = attendanceRecordRepository.findAllByCheckTimeBetween(todayStart, todayEnd);
        List<String> todayAttendedEmployees = todayRecords.stream()
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .collect(Collectors.toList());

        // 昨日出勤数据
        List<AttendanceRecord> yesterdayRecords = attendanceRecordRepository.findAllByCheckTimeBetween(yesterdayStart, yesterdayEnd);
        List<String> yesterdayAttendedEmployees = yesterdayRecords.stream()
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .collect(Collectors.toList());

        // 今日出勤人数
        int todayAttendanceCount = todayAttendedEmployees.size();
        // 确保出勤人数不超过总人数
        todayAttendanceCount = Math.min(todayAttendanceCount, totalEmployeeCount);

        // 昨日出勤人数
        int yesterdayAttendanceCount = yesterdayAttendedEmployees.size();
        // 确保出勤人数不超过总人数
        yesterdayAttendanceCount = Math.min(yesterdayAttendanceCount, totalEmployeeCount);
        
        // 防止昨日出勤人数为0导致计算异常
        if (yesterdayAttendanceCount == 0) {
            yesterdayAttendanceCount = 1; // 避免除零错误
        }

        // 今日出勤人数同比变化率
        double attendanceChangeRate = calculateChangeRate(todayAttendanceCount, yesterdayAttendanceCount);
        // 限制变化率在合理范围内
        attendanceChangeRate = Math.max(-50.0, Math.min(50.0, attendanceChangeRate));

        // 今日请假人数
        List<LeaveRecord> todayLeaveRecords = leaveRecordRepository.findApprovedLeaveByDateRange(today, today);
        int todayLeaveCount = (int) todayLeaveRecords.stream()
                .map(LeaveRecord::getEmployeeNo)
                .distinct()
                .count();
        // 确保请假人数不超过总人数
        todayLeaveCount = Math.min(todayLeaveCount, totalEmployeeCount - todayAttendanceCount);

        // 昨日请假人数
        List<LeaveRecord> yesterdayLeaveRecords = leaveRecordRepository.findApprovedLeaveByDateRange(yesterday, yesterday);
        int yesterdayLeaveCount = (int) yesterdayLeaveRecords.stream()
                .map(LeaveRecord::getEmployeeNo)
                .distinct()
                .count();
        
        // 防止昨日请假人数为0导致计算异常
        if (yesterdayLeaveCount == 0 && todayLeaveCount > 0) {
            yesterdayLeaveCount = 1; // 避免除零错误
        }

        // 今日请假人数同比变化率
        double leaveChangeRate = calculateChangeRate(todayLeaveCount, yesterdayLeaveCount);
        // 限制变化率在合理范围内
        leaveChangeRate = Math.max(-50.0, Math.min(50.0, leaveChangeRate));

        // 今日缺勤人数 = 总人数 - 出勤人数 - 请假人数
        int todayAbsentCount = Math.max(0, totalEmployeeCount - todayAttendanceCount - todayLeaveCount);

        // 昨日缺勤人数
        int yesterdayAbsentCount = Math.max(0, totalEmployeeCount - yesterdayAttendanceCount - yesterdayLeaveCount);
        
        // 防止昨日缺勤人数为0导致计算异常
        if (yesterdayAbsentCount == 0 && todayAbsentCount > 0) {
            yesterdayAbsentCount = 1; // 避免除零错误
        }

        // 今日缺勤人数同比变化率
        double absentChangeRate = calculateChangeRate(todayAbsentCount, yesterdayAbsentCount);
        // 限制变化率在合理范围内
        absentChangeRate = Math.max(-50.0, Math.min(50.0, absentChangeRate));

        // 今日迟到人数
        int todayLateCount = (int) todayRecords.stream()
                .filter(record -> record.getStatus() == 2) // 状态为迟到
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();
        // 确保迟到人数不超过出勤人数
        todayLateCount = Math.min(todayLateCount, todayAttendanceCount);

        // 昨日迟到人数
        int yesterdayLateCount = (int) yesterdayRecords.stream()
                .filter(record -> record.getStatus() == 2) // 状态为迟到
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();
        
        // 防止昨日迟到人数为0导致计算异常
        if (yesterdayLateCount == 0 && todayLateCount > 0) {
            yesterdayLateCount = 1; // 避免除零错误
        }

        // 今日迟到人数同比变化率
        double lateChangeRate = calculateChangeRate(todayLateCount, yesterdayLateCount);
        // 限制变化率在合理范围内
        lateChangeRate = Math.max(-50.0, Math.min(50.0, lateChangeRate));

        // 今日早退人数
        int todayEarlyLeaveCount = (int) todayRecords.stream()
                .filter(record -> record.getStatus() == 3) // 状态为早退
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();
        // 确保早退人数不超过出勤人数
        todayEarlyLeaveCount = Math.min(todayEarlyLeaveCount, todayAttendanceCount);

        // 昨日早退人数
        int yesterdayEarlyLeaveCount = (int) yesterdayRecords.stream()
                .filter(record -> record.getStatus() == 3) // 状态为早退
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();
        
        // 防止昨日早退人数为0导致计算异常
        if (yesterdayEarlyLeaveCount == 0 && todayEarlyLeaveCount > 0) {
            yesterdayEarlyLeaveCount = 1; // 避免除零错误
        }

        // 今日早退人数同比变化率
        double earlyLeaveChangeRate = calculateChangeRate(todayEarlyLeaveCount, yesterdayEarlyLeaveCount);
        // 限制变化率在合理范围内
        earlyLeaveChangeRate = Math.max(-50.0, Math.min(50.0, earlyLeaveChangeRate));

        // 今日准时率计算
        double todayOnTimeRate;
        if (todayAttendanceCount > 0) {
            // 准时人数 = 出勤人数 - 迟到人数 - 早退人数
            int onTimeCount = Math.max(0, todayAttendanceCount - todayLateCount - todayEarlyLeaveCount);
            todayOnTimeRate = (double) onTimeCount / todayAttendanceCount;
        } else {
            todayOnTimeRate = 0;
        }

        // 昨日准时率
        double yesterdayOnTimeRate;
        if (yesterdayAttendanceCount > 0) {
            // 准时人数 = 出勤人数 - 迟到人数 - 早退人数
            int yesterdayOnTimeCount = Math.max(0, yesterdayAttendanceCount - yesterdayLateCount - yesterdayEarlyLeaveCount);
            yesterdayOnTimeRate = (double) yesterdayOnTimeCount / yesterdayAttendanceCount;
        } else {
            yesterdayOnTimeRate = todayOnTimeRate > 0 ? 0.5 : 0; // 如果昨日无人出勤，设置合理的基线
        }

        // 今日准时率同比变化
        double onTimeRateChangeRate = calculateChangeRate(todayOnTimeRate, yesterdayOnTimeRate);
        // 限制变化率在合理范围内
        onTimeRateChangeRate = Math.max(-50.0, Math.min(50.0, onTimeRateChangeRate));

        // 打卡异常总数
        int totalAbnormalCount = todayLateCount + todayEarlyLeaveCount + todayAbsentCount;

        // 人脸识别失败次数（暂时默认为0，后续根据实际需求添加）
        int faceRecognitionFailCount = 0;

        // 未打卡人数 = 应出勤人数 - 实际打卡人数
        int expectedAttendanceCount = totalEmployeeCount - todayLeaveCount;
        int notCheckInCount = Math.max(0, expectedAttendanceCount - todayAttendanceCount);

        // 构建并返回基础指标统计数据
        return AdminDashboardDTO.SummaryDTO.builder()
                .attendanceCount(todayAttendanceCount)
                .attendanceChangeRate(roundToOneDecimal(attendanceChangeRate))
                .absentCount(todayAbsentCount)
                .absentChangeRate(roundToOneDecimal(absentChangeRate))
                .onTimeRate(roundToOneDecimal(todayOnTimeRate * 100)) // 转为百分比
                .onTimeRateChangeRate(roundToOneDecimal(onTimeRateChangeRate))
                .lateCount(todayLateCount)
                .lateChangeRate(roundToOneDecimal(lateChangeRate))
                .leaveCount(todayLeaveCount)
                .leaveChangeRate(roundToOneDecimal(leaveChangeRate))
                .earlyLeaveCount(todayEarlyLeaveCount)
                .earlyLeaveChangeRate(roundToOneDecimal(earlyLeaveChangeRate))
                .totalAbnormalCount(totalAbnormalCount)
                .faceRecognitionFailCount(faceRecognitionFailCount)
                .notCheckInCount(notCheckInCount)
                .build();
    }

    // 计算同比变化率
    private double calculateChangeRate(double currentValue, double previousValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }
        return ((currentValue - previousValue) / previousValue) * 100;
    }

    /**
     * 获取考勤趋势图数据
     *
     * @param timeGranularity 时间粒度：1-日, 2-周, 3-月
     * @return 考勤趋势图数据
     */
    private AdminDashboardDTO.AttendanceTrendDTO getAttendanceTrendData(Integer timeGranularity) {
        // 根据时间粒度确定查询范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        List<String> timeLabels = new ArrayList<>();

        // 根据时间粒度设置开始日期和时间标签
        switch (timeGranularity) {
            case 1: // 日
                startDate = endDate.minusDays(6); // 最近7天
                // 生成日期标签
                for (int i = 0; i < 7; i++) {
                    LocalDate date = startDate.plusDays(i);
                    timeLabels.add(date.format(DATE_FORMATTER));
                }
                break;
            case 2: // 周
                startDate = endDate.minusWeeks(11); // 最近12周
                // 生成周标签
                for (int i = 0; i < 12; i++) {
                    LocalDate weekStart = startDate.plusWeeks(i);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    timeLabels.add(weekStart.format(DATE_FORMATTER) + " ~ " + weekEnd.format(DATE_FORMATTER));
                }
                break;
            case 3: // 月
                startDate = endDate.withDayOfMonth(1).minusMonths(11); // 最近12个月
                // 生成月份标签
                for (int i = 0; i < 12; i++) {
                    LocalDate month = startDate.plusMonths(i);
                    timeLabels.add(month.getYear() + "-" + String.format("%02d", month.getMonthValue()));
                }
                break;
            default:
                startDate = endDate.minusDays(6); // 默认最近7天
                // 生成日期标签
                for (int i = 0; i < 7; i++) {
                    LocalDate date = startDate.plusDays(i);
                    timeLabels.add(date.format(DATE_FORMATTER));
                }
                break;
        }

        // 查询时间范围内的所有考勤记录
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<AttendanceRecord> allRecords = attendanceRecordRepository.findAllByCheckTimeBetween(startDateTime, endDateTime);

        // 查询时间范围内的所有请假记录
        List<LeaveRecord> allLeaveRecords = leaveRecordRepository.findApprovedLeaveByDateRange(startDate, endDate);

        // 获取所有在职员工数量
        int totalEmployeeCount = employeeRepository.countByStatus(1);

        // 存储各统计数据
        List<Integer> attendanceCounts = new ArrayList<>();
        List<Integer> lateCounts = new ArrayList<>();
        List<Integer> leaveCounts = new ArrayList<>();
        List<Integer> absentCounts = new ArrayList<>();
        List<Double> chainGrowthRates = new ArrayList<>();
        List<Double> abnormalRateChanges = new ArrayList<>();

        // 根据时间粒度计算每个时间单位的数据
        for (int i = 0; i < timeLabels.size(); i++) {
            LocalDate periodStart, periodEnd;

            // 根据时间粒度确定每个时间单位的起止日期
            switch (timeGranularity) {
                case 1: // 日
                    periodStart = startDate.plusDays(i);
                    periodEnd = periodStart;
                    break;
                case 2: // 周
                    periodStart = startDate.plusWeeks(i);
                    periodEnd = periodStart.plusDays(6);
                    break;
                case 3: // 月
                    periodStart = startDate.plusMonths(i);
                    periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
                    break;
                default:
                    periodStart = startDate.plusDays(i);
                    periodEnd = periodStart;
                    break;
            }

            // 计算当前时间单位的出勤人数
            LocalDateTime periodStartTime = periodStart.atStartOfDay();
            LocalDateTime periodEndTime = periodEnd.atTime(23, 59, 59);

            List<String> periodAttendedEmployees = allRecords.stream()
                    .filter(record -> {
                        LocalDateTime checkTime = record.getCheckTime();
                        return !checkTime.isBefore(periodStartTime) && !checkTime.isAfter(periodEndTime);
                    })
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .collect(Collectors.toList());

            int attendanceCount = periodAttendedEmployees.size();
            attendanceCounts.add(attendanceCount);

            // 计算当前时间单位的迟到人数
            int lateCount = (int) allRecords.stream()
                    .filter(record -> {
                        LocalDateTime checkTime = record.getCheckTime();
                        return !checkTime.isBefore(periodStartTime) && !checkTime.isAfter(periodEndTime)
                                && record.getStatus() == 2; // 状态为迟到
                    })
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .count();
            lateCounts.add(lateCount);

            // 计算当前时间单位的请假人数
            int leaveCount = (int) allLeaveRecords.stream()
                    .filter(record ->
                            !record.getStartDate().isAfter(periodEnd) && !record.getEndDate().isBefore(periodStart))
                    .map(LeaveRecord::getEmployeeNo)
                    .distinct()
                    .count();
            leaveCounts.add(leaveCount);

            // 计算当前时间单位的缺勤人数
            int absentCount = totalEmployeeCount - attendanceCount - leaveCount;
            if (absentCount < 0) absentCount = 0;
            absentCounts.add(absentCount);

            // 计算环比增长率（与前一个时间单位相比）
            double chainGrowthRate = 0.0;
            if (i > 0 && attendanceCounts.get(i - 1) > 0) {
                chainGrowthRate = ((double) attendanceCount - attendanceCounts.get(i - 1)) / attendanceCounts.get(i - 1) * 100;
            }
            chainGrowthRates.add(chainGrowthRate);

            // 计算异常率变化
            double currentAbnormalRate = totalEmployeeCount > 0 ?
                    (double) (lateCount + absentCount) / totalEmployeeCount * 100 : 0;
            double abnormalRateChange = 0.0;
            if (i > 0) {
                double previousAbnormalRate = totalEmployeeCount > 0 ?
                        (double) (lateCounts.get(i - 1) + absentCounts.get(i - 1)) / totalEmployeeCount * 100 : 0;
                abnormalRateChange = currentAbnormalRate - previousAbnormalRate;
            }
            abnormalRateChanges.add(abnormalRateChange);
        }

        // 构建并返回考勤趋势图数据
        return AdminDashboardDTO.AttendanceTrendDTO.builder()
                .timeGranularity(timeGranularity)
                .timeLabels(timeLabels)
                .attendanceCounts(attendanceCounts)
                .lateCounts(lateCounts)
                .leaveCounts(leaveCounts)
                .absentCounts(absentCounts)
                .chainGrowthRates(chainGrowthRates)
                .abnormalRateChanges(abnormalRateChanges)
                .build();
    }

    /**
     * 获取考勤状态分布数据
     *
     * @return 考勤状态分布数据
     */
    private AdminDashboardDTO.StatusDistributionDTO getStatusDistributionData() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 设置本月的时间范围
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        // 查询本月所有考勤记录
        List<AttendanceRecord> monthRecords = attendanceRecordRepository.findAllByCheckTimeBetween(startDateTime, endDateTime);

        // 查询本月所有请假记录
        List<LeaveRecord> monthLeaveRecords = leaveRecordRepository.findApprovedLeaveByDateRange(startOfMonth, endOfMonth);

        // 统计各个状态的人数
        int normalCount = (int) monthRecords.stream()
                .filter(record -> record.getStatus() == 1) // 状态为正常
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();

        int lateCount = (int) monthRecords.stream()
                .filter(record -> record.getStatus() == 2) // 状态为迟到
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();

        int earlyLeaveCount = (int) monthRecords.stream()
                .filter(record -> record.getStatus() == 3) // 状态为早退
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();

        int absenteeismCount = (int) monthRecords.stream()
                .filter(record -> record.getStatus() == 4) // 状态为旷工
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();

        int overtimeCount = (int) monthRecords.stream()
                .filter(record -> record.getStatus() == 5) // 状态为加班
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();

        // 请假人数
        int leaveCount = (int) monthLeaveRecords.stream()
                .map(LeaveRecord::getEmployeeNo)
                .distinct()
                .count();

        // 假设缺勤人数 = 旷工人数
        int absentCount = absenteeismCount;

        // 调休人数（假设数据，实际需要从专门的表中获取）
        int daysOffCount = 0;

        // 迟到超15分钟人数
        int lateOver15MinCount = (int) monthRecords.stream()
                .filter(record -> record.getStatus() == 2 && record.getReason() != null &&
                        record.getReason().contains("迟到") &&
                        record.getReason().matches(".*迟到(\\d+)分钟.*"))
                .filter(record -> {
                    // 从备注中提取迟到分钟数
                    String reason = record.getReason();
                    try {
                        int minutes = Integer.parseInt(reason.replaceAll(".*迟到(\\d+)分钟.*", "$1"));
                        return minutes > 15;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(AttendanceRecord::getEmployeeNo)
                .distinct()
                .count();

        // 准备状态类型列表和对应的数量
        List<String> statusTypes = List.of("正常", "迟到", "早退", "请假", "旷工", "加班", "调休");
        List<Integer> counts = List.of(normalCount, lateCount, earlyLeaveCount, leaveCount, absenteeismCount, overtimeCount, daysOffCount);

        // 构建并返回考勤状态分布数据
        return AdminDashboardDTO.StatusDistributionDTO.builder()
                .statusTypes(statusTypes)
                .counts(counts)
                .normalCount(normalCount)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .leaveCount(leaveCount)
                .absentCount(absentCount)
                .absenteeismCount(absenteeismCount)
                .daysOffCount(daysOffCount)
                .overtimeCount(overtimeCount)
                .lateOver15MinCount(lateOver15MinCount)
                .build();
    }

    /**
     * 获取部门考勤对比数据
     *
     * @return 部门考勤对比数据
     */
    private AdminDashboardDTO.DepartmentComparisonDTO getDepartmentComparisonData() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 设置本月的时间范围
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        // 查询本月所有考勤记录
        List<AttendanceRecord> monthRecords = attendanceRecordRepository.findAllByCheckTimeBetween(startDateTime, endDateTime);

        // 获取所有在职员工及其部门信息
        List<Employee> allEmployees = employeeRepository.findByStatus(1);

        // 汇总部门信息
        Map<Integer, String> departmentMap = new HashMap<>();
        Map<Integer, List<Employee>> departmentEmployees = new HashMap<>();

        for (Employee employee : allEmployees) {
            Long departmentId = employee.getDepartmentId();
            if (departmentId != null) {
                // 从部门仓库获取部门名称
                Integer deptId = departmentId.intValue();
                // 如果departmentMap中没有这个部门ID，则从数据库查询
                if (!departmentMap.containsKey(deptId)) {
                    String departmentName = departmentRepository.findById(departmentId)
                            .map(Department::getName)
                            .orElse("未知部门");
                    departmentMap.put(deptId, departmentName);
                }

                // 将员工按部门分组
                if (!departmentEmployees.containsKey(deptId)) {
                    departmentEmployees.put(deptId, new ArrayList<>());
                }
                departmentEmployees.get(deptId).add(employee);
            }
        }

        // 部门列表和ID列表
        List<Integer> departmentIds = new ArrayList<>(departmentMap.keySet());
        List<String> departments = departmentIds.stream()
                .map(departmentMap::get)
                .collect(Collectors.toList());

        // 各部门的考勤状态分布
        Map<Integer, Map<String, Integer>> departmentStatusMap = new HashMap<>();

        // 部门考勤率排行
        Map<Integer, Double> attendanceRateRanking = new HashMap<>();

        // 部门异常率排行
        Map<Integer, Double> abnormalRateRanking = new HashMap<>();

        // 连续异常人数
        Map<Integer, Integer> continuousAbnormalCounts = new HashMap<>();

        // 遍历每个部门，计算统计数据
        for (Integer departmentId : departmentIds) {
            List<Employee> employees = departmentEmployees.get(departmentId);
            List<String> employeeNos = employees.stream()
                    .map(Employee::getEmployeeNo)
                    .collect(Collectors.toList());

            // 部门员工总数
            int totalCount = employees.size();

            // 计算部门的出勤、迟到、早退、旷工等人数
            Map<String, Integer> statusCount = new HashMap<>();

            // 正常出勤人数
            int normalCount = (int) monthRecords.stream()
                    .filter(record -> employeeNos.contains(record.getEmployeeNo()) && record.getStatus() == 1)
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .count();
            statusCount.put("正常", normalCount);

            // 迟到人数
            int lateCount = (int) monthRecords.stream()
                    .filter(record -> employeeNos.contains(record.getEmployeeNo()) && record.getStatus() == 2)
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .count();
            statusCount.put("迟到", lateCount);

            // 早退人数
            int earlyLeaveCount = (int) monthRecords.stream()
                    .filter(record -> employeeNos.contains(record.getEmployeeNo()) && record.getStatus() == 3)
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .count();
            statusCount.put("早退", earlyLeaveCount);

            // 旷工人数
            int absenteeismCount = (int) monthRecords.stream()
                    .filter(record -> employeeNos.contains(record.getEmployeeNo()) && record.getStatus() == 4)
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .count();
            statusCount.put("旷工", absenteeismCount);

            // 加班人数
            int overtimeCount = (int) monthRecords.stream()
                    .filter(record -> employeeNos.contains(record.getEmployeeNo()) && record.getStatus() == 5)
                    .map(AttendanceRecord::getEmployeeNo)
                    .distinct()
                    .count();
            statusCount.put("加班", overtimeCount);

            departmentStatusMap.put(departmentId, statusCount);

            // 计算部门出勤率
            double attendanceRate;
            if (totalCount > 0) {
                // 出勤率 = 正常出勤人数 / 部门总人数
                attendanceRate = (double) normalCount / totalCount * 100;
                // 确保出勤率不超过100%
                attendanceRate = Math.min(100.0, attendanceRate);
            } else {
                attendanceRate = 0;
            }
            attendanceRateRanking.put(departmentId, roundToOneDecimal(attendanceRate));

            // 计算部门异常率
            double abnormalRate;
            if (totalCount > 0) {
                // 异常率 = (迟到人数 + 早退人数 + 旷工人数) / 部门总人数
                int abnormalCount = lateCount + earlyLeaveCount + absenteeismCount;
                abnormalRate = (double) abnormalCount / totalCount * 100;
                // 确保异常率不超过100%
                abnormalRate = Math.min(100.0, abnormalRate);
            } else {
                abnormalRate = 0;
            }
            abnormalRateRanking.put(departmentId, roundToOneDecimal(abnormalRate));

            // 连续异常人数（假设数据，实际需要更复杂的查询）
            int continuousAbnormalCount = 0; // 此处简化处理
            continuousAbnormalCounts.put(departmentId, continuousAbnormalCount);
        }

        // 构建并返回部门考勤对比数据
        return AdminDashboardDTO.DepartmentComparisonDTO.builder()
                .departments(departments)
                .departmentIds(departmentIds)
                .departmentStatusMap(departmentStatusMap)
                .attendanceRateRanking(attendanceRateRanking)
                .abnormalRateRanking(abnormalRateRanking)
                .continuousAbnormalCounts(continuousAbnormalCounts)
                .build();
    }

    /**
     * 获取考勤时间热力图数据
     *
     * @return 考勤时间热力图数据
     */
    private AdminDashboardDTO.TimeHeatmapDTO getTimeHeatmapData() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 设置过去30天的时间范围
        LocalDate startDate = today.minusDays(29);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(23, 59, 59);

        // 查询过去30天的所有考勤记录
        List<AttendanceRecord> records = attendanceRecordRepository.findAllByCheckTimeBetween(startDateTime, endDateTime);

        // 星期几列表（1-7，对应周一到周日）
        List<Integer> weekdays = List.of(1, 2, 3, 4, 5, 6, 7);

        // 工作时间段（小时）
        List<Integer> hours = List.of(7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

        // 初始化热力图数据
        List<List<Integer>> heatmapData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            List<Integer> hourData = new ArrayList<>();
            for (int j = 0; j < hours.size(); j++) {
                hourData.add(0);
            }
            heatmapData.add(hourData);
        }

        // 统计每个时间段的打卡人数
        for (AttendanceRecord record : records) {
            LocalDateTime checkTime = record.getCheckTime();
            int dayOfWeek = checkTime.getDayOfWeek().getValue(); // 1-7
            int hour = checkTime.getHour();

            // 检查是否在工作时间范围内
            int hourIndex = hours.indexOf(hour);
            if (hourIndex != -1) {
                // 累加对应时间段的打卡数
                int currentCount = heatmapData.get(dayOfWeek - 1).get(hourIndex);
                heatmapData.get(dayOfWeek - 1).set(hourIndex, currentCount + 1);
            }
        }

        // 构建并返回考勤时间热力图数据
        return AdminDashboardDTO.TimeHeatmapDTO.builder()
                .weekdays(weekdays)
                .hours(hours)
                .heatmapData(heatmapData)
                .build();
    }

    /**
     * 获取异常预警数据
     *
     * @return 异常预警数据
     */
    private AdminDashboardDTO.AbnormalWarningDTO getAbnormalWarningData() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 设置过去30天的时间范围
        LocalDate startDate = today.minusDays(29);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(23, 59, 59);

        // 查询过去30天的所有考勤记录
        List<AttendanceRecord> records = attendanceRecordRepository.findAllByCheckTimeBetween(startDateTime, endDateTime);

        // 高风险员工列表（连续异常次数 ≥ 3）
        List<AdminDashboardDTO.AbnormalWarningDTO.HighRiskEmployeeDTO> highRiskEmployees = new ArrayList<>();

        // 按员工分组统计异常记录
        Map<String, List<AttendanceRecord>> employeeAbnormalRecords = records.stream()
                .filter(record -> record.getStatus() == 2 || record.getStatus() == 3 || record.getStatus() == 4)
                .collect(Collectors.groupingBy(AttendanceRecord::getEmployeeNo));

        // 遍历每个有异常记录的员工
        for (Map.Entry<String, List<AttendanceRecord>> entry : employeeAbnormalRecords.entrySet()) {
            String employeeNo = entry.getKey();
            List<AttendanceRecord> abnormalRecords = entry.getValue();

            // 按日期排序
            abnormalRecords.sort((a, b) -> a.getCheckTime().compareTo(b.getCheckTime()));

            // 检查是否有连续3天及以上的异常
            if (hasConsecutiveAbnormal(abnormalRecords, 3)) {
                // 获取员工信息
                Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
                if (employee != null) {
                    // 统计主要异常类型
                    Map<Integer, Long> statusCounts = abnormalRecords.stream()
                            .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

                    // 找出最常见的异常类型
                    int mainAbnormalType = statusCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(0);

                    String abnormalTypeDesc = getStatusTypeDesc(mainAbnormalType);

                    // 添加到高风险员工列表
                    highRiskEmployees.add(AdminDashboardDTO.AbnormalWarningDTO.HighRiskEmployeeDTO.builder()
                            .employeeNo(employeeNo)
                            .employeeName(employee.getName())
                            .department(getDepartmentName(employee.getDepartmentId()))
                            .continuousAbnormalCount(getMaxConsecutiveDays(abnormalRecords))
                            .abnormalType(abnormalTypeDesc)
                            .build());
                }
            }
        }

        // 异常原因Top3
        List<AdminDashboardDTO.AbnormalWarningDTO.AbnormalReasonDTO> topAbnormalReasons = new ArrayList<>();

        // 统计异常原因
        Map<String, Long> reasonCounts = records.stream()
                .filter(record -> record.getStatus() != 1 && record.getReason() != null && !record.getReason().isEmpty())
                .collect(Collectors.groupingBy(this::extractAbnormalReason, Collectors.counting()));

        // 计算总异常数
        long totalAbnormalCount = reasonCounts.values().stream().mapToLong(Long::longValue).sum();

        // 取Top3异常原因
        reasonCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    String reason = entry.getKey();
                    long count = entry.getValue();
                    double percentage = totalAbnormalCount > 0 ? (double) count / totalAbnormalCount * 100 : 0;

                    topAbnormalReasons.add(AdminDashboardDTO.AbnormalWarningDTO.AbnormalReasonDTO.builder()
                            .reason(reason)
                            .count((int) count)
                            .percentage(percentage)
                            .build());
                });

        // 近7日异常高发时段
        List<AdminDashboardDTO.AbnormalWarningDTO.AbnormalTimeSlotDTO> abnormalTimeSlots = new ArrayList<>();

        // 设置过去7天的时间范围
        LocalDate startDateWeek = today.minusDays(6);
        LocalDateTime startDateTimeWeek = startDateWeek.atStartOfDay();

        // 查询过去7天的异常记录
        List<AttendanceRecord> weekAbnormalRecords = records.stream()
                .filter(record -> record.getStatus() != 1
                        && !record.getCheckTime().isBefore(startDateTimeWeek)
                        && !record.getCheckTime().isAfter(endDateTime))
                .collect(Collectors.toList());

        // 按日期和小时分组统计
        Map<LocalDate, Map<Integer, Integer>> dateHourCounts = new HashMap<>();

        for (AttendanceRecord record : weekAbnormalRecords) {
            LocalDate date = record.getCheckTime().toLocalDate();
            int hour = record.getCheckTime().getHour();

            dateHourCounts.putIfAbsent(date, new HashMap<>());
            Map<Integer, Integer> hourCounts = dateHourCounts.get(date);
            hourCounts.put(hour, hourCounts.getOrDefault(hour, 0) + 1);
        }

        // 找出每天异常最多的时段
        for (LocalDate date : dateHourCounts.keySet()) {
            Map<Integer, Integer> hourCounts = dateHourCounts.get(date);

            // 找出异常最多的小时
            Map.Entry<Integer, Integer> maxEntry = hourCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (maxEntry != null) {
                int hour = maxEntry.getKey();
                int count = maxEntry.getValue();

                // 只添加异常数大于等于2的时段
                if (count >= 2) {
                    abnormalTimeSlots.add(AdminDashboardDTO.AbnormalWarningDTO.AbnormalTimeSlotDTO.builder()
                            .date(date.format(DATE_FORMATTER))
                            .timeSlot(String.format("%02d:00-%02d:59", hour, hour))
                            .count(count)
                            .build());
                }
            }
        }

        // 根据异常次数降序排序
        abnormalTimeSlots.sort((a, b) -> b.getCount().compareTo(a.getCount()));

        // 构建并返回异常预警数据
        return AdminDashboardDTO.AbnormalWarningDTO.builder()
                .highRiskEmployees(highRiskEmployees)
                .topAbnormalReasons(topAbnormalReasons)
                .abnormalTimeSlots(abnormalTimeSlots)
                .build();
    }

    /**
     * 获取审批统计数据
     *
     * @return 审批统计数据
     */
    private AdminDashboardDTO.ApprovalStatisticsDTO getApprovalStatisticsData() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 设置本月的时间范围
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(23, 59, 59);

        // 待处理请假申请数
        int pendingLeaveRequestCount = leaveRecordRepository.countByStatus(0); // 状态为0表示待审批

        // 待处理考勤异常申诉数
        int pendingAttendanceAppealCount = attendanceRecordRepository.countBySubmittedToAdmin(true);

        // 本月已处理记录数（请假审批 + 考勤异常处理）
        int processedLeaveCount = leaveRecordRepository.countByStatusInAndApprovalTimeBetween(
                List.of(1, 2), startDateTime, endDateTime); // 状态为1或2表示已批准或已拒绝

        int processedAttendanceCount = attendanceRecordRepository.countByProcessedByAdminAndUpdatedTimeBetween(
                true, startDateTime, endDateTime);

        int processedRecordCount = processedLeaveCount + processedAttendanceCount;

        // 平均审批用时（小时）
        double averageApprovalTime = 0.0;

        // 获取本月已处理的请假记录
        List<LeaveRecord> processedLeaveRecords = leaveRecordRepository.findByStatusInAndApprovalTimeBetween(
                List.of(1, 2), startDateTime, endDateTime);

        if (!processedLeaveRecords.isEmpty()) {
            // 计算平均审批时间
            double totalHours = 0.0;
            int count = 0;

            for (LeaveRecord record : processedLeaveRecords) {
                if (record.getCreatedTime() != null && record.getApprovalTime() != null) {
                    // 计算从创建到审批的时间差（小时）
                    long seconds = java.time.Duration.between(record.getCreatedTime(), record.getApprovalTime()).getSeconds();
                    double hours = seconds / 3600.0;
                    totalHours += hours;
                    count++;
                }
            }

            if (count > 0) {
                averageApprovalTime = totalHours / count;
            }
        }

        // 构建并返回审批统计数据
        return AdminDashboardDTO.ApprovalStatisticsDTO.builder()
                .pendingLeaveRequestCount(pendingLeaveRequestCount)
                .pendingAttendanceAppealCount(pendingAttendanceAppealCount)
                .processedRecordCount(processedRecordCount)
                .averageApprovalTime(averageApprovalTime)
                .build();
    }

    /**
     * 从异常记录中提取异常原因
     *
     * @param record 异常考勤记录
     * @return 异常原因
     */
    private String extractAbnormalReason(AttendanceRecord record) {
        String reason = record.getReason();

        if (reason == null || reason.isEmpty()) {
            return getStatusTypeDesc(record.getStatus());
        }

        // 简化原因，提取关键信息
        if (reason.contains("迟到")) {
            return "迟到";
        } else if (reason.contains("早退")) {
            return "早退";
        } else if (reason.contains("旷工")) {
            return "旷工";
        } else {
            return reason.length() > 10 ? reason.substring(0, 10) + "..." : reason;
        }
    }

    /**
     * 检查是否有连续n天及以上的异常
     *
     * @param records 按时间排序的异常记录
     * @param n       连续天数
     * @return 是否有连续n天及以上的异常
     */
    private boolean hasConsecutiveAbnormal(List<AttendanceRecord> records, int n) {
        if (records.size() < n) {
            return false;
        }

        // 按日期分组
        Map<LocalDate, List<AttendanceRecord>> dateRecords = records.stream()
                .collect(Collectors.groupingBy(record -> record.getCheckTime().toLocalDate()));

        // 获取所有日期并排序
        List<LocalDate> dates = new ArrayList<>(dateRecords.keySet());
        Collections.sort(dates);

        // 检查是否有连续n天
        int consecutiveDays = 1;
        for (int i = 1; i < dates.size(); i++) {
            LocalDate prevDate = dates.get(i - 1);
            LocalDate currDate = dates.get(i);

            // 如果是连续的日期
            if (currDate.isEqual(prevDate.plusDays(1))) {
                consecutiveDays++;
                if (consecutiveDays >= n) {
                    return true;
                }
            } else {
                consecutiveDays = 1;
            }
        }

        return false;
    }

    /**
     * 获取最大连续异常天数
     *
     * @param records 按时间排序的异常记录
     * @return 最大连续异常天数
     */
    private int getMaxConsecutiveDays(List<AttendanceRecord> records) {
        if (records.isEmpty()) {
            return 0;
        }

        // 按日期分组
        Map<LocalDate, List<AttendanceRecord>> dateRecords = records.stream()
                .collect(Collectors.groupingBy(record -> record.getCheckTime().toLocalDate()));

        // 获取所有日期并排序
        List<LocalDate> dates = new ArrayList<>(dateRecords.keySet());
        Collections.sort(dates);

        // 计算最大连续天数
        int maxConsecutiveDays = 1;
        int currentConsecutiveDays = 1;

        for (int i = 1; i < dates.size(); i++) {
            LocalDate prevDate = dates.get(i - 1);
            LocalDate currDate = dates.get(i);

            // 如果是连续的日期
            if (currDate.isEqual(prevDate.plusDays(1))) {
                currentConsecutiveDays++;
                maxConsecutiveDays = Math.max(maxConsecutiveDays, currentConsecutiveDays);
            } else {
                currentConsecutiveDays = 1;
            }
        }

        return maxConsecutiveDays;
    }

    /**
     * 根据状态编号获取状态描述
     *
     * @param status 状态编号
     * @return 状态描述
     */
    private String getStatusTypeDesc(int status) {
        switch (status) {
            case 1:
                return "正常";
            case 2:
                return "迟到";
            case 3:
                return "早退";
            case 4:
                return "旷工";
            case 5:
                return "加班";
            default:
                return "未知";
        }
    }

    /**
     * 根据部门ID获取部门名称
     *
     * @param departmentId 部门ID
     * @return 部门名称，如果找不到则返回"未知部门"
     */
    private String getDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return "未知部门";
        }
        return departmentRepository.findById(departmentId)
                .map(Department::getName)
                .orElse("未知部门");
    }
} 