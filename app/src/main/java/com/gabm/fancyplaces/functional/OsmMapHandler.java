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
public class OsmMapHandler extends DataSetObserver implements IMapHandler {

    private IMapController curMapController = null;
    private MapView curMapView = null;
    private ArrayAdapter<FancyPlace> adapter = null;

    public OsmMapHandler(MapView mapView) {
        curMapView = mapView;
        curMapController = mapView.getController();
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
        curMapView.getOverlays().clear();

    }

    @Override
    public void addMarker(double lat, double lng, String text, boolean showInfoWindow) {
        OsmMarker marker = new OsmMarker(curMapView.getContext(), curMapView);

        GeoPoint pt = new GeoPoint(lat, lng);
        marker.setPosition(pt);
        marker.setIcon(curMapView.getContext().getDrawable(R.drawable.ic_pin));
        marker.setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM);
        marker.setTitle(text);

        if (showInfoWindow)
            marker.toogleInfoWindow();

        curMapView.getOverlays().add(marker);
        curMapView.invalidate();
    }

    @Override
    public void setCurrentLocationMarker(double lat, double lng, String title) {

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
            addMarker(Double.valueOf(fp.getLocationLat()),
                    Double.valueOf(fp.getLocationLong()),
                    fp.getTitle(), false);
        }
    }

    @Override
    public void setAdapter(ArrayAdapter<FancyPlace> in_adapter) {
        adapter = in_adapter;
        adapter.registerDataSetObserver(this);

        updateMarkersFromDataSource();
    }

}
