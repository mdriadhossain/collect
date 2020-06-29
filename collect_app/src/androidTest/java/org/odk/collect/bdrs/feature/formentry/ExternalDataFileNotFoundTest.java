package org.odk.collect.bdrs.feature.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.bdrs.R;
import org.odk.collect.bdrs.activities.FormEntryActivity;
import org.odk.collect.bdrs.support.pages.FormEntryPage;
import org.odk.collect.bdrs.support.CopyFormRule;
import org.odk.collect.bdrs.support.ResetStateRule;
import org.odk.collect.bdrs.support.FormLoadingUtils;

public class ExternalDataFileNotFoundTest {
    private static final String EXTERNAL_DATA_QUESTIONS = "external_data_questions.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(EXTERNAL_DATA_QUESTIONS);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_DATA_QUESTIONS, true));

    @Test
    public void questionsThatUseExternalFiles_ShouldDisplayFriendlyMessageWhenFilesAreMissing() {
        new FormEntryPage("externalDataQuestions", activityTestRule)
                .assertText(activityTestRule.getActivity().getString(R.string.file_missing, "/storage/emulated/0/odk/forms/external_data_questions-media/fruits.csv"))
                .swipeToNextQuestion()
                .assertText(activityTestRule.getActivity().getString(R.string.file_missing, "/storage/emulated/0/odk/forms/external_data_questions-media/itemsets.csv"));
    }
}
