package com.example.demo.controller;

import com.example.demo.entities.FileUploadResponse;
import com.example.demo.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@RestController
public class UploadController {
    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/uploadFile")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        long size = file.getSize();

        String filecode = fileUploadService.uploadFile(file);

        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setSize(size);
        response.setCode(filecode);
        response.setDownloadUri("/downloadFile/" + filecode);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/allFiles")
    public ResponseEntity<?> getAllFiles() {

        List<FileUploadResponse> resource = null;
        resource = fileUploadService.getAllFiles();

        if (resource == null) {
            return new ResponseEntity<>("Files not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(resource, HttpStatus.OK);
    }
    @GetMapping("/downloadFile/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {

        Resource resource = null;
        try {
            resource = fileUploadService.getFileAsResource(fileCode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    @DeleteMapping("/deleteFile/{fileCode}")
    public ResponseEntity<?> deleteFile(@PathVariable("fileCode") String fileCode)
    {
        if (!fileUploadService.deleteFile(fileCode)) {
            return ResponseEntity.badRequest().body(false);
        } else {
            return ResponseEntity.ok().body(true);
        }

    }
}
