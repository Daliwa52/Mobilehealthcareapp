package com.example.mobilehealthcareapp_java.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp; // Ensure this is imported

public class MedicalDocument {
    @Exclude // Document ID will be set manually from Firestore document
    private String documentId;

    private String fileName;
    private String downloadUrl;
    private String fileType; // e.g., "application/pdf", "image/jpeg"
    private String storagePath; // Full path in Firebase Storage

    @ServerTimestamp // Firestore will automatically populate this on creation
    private Timestamp uploadedAt;

    public MedicalDocument() {
        // Firestore requires an empty constructor
    }

    public MedicalDocument(String fileName, String downloadUrl, String fileType, String storagePath) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.fileType = fileType;
        this.storagePath = storagePath;
        // 'uploadedAt' will be set by Firestore due to @ServerTimestamp
    }

    // Getters
    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public String getStoragePath() { return storagePath; }

    public Timestamp getUploadedAt() { // Getter for ServerTimestamp field
        return uploadedAt;
    }

    // Setters
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public void setUploadedAt(Timestamp uploadedAt) { // Setter for ServerTimestamp field
        this.uploadedAt = uploadedAt;
    }
}
