// Port từ client_jar/shared/mod/game/render/screen/TuDong.java (bản jar / APK).
using System;


/// <summary>
/// TỰ ĐỘNG ĐÁNH chính thức (thay mod cũ của game gốc: Auto up / PK / Vận may / Lên thành / Giao hàng tự động đã bỏ).
/// MÁY CHỦ quyết định: client chỉ chạy khi server trả "đang bật" (lệnh -100, HTBB com.htbb.pokemon.TuDong /
/// real.TuDongGame); server đếm giờ mỗi ngày, chặn nhịp thao tác như người thật, dừng khi PvP / sàn đấu / Gym /
/// boss / đổi map / ra khỏi vùng / hết giờ. Server cũ không trả lời -100 thì không bao giờ chạy.
///
/// Mọi thao tác đi qua ĐÚNG hàm của game như người chơi bấm tay: GameScr.doUseSkillToFocusActor (tự đi tới tầm,
/// kiểm MP / hồi chiêu, có hoạt ảnh), GameScr.doUsePotion, GameService.getDropableFromGround.
///
/// Lệnh -100 client -> server: byte 0 xin trạng thái / 1 bật (+ short bán kính px) / 2 tắt / 3 nhịp (20 giây).
/// Server -> client: byte 0, byte bật, int giây còn lại hôm nay, int giây tối đa hôm nay, short bán kính tối đa,
/// byte cờ (1 tính năng bật, 2 cho tự uống thuốc, 4 dừng khi PvP, 8 chỉ bãi luyện tập), UTF thông báo.
/// Bảng cài đặt: BangTuDong (kiểu Pokémon). Cài đặt lưu RMS "htbbtd" (cùng định dạng bản jar).
/// </summary>
public static class TuDong
{
	public const sbyte LENH = -100;

	public const int CO_TINH_NANG = 1, CO_THUOC = 2, CO_TAT_PVP = 4, CO_CHI_MAP = 8;

	private const string RMS_TEN = "htbbtd";

	private static readonly int[] BINH_HP = new int[] { 94, 93, 22, 21, 3, 2, 1 };

	private static readonly int[] BINH_MP = new int[] { 96, 95, 24, 23, 6, 5, 4 };

	private static readonly sbyte[][] SKILL_BUFF = new sbyte[][] { new sbyte[] { 4, 5 }, new sbyte[] { 4, 5 }, new sbyte[] { 4, 5, 6, 7 }, new sbyte[] { 4, 5 }, new sbyte[] { 4, 5 } };

	// ---------------------------------------------------------------- trạng thái máy chủ
	public static bool coServer;
	public static bool svBat;
	public static int svConLai;
	public static int svToiDa;
	public static int svBanKinh = 240;
	public static int svCo = CO_TINH_NANG | CO_THUOC | CO_TAT_PVP | CO_CHI_MAP;
	private static long svLuc;
	public static string thongBao = string.Empty;
	public static long lucXin;

	// ---------------------------------------------------------------- đang chạy
	public static bool dang;
	public static bool choBat;
	private static long lucChoBat;
	private static int neoX, neoY, neoMap = -1;
	private static Actor mucTieu, monNhat;
	private static long lucDanh, lucBinh, lucNhip, lucNhuong, lucKet, lucDoi;
	private static int ketX, ketY, lanKet;
	private static bool giuPhim, dangHoiPhuc;
	public static string trangThai = string.Empty;
	public static int soHa;

	// ---------------------------------------------------------------- cài đặt (PlayerPrefs)
	public static readonly sbyte[] thuTu = new sbyte[] { -1, -1, -1, -1 };
	public static int nguongHp = 50;
	public static int nguongMp = 30;
	public static int nhat = 1;
	public static readonly string[] TEN_NHAT = new string[] { "Không nhặt", "Thuốc + xu", "Thuốc, xu, đá", "Tất cả" };
	public static bool veHoiPhuc = true;
	public static int vungO = 10;
	public static readonly int[] VUNG_O = new int[] { 4, 7, 10, 15, 20 };

	/** Gục khi đang tự động: bật = máy chủ được tự dùng vật phẩm hồi sinh (trần tu_dong.hoi_sinh_moi_gio / giờ); tắt = dừng. */
	public static bool tuHoiSinh;

	private static long lucChet;
	private static bool daNap;

	// ================================================================ mạng

	private static void gui(int loai, int banKinh)
	{
		try
		{
			Message m = new Message(LENH);
			m.writer().writeByte((sbyte)loai);
			if (loai == 1)
			{
				m.writer().writeShort((short)banKinh);
				m.writer().writeByte((sbyte)(tuHoiSinh ? 1 : 0));
			}
			GameService.gI().session.sendMessage(m);
			m.cleanup();
		}
		catch (Exception)
		{
			// mất kết nối: lần sau
		}
	}

	public static void xinTrangThai()
	{
		lucXin = mSystem.currentTimeMillis();
		gui(0, 0);
	}

	/// <summary>Gói -100 từ server (đầu MessageHandler.onMessage). true = đã xử lý.</summary>
	public static bool nhan(Message msg)
	{
		if (msg == null || msg.command != LENH)
		{
			return false;
		}
		try
		{
			myReader r = msg.reader();
			r.readByte();
			bool bat = r.readByte() != 0;
			svConLai = r.readInt();
			svToiDa = r.readInt();
			svBanKinh = r.readShort();
			svCo = r.readByte();
			string tb = r.readUTF();
			coServer = true;
			svBat = bat;
			svLuc = mSystem.currentTimeMillis();
			if (!string.IsNullOrEmpty(tb))
			{
				thongBao = tb;
			}
			if (bat && choBat)
			{
				choBat = false;
				batThat();
			}
			else if (!bat)
			{
				bool dangChay = dang || choBat;
				choBat = false;
				if (dang)
				{
					dungTaiCho();
				}
				if (dangChay && !string.IsNullOrEmpty(tb))
				{
					Canvas.startOKDlg(tb);
				}
			}
		}
		catch (Exception)
		{
			// gói hỏng: bỏ
		}
		return true;
	}

	public static int conLai()
	{
		if (!coServer)
		{
			return 0;
		}
		int g = svConLai;
		if (dang)
		{
			g -= (int)((mSystem.currentTimeMillis() - svLuc) / 1000L);
		}
		return g < 0 ? 0 : g;
	}

	public static string gio(int giay)
	{
		int h = giay / 3600;
		int p = giay % 3600 / 60;
		if (h > 0)
		{
			return h + " giờ " + (p < 10 ? "0" : string.Empty) + p + " phút";
		}
		return p + " phút";
	}

	public static bool choThuoc()
	{
		return (svCo & CO_THUOC) != 0;
	}

	// ================================================================ bật / tắt

	public static void batTat()
	{
		if (dang || choBat)
		{
			tat(true);
			return;
		}
		bat();
	}

	private static void bat()
	{
		nap();
		GameScr gs = Canvas.gameScr;
		if (gs == null || gs.mainChar == null)
		{
			return;
		}
		if (gs.mainChar.hp <= 0)
		{
			thongBao = "Pokémon đang gục, hồi sinh rồi bật lại.";
			return;
		}
		if (coServer && (svCo & CO_TINH_NANG) == 0)
		{
			thongBao = "Tự động đánh đang tạm khoá trên máy chủ.";
			return;
		}
		thongBao = string.Empty;
		choBat = true;
		lucChoBat = mSystem.currentTimeMillis();
		gui(1, banKinh());
	}

	public static int banKinh()
	{
		int px = vungO * 16;
		return (svBanKinh > 0 && px > svBanKinh) ? svBanKinh : px;
	}

	private static void batThat()
	{
		GameScr gs = Canvas.gameScr;
		if (gs == null || gs.mainChar == null)
		{
			return;
		}
		dang = true;
		neoMap = gs.map;
		neoX = gs.mainChar.x;
		neoY = gs.mainChar.y;
		mucTieu = null;
		monNhat = null;
		dangHoiPhuc = false;
		soHa = 0;
		lucNhip = mSystem.currentTimeMillis() + 20000L;
		trangThai = "bắt đầu";
		// auto gốc của game (BangAuto) không chạy song song
		GameScr.autoFight = false;
		GameScr.saveAutoFight = false;
		GameScr.autoBomHMP = false;
	}

	public static void tat(bool baoServer)
	{
		bool co = dang || choBat;
		choBat = false;
		dungTaiCho();
		if (co && baoServer)
		{
			gui(2, 0);
		}
	}

	private static void dungTaiCho()
	{
		dang = false;
		nha();
		mucTieu = null;
		monNhat = null;
		dangHoiPhuc = false;
		trangThai = string.Empty;
	}

	// ================================================================ vòng chính

	public static void tick()
	{
		long l = mSystem.currentTimeMillis();
		if (choBat && l - lucChoBat > 6000L)
		{
			choBat = false;
			thongBao = coServer ? "Máy chủ không phản hồi, thử lại." : "Máy chủ chưa hỗ trợ Tự động đánh.";
		}
		if (!dang)
		{
			return;
		}
		GameScr gs = Canvas.gameScr;
		if (gs == null || gs.mainChar == null)
		{
			return;
		}
		MainChar mc = gs.mainChar;
		if (l >= lucNhip)
		{
			lucNhip = l + 20000L;
			gui(3, 0);
		}
		if (mc.hp <= 0)
		{
			if (tuHoiSinh)
			{
				if (lucChet == 0)
				{
					lucChet = l;
				}
				if (l - lucChet < 35000)
				{
					nha();
					mucTieu = null;
					trangThai = "chờ hồi sinh";
					return;
				}
			}
			thongBao = "Pokémon đã gục - Tự động đánh dừng. Hồi sinh rồi bật lại.";
			tat(true);
			return;
		}
		lucChet = 0;
		if (gs.map != neoMap)
		{
			thongBao = "Đã rời bãi luyện tập - Tự động đánh dừng.";
			tat(true);
			return;
		}
		if (Canvas.currentScreen != gs || Canvas.currentDialog != null || Canvas.menu.showMenu || gs.chatMode)
		{
			nha();
			trangThai = "tạm dừng";
			return;
		}
		if (nguoiBam())
		{
			lucNhuong = l + 3000L;
			nha();
		}
		if (l < lucNhuong)
		{
			trangThai = "chờ bạn";
			return;
		}
		binh(gs, mc, l);
		if (hoiPhuc(gs, mc, l))
		{
			return;
		}
		if (nhatDo(gs, mc, l))
		{
			return;
		}
		danh(gs, mc, l);
	}

	private static bool nguoiBam()
	{
		if (Canvas.keyPressed[2] || Canvas.keyPressed[4] || Canvas.keyPressed[6] || Canvas.keyPressed[8])
		{
			return true;
		}
		return Canvas.isPointerDown && !MenuNhanh.trongNut(Canvas.px, Canvas.py) && !AutoGuard.trongNut(Canvas.px, Canvas.py);
	}

	// ---------------------------------------------------------------- thuốc

	private static void binh(GameScr gs, MainChar mc, long l)
	{
		if (l < lucBinh || !choThuoc())
		{
			return;
		}
		if (nguongHp > 0 && mc.maxhp > 0 && (long)mc.hp * 100L < (long)mc.maxhp * nguongHp && uong(gs, mc, BINH_HP, l))
		{
			lucBinh = l + 1500L;
			return;
		}
		if (nguongMp > 0 && mc.maxmp > 0 && (long)mc.mp * 100L < (long)mc.maxmp * nguongMp && uong(gs, mc, BINH_MP, l))
		{
			lucBinh = l + 1500L;
		}
	}

	private static int coBinh(MainChar mc, int[] loai)
	{
		for (int i = 0; i < loai.Length; i++)
		{
			int n = loai[i];
			if (mc.potions != null && n < mc.potions.Length && mc.potions[n] > 0)
			{
				return n;
			}
		}
		return -1;
	}

	private static bool uong(GameScr gs, MainChar mc, int[] loai, long l)
	{
		int n = coBinh(mc, loai);
		if (n < 0)
		{
			return false;
		}
		if (MainChar.listPotion != null && n < MainChar.listPotion.Length && MainChar.listPotion[n] != null && l - mc.potionLastTimeUse[n] < MainChar.listPotion[n].delay)
		{
			return false;
		}
		gs.doUsePotion(n);
		return true;
	}

	/// <summary>HP thấp mà không có / không được dùng thuốc -> về điểm đầu đứng nghỉ tới 80%.</summary>
	private static bool hoiPhuc(GameScr gs, MainChar mc, long l)
	{
		if (!veHoiPhuc || mc.maxhp <= 0)
		{
			dangHoiPhuc = false;
			return false;
		}
		int pt = (int)((long)mc.hp * 100L / mc.maxhp);
		int nguong = nguongHp > 0 ? nguongHp : 40;
		bool hetThuoc = !choThuoc() || nguongHp <= 0 || coBinh(mc, BINH_HP) < 0;
		if (!dangHoiPhuc && pt < nguong && hetThuoc)
		{
			dangHoiPhuc = true;
			mucTieu = null;
			gs.focusedActor = null;
		}
		if (dangHoiPhuc && pt >= 80)
		{
			dangHoiPhuc = false;
		}
		if (!dangHoiPhuc)
		{
			return false;
		}
		if (Util.distance(mc.x, mc.y, neoX, neoY) > 20)
		{
			diToi(mc, neoX, neoY, l);
			trangThai = "về hồi phục";
		}
		else
		{
			nha();
			trangThai = "nghỉ hồi phục " + pt + "%";
		}
		return true;
	}

	// ---------------------------------------------------------------- chọn quái + đánh

	private static bool quaiSong(GameScr gs, Actor a)
	{
		return a != null && a.catagory == 1 && !a.isMineral() && a.state != 8 && a.state != 3 && a.state != 5 && a.hp > 0 && gs.actors.conTains(a);
	}

	private static bool trongVung(Actor a)
	{
		return Util.distance(neoX, neoY, a.x, a.y) <= banKinh();
	}

	private static Actor chonQuai(GameScr gs, MainChar mc)
	{
		Actor best = null;
		int d = int.MaxValue;
		for (int i = 0; i < gs.actors.size(); i++)
		{
			Actor a = (Actor)gs.actors.elementAt(i);
			if (!quaiSong(gs, a) || !trongVung(a))
			{
				continue;
			}
			int k = Util.distance(mc.x, mc.y, a.x, a.y);
			if (k < d)
			{
				d = k;
				best = a;
			}
		}
		return best;
	}

	private static void danh(GameScr gs, MainChar mc, long l)
	{
		if (mucTieu != null && !quaiSong(gs, mucTieu))
		{
			if (mucTieu.hp <= 0 || mucTieu.state == 8 || mucTieu.state == 3)
			{
				soHa++;
			}
			mucTieu = null;
		}
		if (mucTieu != null && !trongVung(mucTieu))
		{
			mucTieu = null;
		}
		if (mucTieu != null && l - lucDoi > 4000L && Util.distance(mc.x, mc.y, mucTieu.x, mucTieu.y) > 60)
		{
			mucTieu = null;
		}
		if (mucTieu == null)
		{
			mucTieu = chonQuai(gs, mc);
			lucDoi = l;
		}
		if (mucTieu == null)
		{
			gs.focusedActor = null;
			if (Util.distance(mc.x, mc.y, neoX, neoY) > 24)
			{
				diToi(mc, neoX, neoY, l);
				trangThai = "về chỗ cũ";
			}
			else
			{
				nha();
				trangThai = "chờ Pokémon hoang dã";
			}
			return;
		}
		nha();
		gs.focusedActor = mucTieu;
		trangThai = "đang đánh";
		if (l < lucDanh || mc.isDoing || mc.state == Char.A_ATTACK)
		{
			return;
		}
		sbyte sk = chonChieu(mc, l);
		if (sk < 0)
		{
			return;
		}
		gs.doUseSkillToFocusActor(sk);
		lucDanh = l + 450L;
	}

	public static sbyte chonChieu(MainChar mc, long l)
	{
		sbyte[] ds = dsChieu(mc);
		for (int i = 0; i < ds.Length; i++)
		{
			sbyte s = ds[i];
			if (s < 0 || Char.skillLevelLearnt == null || s >= Char.skillLevelLearnt.Length || (Char.skillLevelLearnt[s] <= 0 && s != 0))
			{
				continue;
			}
			if (l - mc.timeLastUseSkills[s] <= mc.coolDownSkill[s])
			{
				continue;
			}
			if (SkillManager.getSkillMP(s, Char.skillLevelLearnt[s], mc.clazz) > mc.mp)
			{
				continue;
			}
			return s;
		}
		return -1;
	}

	public static sbyte[] dsChieu(MainChar mc)
	{
		int n = 0;
		for (int i = 0; i < thuTu.Length; i++)
		{
			if (thuTu[i] >= 0)
			{
				n++;
			}
		}
		if (n > 0)
		{
			sbyte[] r = new sbyte[n];
			n = 0;
			for (int i = 0; i < thuTu.Length; i++)
			{
				if (thuTu[i] >= 0)
				{
					r[n++] = thuTu[i];
				}
			}
			return r;
		}
		return chieuDanh(mc);
	}

	/// <summary>Mọi chiêu đánh đã học, mạnh trước (bỏ chiêu buff).</summary>
	public static sbyte[] chieuDanh(MainChar mc)
	{
		int tong = mc == null ? 0 : mc.getnSkill();
		sbyte[] tam = new sbyte[System.Math.Max(0, tong)];
		int n = 0;
		for (int i = tong - 1; i >= 0; i--)
		{
			if (laBuff(mc, i) || Char.skillLevelLearnt == null || i >= Char.skillLevelLearnt.Length || (Char.skillLevelLearnt[i] <= 0 && i != 0))
			{
				continue;
			}
			tam[n++] = (sbyte)i;
		}
		sbyte[] r = new sbyte[n];
		Array.Copy(tam, r, n);
		return r;
	}

	private static bool laBuff(MainChar mc, int s)
	{
		sbyte[] b = SKILL_BUFF[System.Math.Min(System.Math.Max(0, (int)mc.clazz), SKILL_BUFF.Length - 1)];
		for (int i = 0; i < b.Length; i++)
		{
			if (b[i] == s)
			{
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- nhặt đồ

	private static bool nhatDo(GameScr gs, MainChar mc, long l)
	{
		if (nhat <= 0)
		{
			return false;
		}
		if (monNhat != null && (!gs.actors.conTains(monNhat) || ((Dropable)monNhat).isSendGet))
		{
			monNhat = null;
		}
		if (monNhat == null)
		{
			monNhat = chonDo(gs, mc);
		}
		if (monNhat == null)
		{
			return false;
		}
		if (mucTieu != null && quaiSong(gs, mucTieu) && Util.distance(mc.x, mc.y, monNhat.x, monNhat.y) > 40)
		{
			return false;
		}
		if (Util.distance(mc.x, mc.y, monNhat.x, monNhat.y) <= 30)
		{
			nha();
			Dropable d = (Dropable)monNhat;
			GameService.gI().getDropableFromGround(d.catagory, d.ID);
			d.isSendGet = true;
			monNhat = null;
			trangThai = "nhặt đồ";
			return true;
		}
		diToi(mc, monNhat.x, monNhat.y, l);
		trangThai = "đi nhặt";
		return true;
	}

	private static Actor chonDo(GameScr gs, MainChar mc)
	{
		Actor best = null;
		int d = int.MaxValue;
		for (int i = 0; i < gs.actors.size(); i++)
		{
			Actor a = (Actor)gs.actors.elementAt(i);
			if (!(a is Dropable) || !a.isItemCanGet() || ((Dropable)a).isSendGet)
			{
				continue;
			}
			int c = a.catagory;
			bool muon = c == 4 ? nhat >= 1 : (c == 3 ? (nhat >= 3 && !mc.isFullInventory()) : nhat >= 2);
			if (!muon || !trongVung(a))
			{
				continue;
			}
			int k = Util.distance(mc.x, mc.y, a.x, a.y);
			if (k < d)
			{
				d = k;
				best = a;
			}
		}
		return best;
	}

	// ---------------------------------------------------------------- đi bộ (giữ phím ảo, như người bấm)

	private static void diToi(MainChar mc, int x, int y, long l)
	{
		int dx = x - mc.x;
		int dy = y - mc.y;
		int ngang = dx < 0 ? 4 : 6;
		int doc = dy < 0 ? 2 : 8;
		int chinh = System.Math.Abs(dx) >= System.Math.Abs(dy) ? ngang : doc;
		int phu = chinh == ngang ? doc : ngang;
		if (tuongTruoc(mc, chinh) && !tuongTruoc(mc, phu))
		{
			chinh = phu;
		}
		nha();
		Canvas.keyHold[chinh] = true;
		giuPhim = true;
		if (lucKet == 0L || mc.x != ketX || mc.y != ketY)
		{
			lucKet = l;
			ketX = mc.x;
			ketY = mc.y;
			lanKet = 0;
		}
		else if (l - lucKet > 1500L)
		{
			lucKet = 0L;
			nha();
			if (++lanKet >= 2)
			{
				mucTieu = null;
				monNhat = null;
			}
		}
	}

	private static bool tuongTruoc(MainChar mc, int phim)
	{
		int tx = mc.x / 16 + (phim == 4 ? -1 : (phim == 6 ? 1 : 0));
		int ty = mc.y / 16 + (phim == 2 ? -1 : (phim == 8 ? 1 : 0));
		return laTuong(tx, ty);
	}

	public static bool laTuong(int x, int y)
	{
		if (Tilemap.type == null || x < 0 || y < 0 || x >= Tilemap.w || y >= Tilemap.h)
		{
			return true;
		}
		int i = y * Tilemap.w + x;
		if (i >= Tilemap.type.Length)
		{
			return true;
		}
		int t = Tilemap.type[i];
		return t >= 2000000000 || (t & 2) == 2;
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

	// ================================================================ thanh trạng thái

	public static string chuThanh()
	{
		if (choBat)
		{
			return "Tự động: đang hỏi máy chủ...";
		}
		if (!dang)
		{
			return null;
		}
		return "Tự động " + gio(conLai()) + " - " + trangThai;
	}

	public static void ve(MyGraphics g, int y)
	{
		string s = chuThanh();
		if (s != null)
		{
			BangTuDong.veThanh(g, s, y);
		}
	}

	// ================================================================ lưu cài đặt

	public static void nap()
	{
		if (daNap)
		{
			return;
		}
		daNap = true;
		try
		{
			string s = RMS.loadRMSString(RMS_TEN);
			if (s == null || s.Length < 9 || s[0] != '1')
			{
				return;
			}
			for (int i = 0; i < thuTu.Length; i++)
			{
				char c = s[1 + i];
				thuTu[i] = (sbyte)(c == '-' ? -1 : c - 'a');
			}
			nguongHp = (s[5] - 'a') * 10;
			nguongMp = (s[6] - 'a') * 10;
			nhat = System.Math.Min(TEN_NHAT.Length - 1, System.Math.Max(0, s[7] - '0'));
			veHoiPhuc = s[8] == '1';
			if (s.Length >= 10)
			{
				vungO = VUNG_O[System.Math.Min(VUNG_O.Length - 1, System.Math.Max(0, s[9] - '0'))];
			}
			if (s.Length >= 11)
			{
				tuHoiSinh = s[10] == '1';
			}
		}
		catch (Exception)
		{
			// mặc định
		}
	}

	public static void luu()
	{
		try
		{
			System.Text.StringBuilder b = new System.Text.StringBuilder("1");
			for (int i = 0; i < thuTu.Length; i++)
			{
				b.Append(thuTu[i] < 0 ? '-' : (char)('a' + thuTu[i]));
			}
			b.Append((char)('a' + nguongHp / 10)).Append((char)('a' + nguongMp / 10)).Append((char)('0' + nhat)).Append(veHoiPhuc ? '1' : '0');
			int k = 0;
			for (int i = 0; i < VUNG_O.Length; i++)
			{
				if (VUNG_O[i] == vungO)
				{
					k = i;
				}
			}
			b.Append((char)('0' + k)).Append(tuHoiSinh ? '1' : '0');
			RMS.saveRMSString(RMS_TEN, b.ToString());
		}
		catch (Exception)
		{
			// không lưu được: giữ trong phiên
		}
	}
}
