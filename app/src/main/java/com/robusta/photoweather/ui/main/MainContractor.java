package com.robusta.photoweather.ui.main;

import com.robusta.photoweather.data.models.WeatherInfo;

public interface MainContractor {
    interface View {
        void showProgress();

        void hideProgress();

        void setLatLng();

        void setWeatherInfo(WeatherInfo weatherInfo);

        void setWeatherInfoError(String errorMsg);
    }

    interface Presenter {

        void getWeatherInfo(double lat,double lon);
    }
}
