package com.google.android.libraries.gsa.d.a;

import android.view.WindowManager.LayoutParams;

final class TransparentOverlayController implements PanelController {

    private final OverlayController overlayController;

    TransparentOverlayController(OverlayController overlayControllerVar) {
        this.overlayController = overlayControllerVar;
    }

    @Override
    public void onPanelDragged() {

    }

    @Override
    public void startPanelDrag() {
    }

    @Override
    public void setPanelEnabled(boolean enabled) {
    }

    @Override
    public void openPanel() {
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

    @Override
    public void closePanel() {
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
        this.overlayController.slidingPanelLayout.panelController = this.overlayController.panelController;
    }

    @Override
    public void setPanelPosition(float position) {
    }

    @Override
    public boolean canInterceptTouchEvents() {
        return true;
    }
}
