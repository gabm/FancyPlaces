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
import android.widget.ImageView;
import android.widget.TextView;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;

import java.util.List;

/**
 * Created by gabm on 15/05/15.
 */
public class FancyPlaceListViewAdapter extends ArrayAdapter<FancyPlace> {
    Context context;

    public FancyPlaceListViewAdapter(Context context, int resourceId, List<FancyPlace> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        FancyPlace fancyPlace = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_fancy_place, null);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.li_fp_title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.li_fp_thumbnail);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.titleTextView.setText(fancyPlace.getTitle());

        ImageFileLoaderTask backgroundTask = new ImageFileLoaderTask(holder.imageView);
        if (fancyPlace.getImage().exists())
            backgroundTask.execute(fancyPlace.getImage());
        return convertView;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView titleTextView;
    }
}