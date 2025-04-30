package com.example.hospital_management_sem8;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.DoctorViewHolder> {
    private Context context;
    private List<Doctor> doctorList;
    private FirebaseUser currentUser;
    private DatabaseReference favRef;

    public DoctorsAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            favRef = FirebaseDatabase.getInstance().getReference("Patient_register")
                    .child(currentUser.getUid()).child("Favorites");
        }
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        if (doctor != null) {
            holder.doctorName.setText("Dr. " + doctor.getFullName());
            holder.doctorSpecialization.setText(doctor.getSpecialization());
            holder.doctorExperience.setText(doctor.getExperience() + " years of experience");

            Glide.with(context)
                    .load(doctor.getProfileImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.profilephoto)
                    .error(R.drawable.profilephoto)
                    .into(holder.doctorImage);

            // ✅ Check if this doctor is already in Favorites and update UI
            if (currentUser != null) {
                favRef.child(doctor.getDoctorId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        holder.favoriteIcon.setImageResource(R.drawable.heart1); // Filled heart
                        holder.favoriteIcon.setTag("filled"); // Save state
                    } else {
                        holder.favoriteIcon.setImageResource(R.drawable.heart); // Empty heart
                        holder.favoriteIcon.setTag("empty");
                    }
                });
            }

            // ⭐ Handle Favorite Button Click
            holder.favoriteIcon.setOnClickListener(v -> {
                if (currentUser != null) {
                    toggleFavorite(doctor, holder.favoriteIcon);
                } else {
                    Toast.makeText(context, "Please log in to add favorites!", Toast.LENGTH_SHORT).show();
                }
            });

            // ✅ Book Now Button Click
            holder.bookNowButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, BookingActivity.class);
                intent.putExtra("doctorId", doctor.getDoctorId());
                intent.putExtra("doctorName", doctor.getFullName());
                intent.putExtra("doctorSpecialization", doctor.getSpecialization());
                intent.putExtra("doctorExperience", doctor.getExperience());
                intent.putExtra("doctorImage", doctor.getProfileImage());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, doctorSpecialization, doctorExperience;
        ImageView doctorImage, favoriteIcon;
        Button bookNowButton;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorSpecialization = itemView.findViewById(R.id.doctorSpecialization);
            doctorExperience = itemView.findViewById(R.id.doctorExperience);
            doctorImage = itemView.findViewById(R.id.doctorImage);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            bookNowButton = itemView.findViewById(R.id.bookNowButton);
        }
    }

    private void toggleFavorite(Doctor doctor, ImageView favoriteIcon) {
        DatabaseReference doctorRef = favRef.child(doctor.getDoctorId());

        doctorRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // ❌ Remove from favorites
                doctorRef.removeValue();
                favoriteIcon.setImageResource(R.drawable.heart);
                favoriteIcon.setTag("empty");
                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                // ✅ Add to favorites
                doctorRef.setValue(doctor);
                favoriteIcon.setImageResource(R.drawable.heart1);
                favoriteIcon.setTag("filled");
                Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
