package com.example.attendance_system.service;

import com.example.attendance_system.dto.AdminAttendanceExceptionUpdateDTO;
import com.example.attendance_system.dto.AttendanceExceptionAppealDTO;
import com.example.attendance_system.dto.AttendanceExceptionPageDTO;
import com.example.attendance_system.dto.AttendanceRecordPageDTO;
import com.example.attendance_system.dto.FaceRecognitionDTO;
import com.example.attendance_system.entity.AttendanceRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * 考勤服务接口
 */
public interface AttendanceService {

    /**
     * 通过人脸识别进行打卡
     * @param file 人脸图像文件
     * @param checkMethod 打卡方式：1-人脸识别，2-管理员录入，3-系统自动生成
     * @return 打卡结果
     * @throws Exception 打卡异常
     */
    FaceRecognitionDTO clockInByFace(MultipartFile file, Integer checkMethod) throws Exception;
    
    /**
     * 分页查询员工的所有考勤记录
     * @param employeeNo 员工编号
     * @param current 当前页码（从1开始）
     * @param size 每页记录数
     * @return 分页查询结果
     */
    AttendanceRecordPageDTO getAttendanceRecords(String employeeNo, Integer current, Integer size);
    
    /**
     * 分页查询员工的异常考勤记录
     * @param employeeNo 员工编号
     * @param current 当前页码（从1开始）
     * @param size 每页记录数
     * @return 分页查询结果
     */
    AttendanceExceptionPageDTO getExceptionRecords(String employeeNo, Integer current, Integer size);
    
    /**
     * 提交异常考勤申诉
     * @param appealDTO 申诉信息
     * @return 是否提交成功
     */
    boolean submitExceptionAppeal(AttendanceExceptionAppealDTO appealDTO);
    
    /**
     * 管理员获取所有提交了申诉的异常考勤记录
     * @param current 当前页码（从1开始）
     * @param size 每页记录数
     * @return 分页查询结果
     */
    AttendanceExceptionPageDTO getAllExceptionAppeals(Integer current, Integer size);
    
    /**
     * 管理员更新异常考勤记录
     * @param updateDTO 更新信息
     * @return 更新后的考勤记录
     */
    AttendanceRecord updateExceptionRecord(AdminAttendanceExceptionUpdateDTO updateDTO);
} 