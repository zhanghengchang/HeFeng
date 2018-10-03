package com.zhc.hefengweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by zhc on 2018/9/25.
 * 向服务器发送请求
 */

public class HttpUtil {

    public static void sendOKHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
