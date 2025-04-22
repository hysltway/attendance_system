package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AdminDashboardDTO;
import com.example.attendance_system.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员仪表盘控制器
 */
@RestController
@CrossOrigin
@Tag(name = "管理员仪表盘", description = "管理员仪表盘数据接口")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * 获取管理员控制台数据接口
     *
     * @param timeGranularity 时间粒度：1-日, 2-周, 3-月，默认为1
     * @return 管理员仪表盘数据
     */
    @Operation(summary = "获取管理员控制台数据", description = "为管理员首页仪表盘提供一站式考勤概览数据中心，用于掌握员工出勤表现、异常分布、部门差异及时间趋势")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard")
    @Deprecated
    public ResponseEntity<?> getDashboardData(
            @Parameter(description = "时间粒度：1-日, 2-周, 3-月，默认为1")
            @RequestParam(required = false) Integer timeGranularity) {
        try {
            AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData(timeGranularity);
            return ResponseEntity.ok(dashboardData);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取仪表盘数据失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 1. 获取综合概览数据（顶部统计）
     * 
     * @return 今日出勤总览、迟到、缺勤、请假、早退、打卡异常等核心指标
     */
    @Operation(summary = "获取综合概览数据", description = "提供今日出勤总览、迟到、缺勤、请假、早退、打卡异常等核心指标")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/summary")
    public ResponseEntity<?> getDashboardSummary() {
        try {
            AdminDashboardDTO.SummaryDTO summaryData = adminDashboardService.getDashboardSummary();
            return ResponseEntity.ok(summaryData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取综合概览数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 2. 获取出勤趋势图数据
     * 
     * @param timeRange 时间范围：week / month (默认week)
     * @return 近7日或近30日的出勤、迟到、请假、缺勤人数变化趋势
     */
    @Operation(summary = "获取出勤趋势图数据", description = "提供近7日或近30日的出勤、迟到、请假、缺勤人数变化趋势")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/trend")
    public ResponseEntity<?> getAttendanceTrend(
            @Parameter(description = "时间范围：week(周) / month(月)，默认week")
            @RequestParam(required = false, defaultValue = "week") String timeRange) {
        try {
            // 将timeRange转换为系统使用的时间粒度
            Integer timeGranularity = "month".equalsIgnoreCase(timeRange) ? 3 : 1;
            AdminDashboardDTO.AttendanceTrendDTO trendData = adminDashboardService.getAttendanceTrend(timeGranularity);
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
     * @return 今日各类考勤状态所占比例（正常、迟到、请假、缺勤等）
     */
    @Operation(summary = "获取考勤状态分布图数据", description = "统计今日各类考勤状态所占比例（正常、迟到、请假、缺勤等）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/status-distribution")
    public ResponseEntity<?> getStatusDistribution() {
        try {
            AdminDashboardDTO.StatusDistributionDTO distributionData = adminDashboardService.getStatusDistribution();
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
     * @return 各部门每日出勤情况对比
     */
    @Operation(summary = "获取部门对比图数据", description = "各部门每日出勤情况对比，展示正常、异常人数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/department-comparison")
    public ResponseEntity<?> getDepartmentComparison() {
        try {
            AdminDashboardDTO.DepartmentComparisonDTO comparisonData = adminDashboardService.getDepartmentComparison();
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
     * @return 一周内每个时间段的打卡密度
     */
    @Operation(summary = "获取打卡热力图数据", description = "统计一周内每个时间段的打卡密度")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/heatmap")
    public ResponseEntity<?> getTimeHeatmap() {
        try {
            AdminDashboardDTO.TimeHeatmapDTO heatmapData = adminDashboardService.getTimeHeatmap();
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
     * @return 异常预警数据，包括连续异常员工、异常类型TOP3等
     */
    @Operation(summary = "获取异常预警数据", description = "识别连续异常员工、异常高发部门")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/warning")
    public ResponseEntity<?> getAbnormalWarning() {
        try {
            AdminDashboardDTO.AbnormalWarningDTO warningData = adminDashboardService.getAbnormalWarning();
            return ResponseEntity.ok(warningData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取异常预警数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 7. 获取平均工时统计数据
     * 
     * @return 本日/本周/本月的人均出勤时长、加班时长、请假时长
     */
    @Operation(summary = "获取平均工时统计数据", description = "统计本日/本周/本月的人均出勤时长、加班时长、请假时长")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/admin/dashboard/working-hours")
    public ResponseEntity<?> getWorkingHours() {
        try {
            Map<String, Object> workingHoursData = adminDashboardService.getWorkingHours();
            return ResponseEntity.ok(workingHoursData);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取平均工时统计数据失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 