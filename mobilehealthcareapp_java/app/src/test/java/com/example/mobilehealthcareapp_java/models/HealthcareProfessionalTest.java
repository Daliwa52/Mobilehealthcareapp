package com.example.mobilehealthcareapp_java.models;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthcareProfessionalTest {

    @Test
    public void professional_DefaultConstructor() {
        HealthcareProfessional professional = new HealthcareProfessional();
        assertNull("Default constructor should leave firebaseId null", professional.getFirebaseId());
        assertNull("Default constructor should leave name null", professional.getName());
        assertNull("Default constructor should leave qualifications null", professional.getQualifications());
        assertNull("Default constructor should leave specialties null", professional.getSpecialties());
        assertNull("Default constructor should leave availability null", professional.getAvailability());
    }

    @Test
    public void professional_ParameterizedConstructorAndGetters() {
        String name = "Dr. Smith";
        String qualifications = "MD, PhD";
        List<String> specialties = Arrays.asList("Cardiology", "Internal Medicine");
        String availability = "Mon-Fri 9am-5pm";
        // firebaseId is typically set separately after object creation or Firestore doc retrieval

        HealthcareProfessional professional = new HealthcareProfessional(name, qualifications, specialties, availability);

        assertEquals("Name should match constructor argument", name, professional.getName());
        assertEquals("Qualifications should match constructor argument", qualifications, professional.getQualifications());
        assertEquals("Specialties should match constructor argument", specialties, professional.getSpecialties());
        assertEquals("Availability should match constructor argument", availability, professional.getAvailability());
    }

    @Test
    public void professional_FirebaseId_GetterSetter() {
        HealthcareProfessional professional = new HealthcareProfessional();
        String testId = "prof123";
        professional.setFirebaseId(testId);
        assertEquals("FirebaseId getter/setter failed", testId, professional.getFirebaseId());
    }

    @Test
    public void professional_Name_GetterSetter() {
        HealthcareProfessional professional = new HealthcareProfessional();
        String testName = "Dr. Emily Carter";
        professional.setName(testName);
        assertEquals("Name getter/setter failed", testName, professional.getName());
    }

    @Test
    public void professional_Qualifications_GetterSetter() {
        HealthcareProfessional professional = new HealthcareProfessional();
        String testQualifications = "MBBS, FRCS";
        professional.setQualifications(testQualifications);
        assertEquals("Qualifications getter/setter failed", testQualifications, professional.getQualifications());
    }

    @Test
    public void professional_Specialties_GetterSetter() {
        HealthcareProfessional professional = new HealthcareProfessional();
        List<String> testSpecialties = new ArrayList<>();
        testSpecialties.add("Neurology");
        professional.setSpecialties(testSpecialties);
        assertEquals("Specialties getter/setter failed", testSpecialties, professional.getSpecialties());
    }

    @Test
    public void professional_Availability_GetterSetter() {
        HealthcareProfessional professional = new HealthcareProfessional();
        String testAvailability = "Tue, Thu 10am-4pm";
        professional.setAvailability(testAvailability);
        assertEquals("Availability getter/setter failed", testAvailability, professional.getAvailability());
    }
}
