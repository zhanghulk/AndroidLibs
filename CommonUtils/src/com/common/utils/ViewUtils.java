
package com.common.utils;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;

public class ViewUtils {
    public static Bitmap convertViewToBitmap(View view)
    {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    public static void setImgLayout(View img) {
        int width = img.getResources().getDisplayMetrics().widthPixels;
        int height = img.getResources().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams lp = img.getLayoutParams();
        // 兼顾横竖屏的宽高变化，矣小的为准
        int imgSize = width < height ? width : height;
        lp.width = imgSize;
        lp.height = imgSize;
        img.setLayoutParams(lp);
    }
}
