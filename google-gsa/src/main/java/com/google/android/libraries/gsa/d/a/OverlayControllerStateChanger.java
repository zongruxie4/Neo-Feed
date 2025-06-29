package com.google.android.libraries.gsa.d.a;

import android.view.WindowManager.LayoutParams;

final class OverlayControllerStateChanger implements PanelController {

    private final OverlayController overlayController;

    OverlayControllerStateChanger(OverlayController overlayController) {
        this.overlayController = overlayController;
    }

    @Override
    public void onPanelDragged() {
        updatePanelState(PanelState.DRAGGING);
        updateAlphaIfNeeded(1.0f);
    }

    @Override
    public void startPanelDrag() {
        updatePanelState(PanelState.DRAGGING);
        overlayController.setVisible(true);
        updateAlphaIfNeeded(1.0f);
    }

    @Override
    public void setPanelEnabled(boolean enabled) {
        if (enabled) {
            overlayController.Hn();
        }
        updatePanelState(PanelState.DRAGGING);
        overlayController.setVisible(false);
    }

    @Override
    public void openPanel() {
        updatePanelState(PanelState.OPEN_AS_DRAWER);
    }

    @Override
    public void setPanelPosition(float position) {
        if (overlayController.overlayCallback != null && !Float.isNaN(position)) {
            try {
                overlayController.overlayCallback.overlayScrollChanged(position);
                overlayController.onScroll(position);
            } catch (Throwable ignored) {
                // Optionally log the exception if needed
            }
        }
    }

    @Override
    public void closePanel() {
        updateAlphaIfNeeded(0.0f);
        updatePanelState(PanelState.CLOSED);
    }

    @Override
    public boolean canInterceptTouchEvents() {
        return overlayController.shouldHandleInput();
    }

    private void updatePanelState(PanelState newState) {
        if (overlayController.panelState != newState) {
            overlayController.panelState = newState;
            overlayController.setState(newState);
        }
    }

    private void updateAlphaIfNeeded(float newAlpha) {
        LayoutParams attributes = overlayController.window.getAttributes();
        if (attributes.alpha != newAlpha) {
            attributes.alpha = newAlpha;
            overlayController.window.setAttributes(attributes);
        }
    }
}
