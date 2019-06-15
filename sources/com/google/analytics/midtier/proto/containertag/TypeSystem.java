package com.google.analytics.midtier.proto.containertag;

import com.google.tagmanager.protobuf.nano.CodedInputByteBufferNano;
import com.google.tagmanager.protobuf.nano.CodedOutputByteBufferNano;
import com.google.tagmanager.protobuf.nano.ExtendableMessageNano;
import com.google.tagmanager.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.tagmanager.protobuf.nano.MessageNano;
import com.google.tagmanager.protobuf.nano.WireFormatNano;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public interface TypeSystem {

    public static final class Value extends ExtendableMessageNano {
        public static final Value[] EMPTY_ARRAY = new Value[0];
        public boolean boolean_ = false;
        public boolean containsReferences = false;
        public int[] escaping = WireFormatNano.EMPTY_INT_ARRAY;
        public String functionId = "";
        public long integer = 0;
        public Value[] listItem = EMPTY_ARRAY;
        public String macroReference = "";
        public Value[] mapKey = EMPTY_ARRAY;
        public Value[] mapValue = EMPTY_ARRAY;
        public String string = "";
        public String tagReference = "";
        public Value[] templateToken = EMPTY_ARRAY;
        public int type = 1;

        public interface Escaping {
            public static final int CONVERT_JS_VALUE_TO_EXPRESSION = 16;
            public static final int ESCAPE_CSS_STRING = 10;
            public static final int ESCAPE_HTML = 1;
            public static final int ESCAPE_HTML_ATTRIBUTE = 3;
            public static final int ESCAPE_HTML_ATTRIBUTE_NOSPACE = 4;
            public static final int ESCAPE_HTML_RCDATA = 2;
            public static final int ESCAPE_JS_REGEX = 9;
            public static final int ESCAPE_JS_STRING = 7;
            public static final int ESCAPE_JS_VALUE = 8;
            public static final int ESCAPE_URI = 12;
            public static final int FILTER_CSS_VALUE = 11;
            public static final int FILTER_HTML_ATTRIBUTES = 6;
            public static final int FILTER_HTML_ELEMENT_NAME = 5;
            public static final int FILTER_NORMALIZE_URI = 14;
            public static final int NORMALIZE_URI = 13;
            public static final int NO_AUTOESCAPE = 15;
            public static final int TEXT = 17;
        }

        public interface Type {
            public static final int BOOLEAN = 8;
            public static final int FUNCTION_ID = 5;
            public static final int INTEGER = 6;
            public static final int LIST = 2;
            public static final int MACRO_REFERENCE = 4;
            public static final int MAP = 3;
            public static final int STRING = 1;
            public static final int TAG_REFERENCE = 9;
            public static final int TEMPLATE = 7;
        }

        public final Value clear() {
            this.type = 1;
            this.string = "";
            this.listItem = EMPTY_ARRAY;
            this.mapKey = EMPTY_ARRAY;
            this.mapValue = EMPTY_ARRAY;
            this.macroReference = "";
            this.functionId = "";
            this.integer = 0;
            this.boolean_ = false;
            this.templateToken = EMPTY_ARRAY;
            this.tagReference = "";
            this.escaping = WireFormatNano.EMPTY_INT_ARRAY;
            this.containsReferences = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Value)) {
                return false;
            }
            Value other = (Value) o;
            if (!(this.type == other.type && (!this.string != null ? other.string != null : !this.string.equals(other.string)) && Arrays.equals(this.listItem, other.listItem) && Arrays.equals(this.mapKey, other.mapKey) && Arrays.equals(this.mapValue, other.mapValue) && (!this.macroReference != null ? other.macroReference != null : !this.macroReference.equals(other.macroReference)) && (!this.functionId != null ? other.functionId != null : !this.functionId.equals(other.functionId)) && this.integer == other.integer && this.boolean_ == other.boolean_ && Arrays.equals(this.templateToken, other.templateToken) && (!this.tagReference != null ? other.tagReference != null : !this.tagReference.equals(other.tagReference)) && Arrays.equals(this.escaping, other.escaping) && this.containsReferences == other.containsReferences && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int i = 0;
            int result2 = (31 * ((31 * 17) + this.type)) + (this.string == null ? 0 : this.string.hashCode());
            if (this.listItem == null) {
                result2 *= 31;
            } else {
                result = result2;
                for (result2 = 0; result2 < this.listItem.length; result2++) {
                    result = (31 * result) + (this.listItem[result2] == null ? 0 : this.listItem[result2].hashCode());
                }
                result2 = result;
            }
            if (this.mapKey == null) {
                result2 *= 31;
            } else {
                result = result2;
                for (result2 = 0; result2 < this.mapKey.length; result2++) {
                    result = (31 * result) + (this.mapKey[result2] == null ? 0 : this.mapKey[result2].hashCode());
                }
                result2 = result;
            }
            if (this.mapValue == null) {
                result2 *= 31;
            } else {
                result = result2;
                for (result2 = 0; result2 < this.mapValue.length; result2++) {
                    result = (31 * result) + (this.mapValue[result2] == null ? 0 : this.mapValue[result2].hashCode());
                }
                result2 = result;
            }
            int i2 = 2;
            result2 = (31 * ((31 * ((31 * ((31 * result2) + (this.macroReference == null ? 0 : this.macroReference.hashCode()))) + (this.functionId == null ? 0 : this.functionId.hashCode()))) + ((int) (this.integer ^ (this.integer >>> 32))))) + (this.boolean_ ? 1 : 2);
            if (this.templateToken == null) {
                result2 *= 31;
            } else {
                result = result2;
                for (result2 = 0; result2 < this.templateToken.length; result2++) {
                    result = (31 * result) + (this.templateToken[result2] == null ? 0 : this.templateToken[result2].hashCode());
                }
                result2 = result;
            }
            result = (31 * result2) + (this.tagReference == null ? 0 : this.tagReference.hashCode());
            if (this.escaping == null) {
                result *= 31;
            } else {
                for (int i3 : this.escaping) {
                    result = (31 * result) + i3;
                }
            }
            result2 = 31 * result;
            if (this.containsReferences) {
                i2 = 1;
            }
            int result3 = 31 * (result2 + i2);
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int len$;
            output.writeInt32(1, this.type);
            if (!this.string.equals("")) {
                output.writeString(2, this.string);
            }
            int i$ = 0;
            if (this.listItem != null) {
                for (Value element : this.listItem) {
                    output.writeMessage(3, element);
                }
            }
            if (this.mapKey != null) {
                for (Value element2 : this.mapKey) {
                    output.writeMessage(4, element2);
                }
            }
            if (this.mapValue != null) {
                for (Value element22 : this.mapValue) {
                    output.writeMessage(5, element22);
                }
            }
            if (!this.macroReference.equals("")) {
                output.writeString(6, this.macroReference);
            }
            if (!this.functionId.equals("")) {
                output.writeString(7, this.functionId);
            }
            if (this.integer != 0) {
                output.writeInt64(8, this.integer);
            }
            if (this.containsReferences) {
                output.writeBool(9, this.containsReferences);
            }
            if (this.escaping != null && this.escaping.length > 0) {
                for (int element3 : this.escaping) {
                    output.writeInt32(10, element3);
                }
            }
            if (this.templateToken != null) {
                Value[] arr$ = this.templateToken;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeMessage(11, arr$[i$]);
                    i$++;
                }
            }
            if (this.boolean_) {
                output.writeBool(12, this.boolean_);
            }
            if (!this.tagReference.equals("")) {
                output.writeString(13, this.tagReference);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size;
            int size2 = 0 + CodedOutputByteBufferNano.computeInt32Size(1, this.type);
            if (!this.string.equals("")) {
                size2 += CodedOutputByteBufferNano.computeStringSize(2, this.string);
            }
            int i$ = 0;
            if (this.listItem != null) {
                size = size2;
                for (Value element : this.listItem) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                }
                size2 = size;
            }
            if (this.mapKey != null) {
                size = size2;
                for (Value element2 : this.mapKey) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                }
                size2 = size;
            }
            if (this.mapValue != null) {
                size = size2;
                for (Value element22 : this.mapValue) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, element22);
                }
                size2 = size;
            }
            if (!this.macroReference.equals("")) {
                size2 += CodedOutputByteBufferNano.computeStringSize(6, this.macroReference);
            }
            if (!this.functionId.equals("")) {
                size2 += CodedOutputByteBufferNano.computeStringSize(7, this.functionId);
            }
            if (this.integer != 0) {
                size2 += CodedOutputByteBufferNano.computeInt64Size(8, this.integer);
            }
            if (this.containsReferences) {
                size2 += CodedOutputByteBufferNano.computeBoolSize(9, this.containsReferences);
            }
            if (this.escaping != null && this.escaping.length > 0) {
                int dataSize = 0;
                for (int element3 : this.escaping) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element3);
                }
                size2 = (size2 + dataSize) + (1 * this.escaping.length);
            }
            if (this.templateToken != null) {
                Value[] arr$ = this.templateToken;
                while (i$ < arr$.length) {
                    size2 += CodedOutputByteBufferNano.computeMessageSize(11, arr$[i$]);
                    i$++;
                }
            }
            if (this.boolean_) {
                size2 += CodedOutputByteBufferNano.computeBoolSize(12, this.boolean_);
            }
            if (!this.tagReference.equals("")) {
                size2 += CodedOutputByteBufferNano.computeStringSize(13, this.tagReference);
            }
            size2 += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size2;
            return size2;
        }

        public Value mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                Value[] newArray;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        int temp = input.readInt32();
                        if (temp != 1 && temp != 2 && temp != 3 && temp != 4 && temp != 5 && temp != 6 && temp != 7 && temp != 8 && temp != 9) {
                            this.type = 1;
                            break;
                        }
                        this.type = temp;
                        break;
                        break;
                    case 18:
                        this.string = input.readString();
                        break;
                    case 26:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        i = this.listItem == null ? 0 : this.listItem.length;
                        newArray = new Value[(i + arrayLength)];
                        if (this.listItem != null) {
                            System.arraycopy(this.listItem, 0, newArray, 0, i);
                        }
                        this.listItem = newArray;
                        while (i < this.listItem.length - 1) {
                            this.listItem[i] = new Value();
                            input.readMessage(this.listItem[i]);
                            input.readTag();
                            i++;
                        }
                        this.listItem[i] = new Value();
                        input.readMessage(this.listItem[i]);
                        break;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        i = this.mapKey == null ? 0 : this.mapKey.length;
                        newArray = new Value[(i + arrayLength)];
                        if (this.mapKey != null) {
                            System.arraycopy(this.mapKey, 0, newArray, 0, i);
                        }
                        this.mapKey = newArray;
                        while (i < this.mapKey.length - 1) {
                            this.mapKey[i] = new Value();
                            input.readMessage(this.mapKey[i]);
                            input.readTag();
                            i++;
                        }
                        this.mapKey[i] = new Value();
                        input.readMessage(this.mapKey[i]);
                        break;
                    case 42:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        i = this.mapValue == null ? 0 : this.mapValue.length;
                        newArray = new Value[(i + arrayLength)];
                        if (this.mapValue != null) {
                            System.arraycopy(this.mapValue, 0, newArray, 0, i);
                        }
                        this.mapValue = newArray;
                        while (i < this.mapValue.length - 1) {
                            this.mapValue[i] = new Value();
                            input.readMessage(this.mapValue[i]);
                            input.readTag();
                            i++;
                        }
                        this.mapValue[i] = new Value();
                        input.readMessage(this.mapValue[i]);
                        break;
                    case 50:
                        this.macroReference = input.readString();
                        break;
                    case 58:
                        this.functionId = input.readString();
                        break;
                    case 64:
                        this.integer = input.readInt64();
                        break;
                    case 72:
                        this.containsReferences = input.readBool();
                        break;
                    case 80:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 80);
                        i = this.escaping.length;
                        int[] newArray2 = new int[(i + arrayLength)];
                        System.arraycopy(this.escaping, 0, newArray2, 0, i);
                        this.escaping = newArray2;
                        while (i < this.escaping.length - 1) {
                            this.escaping[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        this.escaping[i] = input.readInt32();
                        break;
                    case 90:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 90);
                        i = this.templateToken == null ? 0 : this.templateToken.length;
                        newArray = new Value[(i + arrayLength)];
                        if (this.templateToken != null) {
                            System.arraycopy(this.templateToken, 0, newArray, 0, i);
                        }
                        this.templateToken = newArray;
                        while (i < this.templateToken.length - 1) {
                            this.templateToken[i] = new Value();
                            input.readMessage(this.templateToken[i]);
                            input.readTag();
                            i++;
                        }
                        this.templateToken[i] = new Value();
                        input.readMessage(this.templateToken[i]);
                        break;
                    case 96:
                        this.boolean_ = input.readBool();
                        break;
                    case 106:
                        this.tagReference = input.readString();
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

        public static Value parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Value) MessageNano.mergeFrom(new Value(), data);
        }

        public static Value parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Value().mergeFrom(input);
        }
    }
}
