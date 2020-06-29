package org.odk.collect.bdrs.audio;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.bdrs.R;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.bdrs.support.RobolectricHelpers.createThemedActivity;
import static org.odk.collect.bdrs.support.RobolectricHelpers.getCreatedFromResId;

@RunWith(RobolectricTestRunner.class)
public class AudioButtonTest {

    private AudioButton button;

    @Before
    public void setup() {
        Activity activity = createThemedActivity(FragmentActivity.class, com.google.android.material.R.style.Theme_MaterialComponents);
        button = new AudioButton(activity);
    }

    @Test
    public void isPlayingReturnsFalse_andShowsPlayIcon() {
        assertThat(button.isPlaying(), equalTo(false));
        assertThat(getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_volume_up_black_24dp));
    }

    @Test
    public void whenPlayingIsTrue_showsPlayingIcon() {
        button.setPlaying(true);
        assertThat(getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_stop_black_24dp));
    }

    @Test
    public void whenPlayingIsFalse_showsPlayIcon() {
        button.setPlaying(false);
        assertThat(getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_volume_up_black_24dp));
    }
}