package org.odk.collect.bdrs.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.preferences.GeneralKeys;

public class DashboardActivity extends CollectAbstractActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initToolbar();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Get Main Server URL
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
        String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, getApplication().getString(R.string.default_server_url));
        while (mainServerURL.endsWith("/")) {
            mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
        }
        //Get Notification URL
        String getDashboardUrl = getResources().getString(R.string.default_odk_get_dashboard);
        //Get User ID
        String suid = new Integer(AndroidTutorialApp.uid).toString();
        //Get FormID
        String formid = new Integer(DowloadedFormID.frmid).toString();
        // Concatenate Server URL and Notification URL
        String dashboardUrl = mainServerURL + getDashboardUrl;
        //Set params
        String paramUserID = "?UserID=" + suid;
        String paramFormID = "&FormID=" + formid;

        // Add Parameter with the URL
        String dashboardUrlWithParam = dashboardUrl + paramUserID;
        // Change the http:// to https://
        String securedDashboardUrlWithParam= dashboardUrlWithParam.replace("http://","https://");
        //String securedDashboardUrlWithParam = "https://ecds.solversbd.com/UserRole/UserDashboard.php?UserID=467";

        WebView myWebView = (WebView) findViewById(R.id.wvDashboard);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(securedDashboardUrlWithParam);

        //Toast.makeText(this, securedDashboardUrlWithParam, Toast.LENGTH_LONG).show();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);

        setSupportActionBar(toolbar);
    }
}