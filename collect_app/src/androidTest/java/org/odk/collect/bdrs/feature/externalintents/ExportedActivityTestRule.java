package org.odk.collect.bdrs.feature.externalintents;

import android.app.Activity;
import androidx.test.rule.ActivityTestRule;

import static org.odk.collect.bdrs.feature.externalintents.ExportedActivitiesUtils.clearDirectories;

class ExportedActivityTestRule<A extends Activity> extends ActivityTestRule<A> {

    ExportedActivityTestRule(Class<A> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        clearDirectories();
    }

}
