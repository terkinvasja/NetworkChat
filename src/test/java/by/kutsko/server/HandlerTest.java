package by.kutsko.server;

import by.kutsko.Message;
import by.kutsko.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HandlerTest {

    private ByteArrayOutputStream response = new ByteArrayOutputStream();
    private static final List<Message> MESSAGE_ARRAY = new ArrayList<>();

    static {
        MESSAGE_ARRAY.add(new Message(MessageType.ADD_AGENT));
        MESSAGE_ARRAY.add(new Message(MessageType.ADD_CLIENT));
    }

    @Test
    public void testRun() {
        Handler handler = new Handler(new UserSocket());
        handler.run();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);

        ServerCondition.clearAll();
    }


    private class UserSocket extends Socket {
        @Override
        public InputStream getInputStream() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(MESSAGE_ARRAY.get(0));
            oos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return response;
        }
    }
}
