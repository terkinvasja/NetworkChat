package by.kutsko.server;

import by.kutsko.util.ConsoleHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import static org.slf4j.LoggerFactory.getLogger;

public class Server {

    private static final Logger LOG = getLogger(Server.class);

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9750)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            LOG.debug("Server running");
            ExecutorService servicePool = Executors.newCachedThreadPool();

            while (true) {
                //Listening
                LOG.debug("Listening serverSocket");
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                //Start Handler Tread
                //handler.start();
                servicePool.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
