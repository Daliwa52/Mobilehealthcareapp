package com.example.mobilehealthcareapp_java.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude; // For appointmentId

public class Appointment {
    @Exclude // Exclude from Firestore as it's the document ID
    private String appointmentId;

    private String patientId;
    private String patientName;
    private String professionalId;
    private String professionalName;
    private Timestamp appointmentTimestamp;
    private String status; // e.g., "scheduled", "completed", "cancelled", "pending_approval"
    private String reason;

    private double appointmentFee;
    private String paymentStatus; // e.g., "unpaid", "paid", "pending", "failed"

    public Appointment() {}

    public Appointment(String patientId, String patientName, String professionalId, String professionalName,
                       Timestamp appointmentTimestamp, String status, String reason,
                       double appointmentFee, String paymentStatus) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.professionalId = professionalId;
        this.professionalName = professionalName;
        this.appointmentTimestamp = appointmentTimestamp;
        this.status = status;
        this.reason = reason;
        this.appointmentFee = appointmentFee;
        this.paymentStatus = paymentStatus;
    }

    // Getters
    @Exclude
    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getProfessionalId() {
        return professionalId;
    }

    public String getProfessionalName() {
        return professionalName;
    }

    public Timestamp getAppointmentTimestamp() {
        return appointmentTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public double getAppointmentFee() {
        return appointmentFee;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    // Setters
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
    }

    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }

    public void setAppointmentTimestamp(Timestamp appointmentTimestamp) {
        this.appointmentTimestamp = appointmentTimestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setAppointmentFee(double appointmentFee) {
        this.appointmentFee = appointmentFee;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
