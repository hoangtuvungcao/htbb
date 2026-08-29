using System;
using System.IO;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;

/// <summary>
/// Dung tu dong hoa viec tao canh va xuat ban cho tung nen tang.
///
/// VI SAO CO TEP NAY: du an nay khong co canh (.unity) nao trong git. Ma nguon la ban
/// port tu client J2ME, toan bo tro choi chay tu MOT MonoBehaviour duy nhat la Main.
/// Neu tao canh bang tay roi commit tep .unity thi moi lan Unity doi dinh dang lai phai
/// sua tay. Tao bang ma thi canh luon dung voi phien ban Unity dang dung.
///
/// Chay tu dong dong lenh:
///   Unity.exe -batchmode -quit -projectPath ClientUnity -executeMethod XayDung.ChuanBi
///   Unity.exe -batchmode -quit -projectPath ClientUnity -executeMethod XayDung.XayWindows
/// </summary>
public static class XayDung
{
    private const string DUONG_CANH = "Assets/Scenes/Main.unity";
    private const string THU_MUC_XUAT = "Build";

    // -------------------------------------------------------------------
    // Tao canh + dat cau hinh. Phai chay truoc moi lan xay lan dau.
    // -------------------------------------------------------------------
    [MenuItem("KPAH/Chuan bi du an")]
    public static void ChuanBi()
    {
        TaoCanh();
        DatCauHinh();
        AssetDatabase.SaveAssets();
        Debug.Log("[KPAH] Chuan bi xong.");
    }

    private static void TaoCanh()
    {
        Directory.CreateDirectory("Assets/Scenes");

        Scene canh = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);

        // Camera: ban than tro choi ve bang OnGUI + GL, nhung van can mot camera de
        // Unity xoa khung hinh va de GL.* co ma tran chieu hop le.
        GameObject oCam = new GameObject("Camera chinh");
        Camera cam = oCam.AddComponent<Camera>();
        cam.clearFlags = CameraClearFlags.SolidColor;
        cam.backgroundColor = Color.black;
        cam.orthographic = true;
        oCam.tag = "MainCamera";

        // Toan bo tro choi nam trong mot MonoBehaviour duy nhat.
        GameObject oMain = new GameObject("Main");
        oMain.AddComponent<Main>();

        EditorSceneManager.SaveScene(canh, DUONG_CANH);
        EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(DUONG_CANH, true) };
        Debug.Log("[KPAH] Da tao canh " + DUONG_CANH);
    }

    private static void DatCauHinh()
    {
        PlayerSettings.companyName = "KPAH";
        PlayerSettings.productName = "Khi Phach Anh Hung";
        PlayerSettings.defaultScreenWidth = 800;
        PlayerSettings.defaultScreenHeight = 600;
        PlayerSettings.runInBackground = true;
        PlayerSettings.resizableWindow = true;

        // Tro choi ve tay bang GL, khong dung anh sang -> tat bot cho nhe.
        PlayerSettings.colorSpace = ColorSpace.Gamma;

        // WebGL: tat nen de phuc vu duoc tu bat ky may chu tinh nao ma khong can
        // cau hinh header Content-Encoding. Doi lai tep to hon, nhung khoi hong.
        PlayerSettings.WebGL.compressionFormat = WebGLCompressionFormat.Disabled;
        PlayerSettings.WebGL.dataCaching = true;
    }

    // -------------------------------------------------------------------
    // Cac ban xay
    // -------------------------------------------------------------------
    [MenuItem("KPAH/Xay Windows")]
    public static void XayWindows()
    {
        Xay(BuildTarget.StandaloneWindows64, BuildTargetGroup.Standalone,
            THU_MUC_XUAT + "/Windows/KhiPhachAnhHung.exe");
    }

    [MenuItem("KPAH/Xay WebGL")]
    public static void XayWebGL()
    {
        Xay(BuildTarget.WebGL, BuildTargetGroup.WebGL, THU_MUC_XUAT + "/WebGL");
    }

    [MenuItem("KPAH/Xay Android (APK)")]
    public static void XayAndroid()
    {
        PlayerSettings.SetApplicationIdentifier(NamedBuildTarget.Android, "com.kpah.game");
        Xay(BuildTarget.Android, BuildTargetGroup.Android,
            THU_MUC_XUAT + "/Android/KhiPhachAnhHung.apk");
    }

    [MenuItem("KPAH/Xay iOS (du an Xcode)")]
    public static void XayIOS()
    {
        PlayerSettings.SetApplicationIdentifier(NamedBuildTarget.iOS, "com.kpah.game");
        Xay(BuildTarget.iOS, BuildTargetGroup.iOS, THU_MUC_XUAT + "/iOS");
    }

    private static void Xay(BuildTarget dich, BuildTargetGroup nhom, string duongRa)
    {
        if (!BuildPipeline.IsBuildTargetSupported(nhom, dich))
        {
            LoiVaThoat("Chua cai module cho " + dich + ". Mo Unity Hub -> Installs -> "
                     + "banh rang -> Add modules, roi cai module tuong ung.");
            return;
        }

        if (EditorBuildSettings.scenes.Length == 0)
        {
            ChuanBi();
        }

        string thuMuc = Path.GetDirectoryName(duongRa);
        if (!string.IsNullOrEmpty(thuMuc))
        {
            Directory.CreateDirectory(thuMuc);
        }

        BuildPlayerOptions tuyChon = new BuildPlayerOptions
        {
            scenes = new[] { DUONG_CANH },
            locationPathName = duongRa,
            target = dich,
            targetGroup = nhom,
            options = BuildOptions.None,
        };

        BuildReport bc = BuildPipeline.BuildPlayer(tuyChon);
        if (bc.summary.result != BuildResult.Succeeded)
        {
            LoiVaThoat("Xay " + dich + " that bai: " + bc.summary.totalErrors + " loi.");
            return;
        }

        Debug.Log("[KPAH] Xay " + dich + " xong -> " + duongRa
                + " (" + (bc.summary.totalSize / 1024 / 1024) + " MB)");
    }

    // Trong che do batch, Debug.LogError khong lam Unity tra ve ma loi. Phai tu thoat,
    // neu khong thi kich ban goi se tuong la xay thanh cong.
    private static void LoiVaThoat(string thongDiep)
    {
        Debug.LogError("[KPAH] " + thongDiep);
        if (Application.isBatchMode)
        {
            EditorApplication.Exit(1);
        }
    }
}
