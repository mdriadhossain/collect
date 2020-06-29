package org.odk.collect.bdrs.support;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import org.odk.collect.bdrs.utilities.ScreenContext;

public class TestScreenContextActivity extends FragmentActivity implements ScreenContext {

    @Override
    public FragmentActivity getActivity() {
        return this;
    }

    @Override
    public LifecycleOwner getViewLifecycle() {
        return this;
    }
}
