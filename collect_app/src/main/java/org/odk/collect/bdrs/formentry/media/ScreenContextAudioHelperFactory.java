package org.odk.collect.bdrs.formentry.media;

import android.content.Context;

import org.odk.collect.bdrs.audio.AudioHelper;
import org.odk.collect.bdrs.utilities.ScreenContext;

public class ScreenContextAudioHelperFactory implements AudioHelperFactory {

    public AudioHelper create(Context context) {
        ScreenContext screenContext = (ScreenContext) context;
        return new AudioHelper(screenContext.getActivity(), screenContext.getViewLifecycle());
    }
}
