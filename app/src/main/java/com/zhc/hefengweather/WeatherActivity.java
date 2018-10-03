package com.zhc.hefengweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zhc.hefengweather.gson.Forecast;
import com.zhc.hefengweather.gson.Weather;
import com.zhc.hefengweather.service.AutoUpdateService;
import com.zhc.hefengweather.util.HttpUtil;
import com.zhc.hefengweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 请求天气数据
 */

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private ImageView weatherInfoIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 判断SDK是否大于Android5.0
        if (Build.VERSION.SDK_INT >= 21){
            // 将布局显示在状态栏
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            // 将状态栏设置为透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        // 初始化各种控件
        bingPicImg = findViewById(R.id.bing_pic_img);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        weatherInfoIcon = findViewById(R.id.weather_info_icon);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            // 无缓存时去服务器查询天气
            //String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherId = getIntent().getStringExtra("weather_id");// 从Intent中取出天气id
            weatherLayout.setVisibility(View.INVISIBLE);// 隐藏ScrollView
            requestWeather(mWeatherId);// 从服务器请求天气数据
        }
        // 下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击按钮打开滑动菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=e269dd3652e74aa198daeed43a2205e1";
        // 向该地址发出请求
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();// 返回的数据
                // 调用Utility的handleWeatherResponse()方法将返回的JSON数据转换成Weather对象
                final Weather weather = Utility.handleWeatherResponse(responseText);
                // 将当前线程切换到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 如果服务器返回的status的状态是ok，就说明请求天气成功
                        if (weather != null && "ok".equals(weather.status)){
                            // 将返回的数据缓存到SharedPreferences中
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);// 将内容进行显示
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });

            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).
                                into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        // 从weather对象中获取数据，然后显示在相应的控件上
        String cityName = weather.basic.cityName;// 城市
        String updateTime = weather.basic.update.updateTime.split(" ")[1];// 发布时间
        String degree = weather.now.temperature + "℃";// 当前温度
        String weatherInfo = weather.now.more.info;// 天气状况
        switchIcon(weatherInfo,weatherInfoIcon);
        titleCity.setText(cityName);
        titleUpdateTime.setText("发布时间\n"+"\t"+updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();// 移除所有视图
        // 遍历处理每天天气信息
        for(Forecast forecast : weather.forecastList){
            // 动态加载forecast_item布局，并设置相应数据
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            ImageView infoIcon = view.findViewById(R.id.info_icon);
            String forecastInfo = forecast.more.info;
            switchIcon(forecastInfo,infoIcon);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
            forecastLayout.addView(view);// 添加到父布局当中
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);// 将ScrollView设置为可见
        // 启动自动更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    // 根据不同天气状况，加载相应的状况图标
    private void switchIcon(String forecastInfo, ImageView infoIcon) {
        switch (forecastInfo){
            case "晴":
                infoIcon.setImageResource(R.drawable.a100);
                break;
            case "多云":
                infoIcon.setImageResource(R.drawable.a101);
                break;
            case "少云":
                infoIcon.setImageResource(R.drawable.a102);
                break;
            case "晴间多云":
                infoIcon.setImageResource(R.drawable.a103);
                break;
            case "阴":
                infoIcon.setImageResource(R.drawable.a104);
                break;
            case "有风":
                infoIcon.setImageResource(R.drawable.a200);
                break;
            case "平静":
                infoIcon.setImageResource(R.drawable.a201);
                break;
            case "微风":
                infoIcon.setImageResource(R.drawable.a202);
                break;
            case "和风":
                infoIcon.setImageResource(R.drawable.a203);
                break;
            case "清风":
                infoIcon.setImageResource(R.drawable.a204);
                break;
            case "强风/劲风":
                infoIcon.setImageResource(R.drawable.a205);
                break;
            case "疾风":
                infoIcon.setImageResource(R.drawable.a206);
                break;
            case "大风":
                infoIcon.setImageResource(R.drawable.a207);
                break;
            case "烈风":
                infoIcon.setImageResource(R.drawable.a208);
                break;
            case "风暴":
                infoIcon.setImageResource(R.drawable.a209);
                break;
            case "狂爆风":
                infoIcon.setImageResource(R.drawable.a210);
                break;
            case "飓风":
                infoIcon.setImageResource(R.drawable.a211);
                break;
            case "龙卷风":
                infoIcon.setImageResource(R.drawable.a212);
                break;
            case "热带风暴":
                infoIcon.setImageResource(R.drawable.a213);
                break;
            case "阵雨":
                infoIcon.setImageResource(R.drawable.a300);
                break;
            case "强阵雨":
                infoIcon.setImageResource(R.drawable.a301);
                break;
            case "雷阵雨":
                infoIcon.setImageResource(R.drawable.a302);
                break;
            case "强雷阵雨":
                infoIcon.setImageResource(R.drawable.a303);
                break;
            case "雷阵雨伴有冰雹":
                infoIcon.setImageResource(R.drawable.a304);
                break;
            case "小雨":
                infoIcon.setImageResource(R.drawable.a305);
                break;
            case "中雨":
                infoIcon.setImageResource(R.drawable.a306);
                break;
            case "大雨":
                infoIcon.setImageResource(R.drawable.a307);
                break;
            case "极端降雨":
                // a308图片损坏
                infoIcon.setImageResource(R.drawable.a312);
                break;
            case "毛毛雨/细雨":
                infoIcon.setImageResource(R.drawable.a309);
                break;
            case "暴雨":
                infoIcon.setImageResource(R.drawable.a310);
                break;
            case "大暴雨":
                infoIcon.setImageResource(R.drawable.a311);
                break;
            case "特大暴雨":
                infoIcon.setImageResource(R.drawable.a312);
                break;
            case "冻雨":
                infoIcon.setImageResource(R.drawable.a313);
                break;
            case "小到中雨":
                infoIcon.setImageResource(R.drawable.a314);
                break;
            case "中到大雨":
                infoIcon.setImageResource(R.drawable.a315);
                break;
            case "大到暴雨":
                infoIcon.setImageResource(R.drawable.a316);
                break;
            case "暴雨到大暴雨":
                infoIcon.setImageResource(R.drawable.a317);
                break;
            case "大暴雨到特大暴雨":
                infoIcon.setImageResource(R.drawable.a318);
                break;
            case "雨":
                infoIcon.setImageResource(R.drawable.a399);
                break;
            case "小雪":
                infoIcon.setImageResource(R.drawable.a400);
                break;
            case "中雪":
                infoIcon.setImageResource(R.drawable.a401);
                break;
            case "大雪":
                infoIcon.setImageResource(R.drawable.a402);
                break;
            case "暴雪":
                infoIcon.setImageResource(R.drawable.a403);
                break;
            case "雨夹雪":
                infoIcon.setImageResource(R.drawable.a404);
                break;
            case "雨雪天气":
                infoIcon.setImageResource(R.drawable.a405);
                break;
            case "阵雨夹雪":
                infoIcon.setImageResource(R.drawable.a406);
                break;
            case "阵雪":
                infoIcon.setImageResource(R.drawable.a407);
                break;
            case "小到中雪":
                infoIcon.setImageResource(R.drawable.a408);
                break;
            case "中到大雪":
                infoIcon.setImageResource(R.drawable.a409);
                break;
            case "大到暴雪":
                infoIcon.setImageResource(R.drawable.a410);
                break;
            case "雪":
                infoIcon.setImageResource(R.drawable.a499);
                break;
            case "薄雾":
                infoIcon.setImageResource(R.drawable.a500);
                break;
            case "雾":
                infoIcon.setImageResource(R.drawable.a501);
                break;
            case "霾":
                infoIcon.setImageResource(R.drawable.a502);
                break;
            case "扬沙":
                infoIcon.setImageResource(R.drawable.a503);
                break;
            case "浮尘":
                infoIcon.setImageResource(R.drawable.a504);
                break;
            case "沙尘暴":
                infoIcon.setImageResource(R.drawable.a507);
                break;
            case "强沙尘暴":
                infoIcon.setImageResource(R.drawable.a508);
                break;
            case "浓雾":
                infoIcon.setImageResource(R.drawable.a509);
                break;
            case "强浓雾":
                infoIcon.setImageResource(R.drawable.a510);
                break;
            case "中度霾":
                infoIcon.setImageResource(R.drawable.a511);
                break;
            case "重度霾":
                infoIcon.setImageResource(R.drawable.a512);
                break;
            case "严重霾":
                infoIcon.setImageResource(R.drawable.a513);
                break;
            case "大雾":
                infoIcon.setImageResource(R.drawable.a514);
                break;
            case "特强浓雾":
                infoIcon.setImageResource(R.drawable.a515);
                break;
            case "热":
                infoIcon.setImageResource(R.drawable.a900);
                break;
            case "冷":
                infoIcon.setImageResource(R.drawable.a901);
                break;
            default:
                infoIcon.setImageResource(R.drawable.a999);
                break;
        }
    }

}
