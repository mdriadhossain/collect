package org.odk.collect.bdrs.widgets.interfaces;

import org.javarosa.core.model.data.IAnswerData;

/**
 * @author James Knight
 */
public interface Widget {

    IAnswerData getAnswer();

    void clearAnswer();

    void waitForData();

    void cancelWaitingForData();

    boolean isWaitingForData();
}
