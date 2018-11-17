import by.kutsko.Connection;
import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.client.HandlerServerConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HandlerServerConnectionTest {

    private int requestPosition = 0;
    private static final List<Message> MESSAGE_ARRAY = new ArrayList<>();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    static {
        MESSAGE_ARRAY.add(new Message(MessageType.ACCEPTED));
        MESSAGE_ARRAY.add(new Message(MessageType.TEXT, "Test message 1"));
        MESSAGE_ARRAY.add(new Message(MessageType.TEXT, "Test message 2"));
        MESSAGE_ARRAY.add(new Message(MessageType.LEAVE, "End"));
    }

    @Test
    public void testRun() throws IOException {

        UserConnection uc = new UserConnection(new Socket());


        System.setOut(new PrintStream(outContent));

        HandlerServerConnection hsc = new HandlerServerConnection(uc);
        hsc.run();

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < MESSAGE_ARRAY.size(); i++) {
            sb.append(MESSAGE_ARRAY.get(i).getData());
            sb.append(System.lineSeparator());
        }
        String actual = sb.toString();

        Assertions.assertEquals(outContent.toString(), actual);

        System.setOut(originalOut);
        uc.close();
    }

    private class UserConnection extends Connection {

        public UserConnection(Socket socket) throws IOException {
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
            });
        }

        @Override
        public Message receive() throws IOException, ClassNotFoundException {
            return MESSAGE_ARRAY.get(requestPosition++);
        }
    }
}
