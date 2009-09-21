package pt.com.broker.codec.xml;

import java.io.OutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

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
		return holder.toByteArray();
	}

	@Override
	public void marshal(NetMessage message, OutputStream out)
	{
		SoapEnvelope soap = Builder.netMessageToSoap(message);
		SoapSerializer.ToXml(soap, out);
	}

	@Override
	public NetMessage unmarshal(byte[] packet)
	{
		UnsynchronizedByteArrayInputStream bin = new UnsynchronizedByteArrayInputStream(packet);
		SoapEnvelope msg = SoapSerializer.FromXml(bin);

		return Builder.soapToNetMessage(msg);
	}

}
