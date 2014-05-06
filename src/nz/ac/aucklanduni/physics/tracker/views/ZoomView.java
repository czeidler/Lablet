/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Zooms in a view and closes it on tap.
 */
public class ZoomView extends FrameLayout {
    private View currentView;
    private Animator animator;
    private int shortAnimationDuration;

    public ZoomView(Context context) {
        super(context);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setVisibility(View.INVISIBLE);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setVisibility(View.INVISIBLE);
    }

    public void zoom(final View zoomCopy, final Rect startBounds, final Rect finalBounds, final View original) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (animator != null)
            animator.cancel();

        currentView = zoomCopy;
        addView(currentView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        original.setAlpha(0f);
        setAlpha(1f);
        setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        setPivotX(0f);
        setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(this, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(this, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animator = null;
            }
        });
        set.start();
        animator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (animator != null)
                    animator.cancel();

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(view, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(view, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(view, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setVisibility(View.GONE);
                        original.setAlpha(1f);
                        removeAllViews();
                        currentView = null;
                        animator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        setVisibility(View.GONE);
                        original.setAlpha(1f);
                        removeAllViews();
                        currentView = null;
                        animator = null;
                    }
                });
                set.start();
                animator = set;
            }
        });
    }
}
