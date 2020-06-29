package org.odk.collect.bdrs.formentry.saving;

import android.net.Uri;

import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.javarosawrapper.FormController;
import org.odk.collect.bdrs.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener, Analytics analytics);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
