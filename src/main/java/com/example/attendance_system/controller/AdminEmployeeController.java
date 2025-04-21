package com.example.attendance_system.controller;

import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员员工管理控制器
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
@Tag(name = "管理员员工管理", description = "管理员管理员工相关接口，包括查询、编辑、删除、创建等操作")
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 获取员工列表（分页 + 条件搜索）
     *
     * @param current    当前页码（从1开始）
     * @param size       每页显示条数
     * @param name       员工姓名（模糊匹配）
     * @param department 部门名称（精确或模糊匹配）
     * @return 员工列表分页结果
     */
    @Operation(summary = "获取员工列表", description = "管理员在后台查看所有员工的完整信息列表，包括基本资料、部门、职位、考勤状态等，支持分页查询和条件筛选")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/list")
    public ResponseEntity<?> getEmployeeList(
            @Parameter(description = "当前页码，从1开始计数")
            @RequestParam(defaultValue = "1") Integer current,

            @Parameter(description = "每页记录数")
            @RequestParam(defaultValue = "10") Integer size,

            @Parameter(description = "员工姓名（模糊匹配）")
            @RequestParam(required = false) String name,

            @Parameter(description = "部门名称（精确或模糊匹配）")
            @RequestParam(required = false) String department) {

        try {
            // 参数校验
            if (current < 1) {
                throw new IllegalArgumentException("当前页码必须大于等于1");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("每页记录数必须在1-100之间");
            }

            // 调用服务查询员工列表
            Page<Employee> employeePage = employeeService.getEmployeeListPage(current, size, name, department);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", employeePage.getTotalElements());
            response.put("pages", employeePage.getTotalPages());
            response.put("current", current);
            response.put("records", employeePage.getContent());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取员工列表失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 管理员编辑员工信息
     *
     * @param employee 员工信息
     * @return 更新结果
     */
    @Operation(summary = "编辑员工信息", description = "管理员修改员工基本信息，包括姓名、手机号、邮箱、职位、部门等")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/employee/edit")
    public ResponseEntity<?> editEmployee(@RequestBody Employee employee) {
        try {
            // 参数校验
            if (employee.getEmployeeNo() == null || employee.getEmployeeNo().isEmpty()) {
                throw new IllegalArgumentException("员工编号不能为空");
            }

            // 调用服务更新员工信息
            Employee updatedEmployee = employeeService.updateEmployee(employee);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新员工信息成功");
            response.put("employee", updatedEmployee);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新员工信息失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 管理员删除员工
     *
     * @param employeeId 员工ID
     * @return 删除结果
     */
    @Operation(summary = "删除员工", description = "管理员删除员工记录（逻辑删除），设置员工状态为离职")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @DeleteMapping("/employee/delete")
    public ResponseEntity<?> deleteEmployee(
            @Parameter(description = "员工ID", required = true)
            @RequestParam String employeeId) {
        try {
            // 参数校验
            if (employeeId == null || employeeId.isEmpty()) {
                throw new IllegalArgumentException("员工ID不能为空");
            }

            // 调用服务删除员工
            boolean result = employeeService.deleteEmployee(employeeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);

            if (result) {
                response.put("message", "员工删除成功");
            } else {
                response.put("message", "员工删除失败");
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除员工失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 管理员手动添加员工
     *
     * @param employee 员工信息
     * @return 创建结果
     */
    @Operation(summary = "手动添加员工", description = "管理员手动创建新员工账号，录入基本信息")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/employee/create")
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        try {
            // 参数校验
            if (employee.getName() == null || employee.getName().isEmpty()) {
                throw new IllegalArgumentException("员工姓名不能为空");
            }

            // 调用服务创建员工
            Employee createdEmployee = employeeService.createEmployee(employee);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "创建员工成功");
            response.put("employeeNo", createdEmployee.getEmployeeNo());
            response.put("employeeId", createdEmployee.getId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建员工失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
} 