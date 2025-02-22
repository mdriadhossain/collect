package org.odk.collect.bdrs.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.CollectAbstractActivity;

public class FormMetadataPreferencesActivity extends CollectAbstractActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_layout);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_fragment_container, new FormMetadataFragment())
                .commit();
    }
}
