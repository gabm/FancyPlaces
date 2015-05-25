package com.gabm.fancyplaces;

/**
 * Created by gabm on 15/05/15.
 */


public interface OnFancyPlaceSelectedListener {
    int INTENT_VIEW = 0;
    int INTENT_EDIT = 1;
    int INTENT_DELETE = 2;
    int INTENT_SHARE = 3;
    int INTENT_CREATE_NEW = 4;

    void onFancyPlaceSelected(int id, int intent);
}
