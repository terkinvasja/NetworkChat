package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class HandlerConnection extends Thread {

    static final Logger LOG = getLogger(HandlerConnection.class);

    Connection connection;
    String connectionUUID;
    int typeClient;

    HandlerConnection(Connection connection) {
        this.connection = connection;
        connectionUUID = connection.getConnectionUUID();
        start();
    }

    @Override
    public void run() {
        LOG.debug(String.format("HandlerConnection.run %s", typeClient));

        try {
            while (true) {
                //Принимаем сообщения агента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        send(message);
                        break;
                    }
                    case LEAVE: {
                        deleteClient();
                        return;
                    }
                    default: {
                        LOG.debug("Error message type");
                    }
                }
            }
        } catch (IOException e) {
            LOG.debug("", e);
            deleteClient();
        } catch (ClassNotFoundException e) {
            LOG.debug("", e);
        }
    }

    abstract void send(Message message) throws IOException;

    abstract void deleteClient();
}
