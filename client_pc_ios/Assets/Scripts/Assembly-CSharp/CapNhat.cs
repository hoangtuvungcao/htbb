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

	/// <summary>Số lần script cài báo lỗi liên tiếp (capnhat-loi.txt): dưới 3 thì cho thử lại bản đó.</summary>
	private const string KHOA_SO_LOI = "capnhat_so_loi";

	/// <summary>Tải zip mà quá bấy nhiêu giây không thêm byte nào thì huỷ (mạng treo giữa chừng).</summary>
	private const float TAI_TREO_GIAY = 30f;

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
		// Lần cài trước lỗi (script ghi capnhat-loi.txt, game cũ vẫn mở lại): cho thử lại tối đa 3 lần thay vì
		// bỏ qua bản đó mãi (khoá "đã thử" được ghi ngay khi script bắt đầu chạy).
		string logLoi = Path.Combine(Application.temporaryCachePath, "capnhat-loi.txt");
		try
		{
			if (File.Exists(logLoi))
			{
				UnityEngine.Debug.LogWarning("CapNhat: lan cai truoc loi: " + File.ReadAllText(logLoi));
				File.Delete(logLoi);
				int soLoi = PlayerPrefs.GetInt(KHOA_SO_LOI, 0) + 1;
				PlayerPrefs.SetInt(KHOA_SO_LOI, soLoi);
				if (soLoi < 3)
				{
					PlayerPrefs.DeleteKey(KHOA_DA_THU);
				}
				PlayerPrefs.Save();
			}
		}
		catch (Exception ex)
		{
			UnityEngine.Debug.LogWarning("CapNhat: " + ex.Message);
		}
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
			if (moi != null && moi.ban <= dangCai)
			{
				PlayerPrefs.DeleteKey(KHOA_SO_LOI); // đã lên bản mới nhất
			}
			yield break;
		}
		// Bắt buộc có sha256 (64 hex): không có thì không cài file không kiểm được (zip hỏng / bị tráo)
		if (moi.sha256 == null || moi.sha256.Trim().Length != 64)
		{
			UnityEngine.Debug.LogWarning("CapNhat: phienban.json thieu sha256 - bo qua");
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
			ulong daTai = 0;
			float lucCoByte = Time.realtimeSinceStartup;
			bool treo = false;
			while (!r.isDone)
			{
				int pt = (int)(r.downloadProgress * 100f);
				if (pt != daHien)
				{
					daHien = pt;
					hienHop("Đang tải: " + pt + "%");
				}
				// Mạng treo giữa chừng: UnityWebRequest mặc định không có hạn -> game kẹt hộp thoại, chặn bấm mãi
				if (r.downloadedBytes != daTai)
				{
					daTai = r.downloadedBytes;
					lucCoByte = Time.realtimeSinceStartup;
				}
				else if (Time.realtimeSinceStartup - lucCoByte > TAI_TREO_GIAY)
				{
					treo = true;
					r.Abort();
					break;
				}
				yield return null;
			}
			if (treo || r.result != UnityWebRequest.Result.Success)
			{
				baoLoi("Không tải được bản mới, game sẽ chạy bản hiện tại.");
				yield break;
			}
		}
		if (!string.Equals(sha256(zip), moi.sha256.Trim(), StringComparison.OrdinalIgnoreCase))
		{
			baoLoi("File tải về bị lỗi, game sẽ chạy bản hiện tại.");
			yield break;
		}
		hienHop("Đang cài, game sẽ tự mở lại...");
		yield return new WaitForSeconds(0.5f);
		try
		{
			// ghi "đã thử" SAU khi chạy được script cài - trước đây ghi trước, cài lỗi là bỏ qua bản này mãi
			chayCaiDat(zip);
			PlayerPrefs.SetInt(KHOA_DA_THU, moi.ban);
			PlayerPrefs.Save();
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

	/// <summary>File .exe của game trong thư mục (bỏ UnityCrashHandler64.exe); không thấy thì NQSH-H2.exe.</summary>
	private static string timExe(string thuMuc)
	{
		try
		{
			foreach (string f in Directory.GetFiles(thuMuc, "*.exe"))
			{
				if (!string.Equals(Path.GetFileName(f), "UnityCrashHandler64.exe", StringComparison.OrdinalIgnoreCase))
				{
					return f;
				}
			}
		}
		catch (Exception)
		{
		}
		return Path.Combine(thuMuc, "NQSH-H2.exe");
	}

	/// <summary>Script chờ game thoát -> giải nén đè -> mở exe có trong bản mới (lỗi thì mở lại bản cũ).</summary>
	private static void chayCaiDat(string zip)
	{
		string thuMucGame = Path.GetDirectoryName(Application.dataPath);
		// Không dùng Process.GetCurrentProcess().MainModule: bản build Unity hiện tại ném
		// ArgumentNullException ở Process.get_Modules -> không bao giờ cài được. Lấy exe trong thư mục game.
		string exeCu = timExe(thuMucGame);
		string tam = Path.Combine(Application.temporaryCachePath, "capnhat-giainen");
		string log = Path.Combine(Application.temporaryCachePath, "capnhat-loi.txt");
		string ps1 = Path.Combine(Application.temporaryCachePath, "capnhat.ps1");
		int pid = Process.GetCurrentProcess().Id;
		// Cài an toàn: chờ game tắt HẲN (không thì huỷ, không đụng file đang dùng); đổi tên mọi mục sẽ bị thay thành
		// *.capnhat-bak rồi mới chép; chép lỗi thì xoá phần chép dở, đổi .bak về tên cũ (game cũ vẫn chạy). Trước đây
		// xoá thẳng *_Data / GameAssembly.dll rồi mới chép: chép lỗi (antivirus khoá file, đầy đĩa) là hỏng bản cài.
		string s = ""
			+ "$ErrorActionPreference = 'Stop'\r\n"
			+ "$exe = " + q(exeCu) + "\r\n"
			+ "$game = " + q(thuMucGame) + "\r\n"
			+ "$tam = " + q(tam) + "\r\n"
			+ "$daDoi = @()\r\n"
			+ "$moGame = $true\r\n"
			+ "try {\r\n"
			+ "  Wait-Process -Id " + pid + " -Timeout 60 -ErrorAction SilentlyContinue\r\n"
			+ "  if (Get-Process -Id " + pid + " -ErrorAction SilentlyContinue) { $moGame = $false; throw 'game chua tat sau 60 s' }\r\n"
			+ "  Start-Sleep -Milliseconds 500\r\n"
			+ "  if (Test-Path -LiteralPath $tam) { Remove-Item -LiteralPath $tam -Recurse -Force }\r\n"
			+ "  Expand-Archive -LiteralPath " + q(zip) + " -DestinationPath $tam -Force\r\n"
			+ "  $ten = @('GameAssembly.dll', 'baselib.dll', 'MonoBleedingEdge') + @(Get-ChildItem -LiteralPath $tam | ForEach-Object { $_.Name }) | Select-Object -Unique\r\n"
			+ "  foreach ($t in $ten) {\r\n"
			+ "    $cu = Join-Path $game $t\r\n"
			+ "    if (Test-Path -LiteralPath $cu) {\r\n"
			+ "      $bak = $cu + '.capnhat-bak'\r\n"
			+ "      if (Test-Path -LiteralPath $bak) { Remove-Item -LiteralPath $bak -Recurse -Force }\r\n"
			+ "      for ($i = 0; $true; $i++) { try { Rename-Item -LiteralPath $cu -NewName ($t + '.capnhat-bak'); break } catch { if ($i -ge 20) { throw }; Start-Sleep -Milliseconds 500 } }\r\n"
			+ "      $daDoi += $t\r\n"
			+ "    }\r\n"
			+ "  }\r\n"
			+ "  Get-ChildItem -LiteralPath $tam | Copy-Item -Destination $game -Recurse -Force\r\n"
			+ "  foreach ($t in $daDoi) { Remove-Item -LiteralPath (Join-Path $game ($t + '.capnhat-bak')) -Recurse -Force -ErrorAction SilentlyContinue }\r\n"
			+ "  $moi = Get-ChildItem -LiteralPath $tam -Filter *.exe | Where-Object { $_.Name -ne 'UnityCrashHandler64.exe' } | Select-Object -First 1\r\n"
			+ "  if ($moi) { $exe = Join-Path $game $moi.Name }\r\n"
			+ "  Remove-Item -LiteralPath $tam -Recurse -Force -ErrorAction SilentlyContinue\r\n"
			+ "  Remove-Item -LiteralPath " + q(zip) + " -Force -ErrorAction SilentlyContinue\r\n"
			+ "} catch {\r\n"
			+ "  $_ | Out-File -LiteralPath " + q(log) + "\r\n"
			+ "  if (Test-Path -LiteralPath $tam) { Get-ChildItem -LiteralPath $tam | ForEach-Object { $n = Join-Path $game $_.Name; if (Test-Path -LiteralPath ($n + '.capnhat-bak')) { Remove-Item -LiteralPath $n -Recurse -Force -ErrorAction SilentlyContinue } } }\r\n"
			+ "  foreach ($t in $daDoi) { $cu = Join-Path $game $t; if (-not (Test-Path -LiteralPath $cu)) { Rename-Item -LiteralPath ($cu + '.capnhat-bak') -NewName $t -ErrorAction SilentlyContinue } }\r\n"
			+ "}\r\n"
			+ "if ($moGame) { Start-Process -FilePath $exe -WorkingDirectory $game }\r\n";
		File.WriteAllText(ps1, s, new UTF8Encoding(true));
		ProcessStartInfo p = new ProcessStartInfo("powershell.exe", "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File \"" + ps1 + "\"");
		p.UseShellExecute = false;
		p.CreateNoWindow = true;
		Process.Start(p);
	}
}
