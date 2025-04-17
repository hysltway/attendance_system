package com.example.attendance_system.controller;

import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.dto.FaceEnrollmentDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.FaceFeature;
import com.example.attendance_system.service.EmployeeService;
import com.example.attendance_system.service.FaceFeatureService;
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
    @PostMapping("/new")
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
    @PostMapping("/face-enroll/base64")
    public ResponseEntity<?> enrollFaceImageBase64(@RequestBody FaceEnrollmentDTO faceEnrollmentDTO) {
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
    @PostMapping(value = "/face-enroll", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enrollFaceImage(
            @RequestParam("employeeNo") String employeeNo,
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