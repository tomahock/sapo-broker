package pt.com.broker.types;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleFramingDecoderV2 extends CumulativeProtocolDecoder
{
	private static final Logger log = LoggerFactory.getLogger(SimpleFramingDecoderV2.class);

	private final int _max_message_size;

	private static final int MIN_HEADER_LENGTH = 8;

	public SimpleFramingDecoderV2(int max_message_size)
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

			short protocolType = in.getShort();
			short protocolVersion = in.getShort();
			int len = in.getInt();

			// TODO: This could be done only the first time the client sends a
			// message...
			session.setAttribute("PROTOCOL_TYPE", new Short(protocolType));
			session.setAttribute("PROTOCOL_VERSION", new Short(protocolVersion));

			// We can decode the message length
			if (len > _max_message_size)
			{
				dealWithOversizedMessage(len, _max_message_size, session);
				log.error("Illegal message size!! The maximum allowed message size is " + _max_message_size + " bytes.");
				return false;
			}
			else if (len <= 0)
			{
				dealWithOversizedMessage(len, _max_message_size, session);
				log.error("Illegal message size!! The message lenght must be a positive value.");
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
			out.write(processBody(packet, protocolType, protocolVersion));

			return true;

		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
			session.write(NetFault.InvalidMessageFormatErrorMessage);
			return false;
		}
	}

	private void dealWithOversizedMessage(int len, int _max_message_size2, IoSession session)
	{
		session.suspendRead();

		session.write(NetFault.InvalidMessageSizeErrorMessage);

		session.close(false);

	}

	public abstract Object processBody(byte[] packet, short protocolType, short protocolVersion);

}
