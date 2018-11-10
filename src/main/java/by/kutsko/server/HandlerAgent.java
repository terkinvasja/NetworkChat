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
    private String connectionUUID;

    public HandlerAgent(Connection connection) {
        this.connection = connection;
        connectionUUID = connection.getConnectionUUID();
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
//                        ConsoleHelper.writeMessage(message.getData());
                        if (ServerCondition.rooms.containsKey(connectionUUID)) {
                            ServerCondition.rooms.get(connectionUUID).send(message);
                        } else {
                            connection.send(new Message(MessageType.TEXT, "Server: Нет подключенных клиентов"));
                        }
                        break;
                    }
                    case LEAVE: {
                        deleteAgent();
                        return;
                    }
                    default: {
                        ConsoleHelper.writeMessage("Error");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                deleteAgent();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deleteAgent() throws IOException {
        connection.close();
        if (ServerCondition.rooms.containsKey(connectionUUID)) {
            Connection clientConnection = ServerCondition.rooms.get(connectionUUID);
            clientConnection.send(new Message(MessageType.TEXT, "Server: Агент разорвал соединение"));
            ServerCondition.reGetAgent(connectionUUID);
        } else {
            ServerCondition.deleteUUID(connectionUUID);
        }
        LOG.debug("Agent connection closed");
    }
}
