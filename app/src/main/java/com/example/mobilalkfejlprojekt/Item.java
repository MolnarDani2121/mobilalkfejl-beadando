package com.example.mobilalkfejlprojekt;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Item implements Serializable {
    private String name;
    private String description;
    private String price;
    private String documentId;

    public Item() {
    }

    public Item(String name, String description, String price) {
        this.name = new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        this.description = new String(description.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        this.price = new String(price.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = new String(description.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = new String(price.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
