package org.odk.collect.bdrs.support;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.odk.collect.bdrs.activities.FormEntryActivity;
import org.odk.collect.bdrs.storage.StoragePathProvider;
import org.odk.collect.bdrs.storage.StorageSubdirectory;

import static org.odk.collect.bdrs.activities.FormEntryActivity.EXTRA_TESTING_PATH;

public class FormActivityTestRule extends IntentsTestRule<FormEntryActivity> {

    private final String formFilename;

    public FormActivityTestRule(String formFilename) {
        super(FormEntryActivity.class);
        this.formFilename = formFilename;
    }

    @Override
    protected Intent getActivityIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FormEntryActivity.class);
        intent.putExtra(EXTRA_TESTING_PATH, new StoragePathProvider().getDirPath(StorageSubdirectory.FORMS) + "/" + formFilename);

        return intent;
    }
}
