/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import nz.ac.aucklanduni.physics.lablet.R;


/**
 * The side bar that is shown on the left of the home screens.
 */
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
