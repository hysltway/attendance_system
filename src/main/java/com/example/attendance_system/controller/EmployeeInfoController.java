package com.example.attendance_system.controller;

import com.example.attendance_system.dto.EmployeeInfoUpdateAuditDTO;
import com.example.attendance_system.dto.EmployeeInfoUpdateDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import com.example.attendance_system.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工信息控制器
 */
@RestController
@RequestMapping("/employee")
@CrossOrigin
@Tag(name = "员工信息管理", description = "员工个人信息管理相关接口")
public class EmployeeInfoController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 获取员工个人信息
     * @param employeeNo 员工编号
     * @return 员工信息
     */
    @Operation(summary = "获取员工个人信息", description = "根据员工编号获取员工详细个人信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Employee.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/info")
    public ResponseEntity<?> getEmployeeInfo(
            @Parameter(description = "员工编号", required = true)
            @RequestParam String employeeNo) {
        try {
            Employee employee = employeeService.getEmployeeInfo(employeeNo);
            
            return ResponseEntity.ok(employee);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取员工信息失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 提交员工信息更新请求
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    @Operation(summary = "提交信息更新请求", description = "员工提交个人信息更新申请，仅能修改姓名(name)、手机号(phoneNumber)和邮箱(email)，等待管理员审核")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "申请提交成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping("/info/update")
    public ResponseEntity<?> submitInfoUpdateRequest(
            @Parameter(description = "员工信息更新内容，仅支持修改姓名、手机号和邮箱", required = true)
            @RequestBody EmployeeInfoUpdateDTO updateDTO) {
        try {
            EmployeeInfoUpdateRequest request = employeeService.submitInfoUpdateRequest(updateDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "个人信息更新请求已提交，请等待管理员审核");
            response.put("requestId", request.getId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提交信息更新请求失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 查询员工信息更新请求记录
     * @param employeeNo 员工编号
     * @return 请求记录列表
     */
    @Operation(summary = "查询信息更新历史", description = "查询员工个人信息更新申请历史记录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeInfoUpdateRequest.class)))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/info/update/history")
    public ResponseEntity<?> getInfoUpdateHistory(
            @Parameter(description = "员工编号", required = true)
            @RequestParam String employeeNo) {
        try {
            List<EmployeeInfoUpdateRequest> requests = employeeService.getInfoUpdateRequests(employeeNo);
            
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询信息更新记录失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 查询所有待审核的员工信息更新请求（管理员接口）
     * @return 待审核请求列表
     */
    @Operation(summary = "查询待审核请求", description = "管理员查询所有待审核的员工信息更新申请")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeInfoUpdateRequest.class)))),
            @ApiResponse(responseCode = "403", description = "权限不足", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/info/update/pending")
    public ResponseEntity<?> getPendingInfoUpdateRequests() {
        try {
            List<EmployeeInfoUpdateRequest> requests = employeeService.getPendingInfoUpdateRequests();
            
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询待审核请求失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 审核员工信息更新请求（管理员接口）
     * @param auditDTO 审核信息
     * @return 审核结果
     */
    @Operation(summary = "审核信息更新请求", description = "管理员审核员工信息更新申请，通过或拒绝")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "审核成功", 
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
    @PostMapping("/info/update/audit")
    public ResponseEntity<?> auditInfoUpdateRequest(
            @Parameter(description = "审核信息，包含请求ID、审核结果和备注", required = true)
            @RequestBody EmployeeInfoUpdateAuditDTO auditDTO) {
        try {
            EmployeeInfoUpdateRequest request = employeeService.auditInfoUpdateRequest(auditDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", auditDTO.getStatus() == 1 ? "审核通过成功" : "审核拒绝成功");
            response.put("requestId", request.getId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "审核请求失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 