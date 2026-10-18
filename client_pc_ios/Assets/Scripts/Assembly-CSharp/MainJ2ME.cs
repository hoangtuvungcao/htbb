public abstract class MainJ2ME
{
	public Command left;

	public Command center;

	public Command right;

	public static int hTab = 45;

	public static int ITEM_HEIGHT = 40;

	public static int CHAR_HEIGHT = 20;

	private static bool isQwerty;

	public virtual void paint(MyGraphics g)
	{
		int num = GameMidlet.hDrawStringCmd - 2;
		Canvas.resetTrans(g);
		if (UiPokemon.bat())
		{
			// pokemon: giao diện PC / điện thoại - nút lệnh nằm TRONG khung (không còn thanh lệnh kiểu J2ME):
			// lệnh chính (center) góc phải dưới, lệnh phụ cạnh nó, "Đóng / Thoát / Hủy" = nút X góc phải trên,
			// "Trở lại" = nút góc trái trên. Phím mềm (F1 / F2 / Enter) vẫn ánh xạ như cũ (updateKey).
			veNutTrongKhung(g);
			return;
		}
		if (left != null && left.caption != string.Empty)
		{
			if (Canvas.pointCmdBar() != 1)
			{
				g.drawImage(Res.imgButtonBar, 4, Canvas.h - hTab + (hTab - Res.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			else
			{
				g.drawImage(Res.imgButtonBar1, 4, Canvas.h - hTab + (hTab - Res.imgButtonBar1.getHeight()) / 2 + 2, 0);
			}
			MFont.borderFont.drawString(g, left.caption, 4 + Res.imgButtonBar.getWidth() / 2, num + 2, 2);
		}
		if (center != null && center.caption != string.Empty)
		{
			if (Canvas.pointCmdBar() != 2)
			{
				g.drawImage(Res.imgButtonBar, Canvas.w / 2 - Res.imgButtonBar.getWidth() / 2, Canvas.h - hTab + (hTab - Res.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			else
			{
				g.drawImage(Res.imgButtonBar1, Canvas.w / 2 - Res.imgButtonBar1.getWidth() / 2, Canvas.h - hTab + (hTab - Res.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			MFont.borderFont.drawString(g, center.caption, Canvas.hw, num + 2, 2);
		}
		if (right != null && right.caption != string.Empty)
		{
			if (Canvas.pointCmdBar() != 3)
			{
				g.drawImage(Res.imgButtonBar, Canvas.w - Res.imgButtonBar.getWidth() - 4, Canvas.h - hTab + (hTab - Res.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			else
			{
				g.drawImage(Res.imgButtonBar1, Canvas.w - Res.imgButtonBar1.getWidth() - 4, Canvas.h - hTab + (hTab - Res.imgButtonBar.getHeight()) / 2 + 2, 0);
			}
			MFont.borderFont.drawString(g, right.caption, Canvas.w - Res.imgButtonBar.getWidth() / 2 - 4, num + 2, 2);
		}
	}

	// ------------------------------------------------------------------ pokemon: nút lệnh trong khung

	/// <summary>Vùng bấm của 3 lệnh (left, center, right) lần vẽ gần nhất - tọa độ màn logic; w = 0: không có.</summary>
	private readonly int[] vungNut = new int[12];

	private static readonly string[] CHU_DONG = { "đóng", "thoát", "hủy", "huỷ", "bỏ qua", "không", "xóa", "xoá", "close", "exit", "cancel" };

	private static readonly string[] CHU_TRO_LAI = { "trở lại", "quay lại", "trở về", "về", "back" };

	private static bool la(Command c, string[] ds)
	{
		if (c == null || string.IsNullOrEmpty(c.caption))
		{
			return false;
		}
		string s = c.caption.Trim().ToLowerInvariant();
		for (int i = 0; i < ds.Length; i++)
		{
			if (s == ds[i] || s.StartsWith(ds[i] + " "))
			{
				return true;
			}
		}
		return false;
	}

	private static bool co(Command c)
	{
		return c != null && !string.IsNullOrEmpty(c.caption);
	}

	private void datVung(int i, int x, int y, int w, int h)
	{
		vungNut[i * 4] = x;
		vungNut[i * 4 + 1] = y;
		vungNut[i * 4 + 2] = w;
		vungNut[i * 4 + 3] = h;
	}

	private bool trongVung(int i, int px, int py)
	{
		int w = vungNut[i * 4 + 2];
		return w > 0 && px >= vungNut[i * 4] - 2 && px < vungNut[i * 4] + w + 2 && py >= vungNut[i * 4 + 1] - 2 && py < vungNut[i * 4 + 1] + vungNut[i * 4 + 3] + 2;
	}

	private bool dangGiu(int i)
	{
		return Canvas.isPointerDown && trongVung(i, Canvas.px, Canvas.py);
	}

	private void veNutTrongKhung(MyGraphics g)
	{
		for (int i = 0; i < 12; i++)
		{
			vungNut[i] = 0;
		}
		int kx;
		int ky;
		int kw;
		int kh;
		bool khung = UiPokemon.coKhung();
		if (khung)
		{
			kx = UiPokemon.khungX;
			ky = UiPokemon.khungY;
			kw = UiPokemon.khungW;
			kh = UiPokemon.khungH;
		}
		else
		{
			// không có khung (màn chơi...): nút nổi ở hai góc dưới, không vẽ thanh
			kx = 0;
			ky = 0;
			kw = Canvas.w;
			kh = Canvas.h;
		}
		if (this is Menu && khung)
		{
			// menu ngữ cảnh: hàng bấm trực tiếp (Enter = chọn) - chỉ nút X ngoài góc phải trên
			Command dong = co(right) ? right : (co(left) ? left : null);
			if (dong != null)
			{
				int i = dong == right ? 2 : 0;
				int d = 22;
				int x = kx + kw - d + 6;
				int y = ky - 8;
				datVung(i, x, y, d, d);
				UiPokemon.nutDong(g, x, y, d, trongVung(i, UiPokemon.xChuot(), UiPokemon.yChuot()), dangGiu(i));
			}
			return;
		}
		int bh = 28;
		int pad = khung ? 10 : 8;
		int xPhai = kx + kw - pad;
		int yNut = ky + kh - pad - bh;
		// ---- right: X (đóng) góc phải trên, hoặc nút phụ dưới
		bool rDong = co(right) && (la(right, CHU_DONG) || (!co(center) && !co(left)));
		if (co(right) && rDong && khung)
		{
			int d = 24;
			int x = kx + kw - d - 6;
			int y = ky + 4;
			datVung(2, x, y, d, d);
			UiPokemon.nutDong(g, x, y, d, trongVung(2, UiPokemon.xChuot(), UiPokemon.yChuot()), dangGiu(2));
		}
		// ---- center: lệnh chính góc phải dưới
		if (co(center))
		{
			int w = nutRong(center.caption);
			int x = xPhai - w;
			datVung(1, x, yNut, w, bh);
			UiPokemon.nut(g, x, yNut, w, bh, center.caption, UiPokemon.trangThaiNut(x, yNut, w, bh, dangGiu(1) || Canvas.pointCmdBar() == 2));
			xPhai = x - 8;
		}
		// right không phải "đóng" (hoặc không có khung): nút phụ dưới
		if (co(right) && !(rDong && khung))
		{
			int w = nutRong(right.caption);
			int x = xPhai - w;
			datVung(2, x, yNut, w, bh);
			UiPokemon.nut(g, x, yNut, w, bh, right.caption, UiPokemon.trangThaiNut(x, yNut, w, bh, dangGiu(2) || Canvas.pointCmdBar() == 3));
			xPhai = x - 8;
		}
		// ---- left: "Trở lại" góc trái trên, lệnh khác = nút phụ dưới (bên trái nhóm nút)
		if (co(left))
		{
			if (la(left, CHU_TRO_LAI) && khung)
			{
				int w = System.Math.Max(84, MFont.borderFont.getWidth(left.caption) + 30);
				int x = kx + 6;
				int y = ky + 4;
				datVung(0, x, y, w, 22);
				UiPokemon.nutTroLai(g, x, y, w, 22, left.caption, trongVung(0, UiPokemon.xChuot(), UiPokemon.yChuot()), dangGiu(0));
			}
			else
			{
				int w = nutRong(left.caption);
				int x = khung ? kx + pad : xPhai - w;
				if (!khung)
				{
					x = 8; // màn không khung: lệnh trái ở góc trái dưới
				}
				datVung(0, x, yNut, w, bh);
				UiPokemon.nut(g, x, yNut, w, bh, left.caption, UiPokemon.trangThaiNut(x, yNut, w, bh, dangGiu(0) || Canvas.pointCmdBar() == 1));
			}
		}
	}

	private static int nutRong(string chu)
	{
		return System.Math.Max(92, MFont.borderFont.getWidth(chu) + 28);
	}

	/// <summary>Bấm chuột / chạm vào nút trong khung: true = đã xử lý (nuốt cú bấm).</summary>
	private bool bamNutTrongKhung()
	{
		if (!Canvas.isPointerClick || Math.abs(Canvas.pyLast - Canvas.py) > 10 || Math.abs(Canvas.pxLast - Canvas.px) > 10)
		{
			return false;
		}
		if (trongVung(1, Canvas.px, Canvas.py) && center != null && center.action != null)
		{
			Canvas.isPointerClick = false;
			Canvas.endDlg();
			center.perform();
			return true;
		}
		if (trongVung(0, Canvas.px, Canvas.py) && left != null && left.action != null)
		{
			Canvas.isPointerClick = false;
			left.perform();
			return true;
		}
		if (trongVung(2, Canvas.px, Canvas.py) && right != null && right.action != null)
		{
			Canvas.isPointerClick = false;
			right.perform();
			return true;
		}
		return false;
	}

	public virtual void updateKey()
	{
		if (UiPokemon.bat())
		{
			// pokemon: chuột / chạm vào nút trong khung thay cho thanh lệnh đáy
			bamNutTrongKhung();
		}
		else if (Canvas.isPointerClick && Math.abs(Canvas.pyLast - Canvas.py) <= 10 && Math.abs(Canvas.pxLast - Canvas.px) <= 10)
		{
			switch (Canvas.collisionCmdBar())
			{
			case 0:
				if (left != null && left.action != null)
				{
					left.perform();
					Canvas.isPointerClick = false;
				}
				break;
			case 1:
				if (center != null && center.action != null)
				{
					Canvas.endDlg();
					center.perform();
					Canvas.isPointerClick = false;
				}
				break;
			case 2:
				if (right != null && right.action != null)
				{
					right.perform();
					Canvas.isPointerClick = false;
				}
				break;
			}
		}
		if (Canvas.keyPressed[5])
		{
			if (center != null && center.action != null)
			{
				center.perform();
				Canvas.keyPressed[5] = false;
			}
		}
		else if (Canvas.keyPressed[12])
		{
			if (left != null && left.action != null)
			{
				left.perform();
				Canvas.keyPressed[12] = false;
			}
		}
		else if (Canvas.keyPressed[13] && right != null && right.action != null)
		{
			right.perform();
			Canvas.keyPressed[13] = false;
		}
	}
}
