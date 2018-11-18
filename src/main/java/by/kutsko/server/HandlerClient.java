package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.LogHelper;

import java.io.IOException;

public class HandlerClient extends HandlerConnection {

    HandlerClient(Connection connection) {
        super(connection);
    }

    @Override
    void send(Message message) throws IOException {
        if (ServerCondition.getRooms().containsKey(connectionUUID)) {
            if (!ServerCondition.getRooms().get(connectionUUID).isClosed()) {
                ServerCondition.getRooms().get(connectionUUID).send(message);
            }
        } else {
            connection.send(new Message(MessageType.TEXT,
                    "Server: Нет свободного агента. Пожалуйста подождите"));
        }
    }

    @Override
    void deleteClient() {
        connection.close();
        if (ServerCondition.getRooms().containsKey(connectionUUID)) {
            ServerCondition.returnAgent(connectionUUID);
        } else {
//            ServerCondition.deleteUUID(connectionUUID);
        }
        LOG.debug("HandlerConnection. Client connection closed.");
    }
}
