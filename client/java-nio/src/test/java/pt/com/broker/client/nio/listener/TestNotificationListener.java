package pt.com.broker.client.nio.listener;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.tests.mockito.DecoratorMatcher;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.*;

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
public class TestNotificationListener {


    @Mock() BrokerClient bk;



    @Test()
    public void testHandlerCallPublic() throws Throwable{

        ConsumerManager consumerManager = spy(new ConsumerManager());
        NotificationListenerAdapter listener = spy(new MyNotificationListener());

        HostInfo host = new HostInfo("127.0.0.1",3323);

        listener.setBrokerClient(bk);

        NetSubscribe subscribe = new NetSubscribe("/teste/", NetAction.DestinationType.QUEUE);

        consumerManager.addSubscription(subscribe,listener,host);


        ReceiveMessageHandler receiveMessageHandler = new ReceiveMessageHandler(consumerManager);



        EmbeddedChannel channel = new EmbeddedChannel(receiveMessageHandler);

        ChannelDecorator decorator = new ChannelDecorator(channel);

        decorator.setHost(host);

        final NetBrokerMessage netBrokerMessage = new NetBrokerMessage("teste");

        final NetNotification notification = new NetNotification(subscribe.getDestination(),subscribe.getDestinationType(), netBrokerMessage ,subscribe.getDestination());

        NetMessage message = new NetMessage(new NetAction(notification));

        channel.writeInbound(message);


        when(listener.onMessage(notification,host)).thenReturn(true);


        verify(listener, times(1)).deliverMessage(message, host);
        verify(consumerManager, times(1)).deliverMessage(message,host);

        verify(bk).acknowledge((NetNotification) argThat(new DecoratorMatcher(notification)), eq(host));


    }


    public class MyNotificationListener extends   NotificationListenerAdapter {
        @Override
        public boolean onMessage(NetNotification notification, HostInfo host) {
            return true;
        }
    }

}
