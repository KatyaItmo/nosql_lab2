package com.fitness.nosql_lab1.dtos.userDto;

import com.fitness.nosql_lab1.objects.Role;

import java.util.Map;

public class UserData {
    private String username;
    private Role role;
    private Map<String, AttachmentDto> attachments;
    private String authMessage;

    public UserData() {}

    public UserData(String username, Role role, Map<String, AttachmentDto> attachments) {
        this.username = username;
        this.role = role;
        this.attachments = attachments;
    }

    public UserData(String authMessage) {
        this.authMessage = authMessage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Map<String, AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public String getAuthMessage() {
        return authMessage;
    }

    public void setAuthMessage(String authMessage) {
        this.authMessage = authMessage;
    }
}
