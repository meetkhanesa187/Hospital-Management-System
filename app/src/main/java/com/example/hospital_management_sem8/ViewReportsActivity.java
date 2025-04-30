package com.example.hospital_management_sem8;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class ViewReportsActivity extends AppCompatActivity {

    private ListView reportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        reportsList = findViewById(R.id.reports_list);

        // Load the reports data (placeholder for database fetch)
        // Example: Use an ArrayAdapter to display report titles
        // reportsList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reportTitles));
    }
}
