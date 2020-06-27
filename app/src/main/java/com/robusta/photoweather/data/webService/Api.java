package com.robusta.photoweather.data.webService;

import com.robusta.photoweather.data.models.WeatherInfo;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
    @GET("data/2.5/weather")
    Single<WeatherInfo> getWeatherInfo(
            @Query("appid") String apiKey,
            @Query("lat") double lat,
            @Query("lon") double lon);
}
