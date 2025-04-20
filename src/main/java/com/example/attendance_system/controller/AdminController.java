package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AdminAttendanceExceptionUpdateDTO;
import com.example.attendance_system.dto.AttendanceExceptionPageDTO;
import com.example.attendance_system.dto.LeaveRecordApprovalDTO;
import com.example.attendance_system.dto.LeaveRecordDTO;
import com.example.attendance_system.dto.LeaveRecordPageDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.entity.LeaveRecord;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    @Autowired
    private LeaveService leaveService;

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

    /**
     * 管理员获取所有员工请假申请记录接口
     * @param current 当前页码
     * @param size 每页记录数
     * @param status 状态筛选（可选）
     * @param employeeNo 员工编号（可选）
     * @param sortBy 排序字段（可选）
     * @param sortOrder 排序方式（可选）
     * @return 请假记录分页结果
     */
    @Operation(summary = "获取所有员工请假申请记录", description = "管理员查看全体员工提交的请假申请，支持按状态筛选、员工筛选，以及分页、排序等操作")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/leave/requests")
    public ResponseEntity<?> getAllLeaveRequests(
            @Parameter(description = "当前页码，从1开始计数")
            @RequestParam(defaultValue = "1") Integer current,
            
            @Parameter(description = "每页记录数")
            @RequestParam(defaultValue = "10") Integer size,
            
            @Parameter(description = "状态筛选：0-待审批，1-已批准，2-已拒绝")
            @RequestParam(required = false) Integer status,
            
            @Parameter(description = "员工编号筛选")
            @RequestParam(required = false) String employeeNo,
            
            @Parameter(description = "排序字段：createdTime-创建时间，startDate-开始日期")
            @RequestParam(defaultValue = "createdTime") String sortBy,
            
            @Parameter(description = "排序方式：asc-升序，desc-降序")
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            // 参数校验
            if (current < 1) {
                throw new IllegalArgumentException("当前页码必须大于等于1");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("每页记录数必须在1-100之间");
            }
            
            // 创建排序对象
            Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
            // 创建分页对象
            Pageable pageable = PageRequest.of(current - 1, size, sort);
            
            // 获取请假记录
            Page<LeaveRecord> leavePage;
            if (status != null && employeeNo != null) {
                leavePage = leaveService.getLeaveRecordsByEmployeeNoAndStatus(employeeNo, status, pageable);
            } else if (status != null) {
                leavePage = leaveService.getLeaveRecordsByStatus(status, pageable);
            } else if (employeeNo != null) {
                leavePage = leaveService.getLeaveRecordsByEmployeeNo(employeeNo, pageable);
            } else {
                leavePage = leaveService.getAllLeaveRecords(pageable);
            }
            
            // 转换为DTO对象
            List<LeaveRecordDTO> records = leavePage.getContent().stream()
                    .map(this::convertToLeaveRecordDTO)
                    .collect(Collectors.toList());
            
            // 构建分页结果
            LeaveRecordPageDTO resultPage = new LeaveRecordPageDTO();
            resultPage.setCurrent(current);
            resultPage.setSize(size);
            resultPage.setTotal(leavePage.getTotalElements());
            resultPage.setPages(leavePage.getTotalPages());
            resultPage.setRecords(records);
            
            return ResponseEntity.ok(resultPage);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询请假记录失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 管理员审批请假申请接口
     * @param approvalDTO 审批信息
     * @return 审批结果
     */
    @Operation(summary = "审批请假申请", description = "管理员对某条请假申请记录进行审批操作，包括批准或拒绝")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "审批成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/leave/review")
    public ResponseEntity<?> reviewLeaveApplication(@RequestBody LeaveRecordApprovalDTO approvalDTO) {
        try {
            // 参数校验
            if (approvalDTO.getId() == null) {
                throw new IllegalArgumentException("请假记录ID不能为空");
            }
            if (approvalDTO.getApproverNo() == null || approvalDTO.getApproverNo().isEmpty()) {
                throw new IllegalArgumentException("审批人员工编号不能为空");
            }
            if (approvalDTO.getStatus() == null) {
                throw new IllegalArgumentException("审批状态不能为空");
            }
            if (approvalDTO.getStatus() != 1 && approvalDTO.getStatus() != 2) {
                throw new IllegalArgumentException("审批状态无效，只能是1（批准）或2（拒绝）");
            }
            
            // 调用服务审批请假申请
            LeaveRecord record = leaveService.approveLeaveApplication(
                    approvalDTO.getId(),
                    approvalDTO.getApproverNo(),
                    approvalDTO.getStatus(),
                    approvalDTO.getApprovalRemark());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "请假申请审批成功");
            response.put("leaveId", record.getId());
            response.put("status", record.getStatus());
            response.put("statusDesc", getLeaveStatusDesc(record.getStatus()));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "审批请假申请失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 将LeaveRecord实体转换为LeaveRecordDTO
     * @param record 请假记录实体
     * @return 请假记录DTO
     */
    private LeaveRecordDTO convertToLeaveRecordDTO(LeaveRecord record) {
        LeaveRecordDTO dto = new LeaveRecordDTO();
        dto.setId(record.getId());
        dto.setEmployeeNo(record.getEmployeeNo());
        dto.setLeaveType(record.getLeaveType());
        dto.setLeaveTypeDesc(getLeaveTypeDesc(record.getLeaveType()));
        dto.setStartDate(record.getStartDate());
        dto.setEndDate(record.getEndDate());
        dto.setReason(record.getReason());
        dto.setStatus(record.getStatus());
        dto.setStatusDesc(getLeaveStatusDesc(record.getStatus()));
        dto.setApproverNo(record.getApproverNo());
        dto.setApprovalTime(record.getApprovalTime());
        dto.setApprovalRemark(record.getApprovalRemark());
        dto.setCreatedTime(record.getCreatedTime());
        return dto;
    }
    
    /**
     * 获取请假类型描述
     * @param leaveType 请假类型编号
     * @return 请假类型描述
     */
    private String getLeaveTypeDesc(Integer leaveType) {
        switch (leaveType) {
            case 1:
                return "事假";
            case 2:
                return "病假";
            case 3:
                return "年假";
            case 4:
                return "婚假";
            case 5:
                return "产假";
            case 6:
                return "丧假";
            case 7:
                return "其他";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取请假状态描述
     * @param status 请假状态编号
     * @return 请假状态描述
     */
    private String getLeaveStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待审批";
            case 1:
                return "已批准";
            case 2:
                return "已拒绝";
            default:
                return "未知";
        }
    }
} 