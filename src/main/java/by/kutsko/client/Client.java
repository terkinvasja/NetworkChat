package by.kutsko.client;

import by.kutsko.Connection;
import by.kutsko.util.ConsoleHelper;
import by.kutsko.Message;
import by.kutsko.MessageType;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class Client {

    private static final Logger LOG = getLogger(Client.class);
    private Connection connection;
    private String name;
    private final Pattern p = Pattern.compile("(/register) (agent|client) (\\w+)");

    public static void main(String[] args) {

        Client client = new Client();
        client.run();
    }

    private void run() {

        while (true) {
            ConsoleHelper.writeMessage("Зарегистрируйтесь");
            String message;
            if (!(message = ConsoleHelper.readString()).equals("/exit")) {
                Matcher m = p.matcher(message);
                //String[] msg = message.split(" ");
                if (m.matches()) {
                    ConsoleHelper.writeMessage(String.format("%s, %s, %s", m.group(1), m.group(2), m.group(3)));
                    name = m.group(3);
                    SocketThread socketThread = new SocketThread();
                    //Пометить созданный поток как daemon, это нужно для того, чтобы при выходе из программы
                    //вспомогательный поток прервался автоматически
                    socketThread.setDaemon(true);
                    socketThread.start();

                    //Заставить текущий поток ожидать, пока он не получит нотификацию из другого потока
                    LOG.debug("Client. Wait notification from SocketThread");
                    try {
                        synchronized (this) {
                            this.wait();
                        }
                    }
                    catch (InterruptedException e) {
                        ConsoleHelper.writeMessage("Ошибка");
                        return;
                    }

                    LOG.debug("Client. Sending registration data");
                    try {
                        if (m.group(2).equals("agent")) {
                            connection.send(new Message(MessageType.ADD_AGENT, name));
                        } else if (m.group(2).equals("client")) {
                            connection.send(new Message(MessageType.ADD_CLIENT, name));
                        }
                    } catch (IOException e) {
                        ConsoleHelper.writeMessage("Ошибка отправки");
                    }

                    while (true) {
                        if (!(message = ConsoleHelper.readString()).equals("/leave")) {
                            sendTextMessage(name + ": " + message);
                        } else {
                            try {
                                connection.send(new Message(MessageType.LEAVE));
                                break;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    ConsoleHelper.writeMessage("Некорректно введена команада.");
                }
            } else {
                return;
            }
        }
    }

    private void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки");
        }
    }

    /**
     * SocketThread
     **/
    private class SocketThread extends Thread {

        @Override
        public void run() {
            LOG.debug("Created new SocketThread");
            try {
                //Создается новый объект класса java.net.Socket
                Socket socket = new Socket("localhost", 9750);
                //Создается объект класса Connection, используя сокет
                Client.this.connection = new Connection(socket);
                LOG.debug("Created new Connection");
                //Вызов метода, реализующий "рукопожатие" клиента с сервером (clientHandshake())
                clientHandshake();
                //Вызов метода, реализующего основной цикл обработки сообщений сервера.
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void clientHandshake() throws IOException, ClassNotFoundException {
            LOG.debug("SocketThread.clientHandshake");
            while (true) {
                Message message = connection.receive();

                switch (message.getType()) {
                    case ACCEPTED: {
                        LOG.debug("SocketThread.clientHandshake.ACCEPTED");
                        //Оповещать (пробуждать ожидающий) основной поток класса Client.
                        synchronized (Client.this) {
                            Client.this.notify();
                        }
                        return;
                    }
                    default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }

        private void clientMainLoop() throws IOException, ClassNotFoundException {
            LOG.debug("SocketThread.clientMainLoop");
            while (true) {
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        ConsoleHelper.writeMessage(message.getData());
                        break;
                    }
                    default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }
    }
}
