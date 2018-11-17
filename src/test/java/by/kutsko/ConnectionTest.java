package by.kutsko;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionTest {

    private Connection connection;

    private int requestPosition = 0;
    private ByteArrayOutputStream response;
    private static final List<Message> MESSAGE_ARRAY = new ArrayList<>();

    static {
        MESSAGE_ARRAY.add(new Message(MessageType.ACCEPTED));
        MESSAGE_ARRAY.add(new Message(MessageType.TEXT, "Test message"));
    }

    @Test
    public void testSend() {
        Message msg = new Message(MessageType.LEAVE);

        for (int i = 0; i < MESSAGE_ARRAY.size(); i++) {

            response = new ByteArrayOutputStream();

            try {
                connection = new Connection(new UserSocket());
                connection.send(MESSAGE_ARRAY.get(i));
                msg = (Message) new ObjectInputStream(new ByteArrayInputStream(response.toByteArray())).readObject();
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

            requestPosition = i;
            response = new ByteArrayOutputStream();

            try {
                connection = new Connection(new UserSocket());
                msg = connection.receive();
                connection.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            Assertions.assertEquals(msg.getType(), MESSAGE_ARRAY.get(i).getType());
            Assertions.assertEquals(msg.getData(), MESSAGE_ARRAY.get(i).getData());
        }
    }


    private class UserSocket extends Socket {
        @Override
        public InputStream getInputStream() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(MESSAGE_ARRAY.get(requestPosition++));
            oos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return response;
        }
    }
}
