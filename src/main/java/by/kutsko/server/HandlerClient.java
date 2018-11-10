package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.ConsoleHelper;
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
        try {
            while (true) {
                //Принимаем сообщения клиента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
//                        ConsoleHelper.writeMessage(message.getData());
                        if (ServerCondition.rooms.containsKey(connection.getConnectionUUID())) {
                            ServerCondition.rooms.get(connection.getConnectionUUID()).send(message);
                        } else {
                            connection.send(new Message(MessageType.TEXT,
                                    "Server: Нет свободного агента. Пожалуйста подождите"));
                        }
                        break;
                    }
                    case LEAVE: {
                        String connectionUUID = connection.getConnectionUUID();
                        connection.close();
                        if (ServerCondition.rooms.containsKey(connectionUUID)) {
                            ServerCondition.returnAgent(connectionUUID);
                        }
                        LOG.debug("Client connection closed");
                        return;
                    }
                    default: {
                        ConsoleHelper.writeMessage("Error");
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
