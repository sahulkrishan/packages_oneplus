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

public interface Debug {

    public static final class DataLayerEventEvaluationInfo extends ExtendableMessageNano {
        public static final DataLayerEventEvaluationInfo[] EMPTY_ARRAY = new DataLayerEventEvaluationInfo[0];
        public ResolvedFunctionCall[] results = ResolvedFunctionCall.EMPTY_ARRAY;
        public RuleEvaluationStepInfo rulesEvaluation = null;

        public final DataLayerEventEvaluationInfo clear() {
            this.rulesEvaluation = null;
            this.results = ResolvedFunctionCall.EMPTY_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof DataLayerEventEvaluationInfo)) {
                return false;
            }
            DataLayerEventEvaluationInfo other = (DataLayerEventEvaluationInfo) o;
            if (this.rulesEvaluation != null ? !this.rulesEvaluation.equals(other.rulesEvaluation) : other.rulesEvaluation != null) {
                if (Arrays.equals(this.results, other.results)) {
                    if (this.unknownFieldData == null) {
                    }
                }
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = (31 * 17) + (this.rulesEvaluation == null ? 0 : this.rulesEvaluation.hashCode());
            if (this.results == null) {
                result *= 31;
            } else {
                for (int i2 = 0; i2 < this.results.length; i2++) {
                    result = (31 * result) + (this.results[i2] == null ? 0 : this.results[i2].hashCode());
                }
            }
            int result2 = 31 * result;
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result2 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.rulesEvaluation != null) {
                output.writeMessage(1, this.rulesEvaluation);
            }
            if (this.results != null) {
                for (ResolvedFunctionCall element : this.results) {
                    output.writeMessage(2, element);
                }
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.rulesEvaluation != null) {
                size = 0 + CodedOutputByteBufferNano.computeMessageSize(1, this.rulesEvaluation);
            }
            if (this.results != null) {
                for (ResolvedFunctionCall element : this.results) {
                    size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                }
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public DataLayerEventEvaluationInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.rulesEvaluation = new RuleEvaluationStepInfo();
                    input.readMessage(this.rulesEvaluation);
                } else if (tag != 18) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    int i = this.results == null ? 0 : this.results.length;
                    ResolvedFunctionCall[] newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.results != null) {
                        System.arraycopy(this.results, 0, newArray, 0, i);
                    }
                    this.results = newArray;
                    while (i < this.results.length - 1) {
                        this.results[i] = new ResolvedFunctionCall();
                        input.readMessage(this.results[i]);
                        input.readTag();
                        i++;
                    }
                    this.results[i] = new ResolvedFunctionCall();
                    input.readMessage(this.results[i]);
                }
            }
        }

        public static DataLayerEventEvaluationInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DataLayerEventEvaluationInfo) MessageNano.mergeFrom(new DataLayerEventEvaluationInfo(), data);
        }

        public static DataLayerEventEvaluationInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DataLayerEventEvaluationInfo().mergeFrom(input);
        }
    }

    public static final class DebugEvents extends ExtendableMessageNano {
        public static final DebugEvents[] EMPTY_ARRAY = new DebugEvents[0];
        public EventInfo[] event = EventInfo.EMPTY_ARRAY;

        public final DebugEvents clear() {
            this.event = EventInfo.EMPTY_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof DebugEvents)) {
                return false;
            }
            DebugEvents other = (DebugEvents) o;
            if (!Arrays.equals(this.event, other.event) || (this.unknownFieldData != null ? !this.unknownFieldData.equals(other.unknownFieldData) : other.unknownFieldData != null)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int i = 0;
            if (this.event == null) {
                result = 17 * 31;
            } else {
                int result2 = 17;
                for (result = 0; result < this.event.length; result++) {
                    result2 = (31 * result2) + (this.event[result] == null ? 0 : this.event[result].hashCode());
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
            if (this.event != null) {
                for (EventInfo element : this.event) {
                    output.writeMessage(1, element);
                }
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.event != null) {
                for (EventInfo element : this.event) {
                    size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                }
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public DebugEvents mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag != 10) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = this.event == null ? 0 : this.event.length;
                    EventInfo[] newArray = new EventInfo[(i + arrayLength)];
                    if (this.event != null) {
                        System.arraycopy(this.event, 0, newArray, 0, i);
                    }
                    this.event = newArray;
                    while (i < this.event.length - 1) {
                        this.event[i] = new EventInfo();
                        input.readMessage(this.event[i]);
                        input.readTag();
                        i++;
                    }
                    this.event[i] = new EventInfo();
                    input.readMessage(this.event[i]);
                }
            }
        }

        public static DebugEvents parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DebugEvents) MessageNano.mergeFrom(new DebugEvents(), data);
        }

        public static DebugEvents parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DebugEvents().mergeFrom(input);
        }
    }

    public static final class EventInfo extends ExtendableMessageNano {
        public static final EventInfo[] EMPTY_ARRAY = new EventInfo[0];
        public String containerId = "";
        public String containerVersion = "";
        public DataLayerEventEvaluationInfo dataLayerEventResult = null;
        public int eventType = 1;
        public String key = "";
        public MacroEvaluationInfo macroResult = null;

        public interface EventType {
            public static final int DATA_LAYER_EVENT = 1;
            public static final int MACRO_REFERENCE = 2;
        }

        public final EventInfo clear() {
            this.eventType = 1;
            this.containerVersion = "";
            this.containerId = "";
            this.key = "";
            this.macroResult = null;
            this.dataLayerEventResult = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof EventInfo)) {
                return false;
            }
            EventInfo other = (EventInfo) o;
            if (this.eventType != other.eventType || (this.containerVersion != null ? !this.containerVersion.equals(other.containerVersion) : other.containerVersion != null) || (this.containerId != null ? !this.containerId.equals(other.containerId) : other.containerId != null) || (this.key != null ? !this.key.equals(other.key) : other.key != null) || (this.macroResult != null ? !this.macroResult.equals(other.macroResult) : other.macroResult != null) || (this.dataLayerEventResult != null ? !this.dataLayerEventResult.equals(other.dataLayerEventResult) : other.dataLayerEventResult != null) || (this.unknownFieldData != null ? !this.unknownFieldData.equals(other.unknownFieldData) : other.unknownFieldData != null)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * 17) + this.eventType)) + (this.containerVersion == null ? 0 : this.containerVersion.hashCode()))) + (this.containerId == null ? 0 : this.containerId.hashCode()))) + (this.key == null ? 0 : this.key.hashCode()))) + (this.macroResult == null ? 0 : this.macroResult.hashCode()))) + (this.dataLayerEventResult == null ? 0 : this.dataLayerEventResult.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.eventType != 1) {
                output.writeInt32(1, this.eventType);
            }
            if (!this.containerVersion.equals("")) {
                output.writeString(2, this.containerVersion);
            }
            if (!this.containerId.equals("")) {
                output.writeString(3, this.containerId);
            }
            if (!this.key.equals("")) {
                output.writeString(4, this.key);
            }
            if (this.macroResult != null) {
                output.writeMessage(6, this.macroResult);
            }
            if (this.dataLayerEventResult != null) {
                output.writeMessage(7, this.dataLayerEventResult);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.eventType != 1) {
                size = 0 + CodedOutputByteBufferNano.computeInt32Size(1, this.eventType);
            }
            if (!this.containerVersion.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.containerVersion);
            }
            if (!this.containerId.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.containerId);
            }
            if (!this.key.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(4, this.key);
            }
            if (this.macroResult != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, this.macroResult);
            }
            if (this.dataLayerEventResult != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, this.dataLayerEventResult);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public EventInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int temp = input.readInt32();
                    if (temp == 1 || temp == 2) {
                        this.eventType = temp;
                    } else {
                        this.eventType = 1;
                    }
                } else if (tag == 18) {
                    this.containerVersion = input.readString();
                } else if (tag == 26) {
                    this.containerId = input.readString();
                } else if (tag == 34) {
                    this.key = input.readString();
                } else if (tag == 50) {
                    this.macroResult = new MacroEvaluationInfo();
                    input.readMessage(this.macroResult);
                } else if (tag != 58) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.dataLayerEventResult = new DataLayerEventEvaluationInfo();
                    input.readMessage(this.dataLayerEventResult);
                }
            }
        }

        public static EventInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EventInfo) MessageNano.mergeFrom(new EventInfo(), data);
        }

        public static EventInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EventInfo().mergeFrom(input);
        }
    }

    public static final class MacroEvaluationInfo extends ExtendableMessageNano {
        public static final MacroEvaluationInfo[] EMPTY_ARRAY = new MacroEvaluationInfo[0];
        public static final Extension<MacroEvaluationInfo> macro = Extension.create(47497405, new TypeLiteral<MacroEvaluationInfo>() {
        });
        public ResolvedFunctionCall result = null;
        public RuleEvaluationStepInfo rulesEvaluation = null;

        public final MacroEvaluationInfo clear() {
            this.rulesEvaluation = null;
            this.result = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof MacroEvaluationInfo)) {
                return false;
            }
            MacroEvaluationInfo other = (MacroEvaluationInfo) o;
            if (this.rulesEvaluation != null ? !this.rulesEvaluation.equals(other.rulesEvaluation) : other.rulesEvaluation != null) {
                if (this.result != null ? !this.result.equals(other.result) : other.result != null) {
                    if (this.unknownFieldData != null) {
                    }
                }
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * 17) + (this.rulesEvaluation == null ? 0 : this.rulesEvaluation.hashCode()))) + (this.result == null ? 0 : this.result.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.rulesEvaluation != null) {
                output.writeMessage(1, this.rulesEvaluation);
            }
            if (this.result != null) {
                output.writeMessage(3, this.result);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.rulesEvaluation != null) {
                size = 0 + CodedOutputByteBufferNano.computeMessageSize(1, this.rulesEvaluation);
            }
            if (this.result != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, this.result);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public MacroEvaluationInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.rulesEvaluation = new RuleEvaluationStepInfo();
                    input.readMessage(this.rulesEvaluation);
                } else if (tag != 26) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.result = new ResolvedFunctionCall();
                    input.readMessage(this.result);
                }
            }
        }

        public static MacroEvaluationInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (MacroEvaluationInfo) MessageNano.mergeFrom(new MacroEvaluationInfo(), data);
        }

        public static MacroEvaluationInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new MacroEvaluationInfo().mergeFrom(input);
        }
    }

    public static final class ResolvedFunctionCall extends ExtendableMessageNano {
        public static final ResolvedFunctionCall[] EMPTY_ARRAY = new ResolvedFunctionCall[0];
        public String associatedRuleName = "";
        public ResolvedProperty[] properties = ResolvedProperty.EMPTY_ARRAY;
        public Value result = null;

        public final ResolvedFunctionCall clear() {
            this.properties = ResolvedProperty.EMPTY_ARRAY;
            this.result = null;
            this.associatedRuleName = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof ResolvedFunctionCall)) {
                return false;
            }
            ResolvedFunctionCall other = (ResolvedFunctionCall) o;
            if (!Arrays.equals(this.properties, other.properties) || (this.result != null ? !this.result.equals(other.result) : other.result != null) || (this.associatedRuleName != null ? !this.associatedRuleName.equals(other.associatedRuleName) : other.associatedRuleName != null) || (this.unknownFieldData != null ? !this.unknownFieldData.equals(other.unknownFieldData) : other.unknownFieldData != null)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int i = 0;
            if (this.properties == null) {
                result = 17 * 31;
            } else {
                int result2 = 17;
                for (result = 0; result < this.properties.length; result++) {
                    result2 = (31 * result2) + (this.properties[result] == null ? 0 : this.properties[result].hashCode());
                }
                result = result2;
            }
            int result3 = 31 * ((31 * ((31 * result) + (this.result == null ? 0 : this.result.hashCode()))) + (this.associatedRuleName == null ? 0 : this.associatedRuleName.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.properties != null) {
                for (ResolvedProperty element : this.properties) {
                    output.writeMessage(1, element);
                }
            }
            if (this.result != null) {
                output.writeMessage(2, this.result);
            }
            if (!this.associatedRuleName.equals("")) {
                output.writeString(3, this.associatedRuleName);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (this.properties != null) {
                for (ResolvedProperty element : this.properties) {
                    size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                }
            }
            if (this.result != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.result);
            }
            if (!this.associatedRuleName.equals("")) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.associatedRuleName);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public ResolvedFunctionCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    int i = this.properties == null ? 0 : this.properties.length;
                    ResolvedProperty[] newArray = new ResolvedProperty[(i + arrayLength)];
                    if (this.properties != null) {
                        System.arraycopy(this.properties, 0, newArray, 0, i);
                    }
                    this.properties = newArray;
                    while (i < this.properties.length - 1) {
                        this.properties[i] = new ResolvedProperty();
                        input.readMessage(this.properties[i]);
                        input.readTag();
                        i++;
                    }
                    this.properties[i] = new ResolvedProperty();
                    input.readMessage(this.properties[i]);
                } else if (tag == 18) {
                    this.result = new Value();
                    input.readMessage(this.result);
                } else if (tag != 26) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.associatedRuleName = input.readString();
                }
            }
        }

        public static ResolvedFunctionCall parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ResolvedFunctionCall) MessageNano.mergeFrom(new ResolvedFunctionCall(), data);
        }

        public static ResolvedFunctionCall parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ResolvedFunctionCall().mergeFrom(input);
        }
    }

    public static final class ResolvedProperty extends ExtendableMessageNano {
        public static final ResolvedProperty[] EMPTY_ARRAY = new ResolvedProperty[0];
        public String key = "";
        public Value value = null;

        public final ResolvedProperty clear() {
            this.key = "";
            this.value = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof ResolvedProperty)) {
                return false;
            }
            ResolvedProperty other = (ResolvedProperty) o;
            if (this.key != null ? !this.key.equals(other.key) : other.key != null) {
                if (this.value != null ? !this.value.equals(other.value) : other.value != null) {
                    if (this.unknownFieldData != null) {
                    }
                }
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * 17) + (this.key == null ? 0 : this.key.hashCode()))) + (this.value == null ? 0 : this.value.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.key.equals("")) {
                output.writeString(1, this.key);
            }
            if (this.value != null) {
                output.writeMessage(2, this.value);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            if (!this.key.equals("")) {
                size = 0 + CodedOutputByteBufferNano.computeStringSize(1, this.key);
            }
            if (this.value != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.value);
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public ResolvedProperty mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.key = input.readString();
                } else if (tag != 18) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.value = new Value();
                    input.readMessage(this.value);
                }
            }
        }

        public static ResolvedProperty parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ResolvedProperty) MessageNano.mergeFrom(new ResolvedProperty(), data);
        }

        public static ResolvedProperty parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ResolvedProperty().mergeFrom(input);
        }
    }

    public static final class ResolvedRule extends ExtendableMessageNano {
        public static final ResolvedRule[] EMPTY_ARRAY = new ResolvedRule[0];
        public ResolvedFunctionCall[] addMacros = ResolvedFunctionCall.EMPTY_ARRAY;
        public ResolvedFunctionCall[] addTags = ResolvedFunctionCall.EMPTY_ARRAY;
        public ResolvedFunctionCall[] negativePredicates = ResolvedFunctionCall.EMPTY_ARRAY;
        public ResolvedFunctionCall[] positivePredicates = ResolvedFunctionCall.EMPTY_ARRAY;
        public ResolvedFunctionCall[] removeMacros = ResolvedFunctionCall.EMPTY_ARRAY;
        public ResolvedFunctionCall[] removeTags = ResolvedFunctionCall.EMPTY_ARRAY;
        public Value result = null;

        public final ResolvedRule clear() {
            this.positivePredicates = ResolvedFunctionCall.EMPTY_ARRAY;
            this.negativePredicates = ResolvedFunctionCall.EMPTY_ARRAY;
            this.addTags = ResolvedFunctionCall.EMPTY_ARRAY;
            this.removeTags = ResolvedFunctionCall.EMPTY_ARRAY;
            this.addMacros = ResolvedFunctionCall.EMPTY_ARRAY;
            this.removeMacros = ResolvedFunctionCall.EMPTY_ARRAY;
            this.result = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof ResolvedRule)) {
                return false;
            }
            ResolvedRule other = (ResolvedRule) o;
            if (!(Arrays.equals(this.positivePredicates, other.positivePredicates) && Arrays.equals(this.negativePredicates, other.negativePredicates) && Arrays.equals(this.addTags, other.addTags) && Arrays.equals(this.removeTags, other.removeTags) && Arrays.equals(this.addMacros, other.addMacros) && Arrays.equals(this.removeMacros, other.removeMacros) && (!this.result != null ? other.result != null : !this.result.equals(other.result)) && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int result2;
            int i = 0;
            if (this.positivePredicates == null) {
                result = 17 * 31;
            } else {
                result2 = 17;
                for (result = 0; result < this.positivePredicates.length; result++) {
                    result2 = (31 * result2) + (this.positivePredicates[result] == null ? 0 : this.positivePredicates[result].hashCode());
                }
                result = result2;
            }
            if (this.negativePredicates == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.negativePredicates.length; result++) {
                    result2 = (31 * result2) + (this.negativePredicates[result] == null ? 0 : this.negativePredicates[result].hashCode());
                }
                result = result2;
            }
            if (this.addTags == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.addTags.length; result++) {
                    result2 = (31 * result2) + (this.addTags[result] == null ? 0 : this.addTags[result].hashCode());
                }
                result = result2;
            }
            if (this.removeTags == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.removeTags.length; result++) {
                    result2 = (31 * result2) + (this.removeTags[result] == null ? 0 : this.removeTags[result].hashCode());
                }
                result = result2;
            }
            if (this.addMacros == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.addMacros.length; result++) {
                    result2 = (31 * result2) + (this.addMacros[result] == null ? 0 : this.addMacros[result].hashCode());
                }
                result = result2;
            }
            if (this.removeMacros == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.removeMacros.length; result++) {
                    result2 = (31 * result2) + (this.removeMacros[result] == null ? 0 : this.removeMacros[result].hashCode());
                }
                result = result2;
            }
            int result3 = 31 * ((31 * result) + (this.result == null ? 0 : this.result.hashCode()));
            if (this.unknownFieldData != null) {
                i = this.unknownFieldData.hashCode();
            }
            return result3 + i;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int len$;
            int i$ = 0;
            if (this.positivePredicates != null) {
                for (ResolvedFunctionCall element : this.positivePredicates) {
                    output.writeMessage(1, element);
                }
            }
            if (this.negativePredicates != null) {
                for (ResolvedFunctionCall element2 : this.negativePredicates) {
                    output.writeMessage(2, element2);
                }
            }
            if (this.addTags != null) {
                for (ResolvedFunctionCall element22 : this.addTags) {
                    output.writeMessage(3, element22);
                }
            }
            if (this.removeTags != null) {
                for (ResolvedFunctionCall element222 : this.removeTags) {
                    output.writeMessage(4, element222);
                }
            }
            if (this.addMacros != null) {
                for (ResolvedFunctionCall element2222 : this.addMacros) {
                    output.writeMessage(5, element2222);
                }
            }
            if (this.removeMacros != null) {
                ResolvedFunctionCall[] arr$ = this.removeMacros;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeMessage(6, arr$[i$]);
                    i$++;
                }
            }
            if (this.result != null) {
                output.writeMessage(7, this.result);
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size;
            int size2 = 0;
            int i$ = 0;
            if (this.positivePredicates != null) {
                size = 0;
                for (ResolvedFunctionCall element : this.positivePredicates) {
                    size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                }
                size2 = size;
            }
            if (this.negativePredicates != null) {
                size = size2;
                for (ResolvedFunctionCall element2 : this.negativePredicates) {
                    size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                }
                size2 = size;
            }
            if (this.addTags != null) {
                size = size2;
                for (ResolvedFunctionCall element22 : this.addTags) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, element22);
                }
                size2 = size;
            }
            if (this.removeTags != null) {
                size = size2;
                for (ResolvedFunctionCall element222 : this.removeTags) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, element222);
                }
                size2 = size;
            }
            if (this.addMacros != null) {
                size = size2;
                for (ResolvedFunctionCall element2222 : this.addMacros) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, element2222);
                }
                size2 = size;
            }
            if (this.removeMacros != null) {
                ResolvedFunctionCall[] arr$ = this.removeMacros;
                while (i$ < arr$.length) {
                    size2 += CodedOutputByteBufferNano.computeMessageSize(6, arr$[i$]);
                    i$++;
                }
            }
            if (this.result != null) {
                size2 += CodedOutputByteBufferNano.computeMessageSize(7, this.result);
            }
            size2 += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size2;
            return size2;
        }

        public ResolvedRule mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                int arrayLength;
                int i;
                ResolvedFunctionCall[] newArray;
                if (tag == 10) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    i = this.positivePredicates == null ? 0 : this.positivePredicates.length;
                    newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.positivePredicates != null) {
                        System.arraycopy(this.positivePredicates, 0, newArray, 0, i);
                    }
                    this.positivePredicates = newArray;
                    while (i < this.positivePredicates.length - 1) {
                        this.positivePredicates[i] = new ResolvedFunctionCall();
                        input.readMessage(this.positivePredicates[i]);
                        input.readTag();
                        i++;
                    }
                    this.positivePredicates[i] = new ResolvedFunctionCall();
                    input.readMessage(this.positivePredicates[i]);
                } else if (tag == 18) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    i = this.negativePredicates == null ? 0 : this.negativePredicates.length;
                    newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.negativePredicates != null) {
                        System.arraycopy(this.negativePredicates, 0, newArray, 0, i);
                    }
                    this.negativePredicates = newArray;
                    while (i < this.negativePredicates.length - 1) {
                        this.negativePredicates[i] = new ResolvedFunctionCall();
                        input.readMessage(this.negativePredicates[i]);
                        input.readTag();
                        i++;
                    }
                    this.negativePredicates[i] = new ResolvedFunctionCall();
                    input.readMessage(this.negativePredicates[i]);
                } else if (tag == 26) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    i = this.addTags == null ? 0 : this.addTags.length;
                    newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.addTags != null) {
                        System.arraycopy(this.addTags, 0, newArray, 0, i);
                    }
                    this.addTags = newArray;
                    while (i < this.addTags.length - 1) {
                        this.addTags[i] = new ResolvedFunctionCall();
                        input.readMessage(this.addTags[i]);
                        input.readTag();
                        i++;
                    }
                    this.addTags[i] = new ResolvedFunctionCall();
                    input.readMessage(this.addTags[i]);
                } else if (tag == 34) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                    i = this.removeTags == null ? 0 : this.removeTags.length;
                    newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.removeTags != null) {
                        System.arraycopy(this.removeTags, 0, newArray, 0, i);
                    }
                    this.removeTags = newArray;
                    while (i < this.removeTags.length - 1) {
                        this.removeTags[i] = new ResolvedFunctionCall();
                        input.readMessage(this.removeTags[i]);
                        input.readTag();
                        i++;
                    }
                    this.removeTags[i] = new ResolvedFunctionCall();
                    input.readMessage(this.removeTags[i]);
                } else if (tag == 42) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    i = this.addMacros == null ? 0 : this.addMacros.length;
                    newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.addMacros != null) {
                        System.arraycopy(this.addMacros, 0, newArray, 0, i);
                    }
                    this.addMacros = newArray;
                    while (i < this.addMacros.length - 1) {
                        this.addMacros[i] = new ResolvedFunctionCall();
                        input.readMessage(this.addMacros[i]);
                        input.readTag();
                        i++;
                    }
                    this.addMacros[i] = new ResolvedFunctionCall();
                    input.readMessage(this.addMacros[i]);
                } else if (tag == 50) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                    i = this.removeMacros == null ? 0 : this.removeMacros.length;
                    newArray = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.removeMacros != null) {
                        System.arraycopy(this.removeMacros, 0, newArray, 0, i);
                    }
                    this.removeMacros = newArray;
                    while (i < this.removeMacros.length - 1) {
                        this.removeMacros[i] = new ResolvedFunctionCall();
                        input.readMessage(this.removeMacros[i]);
                        input.readTag();
                        i++;
                    }
                    this.removeMacros[i] = new ResolvedFunctionCall();
                    input.readMessage(this.removeMacros[i]);
                } else if (tag != 58) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    this.result = new Value();
                    input.readMessage(this.result);
                }
            }
        }

        public static ResolvedRule parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ResolvedRule) MessageNano.mergeFrom(new ResolvedRule(), data);
        }

        public static ResolvedRule parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ResolvedRule().mergeFrom(input);
        }
    }

    public static final class RuleEvaluationStepInfo extends ExtendableMessageNano {
        public static final RuleEvaluationStepInfo[] EMPTY_ARRAY = new RuleEvaluationStepInfo[0];
        public ResolvedFunctionCall[] enabledFunctions = ResolvedFunctionCall.EMPTY_ARRAY;
        public ResolvedRule[] rules = ResolvedRule.EMPTY_ARRAY;

        public final RuleEvaluationStepInfo clear() {
            this.rules = ResolvedRule.EMPTY_ARRAY;
            this.enabledFunctions = ResolvedFunctionCall.EMPTY_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof RuleEvaluationStepInfo)) {
                return false;
            }
            RuleEvaluationStepInfo other = (RuleEvaluationStepInfo) o;
            if (!(Arrays.equals(this.rules, other.rules) && Arrays.equals(this.enabledFunctions, other.enabledFunctions) && (!this.unknownFieldData != null ? other.unknownFieldData != null : !this.unknownFieldData.equals(other.unknownFieldData)))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int result;
            int result2;
            int i = 0;
            if (this.rules == null) {
                result = 17 * 31;
            } else {
                result2 = 17;
                for (result = 0; result < this.rules.length; result++) {
                    result2 = (31 * result2) + (this.rules[result] == null ? 0 : this.rules[result].hashCode());
                }
                result = result2;
            }
            if (this.enabledFunctions == null) {
                result *= 31;
            } else {
                result2 = result;
                for (result = 0; result < this.enabledFunctions.length; result++) {
                    result2 = (31 * result2) + (this.enabledFunctions[result] == null ? 0 : this.enabledFunctions[result].hashCode());
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
            if (this.rules != null) {
                for (ResolvedRule element : this.rules) {
                    output.writeMessage(1, element);
                }
            }
            if (this.enabledFunctions != null) {
                ResolvedFunctionCall[] arr$ = this.enabledFunctions;
                len$ = arr$.length;
                while (i$ < len$) {
                    output.writeMessage(2, arr$[i$]);
                    i$++;
                }
            }
            WireFormatNano.writeUnknownFields(this.unknownFieldData, output);
        }

        public int getSerializedSize() {
            int size = 0;
            int i$ = 0;
            if (this.rules != null) {
                int size2 = 0;
                for (ResolvedRule element : this.rules) {
                    size2 += CodedOutputByteBufferNano.computeMessageSize(1, element);
                }
                size = size2;
            }
            if (this.enabledFunctions != null) {
                ResolvedFunctionCall[] arr$ = this.enabledFunctions;
                while (i$ < arr$.length) {
                    size += CodedOutputByteBufferNano.computeMessageSize(2, arr$[i$]);
                    i$++;
                }
            }
            size += WireFormatNano.computeWireSize(this.unknownFieldData);
            this.cachedSize = size;
            return size;
        }

        public RuleEvaluationStepInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                int arrayLength;
                int i;
                if (tag == 10) {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                    i = this.rules == null ? 0 : this.rules.length;
                    ResolvedRule[] newArray = new ResolvedRule[(i + arrayLength)];
                    if (this.rules != null) {
                        System.arraycopy(this.rules, 0, newArray, 0, i);
                    }
                    this.rules = newArray;
                    while (i < this.rules.length - 1) {
                        this.rules[i] = new ResolvedRule();
                        input.readMessage(this.rules[i]);
                        input.readTag();
                        i++;
                    }
                    this.rules[i] = new ResolvedRule();
                    input.readMessage(this.rules[i]);
                } else if (tag != 18) {
                    if (this.unknownFieldData == null) {
                        this.unknownFieldData = new ArrayList();
                    }
                    if (!WireFormatNano.storeUnknownField(this.unknownFieldData, input, tag)) {
                        return this;
                    }
                } else {
                    arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                    i = this.enabledFunctions == null ? 0 : this.enabledFunctions.length;
                    ResolvedFunctionCall[] newArray2 = new ResolvedFunctionCall[(i + arrayLength)];
                    if (this.enabledFunctions != null) {
                        System.arraycopy(this.enabledFunctions, 0, newArray2, 0, i);
                    }
                    this.enabledFunctions = newArray2;
                    while (i < this.enabledFunctions.length - 1) {
                        this.enabledFunctions[i] = new ResolvedFunctionCall();
                        input.readMessage(this.enabledFunctions[i]);
                        input.readTag();
                        i++;
                    }
                    this.enabledFunctions[i] = new ResolvedFunctionCall();
                    input.readMessage(this.enabledFunctions[i]);
                }
            }
        }

        public static RuleEvaluationStepInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (RuleEvaluationStepInfo) MessageNano.mergeFrom(new RuleEvaluationStepInfo(), data);
        }

        public static RuleEvaluationStepInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new RuleEvaluationStepInfo().mergeFrom(input);
        }
    }
}
