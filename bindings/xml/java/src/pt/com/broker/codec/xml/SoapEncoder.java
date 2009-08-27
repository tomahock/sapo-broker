package pt.com.broker.codec.xml;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetMessage;
import pt.com.broker.types.SimpleFramingEncoder;

/**
 * Encoder implementation. Used to encode messages exchanged between client and agents.
 * 
 */

public class SoapEncoder extends SimpleFramingEncoder
{
	private static final Logger log = LoggerFactory.getLogger(SoapEncoder.class);

	@Override
	public byte[] processBody(Object message)
	{
		if (!(message instanceof NetMessage))
		{
			String errorMessage = "Message to be encoded is from an unexpected type - " + message.getClass().getName();
			log.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		NetMessage gcsMessage = (NetMessage) message;
		SoapEnvelope soap = Builder.netMessageToSoap(gcsMessage);
		UnsynchByteArrayOutputStream holder = new UnsynchByteArrayOutputStream();
		SoapSerializer.ToXml(soap, holder);
		return holder.toByteArray();
	}

	@Override
	public void processBody(Object message, ProtocolEncoderOutput pout)
	{
		if (!(message instanceof NetMessage))
		{
			String errorMessage = "Message to be encoded is from an unexpected type - " + message.getClass().getName();
			log.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}

		NetMessage gcsMessage = (NetMessage) message;
		SoapEnvelope soap = Builder.netMessageToSoap(gcsMessage);

		IoBuffer wbuf = IoBuffer.allocate(2048, false);
		wbuf.setAutoExpand(true);
		wbuf.putInt(0);
		SoapSerializer.ToXml((SoapEnvelope) soap, wbuf.asOutputStream());
		wbuf.putInt(0, wbuf.position() - 4);
		wbuf.flip();

		pout.write(wbuf);
	}

}