package org.odk.collect.bdrs.widgets;

import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.odk.collect.bdrs.formentry.questions.QuestionDetails;
import org.odk.collect.bdrs.widgets.base.GeneralSelectOneWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ListWidgetTest extends GeneralSelectOneWidgetTest<ListWidget> {
    @NonNull
    @Override
    public ListWidget createWidget() {
        return new ListWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false, false);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (RadioButton radioButton : getWidget().buttons) {
            assertThat(radioButton.getVisibility(), is(View.VISIBLE));
            assertThat(radioButton.isEnabled(), is(Boolean.FALSE));
        }
    }
}
