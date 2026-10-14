using System.Linq;
using UnityEditor;
using UnityEditor.Build.Reporting;

public static class BuildCli
{
    /// <summary>Đường ra: biến KPAH_OUT (phat-hanh.sh trên máy) hoặc -customBuildPath (GameCI trên GitHub Actions).</summary>
    private static string duongRa()
    {
        string[] args = System.Environment.GetCommandLineArgs();
        int i = System.Array.IndexOf(args, "-customBuildPath");
        return i >= 0 && i + 1 < args.Length ? args[i + 1] : System.Environment.GetEnvironmentVariable("KPAH_OUT");
    }

    public static void Win64()
    {
        string[] scenes = EditorBuildSettings.scenes.Where(s => s.enabled).Select(s => s.path).ToArray();
        string outPath = duongRa();
        PlayerSettings.SetScriptingBackend(UnityEditor.Build.NamedBuildTarget.Standalone, ScriptingImplementation.Mono2x);
        // số bản (CapNhat.cs so với "pc.ban" trong phienban.json) - phat-hanh.sh truyền vào
        string ban = System.Environment.GetEnvironmentVariable("KPAH_VERSION_CODE");
        if (!string.IsNullOrEmpty(ban))
        {
            PlayerSettings.bundleVersion = ban;
        }
        AssetDatabase.ImportAsset("Assets/Icon/app_icon.png");
        UnityEngine.Texture2D icon = AssetDatabase.LoadAssetAtPath<UnityEngine.Texture2D>("Assets/Icon/app_icon.png");
        PlayerSettings.SetIcons(UnityEditor.Build.NamedBuildTarget.Unknown, new UnityEngine.Texture2D[] { icon }, IconKind.Any);
        PlayerSettings.fullScreenMode = UnityEngine.FullScreenMode.Windowed;
        PlayerSettings.resizableWindow = true;
        PlayerSettings.allowFullscreenSwitch = true;
        BuildReport r = BuildPipeline.BuildPlayer(scenes, outPath, BuildTarget.StandaloneWindows64, BuildOptions.None);
        UnityEngine.Debug.Log("BUILD RESULT: " + r.summary.result + " errors=" + r.summary.totalErrors);
        EditorApplication.Exit(r.summary.result == BuildResult.Succeeded ? 0 : 1);
    }

    /// <summary>APK Android: IL2CPP ARM64 + ARMv7, màn hình ngang, ký bằng khóa riêng (KPAH_KEY_DIR/key.properties).</summary>
    public static void Android()
    {
        string[] scenes = EditorBuildSettings.scenes.Where(s => s.enabled).Select(s => s.path).ToArray();
        string outPath = System.Environment.GetEnvironmentVariable("KPAH_OUT");
        string keyDir = System.Environment.GetEnvironmentVariable("KPAH_KEY_DIR");
        var key = System.IO.File.ReadAllLines(System.IO.Path.Combine(keyDir, "key.properties"))
            .Where(l => l.Contains("=")).ToDictionary(l => l.Substring(0, l.IndexOf('=')), l => l.Substring(l.IndexOf('=') + 1));

        AssetDatabase.ImportAsset("Assets/Icon/app_icon.png");
        UnityEngine.Texture2D icon = AssetDatabase.LoadAssetAtPath<UnityEngine.Texture2D>("Assets/Icon/app_icon.png");
        PlayerSettings.SetIcons(UnityEditor.Build.NamedBuildTarget.Unknown, new UnityEngine.Texture2D[] { icon }, IconKind.Any);

        var android = UnityEditor.Build.NamedBuildTarget.Android;
        PlayerSettings.SetApplicationIdentifier(android, System.Environment.GetEnvironmentVariable("KPAH_APP_ID") ?? "com.nqsh.h2");
        PlayerSettings.bundleVersion = System.Environment.GetEnvironmentVariable("KPAH_VERSION") ?? "1.0";
        PlayerSettings.Android.bundleVersionCode = int.Parse(System.Environment.GetEnvironmentVariable("KPAH_VERSION_CODE") ?? "1");
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
        string[] scenes = EditorBuildSettings.scenes.Where(s => s.enabled).Select(s => s.path).ToArray();
        string[] args = System.Environment.GetCommandLineArgs();
        int i = System.Array.IndexOf(args, "-customBuildPath");
        string outPath = i >= 0 && i + 1 < args.Length ? args[i + 1] : System.Environment.GetEnvironmentVariable("KPAH_OUT");

        AssetDatabase.ImportAsset("Assets/Icon/app_icon.png");
        UnityEngine.Texture2D icon = AssetDatabase.LoadAssetAtPath<UnityEngine.Texture2D>("Assets/Icon/app_icon.png");
        PlayerSettings.SetIcons(UnityEditor.Build.NamedBuildTarget.Unknown, new UnityEngine.Texture2D[] { icon }, IconKind.Any);

        var ios = UnityEditor.Build.NamedBuildTarget.iOS;
        PlayerSettings.SetApplicationIdentifier(ios, System.Environment.GetEnvironmentVariable("KPAH_APP_ID") ?? "com.nqsh.h2");
        PlayerSettings.bundleVersion = System.Environment.GetEnvironmentVariable("KPAH_VERSION") ?? "1.0";
        PlayerSettings.iOS.buildNumber = System.Environment.GetEnvironmentVariable("KPAH_VERSION_CODE") ?? "1";
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
