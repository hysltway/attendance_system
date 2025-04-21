package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.*;
import com.example.attendance_system.entity.Department;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import com.example.attendance_system.repository.DepartmentRepository;
import com.example.attendance_system.repository.EmployeeInfoUpdateRequestRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeInfoUpdateRequestRepository employeeInfoUpdateRequestRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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

        // 验证手机号唯一性（如果提供了新的手机号且与当前不同）
        if (updateDTO.getPhoneNumber() != null
                && !updateDTO.getPhoneNumber().trim().isEmpty()
                && !updateDTO.getPhoneNumber().equals(employee.getPhoneNumber())) {
            if (employeeRepository.existsByPhoneNumber(updateDTO.getPhoneNumber())) {
                throw new IllegalArgumentException("该手机号已被其他员工使用");
            }
        }

        // 验证邮箱唯一性（如果提供了新的邮箱且与当前不同）
        if (updateDTO.getEmail() != null
                && !updateDTO.getEmail().trim().isEmpty()
                && !updateDTO.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(updateDTO.getEmail())) {
                throw new IllegalArgumentException("该邮箱已被其他员工使用");
            }
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
    public EmployeeInfoUpdatePageDTO getPendingInfoUpdateRequestsExcludeEmployeePage(String excludeEmployeeNo, String employeeNo, String name, Integer current, Integer size) {
        if (excludeEmployeeNo == null || excludeEmployeeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("排除的员工编号不能为空");
        }

        // 参数校验
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        // 创建分页参数，注意：JPA分页从0开始计数
        Pageable pageable = PageRequest.of(current - 1, size);

        // 根据条件查询
        Page<EmployeeInfoUpdateRequest> page;

        if (employeeNo != null && !employeeNo.trim().isEmpty()) {
            // 如果指定了员工编号，按员工编号查询
            page = employeeInfoUpdateRequestRepository.findByStatusAndEmployeeNoAndEmployeeNoNotOrderByCreatedTimeAsc(0, employeeNo, excludeEmployeeNo, pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            // 如果指定了员工姓名，按员工姓名查询
            page = employeeInfoUpdateRequestRepository.findByStatusAndNameContainingAndEmployeeNoNotOrderByCreatedTimeAsc(0, name, excludeEmployeeNo, pageable);
        } else {
            // 查询所有待审核(0)的信息更新请求，排除指定员工，按创建时间升序排列
            page = employeeInfoUpdateRequestRepository.findByStatusAndEmployeeNoNotOrderByCreatedTimeAsc(0, excludeEmployeeNo, pageable);
        }

        // 构建分页结果
        EmployeeInfoUpdatePageDTO result = new EmployeeInfoUpdatePageDTO();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(page.getTotalElements());
        result.setPages(page.getTotalPages());
        result.setRecords(page.getContent());

        return result;
    }

    @Override
    @Transactional
    public EmployeeInfoUpdateRequest auditInfoUpdateRequest(EmployeeInfoUpdateAuditDTO auditDTO) {
        // 查询请求信息
        EmployeeInfoUpdateRequest request = employeeInfoUpdateRequestRepository.findById(auditDTO.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("信息更新请求不存在"));

        // 验证请求状态必须为待审核(0)
        if (request.getStatus() != 0) {
            throw new IllegalStateException("该请求已经审核过，无法再次审核");
        }

        // 验证审核状态是否合法
        if (auditDTO.getStatus() != 1 && auditDTO.getStatus() != 2) {
            throw new IllegalArgumentException("审核状态不合法，只能是1（通过）或2（拒绝）");
        }

        // 如果审核人是请求的申请人，则不允许审核
        if (request.getEmployeeNo().equals(auditDTO.getAdminNo())) {
            throw new IllegalStateException("管理员不能审核自己的信息更新申请");
        }

        // 更新审核状态和意见
        request.setStatus(auditDTO.getStatus());
        request.setAdminComment(auditDTO.getAdminComment());
        request.setAuditTime(LocalDateTime.now());

        if (auditDTO.getStatus() == 1) {
            // 查询员工信息
            Employee employee = employeeRepository.findByEmployeeNo(request.getEmployeeNo());
            if (employee == null) {
                throw new IllegalArgumentException("员工不存在");
            }

            // 更新员工信息，只更新允许的字段（name, phoneNumber, email）
            if (request.getName() != null) {
                employee.setName(request.getName());
            }

            if (request.getPhoneNumber() != null) {
                // 再次验证手机号唯一性
                if (!request.getPhoneNumber().equals(employee.getPhoneNumber()) &&
                        employeeRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    throw new IllegalArgumentException("该手机号已被其他员工使用");
                }
                employee.setPhoneNumber(request.getPhoneNumber());
            }

            if (request.getEmail() != null) {
                // 再次验证邮箱唯一性
                if (!request.getEmail().equals(employee.getEmail()) &&
                        employeeRepository.existsByEmail(request.getEmail())) {
                    throw new IllegalArgumentException("该邮箱已被其他员工使用");
                }
                employee.setEmail(request.getEmail());
            }

            // 保存更新后的员工信息
            employeeRepository.save(employee);
        }

        // 保存更新后的请求信息并返回
        return employeeInfoUpdateRequestRepository.save(request);
    }

    @Override
    public List<EmployeeInfoUpdateRequest> getPendingInfoUpdateRequestsExcludeEmployee(String excludeEmployeeNo, String employeeNo, String name) {
        if (excludeEmployeeNo == null || excludeEmployeeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("排除的员工编号不能为空");
        }

        // 根据条件查询
        if (employeeNo != null && !employeeNo.trim().isEmpty()) {
            // 如果指定了员工编号，按员工编号查询
            return employeeInfoUpdateRequestRepository.findByStatusAndEmployeeNoAndEmployeeNoNotOrderByCreatedTimeAsc(0, employeeNo, excludeEmployeeNo);
        } else if (name != null && !name.trim().isEmpty()) {
            // 如果指定了员工姓名，按员工姓名查询
            return employeeInfoUpdateRequestRepository.findByStatusAndNameContainingAndEmployeeNoNotOrderByCreatedTimeAsc(0, name, excludeEmployeeNo);
        } else {
            // 查询所有待审核(0)的信息更新请求，排除指定员工，按创建时间升序排列（先提交先审核）
            return employeeInfoUpdateRequestRepository.findByStatusAndEmployeeNoNotOrderByCreatedTimeAsc(0, excludeEmployeeNo);
        }
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

    /**
     * 分页查询员工列表
     *
     * @param current    当前页码
     * @param size       每页记录数
     * @param name       员工姓名（模糊匹配）
     * @param department 部门名称（精确或模糊匹配）
     * @return 员工分页列表
     */
    @Override
    public Page<Employee> getEmployeeListPage(Integer current, Integer size, String name, String department) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(current - 1, size);

        // 查询条件
        Page<Employee> employeePage;

        if ((name == null || name.isEmpty()) && (department == null || department.isEmpty())) {
            // 无查询条件，查询所有员工
            employeePage = employeeRepository.findAll(pageable);
        } else if (department == null || department.isEmpty()) {
            // 仅按姓名查询
            employeePage = employeeRepository.findByNameContaining(name, pageable);
        } else if (name == null || name.isEmpty()) {
            // 仅按部门查询
            // 先查询部门信息
            List<Department> departments = departmentRepository.findByNameContaining(department);
            if (departments.isEmpty()) {
                return Page.empty(pageable);
            }
            // 获取部门ID列表
            List<Long> departmentIds = departments.stream()
                    .map(Department::getId)
                    .collect(Collectors.toList());
            // 按部门ID查询员工
            employeePage = employeeRepository.findByDepartmentIdIn(departmentIds, pageable);
        } else {
            // 同时按姓名和部门查询
            // 先查询部门信息
            List<Department> departments = departmentRepository.findByNameContaining(department);
            if (departments.isEmpty()) {
                return Page.empty(pageable);
            }
            // 获取部门ID列表
            List<Long> departmentIds = departments.stream()
                    .map(Department::getId)
                    .collect(Collectors.toList());
            // 按姓名和部门ID查询员工
            employeePage = employeeRepository.findByNameContainingAndDepartmentIdIn(name, departmentIds, pageable);
        }

        // 加载员工关联的部门信息
        employeePage.getContent().forEach(employee -> {
            if (employee.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(employee.getDepartmentId()).orElse(null);
                employee.setDepartment(dept);
            }
        });

        return employeePage;
    }

    /**
     * 更新员工信息
     *
     * @param employee 员工信息
     * @return 更新后的员工信息
     */
    @Override
    public Employee updateEmployee(Employee employee) {
        // 查询员工是否存在
        Employee existingEmployee = employeeRepository.findByEmployeeNo(employee.getEmployeeNo());
        if (existingEmployee == null) {
            throw new IllegalArgumentException("员工不存在");
        }

        // 更新员工基本信息
        existingEmployee.setName(employee.getName());
        existingEmployee.setPhoneNumber(employee.getPhoneNumber());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setPosition(employee.getPosition());
        existingEmployee.setDepartmentId(employee.getDepartmentId());
        existingEmployee.setGender(employee.getGender());
        existingEmployee.setStatus(employee.getStatus());
        existingEmployee.setIsAdmin(employee.getIsAdmin());
        // 密码不为空时才更新密码
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            // 实际项目中应加密存储密码
            existingEmployee.setPassword(employee.getPassword());
        }

        // 保存更新后的员工信息
        return employeeRepository.save(existingEmployee);
    }

    /**
     * 删除员工（逻辑删除）
     *
     * @param employeeId 员工ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteEmployee(String employeeId) {
        try {
            // 解析员工ID
            Long id = Long.parseLong(employeeId);

            // 查询员工是否存在
            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("员工不存在"));

            // 逻辑删除员工（设置状态为离职）
            employee.setStatus(0);
            employeeRepository.save(employee);

            return true;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("员工ID格式不正确");
        }
    }

    /**
     * 创建员工
     *
     * @param employee 员工信息
     * @return 创建后的员工信息
     */
    @Override
    public Employee createEmployee(Employee employee) {
        // 校验必填字段
        if (employee.getName() == null || employee.getName().isEmpty()) {
            throw new IllegalArgumentException("员工姓名不能为空");
        }

        // 校验手机号和邮箱是否已存在
        if (employee.getPhoneNumber() != null && !employee.getPhoneNumber().isEmpty()) {
            if (employeeRepository.existsByPhoneNumber(employee.getPhoneNumber())) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }
        if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
            if (employeeRepository.existsByEmail(employee.getEmail())) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }

        // 生成员工编号
        if (employee.getEmployeeNo() == null || employee.getEmployeeNo().isEmpty()) {
            employee.setEmployeeNo(generateEmployeeNo(LocalDate.now()));
        }

        // 设置默认入职日期（如果未设置）
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }

        // 设置默认状态
        employee.setStatus(1); // 在职

        // 设置默认密码（如果未设置）
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            // 实际项目中应使用加密存储密码，这里仅作示例
            employee.setPassword("123456");
        }

        // 保存员工信息
        return employeeRepository.save(employee);
    }

    /**
     * 根据部门ID查询员工列表
     *
     * @param departmentId 部门ID
     * @param current      当前页码
     * @param size         每页记录数
     * @return 员工分页列表
     */
    @Override
    public Page<Employee> getEmployeesByDepartmentId(Long departmentId, Integer current, Integer size) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(current - 1, size);

        // 查询指定部门的员工
        Page<Employee> employeePage = employeeRepository.findByDepartmentId(departmentId, pageable);

        // 同时加载部门信息
        employeePage.getContent().forEach(employee -> {
            if (employee.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(employee.getDepartmentId()).orElse(null);
                employee.setDepartment(dept);
            }
        });

        return employeePage;
    }
} 