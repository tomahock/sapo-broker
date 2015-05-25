package pt.com.broker.client.nio.codecs;

import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 3. Neither the name of the SAPO nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 09-06-2014.
 */
@RunWith(Parameterized.class)
public class TestMessageEncoding
{

	List<ByteBuf> data = new ArrayList<>(10);

	protected BindingSerializer serializer = null;

	public TestMessageEncoding(BindingSerializer serializer)
	{
		this.serializer = spy(serializer);
	}

	@Parameterized.Parameters
	public static Collection primeNumbers() throws IllegalAccessException, InstantiationException, ClassNotFoundException
	{

		List<Object[]> serializers = new ArrayList<>(7);

		serializers.add(new Object[] { BindingSerializerFactory.getInstance(NetProtocolType.JSON) });
		serializers.add(new Object[] { BindingSerializerFactory.getInstance(NetProtocolType.PROTOCOL_BUFFER) });
		serializers.add(new Object[] { BindingSerializerFactory.getInstance(NetProtocolType.SOAP) });
		serializers.add(new Object[] { BindingSerializerFactory.getInstance(NetProtocolType.THRIFT) });

		return serializers;
	}

	@Test()
	public void testSerializer()
	{

		BrokerMessageEncoder encoder = new BrokerMessageEncoder(serializer);

		EmbeddedChannel channel = createChannel(encoder);

		NetMessage netMessage = NetFault.InvalidAuthenticationChannelType;

		channel.writeOutbound(netMessage);

		verify(serializer).marshal(netMessage);

		ByteBuf buf = data.get(0);

		Assert.assertNotNull(buf);

		int encType = buf.readShort();

		Assert.assertEquals(encType, getProtocolType(serializer.getProtocolType()));

		int encVersion = buf.readShort();

		Assert.assertEquals(encVersion, 0);

		int len = buf.readInt();

		Assert.assertThat(len, greaterThan(0));

		Assert.assertEquals(len, buf.readableBytes());

		buf.readBytes(len);

		Assert.assertEquals(0, buf.readableBytes());
	}

	private EmbeddedChannel createChannel(ChannelHandler handler)
	{
		return new EmbeddedChannel(
				new MockOutboundHandler(),
				handler);
	}

	private class MockOutboundHandler extends ChannelOutboundHandlerAdapter
	{

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
		{

			data.add(((ByteBuf) msg).copy());

			promise.setSuccess();
		}

		@Override
		public void flush(ChannelHandlerContext ctx) throws Exception
		{

		}
	}

	protected short getProtocolType(NetProtocolType protocolType)
	{

		short proto_type = 0;

		switch (protocolType)
		{
		case SOAP:
			proto_type = 0;
			break;
		case PROTOCOL_BUFFER:
			proto_type = 1;
			break;
		case THRIFT:
			proto_type = 2;
			break;
		case JSON:
			proto_type = 3;
			break;
		case SOAP_v0:
			proto_type = 0;
			break;
		default:
			throw new RuntimeException("Invalid Protocol Type: " + serializer.getProtocolType());
		}

		return proto_type;
	}

}
