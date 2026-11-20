package com.fitness.nosql_lab1.dtos.userDto;

public class AttachmentDto {
    private String contentType;
    private long length;
    private String digest;
    private int revpos;
    private boolean stub;

    public AttachmentDto() {}

    public AttachmentDto(String contentType, long length, String digest, int revpos, boolean stub) {
        this.contentType = contentType;
        this.length = length;
        this.digest = digest;
        this.revpos = revpos;
        this.stub = stub;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public int getRevpos() {
        return revpos;
    }

    public void setRevpos(int revpos) {
        this.revpos = revpos;
    }

    public boolean isStub() {
        return stub;
    }

    public void setStub(boolean stub) {
        this.stub = stub;
    }
}
