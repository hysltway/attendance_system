package com.example.attendance_system.service;

import com.example.attendance_system.dto.FaceEnrollmentDTO;
import com.example.attendance_system.entity.FaceFeature;
import org.springframework.web.multipart.MultipartFile;

/**
 * 人脸特征服务接口
 */
public interface FaceFeatureService {
    
    /**
     * 通过Base64编码的图像录入人脸特征
     * @param faceEnrollmentDTO 人脸录入DTO
     * @return 录入结果
     * @throws Exception 处理异常
     */
    FaceFeature enrollFaceFeatureByBase64(FaceEnrollmentDTO faceEnrollmentDTO) throws Exception;
    
    /**
     * 通过上传的图像文件录入人脸特征
     * @param employeeNo 员工编号
     * @param faceImageFile 人脸图像文件
     * @return 录入结果
     * @throws Exception 处理异常
     */
    FaceFeature enrollFaceFeatureByFile(String employeeNo, MultipartFile faceImageFile) throws Exception;
    
    /**
     * 查询员工的人脸特征
     * @param employeeNo 员工编号
     * @return 人脸特征
     */
    FaceFeature getFaceFeature(String employeeNo);
    
    /**
     * 删除员工的人脸特征
     * @param employeeNo 员工编号
     */
    void deleteFaceFeature(String employeeNo);
    
    /**
     * 检查员工是否已录入人脸特征
     * @param employeeNo 员工编号
     * @return 是否已录入
     */
    boolean hasFaceFeature(String employeeNo);
} 