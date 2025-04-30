package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;

public class ChatWithAIActivity extends AppCompatActivity {

    private TextView chatTitle, chatHistory;
    private RecyclerView optionList;
    private OptionAdapter adapter;

    private final List<String> defaultOptions = Arrays.asList(
            "Book Appointment", "Check Reports", "Hospital Location",
            "Emergency Help", "Talk to Doctor", "Opening Hours"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_ai);

        chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText("AI Chat Support");

        chatHistory = findViewById(R.id.chat_history);
        optionList = findViewById(R.id.option_list);

        optionList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OptionAdapter(defaultOptions, this::handleOptionSelected);
        optionList.setAdapter(adapter);
    }

    private void handleOptionSelected(String selectedOption) {
        chatHistory.append("\nYou: " + selectedOption + "\n");

        // Show response description
        String description = getDescription(selectedOption);
        chatHistory.append("AI: " + description + "\n");

        // Then show tappable link, if available
        ClickableSpan linkSpan = getLinkIfAvailable(selectedOption);
        if (linkSpan != null) {
            SpannableString linkText = new SpannableString("Click here to continue.");
            linkText.setSpan(linkSpan, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            chatHistory.append(linkText);
            chatHistory.append("\n");
            chatHistory.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // Scroll to bottom
        findViewById(R.id.chat_scroll).post(() ->
                findViewById(R.id.chat_scroll).scrollTo(0, chatHistory.getBottom())
        );

        // Show options again
        adapter.setOptions(defaultOptions);
    }

    private String getDescription(String option) {
        option = option.toLowerCase();
        if (option.contains("appointment")) {
            return "You can schedule a consultation with a doctor of your choice.";
        } else if (option.contains("report")) {
            return "You can access your medical test reports securely.";
        } else if (option.contains("location")) {
            return "Our hospital is located at Rajkot-morbi highway," +
                    " near marwadi university," +
                    " Rajkot-360003.";
        } else if (option.contains("emergency")) {
            return "For emergencies, please call 108 or visit our ER immediately.";
        } else if (option.contains("doctor")) {
            return "We can connect you to available specialists for a live chat or booking.";
        } else if (option.contains("hours")) {
            return "The hospital operates 24/7. Departments are open from 9 AM to 5 PM.";
        } else {
            return "Let me help you further. Please provide more details.";
        }
    }

    private ClickableSpan getLinkIfAvailable(String option) {
        switch (option) {
            case "Book Appointment":
                return new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        startActivity(new Intent(ChatWithAIActivity.this, BookingActivity.class));
                    }
                };
            case "Check Reports":
                return new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        startActivity(new Intent(ChatWithAIActivity.this, ViewReportsActivity.class));
                    }
                };
            default:
                return null;
        }
    }
}
