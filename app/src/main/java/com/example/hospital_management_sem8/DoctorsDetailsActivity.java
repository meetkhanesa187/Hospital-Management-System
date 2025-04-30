package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class DoctorsDetailsActivity extends AppCompatActivity {

    private RecyclerView doctorsRecyclerView;
    private DoctorsAdapter doctorsAdapter;
    private List<Doctor> doctorList, filteredList;
    private DatabaseReference doctorsRef;
    private EditText searchDoctor;
    private static final String TAG = "DoctorsDetailsActivity";

    private LinearLayout newsIcon, doctorIcon, articlesIcon, favoriteIcon, profileIcon;
    private LinearLayout helpSection; // Help icon + text click listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_doctors);

        // Initialize UI components
        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView);
        searchDoctor = findViewById(R.id.searchDoctor);
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists
        doctorList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Set up adapter
        doctorsAdapter = new DoctorsAdapter(this, filteredList);
        doctorsRecyclerView.setAdapter(doctorsAdapter);

        // Firebase reference
        doctorsRef = FirebaseDatabase.getInstance().getReference("Approved_Doctors");

        // Load doctors from Firebase
        loadDoctors();

        // Search functionality
        searchDoctor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Help button (inside header)
        ImageView helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(v -> {
            Toast.makeText(DoctorsDetailsActivity.this, "Help Section Clicked!", Toast.LENGTH_SHORT).show();
        });

        // Initialize Bottom Navigation
        newsIcon = findViewById(R.id.news_icon);
        doctorIcon = findViewById(R.id.doctor_icon);
        articlesIcon = findViewById(R.id.articles_icon);
        favoriteIcon = findViewById(R.id.favorite_icon);
        profileIcon = findViewById(R.id.profile_icon);
        helpSection = findViewById(R.id.help_section);

        // Set Click Listeners
        newsIcon.setOnClickListener(view -> openActivity(NewsActivity.class));

        // Already in Doctors tab
        doctorIcon.setOnClickListener(view ->
                Toast.makeText(DoctorsDetailsActivity.this, "You are already on the Doctors tab", Toast.LENGTH_SHORT).show()
        );

        articlesIcon.setOnClickListener(view -> openActivity(PatientHomeActivity.class));
        favoriteIcon.setOnClickListener(view -> openActivity(FavoriteDoctorsActivity.class));
        profileIcon.setOnClickListener(view -> openActivity(UpdatePatientProfileActivity.class));
        helpSection.setOnClickListener(view -> openActivity(ChatWithAIActivity.class));
    }

    private void loadDoctors() {
        doctorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(DoctorsDetailsActivity.this, "No approved doctors found!", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "No doctors data in Firebase.");
                } else {
                    for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                        try {
                            String doctorId = doctorSnapshot.getKey();
                            String fullName = doctorSnapshot.child("fullName").getValue(String.class);
                            String email = doctorSnapshot.child("email").getValue(String.class);
                            String specialization = doctorSnapshot.child("specialization").getValue(String.class);
                            String bio = doctorSnapshot.child("bio").getValue(String.class);
                            String mobileNumber = doctorSnapshot.child("mobileNumber").getValue(String.class);
                            Boolean isApproved = doctorSnapshot.child("isApproved").getValue(Boolean.class);

                            int experience = 0;
                            Object experienceObj = doctorSnapshot.child("experience").getValue();
                            if (experienceObj instanceof String) {
                                try {
                                    experience = Integer.parseInt((String) experienceObj);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Experience conversion error: " + e.getMessage());
                                }
                            } else if (experienceObj instanceof Long) {
                                experience = ((Long) experienceObj).intValue();
                            }

                            if (Boolean.TRUE.equals(isApproved)) {
                                Doctor doctor = new Doctor(fullName, email, experience, specialization, isApproved, bio, mobileNumber, "", doctorId);
                                doctorList.add(doctor);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Data parsing issue: " + e.getMessage(), e);
                        }
                    }
                }
                filterDoctors(searchDoctor.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase database error: " + error.getMessage());
                Toast.makeText(DoctorsDetailsActivity.this, "Failed to load doctors!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterDoctors(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(doctorList);
        } else {
            for (Doctor doctor : doctorList) {
                if ((doctor.getFullName() != null && doctor.getFullName().toLowerCase().contains(query.toLowerCase())) ||
                        (doctor.getSpecialization() != null && doctor.getSpecialization().toLowerCase().contains(query.toLowerCase()))) {
                    filteredList.add(doctor);
                }
            }
        }
        doctorsAdapter.notifyDataSetChanged();
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(DoctorsDetailsActivity.this, activityClass);
        startActivity(intent);
    }
}
