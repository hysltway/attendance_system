package com.example.attendance_system.dto;

import com.example.attendance_system.entity.EmployeeInfoUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 员工信息更新请求分页数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInfoUpdatePageDTO {
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页记录数
     */
    private Integer size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 记录列表
     */
    private List<EmployeeInfoUpdateRequest> records;
} 