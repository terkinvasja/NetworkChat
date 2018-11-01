package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.slf4j.LoggerFactory.getLogger;

public class Server {

    private static final Logger LOG = getLogger(Server.class);
    static ConcurrentLinkedDeque<HandlerAgent> agentDeque = new ConcurrentLinkedDeque<>();
    static ConcurrentLinkedDeque<Connection> clientDeque = new ConcurrentLinkedDeque<>();

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

    private static class Handler1 {
        private Socket socket;

        public Handler1(Socket socket) {
            this.socket = socket;
            LOG.debug("Handler created");
        }

        /**
         * Цикл обработки сообщений сервером от клиента
         **/
        private void serverMainLoopAgent(Connection connection) throws IOException, ClassNotFoundException {
            LOG.debug("Handler.serverMainLoopAgent");
            Connection clientConnection;
            while (true) {
                //Принимаем сообщения агента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        ConsoleHelper.writeMessage(message.getData());
                        break;
                    }
                    case LEAVE: {
                        connection.close();
                        LOG.debug("Connection closed");
                        return;
                    }
                    default: {
                        ConsoleHelper.writeMessage("Error");
                    }
                }
            }
        }
    }
}
