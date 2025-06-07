package com.example.mobilehealthcareapp_java.models;

import java.util.List;

public class Patient {
    private String name;
    private String dateOfBirth;
    private String medicalHistory;
    private List<String> allergies;
    private List<String> medications;
    // private String profileImageUrl; // Will be added later

    // Empty constructor required for Firestore
    public Patient() {}

    public Patient(String name, String dateOfBirth, String medicalHistory, List<String> allergies, List<String> medications) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.medicalHistory = medicalHistory;
        this.allergies = allergies;
        this.medications = medications;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public List<String> getMedications() {
        return medications;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public void setMedications(List<String> medications) {
        this.medications = medications;
    }
}
