package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.slf4j.LoggerFactory.getLogger;

public class Server {

    private static final Logger LOG = getLogger(Server.class);
    static ConcurrentLinkedQueue<Connection> agentQueue = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedQueue<Connection> clientDeque = new ConcurrentLinkedQueue<>();
    static ConcurrentHashMap<String, Connection> rooms = new ConcurrentHashMap<>();
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

    static String getAgent(Connection clientConnection) {
        if(!agentQueue.isEmpty()) {
            Connection agent = agentQueue.poll();
            agents.put(agent.getName(), agent);
            rooms.put(agent.getName(), clientConnection);
            return agent.getName();
        } else {
            return "";
        }
    }

    static void returnAgent(String agentName){
        rooms.remove(agentName);
        agentQueue.add(agents.get(agentName));
    }
}
