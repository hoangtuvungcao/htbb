using System;
using System.Collections;
using System.Diagnostics;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

/// <summary>
/// Tự cập nhật bản PC: lúc mở game đọc https://raw.githubusercontent.com/kpah-server/nqsh-h2-tai/main/phienban.json (repo public kpah-server/nqsh-h2-tai, do phat-hanh.sh ghi; không qua Nova WAF),
/// mục "pc": {"ban", "link", "sha256", "ghiChu"}. "ban" lớn hơn Application.version (số bản, đặt lúc
/// build qua KPAH_VERSION_CODE) thì tải zip, kiểm sha256, rồi chạy một script PowerShell chờ game
/// thoát, giải nén đè lên thư mục game và mở lại game. Không có mạng / lỗi thì game chạy bình thường.
/// </summary>
public class CapNhat : MonoBehaviour
{
	/// <summary>Số bản đã tải + cài gần nhất (chống vòng lặp cập nhật).</summary>
	private const string KHOA_DA_THU = "capnhat_da_thu";

	private const string LINK = "https://raw.githubusercontent.com/kpah-server/nqsh-h2-tai/main/phienban.json";

	/// <summary>Đang tải bản mới: Main bỏ qua chuột / phím của game.</summary>
	public static bool dangCapNhat;

	private string thongBao = string.Empty;

	[Serializable]
	private class BanPc
	{
		public int ban;

		public string link;

		public string sha256;

		public string ghiChu;
	}

	[Serializable]
	private class PhienBan
	{
		public BanPc pc;
	}

	public static void batDau(GameObject go)
	{
		if (Application.platform == RuntimePlatform.WindowsPlayer)
		{
			go.AddComponent<CapNhat>();
		}
	}

	private IEnumerator Start()
	{
		int dangCai;
		int.TryParse(Application.version, out dangCai);
		string json;
		// biến môi trường KPAH_CAPNHAT_URL chỉ để thử trên máy (trỏ tới phienban.json cục bộ)
		string link = Environment.GetEnvironmentVariable("KPAH_CAPNHAT_URL");
		if (string.IsNullOrEmpty(link))
		{
			link = LINK;
		}
		using (UnityWebRequest r = UnityWebRequest.Get(link + "?t=" + DateTime.UtcNow.Ticks))
		{
			r.timeout = 8;
			yield return r.SendWebRequest();
			if (r.result != UnityWebRequest.Result.Success)
			{
				yield break;
			}
			json = r.downloadHandler.text;
		}
		BanPc moi = null;
		try
		{
			PhienBan pb = JsonUtility.FromJson<PhienBan>(json);
			moi = pb == null ? null : pb.pc;
		}
		catch (Exception ex)
		{
			UnityEngine.Debug.LogWarning("CapNhat: " + ex.Message);
		}
		UnityEngine.Debug.Log("CapNhat: dang cai " + dangCai + ", moi nhat " + (moi == null ? "?" : moi.ban.ToString()));
		if (moi == null || moi.ban <= dangCai || string.IsNullOrEmpty(moi.link))
		{
			yield break;
		}
		// chống lặp: đã cài bản này một lần mà mở lại vẫn chưa lên số bản đó (bản build ghi sai số)
		// thì không tải lại nữa, chạy bản đang có; bản phát hành sau (số lớn hơn) sẽ thử lại
		if (PlayerPrefs.GetInt(KHOA_DA_THU, 0) >= moi.ban)
		{
			UnityEngine.Debug.LogWarning("CapNhat: da cai ban " + moi.ban + " mot lan nhung van la " + dangCai + " - bo qua");
			yield break;
		}

		dangCapNhat = true;
		// hiện bằng hộp thoại chờ của chính game (khung + biểu tượng xoay) nên phải đợi game dựng xong Canvas
		while (!Main.started || Canvas.currentScreen == null)
		{
			yield return null;
		}
		thongBao = string.IsNullOrEmpty(moi.ghiChu) ? "Có bản cập nhật mới" : moi.ghiChu;
		string zip = Path.Combine(Application.temporaryCachePath, "capnhat-" + moi.ban + ".zip");
		using (UnityWebRequest r = new UnityWebRequest(moi.link, UnityWebRequest.kHttpVerbGET, new DownloadHandlerFile(zip), null))
		{
			r.SendWebRequest();
			int daHien = -1;
			while (!r.isDone)
			{
				int pt = (int)(r.downloadProgress * 100f);
				if (pt != daHien)
				{
					daHien = pt;
					hienHop("Đang tải: " + pt + "%");
				}
				yield return null;
			}
			if (r.result != UnityWebRequest.Result.Success)
			{
				baoLoi("Không tải được bản mới, game sẽ chạy bản hiện tại.");
				yield break;
			}
		}
		if (!string.IsNullOrEmpty(moi.sha256) && !string.Equals(sha256(zip), moi.sha256.Trim(), StringComparison.OrdinalIgnoreCase))
		{
			baoLoi("File tải về bị lỗi, game sẽ chạy bản hiện tại.");
			yield break;
		}
		hienHop("Đang cài, game sẽ tự mở lại...");
		yield return new WaitForSeconds(0.5f);
		try
		{
			PlayerPrefs.SetInt(KHOA_DA_THU, moi.ban);
			PlayerPrefs.Save();
			chayCaiDat(zip);
			Application.Quit();
		}
		catch (Exception ex)
		{
			UnityEngine.Debug.LogWarning("CapNhat: " + ex);
			baoLoi("Không cài được bản mới, game sẽ chạy bản hiện tại.");
		}
	}

	/// <summary>Hộp thoại chờ của game: ghi chú bản mới + dòng trạng thái, không có nút (đang cập nhật thì chặn bấm).</summary>
	private void hienHop(string trangThai)
	{
		string[] ghiChu = MFont.arialFont.splitFontBStrInLine(thongBao, Canvas.w / 2);
		string[] dong = new string[ghiChu.Length + 1];
		Array.Copy(ghiChu, dong, ghiChu.Length);
		dong[ghiChu.Length] = trangThai;
		Canvas.msgdlg.isIcon = true;
		Canvas.msgdlg.setInfo(dong, null, null, null);
		Canvas.msgdlg.show();
	}

	private void baoLoi(string s)
	{
		dangCapNhat = false;
		Canvas.endDlg();
		Canvas.startOKDlg(s);
	}

	private static string sha256(string file)
	{
		using (FileStream f = File.OpenRead(file))
		using (SHA256 sha = SHA256.Create())
		{
			byte[] h = sha.ComputeHash(f);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < h.Length; i++)
			{
				sb.Append(h[i].ToString("x2"));
			}
			return sb.ToString();
		}
	}

	private static string q(string s)
	{
		return "'" + s.Replace("'", "''") + "'";
	}

	/// <summary>Script chờ game thoát -> giải nén đè -> mở exe có trong bản mới (lỗi thì mở lại bản cũ).</summary>
	private static void chayCaiDat(string zip)
	{
		string thuMucGame = Path.GetDirectoryName(Application.dataPath);
		string exeCu = Process.GetCurrentProcess().MainModule.FileName;
		string tam = Path.Combine(Application.temporaryCachePath, "capnhat-giainen");
		string log = Path.Combine(Application.temporaryCachePath, "capnhat-loi.txt");
		string ps1 = Path.Combine(Application.temporaryCachePath, "capnhat.ps1");
		string s = "$ErrorActionPreference = 'Stop'\r\n"
			+ "$exe = " + q(exeCu) + "\r\n"
			+ "try {\r\n"
			+ "  Wait-Process -Id " + Process.GetCurrentProcess().Id + " -Timeout 60 -ErrorAction SilentlyContinue\r\n"
			+ "  Start-Sleep -Milliseconds 500\r\n"
			+ "  $tam = " + q(tam) + "\r\n"
			+ "  if (Test-Path -LiteralPath $tam) { Remove-Item -LiteralPath $tam -Recurse -Force }\r\n"
			+ "  Expand-Archive -LiteralPath " + q(zip) + " -DestinationPath $tam -Force\r\n"
			+ "  Get-ChildItem -LiteralPath $tam | Copy-Item -Destination " + q(thuMucGame) + " -Recurse -Force\r\n"
			+ "  $moi = Get-ChildItem -LiteralPath $tam -Filter *.exe | Where-Object { $_.Name -ne 'UnityCrashHandler64.exe' } | Select-Object -First 1\r\n"
			+ "  if ($moi) { $exe = Join-Path " + q(thuMucGame) + " $moi.Name }\r\n"
			+ "  Remove-Item -LiteralPath $tam -Recurse -Force\r\n"
			+ "  Remove-Item -LiteralPath " + q(zip) + " -Force\r\n"
			+ "} catch { $_ | Out-File -LiteralPath " + q(log) + " }\r\n"
			+ "Start-Process -FilePath $exe -WorkingDirectory " + q(thuMucGame) + "\r\n";
		File.WriteAllText(ps1, s, new UTF8Encoding(true));
		ProcessStartInfo p = new ProcessStartInfo("powershell.exe", "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File \"" + ps1 + "\"");
		p.UseShellExecute = false;
		p.CreateNoWindow = true;
		Process.Start(p);
	}
}
