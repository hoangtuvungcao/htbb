using System;
using UnityEngine;

/// <summary>
/// "Bức vẽ" phần mềm cho giao diện Pokémon bản PC: các lệnh vẽ kiểu J2ME Graphics (setColor, fillRect,
/// fillTriangle, fillArc, drawArc, drawLine, drawRect) vẽ vào mảng điểm ảnh rồi đóng thành MỘT ảnh
/// (Texture2D lọc Point - phóng to theo cửa sổ vẫn sắc nét kiểu pixel). Nhờ vậy code vẽ của bản jar
/// (game.pokemon.GiaoDien / KieuO) chép sang gần như nguyên văn, và mỗi khung hình chỉ vẽ một ảnh đã
/// tạo sẵn thay vì hàng trăm lệnh fillRect IMGUI (xem GiaoDien.anh: bộ nhớ đệm theo khóa).
/// Màu 0xRRGGBB (đục) hoặc 0xAARRGGBB qua setColorA (có trong suốt, pha alpha).
/// </summary>
public sealed class BucVe
{
	public readonly int w;

	public readonly int h;

	private readonly Color32[] px;

	private Color32 mau = new Color32(0, 0, 0, 255);

	/// <summary>Gốc tọa độ dịch (translate) như J2ME.</summary>
	public int tx;

	public int ty;

	public BucVe(int w, int h)
	{
		this.w = System.Math.Max(1, w);
		this.h = System.Math.Max(1, h);
		px = new Color32[this.w * this.h];
	}

	public void setColor(int rgb)
	{
		mau = new Color32((byte)(rgb >> 16 & 0xFF), (byte)(rgb >> 8 & 0xFF), (byte)(rgb & 0xFF), 255);
	}

	public void setColorA(int argb)
	{
		mau = new Color32((byte)(argb >> 16 & 0xFF), (byte)(argb >> 8 & 0xFF), (byte)(argb & 0xFF), (byte)((uint)argb >> 24));
	}

	public void translate(int dx, int dy)
	{
		tx += dx;
		ty += dy;
	}

	private void cham(int x, int y)
	{
		x += tx;
		y += ty;
		if (x < 0 || y < 0 || x >= w || y >= h)
		{
			return;
		}
		int i = y * w + x;
		if (mau.a == 255)
		{
			px[i] = mau;
			return;
		}
		if (mau.a == 0)
		{
			return;
		}
		Color32 d = px[i];
		int a = mau.a;
		int da = d.a * (255 - a) / 255;
		int ra = a + da;
		if (ra <= 0)
		{
			return;
		}
		px[i] = new Color32((byte)((mau.r * a + d.r * da) / ra), (byte)((mau.g * a + d.g * da) / ra), (byte)((mau.b * a + d.b * da) / ra), (byte)ra);
	}

	public void fillRect(int x, int y, int rw, int rh)
	{
		int x0 = System.Math.Max(x, -tx);
		int y0 = System.Math.Max(y, -ty);
		int x1 = System.Math.Min(x + rw, w - tx);
		int y1 = System.Math.Min(y + rh, h - ty);
		for (int yy = y0; yy < y1; yy++)
		{
			for (int xx = x0; xx < x1; xx++)
			{
				cham(xx, yy);
			}
		}
	}

	/// <summary>Khung chữ nhật rộng rw+1, cao rh+1 (như J2ME drawRect).</summary>
	public void drawRect(int x, int y, int rw, int rh)
	{
		fillRect(x, y, rw + 1, 1);
		fillRect(x, y + rh, rw + 1, 1);
		fillRect(x, y + 1, 1, rh - 1);
		fillRect(x + rw, y + 1, 1, rh - 1);
	}

	public void drawLine(int x0, int y0, int x1, int y1)
	{
		int dx = System.Math.Abs(x1 - x0);
		int dy = -System.Math.Abs(y1 - y0);
		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		int e = dx + dy;
		while (true)
		{
			cham(x0, y0);
			if (x0 == x1 && y0 == y1)
			{
				break;
			}
			int e2 = 2 * e;
			if (e2 >= dy)
			{
				e += dy;
				x0 += sx;
			}
			if (e2 <= dx)
			{
				e += dx;
				y0 += sy;
			}
		}
	}

	private static long canh(int ax, int ay, int bx, int by, int cx, int cy)
	{
		return (long)(bx - ax) * (cy - ay) - (long)(by - ay) * (cx - ax);
	}

	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3)
	{
		int minX = System.Math.Min(x1, System.Math.Min(x2, x3));
		int maxX = System.Math.Max(x1, System.Math.Max(x2, x3));
		int minY = System.Math.Min(y1, System.Math.Min(y2, y3));
		int maxY = System.Math.Max(y1, System.Math.Max(y2, y3));
		long dt = canh(x1, y1, x2, y2, x3, y3);
		if (dt == 0)
		{
			drawLine(x1, y1, x2, y2);
			drawLine(x2, y2, x3, y3);
			return;
		}
		for (int y = minY; y <= maxY; y++)
		{
			for (int x = minX; x <= maxX; x++)
			{
				long a = canh(x2, y2, x3, y3, x, y);
				long b = canh(x3, y3, x1, y1, x, y);
				long c = canh(x1, y1, x2, y2, x, y);
				if (dt > 0 ? (a >= 0 && b >= 0 && c >= 0) : (a <= 0 && b <= 0 && c <= 0))
				{
					cham(x, y);
				}
			}
		}
	}

	private static bool trongGoc(double goc, int batDau, int quet)
	{
		if (quet >= 360 || quet <= -360)
		{
			return true;
		}
		if (quet < 0)
		{
			batDau += quet;
			quet = -quet;
		}
		double d = (goc - batDau) % 360.0;
		if (d < 0)
		{
			d += 360.0;
		}
		return d <= quet;
	}

	/// <summary>Cung elip tô kín trong hộp (x, y, rw, rh); góc 0 = hướng 3 giờ, ngược chiều kim đồng hồ.</summary>
	public void fillArc(int x, int y, int rw, int rh, int batDau, int quet)
	{
		if (rw <= 0 || rh <= 0)
		{
			return;
		}
		double rx = rw / 2.0;
		double ry = rh / 2.0;
		double cx = x + rx;
		double cy = y + ry;
		for (int yy = y; yy < y + rh; yy++)
		{
			for (int xx = x; xx < x + rw; xx++)
			{
				double dx = (xx + 0.5 - cx) / rx;
				double dy = (yy + 0.5 - cy) / ry;
				if (dx * dx + dy * dy > 1.0)
				{
					continue;
				}
				double goc = System.Math.Atan2(-(yy + 0.5 - cy), xx + 0.5 - cx) * 180.0 / System.Math.PI;
				if (trongGoc(goc, batDau, quet))
				{
					cham(xx, yy);
				}
			}
		}
	}

	/// <summary>Đường viền elip 1 px trong hộp rộng rw+1, cao rh+1 (như J2ME drawArc).</summary>
	public void drawArc(int x, int y, int rw, int rh, int batDau, int quet)
	{
		double rx = rw / 2.0;
		double ry = rh / 2.0;
		if (rx <= 0 || ry <= 0)
		{
			return;
		}
		double cx = x + rx + 0.5;
		double cy = y + ry + 0.5;
		for (int yy = y; yy <= y + rh; yy++)
		{
			for (int xx = x; xx <= x + rw; xx++)
			{
				double dx = xx + 0.5 - cx;
				double dy = yy + 0.5 - cy;
				double ngoai = (dx / (rx + 0.5)) * (dx / (rx + 0.5)) + (dy / (ry + 0.5)) * (dy / (ry + 0.5));
				double trong = rx > 0.5 && ry > 0.5 ? (dx / (rx - 0.5)) * (dx / (rx - 0.5)) + (dy / (ry - 0.5)) * (dy / (ry - 0.5)) : 2.0;
				if (ngoai <= 1.0 && trong >= 1.0)
				{
					double goc = System.Math.Atan2(-dy, dx) * 180.0 / System.Math.PI;
					if (trongGoc(goc, batDau, quet))
					{
						cham(xx, yy);
					}
				}
			}
		}
	}

	/// <summary>Chép điểm ảnh ARGB (hàng trên trước) vào (x, y), pha alpha.</summary>
	public void veDiem(int[] argb, int pw, int ph, int x, int y)
	{
		Color32 cu = mau;
		for (int j = 0; j < ph; j++)
		{
			for (int i = 0; i < pw; i++)
			{
				int c = argb[j * pw + i];
				if (((uint)c >> 24) == 0)
				{
					continue;
				}
				setColorA(c);
				cham(x + i, y + j);
			}
		}
		mau = cu;
	}

	/// <summary>Đóng thành ảnh (lọc Point, không lặp). Hàng 0 của Texture2D là đáy nên lật dọc.</summary>
	public Image taoAnh()
	{
		Texture2D t = new Texture2D(w, h, TextureFormat.RGBA32, false);
		t.filterMode = FilterMode.Point;
		t.wrapMode = TextureWrapMode.Clamp;
		Color32[] lat = new Color32[px.Length];
		for (int y = 0; y < h; y++)
		{
			Array.Copy(px, y * w, lat, (h - 1 - y) * w, w);
		}
		t.SetPixels32(lat);
		t.Apply(false, true);
		Image a = new Image();
		UnityEngine.Object.Destroy(a.texture); // ảnh 1x1 mặc định của Image
		a.texture = t;
		a.w = w;
		a.h = h;
		return a;
	}
}
