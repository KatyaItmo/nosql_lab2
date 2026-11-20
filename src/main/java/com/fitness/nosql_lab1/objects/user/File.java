package com.fitness.nosql_lab1.objects.user;

public class File {
    private String filename;
    private String contentType;
    private byte[] fileBytes;

    public File() {}

    public File(String filename, String contentType, byte[] fileBytes) {
        this.filename = filename;
        this.contentType = contentType;
        this.fileBytes = fileBytes;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
}
