package com.example.hospital_management_sem8;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewAppointmentsActivity extends AppCompatActivity {

    private DatabaseReference patientsRef;
    private TableLayout tableLayout;
    private Map<String, List<Appointment>> doctorAppointmentsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        patientsRef = FirebaseDatabase.getInstance().getReference("Patient_register");
        tableLayout = findViewById(R.id.tableLayout);

        loadAppointments();
    }

    private void loadAppointments() {
        patientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorAppointmentsMap.clear();
                tableLayout.removeAllViews();

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                Date today = new Date();

                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                    String patientName = patientSnapshot.child("fullName").getValue(String.class);
                    DataSnapshot appointmentsSnapshot = patientSnapshot.child("appointments");

                    if (!appointmentsSnapshot.exists()) continue;

                    for (DataSnapshot appointment : appointmentsSnapshot.getChildren()) {
                        String appointmentDate = appointment.child("appointmentDate").getValue(String.class);
                        String doctorName = appointment.child("doctorName").getValue(String.class);
                        String timeslot = appointment.child("timeSlot").getValue(String.class);

                        if (appointmentDate != null && doctorName != null && patientName != null) {
                            try {
                                Date date = sdf.parse(appointmentDate);
                                if (date != null && !date.before(sdf.parse(sdf.format(today)))) {
                                    // Passing 5 parameters now
                                    Appointment appointmentObj = new Appointment(doctorName, patientName, appointmentDate, timeslot, "");
                                    doctorAppointmentsMap
                                            .computeIfAbsent(doctorName, k -> new ArrayList<>())
                                            .add(appointmentObj);
                                }
                            } catch (Exception e) {
                                Log.e("DateParseError", "Failed to parse date: " + appointmentDate, e);
                            }
                        }
                    }
                }

                for (String doctor : doctorAppointmentsMap.keySet()) {
                    List<Appointment> doctorAppointments = doctorAppointmentsMap.get(doctor);
                    Collections.sort(doctorAppointments, (a, b) -> a.getDate().compareTo(b.getDate()));
                    populateTableForDoctor(doctor, doctorAppointments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching appointments: " + error.getMessage());
            }
        });
    }

    private void populateTableForDoctor(String doctorName, List<Appointment> appointments) {
        TableRow doctorHeaderRow = new TableRow(this);
        TextView doctorHeader = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 3;
        doctorHeader.setLayoutParams(params);
        doctorHeader.setText("Appointments for Dr. " + doctorName);
        doctorHeader.setPadding(16, 16, 16, 16);
        doctorHeader.setTextColor(Color.WHITE);
        doctorHeader.setTextSize(16);
        doctorHeader.setBackgroundColor(Color.parseColor("#4CAF50"));
        doctorHeader.setGravity(Gravity.CENTER);
        doctorHeaderRow.addView(doctorHeader);
        tableLayout.addView(doctorHeaderRow);

        TableRow columnHeaders = new TableRow(this);
        columnHeaders.addView(createTableHeaderCell("Patient"));
        columnHeaders.addView(createTableHeaderCell("Date"));
        columnHeaders.addView(createTableHeaderCell("TimeSlot"));
        tableLayout.addView(columnHeaders);

        for (Appointment appointment : appointments) {
            TableRow row = new TableRow(this);

            // Highlight today's appointment
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            String todayDate = sdf.format(new Date());
            if (appointment.getDate().equals(todayDate)) {
                row.setBackgroundColor(Color.parseColor("#FFEB3B"));
            }

            row.addView(createTableCell(appointment.getPatient()));
            row.addView(createTableCell(appointment.getDate()));
            row.addView(createTableCell(appointment.getTimeslot()));
            tableLayout.addView(row);
        }
    }

    private TextView createTableHeaderCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 12, 16, 12);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundColor(Color.DKGRAY);
        return textView;
    }

    private TextView createTableCell(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 12, 16, 12);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundResource(R.drawable.cell_background);
        return textView;
    }
}