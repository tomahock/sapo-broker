package pt.com.xml.codec;

import org.caudexorigo.io.UnsynchByteArrayInputStream;

import pt.com.types.Constants;
import pt.com.types.SimpleFramingDecoderV2;
import pt.com.xml.SoapEnvelope;
import pt.com.xml.SoapSerializer;

public class SoapDecoderV2 extends SimpleFramingDecoderV2
{

	public SoapDecoderV2()
	{
		super(Constants.MAX_MESSAGE_SIZE);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		UnsynchByteArrayInputStream bin = new UnsynchByteArrayInputStream(packet);
		SoapEnvelope msg = SoapSerializer.FromXml(bin);

		return Builder.soapToNetMessage(msg);
	}

}
