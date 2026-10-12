using System;
using System.IO;
using UnityEngine;

public class ServerListScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static ServerListScr_HAIRMOD_PROTECTED me;

	public Command_HAIRMOD_PROTECTED cmdUpdateServer;

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

	public Scroll_HAIRMOD_PROTECTED myScroll = new Scroll_HAIRMOD_PROTECTED();

	public ServerListScr_HAIRMOD_PROTECTED()
	{
		loadIP();
		left = new Command_HAIRMOD_PROTECTED("Cập nhật");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			doUpdateServer();
		};
		left.action = action;
		center = new Command_HAIRMOD_PROTECTED("Chọn");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			saveIndex = index;
			if (saveIndex != -1)
			{
				Canvas_HAIRMOD_PROTECTED.loginScr.switchToMe();
			}
			if (saveIndex >= 0 && saveIndex <= nameServer.Length - 1)
			{
				LoginScr_HAIRMOD_PROTECTED.nameServer = nameServer[saveIndex];
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

	static ServerListScr_HAIRMOD_PROTECTED()
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

	public static ServerListScr_HAIRMOD_PROTECTED gI()
	{
		if (me == null)
		{
			me = new ServerListScr_HAIRMOD_PROTECTED();
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
		Canvas_HAIRMOD_PROTECTED.gameScr.loadCamera();
		updateCamera();
		init();
		cmdUpdateServer = new Command_HAIRMOD_PROTECTED();
		ActionChat_HAIRMOD_PROTECTED actionChat = delegate(string str)
		{
			if (str == null)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
			}
			else
			{
				string[] array = MFont_HAIRMOD_PROTECTED.splitStringSv(str, ",");
				nameServer = new string[array.Length];
				address = new string[array.Length];
				port = new short[array.Length];
				index_server = new sbyte[array.Length];
				for (int i = 0; i < array.Length; i++)
				{
					string[] array2 = MFont_HAIRMOD_PROTECTED.splitStringSv(array[i], ":");
					nameServer[i] = array2[0];
					address[i] = array2[1];
					port[i] = short.Parse(array2[2].Trim());
					index_server[i] = sbyte.Parse(array2[3].Trim());
				}
				saveIP();
				init();
				Canvas_HAIRMOD_PROTECTED.endDlg();
			}
		};
		cmdUpdateServer.actionChat = actionChat;
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			return;
		}
		right = new Command_HAIRMOD_PROTECTED("Thoát");
		right.action = delegate
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				Application.Quit();
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có thật sự muốn thoát không", yesAction);
		};
	}

	public static void doLoginAuTo()
	{
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			Canvas_HAIRMOD_PROTECTED.loginScr.switchToMe();
			LoginScr_HAIRMOD_PROTECTED.nameServer = nameSvAuto;
		}
	}

	public override void init()
	{
		GameScr_HAIRMOD_PROTECTED.cmx = GameScr_HAIRMOD_PROTECTED.cmtoX;
		GameScr_HAIRMOD_PROTECTED.cmy = GameScr_HAIRMOD_PROTECTED.cmtoY;
		cmy = (cmtoY = 0);
		index = -1;
		saveIndex = -1;
		cmyLim = nameServer.Length * 30 - (h - 10) + 30;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		if (Main_HAIRMOD_PROTECTED.isPC)
		{
			index = 0;
			saveIndex = 0;
		}
	}

	public void doUpdateServer()
	{
		Canvas_HAIRMOD_PROTECTED.startWaitDlg();
		Net_HAIRMOD_PROTECTED.connectHTTP(linkGetHost, cmdUpdateServer);
	}

	public static void saveIP()
	{
		DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
		try
		{
			dataOutputStream_HAIRMOD_PROTECTED.writeByte((sbyte)nameServer.Length);
			for (int i = 0; i < nameServer.Length; i++)
			{
				dataOutputStream_HAIRMOD_PROTECTED.writeUTF(nameServer[i]);
				dataOutputStream_HAIRMOD_PROTECTED.writeUTF(address[i]);
				dataOutputStream_HAIRMOD_PROTECTED.writeShort(port[i]);
				dataOutputStream_HAIRMOD_PROTECTED.writeByte(index_server[i]);
			}
			RMS_HAIRMOD_PROTECTED.saveRMS("ipnqsh", dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
			dataOutputStream_HAIRMOD_PROTECTED.close();
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void saveIPAuto(string nameSv, string add, short port)
	{
		DataOutputStream_HAIRMOD_PROTECTED dataOutputStream_HAIRMOD_PROTECTED = new DataOutputStream_HAIRMOD_PROTECTED();
		try
		{
			dataOutputStream_HAIRMOD_PROTECTED.writeUTF(nameSv);
			dataOutputStream_HAIRMOD_PROTECTED.writeUTF(add);
			dataOutputStream_HAIRMOD_PROTECTED.writeShort(port);
			RMS_HAIRMOD_PROTECTED.saveRMS("ipnqshauto", dataOutputStream_HAIRMOD_PROTECTED.toByteArray());
			dataOutputStream_HAIRMOD_PROTECTED.close();
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
		sbyte[] array = RMS_HAIRMOD_PROTECTED.loadRMS("ipnqshauto");
		if (array == null)
		{
			return;
		}
		DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
		if (dataInputStream_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		try
		{
			nameSvAuto = dataInputStream_HAIRMOD_PROTECTED.readUTF();
			addressAuTo = dataInputStream_HAIRMOD_PROTECTED.readUTF();
			portAuTo = dataInputStream_HAIRMOD_PROTECTED.readShort();
			LoginScr_HAIRMOD_PROTECTED.nameServer = nameSvAuto;
			dataInputStream_HAIRMOD_PROTECTED.close();
		}
		catch (IOException ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void loadIP()
	{
		sbyte[] array = RMS_HAIRMOD_PROTECTED.loadRMS("ipnqsh");
		if (array == null)
		{
			return;
		}
		DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(array);
		if (dataInputStream_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		try
		{
			sbyte b = dataInputStream_HAIRMOD_PROTECTED.readByte();
			nameServer = new string[b];
			address = new string[b];
			port = new short[b];
			index_server = new sbyte[b];
			for (int i = 0; i < b; i++)
			{
				nameServer[i] = dataInputStream_HAIRMOD_PROTECTED.readUTF();
				address[i] = dataInputStream_HAIRMOD_PROTECTED.readUTF();
				port[i] = dataInputStream_HAIRMOD_PROTECTED.readShort();
				index_server[i] = dataInputStream_HAIRMOD_PROTECTED.readByte();
			}
			dataInputStream_HAIRMOD_PROTECTED.close();
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
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(2))
		{
			index--;
			if (index < 0)
			{
				index = nameServer.Length - 1;
			}
			flag = true;
		}
		else if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(8))
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
		ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = myScroll.updateKey();
		if (scrollResult_HAIRMOD_PROTECTED.isDowning || scrollResult_HAIRMOD_PROTECTED.isFinish)
		{
			index = scrollResult_HAIRMOD_PROTECTED.selected;
		}
		if (isPointer(Canvas_HAIRMOD_PROTECTED.hw - 100, Canvas_HAIRMOD_PROTECTED.hh - 80 + 20, 200, 160) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
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
		if (Canvas_HAIRMOD_PROTECTED.px >= x && Canvas_HAIRMOD_PROTECTED.px <= x + w && Canvas_HAIRMOD_PROTECTED.py >= y && Canvas_HAIRMOD_PROTECTED.py <= y + h)
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
		xTo = Util_HAIRMOD_PROTECTED.Cos(goc) * radius >> 10;
		yTo = Util_HAIRMOD_PROTECTED.Sin(goc) * radius >> 10;
		GameScr_HAIRMOD_PROTECTED.cmtoX = xTo + 380;
		GameScr_HAIRMOD_PROTECTED.cmtoY = yTo + 380;
		Canvas_HAIRMOD_PROTECTED.gameScr.updateCamera();
	}
	private Texture2D imgMap;
    private Texture2D LoadTexture2D(string url)
    {
        var TextAssets = Resources.Load(url) as TextAsset;
        return Image_HAIRMOD_PROTECTED.createImage(TextAssets.bytes).texture;
    }
    public void paintBg(MyGraphics_HAIRMOD_PROTECTED g)
    {
        try
        {
            if (imgMap == null)
            {
                imgMap = LoadTexture2D("map");
                return;
            }
            GUI.DrawTexture(new Rect(0, 0, ScaleGUI_HAIRMOD_PROTECTED.WIDTH, ScaleGUI_HAIRMOD_PROTECTED.HEIGHT), imgMap);
        }
        catch (Exception e)
        {
            Debug.LogError(e.ToString());
        }
    }

    public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		paintBg(g);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.drawImage(Canvas_HAIRMOD_PROTECTED.imgLogo, Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh - 130, 3);
		paintPanel(g);
		base.paint(g);
	}

	private void paintPanel(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintDlgDragonFull(g, Canvas_HAIRMOD_PROTECTED.hw - 100, Canvas_HAIRMOD_PROTECTED.hh - 80 + 20, 200, 160);
		g.setClip(Canvas_HAIRMOD_PROTECTED.hw - 100, Canvas_HAIRMOD_PROTECTED.hh - 56, w, h - 7);
		g.translate(0, -cmy);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		myScroll.setStyle(nameServer.Length + 1, 30, Canvas_HAIRMOD_PROTECTED.hw - 100, Canvas_HAIRMOD_PROTECTED.hh - 50, 200, 150, true, 1);
		myScroll.setClip(g, Canvas_HAIRMOD_PROTECTED.hw - 100, Canvas_HAIRMOD_PROTECTED.hh - 50, 200, 130);
		for (int i = 0; i < nameServer.Length; i++)
		{
			if (index == i)
			{
				g.setColor(11908790);
				g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 96, Canvas_HAIRMOD_PROTECTED.hh - 52 + index * 30 + 5, 193, 30);
				g.setColor(34949);
				g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 95, Canvas_HAIRMOD_PROTECTED.hh - 51 + index * 30 + 5, 191, 28);
			}
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, nameServer[i], Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh - 40 + i * 30, 2);
		}
	}

	public static void close()
	{
		me = null;
	}
}
