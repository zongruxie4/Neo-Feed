package com.saulhdev.feeder.ui.views;

import static android.animation.ValueAnimator.areAnimatorsEnabled;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;

import com.saulhdev.feeder.service.OverlayView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class AbstractBottomSheet extends LinearLayout{

    @IntDef(flag = true, value = {
            TYPE_FILTER_SHEET
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FloatingViewType {
    }

    public static final int TYPE_FILTER_SHEET = 1 << 0;

    public static final int TYPE_ALL = TYPE_FILTER_SHEET; // Add other types as needed;

    protected boolean mIsOpen;
    public static ViewGroup container;

    public AbstractBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    public final void close(boolean animate) {
        Log.d("AbstractBottomSheet", "close");
        animate &= areAnimatorsEnabled();
        if (mIsOpen) {
            // Add to WW logging
        }
        handleClose(animate);
        mIsOpen = false;
    }

    protected abstract void handleClose(boolean animate);

    public final boolean isOpen() {
        return mIsOpen;
    }

    protected abstract boolean isOfType(@FloatingViewType int type);

    /**
     * @return Whether the back is consumed. If false, Launcher will handle the back as well.
     */
    public boolean onBackPressed() {
        close(true);
        return true;
    }


    /**
     * Returns a view matching FloatingViewType
     */
    public static <T extends AbstractBottomSheet> T getOpenView(
            Context context, @FloatingViewType int type) {

        OverlayView overlayView =new OverlayView(context);
        FrameLayout container =  overlayView.container;
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            if (child instanceof AbstractBottomSheet) {
                AbstractBottomSheet view = (AbstractBottomSheet) child;
                if (view.isOfType(type) && view.isOpen()) {
                    return (T) view;
                }
            }
        }
        return null;
    }

    public static void closeOpenContainer(Context context,
                                          @FloatingViewType int type) {
        AbstractBottomSheet view = getOpenView(context, type);
        if (view != null) {
            view.close(true);
        }
    }

    public static void closeOpenViews(Context context, boolean animate,
                                      @FloatingViewType int type) {
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            if (child instanceof AbstractBottomSheet) {
                AbstractBottomSheet abs = (AbstractBottomSheet) child;
                if (abs.isOfType(type)) {
                    abs.close(animate);
                }
            }
        }
    }

    public static void closeAllOpenViews(Context context, boolean animate) {
        closeOpenViews(context, animate, TYPE_ALL);
    }

    public static void closeAllOpenViews(Context context) {
        closeAllOpenViews(context, true);
    }
    public static AbstractBottomSheet getTopOpenView(Context context) {
        return getTopOpenViewWithType(context, TYPE_ALL);
    }

    public static AbstractBottomSheet getTopOpenViewWithType(Context context,
                                                              @FloatingViewType int type) {
        return getOpenView(context, type);
    }

}