package com.example.attendance_system.controller;

import com.example.attendance_system.dto.*;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.FaceFeature;
import com.example.attendance_system.entity.LeaveRecord;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.EmployeeService;
import com.example.attendance_system.service.FaceFeatureService;
import com.example.attendance_system.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工控制器
 */
@RestController
@RequestMapping
@CrossOrigin
@Tag(name = "员工管理", description = "员工注册和登录相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private FaceFeatureService faceFeatureService;

    @Autowired
    private LeaveService leaveService;

    /**
     * 员工注册接口
     *
     * @param registrationDTO 员工注册信息
     * @return 注册结果
     */
    @Operation(summary = "员工注册", description = "注册新员工账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
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

    /**
     * 员工登录接口
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @Operation(summary = "员工登录", description = "员工账号登录系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            Employee employee = employeeService.login(loginDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("employeeNo", employee.getEmployeeNo());
            response.put("employeeId", employee.getId());
            response.put("name", employee.getName());
            response.put("isAdmin", employee.getIsAdmin());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 异常考勤信息分页查询接口
     *
     * @param employeeNo 员工编号
     * @param current    当前页码
     * @param size       每页记录数
     * @return 异常考勤信息分页结果
     */
    @Operation(summary = "异常考勤信息分页查询", description = "分页查询员工的异常考勤记录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/employee/exception")
    public ResponseEntity<?> getExceptionRecords(
            @Parameter(description = "员工编号", required = true)
            @RequestParam String employeeNo,

            @Parameter(description = "当前页码（从1开始）", required = true)
            @RequestParam Integer current,

            @Parameter(description = "每页记录数", required = true)
            @RequestParam Integer size) {
        try {
            // 参数校验
            if (current < 1) {
                throw new IllegalArgumentException("当前页码必须大于等于1");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("每页记录数必须在1-100之间");
            }

            // 调用服务查询异常考勤记录
            AttendanceExceptionPageDTO result = attendanceService.getExceptionRecords(employeeNo, current, size);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询异常考勤记录失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 提交异常考勤申诉
     *
     * @param appealDTO 申诉信息，包含员工编号、异常记录ID和申诉理由
     * @return 申诉结果，包含处理状态和说明信息
     */
    @Operation(summary = "提交异常考勤申诉", description = "员工对异常考勤记录（迟到、早退、旷工等）提交申诉，说明情况并提交给管理员审核")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "申诉提交成功，等待管理员审核"),
            @ApiResponse(responseCode = "400", description = "请求参数错误，如员工编号不存在或申诉说明为空"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误，处理申诉请求时出现异常")
    })
    @PostMapping("/employee/exception/contact-administrator")
    public ResponseEntity<?> submitExceptionAppeal(@RequestBody AttendanceExceptionAppealDTO appealDTO) {
        try {
            // 参数校验
            if (appealDTO.getEmployeeNo() == null || appealDTO.getEmployeeNo().isEmpty()) {
                throw new IllegalArgumentException("员工编号不能为空");
            }
            if (appealDTO.getRecordId() == null) {
                throw new IllegalArgumentException("考勤记录ID不能为空");
            }
            if (appealDTO.getExplanation() == null || appealDTO.getExplanation().isEmpty()) {
                throw new IllegalArgumentException("申诉说明不能为空");
            }

            // 调用服务提交异常考勤申诉
            boolean result = attendanceService.submitExceptionAppeal(appealDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);

            if (result) {
                response.put("message", "您的异常考勤申诉已成功提交，管理员将尽快审核处理。请关注申诉结果，如有紧急情况可联系人事部门。");
            } else {
                response.put("message", "申诉提交失败，请稍后重试或直接联系人事部门处理");
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "申诉提交失败：" + e.getMessage() + "，请检查您的输入后重试");

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统处理您的申诉请求时遇到问题：" + e.getMessage() + "，请稍后重试或联系系统管理员");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 员工人脸录入/重新录入接口
     *
     * @param employeeNo 员工编号
     * @param faceImage  人脸图像文件（JPG格式）
     * @return 录入结果
     */
    @Operation(summary = "员工人脸录入/重新录入", description = "支持已注册员工通过上传JPG文件录入或重新录入人脸图像，系统会根据employeeNo判断是首次录入还是覆盖更新。注意：图像中必须只包含一个人脸，否则会返回错误。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "人脸录入/更新成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误，如员工编号不存在、文件格式不正确或图像中包含多个人脸",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误，处理人脸录入时出现异常",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping(value = "/employee/info/register-or-update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerOrUpdateFace(
            @Parameter(description = "员工编号", required = true)
            @RequestParam String employeeNo,

            @Parameter(description = "人脸图像文件（JPG格式），必须只包含一个人脸", required = true)
            @RequestPart MultipartFile faceImage) {
        try {
            // 检查员工是否存在
            Employee employee = employeeService.getEmployeeInfo(employeeNo);
            if (employee == null) {
                throw new IllegalArgumentException("员工编号不存在");
            }

            // 检查文件
            if (faceImage == null || faceImage.isEmpty()) {
                throw new IllegalArgumentException("人脸图像文件不能为空");
            }

            // 检查文件类型
            String contentType = faceImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/jpeg")) {
                throw new IllegalArgumentException("只支持JPG/JPEG格式的图像文件");
            }

            // 检查是否已有人脸信息
            boolean isUpdate = faceFeatureService.hasFaceFeature(employeeNo);

            // 调用服务录入人脸特征
            FaceFeature faceFeature = faceFeatureService.enrollFaceFeatureByFile(employeeNo, faceImage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isUpdate ? "人脸信息更新成功" : "人脸信息首次录入成功");
            response.put("employeeNo", faceFeature.getEmployeeNo());
            response.put("extractionTime", faceFeature.getExtractionTime());
            response.put("isUpdate", isUpdate);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "人脸录入失败：" + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "人脸录入处理时遇到问题：" + e.getMessage() + "，请检查人脸图像是否清晰或重新尝试");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查询员工考勤数据接口
     *
     * @param employeeNo 员工编号
     * @param current    当前页码
     * @param size       每页记录数
     * @param timeRange  时间范围：1-当月, 2-上月, 3-本季度, 4-本年度, 其他值或不传-不限时间范围
     * @return 考勤数据分页结果
     */
    @Operation(summary = "查询员工考勤数据", description = "分页查询员工的所有考勤记录，包括正常和异常记录，支持时间范围筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/employee/record")
    public ResponseEntity<?> getAttendanceRecords(
            @Parameter(description = "员工编号", required = true)
            @RequestParam String employeeNo,

            @Parameter(description = "当前页码（从1开始）", required = true)
            @RequestParam(defaultValue = "1") Integer current,

            @Parameter(description = "每页记录数", required = true)
            @RequestParam(defaultValue = "10") Integer size,

            @Parameter(description = "时间范围：1-当月, 2-上月, 3-本季度, 4-本年度, 不传-不限时间范围")
            @RequestParam(required = false) Integer timeRange) {
        try {
            // 参数校验
            if (current < 1) {
                throw new IllegalArgumentException("当前页码必须大于等于1");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("每页记录数必须在1-100之间");
            }

            // 调用服务查询所有考勤记录
            AttendanceRecordPageDTO result = attendanceService.getAttendanceRecords(employeeNo, current, size, timeRange);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询考勤记录失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查询员工所有请假记录接口
     *
     * @param employeeNo 员工编号
     * @param current    当前页码
     * @param size       每页记录数
     * @param status     状态筛选（可选）
     * @param sortBy     排序字段（可选）
     * @param sortOrder  排序方式（可选）
     * @return 请假记录分页结果
     */
    @Operation(summary = "查询员工所有请假记录", description = "员工本人查看其历史请假记录，包括已提交申请、已审批记录（批准/拒绝）、请假类型、时间范围及申请理由")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/employee/apply")
    public ResponseEntity<?> getEmployeeLeaveRecords(
            @Parameter(description = "员工编号", required = true)
            @RequestParam String employeeNo,

            @Parameter(description = "当前页码（从1开始）", required = true)
            @RequestParam(defaultValue = "1") Integer current,

            @Parameter(description = "每页记录数", required = true)
            @RequestParam(defaultValue = "10") Integer size,

            @Parameter(description = "状态筛选：0-待审批，1-已批准，2-已拒绝")
            @RequestParam(required = false) Integer status,

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
            if (status != null) {
                leavePage = leaveService.getEmployeeLeaveRecordsByStatus(employeeNo, status, pageable);
            } else {
                leavePage = leaveService.getEmployeeLeaveRecords(employeeNo, pageable);
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
     * 员工提交请假申请接口
     *
     * @param createDTO 请假申请信息
     * @return 申请结果
     */
    @Operation(summary = "员工提交请假申请", description = "员工填写并提交新的请假申请，包括请假类型、请假起止日期、请假原因，系统将自动记录申请状态为'待审批'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "申请提交成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/employee/apply/create")
    public ResponseEntity<?> createLeaveApplication(@RequestBody LeaveRecordCreateDTO createDTO) {
        try {
            // 参数校验
            if (createDTO.getEmployeeNo() == null || createDTO.getEmployeeNo().isEmpty()) {
                throw new IllegalArgumentException("员工编号不能为空");
            }
            if (createDTO.getLeaveType() == null) {
                throw new IllegalArgumentException("请假类型不能为空");
            }
            if (createDTO.getStartDate() == null) {
                throw new IllegalArgumentException("请假开始日期不能为空");
            }
            if (createDTO.getEndDate() == null) {
                throw new IllegalArgumentException("请假结束日期不能为空");
            }

            // 时间范围校验
            if (createDTO.getStartDate().isAfter(createDTO.getEndDate())) {
                throw new IllegalArgumentException("请假开始日期不能晚于结束日期");
            }

            // 请假日期不能是过去的日期
            if (createDTO.getStartDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("请假开始日期不能是过去的日期");
            }

            // 请假时长检查
            if (createDTO.getStartDate().until(createDTO.getEndDate().plusDays(1)).getDays() > 90) {
                throw new IllegalArgumentException("单次请假时长最多不能超过90天");
            }

            // 检查是否与已有请假时间重叠
            if (leaveService.hasOverlappingLeave(createDTO.getEmployeeNo(), createDTO.getStartDate(), createDTO.getEndDate())) {
                throw new IllegalArgumentException("当前请假时间与已有请假记录时间重叠，请调整请假时间");
            }

            // 创建请假记录
            LeaveRecord leaveRecord = new LeaveRecord();
            leaveRecord.setEmployeeNo(createDTO.getEmployeeNo());
            leaveRecord.setLeaveType(createDTO.getLeaveType());
            leaveRecord.setStartDate(createDTO.getStartDate());
            leaveRecord.setEndDate(createDTO.getEndDate());
            leaveRecord.setReason(createDTO.getReason());

            // 提交请假申请
            LeaveRecord created = leaveService.submitLeaveApplication(leaveRecord);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "请假申请提交成功，等待管理员审批");
            response.put("leaveId", created.getId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "提交请假申请失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 将LeaveRecord实体转换为LeaveRecordDTO
     *
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
     *
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
     *
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