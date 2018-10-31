package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.slf4j.LoggerFactory.getLogger;

public class Server {

    private static final Logger LOG = getLogger(Server.class);

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

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
            LOG.debug("Handler created");
        }

        @Override
        public void run() {
            LOG.debug("Connection established with " + socket.getRemoteSocketAddress());
            String clientName;
            String agentName;
            try (Connection connection = new Connection(socket)) {
//                clientName = serverHandshake(connection);

                serverHandshake(connection);

                clientName = connection.receive().getData();
                serverMainLoop(connection, clientName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            LOG.debug("Handler.close");
        }

        /**
         * Handshake
         **/
        private void serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            LOG.debug("Handler.serverHandshake");
/*            while (true) {
                //Получаем запрос на регистрацию
                Message message = connection.receive();

                if (message.getType() == MessageType.ADD_AGENT || message.getType() == MessageType.ADD_CLIENT) {
                    connection.send(new Message(MessageType.ACCEPTED));
                }
                return message.getData();
            }*/
            connection.send(new Message(MessageType.ACCEPTED));
        }

        /**
         * Главный цикл обработки сообщений сервером
         **/
        private void serverMainLoop(Connection connection, String name) throws IOException, ClassNotFoundException {
            LOG.debug("Handler.serverMainLoop");
            while (true) {
                //Принимаем сообщения клиента
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
