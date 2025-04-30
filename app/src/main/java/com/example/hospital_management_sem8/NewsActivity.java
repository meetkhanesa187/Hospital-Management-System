package com.example.hospital_management_sem8;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {

    private LinearLayout newsIcon, doctorIcon, articlesIcon, favoriteIcon, profileIcon, helpSection;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;

    // ✅ Replace with your actual NewsAPI key
    private static final String API_KEY = "5c72fe8e413e4b43a6f878169a3f678b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation icons
        newsIcon = findViewById(R.id.news_icon);
        doctorIcon = findViewById(R.id.doctor_icon);
        articlesIcon = findViewById(R.id.articles_icon);
        favoriteIcon = findViewById(R.id.favorite_icon);
        profileIcon = findViewById(R.id.profile_icon);
        helpSection = findViewById(R.id.help_section);

        newsIcon.setOnClickListener(view ->
                Toast.makeText(NewsActivity.this, "You are already on the News tab", Toast.LENGTH_SHORT).show());

        doctorIcon.setOnClickListener(view -> openActivity(DoctorsDetailsActivity.class));
        articlesIcon.setOnClickListener(view -> openActivity(PatientHomeActivity.class));
        favoriteIcon.setOnClickListener(view -> openActivity(FavoriteDoctorsActivity.class));
        profileIcon.setOnClickListener(view -> openActivity(UpdatePatientProfileActivity.class));

        if (helpSection != null) {
            helpSection.setOnClickListener(view -> openActivity(ChatWithAIActivity.class));
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchNews(); // 🔥 Load news from API
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(NewsActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void fetchNews() {
        NewsApiService service = ApiClient.getRetrofitInstance().create(NewsApiService.class);
        Call<NewsResponse> call = service.getEverything("health", "en", API_KEY);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();
                    if (!articles.isEmpty()) {
                        newsAdapter = new NewsAdapter(NewsActivity.this, articles);
                        recyclerView.setAdapter(newsAdapter);
                    } else {
                        Toast.makeText(NewsActivity.this, "No news articles found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewsActivity.this, "Failed to retrieve news.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Toast.makeText(NewsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
