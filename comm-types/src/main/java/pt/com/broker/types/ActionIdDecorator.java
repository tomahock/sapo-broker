package pt.com.broker.types;

import pt.com.broker.types.DecoratorInterface;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by luissantos on 09-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class ActionIdDecorator extends NetMessage implements DecoratorInterface<NetMessage> {

    private NetMessage netMessage;

    /**
     * <p>Constructor for ActionIdDecorator.</p>
     *
     * @param netMessage a {@link pt.com.broker.types.NetMessage} object.
     */
    public ActionIdDecorator(NetMessage netMessage) {
        super(null);
        this.netMessage = netMessage;

    }


    /** {@inheritDoc} */
    @Override
    public Map<String, String> getHeaders() {
        return netMessage.getHeaders();
    }

    /** {@inheritDoc} */
    @Override
    public NetAction getAction() {
        return netMessage.getAction();
    }


    /**
     * <p>getActionId.</p>
     *
     * @return a {@link java.lang.String} object.
     */
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

    /** {@inheritDoc} */
    @Override
    public NetMessage getInstance() {
        return netMessage;
    }
}
