package org.odk.collect.bdrs.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.fragments.dialogs.ProgressDialogFragment;
import org.odk.collect.bdrs.injection.DaggerUtils;

import javax.inject.Inject;

import static org.odk.collect.bdrs.formentry.saving.FormSaveViewModel.SaveResult.State.SAVING;

public class SaveFormProgressDialogFragment extends ProgressDialogFragment {

    @Inject
    Analytics analytics;

    private FormSaveViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        viewModel = new ViewModelProvider(requireActivity(), new FormSaveViewModel.Factory(analytics))
                .get(FormSaveViewModel.class);

        setCancelable(false);
        setTitle(getString(R.string.saving_form));

        viewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.getState() == SAVING && result.getMessage() != null) {
                setMessage(getString(R.string.please_wait) + "\n\n" + result.getMessage());
            } else {
                setMessage(getString(R.string.please_wait));
            }
        });
    }

    @Override
    protected Cancellable getCancellable() {
        return viewModel;
    }
}
