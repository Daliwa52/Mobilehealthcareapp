package com.example.mobilehealthcareapp_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns; // Import Patterns
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException; // Base class
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmailLogin, editTextPasswordLogin;
    private Button buttonLogin;
    private TextView textViewRegisterLink, textViewPasswordResetLink;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        // If a user is already logged in, redirect to MainActivity
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return; // Important to return to prevent further execution of onCreate
        }

        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);
        textViewPasswordResetLink = findViewById(R.id.textViewPasswordResetLink);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        textViewRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        textViewPasswordResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
    }

    private void loginUser() {
        String email = editTextEmailLogin.getText().toString().trim();
        String password = editTextPasswordLogin.getText().toString().trim();

        // Clear previous errors
        editTextEmailLogin.setError(null);
        editTextPasswordLogin.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            editTextEmailLogin.setError("Email is required.");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailLogin.setError("Enter a valid email address.");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPasswordLogin.setError("Password is required.");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Show progress bar here if you have one

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress bar here
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show(); // Added R.string
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                editTextEmailLogin.setError(getString(R.string.error_user_not_found));
                                Toast.makeText(LoginActivity.this, R.string.error_user_not_found, Toast.LONG).show();
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                editTextPasswordLogin.setError(getString(R.string.error_incorrect_password));
                                Toast.makeText(LoginActivity.this, R.string.error_incorrect_password, Toast.LONG).show();
                            } else if (task.getException() instanceof FirebaseAuthException) {
                                Toast.makeText(LoginActivity.this, "Login error: " + task.getException().getLocalizedMessage(), Toast.LONG).show();
                            }
                            else {
                                Toast.makeText(LoginActivity.this, R.string.error_login_failed_generic, Toast.LONG).show();
                            }
                        }
                    }
                });
    }

    private void showPasswordResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set up the input
        final EditText inputEmail = new EditText(this);
        inputEmail.setHint(R.string.hint_email); // Use string resource
        inputEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // It's better to inflate a custom layout for dialogs for more control
        // For simplicity, using a direct EditText here.
        // Consider creating dialog_password_reset.xml and inflating it as done previously if more complex UI is needed.
        // If dialog_password_reset.xml with editTextEmailReset exists from previous steps, use that.
        // For this review, let's assume the simple EditText is fine, or reuse the existing dialog if it fits.

        // Re-using the existing dialog structure if it's suitable:
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password_reset, null); // Assuming this layout exists and has editTextEmailReset
        final EditText emailEditTextInDialog = dialogView.findViewById(R.id.editTextEmailReset); // Ensure this ID is in dialog_password_reset.xml
        builder.setView(dialogView);
        builder.setTitle(R.string.title_password_reset);
        // builder.setMessage("Enter your email to receive password reset instructions."); // Can be part of the dialog's layout

        builder.setPositiveButton(R.string.button_reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailForReset = emailEditTextInDialog.getText().toString().trim();
                emailEditTextInDialog.setError(null); // Clear previous error

                if (TextUtils.isEmpty(emailForReset)) {
                    // This Toast is okay, but for dialogs, setting error on EditText is better if it remains visible
                    Toast.makeText(getApplicationContext(), "Please enter your registered email.", Toast.LENGTH_SHORT).show();
                    // To keep dialog open and show error, would need custom handling of positive button.
                    // For now, this will dismiss the dialog.
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailForReset).matches()) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.sendPasswordResetEmail(emailForReset)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, R.string.password_reset_sent, Toast.LENGTH_LONG).show(); // Added R.string
                                } else {
                                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                        // emailEditTextInDialog.setError(getString(R.string.error_password_reset_email_not_found)); // Dialog might be dismissed
                                        Toast.makeText(LoginActivity.this, R.string.error_password_reset_email_not_found, Toast.LONG).show();
                                    } else if (task.getException() instanceof FirebaseAuthException) {
                                         Toast.makeText(LoginActivity.this, "Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, R.string.error_password_reset_failed_generic, Toast.LONG).show();
                                    }
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
