package pt.com.broker.client.nio.mocks;

/**
 * Created by luissantos on 12-05-2014.
 */
public class ServerFactory {



    public static SocketServer getInstance(int port){



        SocketServer socketServer = new SocketServer(port);

        try {

            socketServer.bind();

            return socketServer;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    public static SocketServer getInstance(){
        return getInstance(0);
    }
}
