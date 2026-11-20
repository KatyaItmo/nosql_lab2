package com.fitness.nosql_lab1.objects.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitness.nosql_lab1.objects.Role;

import java.time.LocalDateTime;
import java.util.Map;

public class User {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String rev;

    private String username;
    private String password;
    private Role role;

    @JsonProperty("_attachments")
    private Map<String, AttachmentMetadata> attachments;

    public User() {}

    public User(String id, String username, String password, Role role, Map<String, AttachmentMetadata> attachments) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.attachments = attachments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Map<String, AttachmentMetadata> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, AttachmentMetadata> attachments) {
        this.attachments = attachments;
    }
}
