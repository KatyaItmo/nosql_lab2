package com.fitness.nosql_lab1.objects.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttachmentMetadata {

    @JsonProperty("content_type")
    private String contentType;

    private long length;
    private String digest;
    private int revpos;
    private boolean stub;

    public AttachmentMetadata() {}

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
