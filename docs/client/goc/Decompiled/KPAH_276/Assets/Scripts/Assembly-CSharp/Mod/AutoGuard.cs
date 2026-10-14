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
		return false;
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

	public static void paint(MyGraphics graphics)
	{
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
