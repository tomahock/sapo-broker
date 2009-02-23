package pt.com.gcs.net.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;

import pt.com.gcs.io.SerializerHelper;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.types.SimpleFramingEncoder;

public class GcsEncoder extends SimpleFramingEncoder
{
	@Override
	public byte[] processBody(Object message, Short protocolType, Short protocolVersion)
	{
		if (!(message instanceof InternalMessage))
		{
			// TODO: decide what to do with error (throw RuntimeException?)
			return new byte[0];
		}
		UnsynchByteArrayOutputStream holder = new UnsynchByteArrayOutputStream();
		SerializerHelper.toStream((InternalMessage) message, holder);
		return holder.toByteArray();
	}

	@Override
	public void processBody(Object message, ProtocolEncoderOutput pout, Short protocolType, Short protocolVersion)
	{

		if (!(message instanceof InternalMessage))
		{
			// TODO: decide what to do with error (throw RuntimeException?)
			return;
		}

		IoBuffer wbuf = IoBuffer.allocate(2048, false);
		wbuf.setAutoExpand(true);
		wbuf.putInt(0);
		SerializerHelper.toStream((InternalMessage) message, wbuf.asOutputStream());
		int msize = wbuf.position() - 4;
		wbuf.putInt(0, msize);

		wbuf.flip();

		pout.write(wbuf);
	}

}