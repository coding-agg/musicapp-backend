package com.example.musicapp.controller;


import com.example.musicapp.service.GoogleDriveService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/upload")
public class UploadController extends AuthenticatedController {

    @Autowired
    private GoogleDriveService driveService;

    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        getAuthenticatedUser(request);
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(convFile);

            String fileId = driveService.uploadToDrive(convFile);

            // Optionally delete local file

            return fileId;

        } catch (IOException | GeneralSecurityException e) {
            return "Error: " + e.getMessage();
        }
    }
}

