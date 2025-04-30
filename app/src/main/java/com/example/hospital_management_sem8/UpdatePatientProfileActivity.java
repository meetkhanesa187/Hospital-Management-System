package com.example.hospital_management_sem8;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class UpdatePatientProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etMobile, etAge;
    private Button btnSave, btnGenerateQR, btnDownloadQR, btnFeedback;
    private ImageView ivQRCode, ivTopRightButton;
    private Bitmap qrBitmap;
    private DatabaseReference patientRef;
    private String currentUserId;
    private LinearLayout profileIcon, newsIcon, doctorIcon, articlesIcon, favoriteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_patient_profile);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        btnSave = findViewById(R.id.btnSave);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnDownloadQR = findViewById(R.id.btnDownloadQR);
        btnFeedback = findViewById(R.id.btnFeedback);
        ivQRCode = findViewById(R.id.ivQRCode);
        ivTopRightButton = findViewById(R.id.ivTopRightButton);

        // Bottom navigation icons
        profileIcon = findViewById(R.id.profile_icon);
        newsIcon = findViewById(R.id.news_icon);
        doctorIcon = findViewById(R.id.doctor_icon);
        articlesIcon = findViewById(R.id.articles_icon);
        favoriteIcon = findViewById(R.id.favorite_icon);

        // Disable email field
        etEmail.setEnabled(false);

        ivTopRightButton.setOnClickListener(v -> {
            new AlertDialog.Builder(UpdatePatientProfileActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(UpdatePatientProfileActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdatePatientProfileActivity.this, PatientLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        // Firebase reference
        patientRef = FirebaseDatabase.getInstance().getReference("Patient_register");

        // Get current user ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            fetchPatientData();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        // Button listeners
        btnSave.setOnClickListener(v -> updateProfile());
        btnGenerateQR.setOnClickListener(v -> generateQRCode());
        btnFeedback.setOnClickListener(v -> showFeedbackDialog());

        // Highlight the active profile tab
        profileIcon.setBackgroundColor(getResources().getColor(R.color.active_tab));

        // Navigation - Profile
        profileIcon.setOnClickListener(v ->
                Toast.makeText(this, "You are already on the profile tab", Toast.LENGTH_SHORT).show()
        );

        // Navigation - News
        newsIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, NewsActivity.class));
            finish();
        });

        // Navigation - Doctor
        doctorIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorsDetailsActivity.class));
            finish();
        });

        // Navigation - Articles
        articlesIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, PatientHomeActivity.class));
            finish();
        });

        // Navigation - Favorites
        favoriteIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteDoctorsActivity.class));
            finish();
        });
    }

    private void fetchPatientData() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        patientRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    etFullName.setText(dataSnapshot.child("fullName").getValue(String.class));
                    etEmail.setText(dataSnapshot.child("email").getValue(String.class));
                    etMobile.setText(dataSnapshot.child("mobileNumber").getValue(String.class));

                    String age = dataSnapshot.child("age").getValue(String.class);
                    if (age != null && !age.equalsIgnoreCase("null")) {
                        etAge.setText(age);
                    } else {
                        etAge.setText("");
                    }
                } else {
                    Toast.makeText(UpdatePatientProfileActivity.this, "Patient data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdatePatientProfileActivity.this, "Fetching patient data failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mobile.matches("\\d{10}")) {
            Toast.makeText(this, "Enter a valid 10-digit mobile number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(age)) {
            try {
                int ageInt = Integer.parseInt(age);
                if (ageInt <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid age.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            age = "null";
        }

        DatabaseReference patientNode = patientRef.child(currentUserId);
        patientNode.child("fullName").setValue(fullName);
        patientNode.child("mobileNumber").setValue(mobile);
        patientNode.child("age").setValue(age);

        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
    }

    private void generateQRCode() {
        String qrData = "Patient Info:\n"
                + "Name: " + etFullName.getText().toString().trim() + "\n"
                + "Email: " + etEmail.getText().toString().trim() + "\n"
                + "Mobile: " + etMobile.getText().toString().trim() + "\n"
                + "Age: " + etAge.getText().toString().trim();

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 400, 400);
            qrBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);

            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 400; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            ivQRCode.setImageBitmap(qrBitmap);
            ivQRCode.setVisibility(View.VISIBLE);
            btnDownloadQR.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showFeedbackDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feedback, null);
        Spinner spinnerFeedback = dialogView.findViewById(R.id.spinnerFeedback);
        Button btnSubmitFeedback = dialogView.findViewById(R.id.btnSubmitFeedback);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Feedback")
                .setView(dialogView)
                .create();

        String[] feedbackOptions = {
                "Very Satisfied",
                "Satisfied",
                "Neutral",
                "Dissatisfied",
                "Very Dissatisfied"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, feedbackOptions);
        spinnerFeedback.setAdapter(adapter);

        btnSubmitFeedback.setOnClickListener(v -> {
            String selectedFeedback = spinnerFeedback.getSelectedItem().toString();

            if (!TextUtils.isEmpty(currentUserId)) {
                patientRef.child(currentUserId).child("feedback").setValue(selectedFeedback)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
