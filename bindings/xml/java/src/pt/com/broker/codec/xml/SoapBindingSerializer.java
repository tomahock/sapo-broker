package pt.com.broker.codec.xml;

import java.io.InputStream;
import java.io.OutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.stats.EncodingStats;

/**
 * SOAP utility class for encoding and decoding.
 * 
 */
public class SoapBindingSerializer implements BindingSerializer
{

	@Override
	public byte[] marshal(NetMessage message)
	{
		SoapEnvelope soap = Builder.netMessageToSoap(message);
		UnsynchronizedByteArrayOutputStream holder = new UnsynchronizedByteArrayOutputStream();
		SoapSerializer.ToXml(soap, holder);
		byte[] data = holder.toByteArray();
		EncodingStats.newSoapEncodedMessage();
		return data;
	}

	@Override
	public void marshal(NetMessage message, OutputStream out)
	{
		SoapEnvelope soap = Builder.netMessageToSoap(message);
		SoapSerializer.ToXml(soap, out);
		EncodingStats.newSoapEncodedMessage();
	}

	@Override
	public NetMessage unmarshal(byte[] packet)
	{
		UnsynchronizedByteArrayInputStream bin = new UnsynchronizedByteArrayInputStream(packet);
		SoapEnvelope msg = SoapSerializer.FromXml(bin);

		NetMessage message = Builder.soapToNetMessage(msg);
		EncodingStats.newSoapDecodedMessage();
		return message;
	}

	@Override
	public NetMessage unmarshal(InputStream in)
	{
		SoapEnvelope msg = SoapSerializer.FromXml(in);
		NetMessage message = Builder.soapToNetMessage(msg);
		EncodingStats.newSoapDecodedMessage();
		return message;
	}

	@Override
	public NetProtocolType getProtocolType()
	{
		return NetProtocolType.SOAP;
	}
}
