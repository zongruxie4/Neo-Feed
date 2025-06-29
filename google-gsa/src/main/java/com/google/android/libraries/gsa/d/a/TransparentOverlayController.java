package com.google.android.libraries.gsa.d.a;

import android.view.WindowManager.LayoutParams;

final class TransparentOverlayController implements PanelController {

    private final OverlayController overlayController;

    TransparentOverlayController(OverlayController overlayControllerVar) {
        this.overlayController = overlayControllerVar;
    }

    public final void onPanelDragged() {

    }

    public final void cnF() {
    }

    public final void setPanelEnabled(boolean enabled) {
    }

    public final void openPanel() {
        this.overlayController.setVisible(true);
        OverlayController overlayControllerVar = this.overlayController;
        LayoutParams attributes = overlayControllerVar.window.getAttributes();
        float f = attributes.alpha;
        attributes.alpha = 1.0f;
        if (f != attributes.alpha) {
            overlayControllerVar.window.setAttributes(attributes);
        }
        overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.OPEN_AS_LAYER;//Todo: PanelState.uoh was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
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
        this.overlayController.setVisible(false);
        overlayControllerVar = this.overlayController;
        PanelState panelStateVar = PanelState.CLOSED;//Todo: PanelState.uoe was default
        if (overlayControllerVar.panelState != panelStateVar) {
            overlayControllerVar.panelState = panelStateVar;
            overlayControllerVar.setState(overlayControllerVar.panelState);
        }
        this.overlayController.slidingPanelLayout.panelController = this.overlayController.overlayControllerStateChanger;
    }

    public final void setPanelPosition(float position) {
    }

    public final boolean canInterceptTouchEvents() {
        return true;
    }
}
