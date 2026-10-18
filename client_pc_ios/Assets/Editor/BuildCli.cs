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
    /// Ảnh banner parallax (màn đăng nhập / chọn Pokémon - BannerParallax.cs): chép htbb-client/shared/banner/
    /// (*.png, *.jpg, lop.txt) vào Assets/Resources/banner/ nếu thư mục đó chưa có ảnh. Không có nguồn thì bỏ
    /// qua - game tự vẽ cảnh trời / đồi.
    /// </summary>
    private static void chepBanner()
    {
        try
        {
            string nguon = System.IO.Path.GetFullPath(System.IO.Path.Combine(UnityEngine.Application.dataPath, "../../shared/banner"));
            string dich = System.IO.Path.Combine(UnityEngine.Application.dataPath, "Resources/banner");
            if (!System.IO.Directory.Exists(nguon))
            {
                return;
            }
            if (System.IO.Directory.Exists(dich) && System.IO.Directory.GetFiles(dich, "*.png").Length > 0)
            {
                return;
            }
            System.IO.Directory.CreateDirectory(dich);
            foreach (string f in System.IO.Directory.GetFiles(nguon))
            {
                string ten = System.IO.Path.GetFileName(f);
                string duoi = System.IO.Path.GetExtension(f).ToLowerInvariant();
                if (duoi == ".png" || duoi == ".jpg" || ten == "lop.txt")
                {
                    System.IO.File.Copy(f, System.IO.Path.Combine(dich, ten), true);
                }
            }
            AssetDatabase.Refresh();
            UnityEngine.Debug.Log("HTBB banner: da chep " + nguon + " -> " + dich);
        }
        catch (System.Exception e)
        {
            UnityEngine.Debug.LogWarning("HTBB banner: " + e.Message);
        }
    }

    private static void datThuongHieu()
    {
        chepBanner();
        PlayerSettings.companyName = ThuongHieu.TEN_CONG_TY;
        PlayerSettings.productName = ThuongHieu.tenSanPham();
        UnityEngine.Debug.Log("HTBB san pham: " + PlayerSettings.companyName + " / " + PlayerSettings.productName);
    }

    public static void Win64()
    {
        datThuongHieu();
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
        // số bản (CapNhat.cs so với "pc.ban" trong phienban.json): tham số -htbbBan (GitHub Actions - GameCI
        // KHÔNG chuyển biến môi trường vào container Unity) hoặc biến HTBB_VERSION_CODE (phat-hanh.sh trên máy)
        string ban = thamSo("-htbbBan") ?? System.Environment.GetEnvironmentVariable("HTBB_VERSION_CODE");
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
        UnityEngine.Debug.Log("BUILD RESULT: " + r.summary.result + " errors=" + r.summary.totalErrors);
        // số bản thật đã đóng vào bản build, ghi CẠNH thư mục build (không vào zip) để workflow kiểm tra
        string thuMuc = System.IO.Path.GetDirectoryName(System.IO.Path.GetFullPath(outPath));
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
        PlayerSettings.SetApplicationIdentifier(android, System.Environment.GetEnvironmentVariable("HTBB_APP_ID") ?? "com.nqsh.h2");
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
        PlayerSettings.SetApplicationIdentifier(ios, System.Environment.GetEnvironmentVariable("HTBB_APP_ID") ?? "com.nqsh.h2");
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
