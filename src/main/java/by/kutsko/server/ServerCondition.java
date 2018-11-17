package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import static org.slf4j.LoggerFactory.getLogger;

class ServerCondition {
    private static final Logger LOG = getLogger(ServerCondition.class);

    private static final LinkedList<Connection> agentList = new LinkedList<>();
    private static final LinkedList<Connection> clientList = new LinkedList<>();
    private static final HashMap<String, Connection> rooms = new HashMap<>();

    static void addAgent(Connection connection) {
        synchronized (agentList) {
            agentList.add(connection);
        }
    }

    static void addClient(Connection connection) {
        synchronized (clientList) {
            clientList.add(connection);
        }
    }

    static HashMap<String, Connection> getRooms() {
        synchronized (rooms) {
            return rooms;
        }
    }

    static synchronized void getAgent() {
        Connection agentConnection;
        Connection clientConnection;

        agentConnection = searchValidConnection(agentList);
        clientConnection = searchValidConnection(clientList);

        if ((agentConnection != null) && (clientConnection != null)) {
            rooms.put(agentConnection.getConnectionUUID(), clientConnection);
            rooms.put(clientConnection.getConnectionUUID(), agentConnection);
            LOG.debug(String.format("%s and %s start chat.",
                    agentConnection.getConnectionUUID(), clientConnection.getConnectionUUID()));
            try {
                agentConnection.send(new Message(MessageType.TEXT,
                        String.format("Server: Клиент %s присоеденился к чату", clientConnection.getName())));
                clientConnection.send(new Message(MessageType.TEXT,
                        "Server: Ваш агент " + agentConnection.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (agentConnection != null) {
            LOG.debug(String.format("Agent %s return to queue.", agentConnection.getConnectionUUID()));
            agentList.add(agentConnection);
        } else if (clientConnection != null) {
            LOG.debug(String.format("Client %s return to queue.", clientConnection.getConnectionUUID()));
            clientList.add(clientConnection);
        }
        LOG.debug("Server.getAgent clientList=" + clientList.size() + ", agentList=" + agentList.size());
    }

    static synchronized void returnAgent(String clientConnectionUUID) {
        LOG.debug("Server.returnAgent");
        Connection agentConnection = rooms.get(clientConnectionUUID);
        LOG.debug(String.format("Client %s end chat. Agent %s return to queue.",
                clientConnectionUUID, agentConnection.getConnectionUUID()));
        agentList.add(agentConnection);
        rooms.remove(clientConnectionUUID);
        String clientName = rooms.get(agentConnection.getConnectionUUID()).getName();
        rooms.remove(agentConnection.getConnectionUUID());
        try {
            agentConnection.send(new Message(MessageType.TEXT,
                    String.format("Server: Клиент %s закончил чат", clientName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.debug("Server.returnAgent clientList=" + clientList.size() + ", agentList=" + agentList.size());
        getAgent();
    }

    static synchronized void reGetAgent(String connectionUUID) {
        Connection client = rooms.get(connectionUUID);
        rooms.remove(connectionUUID);
        rooms.remove(client.getConnectionUUID());
        clientList.addFirst(client);
        LOG.debug("Server.reGetAgent clientList=" + clientList.size() + ", agentList=" + agentList.size());
        getAgent();
    }

    static synchronized void deleteUUID(String connectionUUID) {
        rooms.remove(connectionUUID);
        LOG.debug("Server.deleteUUID clientList=" + clientList.size() + ", agentList=" + agentList.size());
    }

    private static Connection searchValidConnection(LinkedList<Connection> linkedList) {
        Connection connection;
        do {
            connection = linkedList.poll();
            if (connection == null) break;
            if (!connection.isClosed()) {
                LOG.debug(String.format("searchValidConnection. %s is connected.", connection.getConnectionUUID()));
                break;
            } else {
                LOG.debug(String.format("searchValidConnection. %s is closed.", connection.getConnectionUUID()));
                connection = null;
            }
        } while (!linkedList.isEmpty());
        return connection;
    }

}
