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

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.gabm.fancyplaces.data.ImageFile;

import java.lang.ref.WeakReference;

/**
 * Created by gabm on 20/05/15.
 */
public class ImageFileLoaderTask extends AsyncTask<ImageFile, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private OnImageLoaderCompletedListener onImageLoaderCompletedListener = null;

    public interface OnImageLoaderCompletedListener {
        void onImageLoaderCompleted(Bitmap bitmap);
    }

    public ImageFileLoaderTask(ImageView imageView, OnImageLoaderCompletedListener imageLoaderCompletedListener) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<>(imageView);
        onImageLoaderCompletedListener = imageLoaderCompletedListener;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(ImageFile... data) {
        return data[0].loadThumbnail();
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();

            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }

            if (onImageLoaderCompletedListener != null)
                onImageLoaderCompletedListener.onImageLoaderCompleted(bitmap);
        }
    }
}
