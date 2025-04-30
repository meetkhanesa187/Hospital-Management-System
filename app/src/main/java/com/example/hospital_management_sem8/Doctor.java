package com.example.hospital_management_sem8;

import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class Doctor implements Serializable {
    private String fullName;
    private String email;
    private int experience;
    private String specialization;
    private boolean isApproved;
    private String bio;
    private String mobileNumber;
    private String profileImage;
    private String doctorId;

    // Default constructor (required for Firebase)
    public Doctor() {}

    // New constructor for fetching only doctorId and fullName
    public Doctor(String doctorId, String fullName) {
        this.doctorId = doctorId;
        this.fullName = fullName;
    }

    // Full parameterized constructor
    public Doctor(String fullName, String email, int experience, String specialization, boolean isApproved, String bio, String mobileNumber, String profileImage, String doctorId) {
        this.fullName = fullName;
        this.email = email;
        this.experience = experience;
        this.specialization = specialization;
        this.isApproved = isApproved;
        this.bio = bio;
        this.mobileNumber = mobileNumber;
        this.profileImage = profileImage;
        this.doctorId = doctorId;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public int getExperience() {
        return experience;
    }

    public String getSpecialization() {
        return specialization;
    }

    @Exclude
    public boolean isApproved() {
        return isApproved;
    }

    public String getBio() {
        return bio;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getDoctorId() {
        return doctorId;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", experience=" + experience +
                ", specialization='" + specialization + '\'' +
                ", isApproved=" + isApproved +
                ", bio='" + bio + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", doctorId='" + doctorId + '\'' +
                '}';
    }
}

