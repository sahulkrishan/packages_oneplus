package com.google.analytics.containertag.proto;

import com.google.analytics.midtier.proto.containertag.TypeSystem.Value;
import com.google.tagmanager.protobuf.nano.CodedInputByteBufferNano;
import com.google.tagmanager.protobuf.nano.CodedOutputByteBufferNano;
import com.google.tagmanager.protobuf.nano.ExtendableMessageNano;
import com.google.tagmanager.protobuf.nano.Extension;
import com.google.tagmanager.protobuf.nano.Extension.TypeLiteral;
import com.google.tagmanager.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.tagmanager.protobuf.nano.MessageNano;
import com.google.tagmanager.protobuf.nano.WireFormatNano;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public interface Serving {

    public interface ResourceState {
        public static final int LIVE = 2;
        public static final int PREVIEW = 1;
    }

    public interface ResourceType {
        public static final int CLEAR_CACHE = 6;
        public static final int GET_COOKIE = 5;
        public static final int JS_RESOURCE = 1;
        public static final int NS_RESOURCE = 2;
        public static final int PIXEL_COLLECTION = 3;
        public static final int RAW_PROTO = 7;
        public static final int SET_COOKIE = 4;
    }

    public static final class CacheOption extends ExtendableMessageNano {
        public static final CacheOption[] EMPTY_ARRAY = new CacheOption[0];
        public int expirationSeconds = 0;
        public int gcacheExpirationSeconds = 0;
        public int level = 1;

        public interface CacheLevel {
            public static final int NO_CACHE = 1;
            public static final int PRIVATE = 2;
            public static final int PUBLIC = 3;
        }

        public final CacheOption clear() {
            this.level = 1;
            this.expirationSeconds = 0;
            this.gcacheExpirationSeconds = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof CacheOption)) {
                return false;
            }
            CacheOption other = (CacheOption) o;
            if (!(this.level == other.level && this.expirationSeconds == other.expirationSeconds && this.gcacheExpirationSeconds == other.gcacheExpirationSeconds && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * ((31 * 17) + this.level)) + this.expirationSeconds)) + this.gcacheExpirationSeconds)) + (this.unknownFieldData == null ? 0 : this.unknownFieldData.hashCode());
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.level != 1) {
                output.writeInt32(1, this.level);
            }
            if (this.expirationSeconds != 0) {
                output.writeInt32(2, this.expirationSeconds);
            }
            if (this.gcacheExpirationSeconds != 0) {
                output.writeInt32(3, this.gcacheExpirationSeconds);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.level != 1) {
                size = 0 + CodedOutputByteBufferNano.computeInt32Size(1, this.level);
            }
            if (this.expirationSeconds != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.expirationSeconds);
            }
            if (this.gcacheExpirationSeconds != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.gcacheExpirationSeconds);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public CacheOption mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int temp = input.readInt32();
                    if (temp == 1 || temp == 2 || temp == 3) {
                        this.level = temp;
                    } else {
                        this.level = 1;
                    }
                } else if (tag == 16) {
                    this.expirationSeconds = input.readInt32();
                } else if (tag != 24) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.gcacheExpirationSeconds = input.readInt32();
                }
            }
        }

        public static CacheOption parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (CacheOption) MessageNano.mergeFrom(new CacheOption(), data);
        }

        public static CacheOption parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new CacheOption().mergeFrom(input);
        }
    }

    public static final class Container extends ExtendableMessageNano {
        public static final Container[] EMPTY_ARRAY = new Container[0];
        public String containerId = "";
        public Resource jsResource = null;
        public int state = 1;
        public String version = "";

        public final Container clear() {
            this.jsResource = null;
            this.containerId = "";
            this.state = 1;
            this.version = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Container)) {
                return false;
            }
            Container other = (Container) o;
            if (this.jsResource != null ? !this.jsResource.equals(other.jsResource) : other.jsResource != null) {
                if (this.containerId != null ? !this.containerId.equals(other.containerId) : other.containerId != null) {
                    if (this.state == other.state) {
                        if (this.version == null) {
                        }
                        if (this.unknownFieldData == null) {
                        }
                    }
                }
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * ((31 * ((31 * 17) + (this.jsResource == null ? 0 : this.jsResource.hashCode()))) + (this.containerId == null ? 0 : this.containerId.hashCode()))) + this.state)) + (this.version == null ? 0 : this.version.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.jsResource != null) {
                output.writeMessage(1, this.jsResource);
            }
            output.writeString(3, this.containerId);
            output.writeInt32(4, this.state);
            if (!this.version.equals("")) {
                output.writeString(5, this.version);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.jsResource != null) {
                size = 0 + CodedOutputByteBufferNano.computeMessageSize(1, this.jsResource);
            }
            size = (size + CodedOutputByteBufferNano.computeStringSize(3, this.containerId)) + CodedOutputByteBufferNano.computeInt32Size(4, this.state);
            if (!this.version.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(5, this.version);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public Container mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.jsResource = new Resource();
                    input.readMessage(this.jsResource);
                } else if (tag == 26) {
                    this.containerId = input.readString();
                } else if (tag == 32) {
                    int temp = input.readInt32();
                    if (temp == 1 || temp == 2) {
                        this.state = temp;
                    } else {
                        this.state = 1;
                    }
                } else if (tag != 42) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.version = input.readString();
                }
            }
        }

        public static Container parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Container) MessageNano.mergeFrom(new Container(), data);
        }

        public static Container parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Container().mergeFrom(input);
        }
    }

    public static final class FunctionCall extends ExtendableMessageNano {
        public static final FunctionCall[] EMPTY_ARRAY = new FunctionCall[0];
        public int function = 0;
        public boolean liveOnly = false;
        public int name = 0;
        public int[] property = WireFormatNano.EMPTY_INT_ARRAY;
        public boolean serverSide = false;

        public final FunctionCall clear() {
            this.property = WireFormatNano.EMPTY_INT_ARRAY;
            this.function = 0;
            this.name = 0;
            this.liveOnly = false;
            this.serverSide = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof FunctionCall)) {
                return false;
            }
            FunctionCall other = (FunctionCall) o;
            if (!(Arrays.equals(this.property, other.property) && this.function == other.function && this.name == other.name && this.liveOnly == other.liveOnly && this.serverSide == other.serverSide && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int i = 0;
            if (this.property == null) {
                result = 17 * 31;
            } else {
                int result2 = 17;
                for (int i2 : this.property) {
                    result2 = (31 * result2) + i2;
                }
                result = result2;
            }
            int i22 = 2;
            result = 31 * ((31 * ((31 * ((31 * result) + this.function)) + this.name)) + (this.liveOnly ? 1 : 2));
            if (this.serverSide) {
                i22 = 1;
            }
            int result3 = 31 * (result + i22);
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.serverSide) {
                output.writeBool(1, this.serverSide);
            }
            output.writeInt32(2, this.function);
            if (this.property != null) {
                for (int element : this.property) {
                    output.writeInt32(3, element);
                }
            }
            if (this.name != 0) {
                output.writeInt32(4, this.name);
            }
            if (this.liveOnly) {
                output.writeBool(6, this.liveOnly);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.serverSide) {
                size = 0 + CodedOutputByteBufferNano.computeBoolSize(1, this.serverSide);
            }
            size += CodedOutputByteBufferNano.computeInt32Size(2, this.function);
            if (this.property != null && this.property.length > 0) {
                int dataSize = 0;
                for (int element : this.property) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (size + dataSize) + (1 * this.property.length);
            }
            if (this.name != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.name);
            }
            if (this.liveOnly) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, this.liveOnly);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public FunctionCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.serverSide = input.readBool();
                } else if (tag == 16) {
                    this.function = input.readInt32();
                } else if (tag == 24) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                    int i = this.property.length;
                    int[] newArray = new int[(i + arrayLength)];
                    System.arraycopy(this.property, 0, newArray, 0, i);
                    this.property = newArray;
                    while (i < this.property.length - 1) {
                        this.property[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    this.property[i] = input.readInt32();
                } else if (tag == 32) {
                    this.name = input.readInt32();
                } else if (tag != 48) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.liveOnly = input.readBool();
                }
            }
        }

        public static FunctionCall parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (FunctionCall) MessageNano.mergeFrom(new FunctionCall(), data);
        }

        public static FunctionCall parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new FunctionCall().mergeFrom(input);
        }
    }

    public static final class GaExperimentRandom extends ExtendableMessageNano {
        public static final GaExperimentRandom[] EMPTY_ARRAY = new GaExperimentRandom[0];
        public String key = "";
        public long lifetimeInMilliseconds = 0;
        public long maxRandom = 2147483647L;
        public long minRandom = 0;
        public boolean retainOriginalValue = false;

        public final GaExperimentRandom clear() {
            this.key = "";
            this.minRandom = 0;
            this.maxRandom = 2147483647L;
            this.retainOriginalValue = false;
            this.lifetimeInMilliseconds = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof GaExperimentRandom)) {
                return false;
            }
            GaExperimentRandom other = (GaExperimentRandom) o;
            if (this.key != null ? !this.key.equals(other.key) : other.key != null) {
                if (this.minRandom == other.minRandom) {
                    if (this.maxRandom == other.maxRandom && this.retainOriginalValue == other.retainOriginalValue && this.lifetimeInMilliseconds == other.lifetimeInMilliseconds) {
                        if (this.unknownFieldData == null) {
                        }
                    }
                }
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * ((31 * ((31 * ((31 * 17) + (this.key == null ? 0 : this.key.hashCode()))) + ((int) (this.minRandom ^ (this.minRandom >>> 32))))) + ((int) (this.maxRandom ^ (this.maxRandom >>> 32))))) + (this.retainOriginalValue ? 1 : 2))) + ((int) (this.lifetimeInMilliseconds ^ (this.lifetimeInMilliseconds >>> 32))));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.key.equals("")) {
                output.writeString(1, this.key);
            }
            if (this.minRandom != 0) {
                output.writeInt64(2, this.minRandom);
            }
            if (this.maxRandom != 2147483647L) {
                output.writeInt64(3, this.maxRandom);
            }
            if (this.retainOriginalValue) {
                output.writeBool(4, this.retainOriginalValue);
            }
            if (this.lifetimeInMilliseconds != 0) {
                output.writeInt64(5, this.lifetimeInMilliseconds);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (!this.key.equals("")) {
                size = 0 + CodedOutputByteBufferNano.computeStringSize(1, this.key);
            }
            if (this.minRandom != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, this.minRandom);
            }
            if (this.maxRandom != 2147483647L) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, this.maxRandom);
            }
            if (this.retainOriginalValue) {
                size += CodedOutputByteBufferNano.computeBoolSize(4, this.retainOriginalValue);
            }
            if (this.lifetimeInMilliseconds != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.lifetimeInMilliseconds);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public GaExperimentRandom mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.key = input.readString();
                } else if (tag == 16) {
                    this.minRandom = input.readInt64();
                } else if (tag == 24) {
                    this.maxRandom = input.readInt64();
                } else if (tag == 32) {
                    this.retainOriginalValue = input.readBool();
                } else if (tag != 40) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.lifetimeInMilliseconds = input.readInt64();
                }
            }
        }

        public static GaExperimentRandom parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (GaExperimentRandom) MessageNano.mergeFrom(new GaExperimentRandom(), data);
        }

        public static GaExperimentRandom parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new GaExperimentRandom().mergeFrom(input);
        }
    }

    public static final class GaExperimentSupplemental extends ExtendableMessageNano {
        public static final GaExperimentSupplemental[] EMPTY_ARRAY = new GaExperimentSupplemental[0];
        public GaExperimentRandom[] experimentRandom = GaExperimentRandom.EMPTY_ARRAY;
        public Value[] valueToClear = Value.EMPTY_ARRAY;
        public Value[] valueToPush = Value.EMPTY_ARRAY;

        public final GaExperimentSupplemental clear() {
            this.valueToPush = Value.EMPTY_ARRAY;
            this.valueToClear = Value.EMPTY_ARRAY;
            this.experimentRandom = GaExperimentRandom.EMPTY_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof GaExperimentSupplemental)) {
                return false;
            }
            GaExperimentSupplemental other = (GaExperimentSupplemental) o;
            if (!(Arrays.equals(this.valueToPush, other.valueToPush) && Arrays.equals(this.valueToClear, other.valueToClear) && Arrays.equals(this.experimentRandom, other.experimentRandom) && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int result2;
            int i = 0;
            if (this.valueToPush == null) {
                result = 17 * 31;
            } else {
                result2 = 17;
                for (result = 0; result < this.valueToPush.length; result++) {
                    result2 = (31 * result2) + (this.valueToPush[result] == null ? 0 : this.valueToPush[result].hashCode());
                }
                result = result2;
            }
            if (this.valueToClear == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.valueToClear.length; result++) {
                    result2 = (31 * result2) + (this.valueToClear[result] == null ? 0 : this.valueToClear[result].hashCode());
                }
                result = result2;
            }
            if (this.experimentRandom == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.experimentRandom.length; result++) {
                    result2 = (31 * result2) + (this.experimentRandom[result] == null ? 0 : this.experimentRandom[result].hashCode());
                }
                result = result2;
            }
            int result3 = 31 * result;
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int len$;
            int i$ = 0;
            if (this.valueToPush != null) {
                for (Value element : this.valueToPush) {
                    output.writeMessage(1, element);
                }
            }
            if (this.valueToClear != null) {
                for (Value element2 : this.valueToClear) {
                    output.writeMessage(2, element2);
                }
            }
            if (this.experimentRandom != null) {
                GaExperimentRandom[] arr$ = this.experimentRandom;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeMessage(3, arr$[i$]);
                    i$++;
                }
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size;
            int size2 = 0;
            int i$ = 0;
            if (this.valueToPush != null) {
                size = 0;
                for (Value element : this.valueToPush) {
                    size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                }
                size2 = size;
            }
            if (this.valueToClear != null) {
                size = size2;
                for (Value element2 : this.valueToClear) {
                    size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                }
                size2 = size;
            }
            if (this.experimentRandom != null) {
                GaExperimentRandom[] arr$ = this.experimentRandom;
                while (i$ < arr$.length) {
                    size2 += CodedOutputByteBufferNano.computeMessageSize(3, arr$[i$]);
                    i$++;
                }
            }
            size2 += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size2;
            return size2;
        }

        public GaExperimentSupplemental mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                int arrayLength;
                int i;
                Value[] newArray;
                if (tag == 10) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    i = this.valueToPush == null ? 0 : this.valueToPush.length;
                    newArray = new Value[(i + arrayLength)];
                    if (this.valueToPush != null) {
                        System.arraycopy(this.valueToPush, 0, newArray, 0, i);
                    }
                    this.valueToPush = newArray;
                    while (i < this.valueToPush.length - 1) {
                        this.valueToPush[i] = new Value();
                        input.readMessage(this.valueToPush[i]);
                        input.readTag();
                        i++;
                    }
                    this.valueToPush[i] = new Value();
                    input.readMessage(this.valueToPush[i]);
                } else if (tag == 18) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    i = this.valueToClear == null ? 0 : this.valueToClear.length;
                    newArray = new Value[(i + arrayLength)];
                    if (this.valueToClear != null) {
                        System.arraycopy(this.valueToClear, 0, newArray, 0, i);
                    }
                    this.valueToClear = newArray;
                    while (i < this.valueToClear.length - 1) {
                        this.valueToClear[i] = new Value();
                        input.readMessage(this.valueToClear[i]);
                        input.readTag();
                        i++;
                    }
                    this.valueToClear[i] = new Value();
                    input.readMessage(this.valueToClear[i]);
                } else if (tag != 26) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    i = this.experimentRandom == null ? 0 : this.experimentRandom.length;
                    GaExperimentRandom[] newArray2 = new GaExperimentRandom[(i + arrayLength)];
                    if (this.experimentRandom != null) {
                        System.arraycopy(this.experimentRandom, 0, newArray2, 0, i);
                    }
                    this.experimentRandom = newArray2;
                    while (i < this.experimentRandom.length - 1) {
                        this.experimentRandom[i] = new GaExperimentRandom();
                        input.readMessage(this.experimentRandom[i]);
                        input.readTag();
                        i++;
                    }
                    this.experimentRandom[i] = new GaExperimentRandom();
                    input.readMessage(this.experimentRandom[i]);
                }
            }
        }

        public static GaExperimentSupplemental parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (GaExperimentSupplemental) MessageNano.mergeFrom(new GaExperimentSupplemental(), data);
        }

        public static GaExperimentSupplemental parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new GaExperimentSupplemental().mergeFrom(input);
        }
    }

    public static final class Property extends ExtendableMessageNano {
        public static final Property[] EMPTY_ARRAY = new Property[0];
        public int key = 0;
        public int value = 0;

        public final Property clear() {
            this.key = 0;
            this.value = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Property)) {
                return false;
            }
            Property other = (Property) o;
            if (!(this.key == other.key && this.value == other.value && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * 17) + this.key)) + this.value)) + (this.unknownFieldData == null ? 0 : this.unknownFieldData.hashCode());
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            output.writeInt32(1, this.key);
            output.writeInt32(2, this.value);
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = ((0 + CodedOutputByteBufferNano.computeInt32Size(1, this.key)) + CodedOutputByteBufferNano.computeInt32Size(2, this.value)) + WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public Property mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.key = input.readInt32();
                } else if (tag != 16) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.value = input.readInt32();
                }
            }
        }

        public static Property parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Property) MessageNano.mergeFrom(new Property(), data);
        }

        public static Property parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Property().mergeFrom(input);
        }
    }

    public static final class Resource extends ExtendableMessageNano {
        public static final Resource[] EMPTY_ARRAY = new Resource[0];
        private static final String TEMPLATE_VERSION_SET_DEFAULT = "0";
        public String[] key = WireFormatNano.EMPTY_STRING_ARRAY;
        public CacheOption liveJsCacheOption = null;
        public FunctionCall[] macro = FunctionCall.EMPTY_ARRAY;
        public String malwareScanAuthCode = "";
        public boolean oBSOLETEEnableAutoEventTracking = false;
        public FunctionCall[] predicate = FunctionCall.EMPTY_ARRAY;
        public String previewAuthCode = "";
        public Property[] property = Property.EMPTY_ARRAY;
        public float reportingSampleRate = 0.0f;
        public int resourceFormatVersion = 0;
        public Rule[] rule = Rule.EMPTY_ARRAY;
        public String[] supplemental = WireFormatNano.EMPTY_STRING_ARRAY;
        public FunctionCall[] tag = FunctionCall.EMPTY_ARRAY;
        public String templateVersionSet = TEMPLATE_VERSION_SET_DEFAULT;
        public String[] usageContext = WireFormatNano.EMPTY_STRING_ARRAY;
        public Value[] value = Value.EMPTY_ARRAY;
        public String version = "";

        public final Resource clear() {
            this.supplemental = WireFormatNano.EMPTY_STRING_ARRAY;
            this.key = WireFormatNano.EMPTY_STRING_ARRAY;
            this.value = Value.EMPTY_ARRAY;
            this.property = Property.EMPTY_ARRAY;
            this.macro = FunctionCall.EMPTY_ARRAY;
            this.tag = FunctionCall.EMPTY_ARRAY;
            this.predicate = FunctionCall.EMPTY_ARRAY;
            this.rule = Rule.EMPTY_ARRAY;
            this.previewAuthCode = "";
            this.malwareScanAuthCode = "";
            this.templateVersionSet = TEMPLATE_VERSION_SET_DEFAULT;
            this.version = "";
            this.liveJsCacheOption = null;
            this.reportingSampleRate = 0.0f;
            this.oBSOLETEEnableAutoEventTracking = false;
            this.usageContext = WireFormatNano.EMPTY_STRING_ARRAY;
            this.resourceFormatVersion = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Resource)) {
                return false;
            }
            Resource other = (Resource) o;
            if (!(Arrays.equals(this.supplemental, other.supplemental) && Arrays.equals(this.key, other.key) && Arrays.equals(this.value, other.value) && Arrays.equals(this.property, other.property) && Arrays.equals(this.macro, other.macro) && Arrays.equals(this.tag, other.tag) && Arrays.equals(this.predicate, other.predicate) && Arrays.equals(this.rule, other.rule) && (!this.previewAuthCode != null ? other.previewAuthCode != null : !this.previewAuthCode.equals(other.previewAuthCode)) && (!this.malwareScanAuthCode != null ? other.malwareScanAuthCode != null : !this.malwareScanAuthCode.equals(other.malwareScanAuthCode)) && (!this.templateVersionSet != null ? other.templateVersionSet != null : !this.templateVersionSet.equals(other.templateVersionSet)) && (!this.version != null ? other.version != null : !this.version.equals(other.version)) && (!this.liveJsCacheOption != null ? other.liveJsCacheOption != null : !this.liveJsCacheOption.equals(other.liveJsCacheOption)) && this.reportingSampleRate == other.reportingSampleRate && this.oBSOLETEEnableAutoEventTracking == other.oBSOLETEEnableAutoEventTracking && Arrays.equals(this.usageContext, other.usageContext) && this.resourceFormatVersion == other.resourceFormatVersion && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int result2;
            int i = 0;
            if (this.supplemental == null) {
                result = 17 * 31;
            } else {
                result2 = 17;
                for (result = 0; result < this.supplemental.length; result++) {
                    result2 = (31 * result2) + (this.supplemental[result] == null ? 0 : this.supplemental[result].hashCode());
                }
                result = result2;
            }
            if (this.key == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.key.length; result++) {
                    result2 = (31 * result2) + (this.key[result] == null ? 0 : this.key[result].hashCode());
                }
                result = result2;
            }
            if (this.value == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.value.length; result++) {
                    result2 = (31 * result2) + (this.value[result] == null ? 0 : this.value[result].hashCode());
                }
                result = result2;
            }
            if (this.property == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.property.length; result++) {
                    result2 = (31 * result2) + (this.property[result] == null ? 0 : this.property[result].hashCode());
                }
                result = result2;
            }
            if (this.macro == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.macro.length; result++) {
                    result2 = (31 * result2) + (this.macro[result] == null ? 0 : this.macro[result].hashCode());
                }
                result = result2;
            }
            if (this.tag == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.tag.length; result++) {
                    result2 = (31 * result2) + (this.tag[result] == null ? 0 : this.tag[result].hashCode());
                }
                result = result2;
            }
            if (this.predicate == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.predicate.length; result++) {
                    result2 = (31 * result2) + (this.predicate[result] == null ? 0 : this.predicate[result].hashCode());
                }
                result = result2;
            }
            if (this.rule == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.rule.length; result++) {
                    result2 = (31 * result2) + (this.rule[result] == null ? 0 : this.rule[result].hashCode());
                }
                result = result2;
            }
            result2 = (31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * result) + (this.previewAuthCode == null ? 0 : this.previewAuthCode.hashCode()))) + (this.malwareScanAuthCode == null ? 0 : this.malwareScanAuthCode.hashCode()))) + (this.templateVersionSet == null ? 0 : this.templateVersionSet.hashCode()))) + (this.version == null ? 0 : this.version.hashCode()))) + (this.liveJsCacheOption == null ? 0 : this.liveJsCacheOption.hashCode()))) + Float.floatToIntBits(this.reportingSampleRate))) + (this.oBSOLETEEnableAutoEventTracking ? 1 : 2);
            if (this.usageContext == null) {
                result2 *= 31;
            } else {
                for (result = 0; result < this.usageContext.length; result++) {
                    result2 = (31 * result2) + (this.usageContext[result] == null ? 0 : this.usageContext[result].hashCode());
                }
            }
            int result3 = 31 * ((31 * result2) + this.resourceFormatVersion);
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int len$;
            int i$ = 0;
            if (this.key != null) {
                for (String element : this.key) {
                    output.writeString(1, element);
                }
            }
            if (this.value != null) {
                for (Value element2 : this.value) {
                    output.writeMessage(2, element2);
                }
            }
            if (this.property != null) {
                for (Property element3 : this.property) {
                    output.writeMessage(3, element3);
                }
            }
            if (this.macro != null) {
                for (FunctionCall element4 : this.macro) {
                    output.writeMessage(4, element4);
                }
            }
            if (this.tag != null) {
                for (FunctionCall element42 : this.tag) {
                    output.writeMessage(5, element42);
                }
            }
            if (this.predicate != null) {
                for (FunctionCall element422 : this.predicate) {
                    output.writeMessage(6, element422);
                }
            }
            if (this.rule != null) {
                for (Rule element5 : this.rule) {
                    output.writeMessage(7, element5);
                }
            }
            if (!this.previewAuthCode.equals("")) {
                output.writeString(9, this.previewAuthCode);
            }
            if (!this.malwareScanAuthCode.equals("")) {
                output.writeString(10, this.malwareScanAuthCode);
            }
            if (!this.templateVersionSet.equals(TEMPLATE_VERSION_SET_DEFAULT)) {
                output.writeString(12, this.templateVersionSet);
            }
            if (!this.version.equals("")) {
                output.writeString(13, this.version);
            }
            if (this.liveJsCacheOption != null) {
                output.writeMessage(14, this.liveJsCacheOption);
            }
            if (this.reportingSampleRate != 0.0f) {
                output.writeFloat(15, this.reportingSampleRate);
            }
            if (this.usageContext != null) {
                for (String element6 : this.usageContext) {
                    output.writeString(16, element6);
                }
            }
            if (this.resourceFormatVersion != 0) {
                output.writeInt32(17, this.resourceFormatVersion);
            }
            if (this.oBSOLETEEnableAutoEventTracking) {
                output.writeBool(18, this.oBSOLETEEnableAutoEventTracking);
            }
            if (this.supplemental != null) {
                String[] arr$ = this.supplemental;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeString(19, arr$[i$]);
                    i$++;
                }
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int dataSize;
            int i$;
            int size = 0;
            int i$2 = 0;
            if (this.key != null && this.key.length > 0) {
                dataSize = 0;
                for (String element : this.key) {
                    dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                }
                size = (0 + dataSize) + (1 * this.key.length);
            }
            if (this.value != null) {
                dataSize = size;
                for (Value element2 : this.value) {
                    dataSize += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                }
                size = dataSize;
            }
            if (this.property != null) {
                dataSize = size;
                for (Property element3 : this.property) {
                    dataSize += CodedOutputByteBufferNano.computeMessageSize(3, element3);
                }
                size = dataSize;
            }
            if (this.macro != null) {
                dataSize = size;
                for (FunctionCall element4 : this.macro) {
                    dataSize += CodedOutputByteBufferNano.computeMessageSize(4, element4);
                }
                size = dataSize;
            }
            if (this.tag != null) {
                dataSize = size;
                for (FunctionCall element42 : this.tag) {
                    dataSize += CodedOutputByteBufferNano.computeMessageSize(5, element42);
                }
                size = dataSize;
            }
            if (this.predicate != null) {
                dataSize = size;
                for (FunctionCall element422 : this.predicate) {
                    dataSize += CodedOutputByteBufferNano.computeMessageSize(6, element422);
                }
                size = dataSize;
            }
            if (this.rule != null) {
                dataSize = size;
                for (Rule element5 : this.rule) {
                    dataSize += CodedOutputByteBufferNano.computeMessageSize(7, element5);
                }
                size = dataSize;
            }
            if (!this.previewAuthCode.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(9, this.previewAuthCode);
            }
            if (!this.malwareScanAuthCode.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(10, this.malwareScanAuthCode);
            }
            if (!this.templateVersionSet.equals(TEMPLATE_VERSION_SET_DEFAULT)) {
                size += CodedOutputByteBufferNano.computeStringSize(12, this.templateVersionSet);
            }
            if (!this.version.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(13, this.version);
            }
            if (this.liveJsCacheOption != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(14, this.liveJsCacheOption);
            }
            if (this.reportingSampleRate != 0.0f) {
                size += CodedOutputByteBufferNano.computeFloatSize(15, this.reportingSampleRate);
            }
            if (this.usageContext != null && this.usageContext.length > 0) {
                int dataSize2 = 0;
                for (String element6 : this.usageContext) {
                    dataSize2 += CodedOutputByteBufferNano.computeStringSizeNoTag(element6);
                }
                size = (size + dataSize2) + (this.usageContext.length * 2);
            }
            if (this.resourceFormatVersion != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(17, this.resourceFormatVersion);
            }
            if (this.oBSOLETEEnableAutoEventTracking) {
                size += CodedOutputByteBufferNano.computeBoolSize(18, this.oBSOLETEEnableAutoEventTracking);
            }
            if (this.supplemental != null && this.supplemental.length > 0) {
                i$ = 0;
                String[] arr$ = this.supplemental;
                while (i$2 < arr$.length) {
                    i$ += CodedOutputByteBufferNano.computeStringSizeNoTag(arr$[i$2]);
                    i$2++;
                }
                size = (size + i$) + (2 * this.supplemental.length);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public Resource mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                String[] newArray;
                FunctionCall[] newArray2;
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        i = this.key.length;
                        newArray = new String[(i + arrayLength)];
                        System.arraycopy(this.key, 0, newArray, 0, i);
                        this.key = newArray;
                        while (i < this.key.length - 1) {
                            this.key[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        this.key[i] = input.readString();
                        break;
                    case 18:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        i = this.value == null ? 0 : this.value.length;
                        Value[] newArray3 = new Value[(i + arrayLength)];
                        if (this.value != null) {
                            System.arraycopy(this.value, 0, newArray3, 0, i);
                        }
                        this.value = newArray3;
                        while (i < this.value.length - 1) {
                            this.value[i] = new Value();
                            input.readMessage(this.value[i]);
                            input.readTag();
                            i++;
                        }
                        this.value[i] = new Value();
                        input.readMessage(this.value[i]);
                        break;
                    case 26:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        i = this.property == null ? 0 : this.property.length;
                        Property[] newArray4 = new Property[(i + arrayLength)];
                        if (this.property != null) {
                            System.arraycopy(this.property, 0, newArray4, 0, i);
                        }
                        this.property = newArray4;
                        while (i < this.property.length - 1) {
                            this.property[i] = new Property();
                            input.readMessage(this.property[i]);
                            input.readTag();
                            i++;
                        }
                        this.property[i] = new Property();
                        input.readMessage(this.property[i]);
                        break;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        i = this.macro == null ? 0 : this.macro.length;
                        newArray2 = new FunctionCall[(i + arrayLength)];
                        if (this.macro != null) {
                            System.arraycopy(this.macro, 0, newArray2, 0, i);
                        }
                        this.macro = newArray2;
                        while (i < this.macro.length - 1) {
                            this.macro[i] = new FunctionCall();
                            input.readMessage(this.macro[i]);
                            input.readTag();
                            i++;
                        }
                        this.macro[i] = new FunctionCall();
                        input.readMessage(this.macro[i]);
                        break;
                    case 42:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        i = this.tag == null ? 0 : this.tag.length;
                        newArray2 = new FunctionCall[(i + arrayLength)];
                        if (this.tag != null) {
                            System.arraycopy(this.tag, 0, newArray2, 0, i);
                        }
                        this.tag = newArray2;
                        while (i < this.tag.length - 1) {
                            this.tag[i] = new FunctionCall();
                            input.readMessage(this.tag[i]);
                            input.readTag();
                            i++;
                        }
                        this.tag[i] = new FunctionCall();
                        input.readMessage(this.tag[i]);
                        break;
                    case 50:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                        i = this.predicate == null ? 0 : this.predicate.length;
                        newArray2 = new FunctionCall[(i + arrayLength)];
                        if (this.predicate != null) {
                            System.arraycopy(this.predicate, 0, newArray2, 0, i);
                        }
                        this.predicate = newArray2;
                        while (i < this.predicate.length - 1) {
                            this.predicate[i] = new FunctionCall();
                            input.readMessage(this.predicate[i]);
                            input.readTag();
                            i++;
                        }
                        this.predicate[i] = new FunctionCall();
                        input.readMessage(this.predicate[i]);
                        break;
                    case 58:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                        i = this.rule == null ? 0 : this.rule.length;
                        Rule[] newArray5 = new Rule[(i + arrayLength)];
                        if (this.rule != null) {
                            System.arraycopy(this.rule, 0, newArray5, 0, i);
                        }
                        this.rule = newArray5;
                        while (i < this.rule.length - 1) {
                            this.rule[i] = new Rule();
                            input.readMessage(this.rule[i]);
                            input.readTag();
                            i++;
                        }
                        this.rule[i] = new Rule();
                        input.readMessage(this.rule[i]);
                        break;
                    case 74:
                        this.previewAuthCode = input.readString();
                        break;
                    case 82:
                        this.malwareScanAuthCode = input.readString();
                        break;
                    case 98:
                        this.templateVersionSet = input.readString();
                        break;
                    case 106:
                        this.version = input.readString();
                        break;
                    case 114:
                        this.liveJsCacheOption = new CacheOption();
                        input.readMessage(this.liveJsCacheOption);
                        break;
                    case 125:
                        this.reportingSampleRate = input.readFloat();
                        break;
                    case 130:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 130);
                        i = this.usageContext.length;
                        newArray = new String[(i + arrayLength)];
                        System.arraycopy(this.usageContext, 0, newArray, 0, i);
                        this.usageContext = newArray;
                        while (i < this.usageContext.length - 1) {
                            this.usageContext[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        this.usageContext[i] = input.readString();
                        break;
                    case 136:
                        this.resourceFormatVersion = input.readInt32();
                        break;
                    case Const.CODE_C1_SPA /*144*/:
                        this.oBSOLETEEnableAutoEventTracking = input.readBool();
                        break;
                    case Const.CODE_C1_DF2 /*154*/:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, Const.CODE_C1_DF2);
                        i = this.supplemental.length;
                        newArray = new String[(i + arrayLength)];
                        System.arraycopy(this.supplemental, 0, newArray, 0, i);
                        this.supplemental = newArray;
                        while (i < this.supplemental.length - 1) {
                            this.supplemental[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        this.supplemental[i] = input.readString();
                        break;
                    default:
                        if (this.unknownFieldData == null) {
                            this.unknownFieldData = new ArrayList();
                        }
                        if (WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static Resource parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Resource) MessageNano.mergeFrom(new Resource(), data);
        }

        public static Resource parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Resource().mergeFrom(input);
        }
    }

    public static final class Rule extends ExtendableMessageNano {
        public static final Rule[] EMPTY_ARRAY = new Rule[0];
        public int[] addMacro = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] addMacroRuleName = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] addTag = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] addTagRuleName = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] negativePredicate = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] positivePredicate = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] removeMacro = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] removeMacroRuleName = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] removeTag = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] removeTagRuleName = WireFormatNano.EMPTY_INT_ARRAY;

        public final Rule clear() {
            this.positivePredicate = WireFormatNano.EMPTY_INT_ARRAY;
            this.negativePredicate = WireFormatNano.EMPTY_INT_ARRAY;
            this.addTag = WireFormatNano.EMPTY_INT_ARRAY;
            this.removeTag = WireFormatNano.EMPTY_INT_ARRAY;
            this.addTagRuleName = WireFormatNano.EMPTY_INT_ARRAY;
            this.removeTagRuleName = WireFormatNano.EMPTY_INT_ARRAY;
            this.addMacro = WireFormatNano.EMPTY_INT_ARRAY;
            this.removeMacro = WireFormatNano.EMPTY_INT_ARRAY;
            this.addMacroRuleName = WireFormatNano.EMPTY_INT_ARRAY;
            this.removeMacroRuleName = WireFormatNano.EMPTY_INT_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Rule)) {
                return false;
            }
            Rule other = (Rule) o;
            if (!(Arrays.equals(this.positivePredicate, other.positivePredicate) && Arrays.equals(this.negativePredicate, other.negativePredicate) && Arrays.equals(this.addTag, other.addTag) && Arrays.equals(this.removeTag, other.removeTag) && Arrays.equals(this.addTagRuleName, other.addTagRuleName) && Arrays.equals(this.removeTagRuleName, other.removeTagRuleName) && Arrays.equals(this.addMacro, other.addMacro) && Arrays.equals(this.removeMacro, other.removeMacro) && Arrays.equals(this.addMacroRuleName, other.addMacroRuleName) && Arrays.equals(this.removeMacroRuleName, other.removeMacroRuleName) && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int result2;
            int i = 0;
            if (this.positivePredicate == null) {
                result = 17 * 31;
            } else {
                result2 = 17;
                for (int i2 : this.positivePredicate) {
                    result2 = (31 * result2) + i2;
                }
                result = result2;
            }
            if (this.negativePredicate == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i22 : this.negativePredicate) {
                    result2 = (31 * result2) + i22;
                }
                result = result2;
            }
            if (this.addTag == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i222 : this.addTag) {
                    result2 = (31 * result2) + i222;
                }
                result = result2;
            }
            if (this.removeTag == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i2222 : this.removeTag) {
                    result2 = (31 * result2) + i2222;
                }
                result = result2;
            }
            if (this.addTagRuleName == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i22222 : this.addTagRuleName) {
                    result2 = (31 * result2) + i22222;
                }
                result = result2;
            }
            if (this.removeTagRuleName == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i222222 : this.removeTagRuleName) {
                    result2 = (31 * result2) + i222222;
                }
                result = result2;
            }
            if (this.addMacro == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i2222222 : this.addMacro) {
                    result2 = (31 * result2) + i2222222;
                }
                result = result2;
            }
            if (this.removeMacro == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i22222222 : this.removeMacro) {
                    result2 = (31 * result2) + i22222222;
                }
                result = result2;
            }
            if (this.addMacroRuleName == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i222222222 : this.addMacroRuleName) {
                    result2 = (31 * result2) + i222222222;
                }
                result = result2;
            }
            if (this.removeMacroRuleName == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i2222222222 : this.removeMacroRuleName) {
                    result2 = (31 * result2) + i2222222222;
                }
                result = result2;
            }
            int result3 = 31 * result;
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int len$;
            int i$ = 0;
            if (this.positivePredicate != null) {
                for (int element : this.positivePredicate) {
                    output.writeInt32(1, element);
                }
            }
            if (this.negativePredicate != null) {
                for (int element2 : this.negativePredicate) {
                    output.writeInt32(2, element2);
                }
            }
            if (this.addTag != null) {
                for (int element22 : this.addTag) {
                    output.writeInt32(3, element22);
                }
            }
            if (this.removeTag != null) {
                for (int element222 : this.removeTag) {
                    output.writeInt32(4, element222);
                }
            }
            if (this.addTagRuleName != null) {
                for (int element2222 : this.addTagRuleName) {
                    output.writeInt32(5, element2222);
                }
            }
            if (this.removeTagRuleName != null) {
                for (int element22222 : this.removeTagRuleName) {
                    output.writeInt32(6, element22222);
                }
            }
            if (this.addMacro != null) {
                for (int element222222 : this.addMacro) {
                    output.writeInt32(7, element222222);
                }
            }
            if (this.removeMacro != null) {
                for (int element2222222 : this.removeMacro) {
                    output.writeInt32(8, element2222222);
                }
            }
            if (this.addMacroRuleName != null) {
                for (int element22222222 : this.addMacroRuleName) {
                    output.writeInt32(9, element22222222);
                }
            }
            if (this.removeMacroRuleName != null) {
                int[] arr$ = this.removeMacroRuleName;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeInt32(10, arr$[i$]);
                    i$++;
                }
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int dataSize;
            int i$;
            int size = 0;
            int i$2 = 0;
            if (this.positivePredicate != null && this.positivePredicate.length > 0) {
                dataSize = 0;
                for (int element : this.positivePredicate) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (0 + dataSize) + (this.positivePredicate.length * 1);
            }
            if (this.negativePredicate != null && this.negativePredicate.length > 0) {
                dataSize = 0;
                for (int element2 : this.negativePredicate) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                }
                size = (size + dataSize) + (this.negativePredicate.length * 1);
            }
            if (this.addTag != null && this.addTag.length > 0) {
                dataSize = 0;
                for (int element22 : this.addTag) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element22);
                }
                size = (size + dataSize) + (this.addTag.length * 1);
            }
            if (this.removeTag != null && this.removeTag.length > 0) {
                dataSize = 0;
                for (int element222 : this.removeTag) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element222);
                }
                size = (size + dataSize) + (this.removeTag.length * 1);
            }
            if (this.addTagRuleName != null && this.addTagRuleName.length > 0) {
                dataSize = 0;
                for (int element2222 : this.addTagRuleName) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2222);
                }
                size = (size + dataSize) + (this.addTagRuleName.length * 1);
            }
            if (this.removeTagRuleName != null && this.removeTagRuleName.length > 0) {
                dataSize = 0;
                for (int element22222 : this.removeTagRuleName) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element22222);
                }
                size = (size + dataSize) + (this.removeTagRuleName.length * 1);
            }
            if (this.addMacro != null && this.addMacro.length > 0) {
                dataSize = 0;
                for (int element222222 : this.addMacro) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element222222);
                }
                size = (size + dataSize) + (this.addMacro.length * 1);
            }
            if (this.removeMacro != null && this.removeMacro.length > 0) {
                dataSize = 0;
                for (int element2222222 : this.removeMacro) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2222222);
                }
                size = (size + dataSize) + (this.removeMacro.length * 1);
            }
            if (this.addMacroRuleName != null && this.addMacroRuleName.length > 0) {
                dataSize = 0;
                for (int element22222222 : this.addMacroRuleName) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element22222222);
                }
                size = (size + dataSize) + (this.addMacroRuleName.length * 1);
            }
            if (this.removeMacroRuleName != null && this.removeMacroRuleName.length > 0) {
                i$ = 0;
                int[] arr$ = this.removeMacroRuleName;
                while (i$2 < arr$.length) {
                    i$ += CodedOutputByteBufferNano.computeInt32SizeNoTag(arr$[i$2]);
                    i$2++;
                }
                size = (size + i$) + (1 * this.removeMacroRuleName.length);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public Rule mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 8);
                        i = this.positivePredicate.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.positivePredicate, 0, newArray, 0, i);
                        this.positivePredicate = newArray;
                        while (i < this.positivePredicate.length - 1) {
                            this.positivePredicate[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.positivePredicate[i] = input.readInt32();
                        break;
                    case 16:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                        i = this.negativePredicate.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.negativePredicate, 0, newArray, 0, i);
                        this.negativePredicate = newArray;
                        while (i < this.negativePredicate.length - 1) {
                            this.negativePredicate[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.negativePredicate[i] = input.readInt32();
                        break;
                    case 24:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                        i = this.addTag.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.addTag, 0, newArray, 0, i);
                        this.addTag = newArray;
                        while (i < this.addTag.length - 1) {
                            this.addTag[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.addTag[i] = input.readInt32();
                        break;
                    case 32:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 32);
                        i = this.removeTag.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.removeTag, 0, newArray, 0, i);
                        this.removeTag = newArray;
                        while (i < this.removeTag.length - 1) {
                            this.removeTag[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.removeTag[i] = input.readInt32();
                        break;
                    case 40:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 40);
                        i = this.addTagRuleName.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.addTagRuleName, 0, newArray, 0, i);
                        this.addTagRuleName = newArray;
                        while (i < this.addTagRuleName.length - 1) {
                            this.addTagRuleName[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.addTagRuleName[i] = input.readInt32();
                        break;
                    case 48:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 48);
                        i = this.removeTagRuleName.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.removeTagRuleName, 0, newArray, 0, i);
                        this.removeTagRuleName = newArray;
                        while (i < this.removeTagRuleName.length - 1) {
                            this.removeTagRuleName[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.removeTagRuleName[i] = input.readInt32();
                        break;
                    case 56:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 56);
                        i = this.addMacro.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.addMacro, 0, newArray, 0, i);
                        this.addMacro = newArray;
                        while (i < this.addMacro.length - 1) {
                            this.addMacro[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.addMacro[i] = input.readInt32();
                        break;
                    case 64:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 64);
                        i = this.removeMacro.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.removeMacro, 0, newArray, 0, i);
                        this.removeMacro = newArray;
                        while (i < this.removeMacro.length - 1) {
                            this.removeMacro[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.removeMacro[i] = input.readInt32();
                        break;
                    case 72:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 72);
                        i = this.addMacroRuleName.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.addMacroRuleName, 0, newArray, 0, i);
                        this.addMacroRuleName = newArray;
                        while (i < this.addMacroRuleName.length - 1) {
                            this.addMacroRuleName[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.addMacroRuleName[i] = input.readInt32();
                        break;
                    case 80:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 80);
                        i = this.removeMacroRuleName.length;
                        newArray = new int[(i + arrayLength)];
                        System.arraycopy(this.removeMacroRuleName, 0, newArray, 0, i);
                        this.removeMacroRuleName = newArray;
                        while (i < this.removeMacroRuleName.length - 1) {
                            this.removeMacroRuleName[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.removeMacroRuleName[i] = input.readInt32();
                        break;
                    default:
                        if (this.unknownFieldData == null) {
                            this.unknownFieldData = new ArrayList();
                        }
                        if (WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                            break;
                        }
                        return this;
                }
            }
        }

        public static Rule parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Rule) MessageNano.mergeFrom(new Rule(), data);
        }

        public static Rule parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Rule().mergeFrom(input);
        }
    }

    public static final class ServingValue extends ExtendableMessageNano {
        public static final ServingValue[] EMPTY_ARRAY = new ServingValue[0];
        public static final Extension<ServingValue> ext = Extension.create(101, new TypeLiteral<ServingValue>() {
        });
        public int[] listItem = WireFormatNano.EMPTY_INT_ARRAY;
        public int macroNameReference = 0;
        public int macroReference = 0;
        public int[] mapKey = WireFormatNano.EMPTY_INT_ARRAY;
        public int[] mapValue = WireFormatNano.EMPTY_INT_ARRAY;
        public int tagReference = 0;
        public int[] templateToken = WireFormatNano.EMPTY_INT_ARRAY;

        public final ServingValue clear() {
            this.listItem = WireFormatNano.EMPTY_INT_ARRAY;
            this.mapKey = WireFormatNano.EMPTY_INT_ARRAY;
            this.mapValue = WireFormatNano.EMPTY_INT_ARRAY;
            this.macroReference = 0;
            this.templateToken = WireFormatNano.EMPTY_INT_ARRAY;
            this.macroNameReference = 0;
            this.tagReference = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof ServingValue)) {
                return false;
            }
            ServingValue other = (ServingValue) o;
            if (!(Arrays.equals(this.listItem, other.listItem) && Arrays.equals(this.mapKey, other.mapKey) && Arrays.equals(this.mapValue, other.mapValue) && this.macroReference == other.macroReference && Arrays.equals(this.templateToken, other.templateToken) && this.macroNameReference == other.macroNameReference && this.tagReference == other.tagReference && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int result2;
            int i = 0;
            if (this.listItem == null) {
                result = 17 * 31;
            } else {
                result2 = 17;
                for (int i2 : this.listItem) {
                    result2 = (31 * result2) + i2;
                }
                result = result2;
            }
            if (this.mapKey == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i22 : this.mapKey) {
                    result2 = (31 * result2) + i22;
                }
                result = result2;
            }
            if (this.mapValue == null) {
                result *= 31;
            } else {
                result2 = result;
                for (int i222 : this.mapValue) {
                    result2 = (31 * result2) + i222;
                }
                result = result2;
            }
            result2 = (31 * result) + this.macroReference;
            if (this.templateToken == null) {
                result2 *= 31;
            } else {
                for (int i2222 : this.templateToken) {
                    result2 = (31 * result2) + i2222;
                }
            }
            int result3 = 31 * ((31 * ((31 * result2) + this.macroNameReference)) + this.tagReference);
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int len$;
            int i$ = 0;
            if (this.listItem != null) {
                for (int element : this.listItem) {
                    output.writeInt32(1, element);
                }
            }
            if (this.mapKey != null) {
                for (int element2 : this.mapKey) {
                    output.writeInt32(2, element2);
                }
            }
            if (this.mapValue != null) {
                for (int element22 : this.mapValue) {
                    output.writeInt32(3, element22);
                }
            }
            if (this.macroReference != 0) {
                output.writeInt32(4, this.macroReference);
            }
            if (this.templateToken != null) {
                int[] arr$ = this.templateToken;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeInt32(5, arr$[i$]);
                    i$++;
                }
            }
            if (this.macroNameReference != 0) {
                output.writeInt32(6, this.macroNameReference);
            }
            if (this.tagReference != 0) {
                output.writeInt32(7, this.tagReference);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int dataSize;
            int i$;
            int size = 0;
            int i$2 = 0;
            if (this.listItem != null && this.listItem.length > 0) {
                dataSize = 0;
                for (int element : this.listItem) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (0 + dataSize) + (this.listItem.length * 1);
            }
            if (this.mapKey != null && this.mapKey.length > 0) {
                dataSize = 0;
                for (int element2 : this.mapKey) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                }
                size = (size + dataSize) + (this.mapKey.length * 1);
            }
            if (this.mapValue != null && this.mapValue.length > 0) {
                dataSize = 0;
                for (int element22 : this.mapValue) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element22);
                }
                size = (size + dataSize) + (this.mapValue.length * 1);
            }
            if (this.macroReference != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.macroReference);
            }
            if (this.templateToken != null && this.templateToken.length > 0) {
                i$ = 0;
                int[] arr$ = this.templateToken;
                while (i$2 < arr$.length) {
                    i$ += CodedOutputByteBufferNano.computeInt32SizeNoTag(arr$[i$2]);
                    i$2++;
                }
                size = (size + i$) + (1 * this.templateToken.length);
            }
            if (this.macroNameReference != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.macroNameReference);
            }
            if (this.tagReference != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.tagReference);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public ServingValue mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                int arrayLength;
                int i;
                int[] newArray;
                if (tag == 8) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 8);
                    i = this.listItem.length;
                    newArray = new int[(i + arrayLength)];
                    System.arraycopy(this.listItem, 0, newArray, 0, i);
                    this.listItem = newArray;
                    while (i < this.listItem.length - 1) {
                        this.listItem[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    this.listItem[i] = input.readInt32();
                } else if (tag == 16) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                    i = this.mapKey.length;
                    newArray = new int[(i + arrayLength)];
                    System.arraycopy(this.mapKey, 0, newArray, 0, i);
                    this.mapKey = newArray;
                    while (i < this.mapKey.length - 1) {
                        this.mapKey[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    this.mapKey[i] = input.readInt32();
                } else if (tag == 24) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 24);
                    i = this.mapValue.length;
                    newArray = new int[(i + arrayLength)];
                    System.arraycopy(this.mapValue, 0, newArray, 0, i);
                    this.mapValue = newArray;
                    while (i < this.mapValue.length - 1) {
                        this.mapValue[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    this.mapValue[i] = input.readInt32();
                } else if (tag == 32) {
                    this.macroReference = input.readInt32();
                } else if (tag == 40) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 40);
                    i = this.templateToken.length;
                    newArray = new int[(i + arrayLength)];
                    System.arraycopy(this.templateToken, 0, newArray, 0, i);
                    this.templateToken = newArray;
                    while (i < this.templateToken.length - 1) {
                        this.templateToken[i] = input.readInt32();
                        input.readTag();
                        i++;
                    }
                    this.templateToken[i] = input.readInt32();
                } else if (tag == 48) {
                    this.macroNameReference = input.readInt32();
                } else if (tag != 56) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.tagReference = input.readInt32();
                }
            }
        }

        public static ServingValue parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ServingValue) MessageNano.mergeFrom(new ServingValue(), data);
        }

        public static ServingValue parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ServingValue().mergeFrom(input);
        }
    }

    public static final class Supplemental extends ExtendableMessageNano {
        public static final Supplemental[] EMPTY_ARRAY = new Supplemental[0];
        public GaExperimentSupplemental experimentSupplemental = null;
        public String name = "";
        public Value value = null;

        public final Supplemental clear() {
            this.name = "";
            this.value = null;
            this.experimentSupplemental = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Supplemental)) {
                return false;
            }
            Supplemental other = (Supplemental) o;
            if (this.name != null ? !this.name.equals(other.name) : other.name != null) {
                if (this.value != null ? !this.value.equals(other.value) : other.value != null) {
                    if (this.experimentSupplemental != null ? !this.experimentSupplemental.equals(other.experimentSupplemental) : other.experimentSupplemental != null) {
                        if (this.unknownFieldData != null) {
                        }
                    }
                }
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * ((31 * 17) + (this.name == null ? 0 : this.name.hashCode()))) + (this.value == null ? 0 : this.value.hashCode()))) + (this.experimentSupplemental == null ? 0 : this.experimentSupplemental.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.name.equals("")) {
                output.writeString(1, this.name);
            }
            if (this.value != null) {
                output.writeMessage(2, this.value);
            }
            if (this.experimentSupplemental != null) {
                output.writeMessage(3, this.experimentSupplemental);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (!this.name.equals("")) {
                size = 0 + CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.value != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.value);
            }
            if (this.experimentSupplemental != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, this.experimentSupplemental);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public Supplemental mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.name = input.readString();
                } else if (tag == 18) {
                    this.value = new Value();
                    input.readMessage(this.value);
                } else if (tag != 26) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.experimentSupplemental = new GaExperimentSupplemental();
                    input.readMessage(this.experimentSupplemental);
                }
            }
        }

        public static Supplemental parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Supplemental) MessageNano.mergeFrom(new Supplemental(), data);
        }

        public static Supplemental parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Supplemental().mergeFrom(input);
        }
    }

    public static final class SupplementedResource extends ExtendableMessageNano {
        public static final SupplementedResource[] EMPTY_ARRAY = new SupplementedResource[0];
        public String fingerprint = "";
        public Resource resource = null;
        public Supplemental[] supplemental = Supplemental.EMPTY_ARRAY;

        public final SupplementedResource clear() {
            this.supplemental = Supplemental.EMPTY_ARRAY;
            this.resource = null;
            this.fingerprint = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof SupplementedResource)) {
                return false;
            }
            SupplementedResource other = (SupplementedResource) o;
            if (!Arrays.equals(this.supplemental, other.supplemental) || (this.resource != null ? !this.resource.equals(other.resource) : other.resource != null) || (this.fingerprint != null ? !this.fingerprint.equals(other.fingerprint) : other.fingerprint != null) || (this.unknownFieldData != null ? !this.unknownFieldData.equals(other.unknownFieldData) : other.unknownFieldData != null)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int i = 0;
            if (this.supplemental == null) {
                result = 17 * 31;
            } else {
                int result2 = 17;
                for (result = 0; result < this.supplemental.length; result++) {
                    result2 = (31 * result2) + (this.supplemental[result] == null ? 0 : this.supplemental[result].hashCode());
                }
                result = result2;
            }
            int result3 = 31 * ((31 * ((31 * result) + (this.resource == null ? 0 : this.resource.hashCode()))) + (this.fingerprint == null ? 0 : this.fingerprint.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.supplemental != null) {
                for (Supplemental element : this.supplemental) {
                    output.writeMessage(1, element);
                }
            }
            if (this.resource != null) {
                output.writeMessage(2, this.resource);
            }
            if (!this.fingerprint.equals("")) {
                output.writeString(3, this.fingerprint);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.supplemental != null) {
                for (Supplemental element : this.supplemental) {
                    size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                }
            }
            if (this.resource != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.resource);
            }
            if (!this.fingerprint.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.fingerprint);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public SupplementedResource mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = this.supplemental == null ? 0 : this.supplemental.length;
                    Supplemental[] newArray = new Supplemental[(i + arrayLength)];
                    if (this.supplemental != null) {
                        System.arraycopy(this.supplemental, 0, newArray, 0, i);
                    }
                    this.supplemental = newArray;
                    while (i < this.supplemental.length - 1) {
                        this.supplemental[i] = new Supplemental();
                        input.readMessage(this.supplemental[i]);
                        input.readTag();
                        i++;
                    }
                    this.supplemental[i] = new Supplemental();
                    input.readMessage(this.supplemental[i]);
                } else if (tag == 18) {
                    this.resource = new Resource();
                    input.readMessage(this.resource);
                } else if (tag != 26) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.fingerprint = input.readString();
                }
            }
        }

        public static SupplementedResource parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (SupplementedResource) MessageNano.mergeFrom(new SupplementedResource(), data);
        }

        public static SupplementedResource parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new SupplementedResource().mergeFrom(input);
        }
    }
}
