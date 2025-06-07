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

import com.example.mobilehealthcareapp_java.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAppointmentsActivity extends AppCompatActivity {

    private static final String TAG = "ViewAppointments";

    private RecyclerView recyclerViewAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private TextView textViewNoAppointments;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String userRole; // "patient" or "professional"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view appointments.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) {
            Toast.makeText(this, "User role not specified.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        textViewNoAppointments = findViewById(R.id.textViewNoAppointments);
        progressBar = findViewById(R.id.progressBarViewAppointments);

        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, userRole);
        recyclerViewAppointments.setAdapter(appointmentAdapter);

        loadAppointments();
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        textViewNoAppointments.setVisibility(View.GONE);

        Query query;
        if ("patient".equals(userRole)) {
            query = db.collection("appointments")
                    .whereEqualTo("patientId", currentUser.getUid())
                    .orderBy("appointmentTimestamp", Query.Direction.DESCENDING);
        } else if ("professional".equals(userRole)) {
            query = db.collection("appointments")
                    .whereEqualTo("professionalId", currentUser.getUid())
                    .orderBy("appointmentTimestamp", Query.Direction.DESCENDING);
        } else {
            Toast.makeText(this, "Invalid user role.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        query.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                appointmentList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Appointment appointment = document.toObject(Appointment.class);
                    appointment.setAppointmentId(document.getId()); // Set the document ID
                    appointmentList.add(appointment);
                }
                appointmentAdapter.notifyDataSetChanged(); // Use adapter's own update method if it has one for clarity
                // appointmentAdapter.updateAppointments(appointmentList);


                if (appointmentList.isEmpty()) {
                    textViewNoAppointments.setVisibility(View.VISIBLE);
                } else {
                    textViewNoAppointments.setVisibility(View.GONE);
                }
            } else {
                Log.w(TAG, "Error getting appointments: ", task.getException());
                Toast.makeText(ViewAppointmentsActivity.this, "Error loading appointments.", Toast.LENGTH_SHORT).show();
                textViewNoAppointments.setVisibility(View.VISIBLE);
                textViewNoAppointments.setText("Error loading appointments.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload appointments if coming back to this activity, in case an appointment was cancelled
        if (currentUser != null && userRole != null) {
             loadAppointments();
        }
    }
}
