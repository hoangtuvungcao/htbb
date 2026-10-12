public interface ISession_HAIRMOD_PROTECTED
{
	bool isConnected();

	void setHandler(IMessageHandler_HAIRMOD_PROTECTED messageHandler);

	void connect(string host, int port);

	void sendMessage(Message_HAIRMOD_PROTECTED message);

	void close();
}
