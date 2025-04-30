package com.example.hospital_management_sem8;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<AppointmentModel> appointmentList;
    private Context context;
    private String userId;

    public AppointmentAdapter(List<AppointmentModel> appointmentList, Context context, String userId) {
        this.appointmentList = appointmentList;
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public AppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapter.ViewHolder holder, int position) {
        AppointmentModel model = appointmentList.get(position);
        holder.doctorName.setText("👨‍⚕️ " + model.getDoctorName());
        holder.date.setText("🗓 " + model.getAppointmentDate());
        holder.time.setText("⌚ " + model.getTimeSlot());
        holder.specialization.setText("🩺 " + model.getSpecialization());

        // Long-click to delete
        holder.itemView.setOnLongClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Appointment")
                    .setMessage("Are you sure you want to delete this appointment?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String appointmentId = model.getAppointmentId();

                        FirebaseDatabase.getInstance()
                                .getReference("Patient_register")
                                .child(userId)
                                .child("appointments")
                                .child(appointmentId)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Appointment deleted", Toast.LENGTH_SHORT).show();

                                    // Refresh the activity after deletion
                                    if (context instanceof PatientHomeActivity) {
                                        ((PatientHomeActivity) context).recreate();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, date, time, specialization;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.tvDoctorName);
            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTimeSlot);
            specialization = itemView.findViewById(R.id.tvSpecialization);
        }
    }
}
