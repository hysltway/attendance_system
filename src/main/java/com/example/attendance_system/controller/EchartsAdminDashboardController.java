package com.example.attendance_system.controller;

import com.example.attendance_system.dto.EchartsDataDTO;
import com.example.attendance_system.service.impl.EchartsAdminDashboardServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ECharts格式的管理员仪表盘控制器
 */
@RestController
@CrossOrigin
@Tag(name = "ECharts仪表盘接口", description = "优化后的ECharts格式数据大屏接口")
@RequestMapping("/admin/dashboard/echarts")
public class EchartsAdminDashboardController {

    @Autowired
    private EchartsAdminDashboardServiceImpl echartsAdminDashboardService;

    /**
     * 1. 获取基础指标统计数据
     *
     * @return ECharts格式的基础指标统计数据
     */
    @Operation(summary = "获取基础指标统计数据", description = "统一格式返回今日出勤总览、迟到、缺勤、请假、早退等核心指标")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/summary")
    public ResponseEntity<?> getEchartsSummary() {
        try {
            EchartsDataDTO.SummaryDTO summaryData = echartsAdminDashboardService.getFormattedSummary();
            return ResponseEntity.ok(summaryData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取基础指标统计数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 2. 获取出勤趋势图数据
     *
     * @param timeRange 时间范围：week / month (默认week)
     * @return ECharts格式的出勤趋势图数据
     */
    @Operation(summary = "获取出勤趋势图数据", description = "ECharts格式的近7日或近30日出勤、迟到、请假、缺勤人数变化趋势")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/trend")
    public ResponseEntity<?> getEchartsTrend(
            @Parameter(description = "时间范围：week(周) / month(月)，默认week")
            @RequestParam(required = false, defaultValue = "week") String timeRange) {
        try {
            // 将timeRange转换为系统使用的时间粒度
            Integer timeGranularity = "month".equalsIgnoreCase(timeRange) ? 3 : 1;
            EchartsDataDTO.TrendDTO trendData = echartsAdminDashboardService.getFormattedAttendanceTrend(timeGranularity);
            return ResponseEntity.ok(trendData);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取出勤趋势图数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 3. 获取考勤状态分布图数据
     *
     * @return ECharts格式的考勤状态分布数据
     */
    @Operation(summary = "获取考勤状态分布图数据", description = "ECharts格式的今日各类考勤状态所占比例数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/status-distribution")
    public ResponseEntity<?> getEchartsStatusDistribution() {
        try {
            EchartsDataDTO.PieChartDTO distributionData = echartsAdminDashboardService.getFormattedStatusDistribution();
            return ResponseEntity.ok(distributionData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取考勤状态分布图数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 4. 获取部门对比图数据
     *
     * @return ECharts格式的部门考勤对比数据
     */
    @Operation(summary = "获取部门对比图数据", description = "ECharts格式的各部门每日出勤情况对比")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/department-comparison")
    public ResponseEntity<?> getEchartsDepartmentComparison() {
        try {
            EchartsDataDTO.DepartmentComparisonDTO comparisonData = echartsAdminDashboardService.getFormattedDepartmentComparison();
            return ResponseEntity.ok(comparisonData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取部门对比图数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 5. 获取打卡热力图数据
     *
     * @return ECharts格式的打卡热力图数据
     */
    @Operation(summary = "获取打卡热力图数据", description = "ECharts格式的一周内每个时间段的打卡密度")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/heatmap")
    public ResponseEntity<?> getEchartsTimeHeatmap() {
        try {
            EchartsDataDTO.HeatmapDTO heatmapData = echartsAdminDashboardService.getFormattedTimeHeatmap();
            return ResponseEntity.ok(heatmapData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取打卡热力图数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 6. 获取异常预警数据
     *
     * @return ECharts格式的异常预警数据
     */
    @Operation(summary = "获取异常预警数据", description = "ECharts格式的异常预警数据，包括高风险员工、异常原因等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/warning")
    public ResponseEntity<?> getEchartsAbnormalWarning() {
        try {
            EchartsDataDTO.WarningDTO warningData = echartsAdminDashboardService.getFormattedAbnormalWarning();
            return ResponseEntity.ok(warningData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取异常预警数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 7. 获取审批统计数据
     *
     * @return ECharts格式的审批统计数据
     */
    @Operation(summary = "获取审批统计数据", description = "ECharts格式的审批统计数据，包括待处理申请、平均审批时长等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/approval-statistics")
    public ResponseEntity<?> getEchartsApprovalStatistics() {
        try {
            EchartsDataDTO.ApprovalStatisticsDTO approvalData = echartsAdminDashboardService.getFormattedApprovalStatistics();
            return ResponseEntity.ok(approvalData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取审批统计数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 