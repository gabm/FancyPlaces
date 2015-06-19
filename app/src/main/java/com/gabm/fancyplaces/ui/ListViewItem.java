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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.functional.ImageFileLoaderTask;

/**
 * Created by gabm on 08/06/15.
 */
public class ListViewItem implements ImageFileLoaderTask.OnImageLoaderCompletedListener {

    private Bitmap thumbnail;
    private ImageView thumbnailView;
    private TextView titleTextView;
    private LinearLayout backgroundLayoutView;
    private Context curContext;

    private boolean selected = false;
    private boolean selectable = false;

    private Animation animationBegin;
    private Animation animationEnd;


    public ListViewItem(Context context, View v) {
        thumbnailView = (ImageView)v.findViewById(R.id.li_fp_thumbnail);
        titleTextView = (TextView)v.findViewById(R.id.li_fp_title);
        backgroundLayoutView = (LinearLayout) v.findViewById(R.id.li_background);
        curContext = context;

        animationBegin = AnimationUtils.loadAnimation(context, R.anim.to_middle);
        animationEnd = AnimationUtils.loadAnimation(context, R.anim.from_middle);

        thumbnail = ((BitmapDrawable)thumbnailView.getDrawable()).getBitmap();
    }

    public void setFancyPlace(FancyPlace fancyPlace)
    {
        titleTextView.setText(fancyPlace.getTitle());

        ImageFileLoaderTask backgroundTask = new ImageFileLoaderTask(thumbnailView, this);

        if (fancyPlace.getImage().exists())
            backgroundTask.execute(fancyPlace.getImage());
    }

    public boolean isSelectable()
    {
        return selectable;
    }

    public void setSelectable(boolean _selectable)
    {
        selectable = _selectable;

        if (!selectable)
            setSelected(false);
    }

    public void toggleAndAnimateSelected() {
        startTogglingWithAnimation();
    }

    public void setAndAnimateSelected(boolean _selected) {
        if (selected != _selected)
            startTogglingWithAnimation();
    }

    public boolean isSelected()
    {
        return selected;
    }

    protected void setSelected(boolean _selected)
    {
        selected = _selected;

        if (selected) {
            backgroundLayoutView.setBackgroundColor(curContext.getResources().getColor(R.color.ColorBackgroundAccent));
            thumbnailView.setImageResource(R.drawable.ic_done_white_48dp);
        } else {
            backgroundLayoutView.setBackgroundColor(curContext.getResources().getColor(R.color.ColorBackground));
            thumbnailView.setImageBitmap(thumbnail);
        }
    }

    private void startTogglingWithAnimation() {
        thumbnailView.clearAnimation();
        thumbnailView.setAnimation(animationBegin);
        thumbnailView.startAnimation(animationBegin);

        Animation.AnimationListener animListener;
        animListener = new FlipAnimationListener();

        animationBegin.setAnimationListener(animListener);
        animationEnd.setAnimationListener(animListener);
    }

    @Override
    public void onImageLoaderCompleted(Bitmap bitmap) {
        thumbnail = bitmap;
    }


    private class FlipAnimationListener implements Animation.AnimationListener
    {
        public FlipAnimationListener(){}
        @Override
        public void onAnimationStart(Animation animation) {
        }


        @Override
        public void onAnimationRepeat(Animation arg0) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (animation == animationBegin) {
                setSelected(!isSelected());

                thumbnailView.clearAnimation();
                thumbnailView.setAnimation(animationEnd);
                thumbnailView.startAnimation(animationEnd);
            } else {
                thumbnailView.clearAnimation();
            }
        }
    }
}
