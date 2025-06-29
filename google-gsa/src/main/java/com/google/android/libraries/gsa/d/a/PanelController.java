package com.google.android.libraries.gsa.d.a;

interface PanelController {
    void setPanelPosition(float position);

    void onPanelDragged();

    void cnF();

    void openPanel();

    void closePanel();

    boolean canInterceptTouchEvents();

    void setPanelEnabled(boolean enabled);
}
