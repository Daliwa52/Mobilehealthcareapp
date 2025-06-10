package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.Appointment;
import com.example.mobilehealthcareapp_java.models.Patient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "ScheduleAppointment";
    private static final double DEFAULT_APPOINTMENT_FEE = 50.0;

    private TextView textViewSelectedProfessionalName;
    private EditText editTextAppointmentDate, editTextAppointmentTime, editTextAppointmentReason;
    private Button buttonConfirmAppointment;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentFirebaseUser;

    private String professionalId;
    private String professionalName;
    private Calendar selectedDateTime = Calendar.getInstance();
    private boolean dateSelected = false;
    private boolean timeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentFirebaseUser = firebaseAuth.getCurrentUser();

        if (currentFirebaseUser == null) {
            Toast.makeText(this, "You need to be logged in to book appointments.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        professionalId = getIntent().getStringExtra("PROFESSIONAL_ID");
        professionalName = getIntent().getStringExtra("PROFESSIONAL_NAME");

        if (professionalId == null || professionalName == null) {
            Toast.makeText(this, "Professional details not found. Cannot schedule appointment.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textViewSelectedProfessionalName = findViewById(R.id.textViewSelectedProfessionalName);
        editTextAppointmentDate = findViewById(R.id.editTextAppointmentDate);
        editTextAppointmentTime = findViewById(R.id.editTextAppointmentTime);
        editTextAppointmentReason = findViewById(R.id.editTextAppointmentReason); // Optional field
        buttonConfirmAppointment = findViewById(R.id.buttonConfirmAppointment);

        textViewSelectedProfessionalName.setText(professionalName);

        editTextAppointmentDate.setOnClickListener(v -> showDatePickerDialog());
        editTextAppointmentTime.setOnClickListener(v -> showTimePickerDialog());

        buttonConfirmAppointment.setOnClickListener(v -> confirmAppointment());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateEditText();
                    dateSelected = true;
                    editTextAppointmentDate.setError(null); // Clear error on selection
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Today onwards
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateTimeEditText();
                    timeSelected = true;
                    editTextAppointmentTime.setError(null); // Clear error on selection
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false // Use false for 24-hour format if desired, true for AM/PM
        );
        timePickerDialog.show();
    }

    private void updateDateEditText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        editTextAppointmentDate.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void updateTimeEditText() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US); // Use "hh:mm a" for AM/PM
        editTextAppointmentTime.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void confirmAppointment() {
        editTextAppointmentDate.setError(null);
        editTextAppointmentTime.setError(null);

        boolean isValid = true;
        if (!dateSelected) {
            editTextAppointmentDate.setError("Please select a date for the appointment.");
            isValid = false;
        }
        if (!timeSelected) {
            editTextAppointmentTime.setError("Please select a time for the appointment.");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please select a valid date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = editTextAppointmentReason.getText().toString().trim(); // Optional

        // Show progress bar here if you have one

        db.collection("patients").document(currentFirebaseUser.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                String patientName = "Unknown Patient"; // Default name
                if (documentSnapshot.exists()) {
                    Patient patient = documentSnapshot.toObject(Patient.class);
                    if (patient != null && !TextUtils.isEmpty(patient.getName())) {
                        patientName = patient.getName();
                    }
                }
                proceedWithBooking(patientName, reason);
            })
            .addOnFailureListener(e -> {
                // Hide progress bar here
                Log.e(TAG, "Error fetching patient details for appointment booking", e);
                Toast.makeText(ScheduleAppointmentActivity.this, R.string.error_fetching_patient_details, Toast.LENGTH_SHORT).show();
            });
    }

    private void proceedWithBooking(String patientName, String reason) {
        Date appointmentDate = selectedDateTime.getTime();
        Timestamp appointmentFirebaseTimestamp = new Timestamp(appointmentDate);

        Appointment appointment = new Appointment(
                currentFirebaseUser.getUid(),
                patientName,
                professionalId,
                professionalName,
                appointmentFirebaseTimestamp,
                "pending_approval", // Default status
                reason,
                DEFAULT_APPOINTMENT_FEE,
                "unpaid" // Initial payment status
        );

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    // Hide progress bar here
                    Toast.makeText(ScheduleAppointmentActivity.this, "Appointment requested successfully! Awaiting approval.", Toast.LENGTH_LONG).show();
                    String newAppointmentId = documentReference.getId();
                    // Update the appointment with its own ID for easier reference if needed
                    db.collection("appointments").document(newAppointmentId).update("appointmentId", newAppointmentId)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Appointment ID updated in document: " + newAppointmentId))
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating appointment with its ID", e));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Hide progress bar here
                    Toast.makeText(ScheduleAppointmentActivity.this, getString(R.string.error_booking_failed_detailed, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error booking appointment", e);
                });
    }
}
