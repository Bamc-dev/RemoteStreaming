package com.example.demo.services;

import com.example.demo.entities.FileChunk;
import com.example.demo.entities.FileUploadResponse;
import com.example.demo.properties.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    private final ThreadPoolTaskExecutor executor;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.fileUploadLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        this.chunkFolder = fileStorageProperties.getUploadChunk();
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Définir le nombre de threads dans le pool
        executor.initialize();
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
        executor.execute(()->
        {
            try (FileOutputStream fos = new FileOutputStream(chunkPath)) {
                try {
                    fos.write(chunk.getData());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void saveFile(String fileId, String extension) throws IOException {

        File output = new File(fileUploadLocation+File.separator+fileId+"."+extension);
        try (FileOutputStream fos = new FileOutputStream(output)) {
            List<File> chunks = new ArrayList<>(Arrays.stream(new File(chunkFolder + File.separator + fileId).listFiles()).toList());
            chunks.sort(new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Integer.compare(Integer.parseInt(o1.getName()), Integer.parseInt(o2.getName()));
                }
            });

            if (chunks != null) {
                for (int i = 0; i < chunks.size(); i++) {
                    try (FileInputStream fis = new FileInputStream(chunks.get(i))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
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
                this.supprimerRepertoire(new File(chunkFolder+File.separator+fileCode));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return deleted.get();
    }
    public boolean supprimerRepertoire(File repertoire) {
        if (!repertoire.exists()) {
            return true;
        }
        if (repertoire.isDirectory()) {
            File[] fichiers = repertoire.listFiles();
            if (fichiers != null) {
                for (File fichier : fichiers) {
                    boolean success = supprimerRepertoire(fichier);
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return repertoire.delete();
    }
}
