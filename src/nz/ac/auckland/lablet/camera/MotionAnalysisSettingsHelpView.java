package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import nz.ac.auckland.lablet.R;


/**
 * An overlay help view for the {@link nz.ac.auckland.lablet.camera.MotionAnalysisSettingsHelpView}.
 */
public class MotionAnalysisSettingsHelpView extends FrameLayout {
    private ImageView frameRateImageView;
    private ImageView rangeImageView;
    private TextView rangeTextView;

    public MotionAnalysisSettingsHelpView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.motion_analysis_settings_help, null, false);
        addView(view);

        frameRateImageView = (ImageView)view.findViewById(R.id.frameRateImageView);
        rangeImageView = (ImageView)view.findViewById(R.id.rangeImageView);
        rangeTextView = (TextView)view.findViewById(R.id.rangeTextView);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * This function updates the help view item to be aligned with the layout of the activity. For example, to move
     * help text to the right position. Thus the layout of the activity layout must be ready when calling this function.
     * @param parent activity with the layout that should be annotated with help items
     */
    public void setParent(MotionAnalysisSettingsActivity parent) {
        Rect framePickerRect = parent.getFramePickerRect();
        Rect rangeSeekBarRect = parent.getRangeSeekBarRect();

        Rect bounds = new Rect();
        getDrawingRect(bounds);

        // frame rate help
        Rect frameRateImageViewRect = new Rect();
        frameRateImageView.getDrawingRect(frameRateImageViewRect);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)frameRateImageView.getLayoutParams();
        params.rightMargin = bounds.width() - framePickerRect.left;
        params.rightMargin += 20;
        params.topMargin = (framePickerRect.top + framePickerRect.bottom) / 2 - (frameRateImageViewRect.height() / 2);
        frameRateImageView.setLayoutParams(params);

        // range help
        Rect rangeImageViewRect = new Rect();
        rangeImageView.getDrawingRect(rangeImageViewRect);

        params = (RelativeLayout.LayoutParams)rangeImageView.getLayoutParams();
        int rangeArrowXPos = (rangeSeekBarRect.left + rangeSeekBarRect.right) / 2 - rangeImageViewRect.width() / 2;
        params.leftMargin = rangeArrowXPos;
        params.bottomMargin = bounds.height() - rangeSeekBarRect.top;
        params.bottomMargin += 20;
        rangeImageView.setLayoutParams(params);

        Rect rangeTextViewRect = new Rect();
        rangeTextView.getDrawingRect(rangeTextViewRect);
        params = (RelativeLayout.LayoutParams)rangeTextView.getLayoutParams();
        params.leftMargin = rangeArrowXPos - rangeTextViewRect.width() / 2;
        rangeTextView.setLayoutParams(params);
    }
}
