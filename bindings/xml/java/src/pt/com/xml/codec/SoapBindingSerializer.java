package pt.com.xml.codec;

import java.io.OutputStream;

import org.caudexorigo.io.UnsynchByteArrayInputStream;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;

import pt.com.types.BindingSerializer;
import pt.com.types.NetMessage;
import pt.com.xml.SoapEnvelope;
import pt.com.xml.SoapSerializer;

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
