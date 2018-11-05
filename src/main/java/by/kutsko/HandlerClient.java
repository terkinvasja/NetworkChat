package by.kutsko;

import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class HandlerClient extends Thread {

    private static final Logger LOG = getLogger(HandlerClient.class);

    private Connection connection;
    private String agentName = "";

    public HandlerClient(Connection connection) {
        this.connection = connection;
        start();
    }

    @Override
    public void run() {
        LOG.debug("HandlerClient.run");
        try {
            while (true) {
                //Принимаем сообщения клиента
                Message message = connection.receive();

                switch (message.getType()) {
                    case TEXT: {
                        if (!agentName.isEmpty()) {
                            ConsoleHelper.writeMessage(message.getData());
                            Server.agents.get(agentName).send(message);
                        } else {
                            connection.send(new Message(MessageType.TEXT,
                                    "Server: Нет свободного агента. Пожалуйста подождите"));
                        }
                        break;
                    }
                    case LEAVE: {
                        connection.close();
                        if (!agentName.isEmpty()) {
                            Server.returnAgent(agentName);
                            Server.agents.get(agentName).send(new Message(MessageType.TEXT, "Клиент закончил чат"));
                        }
                        LOG.debug("Client connection closed");
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

    void initAgent(String name) {
        agentName = name;
        try {
            Server.agents.get(agentName).send(new Message(MessageType.TEXT,
                    String.format("Клиент %s присоеденился к чату", connection.getName())));
            connection.send(new Message(MessageType.TEXT, "Ваш агент: " + agentName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
