package com.fitness.nosql_lab1.repos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CouchDbResponse<T> {
    private List<T> docs;

    public List<T> getDocs() {
        return docs;
    }

    public void setDocs(List<T> docs) {
        this.docs = docs;
    }
}