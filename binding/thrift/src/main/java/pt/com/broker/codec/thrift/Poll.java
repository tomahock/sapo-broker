/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package pt.com.broker.codec.thrift;

import org.apache.thrift.EncodingUtils;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

import java.util.*;

class Poll implements org.apache.thrift.TBase<Poll, Poll._Fields>, java.io.Serializable, Cloneable, Comparable<Poll> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Poll");

  private static final org.apache.thrift.protocol.TField ACTION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("action_id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField DESTINATION_FIELD_DESC = new org.apache.thrift.protocol.TField("destination", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField TIMEOUT_FIELD_DESC = new org.apache.thrift.protocol.TField("timeout", org.apache.thrift.protocol.TType.I64, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new PollStandardSchemeFactory());
    schemes.put(TupleScheme.class, new PollTupleSchemeFactory());
  }

  public String action_id; // optional
  public String destination; // required
  public long timeout; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ACTION_ID((short)1, "action_id"),
    DESTINATION((short)2, "destination"),
    TIMEOUT((short)3, "timeout");

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
        case 1: // ACTION_ID
          return ACTION_ID;
        case 2: // DESTINATION
          return DESTINATION;
        case 3: // TIMEOUT
          return TIMEOUT;
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
  private static final int __TIMEOUT_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private _Fields optionals[] = {_Fields.ACTION_ID};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ACTION_ID, new org.apache.thrift.meta_data.FieldMetaData("action_id", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.DESTINATION, new org.apache.thrift.meta_data.FieldMetaData("destination", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TIMEOUT, new org.apache.thrift.meta_data.FieldMetaData("timeout", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Poll.class, metaDataMap);
  }

  public Poll() {
  }

  public Poll(
    String destination,
    long timeout)
  {
    this();
    this.destination = destination;
    this.timeout = timeout;
    setTimeoutIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Poll(Poll other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetAction_id()) {
      this.action_id = other.action_id;
    }
    if (other.isSetDestination()) {
      this.destination = other.destination;
    }
    this.timeout = other.timeout;
  }

  public Poll deepCopy() {
    return new Poll(this);
  }

  @Override
  public void clear() {
    this.action_id = null;
    this.destination = null;
    setTimeoutIsSet(false);
    this.timeout = 0;
  }

  public String getAction_id() {
    return this.action_id;
  }

  public Poll setAction_id(String action_id) {
    this.action_id = action_id;
    return this;
  }

  public void unsetAction_id() {
    this.action_id = null;
  }

  /** Returns true if field action_id is set (has been assigned a value) and false otherwise */
  public boolean isSetAction_id() {
    return this.action_id != null;
  }

  public void setAction_idIsSet(boolean value) {
    if (!value) {
      this.action_id = null;
    }
  }

  public String getDestination() {
    return this.destination;
  }

  public Poll setDestination(String destination) {
    this.destination = destination;
    return this;
  }

  public void unsetDestination() {
    this.destination = null;
  }

  /** Returns true if field destination is set (has been assigned a value) and false otherwise */
  public boolean isSetDestination() {
    return this.destination != null;
  }

  public void setDestinationIsSet(boolean value) {
    if (!value) {
      this.destination = null;
    }
  }

  public long getTimeout() {
    return this.timeout;
  }

  public Poll setTimeout(long timeout) {
    this.timeout = timeout;
    setTimeoutIsSet(true);
    return this;
  }

  public void unsetTimeout() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIMEOUT_ISSET_ID);
  }

  /** Returns true if field timeout is set (has been assigned a value) and false otherwise */
  public boolean isSetTimeout() {
    return EncodingUtils.testBit(__isset_bitfield, __TIMEOUT_ISSET_ID);
  }

  public void setTimeoutIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMEOUT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ACTION_ID:
      if (value == null) {
        unsetAction_id();
      } else {
        setAction_id((String)value);
      }
      break;

    case DESTINATION:
      if (value == null) {
        unsetDestination();
      } else {
        setDestination((String)value);
      }
      break;

    case TIMEOUT:
      if (value == null) {
        unsetTimeout();
      } else {
        setTimeout((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ACTION_ID:
      return getAction_id();

    case DESTINATION:
      return getDestination();

    case TIMEOUT:
      return Long.valueOf(getTimeout());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ACTION_ID:
      return isSetAction_id();
    case DESTINATION:
      return isSetDestination();
    case TIMEOUT:
      return isSetTimeout();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Poll)
      return this.equals((Poll)that);
    return false;
  }

  public boolean equals(Poll that) {
    if (that == null)
      return false;

    boolean this_present_action_id = true && this.isSetAction_id();
    boolean that_present_action_id = true && that.isSetAction_id();
    if (this_present_action_id || that_present_action_id) {
      if (!(this_present_action_id && that_present_action_id))
        return false;
      if (!this.action_id.equals(that.action_id))
        return false;
    }

    boolean this_present_destination = true && this.isSetDestination();
    boolean that_present_destination = true && that.isSetDestination();
    if (this_present_destination || that_present_destination) {
      if (!(this_present_destination && that_present_destination))
        return false;
      if (!this.destination.equals(that.destination))
        return false;
    }

    boolean this_present_timeout = true;
    boolean that_present_timeout = true;
    if (this_present_timeout || that_present_timeout) {
      if (!(this_present_timeout && that_present_timeout))
        return false;
      if (this.timeout != that.timeout)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(Poll other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetAction_id()).compareTo(other.isSetAction_id());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAction_id()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.action_id, other.action_id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDestination()).compareTo(other.isSetDestination());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDestination()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.destination, other.destination);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTimeout()).compareTo(other.isSetTimeout());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimeout()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeout, other.timeout);
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
    StringBuilder sb = new StringBuilder("Poll(");
    boolean first = true;

    if (isSetAction_id()) {
      sb.append("action_id:");
      if (this.action_id == null) {
        sb.append("null");
      } else {
        sb.append(this.action_id);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("destination:");
    if (this.destination == null) {
      sb.append("null");
    } else {
      sb.append(this.destination);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("timeout:");
    sb.append(this.timeout);
    first = false;
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

  private static class PollStandardSchemeFactory implements SchemeFactory {
    public PollStandardScheme getScheme() {
      return new PollStandardScheme();
    }
  }

  private static class PollStandardScheme extends StandardScheme<Poll> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Poll struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ACTION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.action_id = iprot.readString();
              struct.setAction_idIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DESTINATION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.destination = iprot.readString();
              struct.setDestinationIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // TIMEOUT
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.timeout = iprot.readI64();
              struct.setTimeoutIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, Poll struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.action_id != null) {
        if (struct.isSetAction_id()) {
          oprot.writeFieldBegin(ACTION_ID_FIELD_DESC);
          oprot.writeString(struct.action_id);
          oprot.writeFieldEnd();
        }
      }
      if (struct.destination != null) {
        oprot.writeFieldBegin(DESTINATION_FIELD_DESC);
        oprot.writeString(struct.destination);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(TIMEOUT_FIELD_DESC);
      oprot.writeI64(struct.timeout);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class PollTupleSchemeFactory implements SchemeFactory {
    public PollTupleScheme getScheme() {
      return new PollTupleScheme();
    }
  }

  private static class PollTupleScheme extends TupleScheme<Poll> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Poll struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetAction_id()) {
        optionals.set(0);
      }
      if (struct.isSetDestination()) {
        optionals.set(1);
      }
      if (struct.isSetTimeout()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetAction_id()) {
        oprot.writeString(struct.action_id);
      }
      if (struct.isSetDestination()) {
        oprot.writeString(struct.destination);
      }
      if (struct.isSetTimeout()) {
        oprot.writeI64(struct.timeout);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Poll struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.action_id = iprot.readString();
        struct.setAction_idIsSet(true);
      }
      if (incoming.get(1)) {
        struct.destination = iprot.readString();
        struct.setDestinationIsSet(true);
      }
      if (incoming.get(2)) {
        struct.timeout = iprot.readI64();
        struct.setTimeoutIsSet(true);
      }
    }
  }

}

