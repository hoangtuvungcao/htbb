using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// Bảng "Ghi công" bản PC (cùng nội dung màn ghi công của jar/APK): đọc Resources/ghicong/GHI_CONG.txt (sinh bởi
/// client_jar/tools/sinh_ghi_cong.py từ ghi công PMDCollab của HTBB). CC BY-NC 4.0 bắt buộc ghi công trong
/// mọi bản phân phối; BuildCli còn chép tệp này cạnh .exe trong zip phát hành.
/// Mở bằng mục "Giấy phép" cuối tab Cài đặt (MenuWindows). Cuộn: con lăn chuột, kéo chuột, phím lên/xuống, PageUp/PageDown.
/// Định dạng: "# ..." = tiêu đề mục, dòng trống = cách đoạn.
/// </summary>
public class GhiCongScr : MyScreen
{
	private static GhiCongScr me;

	private MyScreen truoc;

	private string[] dong;

	private bool[] laTieuDe;

	private int rongDaNgat = -1;

	private int cmy;

	private int cmtoY;

	private int cmyLim;

	private bool dangKeo;

	private int pyKeo;

	private int cmyKeo;

	public static GhiCongScr gI()
	{
		return me ?? (me = new GhiCongScr());
	}

	/// <summary>Mở bảng ghi công; "Đóng" quay về màn đang mở.</summary>
	public static void mo()
	{
		GhiCongScr s = gI();
		if (Canvas.currentScreen != s)
		{
			s.truoc = Canvas.currentScreen;
		}
		s.cmy = (s.cmtoY = 0);
		s.switchToMe();
	}

	public GhiCongScr()
	{
		right = new Command("Đóng");
		right.action = () =>
		{
			MyScreen t = truoc;
			me = null;
			if (t != null)
			{
				t.switchToMe();
			}
			else
			{
				Canvas.loginScr.switchToMe();
			}
		};
	}

	private int x0()
	{
		int w = System.Math.Min(Canvas.w - 24, 620);
		return (Canvas.w - w) / 2;
	}

	private int y0()
	{
		return Canvas.h > 400 ? 24 : 6;
	}

	private int rongKhung()
	{
		return Canvas.w - 2 * x0();
	}

	private int caoKhung()
	{
		return Canvas.h - MyScreen.deltaY - 2 * y0() - 10;
	}

	private static int caoDong()
	{
		return MFont.normalFont[0].getHeight() + 2;
	}

	private static string docTep()
	{
		TextAsset t = Resources.Load<TextAsset>("ghicong/GHI_CONG");
		if (t != null && !string.IsNullOrEmpty(t.text))
		{
			return t.text.Replace("\r", string.Empty);
		}
		return "# Ghi công\nKhông đọc được tệp ghi công trong bản cài.\nXem tệp GHI_CONG.txt cạnh game hoặc trang web của game.";
	}

	private void ngatDong(int rong)
	{
		rongDaNgat = rong;
		List<string> ra = new List<string>();
		List<bool> td = new List<bool>();
		// dòng đầu: phiên bản (người chơi gửi kèm khi báo lỗi)
		ra.Add("Phiên bản " + ThuongHieu.phienBan());
		td.Add(false);
		ra.Add(string.Empty);
		td.Add(false);
		foreach (string d0 in docTep().Split('\n'))
		{
			string d = d0;
			bool laTd = d.StartsWith("# ");
			if (laTd)
			{
				d = d.Substring(2);
				if (ra.Count > 0 && ra[ra.Count - 1].Length > 0)
				{
					ra.Add(string.Empty);
					td.Add(false);
				}
			}
			if (d.Length == 0)
			{
				if (ra.Count > 0 && ra[ra.Count - 1].Length > 0)
				{
					ra.Add(string.Empty);
					td.Add(false);
				}
				continue;
			}
			MFont f = laTd ? MFont.blackFont : MFont.normalFont[0];
			foreach (string p in f.splitFontBStrInLine(d, rong - (laTd ? 14 : 0)))
			{
				ra.Add(p);
				td.Add(laTd);
			}
		}
		dong = ra.ToArray();
		laTieuDe = td.ToArray();
	}

	public override void paint(MyGraphics g)
	{
		Canvas.resetTrans(g);
		if (truoc == null || truoc == Canvas.loginScr || truoc == ServerListScr.me)
		{
			ServerListScr.gI().paintBg(g);
		}
		else
		{
			truoc.paint(g);
		}
		Canvas.resetTrans(g);
		int x = x0();
		int y = y0();
		int w = rongKhung();
		int h = caoKhung();
		GiaoDien.bang(g, x, y, w, h);
		GiaoDien.dai(g, x + 4, y + 4, w - 8, 22, "Giấy phép");
		int noiDungY = y + 32;
		int noiDungH = h - 40;
		int rong = w - 34;
		if (dong == null || rongDaNgat != rong)
		{
			ngatDong(rong);
		}
		int cd = caoDong();
		cmyLim = System.Math.Max(0, dong.Length * cd - noiDungH);
		g.setClip(x + 6, noiDungY, w - 12, noiDungH);
		int dau = System.Math.Max(0, cmy / cd);
		int cuoi = System.Math.Min(dong.Length, dau + noiDungH / cd + 2);
		for (int i = dau; i < cuoi; i++)
		{
			int yd = noiDungY + i * cd - cmy;
			if (laTieuDe[i])
			{
				GiaoDien.khoi(g, x + 12, yd + 3, 5, cd - 6, 1, GiaoDien.DO);
				MFont.borderFont.drawString(g, dong[i], x + 22, yd, 0);
			}
			else if (dong[i].Length > 0)
			{
				MFont.normalFont[0].drawString(g, dong[i], x + 14, yd, 0);
			}
		}
		Canvas.resetTrans(g);
		g.setClip(0, 0, Canvas.w, Canvas.h);
		if (cmyLim > 0)
		{
			int tH = System.Math.Max(14, noiDungH * noiDungH / (cmyLim + noiDungH));
			int tY = noiDungY + (noiDungH - tH) * cmy / cmyLim;
			GiaoDien.khoi(g, x + w - 14, noiDungY, 5, noiDungH, 2, GiaoDien.THAN_DUOI);
			GiaoDien.khoi(g, x + w - 14, tY, 5, tH, 2, GiaoDien.VANG);
		}
		base.paint(g);
	}

	public override void update()
	{
		int cd = caoDong();
		int trang = caoKhung() - 40 - cd;
		if (Canvas.keyHold[8])
		{
			cmtoY += cd;
		}
		if (Canvas.keyHold[2])
		{
			cmtoY -= cd;
		}
		if (Input.GetKeyDown(KeyCode.PageDown) || Input.GetKeyDown(KeyCode.Space))
		{
			cmtoY += trang;
		}
		if (Input.GetKeyDown(KeyCode.PageUp))
		{
			cmtoY -= trang;
		}
		if (Input.GetKeyDown(KeyCode.Home))
		{
			cmtoY = 0;
		}
		if (Input.GetKeyDown(KeyCode.End))
		{
			cmtoY = cmyLim;
		}
		float lan = Input.mouseScrollDelta.y;
		if (lan != 0f)
		{
			cmtoY -= (int)(lan * cd * 3);
		}
		if (Canvas.isPointerDown && Canvas.py < Canvas.h - MyScreen.deltaY * 2)
		{
			if (!dangKeo)
			{
				dangKeo = true;
				pyKeo = Canvas.py;
				cmyKeo = cmy;
			}
			cmtoY = cmyKeo + pyKeo - Canvas.py;
			cmy = cmtoY;
		}
		else
		{
			dangKeo = false;
		}
		cmtoY = System.Math.Max(0, System.Math.Min(cmyLim, cmtoY));
		if (cmy != cmtoY)
		{
			int d = (cmtoY - cmy) / 3;
			cmy += d != 0 ? d : (cmtoY > cmy ? 1 : -1);
		}
		cmy = System.Math.Max(0, System.Math.Min(cmyLim, cmy));
		base.update();
	}
}
