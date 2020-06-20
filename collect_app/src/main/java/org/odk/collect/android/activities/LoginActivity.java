package org.odk.collect.android.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.burgstaller.okhttp.digest.fromhttpclient.BasicNameValuePair;
import com.burgstaller.okhttp.digest.fromhttpclient.NameValuePair;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.UidGlobal;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class LoginActivity extends Activity implements View.OnClickListener {

    private EditText user;
    private EditText pass;
    private Button mSubmit;
    private Button  mRegister;
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Data Collection System");

        // setup input fields
        user = (EditText) findViewById(R.id.username);
        //user.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.edit), null);
        pass = (EditText) findViewById(R.id.password);

        // setup buttons
        mSubmit = (Button) findViewById(R.id.login);
        mRegister = (Button) findViewById(R.id.register);
        //mRegister.setVisibility(Button.INVISIBLE);

        // register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);
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

                //URL (Must be https://)
                String LOGIN_URL = "http://ecds.solversbd.com/Lib/AppAuthChecker.php";
                //String LOGIN_URL = "https://ecds.solversbd.com/Lib/AppAuthChecker.php";
                //String LOGIN_URL = "https://steptoonline.com/lib/AppAuthChecker.php";
                String REQUEST_METHOD = "POST";

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