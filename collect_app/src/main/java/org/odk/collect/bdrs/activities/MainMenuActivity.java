/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.bdrs.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.burgstaller.okhttp.digest.fromhttpclient.BasicNameValuePair;
import com.burgstaller.okhttp.digest.fromhttpclient.NameValuePair;

import org.javarosa.core.io.BufferedInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.analytics.AnalyticsEvents;
import org.odk.collect.bdrs.application.Collect;
import org.odk.collect.bdrs.dao.InstancesDao;
import org.odk.collect.bdrs.injection.DaggerUtils;
import org.odk.collect.bdrs.preferences.AdminPasswordDialogFragment;
import org.odk.collect.bdrs.preferences.AdminPasswordDialogFragment.Action;
import org.odk.collect.bdrs.preferences.AdminPreferencesActivity;
import org.odk.collect.bdrs.preferences.AdminSharedPreferences;
import org.odk.collect.bdrs.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.bdrs.preferences.GeneralKeys;
import org.odk.collect.bdrs.preferences.GeneralSharedPreferences;
import org.odk.collect.bdrs.preferences.PreferenceSaver;
import org.odk.collect.bdrs.preferences.PreferencesActivity;
import org.odk.collect.bdrs.preferences.Transport;
import org.odk.collect.bdrs.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.bdrs.storage.StorageInitializer;
import org.odk.collect.bdrs.storage.StoragePathProvider;
import org.odk.collect.bdrs.storage.StorageStateProvider;
import org.odk.collect.bdrs.storage.migration.StorageMigrationDialog;
import org.odk.collect.bdrs.storage.migration.StorageMigrationRepository;
import org.odk.collect.bdrs.storage.migration.StorageMigrationResult;
import org.odk.collect.bdrs.utilities.AdminPasswordProvider;
import org.odk.collect.bdrs.utilities.ApplicationConstants;
import org.odk.collect.bdrs.utilities.MultiClickGuard;
import org.odk.collect.bdrs.utilities.DialogUtils;
import org.odk.collect.bdrs.utilities.PlayServicesUtil;
import org.odk.collect.bdrs.utilities.SharedPreferencesUtils;
import org.odk.collect.bdrs.utilities.ToastUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.odk.collect.bdrs.preferences.GeneralKeys.KEY_SUBMISSION_TRANSPORT_TYPE;
import static org.odk.collect.bdrs.utilities.DialogUtils.getDialog;
import static org.odk.collect.bdrs.utilities.DialogUtils.showIfNotShowing;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends CollectAbstractActivity implements AdminPasswordDialogFragment.AdminPasswordDialogCallback {
    private static final boolean EXIT = true;
    // buttons
    private Button manageFilesButton;
    private Button sendDataButton;
    private Button viewSentFormsButton;
    private Button reviewDataButton;
    private Button getFormsButton;

    private AlertDialog alertDialog;
    private MenuItem qrcodeScannerMenuItem;
    private int completedCount;
    private int savedCount;
    private int viewSentCount;
    private Cursor finalizedCursor;
    private Cursor savedCursor;
    private Cursor viewSentCursor;
    private final IncomingHandler handler = new IncomingHandler(this);
    private final MyContentObserver contentObserver = new MyContentObserver();

    //String globalUserID = "";

    @Inject
    public Analytics analytics;

    @BindView(R.id.storageMigrationBanner)
    LinearLayout storageMigrationBanner;

    @BindView(R.id.storageMigrationBannerText)
    TextView storageMigrationBannerText;

    @BindView(R.id.storageMigrationBannerDismissButton)
    Button storageMigrationBannerDismissButton;

    @BindView(R.id.storageMigrationBannerLearnMoreButton)
    Button storageMigrationBannerLearnMoreButton;

    @BindView(R.id.version_sha)
    TextView versionSHAView;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    StorageStateProvider storageStateProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    private MainMenuViewModel viewModel;

    // Progress Dialog
    private ProgressDialog pDialog;

    String loggedInUserName;
    String loggedInUserID;

    // JSON parser class
    JSONParserTotalUnreadNotification jsonParser = new JSONParserTotalUnreadNotification();

    // JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    //TextView txtTotalUnreadNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect.getInstance().getComponent().inject(this);
        setContentView(R.layout.main_menu);
        setTitle(R.string.app_name);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, new MainMenuViewModel.Factory()).get(MainMenuViewModel.class);

        //App Version
        String appVersionCode = getResources().getString(R.string.app_version);
        TextView txtVersionCode = findViewById(R.id.app_version);
        txtVersionCode.setText("Application Version: " + appVersionCode);

        //LoggedInUser
        TextView txtLoggedInUserName = findViewById(R.id.logged_in_username);
        Intent i = getIntent();
        Bundle b = i.getExtras();

        if (b != null) {
            loggedInUserID = (String) b.get("loggedInUserID");
            loggedInUserName = (String) b.get("loggedInUser");
            txtLoggedInUserName.setText("You are logged in as: " + loggedInUserName);
        }

        new GetCountUnreadNotification().execute();

        initToolbar();
        DaggerUtils.getComponent(this).inject(this);

        disableSmsIfNeeded();

        storageMigrationRepository.getResult().observe(this, this::onStorageMigrationFinish);

        //Logout Button
        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //SharedPreferences myPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                //SharedPreferences.Editor editor = myPrefs.edit();
                //editor.clear();
                //editor.apply();
                Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();  // This call is missing.
            }
        });

        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        //enterDataButton.setText(getString(R.string.enter_data_button));
        enterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            FormChooserListActivity.class);
                    startActivity(i);
                }
            }
        });

        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        //reviewDataButton.setText(getString(R.string.review_data_button));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.EDIT_SAVED);
                    startActivity(i);
                }
            }
        });

        // send data button. expects a result.
        sendDataButton = findViewById(R.id.send_data);
        //sendDataButton.setText(getString(R.string.send_data_button));
        sendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            InstanceUploaderListActivity.class);
                    startActivity(i);
                }
            }
        });

        //View sent forms
        viewSentFormsButton = findViewById(R.id.view_sent_forms);
        viewSentFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.VIEW_SENT);
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
        getFormsButton = findViewById(R.id.get_forms);
        // getFormsButton.setText(getString(R.string.get_forms));
        getFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(MainMenuActivity.this);
                    String protocol = sharedPreferences.getString(
                            GeneralKeys.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
                    Intent i = null;
                    if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
                        if (PlayServicesUtil.isGooglePlayServicesAvailable(MainMenuActivity.this)) {
                            i = new Intent(getApplicationContext(),
                                    GoogleDriveActivity.class);
                        } else {
                            PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
                            return;
                        }
                    } else {
                        i = new Intent(getApplicationContext(), FormDownloadListActivity.class);
                    }
                    // i.putExtra("UserGlobalID", globalUserID);
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
        manageFilesButton = findViewById(R.id.manage_forms);
        // manageFilesButton.setText(getString(R.string.manage_files));
        manageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            FileManagerTabs.class);
                    startActivity(i);
                }
            }
        });

        ConnectivityManager connec = (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Notification button. no result expected.
        Button getNotificationButton = findViewById(R.id.get_notification);
        getNotificationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                        connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

                    Intent i = new Intent(getApplicationContext(), GetNotification.class);
                    startActivity(i);
                } else {
                    Toast.makeText(MainMenuActivity.this, "Internet connection is not available!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Show data on server button. no result expected.
        Button getShowDataServer = findViewById(R.id.get_showDataServer);
        getShowDataServer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                        connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

                    Intent i = new Intent(getApplicationContext(), ShowInServerActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(MainMenuActivity.this, "Internet connection is not available!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Dashboard button. no result expected.
        Button getDashboard = findViewById(R.id.get_dashboard);
        getDashboard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                        connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

                    Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(MainMenuActivity.this, "Internet connection is not available!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //String versionSHA = viewModel.getVersionCommitDescription();
        String versionSHA = null;
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }

        // must be at the beginning of any activity that can be called from an
        // external intent
        Timber.i("Starting up, creating directories");
        try {
            new StorageInitializer().createOdkDirsOnStorage();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        File f = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings");
        File j = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings.json");
        // Give JSON file preference
        if (j.exists()) {
            boolean success = SharedPreferencesUtils.loadSharedPreferencesFromJSONFile(j);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                j.delete();
                recreate();

                // Delete settings file to prevent overwrite of settings from JSON file on next startup
                if (f.exists()) {
                    f.delete();
                }
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        } else if (f.exists()) {
            boolean success = loadSharedPreferencesFromFile(f);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                f.delete();
                recreate();
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        }
    }

    class GetCountUnreadNotification extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainMenuActivity.this);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success = 0;
            try {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
                String mainServerURL = settings.getString(GeneralKeys.KEY_SERVER_URL, getApplication().getString(R.string.default_server_url));
                while (mainServerURL.endsWith("/")) {
                    mainServerURL = mainServerURL.substring(0, mainServerURL.length() - 1);
                }
                String getNoticeURL = getResources().getString(R.string.default_odk_get_total_unread_notification);

                //String GET_NOTICE_URL = mainServerURL + getNoticeURL + "?userid=" + loggedInUserID;
                //String GET_NOTICE_URL = mainServerURL + getNoticeURL + "?username=" + loggedInUserName;
                String GET_NOTICE_URL = "https://steptoonline.com/lib/CountUnreadNotice.php?userid=" + loggedInUserID;
                //String GET_NOTICE_URL = "https://steptoonline.com/lib/CountUnreadNotice.php?username=" + loggedInUserName;
                String REQUEST_METHOD = "POST";

                Timber.d(GET_NOTICE_URL);
                Timber.d(REQUEST_METHOD);
                Timber.tag("UserName: ").d(loggedInUserName);
                Timber.d("starting");

                JSONObject json = jsonParser.makeOkHttpRequest(GET_NOTICE_URL, REQUEST_METHOD, loggedInUserName);

                // check your log for json response
                Timber.d(json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success > 0) {
                    Timber.tag("TotalNotice: ").d(String.valueOf(success));
                    return json.getString(TAG_MESSAGE);
                } else {
                    Timber.d(json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String totalUnreadNotificationCount = String.valueOf(success);
            return totalUnreadNotificationCount;
        }

        protected void onPostExecute(String noticeTotalCount) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            TextView txtTotalUnreadNotification = findViewById(R.id.badge_notification);
            //txtTotalUnreadNotification.setText("" + success);
            txtTotalUnreadNotification.setText(noticeTotalCount);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        countSavedForms();
        updateButtons();
        if (!storageMigrationRepository.isMigrationBeingPerformed()) {
            getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, contentObserver);
        }

        setButtonsVisibility();
        invalidateOptionsMenu();
        setUpStorageMigrationBanner();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // this is your backendcall
        finish();
        overridePendingTransition( 0, 0);
        startActivity(getIntent());
        overridePendingTransition( 0, 0);
    }

    private void setButtonsVisibility() {
        reviewDataButton.setVisibility(viewModel.shouldEditSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        sendDataButton.setVisibility(viewModel.shouldSendFinalizedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        viewSentFormsButton.setVisibility(viewModel.shouldViewSentFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        getFormsButton.setVisibility(viewModel.shouldGetBlankFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        manageFilesButton.setVisibility(viewModel.shouldDeleteSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void onDestroy() {
        storageMigrationRepository.clearResult();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        qrcodeScannerMenuItem = menu.findItem(R.id.menu_configure_qr_code);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //qrcodeScannerMenuItem.setVisible(this.getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).getBoolean(AdminKeys.KEY_QR_CODE_SCANNER, true));
        qrcodeScannerMenuItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_configure_qr_code:
                analytics.logEvent(AnalyticsEvents.SCAN_QR_CODE, "MainMenu");

                if (adminPasswordProvider.isAdminPasswordSet()) {
                    Bundle args = new Bundle();
                    args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.SCAN_QR_CODE);
                    showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
                } else {
                    startActivity(new Intent(this, ScanQRCodeActivity.class));
                }
                return true;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menu_general_preferences:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            case R.id.menu_admin_preferences:
                if (adminPasswordProvider.isAdminPasswordSet()) {
                    Bundle args = new Bundle();
                    args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.ADMIN_SETTINGS);
                    showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
                } else {
                    startActivity(new Intent(this, AdminPreferencesActivity.class));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setTitle(String.format("%s %s", getString(R.string.app_name), viewModel.getVersion()));
        //setTitle(String.format("%s %s", getString(R.string.app_name), ""));
        setTitle(R.string.app_name);

        setSupportActionBar(toolbar);
    }

    private void countSavedForms() {
        InstancesDao instancesDao = new InstancesDao();

        // count for finalized instances
        try {
            finalizedCursor = instancesDao.getFinalizedInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (finalizedCursor != null) {
            startManagingCursor(finalizedCursor);
        }
        completedCount = finalizedCursor != null ? finalizedCursor.getCount() : 0;

        // count for saved instances
        try {
            savedCursor = instancesDao.getUnsentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (savedCursor != null) {
            startManagingCursor(savedCursor);
        }
        savedCount = savedCursor != null ? savedCursor.getCount() : 0;

        //count for view sent form
        try {
            viewSentCursor = instancesDao.getSentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }
        if (viewSentCursor != null) {
            startManagingCursor(viewSentCursor);
        }
        viewSentCount = viewSentCursor != null ? viewSentCursor.getCount() : 0;
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    @SuppressLint("StringFormatInvalid")
    private void updateButtons() {
        if (finalizedCursor != null && !finalizedCursor.isClosed()) {
            finalizedCursor.requery();
            completedCount = finalizedCursor.getCount();
            if (completedCount > 0) {
                sendDataButton.setText(
                        getString(R.string.send_data_button, String.valueOf(completedCount)));
            } else {
                sendDataButton.setText(getString(R.string.send_data));
            }
        } else {
            sendDataButton.setText(getString(R.string.send_data));
            Timber.w("Cannot update \"Send Finalized\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (savedCursor != null && !savedCursor.isClosed()) {
            savedCursor.requery();
            savedCount = savedCursor.getCount();
            if (savedCount > 0) {
                reviewDataButton.setText(getString(R.string.review_data_button,
                        String.valueOf(savedCount)));
            } else {
                reviewDataButton.setText(getString(R.string.review_data));
            }
        } else {
            reviewDataButton.setText(getString(R.string.review_data));
            Timber.w("Cannot update \"Edit Form\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (viewSentCursor != null && !viewSentCursor.isClosed()) {
            viewSentCursor.requery();
            viewSentCount = viewSentCursor.getCount();
            if (viewSentCount > 0) {
                viewSentFormsButton.setText(
                        getString(R.string.view_sent_forms_button, String.valueOf(viewSentCount)));
            } else {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            }
        } else {
            viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            Timber.w("Cannot update \"View Sent\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }
    }

    private boolean loadSharedPreferencesFromFile(File src) {
        // this should probably be in a thread if it ever gets big
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));

            // first object is preferences
            Map<String, Object> entries = (Map<String, Object>) input.readObject();

            AutoSendPreferenceMigrator.migrate(entries);
            PreferenceSaver.saveGeneralPrefs(GeneralSharedPreferences.getInstance(), entries);

            // second object is admin options
            Map<String, Object> adminEntries = (Map<String, Object>) input.readObject();
            PreferenceSaver.saveAdminPrefs(AdminSharedPreferences.getInstance(), adminEntries);

            Collect.getInstance().initializeJavaRosa();
            res = true;
        } catch (IOException | ClassNotFoundException e) {
            Timber.e(e, "Exception while loading preferences from file due to : %s ", e.getMessage());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }
        return res;
    }

    @Override
    public void onCorrectAdminPassword(Action action) {
        switch (action) {
            case ADMIN_SETTINGS:
                startActivity(new Intent(this, AdminPreferencesActivity.class));
                break;
            case STORAGE_MIGRATION:
                StorageMigrationDialog dialog = showStorageMigrationDialog();
                if (dialog != null) {
                    dialog.startStorageMigration();
                }

                break;
            case SCAN_QR_CODE:
                startActivity(new Intent(this, ScanQRCodeActivity.class));
                break;
        }
    }

    @Override
    public void onIncorrectAdminPassword() {
        ToastUtils.showShortToast(R.string.admin_password_incorrect);
    }

    /*
     * Used to prevent memory leaks
     */
    static class IncomingHandler extends Handler {
        private final WeakReference<MainMenuActivity> target;

        IncomingHandler(MainMenuActivity target) {
            this.target = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMenuActivity target = this.target.get();
            if (target != null) {
                target.updateButtons();
            }
        }
    }

    /**
     * notifies us that something changed
     */
    private class MyContentObserver extends ContentObserver {

        MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(0);
        }
    }

    private void disableSmsIfNeeded() {
        if (Transport.Internet != Transport.fromPreference(GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE))) {
            GeneralSharedPreferences.getInstance().save(KEY_SUBMISSION_TRANSPORT_TYPE, getString(R.string.transport_type_value_internet));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(R.string.sms_feature_disabled_dialog_title)
                    .setMessage(R.string.sms_feature_disabled_dialog_message)
                    .setPositiveButton(R.string.read_details, (dialog, which) -> {
                        Intent intent = new Intent(this, WebViewActivity.class);
                        intent.putExtra("url", "https://forum.opendatakit.org/t/17973");
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss());

            builder
                    .create()
                    .show();
        }
    }

    public void onStorageMigrationBannerDismiss(View view) {
        storageMigrationBanner.setVisibility(View.GONE);
        storageMigrationRepository.clearResult();
    }

    public void onStorageMigrationBannerLearnMoreClick(View view) {
        showStorageMigrationDialog();
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    private void onStorageMigrationFinish(StorageMigrationResult result) {
        if (result == StorageMigrationResult.SUCCESS) {
            DialogUtils.dismissDialog(StorageMigrationDialog.class, getSupportFragmentManager());
            displayBannerWithSuccessStorageMigrationResult();
        } else {
            StorageMigrationDialog dialog = showStorageMigrationDialog();

            if (dialog != null) {
                dialog.handleMigrationError(result);
            }
        }
    }

    @Nullable
    private StorageMigrationDialog showStorageMigrationDialog() {
        Bundle args = new Bundle();
        args.putInt(StorageMigrationDialog.ARG_UNSENT_INSTANCES, savedCount);

        showIfNotShowing(StorageMigrationDialog.class, args, getSupportFragmentManager());
        return getDialog(StorageMigrationDialog.class, getSupportFragmentManager());
    }

    private void setUpStorageMigrationBanner() {
        if (!storageStateProvider.isScopedStorageUsed()) {
            displayStorageMigrationBanner();
        }
    }

    private void displayStorageMigrationBanner() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        //storageMigrationBannerText.setText(R.string.scoped_storage_banner_text);
        storageMigrationBannerLearnMoreButton.setVisibility(View.VISIBLE);
        storageMigrationBannerDismissButton.setVisibility(View.GONE);
    }

    private void displayBannerWithSuccessStorageMigrationResult() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBannerText.setText(R.string.storage_migration_completed);
        storageMigrationBannerLearnMoreButton.setVisibility(View.GONE);
        storageMigrationBannerDismissButton.setVisibility(View.VISIBLE);
    }
}
