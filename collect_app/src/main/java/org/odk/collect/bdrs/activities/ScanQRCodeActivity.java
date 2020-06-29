package org.odk.collect.bdrs.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.application.Collect;
import org.odk.collect.bdrs.listeners.ActionListener;
import org.odk.collect.bdrs.listeners.PermissionListener;
import org.odk.collect.bdrs.preferences.AdminSharedPreferences;
import org.odk.collect.bdrs.preferences.GeneralSharedPreferences;
import org.odk.collect.bdrs.preferences.PreferenceSaver;
import org.odk.collect.bdrs.utilities.CompressionUtils;
import org.odk.collect.bdrs.utilities.LocaleHelper;
import org.odk.collect.bdrs.utilities.PermissionUtils;
import org.odk.collect.bdrs.utilities.ToastUtils;

import java.io.IOException;
import java.util.zip.DataFormatException;

import androidx.annotation.Nullable;
import timber.log.Timber;

import static org.odk.collect.bdrs.activities.ActivityUtils.startActivityAndCloseAllOthers;

// ScanQRCodeActivity initiates barcode scanning and process its results
// it does not have a UI
public class ScanQRCodeActivity extends CollectAbstractActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new PermissionUtils().requestCameraPermission(this, new PermissionListener() {
            @Override
            public void granted() {
                new IntentIntegrator(ScanQRCodeActivity.this)
                        .setCaptureActivity(ScannerWithFlashlightActivity.class)
                        .setBeepEnabled(true)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .setOrientationLocked(false)
                        .setPrompt(getString(R.string.qrcode_scanner_prompt))
                        .initiateScan();
            }

            @Override
            public void denied() {
                (ScanQRCodeActivity.this).finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // request was canceled...
                Timber.i("QR code scanning cancelled");
            } else {
                try {
                    applySettings(this, CompressionUtils.decompress(result.getContents()));
                } catch (IOException | DataFormatException | IllegalArgumentException e) {
                    Timber.e(e);
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
                }
            }
        }

        this.finish(); // catch anything, should always finish
    }

    public static void applySettings(Activity activity, String content) {
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(content, new ActionListener() {
            @Override
            public void onSuccess() {
                Collect.getInstance().initializeJavaRosa();
                ToastUtils.showLongToast(Collect.getInstance().getString(R.string.successfully_imported_settings));
                final LocaleHelper localeHelper = new LocaleHelper();
                localeHelper.updateLocale(activity);
                startActivityAndCloseAllOthers(activity, MainMenuActivity.class);
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof GeneralSharedPreferences.ValidationException) {
                    ToastUtils.showLongToast(Collect.getInstance().getString(R.string.invalid_qrcode));
                } else {
                    Timber.e(exception);
                }
            }
        });
    }

}
