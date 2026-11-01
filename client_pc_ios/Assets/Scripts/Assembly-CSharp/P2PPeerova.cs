using System;

/// <summary>
/// Nhanh p2p: client Unity PC noi cong game qua Peerova P2P bang C# binding Peerova (Peerova.Tunnel trong
/// Assets/Plugins/Peerova/Peerova.cs = p2p/peerova/bindings/csharp/Peerova.cs; native peerova.dll trong tien trinh,
/// khong tien trinh phu). Bat khi co ticket: tham so dong lenh -htbbP2PTicket pvt_... > bien HTBB_P2P_TICKET >
/// P2P_TICKET= trong Resources/htbb_phat_hanh.txt (BuildCli dong vao tu -htbbP2PTicket / bien P2P_TICKET).
/// Native: Assets/Plugins/x86_64/peerova.dll = p2p/out/dist/windows-x86_64-win7/peerova.dll (Windows 7 / Server 2012
/// toi 11). Chi standalone (Windows / Linux / macOS) va Editor; iOS / Android Unity khong bat.
/// Loi P2P: nem Exception (Session_ME -> onConnectionFail), KHONG lui ve TCP thang. CHUA chay trong Unity (chi kiem
/// tren .NET 8 voi cung tep).
/// </summary>
public static class P2PPeerova
{
	private static readonly object khoa = new object();
	private static Peerova.Tunnel tunnel;
	private static int congXa = -1;
	private static string ticketDaDoc;

	/// <summary>Ly do loi P2P gan nhat ("" = khong loi).</summary>
	public static volatile string loiCuoi = "";

	public static string ticket()
	{
		if (ticketDaDoc != null)
		{
			return ticketDaDoc;
		}
		string t = null;
		string[] args = Environment.GetCommandLineArgs();
		int i = Array.IndexOf(args, "-htbbP2PTicket");
		if (i >= 0 && i + 1 < args.Length)
		{
			t = args[i + 1];
		}
		if (string.IsNullOrEmpty(t))
		{
			t = Environment.GetEnvironmentVariable("HTBB_P2P_TICKET");
		}
		if (string.IsNullOrEmpty(t))
		{
			t = ThuongHieu.giaTriPhatHanh("P2P_TICKET");
		}
		ticketDaDoc = t == null ? string.Empty : t.Trim();
		return ticketDaDoc;
	}

	public static bool bat()
	{
#if UNITY_STANDALONE || UNITY_EDITOR
		return ticket().Length > 0;
#else
		return false;
#endif
	}

	private static void xaLog()
	{
		string l;
		while (tunnel != null && (l = tunnel.NextLog()) != null)
		{
			UnityEngine.Debug.Log("[P2P] " + l);
		}
	}

	/// <summary>Cong 127.0.0.1 da noi P2P toi cong game <paramref name="cong"/> (goi tu luong mang, chan toi 30 s).</summary>
	public static int congCucBo(int cong)
	{
		lock (khoa)
		{
			if (tunnel != null && (congXa != cong || tunnel.State == Peerova.TunnelState.Down))
			{
				xaLog();
				tunnel.Dispose();
				tunnel = null;
			}
			if (tunnel == null)
			{
				try
				{
					tunnel = Peerova.Tunnel.Client(ticket(), "{\"forward\":[\"tcp:0=" + cong + "\"],\"timeout_ms\":20000}");
				}
				catch (Exception e)
				{
					loiCuoi = "Không khởi động được kết nối P2P" + (e is DllNotFoundException ? " (thiếu peerova.dll)" : "") + ": " + e.Message;
					UnityEngine.Debug.LogError("[P2P] " + loiCuoi);
					throw new Exception(loiCuoi);
				}
				congXa = cong;
			}
			Peerova.TunnelState st = tunnel.Wait(30000);
			xaLog();
			if (st != Peerova.TunnelState.Up)
			{
				string e = tunnel.Error;
				loiCuoi = "Không kết nối P2P được tới máy chủ" + (string.IsNullOrEmpty(e) ? "" : ": " + e);
				tunnel.Dispose();
				tunnel = null;
				UnityEngine.Debug.LogError("[P2P] " + loiCuoi);
				throw new Exception(loiCuoi);
			}
			loiCuoi = "";
			return tunnel.LocalPort(0);
		}
	}
}
