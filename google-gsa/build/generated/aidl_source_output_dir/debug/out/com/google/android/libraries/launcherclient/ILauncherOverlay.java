/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /opt/android-sdk/build-tools/35.0.0/aidl -p/opt/android-sdk/platforms/android-36/framework.aidl -o/media/saul/Windows/dev/Neo-Feed/google-gsa/build/generated/aidl_source_output_dir/debug/out -I/media/saul/Windows/dev/Neo-Feed/google-gsa/src/main/aidl -I/media/saul/Windows/dev/Neo-Feed/google-gsa/src/debug/aidl -I/home/saul/.gradle/caches/8.13/transforms/458fe9c04ad5ad8d92bc5006a8c4c620/transformed/core-1.16.0/aidl -I/home/saul/.gradle/caches/8.13/transforms/ac952b8aea3fa737acbb575d1637c4bc/transformed/versionedparcelable-1.1.1/aidl -d/tmp/aidl5536487198478788859.d /media/saul/Windows/dev/Neo-Feed/google-gsa/src/main/aidl/com/google/android/libraries/launcherclient/ILauncherOverlay.aidl
 */
package com.google.android.libraries.launcherclient;
public interface ILauncherOverlay extends android.os.IInterface
{
  /** Default implementation for ILauncherOverlay. */
  public static class Default implements com.google.android.libraries.launcherclient.ILauncherOverlay
  {
    @Override public void startScroll() throws android.os.RemoteException
    {
    }
    @Override public void onScroll(float progress) throws android.os.RemoteException
    {
    }
    @Override public void endScroll() throws android.os.RemoteException
    {
    }
    @Override public void windowAttached(android.view.WindowManager.LayoutParams lp, com.google.android.libraries.launcherclient.ILauncherOverlayCallback cb, int flags) throws android.os.RemoteException
    {
    }
    @Override public void windowDetached(boolean isChangingConfigurations) throws android.os.RemoteException
    {
    }
    @Override public void closeOverlay(int flags) throws android.os.RemoteException
    {
    }
    @Override public void onPause() throws android.os.RemoteException
    {
    }
    @Override public void onResume() throws android.os.RemoteException
    {
    }
    @Override public void openOverlay(int flags) throws android.os.RemoteException
    {
    }
    @Override public void requestVoiceDetection(boolean start) throws android.os.RemoteException
    {
    }
    @Override public java.lang.String getVoiceSearchLanguage() throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean isVoiceDetectionRunning() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean hasOverlayContent() throws android.os.RemoteException
    {
      return false;
    }
    @Override public void windowAttached2(android.os.Bundle bundle, com.google.android.libraries.launcherclient.ILauncherOverlayCallback cb) throws android.os.RemoteException
    {
    }
    @Override public void unusedMethod() throws android.os.RemoteException
    {
    }
    @Override public void setActivityState(int flags) throws android.os.RemoteException
    {
    }
    @Override public boolean startSearch(byte[] data, android.os.Bundle bundle) throws android.os.RemoteException
    {
      return false;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.google.android.libraries.launcherclient.ILauncherOverlay
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.google.android.libraries.launcherclient.ILauncherOverlay interface,
     * generating a proxy if needed.
     */
    public static com.google.android.libraries.launcherclient.ILauncherOverlay asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.google.android.libraries.launcherclient.ILauncherOverlay))) {
        return ((com.google.android.libraries.launcherclient.ILauncherOverlay)iin);
      }
      return new com.google.android.libraries.launcherclient.ILauncherOverlay.Stub.Proxy(obj);
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
        case TRANSACTION_startScroll:
        {
          this.startScroll();
          break;
        }
        case TRANSACTION_onScroll:
        {
          float _arg0;
          _arg0 = data.readFloat();
          this.onScroll(_arg0);
          break;
        }
        case TRANSACTION_endScroll:
        {
          this.endScroll();
          break;
        }
        case TRANSACTION_windowAttached:
        {
          android.view.WindowManager.LayoutParams _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.view.WindowManager.LayoutParams.CREATOR);
          com.google.android.libraries.launcherclient.ILauncherOverlayCallback _arg1;
          _arg1 = com.google.android.libraries.launcherclient.ILauncherOverlayCallback.Stub.asInterface(data.readStrongBinder());
          int _arg2;
          _arg2 = data.readInt();
          this.windowAttached(_arg0, _arg1, _arg2);
          break;
        }
        case TRANSACTION_windowDetached:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.windowDetached(_arg0);
          break;
        }
        case TRANSACTION_closeOverlay:
        {
          int _arg0;
          _arg0 = data.readInt();
          this.closeOverlay(_arg0);
          break;
        }
        case TRANSACTION_onPause:
        {
          this.onPause();
          break;
        }
        case TRANSACTION_onResume:
        {
          this.onResume();
          break;
        }
        case TRANSACTION_openOverlay:
        {
          int _arg0;
          _arg0 = data.readInt();
          this.openOverlay(_arg0);
          break;
        }
        case TRANSACTION_requestVoiceDetection:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.requestVoiceDetection(_arg0);
          break;
        }
        case TRANSACTION_getVoiceSearchLanguage:
        {
          java.lang.String _result = this.getVoiceSearchLanguage();
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_isVoiceDetectionRunning:
        {
          boolean _result = this.isVoiceDetectionRunning();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_hasOverlayContent:
        {
          boolean _result = this.hasOverlayContent();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_windowAttached2:
        {
          android.os.Bundle _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          com.google.android.libraries.launcherclient.ILauncherOverlayCallback _arg1;
          _arg1 = com.google.android.libraries.launcherclient.ILauncherOverlayCallback.Stub.asInterface(data.readStrongBinder());
          this.windowAttached2(_arg0, _arg1);
          break;
        }
        case TRANSACTION_unusedMethod:
        {
          this.unusedMethod();
          break;
        }
        case TRANSACTION_setActivityState:
        {
          int _arg0;
          _arg0 = data.readInt();
          this.setActivityState(_arg0);
          break;
        }
        case TRANSACTION_startSearch:
        {
          byte[] _arg0;
          _arg0 = data.createByteArray();
          android.os.Bundle _arg1;
          _arg1 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          boolean _result = this.startSearch(_arg0, _arg1);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.google.android.libraries.launcherclient.ILauncherOverlay
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
      @Override public void startScroll() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startScroll, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onScroll(float progress) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(progress);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onScroll, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void endScroll() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_endScroll, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void windowAttached(android.view.WindowManager.LayoutParams lp, com.google.android.libraries.launcherclient.ILauncherOverlayCallback cb, int flags) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, lp, 0);
          _data.writeStrongInterface(cb);
          _data.writeInt(flags);
          boolean _status = mRemote.transact(Stub.TRANSACTION_windowAttached, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void windowDetached(boolean isChangingConfigurations) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((isChangingConfigurations)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_windowDetached, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void closeOverlay(int flags) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(flags);
          boolean _status = mRemote.transact(Stub.TRANSACTION_closeOverlay, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onPause() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPause, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onResume() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onResume, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void openOverlay(int flags) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(flags);
          boolean _status = mRemote.transact(Stub.TRANSACTION_openOverlay, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void requestVoiceDetection(boolean start) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((start)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestVoiceDetection, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public java.lang.String getVoiceSearchLanguage() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getVoiceSearchLanguage, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public boolean isVoiceDetectionRunning() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isVoiceDetectionRunning, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public boolean hasOverlayContent() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_hasOverlayContent, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void windowAttached2(android.os.Bundle bundle, com.google.android.libraries.launcherclient.ILauncherOverlayCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, bundle, 0);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_windowAttached2, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void unusedMethod() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unusedMethod, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void setActivityState(int flags) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(flags);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setActivityState, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public boolean startSearch(byte[] data, android.os.Bundle bundle) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeByteArray(data);
          _Parcel.writeTypedObject(_data, bundle, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startSearch, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_startScroll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onScroll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_endScroll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_windowAttached = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_windowDetached = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_closeOverlay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_onPause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_onResume = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_openOverlay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_requestVoiceDetection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_getVoiceSearchLanguage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_isVoiceDetectionRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_hasOverlayContent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_windowAttached2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_unusedMethod = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_setActivityState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
    static final int TRANSACTION_startSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.google.android.libraries.launcherclient.ILauncherOverlay";
  public void startScroll() throws android.os.RemoteException;
  public void onScroll(float progress) throws android.os.RemoteException;
  public void endScroll() throws android.os.RemoteException;
  public void windowAttached(android.view.WindowManager.LayoutParams lp, com.google.android.libraries.launcherclient.ILauncherOverlayCallback cb, int flags) throws android.os.RemoteException;
  public void windowDetached(boolean isChangingConfigurations) throws android.os.RemoteException;
  public void closeOverlay(int flags) throws android.os.RemoteException;
  public void onPause() throws android.os.RemoteException;
  public void onResume() throws android.os.RemoteException;
  public void openOverlay(int flags) throws android.os.RemoteException;
  public void requestVoiceDetection(boolean start) throws android.os.RemoteException;
  public java.lang.String getVoiceSearchLanguage() throws android.os.RemoteException;
  public boolean isVoiceDetectionRunning() throws android.os.RemoteException;
  public boolean hasOverlayContent() throws android.os.RemoteException;
  public void windowAttached2(android.os.Bundle bundle, com.google.android.libraries.launcherclient.ILauncherOverlayCallback cb) throws android.os.RemoteException;
  public void unusedMethod() throws android.os.RemoteException;
  public void setActivityState(int flags) throws android.os.RemoteException;
  public boolean startSearch(byte[] data, android.os.Bundle bundle) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
