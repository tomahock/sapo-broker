package pt.com.broker.client.nio.types;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 08-05-2014.
 */
public class DestinationDataFactory {


    public String getSubscription(NetMessage netMessage){

        NetAction netAction = netMessage.getAction();

        NetAction.ActionType actionType = netAction.getActionType();

        String destination = null;

        switch (actionType){

            case NOTIFICATION:
                destination = netAction.getNotificationMessage().getSubscription();
                break;

            case FAULT:
                destination = getDestination(netMessage);
                break;
        }


        return destination;


    }

    public String getDestination(NetMessage netMessage){

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

    public NetAction.DestinationType getDestinationType(NetMessage netMessage){

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

        if(NetFault.PollTimeoutErrorCode.equals(fault.getCode())){

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
