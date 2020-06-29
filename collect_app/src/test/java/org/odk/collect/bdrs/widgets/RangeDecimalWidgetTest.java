package org.odk.collect.bdrs.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.DecimalData;
import org.odk.collect.bdrs.formentry.questions.QuestionDetails;
import org.odk.collect.bdrs.widgets.base.RangeWidgetTest;

/**
 * @author James Knight
 */

public class RangeDecimalWidgetTest extends RangeWidgetTest<RangeDecimalWidget, DecimalData> {

    @NonNull
    @Override
    public RangeDecimalWidget createWidget() {
        return new RangeDecimalWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public DecimalData getNextAnswer() {
        return new DecimalData(random.nextDouble());
    }
}
