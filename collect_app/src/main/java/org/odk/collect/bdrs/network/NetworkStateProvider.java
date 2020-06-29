package org.odk.collect.bdrs.network;

import android.net.NetworkInfo;

public interface NetworkStateProvider {
    boolean isDeviceOnline();

    NetworkInfo getNetworkInfo();
}
