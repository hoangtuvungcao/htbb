package javax.wireless.messaging;

import java.io.IOException;
import java.io.InterruptedIOException;
import javax.microedition.io.Connection;

/** Khai báo API WMA (JSR-120) chỉ để biên dịch. */
public interface MessageConnection extends Connection {
    String TEXT_MESSAGE = "text";
    String BINARY_MESSAGE = "binary";

    Message newMessage(String type);

    Message newMessage(String type, String address);

    void send(Message msg) throws IOException, InterruptedIOException;

    Message receive() throws IOException, InterruptedIOException;

    int numberOfSegments(Message msg);
}
