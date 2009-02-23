package pt.com.types;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class SimpleFramingDecoder extends CumulativeProtocolDecoder
{
	private final int _max_message_size;

	public static final int MIN_HEADER_LENGTH = 4;

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

			int sizeHeader = in.getInt();

			// Get msb
			int maskRes = (sizeHeader & (1 << 31));

			int msize = (sizeHeader ^ maskRes);

			if (maskRes != 0)
			{
				if (in.remaining() < MIN_HEADER_LENGTH)
				{
					in.position(start);
					return false;
				}
			}

			short protocolType = 0;
			short protocolVersion = 0;

			if (maskRes != 0)
			{ /* (msb == 1) */
				protocolType = in.getShort();
				protocolVersion = in.getShort();
			}

			// We can decode the message length
			if (msize > _max_message_size)
			{
				throw new IllegalArgumentException("Illegal message size!! The maximum allowed message size is " + _max_message_size + " bytes.");
			}
			else if (msize <= 0)
			{
				throw new IllegalArgumentException("Illegal message size!! The message lenght must be a positive value.");
			}

			if (in.remaining() < msize)
			{
				// We didn't receive enough bytes to decode the message body.
				// Accumulate remainder to decode later.
				in.position(start);
				return false;
			}

			// TODO: This could be done only the first time the client sends a message...
			session.setAttribute("PROTOCOL_TYPE", new Short(protocolType));
			session.setAttribute("PROTOCOL_VERSION", new Short(protocolVersion));

			byte[] packet = new byte[msize];
			in.get(packet);
			out.write(processBody(packet, protocolType, protocolVersion));

			return true;

		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public abstract Object processBody(byte[] packet, short protocolType, short protocolVersion);

}
