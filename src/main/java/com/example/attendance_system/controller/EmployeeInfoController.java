package com.example.attendance_system.controller;

import com.example.attendance_system.dto.EmployeeInfoUpdateAuditDTO;
import com.example.attendance_system.dto.EmployeeInfoUpdateDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import com.example.attendance_system.service.EmployeeService;
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
public class EmployeeInfoController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 获取员工个人信息
     * @param employeeNo 员工编号
     * @return 员工信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getEmployeeInfo(@RequestParam String employeeNo) {
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
    @PostMapping("/info/update")
    public ResponseEntity<?> submitInfoUpdateRequest(@RequestBody EmployeeInfoUpdateDTO updateDTO) {
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
    @GetMapping("/info/update/history")
    public ResponseEntity<?> getInfoUpdateHistory(@RequestParam String employeeNo) {
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
    @PostMapping("/info/update/audit")
    public ResponseEntity<?> auditInfoUpdateRequest(@RequestBody EmployeeInfoUpdateAuditDTO auditDTO) {
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