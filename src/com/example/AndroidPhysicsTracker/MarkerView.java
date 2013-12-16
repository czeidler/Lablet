package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class MarkerView extends ViewGroup {
    public MarkerView(Context context) {
        super(context);
        init();
    }

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarkerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
/*
        int x = 10;
        int y = 10;
        int width = 700;
        int height = 50;

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(0xff74AC23);
        drawable.setBounds(x, y, x + width, y + height);

        drawable.draw(canvas);
        */
        setBackgroundColor(Color.TRANSPARENT);

        Paint paint = new Paint();
        paint.setColor(Color.RED);

        Rect frame = new Rect();
        getDrawingRect(frame);
        canvas.drawLine(frame.left, frame.top, frame.right, frame.bottom, paint);
        canvas.drawLine(frame.right, frame.top, frame.left, frame.bottom, paint);

        canvas.drawLine(400, frame.top, 400, frame.bottom, paint);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getX() < 400)
            return true;
        return false;
    }

    private void init() {
        setWillNotDraw(false);
    }

}