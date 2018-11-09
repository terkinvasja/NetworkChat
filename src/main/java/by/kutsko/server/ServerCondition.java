package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerCondition {
    private static final Logger LOG = getLogger(ServerCondition.class);

    static ConcurrentLinkedQueue<Connection> agentQueue = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedDeque<Connection> clientDeque = new ConcurrentLinkedDeque<>();
    static ConcurrentHashMap<String, Connection> rooms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Connection> agents = new ConcurrentHashMap<>();

    static synchronized void getAgent() {
        Connection agentConnection;
        Connection clientConnection;
//        if (!clientDeque.isEmpty()) {
//            if (!agentQueue.isEmpty()) {

        agentConnection = searchValidConnection(agentQueue);
        clientConnection = searchValidConnection(clientDeque);


                if ((agentConnection != null) && (clientConnection != null)) {
                    rooms.put(agentConnection.getConnectionUUID(), clientConnection);
                    rooms.put(clientConnection.getConnectionUUID(), agentConnection);
                    try {
                        agentConnection.send(new Message(MessageType.TEXT,
                                String.format("Server: Клиент %s присоеденился к чату", clientConnection.getName())));
                        clientConnection.send(new Message(MessageType.TEXT, "Server: Ваш агент " + agentConnection.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (agentConnection != null) {
                    System.out.println("agent return");
                    agentQueue.add(agentConnection);
                } else if (clientConnection != null) {
                    System.out.println("client return");
                    clientDeque.add(clientConnection);
                }

//            }
//        }
        LOG.debug("Server.getAgent clientDeque=" + clientDeque.size() + ", agentQueue=" + agentQueue.size());
    }

    static synchronized void returnAgent(String connectionUUID){
        LOG.debug("Server.returnAgent");
        Connection agentConnection = rooms.get(connectionUUID);
        agentQueue.add(agentConnection);
        rooms.remove(connectionUUID);
        String clientName = rooms.get(agentConnection.getConnectionUUID()).getName();
        rooms.remove(agentConnection.getConnectionUUID());
        try {
            agentConnection.send(new Message(MessageType.TEXT,
                    String.format("Server: Клиент %s закончил чат", clientName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.debug("Server.returnAgent clientDeque=" + clientDeque.size() + ", agentQueue=" + agentQueue.size());
        getAgent();
    }

    static synchronized void reGetAgent(String connectionUUID) {
        Connection client = rooms.get(connectionUUID);
        rooms.remove(connectionUUID);
        clientDeque.addFirst(client);
        LOG.debug("Server.reGetAgent clientDeque=" + clientDeque.size() + ", agentQueue=" + agentQueue.size());
        getAgent();
    }

    static synchronized void deleteUUID(String connectionUUID) {
        rooms.remove(connectionUUID);
        LOG.debug("Server.deleteUUID clientDeque=" + clientDeque.size() + ", agentQueue=" + agentQueue.size());
    }

    private static Connection searchValidConnection(Queue<Connection> linkedQueue) {
        Connection connection;
        do {
            connection = linkedQueue.poll();
            if (connection == null) break;
            if (!connection.isClosed()) {
                break;
            } else {
                connection = null;
            }
        } while (!agentQueue.isEmpty());
        return connection;
    }

}
