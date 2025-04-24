package com.example.attendance_system.controller;

import com.example.attendance_system.entity.Department;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.service.DepartmentService;
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
 * 管理员部门管理控制器
 */
@RestController
@RequestMapping("/admin/department")
@CrossOrigin
@Tag(name = "部门管理", description = "管理员管理部门相关接口，包括查询、新增、编辑、删除等操作")
public class AdminDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * 分页查询部门列表
     *
     * @param current 当前页码（从1开始）
     * @param size    每页条数
     * @param name    部门名称（模糊匹配，可选）
     * @return 部门列表分页结果
     */
    @Operation(summary = "分页查询部门列表", description = "管理员查看所有部门信息，支持分页查询和名称模糊搜索")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/list")
    public ResponseEntity<?> getDepartmentList(
            @Parameter(description = "当前页码，从1开始计数")
            @RequestParam(defaultValue = "1") Integer current,

            @Parameter(description = "每页条数")
            @RequestParam(defaultValue = "10") Integer size,

            @Parameter(description = "部门名称（支持模糊匹配）")
            @RequestParam(required = false) String name) {

        try {
            // 参数校验
            if (current < 1) {
                throw new IllegalArgumentException("当前页码必须大于等于1");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("每页记录数必须在1-100之间");
            }

            // 调用服务查询部门列表
            Page<Department> departmentPage = departmentService.getDepartmentListPage(current, size, name);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", departmentPage.getTotalElements());
            response.put("pages", departmentPage.getTotalPages());
            response.put("current", current);
            response.put("records", departmentPage.getContent());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取部门列表失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 新增部门
     *
     * @param department 部门信息
     * @return 创建结果
     */
    @Operation(summary = "新增部门", description = "管理员手动添加新部门，设置部门名称、描述等信息")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        try {
            // 参数校验
            if (department.getName() == null || department.getName().isEmpty()) {
                throw new IllegalArgumentException("部门名称不能为空");
            }

            // 清除parentId
            department.setParentId(null);

            // 调用服务创建部门
            Department createdDepartment = departmentService.createDepartment(department);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "创建部门成功");
            response.put("department", createdDepartment);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建部门失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 编辑部门信息
     *
     * @param department 部门信息
     * @return 更新结果
     */
    @Operation(summary = "编辑部门信息", description = "管理员修改部门基本信息，如名称、状态等")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateDepartment(@RequestBody Department department) {
        try {
            // 参数校验
            if (department.getId() == null) {
                throw new IllegalArgumentException("部门ID不能为空");
            }
            if (department.getName() == null || department.getName().isEmpty()) {
                throw new IllegalArgumentException("部门名称不能为空");
            }

            // 清除parentId
            department.setParentId(null);

            // 调用服务更新部门信息
            Department updatedDepartment = departmentService.updateDepartment(department);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新部门信息成功");
            response.put("department", updatedDepartment);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新部门信息失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 删除结果
     */
    @Operation(summary = "删除部门", description = "管理员删除指定部门，仅支持删除无员工的空部门")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteDepartment(
            @Parameter(description = "部门ID", required = true)
            @RequestParam String id) {
        try {
            // 参数校验
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("部门ID不能为空");
            }

            // 调用服务删除部门
            boolean result = departmentService.deleteDepartment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);

            if (result) {
                response.put("message", "部门删除成功");
            } else {
                response.put("message", "部门删除失败");
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
            response.put("message", "删除部门失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查询指定部门的员工详情列表
     *
     * @param departmentId 部门ID
     * @param current      当前页码
     * @param size         每页条数
     * @return 员工列表分页结果
     */
    @Operation(summary = "查询指定部门的员工详情列表", description = "管理员查看某一部门下的所有员工信息，支持分页展示")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/employees")
    public ResponseEntity<?> getDepartmentEmployees(
            @Parameter(description = "目标部门的唯一ID", required = true)
            @RequestParam String departmentId,

            @Parameter(description = "当前页码，从1开始计数", required = true)
            @RequestParam(defaultValue = "1") Integer current,

            @Parameter(description = "每页条数", required = true)
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            // 参数校验
            if (departmentId == null || departmentId.isEmpty()) {
                throw new IllegalArgumentException("部门ID不能为空");
            }
            if (current < 1) {
                throw new IllegalArgumentException("当前页码必须大于等于1");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("每页记录数必须在1-100之间");
            }

            // 检查部门是否存在
            Long deptId = Long.parseLong(departmentId);
            if (!departmentService.existsById(deptId)) {
                throw new IllegalArgumentException("部门不存在");
            }

            // 调用服务查询部门员工列表
            Page<Employee> employeePage = employeeService.getEmployeesByDepartmentId(deptId, current, size);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", employeePage.getTotalElements());
            response.put("pages", employeePage.getTotalPages());
            response.put("current", current);
            response.put("departmentId", departmentId);

            // 获取部门信息
            Department department = departmentService.getDepartmentById(deptId);
            response.put("departmentName", department.getName());

            response.put("records", employeePage.getContent());

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "部门ID格式不正确");

            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取部门员工列表失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
} 