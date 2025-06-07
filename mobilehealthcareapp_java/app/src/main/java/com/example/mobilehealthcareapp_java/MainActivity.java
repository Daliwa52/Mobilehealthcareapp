package com.example.mobilehealthcareapp_java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogout, buttonGoToProfile, buttonGoToProfessionalProfile; // Added buttonGoToProfessionalProfile
    private TextView textViewWelcome;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonLogout = findViewById(R.id.buttonLogout);
        buttonGoToProfile = findViewById(R.id.buttonGoToProfile);
        buttonGoToProfessionalProfile = findViewById(R.id.buttonGoToProfessionalProfile); // Initialize new button
        textViewWelcome = findViewById(R.id.textViewWelcome);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            textViewWelcome.setText(getString(R.string.welcome_message) + " " + currentUser.getEmail());
        }

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonGoToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PatientProfileActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for buttonGoToProfessionalProfile
        buttonGoToProfessionalProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfessionalProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
