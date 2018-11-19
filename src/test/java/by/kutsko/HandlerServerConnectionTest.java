package by.kutsko;

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

        UserConnection uc = new UserConnection();


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

        public UserConnection() throws IOException {
            super(new MockUserSocket(new Message(MessageType.LEAVE)));
        }

        @Override
        public Message receive() throws IOException, ClassNotFoundException {
            return MESSAGE_ARRAY.get(requestPosition++);
        }
    }
}
