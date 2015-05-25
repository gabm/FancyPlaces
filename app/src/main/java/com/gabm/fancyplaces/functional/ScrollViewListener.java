package com.gabm.fancyplaces.functional;

import com.gabm.fancyplaces.ui.ObservableScrollView;

/**
 * Created by gabm on 19/05/15.
 */
public interface ScrollViewListener {

    void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);

}