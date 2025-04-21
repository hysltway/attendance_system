package com.example.attendance_system.dto;

import lombok.Data;

/**
 * 员工查询公告列表DTO
 */
@Data
public class AnnouncementEmployeeQueryDTO {
    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页记录数量
     */
    private Integer size = 10;
} 