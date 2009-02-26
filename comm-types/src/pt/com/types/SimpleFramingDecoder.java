package pt.com.types;

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

	public static final int MIN_HEADER_LENGTH = 4;
	
	private final boolean isNewProto;

	public SimpleFramingDecoder(int max_message_size, boolean isNewProto)
	{
		_max_message_size = max_message_size;
		this.isNewProto = isNewProto;
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
			short protocolType = 0;
			short protocolVersion = 0;

			if (isNewProto)
			{
				if (in.remaining() < MIN_HEADER_LENGTH)
				{
					in.position(start);
					return false;
				}

				protocolType = in.getShort();
				protocolVersion = in.getShort();
			}
			
			// TODO: This could be done only the first time the client sends a message...
			session.setAttribute("PROTOCOL_TYPE", new Short(protocolType));
			session.setAttribute("PROTOCOL_VERSION", new Short(protocolVersion));

			// We can decode the message length
			if (sizeHeader > _max_message_size)
			{
				session.close(true);
				log.error("Illegal message size!! The maximum allowed message size is " + _max_message_size + " bytes.");
			}
			else if (sizeHeader <= 0)
			{
				session.close(true);
				log.error("Illegal message size!! The message lenght must be a positive value.");
			}

			if (in.remaining() < sizeHeader)
			{
				// We didn't receive enough bytes to decode the message body.
				// Accumulate remainder to decode later.
				in.position(start);
				return false;
			}

			byte[] packet = new byte[sizeHeader];
			in.get(packet);
			out.write(processBody(packet, protocolType, protocolVersion));

			return true;

		}
		catch (Throwable t)
		{
			session.close(true);
			log.error(t.getMessage(), t);
			return false;
		}
	}

	public abstract Object processBody(byte[] packet, short protocolType, short protocolVersion);

}
