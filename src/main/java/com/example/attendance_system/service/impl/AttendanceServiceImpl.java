package com.example.attendance_system.service.impl;

import com.example.attendance_system.blockchain.Block;
import com.example.attendance_system.dto.*;
import com.example.attendance_system.entity.AttendanceRecord;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.repository.AttendanceRecordRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.repository.FaceFeatureRepository;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.BlockchainService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Autowired
    private BlockchainService blockchainService;

    /**
     * 人脸相似度阈值，用于判断是否为同一个人
     * 从配置文件中读取
     */
    @Value("${app.attendance.face.similarity-threshold:0.6}")
    private double similarityThreshold;

    @Override
    @Transactional
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
            AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
            
            // 添加到区块链
            try {
                // 将记录DTO添加到区块链
                AttendanceRecordDTO dto = convertToDTO(savedRecord);
                Block block = blockchainService.addAttendanceRecord(dto);
                if (block != null) {
                    log.info("考勤记录 {} 已成功添加到区块链", savedRecord.getId());
                } else {
                    log.warn("考勤记录 {} 添加到区块链失败", savedRecord.getId());
                }
            } catch (Exception e) {
                log.error("添加考勤记录到区块链时发生错误", e);
                // 不影响正常流程继续执行
            }

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
            // 获取员工姓名
            String employeeName = "";
            Employee emp = employeeRepository.findByEmployeeNo(record.getEmployeeNo());
            if (emp != null) {
                employeeName = emp.getName();
            }

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
                    .employeeName(employeeName)
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
     *
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
     *
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
     *
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
            // 获取员工姓名
            String employeeName = "";
            Employee emp = employeeRepository.findByEmployeeNo(record.getEmployeeNo());
            if (emp != null) {
                employeeName = emp.getName();
            }

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
                    .employeeName(employeeName)
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
        // 默认不限制时间范围
        return getAttendanceRecords(employeeNo, current, size, null);
    }

    @Override
    public AttendanceRecordPageDTO getAttendanceRecords(String employeeNo, Integer current, Integer size, Integer timeRange) {
        // 检查用户是否存在
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }

        // 创建分页参数，注意：JPA分页从0开始计数
        Pageable pageable = PageRequest.of(current - 1, size);

        // 根据时间范围确定开始和结束日期
        LocalDate startDate = null;
        LocalDate endDate = null;
        String timeRangeDesc = "全部";

        if (timeRange != null) {
            LocalDate today = LocalDate.now();

            switch (timeRange) {
                case 1: // 当月
                    startDate = today.withDayOfMonth(1);
                    endDate = today.withDayOfMonth(today.lengthOfMonth());
                    timeRangeDesc = "当月";
                    break;
                case 2: // 上月
                    LocalDate lastMonth = today.minusMonths(1);
                    startDate = lastMonth.withDayOfMonth(1);
                    endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                    timeRangeDesc = "上月";
                    break;
                case 3: // 本季度
                    int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                    startDate = LocalDate.of(today.getYear(), (currentQuarter - 1) * 3 + 1, 1);
                    endDate = LocalDate.of(today.getYear(), currentQuarter * 3, 1)
                            .withDayOfMonth(LocalDate.of(today.getYear(), currentQuarter * 3, 1).lengthOfMonth());
                    timeRangeDesc = "本季度";
                    break;
                case 4: // 本年度
                    startDate = LocalDate.of(today.getYear(), 1, 1);
                    endDate = LocalDate.of(today.getYear(), 12, 31);
                    timeRangeDesc = "本年度";
                    break;
                default:
                    // 不限制时间范围
            }
        }

        log.debug("查询考勤记录，时间范围: {}, 开始日期: {}, 结束日期: {}", timeRangeDesc, startDate, endDate);

        // 查询所有考勤记录
        Page<AttendanceRecord> page;
        if (startDate != null && endDate != null) {
            // 设置时间范围
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // 根据员工编号和时间范围查询分页数据
            page = attendanceRecordRepository.findByEmployeeNoAndCheckTimeBetweenOrderByCheckTimeDesc(
                    employeeNo, startDateTime, endDateTime, pageable);
        } else {
            // 不限制时间范围
            page = attendanceRecordRepository.findByEmployeeNoOrderByCheckTimeDesc(employeeNo, pageable);
        }

        // 转换为DTO对象
        List<AttendanceRecordDTO> records = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (AttendanceRecord record : page.getContent()) {
            AttendanceRecordDTO dto = AttendanceRecordDTO.builder()
                    .id(record.getId())
                    .employeeNo(record.getEmployeeNo())
                    .checkTime(record.getCheckTime())
                    .checkTimeStr(record.getCheckTime().format(timeFormatter))
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

        // 查询统计数据
        List<AttendanceRecord> allRecords;
        if (startDate != null && endDate != null) {
            // 设置时间范围
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // 根据员工编号和时间范围查询所有数据
            allRecords = attendanceRecordRepository.findByEmployeeNoAndCheckTimeBetween(
                    employeeNo, startDateTime, endDateTime);
        } else {
            // 不限制时间范围
            allRecords = attendanceRecordRepository.findByEmployeeNo(employeeNo);
        }

        // 使用Java 8流操作计算统计数据
        // 出勤天数：状态为1(正常)、2(迟到)、3(早退)、5(加班)的不同日期数量
        int attendanceDays = (int) allRecords.stream()
                .filter(r -> r.getStatus() == 1 || r.getStatus() == 2 || r.getStatus() == 3 || r.getStatus() == 5)
                .map(r -> r.getCheckTime().toLocalDate())
                .distinct()
                .count();

        // 迟到次数：状态为2(迟到)的记录数量
        int lateTimes = (int) allRecords.stream()
                .filter(r -> r.getStatus() == 2)
                .count();

        // 早退次数：状态为3(早退)的记录数量
        int earlyLeaveTimes = (int) allRecords.stream()
                .filter(r -> r.getStatus() == 3)
                .count();

        // 缺勤天数：状态为4(旷工)的不同日期数量
        int absentDays = (int) allRecords.stream()
                .filter(r -> r.getStatus() == 4)
                .map(r -> r.getCheckTime().toLocalDate())
                .distinct()
                .count();

        // 加班次数：状态为5(加班)的记录数量
        int overtimeTimes = (int) allRecords.stream()
                .filter(r -> r.getStatus() == 5)
                .count();

        // 构建分页结果，包含统计数据
        return AttendanceRecordPageDTO.builder()
                .current(current)
                .size(size)
                .total(page.getTotalElements())
                .pages(page.getTotalPages())
                .records(records)
                .attendanceDays(attendanceDays)
                .lateTimes(lateTimes)
                .earlyLeaveTimes(earlyLeaveTimes)
                .absentDays(absentDays)
                .overtimeTimes(overtimeTimes)
                .build();
    }

    @Override
    public AttendanceExceptionPageDTO getAllExceptionAppealsExcludeEmployee(Integer current, Integer size, String excludeEmployeeNo, String employeeNo, String name) {
        // 参数校验
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (excludeEmployeeNo == null || excludeEmployeeNo.isEmpty()) {
            throw new IllegalArgumentException("排除的员工编号不能为空");
        }

        // 创建分页参数，注意：JPA分页从0开始计数
        Pageable pageable = PageRequest.of(current - 1, size);

        // 查询所有已提交申诉的异常考勤记录，排除指定员工
        Page<AttendanceRecord> page;

        // 根据条件查询
        if (employeeNo != null && !employeeNo.trim().isEmpty()) {
            // 按员工编号查询
            page = attendanceRecordRepository.findAllExceptionAppealsByEmployeeNo(excludeEmployeeNo, employeeNo, pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            // 按员工姓名查询
            // 先查找匹配姓名的员工
            List<Employee> employees = employeeRepository.findByNameContaining(name);
            if (employees.isEmpty()) {
                // 没有匹配的员工，返回空结果
                page = Page.empty(pageable);
            } else {
                // 获取员工编号列表
                List<String> employeeNos = employees.stream()
                        .map(Employee::getEmployeeNo)
                        .collect(Collectors.toList());

                // 按员工编号列表查询
                page = attendanceRecordRepository.findAllExceptionAppealsByEmployeeNos(excludeEmployeeNo, employeeNos, pageable);
            }
        } else {
            // 查询所有记录
            page = attendanceRecordRepository.findAllExceptionAppealsExcludeEmployee(excludeEmployeeNo, pageable);
        }

        // 转换为DTO对象
        List<AttendanceExceptionDTO> records = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AttendanceRecord record : page.getContent()) {
            // 获取员工姓名
            String employeeName = "";
            Employee emp = employeeRepository.findByEmployeeNo(record.getEmployeeNo());
            if (emp != null) {
                employeeName = emp.getName();
            }

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
                    .employeeName(employeeName)
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

        // 构建结果对象
        AttendanceExceptionPageDTO result = new AttendanceExceptionPageDTO();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(page.getTotalElements());
        result.setPages(page.getTotalPages());
        result.setRecords(records);

        return result;
    }

    /**
     * 将历史考勤记录上传到区块链
     * 
     * @param batchSize 每批次处理的记录数
     * @return 上传的记录总数
     */
    public int uploadHistoricalAttendanceRecordsToBlockchain(int batchSize) {
        log.info("开始上传历史考勤记录到区块链，批次大小: {}", batchSize);
        return blockchainService.uploadHistoricalRecords(batchSize);
    }

    /**
     * 将AttendanceRecord转换为AttendanceRecordDTO
     */
    private AttendanceRecordDTO convertToDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setEmployeeNo(record.getEmployeeNo());
        dto.setCheckTime(record.getCheckTime());
        dto.setCheckType(record.getCheckType());
        dto.setLocation(record.getRemark()); // 使用备注作为位置信息
        dto.setRemark(record.getRemark());
        return dto;
    }
} 