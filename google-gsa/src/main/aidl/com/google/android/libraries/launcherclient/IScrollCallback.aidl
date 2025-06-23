package com.google.android.libraries.launcherclient;

interface IScrollCallback {
    void onOverlayScrollChanged(float progress);

    void onServiceStateChanged(boolean overlayAttached);
}