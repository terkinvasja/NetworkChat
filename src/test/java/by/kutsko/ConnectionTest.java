package by.kutsko;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionTest {

    private static final List<Message> MESSAGE_ARRAY = new ArrayList<>();

    static {
        MESSAGE_ARRAY.add(new Message(MessageType.ACCEPTED));
        MESSAGE_ARRAY.add(new Message(MessageType.TEXT, "Test message"));
    }

    @Test
    public void testSend() {
        Message msg = new Message(MessageType.LEAVE);

        for (int i = 0; i < MESSAGE_ARRAY.size(); i++) {
            try {
                Socket mockUserSocket = new MockUserSocket(new Message(MessageType.LEAVE));
                Connection connection = new Connection(mockUserSocket);
                connection.send(MESSAGE_ARRAY.get(i));
                msg = (Message) new ObjectInputStream(new ByteArrayInputStream(
                        ((ByteArrayOutputStream) mockUserSocket.getOutputStream()).toByteArray())).readObject();
                connection.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            Assertions.assertEquals(msg.getType(), MESSAGE_ARRAY.get(i).getType());
            Assertions.assertEquals(msg.getData(), MESSAGE_ARRAY.get(i).getData());
        }
    }

    @Test
    public void testReceive() {
        Message msg = new Message(MessageType.LEAVE);

        for (int i = 0; i < MESSAGE_ARRAY.size(); i++) {
            try {
                Connection connection = new Connection(new MockUserSocket(MESSAGE_ARRAY.get(i)));
                msg = connection.receive();
                connection.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            Assertions.assertEquals(msg.getType(), MESSAGE_ARRAY.get(i).getType());
            Assertions.assertEquals(msg.getData(), MESSAGE_ARRAY.get(i).getData());
        }
    }
}
