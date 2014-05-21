package pt.com.broker.client.nio.codecs;

import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 21-04-2014.
 */
final public class BindingSerializerFactory {


    public static BindingSerializer getInstance(NetProtocolType type) throws ClassNotFoundException, IllegalAccessException, InstantiationException, RuntimeException {

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
