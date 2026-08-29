import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Kiem thu phan dung client Unity (ClientUnity/).
 *
 * VI SAO: du an Unity nay dung duoc la nho vai chi tiet rat de bi pha vo ma khong ai
 * nhan ra cho den luc build hong hoac — te hon — build XONG nhung sai du lieu.
 * Bon dieu duoc canh o day, moi dieu deu tung la mot loi that:
 *
 *   1. MyGraphics tao Material tu CHUOI ma shader. Unity da bo han cach nay; do la loi
 *      bien dich DUY NHAT tren ca 269 tep. Ai chep lai ban cu la hong build ngay.
 *   2. Kich ban xay phai tu thoat voi ma loi. Trong che do batch, Debug.LogError KHONG
 *      lam Unity tra ve ma khac 0, nen kich ban goi se tuong la thanh cong.
 *   3. manifest.json chi duoc dung module dung san. Them goi tu kho tren mang la may
 *      khong co mang se khong mo duoc du an.
 *   4. Bo chuyen tai nguyen KHONG duoc do theo ten cuoi tren khap cay thu muc.
 *      Ban J2ME co ca c/body/42 lan c/leg/42, nen "newEf/42" tung nhan nham anh than
 *      nhan vat: du lieu SAI chu khong phai thieu. Thieu thi con thay, sai thi khong.
 */
public final class KiemClientUnity {

    private static final String F_DO_HOA = "ClientUnity/Assets/Scripts/Game/MyGraphics.cs";
    private static final String F_PLUGIN = "ClientUnity/Assets/Scripts/Game/iOSPlugins.cs";
    private static final String F_DOC_LUONG = "ClientUnity/Assets/Scripts/Game/DataInputStream.cs";
    private static final String F_GOI = "ClientUnity/Assets/Scripts/Game/FilePack.cs";
    private static final String F_BAN_DO = "ClientUnity/Assets/Scripts/Game/Tilemap.cs";
    private static final String F_XAY = "ClientUnity/Assets/Editor/XayDung.cs";
    private static final String F_MANIFEST = "ClientUnity/Packages/manifest.json";
    private static final String F_CHUYEN = "tools/ChuyenTaiNguyen.java";
    private static final String F_BO_QUA = ".gitignore";

    public static void chay() {
        kiemShader();
        kiemPluginIOS();
        kiemThieuTaiNguyenKhongSap();
        kiemBanDoKhongTuGoiMai();
        kiemKichBanXay();
        kiemManifest();
        kiemBoChuyenTaiNguyen();
        kiemBoQua();
        kiemCauTruc();
    }

    /**
     * iOSPlugins khai bao sau ham native bang [DllImport("__Internal")]. IL2CPP sinh
     * tham chieu toi chung du luc chay khong goi, nen neu khong chan bang #if thi:
     *   - WebGL hong o khau lien ket: "undefined symbol: _SMSsend" va nam ham khac;
     *   - Windows bien dich duoc nhung nem DllNotFoundException giua luc choi, vi
     *     dieu kien Application.platform != 0 van dung tren Windows (0 la OSXEditor).
     * Ca hai deu da xay ra that.
     */
    private static void kiemPluginIOS() {
        Kiem.nhom("Client Unity - plugin native chi cho iOS");

        // Phai bo chu thich: phan mo dau cua chinh tep do co nhac ten [DllImport] va
        // #if de giai thich, nen dem tren van ban tho se ra du mot.
        final String ma = boChuThich(doc(F_PLUGIN));
        Kiem.dung("doc duoc " + F_PLUGIN, ma.length() > 0);

        Kiem.dung("co chan bang bien dich co dieu kien", ma.contains("#if UNITY_IOS"));

        // Moi khai bao DllImport phai nam trong khoi #if. Dem thay vi tim tung cai:
        // so lan #if phai du de boc het cac cho dung ham native.
        final int soDllImport = dem(ma, "[DllImport(\"__Internal\")]");
        Kiem.bang("van con du sau khai bao native (khong xoa nham)", 6, soDllImport);
        Kiem.dung("so khoi #if du de boc het khai bao va cho goi",
                dem(ma, "#if UNITY_IOS") >= 7 && dem(ma, "#endif") == dem(ma, "#if UNITY_IOS"));
    }

    /**
     * Thieu mot goi tai nguyen KHONG duoc lam sap ca vong nap.
     *
     * DA XAY RA: ban J2ME khong co goi "items" va "other", nen FilePack.init nem
     * NullReferenceException ngay trong ham dung cua DataInputStream. Res.loadRes() do
     * vo giua chung, va vi no duoc goi tu Main.FixedUpdate nen loi lap lai 30 lan moi
     * giay - do duoc 1474 lan trong 30 giay chay thu.
     *
     * Dieu dang noi: FilePack DA co san nhanh "if (file == null)" de xu ly dung truong
     * hop nay. Nhanh do khong bao gio chay toi duoc vi ngoai le nem som hon mot buoc.
     */
    private static void kiemThieuTaiNguyenKhongSap() {
        Kiem.nhom("Client Unity - thieu tai nguyen khong lam sap vong nap");

        final String dis = boChuThich(doc(F_DOC_LUONG));
        Kiem.dung("doc duoc " + F_DOC_LUONG, dis.length() > 0);
        Kiem.dung("co ham mo tai nguyen chiu duoc thieu",
                dis.contains("public static DataInputStream moNeuCo(string filename)"));
        Kiem.dung("ham do co kiem null truoc khi doc bytes",
                dis.contains("if (textAsset == null)"));

        final String fp = boChuThich(doc(F_GOI));
        Kiem.dung("doc duoc " + F_GOI, fp.length() > 0);

        // Diem mau chot: open() phai dung duong an toan, khong goi thang ham dung.
        Kiem.dung("FilePack.open dung duong an toan",
                fp.contains("file = DataInputStream.moNeuCo(name)"));
        Kiem.sai("FilePack.open KHONG goi thang ham dung cu",
                fp.contains("file = new DataInputStream(name)"));

        // getImg dung instance ma khong ai kiem; goi mo hong thi instance = null.
        Kiem.dung("getImg kiem instance null truoc khi dung",
                fp.contains("if (instance == null)"));
    }

    private static int dem(final String trong, final String mau) {
        int n = 0;
        int i = trong.indexOf(mau);
        while (i >= 0) {
            n++;
            i = trong.indexOf(mau, i + mau.length());
        }
        return n;
    }

    // -----------------------------------------------------------------------
    private static void kiemShader() {
        Kiem.nhom("Client Unity - shader ve duong");

        final String ma = boChuThich(doc(F_DO_HOA));
        Kiem.dung("doc duoc " + F_DO_HOA, ma.length() > 0);

        // Loi bien dich duy nhat tren toan bo ma khi chuyen sang Unity doi moi.
        Kiem.sai("KHONG tao Material tu chuoi ma shader",
                ma.contains("new Material(\"Shader"));
        Kiem.dung("dung shader dung san Hidden/Internal-Colored",
                ma.contains("Shader.Find(\"Hidden/Internal-Colored\")"));

        // Shader cu dat bon trang thai. Shader dung san mac dinh KHAC, nen phai dat lai
        // du ca bon; thieu mot cai la ve sai (vi du mat trong suot, hoac bi ZTest cat).
        Kiem.dung("dat lai che do tron nguon", ma.contains("_SrcBlend"));
        Kiem.dung("dat lai che do tron dich", ma.contains("_DstBlend"));
        Kiem.dung("dat lai Cull (shader cu: Cull Off)", ma.contains("_Cull"));
        Kiem.dung("dat lai ZWrite (shader cu: ZWrite Off)", ma.contains("_ZWrite"));
    }

    /**
     * loadMap bat moi ngoai le roi lui ve ban do 0. Truoc day no goi loadMap(0, null)
     * VO DIEU KIEN, ke ca khi level da la 0 - nen ban do 0 hong la ham tu goi minh mai
     * cho den StackOverflowException. Do duoc that trong luc chay thu ban PC.
     *
     * Day la loi that trong client, khong lien quan den viec thieu tai nguyen: mot lan
     * nap ban do 0 that bai vi bat ky ly do gi cung lam client chet han thay vi bao loi.
     */
    private static void kiemBanDoKhongTuGoiMai() {
        Kiem.nhom("Client Unity - nap ban do khong tu goi minh mai");

        final String ma = boChuThich(doc(F_BAN_DO));
        Kiem.dung("doc duoc " + F_BAN_DO, ma.length() > 0);

        Kiem.dung("chi lui ve ban do 0 khi dang o ban do khac",
                ma.contains("if (level != 0)"));

        // Chan chinh xac: trong khoi catch khong duoc co loi goi loadMap tran.
        final int viTriCatch = ma.lastIndexOf("LOAD TILE ERR");
        final String khoiCatch = (viTriCatch < 0) ? ""
                : ma.substring(viTriCatch, Math.min(ma.length(), viTriCatch + 400));
        Kiem.dung("tim thay khoi bat loi cua loadMap", viTriCatch >= 0);
        Kiem.dung("loi goi lai nam trong dieu kien chan level 0",
                khoiCatch.indexOf("if (level != 0)") >= 0
                        && khoiCatch.indexOf("if (level != 0)") < khoiCatch.indexOf("loadMap(0, null)"));
    }

    private static void kiemKichBanXay() {
        Kiem.nhom("Client Unity - kich ban xay");

        final String ma = boChuThich(doc(F_XAY));
        Kiem.dung("doc duoc " + F_XAY, ma.length() > 0);

        Kiem.dung("co ham chuan bi du an", ma.contains("public static void ChuanBi()"));
        Kiem.dung("co xay Windows", ma.contains("public static void XayWindows()"));
        Kiem.dung("co xay WebGL", ma.contains("public static void XayWebGL()"));
        Kiem.dung("co xay Android", ma.contains("public static void XayAndroid()"));
        Kiem.dung("co xay iOS", ma.contains("public static void XayIOS()"));

        // Bao truoc khi nen tang chua cai module, thay vi de Unity nem loi kho hieu.
        Kiem.dung("kiem module truoc khi xay", ma.contains("IsBuildTargetSupported"));

        // Diem mau chot: trong che do batch, Debug.LogError khong doi ma thoat.
        // Khong tu goi Exit thi xay that bai van tra ve 0 va kich ban goi tuong la xong.
        Kiem.dung("tu thoat voi ma loi khi that bai",
                ma.contains("EditorApplication.Exit(1)"));
        Kiem.dung("co xet ket qua xay", ma.contains("BuildResult.Succeeded"));
    }

    private static void kiemManifest() {
        Kiem.nhom("Client Unity - manifest goi");

        final String js = doc(F_MANIFEST);
        Kiem.dung("doc duoc " + F_MANIFEST, js.length() > 0);

        // Chi duoc phu thuoc module dung san (di kem ban cai Unity). Bat ky goi nao khac
        // deu phai tai tu kho tren mang -> may khong co mang se khong mo duoc du an.
        int soPhuThuoc = 0;
        boolean toanDungSan = true;
        for (final String dong : js.split("\n")) {
            final String t = dong.trim();
            if (!t.startsWith("\"com.") && !t.startsWith("\"unity")) {
                continue;
            }
            soPhuThuoc++;
            if (!t.startsWith("\"com.unity.modules.")) {
                toanDungSan = false;
            }
        }
        Kiem.dung("co khai bao phu thuoc", soPhuThuoc > 0);
        Kiem.dung("chi dung module dung san, khong can mang", toanDungSan);
    }

    private static void kiemBoChuyenTaiNguyen() {
        Kiem.nhom("Client Unity - bo chuyen tai nguyen");

        final String ma = boChuThich(doc(F_CHUYEN));
        Kiem.dung("doc duoc " + F_CHUYEN, ma.length() > 0);

        // Khoa dinh dang goi FilePack. Doi la giai ra rac ma khong bao loi.
        Kiem.dung("giu dung khoa xao tron cua goi", ma.contains("NguyenVanMinh"));

        // Duoi .bytes la bat buoc: Unity chi nhan tep lam TextAsset khi co duoi nay.
        Kiem.dung("ghi ra duoi .bytes", ma.contains(".bytes"));

        // LOI DA TUNG XAY RA: do theo ten cuoi tren khap cay thu muc lam "newEf/42"
        // nhan nham "c/body/42". Chi duoc lay tep o GOC hoac anh trong goi.
        Kiem.sai("KHONG do ten cuoi tren khap cay thu muc",
                ma.contains("endsWith(\"/\" + cuoi)"));
        Kiem.dung("chi nhan tep nam ngay goc ban J2ME",
                ma.contains("if (e.getKey().indexOf('/') >= 0) { continue; }"));

        // Danh sach tai nguyen phai doc tu ma C#, khong viet cung: viet cung thi ma doi
        // mot dang la bao cao thieu/du sai ma khong ai biet.
        Kiem.dung("doc danh sach tai nguyen tu chinh ma C#",
                ma.contains("Main\\\\.res"));

        // LOI DA TUNG XAY RA: sot duoi ".wp" nen wpsplash/0.wp bi ghi thanh
        // wpsplash/0.wp.bytes, trong khi Res.GetWPSplashInfo tim wpsplash/0.
        // Tai nguyen co that ma coi nhu mat. Ban J2ME chi co dung 6 duoi nay.
        //
        // Phai soi DUNG DONG khai bao DUOI_CAT, khong duoc tim chuoi tren ca tep:
        // ".wp" con nam trong DUOI_GOI nua, nen kiem kieu do thi xoa han khoi DUOI_CAT
        // ma bai kiem van xanh. Da thu: dung la no khong bat duoc.
        final int viTri = ma.indexOf("DUOI_CAT = {");
        final int het = (viTri < 0) ? -1 : ma.indexOf('}', viTri);
        Kiem.dung("tim thay khai bao DUOI_CAT", viTri >= 0 && het > viTri);
        final String dongDuoi = (viTri < 0 || het < 0) ? "" : ma.substring(viTri, het);
        for (final String duoi : new String[] { ".png", ".sh", ".wps", ".wp", ".type", ".txt" }) {
            Kiem.dung("DUOI_CAT co cat duoi " + duoi, dongDuoi.contains("\"" + duoi + "\""));
        }

        // Goi dung ra phai giai lai duoc: dongGoi la nghich dao viet tay cua giaiGoi,
        // lech mot byte o phan dau la client doc ra rac ma khong bao loi gi.
        Kiem.dung("tu kiem lai goi vua dong",
                ma.contains("final Map<String, byte[]> doc = giaiGoi(goiMoi)"));
        Kiem.dung("bao loi khi goi dong ra khong giai lai dung",
                ma.contains("Goi vua dong khong giai lai dung"));

        // Cho thieu anh phai dat anh TRONG SUOT, khong duoc muon anh khac cho do trong:
        // dat nham la du lieu sai, ma sai thi khong ai nhin ra.
        Kiem.dung("thieu anh thi dung anh trong suot", ma.contains("anhTrongSuot()"));
        Kiem.dung("anh giu cho la 1x1 trong suot (TYPE_INT_ARGB)",
                ma.contains("BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB)"));

        // Ma client co 49 cho re nhanh theo "anh == null" - vi du imgHorse == null nghia
        // la "khong co ngua thi khoi ve". Dat anh giu cho vao nhung cho do la DOI HANH VI,
        // nen phai bo qua chung. Quy tac thua con hon thieu.
        Kiem.dung("co do cac cho re nhanh theo null", ma.contains("MAU_SO_NULL"));
        Kiem.dung("bo qua anh co nhanh re theo null",
                ma.contains("if (coNhanhNull.contains(e.getValue()))"));
    }

    private static void kiemBoQua() {
        Kiem.nhom("Client Unity - khong commit rac");

        final String bq = doc(F_BO_QUA);
        Kiem.dung("doc duoc " + F_BO_QUA, bq.length() > 0);

        // Library/ mot minh nang hang tram MB. Build/ chua ca ban .exe 94 MB.
        Kiem.dung("bo qua ClientUnity/Library", bq.contains("/ClientUnity/Library/"));
        Kiem.dung("bo qua ClientUnity/Build", bq.contains("/ClientUnity/Build/"));
        Kiem.dung("bo qua ClientUnity/Temp", bq.contains("/ClientUnity/Temp/"));

        // Tai nguyen la san pham dan xuat: tao lai bang mot lenh, khong commit.
        Kiem.dung("bo qua tai nguyen sinh ra",
                bq.contains("/ClientUnity/Assets/Resources/"));
    }

    private static void kiemCauTruc() {
        Kiem.nhom("Client Unity - cau truc thu muc");

        // Unity chi nhan ma nguon nam duoi Assets/.
        Kiem.dung("ma game nam duoi Assets/",
                new File("ClientUnity/Assets/Scripts/Game/Main.cs").isFile());
        Kiem.dung("kich ban xay nam trong Assets/Editor/ (bat buoc voi ma Editor)",
                new File(F_XAY).isFile());
        Kiem.dung("co manifest goi", new File(F_MANIFEST).isFile());

        // Thu muc Client/ cu da chuyen di. Con lai la co ban sao thu hai cua cung mot
        // ma nguon, sua mot ben khong sang ben kia.
        Kiem.sai("khong con thu muc Client/ cu", new File("Client").isDirectory());
    }

    // -----------------------------------------------------------------------
    private static String boChuThich(final String ma) {
        final StringBuilder sb = new StringBuilder(ma.length());
        boolean trongKhoi = false;
        for (final String dong : ma.split("\n", -1)) {
            String t = dong;
            if (trongKhoi) {
                final int het = t.indexOf("*/");
                if (het < 0) {
                    continue;
                }
                t = t.substring(het + 2);
                trongKhoi = false;
            }
            final int mo = t.indexOf("/*");
            if (mo >= 0) {
                final int het = t.indexOf("*/", mo + 2);
                if (het >= 0) {
                    t = t.substring(0, mo) + t.substring(het + 2);
                } else {
                    t = t.substring(0, mo);
                    trongKhoi = true;
                }
            }
            final int hai = t.indexOf("//");
            if (hai >= 0) {
                t = t.substring(0, hai);
            }
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder((int) f.length() + 16);
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
        } catch (final Exception ex) {
            return "";
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }

    private KiemClientUnity() {
    }
}
