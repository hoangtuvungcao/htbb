// Port 1:1 từ Jar276/src/game/render/screen/LenThanh.java

/**
 * Tự chạy lên Thành (map 201). Thành vào từ cổng (39, 90) ở map 9; đường đi bộ từ làng giống
 * đường vận tiêu: làng -> 2 -> 3 -> 5 -> 6 -> 7 -> 8 -> 9 -> Thành. Map có xa phu tới map trên
 * đường thì nhảy thẳng tới map gần Thành nhất; đứng ở map lạ thì dùng Thổ địa phù về làng trước.
 * Gọi mỗi khung hình từ PauseMenu.autoCheDoTick. Bấm phím bất kỳ là dừng.
 */
public sealed class LenThanh
{
	private const int THANH = 201;

	private static readonly int[] DUONG = new int[8] { 2, 3, 5, 6, 7, 8, 9, THANH };

	private const int THO_DIA = 19;

	public static bool dang;

	private static int buoc;

	private static long luc;

	private static long lucBat;

	private static int mapTruoc;

	private static int dichMap;

	private static bool[] dich;

	private static int[] duong;

	private static int duongPos;

	private static long lucKet;

	private static int ketX;

	private static int ketY;

	private static int lanKet;

	private static bool giuPhim;

	private static string trangThai = "";

	private LenThanh()
	{
	}

	public static void bat()
	{
		GameScr gameScr = Canvas.gameScr;
		if (gameScr == null || gameScr.mainChar == null)
		{
			return;
		}
		AutoUp.tat();
		AutoVanTieu.tat();
		AutoVanMay.tat();
		dang = true;
		buoc = 0;
		lucBat = mSystem.currentTimeMillis();
		dich = null;
		duong = null;
		Canvas.menu.showMenu = false;
	}

	public static void tat()
	{
		if (!dang)
		{
			return;
		}
		dang = false;
		LenThanh.nha();
	}

	private static void dung(string @string)
	{
		LenThanh.tat();
		Canvas.startOKDlg(@string);
	}

	public static void tick()
	{
		if (!dang)
		{
			return;
		}
		GameScr gameScr = Canvas.gameScr;
		if (gameScr == null || gameScr.mainChar == null)
		{
			return;
		}
		MainChar mainChar = gameScr.mainChar;
		long l = mSystem.currentTimeMillis();
		if (Canvas.currentScreen != gameScr || Canvas.currentDialog != null)
		{
			LenThanh.nha();
			return;
		}
		if (l - lucBat > 500L && LenThanh.coBam())
		{
			LenThanh.dung("Đã dừng lên thành.");
			return;
		}
		if (mainChar.hp <= 0)
		{
			LenThanh.dung("Lên thành: nhân vật đã chết, dừng.");
			return;
		}
		switch (buoc)
		{
		case 0:
		{
			if (PauseMenu.oMap(THANH))
			{
				LenThanh.dung("Đã lên tới Thành.");
				return;
			}
			int n = LenThanh.mapKe();
			if (n < 0)
			{
				if (LenThanh.xaPhu(gameScr))
				{
					return;
				}
				if (mainChar.potions != null && mainChar.potions.Length > THO_DIA && mainChar.potions[THO_DIA] > 0)
				{
					trangThai = "Thổ địa phù về làng";
					gameScr.doUsePotion(THO_DIA);
					mapTruoc = gameScr.map;
					buoc = 3;
					luc = l;
					return;
				}
				LenThanh.dung("Lên thành: map này không có đường lên Thành, không có xa phu, hết Thổ địa phù.");
				return;
			}
			// có xa phu nhảy xa hơn bước đi bộ kế tiếp thì đi xa phu
			if (LenThanh.xaPhu(gameScr))
			{
				return;
			}
			dich = PauseMenu.dichSatCong(n);
			duong = null;
			lanKet = 0;
			if (dich == null)
			{
				LenThanh.dung(new System.Text.StringBuilder().Append("Lên thành: không thấy cổng sang map ").Append(n).Append(".").ToString());
				return;
			}
			dichMap = n;
			buoc = 1;
			return;
		}
		case 1:
		{
			trangThai = new System.Text.StringBuilder().Append("đi tới cổng (map ").Append(gameScr.map).Append(")").ToString();
			int n = LenThanh.di(mainChar, l);
			if (n < 0)
			{
				LenThanh.dung(new System.Text.StringBuilder().Append("Lên thành: không tìm được đường ở map ").Append(gameScr.map).Append(".").ToString());
				return;
			}
			if (n == 0)
			{
				return;
			}
			CMLocation cMLocation = PauseMenu.congToi(dichMap);
			if (cMLocation == null)
			{
				buoc = 0;
				return;
			}
			LenThanh.nha();
			mapTruoc = gameScr.map;
			PauseMenu.quaCong(cMLocation, dichMap);
			buoc = 3;
			luc = l;
			return;
		}
		case 3:
		{
			// chờ đổi map (qua cổng / xa phu / Thổ địa phù)
			if (gameScr.map != mapTruoc)
			{
				buoc = 4;
				luc = l + 1500L;
				return;
			}
			if (l - luc > 20000L)
			{
				buoc = 0;
			}
			return;
		}
		case 4:
		{
			if (l >= luc)
			{
				buoc = 0;
			}
			return;
		}
		}
		buoc = 0;
	}

	/** Map kế tiếp trên đường làng -> ... -> 9 -> Thành; -1 nếu map hiện tại không thuộc đường. */
	private static int mapKe()
	{
		if (Tilemap.lv == 0 || Tilemap.lv == 70 || Tilemap.lv == 80)
		{
			return DUONG[0];
		}
		for (int i = 0; i < DUONG.Length - 1; ++i)
		{
			if (!PauseMenu.oMap(DUONG[i]))
			{
				continue;
			}
			return DUONG[i + 1];
		}
		return -1;
	}

	/** Vị trí map hiện tại trên đường (-1 làng / không thuộc đường). */
	private static int viTri()
	{
		for (int i = 0; i < DUONG.Length; ++i)
		{
			if (!PauseMenu.oMap(DUONG[i]))
			{
				continue;
			}
			return i;
		}
		return -1;
	}

	/** Xa phu ở map này tới được map trên đường xa hơn chỗ đang đứng: đi tới map gần Thành nhất. */
	private static bool xaPhu(GameScr gameScr)
	{
		XaphuTemplate xaphuTemplate = Res.getXaphu();
		if (xaphuTemplate == null || xaphuTemplate.mapID == null)
		{
			return false;
		}
		int n = LenThanh.viTri();
		int n2 = -1;
		int n3 = -1;
		for (int i = 0; i < xaphuTemplate.mapID.Length; ++i)
		{
			for (int j = DUONG.Length - 2; j > n && j > n3; --j)
			{
				if (xaphuTemplate.mapID[i] != DUONG[j])
				{
					continue;
				}
				n3 = j;
				n2 = i;
				break;
			}
		}
		if (n2 < 0 || (xaphuTemplate.price != null && n2 < xaphuTemplate.price.Length && (long)xaphuTemplate.price[n2] > gameScr.mainChar.charXu))
		{
			return false;
		}
		short s = gameScr.getIndexArrMap(DUONG[n3]);
		if (s < 0 || Res.localXaphu == null || s >= Res.localXaphu.Length || Res.localXaphu[s] == null)
		{
			return false;
		}
		trangThai = "đi xa phu";
		LenThanh.nha();
		mapTruoc = gameScr.map;
		GameService.gI().doMove2Map((sbyte)n2, Res.localXaphu[s][0]);
		ChangScr.gI().switchToMe();
		ChangScr.gI().setMap(gameScr.map, 0, 0);
		Res.removeMonsterTemplet();
		buoc = 3;
		luc = mSystem.currentTimeMillis();
		return true;
	}

	/** Đi theo đường BFS tới vùng dich. 1 = tới nơi, 0 = đang đi, -1 = không có đường. */
	private static int di(MainChar mainChar, long l)
	{
		if (dich == null)
		{
			return -1;
		}
		int n = Tilemap.w;
		int n2 = mainChar.x / 16;
		int n3 = mainChar.y / 16;
		int n4 = n3 * n + n2;
		if (n2 >= 0 && n3 >= 0 && n2 < n && n4 < dich.Length && dich[n4])
		{
			LenThanh.nha();
			return 1;
		}
		if (duong == null)
		{
			duong = PauseMenu.timDuong(dich);
			duongPos = 0;
			if (duong == null)
			{
				return -1;
			}
			if (duong.Length == 0)
			{
				duong = null;
				return 1;
			}
		}
		int n5 = duong[duongPos];
		int n6 = n5 % n * 16 + 8 - mainChar.x;
		int n7 = n5 / n * 16 + 8 - mainChar.y;
		int n8 = Math.max(3, (int)mainChar.speed);
		if (Math.abs(n6) <= n8 && Math.abs(n7) <= n8)
		{
			if (++duongPos >= duong.Length)
			{
				duong = null;
			}
			return 0;
		}
		LenThanh.nha();
		Canvas.keyHold[Math.abs(n6) > n8 ? (n6 < 0 ? 4 : 6) : (n7 < 0 ? 2 : 8)] = true;
		giuPhim = true;
		if (lucKet == 0L || mainChar.x != ketX || mainChar.y != ketY)
		{
			lucKet = l;
			ketX = mainChar.x;
			ketY = mainChar.y;
		}
		else if (l - lucKet > 1500L)
		{
			lucKet = 0L;
			duong = null;
			if (++lanKet >= 5)
			{
				return -1;
			}
		}
		return 0;
	}

	private static void nha()
	{
		if (!giuPhim)
		{
			return;
		}
		giuPhim = false;
		Canvas.keyHold[2] = false;
		Canvas.keyHold[4] = false;
		Canvas.keyHold[6] = false;
		Canvas.keyHold[8] = false;
	}

	private static bool coBam()
	{
		for (int i = 0; i < Canvas.keyPressed.Length; ++i)
		{
			if (!Canvas.keyPressed[i])
			{
				continue;
			}
			return true;
		}
		return Canvas.isPointerDown || Canvas.isPointerClick;
	}

	public static void ve(MyGraphics graphics, int n)
	{
		if (!dang || MFont.normalFont == null || MFont.normalFont[0] == null)
		{
			return;
		}
		string @string = new System.Text.StringBuilder().Append("Lên thành: ").Append(trangThai).ToString();
		MFont font = MFont.normalFont[0];
		while (@string.Length > 6 && font.getWidth(@string) > Canvas.w - 12)
		{
			@string = @string.Substring(0, @string.Length - 1);
		}
		int n2 = Math.min(Canvas.w, font.getWidth(@string) + 12);
		int n3 = font.getHeight() + 6;
		int n4 = (Canvas.w - n2) / 2;
		graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
		graphics.setClip(0, 0, Canvas.w, Canvas.h);
		graphics.setColor(0x2E0B2A);
		graphics.fillRect(n4, n, n2, n3);
		graphics.setColor(0xE040D0);
		graphics.drawRect(n4, n, n2 - 1, n3 - 1);
		font.drawString(graphics, @string, Canvas.w / 2, n + 3, 2);
	}
}
