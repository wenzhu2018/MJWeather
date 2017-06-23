package com.mj.weather.weather.model.repository;

import com.mj.weather.weather.model.http.ApiClient;
import com.mj.weather.weather.model.http.HeProtocol;
import com.mj.weather.weather.model.http.entity.HeBean;

import io.reactivex.Observable;

/**
 * Created by MengJie on 2017/6/19.
 */

public class HeRepository {

    public static Observable<HeBean.RspWeather> getWeather(String city) {
        return ApiClient.heService.getWeather(city, HeProtocol.key);
    }

}