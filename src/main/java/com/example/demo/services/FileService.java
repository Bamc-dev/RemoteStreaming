package com.example.demo.services;

import com.example.demo.entities.FileUploadResponse;
import com.example.demo.properties.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileService {

    private final Path fileUploadLocation;

    private Path foundFile;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.fileUploadLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileUploadLocation);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create the directory where you want to the uploaded the files will be kept.", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        // Renormalize the file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Verify if the file's name  is containing invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! File name is containing invalid path sequence " + fileName);
            }
            // Copy file to the target path (replacing existing file with the same name)
            String fileCode = RandomStringUtils.randomAlphanumeric(8);
            Path targetLocation = this.fileUploadLocation.resolve(fileCode+"-"+fileName);
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileCode;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public Resource getFileAsResource(String fileCode) throws IOException {
        Path dirPath = this.fileUploadLocation;

        Files.list(dirPath).forEach(file -> {
            if (file.getFileName().toString().startsWith(fileCode)) {
                foundFile = file;
                return;
            }
        });

        if (foundFile != null) {
            return new UrlResource(foundFile.toUri());
        }
        return null;
    }
    public List<FileUploadResponse> getAllFiles()
    {
        List<FileUploadResponse> filesList = new ArrayList<>();
        try {
            Files.list(this.fileUploadLocation).forEach((d)->
            {
                FileUploadResponse temp = new FileUploadResponse();
                temp.setFileName(d.getFileName().toString().substring(9));
                temp.setSize(0);
                temp.setDownloadUri("/downloadFile/"+d.getFileName().toString().substring(0,8));
                temp.setCode(d.getFileName().toString().substring(0,8));
                filesList.add(temp);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filesList;
    }

    public boolean deleteFile(String fileCode)
    {
        AtomicBoolean deleted = new AtomicBoolean(false);
        try {
            Files.list(this.fileUploadLocation).forEach(d->
            {
                if(d.getFileName().toString().startsWith(fileCode))
                {
                    try {
                        Files.delete(d);
                        deleted.set(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return deleted.get();

    }
}
