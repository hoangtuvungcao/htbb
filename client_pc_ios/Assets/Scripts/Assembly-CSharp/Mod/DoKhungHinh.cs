/// <summary>
/// Đo chi phí vẽ mỗi khung (chỉ bật khi chạy game với tham số -htbbDo): mỗi 10 s ghi một dòng [DOKHUNG] vào
/// Player.log - thời gian vẽ trung bình / lớn nhất (canvas.paint trong OnGUI), số byte bộ nhớ cấp phát trong
/// lúc vẽ mỗi khung, số lần GC. Dùng để so trước/sau khi tối ưu vẽ.
/// </summary>
public static class DoKhungHinh
{
	public static readonly bool Bat = System.Array.IndexOf(System.Environment.GetCommandLineArgs(), "-htbbDo") >= 0;

	private static readonly System.Diagnostics.Stopwatch dongHo = new System.Diagnostics.Stopwatch();

	private static double tongMs;

	private static double maxMs;

	private static int soKhung;

	private static long tongCapPhat;

	private static int soKhungDoCapPhat;

	private static long heapTruoc;

	private static int gcLucBatDau;

	private static int gcMoc = -1;

	private static float lanGhi;

	// thời gian logic (FixedUpdate) và toàn bộ OnGUI (mọi sự kiện, không chỉ vẽ)
	private static readonly System.Diagnostics.Stopwatch dongHoLogic = new System.Diagnostics.Stopwatch();

	private static readonly System.Diagnostics.Stopwatch dongHoGui = new System.Diagnostics.Stopwatch();

	private static double tongLogicMs;

	private static int soLogic;

	private static double tongGuiMs;

	private static int soGui;

	public static void batDauLogic()
	{
		if (Bat)
		{
			dongHoLogic.Restart();
		}
	}

	public static void ketThucLogic()
	{
		if (Bat)
		{
			dongHoLogic.Stop();
			tongLogicMs += dongHoLogic.Elapsed.TotalMilliseconds;
			soLogic++;
		}
	}

	public static void batDauGui()
	{
		if (Bat)
		{
			dongHoGui.Restart();
		}
	}

	public static void ketThucGui()
	{
		if (Bat)
		{
			dongHoGui.Stop();
			tongGuiMs += dongHoGui.Elapsed.TotalMilliseconds;
			soGui++;
		}
	}

	public static void batDau()
	{
		if (!Bat)
		{
			return;
		}
		gcLucBatDau = System.GC.CollectionCount(0);
		heapTruoc = System.GC.GetTotalMemory(false);
		dongHo.Restart();
	}

	public static void ketThuc()
	{
		if (!Bat)
		{
			return;
		}
		dongHo.Stop();
		long heapSau = System.GC.GetTotalMemory(false);
		double ms = dongHo.Elapsed.TotalMilliseconds;
		tongMs += ms;
		if (ms > maxMs)
		{
			maxMs = ms;
		}
		soKhung++;
		// cấp phát trong lúc vẽ = heap tăng thêm, chỉ tính khung không có GC chen giữa
		if (System.GC.CollectionCount(0) == gcLucBatDau && heapSau >= heapTruoc)
		{
			tongCapPhat += heapSau - heapTruoc;
			soKhungDoCapPhat++;
		}
		if (gcMoc < 0)
		{
			gcMoc = System.GC.CollectionCount(0);
		}
		float t = UnityEngine.Time.realtimeSinceStartup;
		if (t - lanGhi >= 10f)
		{
			int gc = System.GC.CollectionCount(0);
			UnityEngine.Debug.Log("[DOKHUNG] khung=" + soKhung
				+ " ve_tb=" + (tongMs / soKhung).ToString("F3") + "ms ve_max=" + maxMs.ToString("F2") + "ms"
				+ " capphat_tb=" + (soKhungDoCapPhat == 0 ? 0 : tongCapPhat / soKhungDoCapPhat) + "B/khung"
				+ " gc=" + (gc - gcMoc) + " heap=" + (System.GC.GetTotalMemory(false) >> 10) + "KB"
				+ " logic=" + soLogic + "x/" + (soLogic == 0 ? 0 : tongLogicMs / soLogic).ToString("F3") + "ms"
				+ " ongui=" + soGui + "x/" + (soGui == 0 ? 0 : tongGuiMs / soGui).ToString("F3") + "ms"
				+ " logic_tong=" + tongLogicMs.ToString("F0") + "ms ongui_tong=" + tongGuiMs.ToString("F0") + "ms");
			tongLogicMs = 0;
			soLogic = 0;
			tongGuiMs = 0;
			soGui = 0;
			lanGhi = t;
			tongMs = 0;
			maxMs = 0;
			soKhung = 0;
			tongCapPhat = 0;
			soKhungDoCapPhat = 0;
			gcMoc = gc;
		}
	}
}
