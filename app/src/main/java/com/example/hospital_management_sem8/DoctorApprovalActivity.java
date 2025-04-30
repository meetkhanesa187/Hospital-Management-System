package com.example.hospital_management_sem8;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Map;

public class DoctorApprovalActivity extends AppCompatActivity {

    private ListView pendingDoctorsListView;
    private ArrayList<String> doctorList;
    private ArrayList<String> doctorIds;
    private ArrayAdapter<String> adapter;
    private DatabaseReference doctorRef, approvedDoctorsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_approval);

        // Initialize Firebase references
        doctorRef = FirebaseDatabase.getInstance().getReference("Pending_Doctors");
        approvedDoctorsRef = FirebaseDatabase.getInstance().getReference("Approved_Doctors");

        // Initialize UI components
        pendingDoctorsListView = findViewById(R.id.pending_doctors_list);
        doctorList = new ArrayList<>();
        doctorIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorList);
        pendingDoctorsListView.setAdapter(adapter);

        // Load pending doctors
        loadPendingDoctors();

        // Handle doctor selection for approval/rejection
        pendingDoctorsListView.setOnItemClickListener((parent, view, position, id) -> showApprovalDialog(position));
    }

    private void loadPendingDoctors() {
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear();
                doctorIds.clear();

                for (DataSnapshot doctor : snapshot.getChildren()) {
                    String doctorId = doctor.getKey();
                    String name = doctor.child("fullName").getValue(String.class);
                    String specialization = doctor.child("specialization").getValue(String.class);
                    Boolean isApproved = doctor.child("isApproved").getValue(Boolean.class);

                    if (Boolean.FALSE.equals(isApproved) && name != null && specialization != null) {
                        doctorList.add(name + " - " + specialization);
                        doctorIds.add(doctorId);
                    }
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    if (doctorList.isEmpty()) {
                        Toast.makeText(DoctorApprovalActivity.this, "No pending doctors for approval", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load doctors: " + error.getMessage());
                showToast("Failed to load doctors!");
            }
        });
    }

    private void showApprovalDialog(int position) {
        if (position < 0 || position >= doctorIds.size()) return;

        String doctorId = doctorIds.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Doctor Approval")
                .setMessage("Approve or Reject this doctor?")
                .setPositiveButton("Approve", (dialog, which) -> approveDoctor(doctorId, position))
                .setNegativeButton("Reject", (dialog, which) -> rejectDoctor(doctorId, position))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void approveDoctor(String doctorId, int position) {
        DatabaseReference docRef = doctorRef.child(doctorId);

        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showToast("Doctor not found!");
                    return;
                }

                Map<String, Object> doctorData = (Map<String, Object>) snapshot.getValue();
                if (doctorData != null) {
                    doctorData.put("isApproved", true);
                    approvedDoctorsRef.child(doctorId).setValue(doctorData)
                            .addOnSuccessListener(aVoid -> {
                                docRef.removeValue();
                                removeDoctorFromList(position);
                                showToast("Doctor Approved!");
                            })
                            .addOnFailureListener(e -> Log.e("FirebaseError", "Failed to approve doctor", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to approve doctor", error.toException());
            }
        });
    }

    private void rejectDoctor(String doctorId, int position) {
        doctorRef.child(doctorId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    removeDoctorFromList(position);
                    showToast("Doctor Rejected!");
                })
                .addOnFailureListener(e -> Log.e("FirebaseError", "Failed to reject doctor", e));
    }

    private void removeDoctorFromList(int position) {
        if (position < 0 || position >= doctorList.size()) return;

        doctorList.remove(position);
        doctorIds.remove(position);

        runOnUiThread(adapter::notifyDataSetChanged);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(DoctorApprovalActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
