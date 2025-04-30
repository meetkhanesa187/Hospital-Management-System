package com.example.hospital_management_sem8;

public class Appointment {
    private String doctorName;
    private String patient;
    private String date;
    private String timeslot;
    private String extraInfo;

    public Appointment(String doctorName, String patient, String date, String timeslot, String extraInfo) {
        this.doctorName = doctorName;
        this.patient = patient;
        this.date = date;
        this.timeslot = timeslot;
        this.extraInfo = extraInfo;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getPatient() {
        return patient;
    }

    public String getDate() {
        return date;
    }

    public String getTimeslot() {
        return timeslot;
    }

    public String getExtraInfo() {
        return extraInfo;
    }
}
