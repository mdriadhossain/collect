package org.odk.collect.bdrs.formentry.media;

import android.content.Context;

import org.odk.collect.bdrs.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
