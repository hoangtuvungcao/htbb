using System;
using System.Numerics;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using UnityEngine;

/// <summary>
/// Xác thực hai chiều trước khi đăng nhập (lệnh -88), chuyển từ KiemServer.java của bản jar
/// (xem HTBB real/XacThucClient.java).
/// 1. Gửi host đang kết nối + 16 byte ngẫu nhiên nC; server trả chữ ký RSA-1024 của
///    (MAGIC, host, nC, nS) + nS. Kiểm bằng khóa công khai N: sai / không trả lời thì không
///    đăng nhập, báo đúng câu "không thể kết nối" của game.
/// 2. Gửi SHA-256(k | nS | nC | host | k) để server biết đây là client mod;
///    k = SHA-256(N 128 byte | MAU_NEN), giống hệt bản jar.
/// </summary>
public static class KiemServer
{
	private const int LENH = -88;

	private const int CHO_MS = 30000;

	/// <summary>Khóa công khai (e = 65537), 32 số 32-bit, số thấp trước - giống bản jar.</summary>
	private static readonly uint[] N = new uint[32]
	{
		0xe753c265u, 0x2b76f6a4u, 0x15a19590u, 0xecf19eeau, 0x67d5e2e9u, 0xb66ba946u, 0xfc4bc516u, 0xc850b2eeu,
		0x2820ecf2u, 0xf820c771u, 0x8cfa9f69u, 0xbffac9b0u, 0x301d384fu, 0xfd55af57u, 0xcc91852bu, 0xa211e18bu,
		0xc35948b6u, 0xbe73bbd2u, 0xf1105817u, 0x5a8b5de1u, 0xd5b57bc0u, 0xdaedb6efu, 0x05dba856u, 0x37b5e20eu,
		0x62c91a56u, 0x64dd8242u, 0x582aa78du, 0x4519d24du, 0x9d3c0b84u, 0x9ea53550u, 0xf821af79u, 0x8ca875abu
	};

	/// <summary>Giống AutoGuard.MAU_NEN bên jar.</summary>
	private static readonly int[] MAU_NEN = new int[8] { 0x69436c, 0x45957f, 0xd11674, 0xb24394, 0xf69a31, 0x8c9b09, 0xbcdeeb, 0x8f48ec };

	private static readonly byte[] MAGIC = new byte[8] { 141, 50, 41, 192, 231, 78, 23, 234 };

	private static bool dangCho;

	private static bool choPhep;

	private static int lan;

	private static string host;

	private static byte[] nonce;

	private static byte[] nonceS;

	private static string tk;

	private static string mk;

	private static string ver;

	private static Timer hetGio;

	/// <summary>Đầu GameService.login: true = chặn lệnh đăng nhập này (đang xác thực trước).</summary>
	public static bool chanDangNhap(string username, string pass, string version)
	{
		if (choPhep)
		{
			choPhep = false;
			return false;
		}
		tk = username;
		mk = pass;
		ver = version;
		dangCho = true;
		int n = ++lan;
		try
		{
			host = ServerListScr.address[ServerListScr.index];
			nonce = new byte[16];
			using (RandomNumberGenerator rng = RandomNumberGenerator.Create())
			{
				rng.GetBytes(nonce);
			}
			Message message = new Message(LENH);
			message.writer().writeByte(1);
			message.writer().writeUTF(host);
			message.writer().writeUnsignedByte(nonce);
			GameService.gI().session.sendMessage(message);
			message.cleanup();
		}
		catch (Exception ex)
		{
			Debug.LogWarning("KiemServer: " + ex.Message);
			thatBai(n);
			return true;
		}
		if (hetGio != null)
		{
			hetGio.Dispose();
		}
		hetGio = new Timer(delegate
		{
			thatBai(n);
		}, null, CHO_MS, Timeout.Infinite);
		return true;
	}

	/// <summary>Đầu MessageHandler.onMessage: true = gói -88 đã xử lý ở đây.</summary>
	public static bool nhan(Message msg)
	{
		if (msg.command != LENH)
		{
			return false;
		}
		if (!dangCho)
		{
			return true;
		}
		int n = lan;
		bool ok = false;
		try
		{
			sbyte[] chuKy = new sbyte[128];
			msg.reader().read(ref chuKy);
			sbyte[] nS = new sbyte[16];
			msg.reader().read(ref nS);
			nonceS = (byte[])(Array)nS;
			ok = kiem((byte[])(Array)chuKy);
		}
		catch (Exception)
		{
			// gói hỏng = sai
		}
		if (!ok)
		{
			Debug.LogWarning("KiemServer: chữ ký server sai");
			thatBai(n);
			return true;
		}
		try
		{
			Message message = new Message(LENH);
			message.writer().writeByte(2);
			message.writer().writeUnsignedByte(chungMinh(hostBytes(), nonce, nonceS));
			GameService.gI().session.sendMessage(message);
			message.cleanup();
		}
		catch (Exception)
		{
			thatBai(n);
			return true;
		}
		dangCho = false;
		if (hetGio != null)
		{
			hetGio.Dispose();
			hetGio = null;
		}
		choPhep = true;
		GameService.gI().login(tk, mk, ver);
		return true;
	}

	/// <summary>Mã máy: chuỗi ngẫu nhiên sinh một lần, lưu PlayerPrefs (chỉ chữ/số, server lọc như vậy).</summary>
	public static string maMay()
	{
		string s = PlayerPrefs.GetString("kpahmay", string.Empty);
		if (s.Length >= 16)
		{
			return s;
		}
		const string chu = "0123456789abcdefghijklmnopqrstuvwxyz";
		byte[] b = new byte[24];
		using (RandomNumberGenerator rng = RandomNumberGenerator.Create())
		{
			rng.GetBytes(b);
		}
		StringBuilder sb = new StringBuilder("unity");
		for (int i = 0; i < b.Length; i++)
		{
			sb.Append(chu[b[i] % chu.Length]);
		}
		s = sb.ToString();
		PlayerPrefs.SetString("kpahmay", s);
		PlayerPrefs.Save();
		return s;
	}

	private static void thatBai(int n)
	{
		if (n != lan || !dangCho)
		{
			return;
		}
		dangCho = false;
		Canvas.gameScr.onConnectFail();
	}

	private static byte[] hostBytes()
	{
		return Encoding.UTF8.GetBytes(host);
	}

	/// <summary>N dạng 128 byte, byte cao trước.</summary>
	private static byte[] nBytes()
	{
		byte[] b = new byte[128];
		for (int i = 0; i < 128; i++)
		{
			int j = 127 - i;
			b[i] = (byte)(N[j >> 2] >> ((j & 3) << 3));
		}
		return b;
	}

	/// <summary>128 byte byte cao trước -> số không âm.</summary>
	private static BigInteger thanhSo(byte[] beBytes)
	{
		byte[] le = new byte[beBytes.Length + 1];
		for (int i = 0; i < beBytes.Length; i++)
		{
			le[i] = beBytes[beBytes.Length - 1 - i];
		}
		return new BigInteger(le);
	}

	private static bool kiem(byte[] chuKy)
	{
		BigInteger n = thanhSo(nBytes());
		BigInteger s = thanhSo(chuKy);
		if (s >= n)
		{
			return false;
		}
		return BigInteger.ModPow(s, 65537, n) == thanhSo(dongGoi());
	}

	/// <summary>00 01 FF..FF 00 MAGIC host nC nS - phải giống hệt XacThucClient.dongGoi() bên server.</summary>
	private static byte[] dongGoi()
	{
		byte[] h = hostBytes();
		byte[] kq = new byte[128];
		int o = 128 - MAGIC.Length - h.Length - nonce.Length - nonceS.Length;
		kq[1] = 1;
		for (int i = 2; i < o - 1; i++)
		{
			kq[i] = byte.MaxValue;
		}
		Buffer.BlockCopy(MAGIC, 0, kq, o, MAGIC.Length);
		o += MAGIC.Length;
		Buffer.BlockCopy(h, 0, kq, o, h.Length);
		o += h.Length;
		Buffer.BlockCopy(nonce, 0, kq, o, nonce.Length);
		Buffer.BlockCopy(nonceS, 0, kq, o + nonce.Length, nonceS.Length);
		return kq;
	}

	/// <summary>k = SHA-256(N 128 byte | MAU_NEN); trả SHA-256(k | nS | nC | host | k).</summary>
	public static byte[] chungMinh(byte[] h, byte[] nC, byte[] nS)
	{
		byte[] goc = new byte[128 + MAU_NEN.Length * 4];
		Buffer.BlockCopy(nBytes(), 0, goc, 0, 128);
		for (int i = 0; i < MAU_NEN.Length; i++)
		{
			int v = MAU_NEN[i];
			goc[128 + i * 4] = (byte)(v >> 24);
			goc[129 + i * 4] = (byte)(v >> 16);
			goc[130 + i * 4] = (byte)(v >> 8);
			goc[131 + i * 4] = (byte)v;
		}
		using (SHA256 sha = SHA256.Create())
		{
			byte[] k = sha.ComputeHash(goc);
			byte[] b = new byte[96 + h.Length];
			Buffer.BlockCopy(k, 0, b, 0, 32);
			Buffer.BlockCopy(nS, 0, b, 32, 16);
			Buffer.BlockCopy(nC, 0, b, 48, 16);
			Buffer.BlockCopy(h, 0, b, 64, h.Length);
			Buffer.BlockCopy(k, 0, b, 64 + h.Length, 32);
			return sha.ComputeHash(b);
		}
	}
}
