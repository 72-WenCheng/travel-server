package com.zhly.controller;

import com.zhly.annotation.RateLimit;
import com.zhly.common.Result;
import com.zhly.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 上传单个文件
     */
    @PostMapping("/file")
    @RateLimit(permitsPerSecond = 5.0, limitType = RateLimit.LimitType.USER)
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String folder) {
        try {
            Map<String, Object> result = fileUploadService.uploadFile(file, folder);
            return Result.success("文件上传成功", result);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     */
    @PostMapping("/files")
    @RateLimit(permitsPerSecond = 2.0, limitType = RateLimit.LimitType.USER)
    public Result<Map<String, Object>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "common") String folder) {
        try {
            Map<String, Object> result = fileUploadService.uploadFiles(files, folder);
            return Result.success("批量文件上传完成", result);
        } catch (Exception e) {
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传图片
     */
    @PostMapping("/image")
    @RateLimit(permitsPerSecond = 10.0, limitType = RateLimit.LimitType.USER)
    public Result<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "images") String folder) {
        try {
            Map<String, Object> result = fileUploadService.uploadImage(file, folder);
            return Result.success("图片上传成功", result);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @RateLimit(permitsPerSecond = 3.0, limitType = RateLimit.LimitType.USER)
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = fileUploadService.uploadAvatar(file);
            return Result.success("头像上传成功", result);
        } catch (Exception e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<String> deleteFile(@RequestParam String filePath) {
        try {
            boolean success = fileUploadService.deleteFile(filePath);
            if (success) {
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
    @GetMapping("/file/info")
    public Result<Map<String, Object>> getFileInfo(@RequestParam String filePath) {
        try {
            Map<String, Object> result = fileUploadService.getFileInfo(filePath);
            return Result.success("获取文件信息成功", result);
        } catch (Exception e) {
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证文件类型
     */
    @PostMapping("/validate/type")
    public Result<Boolean> validateFileType(
            @RequestParam String fileName,
            @RequestParam String[] allowedTypes) {
        try {
            boolean isValid = fileUploadService.validateFileType(fileName, allowedTypes);
            return Result.success("文件类型验证完成", isValid);
        } catch (Exception e) {
            return Result.error("文件类型验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证文件大小
     */
    @PostMapping("/validate/size")
    public Result<Boolean> validateFileSize(
            @RequestParam long fileSize,
            @RequestParam long maxSize) {
        try {
            boolean isValid = fileUploadService.validateFileSize(fileSize, maxSize);
            return Result.success("文件大小验证完成", isValid);
        } catch (Exception e) {
            return Result.error("文件大小验证失败: " + e.getMessage());
        }
    }
}


















