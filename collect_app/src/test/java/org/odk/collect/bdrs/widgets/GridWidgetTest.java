package org.odk.collect.bdrs.widgets;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.bdrs.analytics.Analytics;
import org.odk.collect.bdrs.audio.AudioHelper;
import org.odk.collect.bdrs.audio.Clip;
import org.odk.collect.bdrs.formentry.media.AudioHelperFactory;
import org.odk.collect.bdrs.formentry.questions.QuestionDetails;
import org.odk.collect.bdrs.injection.config.AppDependencyModule;
import org.odk.collect.bdrs.preferences.GeneralSharedPreferences;
import org.odk.collect.bdrs.support.MockFormEntryPromptBuilder;
import org.odk.collect.bdrs.support.RobolectricHelpers;
import org.odk.collect.bdrs.widgets.base.GeneralSelectOneWidgetTest;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.bdrs.support.CollectHelpers.setupFakeReferenceManager;
import static org.odk.collect.bdrs.utilities.WidgetAppearanceUtils.NO_BUTTONS;

/**
 * @author James Knight
 */

public class GridWidgetTest extends GeneralSelectOneWidgetTest<GridWidget> {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private AudioHelper audioHelper;

    @Mock
    private Analytics analytics;

    @Before
    public void overrideDependencyModule() throws Exception {
        ReferenceManager referenceManager = setupFakeReferenceManager(REFERENCES);
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public AudioHelperFactory providesAudioHelperFactory() {
                return context -> audioHelper;
            }

            @Override
            public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
                return analytics;
            }
        });
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode_clickingChoice_playsAndStopsAudio() throws Exception {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAppearance(NO_BUTTONS)
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        GridWidget widget = getActualWidget();

        widget.onItemClick(0);
        verify(audioHelper).play(new Clip("i am index 0", REFERENCES.get(0).second));

        widget.onItemClick(0);
        verify(audioHelper).stop();
    }

    @Test
    public void whenChoicesHaveAudio_andNoButtonsMode_logsAudioChoiceGridEvent() throws Exception {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withAppearance(NO_BUTTONS)
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        getActualWidget();
        verify(analytics).logEvent("Prompt", "AudioChoiceGrid", "formAnalyticsID");
    }

    @Test
    public void whenChoicesHaveAudio_clickingChoice_doesNotPlayAudio() throws Exception {
        formEntryPrompt = new MockFormEntryPromptBuilder()
                .withIndex("i am index")
                .withSelectChoices(asList(
                        new SelectChoice("1", "1"),
                        new SelectChoice("2", "2")
                ))
                .withSpecialFormSelectChoiceText(asList(
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(0).first),
                        new Pair<>(FormEntryCaption.TEXT_FORM_AUDIO, REFERENCES.get(1).first)
                ))
                .build();

        GridWidget widget = getActualWidget();
        widget.onItemClick(0);

        verify(audioHelper, never()).play(any());
    }

    @NonNull
    @Override
    public GridWidget createWidget() {
        return new GridWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"), false);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (View view : getWidget().itemViews) {
            assertThat(view.getVisibility(), is(View.VISIBLE));
            assertThat(view.isEnabled(), is(Boolean.FALSE));
        }
    }

    private static final List<Pair<String, String>> REFERENCES = asList(
            new Pair<>("ref", "file://audio.mp3"),
            new Pair<>("ref1", "file://audio1.mp3")
    );
}