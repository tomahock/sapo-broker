package pt.com.broker.client.nio.types;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

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
public class DestinationDataDelegator {


    NetMessage netMessage;

    public DestinationDataDelegator(NetMessage msg) {

        netMessage = msg;
    }

    public String getSubscription(){

        NetAction netAction = netMessage.getAction();

        NetAction.ActionType actionType = netAction.getActionType();

        String destination = null;

        switch (actionType){

            case NOTIFICATION:
                destination = netAction.getNotificationMessage().getSubscription();
                break;

            case FAULT:
                destination = getDestination(netMessage.getAction().getFaultMessage());
                break;
        }


        return destination;


    }

    public String getDestination(){

        NetAction netAction = netMessage.getAction();

        NetAction.ActionType actionType = netAction.getActionType();

        String destination = null;

        switch (actionType){

            case NOTIFICATION:
                destination = netAction.getNotificationMessage().getDestination();
                break;

            case FAULT:
                destination = getDestination(netAction.getFaultMessage());
                break;

        }


        return destination;


    }

    public NetAction.DestinationType getDestinationType(){

        NetAction netAction = netMessage.getAction();

        NetAction.ActionType actionType = netAction.getActionType();


        NetAction.DestinationType destinationType = null;

        switch (actionType){

            case NOTIFICATION:
                destinationType = netAction.getNotificationMessage().getDestinationType();
                break;

            case FAULT:
                destinationType = getDestinationType(netAction.getFaultMessage());
                break;

        }


        return destinationType;
    }

    protected String getDestination(NetFault fault){

        if(NetFault.PollTimeoutErrorCode.equals(fault.getCode())
                || NetFault.NoMessageInQueueErrorCode.equals(fault.getCode())    ){

            return fault.getDetail();

        }


        return null;
    }


    protected NetAction.DestinationType getDestinationType(NetFault fault){

        if(NetFault.PollTimeoutErrorCode.equals(fault.getCode())){

            return NetAction.DestinationType.QUEUE;

        }


        return null;
    }
}
