package com.example.hospital_management_sem8;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PatientsDetailsActivity extends AppCompatActivity {

    private ListView patientsList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_patients);

        patientsList = findViewById(R.id.patients_list);
        databaseReference = FirebaseDatabase.getInstance().getReference("Patient_register");

        loadPatientDataFromFirebase();
    }

    private void loadPatientDataFromFirebase() {
        databaseReference.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> patientDetails = new ArrayList<>();

                // Iterate through all patients in the Patient_register node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Extract patient data
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String age = snapshot.child("age").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String mobileNumber = snapshot.child("mobileNumber").getValue(String.class);
                    String feedback = snapshot.child("feedback").getValue(String.class);

                    // Check if basic patient data is present
                    if (fullName != null && age != null && email != null) {
                        StringBuilder patientInfo = new StringBuilder();
                        patientInfo.append("🧑 Name: ").append(fullName).append("\n")
                                .append("🎂 Age: ").append(age).append("\n")
                                .append("📧 Email: ").append(email).append("\n")
                                .append("📞 Mobile: ").append(mobileNumber != null ? mobileNumber : "N/A").append("\n")
                                .append("💬 Feedback: ").append(feedback != null ? feedback : "No feedback").append("\n");

                        // Add constructed patient info to the list
                        patientDetails.add(patientInfo.toString());
                    }
                }

                // If no patients are found, display a default message
                if (patientDetails.isEmpty()) {
                    patientDetails.add("No patients available.");
                }

                // Set up the adapter to display the patient data
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PatientsDetailsActivity.this, android.R.layout.simple_list_item_1, patientDetails);
                patientsList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle failure to retrieve data from Firebase
                Toast.makeText(PatientsDetailsActivity.this, "Failed to load patient data", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Database error: " + error.getMessage());
            }
        });
    }
}
