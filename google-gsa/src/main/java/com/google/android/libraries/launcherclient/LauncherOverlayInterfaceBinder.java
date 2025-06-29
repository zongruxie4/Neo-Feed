package com.google.android.libraries.launcherclient;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.WindowManager.LayoutParams;

import com.google.android.a.LauncherOverlayBinder;
import com.google.android.a.ParcelUtils;


public abstract class LauncherOverlayInterfaceBinder extends LauncherOverlayBinder implements ILauncherOverlay {

    protected LauncherOverlayInterfaceBinder() {
        attachInterface(this, "com.google.android.libraries.launcherclient.ILauncherOverlay");
    }

    public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {//Todo: throws is new
        ILauncherOverlayCallback dVar = null;
        if (super.onTransact(i, parcel, parcel2, i2)) {
            return true;
        }
        IBinder readStrongBinder;
        IInterface queryLocalInterface;
        boolean HC;
        switch (i) {
            case 1:
                startScroll();
                break;
            case 2:
                onScroll(parcel.readFloat());
                break;
            case 3:
                endScroll();
                break;
            case 4:
                LayoutParams layoutParams = (LayoutParams) ParcelUtils.readParcelable(parcel, LayoutParams.CREATOR);
                readStrongBinder = parcel.readStrongBinder();
                if (readStrongBinder != null) {
                    queryLocalInterface = readStrongBinder.queryLocalInterface("com.google.android.libraries.launcherclient.ILauncherOverlayCallback");
                    if (queryLocalInterface instanceof ILauncherOverlayCallback) {
                        dVar = (ILauncherOverlayCallback) queryLocalInterface;
                    } else {
                        dVar = new LauncherOverlayCallback(readStrongBinder);
                    }
                }
                windowAttached(layoutParams, dVar, parcel.readInt());
                break;
            case 5:
                requestVoiceDetection(ParcelUtils.readBoolean(parcel));
                break;
            case 6:
                // Check if the client supports the feature
                break;
            case 7:
                onPause();
                break;
            case 8:
                onResume();
                break;
            case 9:
                openOverlay(parcel.readInt());
                break;
            case 10:
                windowDetached(ParcelUtils.readBoolean(parcel));
                break;
            case 11:
                //Voice search is always enabled, so we don't need to check the version
                break;
            case 12:
                // This method is not used in the current implementation, but we keep it for compatibility
                break;
            case 13:
                parcel2.writeNoException();
                ParcelUtils.writeBoolean(parcel2, true);
                break;
            case 14:
                Bundle bundle = (Bundle) ParcelUtils.readParcelable(parcel, Bundle.CREATOR);
                readStrongBinder = parcel.readStrongBinder();
                if (readStrongBinder != null) {
                    queryLocalInterface = readStrongBinder.queryLocalInterface("com.google.android.libraries.launcherclient.ILauncherOverlayCallback");
                    if (queryLocalInterface instanceof ILauncherOverlayCallback) {
                        dVar = (ILauncherOverlayCallback) queryLocalInterface;
                    } else {
                        dVar = new LauncherOverlayCallback(readStrongBinder);
                    }
                }
                windowAttached(bundle, dVar);
                break;
            case 16:
                closeOverlay(parcel.readInt());
                break;
            case 17:
                // This method is not used in the current implementation, but we keep it for compatibility
                break;
            default:
                return false;
        }
        return true;
    }
}
