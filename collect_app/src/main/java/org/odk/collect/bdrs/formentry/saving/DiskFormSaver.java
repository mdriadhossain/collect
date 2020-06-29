package org.odk.collect.bdrs.formentry.saving;

import android.net.Uri;

import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.javarosawrapper.FormController;
import org.odk.collect.bdrs.tasks.SaveFormToDisk;
import org.odk.collect.bdrs.tasks.SaveToDiskResult;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener, Analytics analytics)
    {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, exitAfter, shouldFinalize, updatedSaveName, instanceContentURI, analytics);
        return saveFormToDisk.saveForm(progressListener);
    }
}
