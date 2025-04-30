package com.example.hospital_management_sem8;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    @GET("v2/everything")
    Call<NewsResponse> getEverything(
            @Query("q") String query,
            @Query("language") String language,
            @Query("apiKey") String apiKey
    );
}

