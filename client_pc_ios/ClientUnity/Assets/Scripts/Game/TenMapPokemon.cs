/// <summary>
/// Doi ten dia danh goc sang dia danh Pokemon - BAN SAO cua src/com/htbb/pokemon/TenMapPokemon.java
/// (test/KiemThuPokemon doi chieu hai bang). Dung cho ten viet cung o client (Tilemap.setNameMap);
/// ten gui tu server (lenh 105) da duoc server doi san.
/// </summary>
public static class TenMapPokemon
{
	public static readonly string[][] BANG = new string[][]
	{
		new string[] { "Dao Châu", "Pallet" },
		new string[] { "Tiên Du", "Viridian" },
		new string[] { "Sơn Nam", "Pewter" },
		new string[] { "Kỳ Bố", "Cerulean" },
		new string[] { "Phù Liệt", "Vermilion" },
		new string[] { "Hàm Tử", "Lavender" },
		new string[] { "Thạch Giang", "Celadon" },
		new string[] { "Đông Sơn", "Saffron" },
		new string[] { "Tử Quan", "Fuchsia" },
		new string[] { "Trường Giang", "Cinnabar" },
		new string[] { "Sơn Lâm", "Rừng Viridian" },
		new string[] { "Phong Châu", "Núi Mặt Trăng" },
		new string[] { "Bình Lục", "Vùng Safari" },
		new string[] { "Lộc Trĩ", "New Bark" },
		new string[] { "Bảo Thái", "Cherrygrove" },
		new string[] { "Hầm Tối", "Hang Tối" },
		new string[] { "Hồi Hồ", "Hồ Phẫn Nộ" },
		new string[] { "Siêu Loại", "Violet" },
		new string[] { "Cẩm Khê", "Azalea" },
		new string[] { "Bạch Hạc", "Goldenrod" },
		new string[] { "Tế Giang", "Ecruteak" },
		new string[] { "Đỗ Đông", "Olivine" },
		new string[] { "Thành Trấn Danh", "Cao nguyên Indigo" },
		new string[] { "Biên Giới", "Đường Victory" },
		new string[] { "Hang Thằn Lằn", "Hang Salamence" },
		new string[] { "Hang Đại Bàng", "Tổ Zapdos" },
		new string[] { "Hang Dracula", "Hang Darkrai" },
		new string[] { "Hang Mãng Xà", "Hang Serperior" },
		new string[] { "Cánh Rừng Đông", "Rừng Ilex" },
		new string[] { "Rừng Già", "Rừng Cổ" },
		new string[] { "Bình Kiều", "Mahogany" },
		new string[] { "Dương Đông", "Blackthorn" },
		new string[] { "Lâm Tây", "Cianwood" },
		new string[] { "Chiến Hạm", "Tàu S.S. Anne" },
		new string[] { "Núi kho báu", "Núi Silver" },
		new string[] { "Nhà bán HP", "Trung tâm Pokémon" },
		new string[] { "Nhà bán MP", "Poké Mart" },
		new string[] { "Nhà thợ rèn", "Xưởng chế tạo" },
	};

	/// <summary>Vung Hoenn (nam 2): ten theo id nap - BAN SAO cua TenMapPokemon.java (server + J2ME).</summary>
	public static readonly int[] HOENN_ID = new int[] { 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 160, 161, 162, 163, 164, 165, 280, 281, 282 };
	public static readonly string[] HOENN_TEN = new string[] { "Littleroot", "Rừng Petalburg", "Rustboro", "Bờ biển Dewford", "Slateport", "Route 110", "Mauville", "Sa mạc Route 111", "Lavaridge", "Núi Chimney", "Rừng mưa Route 119", "Fortree", "Route 120 - Viện Thời tiết", "Lilycove", "Núi Pyre", "Biển Route 124", "Mossdeep", "Sootopolis", "Biển Route 131", "Ever Grande", "Hang Granite", "Thác Meteor", "Hang dưới biển", "Hang Khởi Nguyên", "Tháp Trời", "Đường Victory Hoenn", "Đỉnh Tháp Trời", "Liên minh Hoenn", "Sảnh Nhà vô địch" };

	public static string tenHoenn(int id)
	{
		for (int i = 0; i < HOENN_ID.Length; i++)
		{
			if (HOENN_ID[i] == id)
			{
				return HOENN_TEN[i];
			}
		}
		return string.Empty;
	}

	private static readonly string[] HUONG = new string[] { "Đông ", "Tây ", "Nam ", "Bắc " };

	private static string traBang(string ten)
	{
		for (int i = 0; i < BANG.Length; i++)
		{
			if (string.Equals(BANG[i][0], ten, System.StringComparison.OrdinalIgnoreCase))
			{
				return BANG[i][1];
			}
		}
		return null;
	}

	public static string doi(string ten)
	{
		if (string.IsNullOrEmpty(ten))
		{
			return ten;
		}
		string nguyen = traBang(ten.Trim());
		if (nguyen != null)
		{
			return nguyen;
		}
		for (int i = 0; i < HUONG.Length; i++)
		{
			string h = HUONG[i];
			if (ten.Length > h.Length && string.Compare(ten, 0, h, 0, h.Length, System.StringComparison.OrdinalIgnoreCase) == 0)
			{
				string sau = traBang(ten.Substring(h.Length).Trim());
				if (sau != null)
				{
					return h + sau;
				}
			}
		}
		return ten;
	}
}
