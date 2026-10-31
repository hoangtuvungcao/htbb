using System;
using System.Collections.Generic;
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

	public static int index = -1;

	// Mặc định = đúng MỘT máy chủ Huyền Thoại Bảo Bối (như bản jar 0.9.23, ServerListScr.java); máy chủ cố định của bản
	// build (ThuongHieu.mayChuCoDinh) thay nó ở apMayChuCoDinh. Trước đây là danh sách server của game gốc (IP người khác).
	public static string[] nameServer = new string[1] { ThuongHieu.TEN_GAME };

	public static string[] address = new string[1] { MAY_CHU_MAC_DINH };

	public static short[] port = new short[1] { CONG_MAC_DINH };

	public static sbyte[] index_server = new sbyte[1];

	/// <summary>IP origin máy chủ HTBB (giống bản jar) - chỉ dùng khi bản build không mang máy chủ cố định.</summary>
	public const string MAY_CHU_MAC_DINH = "180.93.57.30";

	public const short CONG_MAC_DINH = 19129;

	/// <summary>Rỗng = không tải danh sách server (bản HTBB chỉ có máy chủ của mình, như TaiDanhSach bản jar).</summary>
	public static string linkGetHost = string.Empty;

	public static string nameSvAuto = string.Empty;

	public static string addressAuTo = string.Empty;

	public static short portAuTo = -1;

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
		ActionPerform action = () =>
		{
			doUpdateServer();
		};
		left.action = action;
		center = new Command("Chọn");
		ActionPerform action2 = () =>
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

	/// <summary>
	/// pokemon: máy chủ cố định (ThuongHieu.mayChuCoDinh: -htbbMayChu / MAY_CHU_CHINH / bản dev 127.0.0.1:19129)
	/// - danh sách chỉ còn máy chủ đó, không tải robots.txt, không dùng danh sách / máy đăng nhập tự động đã lưu.
	/// true = đã áp.
	/// </summary>
	public static bool apMayChuCoDinh()
	{
		string mc = ThuongHieu.mayChuCoDinh();
		if (string.IsNullOrEmpty(mc))
		{
			return false;
		}
		int i = mc.LastIndexOf(':');
		short cong;
		if (i <= 0 || !short.TryParse(mc.Substring(i + 1), out cong))
		{
			return false;
		}
		nameServer = new string[1] { ThuongHieu.TEN_GAME };
		address = new string[1] { mc.Substring(0, i) };
		port = new short[1] { cong };
		index_server = new sbyte[1];
		index = 0;
		nameSvAuto = nameServer[0];
		addressAuTo = address[0];
		portAuTo = cong;
		LoginScr.nameServer = nameServer[0];
		return true;
	}

	public static void loadSv(bool isHTBB)
	{
		if (apMayChuCoDinh())
		{
			return;
		}
		if (isHTBB)
		{
			// Giống bản jar 0.9.23 (ServerListScr / TaiDanhSach): đúng một máy chủ HTBB, không tải danh sách server nào
			nameServer = new string[1] { ThuongHieu.TEN_GAME };
			address = new string[1] { MAY_CHU_MAC_DINH };
			port = new short[1] { CONG_MAC_DINH };
			index_server = new sbyte[1];
			linkGetHost = string.Empty;
			GameMidlet.numberSupport = "19006610";
		}
		else
		{
			nameServer = new string[4] { "Văn Lang", "Đại Việt", "Đại Nam", "Đại la" };
			address = new string[5] { "112.213.94.9", "112.213.94.9", "112.213.94.9", "112.213.94.9", "112.213.94.9" };
			port = new short[5] { 19129, 19129, 19129, 19129, 19129 };
			index_server = new sbyte[5] { 3, 2, 1, 0, 0 };
			linkGetHost = "http://teamobi.com/srvips/ngude2.txt";
			GameMidlet.numberSupport = "19001530";
		}
		Debug.Log("Ban: " + ((!isHTBB) ? "-- ngu de" : "-- huyen thoai bao boi"));
	}

	/// <summary>
	/// Áp danh sách server: mỗi dòng "Tên|host|port" (dòng trống / # bỏ qua), cùng định dạng với bản jar.
	/// Đúng thì lưu RMS (lần sau không có mạng vẫn dùng được) và trả true.
	/// </summary>
	public static bool apDungDanhSach(string str)
	{
		if (str == null)
		{
			return false;
		}
		if (apMayChuCoDinh())
		{
			return true; // pokemon: máy chủ cố định - bỏ danh sách tải về
		}
		List<string[]> list = new List<string[]>();
		string[] lines = str.Replace("\r", string.Empty).Split('\n');
		for (int i = 0; i < lines.Length; i++)
		{
			string line = lines[i].Trim();
			if (line.Length == 0 || line[0] == '#')
			{
				continue;
			}
			string[] parts = line.Split('|');
			short p;
			if (parts.Length < 3 || parts[0].Trim().Length == 0 || parts[1].Trim().Length == 0 || !short.TryParse(parts[2].Trim(), out p))
			{
				continue;
			}
			list.Add(parts);
		}
		if (list.Count == 0)
		{
			return false;
		}
		nameServer = new string[list.Count];
		address = new string[list.Count];
		port = new short[list.Count];
		index_server = new sbyte[list.Count];
		for (int i = 0; i < list.Count; i++)
		{
			nameServer[i] = list[i][0].Trim();
			address[i] = list[i][1].Trim();
			port[i] = short.Parse(list[i][2].Trim());
		}
		saveIP();
		return true;
	}

	/// <summary>
	/// Mỗi lần mở game tự tải danh sách server một lần (như bấm "Cập nhật" nhưng ngầm): đổi IP / cổng
	/// server chỉ cần sửa robots.txt trên web, máy người chơi tự theo. Lỗi mạng thì giữ danh sách đang có.
	/// </summary>
	public static System.Collections.IEnumerator taiDanhSachTuDong()
	{
		if (!string.IsNullOrEmpty(ThuongHieu.mayChuCoDinh()) || string.IsNullOrEmpty(linkGetHost))
		{
			yield break; // pokemon: máy chủ cố định / không có link danh sách - không tải gì
		}
		using (UnityEngine.Networking.UnityWebRequest r = UnityEngine.Networking.UnityWebRequest.Get(linkGetHost))
		{
			r.timeout = 10;
			yield return r.SendWebRequest();
			if (r.result != UnityEngine.Networking.UnityWebRequest.Result.Success)
			{
				Debug.LogWarning("Danh sach server: khong tai duoc " + linkGetHost + " - " + r.error);
				yield break;
			}
			if (apDungDanhSach(r.downloadHandler.text))
			{
				Debug.Log("Danh sach server: da cap nhat " + nameServer.Length + " server, " + address[0] + ":" + port[0]);
				if (me != null)
				{
					me.init();
				}
			}
		}
	}

	public override void switchToMe()
	{
		base.switchToMe();
		// am thanh: nhac man tieu de
		AmThanh.nhac(AmThanh.NHAC_TIEU_DE);
		myScroll.clear();
		goc = 0;
		xTo = (yTo = 0);
		Canvas.gameScr.loadCamera();
		updateCamera();
		init();
		cmdUpdateServer = new Command();
		ActionChat actionChat = (string str) =>
		{
			if (str == null)
			{
				Canvas.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
			}
			else if (!apDungDanhSach(str))
			{
				Canvas.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
			}
			else
			{
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
		right.action = () =>
		{
			ActionPerform yesAction = () =>
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
		if (string.IsNullOrEmpty(linkGetHost))
		{
			// nút "Cập nhật": danh sách cố định theo bản build - không tải gì (như TaiDanhSach.capNhat bản jar)
			loadSv(GameMidlet.isHTBB);
			init();
			return;
		}
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
			RMS.saveRMS(RMS.KHO_DS_MAY_CHU, dataOutputStream.toByteArray());
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
			RMS.saveRMS(RMS.KHO_MAY_CHU_TU_DONG, dataOutputStream.toByteArray());
			dataOutputStream.close();
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void loadIPAuTo()
	{
		if (apMayChuCoDinh())
		{
			return;
		}
		nameSvAuto = string.Empty;
		addressAuTo = string.Empty;
		portAuTo = -1;
		sbyte[] array = RMS.loadRMS(RMS.KHO_MAY_CHU_TU_DONG);
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
			dataInputStream.close();
			if (ThuongHieu.laMayChuCu(nameSvAuto, addressAuTo))
			{
				// máy đăng nhập tự động lưu từ bản cũ trỏ về server game gốc: bỏ
				nameSvAuto = string.Empty;
				addressAuTo = string.Empty;
				portAuTo = -1;
				return;
			}
			LoginScr.nameServer = nameSvAuto;
		}
		catch (IOException ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public static void loadIP()
	{
		if (apMayChuCoDinh())
		{
			return;
		}
		sbyte[] array = RMS.loadRMS(RMS.KHO_DS_MAY_CHU);
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
			string[] ten = new string[b];
			string[] dc = new string[b];
			short[] cg = new short[b];
			sbyte[] ix = new sbyte[b];
			for (int i = 0; i < b; i++)
			{
				ten[i] = dataInputStream.readUTF();
				dc[i] = dataInputStream.readUTF();
				cg[i] = dataInputStream.readShort();
				ix[i] = dataInputStream.readByte();
			}
			dataInputStream.close();
			// danh sách lưu từ bản cũ (nhiều server game gốc, IP người khác / server game gốc) thì bỏ, giữ mặc định
			// (như ServerListScr.loadIP bản jar)
			if (b == 1 && !ThuongHieu.laMayChuCu(ten[0], dc[0]))
			{
				nameServer = ten;
				address = dc;
				port = cg;
				index_server = ix;
			}
		}
		catch (IOException)
		{
			loadSv(GameMidlet.isHTBB);
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

	public void paintBg(MyGraphics g)
	{
		// pokemon: nền banner parallax riêng cho màn chọn máy chủ / đăng nhập / tạo - chọn Pokémon
		BannerParallax.ve(g);
	}

	public override void paint(MyGraphics g)
	{
		paintBg(g);
		Canvas.resetTrans(g);
		// Che do Pokemon: tieu de ThuongHieu (ten game + 5 Pokemon khoi dau) thay logo goc.
		if (!ThuongHieu.veTieuDe(g, Canvas.hw, Canvas.hh - 150))
		{
			g.drawImage(Canvas.imgLogo, Canvas.hw, Canvas.hh - 100, 3);
		}
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
