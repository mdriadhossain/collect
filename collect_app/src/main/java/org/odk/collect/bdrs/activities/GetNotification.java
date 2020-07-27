package org.odk.collect.bdrs.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.viewmodels.Notice;
import org.odk.collect.bdrs.preferences.GeneralKeys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class GetNotification extends CollectAbstractActivity{
    private static String url;

    String URL_GET_DATA = "";
    RecyclerView recyclerView;
    NoticeAdapter adapter;
    List<Notice> noticeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_notification);
        initToolbar();



        //Get Main Server URL
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(GetNotification.this);
        String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, getApplication().getString(R.string.default_server_url));
        while (mainServerURL.endsWith("/")) {
            mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
        }
        //Get Notification URL
        //String getNotificationUrl = getResources().getString(R.string.default_odk_get_notification);
        String getNotificationUrl = settings.getString(GeneralKeys.KEY_NOTIFICATION_URL, getApplication().getString(R.string.default_odk_get_notification));

        //Get User ID
        String suid = new Integer(AndroidTutorialApp.uid).toString();
        // Concatenate Server URL and Notification URL
        String notificationURL = mainServerURL + getNotificationUrl;
        //Set params
        String paramUserID = "?UserID=" + suid;

        // Add Parameter with the URL
        String notificationUrlWithParam = notificationURL + paramUserID;
        // Change the http:// to https://
        String securedNotificationUrlWithParam = notificationUrlWithParam.replace("http://", "https://");
        String securedNotificationUrl = notificationURL.replace("http://", "https://");
        //String securedNotificationUrlWithParam = "https://ecds.solversbd.com/Main/Notification.php?UserID=467";

        url = securedNotificationUrlWithParam;
        //url = "https://steptoonline.com/lib/SampleJSON.php";

        URL_GET_DATA = url;

        recyclerView = findViewById(R.id.notice_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noticeList = new ArrayList<>();

        loadNotices();

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);

        setSupportActionBar(toolbar);
    }

    private void loadNotices() {
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, URL_GET_DATA,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            Timber.tag("NoticeData::").d(String.valueOf(jsonArray));
                            Timber.tag("NoticeDataLength::").d(String.valueOf(jsonArray.length()));

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                Notice notice = new Notice(
                                        obj.getString("id"),
                                        obj.getString("FullName"),
                                        obj.getString("Notification"),
                                        obj.getString("Status"),
                                        obj.getString("DataEntryDate"),
                                        obj.getString("NotificationReadTime")
                                );

                                noticeList.add(notice);
                            }

                            adapter = new NoticeAdapter(noticeList, getApplicationContext());
                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}