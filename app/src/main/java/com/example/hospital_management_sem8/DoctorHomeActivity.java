package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DoctorHomeActivity extends AppCompatActivity {

    private TextView todaysAppointmentsCount, totalTreatedPatientsCount, upcomingAppointment1, upcomingAppointment2;
    private TextView feedback1, feedback2;
    private Button viewAppointmentsButton, updateProfileButton, viewReportsButton;
    private FirebaseAuth auth;
    private DatabaseReference doctorRef, patientRef;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctorhome);

        // Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI references
        todaysAppointmentsCount = findViewById(R.id.todays_appointments_count);
        totalTreatedPatientsCount = findViewById(R.id.total_treated_patients_count);
        upcomingAppointment1 = findViewById(R.id.upcoming_appointment_1);
        upcomingAppointment2 = findViewById(R.id.upcoming_appointment_2);
        feedback1 = findViewById(R.id.feedback_1);
        feedback2 = findViewById(R.id.feedback_2);
        viewAppointmentsButton = findViewById(R.id.view_appointments_button);
        updateProfileButton = findViewById(R.id.update_profile_button);
        viewReportsButton = findViewById(R.id.view_reports_button);

        // Firebase DB references
        doctorRef = FirebaseDatabase.getInstance().getReference("Approved_Doctors").child(user.getUid());
        patientRef = FirebaseDatabase.getInstance().getReference("Patient_register");

        fetchDoctorName(); // Fetch doctor name and load appointments
        loadRecentFeedback(); // Load feedback

        // Button click listeners
        viewAppointmentsButton.setOnClickListener(v -> startActivity(new Intent(this, ViewAppointmentsActivity.class)));
        updateProfileButton.setOnClickListener(v -> startActivity(new Intent(this, UpdateProfileActivity.class)));
        viewReportsButton.setOnClickListener(v -> startActivity(new Intent(this, ViewReportsActivity.class)));
    }

    private void fetchDoctorName() {
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorName = snapshot.child("fullName").getValue(String.class);
                if (doctorName != null) {
                    fetchAppointments();
                } else {
                    Toast.makeText(DoctorHomeActivity.this, "Doctor name not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorHomeActivity.this, "Error fetching doctor info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAppointments() {
        if (doctorName == null) return;

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> upcomingAppointments = new ArrayList<>();
                int appointmentCount = 0;
                int totalPatients = 0;

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                String todayDate = sdf.format(new Date());

                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                    DataSnapshot appointmentsSnapshot = patientSnapshot.child("appointments");

                    for (DataSnapshot appointment : appointmentsSnapshot.getChildren()) {
                        String appointmentDoctorName = appointment.child("doctorName").getValue(String.class);
                        String patientName = patientSnapshot.child("fullName").getValue(String.class);
                        String appointmentDate = appointment.child("appointmentDate").getValue(String.class);

                        if (appointmentDoctorName != null && patientName != null && appointmentDate != null &&
                                appointmentDoctorName.equals(doctorName)) {
                            try {
                                Date appointmentDateObj = sdf.parse(appointmentDate);
                                Date todayDateObj = sdf.parse(todayDate);

                                if (appointmentDateObj != null && !appointmentDateObj.before(todayDateObj)) {
                                    upcomingAppointments.add(patientName + " - Dr. " + doctorName);
                                    appointmentCount++;
                                    totalPatients++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                todaysAppointmentsCount.setText(String.valueOf(appointmentCount));
                totalTreatedPatientsCount.setText(String.valueOf(totalPatients));

                upcomingAppointment1.setText(!upcomingAppointments.isEmpty() ? upcomingAppointments.get(0) : "No upcoming appointments");
                upcomingAppointment2.setText(upcomingAppointments.size() > 1 ? upcomingAppointments.get(1) : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorHomeActivity.this, "Error fetching appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecentFeedback() {
        List<String> feedbackList = new ArrayList<>();

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                    String feedback = patientSnapshot.child("feedback").getValue(String.class);
                    if (feedback != null && !feedback.isEmpty()) {
                        feedbackList.add(feedback);
                    }
                }

                Collections.reverse(feedbackList); // Latest first

                if (feedbackList.size() >= 1) {
                    feedback1.setText(feedbackList.get(0));
                } else {
                    feedback1.setText("No feedback available");
                }

                feedback2.setText(feedbackList.size() >= 2 ? feedbackList.get(1) : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                feedback1.setText("Failed to load feedback.");
                feedback2.setText("");
            }
        });
    }
}
