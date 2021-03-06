/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package pt.com.broker.codec.thrift;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

class Subscribe implements org.apache.thrift.TBase<Subscribe, Subscribe._Fields>, java.io.Serializable, Cloneable, Comparable<Subscribe>
{
	private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Subscribe");

	private static final org.apache.thrift.protocol.TField ACTION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("action_id", org.apache.thrift.protocol.TType.STRING, (short) 1);
	private static final org.apache.thrift.protocol.TField DESTINATION_FIELD_DESC = new org.apache.thrift.protocol.TField("destination", org.apache.thrift.protocol.TType.STRING, (short) 2);
	private static final org.apache.thrift.protocol.TField DESTINATION_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("destination_type", org.apache.thrift.protocol.TType.I32, (short) 3);

	private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
	static
	{
		schemes.put(StandardScheme.class, new SubscribeStandardSchemeFactory());
		schemes.put(TupleScheme.class, new SubscribeTupleSchemeFactory());
	}

	public String action_id; // optional
	public String destination; // required
	/**
	 * 
	 * @see DestinationType
	 */
	public DestinationType destination_type; // required

	/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
	public enum _Fields implements org.apache.thrift.TFieldIdEnum
	{
		ACTION_ID((short) 1, "action_id"),
		DESTINATION((short) 2, "destination"),
		/**
		 * 
		 * @see DestinationType
		 */
		DESTINATION_TYPE((short) 3, "destination_type");

		private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

		static
		{
			for (_Fields field : EnumSet.allOf(_Fields.class))
			{
				byName.put(field.getFieldName(), field);
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, or null if its not found.
		 */
		public static _Fields findByThriftId(int fieldId)
		{
			switch (fieldId)
			{
			case 1: // ACTION_ID
				return ACTION_ID;
			case 2: // DESTINATION
				return DESTINATION;
			case 3: // DESTINATION_TYPE
				return DESTINATION_TYPE;
			default:
				return null;
			}
		}

		/**
		 * Find the _Fields constant that matches fieldId, throwing an exception if it is not found.
		 */
		public static _Fields findByThriftIdOrThrow(int fieldId)
		{
			_Fields fields = findByThriftId(fieldId);
			if (fields == null)
				throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
			return fields;
		}

		/**
		 * Find the _Fields constant that matches name, or null if its not found.
		 */
		public static _Fields findByName(String name)
		{
			return byName.get(name);
		}

		private final short _thriftId;
		private final String _fieldName;

		_Fields(short thriftId, String fieldName)
		{
			_thriftId = thriftId;
			_fieldName = fieldName;
		}

		public short getThriftFieldId()
		{
			return _thriftId;
		}

		public String getFieldName()
		{
			return _fieldName;
		}
	}

	// isset id assignments
	private _Fields optionals[] = { _Fields.ACTION_ID };
	public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
	static
	{
		Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
		tmpMap.put(_Fields.ACTION_ID, new org.apache.thrift.meta_data.FieldMetaData("action_id", org.apache.thrift.TFieldRequirementType.OPTIONAL,
				new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
		tmpMap.put(_Fields.DESTINATION, new org.apache.thrift.meta_data.FieldMetaData("destination", org.apache.thrift.TFieldRequirementType.DEFAULT,
				new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
		tmpMap.put(_Fields.DESTINATION_TYPE, new org.apache.thrift.meta_data.FieldMetaData("destination_type", org.apache.thrift.TFieldRequirementType.DEFAULT,
				new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, DestinationType.class)));
		metaDataMap = Collections.unmodifiableMap(tmpMap);
		org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Subscribe.class, metaDataMap);
	}

	public Subscribe()
	{
	}

	public Subscribe(
			String destination,
			DestinationType destination_type)
	{
		this();
		this.destination = destination;
		this.destination_type = destination_type;
	}

	/**
	 * Performs a deep copy on <i>other</i>.
	 */
	public Subscribe(Subscribe other)
	{
		if (other.isSetAction_id())
		{
			this.action_id = other.action_id;
		}
		if (other.isSetDestination())
		{
			this.destination = other.destination;
		}
		if (other.isSetDestination_type())
		{
			this.destination_type = other.destination_type;
		}
	}

	public Subscribe deepCopy()
	{
		return new Subscribe(this);
	}

	@Override
	public void clear()
	{
		this.action_id = null;
		this.destination = null;
		this.destination_type = null;
	}

	public String getAction_id()
	{
		return this.action_id;
	}

	public Subscribe setAction_id(String action_id)
	{
		this.action_id = action_id;
		return this;
	}

	public void unsetAction_id()
	{
		this.action_id = null;
	}

	/** Returns true if field action_id is set (has been assigned a value) and false otherwise */
	public boolean isSetAction_id()
	{
		return this.action_id != null;
	}

	public void setAction_idIsSet(boolean value)
	{
		if (!value)
		{
			this.action_id = null;
		}
	}

	public String getDestination()
	{
		return this.destination;
	}

	public Subscribe setDestination(String destination)
	{
		this.destination = destination;
		return this;
	}

	public void unsetDestination()
	{
		this.destination = null;
	}

	/** Returns true if field destination is set (has been assigned a value) and false otherwise */
	public boolean isSetDestination()
	{
		return this.destination != null;
	}

	public void setDestinationIsSet(boolean value)
	{
		if (!value)
		{
			this.destination = null;
		}
	}

	/**
	 * 
	 * @see DestinationType
	 */
	public DestinationType getDestination_type()
	{
		return this.destination_type;
	}

	/**
	 * 
	 * @see DestinationType
	 */
	public Subscribe setDestination_type(DestinationType destination_type)
	{
		this.destination_type = destination_type;
		return this;
	}

	public void unsetDestination_type()
	{
		this.destination_type = null;
	}

	/** Returns true if field destination_type is set (has been assigned a value) and false otherwise */
	public boolean isSetDestination_type()
	{
		return this.destination_type != null;
	}

	public void setDestination_typeIsSet(boolean value)
	{
		if (!value)
		{
			this.destination_type = null;
		}
	}

	public void setFieldValue(_Fields field, Object value)
	{
		switch (field)
		{
		case ACTION_ID:
			if (value == null)
			{
				unsetAction_id();
			}
			else
			{
				setAction_id((String) value);
			}
			break;

		case DESTINATION:
			if (value == null)
			{
				unsetDestination();
			}
			else
			{
				setDestination((String) value);
			}
			break;

		case DESTINATION_TYPE:
			if (value == null)
			{
				unsetDestination_type();
			}
			else
			{
				setDestination_type((DestinationType) value);
			}
			break;

		}
	}

	public Object getFieldValue(_Fields field)
	{
		switch (field)
		{
		case ACTION_ID:
			return getAction_id();

		case DESTINATION:
			return getDestination();

		case DESTINATION_TYPE:
			return getDestination_type();

		}
		throw new IllegalStateException();
	}

	/** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
	public boolean isSet(_Fields field)
	{
		if (field == null)
		{
			throw new IllegalArgumentException();
		}

		switch (field)
		{
		case ACTION_ID:
			return isSetAction_id();
		case DESTINATION:
			return isSetDestination();
		case DESTINATION_TYPE:
			return isSetDestination_type();
		}
		throw new IllegalStateException();
	}

	@Override
	public boolean equals(Object that)
	{
		if (that == null)
			return false;
		if (that instanceof Subscribe)
			return this.equals((Subscribe) that);
		return false;
	}

	public boolean equals(Subscribe that)
	{
		if (that == null)
			return false;

		boolean this_present_action_id = true && this.isSetAction_id();
		boolean that_present_action_id = true && that.isSetAction_id();
		if (this_present_action_id || that_present_action_id)
		{
			if (!(this_present_action_id && that_present_action_id))
				return false;
			if (!this.action_id.equals(that.action_id))
				return false;
		}

		boolean this_present_destination = true && this.isSetDestination();
		boolean that_present_destination = true && that.isSetDestination();
		if (this_present_destination || that_present_destination)
		{
			if (!(this_present_destination && that_present_destination))
				return false;
			if (!this.destination.equals(that.destination))
				return false;
		}

		boolean this_present_destination_type = true && this.isSetDestination_type();
		boolean that_present_destination_type = true && that.isSetDestination_type();
		if (this_present_destination_type || that_present_destination_type)
		{
			if (!(this_present_destination_type && that_present_destination_type))
				return false;
			if (!this.destination_type.equals(that.destination_type))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return 0;
	}

	@Override
	public int compareTo(Subscribe other)
	{
		if (!getClass().equals(other.getClass()))
		{
			return getClass().getName().compareTo(other.getClass().getName());
		}

		int lastComparison = 0;

		lastComparison = Boolean.valueOf(isSetAction_id()).compareTo(other.isSetAction_id());
		if (lastComparison != 0)
		{
			return lastComparison;
		}
		if (isSetAction_id())
		{
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.action_id, other.action_id);
			if (lastComparison != 0)
			{
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetDestination()).compareTo(other.isSetDestination());
		if (lastComparison != 0)
		{
			return lastComparison;
		}
		if (isSetDestination())
		{
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.destination, other.destination);
			if (lastComparison != 0)
			{
				return lastComparison;
			}
		}
		lastComparison = Boolean.valueOf(isSetDestination_type()).compareTo(other.isSetDestination_type());
		if (lastComparison != 0)
		{
			return lastComparison;
		}
		if (isSetDestination_type())
		{
			lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.destination_type, other.destination_type);
			if (lastComparison != 0)
			{
				return lastComparison;
			}
		}
		return 0;
	}

	public _Fields fieldForId(int fieldId)
	{
		return _Fields.findByThriftId(fieldId);
	}

	public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException
	{
		schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
	}

	public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException
	{
		schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("Subscribe(");
		boolean first = true;

		if (isSetAction_id())
		{
			sb.append("action_id:");
			if (this.action_id == null)
			{
				sb.append("null");
			}
			else
			{
				sb.append(this.action_id);
			}
			first = false;
		}
		if (!first)
			sb.append(", ");
		sb.append("destination:");
		if (this.destination == null)
		{
			sb.append("null");
		}
		else
		{
			sb.append(this.destination);
		}
		first = false;
		if (!first)
			sb.append(", ");
		sb.append("destination_type:");
		if (this.destination_type == null)
		{
			sb.append("null");
		}
		else
		{
			sb.append(this.destination_type);
		}
		first = false;
		sb.append(")");
		return sb.toString();
	}

	public void validate() throws org.apache.thrift.TException
	{
		// check for required fields
		// check for sub-struct validity
	}

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException
	{
		try
		{
			write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
		}
		catch (org.apache.thrift.TException te)
		{
			throw new java.io.IOException(te);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		try
		{
			read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
		}
		catch (org.apache.thrift.TException te)
		{
			throw new java.io.IOException(te);
		}
	}

	private static class SubscribeStandardSchemeFactory implements SchemeFactory
	{
		public SubscribeStandardScheme getScheme()
		{
			return new SubscribeStandardScheme();
		}
	}

	private static class SubscribeStandardScheme extends StandardScheme<Subscribe>
	{

		public void read(org.apache.thrift.protocol.TProtocol iprot, Subscribe struct) throws org.apache.thrift.TException
		{
			org.apache.thrift.protocol.TField schemeField;
			iprot.readStructBegin();
			while (true)
			{
				schemeField = iprot.readFieldBegin();
				if (schemeField.type == org.apache.thrift.protocol.TType.STOP)
				{
					break;
				}
				switch (schemeField.id)
				{
				case 1: // ACTION_ID
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING)
					{
						struct.action_id = iprot.readString();
						struct.setAction_idIsSet(true);
					}
					else
					{
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 2: // DESTINATION
					if (schemeField.type == org.apache.thrift.protocol.TType.STRING)
					{
						struct.destination = iprot.readString();
						struct.setDestinationIsSet(true);
					}
					else
					{
						org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
					}
					break;
				case 3: // DESTINATION_TYPE
					if (schemeField.type == org.apache.thrift.protocol.TType.I32)
					{
						struct.destination_type = DestinationType.findByValue(iprot.readI32());
						struct.setDestination_typeIsSet(true);
					}
					else
					{
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

		public void write(org.apache.thrift.protocol.TProtocol oprot, Subscribe struct) throws org.apache.thrift.TException
		{
			struct.validate();

			oprot.writeStructBegin(STRUCT_DESC);
			if (struct.action_id != null)
			{
				if (struct.isSetAction_id())
				{
					oprot.writeFieldBegin(ACTION_ID_FIELD_DESC);
					oprot.writeString(struct.action_id);
					oprot.writeFieldEnd();
				}
			}
			if (struct.destination != null)
			{
				oprot.writeFieldBegin(DESTINATION_FIELD_DESC);
				oprot.writeString(struct.destination);
				oprot.writeFieldEnd();
			}
			if (struct.destination_type != null)
			{
				oprot.writeFieldBegin(DESTINATION_TYPE_FIELD_DESC);
				oprot.writeI32(struct.destination_type.getValue());
				oprot.writeFieldEnd();
			}
			oprot.writeFieldStop();
			oprot.writeStructEnd();
		}

	}

	private static class SubscribeTupleSchemeFactory implements SchemeFactory
	{
		public SubscribeTupleScheme getScheme()
		{
			return new SubscribeTupleScheme();
		}
	}

	private static class SubscribeTupleScheme extends TupleScheme<Subscribe>
	{

		@Override
		public void write(org.apache.thrift.protocol.TProtocol prot, Subscribe struct) throws org.apache.thrift.TException
		{
			TTupleProtocol oprot = (TTupleProtocol) prot;
			BitSet optionals = new BitSet();
			if (struct.isSetAction_id())
			{
				optionals.set(0);
			}
			if (struct.isSetDestination())
			{
				optionals.set(1);
			}
			if (struct.isSetDestination_type())
			{
				optionals.set(2);
			}
			oprot.writeBitSet(optionals, 3);
			if (struct.isSetAction_id())
			{
				oprot.writeString(struct.action_id);
			}
			if (struct.isSetDestination())
			{
				oprot.writeString(struct.destination);
			}
			if (struct.isSetDestination_type())
			{
				oprot.writeI32(struct.destination_type.getValue());
			}
		}

		@Override
		public void read(org.apache.thrift.protocol.TProtocol prot, Subscribe struct) throws org.apache.thrift.TException
		{
			TTupleProtocol iprot = (TTupleProtocol) prot;
			BitSet incoming = iprot.readBitSet(3);
			if (incoming.get(0))
			{
				struct.action_id = iprot.readString();
				struct.setAction_idIsSet(true);
			}
			if (incoming.get(1))
			{
				struct.destination = iprot.readString();
				struct.setDestinationIsSet(true);
			}
			if (incoming.get(2))
			{
				struct.destination_type = DestinationType.findByValue(iprot.readI32());
				struct.setDestination_typeIsSet(true);
			}
		}
	}

}
