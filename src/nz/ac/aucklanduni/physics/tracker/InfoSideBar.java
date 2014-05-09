/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


class InfoBarBackgroundDrawable extends Drawable {
    private Paint paint;

    public InfoBarBackgroundDrawable(int color) {
        paint = new Paint();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = new Rect(getBounds());
        // golden ratio
        rect.bottom = rect.top + (int)((float)(rect.height()) * 0.618);
        rect.right = rect.left + (int)((float)(rect.width()) * 0.5);

        canvas.drawRect(rect, paint);
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

public class InfoSideBar extends FrameLayout {
    private ImageView iconView;
    private TextView infoTextView;

    public InfoSideBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.info_side_bar, null, false);
        assert view != null;

        addView(view);

        iconView = (ImageView)view.findViewById(R.id.infoSidebarIcon);
        assert iconView != null;

        infoTextView = (TextView)view.findViewById(R.id.infoSideBarText);
        assert infoTextView != null;
    }

    public void setIcon(int resource) {
        iconView.setImageResource(resource);
    }

    public void setInfoText(String info) {
        infoTextView.setText(info);
    }
}
