package com.fitness.nosql_lab1.dtos.userDto;

import com.fitness.nosql_lab1.objects.Role;

public class UserRegistration {
    private String username;
    private String password;
    private Role role;
    private String secreteCode;

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
