/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package pt.com.broker.codec.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BrokerMessage implements org.apache.thrift.TBase<BrokerMessage, BrokerMessage._Fields>, java.io.Serializable, Cloneable, Comparable<BrokerMessage> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BrokerMessage");

  private static final org.apache.thrift.protocol.TField MESSAGE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("message_id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField PAYLOAD_FIELD_DESC = new org.apache.thrift.protocol.TField("payload", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField EXPIRATION_FIELD_DESC = new org.apache.thrift.protocol.TField("expiration", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField TIMESTAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamp", org.apache.thrift.protocol.TType.I64, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new BrokerMessageStandardSchemeFactory());
    schemes.put(TupleScheme.class, new BrokerMessageTupleSchemeFactory());
  }

  public String message_id; // optional
  public ByteBuffer payload; // required
  public long expiration; // optional
  public long timestamp; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    MESSAGE_ID((short)1, "message_id"),
    PAYLOAD((short)2, "payload"),
    EXPIRATION((short)3, "expiration"),
    TIMESTAMP((short)4, "timestamp");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // MESSAGE_ID
          return MESSAGE_ID;
        case 2: // PAYLOAD
          return PAYLOAD;
        case 3: // EXPIRATION
          return EXPIRATION;
        case 4: // TIMESTAMP
          return TIMESTAMP;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __EXPIRATION_ISSET_ID = 0;
  private static final int __TIMESTAMP_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  private _Fields optionals[] = {_Fields.MESSAGE_ID,_Fields.EXPIRATION,_Fields.TIMESTAMP};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MESSAGE_ID, new org.apache.thrift.meta_data.FieldMetaData("message_id", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PAYLOAD, new org.apache.thrift.meta_data.FieldMetaData("payload", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.EXPIRATION, new org.apache.thrift.meta_data.FieldMetaData("expiration", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.TIMESTAMP, new org.apache.thrift.meta_data.FieldMetaData("timestamp", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BrokerMessage.class, metaDataMap);
  }

  public BrokerMessage() {
  }

  public BrokerMessage(
    ByteBuffer payload)
  {
    this();
    this.payload = payload;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public BrokerMessage(BrokerMessage other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetMessage_id()) {
      this.message_id = other.message_id;
    }
    if (other.isSetPayload()) {
      this.payload = org.apache.thrift.TBaseHelper.copyBinary(other.payload);
;
    }
    this.expiration = other.expiration;
    this.timestamp = other.timestamp;
  }

  public BrokerMessage deepCopy() {
    return new BrokerMessage(this);
  }

  @Override
  public void clear() {
    this.message_id = null;
    this.payload = null;
    setExpirationIsSet(false);
    this.expiration = 0;
    setTimestampIsSet(false);
    this.timestamp = 0;
  }

  public String getMessage_id() {
    return this.message_id;
  }

  public BrokerMessage setMessage_id(String message_id) {
    this.message_id = message_id;
    return this;
  }

  public void unsetMessage_id() {
    this.message_id = null;
  }

  /** Returns true if field message_id is set (has been assigned a value) and false otherwise */
  public boolean isSetMessage_id() {
    return this.message_id != null;
  }

  public void setMessage_idIsSet(boolean value) {
    if (!value) {
      this.message_id = null;
    }
  }

  public byte[] getPayload() {
    setPayload(org.apache.thrift.TBaseHelper.rightSize(payload));
    return payload == null ? null : payload.array();
  }

  public ByteBuffer bufferForPayload() {
    return payload;
  }

  public BrokerMessage setPayload(byte[] payload) {
    setPayload(payload == null ? (ByteBuffer)null : ByteBuffer.wrap(payload));
    return this;
  }

  public BrokerMessage setPayload(ByteBuffer payload) {
    this.payload = payload;
    return this;
  }

  public void unsetPayload() {
    this.payload = null;
  }

  /** Returns true if field payload is set (has been assigned a value) and false otherwise */
  public boolean isSetPayload() {
    return this.payload != null;
  }

  public void setPayloadIsSet(boolean value) {
    if (!value) {
      this.payload = null;
    }
  }

  public long getExpiration() {
    return this.expiration;
  }

  public BrokerMessage setExpiration(long expiration) {
    this.expiration = expiration;
    setExpirationIsSet(true);
    return this;
  }

  public void unsetExpiration() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __EXPIRATION_ISSET_ID);
  }

  /** Returns true if field expiration is set (has been assigned a value) and false otherwise */
  public boolean isSetExpiration() {
    return EncodingUtils.testBit(__isset_bitfield, __EXPIRATION_ISSET_ID);
  }

  public void setExpirationIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __EXPIRATION_ISSET_ID, value);
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public BrokerMessage setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    setTimestampIsSet(true);
    return this;
  }

  public void unsetTimestamp() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
  }

  /** Returns true if field timestamp is set (has been assigned a value) and false otherwise */
  public boolean isSetTimestamp() {
    return EncodingUtils.testBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
  }

  public void setTimestampIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMESTAMP_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case MESSAGE_ID:
      if (value == null) {
        unsetMessage_id();
      } else {
        setMessage_id((String)value);
      }
      break;

    case PAYLOAD:
      if (value == null) {
        unsetPayload();
      } else {
        setPayload((ByteBuffer)value);
      }
      break;

    case EXPIRATION:
      if (value == null) {
        unsetExpiration();
      } else {
        setExpiration((Long)value);
      }
      break;

    case TIMESTAMP:
      if (value == null) {
        unsetTimestamp();
      } else {
        setTimestamp((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case MESSAGE_ID:
      return getMessage_id();

    case PAYLOAD:
      return getPayload();

    case EXPIRATION:
      return Long.valueOf(getExpiration());

    case TIMESTAMP:
      return Long.valueOf(getTimestamp());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case MESSAGE_ID:
      return isSetMessage_id();
    case PAYLOAD:
      return isSetPayload();
    case EXPIRATION:
      return isSetExpiration();
    case TIMESTAMP:
      return isSetTimestamp();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof BrokerMessage)
      return this.equals((BrokerMessage)that);
    return false;
  }

  public boolean equals(BrokerMessage that) {
    if (that == null)
      return false;

    boolean this_present_message_id = true && this.isSetMessage_id();
    boolean that_present_message_id = true && that.isSetMessage_id();
    if (this_present_message_id || that_present_message_id) {
      if (!(this_present_message_id && that_present_message_id))
        return false;
      if (!this.message_id.equals(that.message_id))
        return false;
    }

    boolean this_present_payload = true && this.isSetPayload();
    boolean that_present_payload = true && that.isSetPayload();
    if (this_present_payload || that_present_payload) {
      if (!(this_present_payload && that_present_payload))
        return false;
      if (!this.payload.equals(that.payload))
        return false;
    }

    boolean this_present_expiration = true && this.isSetExpiration();
    boolean that_present_expiration = true && that.isSetExpiration();
    if (this_present_expiration || that_present_expiration) {
      if (!(this_present_expiration && that_present_expiration))
        return false;
      if (this.expiration != that.expiration)
        return false;
    }

    boolean this_present_timestamp = true && this.isSetTimestamp();
    boolean that_present_timestamp = true && that.isSetTimestamp();
    if (this_present_timestamp || that_present_timestamp) {
      if (!(this_present_timestamp && that_present_timestamp))
        return false;
      if (this.timestamp != that.timestamp)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(BrokerMessage other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetMessage_id()).compareTo(other.isSetMessage_id());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMessage_id()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.message_id, other.message_id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPayload()).compareTo(other.isSetPayload());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPayload()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.payload, other.payload);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExpiration()).compareTo(other.isSetExpiration());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExpiration()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.expiration, other.expiration);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTimestamp()).compareTo(other.isSetTimestamp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimestamp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timestamp, other.timestamp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("BrokerMessage(");
    boolean first = true;

    if (isSetMessage_id()) {
      sb.append("message_id:");
      if (this.message_id == null) {
        sb.append("null");
      } else {
        sb.append(this.message_id);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("payload:");
    if (this.payload == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.payload, sb);
    }
    first = false;
    if (isSetExpiration()) {
      if (!first) sb.append(", ");
      sb.append("expiration:");
      sb.append(this.expiration);
      first = false;
    }
    if (isSetTimestamp()) {
      if (!first) sb.append(", ");
      sb.append("timestamp:");
      sb.append(this.timestamp);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class BrokerMessageStandardSchemeFactory implements SchemeFactory {
    public BrokerMessageStandardScheme getScheme() {
      return new BrokerMessageStandardScheme();
    }
  }

  private static class BrokerMessageStandardScheme extends StandardScheme<BrokerMessage> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, BrokerMessage struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MESSAGE_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.message_id = iprot.readString();
              struct.setMessage_idIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // PAYLOAD
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.payload = iprot.readBinary();
              struct.setPayloadIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // EXPIRATION
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.expiration = iprot.readI64();
              struct.setExpirationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // TIMESTAMP
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.timestamp = iprot.readI64();
              struct.setTimestampIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, BrokerMessage struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.message_id != null) {
        if (struct.isSetMessage_id()) {
          oprot.writeFieldBegin(MESSAGE_ID_FIELD_DESC);
          oprot.writeString(struct.message_id);
          oprot.writeFieldEnd();
        }
      }
      if (struct.payload != null) {
        oprot.writeFieldBegin(PAYLOAD_FIELD_DESC);
        oprot.writeBinary(struct.payload);
        oprot.writeFieldEnd();
      }
      if (struct.isSetExpiration()) {
        oprot.writeFieldBegin(EXPIRATION_FIELD_DESC);
        oprot.writeI64(struct.expiration);
        oprot.writeFieldEnd();
      }
      if (struct.isSetTimestamp()) {
        oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
        oprot.writeI64(struct.timestamp);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class BrokerMessageTupleSchemeFactory implements SchemeFactory {
    public BrokerMessageTupleScheme getScheme() {
      return new BrokerMessageTupleScheme();
    }
  }

  private static class BrokerMessageTupleScheme extends TupleScheme<BrokerMessage> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, BrokerMessage struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetMessage_id()) {
        optionals.set(0);
      }
      if (struct.isSetPayload()) {
        optionals.set(1);
      }
      if (struct.isSetExpiration()) {
        optionals.set(2);
      }
      if (struct.isSetTimestamp()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetMessage_id()) {
        oprot.writeString(struct.message_id);
      }
      if (struct.isSetPayload()) {
        oprot.writeBinary(struct.payload);
      }
      if (struct.isSetExpiration()) {
        oprot.writeI64(struct.expiration);
      }
      if (struct.isSetTimestamp()) {
        oprot.writeI64(struct.timestamp);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, BrokerMessage struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.message_id = iprot.readString();
        struct.setMessage_idIsSet(true);
      }
      if (incoming.get(1)) {
        struct.payload = iprot.readBinary();
        struct.setPayloadIsSet(true);
      }
      if (incoming.get(2)) {
        struct.expiration = iprot.readI64();
        struct.setExpirationIsSet(true);
      }
      if (incoming.get(3)) {
        struct.timestamp = iprot.readI64();
        struct.setTimestampIsSet(true);
      }
    }
  }

}

