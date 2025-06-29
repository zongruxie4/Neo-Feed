package com.google.android.libraries.gsa.d.a;

import android.content.res.Configuration;
import android.os.Message;
import java.io.PrintWriter;

public final class MinusOneOverlayCallback extends OverlayControllerCallback {

    private final OverlaysController overlaysController;

    public MinusOneOverlayCallback(OverlaysController overlaysController, OverlayControllerBinder overlayControllerBinder) {
        super(overlayControllerBinder, 3);
        this.overlaysController = overlaysController;
    }

    @Override
    public OverlayController createController(Configuration configuration) {
        return overlaysController.createController(
                configuration,
                overlayControllerBinder.getServerVersion(),
                overlayControllerBinder.getClientVersion()
        );
    }

    @Override
    public void dump(PrintWriter printWriter, String prefix) {
        printWriter.println(prefix + "MinusOneOverlayCallback");
        super.dump(printWriter, prefix);
    }

    @Override
    public boolean handleMessage(Message message) {
        if (super.handleMessage(message)) {
            return true;
        }

        if (overlayController == null) {
            return false;
        }

        final OverlayController controller = overlayController;
        final long timestamp = message.getWhen();

        return switch (message.what) {
            case 3 -> {
                if (!controller.cnD()) {
                    SlidingPanelLayout panel = controller.slidingPanelLayout;
                    if (panel.uoC < panel.mTouchSlop) {
                        panel.BM(0);
                        controller.mAcceptExternalMove = true;
                        controller.unX = 0;
                        panel.mForceDrag = true;
                        controller.obZ = timestamp - 30;
                        controller.b(0, controller.unX, controller.obZ);
                        controller.b(2, controller.unX, timestamp);
                    }
                }
                yield true;
            }

            case 4 -> {
                if (controller.mAcceptExternalMove && message.obj instanceof Float floatValue) {
                    controller.unX = (int) (floatValue * controller.slidingPanelLayout.getMeasuredWidth());
                    controller.b(2, controller.unX, timestamp);
                }
                yield true;
            }

            case 5 -> {
                if (controller.mAcceptExternalMove) {
                    controller.b(1, controller.unX, timestamp);
                }
                controller.mAcceptExternalMove = false;
                yield true;
            }

            default -> false;
        };
    }
}
