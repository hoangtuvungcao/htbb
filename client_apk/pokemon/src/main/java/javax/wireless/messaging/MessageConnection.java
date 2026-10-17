package javax.wireless.messaging;

import java.io.IOException;
import javax.microedition.io.Connection;

/** SMS (WMA): Android không gửi SMS ngầm - Connector.open("sms://...") ném ConnectionNotFoundException. */
public interface MessageConnection extends Connection {
    String TEXT_MESSAGE = "text";
    String BINARY_MESSAGE = "binary";

    Message newMessage(String type);

    Message newMessage(String type, String address);

    void send(Message m) throws IOException;

    Message receive() throws IOException;
}
