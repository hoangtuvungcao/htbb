package javax.wireless.messaging;

public interface Message {
    String getAddress();

    void setAddress(String a);

    java.util.Date getTimestamp();
}
