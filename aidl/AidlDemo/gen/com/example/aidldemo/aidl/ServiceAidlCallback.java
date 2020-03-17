/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\github\\AndroidTools\\aidl\\AidlDemo\\src\\com\\example\\aidldemo\\aidl\\ServiceAidlCallback.aidl
 */
package com.example.aidldemo.aidl;
public interface ServiceAidlCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.aidldemo.aidl.ServiceAidlCallback
{
private static final java.lang.String DESCRIPTOR = "com.example.aidldemo.aidl.ServiceAidlCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.aidldemo.aidl.ServiceAidlCallback interface,
 * generating a proxy if needed.
 */
public static com.example.aidldemo.aidl.ServiceAidlCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.aidldemo.aidl.ServiceAidlCallback))) {
return ((com.example.aidldemo.aidl.ServiceAidlCallback)iin);
}
return new com.example.aidldemo.aidl.ServiceAidlCallback.Stub.Proxy(obj);
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
case TRANSACTION_performAction:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
this.performAction(_arg0);
reply.writeNoException();
return true;
}
default:
{
return super.onTransact(code, data, reply, flags);
}
}
}
private static class Proxy implements com.example.aidldemo.aidl.ServiceAidlCallback
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
@Override public void performAction(java.lang.String action) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(action);
mRemote.transact(Stub.TRANSACTION_performAction, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_performAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void performAction(java.lang.String action) throws android.os.RemoteException;
}
