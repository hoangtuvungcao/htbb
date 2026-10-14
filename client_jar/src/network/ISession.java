package network;

public interface ISession {
    boolean isConnected();

    void setHandler(IMessageHandler iMessageHandler);

    void connect(String str, String str2);

    void sendMessage(Message message);

    void close();
}
