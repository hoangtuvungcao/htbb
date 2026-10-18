// Port từ client_jar/shared/mod/game/render/screen/TienIch.java (bản jar / APK).
using System;

/**
 * Menu Tiện ích (chỉ thứ KHÔNG vượt luật máy chủ): mở cửa hàng chính thức, đăng xuất, bật/tắt popup thông báo.
 * Đã bỏ (vượt luật / tự động hoá ngoài máy chủ): tự đăng nhập lại, tự mua thuốc / vé / thẻ, tầm chiêu 300 phía
 * client, về làng không chờ 10 giây, mở Pidgeot (Bay) từ xa không cần tới NPC, Lên thành tự động.
 */
public sealed class TienIch
{
	private const string RMS_TEN = "htbbti";

	/** Tắt thông báo: nuốt hộp thoại OK của server khi đang trong game (màn đăng nhập vẫn hiện). */
	public static bool tatThongBao;

	private static bool daNap;

	public static void moMenu(int chon)
	{
		TienIch.nap();
		mVector vector = new mVector();
		vector.addElement(new Command("Cửa hàng")
		{
			action = () =>
			{
				Canvas.menu.showMenu = false;
				PauseMenu.gI().showShopNew(0);
			}
		});
		vector.addElement(new Command("Đăng xuất")
		{
			action = () =>
			{
				TienIch.dangXuat();
			}
		});
		vector.addElement(new Command(tatThongBao ? "Thông báo: tắt" : "Thông báo: bật")
		{
			action = () =>
			{
				tatThongBao = !tatThongBao;
				TienIch.luu();
				TienIch.moMenu(2);
			}
		});
		PauseMenu.moMenuGiuChon(vector, 2, chon);
	}

	private static void dangXuat()
	{
		Canvas.menu.showMenu = false;
		TuDong.tat(true);
		TienIch.veManChonServer();
	}

	/** Y hệt nút OK của hộp "Bị ngắt kết nối" (GameScr.onDisconnect). */
	private static void veManChonServer()
	{
		Canvas.endDlg();
		Canvas.instance.init();
		Session_ME.gI().close();
		Tilemap.loadMap(0, null);
		ServerListScr.gI().switchToMe();
	}

	/** Gọi từ AutoGuard.autoEatDlg: true = nuốt hộp thoại này. */
	public static bool anThongBao(string @string)
	{
		TienIch.nap();
		return tatThongBao && Canvas.gameScr != null && Canvas.currentScreen == Canvas.gameScr;
	}

	private static void nap()
	{
		if (daNap)
		{
			return;
		}
		daNap = true;
		try
		{
			string @string = RMS.loadRMSString(RMS_TEN);
			if (@string != null && @string.Length >= 1)
			{
				tatThongBao = @string[0] == '1';
			}
		}
		catch (Exception)
		{
			// mặc định
		}
	}

	private static void luu()
	{
		try
		{
			RMS.saveRMSString(RMS_TEN, tatThongBao ? "1" : "0");
		}
		catch (Exception)
		{
			// empty catch block
		}
	}
}
