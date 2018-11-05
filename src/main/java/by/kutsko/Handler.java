package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.Socket;

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
            Connection connection = new Connection(socket);
            serverHandshake(connection);

            //We define the agent or client
            Message message = connection.receive();
            connection.setName(message.getData());
            switch (message.getType()) {
                case ADD_AGENT: {
                    Server.agentQueue.add(connection);
                    new HandlerAgent(connection);
                    Server.getAgent();
                    break;
                }
                case ADD_CLIENT: {
                    HandlerClient handlerClient = new HandlerClient(connection);
                    Server.clientDeque.add(handlerClient);
                    Server.getAgent();
                    break;
                }
                default: {
                    ConsoleHelper.writeMessage("Exchange protocol error");
                }
            }
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
