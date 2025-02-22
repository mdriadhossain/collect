package org.odk.collect.bdrs.widgets.base;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.bdrs.application.Collect;
import org.odk.collect.bdrs.javarosawrapper.FormController;
import org.odk.collect.bdrs.support.RobolectricHelpers;
import org.odk.collect.bdrs.support.TestScreenContextActivity;
import org.odk.collect.bdrs.widgets.ItemsetWidgetTest;
import org.odk.collect.bdrs.widgets.interfaces.Widget;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class QuestionWidgetTest<W extends Widget, A extends IAnswerData>
        extends WidgetTest {

    protected Random random = new Random();
    protected Activity activity = RobolectricHelpers.buildThemedActivity(TestScreenContextActivity.class).get();

    private W widget;
    private W actualWidget;

    @Mock
    public FormIndex formIndex;

    @Mock
    public FormController formController;

    @NonNull
    public abstract W createWidget();

    @NonNull
    public abstract A getNextAnswer();

    public A getInitialAnswer() {
        return getNextAnswer();
    }

    /**
     * @return Real {@link Widget} object if present otherwise creates one
     * <p>
     * This should be used for mutating the {@link org.odk.collect.bdrs.widgets.QuestionWidget}
     */
    public W getActualWidget() {
        if (actualWidget == null) {
            actualWidget = createWidget();
        }

        return actualWidget;
    }

    /**
     * @return {@link org.mockito.Spy} of the {@link #actualWidget}
     * <p>
     * This should be unless we want to mutate {@link org.odk.collect.bdrs.widgets.QuestionWidget}
     * This is because a spy is not the real object and changing it won't have any effect on the real object
     */
    public W getWidget() {
        if (widget == null) {
            widget = spy(getActualWidget());
        }

        return widget;
    }

    public void resetWidget() {
        actualWidget = null;
        widget = null;
    }

    public void setUp() throws Exception {
        super.setUp();

        when(formEntryPrompt.getIndex()).thenReturn(formIndex);

        Collect.getInstance().setFormController(formController);

        widget = null;
    }

    @Test
    public void getAnswerShouldReturnNullIfPromptDoesNotHaveExistingAnswer() {
        W widget = getWidget();
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        A answer = getInitialAnswer();
        if (answer instanceof StringData && !(this instanceof ItemsetWidgetTest)) {
            when(formEntryPrompt.getAnswerText()).thenReturn((String) answer.getValue());
        } else {
            when(formEntryPrompt.getAnswerValue()).thenReturn(answer);
        }

        W widget = getWidget();
        IAnswerData newAnswer = widget.getAnswer();

        assertNotNull(newAnswer);
        assertEquals(newAnswer.getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void callingClearShouldRemoveTheExistingAnswer() {
        getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();
        widget.clearAnswer();

        assertNull(widget.getAnswer());
    }
}