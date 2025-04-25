package com.example.attendance_system.service;

import com.example.attendance_system.dto.AdminDashboardDTO;

import java.util.Map;

/**
 * 管理员仪表盘服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取管理员仪表盘数据
     *
     * @param timeGranularity 时间粒度：1-日, 2-周, 3-月，默认为1
     * @return 管理员仪表盘数据
     * @deprecated 将被拆分为更小粒度的接口替代
     */
    @Deprecated
    AdminDashboardDTO getDashboardData(Integer timeGranularity);
    
    /**
     * 1. 获取综合概览数据（顶部统计）
     *
     * @return 今日出勤总览、迟到、缺勤、请假、早退、打卡异常等核心指标
     */
    AdminDashboardDTO.SummaryDTO getDashboardSummary();
    
    /**
     * 2. 获取出勤趋势图数据
     *
     * @param timeGranularity 时间粒度：1-日, 3-月
     * @return 近7日或近30日的出勤、迟到、请假、缺勤人数变化趋势
     */
    AdminDashboardDTO.AttendanceTrendDTO getAttendanceTrend(Integer timeGranularity);
    
    /**
     * 3. 获取考勤状态分布图数据
     *
     * @return 今日各类考勤状态所占比例（正常、迟到、请假、缺勤等）
     */
    AdminDashboardDTO.StatusDistributionDTO getStatusDistribution();
    
    /**
     * 4. 获取部门对比图数据
     *
     * @return 各部门每日出勤情况对比
     */
    AdminDashboardDTO.DepartmentComparisonDTO getDepartmentComparison();
    
    /**
     * 5. 获取打卡热力图数据
     *
     * @return 一周内每个时间段的打卡密度
     */
    AdminDashboardDTO.TimeHeatmapDTO getTimeHeatmap();
    
    /**
     * 6. 获取异常预警数据
     *
     * @return 异常预警数据，包括连续异常员工、异常类型TOP3等
     */
    AdminDashboardDTO.AbnormalWarningDTO getAbnormalWarning();
    
    /**
     * 7. 获取平均工时统计数据
     *
     * @return 本日/本周/本月的人均出勤时长、加班时长、请假时长
     */
    Map<String, Object> getWorkingHours();
    
    /**
     * 8. 获取审批统计数据
     *
     * @return 审批统计数据，包括待处理请假申请、考勤异常申诉、平均审批用时等
     */
    AdminDashboardDTO.ApprovalStatisticsDTO getApprovalStatistics();
} 