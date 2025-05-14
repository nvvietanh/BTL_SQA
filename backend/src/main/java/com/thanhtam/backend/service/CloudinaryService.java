package com.thanhtam.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folder) throws IOException;
    String getFileUrl(String publicId);
    void deleteFile(String publicId) throws IOException;
}