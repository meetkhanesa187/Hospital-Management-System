package com.example.hospital_management_sem8;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etMobileNumber, etPassword, etSpecialization, etExperience, etBio;
    private Button btnRegister;
    private TextView tvLoginRedirect;
    private ImageView qrCodeImage;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Pending_Doctors");

        // Bind views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etPassword = findViewById(R.id.etPassword);
        etSpecialization = findViewById(R.id.etSpecialization);
        etExperience = findViewById(R.id.etExperience);
        etBio = findViewById(R.id.etBio);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);
        qrCodeImage = findViewById(R.id.qrCodeImage);

        // Register button click listener
        btnRegister.setOnClickListener(v -> validateAndRegister());

        // Redirect to Login Activity
        tvLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void validateAndRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mobileNumber) || mobileNumber.length() < 10) {
            etMobileNumber.setError("Enter a valid mobile number");
            etMobileNumber.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(specialization)) {
            etSpecialization.setError("Specialization is required");
            etSpecialization.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(experience)) {
            etExperience.setError("Experience is required");
            etExperience.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(bio)) {
            etBio.setError("Bio is required");
            etBio.requestFocus();
            return;
        }

        // Register user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Create doctor object
                            Doctor doctor = new Doctor(fullName, email, mobileNumber, specialization, experience, bio, false);

                            // Save doctor data to Firebase Database in "Pending_Doctors"
                            databaseReference.child(userId).setValue(doctor)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Registration successful. Waiting for admin approval.", Toast.LENGTH_LONG).show();

                                            // Generate and display QR Code
                                            generateQRCode(userId, fullName, email, mobileNumber, specialization, experience, bio);
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "User registration failed. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateQRCode(String userId, String fullName, String email, String mobileNumber, String specialization, String experience, String bio) {
        String qrData = "UserID: " + userId +
                "\nName: " + fullName +
                "\nEmail: " + email +
                "\nMobile: " + mobileNumber +
                "\nSpecialization: " + specialization +
                "\nExperience: " + experience +
                "\nBio: " + bio;

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            Toast.makeText(this, "Failed to Generate QR Code!", Toast.LENGTH_SHORT).show();
        }
    }

    // Doctor class
    public static class Doctor {
        public String fullName, email, mobileNumber, specialization, experience, bio;
        public boolean isApproved;

        public Doctor() {}

        public Doctor(String fullName, String email, String mobileNumber, String specialization, String experience, String bio, boolean isApproved) {
            this.fullName = fullName;
            this.email = email;
            this.mobileNumber = mobileNumber;
            this.specialization = specialization;
            this.experience = experience;
            this.bio = bio;
            this.isApproved = isApproved;
        }
    }
}
