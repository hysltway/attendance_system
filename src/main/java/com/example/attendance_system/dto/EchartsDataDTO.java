package com.example.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ECharts数据格式的DTO类
 * 用于统一数据大屏返回格式
 */
public class EchartsDataDTO {

    /**
     * 通用ECharts数据系列
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesData {
        private String name;
        private Object value;
        private Object itemStyle;
        private Object emphasis;
        private Object label;
    }

    /**
     * 基础统计指标数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO {
        private List<StatisticCard> statisticCards;

        /**
         * 统计卡片数据
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StatisticCard {
            private String title;
            private Object value;
            private Double changeRate;
            private String unit;
            private String type; // 类型：attendance, absent, late, leave, earlyLeave, abnormal
        }
    }

    /**
     * 考勤趋势图数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDTO {
        private List<String> xAxisData; // 时间标签
        private List<Series> series;
        private Integer timeGranularity; // 时间粒度，1-日，3-月

        /**
         * 数据系列
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Series {
            private String name; // 系列名称，如"出勤人数"、"迟到人数"
            private String type; // 图表类型，如"line", "bar"
            private List<Integer> data; // 数据值
            private String stack; // 堆叠组
            private Object itemStyle; // 样式
            private Boolean smooth; // 是否平滑曲线
        }
    }

    /**
     * 考勤状态分布数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieChartDTO {
        private List<SeriesData> data;
        private String title; // 图表标题
        private List<String> legend; // 图例数据
    }

    /**
     * 部门考勤对比数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentComparisonDTO {
        private List<String> xAxisData; // 部门列表
        private List<Series> series;
        private List<Ranking> rankings; // 部门排名

        /**
         * 数据系列
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Series {
            private String name; // 系列名称，如"正常"、"迟到"
            private String type; // 图表类型，如"bar"
            private String stack; // 堆叠组
            private List<Integer> data; // 数据值
            private Object itemStyle; // 样式
        }

        /**
         * 排名数据
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Ranking {
            private String title; // 排名标题，如"考勤率"、"异常率"
            private List<RankItem> items;

            /**
             * 排名项
             */
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class RankItem {
                private String department; // 部门名称
                private Double value; // 排名值
                private Integer rank; // 排名
            }
        }
    }

    /**
     * 热力图数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapDTO {
        private List<String> xAxis; // 横轴标签（小时）
        private List<String> yAxis; // 纵轴标签（星期几）
        private List<List<Object>> data; // [[x, y, value], ...]格式的数据
        private List<List<Integer>> rawData; // 原始二维数组数据
    }

    /**
     * 异常预警数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningDTO {
        private List<HighRiskEmployee> highRiskEmployees; // 高风险员工
        private List<SeriesData> abnormalReasons; // 异常原因Top3
        private List<TimeSlot> abnormalTimeSlots; // 异常高发时段

        /**
         * 高风险员工
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HighRiskEmployee {
            private String employeeNo;
            private String employeeName;
            private String department;
            private Integer continuousCount;
            private String mainAbnormalType;
            private Double riskLevel; // 风险等级，可用于前端展示不同颜色
        }

        /**
         * 异常高发时段
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TimeSlot {
            private String date;
            private String timeSlot;
            private Integer count;
        }
    }

    /**
     * 审批统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalStatisticsDTO {
        private List<StatisticCard> statisticCards;

        /**
         * 统计卡片数据
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StatisticCard {
            private String title;
            private Object value;
            private String unit;
            private String type; // 类型：pending, processed, time
        }
    }
} 