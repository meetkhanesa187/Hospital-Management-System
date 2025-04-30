package com.example.hospital_management_sem8;

public class FavouriteDoctor {
    private String doctorId;
    private String fullName;

    public FavouriteDoctor() {
        // Default constructor required for Firebase
    }

    public FavouriteDoctor(String doctorId, String fullName) {
        this.doctorId = doctorId;
        this.fullName = fullName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getFullName() {
        if (fullName != null) {
            return fullName.replace("_", " ").trim(); // Ensure proper spacing
        }
        return "";
    }
}
