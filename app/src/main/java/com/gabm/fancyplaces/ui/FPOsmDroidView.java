package com.gabm.fancyplaces.ui;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.functional.LocationHandler;
import com.gabm.fancyplaces.functional.OnFancyPlaceSelectedListener;
import com.gabm.fancyplaces.functional.OsmMapHandler;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;

/**
 * Created by gabm on 23/05/15.
 */
public class FPOsmDroidView extends TabItem implements LocationHandler.OnLocationUpdatedListener {

    private final static int LOCATION_UPDATED_INIT = 0;
    private final static int LOCATION_UPDATED_GPS = 1;
    private ResourceProxy mResourceProxy = null;
    private MapView mMapView = null;
    private OsmMapHandler mapHandler = null;
    private LocationHandler locationHandler = null;
    private OnFancyPlaceSelectedListener fancyPlaceSelectedCallback = null;
    private MainWindow parent = null;

    public static FPOsmDroidView newInstance() {
        FPOsmDroidView result = new FPOsmDroidView();

        return result;
    }

    @Override
    public String getTitle() {
        return "OSM View";
    }

    @Override
    public void onLocationUpdated(Location location) {
        onLocationUpdated(location, LOCATION_UPDATED_GPS);
    }

    public void onLocationUpdated(Location location, int reason) {
        if (location != null) {
            switch (reason) {
                case LOCATION_UPDATED_INIT:
                    mapHandler.setCamera(location.getLatitude(), location.getLongitude(), com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_ZOOM_FAR);
                    break;
                case LOCATION_UPDATED_GPS:
                    break;
            }
            mapHandler.setCurrentLocationMarker(location.getLatitude(), location.getLongitude(), getString(R.string.your_location));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());

        mMapView = new MapView(inflater.getContext(), 128, mResourceProxy);
        mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mMapView.setMultiTouchControls(true);
        mMapView.setTilesScaledToDpi(true);

        mapHandler = new OsmMapHandler(mMapView);
        mapHandler.setAdapter(parent.fancyPlaceArrayAdapter);

        locationHandler = new LocationHandler(getActivity());
        onLocationUpdated(locationHandler.getCurLocation(), LOCATION_UPDATED_INIT);
        requestLocationUpdate();

        return mMapView;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            fancyPlaceSelectedCallback = (MainWindow) activity;
            parent = (MainWindow) activity;
        } catch (Exception e) {

        }
    }
}
