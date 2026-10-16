// Port 1:1 từ Jar276/src/game/render/screen/TienIch.java
using System;
using System.Threading;

/**
 * Menu Tiện ích: đăng xuất, bật/tắt thông báo popup, tự đăng nhập lại khi mất kết nối, Thổ địa phù
 * về làng ngay. tick() gọi mỗi khung hình từ AutoGuard.paint (chạy ở MỌI màn hình, kể cả màn đăng
 * nhập - auto đăng nhập cần vậy).
 */
public sealed class TienIch
{
	// Cắt chuỗi thanh trạng thái cho vừa màn hình, nhớ 4 kết quả gần nhất (mỗi khung hình vẽ lại cùng chuỗi:
	// trước đây cắt từng ký tự + đo lại, mỗi chuỗi ngắn hơn lại vào bộ nhớ đo MFont). Chỉ gọi từ luồng vẽ.
	private static readonly string[] CAT_GOC = new string[4];

	private static readonly string[] CAT_KQ = new string[4];

	private static readonly int[] CAT_W = new int[4];

	private static int catTiep;

	public static string catVua(string s, MFont font)
	{
		for (int i = 0; i < 4; ++i)
		{
			if (CAT_W[i] == Canvas.w && s == CAT_GOC[i])
			{
				return CAT_KQ[i];
			}
		}
		string kq = s;
		while (kq.Length > 6 && font.getWidth(kq) > Canvas.w - 12)
		{
			kq = kq.Substring(0, kq.Length - 1);
		}
		CAT_GOC[catTiep] = s;
		CAT_KQ[catTiep] = kq;
		CAT_W[catTiep] = Canvas.w;
		catTiep = (catTiep + 1) % 4;
		return kq;
	}

	private const string RMS_TEN = "kpahti";

	/** Tắt thông báo: nuốt mọi hộp thoại OK của server khi đang trong game (màn đăng nhập vẫn hiện). */
	public static bool tatThongBao;

	/** Auto login (bật/tắt ở menu Auto up): mất kết nối thì tự đăng nhập lại + chọn đúng nhân vật. */
	public static bool tuDangNhap;

	/** Thổ địa phù về làng ngay: client gốc đếm 10s rồi mới gửi lệnh về làng; server không kiểm thời gian. */
	private const bool thoDiaNhanh = true;

	/** Tầm skill phía client luôn = 300 (server vẫn chỉ nhận đòn trong ô vuông 130px). */
	private const short TAM_DANH = 300;

	/** Auto up đổi sang quái khác mỗi 1 giây. */
	public static bool doiMucTieu = true;

	public static bool muaThe;

	public static bool muaBinh;

	public static bool muaVe;

	/** Auto up tự dùng thẻ mua bán vào nhà sửa đồ khi vũ khí sắp hết bền. */
	public static bool suaDo;

	/** Hộp thoại do tự sửa đồ sinh ra (Đã sửa xong / Còn ...s nữa ...): nuốt đi. */
	public static int anSua;

	private static bool daNap;

	private const int THE_MUA_BAN = 675;

	private const int SHOP_VE_GIO_VANG = 0;

	private const int SHOP_HP_15K = 127;

	private const int SHOP_MP_15K = 128;

	private const int BINH_HP_15K = 94;

	private const int BINH_MP_15K = 96;

	private const int CON_IT = 30;

	private static long lucMua;

	/** Hộp "Đã mua..." / "Không đủ..." do chính tự-mua gây ra: nuốt đi. */
	private static int anMua;

	/** [loại] số lần mua liên tiếp chưa thấy kết quả / nghỉ tới lúc. 0 = HP, 1 = MP, 2 = vé, 3 = thẻ */
	private static readonly int[] thuMua = new int[4];

	private static readonly long[] nghiToi = new long[4];

	private static readonly int[] soTruoc = new int[4];

	/** Đã vào game ít nhất một lần (mới có tài khoản / nhân vật để đăng nhập lại). */
	private static bool daVaoGame;

	/** Người chơi tự bấm Đăng xuất: không tự đăng nhập lại tới khi vào game lần sau. */
	private static bool chuDongThoat;

	private static string tenNhanVat;

	private static long lucThu;

	private static long lucChon;

	private static bool dangLam;

	private TienIch()
	{
	}

	public static void moMenu(int chon)
	{
		TienIch.nap();
		mVector vector = new mVector();
		vector.addElement(new Command(LenThanh.dang ? "Dừng lên thành" : "Lên thành")
		{
			action = () =>
			{
				if (LenThanh.dang)
				{
					LenThanh.tat();
				}
				else
				{
					LenThanh.bat();
				}
			}
		});
		vector.addElement(new Command("Đăng xuất")
		{
			action = () =>
			{
				TienIch.dangXuat();
			}
		});
		vector.addElement(new Command(tatThongBao ? "Thông báo: tắt" : "Thông báo: bật")
		{
			action = () =>
			{
				tatThongBao = !tatThongBao;
				TienIch.luu();
				TienIch.moMenu(2);
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	/** Auto up gọi: 1 = đổi mục tiêu, 2 = auto login, 3 = mua bình, 4 = mua vé, 5 = mua thẻ. */
	public static void lat(int n)
	{
		TienIch.nap();
		if (n == 2)
		{
			tuDangNhap = !tuDangNhap;
		}
		else if (n == 3)
		{
			muaBinh = !muaBinh;
		}
		else if (n == 4)
		{
			muaVe = !muaVe;
		}
		else if (n == 5)
		{
			muaThe = !muaThe;
		}
		else if (n == 6)
		{
			suaDo = !suaDo;
		}
		else
		{
			doiMucTieu = !doiMucTieu;
		}
		TienIch.luu();
	}

	/** Trạng thái để Auto up vẽ menu: 3 = mua bình, 4 = mua vé, 5 = mua thẻ. */
	public static bool bat(int n)
	{
		TienIch.nap();
		return n == 3 ? muaBinh : (n == 4 ? muaVe : (n == 5 ? muaThe : suaDo));
	}

	public static bool autoLogin()
	{
		TienIch.nap();
		return tuDangNhap;
	}

	/** Menu xa phu của map đang đứng, mở từ xa (server chỉ đòi map có xa phu, không đòi đứng gần NPC). */
	public static void moXaPhu()
	{
		GameScr gameScr = Canvas.gameScr;
		XaphuTemplate xaphuTemplate = Res.getXaphu();
		if (gameScr == null || xaphuTemplate == null || xaphuTemplate.mapID == null)
		{
			Canvas.startOKDlg("Map này không có xa phu.");
			return;
		}
		mVector vector = new mVector();
		for (int i = 0; i < xaphuTemplate.mapID.Length; ++i)
		{
			int n = i;
			int n2 = xaphuTemplate.mapID[i];
			string @string = n2 >= 0 && n2 < Res.nameBigMap.Length ? Res.nameBigMap[n2] : n2.ToString();
			vector.addElement(new Command(@string)
			{
				action = () =>
				{
					if (xaphuTemplate.price != null && n < xaphuTemplate.price.Length && (long)xaphuTemplate.price[n] > gameScr.mainChar.charXu)
					{
						Canvas.startOKDlg("Không còn đủ tiền");
						return;
					}
					gameScr.doUseXaPhu(n2, n);
				}
			});
		}
		PauseMenu.moMenuGiuChon(vector, 2, 0);
	}

	private static void dangXuat()
	{
		chuDongThoat = true;
		Canvas.menu.showMenu = false;
		AutoUp.tat();
		AutoVanTieu.tat();
		AutoVanMay.tat();
		TienIch.veManChonServer();
	}

	/** Y hệt nút OK của hộp "Bị ngắt kết nối" (GameScr.onDisconnect). */
	private static void veManChonServer()
	{
		Canvas.endDlg();
		Canvas.instance.init();
		Session_ME.gI().close();
		Tilemap.loadMap(0, null);
		ServerListScr.gI().switchToMe();
	}

	/** Gọi từ AutoGuard.autoEatDlg: true = nuốt hộp thoại này. */
	public static bool anThongBao(string @string)
	{
		TienIch.nap();
		if (anSua > 0 && @string != null && (@string.StartsWith("Đã sửa", System.StringComparison.Ordinal) || @string.StartsWith("Còn ", System.StringComparison.Ordinal) || @string.StartsWith("Hết tiền", System.StringComparison.Ordinal)))
		{
			--anSua;
			return true;
		}
		if (anMua > 0 && @string != null && (@string.StartsWith("Đã mua", System.StringComparison.Ordinal) || @string.StartsWith("Không đủ", System.StringComparison.Ordinal) || @string.StartsWith("Hành trang", System.StringComparison.Ordinal)))
		{
			--anMua;
			return true;
		}
		return tatThongBao && Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr;
	}

	private static bool coLuong;

	/** C#: đã xếp một lượt tick vào LuongChinh mà luồng chính chưa chạy. */
	private static volatile bool choTick;

	private static readonly object khoaLuong = new object();

	/**
	 * Chạy tick() trên luồng riêng mỗi 0.1s. Trước đây gọi từ AutoGuard.paint, mà khi ghép nhiều
	 * bản (EmbedAdvMenu) bản đang ẩn KHÔNG được vẽ -> auto login / tự mua / Thổ địa nhanh / tầm 300
	 * không chạy ở bản ẩn. Luồng cập nhật của game thì vẫn chạy nên gọi được từ cả hai chỗ.
	 *
	 * C# (Unity): tick() chạy trên thread nền như bản jar. Phần lớn tick() chỉ đọc/ghi field
	 * (an toàn); các chỗ đụng tới Unity (vẽ / tạo ảnh / GUI) được ghi chú "LUỒNG CHÍNH" bên dưới -
	 * Unity API gọi ngoài luồng chính sẽ ném lỗi (bị catch, vòng sau thử lại). Người tích hợp có thể
	 * chuyển tick() sang gọi từ Update/OnGUI nếu thấy lỗi.
	 */
	public static void batLuong()
	{
		lock (khoaLuong)
		{
			if (coLuong)
			{
				return;
			}
			coLuong = true;
			// C# (thêm so với jar): RMS dùng PlayerPrefs (chỉ gọi được ở luồng chính) -> nạp cài đặt ngay ở
			// đây (batLuong gọi từ AutoGuard.paint = luồng chính), tránh nap() lần đầu chạy trong thread nền
			// bị lỗi mà daNap đã = true -> mất cài đặt.
			TienIch.nap();
			Thread thread = new Thread(() =>
			{
				while (true)
				{
					try
					{
						Thread.Sleep(100);
						// C#: tick đụng game (dialog, đổi màn, gửi lệnh) -> chạy ở luồng chính Unity; chỉ xếp
						// một lần chờ nếu luồng chính chưa chạy lượt trước (không dồn hàng khi game bị treo)
						if (!choTick)
						{
							choTick = true;
							LuongChinh.chay(() =>
							{
								choTick = false;
								TienIch.tick();
							});
						}
					}
					catch (Exception)
					{
						// vòng sau chạy tiếp
					}
				}
			});
			// C#: luồng nền để không giữ tiến trình Unity khi thoát game (Java không cần)
			thread.IsBackground = true;
			thread.Start();
		}
	}

	public static void tick()
	{
		TienIch.nap();
		long l = mSystem.currentTimeMillis();
		GameScr gameScr = Canvas.gameScr;
		MainChar mainChar = gameScr == null ? null : gameScr.mainChar;
		// Java: Session_ME.gI().connected (C#: field static)
		bool bl = Session_ME.connected;
		if (thoDiaNhanh && mainChar != null && mainChar.comeHome && l - mainChar.timeComeHome < 10000L)
		{
			mainChar.timeComeHome = l - 10001L;
		}
		TienIch.apTam();
		if (mainChar != null && Canvas.currentScreen == gameScr && bl && mainChar.name != null && mainChar.name.Length > 0)
		{
			try
			{
				TienIch.tuMua(gameScr, mainChar, l);
			}
			catch (Exception)
			{
				// empty catch block
			}
			daVaoGame = true;
			chuDongThoat = false;
			tenNhanVat = mainChar.name;
			lucThu = 0L;
			lucChon = 0L;
			return;
		}
		if (!tuDangNhap || !daVaoGame || chuDongThoat || dangLam || AutoGuard.tkDangNhap == null || AutoGuard.mkDangNhap == null)
		{
			return;
		}
		// mất kết nối khi đang trong game: hộp "Bị ngắt kết nối" -> bấm OK thay người chơi
		if (Canvas.currentScreen == gameScr && !bl && Canvas.currentDialog != null)
		{
			TienIch.chay(0);
			return;
		}
		// màn chọn server / đăng nhập: đăng nhập lại (thử lại mỗi 20s)
		if ((Canvas.currentScreen is ServerListScr || Canvas.currentScreen is LoginScr) && l >= lucThu)
		{
			lucThu = l + 20000L;
			TienIch.chay(1);
			return;
		}
		// màn chọn nhân vật: chọn đúng nhân vật đang chơi lúc trước
		CharSelectScr charSelectScr = CharSelectScr.me;
		if (charSelectScr != null && Canvas.currentScreen == charSelectScr && charSelectScr.charInfos != null && charSelectScr.cmdSelect != null && l >= lucChon)
		{
			lucChon = l + 10000L;
			for (int i = 0; i < charSelectScr.charInfos.Length; ++i)
			{
				if (charSelectScr.charInfos[i] == null || tenNhanVat == null || tenNhanVat != charSelectScr.charInfos[i].name)
				{
					continue;
				}
				charSelectScr.selected = i;
				break;
			}
			TienIch.chay(2);
		}
	}

	/** Tầm mọi skill phía client = 300 (bảng có thể được nạp lại -> đặt lại mỗi khung hình). */
	private static void apTam()
	{
		short[][] sArray2 = SkillManager.SKILL_RANGE;
		short[] sArray = sArray2 == null ? null : sArray2[0];
		if (sArray == null)
		{
			return;
		}
		for (int i = 0; i < sArray.Length; ++i)
		{
			sArray[i] = TAM_DANH;
		}
	}

	/** Mỗi 3s mua tối đa một món; mua 3 lần liền không thấy kết quả (hết tiền...) thì nghỉ 5 phút. */
	private static void tuMua(GameScr gameScr, MainChar mainChar, long l)
	{
		if (l < lucMua || !(muaBinh || muaVe || muaThe))
		{
			return;
		}
		lucMua = l + 3000L;
		int[] nArray = mainChar.potions;
		if (muaBinh && nArray != null && nArray.Length > BINH_MP_15K)
		{
			if (TienIch.canMua(0, nArray[BINH_HP_15K], l) && nArray[BINH_HP_15K] < CON_IT)
			{
				TienIch.daGui(0, nArray[BINH_HP_15K]);
				GameService.gI().buyItemShopSpecial(SHOP_HP_15K);
				return;
			}
			if (TienIch.canMua(1, nArray[BINH_MP_15K], l) && nArray[BINH_MP_15K] < CON_IT)
			{
				TienIch.daGui(1, nArray[BINH_MP_15K]);
				GameService.gI().buyItemShopSpecial(SHOP_MP_15K);
				return;
			}
		}
		if (muaVe && !TienIch.gioVangCon(mainChar, l))
		{
			GemTemplate gemTemplate = TienIch.veTrongTui();
			if (gemTemplate != null)
			{
				// có vé: dùng (không tính là lần mua)
				GameService.gI().doUseSpecialItem(gemTemplate.rID);
				lucMua = l + 5000L;
				return;
			}
			if (TienIch.canMua(2, 0, l))
			{
				TienIch.daGui(2, 0);
				GameService.gI().buyItemShopSpecial(SHOP_VE_GIO_VANG);
				return;
			}
		}
		if (muaThe && gameScr.isOffLineMap(gameScr.map) && !TienIch.coThe() && TienIch.canMua(3, 0, l))
		{
			TienIch.daGui(3, 0);
			Message message = new Message(24);
			message.writer().writeByte((sbyte)1);
			message.writer().writeByte((sbyte)3);
			message.writer().writeShort((short)THE_MUA_BAN);
			message.writer().writeShort((short)1);
			message.writer().writeByte((sbyte)0);
			GameService.gI().session.sendMessage(message);
			message.cleanup();
		}
	}

	/** Còn được mua loại k? Số lượng tăng so với lần gửi trước = mua được -> đếm lại từ đầu. */
	private static bool canMua(int k, int n, long l)
	{
		if (n > soTruoc[k])
		{
			thuMua[k] = 0;
		}
		soTruoc[k] = n;
		if (l < nghiToi[k])
		{
			return false;
		}
		if (thuMua[k] >= 3)
		{
			thuMua[k] = 0;
			nghiToi[k] = l + 300000L;
			return false;
		}
		return true;
	}

	private static void daGui(int k, int n)
	{
		++thuMua[k];
		soTruoc[k] = n;
		anMua = Math.min(anMua + 1, 3);
	}

	private static bool gioVangCon(MainChar mainChar, long l)
	{
		if (Char.goldTimeMinute <= 0 || mainChar.timeUseGolgTicket <= 0L)
		{
			return false;
		}
		return l - mainChar.timeUseGolgTicket < (long)Char.goldTimeMinute * 60000L;
	}

	/** Vé giờ vàng 100 (shop id 0) đang có trong túi vật phẩm đặc biệt. */
	private static GemTemplate veTrongTui()
	{
		mVector vector = MainChar.shopItem;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			GemTemplate gemTemplate = (GemTemplate)vector.elementAt(i);
			if (gemTemplate == null || gemTemplate.id != SHOP_VE_GIO_VANG || gemTemplate.number <= 0)
			{
				continue;
			}
			return gemTemplate;
		}
		return null;
	}

	private static bool coThe()
	{
		return TienIch.theMuaBan() != null;
	}

	/** Thẻ mua bán trong hành trang (null = không có). */
	public static ItemInInventory theMuaBan()
	{
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			object @object = vector.elementAt(i);
			if (!(@object is ItemInInventory) || ((ItemInInventory)@object).idTemplate != THE_MUA_BAN)
			{
				continue;
			}
			return (ItemInInventory)@object;
		}
		return null;
	}

	/** Làm ngoài luồng vẽ (gọi từ paint): 0 = về màn chọn server, 1 = đăng nhập, 2 = chọn nhân vật. */
	private static void chay(int n)
	{
		dangLam = true;
		Thread thread = new Thread(() =>
		{
			// dangLam chỉ tắt SAU khi việc trên luồng chính chạy xong (trước đây tắt ngay khi gửi đi -> tick sau gửi
			// thêm một lần đăng nhập / chọn nhân vật trong lúc việc trước còn chờ, khác bản jar)
			Action xong = () => dangLam = false;
			try
			{
				// C#: thread nền chỉ chờ như bản jar; việc đụng game (đổi màn, hộp thoại, kết nối) gửi sang luồng chính
				if (n == 0)
				{
					LuongChinh.chay(() =>
					{
						try { TienIch.veManChonServer(); } finally { xong(); }
					});
				}
				else if (n == 1)
				{
					Thread.Sleep(1500);
					LuongChinh.chay(() =>
					{
						try
						{
							Canvas.startWaitDlg("Tự đăng nhập lại...", true);
							Canvas.connect();
							GameService.gI().setClientType();
							GameService.gI().login(AutoGuard.tkDangNhap, AutoGuard.mkDangNhap, GameMidlet.version);
							GameScr.saveAutoFight = false;
						}
						finally
						{
							xong();
						}
					});
				}
				else
				{
					Thread.Sleep(1000);
					LuongChinh.chay(() =>
					{
						try { CharSelectScr.me.cmdSelect.action(); } finally { xong(); }
					});
				}
			}
			catch (Exception)
			{
				// lần sau thử lại
				xong();
			}
		});
		thread.IsBackground = true;
		thread.Start();
	}

	private static void nap()
	{
		if (daNap)
		{
			return;
		}
		daNap = true;
		try
		{
			string @string = RMS.loadRMSString(RMS_TEN);
			if (@string != null && @string.Length >= 3)
			{
				tatThongBao = @string[0] == '1';
				tuDangNhap = @string[1] == '1';
			}
			if (@string != null && @string.Length >= 8)
			{
				doiMucTieu = @string[4] == '1';
				muaThe = @string[5] == '1';
				muaBinh = @string[6] == '1';
				muaVe = @string[7] == '1';
			}
			if (@string != null && @string.Length >= 9)
			{
				suaDo = @string[8] == '1';
			}
		}
		catch (Exception)
		{
			// mặc định
		}
	}

	private static void luu()
	{
		try
		{
			RMS.saveRMSString(RMS_TEN, new System.Text.StringBuilder().Append(tatThongBao ? '1' : '0').Append(tuDangNhap ? '1' : '0').Append('1').Append('1').Append(doiMucTieu ? '1' : '0').Append(muaThe ? '1' : '0').Append(muaBinh ? '1' : '0').Append(muaVe ? '1' : '0').Append(suaDo ? '1' : '0').ToString());
		}
		catch (Exception)
		{
			// empty catch block
		}
	}
}
