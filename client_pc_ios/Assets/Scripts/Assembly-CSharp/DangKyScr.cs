using System;

/// <summary>
/// Màn "Đăng ký tài khoản" ngay trong game (PC) — bản tương đương j2me game.render.screen.DangKyScr.
/// Quy tắc (độ dài, ký tự, tên cấm, từ cấm, hạn mức) do SERVER kiểm (real.cmd.DangKyTrongGame, chung một
/// tệp quy tắc với web); ở đây chỉ kiểm ô trống và mật khẩu nhập lại.
/// Giao thức lệnh 39 "#dk2": bước 0 xin câu hỏi chống bot, bước 1 gửi; server trả 0 câu hỏi / 1 thành công / 2 lỗi.
/// </summary>
public class DangKyScr : MyScreen
{
	public const string DAU = "#dk2";

	private static DangKyScr me;

	private readonly TField[] o = new TField[4];

	private static readonly string[] NHAN = { "Tài khoản:", "Mật khẩu:", "Nhập lại:", "Email:" };

	private int focus;

	private int loiO = -1;

	private string cauHoi;

	private bool choCauHoi;

	private readonly Command cmdDangKy;

	private const int W = 300;

	private const int BUOC = 40;

	public static DangKyScr gI()
	{
		if (me == null)
		{
			me = new DangKyScr();
		}
		return me;
	}

	public static void mo()
	{
		DangKyScr s = gI();
		string u = (Canvas.loginScr != null) ? Canvas.loginScr.tfUser.getText().ToLower().Trim() : string.Empty;
		if (s.o[0].getText().Length == 0 && u.Length > 0)
		{
			s.o[0].setText(u);
		}
		s.datFocus(0);
		s.switchToMe();
	}

	private DangKyScr()
	{
		for (int i = 0; i < 4; i++)
		{
			TField t = new TField();
			t.height = MyScreen.ITEM_HEIGHT + 2;
			t.width = 180;
			t.showSubTextField = false;
			t.setText(string.Empty);
			o[i] = t;
		}
		o[0].setIputType(TField.INPUT_ALPHA_NUMBER_ONLY);
		o[0].setMaxTextLenght(20);
		o[1].setIputType(TField.INPUT_TYPE_PASSWORD);
		o[1].setMaxTextLenght(20);
		o[2].setIputType(TField.INPUT_TYPE_PASSWORD);
		o[2].setMaxTextLenght(20);
		o[3].setIputType(TField.INPUT_TYPE_ANY);
		o[3].setMaxTextLenght(100);
		cmdDangKy = new Command("Đăng ký");
		cmdDangKy.action = () =>
		{
			guiDangKy();
		};
		center = cmdDangKy;
		left = new Command("Quay lại");
		left.action = () =>
		{
			Canvas.loginScr.switchToMe();
		};
		init();
	}

	private int yDau()
	{
		return Canvas.hh - (26 + 4 * BUOC + 10) / 2;
	}

	public override void init()
	{
		int x = Canvas.hw - W / 2 + 100;
		int y = yDau() + 30;
		for (int i = 0; i < 4; i++)
		{
			o[i].x = x;
			o[i].y = y + i * BUOC;
		}
	}

	private void datFocus(int i)
	{
		focus = i;
		for (int k = 0; k < 4; k++)
		{
			o[k].setFocus(k == i);
		}
		right = o[i].cmdClear;
		center = cmdDangKy;
	}

	/// <summary>Mã máy: SystemInfo.deviceUniqueIdentifier (Canvas.IMEI) — server chỉ dùng để đếm hạn mức.</summary>
	private static string maMay()
	{
		string m = Canvas.IMEI ?? string.Empty;
		if (m.Length == 0)
		{
			m = RMS.loadRMSString("maMay");
			if (string.IsNullOrEmpty(m))
			{
				m = "pc-" + Guid.NewGuid().ToString("N").Substring(0, 16);
				RMS.saveRMSString("maMay", m);
			}
		}
		return m;
	}

	private string ten()
	{
		return o[0].getText().ToLower().Trim();
	}

	private void guiDangKy()
	{
		loiO = -1;
		if (ten().Length == 0)
		{
			baoLoi(0, "Vui lòng nhập tên tài khoản muốn đăng ký.");
			return;
		}
		if (o[1].getText().Length == 0)
		{
			baoLoi(1, "Vui lòng nhập mật khẩu.");
			return;
		}
		if (!o[1].getText().Equals(o[2].getText()))
		{
			baoLoi(2, "Mật khẩu nhập lại không khớp.");
			return;
		}
		if (cauHoi != null && Session_ME.connected)
		{
			hoiCauDo();
			return;
		}
		cauHoi = null;
		choCauHoi = true;
		if (!Session_ME.connected)
		{
			Canvas.connect();
		}
		Canvas.startWaitDlg("Đang kết nối máy chủ...", true);
		GameService.gI().dangKyXinCauDo(maMay());
	}

	private void baoLoi(int oLoi, string loi)
	{
		loiO = oLoi;
		Canvas.startOKDlg(loi, () =>
		{
			Canvas.endDlg();
			if (oLoi >= 0)
			{
				datFocus(oLoi);
			}
		});
	}

	private void hoiCauDo()
	{
		Canvas.inputDlg.setInfo(cauHoi, () =>
		{
			string tl = Canvas.inputDlg.tfInput.getText().Trim();
			if (tl.Length == 0)
			{
				return;
			}
			Canvas.endDlg();
			cauHoi = null;
			Canvas.startWaitDlg("Đang đăng ký...", true);
			GameService.gI().dangKyGui(ten(), o[1].getText(), o[2].getText(), o[3].getText().Trim(), tl, maMay());
		}, TField.INPUT_TYPE_NUMERIC, 3, true);
		Canvas.inputDlg.show();
	}

	private static int oCuaMa(string ma)
	{
		if (ma.StartsWith("ten") || ma == "trungTen")
		{
			return 0;
		}
		if (ma == "matKhauSai")
		{
			return 1;
		}
		if (ma == "matKhauKhongKhop")
		{
			return 2;
		}
		if (ma.StartsWith("email"))
		{
			return 3;
		}
		return -1;
	}

	/// <summary>Gói lệnh 39 dạng mới (sau chuỗi "#dk2"). Gọi từ MessageHandler.</summary>
	public static void nhan(myReader r)
	{
		DangKyScr s = gI();
		sbyte loai = r.readByte();
		if (loai == 0)
		{
			s.cauHoi = r.readUTF();
			if (s.choCauHoi)
			{
				s.choCauHoi = false;
				Canvas.endDlg();
				s.hoiCauDo();
			}
			return;
		}
		if (loai == 1)
		{
			string ten = r.readUTF();
			string tb = r.readUTF();
			r.readBoolean();
			string mk = s.o[1].getText();
			for (int i = 0; i < 4; i++)
			{
				s.o[i].setText(string.Empty);
			}
			s.cauHoi = null;
			Canvas.loginScr.tfUser.setText(ten);
			Canvas.loginScr.tfPass.setText(mk);
			Canvas.loginScr.switchToMe();
			Command dong = new Command("Đóng");
			dong.action = () =>
			{
				Canvas.endDlg();
			};
			Command vao = new Command("Đăng nhập");
			vao.action = () =>
			{
				Canvas.endDlg();
				Canvas.loginScr.doLogin();
			};
			Canvas.msgdlg.isIcon = false;
			Canvas.msgdlg.setInfo("Đăng ký thành công tài khoản " + ten + "! " + tb, dong, vao, null);
			Canvas.msgdlg.show();
			me = null;
			return;
		}
		int n = r.readByte();
		string loi = string.Empty;
		int oDau = -1;
		bool saiCauDo = false;
		for (int j = 0; j < n; j++)
		{
			string ma = r.readUTF();
			loi += (j == 0 ? string.Empty : "\n") + r.readUTF();
			if (oDau < 0)
			{
				oDau = oCuaMa(ma);
			}
			if (ma == "traLoi")
			{
				saiCauDo = true;
			}
		}
		string moi = r.readUTF();
		s.cauHoi = (moi.Length > 0) ? moi : null;
		s.choCauHoi = false;
		bool hoiLai = saiCauDo && s.cauHoi != null && oDau < 0;
		int oLoi = oDau;
		s.loiO = oDau;
		Canvas.startOKDlg(loi, () =>
		{
			Canvas.endDlg();
			if (hoiLai)
			{
				s.hoiCauDo();
			}
			else if (oLoi >= 0)
			{
				s.datFocus(oLoi);
			}
		});
	}

	/// <summary>Mất kết nối khi đang ở màn này: quên câu hỏi, trả true để không bật "Bị ngắt kết nối".</summary>
	public static bool matKetNoi()
	{
		if (me == null || Canvas.currentScreen != me)
		{
			return false;
		}
		me.cauHoi = null;
		if (me.choCauHoi)
		{
			me.choCauHoi = false;
			Canvas.startOKDlg("Mất kết nối với máy chủ. Vui lòng bấm Đăng ký lại.");
		}
		return true;
	}

	public override bool keyPress(int keyCode)
	{
		if (Canvas.currentDialog != null)
		{
			return false;
		}
		if (o[focus].keyPressed(keyCode))
		{
			return true;
		}
		return base.keyPress(keyCode);
	}

	public override void update()
	{
		int truoc = focus;
		for (int i = 0; i < 4; i++)
		{
			o[i].update();
		}
		if (Canvas.currentDialog != null)
		{
		}
		else if (Canvas.isKeyPressed(2))
		{
			datFocus((focus == 0) ? 3 : (focus - 1));
		}
		else if (Canvas.isKeyPressed(8))
		{
			datFocus((focus == 3) ? 0 : (focus + 1));
		}
		else
		{
			int moi = -1;
			for (int i = 0; i < 4; i++)
			{
				if (o[i].isFocus && i != truoc)
				{
					moi = i;
				}
			}
			if (moi >= 0)
			{
				datFocus(moi);
			}
			else if (!o[truoc].isFocus)
			{
				o[truoc].setFocus(true);
			}
		}
		if (Canvas.loginScr != null)
		{
			Canvas.loginScr.updateCamera();
		}
		base.update();
	}

	public override void paint(MyGraphics g)
	{
		ServerListScr.gI().paintBg(g);
		Canvas.resetTrans(g);
		int h = 26 + 4 * BUOC + 10;
		int x = Canvas.hw - W / 2;
		int y = yDau();
		Res.paintDlgDragonFull(g, x, y, W, h);
		GiaoDien.dai(g, x + 4, y + 4, W - 8, 18, "Đăng ký tài khoản");
		for (int i = 0; i < 4; i++)
		{
			TField t = o[i];
			MFont.normalFont[0].drawString(g, NHAN[i], x + 14, t.y + 5, 0);
			if (i == loiO)
			{
				GiaoDien.khoi(g, t.x - 2, t.y - 2, t.width + 5, t.height + 5, 3, GiaoDien.DO);
			}
			t.paint(g);
			g.setClip(0, 0, Canvas.w, Canvas.h);
		}
		MFont.smallFont?.drawString(g, "Email không bắt buộc (dùng để lấy lại mật khẩu).", Canvas.hw, y + h - 12, 2);
		base.paint(g);
	}

	public override void switchToMe()
	{
		base.switchToMe();
		init();
	}
}
