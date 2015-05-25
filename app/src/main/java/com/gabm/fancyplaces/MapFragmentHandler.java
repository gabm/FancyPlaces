package com.gabm.fancyplaces;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabm on 23.12.14.
 */
public class MapFragmentHandler extends DataSetObserver implements GoogleMap.OnInfoWindowClickListener {

    GoogleMap _map;
    private Marker curLocationMarker = null;
    private List<Marker> curMarkers = new ArrayList<>();
    private ArrayAdapter<FancyPlace> adapter = null;
    private OnFPClickListener fpClickListener = null;

    public MapFragmentHandler(GoogleMap map) {
        _map = map;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (fpClickListener != null) {
            if (marker.isInfoWindowShown() && !marker.equals(curLocationMarker)) {
                fpClickListener.onFPClicked(curMarkers.indexOf(marker));
            }
        }
    }

    public void setOnMapLongClickListener(GoogleMap.OnMapLongClickListener longClickListener) {
        _map.setOnMapLongClickListener(longClickListener);
    }

    public void setOnFPClickedListener(OnFPClickListener onFPClickedListener) {
        fpClickListener = onFPClickedListener;
        _map.setOnInfoWindowClickListener(this);
    }

    public void setAdapter(ArrayAdapter<FancyPlace> in_adapter) {
        adapter = in_adapter;
        adapter.registerDataSetObserver(this);

        updateMarkersFromDataSource();
    }

    public GoogleMap getGoogleMap() {
        return _map;
    }

    public void setCamera(double lat, double lng, float zoom) {
        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    public void animateCamera(double lat, double lng, int duration) {
        float curZoom = _map.getCameraPosition().zoom;
        animateCamera(lat, lng, curZoom, duration);
    }

    public void animateCamera(double lat, double lng, float zoom, int duration) {
        _map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom), duration, null);
    }

    public void clearMarkers() {
        for (Marker mark : curMarkers)
            mark.remove();

        curMarkers.clear();
    }

    public void addMarker(double lat, double lng, String text) {
        Marker tmp = _map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(text));
        tmp.showInfoWindow();
        curMarkers.add(tmp);
    }

    public void setCurrentLocationMarker(double lat, double lng, String title) {
        if (curLocationMarker != null)
            curLocationMarker.remove();

        // add new
        curLocationMarker = _map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
    }

    protected void updateMarkersFromDataSource() {
        clearMarkers();
        for (int i = 0; i < adapter.getCount(); i++) {
            FancyPlace fp = adapter.getItem(i);
            addMarker(Double.valueOf(fp.getLocationLat()),
                    Double.valueOf(fp.getLocationLong()),
                    fp.getTitle());
        }
    }

    public void onSaveInstanceState(Bundle bundle) {

    }

    public void onRestoreInstanceState(Bundle bundle) {
        updateMarkersFromDataSource();
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

    public interface OnFPClickListener {
        void onFPClicked(int id);
    }
}
