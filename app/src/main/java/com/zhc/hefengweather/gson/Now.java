package com.zhc.hefengweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhc on 2018/9/26.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }

}
