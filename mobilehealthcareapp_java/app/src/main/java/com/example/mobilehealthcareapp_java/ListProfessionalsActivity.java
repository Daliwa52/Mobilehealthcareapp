package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mobilehealthcareapp_java.models.HealthcareProfessional;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListProfessionalsActivity extends AppCompatActivity {

    private static final String TAG = "ListProfessionals";

    private RecyclerView recyclerViewProfessionals;
    private ProfessionalAdapter professionalAdapter;
    private List<HealthcareProfessional> professionalList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_professionals);

        recyclerViewProfessionals = findViewById(R.id.recyclerViewProfessionals);
        progressBar = findViewById(R.id.progressBarListProfessionals);

        recyclerViewProfessionals.setLayoutManager(new LinearLayoutManager(this));
        professionalList = new ArrayList<>();
        professionalAdapter = new ProfessionalAdapter(this, professionalList);
        recyclerViewProfessionals.setAdapter(professionalAdapter);

        db = FirebaseFirestore.getInstance();
        loadProfessionals();
    }

    private void loadProfessionals() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("healthcareProfessionals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            professionalList.clear(); // Clear existing list
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HealthcareProfessional professional = document.toObject(HealthcareProfessional.class);
                                professional.setFirebaseId(document.getId()); // Set the document ID
                                professionalList.add(professional);
                            }
                            professionalAdapter.notifyDataSetChanged();
                            if (professionalList.isEmpty()) {
                                Toast.makeText(ListProfessionalsActivity.this, "No healthcare professionals found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(ListProfessionalsActivity.this, "Error loading professionals.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
