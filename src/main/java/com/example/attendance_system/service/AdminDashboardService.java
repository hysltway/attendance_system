package com.example.attendance_system.service;

import com.example.attendance_system.dto.AdminDashboardDTO;

/**
 * 管理员仪表盘服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取管理员仪表盘数据
     *
     * @param timeGranularity 时间粒度：1-日, 2-周, 3-月，默认为1
     * @return 管理员仪表盘数据
     */
    AdminDashboardDTO getDashboardData(Integer timeGranularity);
} 