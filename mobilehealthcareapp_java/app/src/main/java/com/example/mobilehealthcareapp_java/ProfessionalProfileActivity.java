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

import com.example.mobilehealthcareapp_java.models.HealthcareProfessional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class ProfessionalProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfProfileActivity";

    private EditText editTextProfessionalName, editTextQualifications, editTextSpecialties, editTextAvailability;
    private Button buttonSaveProfessionalProfile, buttonManageSchedule,
                   buttonViewProfessionalAppointments, buttonViewProfessionalBillingHistory; // Added buttonViewProfessionalBillingHistory
    private ImageView imageViewProfessionalProfilePic;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(ProfessionalProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        imageViewProfessionalProfilePic = findViewById(R.id.imageViewProfessionalProfilePic);
        editTextProfessionalName = findViewById(R.id.editTextProfessionalName);
        editTextQualifications = findViewById(R.id.editTextQualifications);
        editTextSpecialties = findViewById(R.id.editTextSpecialties);
        editTextAvailability = findViewById(R.id.editTextAvailability);
        buttonSaveProfessionalProfile = findViewById(R.id.buttonSaveProfessionalProfile);
        buttonManageSchedule = findViewById(R.id.buttonManageSchedule);
        buttonViewProfessionalAppointments = findViewById(R.id.buttonViewProfessionalAppointments);
        buttonViewProfessionalBillingHistory = findViewById(R.id.buttonViewProfessionalBillingHistory); // Initialized button

        buttonSaveProfessionalProfile.setOnClickListener(v -> saveProfessionalProfile());

        buttonManageSchedule.setOnClickListener(v -> Toast.makeText(ProfessionalProfileActivity.this, "Schedule management feature coming soon!", Toast.LENGTH_SHORT).show());

        buttonViewProfessionalAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(ProfessionalProfileActivity.this, ViewAppointmentsActivity.class);
            intent.putExtra("USER_ROLE", "professional");
            startActivity(intent);
        });

        buttonViewProfessionalBillingHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfessionalProfileActivity.this, PaymentHistoryActivity.class);
            intent.putExtra("USER_ROLE", "professional");
            startActivity(intent);
        });

        loadProfessionalProfile();
    }

    private void saveProfessionalProfile() {
        String name = editTextProfessionalName.getText().toString().trim();
        String qualifications = editTextQualifications.getText().toString().trim();
        String specialtiesStr = editTextSpecialties.getText().toString().trim();
        String availability = editTextAvailability.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(qualifications) || TextUtils.isEmpty(specialtiesStr) || TextUtils.isEmpty(availability)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> specialtiesList = Arrays.asList(specialtiesStr.split("\\s*,\\s*"));
        HealthcareProfessional professional = new HealthcareProfessional(name, qualifications, specialtiesList, availability);

        db.collection("healthcareProfessionals").document(currentUserId)
                .set(professional)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfessionalProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfessionalProfileActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving profile", e);
                });
    }

    private void loadProfessionalProfile() {
        if (currentUserId == null) return;

        DocumentReference docRef = db.collection("healthcareProfessionals").document(currentUserId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                HealthcareProfessional professional = documentSnapshot.toObject(HealthcareProfessional.class);
                if (professional != null) {
                    editTextProfessionalName.setText(professional.getName());
                    editTextQualifications.setText(professional.getQualifications());
                    if (professional.getSpecialties() != null) {
                        editTextSpecialties.setText(TextUtils.join(", ", professional.getSpecialties()));
                    }
                    editTextAvailability.setText(professional.getAvailability());
                }
            } else {
                Log.d(TAG, "No such professional document");
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "get failed with ", e);
            Toast.makeText(ProfessionalProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
        });
    }
}
