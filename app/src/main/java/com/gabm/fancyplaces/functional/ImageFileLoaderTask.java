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

    public ImageFileLoaderTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
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
        }
    }
}
