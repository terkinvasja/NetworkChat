package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import org.slf4j.Logger;

import java.io.IOException;
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
        if (!clientDeque.isEmpty()) {
            if (!agentQueue.isEmpty()) {
                Connection agent = agentQueue.poll();
                Connection client = clientDeque.poll();
                rooms.put(agent.getConnectionUUID(), client);
                rooms.put(client.getConnectionUUID(), agent);
                try {
                    agent.send(new Message(MessageType.TEXT,
                            String.format("Клиент %s присоеденился к чату", client.getName())));
                    client.send(new Message(MessageType.TEXT, "Ваш агент: " + agent.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.debug("Server.getAgent clientDeque=" + clientDeque.size() + ", agentQueue=" + agentQueue.size());
    }

    static synchronized void returnAgent(String connectionUUID){
        LOG.debug("Server.returnAgent");
        Connection agentConnection = rooms.get(connectionUUID);
        agentQueue.add(agentConnection);
        rooms.remove(connectionUUID);
        rooms.remove(agentConnection.getConnectionUUID());
        try {
            agentConnection.send(new Message(MessageType.TEXT, "Клиент закончил чат"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        getAgent();
    }

    static synchronized void reGetAgent(String connectionUUID) {
        Connection client = rooms.get(connectionUUID);
        rooms.remove(connectionUUID);
        clientDeque.addFirst(client);
        getAgent();
    }

}
