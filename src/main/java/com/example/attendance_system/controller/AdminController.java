package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AdminAttendanceExceptionUpdateDTO;
import com.example.attendance_system.dto.AttendanceExceptionPageDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
@Tag(name = "管理员功能", description = "管理员专用接口，处理考勤异常申诉等")
public class AdminController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 获取所有待处理的异常申诉记录
     * @param current 当前页码
     * @param size 每页记录数
     * @return 分页查询结果
     */
    @Operation(summary = "获取所有待处理的异常申诉记录", description = "管理员获取所有提交了申诉但尚未处理的异常考勤记录，支持分页浏览")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = AttendanceExceptionPageDTO.class))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/exception/appeals")
    public ResponseEntity<?> getAllExceptionAppeals(
            @Parameter(description = "当前页码，从1开始计数")
            @RequestParam(required = false) Integer current,
            @Parameter(description = "每页记录数")
            @RequestParam(required = false) Integer size) {
        try {
            AttendanceExceptionPageDTO result = attendanceService.getAllExceptionAppeals(current, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取异常申诉记录失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 管理员修改异常记录
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    @Operation(summary = "修改异常考勤记录", description = "管理员可以修改某条异常考勤记录的状态、类型等内容，表示该记录已经审核或更新处理")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class)))
    })
    @PutMapping("/exception/update")
    public ResponseEntity<?> updateExceptionRecord(
            @Parameter(description = "异常考勤记录更新信息", required = true)
            @RequestBody AdminAttendanceExceptionUpdateDTO updateDTO) {
        try {
            AttendanceRecord record = attendanceService.updateExceptionRecord(updateDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新异常考勤记录成功，该申诉已标记为已处理");
            response.put("recordId", record.getId());
            response.put("status", record.getStatus());
            response.put("statusDesc", getCheckTypeDesc(record.getStatus()));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新异常考勤记录失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取考勤类型描述
     * @param checkType 考勤类型编号
     * @return 考勤类型描述
     */
    private String getCheckTypeDesc(Integer checkType) {
        switch (checkType) {
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
} 