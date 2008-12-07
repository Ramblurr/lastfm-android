/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/ramblurr/src/workspace/LastFm/src/com/binaryelysium/android/lastfm/player/IRadioPlayer.aidl
 */
package com.binaryelysium.android.lastfm.player;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
import net.roarsoftware.lastfm.Radio;
import net.roarsoftware.lastfm.Session;
public interface IRadioPlayer extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.binaryelysium.android.lastfm.player.IRadioPlayer
{
private static final java.lang.String DESCRIPTOR = "com.binaryelysium.android.lastfm.player.IRadioPlayer";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IRadioPlayer interface,
 * generating a proxy if needed.
 */
public static com.binaryelysium.android.lastfm.player.IRadioPlayer asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
com.binaryelysium.android.lastfm.player.IRadioPlayer in = (com.binaryelysium.android.lastfm.player.IRadioPlayer)obj.queryLocalInterface(DESCRIPTOR);
if ((in!=null)) {
return in;
}
return new com.binaryelysium.android.lastfm.player.IRadioPlayer.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setTuner:
{
data.enforceInterface(DESCRIPTOR);
net.roarsoftware.lastfm.Radio _arg0;
if ((0!=data.readInt())) {
_arg0 = net.roarsoftware.lastfm.Radio.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.setTuner(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setSession:
{
data.enforceInterface(DESCRIPTOR);
net.roarsoftware.lastfm.Session _arg0;
if ((0!=data.readInt())) {
_arg0 = net.roarsoftware.lastfm.Session.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.setSession(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_pause:
{
data.enforceInterface(DESCRIPTOR);
this.pause();
reply.writeNoException();
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_skipForward:
{
data.enforceInterface(DESCRIPTOR);
this.skipForward();
reply.writeNoException();
return true;
}
case TRANSACTION_startRadio:
{
data.enforceInterface(DESCRIPTOR);
this.startRadio();
reply.writeNoException();
return true;
}
case TRANSACTION_love:
{
data.enforceInterface(DESCRIPTOR);
this.love();
reply.writeNoException();
return true;
}
case TRANSACTION_skip:
{
data.enforceInterface(DESCRIPTOR);
this.skip();
reply.writeNoException();
return true;
}
case TRANSACTION_ban:
{
data.enforceInterface(DESCRIPTOR);
this.ban();
reply.writeNoException();
return true;
}
case TRANSACTION_getArtistName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getArtistName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getAlbumName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getAlbumName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getTrackName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getTrackName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getArtUrl:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getArtUrl();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getDuration:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getDuration();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getPosition:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getPosition();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getBufferPercent:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getBufferPercent();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getStationName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getStationName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getStationUrl:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getStationUrl();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.binaryelysium.android.lastfm.player.IRadioPlayer
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void setTuner(net.roarsoftware.lastfm.Radio tuner) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((tuner!=null)) {
_data.writeInt(1);
tuner.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setTuner, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void setSession(net.roarsoftware.lastfm.Session session) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((session!=null)) {
_data.writeInt(1);
session.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setSession, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void pause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void skipForward() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_skipForward, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void startRadio() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startRadio, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void love() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_love, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void skip() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_skip, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void ban() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_ban, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.lang.String getArtistName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArtistName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String getAlbumName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAlbumName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String getTrackName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTrackName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String getArtUrl() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArtUrl, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long getDuration() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDuration, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long getPosition() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPosition, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getBufferPercent() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBufferPercent, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean isPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String getStationName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getStationName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String getStationUrl() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getStationUrl, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_setTuner = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setSession = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_pause = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_stop = (IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_skipForward = (IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_startRadio = (IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_love = (IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_skip = (IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_ban = (IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getArtistName = (IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getAlbumName = (IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getTrackName = (IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getArtUrl = (IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getDuration = (IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_getPosition = (IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getBufferPercent = (IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_isPlaying = (IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_getStationName = (IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getStationUrl = (IBinder.FIRST_CALL_TRANSACTION + 18);
}
public void setTuner(net.roarsoftware.lastfm.Radio tuner) throws android.os.RemoteException;
public void setSession(net.roarsoftware.lastfm.Session session) throws android.os.RemoteException;
public void pause() throws android.os.RemoteException;
public void stop() throws android.os.RemoteException;
public void skipForward() throws android.os.RemoteException;
public void startRadio() throws android.os.RemoteException;
public void love() throws android.os.RemoteException;
public void skip() throws android.os.RemoteException;
public void ban() throws android.os.RemoteException;
public java.lang.String getArtistName() throws android.os.RemoteException;
public java.lang.String getAlbumName() throws android.os.RemoteException;
public java.lang.String getTrackName() throws android.os.RemoteException;
public java.lang.String getArtUrl() throws android.os.RemoteException;
public long getDuration() throws android.os.RemoteException;
public long getPosition() throws android.os.RemoteException;
public int getBufferPercent() throws android.os.RemoteException;
public boolean isPlaying() throws android.os.RemoteException;
public java.lang.String getStationName() throws android.os.RemoteException;
public java.lang.String getStationUrl() throws android.os.RemoteException;
}
