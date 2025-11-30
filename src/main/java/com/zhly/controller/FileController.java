package com.zhly.controller;

import com.zhly.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/file")
public class FileController {
    
    @Value("${travel.upload.path:/upload/}")
    private String uploadPath;
    
    @Value("${travel.upload.max-size:10485760}")
    private Long maxFileSize;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件大小
            if (file.getSize() > maxFileSize) {
                return Result.error("文件大小超过限制");
            }
            
            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名不能为空");
            }
            
            String extension = getFileExtension(originalFilename);
            if (!isAllowedFileType(extension)) {
                return Result.error("不支持的文件类型");
            }
            
            // 生成新的文件名
            String newFilename = generateFileName(originalFilename);
            
            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + datePath + "/";
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 保存文件
            Path filePath = Paths.get(fullUploadPath + newFilename);
            Files.write(filePath, file.getBytes());
            
            // 返回文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("originalName", originalFilename);
            result.put("fileName", newFilename);
            result.put("filePath", "/upload/" + datePath + "/" + newFilename);
            result.put("fileSize", file.getSize());
            result.put("fileType", extension);
            result.put("uploadTime", LocalDateTime.now());
            
            return Result.success("文件上传成功", result);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public Result<Map<String, Object>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            Map<String, Object> result = new HashMap<>();
            java.util.List<Map<String, Object>> uploadedFiles = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Result<Map<String, Object>> uploadResult = uploadFile(file);
                    if (uploadResult.isSuccess()) {
                        uploadedFiles.add(uploadResult.getData());
                    }
                }
            }
            
            result.put("uploadedFiles", uploadedFiles);
            result.put("totalCount", files.length);
            result.put("successCount", uploadedFiles.size());
            
            return Result.success("批量上传完成", result);
        } catch (Exception e) {
            return Result.error("批量上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestParam String filePath) {
        try {
            String fullPath = uploadPath + filePath.replace("/upload/", "");
            File file = new File(fullPath);
            
            if (file.exists() && file.delete()) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getFileInfo(@RequestParam String filePath) {
        try {
            String fullPath = uploadPath + filePath.replace("/upload/", "");
            File file = new File(fullPath);
            
            if (!file.exists()) {
                return Result.error("文件不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", file.getName());
            result.put("fileSize", file.length());
            result.put("lastModified", file.lastModified());
            result.put("exists", file.exists());
            
            return Result.success("获取文件信息成功", result);
        } catch (Exception e) {
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String extension) {
        String[] allowedExtensions = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // 图片
            "pdf", "doc", "docx", "txt", "rtf", // 文档
            "mp4", "avi", "mov", "wmv", "flv", // 视频
            "mp3", "wav", "flac", "aac", // 音频
            "zip", "rar", "7z", "tar", "gz" // 压缩包
        };
        
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 生成新的文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }
}


















