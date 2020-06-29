package org.odk.collect.bdrs.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.preferences.GeneralKeys;

import timber.log.Timber;

public class ShowInServerActivity extends CollectAbstractActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_in_server);
        initToolbar();

        //Get Main Server URL
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ShowInServerActivity.this);
        String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, getApplication().getString(R.string.default_server_url));
        while (mainServerURL.endsWith("/")) {
            mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
        }
        //Get Notification URL
        String getShowInServerUrl = getResources().getString(R.string.default_odk_show_in_server);
        //Get User ID
        String suid = new Integer(AndroidTutorialApp.uid).toString();
        //Get FormID
        String formid = new Integer(DowloadedFormID.frmid).toString();
        // Concatenate Server URL and Notification URL
        String showInServerUrl = mainServerURL + getShowInServerUrl;
        //Set params
        String paramUserID = "?UserID=" + suid;
        //String paramFormID = "&FormID=" + formid;
        String paramFormID = "&FormID=106";

        // Add Parameter with the URL
         String showInServerUrlWithParam = showInServerUrl + paramUserID + paramFormID;
        // Change the http:// to https://
        String securedShowInServerUrlWithParam = showInServerUrlWithParam.replace("http://","https://");
        //String securedShowInServerUrlWithParam = "https://ecds.solversbd.com/Reports/HorizontalShowUserDataReport.php?UserID=467&FormID=106";

        Timber.d("%s Show in Server URL: ", securedShowInServerUrlWithParam);

        WebView myWebView = (WebView) findViewById(R.id.wvShowInServer);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(securedShowInServerUrlWithParam);

        //Toast.makeText(this, securedShowInServerUrlWithParam, Toast.LENGTH_LONG).show();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);

        setSupportActionBar(toolbar);
    }
}