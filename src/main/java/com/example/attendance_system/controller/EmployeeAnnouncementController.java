package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AnnouncementDTO;
import com.example.attendance_system.dto.AnnouncementEmployeeQueryDTO;
import com.example.attendance_system.dto.AnnouncementEmployeeReadDTO;
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
import java.util.Map;

/**
 * 员工端公告接口
 */
@RestController
@RequestMapping("/employee/announcement")
@Tag(name = "员工公告", description = "员工端公告查看与阅读相关接口")
public class EmployeeAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 查询可见公告列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "查询可见公告列表", description = "获取当前员工可见的公告列表，支持分页查询")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(description = "查询参数（分页信息等）")
            AnnouncementEmployeeQueryDTO queryDTO) {
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
     *
     * @param readDTO 阅读参数
     * @return 公告详情
     */
    @PostMapping("/read")
    @Operation(summary = "查看公告详情并标记已读", description = "获取公告详情并同时将其标记为已读状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnnouncementDTO.class)))
    })
    public ResponseEntity<AnnouncementDTO> read(
            @Parameter(description = "公告阅读参数，包含公告ID和员工ID", required = true)
            @RequestBody AnnouncementEmployeeReadDTO readDTO) {
        AnnouncementDTO detail = announcementService.readAnnouncement(readDTO);
        return ResponseEntity.ok(detail);
    }
} 