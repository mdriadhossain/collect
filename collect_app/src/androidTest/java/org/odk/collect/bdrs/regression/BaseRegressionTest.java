package org.odk.collect.bdrs.regression;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.odk.collect.bdrs.activities.MainMenuActivity;

public class BaseRegressionTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);
}