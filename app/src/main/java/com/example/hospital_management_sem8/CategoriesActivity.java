package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CategoriesActivity extends AppCompatActivity {

    private Button btnAdmin, btnDoctor, btnPatient;
    private TextView tvCategoriesHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        // Initialize UI components
        tvCategoriesHeader = findViewById(R.id.tvCategoriesHeader);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnDoctor = findViewById(R.id.btnDoctor);
        btnPatient = findViewById(R.id.btnPatient);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);

        // Apply animations
        tvCategoriesHeader.startAnimation(fadeIn);
        applyAnimationToButtons(slideIn, btnAdmin, btnDoctor, btnPatient);

        // Set click listeners
        btnAdmin.setOnClickListener(v -> navigateTo(AdminLoginActivity.class));
        btnDoctor.setOnClickListener(v -> navigateTo(LoginActivity.class));
        btnPatient.setOnClickListener(v -> navigateTo(PatientLoginActivity.class));
    }

    // Method to apply animation to multiple buttons
    private void applyAnimationToButtons(Animation animation, Button... buttons) {
        for (Button button : buttons) {
            button.startAnimation(animation);
        }
    }

    // Method to navigate to another activity
    private void navigateTo(Class<?> targetActivity) {
        startActivity(new Intent(CategoriesActivity.this, targetActivity));
    }
}
