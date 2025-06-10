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
import java.util.regex.Pattern; // For card validation patterns

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    public static final String EXTRA_APPOINTMENT_ID = "APPOINTMENT_ID";
    public static final String EXTRA_APPOINTMENT_FEE = "APPOINTMENT_FEE";
    public static final String EXTRA_PROFESSIONAL_NAME = "PROFESSIONAL_NAME";
    public static final String EXTRA_PATIENT_ID = "PATIENT_ID";
    public static final String EXTRA_PROFESSIONAL_ID = "PROFESSIONAL_ID";
    public static final String EXTRA_APPOINTMENT_TIMESTAMP = "APPOINTMENT_TIMESTAMP";

    // Basic regex for card details (can be improved for real-world use)
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern EXPIRY_DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])\\/([0-9]{2})$"); // MM/YY
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");


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

        appointmentId = getIntent().getStringExtra(EXTRA_APPOINTMENT_ID);
        appointmentFee = getIntent().getDoubleExtra(EXTRA_APPOINTMENT_FEE, 0.0);
        professionalName = getIntent().getStringExtra(EXTRA_PROFESSIONAL_NAME);
        patientId = getIntent().getStringExtra(EXTRA_PATIENT_ID);
        professionalId = getIntent().getStringExtra(EXTRA_PROFESSIONAL_ID);
        long timestampSeconds = getIntent().getLongExtra(EXTRA_APPOINTMENT_TIMESTAMP, -1);
        if (timestampSeconds != -1) {
            appointmentTimestamp = new Timestamp(timestampSeconds, 0);
        }


        if (appointmentId == null || appointmentFee <= 0.0 || professionalName == null || patientId == null || professionalId == null || appointmentTimestamp == null) {
            Toast.makeText(this, "Payment details are incomplete or invalid. Cannot proceed.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Incomplete payment details: apptId=" + appointmentId + ", fee=" + appointmentFee +
                               ", profName=" + professionalName + ", patientId=" + patientId + ", profId=" + professionalId + ", timestamp="+(timestampSeconds == -1 ? "null" : timestampSeconds));
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
        textViewPaymentAppointmentDetails.setText(getString(R.string.payment_details_intro, professionalName, sdf.format(appointmentTimestamp.toDate())));
        textViewAppointmentFee.setText(String.format(Locale.US, "$%.2f", appointmentFee));

        buttonPayNow.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        // Clear previous errors
        editTextCardNumber.setError(null);
        editTextExpiryDate.setError(null);
        editTextCVV.setError(null);

        String cardNumber = editTextCardNumber.getText().toString().trim();
        String expiryDate = editTextExpiryDate.getText().toString().trim();
        String cvv = editTextCVV.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(cardNumber)) {
            editTextCardNumber.setError("Card number is required.");
            isValid = false;
        } else if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
            editTextCardNumber.setError("Enter a valid card number (13-19 digits).");
            isValid = false;
        }

        if (TextUtils.isEmpty(expiryDate)) {
            editTextExpiryDate.setError("Expiry date is required.");
            isValid = false;
        } else if (!EXPIRY_DATE_PATTERN.matcher(expiryDate).matches()) {
            editTextExpiryDate.setError("Enter a valid expiry date (MM/YY).");
            isValid = false;
        } else {
            // Basic check for expiry date plausibility (e.g., not in the distant past, valid month)
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]); // Assuming 21st century
            Calendar currentCal = Calendar.getInstance();
            Calendar expiryCal = Calendar.getInstance();
            expiryCal.set(Calendar.YEAR, year);
            expiryCal.set(Calendar.MONTH, month -1); // Calendar month is 0-indexed
            expiryCal.set(Calendar.DAY_OF_MONTH, expiryCal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Last day of expiry month

            if (expiryCal.before(currentCal)) {
                editTextExpiryDate.setError("Card has expired.");
                isValid = false;
            }
        }


        if (TextUtils.isEmpty(cvv)) {
            editTextCVV.setError("CVV is required.");
            isValid = false;
        } else if (!CVV_PATTERN.matcher(cvv).matches()) {
            editTextCVV.setError("Enter a valid CVV (3 or 4 digits).");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please correct the card details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar here if you have one
        buttonPayNow.setEnabled(false); // Prevent multiple clicks

        // Simulate payment processing
        boolean paymentSuccess = new Random().nextInt(10) > 1; // ~80% success rate

        String paymentStatus = paymentSuccess ? "completed" : "failed";
        String paymentMethod = "Simulated Card - Ending " + (cardNumber.length() > 4 ? cardNumber.substring(cardNumber.length() - 4) : "****");

        Payment payment = new Payment(appointmentId, patientId, professionalId, appointmentFee, paymentStatus, paymentMethod);

        db.collection("payments").add(payment)
                .addOnSuccessListener(documentReference -> {
                    String paymentId = documentReference.getId();
                    Log.d(TAG, "Payment record created: " + paymentId + ", Status: " + paymentStatus);

                    if (paymentSuccess) {
                        updateAppointmentPaymentStatus("paid");
                    } else {
                        buttonPayNow.setEnabled(true); // Re-enable button
                        showPaymentResultDialog("Payment Failed", "Your payment could not be processed. Please check your card details or try again later.");
                    }
                })
                .addOnFailureListener(e -> {
                    buttonPayNow.setEnabled(true); // Re-enable button
                    Log.e(TAG, "Error creating payment record", e);
                    Toast.makeText(PaymentActivity.this, getString(R.string.error_payment_record_failed, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void updateAppointmentPaymentStatus(String newStatus) {
        db.collection("appointments").document(appointmentId)
                .update("paymentStatus", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment " + appointmentId + " paymentStatus updated to " + newStatus);
                    showPaymentResultDialog("Payment Successful", "Your payment of $" + String.format(Locale.US, "%.2f", appointmentFee) + " has been processed successfully.");
                })
                .addOnFailureListener(e -> {
                    buttonPayNow.setEnabled(true); // Re-enable button as main action failed
                    Log.e(TAG, "Error updating appointment paymentStatus for " + appointmentId, e);
                    Toast.makeText(PaymentActivity.this, R.string.error_payment_appointment_update_failed, Toast.LENGTH_LONG).show();
                });
    }

    private void showPaymentResultDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    if (title.contains("Successful")) { // Only finish if successful
                        finish();
                    }
                })
                .setIcon(title.contains("Successful") ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                .setCancelable(false) // User must acknowledge result
                .show();
    }
}
