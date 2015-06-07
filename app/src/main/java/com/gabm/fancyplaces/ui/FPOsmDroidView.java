/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.ui;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gabm.fancyplaces.FancyPlacesApplication;
import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.functional.LocationHandler;
import com.gabm.fancyplaces.functional.OnFancyPlaceSelectedListener;
import com.gabm.fancyplaces.functional.OsmMapHandler;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.ResourceProxyImpl;

/**
 * Created by gabm on 23/05/15.
 */
public class FPOsmDroidView extends TabItem implements LocationHandler.OnLocationUpdatedListener {

    private ResourceProxy mResourceProxy = null;
    private OsmMapViewScrollWorkaround mMapView = null;
    private OsmMapHandler mapHandler = null;
    private OnFancyPlaceSelectedListener fancyPlaceSelectedCallback = null;
    private MainWindow parent = null;
    private LocationHandler locationHandler = null;

    public static FPOsmDroidView newInstance() {
        FPOsmDroidView result = new FPOsmDroidView();

        return result;
    }

    @Override
    public String getTitle(Context context) {

        return context.getString(R.string.fp_map_view_title);
    }

    @Override
    public void onLocationUpdated(Location location) {
        mapHandler.setCamera(location.getLatitude(), location.getLongitude(), com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_ZOOM_FAR);
        mapHandler.setCurrentLocationMarker(location.getLatitude(), location.getLongitude(), getString(R.string.your_location));
    }

    @Override
    public void onLocationUpdating() {
        getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.updating_location, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());

        mMapView = new OsmMapViewScrollWorkaround(inflater.getContext(), 128, mResourceProxy);
        mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mMapView.setMultiTouchControls(true);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setWorkaroundEnabled(true);

        mapHandler = new OsmMapHandler(mMapView, fancyPlaceSelectedCallback);
        mapHandler.setAdapter(parent.fancyPlaceArrayAdapter);


        locationHandler = ((FancyPlacesApplication) parent.getApplicationContext()).getLocationHandler();
        locationHandler.addOnLocationUpdatedListener(this);
        locationHandler.updateLocation(false);

        return mMapView;
    }


    @Override
    public void onDestroyView() {
        locationHandler.removeOnLocationUpdatedListener(this);
        super.onDestroyView();

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
