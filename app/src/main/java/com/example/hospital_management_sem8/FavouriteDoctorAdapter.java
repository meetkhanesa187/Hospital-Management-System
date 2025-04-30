package com.example.hospital_management_sem8;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FavouriteDoctorAdapter extends RecyclerView.Adapter<FavouriteDoctorAdapter.ViewHolder> {

    private Context context;
    private List<FavouriteDoctor> favouriteDoctorList;

    public FavouriteDoctorAdapter(Context context, List<FavouriteDoctor> favouriteDoctorList) {
        this.context = context;
        this.favouriteDoctorList = favouriteDoctorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavouriteDoctor doctor = favouriteDoctorList.get(position);
        holder.doctorName.setText(doctor.getFullName());

        holder.itemView.setOnClickListener(v -> {
            showBookingDialog(doctor);
        });
    }

    @Override
    public int getItemCount() {
        return favouriteDoctorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
        }
    }

    private void showBookingDialog(FavouriteDoctor doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Book Appointment")
                .setMessage("Do you want to book an appointment with " + doctor.getFullName() + "?")
                .setPositiveButton("Book", (dialog, which) -> {
                    Intent intent = new Intent(context, BookingActivity.class);
                    intent.putExtra("doctorId", doctor.getDoctorId());
                    intent.putExtra("doctorName", doctor.getFullName());
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
