package com.example.demo.services;

import com.example.demo.entities.FileChunk;
import com.example.demo.entities.FileUploadResponse;
import com.example.demo.properties.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileService {

    private final Path fileUploadLocation;

    private Path foundFile;

    private List<FileChunk> fileChunks;

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

    public void receiveChunk(FileChunk chunk) throws IOException {
        fileChunks.add(chunk);
    }

    public void saveFile(String fileId, String extension) throws IOException {
        List<FileChunk> chunksInOrder = fileChunks;

        // Triez les chunks en fonction du numéro de chunk
        Collections.sort(chunksInOrder, Comparator.comparingInt(FileChunk::getChunkNumber));

        // Écrivez les chunks triés dans le fichier
        Path fileUploadLocation = Paths.get("chemin/vers/votre/dossier/de/destination");
        File fileUploadDirectory = fileUploadLocation.toFile();

        // Vérifiez si le dossier de destination existe, sinon créez-le
        if (!fileUploadDirectory.exists()) {
            fileUploadDirectory.mkdirs();
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileUploadLocation.resolve(fileId + "." + extension).toFile())) {
            for (FileChunk chunk : chunksInOrder) {
                fileOutputStream.write(chunk.getData());
            }
        }

        // Supprimez les chunks une fois qu'ils ont été écrits dans le fichier
        for (FileChunk chunk : chunksInOrder) {
            fileChunks.remove(chunk.getFileId() + "-" + chunk.getChunkNumber());
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
