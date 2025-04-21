package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AnnouncementDTO;
import com.example.attendance_system.dto.AnnouncementEmployeeQueryDTO;
import com.example.attendance_system.dto.AnnouncementEmployeeReadDTO;
import com.example.attendance_system.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工端公告接口
 */
@RestController
@RequestMapping("/employee/announcement")
public class EmployeeAnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 查询可见公告列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(AnnouncementEmployeeQueryDTO queryDTO) {
        Page<AnnouncementDTO> page = announcementService.getEmployeeAnnouncements(queryDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("pages", page.getTotalPages());
        result.put("current", queryDTO.getCurrent());
        result.put("size", queryDTO.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查看公告详情 + 标记已读
     * @param readDTO 阅读参数
     * @return 公告详情
     */
    @PostMapping("/read")
    public ResponseEntity<AnnouncementDTO> read(@RequestBody AnnouncementEmployeeReadDTO readDTO) {
        AnnouncementDTO detail = announcementService.readAnnouncement(readDTO);
        return ResponseEntity.ok(detail);
    }
} 