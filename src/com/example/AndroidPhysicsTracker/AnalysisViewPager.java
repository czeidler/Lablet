package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class AnalysisViewPager extends ViewPager {
    Knob leftKnob = null;
    Knob rightKnob = null;

    boolean touchOnKnob = false;

    public AnalysisViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        leftKnob = new Knob();
        rightKnob = new Knob();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        positionKnobs();

        int i = getCurrentItem();
        int pageNumber = getAdapter().getCount();
        if (i != 0)
            leftKnob.draw(canvas);
        if (i < pageNumber - 1)
            rightKnob.draw(canvas);
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        if (touchOnKnob)
            return super.onInterceptHoverEvent(event);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (touchOnKnob)
            return super.onInterceptTouchEvent(event);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action != MotionEvent.ACTION_DOWN) {
            if (touchOnKnob) {
                if (action == MotionEvent.ACTION_UP)
                    touchOnKnob = false;
                return super.onTouchEvent(event);
            } else
                return false;
        }

        float xPos = event.getX();
        float yPos = event.getY();

        if (!rightKnob.getTouchFrame().contains((int)xPos, (int)yPos)
                && !leftKnob.getTouchFrame().contains((int)xPos, (int)yPos))
            return false;
        touchOnKnob = true;
        return super.onTouchEvent(event);
    }

    private void positionKnobs() {
        Rect frame = new Rect();
        getWindowVisibleDisplayFrame(frame);

        final int SCROLL_RANGE = 40;
        int yPosition = (int)((float)frame.height() * 0.80);

        leftKnob.setTouchFrame(new Rect(frame.left, yPosition, frame.left + SCROLL_RANGE, yPosition + SCROLL_RANGE));
        rightKnob.setTouchFrame(new Rect(frame.right - SCROLL_RANGE, yPosition, frame.right, yPosition + SCROLL_RANGE));

        int i = getCurrentItem();
        frame.offset(frame.width() * i, 0);

        leftKnob.setDrawFrame(new Rect(frame.left, yPosition, frame.left + SCROLL_RANGE, yPosition + SCROLL_RANGE));
        rightKnob.setDrawFrame(new Rect(frame.right - SCROLL_RANGE, yPosition, frame.right, yPosition + SCROLL_RANGE));
    }

    class Knob extends Drawable {
        Paint paint = new Paint();
        Rect frame = null;
        Rect touchFrame = null;

        public Knob() {
            paint.setColor(Color.RED);
        }

        public void setDrawFrame(Rect rect) {
            frame = rect;
        }

        public void setTouchFrame(Rect rect) {
            touchFrame = rect;
        }

        public Rect getTouchFrame() {
            return touchFrame;
        }

        @Override
        public void draw(Canvas canvas) {
            if (frame == null)
                return;
            canvas.drawRect(frame, paint);
        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }
}
