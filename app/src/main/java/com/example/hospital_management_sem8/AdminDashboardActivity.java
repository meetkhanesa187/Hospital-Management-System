package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView totalPatientsCount, totalDoctorsCount;
    private Button managePatientsButton, manageDoctorsButton, approveDoctorsButton, logoutButton;

    private DatabaseReference patientsReference, doctorsReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminhome);

        // Initialize UI elements
        totalPatientsCount = findViewById(R.id.total_patients_count);
        totalDoctorsCount = findViewById(R.id.total_doctors_count);
        managePatientsButton = findViewById(R.id.manage_patients_button);
        manageDoctorsButton = findViewById(R.id.manage_doctors_button);
        approveDoctorsButton = findViewById(R.id.approve_doctors_button);
        logoutButton = findViewById(R.id.logout_button);

        // Initialize Firebase references
        patientsReference = FirebaseDatabase.getInstance().getReference("Patient_register");
        doctorsReference = FirebaseDatabase.getInstance().getReference("Approved_Doctors");
        mAuth = FirebaseAuth.getInstance();

        // Fetch and display counts
        fetchAndDisplayCounts();

        // Set button click listeners
        managePatientsButton.setOnClickListener(v -> navigateToActivity(PatientsDetailsActivity.class));
        manageDoctorsButton.setOnClickListener(v -> navigateToActivity(admin_doctor_detail.class));
        approveDoctorsButton.setOnClickListener(v -> navigateToActivity(DoctorApprovalActivity.class));

        // Handle logout button click
        logoutButton.setOnClickListener(v -> logout());
    }

    private void fetchAndDisplayCounts() {
        // Fetch total patients count
        patientsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long patientCount = dataSnapshot.getChildrenCount();
                totalPatientsCount.setText(String.valueOf(patientCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to fetch patient count: " + databaseError.getMessage());
            }
        });

        // Fetch total doctors count
        doctorsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long doctorCount = dataSnapshot.getChildrenCount();
                totalDoctorsCount.setText(String.valueOf(doctorCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to fetch doctor count: " + databaseError.getMessage());
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(AdminDashboardActivity.this, targetActivity);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(AdminDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        // Sign out from FirebaseAuth
        mAuth.signOut();

        // Redirect to login screen after logout
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity so user can't return to it after logout
    }
}
