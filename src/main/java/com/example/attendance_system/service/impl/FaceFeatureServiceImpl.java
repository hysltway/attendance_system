package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.FaceEnrollmentDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.FaceFeature;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.repository.FaceFeatureRepository;
import com.example.attendance_system.service.FaceFeatureService;
import com.example.attendance_system.util.FaceRecognitionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 人脸特征服务实现类
 */
@Slf4j
@Service
public class FaceFeatureServiceImpl implements FaceFeatureService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private FaceFeatureRepository faceFeatureRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private FaceRecognitionUtil faceRecognitionUtil;

    /**
     * 通过Base64编码的图像录入人脸特征
     *
     * @param faceEnrollmentDTO 人脸录入DTO
     * @return 录入结果
     * @throws Exception 处理异常
     */
    @Override
    @Transactional
    public FaceFeature enrollFaceFeatureByBase64(FaceEnrollmentDTO faceEnrollmentDTO) throws Exception {
        String employeeNo = faceEnrollmentDTO.getEmployeeNo();
        String faceImageBase64 = faceEnrollmentDTO.getFaceImageBase64();

        // 检查参数
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("员工编号不能为空");
        }
        if (faceImageBase64 == null || faceImageBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("人脸图像数据不能为空");
        }

        // 检查员工是否存在
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
        if (employee == null) {
            throw new IllegalArgumentException("员工编号不存在：" + employeeNo);
        }

        // 保存Base64图像到临时文件
        String imagePath = null;
        try {
            imagePath = faceRecognitionUtil.saveBase64ImageToTemp(faceImageBase64);

            // 提取人脸特征
            String featureJson = faceRecognitionUtil.extractFaceFeature(imagePath);
            Map<String, Object> featureMap = objectMapper.readValue(featureJson, Map.class);

            // 检查提取结果
            if (featureMap.containsKey("error")) {
                throw new Exception(featureMap.get("error").toString());
            }

            // 保存或更新人脸特征
            return saveFaceFeature(employeeNo, featureJson);
        } finally {
            // 删除临时文件
            if (imagePath != null) {
                faceRecognitionUtil.deleteTempFile(imagePath);
            }
        }
    }

    /**
     * 通过上传的图像文件录入人脸特征
     *
     * @param employeeNo    员工编号
     * @param faceImageFile 人脸图像文件
     * @return 录入结果
     * @throws Exception 处理异常
     */
    @Override
    @Transactional
    public FaceFeature enrollFaceFeatureByFile(String employeeNo, MultipartFile faceImageFile) throws Exception {
        // 检查参数
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("员工编号不能为空");
        }
        if (faceImageFile == null || faceImageFile.isEmpty()) {
            throw new IllegalArgumentException("人脸图像文件不能为空");
        }

        // 检查员工是否存在
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
        if (employee == null) {
            throw new IllegalArgumentException("员工编号不存在：" + employeeNo);
        }

        // 检查文件类型
        String contentType = faceImageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/jpeg")) {
            throw new IllegalArgumentException("只支持JPG/JPEG格式的图像文件");
        }

        // 保存上传的文件到临时目录
        String tempDir = System.getProperty("java.io.tmpdir");
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileName = "face_" + timeStamp + ".jpg";
        Path tempFilePath = Paths.get(tempDir, fileName);

        try {
            // 写入临时文件
            Files.copy(faceImageFile.getInputStream(), tempFilePath);
            log.info("Saved uploaded image to temporary file: {}", tempFilePath);

            // 提取人脸特征
            String featureJson = faceRecognitionUtil.extractFaceFeature(tempFilePath.toString());
            Map<String, Object> featureMap = objectMapper.readValue(featureJson, Map.class);

            // 检查提取结果
            if (featureMap.containsKey("error")) {
                throw new Exception(featureMap.get("error").toString());
            }

            // 保存或更新人脸特征
            return saveFaceFeature(employeeNo, featureJson);
        } finally {
            // 删除临时文件
            try {
                Files.deleteIfExists(tempFilePath);
                log.info("Deleted temporary file: {}", tempFilePath);
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", tempFilePath, e);
            }
        }
    }

    /**
     * 保存人脸特征数据
     *
     * @param employeeNo  员工编号
     * @param featureJson 特征数据JSON
     * @return 保存的特征实体
     */
    private FaceFeature saveFaceFeature(String employeeNo, String featureJson) {
        // 查询现有特征
        FaceFeature faceFeature = faceFeatureRepository.findByEmployeeNo(employeeNo);

        if (faceFeature != null) {
            // 更新现有特征
            faceFeature.setFeatureVector(featureJson);
            faceFeature.setExtractionTime(LocalDateTime.now());
            faceFeature.setStatus(1);
        } else {
            // 创建新特征
            faceFeature = new FaceFeature();
            faceFeature.setEmployeeNo(employeeNo);
            faceFeature.setFeatureVector(featureJson);
            faceFeature.setExtractionTime(LocalDateTime.now());
            faceFeature.setStatus(1);
        }

        return faceFeatureRepository.save(faceFeature);
    }

    /**
     * 查询员工的人脸特征
     *
     * @param employeeNo 员工编号
     * @return 人脸特征
     */
    @Override
    public FaceFeature getFaceFeature(String employeeNo) {
        return faceFeatureRepository.findByEmployeeNo(employeeNo);
    }

    /**
     * 删除员工的人脸特征
     *
     * @param employeeNo 员工编号
     */
    @Override
    @Transactional
    public void deleteFaceFeature(String employeeNo) {
        faceFeatureRepository.deleteByEmployeeNo(employeeNo);
    }

    /**
     * 检查员工是否已录入人脸特征
     *
     * @param employeeNo 员工编号
     * @return 是否已录入
     */
    @Override
    public boolean hasFaceFeature(String employeeNo) {
        return faceFeatureRepository.existsByEmployeeNo(employeeNo);
    }
} 