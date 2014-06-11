package pt.com.broker.client.nio.codecs;

import io.netty.buffer.ByteBuf;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.caudexorigo.lang.ArrayUtils;
import org.caudexorigo.text.HexUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the SAPO nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 11-06-2014.
 */
@RunWith(Parameterized.class)
public class TestMessageDecoding {



    List<NetMessage> data = new ArrayList<>(10);

    protected BindingSerializer serializer = null;
    protected byte[] msgData = null;




    public TestMessageDecoding(BindingSerializer serializer, byte[] data) {
        this.serializer = spy(serializer);

        msgData = data;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        List<Object[]> serializers = new ArrayList<>(7);

        String json_msg = "00030000000000677b22616374696f6e223a7b22737562736372696265223a7b2264657374696e6174696f6e223a222f74657374652f222c2264657374696e6174696f6e5f74797065223a225155455545227d2c22616374696f6e5f74797065223a22535542534352494245227d7d";
        String protobuf_msg = "0001000000000011120f2a0b12072f74657374652f18016004";
        String thrift_msg = "00020000000000320c00010d00010b0b00000000000c00020c00050b0002000000072f74657374652f080003000000010008000c000000040000";
        String xml_msg = "000000000000016f3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d38223f3e3c736f61703a456e76656c6f706520786d6c6e733a736f61703d22687474703a2f2f7777772e77332e6f72672f323030332f30352f736f61702d656e76656c6f70652220786d6c6e733a7773613d22687474703a2f2f7777772e77332e6f72672f323030352f30382f61646472657373696e672220786d6c6e733a6d713d22687474703a2f2f73657276696365732e7361706f2e70742f62726f6b6572223e3c736f61703a4865616465722f3e3c736f61703a426f64793e3c6d713a4e6f746966793e3c6d713a44657374696e6174696f6e4e616d653e2f74657374652f3c2f6d713a44657374696e6174696f6e4e616d653e3c6d713a44657374696e6174696f6e547970653e51554555453c2f6d713a44657374696e6174696f6e547970653e3c2f6d713a4e6f746966793e3c2f736f61703a426f64793e3c2f736f61703a456e76656c6f70653e";

        serializers.add(new Object[]{BindingSerializerFactory.getInstance(NetProtocolType.JSON),HexUtil.fromHexString(json_msg)});
        serializers.add(new Object[]{BindingSerializerFactory.getInstance(NetProtocolType.PROTOCOL_BUFFER),HexUtil.fromHexString(protobuf_msg)});
        serializers.add(new Object[]{BindingSerializerFactory.getInstance(NetProtocolType.SOAP),HexUtil.fromHexString(xml_msg)});
        serializers.add(new Object[]{BindingSerializerFactory.getInstance(NetProtocolType.THRIFT),HexUtil.fromHexString(thrift_msg)});

        return serializers;
    }


    @Test()
    public void  testDecoding() throws UnsupportedEncodingException {

        BrokerMessageDecoder encoder = new BrokerMessageDecoder(serializer);

        EmbeddedChannel channel =  createChannel(encoder);

        ByteBuf buf = Unpooled.copiedBuffer(msgData);

        channel.writeInbound(buf);

        byte[] msg = ArrayUtils.subarray(msgData, 8, msgData.length);

        verify(serializer).unmarshal(msg);

        NetMessage netMessage = data.get(0);

        Assert.assertNotNull(netMessage);


        Assert.assertNotNull(netMessage.getAction().getSubscribeMessage());

    }

    protected EmbeddedChannel createChannel(ChannelHandlerAdapter handler){
        return  new EmbeddedChannel(handler,new MockInBoundAdapter());

    }

    public class MockInBoundAdapter extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);


            data.add((NetMessage) msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }
    }
}
