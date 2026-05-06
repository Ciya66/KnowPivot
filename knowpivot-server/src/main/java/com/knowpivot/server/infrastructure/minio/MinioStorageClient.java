package com.knowpivot.server.infrastructure.minio;

import com.knowpivot.server.infrastructure.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageClient {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 上传文件
     */
    public String uploadFile(String objectName, MultipartFile file) {
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("File uploaded to MinIO: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO upload failed: {}", objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 获取文件临时访问链接
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(2, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.error("MinIO presigned URL failed: {}", objectName, e);
            throw new RuntimeException("获取文件链接失败", e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO download failed: {}", objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("File deleted from MinIO: {}", objectName);
        } catch (Exception e) {
            log.error("MinIO delete failed: {}", objectName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
                log.info("MinIO bucket created: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("MinIO bucket check failed", e);
        }
    }
}
