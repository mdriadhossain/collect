package org.odk.collect.bdrs.feature.formentry.backgroundlocation;

import android.Manifest;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.FormEntryActivity;
import org.odk.collect.bdrs.support.CopyFormRule;
import org.odk.collect.bdrs.support.ResetStateRule;
import org.odk.collect.bdrs.support.FormLoadingUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class LocationAuditTest {
    private static final String LOCATION_AUDIT_FORM = "location-audit.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(LOCATION_AUDIT_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(LOCATION_AUDIT_FORM, true));

    @Test
    public void locationCollectionSnackbar_ShouldBeDisplayedAtFormLaunch() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(String.format(ApplicationProvider.getApplicationContext().getString(R.string.background_location_enabled), "⋮"))));
    }

    @Test
    public void locationCollectionToggle_ShouldBeAvailable() {
        openActionBarOverflowOrOptionsMenu(activityTestRule.getActivity());
        onView(withText(R.string.track_location)).check(matches(isDisplayed()));
    }
}
