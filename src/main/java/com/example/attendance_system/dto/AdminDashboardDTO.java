package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 管理员仪表盘数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    
    /**
     * 基础指标统计
     */
    private SummaryDTO summary;
    
    /**
     * 考勤趋势图数据
     */
    private AttendanceTrendDTO attendanceTrend;
    
    /**
     * 考勤状态分布数据
     */
    private StatusDistributionDTO statusDistribution;
    
    /**
     * 部门考勤对比数据
     */
    private DepartmentComparisonDTO departmentComparison;
    
    /**
     * 考勤时间热力图数据
     */
    private TimeHeatmapDTO timeHeatmap;
    
    /**
     * 异常预警数据
     */
    private AbnormalWarningDTO abnormaWlarning;
    
    /**
     * 审批统计数据
     */
    private ApprovalStatisticsDTO approvalStatistics;
    
    /**
     * 基础指标统计DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO {
        /**
         * 今日出勤人数
         */
        private Integer attendanceCount;
        
        /**
         * 今日出勤人数同比变化（百分比）
         */
        private Double attendanceChangeRate;
        
        /**
         * 今日缺勤人数
         */
        private Integer absentCount;
        
        /**
         * 今日缺勤人数同比变化（百分比）
         */
        private Double absentChangeRate;
        
        /**
         * 今日准时率
         */
        private Double onTimeRate;
        
        /**
         * 今日准时率同比变化（百分比）
         */
        private Double onTimeRateChangeRate;
        
        /**
         * 今日迟到人数
         */
        private Integer lateCount;
        
        /**
         * 今日迟到人数同比变化（百分比）
         */
        private Double lateChangeRate;
        
        /**
         * 今日请假人数
         */
        private Integer leaveCount;
        
        /**
         * 今日请假人数同比变化（百分比）
         */
        private Double leaveChangeRate;
        
        /**
         * 今日早退人数
         */
        private Integer earlyLeaveCount;
        
        /**
         * 今日早退人数同比变化（百分比）
         */
        private Double earlyLeaveChangeRate;
        
        /**
         * 打卡异常总数：迟到 + 早退 + 缺勤
         */
        private Integer totalAbnormalCount;
        
        /**
         * 人脸识别失败次数
         */
        private Integer faceRecognitionFailCount;
        
        /**
         * 未打卡人数
         */
        private Integer notCheckInCount;
    }
    
    /**
     * 考勤趋势图数据DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceTrendDTO {
        /**
         * 时间粒度：1-日, 2-周, 3-月
         */
        private Integer timeGranularity;
        
        /**
         * 时间标签列表
         */
        private List<String> timeLabels;
        
        /**
         * 出勤人数趋势
         */
        private List<Integer> attendanceCounts;
        
        /**
         * 迟到人数趋势
         */
        private List<Integer> lateCounts;
        
        /**
         * 请假人数趋势
         */
        private List<Integer> leaveCounts;
        
        /**
         * 缺勤人数趋势
         */
        private List<Integer> absentCounts;
        
        /**
         * 环比增长率
         */
        private List<Double> chainGrowthRates;
        
        /**
         * 异常率变化
         */
        private List<Double> abnormalRateChanges;
    }
    
    /**
     * 考勤状态分布DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistributionDTO {
        /**
         * 状态类型列表
         */
        private List<String> statusTypes;
        
        /**
         * 各状态对应的数量
         */
        private List<Integer> counts;
        
        /**
         * 正常出勤人数
         */
        private Integer normalCount;
        
        /**
         * 迟到人数
         */
        private Integer lateCount;
        
        /**
         * 早退人数
         */
        private Integer earlyLeaveCount;
        
        /**
         * 请假人数
         */
        private Integer leaveCount;
        
        /**
         * 缺勤人数
         */
        private Integer absentCount;
        
        /**
         * 旷工人数
         */
        private Integer absenteeismCount;
        
        /**
         * 调休人数
         */
        private Integer daysOffCount;
        
        /**
         * 加班人数
         */
        private Integer overtimeCount;
        
        /**
         * 迟到超15分钟人数
         */
        private Integer lateOver15MinCount;
    }
    
    /**
     * 部门考勤对比DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentComparisonDTO {
        /**
         * 部门列表
         */
        private List<String> departments;
        
        /**
         * 部门ID列表
         */
        private List<Integer> departmentIds;
        
        /**
         * 各部门的考勤状态分布
         * key: 部门ID
         * value: 包含各状态人数的Map
         */
        private Map<Integer, Map<String, Integer>> departmentStatusMap;
        
        /**
         * 部门考勤率排行
         * key: 部门ID
         * value: 考勤率
         */
        private Map<Integer, Double> attendanceRateRanking;
        
        /**
         * 部门异常率排行
         * key: 部门ID
         * value: 异常率
         */
        private Map<Integer, Double> abnormalRateRanking;
        
        /**
         * 连续异常人数
         * key: 部门ID
         * value: 连续异常人数
         */
        private Map<Integer, Integer> continuousAbnormalCounts;
    }
    
    /**
     * 考勤时间热力图DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeHeatmapDTO {
        /**
         * 星期几列表（1-7，对应周一到周日）
         */
        private List<Integer> weekdays;
        
        /**
         * 时间段列表（小时，如7,8,9,10,...）
         */
        private List<Integer> hours;
        
        /**
         * 热力图数据
         * 三维数组：[星期][小时][打卡数]
         */
        private List<List<Integer>> heatmapData;
    }
    
    /**
     * 异常预警数据DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbnormalWarningDTO {
        /**
         * 高风险员工列表
         */
        private List<HighRiskEmployeeDTO> highRiskEmployees;
        
        /**
         * 异常原因Top3
         */
        private List<AbnormalReasonDTO> topAbnormalReasons;
        
        /**
         * 近7日异常高发时段
         */
        private List<AbnormalTimeSlotDTO> abnormalTimeSlots;
        
        /**
         * 高风险员工DTO
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HighRiskEmployeeDTO {
            /**
             * 员工编号
             */
            private String employeeNo;
            
            /**
             * 员工姓名
             */
            private String employeeName;
            
            /**
             * 部门
             */
            private String department;
            
            /**
             * 连续异常次数
             */
            private Integer continuousAbnormalCount;
            
            /**
             * 异常类型
             */
            private String abnormalType;
        }
        
        /**
         * 异常原因DTO
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AbnormalReasonDTO {
            /**
             * 异常原因
             */
            private String reason;
            
            /**
             * 发生次数
             */
            private Integer count;
            
            /**
             * 占比
             */
            private Double percentage;
        }
        
        /**
         * 异常高发时段DTO
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AbnormalTimeSlotDTO {
            /**
             * 日期
             */
            private String date;
            
            /**
             * 时间段
             */
            private String timeSlot;
            
            /**
             * 异常次数
             */
            private Integer count;
        }
    }
    
    /**
     * 审批统计数据DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalStatisticsDTO {
        /**
         * 待处理请假申请数
         */
        private Integer pendingLeaveRequestCount;
        
        /**
         * 待处理考勤异常申诉数
         */
        private Integer pendingAttendanceAppealCount;
        
        /**
         * 本月已处理记录数
         */
        private Integer processedRecordCount;
        
        /**
         * 平均审批用时（小时）
         */
        private Double averageApprovalTime;
    }
} 