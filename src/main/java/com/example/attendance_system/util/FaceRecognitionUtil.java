package com.example.attendance_system.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 人脸识别工具类，用于与Python人脸识别模块交互
 */
@Slf4j
@Component
public class FaceRecognitionUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 临时文件目录
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    // 人脸识别Python模块路径
    private static final String FACE_RECOGNITION_DIR = "Dlib_face_recognition_from_camera";
    
    /**
     * 将Base64编码的图像保存为临时文件
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
     * @param imagePath 图像文件路径
     * @return 人脸特征向量（JSON字符串）
     * @throws Exception 人脸特征提取异常
     */
    public String extractFaceFeature(String imagePath) throws Exception {
        log.info("Extracting face feature from image: {}", imagePath);
        
        // 构建Python脚本执行命令
        ProcessBuilder pb = new ProcessBuilder(
                "python",
                FACE_RECOGNITION_DIR + "/extract_face_feature.py",
                imagePath
        );
        
        // 设置工作目录
        pb.directory(new File(System.getProperty("user.dir")));
        
        // 将标准错误重定向到标准输出
        pb.redirectErrorStream(true);
        
        // 启动进程
        Process process = pb.start();
        
        // 读取进程输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        
        // 等待进程结束
        boolean exitOk = process.waitFor(30, TimeUnit.SECONDS);
        int exitCode = process.exitValue();
        
        // 检查进程是否正常结束
        if (!exitOk || exitCode != 0) {
            log.error("Face feature extraction failed. Exit code: {}", exitCode);
            throw new Exception("人脸特征提取失败，请确保图像中包含清晰的人脸");
        }
        
        String result = output.toString().trim();
        
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
     * @param imagePath 待识别的图像文件路径
     * @return 识别结果，包含最匹配的员工编号和相似度分数
     * @throws Exception 人脸识别异常
     */
    public Map<String, Object> recognizeFace(String imagePath) throws Exception {
        log.info("Recognizing face from image: {}", imagePath);
        
        // 构建Python脚本执行命令
        ProcessBuilder pb = new ProcessBuilder(
                "python",
                FACE_RECOGNITION_DIR + "/recognize_face.py",
                imagePath
        );
        
        // 设置工作目录
        pb.directory(new File(System.getProperty("user.dir")));
        
        // 将标准错误重定向到标准输出
        pb.redirectErrorStream(true);
        
        // 启动进程
        Process process = pb.start();
        
        // 读取进程输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        
        // 等待进程结束
        boolean exitOk = process.waitFor(30, TimeUnit.SECONDS);
        int exitCode = process.exitValue();
        
        // 检查进程是否正常结束
        if (!exitOk || exitCode != 0) {
            log.error("Face recognition failed. Exit code: {}", exitCode);
            throw new Exception("人脸识别失败，请重试");
        }
        
        String result = output.toString().trim();
        
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