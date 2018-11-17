package by.kutsko.client;

import by.kutsko.Connection;
import by.kutsko.util.ConsoleHelper;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.LogHelper;
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
                if (m.matches()) {
                    name = m.group(3);

                    //Создается объект класса Connection, используя сокет
                    try {
                        Socket socket = new Socket("localhost", 9750);
                        connection = new Connection(socket);
                        LOG.debug("Created new Connection");
                    } catch (IOException e) {
                        LOG.debug(LogHelper.exceptionToString(e));
                    }

                    HandlerServerConnection handlerServerConnection = new HandlerServerConnection(connection);
                    //Пометить созданный поток как daemon, это нужно для того, чтобы при выходе из программы
                    //вспомогательный поток прервался автоматически
                    handlerServerConnection.setDaemon(true);
                    handlerServerConnection.start();

                    //Текущий поток ожидает, пока он не получит
                    //подтверждение подключения из другого потока
                    LOG.debug("Client. Wait notification from SocketThread");
                    while (true) {
                        if (connection.isConnected()) break;
                    }

                    LOG.debug("Client. Sending registration data");
                    try {
                        if (m.group(2).equals("agent")) {
                            connection.send(new Message(MessageType.ADD_AGENT, name));
                        } else if (m.group(2).equals("client")) {
                            connection.send(new Message(MessageType.ADD_CLIENT, name));
                        }
                    } catch (IOException e) {
                        LOG.debug("Send error");
                    }

                    while (true) {
                        if (!(message = ConsoleHelper.readString()).equals("/leave")) {
                            sendTextMessage(name + ": " + message);
                        } else {
                            try {
                                connection.send(new Message(MessageType.LEAVE));
                                connection.close();
                                break;
                            } catch (IOException e) {
                                LOG.debug(LogHelper.exceptionToString(e));
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
}
