package com.zhly.service.impl;

import com.zhly.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    
    @Value("${travel.upload.path:./upload/}")
    private String uploadPath;
    
    @Value("${travel.upload.max-size:10485760}")
    private long maxFileSize;
    
    private String absoluteUploadPath;
    
    @PostConstruct
    public void init() {
        // 将相对路径转换为绝对路径
        try {
            java.io.File uploadDir = new java.io.File(uploadPath);
            
            // 如果是相对路径，转换为项目根目录下的绝对路径
            if (!uploadDir.isAbsolute()) {
                String projectRoot = System.getProperty("user.dir");
                uploadDir = new java.io.File(projectRoot, uploadPath);
            }
            
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                logger.info("创建上传目录: {}", uploadDir.getAbsolutePath());
            }
            
            absoluteUploadPath = uploadDir.getAbsolutePath();
            if (!absoluteUploadPath.endsWith(java.io.File.separator)) {
                absoluteUploadPath += java.io.File.separator;
            }
            
            logger.info("文件上传路径: {}", absoluteUploadPath);
        } catch (Exception e) {
            logger.error("创建上传目录失败: {}", e.getMessage());
            throw new RuntimeException("初始化上传目录失败", e);
        }
    }
    
    // 允许的图片类型
    private static final String[] IMAGE_TYPES = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    
    // 允许的文件类型
    private static final String[] ALLOWED_TYPES = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".pdf", ".doc", ".docx", ".txt"};
    
    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, true);
    }
    
    /**
     * 上传文件（内部方法）
     */
    private Map<String, Object> uploadFile(MultipartFile file, String folder, boolean needValidate) {
        try {
            // 验证文件（如果需要）
            if (needValidate && !validateFile(file)) {
                throw new RuntimeException("文件验证失败");
            }
            
            // 验证基本参数
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }
            
            // 生成文件路径
            String filePath = generateFilePath(file.getOriginalFilename(), folder);
            Path targetPath = Paths.get(absoluteUploadPath, filePath);
            
            // 创建目录
            Files.createDirectories(targetPath.getParent());
            
            // 保存文件
            file.transferTo(targetPath.toFile());
            
            // 返回文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("originalName", file.getOriginalFilename());
            result.put("fileName", targetPath.getFileName().toString());
            result.put("filePath", filePath);
            result.put("fileUrl", getFileUrl(filePath));
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());
            result.put("uploadTime", LocalDateTime.now());
            
            logger.info("文件上传成功: {}", filePath);
            return result;
            
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> uploadFiles(List<MultipartFile> files, String folder) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                Map<String, Object> fileResult = uploadFile(file, folder);
                successFiles.add(fileResult);
            } catch (Exception e) {
                failedFiles.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        result.put("successFiles", successFiles);
        result.put("failedFiles", failedFiles);
        result.put("totalCount", files.size());
        result.put("successCount", successFiles.size());
        result.put("failedCount", failedFiles.size());
        
        return result;
    }
    
    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        // 验证是否为图片
        if (!validateFileType(file.getOriginalFilename(), IMAGE_TYPES)) {
            throw new RuntimeException("只允许上传图片文件");
        }
        
        // 跳过uploadFile的重复验证
        return uploadFile(file, folder, false);
    }
    
    @Override
    public Map<String, Object> uploadAvatar(MultipartFile file) {
        // 验证头像文件
        if (!validateFileType(file.getOriginalFilename(), IMAGE_TYPES)) {
            throw new RuntimeException("头像必须是图片文件");
        }
        
        // 头像文件大小限制为2MB
        if (!validateFileSize(file.getSize(), 2 * 1024 * 1024)) {
            throw new RuntimeException("头像文件大小不能超过2MB");
        }
        
        // 跳过uploadFile的重复验证
        return uploadFile(file, "avatar", false);
    }
    
    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(absoluteUploadPath, filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                logger.info("文件删除成功: {}", filePath);
                return true;
            } else {
                logger.warn("文件不存在: {}", filePath);
                return false;
            }
        } catch (Exception e) {
            logger.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getFileInfo(String filePath) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            Path path = Paths.get(absoluteUploadPath, filePath);
            if (Files.exists(path)) {
                info.put("exists", true);
                info.put("fileName", path.getFileName().toString());
                info.put("fileSize", Files.size(path));
                info.put("fileUrl", getFileUrl(filePath));
                info.put("lastModified", Files.getLastModifiedTime(path));
            } else {
                info.put("exists", false);
            }
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}", e.getMessage(), e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }
    
    @Override
    public boolean validateFileType(String fileName, String[] allowedTypes) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        // 获取文件扩展名
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            // 没有扩展名或扩展名为空
            return false;
        }
        
        String extension = fileName.substring(lastDotIndex).toLowerCase();
        return Arrays.asList(allowedTypes).contains(extension);
    }
    
    @Override
    public boolean validateFileSize(long fileSize, long maxSize) {
        return fileSize <= maxSize;
    }
    
    @Override
    public String generateFilePath(String originalFileName, String folder) {
        // 生成时间戳
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        // 生成随机字符串
        String randomStr = UUID.randomUUID().toString().substring(0, 8);
        
        // 获取文件扩展名
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        // 生成文件名
        String fileName = timestamp + "_" + randomStr + extension;
        
        // 生成完整路径
        return folder + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + fileName;
    }
    
    @Override
    public String getFileUrl(String filePath) {
        // 如果是Windows系统，需要将反斜杠转换为正斜杠
        String normalizedPath = filePath.replace("\\", "/");
        
        // 返回相对路径，前端通过代理访问
        return "/upload/" + normalizedPath;
    }
    
    /**
     * 验证文件
     */
    private boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("文件为空或为空文件");
            return false;
        }
        
        String originalFilename = file.getOriginalFilename();
        logger.info("验证文件: {}, 大小: {}, 类型: {}", originalFilename, file.getSize(), file.getContentType());
        
        // 验证文件类型
        if (!validateFileType(originalFilename, ALLOWED_TYPES)) {
            logger.warn("文件类型验证失败: {}", originalFilename);
            return false;
        }
        
        // 验证文件大小
        if (!validateFileSize(file.getSize(), maxFileSize)) {
            logger.warn("文件大小验证失败: {} > {}", file.getSize(), maxFileSize);
            return false;
        }
        
        return true;
    }
}








