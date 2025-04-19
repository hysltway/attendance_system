package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AttendanceExceptionAppealDTO;
import com.example.attendance_system.dto.AttendanceExceptionPageDTO;
import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.dto.LoginDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.FaceFeature;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.EmployeeService;
import com.example.attendance_system.service.FaceFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * 员工注册接口
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
     * @param employeeNo 员工编号
     * @param current 当前页码
     * @param size 每页记录数
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
     * @param employeeNo 员工编号
     * @param faceImage 人脸图像文件（JPG格式）
     * @return 录入结果
     */
    @Operation(summary = "员工人脸录入/重新录入", description = "支持已注册员工通过上传JPG文件录入或重新录入人脸图像，系统会根据employeeNo判断是首次录入还是覆盖更新")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "人脸录入/更新成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误，如员工编号不存在或文件格式不正确", 
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
            
            @Parameter(description = "人脸图像文件（JPG格式）", required = true)
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
} 