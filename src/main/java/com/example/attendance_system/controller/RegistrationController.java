package com.example.attendance_system.controller;

import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.dto.FaceEnrollmentDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.FaceFeature;
import com.example.attendance_system.service.EmployeeService;
import com.example.attendance_system.service.FaceFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工注册控制器
 */
@Slf4j
@RestController
@RequestMapping("/register")
@CrossOrigin
@Tag(name = "员工注册", description = "员工注册和人脸录入相关接口")
public class RegistrationController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private FaceFeatureService faceFeatureService;

    /**
     * 员工基本信息注册接口
     *
     * @param registrationDTO 员工注册信息
     * @return 注册结果
     */
    @Operation(summary = "员工基本信息注册", description = "注册员工基本个人信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "参数错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping("/new")
    public ResponseEntity<?> registerEmployee(
            @Parameter(description = "员工注册信息", required = true)
            @RequestBody EmployeeRegistrationDTO registrationDTO) {
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
            log.error("员工注册失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "员工注册失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 人脸图像录入接口 - 使用Base64编码的图像
     *
     * @param faceEnrollmentDTO 人脸录入DTO
     * @return 录入结果
     */
    @Operation(summary = "人脸录入(Base64)", description = "使用Base64编码的图像进行人脸特征录入")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "人脸录入成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或人脸特征提取失败",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping("/face-enroll/base64")
    public ResponseEntity<?> enrollFaceImageBase64(
            @Parameter(description = "人脸录入信息，包含员工编号和Base64编码的人脸图像", required = true)
            @RequestBody FaceEnrollmentDTO faceEnrollmentDTO) {
        try {
            FaceFeature faceFeature = faceFeatureService.enrollFaceFeatureByBase64(faceEnrollmentDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "人脸录入成功");
            response.put("employeeNo", faceFeature.getEmployeeNo());
            response.put("extractionTime", faceFeature.getExtractionTime());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("人脸录入失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "人脸录入失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 人脸图像录入接口 - 使用上传的图像文件
     *
     * @param employeeNo    员工编号
     * @param faceImageFile 人脸图像文件
     * @return 录入结果
     */
    @Operation(summary = "人脸录入(文件上传)", description = "通过上传图像文件进行人脸特征录入")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "人脸录入成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或人脸特征提取失败",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping(value = "/face-enroll", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enrollFaceImage(
            @Parameter(description = "员工编号", required = true)
            @RequestParam("employeeNo") String employeeNo,
            @Parameter(description = "人脸图像文件，支持JPG、PNG格式", required = true)
            @RequestParam("faceImageFile") MultipartFile faceImageFile) {
        try {
            FaceFeature faceFeature = faceFeatureService.enrollFaceFeatureByFile(employeeNo, faceImageFile);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "人脸录入成功");
            response.put("employeeNo", faceFeature.getEmployeeNo());
            response.put("extractionTime", faceFeature.getExtractionTime());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("人脸录入失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "人脸录入失败：" + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
} 