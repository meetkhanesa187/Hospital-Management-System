package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find UI elements
        TextView tvHealthPriority = findViewById(R.id.tvHealthPriority);
        TextView tvOurPriority = findViewById(R.id.tvOurpriority);
        Button btnStart = findViewById(R.id.btnGetStarted);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Apply animations (Avoid reapplying when activity is recreated)
        if (savedInstanceState == null) {
            tvHealthPriority.startAnimation(fadeIn);
            tvOurPriority.startAnimation(fadeIn);
            btnStart.startAnimation(bounce);
        }

        // Set click listener for the button
        btnStart.setOnClickListener(v -> {
            btnStart.setEnabled(false);  // Prevent multiple rapid clicks
            startActivity(new Intent(MainActivity.this, CategoriesActivity.class));
            finish(); // Close the current activity
        });
    }
}
