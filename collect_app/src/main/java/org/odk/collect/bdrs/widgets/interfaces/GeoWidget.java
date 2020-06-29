package org.odk.collect.bdrs.widgets.interfaces;

public interface GeoWidget extends BinaryWidget {

    void startGeoActivity();

    void updateButtonLabelsAndVisibility(boolean dataAvailable);

    String getAnswerToDisplay(String answer);

    String getDefaultButtonLabel();
}
