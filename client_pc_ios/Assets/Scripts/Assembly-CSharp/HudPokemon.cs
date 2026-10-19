using UnityEngine;

/// <summary>
/// HUD trận đấu kiểu Pokémon cho bản PC (chế độ Pokémon) - bản PC của các hook "// pokemon:" trong
/// GameScr.paintMainCharInfo / paintQuickSlot / paintFocusActorInfo / QuickSlot.paintO của bản jar.
///  - Khung nhân vật góc trái trên: chân dung Pokémon trong vòng tròn, Lv, % EXP, thanh HP (xanh -> vàng
///    -> đỏ) + số, PP (MP), EXP.
///  - Khung đối thủ góc phải trên: tên, Lv, thanh HP, chân dung.
///  - Thanh chiêu dưới giữa: 5 ô màu hệ + hình chiêu, nhãn phím 1-5, hồi chiêu (phủ tối + số giây),
///    thiếu PP (phủ xanh), chuột rê = viền sáng + thẻ chiêu (tên, hệ, PP, hồi chiêu), bấm chuột = dùng
///    chiêu; 2 chấm bên trái = bộ ô (bấm để đổi); tên chiêu vừa dùng hiện trên thanh.
///  - Tên vùng: viên xanh góc trái dưới.
/// Kích thước theo màn logic PC (cao 600, rộng 800..1400 theo tỉ lệ cửa sổ - ScaleGUI): mọi thứ neo
/// theo mép / tâm ngang nên màn 4:3 / 16:9 / 21:9 đều không che vùng giữa.
/// </summary>
public static class HudPokemon
{
	public const int KHUNG_W = 236;

	public const int KHUNG_H = 62;

	public const int O = 44;

	public const int BUOC = 50;


	public static bool bat()
	{
		return Main.isPC;
	}

	// ------------------------------------------------------------------ chuột

	private static int xChuot()
	{
		return (int)ScaleGUI.scaleX(Input.mousePosition.x);
	}

	private static int yChuot()
	{
		return (int)ScaleGUI.scaleY(Screen.height - Input.mousePosition.y);
	}

	private static bool trong(int x, int y, int x0, int y0, int w, int h)
	{
		return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h;
	}

	// ------------------------------------------------------------------ khung nhân vật

	/// <summary>Thay GameScr.paintMainCharInfo (yLech: chừa chỗ dòng tin server chạy ngang).</summary>
	public static void veKhungChinh(GameScr gs, MyGraphics g, int yLech)
	{
		MainChar mc = gs.mainChar;
		if (mc == null)
		{
			return;
		}
		Canvas.resetTrans(g);
		int x0 = 4;
		int y0 = 4 + yLech;
		int W = KHUNG_W;
		int H = KHUNG_H;
		GiaoDien.khung(g, x0, y0, W, H, 8, GiaoDien.VIEN, GiaoDien.TRANG, 0xE6EEF8, true);
		int cx = x0 + 30;
		int cy = y0 + H / 2;
		if (mc.pokeDex <= 0 || !ChanDungPoke.paintHuyHieu(g, mc.pokeDex, mc.pokeShiny, cx, cy, 52))
		{
			GiaoDien.tron(g, cx, cy, 25, GiaoDien.XANH_NHAT, GiaoDien.VIEN);
			Res.getCharPartInfo(2, mc.currentHead).paintStatic(g, (short)cx, (short)(cy + 4), 0, mc.frame);
			Canvas.resetTrans(g);
		}
		int xb = x0 + 62;
		int wb = W - (xb - x0) - 8;
		MFont.blackFont.drawString(g, "Lv " + mc.level, xb, y0 + 5, MFont.LEFT);
		MFont.arialFont.drawString(g, mc.getPercent(), x0 + W - 8, y0 + 5, MFont.RIGHT);
		GiaoDien.thanh(g, xb, y0 + 24, wb, 10, mc.hp, mc.maxhp, 0);
		GiaoDien.thanh(g, xb, y0 + 36, wb, 8, mc.mp, mc.maxmp, 1);
		GiaoDien.thanh(g, xb, y0 + 46, wb, 6, mc.lvpercent, 1000, 2);
		// số HP / PP nhỏ ngay dưới khung
		MFont.smallFont.drawString(g, "HP " + mc.hp + "/" + mc.maxhp + "   PP " + mc.mp + "/" + mc.maxmp, x0 + 6, y0 + H + 4, MFont.LEFT);
		int yDuoi = y0 + H + 18;
		if (mc.killer > 0)
		{
			MFont.smallFont.drawString(g, "PK " + mc.killer, x0 + 6, yDuoi, MFont.LEFT);
			yDuoi += 12;
		}
		if (mc.delay >= 0)
		{
			long[] t = mc.getTime();
			MFont.normalFont[0].drawString(g, t[0] + " : " + t[1], x0 + 6, yDuoi, MFont.LEFT);
			yDuoi += 12;
		}
		string vang = mc.getGoldTime();
		if (!string.IsNullOrEmpty(vang))
		{
			MFont.normalFont[0].drawString(g, vang, x0 + 6, yDuoi, MFont.LEFT);
			MFont.smallFontColor[0].drawString(g, Char.goldTime, x0 + 6, yDuoi + 14, MFont.LEFT);
		}
		// điểm tiềm năng / kỹ năng chưa dùng: viên vàng nhấp nháy cạnh khung
		if (Canvas.gameTick % 10 > 3)
		{
			int xv = x0 + W + 6;
			if (mc.basePointLeft > 0)
			{
				GiaoDien.vien(g, xv, y0 + 4, 74, 16, GiaoDien.LA, "+Tiềm năng");
			}
			if (mc.skillPointLeft > 0)
			{
				GiaoDien.vien(g, xv, y0 + 24, 74, 16, GiaoDien.XANH, "+Chiêu");
			}
		}
	}

	// ------------------------------------------------------------------ khung đối thủ

	/// <summary>Thay GameScr.paintFocusActorInfo.</summary>
	public static void veKhungDich(GameScr gs, MyGraphics g, int yLech)
	{
		Actor a = gs.focusedActor;
		if (a == null || gs.isAutoRangeWhenAuto)
		{
			return;
		}
		Canvas.resetTrans(g);
		LiveActor la = null;
		bool coMau = false;
		if (a.catagory == 0 || a.catagory == 1)
		{
			la = (LiveActor)a;
			coMau = la.maxhp != 0 && la.idBoss == -1;
		}
		bool coTen = a.catagory != Actor.CAT_MY_GROUND && a.catagory != Actor.CAT_MY_TREE && a.catagory != 10 && a.catagory != 11 && a.catagory != 12;
		if (!coTen && !coMau)
		{
			return;
		}
		int W = KHUNG_W;
		int H = coMau ? KHUNG_H : 40;
		// góc phải trên, neo cạnh trái bản đồ nhỏ (bản đồ nhỏ của PC ở góc phải) - màn rộng vẫn sát mép phải
		int x0 = Canvas.w - 4 - W - (gs.isBatMiniMap ? GameScr.wMiniMap + 6 : 0);
		if (x0 < Canvas.hw + 8)
		{
			x0 = Canvas.hw + 8;
		}
		int y0 = 4 + yLech;
		GiaoDien.khung(g, x0, y0, W, H, 8, GiaoDien.VIEN, GiaoDien.TRANG, 0xF8E8E4, true);
		int cx = x0 + W - 30;
		int cy = y0 + H / 2;
		int dex = 0;
		bool shiny = false;
		if (a is Monster)
		{
			Monster m = (Monster)a;
			if (m.monster_type >= 0 && m.monster_type < MonsterTemplate.pokeDex.Length)
			{
				dex = MonsterTemplate.pokeDex[m.monster_type];
				shiny = MonsterTemplate.pokeShiny[m.monster_type];
			}
		}
		else if (a is Char)
		{
			dex = ((Char)a).pokeDex;
			shiny = ((Char)a).pokeShiny;
		}
		if (dex <= 0 || !ChanDungPoke.paintHuyHieu(g, dex, shiny, cx, cy, coMau ? 52 : 36))
		{
			GiaoDien.tron(g, cx, cy, coMau ? 25 : 17, 0xF8D8D0, GiaoDien.VIEN);
			a.paintCorner(g, cx, cy + 10);
			Canvas.resetTrans(g);
		}
		if (coTen)
		{
			string ten = GiaoDien.catVua(MFont.blackFont, a.getDisplayName() ?? string.Empty, W - 72);
			MFont.blackFont.drawString(g, ten, x0 + 10, y0 + 5, MFont.LEFT);
		}
		if (coMau)
		{
			long hp = la.hp <= 0 ? 0 : la.hp;
			GiaoDien.thanh(g, x0 + 10, y0 + 26, W - 72, 10, hp, la.maxhp, 0);
			string so = (la.hp > 0 ? la.hp : 1) + "/" + la.maxhp;
			MFont.smallFont.drawString(g, so, x0 + 10, y0 + 40, MFont.LEFT);
			MFont.smallFontColor[2].drawString(g, "Lv " + la.level, x0 + W - 62, y0 + 40, MFont.RIGHT);
			// hệ của đối thủ (biết theo số Pokédex): viên nhỏ cạnh tên
			if (dex > 0)
			{
				int he = HePoke.he1(dex);
				int w = KieuO.rongVienHe(he);
				if (W - 72 - MFont.blackFont.getWidth(a.getDisplayName() ?? string.Empty) > w + 6)
				{
					KieuO.vienHe(g, x0 + W - 62 - w, y0 + 4, he);
				}
			}
		}
	}

	// ------------------------------------------------------------------ thanh chiêu

	public static int xThanh()
	{
		return Canvas.hw - (BUOC * 5) / 2;
	}

	public static int yThanh()
	{
		return Canvas.h - O - 14;
	}

	private static int oDuoiChuot = -1;

	/// <summary>Thay GameScr.paintQuickSlotPc.</summary>
	public static void veThanhChieu(GameScr gs, MyGraphics g)
	{
		MainChar mc = gs.mainChar;
		if (mc == null || MainChar.quickSlot == null)
		{
			return;
		}
		Canvas.resetTrans(g);
		int x0 = xThanh();
		int y0 = yThanh();
		// đế thanh
		GiaoDien.khung(g, x0 - 22, y0 - 6, BUOC * 5 + 30, O + 12, 10, GiaoDien.VIEN, 0x24508A, 0x14305A, true);
		int lop = mc.clazz;
		long now = mSystem.currentTimeMillis();
		int kMoi = -1;
		long tMoi = 0;
		int xm = xChuot();
		int ym = yChuot();
		oDuoiChuot = -1;
		for (int k = 0; k < 5; k++)
		{
			QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][k];
			int x = x0 + k * BUOC + (BUOC - O) / 2;
			veO(g, mc, qs, x, y0, O, O, null, k == 2);   // không nhãn phím trên ô (phím tắt: trang hướng dẫn)
			if (trong(xm, ym, x, y0, O, O) && Canvas.currentDialog == null && !Canvas.menu.showMenu)
			{
				oDuoiChuot = k;
				KieuO.reO(g, x, y0, O, O);
			}
			if (qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL)
			{
				int sk = qs.getSkillType();
				if (sk >= 0 && sk < mc.timeLastUseSkills.Length)
				{
					long t = mc.timeLastUseSkills[sk];
					if (t > tMoi && now - t < 1300)
					{
						tMoi = t;
						kMoi = k;
					}
				}
			}
		}
		// bộ ô đang dùng: 2 chấm bên trái
		for (int k = 0; k < 2; k++)
		{
			GiaoDien.tron(g, x0 - 12, y0 + O / 2 - 6 + k * 12, 4, k == GameScr.indexTabSlot ? GiaoDien.VANG : 0x5E7AA8, GiaoDien.VIEN);
		}
		// tên chiêu vừa dùng: viên màu hệ ngay trên thanh
		if (kMoi >= 0)
		{
			QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][kMoi];
			int sk = qs.getSkillType();
			string ten = PokeChieu.ten(lop, sk);
			if (ten != null)
			{
				MFont f = MFont.borderFont;
				int wt = f.getWidth(ten) + 20;
				int mau = KieuO.mauHe(PokeChieu.he(lop, sk));
				int yt = y0 - 34;
				GiaoDien.khung(g, Canvas.hw - wt / 2, yt, wt, 20, 9, GiaoDien.VIEN, mau, GiaoDien.pha(mau, 0, 2, 5), false);
				f.drawString(g, ten, Canvas.hw, yt + 3, MFont.CENTER);
			}
		}
		if (oDuoiChuot >= 0)
		{
			veThe(g, mc, MainChar.quickSlot[GameScr.indexTabSlot][oDuoiChuot], x0 + oDuoiChuot * BUOC + BUOC / 2, y0 - 8);
		}
		// buff đang có (giữ của bản PC gốc): bên phải thanh
		if (gs.indexBuff != -1 && GameScr.timeBuff - mSystem.getCurrentTimeMillis() / 1000 > 0)
		{
			int xb = x0 + BUOC * 5 + 16;
			GiaoDien.khung(g, xb, y0 + 6, 32, 32, 6, GiaoDien.VIEN, 0x24508A, 0x14305A, false);
			GameData.imgSkillIcon.drawFrame(gs.indexBuff, xb + 6, y0 + 12, 0, 0, g);
			MFont.borderFont.drawString(g, (GameScr.timeBuff - mSystem.getCurrentTimeMillis() / 1000) + "s", xb + 36, y0 + 14, MFont.LEFT);
		}
	}

	/// <summary>Ô skill nhanh kiểu Pokémon (QuickSlot.paintO bản jar).</summary>
	public static void veO(MyGraphics g, MainChar mc, QuickSlot qs, int x, int y, int w, int h, string phim, bool noiBat)
	{
		try
		{
			int loai = qs == null ? -1 : qs.quickslotType;
			long conLai = 0;
			long tong = 1;
			if (loai == QuickSlot.TYPE_SKILL)
			{
				int lop = mc.clazz;
				int sk = qs.getSkillType();
				KieuO.oChieu(g, x, y, w, h, PokeChieu.he(lop, sk), PokeChieu.hinh(lop, sk), false);
				long da = mSystem.currentTimeMillis() - mc.timeLastUseSkills[sk];
				tong = mc.coolDownSkill[sk];
				if (da < tong)
				{
					conLai = tong - da;
				}
				else
				{
					int cap = Char.skillLevelLearnt != null && sk < Char.skillLevelLearnt.Length ? Char.skillLevelLearnt[sk] : 0;
					int mpCan = 0;
					try
					{
						mpCan = SkillManager.getSkillMP(sk, cap, lop);
					}
					catch (System.Exception)
					{
					}
					if (mpCan > 0 && mc.mp < mpCan)
					{
						KieuO.phu(g, x + 1, y + 1, w - 2, h - 2, unchecked((int)0x90101840));
						g.setColor(GiaoDien.MP_XANH);
						g.fillRect(x + 4, y + h - 5, w - 8, 2);
					}
				}
			}
			else if (loai == QuickSlot.TYPE_POTION)
			{
				int pt = qs.getPotionType();
				GiaoDien.khung(g, x, y, w, h, 6, GiaoDien.VIEN, 0x3A72B8, 0x1A3B6E, false);
				Res.paintPotion(g, MainChar.listPotion[pt].index, x + w / 2, y + h / 2, 3);
				KieuO.soLuong(g, x + w - 2, y + h - 2, KieuO.soGon(mc.potions[pt]));
				long da = mSystem.currentTimeMillis() - mc.potionLastTimeUse[pt];
				tong = MainChar.listPotion[pt].delay;
				if (da < tong)
				{
					conLai = tong - da;
				}
			}
			else
			{
				GiaoDien.khoi(g, x, y, w, h, 6, 0x0E1A30);
				KieuO.phu(g, x + 1, y + 1, w - 2, h - 2, unchecked((int)0x80203A66));
				g.setColor(0x6F86AE);
				g.fillRect(x + w / 2 - 5, y + h / 2, 11, 1);
				g.fillRect(x + w / 2, y + h / 2 - 5, 1, 11);
			}
			if (conLai > 0 && tong > 0)
			{
				int hh = (int)((h - 2) * conLai / tong);
				if (hh < 1)
				{
					hh = 1;
				}
				KieuO.phuCat(g, x + 1, y + 1, w - 2, h - 2, hh, unchecked((int)0xA8000000));
				g.setColor(GiaoDien.VANG);
				g.fillRect(x + 2, y + hh, w - 4, 1);
				long giay = (conLai + 999) / 1000;
				MFont.borderFont.drawString(g, giay.ToString(), x + w / 2, y + (h - MFont.borderFont.getHeight()) / 2, MFont.CENTER);
			}
			if (noiBat)
			{
				g.setColor(GiaoDien.VANG);
				g.drawRect(x - 1, y - 1, w + 1, h + 1);
			}
			if (phim != null)
			{
				KieuO.nhanPhim(g, x - 2, y - 2, phim);
			}
		}
		catch (System.Exception)
		{
			Out.println("ERROR QUICKSLOT POKEMON");
		}
	}

	/// <summary>Thẻ chiêu khi chuột rê lên ô: tên, hệ, cấp, PP, hồi chiêu (đáy thẻ ở yDay, tâm ngang xTam).</summary>
	private static void veThe(MyGraphics g, MainChar mc, QuickSlot qs, int xTam, int yDay)
	{
		if (qs == null || qs.quickslotType != QuickSlot.TYPE_SKILL)
		{
			return;
		}
		int lop = mc.clazz;
		int sk = qs.getSkillType();
		string ten = PokeChieu.ten(lop, sk) ?? string.Empty;
		int he = PokeChieu.he(lop, sk);
		int cap = Char.skillLevelLearnt != null && sk < Char.skillLevelLearnt.Length ? Char.skillLevelLearnt[sk] : 0;
		int mp = 0;
		long cd = 0;
		try
		{
			mp = SkillManager.getSkillMP(sk, cap, lop);
			cd = SkillManager.getSkillCooldown((sbyte)sk, (sbyte)lop, cap);
		}
		catch (System.Exception)
		{
		}
		string dong2 = "Cấp " + cap + "/" + PokeChieu.CAP_TOI_DA + "   PP " + mp + "   Hồi " + (cd + 50) / 100 / 10 + "." + (cd + 50) / 100 % 10 + "s";
		string mt = PokeChieu.moTa(lop, sk);
		MFont f = MFont.arialFontW;
		int wHe = KieuO.rongVienHe(he);
		int w = System.Math.Max(MFont.normalFont[0].getWidth(ten) + wHe + 24, System.Math.Max(f.getWidth(dong2), f.getWidth(mt)) + 16);
		int h = 58;
		int x = xTam - w / 2;
		if (x < 4)
		{
			x = 4;
		}
		if (x + w > Canvas.w - 4)
		{
			x = Canvas.w - 4 - w;
		}
		int y = yDay - h;
		GiaoDien.khung(g, x, y, w, h, 6, GiaoDien.VIEN, 0x1F4682, 0x173662, true);
		MFont.normalFont[0].drawString(g, ten, x + 8, y + 4, MFont.LEFT);
		KieuO.vienHe(g, x + w - 8 - wHe, y + 4, he);
		f.drawString(g, dong2, x + 8, y + 24, MFont.LEFT);
		f.drawString(g, mt, x + 8, y + 39, MFont.LEFT);
	}

	/// <summary>
	/// Chuột trên thanh chiêu (gọi trước khi game xử lý chạm, cùng chỗ AutoGuard.nutUpTick): bấm ô = phím
	/// ô đó (Canvas.keyPressed[1,3,5,7,9]), bấm chấm = đổi bộ ô. Nuốt cú bấm để nhân vật không chạy tới.
	/// </summary>
	public static void tick(GameScr gs)
	{
		if (!bat() || gs == null || Canvas.currentScreen != gs || Canvas.currentDialog != null || Canvas.menu.showMenu || gs.chatMode)
		{
			return;
		}
		int x0 = xThanh();
		int y0 = yThanh();
		bool trenThanh = trong(Canvas.px, Canvas.py, x0 - 22, y0 - 6, BUOC * 5 + 30, O + 12);
		if (Canvas.isPointerDown && trenThanh)
		{
			Canvas.isPointerDown = false;
		}
		if (!Canvas.isPointerClick || !trenThanh)
		{
			return;
		}
		Canvas.isPointerClick = false;
		if (Canvas.px < x0)
		{
			GameScr.indexTabSlot = Canvas.py < y0 + O / 2 ? 0 : 1;
			return;
		}
		int k = (Canvas.px - x0) / BUOC;
		if (k >= 0 && k < 5)
		{
			int phim = gs.QUICKSLOT_KEY[k];
			Canvas.keyPressed[phim] = true;
		}
	}

	/// <summary>Phím số 1-5 trên PC = 5 ô chiêu (giữ G H J K L của bản PC gốc).</summary>
	public static bool phimSo(int keyCode)
	{
		if (!bat() || keyCode < 49 || keyCode > 53)
		{
			return false;
		}
		GameScr gs = Canvas.gameScr;
		if (gs == null || Canvas.currentScreen != gs || Canvas.currentDialog != null || Canvas.menu.showMenu || gs.chatMode)
		{
			return false;
		}
		int i = gs.QUICKSLOT_KEY[keyCode - 49];
		Canvas.keyHold[i] = true;
		Canvas.keyPressed[i] = true;
		return true;
	}

	// ------------------------------------------------------------------ tên vùng

	public static void veTenMap(MyGraphics g, string ten)
	{
		if (string.IsNullOrEmpty(ten))
		{
			return;
		}
		Canvas.resetTrans(g);
		int w = MFont.borderFont.getWidth(ten) + 24;
		GiaoDien.vien(g, 6, Canvas.h - 28, w, 20, GiaoDien.XANH, ten);
	}
}
