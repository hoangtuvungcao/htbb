using UnityEngine;

/// <summary>
/// Bo tile ban do MOI (CC0 Kenney + ve bang code, tools/pokemon/DungBoTileMoi.java,
/// docs/giay-phep.md). Resources/poke_tile/{t,t_hang,t_thanh}.bytes: anh doc 16 x (16*n),
/// chi so tile KHAC bo goc - di cung ban do ve lai (server gui qua goi 12) va kieu
/// Resources/poke_map/kieu_*.bytes (Res.loadTileImage). Res.loadImgMap dung bo nay.
/// </summary>
public class PokeTile
{
	public const string THU_MUC = "poke_tile/";

	/// <summary>Nap mot dai tile; thieu tep tra ve null (khong log loi nhu Image.createImage).</summary>
	public static Image napAnh(string ten)
	{
		TextAsset textAsset = (TextAsset)Resources.Load(THU_MUC + ten, typeof(TextAsset));
		if (textAsset == null || textAsset.bytes == null || textAsset.bytes.Length == 0)
		{
			return null;
		}
		Image image = Image.createImage(textAsset.bytes);
		if (image == null || image.getWidth() != 16 || image.getHeight() % 16 != 0)
		{
			return null;
		}
		return image;
	}

	/// <summary>Anh nho ban do (1 x so tile) cua bo tile moi; thieu tep tra ve null.</summary>
	public static Image napNho(string ten)
	{
		TextAsset textAsset = (TextAsset)Resources.Load(THU_MUC + ten, typeof(TextAsset));
		if (textAsset == null || textAsset.bytes == null || textAsset.bytes.Length == 0)
		{
			return null;
		}
		return Image.createImage(textAsset.bytes);
	}

	/// <summary>
	/// Ghi mau trung binh (0xRRGGBB, bo pixel trong suot) cua tung tile vao mang mau ban do nho.
	/// Texture Unity dem hang tu duoi len, nen tile i nam o hang [cao - 16(i+1), cao - 16i).
	/// </summary>
	public static void tinhMauBanDoNho(Image strip, int[] mau)
	{
		if (strip == null || strip.texture == null || mau == null)
		{
			return;
		}
		Color32[] px = strip.texture.GetPixels32();
		int w = strip.texture.width;
		int h = strip.texture.height;
		int soTile = h / 16;
		for (int i = 0; i < soTile && i < mau.Length; i++)
		{
			long r = 0;
			long g = 0;
			long b = 0;
			int dem = 0;
			int y0 = h - 16 * (i + 1);
			for (int y = y0; y < y0 + 16; y++)
			{
				for (int x = 0; x < 16 && x < w; x++)
				{
					Color32 c = px[y * w + x];
					if (c.a >= 128)
					{
						r += c.r;
						g += c.g;
						b += c.b;
						dem++;
					}
				}
			}
			if (dem > 0)
			{
				mau[i] = (int)((r / dem << 16) | (g / dem << 8) | (b / dem));
			}
		}
	}
}
