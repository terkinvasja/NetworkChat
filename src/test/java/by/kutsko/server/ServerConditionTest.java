package by.kutsko.server;

import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ServerConditionTest {

    private String agentConnectionUUID_1 = UUID.randomUUID().toString();
    private String agentConnectionUUID_2 = UUID.randomUUID().toString();
    private String clientConnectionUUID_1 = UUID.randomUUID().toString();

    @Test
    public void testGetAgentGood() throws IOException {
        Connection agentConnection = createAndAddAgent(agentConnectionUUID_1);
        Connection clientConnection = createAndAddClient(clientConnectionUUID_1);

        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        ServerCondition.getAgent();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 0);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 2);

        //Clear connection
        closeAndRemoveConnection(agentConnection, clientConnection);
    }

    @Test
    public void testGetAgentNotAgent() throws IOException {
        Connection agentConnection = new UserConnection(agentConnectionUUID_1);
        Connection clientConnection = new UserConnection(clientConnectionUUID_1);
        ((UserConnection) agentConnection).setClosed(true);
        ServerCondition.addAgent(agentConnection);
        ServerCondition.addClient(clientConnection);

        ServerCondition.getAgent();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 0);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 0);

        //Clear connection
        closeAndRemoveConnection(agentConnection, clientConnection);
    }

    @Test
    public void testGetAgentNotClient() throws IOException {
        Connection agentConnection = new UserConnection(agentConnectionUUID_1);
        Connection clientConnection = new UserConnection(clientConnectionUUID_1);
        ((UserConnection) clientConnection).setClosed(true);
        ServerCondition.addAgent(agentConnection);
        ServerCondition.addClient(clientConnection);

        ServerCondition.getAgent();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 0);

        //Clear connection
        closeAndRemoveConnection(agentConnection, clientConnection);
    }

    @Test
    public void testReturnAgent() throws IOException {
        Connection agentConnection = createAndAddAgent(agentConnectionUUID_1);
        Connection clientConnection = createAndAddClient(clientConnectionUUID_1);
        ServerCondition.getAgent();
        ((UserConnection) clientConnection).setClosed(true);
        ServerCondition.returnAgent(clientConnectionUUID_1);
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 0);

        //Clear connection
        closeAndRemoveConnection(agentConnection, clientConnection);
    }

    @Test
    public void testReGetAgent() throws IOException {
        Connection agentConnection_1 = createAndAddAgent(agentConnectionUUID_1);
        Connection agentConnection_2 = createAndAddAgent(agentConnectionUUID_2);
        Connection clientConnection_1 = createAndAddClient(clientConnectionUUID_1);

        ServerCondition.getAgent();
        System.out.println(ServerCondition.getRooms().size());
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 2);

        ((UserConnection) agentConnection_1).setClosed(true);
        ServerCondition.reGetAgent(agentConnectionUUID_1);
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 0);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 2);

        //Clear connection
        closeAndRemoveConnection(agentConnection_2, clientConnection_1);
    }

    private Connection createAndAddAgent(String connectionUUID) throws IOException {
        Connection connection = new UserConnection(connectionUUID);
        ServerCondition.addAgent(connection);
        return connection;
    }

    private Connection createAndAddClient(String connectionUUID) throws IOException {
        Connection connection = new UserConnection(connectionUUID);
        ServerCondition.addClient(connection);
        return connection;
    }
    private void closeAndRemoveConnection(Connection... connections) {
        for (Connection connection : connections) {
            ((UserConnection) connection).setClosed(true);
            ServerCondition.getRooms().remove(connection.getConnectionUUID());
        }
        ServerCondition.getAgent();
    }

    private class UserConnection extends Connection {

        private boolean closed = false;

        public UserConnection(String connectionUUID) throws IOException {
            super(new Socket(){
                @Override
                public InputStream getInputStream() throws IOException {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(new Message(MessageType.LEAVE));
                    oos.flush();
                    return new ByteArrayInputStream(baos.toByteArray());
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return new ByteArrayOutputStream();
                }
            }, connectionUUID);
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }
    }
}
