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

import android.widget.TextView;

import com.gabm.fancyplaces.R;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

/**
 * Created by gabm on 25/05/15.
 */
public class OsmMarkerInfoWindow extends InfoWindow {


    private TextView titleView = null;

    public OsmMarkerInfoWindow(MapView mapView, OsmMarker parent) {
        super(R.layout.marker_fancy_place, mapView);

        titleView = (TextView) getView().findViewById(R.id.info_window_title);

        titleView.setOnClickListener(parent);

    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    @Override
    public void onOpen(Object o) {

    }

    @Override
    public void onClose() {

    }
}
