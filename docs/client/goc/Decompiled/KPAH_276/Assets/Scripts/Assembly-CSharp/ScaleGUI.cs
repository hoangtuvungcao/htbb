using System.Collections.Generic;
using UnityEngine;

public class ScaleGUI
{
	public static bool scaleScreen;

	/// <summary>Kích thước logic của game (mọi màn hình bố trí theo khung này).</summary>
	public static float WIDTH;

	public static float HEIGHT;

	private static List<Matrix4x4> stack = new List<Matrix4x4>();

	public static int numScale = 1;

	/// <summary>Tỉ lệ phóng và lề (viền đen) hiện tại: cửa sổ đổi cỡ / toàn màn hình vẫn giữ đúng tỉ lệ.</summary>
	public static float scale = 1f;

	public static float offX;

	public static float offY;

	public const int LOGIC_W = 800;

	public const int LOGIC_H = 600;

	/// <summary>Điện thoại: chiều cao logic cố định, chiều rộng theo tỉ lệ màn hình (không viền đen).</summary>
	public const int LOGIC_H_MOBILE = 480;

	public static void initScaleGUI()
	{
		WIDTH = LOGIC_W;
		HEIGHT = LOGIC_H;
		if (Application.isMobilePlatform)
		{
			float dai = Mathf.Max(Screen.width, Screen.height);
			float ngan = Mathf.Min(Screen.width, Screen.height);
			HEIGHT = LOGIC_H_MOBILE;
			WIDTH = Mathf.Round(LOGIC_H_MOBILE * dai / ngan);
		}
		Debug.Log("Init Scale GUI: Screen.w=" + Screen.width + " Screen.h=" + Screen.height + " logic=" + WIDTH + "x" + HEIGHT);
		scaleScreen = true;
		numScale = 2;
		capNhat();
	}

	/// <summary>Cửa sổ ban đầu: phóng khung 800x600 vừa ~85% chiều cao / 90% chiều rộng màn hình.</summary>
	public static void datCuaSo()
	{
		if (Application.isMobilePlatform)
		{
			return;
		}
		Resolution r = Screen.currentResolution;
		float s = Mathf.Min(r.width * 0.9f / LOGIC_W, r.height * 0.85f / LOGIC_H);
		if (s < 1f)
		{
			s = 1f;
		}
		Screen.SetResolution((int)(LOGIC_W * s), (int)(LOGIC_H * s), FullScreenMode.Windowed);
	}

	private static void capNhat()
	{
		scale = Mathf.Min(Screen.width / WIDTH, Screen.height / HEIGHT);
		if (scale <= 0f)
		{
			scale = 1f;
		}
		offX = (Screen.width - WIDTH * scale) * 0.5f;
		offY = (Screen.height - HEIGHT * scale) * 0.5f;
	}

	public static void BeginGUI()
	{
		if (scaleScreen)
		{
			capNhat();
			stack.Add(GUI.matrix);
			GUI.matrix = Matrix4x4.TRS(new Vector3(offX, offY, 0f), Quaternion.identity, new Vector3(scale, scale, 1f));
		}
	}

	public static void EndGUI()
	{
		if (scaleScreen)
		{
			GUI.matrix = stack[stack.Count - 1];
			stack.RemoveAt(stack.Count - 1);
		}
	}

	/// <summary>
	/// Thay GUIUtility.RotateAroundPivot: hàm gốc tính tâm xoay khi đã bỏ GUI.matrix nên lúc
	/// phóng màn hình thì hình xoay (icon loading...) lệch tâm. Ở đây xoay quanh tâm trong tọa
	/// độ logic rồi mới nhân tỉ lệ phóng.
	/// </summary>
	public static void xoay(float angle, Vector2 pivot)
	{
		Matrix4x4 m = GUI.matrix;
		GUI.matrix = Matrix4x4.identity;
		GUIUtility.RotateAroundPivot(angle, pivot);
		GUI.matrix = m * GUI.matrix;
	}

	/// <summary>Tọa độ chuột (pixel cửa sổ) -> tọa độ logic.</summary>
	public static float scaleX(float x)
	{
		if (!scaleScreen)
		{
			return x;
		}
		return (x - offX) / scale;
	}

	public static float scaleY(float y)
	{
		if (!scaleScreen)
		{
			return y;
		}
		return (y - offY) / scale;
	}
}
