package by.kutsko.server;

import by.kutsko.Message;
import by.kutsko.MessageType;
import by.kutsko.MockUserSocket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HandlerTest {

    @Test
    public void testRun() {
        Handler handler = new Handler(new MockUserSocket(new Message(MessageType.ADD_AGENT)));
        handler.run();
        Assertions.assertEquals(ServerCondition.getSizeAgentList(), 1);
        ServerCondition.clearAll();

        handler = new Handler(new MockUserSocket(new Message(MessageType.ADD_CLIENT)));
        handler.run();
        Assertions.assertEquals(ServerCondition.getSizeClientList(), 1);
        ServerCondition.clearAll();
    }
}
