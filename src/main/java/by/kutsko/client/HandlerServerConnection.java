package by.kutsko.client;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.ConsoleHelper;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class HandlerServerConnection extends Thread {

    private static final Logger LOG = getLogger(HandlerServerConnection.class);

    private Connection connection;
    private History history;

    public HandlerServerConnection(Connection connection, History history) {
        this.connection = connection;
        this.history = history;
    }

    @Override
    public void run() {
        LOG.debug("Created new SocketThread");
        try {
            //Вызов метода, реализующий "рукопожатие" клиента с сервером (clientHandshake())
            clientHandshake();
            //Вызов метода, реализующего основной цикл обработки сообщений сервера.
            clientMainLoop();
        } catch (IOException | ClassNotFoundException e) {
//                LOG.debug("", e)
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
                    connection.setConnected(true);
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
                    history.addText(message.getData());
                    break;
                }
                case CHANGE_AGENT: {
                    ConsoleHelper.writeMessage(message.getData());
                    for (String text : history.getListHistory()) {
                        connection.send(new Message(MessageType.TEXT, text));
                    }
                    break;
                }
                case LEAVE: {
                    ConsoleHelper.writeMessage(message.getData());
                    return;
                }
                default: {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
    }
}
