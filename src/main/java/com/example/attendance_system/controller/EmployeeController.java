package com.example.attendance_system.controller;

import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工控制器
 */
@RestController
@RequestMapping
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工注册接口
     * @param registrationDTO 员工注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerEmployee(@RequestBody EmployeeRegistrationDTO registrationDTO) {
        try {
            Employee employee = employeeService.registerEmployee(registrationDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "员工注册成功");
            response.put("employeeNo", employee.getEmployeeNo());
            response.put("employeeId", employee.getId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "员工注册失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 