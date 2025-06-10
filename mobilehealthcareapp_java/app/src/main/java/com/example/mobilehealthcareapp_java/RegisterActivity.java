package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns; // Import Patterns
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException; // Base class for Firebase Auth exceptions
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException; // For weak password

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmailRegister, editTextPasswordRegister, editTextConfirmPasswordRegister;
    private Button buttonRegister;
    private TextView textViewLoginLink;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        editTextConfirmPasswordRegister = findViewById(R.id.editTextConfirmPasswordRegister);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String email = editTextEmailRegister.getText().toString().trim();
        String password = editTextPasswordRegister.getText().toString().trim();
        String confirmPassword = editTextConfirmPasswordRegister.getText().toString().trim();

        // Clear previous errors
        editTextEmailRegister.setError(null);
        editTextPasswordRegister.setError(null);
        editTextConfirmPasswordRegister.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            editTextEmailRegister.setError("Email is required.");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailRegister.setError("Enter a valid email address.");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPasswordRegister.setError("Password is required.");
            isValid = false;
        } else if (password.length() < 6) {
            editTextPasswordRegister.setError("Password must be at least 6 characters long.");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPasswordRegister.setError("Confirm password is required.");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPasswordRegister.setError("Passwords do not match.");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Show progress bar here if you have one

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress bar here
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to Login or Main activity
                            // It's common to go to Login to make the user sign in explicitly
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                editTextEmailRegister.setError(getString(R.string.error_email_exists));
                                Toast.makeText(RegisterActivity.this, R.string.error_email_exists, Toast.LONG).show();
                            } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                editTextPasswordRegister.setError(getString(R.string.error_weak_password));
                                Toast.makeText(RegisterActivity.this, R.string.error_weak_password, Toast.LONG).show();
                            } else if (task.getException() instanceof FirebaseAuthException) {
                                // Handle other Firebase Auth specific errors
                                Toast.makeText(RegisterActivity.this, "Registration error: " + task.getException().getLocalizedMessage(), Toast.LONG).show();
                            }
                            else {
                                // Generic error for network issues or other problems
                                Toast.makeText(RegisterActivity.this, R.string.error_registration_failed, Toast.LONG).show();
                            }
                        }
                    }
                });
    }
}
