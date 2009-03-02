package pt.com.xml.codec;

import org.apache.mina.core.buffer.IoBuffer;

import pt.com.types.BindingSerializer;
import pt.com.types.NetMessage;
import pt.com.types.SimpleFramingEncoderV2;

public class SoapEncoderV2 extends SimpleFramingEncoderV2
{
	private static final BindingSerializer serializer = new SoapBindingSerializer();

	@Override
	public void processBody(Object message, IoBuffer wbuf, Short protocolType, Short protocolVersion)
	{
		if (!(message instanceof NetMessage))
		{
			throw new IllegalArgumentException("Not a valid message type for this encoder.");
		}

		NetMessage gcsMessage = (NetMessage) message;
		serializer.marshal(gcsMessage, wbuf.asOutputStream());
	}
}