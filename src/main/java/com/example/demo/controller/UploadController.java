package com.example.demo.controller;

import com.example.demo.entities.FileChunk;
import com.example.demo.entities.FileUploadResponse;
import com.example.demo.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class UploadController {
    @Autowired
    private FileService fileService;

    @GetMapping("/allFiles")
    public ResponseEntity<?> getAllFiles() {

        List<FileUploadResponse> resource = null;
        resource = fileService.getAllFiles();

        if (resource == null) {
            return new ResponseEntity<>("Files not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(resource, HttpStatus.OK);
    }
    @GetMapping("/downloadFile/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {

        Resource resource = null;
        try {
            resource = fileService.getFileAsResource(fileCode);
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
        if (!fileService.deleteFile(fileCode)) {
            return ResponseEntity.badRequest().body(false);
        } else {
            return ResponseEntity.ok().body(true);
        }

    }
    @PostMapping("/uploadChunk")
    public ResponseEntity<String> uploadChunk(@RequestBody FileChunk chunk) {
        try {
            fileService.receiveChunk(chunk);
            return ResponseEntity.ok("Chunk received successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error receiving chunk.");
        }
    }
    @PostMapping("/saveFile/{fileId}/{extension}")
    public ResponseEntity<String> saveFile(@PathVariable String fileId, @PathVariable String extension) {
        try {
            fileService.saveFile(fileId, extension);
            return ResponseEntity.ok("File saved successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file.");
        }
    }
}
