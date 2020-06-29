package org.odk.collect.bdrs.support;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.bdrs.activities.MainMenuActivity;
import org.odk.collect.bdrs.support.pages.MainMenuPage;

public class FeatureTestRule extends ActivityTestRule<MainMenuActivity> {

    public FeatureTestRule() {
        super(MainMenuActivity.class);
    }

    public MainMenuPage mainMenu() {
        return new MainMenuPage(this).assertOnPage();
    }
}
