import data.LenhWeb;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.LevelDetail;

/**
 * Lenh tu trang quan tri web -> server game (data.LenhWeb), them 02/09/2026.
 *
 * VI SAO CO. Nguoi dung bao: "buff lv" tren web cho nhan vat dang online khong an - thoat ra
 * vao lai van cap cu. Nguyen nhan: web UPDATE thang lastLv/xp, con ban trong RAM cua server
 * (cap cu) duoc luu de lai o lan tu luu ke tiep. Sua bang hang doi lenh: web chen dong vao
 * web_lenh_nhan_vat, server doc moi giay va cong thang vao RAM neu dang online.
 *
 * Phep kiem chia hai nhom:
 *   - THUAN TOAN (chay that): tinhExp, kepPhanNghin, laNguoiChoiThat - khong can CSDL.
 *   - HOP DONG GIUA BA BEN (soi ma nguon): Java / PHP / SQL phai dung CUNG ten bang, cung
 *     ten cot, cung ma trang thai; web KHONG con UPDATE tob_char; server duoc khoi dong.
 */
public final class KiemLenhWeb {

    public static void chay() {
        Kiem.nhom("Lenh web: dat cap do cho nhan vat dang online (data.LenhWeb)");
        tinhExpKhopLevelDetail();
        kepBien();
        nguoiChoiThat();
        serverKhoiDongLuong();
        webKhongGhiThangNua();
        baBenCungHopDong();
        apDungOnlineDungDuongTuNhienVaLuuNgay();
    }

    /** Cong thuc exp phai la cong thuc cua LevelDetail: dat exp roi doc lai dung cap + %. */
    private static void tinhExpKhopLevelDetail() {
        Kiem.bang("cap 1, 0% -> exp 0", 0L, LenhWeb.tinhExp(1, 0));
        Kiem.bang("cap 2, 0% -> expMain[1] = 200", 200L, LenhWeb.tinhExp(2, 0));
        Kiem.bang("cap 50, 0% -> expMain[49]", LevelDetail.expMain[49], LenhWeb.tinhExp(50, 0));

        final LevelDetail ld = new LevelDetail();
        ld.setExpNew(LenhWeb.tinhExp(50, 500));
        Kiem.bang("cap 50 + 50,0% -> LevelDetail doc lai dung cap 50", 50, ld.lv);
        Kiem.dung("cap 50 + 50,0% -> LevelDetail doc lai ~500 phan nghin (sai so lam tron <= 1)",
                Math.abs(ld.percent - 500) <= 1);

        ld.setExpNew(LenhWeb.tinhExp(LenhWeb.CAP_TOI_DA, 999));
        Kiem.bang("cap toi da + 99,9% van la cap toi da, chua tran sang cap sau", LenhWeb.CAP_TOI_DA, ld.lv);

        // Cung ket qua voi cong thuc "Len Level/Phan tram" cua AdminPanel
        // (server/Panel/ButtonEditor.calculateExpToBuff): expMain[c-1] + expRequire[c] * %/100.
        final long theoAdminPanel = (long) (LevelDetail.expMain[29] + LevelDetail.expRequire[30] * (25.0 / 100.0));
        Kiem.bang("cap 30 + 25% khop cong thuc AdminPanel", theoAdminPanel, LenhWeb.tinhExp(30, 250));
    }

    private static void kepBien() {
        Kiem.bang("CAP_TOI_DA = expMain.length - 1 (= 90, tran cua LevelDetail.getXpFromLevel)",
                LevelDetail.expMain.length - 1, LenhWeb.CAP_TOI_DA);
        Kiem.bang("CAP_TOI_DA khop CAPDO_TOI_DA ben web (capdo.php)", 90, LenhWeb.CAP_TOI_DA);
        Kiem.dung("capdo.php ghim CAPDO_TOI_DA = 90",
                doc("web/admin/inc/capdo.php").contains("const CAPDO_TOI_DA = 90;"));
        Kiem.bang("cap 0 kep len 1", LenhWeb.tinhExp(1, 0), LenhWeb.tinhExp(0, 0));
        Kiem.bang("cap 91 kep xuong 90", LenhWeb.tinhExp(90, 0), LenhWeb.tinhExp(91, 0));
        Kiem.bang("phan nghin am -> 0", 0, LenhWeb.kepPhanNghin(-5));
        Kiem.bang("phan nghin 1000 -> 999", 999, LenhWeb.kepPhanNghin(1000));
        Kiem.bang("phan nghin 250 giu nguyen", 250, LenhWeb.kepPhanNghin(250));
        Kiem.bang("phan nghin am khi tinh exp = 0%", LenhWeb.tinhExp(20, 0), LenhWeb.tinhExp(20, -1));
    }

    private static void nguoiChoiThat() {
        Kiem.sai("null khong phai nguoi choi that", LenhWeb.laNguoiChoiThat(null));
        try {
            final Char c = new Char(null);
            Kiem.sai("Char khong co session (bot / linh thue) KHONG duoc coi la online",
                    LenhWeb.laNguoiChoiThat(c));
        } catch (final Throwable t) {
            Kiem.dung("tao duoc Char(null) de kiem laNguoiChoiThat (" + t + ")", false);
        }
        final String java = doc("src/data/LenhWeb.java");
        Kiem.dung("tim nhan vat online quet vChars theo charDBID VA session, khong dung getCharByCharDbID",
                java.contains("c.charDBID == charId && laNguoiChoiThat(c)")
                && !than(java, "public static Char timNguoiChoiOnline").contains("getCharByCharDbID"));
    }

    private static void serverKhoiDongLuong() {
        final String ts = doc("src/server/TeamServer.java");
        Kiem.dung("TeamServer khoi dong data.LenhWeb.batDau()", ts.contains("data.LenhWeb.batDau();"));
        Kiem.dung("khoi dong SAU GhiSoOnline (cung kieu daemon tu tao bang)",
                ts.indexOf("data.GhiSoOnline.batDau();") < ts.indexOf("data.LenhWeb.batDau();"));
        final String java = doc("src/data/LenhWeb.java");
        Kiem.dung("luong la daemon, dung theo TeamServer.running",
                java.contains("t.setDaemon(true);") && java.contains("while (TeamServer.running)"));
        Kiem.dung("doc bang moi giay (web dang doi ket qua)", java.contains("CHU_KY_MS = 1000L"));
    }

    private static void webKhongGhiThangNua() {
        final String nv = doc("web/admin/nhanvat.php");
        Kiem.sai("nhanvat.php KHONG con UPDATE tob_char SET lastLv (nguyen nhan cua loi)",
                nv.contains("UPDATE tob_char SET lastLv"));
        Kiem.dung("nhanvat.php nap inc/lenh_nhan_vat.php", nv.contains("require_once __DIR__ . '/inc/lenh_nhan_vat.php';"));
        Kiem.dung("ca 'dat_cap_do' va 'buff_level' deu di qua guiLenhCapDo() + choKetQuaLenh()",
                nv.contains("$viec === 'dat_cap_do' || $viec === 'buff_level'")
                && nv.contains("guiLenhCapDo(dbGame(), $id, $ten, $capDoMoi, $phanNghin, adminName())")
                && nv.contains("choKetQuaLenh(dbGame(), $idLenh)"));
        Kiem.dung("van ghi nhat ky hai hanh dong cu (dat_cap_do_nhan_vat / buff_level_nhan_vat)",
                nv.contains("'dat_cap_do_nhan_vat'") && nv.contains("'buff_level_nhan_vat'"));
        Kiem.sai("het canh bao 'chi lam khi offline' trong nhanvat.php",
                nv.contains("chỉ làm khi chắc chắn nhân vật đang OFFLINE") || nv.contains("Chỉ làm khi nhân vật đang offline"));

        final String php = doc("web/admin/inc/lenh_nhan_vat.php");
        // Chi soi chuoi SQL (trong dau nhay don); chu thich duoc phep nhac ten bang de giai thich.
        Kiem.sai("inc/lenh_nhan_vat.php khong co cau SQL nao dong tob_char",
                Pattern.compile("'[^'\\n]*tob_char[^'\\n]*'").matcher(php).find());
        Kiem.dung("web doi toi da 4 giay roi doc lai trang thai", php.contains("LENH_CHO_TOI_DA_GIAY = 4.0"));
    }

    /** Java / PHP / SQL: cung ten bang, cung cot, cung ma trang thai. */
    private static void baBenCungHopDong() {
        final String java = doc("src/data/LenhWeb.java");
        final String php = doc("web/admin/inc/lenh_nhan_vat.php");
        final String sql = doc("sql/them_bang_web_lenh_nhan_vat.sql");
        final String bang = LenhWeb.TEN_BANG;

        Kiem.bang("ten bang Java", "web_lenh_nhan_vat", bang);
        Kiem.dung("PHP dung cung ten bang", php.contains("const LENH_BANG = '" + bang + "';")
                && php.contains("INSERT INTO " + bang + " ") && php.contains("FROM " + bang + " "));
        Kiem.dung("file SQL tao cung ten bang", sql.contains("CREATE TABLE IF NOT EXISTS `" + bang + "`"));

        final String[] cot = {"id", "loai", "char_id", "charname", "cap_do", "phan_nghin", "admin_user",
            "trang_thai", "ket_qua", "tao_luc", "xu_ly_luc"};
        for (final String c : cot) {
            Kiem.dung("cot `" + c + "` co trong ca Java, PHP va SQL",
                    java.contains("`" + c + "`") && php.contains("`" + c + "`") && sql.contains("`" + c + "`"));
        }
        // Cac cot Java doc/ghi trong cau SELECT/UPDATE phai nam trong danh sach tren.
        final Matcher m = Pattern.compile("SELECT ([a-z_, ]+) FROM").matcher(java);
        Kiem.dung("tim thay cau SELECT lenh cho trong Java", m.find());
        if (m.find(0)) {
            for (String c : m.group(1).split(",")) {
                c = c.trim();
                Kiem.dung("Java SELECT cot '" + c + "' co trong file SQL", sql.contains("`" + c + "`"));
            }
        }

        Kiem.bang("ma CHO", 0, LenhWeb.CHO);
        Kiem.bang("ma XONG_ONLINE", 1, LenhWeb.XONG_ONLINE);
        Kiem.bang("ma XONG_OFFLINE (cu)", 2, LenhWeb.XONG_OFFLINE);
        Kiem.bang("ma LOI", 3, LenhWeb.LOI);
        Kiem.bang("ma CHO_DANG_NHAP", 4, LenhWeb.CHO_DANG_NHAP);
        Kiem.bang("ma HUY", 5, LenhWeb.HUY);
        Kiem.dung("PHP dung cung ma trang thai 0..5",
                php.contains("const LENH_CHO = 0;") && php.contains("const LENH_XONG_ONLINE = 1;")
                && php.contains("const LENH_XONG_OFFLINE = 2;") && php.contains("const LENH_LOI = 3;")
                && php.contains("const LENH_CHO_DANG_NHAP = 4;") && php.contains("const LENH_HUY = 5;"));
        Kiem.dung("web chi huy duoc dong con cho (CHO / CHO_DANG_NHAP), khong xoa ket qua da co",
                php.contains("WHERE id = ? AND trang_thai IN (?, ?)") && php.contains("$id, LENH_CHO, LENH_CHO_DANG_NHAP]"));
        Kiem.bang("loai lenh", "dat_cap_do", LenhWeb.LOAI_DAT_CAP_DO);
        Kiem.dung("PHP gui cung loai lenh", php.contains("const LENH_LOAI_DAT_CAP_DO = 'dat_cap_do';"));

        Kiem.dung("bo kiem schema PHP doc file SQL moi va soi ca inc/lenh_nhan_vat.php",
                doc("web/admin/test/kiem_schema.php").contains("'sql/them_bang_web_lenh_nhan_vat.sql'")
                && doc("web/admin/test/kiem_schema.php").contains("'web/admin/inc/lenh_nhan_vat.php'"));
        Kiem.dung("sql/README.md liet ke file migration moi",
                doc("sql/README.md").contains("them_bang_web_lenh_nhan_vat.sql"));
    }

    private static void apDungOnlineDungDuongTuNhienVaLuuNgay() {
        final String java = doc("src/data/LenhWeb.java");
        final String ad = than(java, "public static String apDungOnline");
        Kiem.dung("len cap: cong exp con thieu qua Map.addXPForChar (duong len cap tu nhien)",
                ad.contains("Map.addXPForChar(p, expMoi - expCu, true, NGUON"));
        Kiem.dung("NGUON chua \"adminpanel\" de addXPForChar khong chia 3 / khong random",
                LenhWeb.NGUON.contains("adminpanel")
                && doc("src/real/MapXuLyLenh.java").contains("pos.contains(\"adminpanel\")"));

        // saveChar la ban luu luc THOAT: no p.animal.remove(0)/p.pet.remove(0) -> goi cho nhan
        // vat dang choi la rut sach thu cuoi/linh thu khoi RAM. Phai dung saveCharAuto.
        final String saveChar = doc("src/data/DatabaseNhanVat.java");
        Kiem.dung("(tien de) DatabaseNhanVat.saveChar rut p.animal/p.pet khi luu",
                saveChar.contains("p.animal.remove(0)") && saveChar.contains("p.pet.remove(0)"));
        Kiem.dung("luu ngay bang saveCharAuto sau khi cong exp",
                ad.contains("Database.instance.saveCharAuto(p);")
                && ad.indexOf("Database.instance.saveCharAuto(p);") > ad.indexOf("Map.addXPForChar"));
        Kiem.sai("KHONG dung saveChar(p, true) o bat ky dau trong LenhWeb", java.contains("saveChar(p, true)"));

        // Chi NANG cap: ha cap de lai diem vuot muc -> chot chong gian lan luc dang nhap
        // (CharNapDuLieu) xoa diem/ky nang; hoac len lai cap cu duoc cong diem lan hai.
        Kiem.dung("(tien de) CharNapDuLieu co chot xoa diem khi tong diem vuot muc cua lastLV",
                doc("src/real/CharNapDuLieu.java").contains("if (tong > allBasicPoint || self.basepoint > allBasicPoint)"));
        Kiem.dung("apDungOnline tu choi khi exp moi <= exp hien tai (khong ha cap, khong lui %)",
                ad.contains("if (expMoi <= expCu)") && ad.contains("throw new IllegalStateException"));
        Kiem.sai("khong con nhanh ha cap kieu lenh chat slv (setExpNew thang)", ad.contains("setExpNew("));
        try {
            final Char c = new Char(null);
            if (c.lvDetail == null) {
                c.lvDetail = new LevelDetail();
            }
            c.lvDetail.setExpNew(LenhWeb.tinhExp(50, 200));
            boolean tuChoi = false;
            try {
                LenhWeb.apDungOnline(c, 40, 0, "kiem");
            } catch (final IllegalStateException e) {
                tuChoi = e.getMessage().contains("NANG cap");
            }
            Kiem.dung("lv 50 (20%) -> yeu cau lv 40: bi tu choi truoc khi dong toi RAM/CSDL", tuChoi);
            Kiem.bang("tu choi thi cap trong RAM giu nguyen", 50, c.lvDetail.lv);
            tuChoi = false;
            try {
                LenhWeb.apDungOnline(c, 50, 200, "kiem");
            } catch (final IllegalStateException e) {
                tuChoi = true;
            }
            Kiem.dung("dung muc dang co (lv 50, 20%) cung bi tu choi - khong co gi de cong", tuChoi);
            tuChoi = false;
            try {
                LenhWeb.apDungOnline(c, 50, 100, "kiem");
            } catch (final IllegalStateException e) {
                tuChoi = true;
            }
            Kiem.dung("cung cap nhung % thap hon cung bi tu choi", tuChoi);
        } catch (final Throwable t) {
            Kiem.dung("tao duoc Char(null) de kiem tu choi ha cap (" + t + ")", false);
        }

        // Offline: KHONG ghi tob_char (dang nhap doc pInfo, khong doc lastLv/xp) -> nam cho.
        final String xl = than(java, "public static KetQua xuLy");
        Kiem.sai("LenhWeb KHONG UPDATE tob_char (cot lastLv/xp khong duoc dang nhap doc)", java.contains("UPDATE tob_char"));
        Kiem.dung("(tien de) dang nhap lay cap/exp tu pInfo: CharNapDuLieu doc data[4]/data[5]",
                doc("src/real/CharNapDuLieu.java").contains("self.lastLV = Short.parseShort(data[5]);"));
        Kiem.dung("xuLy: offline -> CHO_DANG_NHAP, sau khi chac tob_char co nhan vat",
                xl.contains("return new KetQua(CHO_DANG_NHAP, \"offline") && xl.contains("if (!coNhanVat(charId))"));
        Kiem.dung("xuLy: dang CHO_DANG_NHAP ma chua vao game thi giu nguyen, khong ghi gi",
                xl.contains("if (trangThaiCu == CHO_DANG_NHAP)") && xl.contains("return new KetQua(CHO_DANG_NHAP, \"\");"));
        Kiem.dung("vong doc lay ca CHO va CHO_DANG_NHAP (de cong khi nguoi choi vua vao game)",
                java.contains("WHERE trang_thai IN (?, ?) ORDER BY id"));
        Kiem.dung("xuLy: cap ngoai 1..CAP_TOI_DA -> nem loi (web ghi trang thai LOI, khong am tham kep)",
                xl.contains("capDo < 1 || capDo > CAP_TOI_DA"));
        Kiem.dung("ghi ket qua chi khi dong con o trang thai cu (web co the vua HUY)",
                java.contains("xu_ly_luc = NOW() WHERE id = ? AND trang_thai = ?"));
    }

    // ------------------------------------------------------------------ tien ich

    /** Than mot method: tu chu ky toi dau "}" o dau dong voi 4 khoang trang. */
    private static String than(final String ma, final String chuKy) {
        final int bd = ma.indexOf(chuKy);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf("\n    }\n", bd);
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
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

    private static String doc(final String duongDan) {
        try {
            return new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Kiem.dung("doc duoc " + duongDan, false);
            return "";
        }
    }

    private KiemLenhWeb() {
    }
}
