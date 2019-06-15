package com.qualcomm.qti.remoteSimlock;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteSimlockServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteSimlockServiceCallback {
        private static final String DESCRIPTOR = "com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockServiceCallback";
        static final int TRANSACTION_uimRemoteSimlockGenerateHMACResponse = 3;
        static final int TRANSACTION_uimRemoteSimlockGetSharedKeyResponse = 2;
        static final int TRANSACTION_uimRemoteSimlockGetSimlockStatusResponse = 5;
        static final int TRANSACTION_uimRemoteSimlockGetVersionResponse = 4;
        static final int TRANSACTION_uimRemoteSimlockProcessSimlockDataResponse = 1;

        private static class Proxy implements IUimRemoteSimlockServiceCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void uimRemoteSimlockProcessSimlockDataResponse(int token, int responseCode, byte[] simlockResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeByteArray(simlockResponse);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteSimlockGetSharedKeyResponse(int token, int responseCode, byte[] encryptedKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeByteArray(encryptedKey);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteSimlockGenerateHMACResponse(int token, int responseCode, byte[] hmacData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeByteArray(hmacData);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteSimlockGetVersionResponse(int token, int responseCode, int majorVersion, int minorVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeInt(majorVersion);
                    _data.writeInt(minorVersion);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimRemoteSimlockGetSimlockStatusResponse(int token, int responseCode, int unlockStatus, long unlockTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeInt(unlockStatus);
                    _data.writeLong(unlockTime);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUimRemoteSimlockServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteSimlockServiceCallback)) {
                return new Proxy(obj);
            }
            return (IUimRemoteSimlockServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            String descriptor = DESCRIPTOR;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(descriptor);
                        uimRemoteSimlockProcessSimlockDataResponse(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(descriptor);
                        uimRemoteSimlockGetSharedKeyResponse(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(descriptor);
                        uimRemoteSimlockGenerateHMACResponse(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(descriptor);
                        uimRemoteSimlockGetVersionResponse(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(descriptor);
                        uimRemoteSimlockGetSimlockStatusResponse(data.readInt(), data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            reply.writeString(descriptor);
            return true;
        }
    }

    void uimRemoteSimlockGenerateHMACResponse(int i, int i2, byte[] bArr) throws RemoteException;

    void uimRemoteSimlockGetSharedKeyResponse(int i, int i2, byte[] bArr) throws RemoteException;

    void uimRemoteSimlockGetSimlockStatusResponse(int i, int i2, int i3, long j) throws RemoteException;

    void uimRemoteSimlockGetVersionResponse(int i, int i2, int i3, int i4) throws RemoteException;

    void uimRemoteSimlockProcessSimlockDataResponse(int i, int i2, byte[] bArr) throws RemoteException;
}
