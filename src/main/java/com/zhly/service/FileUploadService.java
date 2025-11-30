package com.zhly.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 文件上传服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface FileUploadService {
    
    /**
     * 上传单个文件
     */
    Map<String, Object> uploadFile(MultipartFile file, String folder);
    
    /**
     * 批量上传文件
     */
    Map<String, Object> uploadFiles(List<MultipartFile> files, String folder);
    
    /**
     * 上传图片
     */
    Map<String, Object> uploadImage(MultipartFile file, String folder);
    
    /**
     * 上传头像
     */
    Map<String, Object> uploadAvatar(MultipartFile file);
    
    /**
     * 删除文件
     */
    boolean deleteFile(String filePath);
    
    /**
     * 获取文件信息
     */
    Map<String, Object> getFileInfo(String filePath);
    
    /**
     * 验证文件类型
     */
    boolean validateFileType(String fileName, String[] allowedTypes);
    
    /**
     * 验证文件大小
     */
    boolean validateFileSize(long fileSize, long maxSize);
    
    /**
     * 生成文件路径
     */
    String generateFilePath(String originalFileName, String folder);
    
    /**
     * 获取文件访问URL
     */
    String getFileUrl(String filePath);
}


















