package com.fitness.nosql_lab1.objects;

public enum Role {
    USER,
    MODER;

    public String getSecurityAuthority() {
        return "ROLE_" + this.name();
    }
}
