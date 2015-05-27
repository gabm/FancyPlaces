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

package com.gabm.fancyplaces.functional;

import android.database.DataSetObserver;
import android.widget.ArrayAdapter;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.ui.OsmMarker;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Created by gabm on 24/05/15.
 */
public class OsmMapHandler extends DataSetObserver implements IMapHandler, OsmMarker.IMarkerSelected {

    private IMapController curMapController = null;
    private MapView curMapView = null;
    private ArrayAdapter<FancyPlace> adapter = null;
    private OnFancyPlaceSelectedListener fancyPlaceSelectedCallback = null;

    public OsmMapHandler(MapView mapView, OnFancyPlaceSelectedListener listener) {
        curMapView = mapView;
        curMapController = mapView.getController();
        fancyPlaceSelectedCallback = listener;
    }

    @Override
    public void setCamera(double lat, double lng, float zoom) {
        IGeoPoint pt = new GeoPoint(lat, lng);
        curMapController.setCenter(pt);
        curMapController.setZoom((int) zoom);
    }

    @Override
    public void animateCamera(double lat, double lng, int duration) {
        IGeoPoint pt = new GeoPoint(lat, lng);
        curMapController.setCenter(pt);

    }

    @Override
    public void animateCamera(double lat, double lng, float zoom, int duration) {
        IGeoPoint pt = new GeoPoint(lat, lng);
        curMapController.setCenter(pt);
        curMapController.setZoom((int) zoom);
    }

    @Override
    public void clearMarkers() {
        for (int i = 0; i < curMapView.getOverlays().size(); i++) {
            OsmMarker marker = (OsmMarker) curMapView.getOverlays().get(i);
            if (marker != null) {
                marker.setInfoWindowVisible(false);
            }
        }
        curMapView.getOverlays().clear();

    }

    protected OsmMarker createMarker(GeoPoint pt, String text, boolean showInfoWindow, int id, int drawableID) {
        OsmMarker marker = new OsmMarker(curMapView.getContext(), curMapView);

        marker.setPosition(pt);
        marker.setIcon(curMapView.getContext().getDrawable(drawableID));
        marker.setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM);
        marker.setTitle(text);
        marker.setId(id);
        marker.setInfoWindowVisible(showInfoWindow);
        marker.setMarkerSelectedListener(this);

        return marker;

    }

    @Override
    public void addMarker(double lat, double lng, String text, boolean showInfoWindow) {
        GeoPoint pt = new GeoPoint(lat, lng);

        addMarker(createMarker(pt, text, showInfoWindow, -1, R.drawable.ic_pin));
    }

    protected void addMarker(OsmMarker marker) {
        curMapView.getOverlays().add(marker);
        curMapView.invalidate();
    }

    @Override
    public void setCurrentLocationMarker(double lat, double lng, String title) {
        OsmMarker marker = createMarker(new GeoPoint(lat, lng), title, false, -1, R.drawable.ic_my_location);
        addMarker(marker);
    }

    @Override
    public void onChanged() {
        super.onChanged();
        updateMarkersFromDataSource();
    }

    @Override
    public void onInvalidated() {
        super.onInvalidated();
        updateMarkersFromDataSource();
    }

    protected void updateMarkersFromDataSource() {
        clearMarkers();
        for (int i = 0; i < adapter.getCount(); i++) {
            FancyPlace fp = adapter.getItem(i);

            GeoPoint pt = new GeoPoint(Double.valueOf(fp.getLocationLat()), Double.valueOf(fp.getLocationLong()));
            addMarker(createMarker(pt, fp.getTitle(), false, i, R.drawable.ic_pin));
        }
    }

    @Override
    public void setAdapter(ArrayAdapter<FancyPlace> in_adapter) {
        adapter = in_adapter;
        adapter.registerDataSetObserver(this);

        updateMarkersFromDataSource();
    }

    @Override
    public void onMarkerSelected(int id) {
        if (fancyPlaceSelectedCallback != null)
            fancyPlaceSelectedCallback.onFancyPlaceSelected(id, OnFancyPlaceSelectedListener.INTENT_VIEW);
    }
}
