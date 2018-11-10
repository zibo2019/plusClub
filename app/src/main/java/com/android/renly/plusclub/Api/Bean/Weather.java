package com.android.renly.plusclub.Api.Bean;

import com.google.gson.annotations.SerializedName;

public class Weather {
    @SerializedName("weatherinfo")
    private WeatherInfo weatherinfo;

    public Weather() {
        super();
    }

    public WeatherInfo getWeatherinfo() {
        return weatherinfo;
    }

    public void setWeatherinfo(WeatherInfo weatherinfo) {
        this.weatherinfo = weatherinfo;
    }
}