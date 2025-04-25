package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.AdminDashboardDTO;
import com.example.attendance_system.dto.EchartsDataDTO;
import com.example.attendance_system.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ECharts格式的管理员仪表盘服务实现类
 */
@Service
public class EchartsAdminDashboardServiceImpl {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * 1. 获取格式化的基础指标数据
     *
     * @return ECharts格式的基础指标统计数据
     */
    public EchartsDataDTO.SummaryDTO getFormattedSummary() {
        AdminDashboardDTO.SummaryDTO originalData = adminDashboardService.getDashboardSummary();

        List<EchartsDataDTO.SummaryDTO.StatisticCard> cards = new ArrayList<>();

        // 出勤信息
        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("今日出勤")
                        .value(originalData.getAttendanceCount())
                        .changeRate(originalData.getAttendanceChangeRate())
                        .unit("人")
                        .type("attendance")
                        .build()
        );

        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("准时率")
                        .value(originalData.getOnTimeRate())
                        .changeRate(originalData.getOnTimeRateChangeRate())
                        .unit("%")
                        .type("attendance")
                        .build()
        );

        // 异常考勤
        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("今日缺勤")
                        .value(originalData.getAbsentCount())
                        .changeRate(originalData.getAbsentChangeRate())
                        .unit("人")
                        .type("absent")
                        .build()
        );

        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("今日迟到")
                        .value(originalData.getLateCount())
                        .changeRate(originalData.getLateChangeRate())
                        .unit("人")
                        .type("late")
                        .build()
        );

        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("今日早退")
                        .value(originalData.getEarlyLeaveCount())
                        .changeRate(originalData.getEarlyLeaveChangeRate())
                        .unit("人")
                        .type("earlyLeave")
                        .build()
        );

        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("异常考勤")
                        .value(originalData.getTotalAbnormalCount())
                        .changeRate(null) // 无同比变化率
                        .unit("人")
                        .type("abnormal")
                        .build()
        );

        // 请假信息
        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("今日请假")
                        .value(originalData.getLeaveCount())
                        .changeRate(originalData.getLeaveChangeRate())
                        .unit("人")
                        .type("leave")
                        .build()
        );

        cards.add(
                EchartsDataDTO.SummaryDTO.StatisticCard.builder()
                        .title("未打卡")
                        .value(originalData.getNotCheckInCount())
                        .changeRate(null) // 无同比变化率
                        .unit("人")
                        .type("absent")
                        .build()
        );

        return EchartsDataDTO.SummaryDTO.builder()
                .statisticCards(cards)
                .build();
    }

    /**
     * 2. 获取格式化的考勤趋势图数据
     *
     * @param timeGranularity 时间粒度：1-日, 3-月
     * @return ECharts格式的考勤趋势图数据
     */
    public EchartsDataDTO.TrendDTO getFormattedAttendanceTrend(Integer timeGranularity) {
        AdminDashboardDTO.AttendanceTrendDTO originalData = adminDashboardService.getAttendanceTrend(timeGranularity);

        List<EchartsDataDTO.TrendDTO.Series> seriesList = new ArrayList<>();

        // 出勤人数趋势
        seriesList.add(
                EchartsDataDTO.TrendDTO.Series.builder()
                        .name("出勤人数")
                        .type("line")
                        .data(originalData.getAttendanceCounts())
                        .smooth(true)
                        .build()
        );

        // 迟到人数趋势
        seriesList.add(
                EchartsDataDTO.TrendDTO.Series.builder()
                        .name("迟到人数")
                        .type("line")
                        .data(originalData.getLateCounts())
                        .smooth(true)
                        .build()
        );

        // 请假人数趋势
        seriesList.add(
                EchartsDataDTO.TrendDTO.Series.builder()
                        .name("请假人数")
                        .type("line")
                        .data(originalData.getLeaveCounts())
                        .smooth(true)
                        .build()
        );

        // 缺勤人数趋势
        seriesList.add(
                EchartsDataDTO.TrendDTO.Series.builder()
                        .name("缺勤人数")
                        .type("line")
                        .data(originalData.getAbsentCounts())
                        .smooth(true)
                        .build()
        );

        // 环比增长率趋势
        seriesList.add(
                EchartsDataDTO.TrendDTO.Series.builder()
                        .name("环比增长率")
                        .type("bar")
                        .data(originalData.getChainGrowthRates().stream()
                                .map(Double::intValue)
                                .collect(Collectors.toList()))
                        .build()
        );

        return EchartsDataDTO.TrendDTO.builder()
                .xAxisData(originalData.getTimeLabels())
                .series(seriesList)
                .timeGranularity(originalData.getTimeGranularity())
                .build();
    }

    /**
     * 3. 获取格式化的考勤状态分布数据
     *
     * @return ECharts格式的考勤状态分布数据
     */
    public EchartsDataDTO.PieChartDTO getFormattedStatusDistribution() {
        AdminDashboardDTO.StatusDistributionDTO originalData = adminDashboardService.getStatusDistribution();

        List<EchartsDataDTO.SeriesData> seriesDataList = new ArrayList<>();
        List<String> legend = new ArrayList<>();

        // 组装数据
        for (int i = 0; i < originalData.getStatusTypes().size(); i++) {
            String status = originalData.getStatusTypes().get(i);
            Integer count = originalData.getCounts().get(i);
            legend.add(status);

            seriesDataList.add(
                    EchartsDataDTO.SeriesData.builder()
                            .name(status)
                            .value(count)
                            .build()
            );
        }

        return EchartsDataDTO.PieChartDTO.builder()
                .data(seriesDataList)
                .title("考勤状态分布")
                .legend(legend)
                .build();
    }

    /**
     * 4. 获取格式化的部门考勤对比数据
     *
     * @return ECharts格式的部门考勤对比数据
     */
    public EchartsDataDTO.DepartmentComparisonDTO getFormattedDepartmentComparison() {
        AdminDashboardDTO.DepartmentComparisonDTO originalData = adminDashboardService.getDepartmentComparison();

        // 部门列表作为x轴数据
        List<String> xAxisData = originalData.getDepartments();

        // 获取所有状态类型
        Set<String> allStatusTypes = new HashSet<>();
        for (Map<String, Integer> statusMap : originalData.getDepartmentStatusMap().values()) {
            allStatusTypes.addAll(statusMap.keySet());
        }
        List<String> statusTypes = new ArrayList<>(allStatusTypes);

        // 创建系列数据
        List<EchartsDataDTO.DepartmentComparisonDTO.Series> seriesList = new ArrayList<>();
        for (String status : statusTypes) {
            List<Integer> data = new ArrayList<>();
            for (Integer deptId : originalData.getDepartmentIds()) {
                Map<String, Integer> statusMap = originalData.getDepartmentStatusMap().get(deptId);
                data.add(statusMap.getOrDefault(status, 0));
            }

            seriesList.add(
                    EchartsDataDTO.DepartmentComparisonDTO.Series.builder()
                            .name(status)
                            .type("bar")
                            .stack("total")
                            .data(data)
                            .build()
            );
        }

        // 创建排名数据
        List<EchartsDataDTO.DepartmentComparisonDTO.Ranking> rankings = new ArrayList<>();

        // 考勤率排名
        List<EchartsDataDTO.DepartmentComparisonDTO.Ranking.RankItem> attendanceRateItems = new ArrayList<>();
        List<Map.Entry<Integer, Double>> attendanceRateEntries = new ArrayList<>(originalData.getAttendanceRateRanking().entrySet());
        attendanceRateEntries.sort(Map.Entry.<Integer, Double>comparingByValue().reversed());

        for (int i = 0; i < attendanceRateEntries.size(); i++) {
            Map.Entry<Integer, Double> entry = attendanceRateEntries.get(i);
            int deptIndex = originalData.getDepartmentIds().indexOf(entry.getKey());
            String deptName = deptIndex >= 0 ? originalData.getDepartments().get(deptIndex) : "未知部门";

            attendanceRateItems.add(
                    EchartsDataDTO.DepartmentComparisonDTO.Ranking.RankItem.builder()
                            .department(deptName)
                            .value(entry.getValue())
                            .rank(i + 1)
                            .build()
            );
        }

        rankings.add(
                EchartsDataDTO.DepartmentComparisonDTO.Ranking.builder()
                        .title("考勤率排名")
                        .items(attendanceRateItems)
                        .build()
        );

        // 异常率排名
        List<EchartsDataDTO.DepartmentComparisonDTO.Ranking.RankItem> abnormalRateItems = new ArrayList<>();
        List<Map.Entry<Integer, Double>> abnormalRateEntries = new ArrayList<>(originalData.getAbnormalRateRanking().entrySet());
        abnormalRateEntries.sort(Map.Entry.<Integer, Double>comparingByValue().reversed());

        for (int i = 0; i < abnormalRateEntries.size(); i++) {
            Map.Entry<Integer, Double> entry = abnormalRateEntries.get(i);
            int deptIndex = originalData.getDepartmentIds().indexOf(entry.getKey());
            String deptName = deptIndex >= 0 ? originalData.getDepartments().get(deptIndex) : "未知部门";

            abnormalRateItems.add(
                    EchartsDataDTO.DepartmentComparisonDTO.Ranking.RankItem.builder()
                            .department(deptName)
                            .value(entry.getValue())
                            .rank(i + 1)
                            .build()
            );
        }

        rankings.add(
                EchartsDataDTO.DepartmentComparisonDTO.Ranking.builder()
                        .title("异常率排名")
                        .items(abnormalRateItems)
                        .build()
        );

        return EchartsDataDTO.DepartmentComparisonDTO.builder()
                .xAxisData(xAxisData)
                .series(seriesList)
                .rankings(rankings)
                .build();
    }

    /**
     * 5. 获取格式化的打卡热力图数据
     *
     * @return ECharts格式的打卡热力图数据
     */
    public EchartsDataDTO.HeatmapDTO getFormattedTimeHeatmap() {
        AdminDashboardDTO.TimeHeatmapDTO originalData = adminDashboardService.getTimeHeatmap();

        // 转换小时标签
        List<String> xAxis = originalData.getHours().stream()
                .map(hour -> hour + ":00")
                .collect(Collectors.toList());

        // 转换星期标签
        List<String> yAxis = originalData.getWeekdays().stream()
                .map(this::weekdayToName)
                .collect(Collectors.toList());

        // 转换热力图数据为[[x, y, value], ...]格式
        List<List<Object>> formattedData = new ArrayList<>();
        for (int y = 0; y < originalData.getHeatmapData().size(); y++) {
            List<Integer> rowData = originalData.getHeatmapData().get(y);
            for (int x = 0; x < rowData.size(); x++) {
                Integer value = rowData.get(x);
                if (value > 0) {  // 可选：过滤0值，减少数据量
                    List<Object> point = new ArrayList<>();
                    point.add(x);
                    point.add(y);
                    point.add(value);
                    formattedData.add(point);
                }
            }
        }

        return EchartsDataDTO.HeatmapDTO.builder()
                .xAxis(xAxis)
                .yAxis(yAxis)
                .data(formattedData)
                .rawData(originalData.getHeatmapData())
                .build();
    }

    /**
     * 6. 获取格式化的异常预警数据
     *
     * @return ECharts格式的异常预警数据
     */
    public EchartsDataDTO.WarningDTO getFormattedAbnormalWarning() {
        AdminDashboardDTO.AbnormalWarningDTO originalData = adminDashboardService.getAbnormalWarning();

        // 高风险员工
        List<EchartsDataDTO.WarningDTO.HighRiskEmployee> highRiskEmployees = originalData.getHighRiskEmployees().stream()
                .map(emp -> {
                    // 根据连续异常次数计算风险等级，5次以上为1.0（最高风险）
                    double riskLevel = Math.min(1.0, emp.getContinuousAbnormalCount() / 5.0);
                    
                    return EchartsDataDTO.WarningDTO.HighRiskEmployee.builder()
                            .employeeNo(emp.getEmployeeNo())
                            .employeeName(emp.getEmployeeName())
                            .department(emp.getDepartment())
                            .continuousCount(emp.getContinuousAbnormalCount())
                            .mainAbnormalType(emp.getAbnormalType())
                            .riskLevel(riskLevel)
                            .build();
                })
                .sorted(Comparator.comparing(EchartsDataDTO.WarningDTO.HighRiskEmployee::getContinuousCount).reversed())
                .collect(Collectors.toList());

        // 异常原因Top3
        List<EchartsDataDTO.SeriesData> abnormalReasons = originalData.getTopAbnormalReasons().stream()
                .map(reason -> EchartsDataDTO.SeriesData.builder()
                        .name(reason.getReason())
                        .value(reason.getCount())
                        .build())
                .collect(Collectors.toList());

        // 异常高发时段
        List<EchartsDataDTO.WarningDTO.TimeSlot> abnormalTimeSlots = originalData.getAbnormalTimeSlots().stream()
                .map(slot -> EchartsDataDTO.WarningDTO.TimeSlot.builder()
                        .date(slot.getDate())
                        .timeSlot(slot.getTimeSlot())
                        .count(slot.getCount())
                        .build())
                .sorted(Comparator.comparing(EchartsDataDTO.WarningDTO.TimeSlot::getCount).reversed())
                .collect(Collectors.toList());

        return EchartsDataDTO.WarningDTO.builder()
                .highRiskEmployees(highRiskEmployees)
                .abnormalReasons(abnormalReasons)
                .abnormalTimeSlots(abnormalTimeSlots)
                .build();
    }

    /**
     * 7. 获取格式化的审批统计数据
     *
     * @return ECharts格式的审批统计数据
     */
    public EchartsDataDTO.ApprovalStatisticsDTO getFormattedApprovalStatistics() {
        AdminDashboardDTO.ApprovalStatisticsDTO originalData = adminDashboardService.getApprovalStatistics();

        List<EchartsDataDTO.ApprovalStatisticsDTO.StatisticCard> cards = new ArrayList<>();

        // 待处理请假申请
        cards.add(
                EchartsDataDTO.ApprovalStatisticsDTO.StatisticCard.builder()
                        .title("待处理请假申请")
                        .value(originalData.getPendingLeaveRequestCount())
                        .unit("条")
                        .type("pending")
                        .build()
        );

        // 待处理考勤申诉
        cards.add(
                EchartsDataDTO.ApprovalStatisticsDTO.StatisticCard.builder()
                        .title("待处理考勤申诉")
                        .value(originalData.getPendingAttendanceAppealCount())
                        .unit("条")
                        .type("pending")
                        .build()
        );

        // 本月已处理记录
        cards.add(
                EchartsDataDTO.ApprovalStatisticsDTO.StatisticCard.builder()
                        .title("本月已处理记录")
                        .value(originalData.getProcessedRecordCount())
                        .unit("条")
                        .type("processed")
                        .build()
        );

        // 平均审批用时
        cards.add(
                EchartsDataDTO.ApprovalStatisticsDTO.StatisticCard.builder()
                        .title("平均审批用时")
                        .value(originalData.getAverageApprovalTime())
                        .unit("小时")
                        .type("time")
                        .build()
        );

        return EchartsDataDTO.ApprovalStatisticsDTO.builder()
                .statisticCards(cards)
                .build();
    }

    /**
     * 将数字星期几转换为文字表示
     *
     * @param weekday 数字星期几（1-7）
     * @return 文字表示的星期几
     */
    private String weekdayToName(int weekday) {
        switch (weekday) {
            case 1: return "周一";
            case 2: return "周二";
            case 3: return "周三";
            case 4: return "周四";
            case 5: return "周五";
            case 6: return "周六";
            case 7: return "周日";
            default: return "未知";
        }
    }
} 