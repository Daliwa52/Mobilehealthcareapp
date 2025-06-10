package com.example.mobilehealthcareapp_java.models;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import java.util.Date;

public class PaymentTest {

    @Test
    public void payment_DefaultConstructor() {
        Payment payment = new Payment();
        assertNull("Default constructor paymentId should be null", payment.getPaymentId());
        assertNull("Default constructor appointmentId should be null", payment.getAppointmentId());
        assertNull("Default constructor patientId should be null", payment.getPatientId());
        assertNull("Default constructor professionalId should be null", payment.getProfessionalId());
        assertEquals("Default constructor amount should be 0.0", 0.0, payment.getAmount(), 0.001);
        assertNull("Default constructor paymentTimestamp should be null", payment.getPaymentTimestamp());
        assertNull("Default constructor status should be null", payment.getStatus());
        assertNull("Default constructor paymentMethodDetails should be null", payment.getPaymentMethodDetails());
    }

    @Test
    public void payment_ParameterizedConstructorAndGetters() {
        String appointmentId = "appt123";
        String patientId = "patient1";
        String professionalId = "prof1";
        double amount = 100.0;
        String status = "completed";
        String methodDetails = "Card ending XXXX";
        // Timestamp usually set by @ServerTimestamp or manually before saving

        Payment payment = new Payment(appointmentId, patientId, professionalId, amount, status, methodDetails);

        assertEquals("AppointmentId should match constructor argument", appointmentId, payment.getAppointmentId());
        assertEquals("PatientId should match constructor argument", patientId, payment.getPatientId());
        assertEquals("ProfessionalId should match constructor argument", professionalId, payment.getProfessionalId());
        assertEquals("Amount should match constructor argument", amount, payment.getAmount(), 0.001);
        assertEquals("Status should match constructor argument", status, payment.getStatus());
        assertEquals("PaymentMethodDetails should match constructor argument", methodDetails, payment.getPaymentMethodDetails());
    }

    @Test
    public void payment_PaymentId_GetterSetter() {
        Payment payment = new Payment();
        String testId = "pay789";
        payment.setPaymentId(testId);
        assertEquals("PaymentId getter/setter failed", testId, payment.getPaymentId());
    }

    @Test
    public void payment_AppointmentId_GetterSetter() {
        Payment payment = new Payment();
        String testApptId = "appointmentXYZ";
        payment.setAppointmentId(testApptId);
        assertEquals("AppointmentId getter/setter failed", testApptId, payment.getAppointmentId());
    }

    @Test
    public void payment_PatientId_GetterSetter() {
        Payment payment = new Payment();
        String testPatientId = "patientABC";
        payment.setPatientId(testPatientId);
        assertEquals("PatientId getter/setter failed", testPatientId, payment.getPatientId());
    }

    @Test
    public void payment_ProfessionalId_GetterSetter() {
        Payment payment = new Payment();
        String testProfId = "profDEF";
        payment.setProfessionalId(testProfId);
        assertEquals("ProfessionalId getter/setter failed", testProfId, payment.getProfessionalId());
    }

    @Test
    public void payment_Amount_GetterSetter() {
        Payment payment = new Payment();
        double testAmount = 75.50;
        payment.setAmount(testAmount);
        assertEquals("Amount getter/setter failed", testAmount, payment.getAmount(), 0.001);
    }

    @Test
    public void payment_PaymentTimestamp_GetterSetter() {
        Payment payment = new Payment();
        Timestamp testTimestamp = new Timestamp(new Date());
        payment.setPaymentTimestamp(testTimestamp);
        assertEquals("PaymentTimestamp getter/setter failed", testTimestamp, payment.getPaymentTimestamp());
    }

    @Test
    public void payment_Status_GetterSetter() {
        Payment payment = new Payment();
        String testStatus = "failed";
        payment.setStatus(testStatus);
        assertEquals("Status getter/setter failed", testStatus, payment.getStatus());
    }

    @Test
    public void payment_PaymentMethodDetails_GetterSetter() {
        Payment payment = new Payment();
        String testMethodDetails = "PayPal Transaction ID: ABC123XYZ";
        payment.setPaymentMethodDetails(testMethodDetails);
        assertEquals("PaymentMethodDetails getter/setter failed", testMethodDetails, payment.getPaymentMethodDetails());
    }
}
