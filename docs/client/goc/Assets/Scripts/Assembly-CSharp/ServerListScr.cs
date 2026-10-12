using System;
using System.IO;
using UnityEngine;

public class ServerListScr : MyScreen
{
	public static ServerListScr me;

	public Command cmdUpdateServer;

	public int goc;

	public int xTo;

	public int yTo;

	public int radius = 200;

	public int w = 200;

	public int h = 160;

	public static int cmtoY;

	public static int cmy;

	public static int cmdy;

	public static int cmvy;

	public static int cmyLim;

	public static int index;

	public static string[] nameServer;

	public static string[] address;

	public static short[] port;

	public static sbyte[] index_server;

	public static string linkGetHost;

	public static string nameSvAuto;

	public static string addressAuTo;

	public static short portAuTo;

	private bool trans;

	private bool isPerFom;

	private int pa;

	private int timeClick;

	private int saveIndex = -1;

	private long timeTouch;

	public bool isAction;

	public int timeDeplay;

	private float zoom = 2f;

	public Scroll myScroll = new Scroll();

	public ServerListScr()
	{
		loadIP();
		left = new Command("Cập nhật");
		ActionPerform action = delegate
		{
			doUpdateServer();
		};
		left.action = action;
		center = new Command("Chọn");
		ActionPerform action2 = delegate
		{
			saveIndex = index;
			if (saveIndex != -1)
			{
				Canvas.loginScr.switchToMe();
			}
			if (saveIndex >= 0 && saveIndex <= nameServer.Length - 1)
			{
				LoginScr.nameServer = nameServer[saveIndex];
				nameSvAuto = nameServer[saveIndex];
				portAuTo = port[saveIndex];
				addressAuTo = address[saveIndex];
				goc = 0;
				xTo = (yTo = 0);
			}
			timeTouch = 0L;
			saveIndex = -1;
		};
		center.action = action2;
	}

	static ServerListScr()
	{
		index = -1;
		nameServer = new string[6] { "Hoa Lư", "Kinh Môn", "Hoàng Vương", "Mỹ Sơn", "Tràng An", "Tiên La" };
		address = new string[6] { "27.0.12.112", "112.213.94.124", "112.213.94.124", "112.213.94.124", "112.213.94.124", "112.213.94.124" };
		port = new short[6] { 19129, 19129, 19129, 19129, 19129, 19129 };
		index_server = new sbyte[6] { 0, 0, 3, 4, 1, 2 };
		linkGetHost = "http://teamobi.com/srvips/NQSH2.txt";
		nameSvAuto = string.Empty;
		addressAuTo = string.Empty;
		portAuTo = -1;
	}

	public static ServerListScr gI()
	{
		if (me == null)
		{
			me = new ServerListScr();
		}
		return me;
	}

	public override void pointerPress(int dx, int dy)
	{
		base.pointerPress(dx, dy);
	}

	public static string IP;
	public static short portWeb;
	public static void loadSv(bool isKPAH)
	{
        nameServer = new string[3] { "Thăng Long", "Thành Cổ", "Đại Việt" };
		address = new string[3] { "sv.kpah3.com", "sv2.kpah3.com", "sv3.kpah3.com" };
		port = new short[3] { 19130, 19130, 19130 };
		linkGetHost = "http://api.kpah3.com/storage/servers.txt";
		GameMidlet.numberSupport = "113";
	}

	public override void switchToMe()
	{
		base.switchToMe();
		myScroll.clear();
		goc = 0;
		xTo = (yTo = 0);
		Canvas.gameScr.loadCamera();
		updateCamera();
		init();
		cmdUpdateServer = new Command();
		ActionChat actionChat = delegate(string str)
		{
			if (str == null)
			{
				Canvas.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
			}
			else
			{
				string[] array = MFont.splitStringSv(str, ",");
				nameServer = new string[array.Length];
				address = new string[array.Length];
				port = new short[array.Length];
				index_server = new sbyte[array.Length];
				for (int i = 0; i < array.Length; i++)
				{
					string[] array2 = MFont.splitStringSv(array[i], ":");
					nameServer[i] = array2[0];
					address[i] = array2[1];
					port[i] = short.Parse(array2[2].Trim());
					index_server[i] = sbyte.Parse(array2[3].Trim());
				}
				saveIP();
				init();
				Canvas.endDlg();
			}
		};
		cmdUpdateServer.actionChat = actionChat;
		if (!Main.isPC)
		{
			return;
		}
		right = new Command("Thoát");
		right.action = delegate
		{
			ActionPerform yesAction = delegate
			{
				Application.Quit();
				Canvas.endDlg();
			};
			Canvas.startYesNoDlg("Bạn có thật sự muốn thoát không", yesAction);
		};
	}

	public static void doLoginAuTo()
	{
		if (!Main.isPC)
		{
			Canvas.loginScr.switchToMe();
			LoginScr.nameServer = nameSvAuto;
		}
	}

	public override void init()
	{
		GameScr.cmx = GameScr.cmtoX;
		GameScr.cmy = GameScr.cmtoY;
		cmy = (cmtoY = 0);
		index = -1;
		saveIndex = -1;
		cmyLim = nameServer.Length * 30 - (h - 10) + 30;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		if (Main.isPC)
		{
			index = 0;
			saveIndex = 0;
		}
	}

	public void doUpdateServer()
	{
		Canvas.startWaitDlg();
		Net.connectHTTP(linkGetHost, cmdUpdateServer);
	}

	public static void saveIP()
	{
		DataOutputStream dataOutputStream = new DataOutputStream();
		try
		{
			dataOutputStream.writeByte((sbyte)nameServer.Length);
			for (int i = 0; i < nameServer.Length; i++)
			{
				dataOutputStream.writeUTF(nameServer[i]);
				dataOutputStream.writeUTF(address[i]);
				dataOutputStream.writeShort(port[i]);
				dataOutputStream.writeByte(index_server[i]);
			}
			RMS.saveRMS("ipnqsh", dataOutputStream.toByteArray());
			dataOutputStream.close();
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void saveIPAuto(string nameSv, string add, short port)
	{
		DataOutputStream dataOutputStream = new DataOutputStream();
		try
		{
			dataOutputStream.writeUTF(nameSv);
			dataOutputStream.writeUTF(add);
			dataOutputStream.writeShort(port);
			RMS.saveRMS("ipnqshauto", dataOutputStream.toByteArray());
			dataOutputStream.close();
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void loadIPAuTo()
	{
		nameSvAuto = string.Empty;
		addressAuTo = string.Empty;
		portAuTo = -1;
		sbyte[] array = RMS.loadRMS("ipnqshauto");
		if (array == null)
		{
			return;
		}
		DataInputStream dataInputStream = new DataInputStream(array);
		if (dataInputStream == null)
		{
			return;
		}
		try
		{
			nameSvAuto = dataInputStream.readUTF();
			addressAuTo = dataInputStream.readUTF();
			portAuTo = dataInputStream.readShort();
			LoginScr.nameServer = nameSvAuto;
			dataInputStream.close();
		}
		catch (IOException ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void loadIP()
	{
		sbyte[] array = RMS.loadRMS("ipnqsh");
		if (array == null)
		{
			return;
		}
		DataInputStream dataInputStream = new DataInputStream(array);
		if (dataInputStream == null)
		{
			return;
		}
		try
		{
			sbyte b = dataInputStream.readByte();
			nameServer = new string[b];
			address = new string[b];
			port = new short[b];
			index_server = new sbyte[b];
			for (int i = 0; i < b; i++)
			{
				nameServer[i] = dataInputStream.readUTF();
				address[i] = dataInputStream.readUTF();
				port[i] = dataInputStream.readShort();
				index_server[i] = dataInputStream.readByte();
			}
			dataInputStream.close();
		}
		catch (IOException ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public override void update()
	{
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
		else
		{
			cmy = cmtoY;
		}
		updateCamera();
	}

	public override void updateKey()
	{
		bool flag = false;
		if (Canvas.isKeyPressed(2))
		{
			index--;
			if (index < 0)
			{
				index = nameServer.Length - 1;
			}
			flag = true;
		}
		else if (Canvas.isKeyPressed(8))
		{
			index++;
			if (index >= nameServer.Length)
			{
				index = 0;
			}
			flag = true;
		}
		if (flag)
		{
			myScroll.cmtoY = index * 30 - (h - 10) / 2;
			if (myScroll.cmtoY < 0)
			{
				myScroll.cmtoY = 0;
			}
			if (myScroll.cmtoY > myScroll.cmyLim)
			{
				myScroll.cmtoY = myScroll.cmyLim;
			}
		}
		base.updateKey();
		ScrollResult scrollResult = myScroll.updateKey();
		if (scrollResult.isDowning || scrollResult.isFinish)
		{
			index = scrollResult.selected;
		}
		if (isPointer(Canvas.hw - 100, Canvas.hh - 80 + 20, 200, 160) && Canvas.isPointerClick)
		{
			Canvas.isPointerClick = false;
			if (index != -1 && center != null && center != null)
			{
				saveIndex = index;
				isAction = true;
				timeDeplay = 7;
			}
		}
		if (isAction && timeDeplay > 0)
		{
			timeDeplay--;
			if (timeDeplay <= 0)
			{
				timeDeplay = 0;
				isAction = false;
				if (center != null)
				{
					saveIndex = index;
					center.perform();
				}
			}
		}
		myScroll.updatecm();
	}

	public bool isPointer(int x, int y, int w, int h)
	{
		if (Canvas.px >= x && Canvas.px <= x + w && Canvas.py >= y && Canvas.py <= y + h)
		{
			return true;
		}
		return false;
	}

	public void updateCamera()
	{
		goc++;
		if (goc > 360)
		{
			goc = 0;
		}
		xTo = Util.Cos(goc) * radius >> 10;
		yTo = Util.Sin(goc) * radius >> 10;
		GameScr.cmtoX = xTo + 380;
		GameScr.cmtoY = yTo + 380;
		Canvas.gameScr.updateCamera();
	}
	Texture2D imgMap;
	private Texture2D LoadTexture2D(string url)
	{
		var TextAssets = Resources.Load(url) as TextAsset;
		return Image.createImage(TextAssets.bytes).texture;
	}
	public void paintBg(MyGraphics g)
	{
		try
		{
			if(imgMap == null)
			{
				imgMap = LoadTexture2D("map");
				return;
			}
			GUI.DrawTexture(new Rect(0, 0, ScaleGUI.WIDTH, ScaleGUI.HEIGHT), imgMap);
		} catch(Exception e)
		{
			Debug.LogError(e.ToString());
		}
	}

	public override void paint(MyGraphics g)
	{
		paintBg(g);
		Canvas.resetTrans(g);
		g.drawImage(Canvas.imgLogo, Canvas.hw, Canvas.hh - 130, 3);
		paintPanel(g);
		base.paint(g);
	}

	private void paintPanel(MyGraphics g)
	{
		Res.paintDlgDragonFull(g, Canvas.hw - 100, Canvas.hh - 80 + 20, 200, 160);
		g.setClip(Canvas.hw - 100, Canvas.hh - 56, w, h - 7);
		g.translate(0, -cmy);
		Canvas.resetTrans(g);
		myScroll.setStyle(nameServer.Length + 1, 30, Canvas.hw - 100, Canvas.hh - 50, 200, 150, true, 1);
		myScroll.setClip(g, Canvas.hw - 100, Canvas.hh - 50, 200, 130);
		for (int i = 0; i < nameServer.Length; i++)
		{
			if (index == i)
			{
				g.setColor(11908790);
				g.fillRect(Canvas.hw - 96, Canvas.hh - 52 + index * 30 + 5, 193, 30);
				g.setColor(34949);
				g.fillRect(Canvas.hw - 95, Canvas.hh - 51 + index * 30 + 5, 191, 28);
			}
			MFont.normalFont[0].drawString(g, nameServer[i], Canvas.hw, Canvas.hh - 40 + i * 30, 2);
		}
	}

	public static void close()
	{
		me = null;
	}
}
