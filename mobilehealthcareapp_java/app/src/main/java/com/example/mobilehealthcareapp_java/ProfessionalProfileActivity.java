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
                   buttonViewProfessionalAppointments, buttonViewProfessionalBillingHistory;
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
        buttonViewProfessionalBillingHistory = findViewById(R.id.buttonViewProfessionalBillingHistory);

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
        // Clear previous errors
        editTextProfessionalName.setError(null);
        editTextQualifications.setError(null);
        editTextSpecialties.setError(null);
        editTextAvailability.setError(null);

        String name = editTextProfessionalName.getText().toString().trim();
        String qualifications = editTextQualifications.getText().toString().trim();
        String specialtiesStr = editTextSpecialties.getText().toString().trim();
        String availability = editTextAvailability.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            editTextProfessionalName.setError("Name is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(qualifications)) {
            editTextQualifications.setError("Qualifications are required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(specialtiesStr)) {
            editTextSpecialties.setError("At least one specialty is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(availability)) {
            editTextAvailability.setError("Availability information is required.");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please correct the errors.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming specialties are comma-separated; could be enhanced with a ChipGroup or similar.
        List<String> specialtiesList = Arrays.asList(specialtiesStr.split("\\s*,\\s*"));
        // Filter out empty strings if user puts multiple commas, e.g., "Cardiology,,Pediatrics"
        specialtiesList.removeAll(Arrays.asList("", null));
        if(specialtiesList.isEmpty()){
             editTextSpecialties.setError("Valid specialties are required (comma-separated).");
             Toast.makeText(this, "Please enter valid specialties.", Toast.LENGTH_SHORT).show();
             return;
        }


        HealthcareProfessional professional = new HealthcareProfessional(name, qualifications, specialtiesList, availability);

        db.collection("healthcareProfessionals").document(currentUserId)
                .set(professional)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfessionalProfileActivity.this, R.string.profile_save_success, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfessionalProfileActivity.this, R.string.error_profile_save_failed, Toast.LENGTH_LONG).show();
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
                Log.d(TAG, "No such professional document, profile can be created.");
                 Toast.makeText(ProfessionalProfileActivity.this, R.string.profile_load_no_data, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load profile with error: ", e); // Log the full exception
            Toast.makeText(ProfessionalProfileActivity.this, R.string.error_profile_load_failed, Toast.LENGTH_SHORT).show();
        });
    }
}
