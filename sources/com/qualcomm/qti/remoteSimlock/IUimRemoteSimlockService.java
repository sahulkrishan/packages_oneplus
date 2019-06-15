package com.qualcomm.qti.remoteSimlock;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimRemoteSimlockService extends IInterface {

    public static abstract class Stub extends Binder implements IUimRemoteSimlockService {
        private static final String DESCRIPTOR = "com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockService";
        static final int TRANSACTION_deregisterCallback = 2;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_uimRemoteSimlockGenerateHMAC = 5;
        static final int TRANSACTION_uimRemoteSimlockGetSharedKey = 4;
        static final int TRANSACTION_uimRemoteSimlockGetSimlockStatus = 7;
        static final int TRANSACTION_uimRemoteSimlockGetVersion = 6;
        static final int TRANSACTION_uimRemoteSimlockProcessSimlockData = 3;

        private static class Proxy implements IUimRemoteSimlockService {
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

            public int registerCallback(IUimRemoteSimlockServiceCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deregisterCallback(IUimRemoteSimlockServiceCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockProcessSimlockData(int token, byte[] simlockData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeByteArray(simlockData);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGetSharedKey(int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGenerateHMAC(int token, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeByteArray(data);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGetVersion(int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimRemoteSimlockGetSimlockStatus(int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUimRemoteSimlockService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimRemoteSimlockService)) {
                return new Proxy(obj);
            }
            return (IUimRemoteSimlockService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            if (code != 1598968902) {
                int _result;
                int _result2;
                switch (code) {
                    case 1:
                        data.enforceInterface(descriptor);
                        _result = registerCallback(com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(descriptor);
                        _result = deregisterCallback(com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(descriptor);
                        _result2 = uimRemoteSimlockProcessSimlockData(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(descriptor);
                        _result = uimRemoteSimlockGetSharedKey(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(descriptor);
                        _result2 = uimRemoteSimlockGenerateHMAC(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(descriptor);
                        _result = uimRemoteSimlockGetVersion(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 7:
                        data.enforceInterface(descriptor);
                        _result = uimRemoteSimlockGetSimlockStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            reply.writeString(descriptor);
            return true;
        }
    }

    int deregisterCallback(IUimRemoteSimlockServiceCallback iUimRemoteSimlockServiceCallback) throws RemoteException;

    int registerCallback(IUimRemoteSimlockServiceCallback iUimRemoteSimlockServiceCallback) throws RemoteException;

    int uimRemoteSimlockGenerateHMAC(int i, byte[] bArr) throws RemoteException;

    int uimRemoteSimlockGetSharedKey(int i) throws RemoteException;

    int uimRemoteSimlockGetSimlockStatus(int i) throws RemoteException;

    int uimRemoteSimlockGetVersion(int i) throws RemoteException;

    int uimRemoteSimlockProcessSimlockData(int i, byte[] bArr) throws RemoteException;
}
