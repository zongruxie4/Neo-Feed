package com.google.android.libraries.gsa.d.a;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.SparseArray;

import java.util.Arrays;

public abstract class OverlaysController {

    private final Service service;
    private final SparseArray<OverlayControllerBinder> clients = new SparseArray<>();
    public final Handler handler = new Handler();

    protected OverlaysController(Service service) {
        this.service = service;
    }

    public abstract OverlayController createController(Configuration configuration, int i, int i2);

    public final synchronized IBinder onBind(Intent intent) {
        OverlayControllerBinder iBinder;
        int i = Integer.MAX_VALUE;
        synchronized (this) {
            Uri data = intent.getData();
            int port = data.getPort();
            if (port == -1) {
                iBinder = null;
            } else {
                int parseInt;
                try {
                    parseInt = Integer.parseInt(data.getQueryParameter("v"));
                } catch (Exception e) {
                    parseInt = i;
                }
                try {
                    i = Integer.parseInt(data.getQueryParameter("cv"));
                } catch (Exception ignored) {

                }
                String[] packagesForUid = this.service.getPackageManager().getPackagesForUid(port);
                String host = data.getHost();
                if (packagesForUid == null || !Arrays.asList(packagesForUid).contains(host)) {
                    iBinder = null;
                } else {
                    iBinder = this.clients.get(port);
                    if (!(iBinder == null || iBinder.mServerVersion == parseInt)) {
                        iBinder.destroy();
                        iBinder = null;
                    }
                    if (iBinder == null) {
                        iBinder = new OverlayControllerBinder(this, port, host, parseInt, i);
                        this.clients.put(port, iBinder);
                    }
                }
            }
        }
        return iBinder;
    }

    public final synchronized void onUnbind(Intent intent) {
        int port = intent.getData().getPort();
        if (port != -1) {
            OverlayControllerBinder overlayControllerBinderVar = this.clients.get(port);
            if (overlayControllerBinderVar != null) {
                overlayControllerBinderVar.destroy();
            }
            this.clients.remove(port);
        }
    }

    public final synchronized void onDestroy() {
        for (int size = this.clients.size() - 1; size >= 0; size--) {
            OverlayControllerBinder overlayControllerBinderVar = this.clients.valueAt(size);
            if (overlayControllerBinderVar != null) {
                overlayControllerBinderVar.destroy();
            }
        }
        this.clients.clear();
    }

    public v HA() {
        return new v();
    }

    //Todo: maybe remove
    public int Hx() {
        return 24;
    }
}
