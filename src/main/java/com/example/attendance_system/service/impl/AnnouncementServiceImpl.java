package com.example.attendance_system.service.impl;

import com.example.attendance_system.dto.*;
import com.example.attendance_system.entity.Announcement;
import com.example.attendance_system.entity.AnnouncementReadLog;
import com.example.attendance_system.entity.Department;
import com.example.attendance_system.entity.Employee;
import com.example.attendance_system.repository.AnnouncementReadLogRepository;
import com.example.attendance_system.repository.AnnouncementRepository;
import com.example.attendance_system.repository.DepartmentRepository;
import com.example.attendance_system.repository.EmployeeRepository;
import com.example.attendance_system.service.AnnouncementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementReadLogRepository readLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<AnnouncementDTO> getAnnouncementPage(AnnouncementPageQueryDTO queryDTO) {
        // 构建查询条件
        Specification<Announcement> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 标题模糊查询
            if (StringUtils.hasText(queryDTO.getTitle())) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + queryDTO.getTitle() + "%"));
            }

            // 状态查询
            if (StringUtils.hasText(queryDTO.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
            }

            // 发布时间范围查询
            if (StringUtils.hasText(queryDTO.getStartDate())) {
                LocalDateTime startDateTime = LocalDate.parse(queryDTO.getStartDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishTime"), startDateTime));
            }

            if (StringUtils.hasText(queryDTO.getEndDate())) {
                LocalDateTime endDateTime = LocalDate.parse(queryDTO.getEndDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("publishTime"), endDateTime));
            }

            // 未删除
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 分页查询
        PageRequest pageRequest = PageRequest.of(
                queryDTO.getCurrent() - 1,
                queryDTO.getSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );

        Page<Announcement> announcementPage = announcementRepository.findAll(spec, pageRequest);

        // 转换为DTO
        List<AnnouncementDTO> announcementDTOList = announcementPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(announcementDTOList, pageRequest, announcementPage.getTotalElements());
    }

    @Override
    @Transactional
    public Long createAnnouncement(AnnouncementCreateDTO createDTO) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(createDTO, announcement);

        // 处理部门列表
        if (createDTO.getTargetScope().equals("partial") && createDTO.getDepartments() != null) {
            try {
                announcement.setDepartments(objectMapper.writeValueAsString(createDTO.getDepartments()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("部门列表序列化失败", e);
            }
        }

        // 处理发布状态
        if (createDTO.getPublishType().equals("immediate")) {
            announcement.setPublishTime(LocalDateTime.now());
            announcement.setStatus("published");
        } else {
            announcement.setStatus("draft");
        }

        // 未删除
        announcement.setIsDeleted(0);

        Announcement saved = announcementRepository.save(announcement);
        return saved.getId();
    }

    @Override
    @Transactional
    public boolean updateAnnouncement(AnnouncementUpdateDTO updateDTO) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(updateDTO.getId());
        if (optionalAnnouncement.isEmpty()) {
            return false;
        }

        Announcement announcement = optionalAnnouncement.get();
        BeanUtils.copyProperties(updateDTO, announcement);

        // 处理部门列表
        if (updateDTO.getTargetScope().equals("partial") && updateDTO.getDepartments() != null) {
            try {
                announcement.setDepartments(objectMapper.writeValueAsString(updateDTO.getDepartments()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("部门列表序列化失败", e);
            }
        }

        // 处理发布状态
        if (updateDTO.getPublishType().equals("immediate")) {
            announcement.setPublishTime(LocalDateTime.now());
            announcement.setStatus("published");
        } else if (updateDTO.getPublishTime() != null && updateDTO.getPublishTime().isBefore(LocalDateTime.now())) {
            announcement.setStatus("published");
        }

        announcementRepository.save(announcement);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteAnnouncement(Long id) {
        int rows = announcementRepository.logicalDeleteById(id);
        return rows > 0;
    }

    @Override
    public AnnouncementDTO getAnnouncementDetail(Long id) {
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(id);
        if (optionalAnnouncement.isEmpty()) {
            return null;
        }

        return convertToDTO(optionalAnnouncement.get());
    }

    @Override
    public List<AnnouncementReadStatusDTO> getReadStatus(Long announcementId, String status) {
        // 查询该公告的所有已读记录
        List<AnnouncementReadLog> readLogs = readLogRepository.findByAnnouncementId(announcementId);
        Set<String> readEmployeeNos = readLogs.stream()
                .map(AnnouncementReadLog::getEmployeeNo)
                .collect(Collectors.toSet());

        List<AnnouncementReadStatusDTO> result = new ArrayList<>();

        // 如果查询已读状态
        if ("read".equals(status) || status == null) {
            result.addAll(readLogs.stream().map(log -> {
                AnnouncementReadStatusDTO dto = new AnnouncementReadStatusDTO();
                dto.setEmployeeNo(log.getEmployeeNo());
                dto.setEmployeeName(log.getEmployeeName());
                dto.setDepartmentId(log.getDepartmentId());
                dto.setDepartmentName(log.getDepartmentName());
                dto.setStatus("read");
                dto.setReadTime(log.getReadTime());
                return dto;
            }).collect(Collectors.toList()));
        }

        // 如果查询未读状态
        if ("unread".equals(status) || status == null) {
            // 查询公告目标范围内的所有员工
            Optional<Announcement> optionalAnnouncement = announcementRepository.findById(announcementId);
            if (optionalAnnouncement.isEmpty()) {
                return result;
            }

            Announcement announcement = optionalAnnouncement.get();
            List<Employee> targetEmployees = new ArrayList<>();

            if ("all".equals(announcement.getTargetScope())) {
                // 全体员工
                targetEmployees = employeeRepository.findAll();
            } else if ("partial".equals(announcement.getTargetScope())) {
                // 指定部门的员工
                try {
                    List<String> departmentIds = objectMapper.readValue(
                            announcement.getDepartments(),
                            new TypeReference<List<String>>() {
                            }
                    );
                    for (String deptId : departmentIds) {
                        Page<Employee> deptEmployeesPage = employeeRepository.findByDepartmentId(
                                Long.parseLong(deptId),
                                PageRequest.of(0, Integer.MAX_VALUE));
                        targetEmployees.addAll(deptEmployeesPage.getContent());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("解析部门ID列表失败", e);
                }
            }

            // 过滤出未读的员工
            List<AnnouncementReadStatusDTO> unreadList = targetEmployees.stream()
                    .filter(employee -> !readEmployeeNos.contains(employee.getEmployeeNo()))
                    .map(employee -> {
                        AnnouncementReadStatusDTO dto = new AnnouncementReadStatusDTO();
                        dto.setEmployeeNo(employee.getEmployeeNo());
                        dto.setEmployeeName(employee.getName());
                        dto.setDepartmentId(employee.getDepartmentId());

                        Optional<Department> department = departmentRepository.findById(employee.getDepartmentId());
                        department.ifPresent(d -> dto.setDepartmentName(d.getName()));

                        dto.setStatus("unread");
                        return dto;
                    }).collect(Collectors.toList());

            result.addAll(unreadList);
        }

        return result;
    }

    @Override
    public Page<AnnouncementDTO> getEmployeeAnnouncements(AnnouncementEmployeeQueryDTO queryDTO) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 查询当前有效的公告
        Specification<Announcement> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 有效期内
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("validFrom"), now));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("validTo"), now));

            // 已发布
            predicates.add(criteriaBuilder.equal(root.get("status"), "published"));

            // 未删除
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 分页查询
        PageRequest pageRequest = PageRequest.of(
                queryDTO.getCurrent() - 1,
                queryDTO.getSize(),
                Sort.by(Sort.Direction.DESC, "createdTime")
        );

        Page<Announcement> announcementPage = announcementRepository.findAll(spec, pageRequest);

        // 查询员工信息，用于筛选可见公告
        Employee employee = employeeRepository.findByEmployeeNo(queryDTO.getEmployeeNo());
        if (employee == null) {
            return Page.empty();
        }

        // 查询员工已读公告ID列表
        List<Long> readAnnouncementIds = readLogRepository.findAnnouncementIdsByEmployeeNo(queryDTO.getEmployeeNo());
        Set<Long> readAnnouncementIdSet = new HashSet<>(readAnnouncementIds);

        // 过滤出员工可见的公告，并转换为DTO
        List<AnnouncementDTO> announcementDTOList = announcementPage.getContent().stream()
                .filter(announcement -> {
                    // 全体可见
                    if ("all".equals(announcement.getTargetScope())) {
                        return true;
                    }

                    // 指定部门可见
                    if ("partial".equals(announcement.getTargetScope())) {
                        try {
                            List<String> departmentIds = objectMapper.readValue(
                                    announcement.getDepartments(),
                                    new TypeReference<List<String>>() {
                                    }
                            );
                            return departmentIds.contains(employee.getDepartmentId().toString());
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    return false;
                })
                .map(announcement -> {
                    AnnouncementDTO dto = convertToDTO(announcement);

                    // 设置是否已读
                    dto.setIsRead(readAnnouncementIdSet.contains(announcement.getId()));

                    // 设置摘要
                    if (announcement.getContent() != null) {
                        String content = announcement.getContent();
                        // 去除HTML标签
                        content = content.replaceAll("<[^>]*>", "");
                        dto.setSummary(content.length() > 50 ? content.substring(0, 50) + "..." : content);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(announcementDTOList, pageRequest, announcementPage.getTotalElements());
    }

    @Override
    @Transactional
    public AnnouncementDTO readAnnouncement(AnnouncementEmployeeReadDTO readDTO) {
        // 查询公告
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(readDTO.getAnnouncementId());
        if (optionalAnnouncement.isEmpty()) {
            return null;
        }

        Announcement announcement = optionalAnnouncement.get();

        // 检查是否已读
        Optional<AnnouncementReadLog> existingLog = readLogRepository.findByAnnouncementIdAndEmployeeNo(
                readDTO.getAnnouncementId(), readDTO.getEmployeeNo());

        if (existingLog.isEmpty()) {
            // 创建阅读记录
            Employee employee = employeeRepository.findByEmployeeNo(readDTO.getEmployeeNo());
            if (employee != null) {
                AnnouncementReadLog readLog = new AnnouncementReadLog();
                readLog.setAnnouncementId(readDTO.getAnnouncementId());
                readLog.setEmployeeNo(readDTO.getEmployeeNo());
                readLog.setEmployeeName(employee.getName());
                readLog.setDepartmentId(employee.getDepartmentId());

                Optional<Department> department = departmentRepository.findById(employee.getDepartmentId());
                department.ifPresent(d -> readLog.setDepartmentName(d.getName()));

                readLogRepository.save(readLog);
            }
        }

        AnnouncementDTO dto = convertToDTO(announcement);
        dto.setIsRead(true);
        return dto;
    }

    /**
     * 将公告实体转换为DTO
     */
    private AnnouncementDTO convertToDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        BeanUtils.copyProperties(announcement, dto);

        // 处理部门列表
        if ("partial".equals(announcement.getTargetScope()) && StringUtils.hasText(announcement.getDepartments())) {
            try {
                List<String> departmentIds = objectMapper.readValue(
                        announcement.getDepartments(),
                        new TypeReference<List<String>>() {
                        }
                );
                dto.setDepartments(departmentIds);

                // 查询部门名称
                List<String> departmentNames = new ArrayList<>();
                for (String deptId : departmentIds) {
                    Optional<Department> department = departmentRepository.findById(Long.parseLong(deptId));
                    department.ifPresent(d -> departmentNames.add(d.getName()));
                }
                dto.setDepartmentNames(departmentNames);
            } catch (Exception e) {
                // 解析失败，使用空列表
                dto.setDepartments(new ArrayList<>());
                dto.setDepartmentNames(new ArrayList<>());
            }
        }

        return dto;
    }
} 