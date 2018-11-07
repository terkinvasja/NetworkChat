package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.util.ConsoleHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class Handler extends Thread {

    private static final Logger LOG = getLogger(Handler.class);
    private Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
        LOG.debug("Handler created");
    }

    @Override
    public void run() {
        LOG.debug("Connection established with " + socket.getRemoteSocketAddress());
        try {
            Connection connection = new Connection(socket, UUID.randomUUID().toString());
            serverHandshake(connection);

            //We define the agent or client
            Message message = connection.receive();
            connection.setName(message.getData());
            switch (message.getType()) {
                case ADD_AGENT: {
                    ServerCondition.agentQueue.add(connection);
                    new HandlerAgent(connection);
                    break;
                }
                case ADD_CLIENT: {
                    ServerCondition.clientDeque.add(connection);
                    new HandlerClient(connection);
                    break;
                }
                default: {
                    ConsoleHelper.writeMessage("Exchange protocol error");
                }
            }
            ServerCondition.getAgent();
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
    private void serverHandshake(Connection connection) throws IOException {
        LOG.debug("Handler.serverHandshake");
        connection.send(new Message(MessageType.ACCEPTED));
    }
}
