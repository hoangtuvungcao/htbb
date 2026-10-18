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

    private static final String F_DO_HOA = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/MyGraphics.cs";
    private static final String F_PLUGIN = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/iOSPlugins.cs";
    private static final String F_DOC_LUONG = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/DataInputStream.cs";
    private static final String F_GOI = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/FilePack.cs";
    private static final String F_BAN_DO = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/Tilemap.cs";
    private static final String F_MAIN = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/Main.cs";
    private static final String F_LENH = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/MainJ2ME.cs";
    private static final String F_TAI_NGUYEN = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/Res.cs";
    private static final String F_CHU = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/MFont.cs";
    private static final String F_NHAT_KY = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/Out.cs";
    private static final String F_XAY = "../client_pc_ios/ClientUnity/Assets/Editor/XayDung.cs";
    private static final String F_MANIFEST = "../client_pc_ios/ClientUnity/Packages/manifest.json";
    private static final String F_CHUYEN = "../tools/ChuyenTaiNguyen.java";
    private static final String F_RUT = "../tools/RutTaiNguyenUnity.java";
    private static final String F_XAY_BAT = "xay-client.bat";
    private static final String F_BO_QUA = "../.gitignore";

    public static void chay() {
        kiemShader();
        kiemPluginIOS();
        kiemThieuTaiNguyenKhongSap();
        kiemBanDoKhongTuGoiMai();
        kiemGiaoDien();
        kiemAnhNutVaDoChu();
        kiemNenBanDo();
        kiemKichBanXay();
        kiemManifest();
        kiemBoChuyenTaiNguyen();
        kiemRutTaiNguyenGoc();
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

    /**
     * Bo rut tai nguyen tu ban Unity goc (KPAH_276_Data), va thu tu ghep hai bo.
     *
     * VI SAO CA NHOM NAY TON TAI: rat lau ta tuong nhieu loi giao dien la loi BO CUC
     * trong ma nguon, va da di sua tay cac moc toa do. Su that la ma nguon dung, chi co
     * ANH la sai bo: ban goc dung mot bo tai nguyen rieng, ta lai lay tam bo J2ME. Hai
     * bo co anh CUNG TEN ma KHAC KICH THUOC:
     *
     *   info   : goc 114x60, J2ME 96x34  -> hai vet den "mau da mat" roi xuong bai co
     *   inv_1  : goc 190x208, J2ME 125x116 -> so tiem nang lech khoi ten thuoc tinh,
     *            va bam vao hang "May man" lai cong diem vao "Suc khoe"
     *
     * Nen dieu duy nhat that su phai canh o day la: BO GOC PHAI VAO TRUOC, bo J2ME chi
     * duoc lap cho con thieu. Dao thu tu la ca hai loi tren quay lai nguyen ven.
     */
    private static void kiemRutTaiNguyenGoc() {
        Kiem.nhom("Client Unity - rut tai nguyen tu ban goc");

        final String ma = boChuThich(doc(F_RUT));
        Kiem.dung("doc duoc " + F_RUT, ma.length() > 0);

        // LOI DA TUNG XAY RA: o SerializedFile phien ban duoi 16, truong sau doDai CHINH
        // LA classID chu khong phai chi so vao bang kieu, va sau no con uint16 classID +
        // uint16 "da huy" + 1 byte "bi luoc". Doc thieu ba truong do thi ca 384 doi tuong
        // deu ra lop rac va rut duoc DUNG 0 tai nguyen - ma khong nem ngoai le nao.
        Kiem.dung("bo qua du 5 byte sau classID cua moi doi tuong", ma.contains("t.vt += 5;"));
        Kiem.dung("chi doc phien ban 15", ma.contains("phienBan != 15"));

        // Khong co bang duong dan cua ResourceManager thi 366 tai nguyen phai dung ten
        // tran, ma ten tran dung nhau rat nhieu (hang chuc tep ten "0", "1", "2" o cac
        // thu muc khac nhau) - ghi ra chi con 150 tep, sai 216 cai ma van "chay xong".
        Kiem.dung("co doc ResourceManager", ma.contains("LOP_QUAN_LY_TAI_NGUYEN = 147"));
        Kiem.dung("khong co bang duong dan thi DUNG LAI, khong ghi bua",
                ma.contains("khong tim thay bang duong dan"));

        // ResourceManager ha het duong dan ve chu thuong. Giu nguyen thi tren Windows
        // van chay (he tep khong phan biet hoa thuong) nhung Linux/WebGL mat sach 31 anh
        // hieu ung res/newEf/*. Cach viet phai lay tu chinh ma C#, khong duoc viet cung.
        Kiem.dung("lay cach viet hoa thuong tu ma C#", ma.contains("Main\\\\.res"));
        Kiem.dung("co ham doi ten ve dung cach viet cua ma", ma.contains("theoCachVietCuaMa"));

        // --- Thu tu ghep hai bo, doc tu chinh kich ban xay ---
        final String xay = doc(F_XAY_BAT);
        Kiem.dung("doc duoc " + F_XAY_BAT, xay.length() > 0);
        final int viTriGoc = xay.indexOf("RutTaiNguyenUnity ^");
        final int viTriJ2me = xay.indexOf("ChuyenTaiNguyen ^");
        Kiem.dung("kich ban co goi ca hai cong cu", viTriGoc > 0 && viTriJ2me > 0);
        Kiem.dung("BO GOC CHAY TRUOC, bo J2ME lap cho sau", viTriGoc < viTriJ2me);

        // Bo J2ME chi duoc lap cho: no PHAI khong ghi de tep da co, neu khong thi anh
        // J2ME sai kich thuoc se de len anh goc va moi thu hong lai y nhu cu.
        final String chuyen = boChuThich(doc(F_CHUYEN));
        Kiem.dung("buoc guong tep roi khong ghi de", chuyen.contains(
                "final Path dich = ra.resolve(catDuoi(khoa) + \".bytes\");"));
        Kiem.dung("anh trong goi chi ghi khi chua co",
                chuyen.contains("if (!Files.exists(dich)) { ghi(dich, e.getValue()); soGhi++; }"));

        // --- Doi chieu voi ANH THAT, neu da tao tai nguyen ---
        // Day moi la phep thu co rang: hai so duoi day chi dung khi bo GOC thang. Neu ai
        // do dao thu tu hai buoc, hoac tro taonguon vao thu muc chi co tep J2ME, thi
        // inv_1 se la 125x116 va bai kiem nay hong ngay.
        final File invGoc = new File("../client_pc_ios/ClientUnity/Assets/Resources/res/inv_1.bytes");
        if (invGoc.isFile()) {
            final int[] co = doCoPng(invGoc);
            Kiem.bang("inv_1 la ban GOC 190 rong", 190, co[0]);
            Kiem.bang("inv_1 la ban GOC 208 cao", 208, co[1]);
        } else {
            System.out.println("    (chua tao tai nguyen nen bo qua phep do anh that)");
        }
    }

    /** Doc rong/cao tu phan IHDR cua tep PNG. Tra {-1,-1} neu khong phai PNG. */
    private static int[] doCoPng(final File tep) {
        try {
            final byte[] d = new byte[33];
            final FileInputStream in = new FileInputStream(tep);
            try {
                int da = 0;
                while (da < d.length) {
                    final int n = in.read(d, da, d.length - da);
                    if (n < 0) {
                        return new int[] { -1, -1 };
                    }
                    da += n;
                }
            } finally {
                in.close();
            }
            if ((d[0] & 0xFF) != 0x89 || d[1] != 'P' || d[2] != 'N' || d[3] != 'G') {
                return new int[] { -1, -1 };
            }
            return new int[] { doI32(d, 16), doI32(d, 20) };
        } catch (final Exception e) {
            return new int[] { -1, -1 };
        }
    }

    private static int doI32(final byte[] d, final int i) {
        return ((d[i] & 0xFF) << 24) | ((d[i + 1] & 0xFF) << 16)
                | ((d[i + 2] & 0xFF) << 8) | (d[i + 3] & 0xFF);
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

    /**
     * Hai loi giao dien do duoc bang cach chay that va chup lai cua so.
     *
     * 1. Main.checkInput doi toa do chuot sang toa do game. No dung ScaleGUI.scaleX cho
     *    CA hai truc, ke ca truc y - trong khi ScaleGUI CO san scaleY. Hien tai vo hai
     *    vi WIDTH == Screen.width nen ca hai ham deu tra ve chinh no, nhung do la loi
     *    nam san: ngay nao bat phong giao dien len la cham chuot lech het theo chieu doc.
     *
     * 2. MainJ2ME.paint can giua chu lenh theo anh nut Res.imgButtonBar. Anh "bar" hien
     *    co chi 11x7 pixel, nen nua chu tran ra ngoai man hinh: anh chup cho thay goc
     *    trai chi con "p nhat" thay vi "Cap nhat", goc phai cut "Thoat" thanh "Thoa".
     */
    private static void kiemGiaoDien() {
        Kiem.nhom("Client Unity - loi giao dien do duoc khi chay that");

        final String main = boChuThich(doc(F_MAIN));
        Kiem.dung("doc duoc " + F_MAIN, main.length() > 0);

        // Toa do y phai dung scaleY. Dem cho chac: co dung 3 cho nhan chuot.
        Kiem.bang("ca 3 cho doi toa do y deu dung scaleY", 3,
                dem(main, "ScaleGUI.scaleY((float)Screen.height"));
        Kiem.sai("KHONG con dung scaleX cho toa do y",
                main.contains("ScaleGUI.scaleX((float)Screen.height"));

        final String j2me = boChuThich(doc(F_LENH));
        Kiem.dung("doc duoc " + F_LENH, j2me.length() > 0);

        // Chan tran phai dua tren be rong chu THAT, khong phai so cung: anh nut du to
        // thi vi tri cu phai duoc giu nguyen khong doi mot pixel nao.
        Kiem.dung("chu lenh trai co chan tran",
                j2me.contains("Math.max(4 + Res.imgButtonBar.getWidth() / 2"));
        Kiem.dung("chu lenh phai co chan tran",
                j2me.contains("Math.min(Canvas.w - Res.imgButtonBar.getWidth() / 2 - 4"));
        Kiem.dung("chan tran do bang be rong chu that",
                dem(j2me, "MFont.borderFont.getWidth(") >= 2);
    }

    /**
     * Hai loi nua, tim ra bang dot khao sat ma nguon nhieu huong.
     *
     * 1. bar1 (anh nut o trang thai DANG BAM) thieu trong bo tai nguyen hien co, ma
     *    MainJ2ME.paint doc thang imgButtonBar1.getHeight()/getWidth() o ba cho. Cu bam
     *    mot nut lenh la nem NullReferenceException, va vi paint chay tu OnGUI nen loi
     *    lap lai moi khung hinh. Day KHONG phai chuyen tham my - la crash.
     *
     * 2. MFont.wO duoc tinh trong ham dung bang getWidthExactOf("o"), nhung ham dung
     *    chay tu MFont.init() <- Main.FixedUpdate, tuc NGOAI OnGUI, ma GUIStyle.CalcSize
     *    chi chay duoc trong OnGUI. Ngoai le bi bat roi ve chinh getWidthNotExactOf luc
     *    wO con 0 -> wO = 0 vinh vien -> ham do du phong tra ve 0 cho MOI chuoi.
     */
    private static void kiemAnhNutVaDoChu() {
        Kiem.nhom("Client Unity - anh nut thieu va do be rong chu");

        final String res = boChuThich(doc(F_TAI_NGUYEN));
        Kiem.dung("doc duoc " + F_TAI_NGUYEN, res.length() > 0);
        Kiem.dung("thieu bar1 thi lui ve anh nut thuong",
                res.contains("imgButtonBar1 = imgButtonBar"));

        // Chan chinh xac: cho lui ve phai nam SAU cho nap, khong phai truoc.
        final int viTriNap = res.indexOf("imgButtonBar1 = Image.createImage");
        final int viTriLui = res.indexOf("imgButtonBar1 = imgButtonBar");
        Kiem.dung("cho lui ve nam sau cho nap anh",
                viTriNap >= 0 && viTriLui > viTriNap);

        final String font = boChuThich(doc(F_CHU));
        Kiem.dung("doc duoc " + F_CHU, font.length() > 0);
        Kiem.dung("do be rong du phong tinh tre khi wO chua co",
                font.contains("if (wO <= 0)"));

        // Co chu: ba font asset goc (normalFont0/1/2) khong ton tai, va truoc day khong
        // cho nao dat gUIStyle.fontSize - ca 18 font ve y het nhau, mat sach phan cap.
        Kiem.dung("co bang co chu ba bac", font.contains("CO_NHO")
                && font.contains("CO_THUONG") && font.contains("CO_TO"));
        Kiem.dung("co chu chon theo dung nhom id cua ban goc",
                font.contains("if ((id >= 6 && id <= 10) || id == 16)")
                        && font.contains("if (id == 12)"));

        // Hai chan duoi day trong nhu thua nhung KHONG thua: bo chung ra thi xoa han
        // "gUIStyle.fontSize = coChu" hoac thay "coChuTheoId(id)" bang mot hang so deu
        // KHONG bi bat - da thu that, ca hai phep pha deu lot.
        final int vtAp = font.indexOf("private void apCoChu(GUIStyle gUIStyle)");
        final int hetAp = (vtAp < 0) ? -1 : font.indexOf("\n\t}", vtAp);
        final String thanAp = (vtAp < 0 || hetAp < 0) ? "" : font.substring(vtAp, hetAp);
        Kiem.dung("apCoChu that su dat fontSize", thanAp.contains("gUIStyle.fontSize = coChu"));
        Kiem.dung("apCoChu that su dat fontStyle", thanAp.contains("gUIStyle.fontStyle = NET_CHU"));
        Kiem.dung("ham dung goi coChuTheoId, khong dat cung mot hang so",
                font.contains("coChu = coChuTheoId(id)"));

        // Mau chu trang phai di qua MOT hang co ten, de doi mot lan la doi ca game.
        // Da thu mau nga vang cho do choi nhung nhin kho hon tren nen ban do, nen quay
        // lai trang tinh - gia tri thi doi duoc, cho dat thi phai giu mot cho.
        Kiem.dung("co hang mau trang", font.contains("public static Color MAU_TRANG"));
        Kiem.bang("Color.white chi xuat hien dung mot lan, o dong dinh nghia hang",
                1, dem(font, "Color.white"));
        Kiem.dung("dong duy nhat do chinh la dinh nghia hang",
                font.contains("MAU_TRANG = Color.white"));
        // Con dung nguyen Color.black / Color.red ... la binh thuong: chi mau trang moi
        // duoc gom ve mot cho vi no la mau duy nhat phai chinh di chinh lai.
        Kiem.dung("cac mau khac van giu nguyen", font.contains("Color.black"));

        // DIEM MAU CHOT: ve mot co ma DO mot co khac thi moi phep can le va ngat dong
        // deu sai. Phai ap co chu o CA BON cho: mot cho ve va ba cho do.
        Kiem.bang("ap co chu du ca bon cho (1 ve + 3 do)", 4, dem(font, "apCoChu(gUIStyle)"));

        // Phai tim theo CHU KY day du, khong duoc tim theo "ten + (": ten ham con xuat
        // hien o cac LOI GOI nam truoc dinh nghia (vi du wO = getWidthExactOf("o") trong
        // ham dung), nen se cat nham mot doan than ham khac. Da bi dinh dung loi nay.
        final String[] chuKy = {
            "public void _drawString(MyGraphics g, string st, int x0, int y0, int align)",
            "public int getWidthExactOf(string s)",
            "public int getWidthNotExactOf(string s)",
            "public int getHeight()",
        };
        for (final String ky : chuKy) {
            final int vt = font.indexOf(ky);
            final int het = (vt < 0) ? -1 : font.indexOf("\n\t}", vt);
            final String than = (vt < 0 || het < 0) ? "" : font.substring(vt, het);
            final String ten = ky.substring(ky.indexOf(' ', ky.indexOf(' ') + 1) + 1,
                    ky.indexOf('('));
            Kiem.dung(ten + " co ap co chu", than.contains("apCoChu(gUIStyle)"));
        }
        Kiem.sai("KHONG con tra ve thang s.Length * wO khi wO bang 0",
                font.contains("public int getWidthNotExactOf(string s)\n\t{\n\t\treturn s.Length * wO;"));
    }

    /**
     * Ban do nen khong hien tren ban Windows, va truy ra HAI loi chong len nhau.
     *
     * 1. Lop Out co bon ham println/Log/LogError/LogWarning va CA BON DEU RONG. Moi loi
     *    client tu bao deu bien mat. Tilemap.loadMap bat ngoai le roi bao bang
     *    Out.println("LOAD TILE ERR"), nen ban do khong nap duoc ma Player.log van sach
     *    tinh - nhin vao tuong khong co loi gi.
     *
     * 2. Bo chuyen tai nguyen cat duoi lam nhieu tep goc doi ve cung mot ten dich. Ban
     *    J2ME co ca "0" (ban do, 4263 byte) lan "0.png" (anh, 258 byte), ca hai ra
     *    res/0.bytes va cai sau de len cai truoc. Tilemap doc hai byte dau cua PNG ra
     *    w=137 h=80, doi 10960 o trong mot tep 258 byte -> IndexOutOfRange.
     *    Tuong tu voi "t.png" de len "t.type".
     */
    private static void kiemNenBanDo() {
        Kiem.nhom("Client Unity - ban do nen va kenh ghi nhat ky");

        final String out = boChuThich(doc(F_NHAT_KY));
        Kiem.dung("doc duoc " + F_NHAT_KY, out.length() > 0);
        Kiem.dung("Out thuc su ghi ra nhat ky, khong con rong",
                dem(out, "Debug.Log") >= 4);
        for (final String ham : new String[] { "println", "Log", "LogError", "LogWarning" }) {
            final int vt = out.indexOf("public static void " + ham + "(string str)");
            final String than = (vt < 0) ? "" : out.substring(vt,
                    Math.min(out.length(), vt + 160));
            Kiem.dung("Out." + ham + " co ghi ra that", than.contains("Debug.Log"));
        }

        final String banDo = boChuThich(doc(F_BAN_DO));
        Kiem.dung("khoi bat loi cua loadMap co giu noi dung ngoai le",
                banDo.contains("ex.Message") && banDo.contains("ex.StackTrace"));

        // Bo chuyen: khong duoc de tep den sau ghi de tep da co.
        final String chuyen = boChuThich(doc(F_CHUYEN));
        Kiem.dung("co ham xep uu tien khi va cham ten",
                chuyen.contains("private static int uuTien(final String duongDan)"));
        Kiem.dung("tep khong co duoi duoc uu tien cao nhat",
                chuyen.contains("if (ten.indexOf('.') < 0)"));
        Kiem.dung("va cham thi BO QUA chu khong ghi de",
                chuyen.contains("KHONG ghi de") || chuyen.contains("khong ghi de"));
        Kiem.dung("va cham duoc bao ra man hinh, khong im lang",
                chuyen.contains("va cham ten: "));
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
                new File("../client_pc_ios/ClientUnity/Assets/Scripts/Game/Main.cs").isFile());
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
