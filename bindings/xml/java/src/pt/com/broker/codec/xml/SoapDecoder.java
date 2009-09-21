package pt.com.broker.codec.xml;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;

import pt.com.broker.types.SimpleFramingDecoder;

public class SoapDecoder extends SimpleFramingDecoder
{
	public SoapDecoder(int max_message_size)
	{
		super(max_message_size);
	}

	@Override
	public Object processBody(byte[] packet)
	{
		UnsynchronizedByteArrayInputStream bin = new UnsynchronizedByteArrayInputStream(packet);
		SoapEnvelope msg = SoapSerializer.FromXml(bin);

		return Builder.soapToNetMessage(msg);
	}

}
