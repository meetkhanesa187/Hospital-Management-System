package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatientHomeActivity extends AppCompatActivity {

    private LinearLayout newsIcon, doctorIcon, articlesIcon, favoriteIcon, profileIcon, helpSection;
    private RecyclerView appointmentRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    private ArrayList<AppointmentModel> appointmentList;
    private DatabaseReference appointmentsRef;
    private String userId;
    private Button toggleViewButton;
    private boolean showingPastAppointments = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        // Initialize UI
        newsIcon = findViewById(R.id.news_icon);
        doctorIcon = findViewById(R.id.doctor_icon);
        articlesIcon = findViewById(R.id.articles_icon);
        favoriteIcon = findViewById(R.id.favorite_icon);
        profileIcon = findViewById(R.id.profile_icon);
        helpSection = findViewById(R.id.help_section);
        toggleViewButton = findViewById(R.id.toggleViewButton); // 🔁 Your layout must have this button

        newsIcon.setOnClickListener(view -> openActivity(NewsActivity.class));
        doctorIcon.setOnClickListener(view -> openActivity(DoctorsDetailsActivity.class));
        articlesIcon.setOnClickListener(view -> Toast.makeText(this, "You are already on the Articles tab", Toast.LENGTH_SHORT).show());
        favoriteIcon.setOnClickListener(view -> openActivity(FavoriteDoctorsActivity.class));
        profileIcon.setOnClickListener(view -> openActivity(UpdatePatientProfileActivity.class));
        helpSection.setOnClickListener(view -> openActivity(ChatWithAIActivity.class));

        // RecyclerView
        appointmentRecyclerView = findViewById(R.id.appointmentRecyclerView);
        appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        userId = auth.getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance()
                .getReference("Patient_register")
                .child(userId)
                .child("appointments");

        appointmentAdapter = new AppointmentAdapter(appointmentList, this, userId);
        appointmentRecyclerView.setAdapter(appointmentAdapter);

        // Toggle between past & upcoming
        toggleViewButton.setOnClickListener(v -> {
            showingPastAppointments = !showingPastAppointments;
            toggleViewButton.setText(showingPastAppointments ? "Show Upcoming Appointments" : "Show Past Appointments");
            loadAppointments();
        });

        // Load initially
        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date today = new Date();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    AppointmentModel model = ds.getValue(AppointmentModel.class);
                    if (model != null) {
                        model.setAppointmentId(ds.getKey());
                        try {
                            Date appointmentDate = sdf.parse(model.getAppointmentDate());
                            if (appointmentDate != null) {
                                boolean isPast = appointmentDate.before(today);
                                if ((showingPastAppointments && isPast) || (!showingPastAppointments && !isPast)) {
                                    appointmentList.add(model);
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("DATE_PARSE", "Error parsing date: " + model.getAppointmentDate(), e);
                        }
                    }
                }

                appointmentAdapter.notifyDataSetChanged();
                Log.d("FIREBASE_APPT", "Appointments loaded: " + appointmentList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PatientHomeActivity.this, "Failed to load appointments: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("FIREBASE_ERROR", error.toException().toString());
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
