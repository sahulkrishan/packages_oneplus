package com.google.tagmanager.protobuf.nano;

import java.util.Arrays;

public final class UnknownFieldData {
    final byte[] bytes;
    final int tag;

    UnknownFieldData(int tag, byte[] bytes) {
        this.tag = tag;
        this.bytes = bytes;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof UnknownFieldData)) {
            return false;
        }
        UnknownFieldData other = (UnknownFieldData) o;
        if (!(this.tag == other.tag && Arrays.equals(this.bytes, other.bytes))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result = (31 * 17) + this.tag;
        for (byte b : this.bytes) {
            result = (31 * result) + b;
        }
        return result;
    }
}
