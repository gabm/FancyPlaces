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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.ui.ListViewItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gabm on 15/05/15.
 */
public class FancyPlaceListViewAdapter extends ArrayAdapter<FancyPlace> {

    private static final int MODE_NORMAL = 0;
    private static final int MODE_SELECT_MULTIPLE = 1;

    private int curMode = MODE_NORMAL;

    private Map<Integer, ListViewItem> listViewItemMap = new HashMap<>();
    private List<ListViewItem> listViewItems = new ArrayList<>();

    public FancyPlaceListViewAdapter(Context context, int resourceId, List<FancyPlace> items) {
        super(context, resourceId, items);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        FancyPlace fancyPlace = getItem(position);

        ListViewItem item = null;

        LayoutInflater mInflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_fancy_place, null);
            item = new ListViewItem(getContext(), convertView);

            convertView.setTag(item);
        } else
            item = (ListViewItem) convertView.getTag();


        item.setFancyPlace(fancyPlace);

        listViewItems.add(item);

        /*convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (curMode == MODE_NORMAL)
                    switchMode(MODE_SELECT_MULTIPLE);
                else if (curMode == MODE_SELECT_MULTIPLE)
                    switchMode(MODE_NORMAL);

                return true;
            }
        });*/

        return convertView;
    }

    private void switchMode(int newMode)
    {
        curMode = newMode;

        if (curMode == MODE_NORMAL)
        {
            for (int i=0; i<listViewItems.size(); i++)
                listViewItems.get(i).setSelectable(false);

        } else if (curMode == MODE_SELECT_MULTIPLE)
        {
            for (int i=0; i<listViewItems.size(); i++)
                listViewItems.get(i).setSelectable(true);
        }

    }
}