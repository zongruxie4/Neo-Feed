package com.google.android.libraries.gsa.d.a;

import android.view.WindowManager.LayoutParams;

final class OverlayControllerStateChanger implements PanelController {

    private final OverlayController overlayController;

    OverlayControllerStateChanger(OverlayController overlayControllerVar) {
        this.overlayController = overlayControllerVar;
    }

    public final void onPanelDragged() {
        OverlayController overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.DRAGGING;//Todo: PanelState.uof was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
        overlayControllerVar = this.overlayController;
        LayoutParams attributes = overlayControllerVar.window.getAttributes();
        float f = attributes.alpha;
        attributes.alpha = 1.0f;
        if (f != attributes.alpha) {
            overlayControllerVar.window.setAttributes(attributes);
        }
    }

    public final void cnF() {
        OverlayController overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.DRAGGING;//Todo: PanelState.uof was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
        this.overlayController.setVisible(true);
        overlayControllerVar = this.overlayController;
        LayoutParams attributes = overlayControllerVar.window.getAttributes();
        float f = attributes.alpha;
        attributes.alpha = 1.0f;
        if (f != attributes.alpha) {
            overlayControllerVar.window.setAttributes(attributes);
        }
    }

    public final void setPanelEnabled(boolean enabled) {
        if (enabled) {
            this.overlayController.Hn();
        }
        OverlayController overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.DRAGGING;//Todo: PanelState.uof was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
        this.overlayController.setVisible(false);
    }

    public final void openPanel() {
        OverlayController overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.OPEN_AS_DRAWER;//Todo: PanelState.uog was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
    }

    public final void setPanelPosition(float position) {
        if (this.overlayController.uoa != null && !Float.isNaN(position)) {
            try {
                this.overlayController.uoa.overlayScrollChanged(position);
                this.overlayController.onScroll(position);
            } catch (Throwable ignored) {

            }
        }
    }

    public final void closePanel() {
        OverlayController overlayControllerVar = this.overlayController;
        LayoutParams attributes = overlayControllerVar.window.getAttributes();
        float f = attributes.alpha;
        attributes.alpha = 0.0f;
        if (f != attributes.alpha) {
            overlayControllerVar.window.setAttributes(attributes);
        }
        overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.CLOSED;//Todo: PanelState.uoe was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
    }

    public final boolean canInterceptTouchEvents() {
        return this.overlayController.Ho();
    }
}
