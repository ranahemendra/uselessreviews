package com.mishga.uselessreviews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ImageView extends android.widget.ImageView
{
    public ImageView(Context context) {
        super(context);
    }

    public ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN && isEnabled())
            setColorFilter(0x33ff0000, PorterDuff.Mode.MULTIPLY); //your color here

        if(event.getAction() == MotionEvent.ACTION_UP)
            setColorFilter(null);

        return super.onTouchEvent(event);
    }
}
