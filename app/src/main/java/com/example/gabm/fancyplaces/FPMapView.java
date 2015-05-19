package com.example.gabm.fancyplaces;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by gabm on 15/05/15.
 */
public class FPMapView extends TabItem implements MapFragmentHandler.OnFPClickListener, LocationHandler.OnLocationUpdatedListener {

    private final static int LOCATION_UPDATED_INIT = 0;
    private final static int LOCATION_UPDATED_GPS = 1;
    private static MyFancyPlacesApplication curAppContext = null;
    private OnFancyPlaceSelectedListener fancyPlaceSelectedCallback = null;
    private MapFragmentHandler mapHandler = null;
    private MainWindow parent = null;
    private LocationHandler locationHandler = null;

    public static FPMapView newInstance() {
        FPMapView result = new FPMapView();
        return result;
    }

    @Override
    public void onFPClicked(int id) {
        fancyPlaceSelectedCallback.onFancyPlaceSelected(id, OnFancyPlaceSelectedListener.INTENT_VIEW);
    }

    @Override
    public void onLocationUpdated(Location location) {
        onLocationUpdated(location, LOCATION_UPDATED_GPS);
    }

    public void onLocationUpdated(Location location, int reason) {
        if (location != null) {
            switch (reason) {
                case LOCATION_UPDATED_INIT:
                    mapHandler.setCamera(location.getLatitude(), location.getLongitude(), MyFancyPlacesApplication.MAP_DEFAULT_ZOOM_FAR);
                    break;
                case LOCATION_UPDATED_GPS:
                    break;
            }
            mapHandler.setCurrentLocationMarker(location.getLatitude(), location.getLongitude(), getString(R.string.your_location));
        }
    }

    private void requestLocationUpdate() {
        if (locationHandler.requireNewLocationUpdate()) {
            locationHandler.updateLocation();
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), R.string.updating_location, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fancy_places_map_view, container, false);

        curAppContext = (MyFancyPlacesApplication) getActivity().getApplicationContext();


        WorkaroundMapFragment map = (WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.fp_map);
        mapHandler = new MapFragmentHandler(map.getMap());
        mapHandler.setOnFPClickedListener(this);
        mapHandler.setAdapter(parent.fancyPlaceArrayAdapter);

        locationHandler = new LocationHandler(getActivity());
        onLocationUpdated(locationHandler.getCurLocation(), LOCATION_UPDATED_INIT);
        requestLocationUpdate();

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            fancyPlaceSelectedCallback = (MainWindow) activity;
            parent = (MainWindow) activity;
        } catch (Exception e) {

        }
    }

    @Override
    public String getTitle() {
        return "Map";
    }


}