package by.kutsko;

import java.io.*;
import java.net.Socket;

public class MockUserSocket extends Socket {

    private Message message;
    private ByteArrayOutputStream response = new ByteArrayOutputStream();

    public MockUserSocket(Message message) {
        this.message = message;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response;
    }
}
