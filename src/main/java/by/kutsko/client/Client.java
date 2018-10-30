package by.kutsko.client;

import by.kutsko.Connection;

import java.io.IOException;
import java.net.Socket;

public class Client {

    private Connection connection;

    public static void main(String[] args) {

        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = new SocketThread();
        //Пометить созданный поток как daemon, это нужно для того, чтобы при выходе из программы
        //вспомогательный поток прервался автоматически
        socketThread.setDaemon(true);
        socketThread.start();


    }

    /**
     * SocketThread
     **/
    public class SocketThread extends Thread {

        @Override
        public void run() {
            try {
                //Создается новый объект класса java.net.Socket
                Socket socket = new Socket("192.168.1.2", 9750);
                //Создается объект класса Connection, используя сокет
                Client.this.connection = new Connection(socket);
            } catch (IOException e) {

            }

        }
    }
}
