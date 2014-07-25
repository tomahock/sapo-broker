/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package pt.com.broker.codec.thrift;

import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

import java.util.*;

class Fault implements org.apache.thrift.TBase<Fault, Fault._Fields>, java.io.Serializable, Cloneable, Comparable<Fault> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Fault");

  private static final org.apache.thrift.protocol.TField ACTION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("action_id", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField FAULT_CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("fault_code", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField FAULT_MESSAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("fault_message", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField FAULT_DETAIL_FIELD_DESC = new org.apache.thrift.protocol.TField("fault_detail", org.apache.thrift.protocol.TType.STRING, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new FaultStandardSchemeFactory());
    schemes.put(TupleScheme.class, new FaultTupleSchemeFactory());
  }

  public String action_id; // optional
  public String fault_code; // required
  public String fault_message; // required
  public String fault_detail; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ACTION_ID((short)1, "action_id"),
    FAULT_CODE((short)2, "fault_code"),
    FAULT_MESSAGE((short)3, "fault_message"),
    FAULT_DETAIL((short)4, "fault_detail");

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
        case 2: // FAULT_CODE
          return FAULT_CODE;
        case 3: // FAULT_MESSAGE
          return FAULT_MESSAGE;
        case 4: // FAULT_DETAIL
          return FAULT_DETAIL;
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
  private _Fields optionals[] = {_Fields.ACTION_ID,_Fields.FAULT_DETAIL};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ACTION_ID, new org.apache.thrift.meta_data.FieldMetaData("action_id", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FAULT_CODE, new org.apache.thrift.meta_data.FieldMetaData("fault_code", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FAULT_MESSAGE, new org.apache.thrift.meta_data.FieldMetaData("fault_message", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FAULT_DETAIL, new org.apache.thrift.meta_data.FieldMetaData("fault_detail", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Fault.class, metaDataMap);
  }

  public Fault() {
  }

  public Fault(
    String fault_code,
    String fault_message)
  {
    this();
    this.fault_code = fault_code;
    this.fault_message = fault_message;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Fault(Fault other) {
    if (other.isSetAction_id()) {
      this.action_id = other.action_id;
    }
    if (other.isSetFault_code()) {
      this.fault_code = other.fault_code;
    }
    if (other.isSetFault_message()) {
      this.fault_message = other.fault_message;
    }
    if (other.isSetFault_detail()) {
      this.fault_detail = other.fault_detail;
    }
  }

  public Fault deepCopy() {
    return new Fault(this);
  }

  @Override
  public void clear() {
    this.action_id = null;
    this.fault_code = null;
    this.fault_message = null;
    this.fault_detail = null;
  }

  public String getAction_id() {
    return this.action_id;
  }

  public Fault setAction_id(String action_id) {
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

  public String getFault_code() {
    return this.fault_code;
  }

  public Fault setFault_code(String fault_code) {
    this.fault_code = fault_code;
    return this;
  }

  public void unsetFault_code() {
    this.fault_code = null;
  }

  /** Returns true if field fault_code is set (has been assigned a value) and false otherwise */
  public boolean isSetFault_code() {
    return this.fault_code != null;
  }

  public void setFault_codeIsSet(boolean value) {
    if (!value) {
      this.fault_code = null;
    }
  }

  public String getFault_message() {
    return this.fault_message;
  }

  public Fault setFault_message(String fault_message) {
    this.fault_message = fault_message;
    return this;
  }

  public void unsetFault_message() {
    this.fault_message = null;
  }

  /** Returns true if field fault_message is set (has been assigned a value) and false otherwise */
  public boolean isSetFault_message() {
    return this.fault_message != null;
  }

  public void setFault_messageIsSet(boolean value) {
    if (!value) {
      this.fault_message = null;
    }
  }

  public String getFault_detail() {
    return this.fault_detail;
  }

  public Fault setFault_detail(String fault_detail) {
    this.fault_detail = fault_detail;
    return this;
  }

  public void unsetFault_detail() {
    this.fault_detail = null;
  }

  /** Returns true if field fault_detail is set (has been assigned a value) and false otherwise */
  public boolean isSetFault_detail() {
    return this.fault_detail != null;
  }

  public void setFault_detailIsSet(boolean value) {
    if (!value) {
      this.fault_detail = null;
    }
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

    case FAULT_CODE:
      if (value == null) {
        unsetFault_code();
      } else {
        setFault_code((String)value);
      }
      break;

    case FAULT_MESSAGE:
      if (value == null) {
        unsetFault_message();
      } else {
        setFault_message((String)value);
      }
      break;

    case FAULT_DETAIL:
      if (value == null) {
        unsetFault_detail();
      } else {
        setFault_detail((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ACTION_ID:
      return getAction_id();

    case FAULT_CODE:
      return getFault_code();

    case FAULT_MESSAGE:
      return getFault_message();

    case FAULT_DETAIL:
      return getFault_detail();

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
    case FAULT_CODE:
      return isSetFault_code();
    case FAULT_MESSAGE:
      return isSetFault_message();
    case FAULT_DETAIL:
      return isSetFault_detail();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Fault)
      return this.equals((Fault)that);
    return false;
  }

  public boolean equals(Fault that) {
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

    boolean this_present_fault_code = true && this.isSetFault_code();
    boolean that_present_fault_code = true && that.isSetFault_code();
    if (this_present_fault_code || that_present_fault_code) {
      if (!(this_present_fault_code && that_present_fault_code))
        return false;
      if (!this.fault_code.equals(that.fault_code))
        return false;
    }

    boolean this_present_fault_message = true && this.isSetFault_message();
    boolean that_present_fault_message = true && that.isSetFault_message();
    if (this_present_fault_message || that_present_fault_message) {
      if (!(this_present_fault_message && that_present_fault_message))
        return false;
      if (!this.fault_message.equals(that.fault_message))
        return false;
    }

    boolean this_present_fault_detail = true && this.isSetFault_detail();
    boolean that_present_fault_detail = true && that.isSetFault_detail();
    if (this_present_fault_detail || that_present_fault_detail) {
      if (!(this_present_fault_detail && that_present_fault_detail))
        return false;
      if (!this.fault_detail.equals(that.fault_detail))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(Fault other) {
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
    lastComparison = Boolean.valueOf(isSetFault_code()).compareTo(other.isSetFault_code());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFault_code()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fault_code, other.fault_code);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFault_message()).compareTo(other.isSetFault_message());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFault_message()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fault_message, other.fault_message);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFault_detail()).compareTo(other.isSetFault_detail());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFault_detail()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fault_detail, other.fault_detail);
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
    StringBuilder sb = new StringBuilder("Fault(");
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
    sb.append("fault_code:");
    if (this.fault_code == null) {
      sb.append("null");
    } else {
      sb.append(this.fault_code);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("fault_message:");
    if (this.fault_message == null) {
      sb.append("null");
    } else {
      sb.append(this.fault_message);
    }
    first = false;
    if (isSetFault_detail()) {
      if (!first) sb.append(", ");
      sb.append("fault_detail:");
      if (this.fault_detail == null) {
        sb.append("null");
      } else {
        sb.append(this.fault_detail);
      }
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class FaultStandardSchemeFactory implements SchemeFactory {
    public FaultStandardScheme getScheme() {
      return new FaultStandardScheme();
    }
  }

  private static class FaultStandardScheme extends StandardScheme<Fault> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Fault struct) throws org.apache.thrift.TException {
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
          case 2: // FAULT_CODE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.fault_code = iprot.readString();
              struct.setFault_codeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // FAULT_MESSAGE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.fault_message = iprot.readString();
              struct.setFault_messageIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // FAULT_DETAIL
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.fault_detail = iprot.readString();
              struct.setFault_detailIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, Fault struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.action_id != null) {
        if (struct.isSetAction_id()) {
          oprot.writeFieldBegin(ACTION_ID_FIELD_DESC);
          oprot.writeString(struct.action_id);
          oprot.writeFieldEnd();
        }
      }
      if (struct.fault_code != null) {
        oprot.writeFieldBegin(FAULT_CODE_FIELD_DESC);
        oprot.writeString(struct.fault_code);
        oprot.writeFieldEnd();
      }
      if (struct.fault_message != null) {
        oprot.writeFieldBegin(FAULT_MESSAGE_FIELD_DESC);
        oprot.writeString(struct.fault_message);
        oprot.writeFieldEnd();
      }
      if (struct.fault_detail != null) {
        if (struct.isSetFault_detail()) {
          oprot.writeFieldBegin(FAULT_DETAIL_FIELD_DESC);
          oprot.writeString(struct.fault_detail);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class FaultTupleSchemeFactory implements SchemeFactory {
    public FaultTupleScheme getScheme() {
      return new FaultTupleScheme();
    }
  }

  private static class FaultTupleScheme extends TupleScheme<Fault> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Fault struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetAction_id()) {
        optionals.set(0);
      }
      if (struct.isSetFault_code()) {
        optionals.set(1);
      }
      if (struct.isSetFault_message()) {
        optionals.set(2);
      }
      if (struct.isSetFault_detail()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetAction_id()) {
        oprot.writeString(struct.action_id);
      }
      if (struct.isSetFault_code()) {
        oprot.writeString(struct.fault_code);
      }
      if (struct.isSetFault_message()) {
        oprot.writeString(struct.fault_message);
      }
      if (struct.isSetFault_detail()) {
        oprot.writeString(struct.fault_detail);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Fault struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.action_id = iprot.readString();
        struct.setAction_idIsSet(true);
      }
      if (incoming.get(1)) {
        struct.fault_code = iprot.readString();
        struct.setFault_codeIsSet(true);
      }
      if (incoming.get(2)) {
        struct.fault_message = iprot.readString();
        struct.setFault_messageIsSet(true);
      }
      if (incoming.get(3)) {
        struct.fault_detail = iprot.readString();
        struct.setFault_detailIsSet(true);
      }
    }
  }

}

