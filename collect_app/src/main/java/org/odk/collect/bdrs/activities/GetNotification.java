package org.odk.collect.bdrs.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alespero.expandablecardview.ExpandableCardView;
import com.burgstaller.okhttp.digest.fromhttpclient.NameValuePair;
import com.google.api.client.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.preferences.GeneralKeys;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class GetNotification extends CollectAbstractActivity{
    // JSON parser class
    private String TAG = GetNotification.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;
    private ExpandableCardView noticeCardView;
    private static String url;

    ArrayList<HashMap<String, String>> noticeList;

    private String noticeID;
    private String noticeFrom;
    private String noticeMsg;
    private String noticeStatus;
    private String noticeEntryDate;
    private String noticeOpenDate;


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

        noticeList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.notice_list);

        noticeCardView = findViewById(R.id.noticeCardView);

        new GetNotices().execute();

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);

        setSupportActionBar(toolbar);
    }

    /**
     * Async task class to get json by making HTTP call
     */
    public class GetNotices extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(GetNotification.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandlerForNotification sh = new HttpHandlerForNotification();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Timber.e("Response from url: %s", jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray notices = jsonObj.getJSONArray("notificationData");
                    // looping through All Contacts
                    for (int i = 0; i < notices.length(); i++) {
                        JSONObject c = notices.getJSONObject(i);

                        noticeID = c.getString("id");
                        noticeFrom = c.getString("FullName");
                        noticeMsg = c.getString("Notification");
                        noticeStatus = c.getString("Status");
                        noticeEntryDate = c.getString("DataEntryDate");
                        noticeOpenDate = c.getString("NotificationReadTime");

                        // tmp hash map for single contact
                        HashMap<String, String> notice = new HashMap<>();

                        // adding each child node to HashMap key => value
                        notice.put("id", noticeID);
                        notice.put("FullName", noticeFrom);
                        notice.put("Notification", noticeMsg);
                        if (noticeStatus.equals("1")){
                            noticeStatus = "Opened";
                            //noticeCardView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.blue_500));
                        }
                        if (noticeStatus.equals("0")){
                            noticeStatus = "Unread";
                            //noticeCardView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.amber_500));
                        }

                        notice.put("Status", noticeStatus);
                        notice.put("DataEntryDate", noticeEntryDate);
                        notice.put("NotificationReadTime", noticeOpenDate);

                        // adding contact to contact list
                        noticeList.add(notice);
                    }
                } catch (final JSONException e) {
                    Timber.e("Json parsing error: %s", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Timber.e("Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(GetNotification.this, noticeList, R.layout.notice_list_row,
                    new String[]{"id", "FullName", "Notification", "Status", "DataEntryDate", "NotificationReadTime"},
                    new int[]{R.id.noticeID, R.id.noticeFrom, R.id.noticeMsg, R.id.noticeStatus,
                            R.id.noticeEntryDate, R.id.noticeOpenDate});
            lv.setAdapter(adapter);

        }
    }
}