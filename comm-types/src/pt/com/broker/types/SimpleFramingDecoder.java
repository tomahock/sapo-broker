package pt.com.broker.types;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleFramingDecoder extends CumulativeProtocolDecoder
{
	private static final Logger log = LoggerFactory.getLogger(SimpleFramingDecoder.class);

	private final int _max_message_size;

	private static final int MIN_HEADER_LENGTH = 4;

	public SimpleFramingDecoder(int max_message_size)
	{
		_max_message_size = max_message_size;
	}

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
	{
		try
		{
			super.decode(session, in, out);
		}
		catch (Throwable e)
		{
			in.clear();
			(session.getHandler()).exceptionCaught(session, e);
		}
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
	{
		try
		{
			// Remember the initial position.
			int start = in.position();

			if (in.remaining() < MIN_HEADER_LENGTH)
			{
				// We didn't receive enough bytes to decode the
				// message length. Cumulate remainder to decode later.
				in.position(start);
				return false;
			}

			int len = in.getInt();

			// We can decode the message length
			if (len > _max_message_size)
			{
				dealWithOversizedMessage(len, _max_message_size, session);
				log.error("Illegal message size!! Received message claimed to have " + len + " bytes.");
				return false;
			}
			else if (len <= 0)
			{
				dealWithOversizedMessage(len, _max_message_size, session);
				log.error("Illegal message size!! Received message claimed to have " + len + " bytes.");
				return false;
			}

			if (in.remaining() < len)
			{
				// We didn't receive enough bytes to decode the message body.
				// Accumulate remainder to decode later.
				in.position(start);
				return false;
			}

			byte[] packet = new byte[len];
			in.get(packet);
			out.write(processBody(packet));

			return true;

		}
		catch (Throwable t)
		{
			session.close(true);
			log.error(t.getMessage(), t);
			return false;
		}
	}

	private void dealWithOversizedMessage(int len, int _max_message_size2, IoSession session)
	{
		session.suspendRead();

		session.write(NetFault.InvalidMessageSizeErrorMessage);

		session.close(false);

	}

	public abstract Object processBody(byte[] packet);

}
