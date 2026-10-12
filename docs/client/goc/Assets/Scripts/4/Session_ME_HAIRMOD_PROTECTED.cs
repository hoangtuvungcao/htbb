using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using UnityEngine;

public class Session_ME_HAIRMOD_PROTECTED : ISession_HAIRMOD_PROTECTED
{
	public class Sender
	{
		private List<Message_HAIRMOD_PROTECTED> sendingMessage;

		public Sender()
		{
			sendingMessage = new List<Message_HAIRMOD_PROTECTED>();
		}

		public void AddMessage(Message_HAIRMOD_PROTECTED message)
		{
			sendingMessage.Add(message);
		}

		public void run()
		{
			try
			{
				while (connected)
				{
					if (getKeyComplete)
					{
						while (sendingMessage.Count > 0)
						{
							Message_HAIRMOD_PROTECTED m = sendingMessage[0];
							doSendMessage(m);
							sendingMessage.RemoveAt(0);
						}
					}
					try
					{
						Thread.Sleep(10);
					}
					catch (Exception ex)
					{
						Out_HAIRMOD_PROTECTED.LogError(ex.ToString() + " loi tai ham run SS");
					}
				}
			}
			catch (Exception)
			{
				Debug.Log("error send message!");
			}
		}
	}

	private class MessageCollector
	{
		public void run()
		{
			try
			{
				while (connected)
				{
					Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = readMessage();
					if (message_HAIRMOD_PROTECTED == null)
					{
						break;
					}
					try
					{
						if (message_HAIRMOD_PROTECTED.command == -40)
						{
							getKey(message_HAIRMOD_PROTECTED);
						}
						else
						{
							onRecieveMsg(message_HAIRMOD_PROTECTED);
						}
					}
					catch (Exception)
					{
					}
					try
					{
						Thread.Sleep(5);
					}
					catch (Exception)
					{
					}
				}
			}
			catch (Exception ex3)
			{
				Debug.Log("error read message!" + ex3.ToString());
				Debug.Log(ex3.Message.ToString());
			}
			if (!connected)
			{
				return;
			}
			if (messageHandler != null)
			{
				if (currentTimeMillis() - timeConnected > 500)
				{
					messageHandler.onDisconnected();
				}
				else
				{
					messageHandler.onConnectionFail();
				}
			}
			if (sc != null)
			{
				cleanNetwork();
			}
		}

		private void getKey(Message_HAIRMOD_PROTECTED message)
		{
			try
			{
				sbyte b = message.reader().readSByte();
				key = new sbyte[b];
				for (int i = 0; i < b; i++)
				{
					key[i] = message.reader().readSByte();
				}
				for (int j = 0; j < key.Length - 1; j++)
				{
					key[j + 1] ^= key[j];
				}
				getKeyComplete = true;
			}
			catch (Exception)
			{
			}
		}

		private Message_HAIRMOD_PROTECTED readMessage()
		{
			try
			{
				sbyte b = dis.ReadSByte();
				if (getKeyComplete)
				{
					b = readKey(b);
				}
				int num;
				if (getKeyComplete)
				{
					if (b == sbyte.MinValue)
					{
						b = dis.ReadSByte();
						b = readKey(b);
						sbyte[] array = new sbyte[4]
						{
							dis.ReadSByte(),
							dis.ReadSByte(),
							dis.ReadSByte(),
							dis.ReadSByte()
						};
						num = (readKey(array[3]) & 0xFF) | ((readKey(array[2]) & 0xFF) << 8) | ((readKey(array[1]) & 0xFF) << 16) | ((readKey(array[0]) & 0xFF) << 24);
					}
					else
					{
						sbyte b2 = dis.ReadSByte();
						sbyte b3 = dis.ReadSByte();
						num = ((readKey(b2) & 0xFF) << 8) | (readKey(b3) & 0xFF);
					}
				}
				else
				{
					sbyte b4 = dis.ReadSByte();
					sbyte b5 = dis.ReadSByte();
					num = (b4 & 0xFF00) | (b5 & 0xFF);
				}
				sbyte[] array2 = new sbyte[num];
				int num2 = 0;
				int num3 = 0;
				byte[] data = dis.ReadBytes(num);
				array2 = ArrayCast_HAIRMOD_PROTECTED.cast(data);
				recvByteCount += 5 + num;
				int num4 = recvByteCount + sendByteCount;
				strRecvByteCount = num4 / 1024 + "." + num4 % 1024 / 102 + "Kb";
				if (getKeyComplete)
				{
					for (int i = 0; i < array2.Length; i++)
					{
						array2[i] = readKey(array2[i]);
					}
				}
				return new Message_HAIRMOD_PROTECTED(b, array2);
			}
			catch (Exception ex)
			{
				Debug.Log(ex.StackTrace.ToString());
			}
			return null;
		}
	}

	protected static Session_ME_HAIRMOD_PROTECTED instance = new Session_ME_HAIRMOD_PROTECTED();

	private static NetworkStream dataStream;

	private static BinaryReader dis;

	private static BinaryWriter dos;

	public static IMessageHandler_HAIRMOD_PROTECTED messageHandler;

	private static TcpClient sc;

	public static bool connected;

	public static bool connecting;

	private static Sender sender = new Sender();

	public static Thread initThread;

	public static Thread collectorThread;

	public static Thread sendThread;

	public static int sendByteCount;

	public static int recvByteCount;

	private static bool getKeyComplete;

	public static sbyte[] key = null;

	private static sbyte curR;

	private static sbyte curW;

	private static int timeConnected;

	public static string strRecvByteCount = string.Empty;

	public static bool isCancel;

	private string host;

	private int port;

	public static mVector_HAIRMOD_PROTECTED recieveMsg = new mVector_HAIRMOD_PROTECTED();

	public Session_ME_HAIRMOD_PROTECTED()
	{
		Debug.Log("init Session_ME");
	}

	public static Session_ME_HAIRMOD_PROTECTED gI()
	{
		return instance;
	}

	public bool isConnected()
	{
		return connected;
	}

	public void setHandler(IMessageHandler_HAIRMOD_PROTECTED msgHandler)
	{
		messageHandler = msgHandler;
	}

	public void connect(string host, int port)
	{
		if (!connected && !connecting)
		{
			this.host = host;
			this.port = port;
			getKeyComplete = false;
			sc = null;
			Debug.Log("connecting...!");
			Debug.Log("host: " + host);
			Debug.Log("port: " + port);
			initThread = new Thread(NetworkInit);
			initThread.Start();
		}
	}

	private void NetworkInit()
	{
		isCancel = false;
		new Thread((ThreadStart)delegate
		{
			try
			{
				Thread.Sleep(20000);
			}
			catch (Exception)
			{
			}
			if (connecting)
			{
				try
				{
					sc.Close();
				}
				catch (Exception)
				{
				}
				isCancel = true;
				connecting = false;
				connected = false;
				messageHandler.onConnectionFail();
			}
		}).Start();
		connecting = true;
		Thread.CurrentThread.Priority = System.Threading.ThreadPriority.Lowest;
		connected = true;
		try
		{
			doConnect(host, port);
			messageHandler.onConnectOK();
		}
		catch (Exception)
		{
			try
			{
				Thread.Sleep(500);
			}
			catch (Exception)
			{
			}
			if (isCancel && messageHandler != null)
			{
				close();
				messageHandler.onConnectionFail();
			}
		}
	}

	public void doConnect(string host, int port)
	{
		sc = new TcpClient();
		sc.Connect(host, port);
		dataStream = sc.GetStream();
		dis = new BinaryReader(dataStream, new UTF8Encoding());
		dos = new BinaryWriter(dataStream, new UTF8Encoding());
		new Thread(sender.run).Start();
		MessageCollector @object = new MessageCollector();
		collectorThread = new Thread(@object.run);
		collectorThread.Start();
		timeConnected = currentTimeMillis();
		connecting = false;
		doSendMessage(new Message_HAIRMOD_PROTECTED(-40));
	}

	public void sendMessage(Message_HAIRMOD_PROTECTED message)
	{
		sender.AddMessage(message);
	}

	private static void doSendMessage(Message_HAIRMOD_PROTECTED m)
	{
		if (m == null)
		{
			return;
		}
		sbyte[] data = m.getData();
		try
		{
			if (getKeyComplete)
			{
				sbyte value = writeKey(m.command);
				dos.Write(value);
			}
			else
			{
				dos.Write(m.command);
			}
			if (data != null)
			{
				int num = data.Length;
				if (getKeyComplete)
				{
					int num2 = writeKey((sbyte)(num >> 8));
					dos.Write((sbyte)num2);
					int num3 = writeKey((sbyte)(num & 0xFF));
					dos.Write((sbyte)num3);
				}
				else
				{
					dos.Write((ushort)num);
				}
				if (getKeyComplete)
				{
					for (int i = 0; i < data.Length; i++)
					{
						sbyte value2 = writeKey(data[i]);
						dos.Write(value2);
					}
				}
				sendByteCount += 5 + data.Length;
			}
			else
			{
				if (getKeyComplete)
				{
					int num4 = 0;
					int num5 = writeKey((sbyte)(num4 >> 8));
					dos.Write((sbyte)num5);
					int num6 = writeKey((sbyte)(num4 & 0xFF));
					dos.Write((sbyte)num6);
				}
				else
				{
					dos.Write((ushort)0);
				}
				sendByteCount += 5;
			}
			dos.Flush();
		}
		catch (Exception ex)
		{
			Debug.Log(ex.StackTrace);
		}
	}

	public static sbyte readKey(sbyte b)
	{
		sbyte result = (sbyte)((key[curR++] & 0xFF) ^ (b & 0xFF));
		if (curR >= key.Length)
		{
			curR %= (sbyte)key.Length;
		}
		return result;
	}

	public static sbyte writeKey(sbyte b)
	{
		sbyte result = (sbyte)((key[curW++] & 0xFF) ^ (b & 0xFF));
		if (curW >= key.Length)
		{
			curW %= (sbyte)key.Length;
		}
		return result;
	}

	public static void onRecieveMsg(Message_HAIRMOD_PROTECTED msg)
	{
		if (Thread.CurrentThread.Name == Main_HAIRMOD_PROTECTED.mainThreadName)
		{
			messageHandler.onMessage(msg);
		}
		else
		{
			recieveMsg.addElement(msg);
		}
	}

	public static void update()
	{
		while (recieveMsg.size() > 0)
		{
			Message_HAIRMOD_PROTECTED message_HAIRMOD_PROTECTED = (Message_HAIRMOD_PROTECTED)recieveMsg.elementAt(0);
			if (message_HAIRMOD_PROTECTED != null)
			{
				messageHandler.onMessage(message_HAIRMOD_PROTECTED);
			}
			recieveMsg.removeElementAt(0);
		}
	}

	public void close()
	{
		recieveMsg.removeAllElements();
		cleanNetwork();
	}

	private static void cleanNetwork()
	{
		key = null;
		curR = 0;
		curW = 0;
		try
		{
			connected = false;
			connecting = false;
			if (sc != null)
			{
				sc.Close();
				sc = null;
			}
			if (dataStream != null)
			{
				dataStream.Close();
				dataStream = null;
			}
			if (dos != null)
			{
				dos.Close();
				dos = null;
			}
			if (dis != null)
			{
				dis.Close();
				dis = null;
			}
			sendThread = null;
			collectorThread = null;
		}
		catch (Exception)
		{
		}
	}

	public static int currentTimeMillis()
	{
		return Environment.TickCount;
	}

	public static byte convertSbyteToByte(sbyte var)
	{
		if (var > 0)
		{
			return (byte)var;
		}
		return (byte)(var + 256);
	}

	public static byte[] convertSbyteToByte(sbyte[] var)
	{
		byte[] array = new byte[var.Length];
		for (int i = 0; i < var.Length; i++)
		{
			if (var[i] > 0)
			{
				array[i] = (byte)var[i];
			}
			else
			{
				array[i] = (byte)(var[i] + 256);
			}
		}
		return array;
	}
}
