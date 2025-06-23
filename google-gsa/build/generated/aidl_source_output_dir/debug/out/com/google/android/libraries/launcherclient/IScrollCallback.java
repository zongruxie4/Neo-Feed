/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /opt/android-sdk/build-tools/35.0.0/aidl -p/opt/android-sdk/platforms/android-36/framework.aidl -o/media/saul/Windows/dev/Neo-Feed/google-gsa/build/generated/aidl_source_output_dir/debug/out -I/media/saul/Windows/dev/Neo-Feed/google-gsa/src/main/aidl -I/media/saul/Windows/dev/Neo-Feed/google-gsa/src/debug/aidl -I/home/saul/.gradle/caches/8.13/transforms/458fe9c04ad5ad8d92bc5006a8c4c620/transformed/core-1.16.0/aidl -I/home/saul/.gradle/caches/8.13/transforms/ac952b8aea3fa737acbb575d1637c4bc/transformed/versionedparcelable-1.1.1/aidl -d/tmp/aidl7634424639822153027.d /media/saul/Windows/dev/Neo-Feed/google-gsa/src/main/aidl/com/google/android/libraries/launcherclient/IScrollCallback.aidl
 */
package com.google.android.libraries.launcherclient;
public interface IScrollCallback extends android.os.IInterface
{
  /** Default implementation for IScrollCallback. */
  public static class Default implements com.google.android.libraries.launcherclient.IScrollCallback
  {
    @Override public void onOverlayScrollChanged(float progress) throws android.os.RemoteException
    {
    }
    @Override public void onServiceStateChanged(boolean overlayAttached) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.google.android.libraries.launcherclient.IScrollCallback
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.google.android.libraries.launcherclient.IScrollCallback interface,
     * generating a proxy if needed.
     */
    public static com.google.android.libraries.launcherclient.IScrollCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.google.android.libraries.launcherclient.IScrollCallback))) {
        return ((com.google.android.libraries.launcherclient.IScrollCallback)iin);
      }
      return new com.google.android.libraries.launcherclient.IScrollCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_onOverlayScrollChanged:
        {
          float _arg0;
          _arg0 = data.readFloat();
          this.onOverlayScrollChanged(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onServiceStateChanged:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.onServiceStateChanged(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.google.android.libraries.launcherclient.IScrollCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void onOverlayScrollChanged(float progress) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(progress);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onOverlayScrollChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onServiceStateChanged(boolean overlayAttached) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((overlayAttached)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_onServiceStateChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_onOverlayScrollChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onServiceStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.google.android.libraries.launcherclient.IScrollCallback";
  public void onOverlayScrollChanged(float progress) throws android.os.RemoteException;
  public void onServiceStateChanged(boolean overlayAttached) throws android.os.RemoteException;
}
