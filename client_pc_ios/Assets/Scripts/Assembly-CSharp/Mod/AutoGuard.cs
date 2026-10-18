// Port từ client_jar/shared/mod/game/render/screen/AutoGuard.java (bản jar / APK).
using System;

/// <summary>
/// Cổng hook của code mod. Sau khi bỏ mod NQSH (Auto up, PK, Vận may, Lên thành, Giao hàng tự động, Chế đồ nhanh,
/// Dòng ẩn, Pidgeot từ xa, tự mua, tự đăng nhập lại...) chỉ còn:
///  - Tự động đánh CHÍNH THỨC (TuDong, máy chủ điều khiển): vòng mỗi khung hình, thanh trạng thái;
///  - Menu nhanh / nút "Tự động" nổi, hiệu ứng (HieuUng), Tiện ích (cửa hàng, đăng xuất, tắt popup);
///  - hai sửa lỗi hiển thị của game gốc (NPC mất khi vào lại cùng map, xe giao hàng biến mất khi đổi màu).
/// Gói -100 xử lý ở đầu MessageHandler.onMessage (TuDong.nhan), như AutoGuard.goiGuard bản jar.
/// </summary>
public sealed class AutoGuard
{
	/** Bảng màu nền thanh trạng thái (KiemServer dùng làm muối xác thực -88 - không đổi). */
	public static readonly int[] MAU_NEN = new int[8] { 0x69436c, 0x45957f, 0xd11674, 0xb24394, 0xf69a31, 0x8c9b09, 0xbcdeeb, 0x8f48ec };

	/** Canvas.startOKDlg: true = nuốt hộp thoại (Tiện ích "Thông báo: tắt"). */
	public static bool autoEatDlg(string @string)
	{
		try
		{
			return TienIch.anThongBao(@string);
		}
		catch (Exception)
		{
			return false;
		}
	}

	/** Chèn ở ĐẦU GameScr.onActorMove: không chặn, chỉ bỏ actor quái đang chết trùng id (xe giao hàng đổi màu). */
	public static bool viTriGuard(sbyte by, short s, short s2, short s3, short s4)
	{
		try
		{
			AutoGuard.boQuaiDangChet(by, s2);
		}
		catch (Exception)
		{
			// không bao giờ cản lệnh di chuyển của game
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

	/** Chèn ở đầu xử lý phím của Canvas (sau MenuNhanh.phim): không còn phím tắt mod. */
	public static bool phimPk(int n)
	{
		return false;
	}

	// ---------------------------------------------------------------- nút "Tự động" nổi (bản không Pokémon)

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

	/** Điểm (x, y) nằm trên nút mở bảng (đang ở màn game). */
	public static bool trongNut(int x, int y)
	{
		if (MenuNhanh.bat())
		{
			return MenuNhanh.trongNut(x, y);
		}
		if (BangTuDong.dangMo)
		{
			return true;
		}
		return Canvas.currentScreen == Canvas.gameScr && x >= nutX() && x < nutX() + NUT_W && y >= nutY() && y < nutY() + NUT_H;
	}

	/** Mỗi khung hình (GameScr.update, trước khi game xử lý chạm): nút / Menu nhanh / bảng + Tự động đánh. */
	public static void tick()
	{
		try
		{
			AutoGuard.nutUpTick();
			TuDong.tick();
		}
		catch (Exception)
		{
			// tự động lỗi không được làm hỏng khung hình của game
		}
	}

	/** Chạm nút thì mở bảng Tự động đánh và nuốt cú chạm (Pokémon không chạy tới chỗ đó). */
	public static void nutUpTick()
	{
		if (Canvas.currentScreen != Canvas.gameScr || Canvas.menu.showMenu || Canvas.currentDialog != null)
		{
			return;
		}
		if (MenuNhanh.bat())
		{
			// pokemon: Menu nhanh (+ bảng Tự động đánh) + thanh chiêu nhận chuột trước game
			MenuNhanh.tick();
			HudPokemon.tick(Canvas.gameScr);
			return;
		}
		if (BangTuDong.dangMo)
		{
			BangTuDong.tick();
			return;
		}
		if (Canvas.isPointerDown && trongNut(Canvas.px, Canvas.py))
		{
			Canvas.isPointerDown = false;
		}
		if (Canvas.isPointerClick && trongNut(Canvas.px, Canvas.py) && trongNut(Canvas.pxLast, Canvas.pyLast))
		{
			Canvas.isPointerClick = false;
			BangTuDong.mo();
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

	/** Nút "Tự động": Menu nhanh (chế độ Pokémon) hoặc một nút tròn; bảng Tự động đánh khi đang mở. */
	public static void veNutUp(MyGraphics graphics)
	{
		if (Canvas.currentScreen != Canvas.gameScr)
		{
			return;
		}
		if (MenuNhanh.bat())
		{
			MenuNhanh.ve(graphics);
			return;
		}
		if (BangTuDong.dangMo)
		{
			BangTuDong.ve(graphics);
			return;
		}
		AutoGuard.veNut(graphics, nutX(), nutY(), "Tự động", TuDong.dang ? "Bật" : "Tắt", TuDong.dang);
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
		catch (Exception)
		{
			// hiệu ứng lỗi thì bỏ qua
		}
		try
		{
			AutoGuard.veNutUp(graphics);
		}
		catch (Exception)
		{
			// nút lỗi thì bỏ qua
		}
		try
		{
			TuDong.ve(graphics, 36);
		}
		catch (Exception)
		{
			// thanh trạng thái lỗi thì bỏ qua
		}
	}
}
