package com.example.attendance_system.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 人脸识别工具类，用于与Python人脸识别模块交互
 */
@Slf4j
@Component
public class FaceRecognitionUtil {
    // 临时文件目录
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    // 人脸识别Python模块路径
    private static final String FACE_RECOGNITION_DIR = "face_recognition";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将Base64编码的图像保存为临时文件
     *
     * @param base64Image Base64编码的图像（不包含前缀"data:image/jpeg;base64,"）
     * @return 临时文件路径
     * @throws IOException IO异常
     */
    public String saveBase64ImageToTemp(String base64Image) throws IOException {
        // 确保base64字符串不包含数据URI前缀
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }

        // 解码Base64字符串
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // 创建临时文件
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileName = "face_" + timeStamp + ".jpg";
        Path tempFilePath = Paths.get(TEMP_DIR, fileName);

        // 保存图像到临时文件
        Files.write(tempFilePath, imageBytes);
        log.info("Saved base64 image to temporary file: {}", tempFilePath);

        return tempFilePath.toString();
    }

    /**
     * 提取人脸特征
     *
     * @param imagePath 图像文件路径
     * @return 人脸特征向量（JSON字符串）
     * @throws Exception 人脸特征提取异常
     */
    public String extractFaceFeature(String imagePath) throws Exception {
        log.info("Extracting face feature from image: {}", imagePath);

        // 构建Python脚本执行命令 - 使用批处理文件
        File batchFile = new File(System.getProperty("user.dir"), "run_face_extract.bat");
        if (!batchFile.exists()) {
            log.error("批处理文件不存在: {}", batchFile.getAbsolutePath());
            throw new Exception("批处理文件不存在: " + batchFile.getAbsolutePath());
        }

        log.info("使用批处理文件: {}", batchFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(
                batchFile.getAbsolutePath(),
                imagePath
        );

        // 设置工作目录
        pb.directory(new File(System.getProperty("user.dir")));

        // 将标准错误重定向到标准输出
        pb.redirectErrorStream(true);

        log.info("开始执行进程");

        // 启动进程
        Process process = pb.start();

        // 读取进程输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            log.info("进程输出: {}", line);
        }

        // 等待进程结束
        boolean exitOk = process.waitFor(30, TimeUnit.SECONDS);
        int exitCode = process.exitValue();

        log.info("进程执行完成，exitOk: {}, exitCode: {}", exitOk, exitCode);

        // 检查进程是否正常结束
        if (!exitOk || exitCode != 0) {
            log.error("Face feature extraction failed. Exit code: {}", exitCode);

            // 检查日志文件
            File logFile = new File(System.getProperty("user.dir"), "face_extract_log.txt");
            if (logFile.exists()) {
                log.info("读取批处理日志文件: {}", logFile.getAbsolutePath());
                String logContent = new String(Files.readAllBytes(logFile.toPath()));
                log.info("批处理日志内容:\n{}", logContent);
            } else {
                log.warn("批处理日志文件不存在: {}", logFile.getAbsolutePath());
            }

            throw new Exception("人脸特征提取失败，请确保图像中包含清晰的人脸");
        }

        String result = output.toString().trim();
        log.info("提取结果: {}", result);

        // 验证结果是否为有效的JSON
        try {
            Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
            if (resultMap.containsKey("error")) {
                throw new Exception(resultMap.get("error").toString());
            }
            if (!resultMap.containsKey("feature_vector")) {
                throw new Exception("未能提取到人脸特征");
            }
            return result;
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON result: {}", result, e);
            throw new Exception("人脸特征提取结果格式无效");
        }
    }

    /**
     * 人脸识别
     *
     * @param imagePath 待识别的图像文件路径
     * @return 识别结果，包含最匹配的员工编号和相似度分数
     * @throws Exception 人脸识别异常
     */
    public Map<String, Object> recognizeFace(String imagePath) throws Exception {
        log.info("Recognizing face from image: {}", imagePath);

        // 构建Python脚本执行命令 - 使用批处理文件
        File batchFile = new File(System.getProperty("user.dir"), "run_face_recognize.bat");
        if (!batchFile.exists()) {
            log.error("批处理文件不存在: {}", batchFile.getAbsolutePath());
            throw new Exception("批处理文件不存在: " + batchFile.getAbsolutePath());
        }

        log.info("使用批处理文件: {}", batchFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(
                batchFile.getAbsolutePath(),
                imagePath
        );

        // 设置工作目录
        pb.directory(new File(System.getProperty("user.dir")));

        // 将标准错误重定向到标准输出
        pb.redirectErrorStream(true);

        log.info("开始执行进程");

        // 启动进程
        Process process = pb.start();

        // 读取进程输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            log.info("进程输出: {}", line);
        }

        // 等待进程结束
        boolean exitOk = process.waitFor(30, TimeUnit.SECONDS);
        int exitCode = process.exitValue();

        log.info("进程执行完成，exitOk: {}, exitCode: {}", exitOk, exitCode);

        // 检查进程是否正常结束
        if (!exitOk || exitCode != 0) {
            log.error("Face recognition failed. Exit code: {}", exitCode);

            // 检查日志文件
            File logFile = new File(System.getProperty("user.dir"), "face_recognize_log.txt");
            if (logFile.exists()) {
                log.info("读取批处理日志文件: {}", logFile.getAbsolutePath());
                String logContent = new String(Files.readAllBytes(logFile.toPath()));
                log.info("批处理日志内容:\n{}", logContent);
            } else {
                log.warn("批处理日志文件不存在: {}", logFile.getAbsolutePath());
            }

            throw new Exception("人脸识别失败，请重试");
        }

        String result = output.toString().trim();
        log.info("识别结果: {}", result);

        // 解析识别结果
        try {
            return objectMapper.readValue(result, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON result: {}", result, e);
            throw new Exception("人脸识别结果格式无效");
        }
    }

    /**
     * 删除临时文件
     *
     * @param filePath 文件路径
     */
    public void deleteTempFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            log.info("Deleted temporary file: {}", filePath);
        } catch (IOException e) {
            log.warn("Failed to delete temporary file: {}", filePath, e);
        }
    }
} 