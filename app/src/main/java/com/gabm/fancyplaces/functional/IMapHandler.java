package com.gabm.fancyplaces.functional;

import android.widget.ArrayAdapter;

import com.gabm.fancyplaces.data.FancyPlace;

/**
 * Created by gabm on 24/05/15.
 */
public interface IMapHandler {
    void setCamera(double lat, double lng, float zoom);

    void animateCamera(double lat, double lng, int duration);

    void animateCamera(double lat, double lng, float zoom, int duration);

    void clearMarkers();

    void addMarker(double lat, double lng, String text);

    void setCurrentLocationMarker(double lat, double lng, String title);

    void setAdapter(ArrayAdapter<FancyPlace> in_adapter);

}
