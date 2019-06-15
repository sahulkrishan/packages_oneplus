package com.oneplus.faceunlock.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOPFacelockCallback extends IInterface {

    public static abstract class Stub extends Binder implements IOPFacelockCallback {
        private static final String DESCRIPTOR = "com.oneplus.faceunlock.internal.IOPFacelockCallback";
        static final int TRANSACTION_onBeginRecognize = 1;
        static final int TRANSACTION_onCompared = 2;
        static final int TRANSACTION_onEndRecognize = 3;

        private static class Proxy implements IOPFacelockCallback {
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

            public void onBeginRecognize(int faceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faceId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCompared(int faceId, int userId, int result, int compareTimeMillis, int score) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faceId);
                    _data.writeInt(userId);
                    _data.writeInt(result);
                    _data.writeInt(compareTimeMillis);
                    _data.writeInt(score);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEndRecognize(int faceId, int userId, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faceId);
                    _data.writeInt(userId);
                    _data.writeInt(result);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOPFacelockCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOPFacelockCallback)) {
                return new Proxy(obj);
            }
            return (IOPFacelockCallback) iin;
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
                        onBeginRecognize(data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(descriptor);
                        onCompared(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        parcel.enforceInterface(descriptor);
                        onEndRecognize(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            reply.writeString(descriptor);
            return true;
        }
    }

    void onBeginRecognize(int i) throws RemoteException;

    void onCompared(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void onEndRecognize(int i, int i2, int i3) throws RemoteException;
}
