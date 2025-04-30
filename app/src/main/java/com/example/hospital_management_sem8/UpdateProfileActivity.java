package com.example.hospital_management_sem8;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etMobile, etSpecialization;
    private Button btnUpdateProfile, btnGenerateQR, btnDownloadQR;
    private ImageView ivQRCode;
    private Bitmap qrBitmap;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_doctor_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etSpecialization = findViewById(R.id.etSpecialization);
        btnUpdateProfile = findViewById(R.id.btnSave);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnDownloadQR = findViewById(R.id.btnDownloadQR);
        ivQRCode = findViewById(R.id.ivQRCode);

        storageReference = FirebaseStorage.getInstance().getReference("Doctor_Photos");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Approved_Doctors").child(userId);
            fetchDoctorData();
        }

        btnUpdateProfile.setOnClickListener(v -> updateDoctorProfile());
        btnGenerateQR.setOnClickListener(v -> generateQRCode());
        btnDownloadQR.setOnClickListener(v -> downloadQRCode());
    }

    private void fetchDoctorData() {
        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                etFullName.setText(dataSnapshot.child("fullName").getValue(String.class));
                etEmail.setText(dataSnapshot.child("email").getValue(String.class));
                etMobile.setText(dataSnapshot.child("mobileNumber").getValue(String.class));
                etSpecialization.setText(dataSnapshot.child("specialization").getValue(String.class));
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void updateDoctorProfile() {
        String fullName = etFullName.getText().toString().trim();
        String mobileNumber = etMobile.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();

        if (fullName.isEmpty() || mobileNumber.isEmpty() || specialization.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("fullName", fullName);
        updateData.put("mobileNumber", mobileNumber);
        updateData.put("specialization", specialization);

        databaseReference.updateChildren(updateData).addOnSuccessListener(unused ->
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void generateQRCode() {
        String qrData = "Doctor Info:\n"
                + "Name: " + etFullName.getText().toString() + "\n"
                + "Email: " + etEmail.getText().toString() + "\n"
                + "Mobile: " + etMobile.getText().toString() + "\n"
                + "Specialization: " + etSpecialization.getText().toString();

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

    private void downloadQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Generate QR code first!", Toast.LENGTH_SHORT).show();
            return;
        }

        try (OutputStream fos = getContentResolver().openOutputStream(
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues()))) {
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(this, "QR Code saved in Gallery!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
