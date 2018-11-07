package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.ConsoleHelper;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class HandlerAgent extends Thread {

    private static final Logger LOG = getLogger(HandlerAgent.class);

    private Connection connection;

    public HandlerAgent(Connection connection) {
        this.connection = connection;
        start();
    }

    @Override
    public void run() {
        LOG.debug("HandlerAgent.run");

        try {
            while (true) {
                //Принимаем сообщения агента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        ConsoleHelper.writeMessage(message.getData());
                        if (ServerCondition.rooms.containsKey(connection.getConnectionUUID())) {
                            ServerCondition.rooms.get(connection.getConnectionUUID()).send(message);
                        } else {
                            connection.send(new Message(MessageType.TEXT, "Нет подключенных клиентов"));
                        }
                        break;
                    }
                    case LEAVE: {
                        String connectionUUID = connection.getConnectionUUID();
                        connection.close();
                        if (ServerCondition.rooms.containsKey(connectionUUID)) {
                            ServerCondition.rooms.get(connectionUUID)
                                    .send(new Message(MessageType.TEXT, "Агент разорвал соединение"));
                            ServerCondition.reGetAgent(connectionUUID);
                        }
                        LOG.debug("Agent connection closed");
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
