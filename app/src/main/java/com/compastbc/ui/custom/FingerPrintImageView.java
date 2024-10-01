package com.compastbc.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Hemant on 22/08/2019.
 */
public class FingerPrintImageView extends AppCompatImageView {

    public static float radius = 2000.0f;

    public FingerPrintImageView(Context context) {
        super(context);
    }

    public FingerPrintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FingerPrintImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //float radius = 36.0f;
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}