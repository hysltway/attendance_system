package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.EmployeeInfoUpdateAuditDTO;
import com.example.attendance_system.dto.EmployeeInfoUpdateDTO;
import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.dto.LoginDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import com.example.attendance_system.repository.EmployeeInfoUpdateRequestRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeInfoUpdateRequestRepository employeeInfoUpdateRequestRepository;

    @Override
    @Transactional
    public Employee registerEmployee(EmployeeRegistrationDTO registrationDTO) {
        validateEmployeeData(registrationDTO);
        
        Employee employee = new Employee();
        
        BeanUtils.copyProperties(registrationDTO, employee);
        
        employee.setEmployeeNo(generateEmployeeNo(registrationDTO.getHireDate()));
        
        employee.setStatus(1);
        
        employee.setIsAdmin(false);
        
        return employeeRepository.save(employee);
    }
    
    @Override
    public Employee login(LoginDTO loginDTO) {
        if (loginDTO.getEmployeeNo() == null || loginDTO.getEmployeeNo().trim().isEmpty()) {
            throw new IllegalArgumentException("员工编号不能为空");
        }
        
        if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        Employee employee = employeeRepository.findByEmployeeNo(loginDTO.getEmployeeNo());
        
        if (employee == null) {
            throw new IllegalArgumentException("员工编号不存在");
        }
        
        if (!employee.getPassword().equals(loginDTO.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        if (employee.getStatus() != 1) {
            throw new IllegalArgumentException("该员工已离职，无法登录");
        }
        
        return employee;
    }
    
    @Override
    public Employee getEmployeeInfo(String employeeNo) {
        // 根据员工编号查询员工信息
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo);
        // 如果员工不存在，抛出异常
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        // 返回员工信息
        return employee;
    }
    
    @Override
    @Transactional
    public EmployeeInfoUpdateRequest submitInfoUpdateRequest(EmployeeInfoUpdateDTO updateDTO) {
        // 检查员工是否存在
        Employee employee = employeeRepository.findByEmployeeNo(updateDTO.getEmployeeNo());
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        // 检查是否有未审核的请求
        // 查询该员工所有状态为待审核(0)的请求，按创建时间降序排列
        List<EmployeeInfoUpdateRequest> pendingRequests = 
                employeeInfoUpdateRequestRepository.findByEmployeeNoAndStatusOrderByCreatedTimeDesc(
                        updateDTO.getEmployeeNo(), 0);
        
        // 如果存在未审核的请求，不允许提交新的请求
        if (!pendingRequests.isEmpty()) {
            throw new IllegalArgumentException("您有待审核的信息更新请求，请等待审核完成后再提交");
        }
        
        // 创建信息更新请求对象
        EmployeeInfoUpdateRequest request = new EmployeeInfoUpdateRequest();
        // 将DTO中的属性复制到请求对象中
        BeanUtils.copyProperties(updateDTO, request);
        
        // 设置状态为待审核(0)
        request.setStatus(0);
        
        // 保存请求到数据库并返回
        return employeeInfoUpdateRequestRepository.save(request);
    }
    
    @Override
    public List<EmployeeInfoUpdateRequest> getInfoUpdateRequests(String employeeNo) {
        // 查询指定员工的所有信息更新请求，按创建时间降序排列
        return employeeInfoUpdateRequestRepository.findByEmployeeNoOrderByCreatedTimeDesc(employeeNo);
    }
    
    @Override
    public List<EmployeeInfoUpdateRequest> getPendingInfoUpdateRequests() {
        // 查询所有待审核(0)的信息更新请求，按创建时间升序排列（先提交先审核）
        return employeeInfoUpdateRequestRepository.findByStatusOrderByCreatedTimeAsc(0);
    }
    
    @Override
    @Transactional
    public EmployeeInfoUpdateRequest auditInfoUpdateRequest(EmployeeInfoUpdateAuditDTO auditDTO) {
        // 检查请求是否存在，不存在则抛出异常
        EmployeeInfoUpdateRequest request = employeeInfoUpdateRequestRepository.findById(auditDTO.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("更新请求不存在"));
        
        // 检查请求状态，如果不是待审核状态(0)，则抛出异常
        if (request.getStatus() != 0) {
            throw new IllegalArgumentException("该请求已经被审核过了");
        }
        
        // 更新请求状态为审核结果（1-通过，2-拒绝）在玩
        request.setStatus(auditDTO.getStatus());
        // 设置管理员审核意见
        request.setAdminComment(auditDTO.getAdminComment());
        // 设置审核时间为当前时间
        request.setAuditTime(LocalDateTime.now());
        
        // 如果审核通过(1)，则更新员工信息
        if (auditDTO.getStatus() == 1) {
            // 查询员工信息
            Employee employee = employeeRepository.findByEmployeeNo(request.getEmployeeNo());
            if (employee == null) {
                throw new IllegalArgumentException("员工不存在");
            }
            
            // 更新员工信息，只更新请求中非空的字段
            if (request.getName() != null) {
                employee.setName(request.getName());
            }
            
            if (request.getGender() != null) {
                employee.setGender(request.getGender());
            }
            
            if (request.getPhoneNumber() != null) {
                employee.setPhoneNumber(request.getPhoneNumber());
            }
            
            if (request.getEmail() != null) {
                employee.setEmail(request.getEmail());
            }
            
            if (request.getDepartmentId() != null) {
                employee.setDepartmentId(request.getDepartmentId());
            }
            
            if (request.getPosition() != null) {
                employee.setPosition(request.getPosition());
            }
            
            // 保存更新后的员工信息
            employeeRepository.save(employee);
        }
        
        // 保存更新后的请求信息并返回
        return employeeInfoUpdateRequestRepository.save(request);
    }
    private void validateEmployeeData(EmployeeRegistrationDTO registrationDTO) {
        if (registrationDTO.getName() == null || registrationDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("员工姓名不能为空");
        }
        
        if (registrationDTO.getGender() == null) {
            throw new IllegalArgumentException("性别不能为空");
        }
        
        if (registrationDTO.getHireDate() == null) {
            throw new IllegalArgumentException("入职日期不能为空");
        }
        
        if (registrationDTO.getPassword() == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (registrationDTO.getPhoneNumber() != null && !registrationDTO.getPhoneNumber().trim().isEmpty() &&
                employeeRepository.existsByPhoneNumber(registrationDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("手机号已被使用");
        }
        
        if (registrationDTO.getEmail() != null && !registrationDTO.getEmail().trim().isEmpty() &&
                employeeRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
    }
    
    private String generateEmployeeNo(LocalDate hireDate) {
        int month = hireDate.getMonthValue();
        int year = hireDate.getYear() % 100;
        
        Random random = new Random();
        int randomNumber = random.nextInt(10000);
        
        return String.format("%02d%02d%04d", month, year, randomNumber);
    }
} 