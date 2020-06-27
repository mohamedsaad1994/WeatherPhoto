package com.robusta.photoweather.ui.main;

import android.util.Log;

import com.robusta.photoweather.data.models.WeatherInfo;
import com.robusta.photoweather.data.webService.RetrofitClient;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter implements MainContractor.Presenter {
    private static final String TAG = "MainPresenter";
    private MainContractor.View view;
    private CompositeDisposable dis = new CompositeDisposable();

    public MainPresenter(MainContractor.View view) {
        this.view = view;
    }

    @Override
    public void getWeatherInfo(double lat, double lon) {
        Single observable = RetrofitClient.getInstance().getApi().getWeatherInfo("83f8cae831f0d76b707155736944a7ac", lat, lon)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        SingleObserver<WeatherInfo> observer = new SingleObserver<WeatherInfo>() {
            @Override
            public void onSubscribe(Disposable d) {
                dis.add(d);
            }

            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                view.setWeatherInfo(weatherInfo);
            }

            @Override
            public void onError(Throwable e) {
                view.setWeatherInfoError(e.getMessage());
                Log.d(TAG, "onError: " + e.getMessage());
            }
        };

        observable.subscribe(observer);
    }
}
