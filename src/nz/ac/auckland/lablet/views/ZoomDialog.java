/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;


/**
 * Takes view and zooms it to a certain size. On tab it zooms back and closes.
 */
public class ZoomDialog extends Dialog {
    private View contentView;
    private Rect startBounds;
    private Rect finalBounds;
    private float startScale;

    private FrameLayout frameLayout;
    private int shortAnimationDuration;
    private Animator animator;

    /**
     * Dialog that open with size finalBounds and then zooms the content view from startBounds up to finalBounds.
     * TODO: Somehow it does not work to position the Dialog. So how it works at the moment is that the Dialog
     * goes fullscreen. Thus finalBounds must be the the fullscreen coordinates.
     * @param context
     * @param content A copy of the view that should be zoomed.
     * @param startBounds The current bounds of the view that should be zoomed.
     * @param finalBounds The target size of the zoomed view.
     */
    public ZoomDialog(Context context, final View content, Rect startBounds, Rect finalBounds) {
        super(context);

        this.contentView = content;
        this.startBounds = new Rect(startBounds);
        this.finalBounds = new Rect(finalBounds);

        shortAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        frameLayout = new FrameLayout(context);
        setContentView(frameLayout);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        frameLayout.addView(contentView, params);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);


        setupZoomAnimation();
    }

    private void setupZoomAnimation() {
        // Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of the zoomed-in view
        // (the default is the center of the view).
        contentView.setPivotX(0f);
        contentView.setPivotY(0f);

        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            this.startBounds.left -= deltaWidth;
            this.startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            this.startBounds.top -= deltaHeight;
            this.startBounds.bottom += deltaHeight;
        }

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (animator != null)
                    animator.cancel();

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(contentView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(contentView, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(contentView, View.SCALE_X, startScale))
                        .with(ObjectAnimator.ofFloat(contentView, View.SCALE_Y, startScale))
                        .with(ObjectAnimator.ofFloat(contentView, View.ALPHA, 1f, 0.3f));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animator = null;
                        dismiss();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        animator = null;
                        dismiss();
                    }
                });
                set.start();
                animator = set;
            }
        });
    }

    @Override
    protected void onStart() {
        if (animator != null)
            animator.cancel();

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(contentView, View.X, startBounds.left, 0))
                .with(ObjectAnimator.ofFloat(contentView, View.Y, startBounds.top, 0))
                .with(ObjectAnimator.ofFloat(contentView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(contentView, View.SCALE_Y, startScale, 1f));
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
    }
}
