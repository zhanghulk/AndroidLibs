
package com.hulk.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader {

    public void loadBitmapSimplely(ImageView imageView, int resId) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(resId);
    }

    public void loadResBitmap(ImageView imageView, int resId, Bitmap placeHolderBitmap) {
        if (BitmapWorkerTask.cancelPotentialWork(resId, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(imageView.getResources(), placeHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId);
        }
    }

    public static Bitmap getBitmap(Context c, int resId) {
        return BitmapFactory.decodeResource(c.getResources(), resId);
    }
}
