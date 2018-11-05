package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class HandlerAgent extends Thread {

    private static final Logger LOG = getLogger(HandlerAgent.class);

    private Connection connection;

    public HandlerAgent(Connection connection) {
        this.connection = connection;
        start();
    }

    @Override
    public void run() {
        LOG.debug("HandlerAgent.run");

        try {
            while (true) {
                //Принимаем сообщения агента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        ConsoleHelper.writeMessage(message.getData());
                        Server.rooms.get(connection.getName()).send(message);
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
