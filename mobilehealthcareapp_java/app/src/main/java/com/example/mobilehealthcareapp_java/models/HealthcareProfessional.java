package com.example.mobilehealthcareapp_java.models;

import com.google.firebase.firestore.Exclude; // Required for excluding fields from Firestore

import java.util.List;

public class HealthcareProfessional {
    private String firebaseId; // Document ID from Firestore (which is Firebase Auth User ID)
    private String name;
    private String qualifications;
    private List<String> specialties;
    private String availability;
    // private String profileImageUrl;

    public HealthcareProfessional() {}

    public HealthcareProfessional(String name, String qualifications, List<String> specialties, String availability) {
        this.name = name;
        this.qualifications = qualifications;
        this.specialties = specialties;
        this.availability = availability;
    }

    // Use @Exclude to prevent firebaseId from being written to Firestore as a field,
    // as it's the document ID itself.
    @Exclude
    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
