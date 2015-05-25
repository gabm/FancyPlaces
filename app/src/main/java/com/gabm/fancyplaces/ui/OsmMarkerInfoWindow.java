package com.gabm.fancyplaces.ui;

import android.widget.TextView;

import com.gabm.fancyplaces.R;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

/**
 * Created by gabm on 25/05/15.
 */
public class OsmMarkerInfoWindow extends InfoWindow {

    public OsmMarkerInfoWindow(MapView mapView) {
        super(R.layout.marker_fancy_place, mapView);
    }

    public void setTitle(String title) {
        ((TextView) getView().findViewById(R.id.info_window_title)).setText(title);
    }

    @Override
    public void onOpen(Object o) {

    }

    @Override
    public void onClose() {

    }
}
