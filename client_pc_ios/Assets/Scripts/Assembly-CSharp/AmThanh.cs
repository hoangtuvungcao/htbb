using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// AudioManager cua ban PC: phat hieu ung am thanh (SFX) va nhac nen (BGM) tu Resources/am_thanh/.
///
/// Tep am thanh dat o client_pc_ios/Assets/Resources/am_thanh/&lt;ten&gt;.ogg|.wav (Unity nhan ca hai), ten tep
/// dung cac hang so ben duoi - TRUNG ten tep ban jar (client_jar/res/am, sinh boi client_jar/tools/lam_am_thanh.py
/// tu goi Kenney CC0): SFX s_*.wav chep thang duoc; nhac nen ban jar la MIDI (n_*.mid) - Unity KHONG
/// doc MIDI, can xuat ra n_*.ogg (vd fluidsynth + ffmpeg) roi dat cung ten. Thieu tep thi im lang
/// (khong loi; ket qua Resources.Load ke ca null duoc nho).
///
/// Su kien da noi san (hook co chu thich "// am thanh:"):
///   s_danh / s_bidanh / s_chimang / s_hieuqua / s_truot / s_hoimau - PhanHoiChienDau (chu bay len)
///   s_lencap  - GameScr.onLevelUp (nhan vat chinh)
///   s_tienhoa - Char.batDauTienHoa (lenh -92)
///   s_click   - Main.checkInput (bam chuot khi dang mo menu / hop thoai / man ngoai game)
///   nhac nen  - Tilemap.loadMap theo loai ban do (Tilemap.typeTile): n_thitran / n_hang / n_dauchien;
///               man chon may chu / dang nhap: n_tieude
///
/// Am luong luu PlayerPrefs (htbb_am_sfx, htbb_am_nhac: 0..100). Tat bang tham so -htbbIm.
/// </summary>
public static class AmThanh
{
	public const string THU_MUC = "am_thanh/";

	// ---- SFX ----
	public const string DANH = "s_danh";

	/// <summary>Nhan vat chinh bi trung don.</summary>
	public const string BI_DANH = "s_bidanh";

	public const string CHI_MANG = "s_chimang";

	public const string HIEU_QUA_CAO = "s_hieuqua";

	public const string TRUOT = "s_truot";

	public const string HOI_MAU = "s_hoimau";

	public const string LEN_CAP = "s_lencap";

	public const string TIEN_HOA = "s_tienhoa";

	public const string BAM = "s_click";

	// ---- BGM ----
	public const string NHAC_TIEU_DE = "n_tieude";

	/// <summary>Ban do ngoai troi / lang (Tilemap.typeTile 1, map &lt; 110).</summary>
	public const string NHAC_NGOAI_TROI = "n_thitran";

	/// <summary>Hang dong (typeTile 2, map 110..200).</summary>
	public const string NHAC_HANG = "n_hang";

	/// <summary>Thanh / dau truong (typeTile 0, map &gt; 200).</summary>
	public const string NHAC_THANH = "n_dauchien";

	private const string KHOA_SFX = "htbb_am_sfx";

	private const string KHOA_NHAC = "htbb_am_nhac";

	/// <summary>Cung mot SFX khong phat lai trong khoang nay (giay) - tranh don dap khi danh lan.</summary>
	private const float CACH_TOI_THIEU = 0.06f;

	private static readonly Dictionary<string, AudioClip> boNho = new Dictionary<string, AudioClip>();

	private static readonly Dictionary<string, float> lanCuoi = new Dictionary<string, float>();

	private static GameObject goc;

	private static AudioSource nguonSfx;

	private static AudioSource nguonNhac;

	private static string nhacDangPhat;

	private static bool daKiemTat;

	private static bool tat;

	public static float amLuongSfx
	{
		get { return PlayerPrefs.GetInt(KHOA_SFX, 80) / 100f; }
		set
		{
			PlayerPrefs.SetInt(KHOA_SFX, Mathf.Clamp((int)(value * 100f), 0, 100));
		}
	}

	public static float amLuongNhac
	{
		get { return PlayerPrefs.GetInt(KHOA_NHAC, 50) / 100f; }
		set
		{
			PlayerPrefs.SetInt(KHOA_NHAC, Mathf.Clamp((int)(value * 100f), 0, 100));
			if (nguonNhac != null)
			{
				nguonNhac.volume = amLuongNhac;
			}
		}
	}

	private static bool biTat()
	{
		if (!daKiemTat)
		{
			daKiemTat = true;
			tat = System.Array.IndexOf(System.Environment.GetCommandLineArgs(), "-htbbIm") >= 0;
		}
		return tat;
	}

	private static bool sanSang()
	{
		if (biTat())
		{
			return false;
		}
		if (goc == null)
		{
			goc = new GameObject("HTBB_AmThanh");
			Object.DontDestroyOnLoad(goc);
			nguonSfx = goc.AddComponent<AudioSource>();
			nguonSfx.playOnAwake = false;
			nguonNhac = goc.AddComponent<AudioSource>();
			nguonNhac.playOnAwake = false;
			nguonNhac.loop = true;
			nguonNhac.volume = amLuongNhac;
		}
		return true;
	}

	/// <summary>Nap clip (nho ca ket qua null). Tra ve null neu thieu tep.</summary>
	public static AudioClip lay(string ten)
	{
		if (string.IsNullOrEmpty(ten))
		{
			return null;
		}
		AudioClip clip;
		if (boNho.TryGetValue(ten, out clip))
		{
			return clip;
		}
		try
		{
			clip = Resources.Load<AudioClip>(THU_MUC + ten);
		}
		catch (System.Exception)
		{
			clip = null;
		}
		boNho[ten] = clip;
		return clip;
	}

	/// <summary>Phat mot hieu ung am thanh (SFX). An toan khi thieu tep / chua co AudioListener.</summary>
	public static void phat(string ten)
	{
		phat(ten, 1f);
	}

	public static void phat(string ten, float heSo)
	{
		try
		{
			if (!sanSang())
			{
				return;
			}
			float bayGio = Time.unscaledTime;
			float truoc;
			if (lanCuoi.TryGetValue(ten, out truoc) && bayGio - truoc < CACH_TOI_THIEU)
			{
				return;
			}
			AudioClip clip = lay(ten);
			if (clip == null)
			{
				return;
			}
			lanCuoi[ten] = bayGio;
			nguonSfx.PlayOneShot(clip, amLuongSfx * heSo);
		}
		catch (System.Exception)
		{
			// am thanh khong bao gio duoc lam hong game
		}
	}

	/// <summary>Doi nhac nen (giu nguyen neu dang phat dung ban do). null/rong = tat nhac.</summary>
	public static void nhac(string ten)
	{
		try
		{
			if (!sanSang() || ten == nhacDangPhat)
			{
				return;
			}
			nhacDangPhat = ten;
			AudioClip clip = lay(ten);
			if (clip == null)
			{
				nguonNhac.Stop();
				nguonNhac.clip = null;
				return;
			}
			nguonNhac.clip = clip;
			nguonNhac.volume = amLuongNhac;
			nguonNhac.Play();
		}
		catch (System.Exception)
		{
		}
	}

	/// <summary>Nhac nen theo loai ban do (Tilemap.typeTile do Res.loadTileImage dat).</summary>
	public static void nhacTheoMap(int loaiTile)
	{
		switch (loaiTile)
		{
		case 0:
			nhac(NHAC_THANH);
			break;
		case 2:
			nhac(NHAC_HANG);
			break;
		default:
			nhac(NHAC_NGOAI_TROI);
			break;
		}
	}

	/// <summary>Tieng bam giao dien: chi khi dang mo menu / hop thoai / man ngoai game (khong keu moi cu bam di chuyen).</summary>
	public static void bamGiaoDien()
	{
		if (Canvas.menu.showMenu || Canvas.currentDialog != null || Canvas.currentScreen != Canvas.gameScr)
		{
			phat(BAM, 0.6f);
		}
	}
}
