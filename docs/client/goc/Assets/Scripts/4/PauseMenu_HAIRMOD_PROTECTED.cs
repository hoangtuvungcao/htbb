public class PauseMenu_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static PauseMenu_HAIRMOD_PROTECTED me;

	private string[][] name = new string[9][]
	{
		new string[1] { "Cửa hàng" },
		new string[7] { "Cài đặt", "Cấu hình", "Bản đồ lớn", "Bật/tắt giao diện", "Hướng dẫn", "âm thanh", "Bật/tắt lời mời" },
		new string[7] { "Bản thân", "Hành trang", "Kỹ năng", "Tiềm năng", "Trang bị", "Thông tin", "Trang bị thú" },
		new string[1] { "Nap Xu" },
		new string[1] { "Nhiệm vụ" },
		new string[1] { "Đồ sát" },
		new string[7] { "Khác", "Tin nhắn", "Nhóm", "Bạn bè", "Top cao thủ", "Top đại gia", "Kênh thế giới" },
		new string[1] { string.Empty },
		new string[1] { "Thoát" }
	};

	public static PauseMenu_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new PauseMenu_HAIRMOD_PROTECTED());
	}

	public override void init()
	{
	}

	public void showMe(int index)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(index, WindowInfoScr_HAIRMOD_PROTECTED.gI().isSell, WindowInfoScr_HAIRMOD_PROTECTED.gI().isSell ? WindowInfoScr_HAIRMOD_PROTECTED.gI().arrSell : new sbyte[8] { 0, 1, 2, 3, 4, 5, 6, 31 });
		WindowInfoScr_HAIRMOD_PROTECTED.charWearing = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar;
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setImageCharWear();
	}

	public void showShopNew(int index)
	{
		ShopNew_HAIRMOD_PROTECTED.gI().switchToMe();
		ShopNew_HAIRMOD_PROTECTED.gI().setInfo(index, ShopNew_HAIRMOD_PROTECTED.gI().isSell, ShopNew_HAIRMOD_PROTECTED.gI().isSell ? ShopNew_HAIRMOD_PROTECTED.gI().arrSell : new sbyte[8] { 0, 1, 2, 3, 4, 5, 6, 31 });
		ShopNew_HAIRMOD_PROTECTED.charWearing = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar;
		ShopNew_HAIRMOD_PROTECTED.gI().setImageCharWear();
	}

	public void initName()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idClan == -1)
		{
			name[7] = new string[3] { "Bang hội", "Top bang hội", "Đăng ký bang hội" };
		}
		else if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 0)
		{
			name[7] = new string[10] { "Bang hội", "Top bang hội", "Nhiệm vụ", "Thành viên bang hội", "Thông tin bang hội", "Tin nhắn bang hội", "Kỹ năng", "Chat toàn bang", "Quyên góp", "Giải tán bang hội" };
			if (Char_HAIRMOD_PROTECTED.dissolved)
			{
				name[7][9] = "Phục hồi bang hội";
			}
		}
		else
		{
			name[7] = new string[10] { "Bang hội", "Top bang hội", "Nhiệm vụ", "Thành viên bang hội", "Thông tin bang hội", "Tin nhắn bang hội", "Kỹ năng", "Chat toàn bang", "Quyên góp", "Rời bang" };
		}
	}
}
