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
                        if (Server.rooms.containsKey(connection.getName())) {
                            Server.rooms.get(connection.getName()).getConnection().send(message);
                        } else {
                            connection.send(new Message(MessageType.TEXT, "Нет подключенных клиентов"));
                        }
                        break;
                    }
                    case LEAVE: {
                        String agentName = connection.getName();
                        connection.close();
                        if (Server.rooms.containsKey(agentName)) {
                            Server.rooms.get(agentName).getConnection()
                                    .send(new Message(MessageType.TEXT, "Агент разорвал соединение"));
                            Server.reGetAgent(agentName);
                        }
                        LOG.debug("Agent connection closed");
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
