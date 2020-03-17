/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\github\\AndroidTools\\aidl\\AidlDemo\\src\\com\\example\\aidldemo\\aidl\\ISslVpn.aidl
 */
package com.example.aidldemo.aidl;
public interface ISslVpn extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.aidldemo.aidl.ISslVpn
{
private static final java.lang.String DESCRIPTOR = "com.example.aidldemo.aidl.ISslVpn";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.aidldemo.aidl.ISslVpn interface,
 * generating a proxy if needed.
 */
public static com.example.aidldemo.aidl.ISslVpn asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.aidldemo.aidl.ISslVpn))) {
return ((com.example.aidldemo.aidl.ISslVpn)iin);
}
return new com.example.aidldemo.aidl.ISslVpn.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
java.lang.String descriptor = DESCRIPTOR;
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(descriptor);
return true;
}
case TRANSACTION_updateAppList:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.updateAppList(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(descriptor);
com.example.aidldemo.aidl.ISslVpnCallback _arg0;
_arg0 = com.example.aidldemo.aidl.ISslVpnCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
default:
{
return super.onTransact(code, data, reply, flags);
}
}
}
private static class Proxy implements com.example.aidldemo.aidl.ISslVpn
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
@Override public boolean updateAppList(java.lang.String sslVpnConfig) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sslVpnConfig);
mRemote.transact(Stub.TRANSACTION_updateAppList, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void registerCallback(com.example.aidldemo.aidl.ISslVpnCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_updateAppList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public boolean updateAppList(java.lang.String sslVpnConfig) throws android.os.RemoteException;
public void registerCallback(com.example.aidldemo.aidl.ISslVpnCallback cb) throws android.os.RemoteException;
}
