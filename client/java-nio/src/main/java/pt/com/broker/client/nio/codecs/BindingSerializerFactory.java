package pt.com.broker.client.nio.codecs;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 21-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
final public class BindingSerializerFactory
{

	/**
	 * <p>
	 * getInstance.
	 * </p>
	 *
	 * @param type
	 *            a {@link pt.com.broker.types.NetProtocolType} object.
	 * @return a {@link pt.com.broker.types.BindingSerializer} object.
	 * @throws java.lang.ClassNotFoundException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.InstantiationException
	 *             if any.
	 * @throws java.lang.RuntimeException
	 *             if any.
	 */
	public static BindingSerializer getInstance(NetProtocolType type) throws ClassNotFoundException, IllegalAccessException, InstantiationException, RuntimeException
	{

		BindingSerializer serializer;

		switch (type)
		{
		case SOAP:
			serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.xml.SoapBindingSerializer").newInstance();
			break;
		case PROTOCOL_BUFFER:
			serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.protobuf.ProtoBufBindingSerializer").newInstance();
			break;
		case THRIFT:
			serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.thrift.ThriftBindingSerializer").newInstance();
			break;
		case JSON:
			serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.protobuf.JsonCodecForProtoBuf").newInstance();
			break;
		case SOAP_v0:
			serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.xml.SoapBindingSerializer").newInstance();
			break;
		default:
			throw new RuntimeException("Invalid Protocol Type: " + type.name());
		}

		return serializer;
	}
}
