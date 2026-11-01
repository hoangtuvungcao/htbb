using System.Linq;
using UnityEditor;
using UnityEditor.Build.Reporting;

public static class BuildCli
{
    /// <summary>Đường ra: biến HTBB_OUT (phat-hanh.sh trên máy) hoặc -customBuildPath (GameCI trên GitHub Actions).</summary>
    private static string duongRa()
    {
        return thamSo("-customBuildPath") ?? System.Environment.GetEnvironmentVariable("HTBB_OUT");
    }

    private static string thamSo(string ten)
    {
        string[] args = System.Environment.GetCommandLineArgs();
        int i = System.Array.IndexOf(args, ten);
        return i >= 0 && i + 1 < args.Length ? args[i + 1] : null;
    }

    /// <summary>
    /// Tên hiển thị cho người chơi (Huyền Thoại Bảo Bối) lấy từ ThuongHieu.cs - đổi tên game ở MỘT chỗ.
    /// productName không dấu (tên thư mục lưu PlayerPrefs / persistentDataPath); tiêu đề cửa sổ có dấu do
    /// TieuDeCuaSo.cs đặt lúc chạy. Không đổi applicationIdentifier / tên file build (hạ tầng phát hành).
    /// </summary>
    /// <summary>
    /// Key art parallax (màn tải / đăng nhập / chọn Pokémon - BannerParallax.cs): chép client_jar/shared/banner/lop/
    /// (*.png -> *.bytes để Unity không nén / lọc ảnh pixel art; lop.txt) và client_jar/shared/brand/logo_512.png (-> logo.bytes)
    /// vào Assets/Resources/banner/. Chép lại khi nguồn mới hơn. Không có nguồn thì bỏ qua - game tự vẽ cảnh đơn giản.
    /// </summary>
    private static void chepBanner()
    {
        try
        {
            string goc = System.IO.Path.GetFullPath(System.IO.Path.Combine(UnityEngine.Application.dataPath, "../../shared"));
            string nguon = System.IO.Path.Combine(goc, "banner/lop");
            string dich = System.IO.Path.Combine(UnityEngine.Application.dataPath, "Resources/banner");
            if (!System.IO.Directory.Exists(nguon))
            {
                return;
            }
            System.IO.Directory.CreateDirectory(dich);
            int n = 0;
            foreach (string f in System.IO.Directory.GetFiles(nguon))
            {
                string ten = System.IO.Path.GetFileName(f);
                string duoi = System.IO.Path.GetExtension(f).ToLowerInvariant();
                if (duoi == ".png")
                {
                    n += chepNeuMoi(f, System.IO.Path.Combine(dich, System.IO.Path.GetFileNameWithoutExtension(f) + ".bytes"));
                }
                else if (ten == "lop.txt")
                {
                    n += chepNeuMoi(f, System.IO.Path.Combine(dich, ten));
                }
            }
            string logo = System.IO.Path.Combine(goc, "brand/logo_512.png");
            if (System.IO.File.Exists(logo))
            {
                n += chepNeuMoi(logo, System.IO.Path.Combine(dich, "logo.bytes"));
            }
            if (n > 0)
            {
                AssetDatabase.Refresh();
                UnityEngine.Debug.Log("HTBB banner: da chep " + n + " tep " + nguon + " -> " + dich);
            }
        }
        catch (System.Exception e)
        {
            UnityEngine.Debug.LogWarning("HTBB banner: " + e.Message);
        }
    }

    private static int chepNeuMoi(string nguon, string dich)
    {
        if (System.IO.File.Exists(dich) && System.IO.File.GetLastWriteTimeUtc(dich) >= System.IO.File.GetLastWriteTimeUtc(nguon))
        {
            return 0;
        }
        System.IO.File.Copy(nguon, dich, true);
        return 1;
    }

    private static void datThuongHieu()
    {
        chepBanner();
        PlayerSettings.companyName = ThuongHieu.TEN_CONG_TY;
        PlayerSettings.productName = ThuongHieu.tenSanPham();
        UnityEngine.Debug.Log("HTBB san pham: " + PlayerSettings.companyName + " / " + PlayerSettings.productName);
    }

    /// <summary>KHOA=giatri trong một tệp cấu hình của tools/phat-hanh/ (không có tệp / khoá = null).</summary>
    private static string docConf(string tep, string khoa)
    {
        try
        {
            string duong = System.IO.Path.GetFullPath(System.IO.Path.Combine(UnityEngine.Application.dataPath, "../../tools/phat-hanh", tep));
            if (!System.IO.File.Exists(duong))
            {
                return null;
            }
            string kq = null;
            foreach (string dong in System.IO.File.ReadAllLines(duong))
            {
                string d = dong.Trim();
                if (d.StartsWith(khoa + "="))
                {
                    kq = d.Substring(khoa.Length + 1).Trim().Trim('"');
                }
            }
            return string.IsNullOrEmpty(kq) ? null : kq;
        }
        catch (System.Exception e)
        {
            UnityEngine.Debug.LogWarning("HTBB conf " + tep + ": " + e.Message);
            return null;
        }
    }

    private static string khongRong(params string[] ds)
    {
        foreach (string s in ds)
        {
            if (!string.IsNullOrEmpty(s) && s.Trim().Length > 0)
            {
                return s.Trim();
            }
        }
        return null;
    }

    private const string TEP_PHAT_HANH = "Assets/Resources/" + ThuongHieu.TEP_PHAT_HANH + ".txt";

    /// <summary>
    /// Máy chủ + kênh cập nhật + phiên bản cho BẢN PHÁT HÀNH, đóng vào Resources/htbb_phat_hanh.txt (ThuongHieu đọc
    /// lúc chạy). Máy chủ: -htbbMayChu (GameCI customParameters) > biến MAY_CHU_CHINH > tools/phat-hanh/may_chu.conf >
    /// ThuongHieu.MAY_CHU_CHINH - thiếu là DỪNG build (trước đây rơi về danh sách server của game gốc).
    /// Kênh cập nhật: -htbbCapNhat > CAP_NHAT_URL > may_chu.conf; rỗng = tắt tự cập nhật; kênh phát hành cũ của game gốc (ThuongHieu.laKenhCu) = DỪNG.
    /// Trả false nếu cấu hình sai (đã ghi lỗi).
    /// </summary>
    private static bool dongCauHinhPhatHanh(out string ban)
    {
        string mayChu = khongRong(thamSo("-htbbMayChu"), System.Environment.GetEnvironmentVariable("MAY_CHU_CHINH"),
            docConf("may_chu.conf", "MAY_CHU_CHINH"), ThuongHieu.MAY_CHU_CHINH);
        string capNhat = khongRong(thamSo("-htbbCapNhat"), System.Environment.GetEnvironmentVariable("CAP_NHAT_URL"),
            docConf("may_chu.conf", "CAP_NHAT_URL"), ThuongHieu.CAP_NHAT_URL) ?? string.Empty;
        string phienBan = khongRong(System.Environment.GetEnvironmentVariable("HTBB_PHIEN_BAN"),
            docConf("phien_ban.conf", "PHIEN_BAN"), ThuongHieu.PHIEN_BAN);
        // số bản (CapNhat.cs so với "pc.ban" trong phienban.json): tham số -htbbBan (GitHub Actions - GameCI
        // KHÔNG chuyển biến môi trường vào container Unity) hoặc biến HTBB_VERSION_CODE (phat-hanh.sh trên máy)
        // hoặc BAN trong tools/phat-hanh/phien_ban.conf
        ban = khongRong(thamSo("-htbbBan"), System.Environment.GetEnvironmentVariable("HTBB_VERSION_CODE"),
            docConf("phien_ban.conf", "BAN"));
        if (mayChu == null || mayChu.IndexOf(':') <= 0)
        {
            UnityEngine.Debug.LogError("HTBB LOI: ban phat hanh PC CAN may chu production host:cong. Dat -htbbMayChu host:cong "
                + "(GameCI customParameters), bien MAY_CHU_CHINH, hoac MAY_CHU_CHINH= trong tools/phat-hanh/may_chu.conf "
                + "(mau: may_chu.conf.example). Hien tai: '" + mayChu + "'");
            return false;
        }
        if (ThuongHieu.laKenhCu(mayChu) || ThuongHieu.laKenhCu(capNhat))
        {
            UnityEngine.Debug.LogError("HTBB LOI: may chu / kenh cap nhat tro ve kenh cu cua game goc (" + mayChu + " / " + capNhat + ") - khong build ban HTBB");
            return false;
        }
        int so;
        if (ban != null && !int.TryParse(ban, out so))
        {
            UnityEngine.Debug.LogError("HTBB LOI: so ban '" + ban + "' khong phai so nguyen");
            return false;
        }
        string p2pTicket = khongRong(thamSo("-htbbP2PTicket"), System.Environment.GetEnvironmentVariable("P2P_TICKET"));
        if (p2pTicket != null && !p2pTicket.StartsWith("pvt_"))
        {
            UnityEngine.Debug.LogError("HTBB LOI: P2P_TICKET phai bat dau bang pvt_");
            return false;
        }
        System.IO.Directory.CreateDirectory(System.IO.Path.GetDirectoryName(TEP_PHAT_HANH));
        System.IO.File.WriteAllText(TEP_PHAT_HANH, "# Sinh boi BuildCli.Win64 luc build - KHONG sua tay, KHONG dua len git\n"
            + "MAY_CHU_CHINH=" + mayChu + "\nCAP_NHAT_URL=" + capNhat + "\nPHIEN_BAN=" + phienBan + "\n"
            // nhanh p2p: ticket Peerova (-htbbP2PTicket / bien P2P_TICKET) -> P2PPeerova.cs noi game qua P2P
            + (p2pTicket != null ? "P2P_TICKET=" + p2pTicket + "\n" : ""));
        AssetDatabase.ImportAsset(TEP_PHAT_HANH);
        UnityEngine.Debug.Log("HTBB phat hanh: may chu " + mayChu + ", cap nhat " + (capNhat.Length > 0 ? capNhat : "(TAT)")
            + ", phien ban " + phienBan);
        return true;
    }

    private static void xoaCauHinhPhatHanh()
    {
        // không để máy chủ production lại trong Editor (Play trong Editor sẽ vào server thật)
        AssetDatabase.DeleteAsset(TEP_PHAT_HANH);
    }

    public static void Win64()
    {
        datThuongHieu();
        string ban;
        if (!dongCauHinhPhatHanh(out ban))
        {
            UnityEngine.Debug.Log("BUILD RESULT: Failed errors=1 (cau hinh phat hanh)");
            EditorApplication.Exit(1);
            return;
        }
        string[] scenes = EditorBuildSettings.scenes.Where(s => s.enabled).Select(s => s.path).ToArray();
        string outPath = duongRa();
        // IL2CPP: C# dịch ra C++ rồi mã máy - chạy nhanh hơn Mono và khó dịch ngược hơn (DLL không còn mã C#).
        // Cần module "Windows Build Support (IL2CPP)" của Unity + Visual Studio C++ (máy GitHub Windows có sẵn).
        // Máy chưa cài module (hoặc truyền -htbbMono) thì build Mono như trước.
        var standalone = UnityEditor.Build.NamedBuildTarget.Standalone;
        bool coIl2cpp = System.IO.Directory.Exists(System.IO.Path.Combine(EditorApplication.applicationContentsPath,
            "PlaybackEngines/windowsstandalonesupport/Variations/win64_player_nondevelopment_il2cpp"));
        if (coIl2cpp && System.Array.IndexOf(System.Environment.GetCommandLineArgs(), "-htbbMono") < 0)
        {
            PlayerSettings.SetScriptingBackend(standalone, ScriptingImplementation.IL2CPP);
            PlayerSettings.SetIl2CppCompilerConfiguration(standalone, Il2CppCompilerConfiguration.Release);
            PlayerSettings.SetIl2CppCodeGeneration(standalone, UnityEditor.Build.Il2CppCodeGeneration.OptimizeSpeed);
            PlayerSettings.SetManagedStrippingLevel(standalone, ManagedStrippingLevel.Low);
        }
        else
        {
            PlayerSettings.SetScriptingBackend(standalone, ScriptingImplementation.Mono2x);
        }
        UnityEngine.Debug.Log("HTBB scripting backend: " + PlayerSettings.GetScriptingBackend(standalone));
        if (!string.IsNullOrEmpty(ban))
        {
            PlayerSettings.bundleVersion = ban;
        }
        UnityEngine.Debug.Log("HTBB ban (bundleVersion): " + PlayerSettings.bundleVersion);
        AssetDatabase.ImportAsset("Assets/Icon/app_icon.png");
        UnityEngine.Texture2D icon = AssetDatabase.LoadAssetAtPath<UnityEngine.Texture2D>("Assets/Icon/app_icon.png");
        PlayerSettings.SetIcons(UnityEditor.Build.NamedBuildTarget.Unknown, new UnityEngine.Texture2D[] { icon }, IconKind.Any);
        PlayerSettings.fullScreenMode = UnityEngine.FullScreenMode.Windowed;
        PlayerSettings.resizableWindow = true;
        PlayerSettings.allowFullscreenSwitch = true;
        BuildReport r = BuildPipeline.BuildPlayer(scenes, outPath, BuildTarget.StandaloneWindows64, BuildOptions.None);
        xoaCauHinhPhatHanh();
        UnityEngine.Debug.Log("BUILD RESULT: " + r.summary.result + " errors=" + r.summary.totalErrors);
        // số bản thật đã đóng vào bản build, ghi CẠNH thư mục build (không vào zip) để workflow kiểm tra
        string thuMuc = System.IO.Path.GetDirectoryName(System.IO.Path.GetFullPath(outPath));
        // ghi công đọc được ngay trong zip phát hành (CC BY-NC 4.0 của PMDCollab), cạnh .exe
        try
        {
            string ghiCong = System.IO.Path.Combine(UnityEngine.Application.dataPath, "Resources/ghicong/GHI_CONG.txt");
            if (System.IO.File.Exists(ghiCong))
            {
                System.IO.File.Copy(ghiCong, System.IO.Path.Combine(thuMuc, "GHI_CONG.txt"), true);
            }
            else
            {
                UnityEngine.Debug.LogError("HTBB: thieu Assets/Resources/ghicong/GHI_CONG.txt (chay client_jar/tools/sinh_ghi_cong.py)");
            }
        }
        catch (System.Exception e)
        {
            UnityEngine.Debug.LogError("HTBB ghi cong: " + e.Message);
        }
        System.IO.File.WriteAllText(System.IO.Path.Combine(System.IO.Path.GetDirectoryName(thuMuc), "htbb-ban.txt"), PlayerSettings.bundleVersion);
        EditorApplication.Exit(r.summary.result == BuildResult.Succeeded ? 0 : 1);
    }

    /// <summary>APK Android: IL2CPP ARM64 + ARMv7, màn hình ngang, ký bằng khóa riêng (HTBB_KEY_DIR/key.properties).</summary>
    public static void Android()
    {
        datThuongHieu();
        string[] scenes = EditorBuildSettings.scenes.Where(s => s.enabled).Select(s => s.path).ToArray();
        string outPath = System.Environment.GetEnvironmentVariable("HTBB_OUT");
        string keyDir = System.Environment.GetEnvironmentVariable("HTBB_KEY_DIR");
        var key = System.IO.File.ReadAllLines(System.IO.Path.Combine(keyDir, "key.properties"))
            .Where(l => l.Contains("=")).ToDictionary(l => l.Substring(0, l.IndexOf('=')), l => l.Substring(l.IndexOf('=') + 1));

        AssetDatabase.ImportAsset("Assets/Icon/app_icon.png");
        UnityEngine.Texture2D icon = AssetDatabase.LoadAssetAtPath<UnityEngine.Texture2D>("Assets/Icon/app_icon.png");
        PlayerSettings.SetIcons(UnityEditor.Build.NamedBuildTarget.Unknown, new UnityEngine.Texture2D[] { icon }, IconKind.Any);

        var android = UnityEditor.Build.NamedBuildTarget.Android;
        PlayerSettings.SetApplicationIdentifier(android, System.Environment.GetEnvironmentVariable("HTBB_APP_ID") ?? ThuongHieu.maUngDung());
        PlayerSettings.bundleVersion = System.Environment.GetEnvironmentVariable("HTBB_VERSION") ?? "1.0";
        PlayerSettings.Android.bundleVersionCode = int.Parse(System.Environment.GetEnvironmentVariable("HTBB_VERSION_CODE") ?? "1");
        PlayerSettings.SetScriptingBackend(android, ScriptingImplementation.IL2CPP);
        PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64 | AndroidArchitecture.ARMv7;
        PlayerSettings.defaultInterfaceOrientation = UIOrientation.AutoRotation;
        PlayerSettings.allowedAutorotateToPortrait = false;
        PlayerSettings.allowedAutorotateToPortraitUpsideDown = false;
        PlayerSettings.allowedAutorotateToLandscapeLeft = true;
        PlayerSettings.allowedAutorotateToLandscapeRight = true;
        PlayerSettings.Android.forceInternetPermission = true;
        PlayerSettings.Android.useCustomKeystore = true;
        PlayerSettings.Android.keystoreName = System.IO.Path.Combine(keyDir, key["keystore"]);
        PlayerSettings.Android.keystorePass = key["password"];
        PlayerSettings.Android.keyaliasName = key["alias"];
        PlayerSettings.Android.keyaliasPass = key["password"];
        EditorUserBuildSettings.buildAppBundle = false;

        BuildReport r = BuildPipeline.BuildPlayer(scenes, outPath, BuildTarget.Android, BuildOptions.None);
        UnityEngine.Debug.Log("BUILD RESULT: " + r.summary.result + " errors=" + r.summary.totalErrors);
        EditorApplication.Exit(r.summary.result == BuildResult.Succeeded ? 0 : 1);
    }

    /// <summary>
    /// Xuất project Xcode cho iOS (GitHub Actions / GameCI gọi qua buildMethod, đường ra ở -customBuildPath).
    /// Không ký: IPA ký sau bằng Sideloadly / AltStore / tài khoản Apple Developer.
    /// </summary>
    public static void iOS()
    {
        datThuongHieu();
        string[] scenes = EditorBuildSettings.scenes.Where(s => s.enabled).Select(s => s.path).ToArray();
        string[] args = System.Environment.GetCommandLineArgs();
        int i = System.Array.IndexOf(args, "-customBuildPath");
        string outPath = i >= 0 && i + 1 < args.Length ? args[i + 1] : System.Environment.GetEnvironmentVariable("HTBB_OUT");

        AssetDatabase.ImportAsset("Assets/Icon/app_icon.png");
        UnityEngine.Texture2D icon = AssetDatabase.LoadAssetAtPath<UnityEngine.Texture2D>("Assets/Icon/app_icon.png");
        PlayerSettings.SetIcons(UnityEditor.Build.NamedBuildTarget.Unknown, new UnityEngine.Texture2D[] { icon }, IconKind.Any);

        var ios = UnityEditor.Build.NamedBuildTarget.iOS;
        PlayerSettings.SetApplicationIdentifier(ios, System.Environment.GetEnvironmentVariable("HTBB_APP_ID") ?? ThuongHieu.maUngDung());
        PlayerSettings.bundleVersion = System.Environment.GetEnvironmentVariable("HTBB_VERSION") ?? "1.0";
        PlayerSettings.iOS.buildNumber = System.Environment.GetEnvironmentVariable("HTBB_VERSION_CODE") ?? "1";
        PlayerSettings.SetScriptingBackend(ios, ScriptingImplementation.IL2CPP);
        PlayerSettings.defaultInterfaceOrientation = UIOrientation.AutoRotation;
        PlayerSettings.allowedAutorotateToPortrait = false;
        PlayerSettings.allowedAutorotateToPortraitUpsideDown = false;
        PlayerSettings.allowedAutorotateToLandscapeLeft = true;
        PlayerSettings.allowedAutorotateToLandscapeRight = true;
        PlayerSettings.iOS.appleEnableAutomaticSigning = false;
        PlayerSettings.iOS.requiresFullScreen = true;
        // server chạy TCP thô, nhưng danh sách server tải qua https - không cần mở ATS

        BuildReport r = BuildPipeline.BuildPlayer(scenes, outPath, BuildTarget.iOS, BuildOptions.None);
        UnityEngine.Debug.Log("BUILD RESULT: " + r.summary.result + " errors=" + r.summary.totalErrors);
        if (UnityEngine.Application.isBatchMode)
        {
            EditorApplication.Exit(r.summary.result == BuildResult.Succeeded ? 0 : 1);
        }
    }
}
