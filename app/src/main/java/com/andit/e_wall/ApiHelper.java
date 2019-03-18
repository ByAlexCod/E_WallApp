package com.andit.e_wall;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.andit.e_wall.data_model.BoardModel;
import com.andit.e_wall.data_model.PathModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiHelper {
    /*to controller */
    public static String baseAPIUrl = "http://192.168.42.94:5000/api";
    private Runnable callback;

    public ApiHelper(Runnable callbackMethod) {
        callback = callbackMethod;
    }

    public ApiHelper() {
    }

    public String SendQRCodeResult(String qrresult, AppCompatActivity ctx, MainPage.ApiRequestListener req) {
        OkHttpClient client = new OkHttpClient();

        String url = null;
        try {
            url = baseAPIUrl + "/paths/invitation/use/" + URLEncoder.encode(qrresult, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String[] token = new String[1];
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                req.apiResult(response.body().string());

            }

        });
        return "registered, go to map";


    }

    public void getPathBoards(String token, MapPage.ApiRequestListener req) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();

        String url = baseAPIUrl + "/paths/entry/use/token/" + URLEncoder.encode(token, "UTF-8");

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                ObjectReader reader = objectMapper.reader().forType(PathModel.class);
                PathModel rere = reader.readValue(response.body().string());
                Log.i("bug254", rere.getPathName());
                req.apiResult(rere.getBoardsList());
            }
        });
    }

    public void getBoardMessages(String token, String boardId, ARPage.ApiRequestMessageList req) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();

        String url = baseAPIUrl + "/paths/entry/use/messages/" + boardId + "/" + URLEncoder.encode(token, "UTF-8");

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                ObjectReader reader = objectMapper.reader().forType(BoardModel.class);
                BoardModel rere = reader.readValue(response.body().string());
                Log.i("bug254", rere.getName());
                req.apiResult(rere.getMessagesList(), rere);
            }
        });
    }
}
