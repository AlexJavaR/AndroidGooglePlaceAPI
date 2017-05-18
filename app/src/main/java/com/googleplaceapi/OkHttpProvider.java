package com.googleplaceapi;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class OkHttpProvider {

    private static OkHttpProvider ourInstance = new OkHttpProvider();

    private OkHttpClient client;

    static OkHttpProvider getInstance() {
        return ourInstance;
    }

    private OkHttpProvider() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    void getForGoogleDetails(String path, Callback callback) {
        Request request = new Request.Builder()
                .url(path)
                .get()
                .build();

        sendAsynchronousRequest(request, callback);
    }

    private void sendAsynchronousRequest(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }
}
