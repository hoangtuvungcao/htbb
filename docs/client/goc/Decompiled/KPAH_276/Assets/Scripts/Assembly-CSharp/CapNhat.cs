using System;
using System.Collections;
using System.Diagnostics;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;

/// <summary>
/// Tự cập nhật bản PC: lúc mở game đọc https://nqsh-h2.top/tai/phienban.json (do phat-hanh.sh ghi),
/// mục "pc": {"ban", "link", "sha256", "ghiChu"}. "ban" lớn hơn Application.version (số bản, đặt lúc
/// build qua KPAH_VERSION_CODE) thì tải zip, kiểm sha256, rồi chạy một script PowerShell chờ game
/// thoát, giải nén đè lên thư mục game và mở lại game. Không có mạng / lỗi thì game chạy bình thường.
/// </summary>
public class CapNhat : MonoBehaviour
{
	private const string LINK = "https://nqsh-h2.top/tai/phienban.json";

	/// <summary>Đang tải bản mới: Main bỏ qua chuột / phím của game.</summary>
	public static bool dangCapNhat;

	private string thongBao = string.Empty;

	private float tienDo;

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

		dangCapNhat = true;
		thongBao = string.IsNullOrEmpty(moi.ghiChu) ? "Đang tải bản mới..." : moi.ghiChu;
		string zip = Path.Combine(Application.temporaryCachePath, "capnhat-" + moi.ban + ".zip");
		using (UnityWebRequest r = new UnityWebRequest(moi.link, UnityWebRequest.kHttpVerbGET, new DownloadHandlerFile(zip), null))
		{
			r.SendWebRequest();
			while (!r.isDone)
			{
				tienDo = r.downloadProgress;
				yield return null;
			}
			if (r.result != UnityWebRequest.Result.Success)
			{
				yield return baoLoi("Không tải được bản mới, game sẽ chạy bản hiện tại.");
				yield break;
			}
		}
		tienDo = 1f;
		if (!string.IsNullOrEmpty(moi.sha256) && !string.Equals(sha256(zip), moi.sha256.Trim(), StringComparison.OrdinalIgnoreCase))
		{
			yield return baoLoi("File tải về bị lỗi, game sẽ chạy bản hiện tại.");
			yield break;
		}
		thongBao = "Đang cài bản mới, game sẽ tự mở lại...";
		yield return null;
		try
		{
			chayCaiDat(zip);
			Application.Quit();
		}
		catch (Exception ex)
		{
			UnityEngine.Debug.LogWarning("CapNhat: " + ex);
			dangCapNhat = false;
		}
	}

	private IEnumerator baoLoi(string s)
	{
		thongBao = s;
		yield return new WaitForSeconds(3f);
		dangCapNhat = false;
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

	private void OnGUI()
	{
		if (!dangCapNhat)
		{
			return;
		}
		GUI.depth = -100;
		GUI.matrix = Matrix4x4.identity;
		float w = Screen.width;
		float h = Screen.height;
		GUI.color = new Color(0f, 0f, 0f, 0.85f);
		GUI.DrawTexture(new Rect(0f, 0f, w, h), Texture2D.whiteTexture);
		GUI.color = Color.white;
		GUIStyle st = new GUIStyle(GUI.skin.label);
		st.fontSize = Mathf.Max(14, (int)(h / 30f));
		st.alignment = TextAnchor.MiddleCenter;
		st.wordWrap = true;
		GUI.Label(new Rect(w * 0.1f, h * 0.38f, w * 0.8f, h * 0.12f), thongBao, st);
		float bw = w * 0.6f;
		float bh = Mathf.Max(8f, h / 40f);
		Rect khung = new Rect((w - bw) / 2f, h * 0.52f, bw, bh);
		GUI.color = new Color(1f, 1f, 1f, 0.25f);
		GUI.DrawTexture(khung, Texture2D.whiteTexture);
		GUI.color = new Color(0.95f, 0.75f, 0.2f, 1f);
		GUI.DrawTexture(new Rect(khung.x, khung.y, khung.width * Mathf.Clamp01(tienDo), khung.height), Texture2D.whiteTexture);
		GUI.color = Color.white;
		GUI.Label(new Rect(w * 0.1f, h * 0.56f, w * 0.8f, h * 0.08f), (int)(Mathf.Clamp01(tienDo) * 100f) + "%", st);
	}
}
