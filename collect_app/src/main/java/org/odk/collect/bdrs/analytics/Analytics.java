package org.odk.collect.bdrs.analytics;

public interface Analytics {

    void logEvent(String category, String action);

    void logEvent(String category, String action, String label);

    void setAnalyticsCollectionEnabled(boolean isAnalyticsEnabled);
}