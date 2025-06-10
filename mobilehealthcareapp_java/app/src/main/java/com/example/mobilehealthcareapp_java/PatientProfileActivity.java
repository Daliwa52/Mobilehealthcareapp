package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.DatePickerDialog;
import android.app.ProgressDialog; // For upload progress
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.MedicalDocument;
import com.example.mobilehealthcareapp_java.models.Patient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // For orderBy
import com.google.firebase.firestore.QuerySnapshot; // For get() result
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener; // For progress
import com.google.firebase.storage.StorageReference; // For Storage
import com.google.firebase.storage.UploadTask; // For Storage

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PatientProfileActivity extends AppCompatActivity implements MedicalDocumentAdapter.OnDocumentActionListener {

    private static final String TAG = "PatientProfileActivity";
    private static final Pattern DOB_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final int PICK_FILE_REQUEST_CODE = 101;

    private EditText editTextPatientName, editTextPatientDOB, editTextMedicalHistory, editTextAllergies, editTextMedications;
    private Button buttonSavePatientProfile, buttonUploadDocument, buttonBookAppointment,
                   buttonViewPatientAppointments, buttonViewPatientPaymentHistory;
    private ImageView imageViewPatientProfilePic;
    private RecyclerView recyclerViewMedicalDocuments;
    private TextView textViewNoDocuments;
    private MedicalDocumentAdapter medicalDocumentAdapter;
    private List<MedicalDocument> medicalDocumentList;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private String currentUserId;
    private Calendar dobCalendar = Calendar.getInstance();
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
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
        buttonViewPatientPaymentHistory = findViewById(R.id.buttonViewPatientPaymentHistory);

        recyclerViewMedicalDocuments = findViewById(R.id.recyclerViewMedicalDocuments);
        textViewNoDocuments = findViewById(R.id.textViewNoDocuments);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Document...");
        progressDialog.setCancelable(false);

        setupRecyclerView();

        editTextPatientDOB.setOnClickListener(v -> showDatePickerDialog());
        buttonSavePatientProfile.setOnClickListener(v -> savePatientProfile());
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

        buttonUploadDocument.setOnClickListener(v -> openFilePicker());

        loadPatientProfile();
        loadMedicalDocuments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh documents list when returning to the activity
        if (currentUser != null) { // Ensure user is still valid
            loadMedicalDocuments();
        }
    }

    private void setupRecyclerView() {
        medicalDocumentList = new ArrayList<>();
        medicalDocumentAdapter = new MedicalDocumentAdapter(this, medicalDocumentList, this);
        recyclerViewMedicalDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMedicalDocuments.setAdapter(medicalDocumentAdapter);
        updateNoDocumentsView();
    }

    private void updateNoDocumentsView() {
        if (medicalDocumentList.isEmpty()) {
            textViewNoDocuments.setVisibility(View.VISIBLE);
            recyclerViewMedicalDocuments.setVisibility(View.GONE);
        } else {
            textViewNoDocuments.setVisibility(View.GONE);
            recyclerViewMedicalDocuments.setVisibility(View.VISIBLE);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimetypes = {"application/pdf", "image/jpeg", "image/png", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_medical_document_title)), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.file_picker_no_manager, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            String originalFileName = getFileNameFromUri(fileUri);
            String fileType = getContentResolver().getType(fileUri);

            if (originalFileName == null || fileType == null) {
                Toast.makeText(this, "Could not get file details. Please try a different file or app.", Toast.LENGTH_LONG).show();
                return;
            }

            uploadFileToStorage(fileUri, originalFileName, fileType);
        }
    }

    private void uploadFileToStorage(Uri fileUri, String originalFileName, String fileType) {
        if (currentUserId == null) return;
        progressDialog.setMessage("Uploading " + originalFileName + "...");
        progressDialog.show();

        String storageFileName = currentUserId + "/" + System.currentTimeMillis() + "_" + originalFileName;
        StorageReference storageRef = storage.getReference("medical_documents/" + storageFileName);
        UploadTask uploadTask = storageRef.putFile(fileUri);

        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressDialog.setProgress((int) progress);
        }).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    MedicalDocument document = new MedicalDocument(originalFileName, downloadUri.toString(), fileType, storageRef.getPath());
                    // 'uploadedAt' will be set by Firestore @ServerTimestamp
                    saveDocumentMetadataToFirestore(document);
                } else {
                    Toast.makeText(PatientProfileActivity.this, "Upload succeeded but could not get download URL.", Toast.LONG).show();
                }
            } else {
                Log.e(TAG, "Upload failed: ", task.getException());
                Toast.makeText(PatientProfileActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDocumentMetadataToFirestore(MedicalDocument document) {
        if (currentUserId == null) return;
        db.collection("patients").document(currentUserId).collection("medicalDocuments")
            .add(document)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, document.getFileName() + " uploaded and metadata saved!", Toast.LENGTH_SHORT).show();
                loadMedicalDocuments(); // Refresh the list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save document metadata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error saving document metadata to Firestore", e);
                // Consider deleting the uploaded file from Storage if metadata save fails critically
            });
    }

    private void loadMedicalDocuments() {
        if (currentUserId == null) {
            updateNoDocumentsView(); // Ensure UI reflects no data if user is somehow null
            return;
        }
        // Show progress bar or some loading indicator if desired
        db.collection("patients").document(currentUserId).collection("medicalDocuments")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                // Hide progress bar
                if (task.isSuccessful()) {
                    medicalDocumentList.clear();
                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            MedicalDocument doc = snapshot.toObject(MedicalDocument.class);
                            doc.setDocumentId(snapshot.getId()); // Store Firestore document ID
                            medicalDocumentList.add(doc);
                        }
                    }
                    medicalDocumentAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error loading medical documents: ", task.getException());
                    Toast.makeText(this, "Failed to load medical documents.", Toast.LENGTH_SHORT).show();
                }
                updateNoDocumentsView(); // Update visibility based on new list content
            });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                         fileName = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            if (fileName != null) {
                int cut = fileName.lastIndexOf('/');
                if (cut != -1) {
                    fileName = fileName.substring(cut + 1);
                }
            } else { // Fallback if path is null
                fileName = "unknown_file";
            }
        }
        return fileName;
    }

    private void showDatePickerDialog() {
        // ... (remains the same)
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            dobCalendar.set(Calendar.YEAR, year);
            dobCalendar.set(Calendar.MONTH, month);
            dobCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDOBEditText();
        };

        new DatePickerDialog(PatientProfileActivity.this, dateSetListener,
                dobCalendar.get(Calendar.YEAR),
                dobCalendar.get(Calendar.MONTH),
                dobCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDOBEditText() {
        // ... (remains the same)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        editTextPatientDOB.setText(sdf.format(dobCalendar.getTime()));
        editTextPatientDOB.setError(null);
    }

    private void savePatientProfile() {
        // ... (remains the same as previously reviewed) ...
        editTextPatientName.setError(null);
        editTextPatientDOB.setError(null);
        String name = editTextPatientName.getText().toString().trim();
        String dob = editTextPatientDOB.getText().toString().trim();
        String medicalHistory = editTextMedicalHistory.getText().toString().trim();
        String allergiesStr = editTextAllergies.getText().toString().trim();
        String medicationsStr = editTextMedications.getText().toString().trim();
        boolean isValid = true;
        if (TextUtils.isEmpty(name)) {
            editTextPatientName.setError("Name is required.");
            isValid = false;
        }
        if (TextUtils.isEmpty(dob)) {
            editTextPatientDOB.setError("Date of Birth is required.");
            isValid = false;
        }
        if (!isValid) {
            Toast.makeText(this, "Please correct the errors.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> allergiesList = TextUtils.isEmpty(allergiesStr) ? Arrays.asList() : Arrays.asList(allergiesStr.split("\\s*,\\s*"));
        List<String> medicationsList = TextUtils.isEmpty(medicationsStr) ? Arrays.asList() : Arrays.asList(medicationsStr.split("\\s*,\\s*"));
        Patient patient = new Patient(name, dob, medicalHistory, allergiesList, medicationsList);
        db.collection("patients").document(currentUserId)
                .set(patient)
                .addOnSuccessListener(aVoid -> Toast.makeText(PatientProfileActivity.this, R.string.profile_save_success, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(PatientProfileActivity.this, R.string.error_profile_save_failed, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving profile", e);
                });
    }

    private void loadPatientProfile() {
        // ... (remains the same as previously reviewed) ...
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
                        if(patient.getDateOfBirth() != null && DOB_PATTERN.matcher(patient.getDateOfBirth()).matches()){
                            try {
                                String[] parts = patient.getDateOfBirth().split("-");
                                dobCalendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                                dobCalendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                                dobCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                            } catch (NumberFormatException e){
                                Log.e(TAG, "Error parsing DOB from Firestore for DatePickerDialog", e);
                            }
                        }
                        editTextMedicalHistory.setText(patient.getMedicalHistory());
                        if (patient.getAllergies() != null) {
                            editTextAllergies.setText(TextUtils.join(", ", patient.getAllergies()));
                        }
                        if (patient.getMedications() != null) {
                            editTextMedications.setText(TextUtils.join(", ", patient.getMedications()));
                        }
                    }
                } else {
                    Log.d(TAG, "No such patient document, profile can be created.");
                    Toast.makeText(PatientProfileActivity.this, R.string.profile_load_no_data, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to load profile with error: ", task.getException());
                Toast.makeText(PatientProfileActivity.this, R.string.error_profile_load_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewDocument(MedicalDocument document) {
        if (document.getDownloadUrl() != null && !document.getDownloadUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = document.getFileType();
            if (type == null || type.isEmpty()) {
                // Fallback if fileType is somehow null or empty, though unlikely with current setup
                type = "*/*";
            }
            intent.setDataAndType(Uri.parse(document.getDownloadUrl()), type);
            // FLAG_ACTIVITY_NO_HISTORY: If you don't want the viewer app to be in the history stack.
            // FLAG_GRANT_READ_URI_PERMISSION: Generally for content URIs, but good for robustness.
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                // Using createChooser to give user a choice if multiple apps can handle the type
                startActivity(Intent.createChooser(intent, "Open with..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No application found to open " + type + ".", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ActivityNotFoundException for type: " + type, ex);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open document.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error viewing document", e);
            }
        } else {
            Toast.makeText(this, "Document URL not available or invalid.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteDocument(MedicalDocument document, int position) {
        handleDeleteDocumentConfirmation(document, position);
    }

    private void handleDeleteDocumentConfirmation(MedicalDocument document, final int position) {
        if (document == null || TextUtils.isEmpty(document.getFileName())) {
            Toast.makeText(this, "Cannot delete: document data is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to delete \"" + document.getFileName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteDocumentFromStorage(document, position))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteDocumentFromStorage(MedicalDocument document, final int position) {
        if (TextUtils.isEmpty(document.getStoragePath())) {
            Toast.makeText(this, "Cannot delete: storage path is missing.", Toast.LENGTH_SHORT).show();
            // If storage path is missing, but metadata exists, attempt to delete metadata only.
            deleteDocumentMetadataFromFirestore(document, position, true); // pass a flag indicating storage part failed/skipped
            return;
        }
        progressDialog.setMessage("Deleting " + document.getFileName() + "...");
        progressDialog.show();

        StorageReference fileRef = storage.getReferenceFromUrl(document.getDownloadUrl()); // Or storage.getReference(document.getStoragePath());

        fileRef.delete().addOnSuccessListener(aVoid -> {
            // File deleted from Storage successfully, now delete from Firestore
            deleteDocumentMetadataFromFirestore(document, position, false);
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(PatientProfileActivity.this, "Failed to delete from Storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error deleting from Firebase Storage", e);
            // Optionally, ask user if they want to try deleting metadata anyway, or just log the inconsistency.
        });
    }

    private void deleteDocumentMetadataFromFirestore(MedicalDocument document, final int position, boolean storageDeletionFailed) {
        if (currentUserId == null || TextUtils.isEmpty(document.getDocumentId())) {
            progressDialog.dismiss(); // Ensure progress dialog is dismissed
            Toast.makeText(this, "Cannot delete metadata: critical information missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("patients").document(currentUserId).collection("medicalDocuments").document(document.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    if (!storageDeletionFailed) {
                        Toast.makeText(PatientProfileActivity.this, "\"" + document.getFileName() + "\" deleted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                         Toast.makeText(PatientProfileActivity.this, "Metadata deleted, but file might still exist in storage due to an earlier error.", Toast.LENGTH_LONG).show();
                    }
                    medicalDocumentAdapter.removeItem(position);
                    updateNoDocumentsView();
                    // loadMedicalDocuments(); // Or just remove from adapter if that's preferred
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PatientProfileActivity.this, "Failed to delete document metadata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error deleting document metadata from Firestore", e);
                     if (!storageDeletionFailed) {
                        // This means storage deletion was successful but metadata deletion failed. This is an orphaned file situation.
                        // Log this or provide instructions to user/admin for manual cleanup if necessary.
                        Log.w(TAG, "Orphaned file in Storage: " + document.getStoragePath() + " as Firestore metadata deletion failed.");
                    }
                });
    }
}
