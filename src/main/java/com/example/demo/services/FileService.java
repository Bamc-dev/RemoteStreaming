package com.example.demo.services;

import com.example.demo.entities.FileChunk;
import com.example.demo.entities.FileUploadResponse;
import com.example.demo.properties.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileService {

    private final Path fileUploadLocation;

    private final String chunkFolder;

    private Path foundFile;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.fileUploadLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        this.chunkFolder = fileStorageProperties.getUploadChunk();

        File dossier = new File(chunkFolder);

        // Vérifie si le dossier n'existe pas
        if (!dossier.exists()) {
            dossier.mkdirs();
        }
        try {
            Files.createDirectories(this.fileUploadLocation);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create the directory where you want to the uploaded the files will be kept.", e);
        }
    }

    public void receiveChunk(FileChunk chunk) throws IOException {
        String chunkPath = chunkFolder+ File.separator + chunk.getFileId() + File.separator + chunk.getChunkNumber();
        new File(chunkFolder+ File.separator + chunk.getFileId()).mkdir();
        try (FileOutputStream fos = new FileOutputStream(chunkPath)) {
            fos.write(chunk.getData());
        }
    }

    public void saveFile(String fileId, String extension) throws IOException {

        File output = new File(fileUploadLocation+File.separator+fileId+"."+extension);
        try (FileOutputStream fos = new FileOutputStream(output)) {
            File[] chunks = new File(chunkFolder + File.separator + fileId).listFiles();

            if (chunks != null) {
                List<File> chunkList = new ArrayList<>();
                for (File chunk : chunks) {
                    chunkList.add(chunk);
                }

                chunkList.sort(Comparator.comparing(File::getName));

                for (File chunk : chunkList) {
                    try (FileInputStream fis = new FileInputStream(chunk)) {
                        byte[] buffer = new byte[50 * 1024 * 1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    chunk.delete();
                }
            }
        }
        Files.delete(Path.of(chunkFolder + File.separator + fileId));
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
