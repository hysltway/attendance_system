package com.example.attendance_system.service;

import com.example.attendance_system.dto.FaceRecognitionDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 考勤服务接口
 */
public interface AttendanceService {

    /**
     * 通过人脸识别进行打卡
     * @param file 人脸图像文件
     * @return 打卡结果
     * @throws Exception 打卡异常
     */
    FaceRecognitionDTO clockInByFace(MultipartFile file) throws Exception;
} 