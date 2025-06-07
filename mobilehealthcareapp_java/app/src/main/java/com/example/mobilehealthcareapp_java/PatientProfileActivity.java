package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class PatientProfileActivity extends AppCompatActivity {

    private static final String TAG = "PatientProfileActivity";

    private EditText editTextPatientName, editTextPatientDOB, editTextMedicalHistory, editTextAllergies, editTextMedications;
    private Button buttonSavePatientProfile, buttonUploadDocument, buttonBookAppointment,
                   buttonViewPatientAppointments, buttonViewPatientPaymentHistory; // Added buttonViewPatientPaymentHistory
    private ImageView imageViewPatientProfilePic;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(PatientProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        imageViewPatientProfilePic = findViewById(R.id.imageViewPatientProfilePic);
        editTextPatientName = findViewById(R.id.editTextPatientName);
        editTextPatientDOB = findViewById(R.id.editTextPatientDOB);
        editTextMedicalHistory = findViewById(R.id.editTextMedicalHistory);
        editTextAllergies = findViewById(R.id.editTextAllergies);
        editTextMedications = findViewById(R.id.editTextMedications);
        buttonSavePatientProfile = findViewById(R.id.buttonSavePatientProfile);
        buttonUploadDocument = findViewById(R.id.buttonUploadDocument);
        buttonBookAppointment = findViewById(R.id.buttonBookAppointment);
        buttonViewPatientAppointments = findViewById(R.id.buttonViewPatientAppointments);
        buttonViewPatientPaymentHistory = findViewById(R.id.buttonViewPatientPaymentHistory); // Initialized button

        buttonSavePatientProfile.setOnClickListener(v -> savePatientProfile());

        buttonUploadDocument.setOnClickListener(v -> Toast.makeText(PatientProfileActivity.this, "Document upload feature coming soon!", Toast.LENGTH_SHORT).show());

        buttonBookAppointment.setOnClickListener(v -> startActivity(new Intent(PatientProfileActivity.this, ListProfessionalsActivity.class)));

        buttonViewPatientAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(PatientProfileActivity.this, ViewAppointmentsActivity.class);
            intent.putExtra("USER_ROLE", "patient");
            startActivity(intent);
        });

        buttonViewPatientPaymentHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PatientProfileActivity.this, PaymentHistoryActivity.class);
            intent.putExtra("USER_ROLE", "patient");
            startActivity(intent);
        });

        loadPatientProfile();
    }

    private void savePatientProfile() {
        String name = editTextPatientName.getText().toString().trim();
        String dob = editTextPatientDOB.getText().toString().trim();
        String medicalHistory = editTextMedicalHistory.getText().toString().trim();
        String allergiesStr = editTextAllergies.getText().toString().trim();
        String medicationsStr = editTextMedications.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextPatientName.setError("Name is required.");
            return;
        }

        List<String> allergiesList = TextUtils.isEmpty(allergiesStr) ? Arrays.asList() : Arrays.asList(allergiesStr.split("\\s*,\\s*"));
        List<String> medicationsList = TextUtils.isEmpty(medicationsStr) ? Arrays.asList() : Arrays.asList(medicationsStr.split("\\s*,\\s*"));

        Patient patient = new Patient(name, dob, medicalHistory, allergiesList, medicationsList);

        db.collection("patients").document(currentUserId)
                .set(patient)
                .addOnSuccessListener(aVoid -> Toast.makeText(PatientProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(PatientProfileActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving profile", e);
                });
    }

    private void loadPatientProfile() {
        if (currentUserId == null) return;

        DocumentReference docRef = db.collection("patients").document(currentUserId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Patient patient = document.toObject(Patient.class);
                    if (patient != null) {
                        editTextPatientName.setText(patient.getName());
                        editTextPatientDOB.setText(patient.getDateOfBirth());
                        editTextMedicalHistory.setText(patient.getMedicalHistory());
                        if (patient.getAllergies() != null) {
                            editTextAllergies.setText(TextUtils.join(", ", patient.getAllergies()));
                        }
                        if (patient.getMedications() != null) {
                            editTextMedications.setText(TextUtils.join(", ", patient.getMedications()));
                        }
                    }
                } else {
                    Log.d(TAG, "No such patient document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                Toast.makeText(PatientProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
