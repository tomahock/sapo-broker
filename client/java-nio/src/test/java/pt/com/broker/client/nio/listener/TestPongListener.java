package pt.com.broker.client.nio.listener;

import io.netty.channel.embedded.EmbeddedChannel;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveFaultHandler;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.*;

import java.util.UUID;

import static org.mockito.Mockito.*;

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
 * Created by Luis Santos<luis.santos@telecom.pt> on 05-06-2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPongListener {

    @Mock() PongListenerAdapter pongListenerAdapter;


    @Test()
    public void testHandlerCallPublic() throws Throwable{

        PongConsumerManager pongConsumerManager = new PongConsumerManager();

        PongMessageHandler pongMessageHandler = new PongMessageHandler(pongConsumerManager);


        String actionId = UUID.randomUUID().toString();

        pongConsumerManager.addSubscription(actionId,pongListenerAdapter);



        EmbeddedChannel channel = new EmbeddedChannel(pongMessageHandler);

        ChannelDecorator decorator = new ChannelDecorator(channel);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        decorator.setHost(host);


        NetMessage message = createPong(actionId);

        channel.writeInbound(message);


        verify(pongListenerAdapter, times(1)).deliverMessage(message, host);
        verify(pongListenerAdapter, times(1)).onMessage(message.getAction().getPongMessage(), host);


    }

    @Test()
    public void testHeartBeatMessage() throws Throwable{


        String actionId = PongMessageHandler.HEART_BEAT_ACTION_ID;

        PongConsumerManager pongConsumerManager = new PongConsumerManager();

        PongMessageHandler pongMessageHandler = new PongMessageHandler(pongConsumerManager);


        pongConsumerManager.addSubscription(actionId,pongListenerAdapter);


        EmbeddedChannel channel = new EmbeddedChannel(pongMessageHandler);

        ChannelDecorator decorator = new ChannelDecorator(channel);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        decorator.setHost(host);


        NetMessage message = createPong(actionId);

        channel.writeInbound(message);


        verify(pongListenerAdapter, never()).deliverMessage(message, host);
        verify(pongListenerAdapter, never()).onMessage(message.getAction().getPongMessage(), host);


    }


    protected NetMessage createPong(String actionId){
        return new NetMessage(new NetAction(new NetPong(actionId)));
    }


    @Test()
    public void testPongConsumerInvalidActionId(){


        String actionId = UUID.randomUUID().toString();

        PongConsumerManager pongConsumerManager = new PongConsumerManager();

        PongMessageHandler pongMessageHandler = new PongMessageHandler(pongConsumerManager);


        EmbeddedChannel channel = new EmbeddedChannel(pongMessageHandler);


        NetMessage message = createPong(actionId);

        channel.writeInbound(message);


    }

}
