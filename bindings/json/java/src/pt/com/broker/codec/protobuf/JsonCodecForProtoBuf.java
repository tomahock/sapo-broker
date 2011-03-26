package pt.com.broker.codec.protobuf;

import java.io.InputStream;
import java.io.OutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import protobuf.codec.Codec;
import protobuf.codec.json.JsonCodec;
import pt.com.broker.codec.protobuf.PBMessage.Atom;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

public class JsonCodecForProtoBuf implements BindingSerializer
{
	private static final Logger log = LoggerFactory.getLogger(JsonCodecForProtoBuf.class);

	private static final ProtoBufBindingSerializer protobuf_serializer = new ProtoBufBindingSerializer();

	private static final Codec codec = new JsonCodec();

	@Override
	public NetMessage unmarshal(InputStream in)
	{
		try
		{
			Atom atom = codec.toMessage(Atom.class, in);
			return protobuf_serializer.constructMessage(atom);
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	@Override
	public NetMessage unmarshal(byte[] packet)
	{
		try
		{
			Atom atom = codec.toMessage(Atom.class, new UnsynchronizedByteArrayInputStream(packet));
			return protobuf_serializer.constructMessage(atom);
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	@Override
	public byte[] marshal(NetMessage message)
	{
		try
		{
			Atom atom = protobuf_serializer.buildAtom(message);

			UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
			codec.fromMessage(atom, out);

			return out.toByteArray();
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
			return null;
		}
	}

	@Override
	public void marshal(NetMessage message, OutputStream out)
	{
		try
		{
			Atom atom = protobuf_serializer.buildAtom(message);
			codec.fromMessage(atom, out);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

	@Override
	public NetProtocolType getProtocolType()
	{
		return NetProtocolType.JSON;
	}
}