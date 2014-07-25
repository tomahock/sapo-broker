package pt.com.broker.client.nio.listener;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.mockito.Mockito;
import pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager;
import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.handlers.AcceptMessageHandler;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

import java.util.UUID;

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
 * Created by Luis Santos<luis.santos@telecom.pt> on 06-06-2014.
 */
public class TestAcceptResponseListener {




    @Test()
    public void testAcceptListener() throws Throwable {

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager();

        AcceptResponseListener listener = Mockito.mock(AcceptResponseListener.class);

        AcceptMessageHandler acceptMessageHandler = new AcceptMessageHandler(manager);


        NetAccepted  netAccepted = new NetAccepted(UUID.randomUUID().toString());

        manager.addAcceptRequest(netAccepted.getActionId(),2000,listener);


        NetMessage netMessage = new NetMessage(new NetAction(netAccepted));


        EmbeddedChannel channel = new EmbeddedChannel(acceptMessageHandler);

        ChannelDecorator decorator = new ChannelDecorator(channel);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        decorator.setHost(host);

        channel.writeInbound(netMessage);


        verify(listener).deliverMessage(netMessage,host);
        verify(listener).onMessage(netAccepted,host);


    }

    @Test()
    public void testAcceptListenerTimeout() throws Throwable {

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager();

        AcceptResponseListener listener = Mockito.mock(AcceptResponseListener.class);


        NetAccepted  netAccepted = new NetAccepted(UUID.randomUUID().toString());

        manager.addAcceptRequest(netAccepted.getActionId(),2000,listener);


        Thread.sleep(3000);

        verify(listener).onTimeout(netAccepted.getActionId());

    }

    @Test()
    public void testAcceptListenerFault() throws Throwable {

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager();

        AcceptResponseListener listener = Mockito.mock(AcceptResponseListener.class);

        AcceptMessageHandler acceptMessageHandler = new AcceptMessageHandler(manager);

        String actionId = UUID.randomUUID().toString();

        NetFault fault = NetFault.AccessDeniedErrorMessage.getAction().getFaultMessage();

        fault.setActionId(actionId);

        manager.addAcceptRequest(actionId,2000,listener);


        NetMessage netMessage = new NetMessage(new NetAction(fault));


        EmbeddedChannel channel = new EmbeddedChannel(acceptMessageHandler);

        ChannelDecorator decorator = new ChannelDecorator(channel);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        decorator.setHost(host);

        channel.writeInbound(netMessage);


        verify(listener).deliverMessage(netMessage,host);
        verify(listener).onFault(fault,host);

    }
}
