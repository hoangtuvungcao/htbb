package network;

public interface ISession {
    void close();

    void connect(String str, int i);

    boolean isConnected();

    void sendMessage(Message message);

    void setHandler(IMessageHandler iMessageHandler);
}
