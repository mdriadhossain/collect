package org.odk.collect.bdrs.activities;

import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.burgstaller.okhttp.digest.fromhttpclient.BasicNameValuePair;
import com.burgstaller.okhttp.digest.fromhttpclient.NameValuePair;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.preferences.PreferencesActivity;
import org.odk.collect.bdrs.preferences.GeneralKeys;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class LoginActivity extends Activity implements View.OnClickListener {

    private EditText user;
    private EditText pass;
    private Button mSubmit;
    private Button  mRegister;
    private CheckBox saveLoginCheckBox;

    private ImageView passShowHideImg;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private Application application;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;


    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.app_name);

        // setup input fields
        user = (EditText) findViewById(R.id.username);
        pass = (EditText) findViewById(R.id.password);

        passShowHideImg = findViewById(R.id.show_pass_btn);

        saveLoginCheckBox = (CheckBox)findViewById(R.id.saveLoginCheckBox);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        // setup buttons
        mSubmit = (Button) findViewById(R.id.login);
        mRegister = (Button) findViewById(R.id.register);
        //mRegister.setVisibility(Button.INVISIBLE);

        // register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            user.setText(loginPreferences.getString("username", ""));
            pass.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }
    }

    public void ShowHidePass(View view){
        if(view.getId()==R.id.show_pass_btn){
            if(pass.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                //passShowHideImg.setImageResource(R.drawable.hide_password);
                passShowHideImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.hide_password));
                //Show Password
                pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else{
                //passShowHideImg.setImageResource(R.drawable.show_password);
                passShowHideImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.show_password));
                //Hide Password
                pass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.login) {
            //isInternetOn();
            // Check for network connections
            ConnectivityManager connec =
                    (ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
            if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                    connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                    connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                    connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
                // if connected with internet
                // Toast.makeText(this, "Connected to Internet", Toast.LENGTH_LONG).show();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(user.getWindowToken(), 0);

                    String username = user.getText().toString();
                    String password = pass.getText().toString();

                    if (saveLoginCheckBox.isChecked()) {
                        loginPrefsEditor.putBoolean("saveLogin", true);
                        loginPrefsEditor.putString("username", username);
                        loginPrefsEditor.putString("password", password);
                        loginPrefsEditor.commit();
                    } else {
                        loginPrefsEditor.clear();
                        loginPrefsEditor.commit();
                    }

                    new AttemptLogin().execute();
            }

            else if (
                    connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                            connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {

                Toast.makeText(this, "Please check your Internet connection", Toast.LENGTH_LONG).show();

            }
        } else if (id == R.id.register) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
        } else {
        }
    }

    class AttemptLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            //pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            //int uid;

            String username = user.getText().toString();
            String password = pass.getText().toString();
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));

                //String LOGIN_URL = "http://ecds.solversbd.com/Lib/AppAuthChecker.php";
                //String LOGIN_URL = "http://data.solversbd.com/Lib/AppAuthChecker.php";
                // String authCheckUrl = "/Lib/AppAuthChecker.php";

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, getApplication().getString(R.string.default_server_url));
                while (mainServerURL.endsWith("/")) {
                    mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
                }
                String authCheckUrl = getResources().getString(R.string.default_odk_authchecker);

                String LOGIN_URL = mainServerURL + authCheckUrl;
                String REQUEST_METHOD = "POST";

                //Timber.d(mainServerURL);
                //Timber.d(authCheckUrl);
                Timber.d(username);
                Timber.d(password);
                Timber.d(LOGIN_URL);
                Timber.d(REQUEST_METHOD);
                Timber.d("starting");

                JSONObject json = jsonParser.makeOkHttpRequest(LOGIN_URL, REQUEST_METHOD, username, password);

                // check your log for json response
                Timber.d(json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                AndroidTutorialApp.uid=success;

                if (success > 0)
                {
                    Timber.d(json.toString());
                    Timber.d(String.valueOf(success));

                   // UidGlobal userIDGlobal = UidGlobal.getInstance();
                   // userIDGlobal.setData(String.valueOf(success));
                    Intent i = new Intent(LoginActivity.this, MainMenuActivity.class);
                   // i.putExtra("globalUserID", String.valueOf(userIDGlobal));
                    i.putExtra("loggedInUser", username);
                    startActivity(i);
                    finish();
                    return json.getString(TAG_MESSAGE);
                } else {
                    Timber.d(json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}