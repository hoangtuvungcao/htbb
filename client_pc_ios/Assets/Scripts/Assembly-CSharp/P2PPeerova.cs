using System;
using System.Runtime.InteropServices;
using System.Text;

/// <summary>
/// Nhanh p2p: client Unity PC noi cong game qua Peerova P2P (peerova.dll, C ABI peerova_tunnel_*, trong tien trinh -
/// khong tien trinh phu). Bat khi co ticket: tham so dong lenh -htbbP2PTicket pvt_... > bien HTBB_P2P_TICKET >
/// P2P_TICKET= trong Resources/htbb_phat_hanh.txt (BuildCli dong vao tu -htbbP2PTicket / bien P2P_TICKET).
/// Native: Assets/Plugins/x86_64/peerova.dll = p2p/out/dist/windows-x86_64-win7/peerova.dll (chay tu Windows 7 / Server
/// 2012 toi 11). Chi Windows / Linux / macOS standalone; iOS / Android Unity khong bat (khong co ticket thi khong goi native).
/// Loi P2P: nem Exception (Session_ME bao onConnectionFail), KHONG lui ve TCP thang. CHUA chay thu trong Unity.
/// </summary>
public static class P2PPeerova
{
	private const string LIB = "peerova";

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern IntPtr peerova_tunnel_client(byte[] ticket, byte[] configJson);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern int peerova_tunnel_wait(IntPtr t, uint timeoutMs);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern int peerova_tunnel_state(IntPtr t);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern int peerova_tunnel_local_port(IntPtr t, int index);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern int peerova_tunnel_error(IntPtr t, byte[] buf, int len);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern int peerova_tunnel_next_log(IntPtr t, byte[] buf, int len);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern void peerova_tunnel_free(IntPtr t);

	[DllImport(LIB, CallingConvention = CallingConvention.Cdecl)]
	private static extern IntPtr peerova_last_error();

	private const int UP = 1, DOWN = 2;
	private static readonly object khoa = new object();
	private static IntPtr tunnel = IntPtr.Zero;
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

	private static byte[] z(string s)
	{
		return Encoding.UTF8.GetBytes(s + "\0");
	}

	private static string chuoi(Func<byte[], int, int> f)
	{
		int can = f(null, 0);
		if (can <= 0)
		{
			return null;
		}
		byte[] b = new byte[can];
		f(b, can);
		return Encoding.UTF8.GetString(b, 0, can - 1);
	}

	private static void xaLog()
	{
		string l;
		IntPtr t = tunnel;
		while ((l = chuoi((b, n) => peerova_tunnel_next_log(t, b, n))) != null)
		{
			UnityEngine.Debug.Log("[P2P] " + l);
		}
	}

	/// <summary>Cong 127.0.0.1 da noi P2P toi cong game <paramref name="cong"/> (chan toi 30 s).</summary>
	public static int congCucBo(int cong)
	{
		lock (khoa)
		{
			if (tunnel != IntPtr.Zero && (congXa != cong || peerova_tunnel_state(tunnel) == DOWN))
			{
				xaLog();
				peerova_tunnel_free(tunnel);
				tunnel = IntPtr.Zero;
			}
			if (tunnel == IntPtr.Zero)
			{
				try
				{
					tunnel = peerova_tunnel_client(z(ticket()), z("{\"forward\":[\"tcp:0=" + cong + "\"],\"timeout_ms\":20000}"));
				}
				catch (Exception e)
				{
					loiCuoi = "Không khởi động được kết nối P2P (thiếu peerova.dll?): " + e.Message;
					throw new Exception(loiCuoi);
				}
				if (tunnel == IntPtr.Zero)
				{
					loiCuoi = "Không khởi động được kết nối P2P: " + Marshal.PtrToStringAnsi(peerova_last_error());
					throw new Exception(loiCuoi);
				}
				congXa = cong;
			}
			int st = peerova_tunnel_wait(tunnel, 30000);
			xaLog();
			if (st != UP)
			{
				IntPtr t = tunnel;
				string e = chuoi((b, n) => peerova_tunnel_error(t, b, n));
				loiCuoi = "Không kết nối P2P được tới máy chủ" + (string.IsNullOrEmpty(e) ? "" : ": " + e);
				peerova_tunnel_free(tunnel);
				tunnel = IntPtr.Zero;
				UnityEngine.Debug.LogError("[P2P] " + loiCuoi);
				throw new Exception(loiCuoi);
			}
			loiCuoi = "";
			return peerova_tunnel_local_port(tunnel, 0);
		}
	}
}
