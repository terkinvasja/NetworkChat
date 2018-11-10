package by.kutsko;

import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.slf4j.LoggerFactory.getLogger;

public class Connection implements Closeable {
    private static final Logger LOG = getLogger(Connection.class);
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private String connectionUUID;
    private String name;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.connectionUUID = "";
    }

    public Connection(Socket socket, String connectionUUID) throws IOException {
        this(socket);
        this.connectionUUID = connectionUUID;
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        Message message;
        synchronized (in) {
            message = (Message) in.readObject();
            return message;
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public String getConnectionUUID() {
        return connectionUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
        LOG.debug("Connection.close");
    }
}
