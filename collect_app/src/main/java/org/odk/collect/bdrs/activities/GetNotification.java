package org.odk.collect.bdrs.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.preferences.GeneralKeys;

public class GetNotification extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_notification);
        //initToolbar();
        //Toast.makeText(this, "This is Notification Activity", Toast.LENGTH_LONG).show();

        //Get Main Server URL
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(GetNotification.this);
        String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, getApplication().getString(R.string.default_server_url));
        while (mainServerURL.endsWith("/")) {
            mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
        }
        //Get Notification URL
        String getNotificationUrl = getResources().getString(R.string.default_odk_get_notification);
        //Get User ID
        String suid = new Integer(AndroidTutorialApp.uid).toString();
        // Concatenate Server URL and Notification URL
        String notificationURL = mainServerURL + getNotificationUrl;
        //Set params
        String paramUserID = "?UserID=" + suid;

        // Add Parameter with the URL
        String notificationUrlWithParam = notificationURL + paramUserID;
        // Change the http:// to https://
        String securedNotificationUrlWithParam= notificationUrlWithParam.replace("http://","https://");
        //String securedNotificationUrlWithParam = "https://ecds.solversbd.com/Main/Notification.php?UserID=467";

        WebView myWebView = (WebView) findViewById(R.id.wvGetNotification);
        myWebView.loadUrl(securedNotificationUrlWithParam);

        //Toast.makeText(this, securedNotificationUrlWithParam, Toast.LENGTH_LONG).show();
    }

    /*private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);

        setSupportActionBar(toolbar);
    }*/
}