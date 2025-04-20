package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.AdminAttendanceExceptionUpdateDTO;
import com.example.attendance_system.dto.AttendanceExceptionAppealDTO;
import com.example.attendance_system.dto.AttendanceExceptionDTO;
import com.example.attendance_system.dto.AttendanceExceptionPageDTO;
import com.example.attendance_system.dto.AttendanceRecordDTO;
import com.example.attendance_system.dto.AttendanceRecordPageDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public FaceRecognitionDTO clockInByFace(MultipartFile file, Integer checkMethod) throws Exception {
        if (file.isEmpty()) {
            return FaceRecognitionDTO.builder()
                    .status("error")
                    .message("上传的图像文件为空")
                    .checkMethod(checkMethod)
                    .similarityThreshold(similarityThreshold)
                    .build();
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/jpeg")) {
            return FaceRecognitionDTO.builder()
                    .status("error")
                    .message("仅支持JPEG/JPG格式图像")
                    .checkMethod(checkMethod)
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
                        .checkMethod(checkMethod)
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
                        .checkMethod(checkMethod)
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
                        .checkMethod(checkMethod)
                        .similarityThreshold(similarityThreshold)
                        .build();
            }

            // 记录打卡信息
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            
            // 规定时间配置
            LocalTime standardStartTime = LocalTime.of(9, 0); // 9:00上班
            LocalTime standardEndTime = LocalTime.of(17, 0); // 17:00下班
            LocalTime lateLimitTime = LocalTime.of(11, 0); // 11:00后算旷工
            LocalTime earlyLimitTime = LocalTime.of(15, 0); // 15:00前算旷工
            
            // 设置当天开始和结束时间，用于查询当天打卡记录
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            
            // 判断打卡类型（上班/下班）
            LocalTime currentTime = now.toLocalTime();
            LocalTime noonTime = LocalTime.of(12, 0);
            int checkType;
            
            // 查询当天是否有打卡记录（无论上班还是下班）
            long todayTotalAttendance = attendanceRecordRepository.countByEmployeeNoAndCheckTimeBetween(
                    matchedEmployeeNo, startOfDay, endOfDay);

            if (todayTotalAttendance == 0) {
                // 当天第一次打卡，无论何时都记为上班打卡
                checkType = 1; // 1-上班打卡
            } else if (currentTime.isBefore(noonTime)) {
                // 当天已有打卡记录，上午打卡，记为上班打卡
                checkType = 1; // 1-上班打卡
            } else {
                // 当天已有打卡记录，下午打卡，记为下班打卡
                checkType = 2; // 2-下班打卡
            }
            
            // 检查是否已经打过相同类型的卡
            long todayAttendanceCount = attendanceRecordRepository.countTodayAttendance(
                    matchedEmployeeNo, startOfDay, endOfDay, checkType);
            
            if (todayAttendanceCount > 0) {
                // 已经打过相同类型的卡，返回提示信息
                return FaceRecognitionDTO.builder()
                        .status("error")
                        .message("您今天已经完成" + (checkType == 1 ? "上班" : "下班") + "打卡，无需重复打卡")
                        .employeeNo(matchedEmployeeNo)
                        .name(employee.getName())
                        .timestamp(now)
                        .checkMethod(checkMethod)
                        .similarity(similarity)
                        .similarityThreshold(similarityThreshold)
                        .build();
            }
            
            // 创建考勤记录
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployeeNo(matchedEmployeeNo);
            record.setCheckTime(now);
            record.setCheckMethod(checkMethod); // 使用传入的打卡方式
            record.setCheckType(checkType);
            
            // 打卡状态和原因判断
            int status;
            String reason;
            
            if (checkType == 1) { // 上班打卡
                if (currentTime.isAfter(noonTime)) {
                    // 下午才来上班，直接记为旷工
                    status = 4; // 4-旷工
                    reason = String.format("严重迟到，下午%s才第一次打卡，已记为旷工。应打卡时间：%s", 
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            standardStartTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else if (currentTime.isBefore(standardStartTime)) {
                    // 正常打卡（提前到岗）
                    status = 1; // 1-正常
                    reason = "按时上班打卡";
                } else if (currentTime.isBefore(lateLimitTime)) {
                    // 迟到
                    long lateMinutes = java.time.Duration.between(standardStartTime, currentTime).toMinutes();
                    status = 2; // 2-迟到
                    reason = String.format("上班迟到%d分钟，应打卡时间：%s，实际打卡时间：%s", 
                            lateMinutes,
                            standardStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    // 旷工（超过11点打卡算旷工）
                    status = 4; // 4-旷工
                    reason = String.format("严重迟到，已记为旷工。应打卡时间：%s，实际打卡时间：%s", 
                            standardStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
            } else { // 下班打卡
                if (currentTime.isBefore(earlyLimitTime)) {
                    // 旷工（早退太早，算旷工）
                    status = 4; // 4-旷工
                    reason = String.format("严重早退，已记为旷工。应打卡时间：%s，实际打卡时间：%s", 
                            standardEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else if (currentTime.isBefore(standardEndTime)) {
                    // 早退
                    long earlyMinutes = java.time.Duration.between(currentTime, standardEndTime).toMinutes();
                    status = 3; // 3-早退
                    reason = String.format("下班早退%d分钟，应打卡时间：%s，实际打卡时间：%s", 
                            earlyMinutes,
                            standardEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else if (currentTime.isAfter(standardEndTime.plusHours(2))) {
                    // 加班（超过正常下班时间2小时）
                    long overtimeMinutes = java.time.Duration.between(standardEndTime, currentTime).toMinutes();
                    status = 5; // 5-加班
                    reason = String.format("加班%d分钟，标准下班时间：%s，实际打卡时间：%s", 
                            overtimeMinutes,
                            standardEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    // 正常下班
                    status = 1; // 1-正常
                    reason = "按时下班打卡";
                }
            }
            
            // 设置状态和原因
            record.setStatus(status);
            record.setReason(reason);
            
            // 保存打卡记录
            attendanceRecordRepository.save(record);
            
            // 构建返回消息
            String resultMessage;
            switch (status) {
                case 1:
                    resultMessage = "打卡成功，" + (checkType == 1 ? "上班" : "下班") + "考勤正常";
                    break;
                case 2:
                    resultMessage = "打卡成功，已记录迟到情况";
                    break;
                case 3:
                    resultMessage = "打卡成功，已记录早退情况";
                    break;
                case 4:
                    resultMessage = "打卡成功，但已记录为旷工，请联系管理员";
                    break;
                case 5:
                    resultMessage = "打卡成功，已记录加班情况";
                    break;
                default:
                    resultMessage = "打卡成功";
            }

            // 构建返回结果
            return FaceRecognitionDTO.builder()
                    .status("success")
                    .employeeNo(matchedEmployeeNo)
                    .name(employee.getName())
                    .timestamp(now)
                    .message(resultMessage)
                    .similarity(similarity)
                    .checkMethod(checkMethod)
                    .similarityThreshold(similarityThreshold)
                    .build();
        } finally {
            // 删除临时文件
            faceRecognitionUtil.deleteTempFile(tempFile.toString());
        }
    }
    
    @Override
    public AttendanceExceptionPageDTO getExceptionRecords(String employeeNo, Integer current, Integer size) {
        // 检查用户是否存在
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        // 创建分页参数，注意：JPA分页从0开始计数
        Pageable pageable = PageRequest.of(current - 1, size);
        
        // 查询异常考勤记录
        Page<AttendanceRecord> page = attendanceRecordRepository.findExceptionRecords(employeeNo, pageable);
        
        // 转换为DTO对象
        List<AttendanceExceptionDTO> records = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (AttendanceRecord record : page.getContent()) {
            AttendanceExceptionDTO dto = AttendanceExceptionDTO.builder()
                    .id(record.getId())
                    .date(record.getCheckTime().format(dateFormatter))
                    .checkTime(record.getCheckTime())
                    .checkType(record.getCheckType())
                    .checkTypeText(getCheckTypeText(record.getCheckType()))
                    .checkStatus(record.getStatus())
                    .checkTypeDesc(getCheckTypeDesc(record.getStatus()))
                    .reason(record.getReason())
                    .status(record.getSubmittedToAdmin() ? 1 : 0)
                    .employeeNo(record.getEmployeeNo())
                    .explanation(record.getExplanation())
                    .remark(record.getRemark())
                    .submittedToAdmin(record.getSubmittedToAdmin())
                    .processedByAdmin(record.getProcessedByAdmin())
                    .checkMethod(record.getCheckMethod())
                    .checkMethodText(getCheckMethodText(record.getCheckMethod()))
                    .createdTime(record.getCreatedTime())
                    .updatedTime(record.getUpdatedTime())
                    .build();
            records.add(dto);
        }
        
        // 构建分页结果
        return AttendanceExceptionPageDTO.builder()
                .current(current)
                .size(size)
                .total(page.getTotalElements())
                .records(records)
                .build();
    }
    
    @Override
    public boolean submitExceptionAppeal(AttendanceExceptionAppealDTO appealDTO) {
        // 先检查用户是否存在
        Employee employee = employeeRepository.findByEmployeeNo(appealDTO.getEmployeeNo());
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        // 查找指定的异常考勤记录
        Optional<AttendanceRecord> recordOpt = attendanceRecordRepository.findById(appealDTO.getRecordId());
        
        // 验证记录是否存在，且属于当前员工
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("未找到指定的考勤记录");
        }
        
        AttendanceRecord record = recordOpt.get();
        
        // 验证记录是否属于当前员工
        if (!record.getEmployeeNo().equals(appealDTO.getEmployeeNo())) {
            throw new IllegalArgumentException("无权操作此考勤记录");
        }
        
        // 验证记录是否已被管理员处理过
        if (record.getProcessedByAdmin()) {
            throw new IllegalArgumentException("该考勤记录已被管理员处理，无法再次申诉，如有异议请直接联系人事部门");
        }
        
        // 验证记录状态是否为异常且未提交
        if (record.getStatus() == 1 || record.getSubmittedToAdmin()) {
            throw new IllegalArgumentException("该记录不是未处理的异常记录，无法申诉");
        }
        
        // 更新申诉信息
        record.setExplanation(appealDTO.getExplanation());
        record.setSubmittedToAdmin(true);
        attendanceRecordRepository.save(record);
        
        return true;
    }
    
    /**
     * 获取考勤类型描述
     * @param checkType 考勤类型编号
     * @return 考勤类型描述
     */
    private String getCheckTypeDesc(Integer checkType) {
        switch (checkType) {
            case 1:
                return "正常";
            case 2:
                return "迟到";
            case 3:
                return "早退";
            case 4:
                return "旷工";
            case 5:
                return "加班";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取打卡类型描述
     * @param checkType 打卡类型编号
     * @return 打卡类型描述
     */
    private String getCheckTypeText(Integer checkType) {
        switch (checkType) {
            case 1:
                return "上班打卡";
            case 2:
                return "下班打卡";
            case 3:
                return "外出打卡";
            case 4:
                return "返回打卡";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取打卡方式描述
     * @param checkMethod 打卡方式编号
     * @return 打卡方式描述
     */
    private String getCheckMethodText(Integer checkMethod) {
        switch (checkMethod) {
            case 1:
                return "人脸识别";
            case 2:
                return "管理员录入";
            case 3:
                return "系统自动生成";
            default:
                return "未知";
        }
    }
    
    @Override
    public AttendanceExceptionPageDTO getAllExceptionAppeals(Integer current, Integer size) {
        // 参数校验
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        // 创建分页参数，注意：JPA分页从0开始计数
        Pageable pageable = PageRequest.of(current - 1, size);
        
        // 查询所有已提交申诉的异常考勤记录
        Page<AttendanceRecord> page = attendanceRecordRepository.findAllExceptionAppeals(pageable);
        
        // 转换为DTO对象
        List<AttendanceExceptionDTO> records = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (AttendanceRecord record : page.getContent()) {
            AttendanceExceptionDTO dto = AttendanceExceptionDTO.builder()
                    .id(record.getId())
                    .date(record.getCheckTime().format(dateFormatter))
                    .checkTime(record.getCheckTime())
                    .checkType(record.getCheckType())
                    .checkTypeText(getCheckTypeText(record.getCheckType()))
                    .checkStatus(record.getStatus())
                    .checkTypeDesc(getCheckTypeDesc(record.getStatus()))
                    .reason(record.getReason())
                    .status(record.getSubmittedToAdmin() ? 1 : 0)
                    .employeeNo(record.getEmployeeNo())
                    .explanation(record.getExplanation())
                    .remark(record.getRemark())
                    .submittedToAdmin(record.getSubmittedToAdmin())
                    .processedByAdmin(record.getProcessedByAdmin())
                    .checkMethod(record.getCheckMethod())
                    .checkMethodText(getCheckMethodText(record.getCheckMethod()))
                    .createdTime(record.getCreatedTime())
                    .updatedTime(record.getUpdatedTime())
                    .build();
            records.add(dto);
        }
        
        // 构建分页结果
        return AttendanceExceptionPageDTO.builder()
                .current(current)
                .size(size)
                .total(page.getTotalElements())
                .records(records)
                .build();
    }
    
    @Override
    @Transactional
    public AttendanceRecord updateExceptionRecord(AdminAttendanceExceptionUpdateDTO updateDTO) {
        // 查找指定的异常考勤记录
        AttendanceRecord record = attendanceRecordRepository.findById(updateDTO.getRecordId())
                .orElseThrow(() -> new IllegalArgumentException("未找到指定的考勤记录"));
        
        // 验证记录是否为已提交申诉的异常记录
        if (!record.getSubmittedToAdmin()) {
            throw new IllegalArgumentException("该记录未提交申诉，无法处理");
        }
        
        // 更新记录状态
        if (updateDTO.getStatus() != null) {
            record.setStatus(updateDTO.getStatus());
        }
        
        // 更新备注
        if (updateDTO.getRemark() != null) {
            record.setRemark(updateDTO.getRemark());
        }
        
        // 标记申诉已处理，将submittedToAdmin设为false，使其不再出现在待处理列表中
        record.setSubmittedToAdmin(false);
        
        // 标记该记录已被管理员处理过，防止再次提交申诉
        record.setProcessedByAdmin(true);
        
        // 保存记录
        return attendanceRecordRepository.save(record);
    }
    
    @Override
    public AttendanceRecordPageDTO getAttendanceRecords(String employeeNo, Integer current, Integer size) {
        // 检查用户是否存在
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        // 创建分页参数，注意：JPA分页从0开始计数
        Pageable pageable = PageRequest.of(current - 1, size);
        
        // 查询所有考勤记录
        Page<AttendanceRecord> page = attendanceRecordRepository.findByEmployeeNoOrderByCheckTimeDesc(employeeNo, pageable);
        
        // 转换为DTO对象
        List<AttendanceRecordDTO> records = new ArrayList<>();
        
        for (AttendanceRecord record : page.getContent()) {
            AttendanceRecordDTO dto = AttendanceRecordDTO.builder()
                    .id(record.getId())
                    .employeeNo(record.getEmployeeNo())
                    .checkTime(record.getCheckTime())
                    .checkType(record.getCheckType())
                    .checkTypeDesc(getCheckTypeText(record.getCheckType()))
                    .checkMethod(record.getCheckMethod())
                    .checkMethodDesc(getCheckMethodText(record.getCheckMethod()))
                    .status(record.getStatus())
                    .statusDesc(getCheckTypeDesc(record.getStatus()))
                    .remark(record.getRemark())
                    .reason(record.getReason())
                    .explanation(record.getExplanation())
                    .submittedToAdmin(record.getSubmittedToAdmin())
                    .processedByAdmin(record.getProcessedByAdmin())
                    .createdTime(record.getCreatedTime())
                    .updatedTime(record.getUpdatedTime())
                    .build();
            records.add(dto);
        }
        
        // 构建分页结果
        return AttendanceRecordPageDTO.builder()
                .current(current)
                .size(size)
                .total(page.getTotalElements())
                .pages(page.getTotalPages())
                .records(records)
                .build();
    }
} 