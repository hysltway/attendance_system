package com.example.attendance_system.controller;

import com.example.attendance_system.dto.*;
import com.example.attendance_system.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员端公告管理接口
 */
@RestController
@RequestMapping("/admin/announcement")
public class AdminAnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 分页查询公告列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(AnnouncementPageQueryDTO queryDTO) {
        Page<AnnouncementDTO> page = announcementService.getAnnouncementPage(queryDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("pages", page.getTotalPages());
        result.put("current", queryDTO.getCurrent());
        result.put("size", queryDTO.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 创建公告
     * @param createDTO 创建参数
     * @return 公告ID
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody AnnouncementCreateDTO createDTO) {
        Long announcementId = announcementService.createAnnouncement(createDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", announcementId);
        result.put("success", true);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 更新公告
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody AnnouncementUpdateDTO updateDTO) {
        boolean success = announcementService.updateAnnouncement(updateDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 删除公告
     * @param id 公告ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam("id") Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查看公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/detail")
    public ResponseEntity<AnnouncementDTO> detail(@RequestParam("id") Long id) {
        AnnouncementDTO detail = announcementService.getAnnouncementDetail(id);
        return ResponseEntity.ok(detail);
    }
    
    /**
     * 查看公告阅读情况
     * @param announcementId 公告ID
     * @param status 阅读状态（read/unread）
     * @return 阅读状态列表
     */
    @GetMapping("/read-status")
    public ResponseEntity<List<AnnouncementReadStatusDTO>> readStatus(
            @RequestParam("announcementId") Long announcementId,
            @RequestParam(value = "status", required = false) String status) {
        List<AnnouncementReadStatusDTO> statusList = announcementService.getReadStatus(announcementId, status);
        return ResponseEntity.ok(statusList);
    }
} 