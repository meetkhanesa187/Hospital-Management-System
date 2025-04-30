package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private TextView tvAdminForgotPassword;
    private ImageView AdminLogo;
    private FirebaseAuth mAuth;

    // Securely store admin credentials (avoid hardcoding in production)
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin@187";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminlogin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        etAdminEmail = findViewById(R.id.AdminEmail);
        etAdminPassword = findViewById(R.id.AdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvAdminForgotPassword = findViewById(R.id.tvAdminForgotPassword);
        AdminLogo = findViewById(R.id.AdminLogo);

        // Login button click listener
        btnAdminLogin.setOnClickListener(v -> validateAndLogin());

        // Forgot password click listener
        tvAdminForgotPassword.setOnClickListener(v ->
                Toast.makeText(AdminLoginActivity.this, "Forgot Password? Contact IT Support", Toast.LENGTH_SHORT).show()
        );
    }

    private void validateAndLogin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etAdminEmail.setError("Email is required");
            etAdminEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etAdminEmail.setError("Enter a valid email");
            etAdminEmail.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etAdminPassword.setError("Password is required");
            etAdminPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etAdminPassword.setError("Password must be at least 6 characters");
            etAdminPassword.requestFocus();
            return;
        }

        // Authenticate admin
        if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            Toast.makeText(AdminLoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();

            // Redirect to admin dashboard
            startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
            finish();
        } else {
            Toast.makeText(AdminLoginActivity.this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
