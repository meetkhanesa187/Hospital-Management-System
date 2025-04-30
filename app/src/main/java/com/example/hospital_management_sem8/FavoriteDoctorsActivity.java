package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavoriteDoctorsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavouriteDoctorAdapter favouriteDoctorAdapter;
    private List<FavouriteDoctor> favouriteDoctorList;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private LinearLayout newsIcon, doctorIcon, articlesIcon, favoriteIcon, profileIcon;
    private LinearLayout helpSection; // Help icon + text click listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_doctor);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favouriteDoctorList = new ArrayList<>();
        favouriteDoctorAdapter = new FavouriteDoctorAdapter(this, favouriteDoctorList);
        recyclerView.setAdapter(favouriteDoctorAdapter);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Patient_register")
                    .child(userId)
                    .child("Favorites");

            fetchFavoriteDoctors();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        // Initialize Views
        newsIcon = findViewById(R.id.news_icon);
        doctorIcon = findViewById(R.id.doctor_icon);
        articlesIcon = findViewById(R.id.articles_icon);
        favoriteIcon = findViewById(R.id.favorite_icon);
        profileIcon = findViewById(R.id.profile_icon);
        helpSection = findViewById(R.id.help_section); // LinearLayout containing help icon & text

        // Set Click Listeners
        newsIcon.setOnClickListener(view -> openActivity(NewsActivity.class));
        doctorIcon.setOnClickListener(view -> openActivity(DoctorsDetailsActivity.class));
        articlesIcon.setOnClickListener(view -> openActivity(PatientHomeActivity.class));

        // Show toast if already on FavoriteDoctorsActivity
        favoriteIcon.setOnClickListener(view ->
                Toast.makeText(FavoriteDoctorsActivity.this, "You are already on the Favorites tab", Toast.LENGTH_SHORT).show()
        );

        profileIcon.setOnClickListener(view -> openActivity(UpdatePatientProfileActivity.class));
        helpSection.setOnClickListener(view -> openActivity(ChatWithAIActivity.class)); // Help button opens AI chat
    }

    private void fetchFavoriteDoctors() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favouriteDoctorList.clear();
                for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                    FavouriteDoctor doctor = doctorSnapshot.getValue(FavouriteDoctor.class);
                    if (doctor != null) {
                        favouriteDoctorList.add(doctor);
                    }
                }
                favouriteDoctorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data", error.toException());
                Toast.makeText(FavoriteDoctorsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper Method to Start an Activity
    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(FavoriteDoctorsActivity.this, activityClass);
        startActivity(intent);
    }
}
