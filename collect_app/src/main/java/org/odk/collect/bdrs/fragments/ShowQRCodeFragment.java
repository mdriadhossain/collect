/* Copyright (C) 2017 Shobhit
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

package org.odk.collect.bdrs.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import org.odk.collect.bdrs.BuildConfig;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.CollectAbstractActivity;
import org.odk.collect.bdrs.activities.ScanQRCodeActivity;
import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.injection.DaggerUtils;
import org.odk.collect.bdrs.utilities.FileUtils;
import org.odk.collect.bdrs.utilities.QRCodeUtils;
import org.odk.collect.bdrs.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.bdrs.analytics.AnalyticsEvents.SCAN_QR_CODE;
import static org.odk.collect.bdrs.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.bdrs.preferences.GeneralKeys.KEY_PASSWORD;

public class ShowQRCodeFragment extends Fragment {

    private static final int SELECT_PHOTO = 111;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final boolean[] checkedItems = {true, true};

    @BindView(R.id.ivQRcode)
    ImageView ivQRCode;
    @BindView(R.id.circularProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.tvPasswordWarning)
    TextView tvPasswordWarning;

    private Intent shareIntent;
    private AlertDialog dialog;

    @Inject
    public Analytics analytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        ((CollectAbstractActivity) getActivity()).initToolbar(getString(R.string.configure_via_qr_code));
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        generateCode();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DaggerUtils.getComponent(activity).inject(this);
    }

    private void generateCode() {
        shareIntent = null;
        progressBar.setVisibility(VISIBLE);
        ivQRCode.setVisibility(GONE);
        addPasswordStatusString();

        Disposable disposable = QRCodeUtils.getQRCodeGeneratorObservable(getSelectedPasswordKeys())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    progressBar.setVisibility(GONE);
                    ivQRCode.setVisibility(VISIBLE);
                    ivQRCode.setImageBitmap(bitmap);
                }, Timber::e, this::updateShareIntent);
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void addPasswordStatusString() {
        String status;
        if (checkedItems[0] && checkedItems[1]) {
            status = getString(R.string.qrcode_with_both_passwords);
        } else if (checkedItems[0]) {
            status = getString(R.string.qrcode_with_admin_password);
        } else if (checkedItems[1]) {
            status = getString(R.string.qrcode_with_server_password);
        } else {
            status = getString(R.string.qrcode_without_passwords);
        }
        tvPasswordWarning.setText(status);
    }

    private void updateShareIntent() {
        // Initialize the intent to share QR Code
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri =
                FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(QRCodeUtils.getQrCodeFilepath()));
        FileUtils.grantFileReadPermissions(shareIntent, uri, getActivity());
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
    }

    @OnClick(R.id.btnScan)
    void scanButtonClicked() {
        analytics.logEvent(SCAN_QR_CODE, "Settings");
        Intent i = new Intent(getActivity(), ScanQRCodeActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.btnSelect)
    void chooseButtonClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        } else {
            ToastUtils.showShortToast(getString(R.string.activity_not_found, getString(R.string.choose_image)));
            Timber.w(getString(R.string.activity_not_found, getString(R.string.choose_image)));
        }
    }

    @OnClick(R.id.tvPasswordWarning)
    void passwordWarningClicked() {
        if (dialog == null) {
            final String[] items = {
                    getString(R.string.admin_password),
                    getString(R.string.server_password)};

            dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.include_password_dialog)
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                    .setCancelable(false)
                    .setPositiveButton(R.string.generate, (dialog, which) -> {
                        generateCode();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .create();
        }
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    boolean qrCodeFound = false;
                    final Uri imageUri = data.getData();
                    if (imageUri != null) {
                        final InputStream imageStream = getActivity().getContentResolver()
                                .openInputStream(imageUri);

                        final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        if (bitmap != null) {
                            String response = QRCodeUtils.decodeFromBitmap(bitmap);
                            if (response != null) {
                                qrCodeFound = true;
                                ScanQRCodeActivity.applySettings(getActivity(), response);
                            }
                        }
                    }
                    if (!qrCodeFound) {
                        ToastUtils.showLongToast(R.string.qr_code_not_found);
                    }
                } catch (FormatException | NotFoundException | ChecksumException e) {
                    Timber.i(e);
                    ToastUtils.showLongToast(R.string.qr_code_not_found);
                } catch (DataFormatException | IOException | OutOfMemoryError | IllegalArgumentException e) {
                    Timber.e(e);
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
                }
            } else {
                Timber.i("Choosing QR code from sdcard cancelled");
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                if (shareIntent != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qrcode)));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Collection<String> getSelectedPasswordKeys() {
        Collection<String> keys = new ArrayList<>();

        //adding the selected password keys
        if (checkedItems[0]) {
            keys.add(KEY_ADMIN_PW);
        }

        if (checkedItems[1]) {
            keys.add(KEY_PASSWORD);
        }
        return keys;
    }
}
