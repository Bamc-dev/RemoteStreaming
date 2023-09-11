package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileChunk {
    @JsonProperty("FileId")
    private String fileId;
    @JsonProperty("ChunkNumber")
    private int chunkNumber;
    @JsonProperty("Data")
    private byte[] data;

    public FileChunk(String fileId, int chunkNumber, byte[] byteArray) {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
