package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.FaceRecognitionDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.FaceFeature;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.repository.FaceFeatureRepository;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.util.FaceRecognitionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * 考勤服务实现类
 */
@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private FaceRecognitionUtil faceRecognitionUtil;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FaceFeatureRepository faceFeatureRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    /**
     * 人脸相似度阈值，用于判断是否为同一个人
     * 从配置文件中读取
     */
    @Value("${app.attendance.face.similarity-threshold:0.6}")
    private double similarityThreshold;

    @Override
    public FaceRecognitionDTO clockInByFace(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return FaceRecognitionDTO.builder()
                    .status("error")
                    .message("上传的图像文件为空")
                    .similarityThreshold(similarityThreshold)
                    .build();
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/jpeg")) {
            return FaceRecognitionDTO.builder()
                    .status("error")
                    .message("仅支持JPEG/JPG格式图像")
                    .similarityThreshold(similarityThreshold)
                    .build();
        }

        // 创建临时文件
        Path tempFile = Files.createTempFile("face_", ".jpg");
        file.transferTo(tempFile);

        try {
            // 调用人脸识别工具进行识别
            Map<String, Object> recognitionResult = faceRecognitionUtil.recognizeFace(tempFile.toString());

            // 判断识别结果
            if (recognitionResult.containsKey("error")) {
                return FaceRecognitionDTO.builder()
                        .status("error")
                        .message(recognitionResult.get("error").toString())
                        .similarityThreshold(similarityThreshold)
                        .build();
            }

            // 获取匹配结果
            String matchedEmployeeNo = (String) recognitionResult.get("employee_no");
            double similarity = (double) recognitionResult.get("similarity");

            // 如果相似度低于阈值，则认为识别失败
            if (similarity < similarityThreshold) {
                return FaceRecognitionDTO.builder()
                        .status("error")
                        .message("未识别到匹配人脸，请重试或联系管理员")
                        .similarity(similarity)
                        .similarityThreshold(similarityThreshold)
                        .build();
            }

            // 查询员工信息
            Employee employee = employeeRepository.findByEmployeeNo(matchedEmployeeNo);
            if (employee == null) {
                return FaceRecognitionDTO.builder()
                        .status("error")
                        .message("未找到匹配的员工信息")
                        .similarity(similarity)
                        .similarityThreshold(similarityThreshold)
                        .build();
            }

            // 记录打卡信息
            LocalDateTime now = LocalDateTime.now();
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployeeNo(matchedEmployeeNo);
            record.setCheckTime(now);
            record.setCheckMethod(1); // 1-人脸识别

            // 根据时间判断打卡类型（上班/下班）
            LocalTime currentTime = now.toLocalTime();
            LocalTime noonTime = LocalTime.of(12, 0);
            if (currentTime.isBefore(noonTime)) {
                record.setCheckType(1); // 1-上班打卡
            } else {
                record.setCheckType(2); // 2-下班打卡
            }

            // 保存打卡记录
            attendanceRecordRepository.save(record);

            // 构建返回结果
            return FaceRecognitionDTO.builder()
                    .status("success")
                    .employeeNo(matchedEmployeeNo)
                    .name(employee.getName())
                    .timestamp(now)
                    .message("人脸识别成功，已记录打卡")
                    .similarity(similarity)
                    .similarityThreshold(similarityThreshold)
                    .build();
        } finally {
            // 删除临时文件
            faceRecognitionUtil.deleteTempFile(tempFile.toString());
        }
    }
} 