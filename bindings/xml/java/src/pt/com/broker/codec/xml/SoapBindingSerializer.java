package pt.com.broker.codec.xml;

import java.io.OutputStream;

import org.caudexorigo.io.UnsynchByteArrayInputStream;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;

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
		UnsynchByteArrayOutputStream holder = new UnsynchByteArrayOutputStream();
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
		UnsynchByteArrayInputStream bin = new UnsynchByteArrayInputStream(packet);
		SoapEnvelope msg = SoapSerializer.FromXml(bin);

		return Builder.soapToNetMessage(msg);
	}

}
