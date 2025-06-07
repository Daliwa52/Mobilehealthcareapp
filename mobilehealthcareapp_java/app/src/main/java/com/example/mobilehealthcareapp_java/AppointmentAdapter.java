package com.example.mobilehealthcareapp_java;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilehealthcareapp_java.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private static final String TAG = "AppointmentAdapter";
    private Context context;
    private List<Appointment> appointmentList;
    private FirebaseFirestore db;
    private String userRole; // "patient" or "professional"
    private FirebaseUser currentUser;

    // Define a fixed fee for now, or pass it if available from professional's profile later
    private static final double SIMULATED_APPOINTMENT_FEE = 50.0;

    public AppointmentAdapter(Context context, List<Appointment> appointmentList, String userRole) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.db = FirebaseFirestore.getInstance();
        this.userRole = userRole;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        String otherParticipantId = null;
        String otherParticipantName = null;

        if ("patient".equals(userRole)) {
            holder.textViewAppointmentWithLabel.setText("Appointment With:");
            holder.textViewAppointmentName.setText(appointment.getProfessionalName());
            otherParticipantId = appointment.getProfessionalId();
            otherParticipantName = appointment.getProfessionalName();
        } else if ("professional".equals(userRole)) {
            holder.textViewAppointmentWithLabel.setText("Appointment For:");
            holder.textViewAppointmentName.setText(appointment.getPatientName());
            otherParticipantId = appointment.getPatientId();
            otherParticipantName = appointment.getPatientName();
        } else {
             holder.textViewAppointmentWithLabel.setText("Appointment Details:");
             holder.textViewAppointmentName.setText("Patient: " + appointment.getPatientName() + " / Prof: " + appointment.getProfessionalName());
        }

        if (appointment.getAppointmentTimestamp() != null) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.US);
            holder.textViewAppointmentDateTime.setText(
                    "Date: " + sdfDate.format(appointment.getAppointmentTimestamp().toDate()) +
                    ", Time: " + sdfTime.format(appointment.getAppointmentTimestamp().toDate())
            );
        } else {
            holder.textViewAppointmentDateTime.setText("Date/Time: Not set");
        }

        String statusText = "Status: " + appointment.getStatus();
        if ("patient".equals(userRole) && "unpaid".equalsIgnoreCase(appointment.getPaymentStatus()) &&
            ("scheduled".equalsIgnoreCase(appointment.getStatus()) || "completed".equalsIgnoreCase(appointment.getStatus())) ) {
            statusText += " (Payment Due)";
        } else if (!"unpaid".equalsIgnoreCase(appointment.getPaymentStatus())) {
            statusText += " (Paid)";
        }
        holder.textViewAppointmentStatus.setText(statusText);


        if (!TextUtils.isEmpty(appointment.getReason())) {
            holder.textViewAppointmentReason.setText("Reason: " + appointment.getReason());
            holder.textViewAppointmentReason.setVisibility(View.VISIBLE);
        } else {
            holder.textViewAppointmentReason.setVisibility(View.GONE);
        }

        boolean canCancel = "scheduled".equalsIgnoreCase(appointment.getStatus()) || "pending_approval".equalsIgnoreCase(appointment.getStatus());
        holder.buttonCancelAppointment.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        boolean canChat = "scheduled".equalsIgnoreCase(appointment.getStatus()) || "completed".equalsIgnoreCase(appointment.getStatus());
        holder.buttonChat.setVisibility(canChat ? View.VISIBLE : View.GONE);

        // Payment button visibility (only for patients, for unpaid scheduled/completed appointments)
        if ("patient".equals(userRole) && "unpaid".equalsIgnoreCase(appointment.getPaymentStatus()) &&
            ("scheduled".equalsIgnoreCase(appointment.getStatus()) || "completed".equalsIgnoreCase(appointment.getStatus()))) {
            holder.buttonMakePayment.setVisibility(View.VISIBLE);
        } else {
            holder.buttonMakePayment.setVisibility(View.GONE);
        }


        holder.buttonCancelAppointment.setOnClickListener(v -> {
            if (appointment.getAppointmentId() != null && !appointment.getAppointmentId().isEmpty()) {
                cancelAppointment(appointment.getAppointmentId(), position);
            } else {
                Toast.makeText(context, "Error: Appointment ID is missing for cancel.", Toast.LENGTH_SHORT).show();
            }
        });

        final String finalOtherParticipantId = otherParticipantId;
        final String finalOtherParticipantName = otherParticipantName;
        holder.buttonChat.setOnClickListener(v -> {
            if (currentUser != null && finalOtherParticipantId != null) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_RECEIVER_ID, finalOtherParticipantId);
                intent.putExtra(ChatActivity.EXTRA_RECEIVER_NAME, finalOtherParticipantName);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Error: Cannot initiate chat.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.buttonMakePayment.setOnClickListener(v -> {
            if (appointment.getAppointmentId() == null || appointment.getPatientId() == null || appointment.getProfessionalId() == null || appointment.getAppointmentTimestamp() == null) {
                 Toast.makeText(context, "Error: Missing critical appointment details for payment.", Toast.LENGTH_LONG).show();
                 return;
            }
            Intent intent = new Intent(context, PaymentActivity.class);
            intent.putExtra(PaymentActivity.EXTRA_APPOINTMENT_ID, appointment.getAppointmentId());
            // Assuming appointmentFee is now part of the Appointment model. If not, use SIMULATED_APPOINTMENT_FEE
            intent.putExtra(PaymentActivity.EXTRA_APPOINTMENT_FEE, appointment.getAppointmentFee() > 0 ? appointment.getAppointmentFee() : SIMULATED_APPOINTMENT_FEE);
            intent.putExtra(PaymentActivity.EXTRA_PROFESSIONAL_NAME, appointment.getProfessionalName());
            intent.putExtra(PaymentActivity.EXTRA_PATIENT_ID, appointment.getPatientId());
            intent.putExtra(PaymentActivity.EXTRA_PROFESSIONAL_ID, appointment.getProfessionalId());
            intent.putExtra(PaymentActivity.EXTRA_APPOINTMENT_TIMESTAMP, appointment.getAppointmentTimestamp().getSeconds());
            context.startActivity(intent);
        });
    }

    private void cancelAppointment(String appointmentId, int position) {
        db.collection("appointments").document(appointmentId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Appointment cancelled.", Toast.LENGTH_SHORT).show();
                    if (position != RecyclerView.NO_POSITION && position < appointmentList.size()) {
                         appointmentList.get(position).setStatus("cancelled");
                         appointmentList.get(position).setPaymentStatus("refund_pending"); // Or similar, if applicable
                         notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to cancel appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error cancelling appointment", e);
                });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAppointmentName, textViewAppointmentDateTime, textViewAppointmentStatus, textViewAppointmentReason, textViewAppointmentWithLabel;
        Button buttonCancelAppointment, buttonChat, buttonMakePayment; // Added buttonMakePayment

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAppointmentWithLabel = itemView.findViewById(R.id.textViewAppointmentWithLabel);
            textViewAppointmentName = itemView.findViewById(R.id.textViewAppointmentName);
            textViewAppointmentDateTime = itemView.findViewById(R.id.textViewAppointmentDateTime);
            textViewAppointmentStatus = itemView.findViewById(R.id.textViewAppointmentStatus);
            textViewAppointmentReason = itemView.findViewById(R.id.textViewAppointmentReason);
            buttonCancelAppointment = itemView.findViewById(R.id.buttonCancelAppointment);
            buttonChat = itemView.findViewById(R.id.buttonChat);
            buttonMakePayment = itemView.findViewById(R.id.buttonMakePayment); // Initialized buttonMakePayment
        }
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        this.appointmentList.clear();
        this.appointmentList.addAll(newAppointments);
        notifyDataSetChanged();
    }
}
