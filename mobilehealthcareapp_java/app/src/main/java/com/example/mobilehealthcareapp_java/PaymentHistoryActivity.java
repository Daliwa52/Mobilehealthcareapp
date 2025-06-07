package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.Payment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryActivity extends AppCompatActivity {

    private static final String TAG = "PaymentHistoryActivity";

    private RecyclerView recyclerViewPaymentHistory;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private List<Payment> paymentList;
    private TextView textViewNoPaymentsHistory;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String userRole; // "patient" or "professional"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view payment history.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) {
            Toast.makeText(this, "User role not specified for payment history.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerViewPaymentHistory = findViewById(R.id.recyclerViewPaymentHistory);
        textViewNoPaymentsHistory = findViewById(R.id.textViewNoPaymentsHistory);
        progressBar = findViewById(R.id.progressBarPaymentHistory);

        recyclerViewPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        paymentList = new ArrayList<>();
        paymentHistoryAdapter = new PaymentHistoryAdapter(this, paymentList);
        recyclerViewPaymentHistory.setAdapter(paymentHistoryAdapter);

        loadPaymentHistory();
    }

    private void loadPaymentHistory() {
        progressBar.setVisibility(View.VISIBLE);
        textViewNoPaymentsHistory.setVisibility(View.GONE);

        Query query;
        String fieldToQuery = "";

        if ("patient".equals(userRole)) {
            fieldToQuery = "patientId";
        } else if ("professional".equals(userRole)) {
            fieldToQuery = "professionalId";
        } else {
            Toast.makeText(this, "Invalid user role for payment history.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        query = db.collection("payments")
                .whereEqualTo(fieldToQuery, currentUser.getUid())
                .orderBy("paymentTimestamp", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                paymentList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Payment payment = document.toObject(Payment.class);
                    payment.setPaymentId(document.getId()); // Set the document ID
                    paymentList.add(payment);
                }
                paymentHistoryAdapter.updatePaymentHistory(paymentList);

                if (paymentList.isEmpty()) {
                    textViewNoPaymentsHistory.setVisibility(View.VISIBLE);
                } else {
                    textViewNoPaymentsHistory.setVisibility(View.GONE);
                }
            } else {
                Log.w(TAG, "Error getting payment history: ", task.getException());
                Toast.makeText(PaymentHistoryActivity.this, "Error loading payment history.", Toast.LENGTH_SHORT).show();
                textViewNoPaymentsHistory.setVisibility(View.VISIBLE);
                textViewNoPaymentsHistory.setText("Error loading payment history.");
            }
        });
    }
}
