package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class HandlerClient extends Thread {

    private static final Logger LOG = getLogger(HandlerClient.class);

    private Connection connection;

    public HandlerClient(Connection connection) {
        this.connection = connection;
        start();
    }

    @Override
    public void run() {
        LOG.debug("HandlerClient.run");
        HandlerAgent handlerAgent = Server.agentDeque.getFirst();
        Connection agentConnection = handlerAgent.getConnection();
        handlerAgent.setClient(this.connection);

        try {
            while (true) {
                //Принимаем сообщения клиента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        ConsoleHelper.writeMessage(message.getData());
                        agentConnection.send(message);
                        break;
                    }
                    case LEAVE: {
                        connection.close();
                        LOG.debug("Connection closed");
                        Server.agentDeque.addLast(handlerAgent);
                        return;
                    }
                    default: {
                        ConsoleHelper.writeMessage("Error");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
