package com.fitness.nosql_lab1.dtos;

import com.fitness.nosql_lab1.objects.Role;

public class UserDto {
    private String username;
    private String password;
    private Role role;
    private String secreteCode;

    public UserDto(String username, String password, Role role, String secreteCode) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.secreteCode = secreteCode;
    }

    public UserDto() {}

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

    public String getSecreteCode() {
        return secreteCode;
    }

    public void setSecreteCode(String secreteCode) {
        this.secreteCode = secreteCode;
    }
}
