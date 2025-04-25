package com.example.attendance_system.controller;

import com.example.attendance_system.blockchain.Blockchain;
import com.example.attendance_system.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员区块链配置控制器
 * 提供区块链参数配置和状态管理的API
 */
@RestController
@RequestMapping("/admin/blockchain")
@CrossOrigin
@Slf4j
@Tag(name = "管理员区块链管理", description = "管理员区块链配置和管理接口，包括查看状态、修改参数、触发上传等操作")
public class AdminBlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    @Value("${blockchain.difficulty:4}")
    private int difficulty;

    @Value("${blockchain.batch-size:20}")
    private int batchSize;

    @Value("${blockchain.file.path:blockchain.json}")
    private String blockchainFilePath;

    /**
     * 获取区块链状态和配置信息
     *
     * @return 区块链状态信息
     */
    @Operation(summary = "获取区块链状态和配置", description = "管理员获取区块链的当前状态、配置参数和信息统计")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
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
            response.put("difficulty", blockchain.getDifficulty());
            response.put("batchSize", batchSize);
            response.put("filePath", blockchainFilePath);

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
     * 修改区块链挖矿难度
     *
     * @param difficulty 新的难度值
     * @return 修改结果
     */
    @Operation(summary = "修改区块链难度", description = "修改区块链的挖矿难度，用于调整区块生成的速度和安全性")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/difficulty")
    public ResponseEntity<?> updateDifficulty(
            @Parameter(description = "新的难度值（1-6，数值越大计算难度越高）", required = true)
            @RequestParam int difficulty) {
        try {
            if (difficulty < 1 || difficulty > 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "难度值必须在1-6之间"
                ));
            }

            Blockchain blockchain = blockchainService.getBlockchain();
            blockchain.setDifficulty(difficulty);
            blockchainService.saveBlockchain();

            log.info("区块链难度已修改为: {}", difficulty);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "区块链难度已修改为: " + difficulty
            ));
        } catch (Exception e) {
            log.error("修改区块链难度失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "修改区块链难度失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 修改区块链记录批次大小
     *
     * @param batchSize 新的批次大小
     * @return 修改结果
     */
    @Operation(summary = "修改批次大小", description = "修改区块链记录批量处理的大小，用于优化性能")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/batch-size")
    public ResponseEntity<?> updateBatchSize(
            @Parameter(description = "新的批次大小（5-100）", required = true)
            @RequestParam int batchSize) {
        try {
            if (batchSize < 5 || batchSize > 100) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "批次大小必须在5-100之间"
                ));
            }

            this.batchSize = batchSize;
            // 注意：实际生产环境中，你可能需要将此值持久化到配置文件或数据库中

            log.info("区块链批次大小已修改为: {}", batchSize);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "区块链批次大小已修改为: " + batchSize
            ));
        } catch (Exception e) {
            log.error("修改区块链批次大小失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "修改区块链批次大小失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 手动触发历史考勤记录上传到区块链
     *
     * @param days      要上传的天数
     * @param batchSize 批次大小
     * @return 上传结果
     */
    @Operation(summary = "手动上传历史记录", description = "手动触发将历史考勤记录上传到区块链")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadHistoricalRecords(
            @Parameter(description = "要上传的天数（1-30）", required = true)
            @RequestParam(defaultValue = "1") int days,

            @Parameter(description = "批次大小（可选）")
            @RequestParam(required = false) Integer batchSize) {
        try {
            if (days < 1 || days > 30) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "上传天数必须在1-30之间"
                ));
            }

            int actualBatchSize = batchSize != null ? batchSize : this.batchSize;
            if (actualBatchSize < 5 || actualBatchSize > 100) {
                actualBatchSize = 20; // 使用默认值
            }

            int totalRecords = blockchainService.uploadHistoricalRecords(actualBatchSize);

            log.info("手动上传历史考勤记录完成，共上传{}条记录", totalRecords);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("历史考勤记录上传完成，共上传%d条记录", totalRecords),
                    "totalRecords", totalRecords
            ));
        } catch (Exception e) {
            log.error("上传历史考勤记录失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "上传历史考勤记录失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 验证区块链完整性
     *
     * @return 验证结果
     */
    @Operation(summary = "验证区块链完整性", description = "验证区块链的完整性和有效性")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证成功"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/verify")
    public ResponseEntity<?> verifyBlockchain() {
        try {
            boolean isValid = blockchainService.verifyBlockchain();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isValid", isValid);
            response.put("message", isValid ? "区块链验证通过" : "区块链验证失败，数据可能已被篡改");

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