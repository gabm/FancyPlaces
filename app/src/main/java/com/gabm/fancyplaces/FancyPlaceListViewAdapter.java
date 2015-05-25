package com.gabm.fancyplaces;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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