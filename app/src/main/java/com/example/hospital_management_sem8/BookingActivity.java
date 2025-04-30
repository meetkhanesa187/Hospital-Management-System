package com.example.hospital_management_sem8;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private TextView doctorName, doctorSpecialization;
    private EditText editTextDate;
    private Spinner timeSlotSpinner;
    private Button confirmBookingButton, cancelButton;
    private ImageView backButton; // Back button ImageView
    private Calendar calendar;
    private String selectedDate = "";
    private String selectedTimeSlot = "";
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize UI components
        doctorName = findViewById(R.id.doctorName);
        doctorSpecialization = findViewById(R.id.doctorClinic);
        editTextDate = findViewById(R.id.editTextDate);
        timeSlotSpinner = findViewById(R.id.spinnerTimeSlot);
        confirmBookingButton = findViewById(R.id.bookAppointmentButton);
        cancelButton = findViewById(R.id.cancelButton);
        backButton = findViewById(R.id.backButton); // Initialize back button

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Patient_register");
        auth = FirebaseAuth.getInstance();

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Booking your appointment...");
        progressDialog.setCancelable(false);

        // Get doctor details from Intent
        String name = getIntent().getStringExtra("doctorName");
        String specialization = getIntent().getStringExtra("doctorSpecialization");

        // Set doctor details
        doctorName.setText("Dr. " + name);
        doctorSpecialization.setText(specialization);

        // Open DatePicker on EditText click
        editTextDate.setOnClickListener(v -> openDatePicker());

        // Set up the Time Slot Spinner
        setupTimeSlotSpinner();

        // Handle Booking Confirmation
        confirmBookingButton.setOnClickListener(v -> checkAndSaveBooking(name, specialization));

        // Handle Cancel Button Click
        cancelButton.setOnClickListener(v -> {
            Toast.makeText(this, "Booking Canceled", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Handle Back Button Click
        backButton.setOnClickListener(v -> finish()); // Navigates back to the previous activity
    }

    private void openDatePicker() {
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year1, month1, dayOfMonth);

            if (selectedCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(this, "Sunday is not available. Please select another day.", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            editTextDate.setText(selectedDate);
        }, year, month, day);

        // Prevent past & today selection
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);
        datePickerDialog.show();
    }

    private void setupTimeSlotSpinner() {
        String[] timeSlots = {"Select Time Slot", "Morning", "Evening"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlotSpinner.setAdapter(adapter);

        timeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeSlot = position == 0 ? "" : parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimeSlot = "";
            }
        });
    }

    private void checkAndSaveBooking(String doctorName, String specialization) {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTimeSlot.isEmpty()) {
            Toast.makeText(this, "Please select a time slot before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to book an appointment.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        progressDialog.show();

        DatabaseReference userAppointmentsRef = databaseReference.child(userId).child("appointments");

        userAppointmentsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String existingDate = snapshot.child("appointmentDate").getValue(String.class);
                    String existingTimeSlot = snapshot.child("timeSlot").getValue(String.class);

                    if (selectedDate.equals(existingDate) && selectedTimeSlot.equals(existingTimeSlot)) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "You already booked an appointment with this doctor at this time.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            saveBookingToRealtimeDatabase(userId, doctorName, specialization);
        });
    }

    private void saveBookingToRealtimeDatabase(String userId, String doctorName, String specialization) {
        DatabaseReference userAppointmentsRef = databaseReference.child(userId).child("appointments");

        // Generate a unique ID for each appointment
        String appointmentId = userAppointmentsRef.push().getKey();

        Map<String, Object> booking = new HashMap<>();
        booking.put("doctorName", doctorName);
        booking.put("specialization", specialization);
        booking.put("appointmentDate", selectedDate);
        booking.put("timeSlot", selectedTimeSlot);
        booking.put("timestamp", System.currentTimeMillis());

        if (appointmentId != null) {
            userAppointmentsRef.child(appointmentId).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        showSuccessDialog(); // Show success pop-up
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to book appointment. Try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Error generating appointment ID. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show success pop-up dialog with smile emoji
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Booking Successful ✅")
                .setMessage("Your appointment has been booked successfully! 😊")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Close the activity after success
                })
                .setCancelable(false)
                .show();
    }
}

