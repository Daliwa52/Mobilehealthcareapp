package com.example.mobilehealthcareapp_java.models;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp; // Import Firebase Timestamp
import java.util.Date; // For creating a Timestamp

public class AppointmentTest {

    @Test
    public void appointment_DefaultConstructor() {
        Appointment appointment = new Appointment();
        assertNull("Default constructor appointmentId should be null", appointment.getAppointmentId());
        assertNull("Default constructor patientId should be null", appointment.getPatientId());
        assertNull("Default constructor patientName should be null", appointment.getPatientName());
        assertNull("Default constructor professionalId should be null", appointment.getProfessionalId());
        assertNull("Default constructor professionalName should be null", appointment.getProfessionalName());
        assertNull("Default constructor appointmentTimestamp should be null", appointment.getAppointmentTimestamp());
        assertNull("Default constructor status should be null", appointment.getStatus());
        assertNull("Default constructor reason should be null", appointment.getReason());
        assertEquals("Default constructor appointmentFee should be 0.0", 0.0, appointment.getAppointmentFee(), 0.001);
        assertNull("Default constructor paymentStatus should be null", appointment.getPaymentStatus());
    }

    @Test
    public void appointment_ParameterizedConstructorAndGetters() {
        String patientId = "patient123";
        String patientName = "John Patient";
        String professionalId = "prof456";
        String professionalName = "Dr. Smith";
        Timestamp timestamp = new Timestamp(new Date());
        String status = "scheduled";
        String reason = "Annual Checkup";
        double fee = 75.0;
        String paymentStatus = "unpaid";

        Appointment appointment = new Appointment(patientId, patientName, professionalId, professionalName,
                                                timestamp, status, reason, fee, paymentStatus);

        assertEquals("PatientId should match", patientId, appointment.getPatientId());
        assertEquals("PatientName should match", patientName, appointment.getPatientName());
        assertEquals("ProfessionalId should match", professionalId, appointment.getProfessionalId());
        assertEquals("ProfessionalName should match", professionalName, appointment.getProfessionalName());
        assertEquals("Timestamp should match", timestamp, appointment.getAppointmentTimestamp());
        assertEquals("Status should match", status, appointment.getStatus());
        assertEquals("Reason should match", reason, appointment.getReason());
        assertEquals("Fee should match", fee, appointment.getAppointmentFee(), 0.001);
        assertEquals("PaymentStatus should match", paymentStatus, appointment.getPaymentStatus());
    }

    @Test
    public void appointment_Id_GetterSetter() {
        Appointment appointment = new Appointment();
        String testId = "apptXYZ";
        appointment.setAppointmentId(testId);
        assertEquals("AppointmentId getter/setter failed", testId, appointment.getAppointmentId());
    }

    @Test
    public void appointment_PatientId_GetterSetter() {
        Appointment appointment = new Appointment();
        String testPatientId = "patientABC";
        appointment.setPatientId(testPatientId);
        assertEquals("PatientId getter/setter failed", testPatientId, appointment.getPatientId());
    }

    @Test
    public void appointment_PatientName_GetterSetter() {
        Appointment appointment = new Appointment();
        String testPatientName = "Alice Wonderland";
        appointment.setPatientName(testPatientName);
        assertEquals("PatientName getter/setter failed", testPatientName, appointment.getPatientName());
    }

    @Test
    public void appointment_ProfessionalId_GetterSetter() {
        Appointment appointment = new Appointment();
        String testProfId = "profXYZ";
        appointment.setProfessionalId(testProfId);
        assertEquals("ProfessionalId getter/setter failed", testProfId, appointment.getProfessionalId());
    }

    @Test
    public void appointment_ProfessionalName_GetterSetter() {
        Appointment appointment = new Appointment();
        String testProfName = "Dr. Who";
        appointment.setProfessionalName(testProfName);
        assertEquals("ProfessionalName getter/setter failed", testProfName, appointment.getProfessionalName());
    }

    @Test
    public void appointment_Timestamp_GetterSetter() {
        Appointment appointment = new Appointment();
        Timestamp testTimestamp = new Timestamp(new Date(System.currentTimeMillis() - 10000)); // A time in the past
        appointment.setAppointmentTimestamp(testTimestamp);
        assertEquals("Timestamp getter/setter failed", testTimestamp, appointment.getAppointmentTimestamp());
    }

    @Test
    public void appointment_Status_GetterSetter() {
        Appointment appointment = new Appointment();
        String testStatus = "completed";
        appointment.setStatus(testStatus);
        assertEquals("Status getter/setter failed", testStatus, appointment.getStatus());
    }

    @Test
    public void appointment_Reason_GetterSetter() {
        Appointment appointment = new Appointment();
        String testReason = "Follow-up";
        appointment.setReason(testReason);
        assertEquals("Reason getter/setter failed", testReason, appointment.getReason());
    }

    @Test
    public void appointment_Fee_GetterSetter() {
        Appointment appointment = new Appointment();
        double testFee = 120.50;
        appointment.setAppointmentFee(testFee);
        assertEquals("AppointmentFee getter/setter failed", testFee, appointment.getAppointmentFee(), 0.001);
    }

    @Test
    public void appointment_PaymentStatus_GetterSetter() {
        Appointment appointment = new Appointment();
        String testPaymentStatus = "paid";
        appointment.setPaymentStatus(testPaymentStatus);
        assertEquals("PaymentStatus getter/setter failed", testPaymentStatus, appointment.getPaymentStatus());
    }
}
