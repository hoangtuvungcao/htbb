// Port từ client_jar/shared/mod/game/render/screen/PauseMenu.java (phần mod). Phần gốc của menu ở ../PauseMenu.cs.
// Mod NQSH cũ (Pidgeot từ xa, Chế đồ nhanh, Dòng ẩn, Auto up, Giao hàng tự động, PK, Vận may: đập / thêm dòng /
// khóa / nuôi pet / học chiêu pet / nghiền / tự đăng nhập lại / tự đi bộ) đã bỏ hẳn. Hàng mod = "Tự động đánh"
// (chính thức, máy chủ điều khiển: TuDong + BangTuDong), "Hiệu ứng", "Tiện ích", "Ngoại hình".
public partial class PauseMenu : MyScreen
{
	/** Menu con mở gần nhất. Mở lại mà vẫn đứng ở dòng vừa bấm (Menu.startAt luôn về dòng đầu). */
	public static mVector menuMod;

	public static void moMenuGiuChon(mVector vector, int n, int n2)
	{
		menuMod = vector;
		// mọi popup của mod mở ở vị trí 3 (góc dưới phải) như menu Pidgeot của game
		Canvas.menu.startAt(vector, 3);
		if (n2 <= 0 || n2 >= vector.size())
		{
			return;
		}
		Menu menu = Canvas.menu;
		menu.selected = n2;
		int n3 = n2 * MyScreen.ITEM_HEIGHT - MyScreen.ITEM_HEIGHT * 2;
		if (n3 > menu.cylim)
		{
			n3 = menu.cylim;
		}
		if (n3 < 0)
		{
			n3 = 0;
		}
		menu.ctoyBag = n3;
		menu.cyBag = n3;
	}

	/** Hàng mod dạng popup (mở từ tab "Khác" của MenuWindows). */
	public static void moMenuMod()
	{
		mVector vector = new mVector();
		vector.addElement(new Command("Tự động đánh")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				BangTuDong.mo();
			}
		});
		vector.addElement(new Command("Hiệu ứng")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				HieuUng.moMenu(0);
			}
		});
		vector.addElement(new Command("Tiện ích")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				TienIch.moMenu(0);
			}
		});
		vector.addElement(new Command("Ngoại hình")
		{
			action = () =>
			{
				Canvas.gameScr.switchToMe();
				NgoaiHinhMenu.mo(0); // ngoại hình trang bị: ẩn/hiện phụ kiện, trang phục
			}
		});
		PauseMenu.moMenuGiuChon(vector, 3, 0);
	}

	/** Gọi ở ĐẦU GameScr.update(): Tự động đánh chính thức + Menu nhanh (bắt hết lỗi bên trong). */
	public static void autoCheDoTick()
	{
		AutoGuard.tick();
	}

	public PauseMenu()
	{
		this.init();
	}
}
