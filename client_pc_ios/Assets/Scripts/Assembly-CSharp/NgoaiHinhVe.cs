
/// <summary>
/// Ve NGOAI HINH TRANG BI quanh than Pokemon (HTBB docs/thiet-ke-game.md), ban PC cua
/// game.pokemon.NgoaiHinhVe (jar):
/// ban 2 "it ma dep":
///  - TOI DA 2 phu kien: do doi dau (mu / no / vuong mien) + MOT mon "noi bat" nhat trong khan / vong / do cam /
///    sau lung (do hiem roi bac cao nhat). Da he, kinh khong ve. Loai ngoai le (phukien.bytes) cam tung o.
///  - Khong ve len mat: mu tren dinh dau; do o co giua diem dau va tam than, chi ve TRUOC than khi nhin xuong;
///    co qua ngan thi bo. Be ngang do o co toi da 36% be ngang Pokemon (khong vua: ban nho, van khong: bo), mu
///    55%; loai to (rong &gt;= 46 hoac cao &gt;= 64 px) ve gap doi.
///  - Vong sang duoi chan: elip mem alpha thap theo bac tot nhat (bo 7 / 11 mon sang hon). Vien sang chi o +15;
///    hat lap lanh chi +13 (1 hat) / +15 (2 hat), bay o hai ben than.
/// Phu kien dat theo diem neo cua tung khung (PokeBody.neo); loai khong co tep neo thi chi ve phan sang.
/// Khong cap phat moi khung hinh: anh phu kien / elip / vien sang / trang phuc nho trong bo dem (khoa so).
///
/// tick vao la tick game Unity (50/giay, nhu PokeBody); hieu ung chop / quay / bay dung tick/2 de cung toc
/// do voi ban jar (25/giay). Diem neo va vien dung tick goc (cung khung voi than).
/// </summary>
public static class NgoaiHinhVe
{
	/// <summary>Ve ngoai hinh cua NGUOI KHAC (cai dat phia client "Phụ kiện người khác", RMS cua NgoaiHinhMenu).</summary>
	public static bool hien = true;

	private static readonly int[] NEO = new int[8];

	/// <summary>Thu tu uu tien mon "noi bat" khi bang diem: khan, do cam, vong, sau lung.</summary>
	private static readonly int[] UU_TIEN = { NgoaiHinhCua.KHAN, NgoaiHinhCua.TAY, NgoaiHinhCua.VONG, NgoaiHinhCua.LUNG };

	/// <summary>He so phong phu kien: loai to ve gap doi.</summary>
	private static int heSo(int dex, bool shiny)
	{
		return PokeBody.rongHinh(dex, shiny) >= 46 || PokeBody.caoTrenChan(dex, shiny, 100) >= 64 ? 2 : 1;
	}

	/// <summary>Ban kinh ngang vong sang duoi chan theo be ngang hinh.</summary>
	private static int banKinh(int dex, bool shiny)
	{
		int r = PokeBody.rongHinh(dex, shiny) * 2 / 5;
		return r < 8 ? 8 : r > 30 ? 30 : r;
	}

	/// <summary>Co cua loai cam o (bang ngoai le).</summary>
	private static bool biCam(int dex, int o)
	{
		int nl = PhuKien.ngoaiLe(dex);
		switch (o)
		{
		case NgoaiHinhCua.MU:
			return (nl & PhuKien.NL_MU) != 0;
		case NgoaiHinhCua.KHAN:
		case NgoaiHinhCua.VONG:
			return (nl & PhuKien.NL_CO) != 0;
		case NgoaiHinhCua.TAY:
			return (nl & PhuKien.NL_TAY) != 0;
		case NgoaiHinhCua.LUNG:
			return (nl & PhuKien.NL_LUNG) != 0;
		default:
			return true; // da he, kinh: chi gop vao vong sang (khong che mat)
		}
	}

	/// <summary>Mon "noi bat" thu hai (ngoai mu) se ve, -1 = khong co. On dinh theo trang bi, khong doi theo huong.</summary>
	private static int monThuHai(NgoaiHinhCua n, int dex)
	{
		int chon = -1, diem = -1;
		for (int i = 0; i < UU_TIEN.Length; i++)
		{
			int o = UU_TIEN[i];
			if (n.kieu[o] <= 0 || biCam(dex, o))
			{
				continue;
			}
			int d = n.hiem[o] * 16 + n.bac[o];
			if (d > diem)
			{
				diem = d;
				chon = o;
			}
		}
		return chon;
	}

	public static void veSau(MyGraphics g, NgoaiHinhCua n, int dex, bool shiny, int x, int y, int yChan, int dir, int dongTac, int tick)
	{
		if (n == null)
		{
			return;
		}
		int t = tick / 2;
		// vong sang mem duoi chan (moi bac >= 1; bo du 11 mon sang hon)
		if (n.bacTot >= 1)
		{
			int rw = banKinh(dex, shiny), rh = rw / 3 + 1;
			int mau = n.bacTot >= 10 ? 0xFFD860 : PhuKien.mauBac(n.bacTot, t);
			int a = 40 + n.bacTot * 4 + (n.bo >= 11 ? 30 : n.bo >= 7 ? 12 : 0);
			Image e = PhuKien.elip(rw, rh, mau, a > 120 ? 120 : a);
			if (e != null)
			{
				g.drawImage(e, x - rw, yChan - rh);
			}
		}
		if (n.congTot >= 15)
		{
			PokeBody.veVien(g, dex, shiny, x, y, dir, dongTac, tick, n.bacTot >= 10 ? PhuKien.cauVong(t / 12) : PhuKien.mauBac(n.bacTot, t));
		}
		veLop(g, n, dex, shiny, x, y, dir, dongTac, tick, PhuKien.SAU);
	}

	public static void veTruoc(MyGraphics g, NgoaiHinhCua n, int dex, bool shiny, int x, int y, int dir, int dongTac, int tick)
	{
		if (n == null)
		{
			return;
		}
		veLop(g, n, dex, shiny, x, y, dir, dongTac, tick, PhuKien.TRUOC);
		if (n.congTot < 13)
		{
			return;
		}
		int tg = tick / 2;
		int rw = banKinh(dex, shiny);
		int cao = PokeBody.caoTrenChan(dex, shiny, 100);
		int so = n.congTot >= 15 ? 2 : 1;
		int cb = n.bacTot >= 1 ? PhuKien.mauBac(n.bacTot, tg) : 0xFFF2A8;
		for (int i = 0; i < so; i++)
		{
			int t = tg + i * 23;
			int pha = t % 40;
			if (pha >= 28)
			{
				continue; // nghi giua hai lan: thua thot
			}
			int ben = ((t / 40 + i) & 1) == 0 ? -1 : 1;
			int px = x + ben * (rw + 2);
			int py = y - 4 - pha * cao / 40;
			g.setColor(pha < 18 ? 0xFFFFFF : cb);
			g.fillRect(px - 1, py, 3, 1);
			g.fillRect(px, py - 1, 1, 3);
		}
	}

	/// <summary>Ve cac phu kien (toi da 2) cua lop SAU / TRUOC.</summary>
	private static void veLop(MyGraphics g, NgoaiHinhCua n, int dex, bool shiny, int x, int y, int dir, int dongTac, int tick, int lop)
	{
		if (!PhuKien.nap() || !PokeBody.neo(dex, shiny, dir, dongTac, tick, 100, NEO))
		{
			return;
		}
		int k = heSo(dex, shiny);
		int rong = PokeBody.rongHinh(dex, shiny);
		int hai = monThuHai(n, dex);
		// sau than: mon thu hai (neu thuoc lop sau) truoc, roi mu (khong bao gio sau than)
		if (hai >= 0)
		{
			veO(g, n, hai, x, y, dir, k, rong, tick / 2, lop);
		}
		if (n.kieu[NgoaiHinhCua.MU] > 0 && !biCam(dex, NgoaiHinhCua.MU))
		{
			veO(g, n, NgoaiHinhCua.MU, x, y, dir, k, rong, tick / 2, lop);
		}
	}

	private static void veO(MyGraphics g, NgoaiHinhCua n, int o, int x, int y, int dir, int k, int rong, int t, int lop)
	{
		int kieu = n.kieu[o];
		int huong = dir == 0 ? 0 : dir == 1 ? 1 : 2;
		bool lat = dir == 3;
		bool co = o == NgoaiHinhCua.KHAN || o == NgoaiHinhCua.VONG;
		// do o co: be ngang toi da ~1/3 than; mu: toi da ~1/2
		int toiDa = co ? rong * 36 / 100 : rong * 55 / 100;
		PhuKien.Hinh h = PhuKien.hinh(o, kieu, huong, false);
		if (h != null && (h.rong - 2) * k > toiDa)
		{
			h = PhuKien.hinh(o, kieu, huong, true);
			if (h != null && (h.rong - 2) * k > toiDa && co)
			{
				h = null;
			}
		}
		if (h == null)
		{
			return;
		}
		int l = h.lop;
		if (o == NgoaiHinhCua.TAY && (NEO[7] & 2) != 0)
		{
			l = PhuKien.SAU;
		}
		if (l != lop)
		{
			return;
		}
		int px, py;
		switch (h.neo)
		{
		case PhuKien.N_DINH:
			px = NEO[0];
			py = NEO[2];
			break;
		case PhuKien.N_DAU:
			px = NEO[0];
			py = NEO[1];
			break;
		case PhuKien.N_TAY:
			px = NEO[3];
			py = NEO[4];
			break;
		case PhuKien.N_TAM:
			px = NEO[5];
			py = NEO[6];
			break;
		default:
			// co (ngay duoi cam): trung binh cua "ngay duoi diem dau PMD" va "giua dinh dau -> chan"; lech ve tam
			// than theo chieu ngang
			px = NEO[0] + (NEO[5] - NEO[0]) / 2;
			py = (NEO[1] + k + NEO[2] / 2) / 2;
			if (py < NEO[2] + 4 * k)
			{
				py = NEO[2] + 4 * k;
			}
			if (py > NEO[6] + 2 * k)
			{
				py = NEO[6] + 2 * k;
			}
			if (co && py > -3 * k)
			{
				return; // co sat mat dat: loai qua thap, khong ve
			}
			break;
		}
		int dx = h.dx * k, dy = h.dy * k;
		if (lat)
		{
			dx = -dx;
		}
		int ax = lat ? h.rong - 1 - h.ax : h.ax;
		Image img = PhuKien.anh(h, n.hiem[o], lat, k, (t / 8) & 3);
		if (img == null)
		{
			return;
		}
		g.drawImage(img, x + px + dx - ax * k, y + py + dy - h.ay * k);
	}

	/// <summary>Thu tu huong xoay tren the trang bi: xuong, phai, len, trai.</summary>
	private static readonly int[] XOAY = { 0, 3, 1, 2 };

	/// <summary>Huong ve Pokemon tren the trang bi (doi moi 1,5 giay); tick = Canvas.gameTick (50/giay).</summary>
	public static int huongThe(int tick)
	{
		return XOAY[(tick / 2 / 38) & 3];
	}

	/// <summary>Chuoi cua the tom tat, giu lai khi ngoai hinh khong doi (khong ghep chuoi moi khung hinh).</summary>
	private static NgoaiHinhCua theCu;

	private static int theBac = -2, theCong = -1, theBo = -1, theTp = -1;

	private static string dong1, dong2;

	/// <summary>
	/// The tom tat kieu Pokemon (xem trang bi): dai thong tin tren cung o giua bang Trang bi - bac tot nhat
	/// (o mau bac), +N cao nhat, bo trang bi, trang phuc. cx = tam ngang, y = mep tren.
	/// </summary>
	public static void veThe(MyGraphics g, NgoaiHinhCua n, int cx, int y)
	{
		if (n == null)
		{
			return;
		}
		MFont f = MFont.smallFont;
		if (f == null)
		{
			return;
		}
		if (n != theCu || n.bacTot != theBac || n.congTot != theCong || n.bo != theBo || n.trangPhuc != theTp)
		{
			theCu = n;
			theBac = n.bacTot;
			theCong = n.congTot;
			theBo = n.bo;
			theTp = n.trangPhuc;
			dong1 = n.bacTot >= 0 ? PhuKien.tenBac(n.bacTot) + (n.congTot > 0 ? " +" + n.congTot : string.Empty) : "Chưa mặc";
			dong2 = n.bo >= 4 ? "Bộ " + n.bo + "/11" : string.Empty;
			if (n.trangPhuc > 0)
			{
				dong2 = dong2.Length > 0 ? dong2 + " · " + PhuKien.tenTrangPhuc(n.trangPhuc) : PhuKien.tenTrangPhuc(n.trangPhuc);
			}
		}
		int dongCao = f.getHeight();
		if (dongCao <= 0)
		{
			dongCao = 9;
		}
		int w = f.getWidth(dong1) + 14;
		int w2 = dong2.Length > 0 ? f.getWidth(dong2) + 8 : 0;
		if (w2 > w)
		{
			w = w2;
		}
		int h = dong2.Length > 0 ? dongCao * 2 + 2 : dongCao + 2;
		GiaoDien.khoi(g, cx - w / 2, y, w, h, 3, 0x0B1222);
		int mau = n.bacTot >= 0 ? PhuKien.mauBac(n.bacTot, Canvas.gameTick / 2) : 0x7080A0;
		g.setColor(mau);
		g.fillRect(cx - w / 2 + 3, y + (dongCao + 2 - 5) / 2, 5, 5);
		f.drawString(g, dong1, cx + 4, y + 1, MFont.CENTER);
		if (dong2.Length > 0)
		{
			f.drawString(g, dong2, cx, y + 1 + dongCao, MFont.CENTER);
		}
	}
}
