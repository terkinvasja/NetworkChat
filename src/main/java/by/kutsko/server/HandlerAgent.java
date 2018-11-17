package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.LogHelper;

import java.io.IOException;

public class HandlerAgent extends HandlerConnection {

    public HandlerAgent(Connection connection) {
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
                    "Server: Нет подключенных клиентов"));
        }
    }

    @Override
    void deleteClient() {
        connection.close();
        if (ServerCondition.getRooms().containsKey(connectionUUID)) {
            Connection clientConnection = ServerCondition.getRooms().get(connectionUUID);
            try {
                clientConnection.send(new Message(MessageType.TEXT,
                        "Server: Агент разорвал соединение. Подождите пока подключится новый агент."));
            } catch (IOException e) {
                LOG.debug(LogHelper.exceptionToString(e));
            }
            ServerCondition.reGetAgent(connectionUUID);
        } else {
            ServerCondition.deleteUUID(connectionUUID);
        }
        LOG.debug("HandlerConnection. Client connection closed.");
    }
}
