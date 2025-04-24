package com.example.attendance_system.controller;

import com.example.attendance_system.blockchain.Block;
import com.example.attendance_system.blockchain.Blockchain;
import com.example.attendance_system.service.BlockchainService;
import com.example.attendance_system.service.impl.AttendanceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块链控制器
 * 提供区块链状态查询和管理API
 */
@Slf4j
@RestController
@RequestMapping("/blockchain")
@CrossOrigin
@Tag(name = "区块链管理", description = "提供区块链状态查询和管理功能")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private AttendanceServiceImpl attendanceService;
    
    /**
     * 获取区块链状态
     * @return 区块链状态信息
     */
    @Operation(summary = "获取区块链状态", description = "查询区块链的基本状态信息，包括区块总数、是否有效等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<?> getBlockchainStatus() {
        try {
            Blockchain blockchain = blockchainService.getBlockchain();
            boolean isValid = blockchainService.verifyBlockchain();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalBlocks", blockchain.getChain().size());
            response.put("isValid", isValid);
            response.put("latestBlockHash", blockchain.getLatestBlock().getHash());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取区块链状态失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取区块链状态失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取所有区块
     * @return 区块链中的所有区块
     */
    @Operation(summary = "获取所有区块", description = "返回区块链中的所有区块")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/blocks")
    public ResponseEntity<?> getAllBlocks() {
        try {
            Blockchain blockchain = blockchainService.getBlockchain();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("blocks", blockchain.getChain());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取区块链所有区块失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取区块链所有区块失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据索引获取区块
     * @param index 区块索引
     * @return 对应索引的区块
     */
    @Operation(summary = "根据索引获取区块", description = "返回区块链中指定索引的区块")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "400", description = "参数错误，如索引超出范围",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/block/{index}")
    public ResponseEntity<?> getBlockByIndex(
            @Parameter(description = "区块索引", required = true)
            @PathVariable int index) {
        try {
            Blockchain blockchain = blockchainService.getBlockchain();
            
            if (index < 0 || index >= blockchain.getChain().size()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "索引超出范围，有效范围: 0 - " + (blockchain.getChain().size() - 1));
                
                return ResponseEntity.badRequest().body(response);
            }
            
            Block block = blockchain.getChain().get(index);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("block", block);
            response.put("index", index);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取区块失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取区块失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 上传历史考勤记录到区块链
     * @param batchSize 每批次处理的记录数
     * @return 上传结果
     */
    @Operation(summary = "上传历史考勤记录到区块链", description = "将历史考勤记录批量添加到区块链中")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping("/upload-history")
    public ResponseEntity<?> uploadHistoricalRecords(
            @Parameter(description = "每批次处理的记录数", required = false)
            @RequestParam(required = false, defaultValue = "50") int batchSize) {
        try {
            int totalRecords = attendanceService.uploadHistoricalAttendanceRecordsToBlockchain(batchSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "历史考勤记录上传到区块链成功");
            response.put("totalRecords", totalRecords);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("上传历史考勤记录到区块链失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "上传历史考勤记录到区块链失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 验证区块链有效性
     * @return 验证结果
     */
    @Operation(summary = "验证区块链有效性", description = "检查区块链中的所有区块是否有效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证完成",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/verify")
    public ResponseEntity<?> verifyBlockchain() {
        try {
            boolean isValid = blockchainService.verifyBlockchain();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isValid", isValid);
            response.put("message", isValid ? "区块链验证通过" : "区块链验证失败，数据可能被篡改");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("验证区块链失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "验证区块链失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 