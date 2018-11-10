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
    private String connectionUUID;
    private int typeClient;

    public HandlerClient(Connection connection, int typeClient) {
        this.connection = connection;
        this.typeClient = typeClient;
        connectionUUID = connection.getConnectionUUID();
        start();
    }

    @Override
    public void run() {
        LOG.debug(String.format("HandlerClient.run %s", typeClient));

        try {
            while (true) {
                //Принимаем сообщения агента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        if (ServerCondition.rooms.containsKey(connectionUUID)) {
                            if (!ServerCondition.rooms.get(connectionUUID).isClosed()) {
                                ServerCondition.rooms.get(connectionUUID).send(message);
                            }
                        } else {
                            if (typeClient == 0) {
                                connection.send(new Message(MessageType.TEXT,
                                        "Server: Нет подключенных клиентов"));
                            } else {
                                connection.send(new Message(MessageType.TEXT,
                                        "Server: Нет свободного агента. Пожалуйста подождите"));
                            }
                        }
                        break;
                    }
                    case LEAVE: {
                        deleteClient();
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
                deleteClient();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void deleteClient() throws IOException {
        connection.close();
        if (ServerCondition.rooms.containsKey(connectionUUID)) {
            if (typeClient == 0) {
//                Connection clientConnection = ServerCondition.rooms.get(connectionUUID);
//                clientConnection.send(new Message(MessageType.TEXT, "Server: Агент разорвал соединение. Подождите пока подключится новый агент."));
                ServerCondition.reGetAgent(connectionUUID);
            } else {
                ServerCondition.returnAgent(connectionUUID);
            }
        } else {
            ServerCondition.deleteUUID(connectionUUID);
        }
        LOG.debug("HandlerClient. Client connection closed.");
    }
}
