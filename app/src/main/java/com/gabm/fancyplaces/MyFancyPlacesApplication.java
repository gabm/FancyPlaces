package com.gabm.fancyplaces;

import android.app.Application;

/**
 * Created by gabm on 19/05/15.
 */
public class MyFancyPlacesApplication extends Application {
    public static final int TARGET_PIX_SIZE = 1000;
    public static final String TMP_IMAGE_FILENAME = "tmpFancyPlacesImg.png";
    static public final int MAP_DEFAULT_ZOOM_NEAR = 16;
    static public final int MAP_DEFAULT_ZOOM_FAR = 13;
    static public final int MAP_DEFAULT_DURATION = 3000;
    public static String TMP_IMAGE_FULL_PATH = "";

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
