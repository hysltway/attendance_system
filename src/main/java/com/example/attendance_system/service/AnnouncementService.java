package com.example.attendance_system.service;

import com.example.attendance_system.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    
    /**
     * 分页查询公告列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<AnnouncementDTO> getAnnouncementPage(AnnouncementPageQueryDTO queryDTO);
    
    /**
     * 创建公告
     * @param createDTO 创建参数
     * @return 公告ID
     */
    Long createAnnouncement(AnnouncementCreateDTO createDTO);
    
    /**
     * 更新公告
     * @param updateDTO 更新参数
     * @return 是否成功
     */
    boolean updateAnnouncement(AnnouncementUpdateDTO updateDTO);
    
    /**
     * 删除公告
     * @param id 公告ID
     * @return 是否成功
     */
    boolean deleteAnnouncement(Long id);
    
    /**
     * 获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    AnnouncementDTO getAnnouncementDetail(Long id);
    
    /**
     * 查询公告阅读状态
     * @param announcementId 公告ID
     * @param status 阅读状态
     * @return 阅读状态列表
     */
    List<AnnouncementReadStatusDTO> getReadStatus(Long announcementId, String status);
    
    /**
     * 员工查询可见公告列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<AnnouncementDTO> getEmployeeAnnouncements(AnnouncementEmployeeQueryDTO queryDTO);
    
    /**
     * 员工阅读公告
     * @param readDTO 阅读参数
     * @return 公告详情
     */
    AnnouncementDTO readAnnouncement(AnnouncementEmployeeReadDTO readDTO);
} 