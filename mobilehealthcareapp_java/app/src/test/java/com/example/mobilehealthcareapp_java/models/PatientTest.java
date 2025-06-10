package com.example.mobilehealthcareapp_java.models;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatientTest {

    @Test
    public void patient_DefaultConstructor() {
        Patient patient = new Patient();
        assertNull("Default constructor should leave name null", patient.getName());
        assertNull("Default constructor should leave dateOfBirth null", patient.getDateOfBirth());
        assertNull("Default constructor should leave medicalHistory null", patient.getMedicalHistory());
        assertNull("Default constructor should leave allergies null", patient.getAllergies());
        assertNull("Default constructor should leave medications null", patient.getMedications());
    }

    @Test
    public void patient_ParameterizedConstructorAndGetters() {
        String name = "Jane Doe";
        String dob = "1990-01-01";
        String history = "None";
        List<String> allergies = Arrays.asList("Peanuts", "Penicillin");
        List<String> medications = Arrays.asList("Loratadine");

        Patient patient = new Patient(name, dob, history, allergies, medications);

        assertEquals("Name should match constructor argument", name, patient.getName());
        assertEquals("DateOfBirth should match constructor argument", dob, patient.getDateOfBirth());
        assertEquals("MedicalHistory should match constructor argument", history, patient.getMedicalHistory());
        assertEquals("Allergies should match constructor argument", allergies, patient.getAllergies());
        assertEquals("Medications should match constructor argument", medications, patient.getMedications());
    }

    @Test
    public void patient_Name_GetterSetter() {
        Patient patient = new Patient();
        String testName = "John Doe";
        patient.setName(testName);
        assertEquals("Name getter/setter failed", testName, patient.getName());
    }

    @Test
    public void patient_DateOfBirth_GetterSetter() {
        Patient patient = new Patient();
        String testDOB = "1985-05-15";
        patient.setDateOfBirth(testDOB);
        assertEquals("DateOfBirth getter/setter failed", testDOB, patient.getDateOfBirth());
    }

    @Test
    public void patient_MedicalHistory_GetterSetter() {
        Patient patient = new Patient();
        String testHistory = "Hypertension";
        patient.setMedicalHistory(testHistory);
        assertEquals("MedicalHistory getter/setter failed", testHistory, patient.getMedicalHistory());
    }

    @Test
    public void patient_Allergies_GetterSetter() {
        Patient patient = new Patient();
        List<String> testAllergies = new ArrayList<>();
        testAllergies.add("Pollen");
        testAllergies.add("Dust Mites");
        patient.setAllergies(testAllergies);
        assertEquals("Allergies getter/setter failed", testAllergies, patient.getAllergies());
    }

    @Test
    public void patient_Medications_GetterSetter() {
        Patient patient = new Patient();
        List<String> testMedications = new ArrayList<>();
        testMedications.add("Metformin");
        testMedications.add("Aspirin");
        patient.setMedications(testMedications);
        assertEquals("Medications getter/setter failed", testMedications, patient.getMedications());
    }
}
