package com.mj.weather.weather.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mj.weather.R;
import com.mj.weather.weather.activity.MainActivity;
import com.mj.weather.weather.contract.WeatherContract;
import com.mj.weather.weather.model.http.entity.HeBean;
import com.mj.weather.weather.view.adapter.AirAdapter;
import com.mj.weather.weather.view.adapter.ForecastAdapter;
import com.mj.weather.weather.view.adapter.TipAdapter;
import com.mj.weather.common.base.BaseFragment;
import com.mj.weather.common.util.MyNestedScrollView;
import com.mj.weather.common.util.ToastUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by MengJie on 2017/6/25.
 */

public class WeatherFragment extends BaseFragment implements WeatherContract.View {
    private static final String TAG = "WeatherFragment";

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_tmp_now)
    TextView tvTmpNow;
    @BindView(R.id.tv_txt_now)
    TextView tvTxtNow;
    @BindView(R.id.tv_wind_now)
    TextView tvWindNow;
    @BindView(R.id.recycler_view_forecast)
    RecyclerView rvForecast;
    @BindView(R.id.recycler_view_aqi)
    RecyclerView rvAqi;
    @BindView(R.id.recycler_view_suggestion)
    RecyclerView rvSuggestion;
    @BindView(R.id.nested_scroll_view)
    MyNestedScrollView scrollView;
    private WeatherContract.Presenter mPresenter;
    private Context context;


    private List<HeBean.Daily_forecast> forecastList = new ArrayList<>();
    private List<HeBean.AirQuality> aqiList = new ArrayList<>();
    private List<HeBean.Tip> tipList = new ArrayList<>();

    private ForecastAdapter forecastAdapter;
    private AirAdapter airAdapter;
    private TipAdapter tipAdapter;
    private MainActivity activity;
    private String cityName = "";
    private String districtName = "";

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(WeatherContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        //ButterKnife
        ButterKnife.bind(this, view);
        context = getContext();
        activity = (MainActivity) getActivity();
        //initView
        initView();

        return view;
    }

    private void initView() {

        //forecast
        forecastAdapter = new ForecastAdapter(forecastList);
        LinearLayoutManager forecastLayoutManager = new LinearLayoutManager(context);
        rvForecast.setLayoutManager(forecastLayoutManager);
        rvForecast.setAdapter(forecastAdapter);

        //aqi
        airAdapter = new AirAdapter(aqiList);
        GridLayoutManager airLayoutManager = new GridLayoutManager(context, 2);
        rvAqi.setLayoutManager(airLayoutManager);
        rvAqi.setAdapter(airAdapter);

        //tip
        tipAdapter = new TipAdapter(tipList);
        GridLayoutManager tipLayoutManager = new GridLayoutManager(context, 2);
        rvSuggestion.setLayoutManager(tipLayoutManager);
        rvSuggestion.setAdapter(tipAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //然后刷新数据 刷新背景
                mPresenter.getWeather(cityName, districtName);
                activity.loadBackground();
            }
        });
    }

    /**
     * weatherObserver
     * @return
     */
    @Override
    public Observer<? super HeBean.RspWeather> weatherObserver() {
        return new Observer<HeBean.RspWeather>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull HeBean.RspWeather rspWeather) {
                stopRefresh();
                loadWeather(rspWeather);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Logger.e(e.getMessage());
                ToastUtils.showToast(e.getMessage());
                stopRefresh();
            }

            @Override
            public void onComplete() {
                stopRefresh();
            }
        };
    }

    /**
     * weatherCacheObserver
     * @return
     */
    @Override
    public Observer<? super HeBean.RspWeather> weatherCacheObserver() {
        return new Observer<HeBean.RspWeather>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull HeBean.RspWeather rspWeather) {
                loadWeather(rspWeather);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Logger.d(e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };
    }

    @Override
    public void setCity(String city, String district) {
        cityName = city;
        districtName = district;
        activity.setTitle(district);
    }

    private void loadWeather(HeBean.RspWeather rspWeather) {
        HeBean.HeWeather5 weather = rspWeather.HeWeather5.get(0);
        if (weather.status.equals("ok")) {
            HeBean.Now now = weather.now;
            if (now != null) {
                tvTmpNow.setText(now.tmp + "");
                tvTxtNow.setText(now.cond.txt);
                tvWindNow.setText("湿度：" + now.hum + "    " + now.wind.dir + "：" + now.wind.sc);
            } else {
                Logger.e("now=null");
            }

            List<HeBean.Daily_forecast> forecasts = weather.daily_forecast;
            forecastList.clear();
            if (forecasts != null) {
                forecastList.addAll(forecasts);
            } else {
                Logger.e("forecasts=null");
            }
            forecastAdapter.notifyDataSetChanged();

            HeBean.Aqi aqi = weather.aqi;
            aqiList.clear();
            if (aqi != null) {
                aqiList.addAll(HeBean.getAqiList(aqi.city));
            } else {
                ToastUtils.showTestToast("aqi=null");
                Logger.e("aqi=null");
            }
            airAdapter.notifyDataSetChanged();

            HeBean.Suggestion suggestion = weather.suggestion;
            tipList.clear();
            if (suggestion != null) {
                tipList.addAll(HeBean.getSuggestionList(suggestion));
            } else {
                Logger.e("suggestion=null");
            }
            tipAdapter.notifyDataSetChanged();

        } else {
            ToastUtils.showToast(weather.status);
        }
    }

    @Override
    public void stopRefresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
