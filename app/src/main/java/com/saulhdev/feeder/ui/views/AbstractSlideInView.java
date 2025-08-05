package com.saulhdev.feeder.ui.views;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSlideInView extends AbstractBottomSheet {
    protected static final Property<AbstractSlideInView, Float> TRANSLATION_SHIFT =
            new Property<AbstractSlideInView, Float>(Float.class, "translationShift") {

                @Override
                public Float get(AbstractSlideInView view) {
                    return view.mTranslationShift;
                }

                @Override
                public void set(AbstractSlideInView view, Float value) {
                    view.setTranslationShift(value);
                }
            };

    protected static final float TRANSLATION_SHIFT_CLOSED = 1f;
    protected static final float TRANSLATION_SHIFT_OPENED = 0f;
    protected final ObjectAnimator mOpenCloseAnimator;
    protected Interpolator mScrollInterpolator;
    protected float mTranslationShift = TRANSLATION_SHIFT_CLOSED;
    protected List<OnCloseListener> mOnCloseListeners = new ArrayList<>();
    protected View mContent;
    protected final View mColorScrim;

    public AbstractSlideInView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScrollInterpolator =  new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t*t*t + 1;
            }
        };

        mOpenCloseAnimator = ObjectAnimator.ofPropertyValuesHolder(this);
        mOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        int scrimColor = getScrimColor(context);
        mColorScrim = scrimColor != -1 ? createColorScrim(context, scrimColor) : null;
    }

    protected int getScrimColor(Context context) {
        return -1;
    }

    protected View createColorScrim(Context context, int bgColor) {
        View view = new View(context);
        view.forceHasOverlappingRendering(false);
        view.setBackgroundColor(bgColor);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setLayoutParams(lp);

        return view;
    }
    protected void handleClose(boolean animate, long defaultDuration) {
        if (!mIsOpen) {
            return;
        }
        if (!animate) {
            mOpenCloseAnimator.cancel();
            setTranslationShift(TRANSLATION_SHIFT_CLOSED);
            onCloseComplete();
            return;
        }
        mOpenCloseAnimator.setValues(
                PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_CLOSED));
        mOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onCloseComplete();
            }
        });

        mOpenCloseAnimator.setInterpolator(mScrollInterpolator);
        mOpenCloseAnimator.start();
    }

    protected void setTranslationShift(float translationShift) {
        mTranslationShift = translationShift;
        mContent.setTranslationY(mTranslationShift * mContent.getHeight());
        if (mColorScrim != null) {
            mColorScrim.setAlpha(1 - mTranslationShift);
        }
    }

    protected void onCloseComplete() {
        mIsOpen = false;
        container.removeView(this);
        if (mColorScrim != null) {
            container.removeView(mColorScrim);
        }
        mOnCloseListeners.forEach(OnCloseListener::onSlideInViewClosed);
    }



    /**
     * Interface to report that the {@link AbstractSlideInView} has closed.
     */
    public interface OnCloseListener {

        /**
         * Called when {@link AbstractSlideInView} closes.
         */
        void onSlideInViewClosed();
    }
}
