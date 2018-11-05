package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.slf4j.LoggerFactory.getLogger;

public class Server {

    private static final Logger LOG = getLogger(Server.class);
    static ConcurrentLinkedQueue<Connection> agentQueue = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedDeque<HandlerClient> clientDeque = new ConcurrentLinkedDeque<>();
    static ConcurrentHashMap<String, HandlerClient> rooms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Connection> agents = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9750)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            LOG.debug("Server running");

            while (true) {
                //Listening
                LOG.debug("Listening serverSocket");
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                //Start Handler Tread
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void getAgent() {
        if (!clientDeque.isEmpty()) {
            if (!agentQueue.isEmpty()) {
                Connection agent = agentQueue.poll();
                agents.put(agent.getName(), agent);
                HandlerClient handlerClient = clientDeque.poll();
                handlerClient.initAgent(agent.getName());
                rooms.put(agent.getName(), handlerClient);
            }
        }
        LOG.debug("Server.getAgent clientDeque=" + clientDeque.size() + ", agentQueue=" + agentQueue.size());
    }

    static void returnAgent(String agentName){
        LOG.debug("Server.returnAgent");
        rooms.remove(agentName);
        agentQueue.add(agents.get(agentName));
        agents.remove(agentName);
        getAgent();
    }

    static void reGetAgent(String agentName) {
        HandlerClient handlerClient = rooms.get(agentName);
        rooms.remove(agentName);
        agents.remove(agentName);
        clientDeque.addFirst(handlerClient);
        getAgent();
    }
}
