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

    String agentConnectionUUID = UUID.randomUUID().toString();
    String clientConnectionUUID = UUID.randomUUID().toString();

    @Test
    public void testGetAgentGood() throws IOException {
        String agentConnectionUUID = UUID.randomUUID().toString();
        String clientConnectionUUID = UUID.randomUUID().toString();
        Connection agentConnection = new UserConnection(new Socket(), agentConnectionUUID);
        Connection clientConnection = new UserConnection(new Socket(), clientConnectionUUID);
        ServerCondition.addAgent(agentConnection);
        ServerCondition.addClient(clientConnection);

        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        ServerCondition.getAgent();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 0);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 2);
        ServerCondition.deleteUUID(agentConnectionUUID);
        ServerCondition.deleteUUID(clientConnectionUUID);
    }

    @Test
    public void testGetAgentNotAgent() throws IOException {
        Connection agentConnection = new UserConnection(new Socket(), agentConnectionUUID);
        Connection clientConnection = new UserConnection(new Socket(), clientConnectionUUID);
        ((UserConnection) agentConnection).setClosed(true);
        ServerCondition.addAgent(agentConnection);
        ServerCondition.addClient(clientConnection);

        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        ServerCondition.getAgent();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 0);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 0);
        ServerCondition.deleteUUID(agentConnectionUUID);
        ServerCondition.deleteUUID(clientConnectionUUID);
        ServerCondition.getAgent();
    }

    @Test
    public void testGetAgentNotClient() throws IOException {
        Connection agentConnection = new UserConnection(new Socket(), agentConnectionUUID);
        Connection clientConnection = new UserConnection(new Socket(), clientConnectionUUID);
        ((UserConnection) clientConnection).setClosed(true);
        ServerCondition.addAgent(agentConnection);
        ServerCondition.addClient(clientConnection);

        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        ServerCondition.getAgent();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 0);
        Assertions.assertEquals(ServerCondition.getRooms().size(), 0);
        ServerCondition.getAgent();
    }

    private class UserConnection extends Connection {

        private boolean closed = false;

        public UserConnection(Socket socket, String connectionUUID) throws IOException {
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
