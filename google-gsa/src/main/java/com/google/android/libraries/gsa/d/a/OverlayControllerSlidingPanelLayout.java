package com.google.android.libraries.gsa.d.a;

import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.savedstate.SavedStateRegistry;
import androidx.savedstate.SavedStateRegistryController;
import androidx.savedstate.SavedStateRegistryOwner;

public final class OverlayControllerSlidingPanelLayout extends SlidingPanelLayout implements
        LifecycleOwner, SavedStateRegistryOwner {

    private final OverlayController overlayController;
    private SavedStateRegistryController savedStateRegistryController = SavedStateRegistryController.create(this);
    private SavedStateRegistry savedStateRegistry = savedStateRegistryController.getSavedStateRegistry();
    public LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    public OverlayControllerSlidingPanelLayout(OverlayController overlayControllerVar) {
        super(overlayControllerVar);
        this.overlayController = overlayControllerVar;
        ViewTreeLifecycleOwner.set(this, this);
    }

    public void determineScrollingStart(MotionEvent motionEvent, float f) {
        Object obj = 1;
        if (motionEvent.findPointerIndex(this.activePointerId) != -1) {
            float x = motionEvent.getX() - this.downX;
            float abs = Math.abs(x);
            float abs2 = Math.abs(motionEvent.getY() - this.downY);
            if (Float.compare(abs, 0.0f) != 0) {
                abs = (float) Math.atan((double) (abs2 / abs));
                Object obj2;
                if (this.isRtl) {
                    obj2 = x < 0.0f ? 1 : null;
                } else if (x > 0.0f) {
                    obj2 = 1;//TODO: different from source
                } else {
                    obj2 = null;
                }
                if (!this.isPanelOpen || this.isPageMoving) {
                    obj = null;
                }
                if (obj != null && obj2 != null) {//TODO: different from source
                    return;
                }
                if ((obj != null && this.panelController.canInterceptTouchEvents()) || abs > 1.0471976f) {
                    return;
                }
                if (abs > 0.5235988f) {
                    super.determineScrollStart(motionEvent, (((float) Math.sqrt((double) ((abs - 0.5235988f) / 0.5235988f))) * 4.0f) + 1.0f);
                } else {
                    super.determineScrollStart(motionEvent, f);
                }
            }
        }
    }

    protected boolean fitSystemWindows(Rect rect) {
        return !this.overlayController.unZ || super.fitSystemWindows(rect);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @NonNull
    @Override
    public SavedStateRegistry getSavedStateRegistry() {
        return savedStateRegistry;
    }
}
