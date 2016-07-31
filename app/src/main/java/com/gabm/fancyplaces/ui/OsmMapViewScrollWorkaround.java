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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gabm.fancyplaces.R;

import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.MapView;

/**
 * Created by gabm on 25/05/15.
 */
public class OsmMapViewScrollWorkaround extends MapView {


    protected Boolean WorkaroundEnabled = false;

    protected OsmMapViewScrollWorkaround(Context context, MapTileProviderBase tileProvider, Handler tileRequestCompleteHandler, AttributeSet attrs) {
        super(context, tileProvider, tileRequestCompleteHandler, attrs);

        setWorkaroundEnabled(context, attrs);
    }

    public OsmMapViewScrollWorkaround(Context context) {
        super(context);
    }

    public OsmMapViewScrollWorkaround(Context context, MapTileProviderBase aTileProvider) {
        super(context, aTileProvider);
    }

    public OsmMapViewScrollWorkaround(Context context, MapTileProviderBase aTileProvider, Handler tileRequestCompleteHandler) {
        super(context, aTileProvider, tileRequestCompleteHandler);
    }

    public OsmMapViewScrollWorkaround(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWorkaroundEnabled(context, attrs);
    }


    protected void setWorkaroundEnabled(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.com_gabm_fancyplaces_OsmMapViewScrollWorkaround,
                0, 0);


        try {
            WorkaroundEnabled = a.getBoolean(R.styleable.com_gabm_fancyplaces_OsmMapViewScrollWorkaround_ScrollWorkaroundEnabled, false);
        } finally {
            a.recycle();
        }
    }

    public void setWorkaroundEnabled(Boolean workaroundEnabled) {
        WorkaroundEnabled = workaroundEnabled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (WorkaroundEnabled)
                    getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
                if (WorkaroundEnabled)
                    getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
