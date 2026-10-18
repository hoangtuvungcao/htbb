/// <summary>
/// Menu "Ngoại hình" (hàng mod của PauseMenu.moMenuMod / tab Khác): ẩn/hiện từng phụ kiện trên người mình và
/// trang phục (gửi -103 lên server, mọi người thấy theo), và "Phụ kiện người khác" (chỉ phía client, RMS
/// "htbbnh") để máy yếu tắt bớt hình. Chỉ đổi HÌNH, không đổi chỉ số. Bản PC của game.pokemon.NgoaiHinhMenu.
/// </summary>
public static class NgoaiHinhMenu
{
	private static readonly string[] TEN_O = { "Mũ / nơ", "Khăn quàng", "Vòng cổ", "Đồ cầm tay", "Ruy băng / áo choàng", "Phiến đá hệ", "Kính" };

	private const string RMS_TEN = "htbbnh";

	private static bool daNap;

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
			if (s != null && s.Length > 0)
			{
				NgoaiHinhVe.hien = s[0] != '0';
			}
		}
		catch (System.Exception)
		{
			// mặc định: hiện
		}
	}

	private static int anHienTai()
	{
		if (Canvas.gameScr == null || Canvas.gameScr.mainChar == null || Canvas.gameScr.mainChar.ngoaiHinh == null)
		{
			return 0;
		}
		return Canvas.gameScr.mainChar.ngoaiHinh.an;
	}

	/// <summary>Gửi mặt nạ ẩn lên server (-103 loại 0). Server gửi lại -102.</summary>
	private static void gui(int an)
	{
		try
		{
			Message m = new Message(-103);
			m.writer().writeByte(0);
			m.writer().writeUnsignedByte((byte)(an & 0xFF));
			GameService.gI().session.sendMessage(m);
			m.cleanup();
			if (Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.ngoaiHinh != null)
			{
				Canvas.gameScr.mainChar.ngoaiHinh.an = an; // menu mở lại hiện ngay
			}
		}
		catch (System.Exception)
		{
			// mất kết nối
		}
	}

	public static void mo(int chon)
	{
		nap();
		int an = anHienTai();
		mVector v = new mVector();
		v.addElement(new Command("Trang phục: " + ((an & NgoaiHinhCua.AN_TRANG_PHUC) != 0 ? "Ẩn" : "Hiện"))
		{
			action = () =>
			{
				gui(an ^ NgoaiHinhCua.AN_TRANG_PHUC);
				mo(0);
			}
		});
		for (int i = 0; i < TEN_O.Length; i++)
		{
			int o = i;
			v.addElement(new Command(TEN_O[i] + ": " + ((an & (1 << i)) != 0 ? "Ẩn" : "Hiện"))
			{
				action = () =>
				{
					gui(an ^ (1 << o));
					mo(o + 1);
				}
			});
		}
		v.addElement(new Command("Phụ kiện người khác: " + (NgoaiHinhVe.hien ? "Hiện" : "Tắt"))
		{
			action = () =>
			{
				NgoaiHinhVe.hien = !NgoaiHinhVe.hien;
				try
				{
					RMS.saveRMSString(RMS_TEN, NgoaiHinhVe.hien ? "1" : "0");
				}
				catch (System.Exception)
				{
					// bỏ qua
				}
				mo(TEN_O.Length + 1);
			}
		});
		PauseMenu.moMenuGiuChon(v, 3, chon);
	}
}
