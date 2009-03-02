package pt.com.xml.codec;

import pt.com.types.BindingSerializer;
import pt.com.types.Constants;
import pt.com.types.SimpleFramingDecoderV2;

public class SoapDecoderV2 extends SimpleFramingDecoderV2
{

	private static final BindingSerializer serializer = new SoapBindingSerializer();

	public SoapDecoderV2()
	{
		super(Constants.MAX_MESSAGE_SIZE);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		return serializer.unmarshal(packet);
	}

}
