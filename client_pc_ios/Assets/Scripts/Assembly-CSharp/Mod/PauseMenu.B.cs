// Port 1:1 từ Jar276 PauseMenu.java: oMap .. autoPetExitHouse (điều hướng, relog, guard, menu, job menu/helper).
public partial class PauseMenu
{
	/** Đang ở map m (so theo nhóm map của client, vì một map lớn có nhiều map con). */
	public static bool oMap(int n)
	{
		GameScr gameScr = Canvas.gameScr;
		if (gameScr == null)
		{
			return false;
		}
		if (gameScr.map == n)
		{
			return true;
		}
		short s = gameScr.getIndexArrMap();
		return s >= 0 && s == gameScr.getIndexArrMap(n);
	}

	private static bool oTrongGame()
	{
		return Canvas.currentScreen == Canvas.gameScr && Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Tilemap.type != null && Tilemap.w > 0;
	}

	/**
	 * Ô không đi qua được - đúng luật va chạm của client ((type & 2) == 2). Ô cổng chuyển map
	 * (type >= 2000000000, xem Tilemap.loadMap) cũng không đi lên: tới sát cổng là đủ.
	 */
	public static bool laTuong(int n, int n2)
	{
		if (n < 0 || n2 < 0 || n >= Tilemap.w || n2 >= Tilemap.h)
		{
			return true;
		}
		int n3 = n2 * Tilemap.w + n;
		if (n3 >= Tilemap.type.Length)
		{
			return true;
		}
		int n4 = Tilemap.type[n3];
		if (n4 >= 2000000000 || (n4 & 2) == 2)
		{
			return true;
		}
		// vùng chặn của NPC (vd tượng lân sự kiện giữa làng): không có trong dữ liệu ô nhưng
		// GameScr.doActorMove vẫn chặn bước đi -> trước đây BFS đi xuyên qua rồi đứng kẹt
		return Canvas.gameScr != null && Canvas.gameScr.checkMoveLimit(n * 16 + 8, n2 * 16 + 8);
	}

	/**
	 * BFS 4 hướng trên toàn bản đồ từ chỗ đứng tới ô đích bất kỳ, rồi chỉ giữ các GÓC đổi hướng
	 * và ô cuối - đi bằng phím mũi tên nên mỗi đoạn là một đường thẳng ngang/dọc. null = không có đường.
	 */
	public static int[] timDuong(bool[] blArray)
	{
		int n = Tilemap.w;
		int n2 = Tilemap.h;
		int n3 = Canvas.gameScr.mainChar.x / 16;
		int n4 = Canvas.gameScr.mainChar.y / 16;
		if (n3 < 0 || n4 < 0 || n3 >= n || n4 >= n2)
		{
			return null;
		}
		int[] nArray = new int[n * n2];
		for (int i = 0; i < nArray.Length; ++i)
		{
			nArray[i] = -2;
		}
		int[] nArray2 = new int[n * n2];
		int n5 = 0;
		int n6 = 0;
		int n7 = n4 * n + n3;
		nArray[n7] = -1;
		nArray2[n6++] = n7;
		int n8 = -1;
		int[] nArray3 = new int[] { 1, -1, 0, 0 };
		int[] nArray4 = new int[] { 0, 0, 1, -1 };
		while (n5 < n6)
		{
			int n9 = nArray2[n5++];
			if (blArray[n9])
			{
				n8 = n9;
				break;
			}
			int n10 = n9 % n;
			int n11 = n9 / n;
			for (int i = 0; i < 4; ++i)
			{
				int n12 = n10 + nArray3[i];
				int n13 = n11 + nArray4[i];
				if (PauseMenu.laTuong(n12, n13)) continue;
				int n14 = n13 * n + n12;
				if (nArray[n14] != -2) continue;
				nArray[n14] = n9;
				nArray2[n6++] = n14;
			}
		}
		if (n8 < 0)
		{
			return null;
		}
		int n15 = 0;
		for (int i = n8; i != -1; i = nArray[i])
		{
			++n15;
		}
		int[] nArray5 = new int[n15];
		for (int i = n8; i != -1; i = nArray[i])
		{
			nArray5[--n15] = i;
		}
		mVector vector = new mVector();
		for (int i = 1; i < nArray5.Length; ++i)
		{
			if (i < nArray5.Length - 1 && nArray5[i] - nArray5[i - 1] == nArray5[i + 1] - nArray5[i]) continue;
			vector.addElement(nArray5[i]);
		}
		int[] nArray6 = new int[vector.size()];
		for (int i = 0; i < nArray6.Length; ++i)
		{
			nArray6[i] = (int)vector.elementAt(i);
		}
		return nArray6;
	}

	/** Đích là hình chữ nhật ô (x1,y1)-(x2,y2). */
	private void datDich(int n, int n2, int n3, int n4)
	{
		bool[] blArray = new bool[Tilemap.w * Tilemap.h];
		for (int i = Math.max(0, n2); i <= n4 && i < Tilemap.h; ++i)
		{
			for (int j = Math.max(0, n); j <= n3 && j < Tilemap.w; ++j)
			{
				blArray[i * Tilemap.w + j] = !PauseMenu.laTuong(j, i);
			}
		}
		this.datDich(blArray);
	}

	private void datDich(bool[] blArray)
	{
		this.autoDich = blArray;
		this.autoDiPhim = false;
		this.autoDiBiKeo = 0;
		this.autoCoKeo = false;
		this.autoDuong = null;
		this.autoDuongPos = 0;
		this.autoDiDung = 0;
		this.autoDiTinhLai = 0;
	}

	/**
	 * Cổng (lối ra) này có đưa tới map m không: trả vị trí điểm đến m trong bảng Res.getLocation,
	 * -1 nếu không. CHÚ Ý cMLocation.toM KHÔNG phải id map đích mà là SỐ THỨ TỰ LỐI RA của
	 * bản đồ (0, 1, 2...); map đích thật nằm trong Res.getLocation(nhóm map, toM) - mỗi điểm
	 * đến 3 số (map, x, y). Vd map 9: lối ra 1 (mép trên) -> {201, 39, 90, ...} = Thành.
	 */
	public static int congDen(CMLocation cMLocation, int n)
	{
		short[] sArray = Res.getLocation(Canvas.gameScr.getIndexArrMap(), cMLocation.toM);
		for (int i = 0; sArray != null && i * 3 < sArray.Length; ++i)
		{
			if (sArray[i * 3] != n) continue;
			return i;
		}
		return -1;
	}

	/** Cổng dẫn tới map m trên bản đồ hiện tại (lấy từ dữ liệu bản đồ của client). */
	public static CMLocation congToi(int n)
	{
		mVector vector = Tilemap.mapchangeLocations;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			CMLocation cMLocation = (CMLocation)vector.elementAt(i);
			if (PauseMenu.congDen(cMLocation, n) < 0) continue;
			return cMLocation;
		}
		return null;
	}

	/** Ô trống sát (8 hướng) các ô cổng dẫn tới map m; null nếu bản đồ không có cổng đó. */
	public static bool[] dichSatCong(int n)
	{
		mVector vector = Tilemap.mapchangeLocations;
		if (vector == null)
		{
			return null;
		}
		int n2 = Tilemap.w;
		bool[] blArray = new bool[n2 * Tilemap.h];
		bool bl = false;
		for (int i = 0; i < Tilemap.type.Length && i < blArray.Length; ++i)
		{
			int n3 = Tilemap.type[i] - 2000000000;
			if (Tilemap.type[i] < 2000000000 || n3 >= vector.size()) continue;
			CMLocation cMLocation = (CMLocation)vector.elementAt(n3);
			if (PauseMenu.congDen(cMLocation, n) < 0) continue;
			int n4 = i % n2;
			int n5 = i / n2;
			for (int j = -1; j <= 1; ++j)
			{
				for (int k = -1; k <= 1; ++k)
				{
					if (PauseMenu.laTuong(n4 + k, n5 + j)) continue;
					blArray[(n5 + j) * n2 + n4 + k] = true;
					bl = true;
				}
			}
		}
		return bl ? blArray : null;
	}

	/** Qua cổng tới map m như client tự làm (GameScr.checkCanChangeMap) - chọn đúng điểm đến m. */
	public static void quaCong(CMLocation cMLocation, int n)
	{
		GameScr gameScr = Canvas.gameScr;
		// tới sát cổng game có thể tự bật menu chọn map - đóng đi, auto tự chọn đúng điểm đến
		Canvas.menu.showMenu = false;
		ChangScr.gI().setMap(gameScr.map, cMLocation.toX, cMLocation.toY);
		short[] sArray = Res.getLocation(gameScr.getIndexArrMap(), cMLocation.toM);
		int n2 = PauseMenu.congDen(cMLocation, n);
		GameScr.oldMap = gameScr.map;
		GameService.gI().changeMap(sArray[n2 * 3], sArray[n2 * 3 + 1], sArray[n2 * 3 + 2]);
		ChangScr.gI().switchToMe();
		Res.removeMonsterTemplet();
	}

	/** Nhả hết phím mũi tên ảo mà diBo() đang giữ. */
	private static void nhaPhim()
	{
		Canvas.keyHold[2] = false;
		Canvas.keyHold[4] = false;
		Canvas.keyHold[6] = false;
		Canvas.keyHold[8] = false;
	}

	/**
	 * Đi theo đường BFS bằng cách GIỮ PHÍM MŨI TÊN ẢO (Canvas.keyHold) - y như người chơi tự
	 * bấm: GameScr.doActorMove lo bước đi, trượt góc tường và gửi vị trí lên server. (Gọi thẳng
	 * mainChar.moveTo không làm nhân vật chính đi.) 1 = đã tới đích, 0 = đang đi, -1 = không đi được.
	 */
	private int diBo()
	{
		MainChar mainChar = Canvas.gameScr.mainChar;
		int n = mainChar.x / 16;
		int n2 = mainChar.y / 16;
		int n3 = n2 * Tilemap.w + n;
		if (this.autoDich == null)
		{
			PauseMenu.nhaPhim();
			return -1;
		}
		if (n >= 0 && n2 >= 0 && n < Tilemap.w && n3 < this.autoDich.Length && this.autoDich[n3])
		{
			PauseMenu.nhaPhim();
			return 1;
		}
		if (this.autoDuong == null)
		{
			PauseMenu.nhaPhim();
			if (++this.autoDiTinhLai > 5)
			{
				return -1;
			}
			this.autoDuong = PauseMenu.timDuong(this.autoDich);
			this.autoDuongPos = 0;
			this.autoDiDung = 0;
			if (this.autoDuong == null)
			{
				return -1;
			}
		}
		int n4 = this.autoDuong[this.autoDuongPos];
		int n5 = n4 % Tilemap.w * 16 + 8 - mainChar.x;
		int n6 = n4 / Tilemap.w * 16 + 8 - mainChar.y;
		int n7 = this.autoDiPhim ? Math.max(3, (int)mainChar.speed) : 2;
		if (Math.abs(n5) <= n7 && Math.abs(n6) <= n7)
		{
			if (++this.autoDuongPos >= this.autoDuong.Length)
			{
				this.autoDuong = null;
			}
			return 0;
		}
		if (!this.autoDiPhim)
		{
			return this.luot(mainChar, n5, n6);
		}
		PauseMenu.nhaPhim();
		if (Math.abs(n5) > n7)
		{
			Canvas.keyHold[n5 < 0 ? 4 : 6] = true;
		}
		else
		{
			Canvas.keyHold[n6 < 0 ? 2 : 8] = true;
		}
		// kẹt (vướng người/vật, bị server kéo lại): đứng yên ~60 tick thì tìm đường lại
		if (mainChar.x == this.autoDiLastX && mainChar.y == this.autoDiLastY)
		{
			if (++this.autoDiDung >= 60)
			{
				this.autoDuong = null;
			}
		}
		else
		{
			this.autoDiDung = 0;
			this.autoDiLastX = mainChar.x;
			this.autoDiLastY = mainChar.y;
		}
		return 0;
	}

	/**
	 * Tài khoản/mật khẩu để đăng nhập lại: lần gửi lệnh đăng nhập gần nhất (AutoGuard.loginGuard)
	 * -> ô nhập màn hình đăng nhập -> mật khẩu game đã lưu (RMS "nqshlogin", xem LoginScr.loadPass).
	 */
	private static string[] layTaiKhoan()
	{
		if (AutoGuard.tkDangNhap != null && AutoGuard.mkDangNhap != null)
		{
			return new string[] { AutoGuard.tkDangNhap, AutoGuard.mkDangNhap };
		}
		if (Canvas.loginScr != null && Canvas.loginScr.tfUser.getText().Trim().Length > 0 && Canvas.loginScr.tfPass.getText().Length > 0)
		{
			return new string[] { Canvas.loginScr.tfUser.getText().ToLower().Trim(), Canvas.loginScr.tfPass.getText() };
		}
		try
		{
			sbyte[] byArray = RMS.loadRMS("nqshlogin");
			if (byArray != null)
			{
				DataInputStream dataInputStream = new DataInputStream(byArray);
				if (dataInputStream.readBoolean())
				{
					string text = dataInputStream.readUTF();
					string text2 = dataInputStream.readUTF();
					dataInputStream.close();
					return new string[] { text.ToLower().Trim(), text2 };
				}
				dataInputStream.close();
			}
		}
		catch (System.Exception)
		{
			// không đọc được mật khẩu đã lưu
		}
		return new string[] { "", "" };
	}

	/**
	 * Chèn (qua AutoGuard) ở ĐẦU GameScr.onActorMove: server từ chối bước đi thì gửi lại lệnh 4
	 * với vị trí THẬT của chính mình. Client gốc chỉ đổi điểm đến chứ không đặt lại tọa độ, nên
	 * lướt mà bị từ chối là client tưởng đã tới còn server giữ chỗ cũ (NPC không trả lời).
	 */
	public static void viTriServer(sbyte by, short s, short s2, short s3)
	{
		if (me == null || !me.autoRunning || me.autoJob != 11 || by != 0 || Canvas.gameScr == null || Canvas.gameScr.mainChar == null || s != Canvas.gameScr.mainChar.ID)
		{
			return;
		}
		me.autoKeoX = s2;
		me.autoKeoY = s3;
		me.autoCoKeo = true;
	}

	/**
	 * Lướt 1 bước tới góc kế tiếp: tối đa 48px mỗi trục (server cho <= 50px/bước, đường thẳng
	 * không xuyên tường), cách nhau 1000/tốc độ ms (server: 200ms*5/speed giữa 2 bước).
	 * Bị server từ chối thì đặt ngay về vị trí server rồi tìm đường lại; 3 lần thì giữ phím.
	 */
	private int luot(MainChar mainChar, int n, int n2)
	{
		PauseMenu.nhaPhim();
		if (this.autoCoKeo)
		{
			this.autoCoKeo = false;
			short s = (short)this.autoKeoX;
			short s2 = (short)this.autoKeoY;
			mainChar.x = s;
			mainChar.y = s2;
			mainChar.xTo = s;
			mainChar.yTo = s2;
			mainChar.xBegin = s;
			mainChar.yBegin = s2;
			this.autoDuong = null;
			if (++this.autoDiBiKeo >= 3)
			{
				this.autoDiPhim = true;
				this.autoDiTinhLai = 0;
			}
			this.autoDiLuc = mSystem.currentTimeMillis() + 300L;
			return 0;
		}
		long l = mSystem.currentTimeMillis();
		if (l < this.autoDiLuc)
		{
			return 0;
		}
		int n3 = Math.max(-48, Math.min(48, n));
		int n4 = Math.max(-48, Math.min(48, n2));
		short s3 = (short)(mainChar.x + n3);
		short s4 = (short)(mainChar.y + n4);
		mainChar.x = s3;
		mainChar.y = s4;
		mainChar.xTo = s3;
		mainChar.yTo = s4;
		mainChar.xBegin = s3;
		mainChar.yBegin = s4;
		GameService.gI().moveChar(s3, s4);
		this.autoDiLuc = l + 1000L / (long)Math.max(1, (int)mainChar.speed) + 40L;
		return 0;
	}

	/** Thoát game rồi đăng nhập lại đúng nhân vật này (chạy trên luồng riêng). */
	private void batDauRelog()
	{
		string[] stringArray = PauseMenu.layTaiKhoan();
		string text = stringArray[0];
		string text2 = stringArray[1];
		string text3 = Canvas.gameScr.mainChar.name;
		if (text.Length == 0 || text2.Length == 0 || text3 == null)
		{
			this.autoStop("Auto nuôi pet: không lấy được tài khoản/mật khẩu để đăng nhập lại (hãy thoát game, đăng nhập lại rồi bật auto), đã dừng.");
			return;
		}
		++this.autoPetRelog;
		this.autoRelogLoi = null;
		this.autoPhase = 151;
		CharSelectScr.me = null;
		// ngắt kết nối đưa về màn chọn server, ServerListScr.init() đặt index = 0 (server ĐẦU
		// danh sách) -> phải nhớ server đang chơi để kết nối lại đúng chỗ
		int n = ServerListScr.index;
		Canvas.cmdNgatKetNoi.action();
		ServerListScr.index = n;
		new System.Threading.Thread(() =>
		{
			string text4 = PauseMenu.relog(text, text2, text3, n);
			if (text4 != null)
			{
				// không vào được game nên vòng auto không chạy: tự dừng và báo ở đây
				this.autoRelogLoi = text4;
				this.autoRunning = false;
				AutoGuard.on = false;
				// C#: hộp thoại phải tạo ở luồng chính Unity
				LuongChinh.chay(() => Canvas.startOKDlg("Auto nuôi pet: " + text4 + ", đã dừng."));
			}
		}).Start();
	}

	/** null = đã vào game; còn lại là lý do lỗi. */
	private static string relog(string text, string text2, string text3, int n0)
	{
		try
		{
			// chờ server gỡ phiên cũ, đăng nhập sớm quá server đá cả hai phiên
			System.Threading.Thread.Sleep(10000);
			for (int i = 0; i < 2; ++i)
			{
				// C#: kết nối + đăng nhập ở luồng chính Unity; thread này chỉ chờ kết quả như bản jar
				LuongChinh.chay(() =>
				{
					ServerListScr.index = n0;
					Canvas.connect();
					GameService.gI().setClientType();
					GameService.gI().login(text, text2, GameMidlet.version);
				});
				long l = mSystem.currentTimeMillis() + 20000L;
				while (mSystem.currentTimeMillis() < l && (CharSelectScr.me == null || CharSelectScr.me.charInfos == null || CharSelectScr.me.cmdSelect == null))
				{
					System.Threading.Thread.Sleep(200);
				}
				if (CharSelectScr.me != null && CharSelectScr.me.charInfos != null && CharSelectScr.me.cmdSelect != null) break;
				LuongChinh.chay(() => Session_ME.gI().close());
				System.Threading.Thread.Sleep(8000);
			}
			CharSelectScr charSelectScr = CharSelectScr.me;
			if (charSelectScr == null || charSelectScr.charInfos == null || charSelectScr.cmdSelect == null)
			{
				return "không đăng nhập lại được";
			}
			int n = -1;
			for (int i = 0; i < charSelectScr.charInfos.Length; ++i)
			{
				if (charSelectScr.charInfos[i] == null || !(text3 == charSelectScr.charInfos[i].name)) continue;
				n = i;
			}
			if (n < 0)
			{
				return "không thấy nhân vật " + text3;
			}
			LuongChinh.chay(() =>
			{
				charSelectScr.selected = n;
				charSelectScr.cmdSelect.action();
			});
			long l2 = mSystem.currentTimeMillis() + 30000L;
			while (mSystem.currentTimeMillis() < l2 && !PauseMenu.oTrongGame())
			{
				System.Threading.Thread.Sleep(300);
			}
			return PauseMenu.oTrongGame() ? null : "vào game quá lâu";
		}
		catch (System.Exception)
		{
			return "lỗi đăng nhập lại";
		}
	}

	/**
	 * Chèn ở ĐẦU GameScr.showWindow: lúc nuôi pet không mở cửa sổ chế đồ (tab 28) - dữ liệu
	 * nguyên liệu onCheDo đã nạp xong trước khi gọi showWindow, auto chế thẳng, đỡ hoạt ảnh.
	 */
	public static bool anCuaSoCheDo(int n, bool bl, sbyte[] byArray)
	{
		return me != null && me.autoRunning && (me.autoJob == 11 || me.autoJob == 14) && byArray != null && byArray.Length == 1 && byArray[0] == 28;
	}

	/** NPC -16 trả danh sách "Cho pet ăn" (idMenu 1): giữ lại để chọn, không hiện menu. */
	public static bool petMenuGuard(int n, sbyte by, string[] stringArray)
	{
		if (me != null && me.autoRunning && me.autoJob == 13 && n == -513)
		{
			return true;
		}
		if (me == null || !me.autoRunning || me.autoJob != 11 || n != -16 || by != 1)
		{
			return false;
		}
		me.autoPetDsAn = stringArray;
		++me.autoPetDsNhan;
		return true;
	}

	/** Mở lại menu cài đặt mà vẫn đứng ở dòng vừa bấm (Menu.startAt luôn về dòng đầu). */
	/** Menu mod mở gần nhất - các auto không tự đóng menu này (vd AutoVanTieu đóng menu cổng). */
	public static mVector menuMod;

	public static void moMenuGiuChon(mVector vector, int n, int n2)
	{
		menuMod = vector;
		// mọi popup của mod mở ở vị trí 3 (góc dưới phải) như menu xa phu của game
		Canvas.menu.startAt(vector, 3);
		if (n2 <= 0 || n2 >= vector.size())
		{
			return;
		}
		Menu menu = Canvas.menu;
		menu.selected = n2;
		int n3 = n2 * MyScreen.ITEM_HEIGHT - MyScreen.ITEM_HEIGHT * 2;
		if (n3 > menu.cylim)
		{
			n3 = menu.cylim;
		}
		if (n3 < 0)
		{
			n3 = 0;
		}
		menu.ctoyBag = n3;
		menu.cyBag = n3;
	}

	/**
	 * Menu mod dạng popup - cùng mục và hành động với hàng mod (index 0) của doSelect. Bản jar mở hàng
	 * đó trong màn PauseMenu; Android / PC (menu thật là MenuWindows) mở popup này từ tab "Khác".
	 */
	public static void moMenuMod()
	{
		PauseMenu pauseMenu = PauseMenu.gI();
		mVector vector = new mVector();
		vector.addElement(new Command("Xa phu")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				TienIch.moXaPhu();
			}
		});
		vector.addElement(new Command("Chế đồ nhanh")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				pauseMenu.showCheDoNhanhMenu();
			}
		});
		vector.addElement(new Command("Dòng ẩn")
		{
			action = () =>
			{
				pauseMenu.moMenuDongAn(0);
			}
		});
		vector.addElement(new Command("Auto up")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				AutoUp.moMenu(0);
			}
		});
		vector.addElement(new Command("Vận tiêu")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				AutoVanTieu.moMenu(0);
			}
		});
		vector.addElement(new Command("PK")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				AutoPk.moMenu(0);
			}
		});
		vector.addElement(new Command("Vận may")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				AutoVanMay.moMenu(0);
			}
		});
		vector.addElement(new Command("Tiện ích")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				TienIch.moMenu(0);
			}
		});
		PauseMenu.moMenuGiuChon(vector, 3, 0);
	}

	/** "Dòng ẩn" dạng popup bật/tắt (bản jar: hàng index 9 của màn PauseMenu). */
	private void moMenuDongAn(int chon)
	{
		mVector vector = new mVector();
		for (int i = 0; i < this.dongAnChon.Length; ++i)
		{
			int n = i;
			vector.addElement(new Command((this.dongAnChon[i] ? "(x) " : "( ) ") + this.dongAnLabel(i))
			{
				action = () =>
				{
					this.dongAnChon[n] = !this.dongAnChon[n];
					// bản C# không có hàng this.name[9] (màn PauseMenu J2ME) - bỏ cập nhật nhãn hàng đó
					this.moMenuDongAn(n);
				}
			});
		}
		PauseMenu.moMenuGiuChon(vector, 3, chon);
	}

	private void showHocSkillMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Skill slot 1: " + SK_TEN[this.autoSkMuon])
		{
			action = () =>
			{
				this.autoSkMuon = (this.autoSkMuon + 1) % SK_TEN.Length;
				if (this.autoSkMuon == this.autoSkMuon2)
				{
					this.autoSkMuon2 = (this.autoSkMuon2 + 1) % SK_TEN.Length;
				}
				this.showHocSkillMenu(0);
			}
		});
		vector.addElement(new Command(this.autoSkSlot2 ? "Học slot 2: Bật" : "Học slot 2: Tắt")
		{
			action = () =>
			{
				this.autoSkSlot2 = !this.autoSkSlot2;
				this.showHocSkillMenu(1);
			}
		});
		int n = 2;
		if (this.autoSkSlot2)
		{
			vector.addElement(new Command("Skill slot 2: " + SK_TEN[this.autoSkMuon2])
			{
				action = () =>
				{
					// 2 slot không được trùng skill (server không cho)
					do
					{
						this.autoSkMuon2 = (this.autoSkMuon2 + 1) % SK_TEN.Length;
					}
					while (this.autoSkMuon2 == this.autoSkMuon);
					this.showHocSkillMenu(2);
				}
			});
			n = 3;
		}
		int n2 = n;
		vector.addElement(new Command("Học khi phẩm chất từ: " + SK_PHAM_CHAT[this.autoSkPhamChat])
		{
			action = () =>
			{
				this.autoSkPhamChat = this.autoSkPhamChat >= 3 ? 1 : this.autoSkPhamChat + 1;
				this.showHocSkillMenu(n2);
			}
		});
		vector.addElement(new Command(this.autoSkMua ? "Tự mua Huyết Bồ Đề (10 lượng): Bật" : "Tự mua Huyết Bồ Đề: Tắt")
		{
			action = () =>
			{
				this.autoSkMua = !this.autoSkMua;
				this.showHocSkillMenu(n2 + 1);
			}
		});
		vector.addElement(new Command("Bắt đầu học skill (sách " + PauseMenu.skSach() + ", Huyết Bồ Đề " + PauseMenu.skHbdCo() + ")")
		{
			action = () =>
			{
				this.autoStartHocSkill();
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	/** Tên mục tiêu: "Tiên khí" hoặc "Tiên khí + Phá triền". */
	private string skMucTieu()
	{
		return this.autoSkSlot2 ? SK_TEN[this.autoSkMuon] + " + " + SK_TEN[this.autoSkMuon2] : SK_TEN[this.autoSkMuon];
	}

	/**
	 * Đánh giá skill pet đang có (theo thứ tự slot 1, slot 2):
	 * 1 = đã đạt mục tiêu, 0 = còn có thể học tiếp ở slot trống, -1 = có skill sai -> phải xoá.
	 * Học slot 2: Tắt - chỉ xét slot 1. Bật - cần đủ cặp; slot 1 trúng skill nào trong cặp cũng
	 * được giữ (học slot 2 tìm skill còn lại), kết quả cuối vẫn đúng cặp đã chọn.
	 */
	private int skDanhGia(mVector vector)
	{
		if (!this.autoSkSlot2)
		{
			if (vector.size() == 0)
			{
				return 0;
			}
			return (int)vector.elementAt(0) == this.autoSkMuon ? 1 : -1;
		}
		bool bl = false;
		bool bl2 = false;
		for (int i = 0; i < vector.size(); ++i)
		{
			int n = (int)vector.elementAt(i);
			if (n == this.autoSkMuon)
			{
				bl = true;
				continue;
			}
			if (n == this.autoSkMuon2)
			{
				bl2 = true;
				continue;
			}
			return -1;
		}
		if (bl && bl2)
		{
			return 1;
		}
		return 0;
	}

	private static int skSach()
	{
		MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
		return mainChar == null || mainChar.potions == null || mainChar.potions.Length <= POTION_SACH_SKILL ? 0 : mainChar.potions[POTION_SACH_SKILL];
	}

	private static ItemInInventory skHbd()
	{
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			if (itemInInventory.idTemplate != ITEM_HUYET_BO_DE || itemInInventory.isSelling) continue;
			return itemInInventory;
		}
		return null;
	}

	private static int skHbdCo()
	{
		int n = 0;
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			if (((ItemInInventory)vector.elementAt(i)).idTemplate != ITEM_HUYET_BO_DE) continue;
			++n;
		}
		return n;
	}

	/** Phẩm chất pet (0..3) từ dòng "- Phẩm chất: ...", -1 = chưa có. */
	private static int skPhamChat()
	{
		string text = PauseMenu.petInfo();
		int n = text == null ? -1 : text.IndexOf("Phẩm chất:", System.StringComparison.Ordinal);
		if (n < 0)
		{
			return -1;
		}
		string text2 = text.Substring(n + "Phẩm chất:".Length).Trim();
		for (int i = SK_PHAM_CHAT.Length - 1; i >= 0; --i)
		{
			if (!text2.StartsWith(SK_PHAM_CHAT[i], System.StringComparison.Ordinal)) continue;
			return i;
		}
		return -1;
	}

	/** Các dòng "- Kỹ năng: <tên>..." (bỏ dòng "Kỹ năng có thể học"). */
	private static mVector skDangCo()
	{
		mVector vector = new mVector();
		string text = PauseMenu.petInfo();
		int n = 0;
		while (text != null && (n = text.IndexOf("- Kỹ năng: ", n, System.StringComparison.Ordinal)) >= 0)
		{
			n += "- Kỹ năng: ".Length;
			for (int i = 0; i < SK_TEN.Length; ++i)
			{
				// Java: string.startsWith(SK_TEN[i], n)
				if (text.Length - n < SK_TEN[i].Length || string.CompareOrdinal(text, n, SK_TEN[i], 0, SK_TEN[i].Length) != 0) continue;
				vector.addElement(i);
				break;
			}
		}
		return vector;
	}

	/** Trạng thái tiến hoá "- Trạng thái: x/3" (1..3). */
	private static int skTrangThai()
	{
		return PauseMenu.petDocSo(PauseMenu.petInfo(), "Trạng thái:");
	}

	private void autoStartHocSkill()
	{
		if (PauseMenu.petInfo() == null || PauseMenu.petCap() < 0)
		{
			Canvas.startOKDlg("Bạn phải mang theo pet.");
			return;
		}
		if (PauseMenu.skTrangThai() < 2)
		{
			Canvas.startOKDlg("Pet phải tiến hoá lên trạng thái 2 mới học được kỹ năng.");
			return;
		}
		if (this.skDanhGia(PauseMenu.skDangCo()) == 1)
		{
			Canvas.startOKDlg("Pet đã có sẵn kỹ năng " + this.skMucTieu() + ".");
			return;
		}
		this.autoSkSach = 0;
		this.autoSkHbd = 0;
		this.autoSkDaMua = 0;
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 12;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoPhase = 170;
		this.autoWaitTicks = 0;
	}

	private void showHklMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command(this.autoHklMua ? "Tự mua Hỏa thạch cấp 1 (10 lượng): Bật" : "Tự mua Hỏa thạch cấp 1: Tắt")
		{
			action = () =>
			{
				this.autoHklMua = !this.autoHklMua;
				this.showHklMenu(0);
			}
		});
		vector.addElement(new Command("Bắt đầu nâng cấp (Hỏa thạch cấp 1: " + PauseMenu.hklDa(HOA_THACH_C1) + ", cấp 2: " + PauseMenu.hklDa(HOA_THACH_C2) + ")")
		{
			action = () =>
			{
				this.autoStartHkl();
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	private static int hklDa(int n)
	{
		return PauseMenu.autoGemCountLock(n, false) + PauseMenu.autoGemCountLock(n, true);
	}

	/** Hỏa kỳ lân trong hành trang (681..683), ưu tiên món chưa tối đa. */
	private static ItemInInventory hklMon()
	{
		ItemInInventory itemInInventory = null;
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
			if (itemInInventory2.idTemplate < 681 || itemInInventory2.idTemplate > HKL_CAP_TOI_DA || itemInInventory2.isSelling) continue;
			if (itemInInventory2.idTemplate < HKL_CAP_TOI_DA)
			{
				return itemInInventory2;
			}
			itemInInventory = itemInInventory2;
		}
		return itemInInventory;
	}

	private void autoStartHkl()
	{
		ItemInInventory itemInInventory = PauseMenu.hklMon();
		if (itemInInventory == null)
		{
			Canvas.startOKDlg("Không thấy Hỏa kỳ lân trong hành trang (nếu đang mặc, hãy tháo ra hành trang).");
			return;
		}
		if (itemInInventory.idTemplate >= HKL_CAP_TOI_DA)
		{
			Canvas.startOKDlg("Hỏa kỳ lân đã nâng cấp tối đa (cấp 3).");
			return;
		}
		this.autoHklOk = 0;
		this.autoHklHong = 0;
		this.autoHklDaMua = 0;
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 13;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoPhase = 180;
		this.autoWaitTicks = 0;
	}

	private void showNghienMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Loại nguyên liệu: " + (this.autoNbLoai < 0 ? "Tất cả" : PET_LOAI_TEN[this.autoNbLoai]))
		{
			action = () =>
			{
				this.autoNbLoai = this.autoNbLoai >= PET_LOAI_TEN.Length - 1 ? -1 : this.autoNbLoai + 1;
				this.showNghienMenu(0);
			}
		});
		vector.addElement(new Command("Nguyên liệu sơ cấp: cấp " + (this.autoNbCapSo + 1))
		{
			action = () =>
			{
				this.autoNbCapSo = (this.autoNbCapSo + 1) % 6;
				this.showNghienMenu(1);
			}
		});
		vector.addElement(new Command("Nguyên liệu cao cấp: cấp " + (this.autoNbCapCao + 1))
		{
			action = () =>
			{
				this.autoNbCapCao = (this.autoNbCapCao + 1) % 6;
				this.showNghienMenu(2);
			}
		});
		vector.addElement(new Command("Bắt đầu chế và nghiền bột")
		{
			action = () =>
			{
				this.autoStartNghien();
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	/** Món auto được phép nghiền: trang bị thường (type < 13) cấp 20/21, có màu, không đang bán. */
	private static bool nbNghienDuoc(ItemInInventory itemInInventory)
	{
		ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
		return itemTemplate != null && itemTemplate.type < 13 && itemInInventory.level >= 20 && itemInInventory.level <= 21 && itemInInventory.colorName != 0 && !itemInInventory.isSelling;
	}

	/** Món đầu tiên trong túi auto được nghiền, null nếu không còn. */
	private static ItemInInventory nbMonNghien()
	{
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector.elementAt(i);
			if (!PauseMenu.nbNghienDuoc(itemInInventory)) continue;
			return itemInInventory;
		}
		return null;
	}

	private static int nbDemNghien()
	{
		int n = 0;
		mVector vector = Char.inventory;
		for (int i = 0; vector != null && i < vector.size(); ++i)
		{
			if (!PauseMenu.nbNghienDuoc((ItemInInventory)vector.elementAt(i))) continue;
			++n;
		}
		return n;
	}

	private void autoStartNghien()
	{
		this.autoNbDang = (short)-1;
		this.autoNbCo = false;
		this.autoNbThu = 0;
		this.autoNbLuc = 0L;
		this.autoNbHet = false;
		this.autoNbChe = 0;
		this.autoNbBan = 0;
		this.autoNbLoi = null;
		// không giữ hàng đợi: mỗi lượt nghiền lấy thẳng món đủ điều kiện trong túi (nbMonNghien) -
		// gồm cả đồ cấp 20/21 có màu còn dư từ trước và món vừa chế
		this.autoNbGom = PauseMenu.nbDemNghien();
		this.autoNbBo = 0;
		this.autoNbGui = 0;
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 14;
		this.autoMode = 0;
		this.autoSlot = 0;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoSoldIds.removeAllElements();
		// nghiền chỉ làm được trong nhà; vào nhà rồi qua 193 (chờ hàng đợi / hành trang) mới chế
		if (Canvas.gameScr.isOffLineMap(Canvas.gameScr.map))
		{
			this.autoPhase = 193;
		}
		else
		{
			this.autoAfterHouse = 193;
			this.autoPhase = 20;
		}
		this.autoWaitTicks = 0;
	}

	/**
	 * Gọi mỗi tick khi đang nghiền (trước switch): đủ 5s thì nghiền món đầu hàng đợi. Món vừa
	 * gửi nghiền mà còn trong túi ở lượt sau (vd "Thao tác quá nhanh") thì gửi lại, 3 lần thì bỏ.
	 */
	private void nghienTick()
	{
		long l = mSystem.currentTimeMillis();
		if (l < this.autoNbLuc)
		{
			return;
		}
		if (this.autoNbCo)
		{
			// KHÔNG xét "món còn trong túi": server tái dùng id - nghiền xong id X được trả lại
			// và món chế ngay sau lấy đúng X, trông như món chưa bị nghiền. Đếm theo số bột.
			if (PauseMenu.hklDa(NB_BOT) > this.autoNbBotTruoc)
			{
				++this.autoFound;
				this.autoNbCo = false;
			}
			else if (this.autoNbLoi == null && l - this.autoNbGuiLuc < NB_CHO_KQ_MS)
			{
				// kết quả chưa về: chờ tiếp
				return;
			}
			else if (this.autoNbLoi != null && this.autoNbLoi.StartsWith("Thao tác quá nhanh", System.StringComparison.Ordinal))
			{
				// server còn đang trong thời gian chờ (vd server chưa cập nhật bản 1 giây): gửi lại,
				// không tính là một lần thất bại
				this.autoNbLoi = null;
			}
			else if (++this.autoNbThu >= 3)
			{
				this.autoStop("Auto nghiền bột: server không cho nghiền" + (this.autoNbLoi == null ? " (không có phản hồi)" : ": " + this.autoNbLoi) + " - map " + Canvas.gameScr.map + ", đã gửi " + this.autoNbGui + " lệnh, đã dừng.");
				return;
			}
		}
		if (!this.autoNbCo)
		{
			ItemInInventory itemInInventory = PauseMenu.nbMonNghien();
			if (itemInInventory != null)
			{
				this.autoNbDang = itemInInventory.itemID;
				this.autoNbCo = true;
				this.autoNbThu = 0;
			}
		}
		if (!this.autoNbCo)
		{
			return;
		}
		this.autoNbBotTruoc = PauseMenu.hklDa(NB_BOT);
		// món này sắp biến mất và id của nó có thể được cấp lại cho món đang chế dở: bỏ id khỏi
		// snapshot để phase 190 vẫn nhận ra món mới mang lại id đó
		for (int i = 0; this.autoSnapshot != null && i < this.autoSnapshot.Length; ++i)
		{
			if (this.autoSnapshot[i] != this.autoNbDang) continue;
			this.autoSnapshot[i] = (short)-32768;
		}
		this.autoNbLoi = null;
		this.autoNbGuiLuc = l;
		GameService.gI().doRequestTachNguyenLIeu(this.autoNbDang, 4, 0, 0);
		++this.autoNbGui;
		this.autoNbLuc = l + NB_CACH_MS;
	}

	private static string cttMucText(int n)
	{
		return n >= 5 ? "0.5%" : "0." + n + "% - 0.5%";
	}

	private static string cttGiaTri(int n)
	{
		return n / 10 + "." + n % 10 + "%";
	}

	/** Mức cao nhất món này ra được với viên đá đang chọn - Item.doAddNewAttribute ở server. */
	private int cttMucToiDa(ItemInInventory itemInInventory)
	{
		if (this.autoCttDa != CTT_DA_CAO_CAP || itemInInventory == null)
		{
			return 3;
		}
		return itemInInventory.hangItem == 1 ? 5 : (itemInInventory.hangItem == 2 ? 4 : 3);
	}

	/** Món đã có sẵn thuộc tính đạt mục tiêu (server chỉ giữ 1 trong 4 loại lan mỗi lần cộng). */
	private bool cttDatSan(ItemInInventory itemInInventory)
	{
		for (int i = 0; i < CTT_ID.Length; ++i)
		{
			if (this.autoCttLoai >= 0 && this.autoCttLoai != i || PauseMenu.autoAttVal(itemInInventory, CTT_ID[i]) < this.autoCttMin) continue;
			return true;
		}
		return false;
	}

	/** Đồ xanh lá còn cộng được tới mục tiêu: không đang bán, không phải đồ thú, chưa đạt sẵn. */
	private bool cttCanDap(ItemInInventory itemInInventory)
	{
		ItemTemplate itemTemplate = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
		return itemTemplate != null && (itemTemplate.type < 14 || itemTemplate.type > 18) && itemInInventory.colorName == 1 && !itemInInventory.isSelling && this.cttMucToiDa(itemInInventory) >= this.autoCttMin && !this.cttDatSan(itemInInventory);
	}

	private void autoStartCongTT(bool bl)
	{
		ItemInInventory itemInInventory = this.autoFindItem(this.autoCttItem);
		if (!bl && itemInInventory == null)
		{
			Canvas.startOKDlg("Không thấy món đồ trong hành trang.");
			return;
		}
		int n = this.cttMucToiDa(itemInInventory);
		if (!bl && this.autoCttMin > n)
		{
			Canvas.startOKDlg("Với " + (this.autoCttDa == CTT_DA_CAO_CAP ? "món đồ này" : "đá thuộc tính thường") + " chỉ ra được tối đa " + PauseMenu.cttGiaTri(n) + (this.autoCttDa == CTT_DA_CAO_CAP ? " (0.5% cần đồ Nhất phẩm, 0.4% cần Nhị phẩm)." : ", hãy dùng đá cao cấp."));
			return;
		}
		this.autoCttFull = bl;
		if (bl)
		{
			// món đang đặt (nếu hợp lệ) đập trước, rồi lần lượt các món còn lại trong hành trang
			mVector vector = Char.inventory;
			short[] sArray = new short[(vector == null ? 0 : vector.size()) + 1];
			int n2 = 0;
			if (itemInInventory != null && this.cttCanDap(itemInInventory))
			{
				sArray[n2++] = itemInInventory.itemID;
			}
			for (int i = 0; vector != null && i < vector.size(); ++i)
			{
				ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(i);
				if (itemInInventory2 == itemInInventory || !this.cttCanDap(itemInInventory2)) continue;
				sArray[n2++] = itemInInventory2.itemID;
			}
			if (n2 == 0)
			{
				Canvas.startOKDlg("Không có món nào cần cộng tới " + (this.autoCttLoai < 0 ? "" : CTT_NAME[this.autoCttLoai] + " ") + PauseMenu.cttMucText(this.autoCttMin) + " (cần đồ xanh lá" + (this.autoCttDa == CTT_DA_CAO_CAP ? (this.autoCttMin >= 5 ? ", Nhất phẩm" : (this.autoCttMin == 4 ? ", Nhất/Nhị phẩm" : "")) : "") + (this.autoCttDa != CTT_DA_CAO_CAP && this.autoCttMin > 3 ? "; đá thường chỉ ra tối đa 0.3%" : "") + ").");
				return;
			}
			this.autoTargets = new short[n2];
			System.Array.Copy(sArray, 0, this.autoTargets, 0, n2);
			this.autoTargetPos = 0;
			this.autoCttItem = this.autoTargets[0];
		}
		this.autoCttVuaRa = null;
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 9;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoPhase = 95;
		this.autoWaitTicks = 0;
	}

	private void showThanBinhMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Cấp thần binh đang mặc: " + this.autoTbLevel)
		{
			action = () =>
			{
				this.autoTbLevel = this.autoTbLevel >= 80 ? 60 : this.autoTbLevel + 10;
				this.showThanBinhMenu();
			}
		});
		vector.addElement(new Command("Bắt đầu tinh luyện thần binh")
		{
			action = () =>
			{
				this.autoStartThanBinh();
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private void autoStartThanBinh()
	{
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 6;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoTbPct = -1;
		this.autoSlot = 0;
		this.autoPhase = 0;
		this.autoWaitTicks = 0;
	}

	private void showThanKhiMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Loại: " + TK_TYPE_NAME[this.autoTkType])
		{
			action = () =>
			{
				this.autoTkType = (this.autoTkType + 1) % TK_TYPE_NAME.Length;
				this.showThanKhiMenu();
			}
		});
		vector.addElement(new Command(this.autoTkFilter == 0 ? "Lọc: chỉ giữ 9 dòng" : "Lọc: Tắt")
		{
			action = () =>
			{
				this.autoTkFilter = 1 - this.autoTkFilter;
				this.showThanKhiMenu();
			}
		});
		vector.addElement(new Command("Số lượng: " + this.autoTkQty)
		{
			action = () =>
			{
				int n = 0;
				for (int i = 0; i < TK_QTY.Length; ++i)
				{
					if (TK_QTY[i] != this.autoTkQty) continue;
					n = i;
				}
				this.autoTkQty = TK_QTY[(n + 1) % TK_QTY.Length];
				this.showThanKhiMenu();
			}
		});
		vector.addElement(new Command("Bắt đầu chế thần khí")
		{
			action = () =>
			{
				this.autoStartThanKhi();
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private void autoStartThanKhi()
	{
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 5;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoTkGrade = 5;
		this.autoTkNeedStone = false;
		this.autoTkStones = PauseMenu.autoGemCount(154);
		this.autoAfterHouse = 60;
		this.autoPhase = this.autoTkFilter == 0 ? 20 : 60;
		this.autoWaitTicks = 0;
	}

	private void showThuMenu()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Cấp đồ thú: lv" + this.autoPetLevel)
		{
			action = () =>
			{
				this.autoPetLevel = this.autoPetLevel >= 85 ? 30 : this.autoPetLevel + 5;
				this.showThuMenu();
			}
		});
		vector.addElement(new Command("Kiểu: " + PET_MODE_NAME[this.autoPetMode])
		{
			action = () =>
			{
				this.autoPetMode = (this.autoPetMode + 1) % PET_MODE_NAME.Length;
				this.showThuMenu();
			}
		});
		if (this.autoPetMode != 2)
		{
			vector.addElement(new Command("Món: " + PET_SLOT_NAME[this.autoPetSlot])
			{
				action = () =>
				{
					this.autoPetSlot = (this.autoPetSlot + 1) % PET_SLOT_NAME.Length;
					this.showThuMenu();
				}
			});
		}
		if (this.autoPetMode == 1)
		{
			vector.addElement(new Command("Ngũ hành: " + PET_HE_NAME[this.autoPetHe])
			{
				action = () =>
				{
					this.autoPetHe = (this.autoPetHe + 1) % PET_HE_NAME.Length;
					this.showThuMenu();
				}
			});
		}
		vector.addElement(new Command(this.heMaPhap ? "Hệ: Ma pháp" : "Hệ: Vật lý")
		{
			action = () =>
			{
				this.heMaPhap = !this.heMaPhap;
				this.showThuMenu();
			}
		});
		vector.addElement(new Command("Lọc: " + PET_FILTER_NAME[this.autoPetFilter])
		{
			action = () =>
			{
				this.autoPetFilter = (this.autoPetFilter + 1) % PET_FILTER_NAME.Length;
				this.showThuMenu();
			}
		});
		vector.addElement(new Command("Bắt đầu auto đồ thú")
		{
			action = () =>
			{
				this.autoStartPet();
			}
		});
		Canvas.menu.startAt(vector, 3);
	}

	private void autoStartPet()
	{
		this.autoRunning = true;
		AutoGuard.on = true;
		AutoGuard.pendingSell = 0;
		this.autoJob = 4;
		this.autoTries = 0;
		this.autoFound = 0;
		this.autoPetCurSlot = this.autoPetSlot == 5 || this.autoPetMode == 2 ? 0 : this.autoPetSlot;
		this.autoPetEndSlot = this.autoPetSlot == 5 || this.autoPetMode == 2 ? 5 : this.autoPetSlot + 1;
		this.autoPetBaseHe = -1;
		this.autoPetOwned = new mVector();
		this.autoPetSell = new mVector();
		this.autoAfterHouse = 41;
		this.autoPhase = 30;
		this.autoWaitTicks = 0;
	}

	private int autoPetN()
	{
		return PET_MAT[(this.autoPetLevel - 30) / 5];
	}

	private int autoPetTemplate()
	{
		return PET_BASE[this.autoPetCurSlot] + (this.autoPetLevel - 30) / 5;
	}

	private static bool autoHasGemType(int n)
	{
		if (MainChar.gemItem == null)
		{
			return false;
		}
		for (int i = 0; i < MainChar.gemItem.size(); ++i)
		{
			if (((GemTemplate)MainChar.gemItem.elementAt(i)).id != n) continue;
			return true;
		}
		return false;
	}

	private static int autoGemCount(int n)
	{
		int n2 = 0;
		mVector[] vectorArray = new mVector[] { MainChar.gemItem, MainChar.gemItemKhoa };
		for (int i = 0; i < 2; ++i)
		{
			if (vectorArray[i] == null) continue;
			for (int j = 0; j < vectorArray[i].size(); ++j)
			{
				GemTemplate gemTemplate = (GemTemplate)vectorArray[i].elementAt(j);
				if (gemTemplate.id != n) continue;
				n2 += gemTemplate.number;
			}
		}
		return n2;
	}

	private bool autoPetIsOwned(short s)
	{
		for (int i = 0; i < this.autoPetOwned.size(); ++i)
		{
			// Java: ((Integer)elementAt(i)).intValue() - Convert để chịu được cả int/short đã đóng hộp
			if (System.Convert.ToInt32(this.autoPetOwned.elementAt(i)) != s) continue;
			return true;
		}
		return false;
	}

	private bool autoPetIsSell(short s)
	{
		for (int i = 0; i < this.autoPetSell.size(); ++i)
		{
			if (System.Convert.ToInt32(this.autoPetSell.elementAt(i)) != s) continue;
			return true;
		}
		return false;
	}

	private mVector autoPetByColor(int n)
	{
		mVector vector = new mVector();
		mVector vector2 = Char.inventory;
		if (vector2 == null)
		{
			return vector;
		}
		for (int i = 0; i < vector2.size(); ++i)
		{
			ItemInInventory itemInInventory = (ItemInInventory)vector2.elementAt(i);
			if (itemInInventory.colorName != n || !this.autoPetIsOwned(itemInInventory.itemID) || this.autoPetIsSell(itemInInventory.itemID)) continue;
			if (itemInInventory.idTemplate == this.autoPetTemplate())
			{
				vector.insertElementAt(itemInInventory, 0);
				continue;
			}
			vector.addElement(itemInInventory);
		}
		return vector;
	}

	private int autoPetH()
	{
		if (this.autoPetMode == 1)
		{
			return this.autoPetHe;
		}
		if (this.autoPetMode == 2 && this.autoPetBaseHe >= 0)
		{
			return (this.autoPetBaseHe + this.autoPetCurSlot) % 5;
		}
		return -1;
	}

	private bool autoPetCenterOk(ItemInInventory itemInInventory, int n)
	{
		return itemInInventory.idTemplate == this.autoPetTemplate() && (n == 0 || this.autoPetH() < 0 || itemInInventory.heItem == this.autoPetH());
	}

	private bool autoPetHas(int n, int n2)
	{
		mVector vector = this.autoPetByColor(n);
		for (int i = 0; i < vector.size(); ++i)
		{
			if (!this.autoPetCenterOk((ItemInInventory)vector.elementAt(i), n2)) continue;
			return true;
		}
		return false;
	}

	private mVector autoPetPickEp(int n)
	{
		mVector vector = this.autoPetByColor(PET_SRC_COLOR[n]);
		if (vector.size() < 5)
		{
			return null;
		}
		ItemInInventory itemInInventory = null;
		for (int i = 0; i < vector.size(); ++i)
		{
			if (!this.autoPetCenterOk((ItemInInventory)vector.elementAt(i), n)) continue;
			itemInInventory = (ItemInInventory)vector.elementAt(i);
			break;
		}
		if (itemInInventory == null)
		{
			itemInInventory = (ItemInInventory)vector.elementAt(0);
		}
		mVector vector2 = new mVector();
		vector2.addElement(itemInInventory);
		for (int i = 0; i < 2 && vector2.size() < 5; ++i)
		{
			for (int j = 0; j < vector.size() && vector2.size() < 5; ++j)
			{
				ItemInInventory itemInInventory2 = (ItemInInventory)vector.elementAt(j);
				if (itemInInventory2 == itemInInventory || this.autoPetCenterOk(itemInInventory2, 1) != (i == 1)) continue;
				vector2.addElement(itemInInventory2);
			}
		}
		return vector2;
	}

	private mVector autoPetBuildGroups(int n, int n2)
	{
		mVector vector = new mVector();
		mVector vector2 = this.autoPetPickEp(n);
		if (vector2 == null || n2 <= 0)
		{
			return vector;
		}
		vector.addElement(vector2);
		mVector vector3 = this.autoPetByColor(PET_SRC_COLOR[n]);
		for (int i = 0; i < vector2.size(); ++i)
		{
			vector3.removeElement(vector2.elementAt(i));
		}
		while (vector.size() < n2 && vector3.size() >= 5)
		{
			mVector vector4 = new mVector();
			for (int i = 0; i < 5; ++i)
			{
				vector4.addElement(vector3.elementAt(0));
				vector3.removeElementAt(0);
			}
			vector.addElement(vector4);
		}
		return vector;
	}

	private int[] autoPetNeeds()
	{
		bool bl = this.autoPetHas(2, 2);
		bool bl2 = this.autoPetHas(3, 1);
		int n = Math.max(0, 5 - this.autoPetByColor(2).size());
		if (!bl)
		{
			n = Math.max(n, 1);
		}
		int n2 = Math.max(0, 5 * n - this.autoPetByColor(3).size());
		if (!bl && !bl2)
		{
			n2 = Math.max(n2, 1);
		}
		int n3 = Math.max(0, 5 * n2 - this.autoPetByColor(0).size());
		return new int[] { n, n2, n3 };
	}

	private bool autoPetOk(ItemInInventory itemInInventory)
	{
		if (this.autoPetFilter == 3)
		{
			return true;
		}
		int n = PauseMenu.autoAttVal(itemInInventory, 61);
		int n2 = PauseMenu.autoAttVal(itemInInventory, 62);
		if (this.autoPetFilter == 0)
		{
			return n >= 3;
		}
		if (this.autoPetFilter == 1)
		{
			return n2 >= 3;
		}
		return n >= 3 || n2 >= 3;
	}

	private void autoPetExitHouse()
	{
		mVector vector = Tilemap.mapchangeLocations;
		CMLocation cMLocation = vector == null || vector.size() == 0 ? null : (CMLocation)vector.elementAt(0);
		if (cMLocation == null)
		{
			this.autoStop("Auto đồ thú: không tìm thấy cửa ra khỏi nhà, đã dừng.");
			return;
		}
		GameService.gI().changeMap(GameScr.oldMap, cMLocation.toX, cMLocation.toY);
		ChangScr.gI().switchToMe();
		Res.removeMonsterTemplet();
		this.autoPhase = 51;
		this.autoWaitCount = 0;
	}
}
