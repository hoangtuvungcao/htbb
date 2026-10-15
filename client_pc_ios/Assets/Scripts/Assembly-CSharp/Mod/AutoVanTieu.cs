/**
 * Auto vận tiêu (chạy song song với game như AutoUp, gọi mỗi khung hình từ PauseMenu.autoCheDoTick).
 * Vòng: (thiếu Thổ địa phù thì vào nhà mua) -> nhận tiêu NPC -50 -> chưa đúng màu thì đổi NGAY tại
 * chỗ bằng Lệnh bài đổi tiêu cao cấp (116, hết thì mua gói 130 bằng lượng) -> đi 2 (Tiên Du),3,5,6,
 * 7,8,9 -> Biên giới 118 -> trả tiêu NPC -51 -> Thổ địa phù về làng -> lặp.
 * Bật auto lúc đang ở ngoài làng mà có tiêu cạnh mình thì đi tiếp chuyến đó, không về làng.
 * Luật server (xem KPAH real/Char.doInitMonter, doChangeTieu, TraVanTieu, MonsterVantieu):
 *  - tiêu chỉ đi theo khi chủ cách <= 80px mỗi trục, xa hơn thì đứng im -> đi chậm, chờ tiêu;
 *  - qua map tiêu chỉ sang theo khi cách <= 120px lúc gửi lệnh đổi map;
 *  - đổi tiêu chỉ ở làng nhà (cả map clone), tiêu phải cùng map;
 *  - trả tiêu: đứng ô x 51-59, y 16-24 map 118, tiêu cách <= 120px.
 * Thông báo nhận/đổi tiêu đọc qua AutoGuard.autoEatDlg (startOKDlg) để biết màu.
 */
public sealed class AutoVanTieu
{
	private static readonly string[] MAU = new string[] { "Trắng", "Xanh", "Đỏ", "Hoàn mỹ", "Vàng", "Tím" };
	private static readonly int[] DUONG = new int[] { 2, 3, 5, 6, 7, 8, 9, 118 };
	private const int MAP_BIEN_GIOI = 118;
	private const int NPC_NHAN = -50;
	private const int NPC_TRA = -51;
	private const int THO_DIA = 19;
	private const int LENH_CAO_CAP = 116;
	private const int THE_VAN_TIEU = 82;
	private const int SHOP_LENH_CAO_CAP = 130;
	private const int SHOP_THE_VAN_TIEU = 131;

	public static bool dang;
	private static int mauMucTieu = 2;
	private static bool doiTieu = true;
	private static long lucBat;
	private static int buoc;
	private static long luc;
	private static long lucCho;
	private static int lan;
	private static int mau = -1;
	private static int idTieu = -1;
	private static int chuyen;
	private static int doi;
	private static int theDaMua;
	private static bool hetLuong;
	/** Server từ chối đổi tiêu ở chỗ này (vd nhận tiêu ở map 80): đi luôn với màu đang có. */
	private static bool thoiDoi;
	private static bool donTieu;
	private static long lucChoCong;
	private static int soTruoc;
	private static int mapTruoc;
	private static long lucMatTieu;
	private static string trangThai = "";
	// Java: field `thongBao` (C# không cho field trùng tên hàm thongBao(string)) -> đổi thành thongBao_
	private static string thongBao_;
	// đường đi BFS (các góc rẽ) tới vùng đích
	private static bool[] dich;
	private static int[] duong;
	private static int duongPos;
	private static int ketX;
	private static int ketY;
	private static long lucKet;
	private static long lucChoTieu;

	private AutoVanTieu()
	{
	}

	// ------------------------------------------------------------------ menu

	public static void moMenu(int chon)
	{
		mVector vector = new mVector();
		vector.addElement(new Command(dang ? "Tắt vận tiêu" : "Bật vận tiêu")
		{
			action = () =>
			{
				if (dang)
				{
					AutoVanTieu.tat();
				}
				else
				{
					AutoVanTieu.bat();
				}
			}
		});
		vector.addElement(new Command(new System.Text.StringBuilder().Append("Màu: ").Append(MAU[mauMucTieu]).Append(" trở lên").ToString())
		{
			action = () =>
			{
				mauMucTieu = mauMucTieu >= MAU.Length - 1 ? 1 : mauMucTieu + 1;
				AutoVanTieu.moMenu(1);
			}
		});
		vector.addElement(new Command(doiTieu ? "Đổi tiêu: bật" : "Đổi tiêu: tắt")
		{
			action = () =>
			{
				doiTieu = !doiTieu;
				AutoVanTieu.moMenu(2);
			}
		});
		MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
		if (mainChar != null)
		{
			vector.addElement(new Command(new System.Text.StringBuilder().Append("Lệnh bài ").Append(AutoVanTieu.so(LENH_CAO_CAP)).Append(" | Thổ địa ").Append(AutoVanTieu.so(THO_DIA)).ToString())
			{
				action = () =>
				{
					AutoVanTieu.moMenu(3);
				}
			});
		}
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	private static void bat()
	{
		if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null)
		{
			return;
		}
		AutoUp.tat();
		AutoVanMay.tat();
		GameScr.autoFight = false;
		GameScr.saveAutoFight = false;
		dang = true;
		buoc = 0;
		lan = 0;
		mau = -1;
		// KHÔNG xoá idTieu: tắt auto, tự đi tay rồi bật lại giữa đường thì vẫn nhận ra tiêu của mình
		chuyen = 0;
		doi = 0;
		theDaMua = 0;
		hetLuong = false;
		thoiDoi = false;
		donTieu = false;
		thongBao_ = null;
		duong = null;
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
		AutoVanTieu.nha();
		Canvas.menu.showMenu = false;
	}

	private static void dung(string @string)
	{
		AutoVanTieu.tat();
		Canvas.startOKDlg(@string);
	}

	// ------------------------------------------------------------------ thông báo server

	/** Gọi từ AutoGuard.autoEatDlg: true = nuốt hộp thoại (auto tự xử lý). */
	public static bool thongBao(string @string)
	{
		if (!dang || @string == null)
		{
			return false;
		}
		if (@string.IndexOf("tiêu", System.StringComparison.Ordinal) >= 0 || @string.StartsWith("Đã mua", System.StringComparison.Ordinal) || @string.StartsWith("Bạn đã mua", System.StringComparison.Ordinal))
		{
			thongBao_ = @string;
			return true;
		}
		return false;
	}

	private static int mauTrong(string @string)
	{
		for (int i = MAU.Length - 1; i >= 0; --i)
		{
			if (!@string.EndsWith(MAU[i], System.StringComparison.Ordinal)) continue;
			return i;
		}
		return -1;
	}

	// ------------------------------------------------------------------ vòng chính

	public static void tick()
	{
		if (!dang)
		{
			return;
		}
		GameScr gameScr = Canvas.gameScr;
		if (gameScr == null || gameScr.mainChar == null || Canvas.currentScreen != gameScr || Tilemap.type == null || Tilemap.w <= 0)
		{
			AutoVanTieu.nha();
			return;
		}
		MainChar mainChar = gameScr.mainChar;
		// đi vào VÙNG cổng (rộng hơn ô cổng nhiều) game tự bật menu chọn khu (Tiên du / Đông / Tây...):
		// đóng đi, lúc qua cổng auto tự chọn map gốc (PauseMenu.quaCong). Auto không mở NPC nào nên
		// menu lạ chỉ có thể là menu cổng; menu mod (để tắt auto) thì giữ. Muốn can thiệp: bấm phím.
		if (Canvas.menu.showMenu && Canvas.menu.menuItems != PauseMenu.menuMod)
		{
			Canvas.menu.showMenu = false;
		}
		if (Canvas.currentDialog != null || Canvas.menu.showMenu)
		{
			AutoVanTieu.nha();
			trangThai = "tạm dừng";
			return;
		}
		long l = mSystem.currentTimeMillis();
		// bấm phím / chạm màn hình bất kỳ thì dừng (bỏ qua 0.5s đầu: chính cú bấm bật auto)
		if (l - lucBat > 500L && AutoVanTieu.coBam())
		{
			AutoVanTieu.dung(new System.Text.StringBuilder().Append("Đã dừng vận tiêu. Đã đi ").Append(chuyen).Append(" chuyến.").ToString());
			return;
		}
		if (mainChar.hp <= 0)
		{
			AutoVanTieu.nha();
			trangThai = "chờ hồi sinh";
			return;
		}
		if (l < lucCho)
		{
			return;
		}
		switch (buoc)
		{
			case 0:
			{
				// chuẩn bị ở làng
				if (!AutoVanTieu.oLang())
				{
					// đang ngoài làng: có tiêu của mình (hoặc tiêu sát bên) thì đi tiếp chuyến đó
					Actor actor = idTieu >= 0 ? AutoVanTieu.timTieu(gameScr) : null;
					if (actor == null)
					{
						actor = AutoVanTieu.tieuGanNhat(gameScr, mainChar, 120);
					}
					if (actor != null)
					{
						idTieu = ((Monster)actor).idChar;
						lucMatTieu = 0L;
						buoc = 40;
						return;
					}
					buoc = 60;
					return;
				}
				if (AutoVanTieu.so(THO_DIA) < 1)
				{
					buoc = 10;
					return;
				}
				buoc = 20;
				return;
			}
			case 10:
			{
				trangThai = "vào nhà mua Thổ địa phù";
				if (gameScr.isOffLineMap(gameScr.map))
				{
					buoc = 12;
					return;
				}
				CMLocation cMLocation = PauseMenu.autoFindHouseDoor();
				if (cMLocation == null)
				{
					AutoVanTieu.dung("Vận tiêu: hết Thổ địa phù và không thấy cửa nhà để mua. Hãy mua rồi bật lại.");
					return;
				}
				GameScr.oldMap = gameScr.map;
				GameService.gI().changeMap(cMLocation.toM, cMLocation.toX, cMLocation.toY);
				ChangScr.gI().switchToMe();
				Res.removeMonsterTemplet();
				buoc = 11;
				luc = l;
				return;
			}
			case 11:
			{
				if (gameScr.isOffLineMap(gameScr.map))
				{
					buoc = 12;
					lucCho = l + 800L;
					return;
				}
				if (l - luc > 20000L)
				{
					AutoVanTieu.dung("Vận tiêu: không vào được nhà để mua Thổ địa phù.");
				}
				return;
			}
			case 12:
			{
				soTruoc = AutoVanTieu.so(THO_DIA);
				try
				{
					Message message = new Message(24);
					message.writer().writeByte((sbyte)1);
					message.writer().writeByte((sbyte)4);
					message.writer().writeShort((short)THO_DIA);
					message.writer().writeShort((short)5);
					GameService.gI().session.sendMessage(message);
					message.cleanup();
				}
				catch (System.Exception)
				{
					// gửi lỗi thì chờ hết giờ rồi báo
				}
				buoc = 13;
				luc = l;
				return;
			}
			case 13:
			{
				if (AutoVanTieu.so(THO_DIA) > soTruoc || l - luc > 5000L)
				{
					if (AutoVanTieu.so(THO_DIA) <= soTruoc)
					{
						AutoVanTieu.dung("Vận tiêu: không mua được Thổ địa phù (thiếu xu?).");
						return;
					}
					mVector vector = Tilemap.mapchangeLocations;
					CMLocation cMLocation = vector == null || vector.size() == 0 ? null : (CMLocation)vector.elementAt(0);
					if (cMLocation == null)
					{
						AutoVanTieu.dung("Vận tiêu: không tìm thấy cửa ra khỏi nhà.");
						return;
					}
					GameService.gI().changeMap(GameScr.oldMap, cMLocation.toX, cMLocation.toY);
					ChangScr.gI().switchToMe();
					Res.removeMonsterTemplet();
					buoc = 14;
					luc = l;
				}
				return;
			}
			case 14:
			{
				if (!gameScr.isOffLineMap(gameScr.map))
				{
					buoc = 0;
					lucCho = l + 1000L;
					return;
				}
				if (l - luc > 20000L)
				{
					AutoVanTieu.dung("Vận tiêu: không ra được khỏi nhà.");
				}
				return;
			}
			case 20:
			{
				trangThai = "nhận tiêu";
				thongBao_ = null;
				mau = -1;
				idTieu = -1;
				thoiDoi = false;
				donTieu = false;
				GameService.gI().doMenuOption(NPC_NHAN, (sbyte)0, 0);
				buoc = 21;
				luc = l;
				return;
			}
			case 21:
			{
				string @string = thongBao_;
				if (@string == null)
				{
					if (l - luc > 6000L)
					{
						if (++lan >= 3)
						{
							AutoVanTieu.dung("Vận tiêu: NPC không trả lời khi nhận tiêu (ngoài giờ 9h-21h?).");
							return;
						}
						buoc = 20;
					}
					return;
				}
				thongBao_ = null;
				lan = 0;
				if (@string.StartsWith("Bạn đã nhận được tiêu", System.StringComparison.Ordinal))
				{
					mau = AutoVanTieu.mauTrong(@string);
					buoc = 22;
					luc = l;
					return;
				}
				if (@string.IndexOf("hết lượt", System.StringComparison.Ordinal) >= 0)
				{
					if (theDaMua >= 2)
					{
						AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: hết lượt hôm nay. Đã đi ").Append(chuyen).Append(" chuyến.").ToString());
						return;
					}
					buoc = 25;
					return;
				}
				if (@string.IndexOf("chưa hoàn thành", System.StringComparison.Ordinal) >= 0)
				{
					// đang có tiêu nhận từ trước (vd trước khi thoát game): chưa biết màu -> mau = -1,
					// bước đổi tiêu sẽ đổi ít nhất 1 lần để server báo màu
					buoc = 22;
					luc = l;
					mau = -1;
					return;
				}
				AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: ").Append(@string).ToString());
				return;
			}
			case 22:
			{
				trangThai = "tìm tiêu";
				Actor actor = idTieu >= 0 ? AutoVanTieu.timTieu(gameScr) : null;
				if (actor == null)
				{
					actor = AutoVanTieu.tieuGanNhat(gameScr, mainChar, 320);
				}
				if (actor != null)
				{
					idTieu = ((Monster)actor).idChar;
					lucMatTieu = 0L;
					// ở làng: đổi màu ngay tại chỗ (server cho đổi khắp làng nhà) rồi mới đi;
					// ngoài làng (vd vào lại game giữa đường): đi tiếp, không đổi
					buoc = AutoVanTieu.oLang() ? 31 : 40;
					return;
				}
				if (l - luc > 6000L)
				{
					AutoVanTieu.dung("Vận tiêu: nhận tiêu xong nhưng không thấy tiêu quanh mình.");
				}
				return;
			}
			case 25:
			{
				// hết lượt: mua + dùng Thẻ vận tiêu (server cho mua 2 thẻ/ngày)
				trangThai = "mua thẻ vận tiêu";
				if (AutoVanTieu.so(THE_VAN_TIEU) > 0)
				{
					GameService.gI().usePotion(THE_VAN_TIEU);
					++theDaMua;
					buoc = 20;
					lucCho = l + 1200L;
					return;
				}
				soTruoc = 0;
				GameService.gI().buyItemShopSpecial(SHOP_THE_VAN_TIEU);
				buoc = 26;
				luc = l;
				return;
			}
			case 26:
			{
				if (AutoVanTieu.so(THE_VAN_TIEU) > 0)
				{
					buoc = 25;
					lucCho = l + 300L;
					return;
				}
				if (l - luc > 5000L)
				{
					AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: hết lượt và không mua được Thẻ vận tiêu. Đã đi ").Append(chuyen).Append(" chuyến.").ToString());
				}
				return;
			}
			case 31:
			{
				// đổi tiêu tới khi đạt màu
				trangThai = "đổi tiêu";
				if (!doiTieu || mau >= mauMucTieu || hetLuong || thoiDoi)
				{
					buoc = 40;
					return;
				}
				if (AutoVanTieu.coTieu(gameScr, mainChar, l) == null)
				{
					return;
				}
				if (AutoVanTieu.so(LENH_CAO_CAP) <= 0)
				{
					soTruoc = 0;
					GameService.gI().buyItemShopSpecial(SHOP_LENH_CAO_CAP);
					buoc = 33;
					luc = l;
					return;
				}
				thongBao_ = null;
				GameService.gI().usePotion(LENH_CAO_CAP);
				++doi;
				buoc = 32;
				luc = l;
				return;
			}
			case 32:
			{
				string @string = thongBao_;
				if (@string == null)
				{
					if (l - luc > 5000L)
					{
						buoc = 31;
					}
					return;
				}
				thongBao_ = null;
				if (@string.StartsWith("Chúc mừng bạn đã đổi được tiêu", System.StringComparison.Ordinal))
				{
					mau = AutoVanTieu.mauTrong(@string);
				}
				else if (@string.StartsWith("Không thể đổi tiêu", System.StringComparison.Ordinal))
				{
					// server trả lại lệnh bài; đổi tiếp chỉ lặp vô hạn -> đi với màu đang có
					thoiDoi = true;
				}
				buoc = 31;
				lucCho = l + 350L;
				return;
			}
			case 33:
			{
				if (AutoVanTieu.so(LENH_CAO_CAP) > 0)
				{
					buoc = 31;
					return;
				}
				if (l - luc > 5000L)
				{
					// không mua được (hết lượng): đi luôn với màu hiện có
					hetLuong = true;
					buoc = 40;
				}
				return;
			}
			case 40:
			{
				// đi tiếp sang map kế trên đường tới Biên giới
				if (PauseMenu.oMap(MAP_BIEN_GIOI))
				{
					AutoVanTieu.datDichVung(52, 17, 58, 23);
					buoc = 50;
					return;
				}
				int n = AutoVanTieu.mapKe();
				if (n < 0)
				{
					AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: map ").Append(gameScr.map).Append(" không nằm trên đường đi tiêu.").ToString());
					return;
				}
				if (!AutoVanTieu.datDichCong(n))
				{
					AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: không thấy cổng sang map ").Append(n).Append(".").ToString());
					return;
				}
				buoc = 41;
				return;
			}
			case 41:
			{
				trangThai = new System.Text.StringBuilder().Append("đi tiêu (map ").Append(gameScr.map).Append(")").ToString();
				Actor actor = AutoVanTieu.coTieu(gameScr, mainChar, l);
				if (actor == null || AutoVanTieu.dangDon(mainChar, actor))
				{
					return;
				}
				int n = AutoVanTieu.di(gameScr, mainChar, l);
				if (n < 0)
				{
					AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: không tìm được đường ở map ").Append(gameScr.map).Append(".").ToString());
					return;
				}
				if (n == 0)
				{
					return;
				}
				// tới sát cổng: chờ tiêu sát (<= 60px, server cho <= 120px) rồi mới qua
				if (!AutoVanTieu.tieuSat(mainChar, actor, 60, l))
				{
					trangThai = "chờ tiêu tới cổng";
					return;
				}
				int n2 = AutoVanTieu.mapKe();
				CMLocation cMLocation = n2 < 0 ? null : PauseMenu.congToi(n2);
				if (cMLocation == null)
				{
					buoc = 40;
					return;
				}
				AutoVanTieu.nha();
				mapTruoc = gameScr.map;
				PauseMenu.quaCong(cMLocation, n2);
				buoc = 42;
				luc = l;
				return;
			}
			case 42:
			{
				if (gameScr.map != mapTruoc)
				{
					lucMatTieu = 0L;
					buoc = 40;
					lucCho = l + 1500L;
					return;
				}
				if (l - luc > 20000L)
				{
					buoc = 40;
				}
				return;
			}
			case 50:
			{
				trangThai = "tới Dịch trạm";
				Actor actor = AutoVanTieu.coTieu(gameScr, mainChar, l);
				if (actor == null || AutoVanTieu.dangDon(mainChar, actor))
				{
					return;
				}
				int n = AutoVanTieu.di(gameScr, mainChar, l);
				if (n < 0)
				{
					AutoVanTieu.dung("Vận tiêu: không tìm được đường tới Dịch trạm.");
					return;
				}
				if (n == 0)
				{
					return;
				}
				if (!AutoVanTieu.tieuSat(mainChar, actor, 90, l))
				{
					trangThai = "chờ tiêu";
					return;
				}
				thongBao_ = null;
				GameService.gI().doMenuOption(NPC_TRA, (sbyte)0, 0);
				buoc = 51;
				luc = l;
				return;
			}
			case 51:
			{
				trangThai = "trả tiêu";
				if (AutoVanTieu.timTieu(gameScr) == null)
				{
					// tiêu biến mất = đã trả xong
					++chuyen;
					idTieu = -1;
					buoc = 60;
					lucCho = l + 1500L;
					return;
				}
				if (thongBao_ != null && thongBao_.StartsWith("Không thể trả tiêu", System.StringComparison.Ordinal))
				{
					thongBao_ = null;
					if (++lan >= 5)
					{
						AutoVanTieu.dung("Vận tiêu: trả tiêu không được.");
						return;
					}
					buoc = 50;
					lucCho = l + 1500L;
					return;
				}
				if (l - luc > 8000L)
				{
					buoc = 50;
				}
				return;
			}
			case 60:
			{
				// về làng bằng Thổ địa phù
				if (AutoVanTieu.oLang())
				{
					buoc = 0;
					lan = 0;
					lucCho = l + 1000L;
					return;
				}
				if (AutoVanTieu.so(THO_DIA) <= 0)
				{
					AutoVanTieu.dung(new System.Text.StringBuilder().Append("Vận tiêu: hết Thổ địa phù để về làng. Đã đi ").Append(chuyen).Append(" chuyến.").ToString());
					return;
				}
				trangThai = "về làng";
				AutoVanTieu.nha();
				gameScr.doUsePotion(THO_DIA);
				buoc = 61;
				luc = l;
				return;
			}
			case 61:
			{
				trangThai = "về làng";
				if (AutoVanTieu.oLang())
				{
					buoc = 0;
					lan = 0;
					lucCho = l + 1500L;
					return;
				}
				if (l - luc > 30000L)
				{
					buoc = 60;
				}
				return;
			}
		}
	}

	// ------------------------------------------------------------------ tiêu

	private static bool laTieu(Actor actor)
	{
		return actor is MonsterNew && ((MonsterNew)actor).isMonsterVantieu();
	}

	/** Tiêu của mình (theo mã chủ tiêu idChar lấy lúc nhận). */
	private static Actor timTieu(GameScr gameScr)
	{
		mVector vector = gameScr.actors;
		for (int i = 0; i < vector.size(); ++i)
		{
			Actor actor = (Actor)vector.elementAt(i);
			if (!AutoVanTieu.laTieu(actor) || ((Monster)actor).idChar != idTieu) continue;
			return actor;
		}
		return null;
	}

	/** Tiêu gần mình nhất (<= n px) - dùng khi chưa biết mã chủ tiêu của mình (vừa nhận / bật lại giữa đường). */
	private static Actor tieuGanNhat(GameScr gameScr, MainChar mainChar, int n)
	{
		Actor actor = null;
		mVector vector = gameScr.actors;
		for (int i = 0; i < vector.size(); ++i)
		{
			Actor actor2 = (Actor)vector.elementAt(i);
			if (!AutoVanTieu.laTieu(actor2) || ((Monster)actor2).idChar < 0) continue;
			int n2 = Util.distance(mainChar.x, mainChar.y, actor2.x, actor2.y);
			if (n2 > n) continue;
			n = n2;
			actor = actor2;
		}
		return actor;
	}

	/** Tiêu đang ở cùng map; mất tiêu > 10s thì coi như tiêu đã bị cướp/hết giờ -> về làng. */
	private static Actor coTieu(GameScr gameScr, MainChar mainChar, long l)
	{
		Actor actor = AutoVanTieu.timTieu(gameScr);
		if (actor != null)
		{
			lucMatTieu = 0L;
			return actor;
		}
		AutoVanTieu.nha();
		trangThai = "không thấy tiêu";
		if (lucMatTieu == 0L)
		{
			lucMatTieu = l;
		}
		else if (l - lucMatTieu > 10000L)
		{
			lucMatTieu = 0L;
			idTieu = -1;
			donTieu = false;
			buoc = 60;
		}
		return null;
	}

	/**
	 * Đứng ở đích chờ tiêu lại sát (<= n px). Tiêu cách > 80px thì server cho đứng im, chờ mãi
	 * không tới -> sau 3s chuyển sang quay lại đón (dangDon). true = tiêu đã sát.
	 */
	private static bool tieuSat(MainChar mainChar, Actor actor, int n, long l)
	{
		if (AutoVanTieu.cach(mainChar, actor) <= n)
		{
			lucChoCong = 0L;
			return true;
		}
		AutoVanTieu.nha();
		if (lucChoCong == 0L)
		{
			lucChoCong = l;
		}
		else if (l - lucChoCong > 3000L)
		{
			lucChoCong = 0L;
			donTieu = true;
		}
		return false;
	}

	/** Đang quay lại đón tiêu: đi thẳng về phía tiêu tới khi sát (<= 32px) rồi tìm lại đường. */
	private static bool dangDon(MainChar mainChar, Actor actor)
	{
		if (!donTieu)
		{
			return false;
		}
		if (AutoVanTieu.cach(mainChar, actor) <= 32)
		{
			donTieu = false;
			duong = null;
			AutoVanTieu.nha();
			return false;
		}
		trangThai = "quay lại đón tiêu";
		AutoVanTieu.giuPhim(actor.x - mainChar.x, actor.y - mainChar.y);
		return true;
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

	private static int cach(MainChar mainChar, Actor actor)
	{
		return Math.max(Math.abs(mainChar.x - actor.x), Math.abs(mainChar.y - actor.y));
	}

	// ------------------------------------------------------------------ đường

	private static bool oLang()
	{
		return Tilemap.lv == 0 || Tilemap.lv == 70;
	}

	/** Map kế tiếp trên đường làng -> Tiên Du -> ... -> Biên giới; -1 nếu map hiện tại không thuộc đường. */
	private static int mapKe()
	{
		if (AutoVanTieu.oLang())
		{
			return DUONG[0];
		}
		for (int i = 0; i < DUONG.Length - 1; ++i)
		{
			if (!PauseMenu.oMap(DUONG[i])) continue;
			return DUONG[i + 1];
		}
		return -1;
	}

	private static bool datDichCong(int n)
	{
		bool[] blArray = PauseMenu.dichSatCong(n);
		dich = blArray;
		duong = null;
		lucKet = 0L;
		lucChoTieu = 0L;
		return blArray != null;
	}

	private static void datDichVung(int n, int n2, int n3, int n4)
	{
		bool[] blArray = new bool[Tilemap.w * Tilemap.h];
		for (int i = n2; i <= n4; ++i)
		{
			for (int j = n; j <= n3; ++j)
			{
				if (PauseMenu.laTuong(j, i)) continue;
				blArray[i * Tilemap.w + j] = true;
			}
		}
		dich = blArray;
		duong = null;
		lucKet = 0L;
		lucChoTieu = 0L;
	}

	/**
	 * Đi theo đường BFS tới vùng dich, giữ tiêu bám theo: tiêu cách > 56px (server: > 80px là
	 * tiêu đứng im) thì đứng chờ; chờ > 6s mà tiêu không lại gần thì quay lại đón tiêu.
	 * 1 = tới nơi, 0 = đang đi/chờ, -1 = không có đường.
	 */
	private static int di(GameScr gameScr, MainChar mainChar, long l)
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
			AutoVanTieu.nha();
			return 1;
		}
		Actor actor = AutoVanTieu.timTieu(gameScr);
		if (actor != null && AutoVanTieu.cach(mainChar, actor) > 56)
		{
			AutoVanTieu.nha();
			if (lucChoTieu == 0L)
			{
				lucChoTieu = l;
			}
			if (l - lucChoTieu > 6000L)
			{
				// tiêu kẹt: quay lại đón
				AutoVanTieu.giuPhim(actor.x - mainChar.x, actor.y - mainChar.y);
				duong = null;
				if (AutoVanTieu.cach(mainChar, actor) <= 32)
				{
					lucChoTieu = 0L;
				}
			}
			return 0;
		}
		lucChoTieu = 0L;
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
		AutoVanTieu.giuPhim(Math.abs(n6) > n8 ? n6 : 0, Math.abs(n6) > n8 ? 0 : n7);
		if (lucKet == 0L || mainChar.x != ketX || mainChar.y != ketY)
		{
			lucKet = l;
			ketX = mainChar.x;
			ketY = mainChar.y;
		}
		else if (l - lucKet > 2000L)
		{
			lucKet = 0L;
			duong = null;
		}
		return 0;
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

	private static int so(int n)
	{
		MainChar mainChar = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
		return mainChar == null || n >= mainChar.potions.Length ? 0 : mainChar.potions[n];
	}

	// ------------------------------------------------------------------ thanh trạng thái

	public static void ve(MyGraphics graphics, int n)
	{
		if (!dang || MFont.normalFont == null || MFont.normalFont[0] == null)
		{
			return;
		}
		string @string = new System.Text.StringBuilder().Append("Tiêu ").Append(mau < 0 ? "?" : MAU[mau]).Append(": chuyến ").Append(chuyen).Append(" | đổi ").Append(doi).Append(" | ").Append(trangThai).ToString();
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
		graphics.setColor(0x2E1A0B);
		graphics.fillRect(n4, n, n2, n3);
		graphics.setColor(0xE08040);
		graphics.drawRect(n4, n, n2 - 1, n3 - 1);
		font.drawString(graphics, @string, Canvas.w / 2, n + 3, 2);
	}
}
