

/**
 * Vận tiêu theo bản "KPAH2-DOITIEU" (đọc từ bản đã giải mã):
 *  - Auto đổi (vòng, luồng teHFm): ở Biên giới thì dùng Thổ địa phù về làng; ở làng vào nhà rồi ra
 *    lại, đi tới chỗ đứng, rồi chạy Đổi tiêu.
 *  - Đổi tiêu (luồng knqO): nhận tiêu NPC -50 một lần, rồi cứ 250ms dùng Lệnh bài đổi tiêu cao cấp
 *    (116, hết mà còn >= 100 lượng thì mua gói 130). Server báo "Chúc mừng bạn đã đổi được tiêu
 *    <màu chọn>" (đúng màu) thì thôi đổi và chạy Đi tiêu.
 *  - Đi tiêu (luồng COMb): chạy thẳng theo các mốc ghi sẵn từng map, nháy map ở mỗi điểm dừng để
 *    server kéo xe tiêu tới chỗ mình, kiểm tra tiêu ở các mốc, tới Dịch trạm thì trả tiêu (NPC -51).
 *    Server báo "Chúc mừng bạn nhận được" ở Biên giới thì vòng Auto đổi chạy lại.
 * Khác bản gốc (để chạy được trên server này): đổi map qua cổng lấy toạ độ đến của đúng cổng đang
 * đứng (server kick người xin sang map kề sai cổng); nháy kéo xe sát trước khi qua cổng.
 */
public sealed class AutoVanTieu
{
	private const int MAP_BIEN_GIOI = 118;
	private const int NPC_NHAN = -50;
	private const int NPC_TRA = -51;
	private const int THO_DIA = 19;
	private const int LENH_CAO_CAP = 116;
	private const int SHOP_LENH_CAO_CAP = 130;
	/** Màu chọn (thứ tự như bản gốc; "Xanh lá" bản gốc = "Hoàn mỹ" trên server này). */
	private static readonly string[] MAU = new string[]{"Tím", "Vàng", "Hoàn mỹ", "Đỏ", "Xanh", "Trắng"};

	/** Có chức năng vận tiêu nào đang chạy (AutoGuard.chanXinQuai, thanh trạng thái). */
	public static bool dang;
	private static volatile bool chayVong;
	private static volatile bool chayDoi;
	private static volatile bool chayDi;
	/** Bật bằng Auto đổi / nút TIÊU: trả tiêu xong thì tự chạy chuyến tiếp. */
	private static volatile bool tuDong;
	private static int mauIndex = 0;
	public static int tocNhay = 30;
	public static bool kiemTra = true;
	private static int soTieu;
	private static int idTieu = -1;
	private static string trangThai = "";

	private AutoVanTieu()
	{
	}

	private static void capNhat()
	{
		dang = chayVong || chayDoi || chayDi;
		if (!chayDi)
		{
			AutoVanTieu.nha();
		}
	}

	// ------------------------------------------------------------------ menu

	public static void moMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command(chayVong || chayDoi ? "Auto đổi: Bật" : "Auto đổi: Tắt") { action = () =>
		{
				if (chayVong || chayDoi)
				{
					tuDong = false;
					chayVong = false;
					chayDoi = false;
				}
				else
				{
					AutoVanTieu.batVong();
				}
				AutoVanTieu.capNhat();
				AutoVanTieu.moMenu(0);
			} });
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Màu chọn: ").Append(MAU[mauIndex]).ToString()) { action = () =>
		{
				if (++mauIndex >= MAU.Length)
				{
					mauIndex = 0;
				}
				AutoVanTieu.moMenu(1);
			} });
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Tốc độ nháy: ").Append(tocNhay).ToString()) { action = () =>
		{
				// máy/mạng mạnh 100, yếu 200-300; còn bỏ rơi tiêu thì tăng lên
				Canvas.inputDlg.setInfo(new System.Text.StringBuilder().Append("Tốc độ nháy: ").Append(tocNhay).ToString(), () =>
				{
						try
						{
							int n = int.Parse(Canvas.inputDlg.tfInput.getText().Trim());
							if (n > 0)
							{
								tocNhay = n;
							}
						}
						catch (System.Exception)
						{
							// empty catch block
						}
						Canvas.endDlg();
						AutoVanTieu.moMenu(2);
					}, 1, 3, true);
				Canvas.inputDlg.show();
			} });
		vector.addElement(new Command(kiemTra ? "K.Tra Tiêu: Bật" : "K.Tra Tiêu: Tắt") { action = () =>
		{
				kiemTra = !kiemTra;
				AutoVanTieu.moMenu(3);
			} });
		vector.addElement(new Command(chayDi ? "Đi tiêu: đang chạy" : "Đi tiêu") { action = () =>
		{
				if (!chayDi)
				{
					AutoVanTieu.batDi();
				}
				Canvas.menu.showMenu = false;
			} });
		vector.addElement(new Command("Dừng toàn bộ") { action = () =>
		{
				AutoVanTieu.tat();
			} });
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	/** Nút TIÊU nổi trên màn game (AutoGuard): bật = Auto đổi (cả vòng), tắt = dừng toàn bộ. */
	public static void batTat()
	{
		if (dang)
		{
			AutoVanTieu.tat();
		}
		else if (Tilemap.lv == 0 || Tilemap.lv == 70 || Tilemap.lv == MAP_BIEN_GIOI)
		{
			AutoVanTieu.batVong();
		}
		else
		{
			// đang giữa đường (đã có tiêu): đi tiêu tiếp từ map này, trả xong thì chạy tiếp vòng
			tuDong = true;
			AutoVanTieu.batDi();
		}
	}

	/** Nút sao bản gốc: dừng toàn bộ thao tác. */
	public static void tat()
	{
		tuDong = false;
		chayVong = false;
		chayDoi = false;
		chayDi = false;
		AutoVanTieu.capNhat();
		Canvas.menu.showMenu = false;
	}

	// ------------------------------------------------------------------ thông báo server

	/** Gọi từ AutoGuard.autoEatDlg (checkThongBao bản gốc): true = nuốt hộp thoại. */
	public static bool thongBao(string @string)
	{
		if (!dang || @string == null)
		{
			return false;
		}
		if (chayDoi && @string.IndexOf("Chúc mừng bạn đã đổi được tiêu " + MAU[mauIndex]) != -1)
		{
			chayDoi = false;
			AutoVanTieu.batDi();
		}
		if (@string.IndexOf("Không thể sử dụng khi bạn đang chưa nhận tiêu") != -1)
		{
			chayDoi = false;
			AutoVanTieu.capNhat();
		}
		if (@string.IndexOf("Chúc mừng bạn nhận được") != -1)
		{
			if (Tilemap.lv == MAP_BIEN_GIOI && !chayVong)
			{
				++soTieu;
				chayDi = false;
				AutoVanTieu.batVong();
			}
		}
		return @string.IndexOf("tiêu") >= 0 || @string.StartsWith("Chúc mừng bạn nhận được", System.StringComparison.Ordinal) || @string.StartsWith("Đã mua", System.StringComparison.Ordinal) || @string.StartsWith("Bạn đã mua", System.StringComparison.Ordinal);
	}

	/** Mỗi khung hình (PauseMenu.autoCheDoTick): chỉ đóng menu chọn khu game tự bật khi đi vào vùng cổng. */
	public static void tick()
	{
		if (!chayDi || Canvas.gameScr == null)
		{
			return;
		}
		if (Canvas.menu.showMenu && Canvas.menu.menuItems != PauseMenu.menuMod)
		{
			Canvas.menu.showMenu = false;
		}
	}

	// ------------------------------------------------------------------ Auto đổi (vòng)

	private static void batVong()
	{
		if (chayVong)
		{
			return;
		}
		tuDong = true;
		chayVong = true;
		AutoVanTieu.capNhat();
		new System.Threading.Thread(() =>
		{
				try
				{
					AutoVanTieu.vong();
				}
				catch (System.Exception)
				{
					// empty catch block
				}
				chayVong = false;
				AutoVanTieu.capNhat();
			}) { IsBackground = true }.Start();
	}

	/** teHFm.run: về làng (Biên giới), vào nhà ra lại để đứng đúng chỗ, rồi chạy Đổi tiêu. */
	private static void vong()
	{
		while (chayVong)
		{
			AutoVanTieu.ngu(1000L);
			AutoVanTieu.ngu(1000L);
			if (!chayVong || Canvas.gameScr == null || Canvas.gameScr.mainChar == null)
			{
				continue;
			}
			if (Tilemap.lv == MAP_BIEN_GIOI)
			{
				// "sudungphu" + "velang" của bản gốc: dùng Thổ địa phù
				trangThai = "về làng";
				Canvas.gameScr.doUsePotion(THO_DIA);
				AutoVanTieu.ngu(2000L);
			}
			if (Tilemap.lv == 0 || Tilemap.lv == 70)
			{
				trangThai = "ra chỗ nhận tiêu";
				if (Tilemap.lv == 0)
				{
					GameService.gI().changeMap(103, 11, 9);
					AutoVanTieu.ngu(1000L);
					GameService.gI().changeMap(0, 34, 46);
				}
				else
				{
					GameService.gI().changeMap(73, 4, 4);
					AutoVanTieu.ngu(1000L);
					GameService.gI().changeMap(70, 31, 50);
				}
				AutoVanTieu.ngu(1000L);
				AutoVanTieu.giu(8, 500L);
				AutoVanTieu.giu(4, 1000L);
				AutoVanTieu.giu(8, 300L);
				chayVong = false;
				AutoVanTieu.batDoi();
				return;
			}
		}
	}

	/** Giữ một phím mũi tên ảo trong l ms. */
	private static void giu(int n, long l)
	{
		Canvas.keyHold[n] = true;
		dangGiu = true;
		AutoVanTieu.ngu(l);
		AutoVanTieu.nha();
	}

	// ------------------------------------------------------------------ Đổi tiêu

	private static void batDoi()
	{
		chayDoi = true;
		AutoVanTieu.capNhat();
		new System.Threading.Thread(() =>
		{
				try
				{
					AutoVanTieu.doi();
				}
				catch (System.Exception)
				{
					// empty catch block
				}
				AutoVanTieu.capNhat();
			}) { IsBackground = true }.Start();
	}

	/** knqO.run: nhận tiêu một lần rồi dùng Lệnh bài đổi tiêu cao cấp mỗi 250ms (hết thì mua). */
	private static void doi()
	{
		trangThai = "nhận tiêu";
		idTieu = -1;
		GameService.gI().doMenuOption(NPC_NHAN, (sbyte)0, 0);
		while (chayDoi)
		{
			MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
			if (mainChar != null)
			{
				trangThai = new System.Text.StringBuilder().Append("đổi tiêu (chờ ").Append(MAU[mauIndex]).Append(")").ToString();
				if (mainChar.potions[LENH_CAO_CAP] <= 0 && mainChar.luong >= 100)
				{
					GameService.gI().buyItemShopSpecial(SHOP_LENH_CAO_CAP);
				}
				if (mainChar.potions[LENH_CAO_CAP] > 0)
				{
					Canvas.gameScr.doUsePotion(LENH_CAO_CAP);
				}
			}
			AutoVanTieu.ngu(250L);
		}
	}

	// ------------------------------------------------------------------ Đi tiêu

	private static void batDi()
	{
		chayDi = true;
		AutoVanTieu.capNhat();
		new System.Threading.Thread(() =>
		{
				try
				{
					AutoVanTieu.luongDiTieu();
				}
				catch (System.Exception)
				{
					// empty catch block
				}
				chayDi = false;
				AutoVanTieu.capNhat();
			}) { IsBackground = true }.Start();
	}

	private static bool chay()
	{
		return chayDi && Canvas.gameScr != null && Canvas.gameScr.mainChar != null;
	}

	private static bool song()
	{
		return AutoVanTieu.chay() && Canvas.gameScr.mainChar.hp > 0;
	}

	private static int tx()
	{
		return Canvas.gameScr.mainChar.x / 16;
	}

	private static int ty()
	{
		return Canvas.gameScr.mainChar.y / 16;
	}

	private static void ngu(long l)
	{
		try
		{
			System.Threading.Thread.Sleep((int)l);
		}
		catch (System.Exception)
		{
			// empty catch block
		}
	}

	private static bool laTieu(Actor actor)
	{
		return actor is MonsterNew && ((MonsterNew)actor).isMonsterVantieu();
	}

	/** Nháy map: xin sang map 24 (server từ chối, nạp lại map đang đứng, kéo xe tiêu tới chỗ mình). */
	private static void nhay()
	{
		GameService.gI().changeMap(24, 3, 3);
	}

	/**
	 * Sang map n. Server này kick người xin sang map kề mà không đứng đúng cổng (MapXuLyLenh "hack chuyen
	 * map"), nên lấy toạ độ đến của đúng cổng đang đứng cạnh (dữ liệu cổng của client, như game tự qua
	 * cổng); không có thì dùng toạ độ ghi sẵn của bản đổi tiêu.
	 */
	private static void doiMap(int n, int n2, int n3)
	{
		GameScr gameScr = Canvas.gameScr;
		CMLocation cMLocation = AutoVanTieu.congGan(gameScr.mainChar, n);
		if (cMLocation != null)
		{
			short[] sArray = Res.getLocation(gameScr.getIndexArrMap(), cMLocation.toM);
			int n4 = PauseMenu.congDen(cMLocation, n);
			if (sArray != null && n4 >= 0)
			{
				n2 = sArray[n4 * 3 + 1];
				n3 = sArray[n4 * 3 + 2];
			}
		}
		GameScr.oldMap = gameScr.map;
		GameService.gI().changeMap(n, n2, n3);
	}

	/** Cổng dẫn tới map n gần nhân vật nhất; null nếu không có (làng có 2 cổng sang Tiên Du). */
	private static CMLocation congGan(MainChar mainChar, int n)
	{
		mVector vector = Tilemap.mapchangeLocations;
		if (vector == null || Tilemap.type == null)
		{
			return null;
		}
		CMLocation cMLocation = null;
		int n2 = int.MaxValue;
		int n3 = Tilemap.w;
		for (int i = 0; i < Tilemap.type.Length; ++i)
		{
			int n4 = Tilemap.type[i] - 2000000000;
			if (Tilemap.type[i] < 2000000000 || n4 >= vector.size()) continue;
			CMLocation cMLocation2 = (CMLocation)vector.elementAt(n4);
			if (PauseMenu.congDen(cMLocation2, n) < 0) continue;
			int n5 = Math.abs(i % n3 * 16 + 8 - mainChar.x) + Math.abs(i / n3 * 16 + 8 - mainChar.y);
			if (n5 >= n2) continue;
			n2 = n5;
			cMLocation = cMLocation2;
		}
		return cMLocation;
	}

	/** checkTieu: có xe tiêu (của mình nếu đã biết mã chủ) trong 15 ô mỗi trục. */
	private static bool checkTieu()
	{
		try
		{
			GameScr gameScr = Canvas.gameScr;
			MainChar mainChar = gameScr.mainChar;
			mVector vector = gameScr.actors;
			for (int i = vector.size() - 1; i >= 0; --i)
			{
				Actor actor = (Actor)vector.elementAt(i);
				if (!AutoVanTieu.laTieu(actor) || idTieu >= 0 && ((Monster)actor).idChar != idTieu) continue;
				if (Math.abs(actor.x / 16 - mainChar.x / 16) > 15 || Math.abs(actor.y / 16 - mainChar.y / 16) > 15) continue;
				if (idTieu < 0)
				{
					idTieu = ((Monster)actor).idChar;
				}
				return true;
			}
		}
		catch (System.Exception)
		{
			// danh sách actor đang bị luồng game sửa: coi như chưa thấy, lần sau xem lại
		}
		return false;
	}

	/** kiemTraTieu(x,y, lùi về bx,by): mất tiêu thì lùi dần tìm xe, thấy thì quay lại (x,y). */
	private static void kiemTraTieu(int n, int n2, int n3, int n4)
	{
		if (!kiemTra || !AutoVanTieu.song())
		{
			return;
		}
		AutoVanTieu.ngu(1000L);
		if (AutoVanTieu.checkTieu() || !AutoVanTieu.chay())
		{
			return;
		}
		AutoVanTieu.ngu(1000L);
		if (AutoVanTieu.checkTieu() || !AutoVanTieu.chay())
		{
			return;
		}
		trangThai = new System.Text.StringBuilder().Append("mất tiêu, dò ngược mốc ").Append(n3).Append(",").Append(n4).ToString();
		int n5 = n;
		int n6 = n2;
		if (n6 != n4)
		{
			int n7 = n4 > n6 ? 1 : -1;
			while (n6 != n4 && !AutoVanTieu.checkTieu() && AutoVanTieu.chay())
			{
				int n8 = n6 + 5 * n7;
				if (n7 == 1 && n8 > n4 || n7 == -1 && n8 < n4)
				{
					n8 = n4;
				}
				AutoVanTieu.doAtorMove(n5, n6, n5, n8);
				n6 = n8;
			}
		}
		if (n5 != n3 && !AutoVanTieu.checkTieu() && AutoVanTieu.chay())
		{
			int n9 = n3 > n5 ? 1 : -1;
			while (n5 != n3 && !AutoVanTieu.checkTieu() && AutoVanTieu.chay())
			{
				int n10 = n5 + 5 * n9;
				if (n9 == 1 && n10 > n3 || n9 == -1 && n10 < n3)
				{
					n10 = n3;
				}
				AutoVanTieu.doAtorMove(n5, n6, n10, n6);
				n5 = n10;
			}
		}
		if (!AutoVanTieu.checkTieu())
		{
			trangThai = "lùi hết vẫn chưa thấy tiêu, đứng chờ";
			while (!AutoVanTieu.checkTieu() && AutoVanTieu.chay())
			{
				AutoVanTieu.ngu(1000L);
			}
		}
		if (AutoVanTieu.chay())
		{
			trangThai = "đã thấy tiêu, đi tiếp";
			AutoVanTieu.doAtorMoveSafe(AutoVanTieu.tx(), AutoVanTieu.ty(), n, n2, 5);
		}
	}

	/** doAtorMove: giữ phím trục x trước rồi trục y tới ô (n3,n4), tối đa 10s; tới nơi thì nghỉ + nháy map. */
	private static void doAtorMove(int n, int n2, int n3, int n4)
	{
		long l = mSystem.currentTimeMillis() + 10000L;
		// Đường gốc có đoạn đi thẳng đâm vào vách (map 7: (47,57)->(81,60) vướng ô 50,57; (81,48)->(48,54);
		// (24,20)->(25,14)) - bản gốc đứng giữ phím 10s rồi bỏ điểm, dễ kẹt. Đoạn nào đi thẳng (trục x
		// trước rồi y) bị vách chắn thì đi vòng theo đường BFS tới đúng điểm đó.
		int[] nArray = null;
		int n7 = 0;
		if (AutoVanTieu.thangBiChan(AutoVanTieu.tx(), AutoVanTieu.ty(), n3, n4))
		{
			try
			{
				bool[] blArray = new bool[Tilemap.w * Tilemap.h];
				blArray[n4 * Tilemap.w + n3] = true;
				nArray = PauseMenu.timDuong(blArray);
				l += 10000L;
			}
			catch (System.Exception)
			{
				nArray = null;
			}
		}
		while (AutoVanTieu.song() && mSystem.currentTimeMillis() <= l)
		{
			int n5 = AutoVanTieu.tx();
			int n6 = AutoVanTieu.ty();
			if (nArray != null && n7 < nArray.Length - 1)
			{
				// đang đi vòng: tới từng góc rẽ của đường BFS (góc cuối chính là điểm đích)
				int n8 = nArray[n7] % Tilemap.w;
				int n9 = nArray[n7] / Tilemap.w;
				if (n5 == n8 && n6 == n9)
				{
					++n7;
					continue;
				}
				if (n5 < n8)
				{
					AutoVanTieu.giuPhim(1, 0);
				}
				else if (n5 > n8)
				{
					AutoVanTieu.giuPhim(-1, 0);
				}
				else if (n6 < n9)
				{
					AutoVanTieu.giuPhim(0, 1);
				}
				else
				{
					AutoVanTieu.giuPhim(0, -1);
				}
				AutoVanTieu.ngu(1L);
				continue;
			}
			bool bl = n5 == n3;
			if ((n3 == 120 || n3 == 125) && (n5 == 119 || n5 == 120))
			{
				bl = true;
				n5 = n3;
			}
			if (bl && n6 == n4)
			{
				AutoVanTieu.nha();
				if (Tilemap.lv == MAP_BIEN_GIOI)
				{
					AutoVanTieu.ngu(500L);
				}
				else if (Tilemap.lv == 5)
				{
					AutoVanTieu.ngu(300L);
					AutoVanTieu.nhay();
				}
				else if (Tilemap.lv == 6)
				{
					AutoVanTieu.ngu(500L);
					AutoVanTieu.nhay();
				}
				else
				{
					AutoVanTieu.ngu((long)tocNhay);
					AutoVanTieu.nhay();
				}
				return;
			}
			if (n5 < n3)
			{
				AutoVanTieu.giuPhim(1, 0);
			}
			else if (n5 > n3)
			{
				AutoVanTieu.giuPhim(-1, 0);
			}
			else if (n6 < n4)
			{
				AutoVanTieu.giuPhim(0, 1);
			}
			else if (n6 > n4)
			{
				AutoVanTieu.giuPhim(0, -1);
			}
			AutoVanTieu.ngu(1L);
		}
		AutoVanTieu.nha();
	}

	/** Đi thẳng trục x trước rồi trục y từ (n,n2) tới (n3,n4) có gặp ô vách / vùng chặn NPC không. */
	private static bool thangBiChan(int n, int n2, int n3, int n4)
	{
		try
		{
			int n5 = n;
			while (n5 != n3)
			{
				n5 += n3 > n5 ? 1 : -1;
				if (PauseMenu.laTuong(n5, n2))
				{
					return true;
				}
			}
			int n6 = n2;
			while (n6 != n4)
			{
				n6 += n4 > n6 ? 1 : -1;
				if (PauseMenu.laTuong(n3, n6))
				{
					return true;
				}
			}
		}
		catch (System.Exception)
		{
			// empty catch block
		}
		return false;
	}

	/** doAtorMoveSafe: đi trục x trước rồi trục y, mỗi lần n5 ô (mỗi lần một doAtorMove = một lần nháy). */
	private static void doAtorMoveSafe(int n, int n2, int n3, int n4, int n5)
	{
		int n6 = n;
		int n7 = n2;
		if (n != n3)
		{
			int n8 = n3 > n ? 1 : -1;
			while (n6 != n3)
			{
				if (!AutoVanTieu.song())
				{
					return;
				}
				int n9 = n6 + n5 * n8;
				if (n8 == 1 && n9 > n3 || n8 == -1 && n9 < n3)
				{
					n9 = n3;
				}
				AutoVanTieu.doAtorMove(n6, n7, n9, n7);
				n6 = n9;
			}
		}
		if (n2 != n4)
		{
			int n10 = n4 > n2 ? 1 : -1;
			while (n7 != n4)
			{
				if (!AutoVanTieu.song())
				{
					return;
				}
				int n11 = n7 + n5 * n10;
				if (n10 == 1 && n11 > n4 || n10 == -1 && n11 < n4)
				{
					n11 = n4;
				}
				AutoVanTieu.doAtorMove(n6, n7, n6, n11);
				n7 = n11;
			}
		}
	}

	/** doAtorMoveStep (Biên giới): như doAtorMoveSafe nhưng tính từ chỗ đang đứng, nghỉ 0.2s mỗi bước. */
	private static void doAtorMoveStep(int n, int n2, int n3, int n4, int n5)
	{
		int n6 = AutoVanTieu.tx();
		int n7 = AutoVanTieu.ty();
		if (n != n3)
		{
			int n8 = n3 > n ? 1 : -1;
			while (n6 != n3)
			{
				if (!AutoVanTieu.song())
				{
					return;
				}
				int n9 = n6 + n5 * n8;
				if (n8 == 1 && n9 > n3 || n8 == -1 && n9 < n3)
				{
					n9 = n3;
				}
				AutoVanTieu.ngu(200L);
				AutoVanTieu.doAtorMove(n6, n7, n9, n7);
				n6 = AutoVanTieu.tx();
			}
		}
		if (n2 != n4)
		{
			int n10 = n4 > n2 ? 1 : -1;
			while (n7 != n4)
			{
				if (!AutoVanTieu.song())
				{
					return;
				}
				int n11 = n7 + n5 * n10;
				if (n10 == 1 && n11 > n4 || n10 == -1 && n11 < n4)
				{
					n11 = n4;
				}
				AutoVanTieu.ngu(200L);
				AutoVanTieu.doAtorMove(n6, n7, n6, n11);
				n7 = AutoVanTieu.ty();
			}
		}
	}

	/** Cuối map: tới sát (n,n2) <= 2 ô rồi đổi sang map n3 (n4,n5); tối đa nLan lần thử. */
	private static void raCong(int n, int n2, int n3, int n4, int n5, int nLan)
	{
		int n6 = 0;
		while (AutoVanTieu.song())
		{
			int n7 = AutoVanTieu.tx();
			int n8 = AutoVanTieu.ty();
			if (Math.abs(n7 - n) <= 2 && Math.abs(n8 - n2) <= 2)
			{
				// kiemTraTieu coi "thấy tiêu" khi xe trong 15 ô, server chỉ cho xe theo qua cổng khi
				// cách <= 120px: nháy một lần kéo xe sát mình rồi mới qua (Biên giới / làng không nháy)
				if (Tilemap.lv != 0 && Tilemap.lv != 70)
				{
					AutoVanTieu.nhay();
					AutoVanTieu.ngu((long)tocNhay + 400L);
				}
				AutoVanTieu.doiMap(n3, n4, n5);
			}
			else if (++n6 <= nLan)
			{
				AutoVanTieu.doAtorMove(n7, n8, n, n2);
				AutoVanTieu.ngu(500L);
				continue;
			}
			AutoVanTieu.ngu(1000L);
			return;
		}
	}

	/** Chờ map đổi khác n (tối đa 10s) - bản gốc chạy tuần tự các khối map, ở đây chờ cho chắc. */
	private static void choDoiMap(int n)
	{
		long l = mSystem.currentTimeMillis() + 10000L;
		while (AutoVanTieu.chay() && Tilemap.lv == n && mSystem.currentTimeMillis() < l)
		{
			AutoVanTieu.ngu(100L);
		}
		AutoVanTieu.ngu(500L);
	}

	/** COMb.run: lần lượt làng -> 2 -> 3 -> 5 -> 6 -> 7 -> 8 -> 9 -> Biên giới 118 -> trả tiêu. */
	private static void luongDiTieu()
	{
		while (AutoVanTieu.chay())
		{
			int n = Tilemap.lv;
			trangThai = new System.Text.StringBuilder().Append("đi tiêu (map ").Append(Canvas.gameScr.map).Append(")").ToString();
			if (n == 0 || n == 70)
			{
				if (n == 70)
				{
					// auto nhận tiêu ở NPC giữa làng: ra cổng Tiên Du (22,57) trước - bản gốc bắt đầu từ cổng
					if (AutoVanTieu.ty() < 50)
					{
						AutoVanTieu.doAtorMove(AutoVanTieu.tx(), AutoVanTieu.ty(), 22, 51);
						continue;
					}
					AutoVanTieu.ngu(2000L);
					AutoVanTieu.raCong(22, 57, 2, 15, 1, 5);
					AutoVanTieu.choDoiMap(n);
					continue;
				}
				AutoVanTieu.ngu(2000L);
				AutoVanTieu.doiMap(2, 15, 1);
				AutoVanTieu.ngu(2000L);
				if (Tilemap.lv != 2)
				{
					AutoVanTieu.doiMap(2, 15, 1);
					AutoVanTieu.ngu(2000L);
				}
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 2)
			{
				AutoVanTieu.ngu(2000L);
				if (!AutoVanTieu.chay())
				{
					return;
				}
				AutoVanTieu.doAtorMove(15, 3, 15, 7);
				AutoVanTieu.doAtorMove(15, 7, 15, 10);
				AutoVanTieu.doAtorMove(15, 10, 15, 13);
				AutoVanTieu.doAtorMove(15, 13, 15, 15);
				AutoVanTieu.doAtorMove(15, 15, 20, 18);
				AutoVanTieu.doAtorMove(15, 18, 20, 18);
				int n2 = 20;
				for (int i = 25; n2 <= 100 && i <= 95; i += 5)
				{
					if (!AutoVanTieu.chay())
					{
						return;
					}
					AutoVanTieu.doAtorMove(n2, 18, i, 18);
					n2 += 5;
				}
				AutoVanTieu.kiemTraTieu(100, 18, 20, 18);
				if (!AutoVanTieu.chay())
				{
					return;
				}
				AutoVanTieu.doAtorMove(95, 18, 100, 18);
				AutoVanTieu.doAtorMove(100, 18, 100, 20);
				n2 = 100;
				for (int i = 105; n2 <= 120 && i <= 125; i += 4)
				{
					if (!AutoVanTieu.song())
					{
						return;
					}
					int n3 = AutoVanTieu.tx();
					if (n3 == 119 || n3 >= 120) break;
					AutoVanTieu.doAtorMove(n2, 20, i, 20);
					n2 += 4;
				}
				AutoVanTieu.kiemTraTieu(120, 20, 100, 18);
				AutoVanTieu.raCong(126, 20, 3, 1, 22, 5);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 3)
			{
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(3, 22, 52, 22, 4);
				AutoVanTieu.kiemTraTieu(52, 22, 3, 22);
				AutoVanTieu.doAtorMoveSafe(52, 22, 52, 64, 4);
				AutoVanTieu.kiemTraTieu(52, 64, 52, 22);
				AutoVanTieu.doAtorMoveSafe(52, 64, 83, 64, 5);
				AutoVanTieu.doAtorMoveSafe(83, 64, 83, 91, 5);
				AutoVanTieu.kiemTraTieu(83, 91, 83, 64);
				AutoVanTieu.doAtorMoveSafe(83, 91, 91, 91, 5);
				AutoVanTieu.doAtorMoveSafe(91, 91, 91, 94, 5);
				AutoVanTieu.kiemTraTieu(91, 94, 91, 91);
				AutoVanTieu.raCong(91, 94, 5, 71, 1, 3);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 5)
			{
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(71, 3, 71, 15, 5);
				AutoVanTieu.doAtorMoveSafe(71, 15, 59, 15, 5);
				AutoVanTieu.kiemTraTieu(59, 15, 71, 5);
				AutoVanTieu.doAtorMoveSafe(59, 15, 59, 55, 5);
				AutoVanTieu.doAtorMoveSafe(59, 55, 65, 75, 5);
				AutoVanTieu.kiemTraTieu(65, 75, 59, 15);
				AutoVanTieu.raCong(65, 75, 6, 40, 2, 3);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 6)
			{
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(40, 2, 38, 26, 5);
				AutoVanTieu.doAtorMoveSafe(38, 26, 37, 44, 5);
				AutoVanTieu.doAtorMoveSafe(37, 44, 45, 44, 5);
				AutoVanTieu.kiemTraTieu(45, 44, 37, 44);
				AutoVanTieu.raCong(45, 44, 7, 2, 91, 3);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 7)
			{
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(2, 91, 47, 81, 5);
				AutoVanTieu.doAtorMoveSafe(47, 81, 47, 57, 5);
				AutoVanTieu.kiemTraTieu(47, 57, 2, 91);
				AutoVanTieu.doAtorMove(47, 57, 81, 60);
				AutoVanTieu.doAtorMove(81, 60, 81, 48);
				AutoVanTieu.doAtorMove(81, 48, 48, 54);
				AutoVanTieu.doAtorMoveSafe(48, 54, 37, 54, 5);
				AutoVanTieu.doAtorMoveSafe(37, 54, 37, 47, 5);
				AutoVanTieu.doAtorMoveSafe(37, 47, 28, 47, 5);
				AutoVanTieu.doAtorMoveSafe(28, 47, 28, 39, 5);
				AutoVanTieu.doAtorMoveSafe(28, 39, 18, 39, 5);
				AutoVanTieu.kiemTraTieu(18, 39, 28, 39);
				AutoVanTieu.doAtorMoveSafe(18, 39, 17, 20, 5);
				AutoVanTieu.doAtorMoveSafe(17, 20, 24, 20, 5);
				// (bản gốc gửi thêm lệnh 79 xem giá xa phu map 5 rồi 7 - chỉ mở bảng giá, không đi; bỏ)
				AutoVanTieu.ngu(1500L);
				AutoVanTieu.nhay();
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(20, 14, 28, 1, 5);
				AutoVanTieu.kiemTraTieu(28, 1, 20, 14);
				AutoVanTieu.raCong(28, 1, 8, 27, 77, 3);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 8)
			{
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(27, 77, 27, 64, 4);
				AutoVanTieu.doAtorMoveSafe(27, 64, 38, 64, 4);
				AutoVanTieu.doAtorMoveSafe(38, 64, 41, 50, 4);
				AutoVanTieu.doAtorMoveSafe(41, 50, 41, 3, 4);
				AutoVanTieu.doAtorMoveSafe(41, 3, 47, 3, 4);
				AutoVanTieu.kiemTraTieu(47, 3, 41, 3);
				AutoVanTieu.raCong(47, 3, 9, 28, 98, 3);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == 9)
			{
				AutoVanTieu.ngu(500L);
				AutoVanTieu.doAtorMoveSafe(28, 98, 33, 3, 5);
				AutoVanTieu.kiemTraTieu(33, 3, 28, 98);
				AutoVanTieu.raCong(33, 2, 118, 10, 20, 3);
				AutoVanTieu.choDoiMap(n);
				continue;
			}
			if (n == MAP_BIEN_GIOI)
			{
				AutoVanTieu.doAtorMoveStep(10, 20, 14, 20, 3);
				AutoVanTieu.doAtorMoveStep(14, 20, 14, 10, 3);
				AutoVanTieu.kiemTraTieu(14, 10, 14, 20);
				AutoVanTieu.doAtorMoveStep(14, 10, 34, 13, 3);
				AutoVanTieu.kiemTraTieu(34, 13, 14, 10);
				if (!AutoVanTieu.chay())
				{
					return;
				}
				AutoVanTieu.doAtorMoveStep(34, 10, 34, 17, 3);
				AutoVanTieu.doAtorMoveStep(34, 17, 55, 17, 3);
				AutoVanTieu.kiemTraTieu(55, 17, 34, 17);
				if (!AutoVanTieu.chay())
				{
					return;
				}
				trangThai = "trả tiêu";
				GameService.gI().doMenuOption(NPC_TRA, (sbyte)0, 0);
				// Server này báo trả tiêu bằng "Chúc mừng bạn đã có 1 chuyến tiêu thành công" (thông báo
				// chạy chữ, không qua hộp thoại) + "Bạn nhận được ... điểm đổi thưởng" - không phải câu
				// "Chúc mừng bạn nhận được" bản gốc chờ. Nên tự chờ xe tiêu biến mất (= trả xong) rồi chạy
				// lại vòng nếu đang tự động.
				for (int i = 0; i < 30 && chayDi; ++i)
				{
					AutoVanTieu.ngu(500L);
					if (!AutoVanTieu.checkTieu())
					{
						AutoVanTieu.ngu(1000L);
						if (!AutoVanTieu.checkTieu())
						{
							++soTieu;
							trangThai = "trả tiêu xong";
							if (tuDong)
							{
								AutoVanTieu.batVong();
							}
							return;
						}
					}
				}
				return;
			}
			trangThai = new System.Text.StringBuilder().Append("map ").Append(Canvas.gameScr.map).Append(" không nằm trên đường đi tiêu").ToString();
			return;
		}
	}

	private static bool dangGiu;

	private static void giuPhim(int n, int n2)
	{
		AutoVanTieu.nha();
		if (Math.abs(n) >= Math.abs(n2))
		{
			Canvas.keyHold[n < 0 ? 4 : 6] = true;
		}
		else
		{
			Canvas.keyHold[n2 < 0 ? 2 : 8] = true;
		}
		dangGiu = true;
	}

	private static void nha()
	{
		if (!dangGiu)
		{
			return;
		}
		dangGiu = false;
		Canvas.keyHold[2] = false;
		Canvas.keyHold[4] = false;
		Canvas.keyHold[6] = false;
		Canvas.keyHold[8] = false;
	}

	// ------------------------------------------------------------------ thanh trạng thái

	public static void ve(MyGraphics graphics, int n)
	{
		if (!dang || MFont.normalFont == null || MFont.normalFont[0] == null)
		{
			return;
		}
		string @string = new System.Text.StringBuilder().Append("Tiêu ").Append(MAU[mauIndex]).Append(": đã đi ").Append(soTieu).Append(" | ").Append(trangThai).ToString();
		MFont font = MFont.normalFont[0];
		@string = TienIch.catVua(@string, font);
		int n2 = Math.min(Canvas.w, font.getWidth(@string) + 12);
		int n3 = font.getHeight() + 6;
		int n4 = (Canvas.w - n2) / 2;
		graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
		graphics.setClip(0, 0, Canvas.w, Canvas.h);
		graphics.setColor(0x2E1A0B);
		graphics.fillRect(n4, n, n2, n3);
		graphics.setColor(0xE08040);
		graphics.drawRect(n4, n, n2 - 1, n3 - 1);
		font.drawString(graphics, @string, Canvas.w / 2, n + 3, 2);
	}
}
