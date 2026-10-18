/**
 * Auto thử vận may hộp vàng 20 lượng (NPC -10 Tổng quản, HTBB real/MapDoMenu.java case -10).
 * Mỗi lượt gửi liền (server xử lý tuần tự): menu 0 mục 2 "Thử vận may" -> menu 2 mục 1 "Hộp vàng"
 * (typeBox = 1) -> menu 3 mục 0 "5 hộp 20l" (trừ 20 lượng, mở bảng hộp) -> lệnh -62 kiểu 2 mở hết hộp.
 * Menu NPC -10 và bảng hộp (MiniGame) hiện ra giữa chừng thì tự đóng; câu báo thưởng đọc qua
 * AutoGuard.autoEatDlg. Dừng khi đủ số lượt, server báo "Không đủ tiền." hoặc bấm phím bất kỳ.
 * Gửi theo đợt: tối đa DOT lượt nối đuôi (4 gói/lượt), mỗi đợt cách nhau >= 1s -> ~20 gói/giây,
 * dưới xa trần chống spam của server (io.BoDemGoiTin: 300 gói / 5s).
 */
public sealed class AutoVanMay
{
	private const int NPC = -10;
	private static readonly int[] SO_LUOT = new int[] { 10, 50, 100, 0 };
	private const int DOT = 5;

	public static bool dang;
	private static int soLuot;
	private static long luc;
	private static long lucDot;
	private static long lucBat;
	private static int daMo;
	private static int trung;
	private static string thuongCuoi = "";
	// Java: field `thongBao` (C# không cho field trùng tên hàm thongBao(string)) -> đổi thành thongBao_
	private static readonly mVector thongBao_ = new mVector();
	private static int choKetQua;
	private static bool hetLuong;

	private AutoVanMay()
	{
	}

	public static void moMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command(dang ? "Tắt vận may" : "Bật vận may (hộp vàng 20l)")
		{
			action = () =>
			{
				if (dang)
				{
					AutoVanMay.tat();
				}
				else
				{
					AutoVanMay.bat();
				}
			}
		});
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Số lượt: ").Append(SO_LUOT[soLuot] == 0 ? "tới khi hết lượng" : SO_LUOT[soLuot].ToString()).ToString())
		{
			action = () =>
			{
				soLuot = (soLuot + 1) % SO_LUOT.Length;
				AutoVanMay.moMenu(1);
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	private static void bat()
	{
		if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null)
		{
			return;
		}
		AutoUp.tat();
		AutoVanTieu.tat();
		dang = true;
		daMo = 0;
		trung = 0;
		thuongCuoi = "";
		thongBao_.removeAllElements();
		choKetQua = 0;
		hetLuong = false;
		lucDot = 0L;
		lucBat = mSystem.currentTimeMillis();
		Canvas.menu.showMenu = false;
	}

	public static void tat()
	{
		if (!dang)
		{
			return;
		}
		dang = false;
		Canvas.menu.showMenu = false;
	}

	private static void dung(string @string)
	{
		AutoVanMay.tat();
		if (Canvas.currentScreen != Canvas.gameScr && Canvas.gameScr != null)
		{
			Canvas.gameScr.switchToMe();
		}
		Canvas.startOKDlg(@string);
	}

	/** Gọi từ AutoGuard.autoEatDlg: nuốt mọi hộp thoại server trong lúc chạy, giữ lại câu cuối. */
	public static bool thongBao(string @string)
	{
		if (!dang || @string == null)
		{
			return false;
		}
		thongBao_.addElement(@string);
		return true;
	}

	/** Gọi từ AutoGuard.goiGuard: bảng hộp (lệnh -62) không mở ra - mở hết hộp ngay bằng lệnh. */
	// C#: gọi từ MessageHandler với msg.command (sbyte) -> tham số sbyte (Java: int)
	public static bool anGoi(sbyte command)
	{
		return dang && command == -62;
	}

	/** Gọi từ AutoGuard.menuGuard: menu của NPC -10 hiện ra giữa chừng thì không hiện. */
	public static bool anMenu(int n)
	{
		return dang && n == NPC;
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
		long l = mSystem.currentTimeMillis();
		// bấm phím / chạm màn hình bất kỳ thì dừng (bỏ qua 0.5s đầu: chính cú bấm bật auto)
		if (l - lucBat > 500L && AutoVanMay.coBam())
		{
			AutoVanMay.dung(new System.Text.StringBuilder().Append("Đã dừng vận may. Đã mở ").Append(daMo).Append(" lượt, trúng ").Append(trung).Append(" lần.").ToString());
			return;
		}
		// đọc hết kết quả đã về
		while (thongBao_.size() > 0)
		{
			string @string = (string)thongBao_.elementAt(0);
			thongBao_.removeElementAt(0);
			if (@string.StartsWith("Không đủ", System.StringComparison.Ordinal))
			{
				hetLuong = true;
				choKetQua = Math.max(0, choKetQua - 1);
				continue;
			}
			string string2 = "Chúc mừng bạn đã nhận được ";
			if (@string.StartsWith(string2, System.StringComparison.Ordinal))
			{
				++trung;
				thuongCuoi = @string.Substring(string2.Length).Trim();
			}
			else if (!@string.StartsWith("Chúc bạn may mắn", System.StringComparison.Ordinal))
			{
				continue;
			}
			++daMo;
			choKetQua = Math.max(0, choKetQua - 1);
			luc = l;
		}
		AutoVanMay.dongBang(gameScr);
		if (choKetQua > 0)
		{
			// còn lượt chưa có kết quả: chờ, quá 5s không thấy gì thì bỏ qua số còn thiếu
			if (l - luc > 5000L)
			{
				choKetQua = 0;
			}
			return;
		}
		if (hetLuong)
		{
			AutoVanMay.dung(new System.Text.StringBuilder().Append("Vận may: hết lượng. Đã mở ").Append(daMo).Append(" lượt, trúng ").Append(trung).Append(" lần.").ToString());
			return;
		}
		int n = SO_LUOT[soLuot];
		if (n > 0 && daMo >= n)
		{
			AutoVanMay.dung(new System.Text.StringBuilder().Append("Vận may: xong ").Append(daMo).Append(" lượt, trúng ").Append(trung).Append(" lần.").ToString());
			return;
		}
		if (l - lucDot < 1000L)
		{
			return;
		}
		int n2 = n > 0 ? Math.min(DOT, n - daMo) : DOT;
		GameService gameService = GameService.gI();
		for (int i = 0; i < n2; ++i)
		{
			gameService.doMenuOption(NPC, (sbyte)0, 2);
			gameService.doMenuOption(NPC, (sbyte)2, 1);
			gameService.doMenuOption(NPC, (sbyte)3, 0);
			// Java: Nen.moRuongVanMay(gameService) (lớp đệm nền tảng) -> bản J2ME gọi thẳng:
			gameService.playMiniGame((sbyte)2, (sbyte)100);
		}
		choKetQua = n2;
		lucDot = l;
		luc = l;
	}

	private static bool coBam()
	{
		for (int i = 0; i < Canvas.keyPressed.Length; ++i)
		{
			if (!Canvas.keyPressed[i]) continue;
			return true;
		}
		return Canvas.isPointerDown || Canvas.isPointerClick;
	}

	private static void dongBang(GameScr gameScr)
	{
		Canvas.menu.showMenu = false;
		// Java: Nen.laManRuong(Canvas.currentScreen) -> bản J2ME: màn MiniGame
		if (Canvas.currentScreen == MiniGame.gI())
		{
			gameScr.switchToMe();
		}
	}

	public static void ve(MyGraphics graphics, int n)
	{
		if (!dang || MFont.normalFont == null || MFont.normalFont[0] == null)
		{
			return;
		}
		string @string = new System.Text.StringBuilder().Append("Vận may: lượt ").Append(daMo).Append(" | trúng ").Append(trung).Append(thuongCuoi.Length > 0 ? new System.Text.StringBuilder().Append(" | ").Append(thuongCuoi).ToString() : "").ToString();
		MFont font = MFont.normalFont[0];
		@string = TienIch.catVua(@string, font);
		int n2 = Math.min(Canvas.w, font.getWidth(@string) + 12);
		int n3 = font.getHeight() + 6;
		int n4 = (Canvas.w - n2) / 2;
		graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
		graphics.setClip(0, 0, Canvas.w, Canvas.h);
		graphics.setColor(0x2E2A0B);
		graphics.fillRect(n4, n, n2, n3);
		graphics.setColor(0xE0D040);
		graphics.drawRect(n4, n, n2 - 1, n3 - 1);
		font.drawString(graphics, @string, Canvas.w / 2, n + 3, 2);
	}
}
