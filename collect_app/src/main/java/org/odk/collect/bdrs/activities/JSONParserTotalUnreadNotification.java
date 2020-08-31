package org.odk.collect.bdrs.activities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class JSONParserTotalUnreadNotification {
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    static String jsonData;

    private final OkHttpClient httpClient = new OkHttpClient();

    // constructor
    public JSONParserTotalUnreadNotification() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod
    public JSONObject makeOkHttpRequest(String url, String method, String userid) {
        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("userid", userid)
                .build();

        /*Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "OkHttp Bot")
                .post(formBody)
                .build();*/
        Request request = new Request.Builder()
                .url(url)
                .build();
        Timber.tag("RequestURL: ").d(String.valueOf(request));
        // Making HTTP request
        try {
            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    // Get response body
                    jsonData = response.body().string().toString();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(jsonData);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }
}
