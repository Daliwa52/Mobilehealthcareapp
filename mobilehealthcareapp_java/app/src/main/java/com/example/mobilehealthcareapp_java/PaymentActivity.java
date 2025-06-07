package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.Payment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    public static final String EXTRA_APPOINTMENT_ID = "APPOINTMENT_ID";
    public static final String EXTRA_APPOINTMENT_FEE = "APPOINTMENT_FEE";
    public static final String EXTRA_PROFESSIONAL_NAME = "PROFESSIONAL_NAME";
    public static final String EXTRA_PATIENT_ID = "PATIENT_ID"; // Not strictly needed if current user is always patient
    public static final String EXTRA_PROFESSIONAL_ID = "PROFESSIONAL_ID";
    public static final String EXTRA_APPOINTMENT_TIMESTAMP = "APPOINTMENT_TIMESTAMP";


    private TextView textViewPaymentAppointmentDetails, textViewAppointmentFee;
    private EditText editTextCardNumber, editTextExpiryDate, editTextCVV;
    private Button buttonPayNow;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private String appointmentId;
    private double appointmentFee;
    private String professionalName;
    private String patientId;
    private String professionalId;
    private Timestamp appointmentTimestamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to make a payment.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // patientId = currentUser.getUid(); // Assuming the current user is always the patient making payment

        appointmentId = getIntent().getStringExtra(EXTRA_APPOINTMENT_ID);
        appointmentFee = getIntent().getDoubleExtra(EXTRA_APPOINTMENT_FEE, 0.0);
        professionalName = getIntent().getStringExtra(EXTRA_PROFESSIONAL_NAME);
        patientId = getIntent().getStringExtra(EXTRA_PATIENT_ID); // Passed from adapter
        professionalId = getIntent().getStringExtra(EXTRA_PROFESSIONAL_ID); // Passed from adapter
        long timestampSeconds = getIntent().getLongExtra(EXTRA_APPOINTMENT_TIMESTAMP, -1);
        if (timestampSeconds != -1) {
            appointmentTimestamp = new Timestamp(timestampSeconds, 0);
        }


        if (appointmentId == null || appointmentFee == 0.0 || professionalName == null || patientId == null || professionalId == null || appointmentTimestamp == null) {
            Toast.makeText(this, "Payment details are incomplete.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Incomplete payment details: apptId=" + appointmentId + ", fee=" + appointmentFee +
                               ", profName=" + professionalName + ", patientId=" + patientId + ", profId=" + professionalId + ", timestamp="+timestampSeconds);
            finish();
            return;
        }

        textViewPaymentAppointmentDetails = findViewById(R.id.textViewPaymentAppointmentDetails);
        textViewAppointmentFee = findViewById(R.id.textViewAppointmentFee);
        editTextCardNumber = findViewById(R.id.editTextCardNumber);
        editTextExpiryDate = findViewById(R.id.editTextExpiryDate);
        editTextCVV = findViewById(R.id.editTextCVV);
        buttonPayNow = findViewById(R.id.buttonPayNow);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
        textViewPaymentAppointmentDetails.setText("With " + professionalName + "\non " + sdf.format(appointmentTimestamp.toDate()));
        textViewAppointmentFee.setText(String.format(Locale.US, "$%.2f", appointmentFee));

        buttonPayNow.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        String cardNumber = editTextCardNumber.getText().toString().trim();
        String expiryDate = editTextExpiryDate.getText().toString().trim();
        String cvv = editTextCVV.getText().toString().trim();

        if (TextUtils.isEmpty(cardNumber) || cardNumber.length() < 13) { // Basic check for length
            editTextCardNumber.setError("Valid card number is required.");
            return;
        }
        if (TextUtils.isEmpty(expiryDate) || !expiryDate.matches("\\d{2}/\\d{2}")) { // MM/YY
            editTextExpiryDate.setError("Valid expiry date (MM/YY) is required.");
            return;
        }
        if (TextUtils.isEmpty(cvv) || cvv.length() != 3) {
            editTextCVV.setError("Valid CVV is required.");
            return;
        }

        // Simulate payment processing
        boolean paymentSuccess = new Random().nextInt(10) > 1; // ~80% success rate for simulation

        String paymentStatus = paymentSuccess ? "completed" : "failed";
        String paymentDetails = "Simulated Card Payment - Ending " + (cardNumber.length() > 4 ? cardNumber.substring(cardNumber.length() - 4) : "****");

        Payment payment = new Payment(appointmentId, patientId, professionalId, appointmentFee, paymentStatus, paymentDetails);

        db.collection("payments").add(payment)
                .addOnSuccessListener(documentReference -> {
                    String paymentId = documentReference.getId();
                    payment.setPaymentId(paymentId); // Set the generated ID back if needed elsewhere
                    Log.d(TAG, "Payment record created with ID: " + paymentId + " and status: " + paymentStatus);

                    if (paymentSuccess) {
                        updateAppointmentPaymentStatus("paid");
                    } else {
                        showPaymentResultDialog("Payment Failed", "Your payment could not be processed. Please try again or use a different card.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating payment record", e);
                    Toast.makeText(PaymentActivity.this, "Error recording payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateAppointmentPaymentStatus(String newStatus) {
        db.collection("appointments").document(appointmentId)
                .update("paymentStatus", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment " + appointmentId + " paymentStatus updated to " + newStatus);
                    showPaymentResultDialog("Payment Successful", "Your payment has been processed successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating appointment paymentStatus for " + appointmentId, e);
                    // This is tricky. Payment record might be 'completed' but appointment not updated.
                    // For a real app, more robust error handling/retry or manual reconciliation might be needed.
                    Toast.makeText(PaymentActivity.this, "Payment processed, but error updating appointment. Please contact support.", Toast.LENGTH_LONG).show();
                });
    }

    private void showPaymentResultDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    // Navigate back to the appointment list or profile
                    // Intent intent = new Intent(PaymentActivity.this, ViewAppointmentsActivity.class);
                    // intent.putExtra("USER_ROLE", "patient"); // Assuming patient made the payment
                    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // startActivity(intent);
                    finish(); // Just finish this activity for now
                })
                .setIcon(title.equals("Payment Successful") ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                .show();
    }
}
