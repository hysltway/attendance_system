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
} 