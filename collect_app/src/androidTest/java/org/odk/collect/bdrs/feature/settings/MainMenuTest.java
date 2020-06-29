package org.odk.collect.bdrs.feature.settings;

import android.Manifest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.MainMenuActivity;
import org.odk.collect.bdrs.support.ResetStateRule;
import org.odk.collect.bdrs.support.pages.AdminSettingsPage;
import org.odk.collect.bdrs.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class MainMenuTest {
    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule());

    @Test
    public void configureWithQRCanBeDisabled() {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnString(R.string.main_menu_settings)
                .clickOnString(R.string.qr_code)
                .pressBack(new AdminSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .checkIfTextDoesNotExist(R.string.configure_via_qr_code);
    }

}
