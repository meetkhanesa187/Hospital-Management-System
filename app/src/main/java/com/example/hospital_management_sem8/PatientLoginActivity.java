package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class PatientLoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient; // Moved to field to avoid recreation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        configureGoogleSignIn();

        // Bind views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);
        ImageView ivGoogle = findViewById(R.id.ivGoogle);
        ImageView ivFacebook = findViewById(R.id.ivFacebook);

        // Set click listeners
        btnLogin.setOnClickListener(v -> validateAndLogin());
        ivGoogle.setOnClickListener(v -> signInWithGoogle());
        ivFacebook.setOnClickListener(v -> showToast("Facebook Login clicked"));
        tvForgotPassword.setOnClickListener(v -> navigateTo(ForgotPasswordActivity.class));
        tvRegister.setOnClickListener(v -> navigateTo(PatientRegisterActivity.class));
    }

    private void configureGoogleSignIn() {
        // Avoid crash if web client ID is missing
        String webClientId = getString(R.string.default_web_client_id);
        if (TextUtils.isEmpty(webClientId)) {
            Log.e("GoogleSignIn", "Web Client ID is missing. Check google-services.json file.");
            return;
        }

        // Initialize Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void validateAndLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        showToast("Login successful: " + (user != null ? user.getEmail() : ""));
                        navigateTo(PatientHomeActivity.class);
                        finish();
                    } else {
                        Log.e("FirebaseAuth", "Authentication failed: ", task.getException());
                        showToast("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    // Google Sign-In method
    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            showToast("Google Sign-In not configured properly");
            return;
        }
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Google Sign-In failed: " + e.getMessage());
                showToast("Google Sign-In failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        showToast("Login successful: " + (user != null ? user.getEmail() : ""));
                        navigateTo(PatientHomeActivity.class);
                        finish();
                    } else {
                        Log.e("FirebaseAuth", "Google authentication failed", task.getException());
                        showToast("Authentication failed");
                    }
                });
    }

    // Helper methods
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
