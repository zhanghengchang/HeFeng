package com.zhc.hefengweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhc on 2018/9/26.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
