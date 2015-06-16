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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.ui.ListViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabm on 15/05/15.
 */
public class FancyPlaceListViewAdapter extends ArrayAdapter<FancyPlace> implements IOnListModeChangeListener {

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

        return convertView;
    }

    public void toggleSelected(int i)
    {
        listViewItems.get(i).toggleSelected();
    }

    public void setSelected(int i, boolean isSelected) {
        listViewItems.get(i).setSelected(isSelected);
    }

    @Override
    public void onListModeChange(int newMode) {
        if (newMode == MODE_NORMAL)
        {
            for (int i=0; i<listViewItems.size(); i++)
                listViewItems.get(i).setSelectable(false);

        } else if (newMode == MODE_MULTI_SELECT)
        {
            for (int i=0; i<listViewItems.size(); i++)
                listViewItems.get(i).setSelectable(true);
        }
    }
}