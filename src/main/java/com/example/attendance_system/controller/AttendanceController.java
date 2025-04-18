package com.example.attendance_system.controller;

import com.example.attendance_system.dto.FaceRecognitionDTO;
import com.example.attendance_system.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 考勤管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/attendance")
@CrossOrigin
@Tag(name = "考勤管理", description = "考勤打卡相关接口")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 人脸识别打卡接口
     * 接收前端上传的人脸图像，进行识别并记录打卡
     *
     * @param file 人脸图像文件
     * @return 打卡结果
     */
    @Operation(summary = "人脸识别打卡", description = "上传人脸图像进行身份识别并记录打卡")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "打卡成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = FaceRecognitionDTO.class))),
            @ApiResponse(responseCode = "400", description = "人脸识别失败或参数错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = FaceRecognitionDTO.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = FaceRecognitionDTO.class)))
    })
    @PostMapping("/face")
    public ResponseEntity<FaceRecognitionDTO> faceRecognition(
            @Parameter(description = "人脸图像文件，支持JPG、PNG格式", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("收到人脸识别打卡请求，文件大小: {} bytes", file.getSize());
        
        try {
            FaceRecognitionDTO result = attendanceService.clockInByFace(file);
            
            if ("success".equals(result.getStatus())) {
                log.info("人脸识别打卡成功，员工: {}, 姓名: {}", result.getEmployeeNo(), result.getName());
                return ResponseEntity.ok(result);
            } else {
                log.warn("人脸识别打卡失败: {}", result.getMessage());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("人脸识别打卡异常", e);
            FaceRecognitionDTO errorResult = FaceRecognitionDTO.builder()
                    .status("error")
                    .message("系统处理异常: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
} 