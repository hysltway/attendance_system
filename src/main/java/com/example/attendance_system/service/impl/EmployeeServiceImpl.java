package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.EmployeeRegistrationDTO;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

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