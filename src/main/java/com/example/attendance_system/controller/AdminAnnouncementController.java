package com.example.attendance_system.controller;

import com.example.attendance_system.dto.*;
import com.example.attendance_system.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理员公告管理", description = "提供管理员公告的增删改查、状态查看等功能")
public class AdminAnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 分页查询公告列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询公告列表", description = "管理员查询公告列表，支持标题模糊搜索、状态筛选和时间范围筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(description = "查询参数（标题关键词、状态、时间范围、分页信息等）")
            AnnouncementPageQueryDTO queryDTO) {
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
    @Operation(summary = "创建公告", description = "创建一条新公告，支持定时发布和指定部门可见")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> create(
            @Parameter(description = "公告创建参数", required = true)
            @RequestBody AnnouncementCreateDTO createDTO) {
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
    @Operation(summary = "更新公告", description = "更新现有公告的内容、发布范围、有效期等信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "公告更新参数", required = true)
            @RequestBody AnnouncementUpdateDTO updateDTO) {
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
    @Operation(summary = "删除公告", description = "根据ID删除公告")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> delete(
            @Parameter(description = "公告ID", required = true, example = "1001")
            @RequestParam("id") Long id) {
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
    @Operation(summary = "查看公告详情", description = "根据ID获取公告详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AnnouncementDTO.class)))
    })
    public ResponseEntity<AnnouncementDTO> detail(
            @Parameter(description = "公告ID", required = true, example = "1001")
            @RequestParam("id") Long id) {
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
    @Operation(summary = "查看公告阅读情况", description = "查看指定公告的员工阅读状态列表，可按已读/未读筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<AnnouncementReadStatusDTO>> readStatus(
            @Parameter(description = "公告ID", required = true, example = "1001")
            @RequestParam("announcementId") Long announcementId,
            
            @Parameter(description = "阅读状态：read=已读，unread=未读，不传则查询全部", example = "read")
            @RequestParam(value = "status", required = false) String status) {
        List<AnnouncementReadStatusDTO> statusList = announcementService.getReadStatus(announcementId, status);
        return ResponseEntity.ok(statusList);
    }
} 