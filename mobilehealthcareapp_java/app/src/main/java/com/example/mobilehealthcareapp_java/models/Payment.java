package com.example.mobilehealthcareapp_java.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Payment {
    private String paymentId; // Document ID
    private String appointmentId;
    private String patientId;
    private String professionalId;
    private double amount;
    private Timestamp paymentTimestamp;
    private String status; // "pending", "completed", "failed", "refunded"
    private String paymentMethodDetails; // e.g., "Simulated Card Payment"

    public Payment() {
        // Firestore requires an empty constructor
    }

    public Payment(String appointmentId, String patientId, String professionalId, double amount, String status, String paymentMethodDetails) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.professionalId = professionalId;
        this.amount = amount;
        this.status = status;
        this.paymentMethodDetails = paymentMethodDetails;
        // Timestamp will be set by Firestore @ServerTimestamp or manually before saving
    }

    // Getters
    public String getPaymentId() {
        return paymentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public double getAmount() {
        return amount;
    }

    @ServerTimestamp // Automatically set server-side timestamp on creation for new payments
    public Timestamp getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethodDetails() {
        return paymentMethodDetails;
    }

    // Setters
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaymentTimestamp(Timestamp paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentMethodDetails(String paymentMethodDetails) {
        this.paymentMethodDetails = paymentMethodDetails;
    }
}
