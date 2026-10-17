// Port 1:1 từ Jar276/src/game/render/screen/AutoGuard.java
using System;

public sealed class AutoGuard
{
	public static bool on;

	public static int pendingSell;

	public static bool eatXinCho;

	/** Bảng màu nền thanh trạng thái auto. */
	public static readonly int[] MAU_NEN = new int[8] { 0x69436c, 0x45957f, 0xd11674, 0xb24394, 0xf69a31, 0x8c9b09, 0xbcdeeb, 0x8f48ec };

	public static bool autoHideUi()
	{
		return on;
	}

	public static bool autoEatDlg(string @string)
	{
		try
		{
			if (AutoVanTieu.thongBao(@string) || AutoVanMay.thongBao(@string))
			{
				return true;
			}
		}
		catch (Exception)
		{
			// không được chặn hộp thoại của game
		}
		try
		{
			if (TienIch.anThongBao(@string))
			{
				return true;
			}
		}
		catch (Exception)
		{
			// empty catch block
		}
		if (eatXinCho)
		{
			eatXinCho = false;
			if (@string != null && @string.StartsWith("Xin ch", System.StringComparison.Ordinal))
			{
				return true;
			}
		}
		if (pendingSell > 0 && @string != null && @string.StartsWith("Đã bán", System.StringComparison.Ordinal))
		{
			--pendingSell;
			return true;
		}
		if (!on)
		{
			return false;
		}
		return PauseMenu.autoEatDlg(@string);
	}

	public static void startAtDapDo(Menu menu, mVector vector, int n)
	{
		try
		{
			PauseMenu.addLuyenCommands(vector);
		}
		catch (Exception)
		{
			// never block the original menu
		}
		menu.startAt(vector, n);
	}

	public static void themDong(GameService gameService, short s, int n, int n2, int n3, int n4, int n5)
	{
		try
		{
			if (PauseMenu.showThemDongMenu(s, n, n2, n3, n4, n5))
			{
				eatXinCho = true;
				return;
			}
		}
		catch (Exception)
		{
			// fall back to the game's own single roll
		}
		gameService.doRequestThemThuocTinh(s, n, n2, n3, n4, n5);
	}

	private static bool dapThat;

	/**
	 * Chèn ở ĐẦU ScreenDapDo$7.perform() (nút "Đập"). Màn Cộng thuộc tính (type 0) / Khóa đồ thú (type 2) đã đặt đủ đồ
	 * + đá: chỉ mở menu rồi trả true để bỏ toàn bộ code gốc (gửi lệnh, animation, đổi nút
	 * "Tiếp tục") - chưa có thao tác nào cho tới khi chọn một mục trong menu.
	 * Thiếu đồ/đá thì trả false để code gốc tự báo lỗi như cũ.
	 * (C#: chèn ở đầu cmdEp.action trong ScreenDapDo.cs)
	 */
	public static bool dapGuard()
	{
		if (dapThat)
		{
			return false;
		}
		try
		{
			ScreenDapDo screenDapDo = ScreenDapDo.gI();
			if ((screenDapDo.type != 0 && screenDapDo.type != 2) || ScreenDapDo.vnguyenLieu.size() <= 0 || ScreenDapDo.itemSelect == null || ScreenDapDo.selectGem == null)
			{
				return false;
			}
			if (screenDapDo.type == 2)
			{
				return PauseMenu.showKhoaThuMenu(ScreenDapDo.itemSelect.itemID, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.ilock ? 1 : 0);
			}
			return PauseMenu.showCongTTMenu(ScreenDapDo.itemSelect.itemID, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.ilock ? 1 : 0);
		}
		catch (Exception)
		{
			// never block the original button
			return false;
		}
	}

	/** Chèn ở ĐẦU GameScr.onMenuOption: true = menu NPC đã được auto dùng, không hiện ra. */
	public static bool menuGuard(int n, sbyte by, string[] stringArray)
	{
		try
		{
			if (AutoVanMay.anMenu(n))
			{
				return true;
			}
			return PauseMenu.petMenuGuard(n, by, stringArray);
		}
		catch (Exception)
		{
			return false;
		}
	}

	/** Tài khoản của lần đăng nhập gần nhất - để auto nuôi pet tự đăng nhập lại (chỉ giữ trong bộ nhớ). */
	// C#: GameService.login gán hai biến này (thay cho loginGuard bản jar).
	public static string tkDangNhap;

	public static string mkDangNhap;

	// BỎ (không port): loginGuard / maMay / guiDangNhap.
	// Bản C# đã xử lý trong GameService.login (ghi nhớ tkDangNhap/mkDangNhap, gọi KiemServer.chanDangNhap,
	// gửi gói đăng nhập kèm KiemServer.maMay()) nên không cần guard ở đây.

	// BỎ (không port): goiGuard.
	// Bản C# MessageHandler.onMessage gọi thẳng KiemServer.nhan(msg) và AutoVanMay.anGoi(msg.command).

	/** Chèn ở ĐẦU GameScr.onActorMove: chỉ ghi nhận vị trí server gửi cho chính mình, không chặn. */
	public static bool viTriGuard(sbyte by, short s, short s2, short s3, short s4)
	{
		try
		{
			PauseMenu.viTriServer(by, s2, s3, s4);
		}
		catch (Exception)
		{
			// không bao giờ cản lệnh di chuyển của game
		}
		try
		{
			AutoGuard.boQuaiDangChet(by, s2);
		}
		catch (Exception)
		{
			// empty catch block
		}
		return false;
	}

	/**
	 * Chèn ở ĐẦU GameService.requestMonsterInfo: true = không gửi lệnh 7. Lúc auto vận tiêu nháy map
	 * (nạp lại map mỗi vài ô) client xin lại thông tin MỌI con quái -> vượt chống flood của server
	 * (300 gói / 5s), server bỏ cả lệnh nháy -> xe tiêu bị bỏ lại. Chỉ xin cho xe tiêu (idChar >= 0).
	 */
	public static bool chanXinQuai(short s)
	{
		try
		{
			if (!AutoVanTieu.dang || Canvas.gameScr == null)
			{
				return false;
			}
			mVector vector = Canvas.gameScr.actors;
			for (int i = vector.size() - 1; i >= 0; --i)
			{
				Actor actor = (Actor)vector.elementAt(i);
				if (actor == null || actor.catagory != 1 || actor.ID != s) continue;
				return !(actor is Monster) || ((Monster)actor).idChar < 0;
			}
		}
		catch (Exception)
		{
			// không bao giờ cản lệnh của game
		}
		return false;
	}

	/**
	 * Lệnh 4 cho quái (cat 1) mà actor cùng id đang chạy hiệu ứng chết (lệnh 90, die = true): bỏ
	 * actor cũ để onActorMove tạo lại actor mới (đúng loại + mã chủ tiêu). Đổi tiêu là vậy: server
	 * gửi 90 rồi 4 cùng id với loại xe mới; giữ actor cũ thì nó chỉ đổi vị trí rồi bị xoá ở khung
	 * hình sau -> xe tiêu biến mất tới khi nó đi lại.
	 */
	private static void boQuaiDangChet(sbyte by, short s)
	{
		if (by != 1 || Canvas.gameScr == null)
		{
			return;
		}
		mVector vector = Canvas.gameScr.actors;
		for (int i = vector.size() - 1; i >= 0; --i)
		{
			Actor actor = (Actor)vector.elementAt(i);
			if (actor == null || actor.catagory != 1 || actor.ID != s || !actor.die) continue;
			vector.removeElementAt(i);
		}
	}

	/**
	 * Chèn ở ĐẦU GameScr.onMap. onMap luôn xóa hết actor (cả NPC tĩnh của map) nhưng chỉ nạp lại
	 * map - và NPC - khi id map KHÁC map đang đứng. Server gửi lại cùng id (đăng nhập lại đúng map
	 * cũ, đổi khu, hoa tiêu trong cùng map) thì NPC mất hẳn. Đặt map = -1 để luôn nạp lại; nạp xong
	 * client gửi changeMapOK(-500) và server gửi lại quái gần.
	 */
	public static bool mapGuard(short s)
	{
		try
		{
			GameScr gameScr = Canvas.gameScr;
			if (gameScr != null && gameScr.map == s)
			{
				gameScr.map = (short)(-1);
			}
		}
		catch (Exception)
		{
			// không bao giờ cản lệnh vào map
		}
		return false;
	}

	// BỎ (không port): dsGuard / capNhatGuard (TaiDanhSach.taiNgam / capNhat).
	// Bản C# ServerListScr tự tải danh sách server (ServerListScr.taiDanhSachTuDong).

	/** Chèn ở ĐẦU GameScr.onAdd2Party: auto up tự vào nhóm / bỏ qua lời mời người lạ. */
	public static bool nhomGuard(Message message)
	{
		try
		{
			// AutoUp.moiNhom đọc trên bản sao new myReader(message.reader().buffer) (C# không có mark/reset)
			return AutoUp.moiNhom(message);
		}
		catch (Exception)
		{
			return false;
		}
	}

	/** Chèn ở ĐẦU GameScr.showWindow: true = không mở cửa sổ (auto nuôi pet chế thẳng). */
	public static bool cuaSoGuard(int n, bool bl, sbyte[] byArray)
	{
		try
		{
			return PauseMenu.anCuaSoCheDo(n, bl, byArray);
		}
		catch (Exception)
		{
			return false;
		}
	}

	/** Mục "Đập 1 lần": chạy đúng nút "Đập" gốc (gửi lệnh + animation) một lần. */
	public static void dapMotLan()
	{
		dapThat = true;
		try
		{
			ScreenDapDo.gI().cmdEp.action();
		}
		finally
		{
			dapThat = false;
		}
	}

	// ---------------------------------------------------------------- nút UP nổi trên màn game
	// Bật/tắt Auto up không cần mở menu (bản jar: Nen.coNutUp() = false trên J2ME; Unity luôn có).

	public const int NUT_W = 52;

	public const int NUT_H = 52;

	public static int nutX()
	{
		return Canvas.w - NUT_W - 6;
	}

	public static int nutY()
	{
		return Canvas.h / 3;
	}

	/** Nút TIÊU (bật/tắt vòng vận tiêu) nằm ngay dưới nút UP. */
	public static int nutY2()
	{
		return nutY() + NUT_H + 8;
	}

	/** Điểm (x, y) nằm trong nút (đang ở màn game). */
	public static bool trongNutUp(int x, int y)
	{
		if (MenuNhanh.bat())
		{
			return MenuNhanh.trongNut(x, y);
		}
		return Canvas.currentScreen == Canvas.gameScr && x >= nutX() && x < nutX() + NUT_W && y >= nutY() && y < nutY() + NUT_H;
	}

	/** Nút PK (bật/tắt Đập người) dưới nút TIÊU. */
	public static int nutY3()
	{
		return nutY2() + NUT_H + 8;
	}

	public static bool trongNutPk(int x, int y)
	{
		return Canvas.currentScreen == Canvas.gameScr && x >= nutX() && x < nutX() + NUT_W && y >= nutY3() && y < nutY3() + NUT_H;
	}

	/** Cột nút thứ hai (MÃ PK / NHÁY / ĐS / DỪNG) bên trái cột UP / TIÊU / PK. */
	public static int nutX2()
	{
		return nutX() - NUT_W - 6;
	}

	public static int nutYPhim(int n)
	{
		return nutY() + n * (NUT_H + 8);
	}

	/** Nút phím PK thứ n (0..3) có điểm (x, y) - chỉ khi đang hiện. */
	public static bool trongNutPhim(int n, int x, int y)
	{
		return AutoPk.hienNut && Canvas.currentScreen == Canvas.gameScr && x >= nutX2() && x < nutX2() + NUT_W && y >= nutYPhim(n) && y < nutYPhim(n) + NUT_H;
	}

	public static bool trongNutTieu(int x, int y)
	{
		return Canvas.currentScreen == Canvas.gameScr && x >= nutX() && x < nutX() + NUT_W && y >= nutY2() && y < nutY2() + NUT_H;
	}

	/** Chèn ở đầu xử lý phím của Canvas: phím tắt PK (AutoPk.phim). true = đã dùng phím này. */
	public static bool phimPk(int n)
	{
		try
		{
			return AutoPk.phim(n);
		}
		catch (Exception)
		{
			return false;
		}
	}

	/**
	 * Đầu PauseMenu.autoCheDoTick (trước khi game xử lý chạm): chạm nút thì bật/tắt Auto up và nuốt cú
	 * chạm - nhân vật không chạy tới chỗ đó, AutoUp.nguoiBam không coi là người chơi chạm để dừng.
	 */
	public static void nutUpTick()
	{
		if (Canvas.currentScreen != Canvas.gameScr || Canvas.menu.showMenu || Canvas.currentDialog != null)
		{
			return;
		}
		if (MenuNhanh.bat())
		{
			// pokemon: Menu nhanh + thanh chiêu nhận chuột trước game
			MenuNhanh.tick();
			HudPokemon.tick(Canvas.gameScr);
			return;
		}
		if (Canvas.isPointerDown && (trongNutUp(Canvas.px, Canvas.py) || trongNutTieu(Canvas.px, Canvas.py) || trongNutPk(Canvas.px, Canvas.py)))
		{
			Canvas.isPointerDown = false;
		}
		if (Canvas.isPointerClick && trongNutUp(Canvas.px, Canvas.py) && trongNutUp(Canvas.pxLast, Canvas.pyLast))
		{
			Canvas.isPointerClick = false;
			AutoUp.batTat();
		}
		if (Canvas.isPointerClick && trongNutTieu(Canvas.px, Canvas.py) && trongNutTieu(Canvas.pxLast, Canvas.pyLast))
		{
			Canvas.isPointerClick = false;
			AutoVanTieu.batTat();
		}
		if (Canvas.isPointerClick && trongNutPk(Canvas.px, Canvas.py) && trongNutPk(Canvas.pxLast, Canvas.pyLast))
		{
			Canvas.isPointerClick = false;
			AutoPk.batTat();
		}
		for (int i = 0; i < 5; ++i)
		{
			if (Canvas.isPointerDown && trongNutPhim(i, Canvas.px, Canvas.py))
			{
				Canvas.isPointerDown = false;
			}
			if (!Canvas.isPointerClick || !trongNutPhim(i, Canvas.px, Canvas.py) || !trongNutPhim(i, Canvas.pxLast, Canvas.pyLast)) continue;
			Canvas.isPointerClick = false;
			if (i == 0)
			{
				AutoPk.nutMaPk();
			}
			else if (i == 1)
			{
				AutoPk.nutNhay();
			}
			else if (i == 2)
			{
				AutoPk.nutDs();
			}
			else if (i == 3)
			{
				AutoPk.nutDung();
			}
			else
			{
				AutoPk.nutDoiPet();
			}
		}
	}

	/**
	 * Hình tròn tâm (cx, cy) bán kính r tô dải màu dọc từ tren xuống duoi (0xRRGGBB) vào mảng điểm ảnh
	 * (gốc trên-trái, rộng NUT_W). Bản PC vẽ nút một lần vào ảnh rồi mỗi khung chỉ vẽ ảnh - vẽ từng hàng bằng
	 * fillRect (như bản jar/Android) tốn ~190 lệnh vẽ IMGUI mỗi khung.
	 */
	private static void veTron(UnityEngine.Color32[] diem, int cx, int cy, int r, int tren, int duoi)
	{
		for (int dy = -r; dy <= r; dy++)
		{
			int w = 0;
			while ((w + 1) * (w + 1) <= r * r + r - dy * dy)
			{
				w++;
			}
			int m = tronMau(tren, duoi, dy + r, 2 * r);
			UnityEngine.Color32 c = new UnityEngine.Color32((byte)(m >> 16), (byte)(m >> 8), (byte)m, 255);
			int y = cy + dy;
			if (y < 0 || y >= NUT_H)
			{
				continue;
			}
			for (int x = System.Math.Max(0, cx - w); x <= System.Math.Min(NUT_W - 1, cx + w); x++)
			{
				diem[(NUT_H - 1 - y) * NUT_W + x] = c; // Texture2D: hàng 0 ở dưới
			}
		}
	}

	/** Ảnh nút: 0 = tắt, 1 = bật (viền sáng), 2 = bật (viền tối) - nhấp nháy giữa 1 và 2. */
	private static Image[] anhNut = new Image[3];

	private static Image taoAnhNut(bool bat, bool sang)
	{
		UnityEngine.Color32[] diem = new UnityEngine.Color32[NUT_W * NUT_H]; // mặc định trong suốt
		int r = NUT_W / 2 - 3;
		int cx = NUT_W / 2;
		int cy = NUT_H / 2 - 1;
		veTron(diem, cx, cy + 2, r + 1, 0x000000, 0x000000);
		int vien = bat ? (sang ? 0xFFD870 : 0xC8963A) : 0xB4B4B4;
		veTron(diem, cx, cy, r, vien, bat ? 0x8A5A10 : 0x5A5A5A);
		veTron(diem, cx, cy, r - 2, 0x141414, 0x141414);
		if (bat)
		{
			veTron(diem, cx, cy, r - 3, 0x5ED872, 0x0F5A22);
		}
		else
		{
			veTron(diem, cx, cy, r - 3, 0x6A6A6A, 0x242424);
		}
		UnityEngine.Texture2D t = new UnityEngine.Texture2D(NUT_W, NUT_H, UnityEngine.TextureFormat.RGBA32, false);
		t.filterMode = UnityEngine.FilterMode.Point;
		t.wrapMode = UnityEngine.TextureWrapMode.Clamp;
		t.SetPixels32(diem);
		t.Apply(false, true);
		Image img = new Image();
		img.texture = t;
		img.w = NUT_W;
		img.h = NUT_H;
		return img;
	}

	private static int tronMau(int a, int b, int t, int n)
	{
		if (a == b || n <= 0)
		{
			return a;
		}
		int r = ((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t / n;
		int g = ((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t / n;
		int c = (a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t / n;
		return (r << 16) | (g << 8) | c;
	}

	/** Nút tròn: viền vàng nhấp nháy + nền xanh khi đang bật, viền bạc + nền xám khi tắt; chữ UP và BẬT/TẮT. */
	public static void veNutUp(MyGraphics graphics)
	{
		if (Canvas.currentScreen != Canvas.gameScr)
		{
			return;
		}
		if (MenuNhanh.bat())
		{
			// pokemon: một Poké Ball nhỏ mép trái + bảng Menu nhanh thay hai cột nút che màn chơi
			MenuNhanh.ve(graphics);
			return;
		}
		AutoGuard.veNut(graphics, nutY(), "Up", AutoUp.dang);
		AutoGuard.veNut(graphics, nutY2(), "Tiêu", AutoVanTieu.dang);
		AutoGuard.veNut(graphics, nutY3(), "PK", AutoPk.danh());
		if (AutoPk.hienNut)
		{
			AutoGuard.veNut(graphics, nutX2(), nutYPhim(0), "Mã PK", AutoPk.maPk ? "Bật" : "Tắt", AutoPk.maPk);
			AutoGuard.veNut(graphics, nutX2(), nutYPhim(1), "Nháy", "Map", false);
			AutoGuard.veNut(graphics, nutX2(), nutYPhim(2), "Bật", "ĐS", false);
			AutoGuard.veNut(graphics, nutX2(), nutYPhim(3), "Dừng", "Hết", false);
			AutoGuard.veNut(graphics, nutX2(), nutYPhim(4), "Đổi", "Pet", false);
		}
	}

	private static void veNut(MyGraphics graphics, int y, string ten, bool bat)
	{
		AutoGuard.veNut(graphics, nutX(), y, ten, bat ? "Bật" : "Tắt", bat);
	}

	private static void veNut(MyGraphics graphics, int x, int y, string ten, string dong2, bool bat)
	{
		int cx = x + NUT_W / 2;
		int cy = y + NUT_H / 2 - 1;
		bool sang = mSystem.currentTimeMillis() / 500 % 2 == 0;
		int loai = bat ? (sang ? 1 : 2) : 0;
		if (anhNut[loai] == null)
		{
			anhNut[loai] = taoAnhNut(bat, sang);
		}
		graphics.drawImage(anhNut[loai], x, y);
		int h = MFont.normalFont[0].getHeight();
		MFont.normalFont[0].drawString(graphics, ten, cx, cy - h + 1, 2);
		MFont.normalFont[0].drawString(graphics, dong2, cx, cy, 2);
	}

	public static void paint(MyGraphics graphics)
	{
		try
		{
			HieuUng.ve(graphics);
		}
		catch (System.Exception)
		{
			// hiệu ứng lỗi thì bỏ qua
		}
		try
		{
			AutoGuard.veNutUp(graphics);
		}
		catch (System.Exception)
		{
			// nút UP lỗi thì bỏ qua
		}
		try
		{
			AutoUp.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
			AutoVanTieu.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
			AutoVanMay.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
			LenThanh.ve(graphics, on ? 4 + AutoUp.caoThanh() : 2);
		}
		catch (Exception)
		{
			// thanh trạng thái auto up lỗi thì bỏ qua
		}
		try
		{
			TienIch.batLuong();
		}
		catch (Exception)
		{
			// thanh trạng thái auto up lỗi thì bỏ qua
		}
		if (!on)
		{
			return;
		}
		try
		{
			PauseMenu.autoPaintStatus(graphics);
		}
		catch (Exception)
		{
			// an overlay problem must never break the game's frame
		}
	}
}
