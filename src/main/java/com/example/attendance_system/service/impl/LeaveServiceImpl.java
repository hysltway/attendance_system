package com.example.attendance_system.service.impl;

import com.example.attendance_system.entity.LeaveRecord;
import com.example.attendance_system.repository.LeaveRecordRepository;
import com.example.attendance_system.service.LeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 请假服务实现类
 */
@Slf4j
@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    @Override
    @Transactional
    public LeaveRecord submitLeaveApplication(LeaveRecord leaveRecord) {
        // 设置默认状态为待审批
        leaveRecord.setStatus(0);
        return leaveRecordRepository.save(leaveRecord);
    }

    @Override
    @Transactional
    public LeaveRecord approveLeaveApplication(Long id, String approverNo, Integer status, String remark) {
        Optional<LeaveRecord> optionalLeaveRecord = leaveRecordRepository.findById(id);
        if (optionalLeaveRecord.isEmpty()) {
            throw new IllegalArgumentException("请假记录不存在");
        }
        
        LeaveRecord leaveRecord = optionalLeaveRecord.get();
        // 只能审批待审批状态的请假记录
        if (leaveRecord.getStatus() != 0) {
            throw new IllegalStateException("该请假申请已被处理，无法再次审批");
        }
        
        // 更新审批信息
        leaveRecord.setStatus(status);
        leaveRecord.setApproverNo(approverNo);
        leaveRecord.setApprovalTime(LocalDateTime.now());
        leaveRecord.setApprovalRemark(remark);
        
        return leaveRecordRepository.save(leaveRecord);
    }

    @Override
    public List<LeaveRecord> getEmployeeLeaveRecords(String employeeNo) {
        return leaveRecordRepository.findByEmployeeNo(employeeNo);
    }

    @Override
    public List<LeaveRecord> getPendingLeaveRecords() {
        return leaveRecordRepository.findByEmployeeNoAndStatus(null, 0);
    }

    @Override
    public boolean isEmployeeOnLeave(String employeeNo, LocalDate date) {
        return leaveRecordRepository.hasApprovedLeave(employeeNo, date);
    }

    @Override
    public List<String> getEmployeesOnLeave(LocalDate date) {
        return leaveRecordRepository.findEmployeesOnLeave(date);
    }
} 