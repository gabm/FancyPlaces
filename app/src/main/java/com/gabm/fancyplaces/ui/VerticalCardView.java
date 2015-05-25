package com.gabm.fancyplaces.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gabm.fancyplaces.R;

/**
 * Created by gabm on 23.12.14.
 */
public class VerticalCardView extends LinearLayout {

    private String title;

    public VerticalCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.com_gabm_fancyplaces_VerticalCardView,
                0, 0);


        try {
            title = a.getString(R.styleable.com_gabm_fancyplaces_VerticalCardView_card_title);
        } finally {
            a.recycle();
        }

        // own settings
        this.setOrientation(VERTICAL);
        this.setPadding(30, 30, 30, 30);

        // text view as title
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 20, 0, 10);
        this.addView(textView);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
        invalidate();
        requestLayout();
    }
}
