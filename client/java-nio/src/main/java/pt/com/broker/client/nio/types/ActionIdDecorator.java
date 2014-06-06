package pt.com.broker.client.nio.types;

import pt.com.broker.client.nio.utils.DecoratorInterface;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by luissantos on 09-05-2014.
 */
public class ActionIdDecorator extends NetMessage implements DecoratorInterface<NetMessage> {

    private NetMessage netMessage;

    public ActionIdDecorator(NetMessage netMessage) {
        super(null);
        this.netMessage = netMessage;

    }


    @Override
    public Map<String, String> getHeaders() {
        return netMessage.getHeaders();
    }

    @Override
    public NetAction getAction() {
        return netMessage.getAction();
    }


    public String getActionId(){

        NetAction netAction = getAction();

        if(netAction.getActionType() == NetAction.ActionType.NOTIFICATION){
            return null;
        }


        Object object = netAction.getNetActionMessage();

        try {

            Method method = object.getClass().getMethod("getActionId");


            return (String)method.invoke(object);


        } catch (Throwable e) {
            return null;
        }


    }

    @Override
    public NetMessage getInstance() {
        return netMessage;
    }
}
