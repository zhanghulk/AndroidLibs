/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\github\\AndroidTools\\aidl\\AidlService\\src\\com\\example\\aidldemo\\aidl\\ServiceAidl.aidl
 */
package com.example.aidldemo.aidl;
public interface ServiceAidl extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.aidldemo.aidl.ServiceAidl
{
private static final java.lang.String DESCRIPTOR = "com.example.aidldemo.aidl.ServiceAidl";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.aidldemo.aidl.ServiceAidl interface,
 * generating a proxy if needed.
 */
public static com.example.aidldemo.aidl.ServiceAidl asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.aidldemo.aidl.ServiceAidl))) {
return ((com.example.aidldemo.aidl.ServiceAidl)iin);
}
return new com.example.aidldemo.aidl.ServiceAidl.Stub.Proxy(obj);
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
case TRANSACTION_registerTestCall:
{
data.enforceInterface(descriptor);
com.example.aidldemo.aidl.ServiceAidlCallback _arg0;
_arg0 = com.example.aidldemo.aidl.ServiceAidlCallback.Stub.asInterface(data.readStrongBinder());
this.registerTestCall(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_invokCallBack:
{
data.enforceInterface(descriptor);
this.invokCallBack();
reply.writeNoException();
return true;
}
default:
{
return super.onTransact(code, data, reply, flags);
}
}
}
private static class Proxy implements com.example.aidldemo.aidl.ServiceAidl
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
@Override public void registerTestCall(com.example.aidldemo.aidl.ServiceAidlCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerTestCall, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void invokCallBack() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_invokCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_registerTestCall = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_invokCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void registerTestCall(com.example.aidldemo.aidl.ServiceAidlCallback cb) throws android.os.RemoteException;
public void invokCallBack() throws android.os.RemoteException;
}
