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
 *   - HOP DONG GIUA BA BEN (soi ma nguon): Java / web (HTBB-Web) / SQL phai dung CUNG ten bang, cung
 *     ten cot, cung ma trang thai; web KHONG con UPDATE tob_char; server duoc khoi dong.
 */
public final class KiemLenhWeb {

    /** HTBB-Web (thay website PHP da bo - docs/kien-truc.md). */
    private static final String TS_LENH = "server/utils/lenhNhanVat.ts";
    private static final String TS_CAP_DO = "server/api/admin/nhanvat/[id]/cap-do.post.ts";
    private static final String TS_XOA_VP = "server/api/admin/nhanvat/[id]/xoa-vat-pham.post.ts";

    public static void chay() {
        Kiem.nhom("Lenh web: dat cap do cho nhan vat dang online (data.LenhWeb)");
        tinhExpKhopLevelDetail();
        kepBien();
        nguoiChoiThat();
        serverKhoiDongLuong();
        webKhongGhiThangNua();
        baBenCungHopDong();
        apDungOnlineDungDuongTuNhienVaLuuNgay();
        xoaVatPhamQuaLenh();
        nangLevelToanBo();
    }

    /**
     * "Xoa vat pham" cung tung ghi thang cot equip/inven/bag -> online bi luu de. Nay di qua
     * cung hang doi: tham_so = "<cot>><mau>", server go mon khop TUNG KY TU trong RAM.
     */
    private static void xoaVatPhamQuaLenh() {
        // --- thuan toan: tach tham so ---
        String[] ts = LenhWeb.tachThamSo("inven>12,3,4|5,6");
        Kiem.dung("tach \"inven>12,3,4|5,6\" -> cot inven, mau giu nguyen ca dau |",
                ts != null && ts[0].equals("inven") && ts[1].equals("12,3,4|5,6"));
        Kiem.dung("cot khong trong danh sach -> null", LenhWeb.tachThamSo("tuido>1,2") == null);
        Kiem.dung("thieu dau > -> null", LenhWeb.tachThamSo("inven") == null);
        Kiem.dung("mau rong -> null", LenhWeb.tachThamSo("bag>") == null);
        Kiem.dung("null -> null", LenhWeb.tachThamSo(null) == null);

        // --- thuan toan: xoa mau khoi chuoi cot ---
        Kiem.bang("xoa mau o giua", "a|1>c|3", LenhWeb.xoaMauKhoiChuoi("a|1>b|2>c|3", "b|2"));
        Kiem.bang("xoa mau dau, bo dau > dau chuoi nhu server", "b|2>c|3", LenhWeb.xoaMauKhoiChuoi(">a|1>b|2>c|3", "a|1"));
        Kiem.bang("xoa mau cuoi", "a|1>b|2", LenhWeb.xoaMauKhoiChuoi("a|1>b|2>c|3", "c|3"));
        Kiem.bang("chi xoa MOT lan du trung", "x|1>x|1", LenhWeb.xoaMauKhoiChuoi("x|1>x|1>x|1", "x|1"));
        Kiem.bang("giu nguyen phan thoi trang dang mac o cuoi inven", "a|1>0,5,7", LenhWeb.xoaMauKhoiChuoi("a|1>b|2>0,5,7", "b|2"));
        Kiem.bang("mau chi la mot phan cua mau khac thi KHONG khop (khop tung ky tu)", null, LenhWeb.xoaMauKhoiChuoi("ab|1>c|2", "b|1"));
        Kiem.bang("khong tim thay -> null", null, LenhWeb.xoaMauKhoiChuoi("a|1>b|2", "z|9"));
        Kiem.bang("chuoi rong -> null", null, LenhWeb.xoaMauKhoiChuoi("", "a|1"));
        Kiem.bang("xoa mon duy nhat -> chuoi rong", "", LenhWeb.xoaMauKhoiChuoi("a|1", "a|1"));

        // --- hop dong Java / web (HTBB-Web) / SQL ---
        final String java = doc("src/data/LenhWeb.java");
        final String sql = DayDu.ddl("web_lenh_nhan_vat");
        Kiem.bang("loai lenh xoa", "xoa_vat_pham", LenhWeb.LOAI_XOA_VAT_PHAM);
        if (!WebHtbb.boQua("hop dong xoa vat pham voi HTBB-Web")) {
            final String tsLenh = WebHtbb.doc(TS_LENH);
            final String nv = WebHtbb.doc(TS_XOA_VP);
            Kiem.dung("web gui cung loai", tsLenh.contains("export const LENH_LOAI_XOA_VAT_PHAM = 'xoa_vat_pham'"));
            Kiem.dung("web ghep tham so cung dinh dang cot + '>' + mau", tsLenh.contains("return `${cot}>${mau}`"));
            Kiem.dung("cot tham_so co trong Java, web va SQL",
                    java.contains("`tham_so`") && tsLenh.contains("`tham_so`") && sql.contains("`tham_so`"));
            Kiem.dung("bang cu (chua co tham_so) duoc them cot: Java qua information_schema, web qua SHOW COLUMNS",
                    java.contains("information_schema.COLUMNS") && java.contains("ADD COLUMN `tham_so`")
                    && tsLenh.contains("SHOW COLUMNS FROM") && tsLenh.contains("ADD COLUMN \\`tham_so\\`"));
            Kiem.dung("Java va web dung cung danh sach cot equip/inven/bag",
                    java.contains("{\"equip\", \"inven\", \"bag\"}") && tsLenh.contains("['equip', 'inven', 'bag'] as const"));

            // --- web khong sua cot nua ---
            Kiem.sai("API xoa vat pham KHONG UPDATE tob_char",
                    Pattern.compile("UPDATE\\s+tob_char", Pattern.CASE_INSENSITIVE).matcher(nv).find());
            Kiem.dung("API xoa vat pham gui lenh qua guiLenhXoaVatPham()",
                    nv.contains("guiLenhXoaVatPham(dbGame(), id, ten, cot, mau, admin.username)"));
            Kiem.dung("API xoa doi chieu template + plus cua mon nguoi bam da nhin thay",
                    nv.contains("int(body?.template, -1) === vp.template") && nv.contains("int(body?.plus, 0) === vp.plus"));
            Kiem.dung("ten cot qua danh sach trang laCotVatPham() truoc khi ghep vao SQL",
                    nv.indexOf("if (!laCotVatPham(cot))") >= 0
                    && nv.indexOf("if (!laCotVatPham(cot))") < nv.indexOf("SELECT ${cot} FROM tob_char"));
        }

        // --- server: online go dung mon, offline sua CSDL roi doi chieu khi vao game ---
        final String go = than(java, "public static boolean goVatPhamOnline");
        Kiem.dung("equip: quet wItems[slot][] (dat null) va awItems", go.contains("p.wItems[slot]") && go.contains("goKhoiVector(p.awItems, mau)"));
        Kiem.dung("inven: ba mon dac biet (HKL, vu khi thoi trang, than khi) roi iItems",
                go.contains("p.itemHoaKyLan = null;") && go.contains("p.itemVukhiThoiTrang = null;")
                && go.contains("p.itemThanKhi = null;") && go.contains("goKhoiVector(p.iItems, mau)"));
        Kiem.dung("bag: bItem", go.contains("goKhoiVector(p.bItem, mau)"));
        Kiem.dung("khop bang getInfoSave() - dung chuoi server ghi xuong cot",
                java.contains("mau.equals(it.getInfoSave())"));
        Kiem.dung("sau khi go: calculateAttrib, gui lai tui + do mac + thong tin, roi saveCharAuto",
                go.contains("p.calculateAttrib();") && go.contains("createCharInventoryMessage(p, 1)")
                && go.contains("createCharWearingMessage(p, p)") && go.contains("Database.instance.saveCharAuto(p);")
                && go.indexOf("Database.instance.saveCharAuto(p);") > go.indexOf("createCharInventoryMessage(p, 1)"));
        final String xl = than(java, "static KetQua xuLyXoaVatPham");
        Kiem.dung("online ma khong khop -> LOI ro rang, khong doan mo",
                xl.contains("khong thay mon khop trong \" + cot"));
        Kiem.dung("offline: sua CSDL roi CHO_DANG_NHAP de doi chieu RAM khi vao game",
                xl.contains("xoaVatPhamOffline(charId, cot, mau)")
                && xl.contains("return new KetQua(CHO_DANG_NHAP, \"da xoa trong CSDL"));
        Kiem.dung("vao game sau khi da xoa offline ma khong con mon -> XONG, khong bao loi",
                xl.contains("trangThaiCu == CHO_DANG_NHAP") && xl.contains("xac nhan khi vao game: khong con mon do"));
        Kiem.dung("xoa offline chi dong dung cot trong danh sach (ten cot ghep vao SQL)",
                than(java, "static int xoaVatPhamOffline").contains("if (!laCotVatPham(cot))"));
    }

    /**
     * "Nang level toan bo" (truoc la web/admin/nang_level_toanbo.php; nay la HTBB-Web
     * server/api/admin/nang-level-toanbo/index.post.ts + server/utils/nangLevelToanBo.ts, them cho chuc nang nang level
     * hang loat toan server): TAI SU DUNG dung loai lenh dat_cap_do da co san (khong them
     * loai lenh moi) vi dat_cap_do von da CHI NANG cap - dua nhan vat len DUNG mot cap dich,
     * tu choi neu nhan vat da o cap do tro len. Danh dau rieng bang cot tham_so = 'toanbo' de
     * phan biet voi lenh dat_cap_do tung nhan vat mot cua trang Nhan vat (tham_so rong o do).
     */
    private static void nangLevelToanBo() {
        Kiem.sai("Java khong co loai lenh rieng tang_cap_do (tai su dung dat_cap_do)",
                doc("src/data/LenhWeb.java").contains("LOAI_TANG_CAP_DO"));
        if (WebHtbb.boQua("nang level toan bo (HTBB-Web)")) {
            return;
        }
        final String ts = WebHtbb.doc(TS_LENH);
        final String tb = WebHtbb.doc("server/api/admin/nang-level-toanbo/index.post.ts");
        final String tbGet = WebHtbb.doc("server/api/admin/nang-level-toanbo/index.get.ts");
        final String tbDanhDau = WebHtbb.doc("server/utils/nangLevelToanBo.ts");
        Kiem.dung("doc duoc API nang-level-toanbo", !tb.isEmpty() && !tbGet.isEmpty());

        Kiem.sai("web khong co loai lenh rieng tang_cap_do (tai su dung dat_cap_do)",
                ts.contains("LENH_LOAI_TANG_CAP_DO") || tb.contains("LENH_LOAI_TANG_CAP_DO"));
        Kiem.dung("API toan bo dung dung hang so LENH_LOAI_DAT_CAP_DO da co san", tb.contains("LENH_LOAI_DAT_CAP_DO"));
        Kiem.dung("nhap cap do DICH (tuyet doi), khong phai so level cong them", tb.contains("body?.cap_do_dich"));
        Kiem.dung("danh dau rieng qua tham_so de khong lan voi lenh cua trang Nhan vat",
                tbDanhDau.contains("TANG_LEVEL_TOANBO_DANH_DAU = 'toanbo'")
                && tb.contains("TANG_LEVEL_TOANBO_DANH_DAU") && tbGet.contains("tham_so = ?"));

        // --- API hang loat: chen thang vao hang doi, khong cho tung dong, khong UPDATE tob_char ---
        Kiem.dung("chi SELECT nhan vat con hoat dong (del = 1), khong dong den tob_char khac",
                tb.contains("SELECT id, charname FROM tob_char WHERE del = 1"));
        Kiem.sai("KHONG UPDATE tob_char (phai qua hang doi, giong giam_level/reset_toanbo)",
                Pattern.compile("UPDATE\\s+tob_char", Pattern.CASE_INSENSITIVE).matcher(tb).find());
        Kiem.dung("chen hang loat vao web_lenh_nhan_vat trong MOT transaction (khong cho tung dong)",
                tb.contains("await transaction(db,") && tb.contains("INSERT INTO web_lenh_nhan_vat")
                && tb.indexOf("await transaction(db,") < tb.indexOf("INSERT INTO web_lenh_nhan_vat"));
        Kiem.sai("khong doi ket qua tung dong (hang tram/nghin nhan vat se treo trang)", tb.contains("choKetQuaLenh"));
        Kiem.dung("ghi nhat ky hanh dong nang_level_toanbo", tb.contains("ghiNhatKy(event, 'nang_level_toanbo'"));

        // --- co trong menu quan tri ---
        final String menu = WebHtbb.doc("app/utils/adminMenu.ts");
        Kiem.dung("co muc menu tro toi /admin/nang-level-toanbo", menu.contains("to: '/admin/nang-level-toanbo'"));
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
        Kiem.bang("CAP_TOI_DA khop CAPDO_TOI_DA ben web (bang-cap-do.json)", 90, LenhWeb.CAP_TOI_DA);
        if (!WebHtbb.boQua("CAPDO_TOI_DA ben web")) {
            Kiem.dung("HTBB-Web bang-cap-do.json ghim capDoToiDa = 90 va capDo.ts doc dung khoa do",
                    WebHtbb.doc("shared/data/bang-cap-do.json").contains("\"capDoToiDa\": 90,")
                    && WebHtbb.doc("server/utils/capDo.ts").contains("CAPDO_TOI_DA: number = raw.capDoToiDa"));
        }
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
        if (WebHtbb.boQua("web khong ghi thang tob_char (HTBB-Web)")) {
            return;
        }
        final String nv = WebHtbb.doc(TS_CAP_DO);
        Kiem.dung("doc duoc " + WebHtbb.ten(TS_CAP_DO), !nv.isEmpty());
        Kiem.sai("API cap do KHONG UPDATE tob_char (nguyen nhan cua loi)",
                Pattern.compile("UPDATE\\s+tob_char", Pattern.CASE_INSENSITIVE).matcher(nv).find());
        Kiem.dung("ca 'dat_cap_do' va 'buff_level' deu di qua guiLenhCapDo() + choKetQuaLenh()",
                nv.contains("viec !== 'dat_cap_do' && viec !== 'buff_level'")
                && nv.contains("guiLenhCapDo(dbGame(), id, ten, capDoMoi, phanNghin, admin.username)")
                && nv.contains("choKetQuaLenh(dbGame(), idLenh)"));
        Kiem.dung("van ghi nhat ky hai hanh dong cu (dat_cap_do_nhan_vat / buff_level_nhan_vat)",
                nv.contains("'dat_cap_do_nhan_vat'") && nv.contains("'buff_level_nhan_vat'"));

        final String ts = WebHtbb.doc(TS_LENH);
        // Chi soi chuoi SQL (trong dau nhay); chu thich duoc phep nhac ten bang de giai thich.
        Kiem.sai("lenhNhanVat.ts khong co cau SQL nao dong tob_char",
                Pattern.compile("['`][^'`\\n]*tob_char[^'`\\n]*['`]").matcher(ts).find());
        Kiem.dung("web doi toi da 4 giay roi doc lai trang thai", ts.contains("LENH_CHO_TOI_DA_GIAY = 4.0"));
    }

    /** Java / web (HTBB-Web lenhNhanVat.ts) / SQL: cung ten bang, cung cot, cung ma trang thai. */
    private static void baBenCungHopDong() {
        final String java = doc("src/data/LenhWeb.java");
        final boolean coWeb = !WebHtbb.boQua("hop dong lenh voi HTBB-Web");
        final String php = WebHtbb.doc(TS_LENH);
        final String sql = DayDu.ddl("web_lenh_nhan_vat");
        final String bang = LenhWeb.TEN_BANG;

        Kiem.bang("ten bang Java", "web_lenh_nhan_vat", bang);
        if (coWeb) {
            Kiem.dung("web dung cung ten bang", php.contains("export const LENH_BANG = '" + bang + "'")
                    && php.contains("INSERT INTO " + bang + " ") && php.contains("FROM " + bang + " "));
        }
        Kiem.dung("htbb_day_du.sql co cung ten bang", sql.contains("CREATE TABLE `" + bang + "`"));

        final String[] cot = {"id", "loai", "char_id", "charname", "cap_do", "phan_nghin", "tham_so", "admin_user",
            "trang_thai", "ket_qua", "tao_luc", "xu_ly_luc"};
        for (final String c : cot) {
            Kiem.dung("cot `" + c + "` co trong ca Java" + (coWeb ? ", web" : "") + " va SQL",
                    java.contains("`" + c + "`") && (!coWeb || php.contains("`" + c + "`")) && sql.contains("`" + c + "`"));
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
        Kiem.bang("loai lenh", "dat_cap_do", LenhWeb.LOAI_DAT_CAP_DO);
        if (coWeb) {
            Kiem.dung("web dung cung ma trang thai 0..5",
                    php.contains("export const LENH_CHO = 0\n") && php.contains("export const LENH_XONG_ONLINE = 1\n")
                    && php.contains("export const LENH_XONG_OFFLINE = 2\n") && php.contains("export const LENH_LOI = 3\n")
                    && php.contains("export const LENH_CHO_DANG_NHAP = 4\n") && php.contains("export const LENH_HUY = 5\n"));
            Kiem.dung("web chi huy duoc dong con cho (CHO / CHO_DANG_NHAP), khong xoa ket qua da co",
                    php.contains("WHERE id = ? AND trang_thai IN (?, ?) AND loai != ?")
                            && php.contains("id, LENH_CHO, LENH_CHO_DANG_NHAP, LENH_LOAI_XOA_NHAN_VAT]"));
            // xoa_nhan_vat khong bao gio huy duoc (05/09/2026): Java doc dong CHO va web UPDATE
            // huy chay o hai tien trinh khac nhau, khong khoa gi - "huy" mot lenh da xoa xong se
            // hien nham "đã huỷ" trong khi nhan vat that ra da mat vinh vien. Xem chu thich day
            // du o huyLenh() trong HTBB-Web server/utils/lenhNhanVat.ts.
            final int mocHuyLenh = php.indexOf("export async function huyLenh(");
            Kiem.dung("tim thay ham huyLenh", mocHuyLenh > 0);
            if (mocHuyLenh > 0) {
                final int mocKetHuyLenh = php.indexOf("function ", mocHuyLenh + 30);
                final String thanHuyLenh = php.substring(mocHuyLenh, mocKetHuyLenh > mocHuyLenh ? mocKetHuyLenh : php.length());
                Kiem.dung("huyLenh loai tru han loai xoa_nhan_vat khoi dieu kien huy duoc",
                        thanHuyLenh.contains("LENH_LOAI_XOA_NHAN_VAT"));
            }
            Kiem.dung("web gui cung loai lenh", php.contains("export const LENH_LOAI_DAT_CAP_DO = 'dat_cap_do'"));
            // Ban PHP co bo kiem schema rieng (web/admin/test/kiem_schema.php); ban Nuxt tao bang
            // bang CUNG dinh nghia voi file SQL - doi chieu thang tung cot trong CREATE TABLE.
            Kiem.dung("web taoBangLenhNhanVat() tao cung bang (CREATE TABLE IF NOT EXISTS + 12 cot o tren)",
                    php.contains("CREATE TABLE IF NOT EXISTS \\`${LENH_BANG}\\`")
                    && php.contains("'`tham_so`    TEXT         NULL,'") && php.contains("'KEY `idx_trang_thai` (`trang_thai`)'"));
        }
        Kiem.dung("sql/nang_cap.sql tao bang web_lenh_nhan_vat (CSDL dang chay)",
                DayDu.nangCap().contains("CREATE TABLE IF NOT EXISTS `web_lenh_nhan_vat`"));
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

        // Offline: KHONG ghi lastLv/xp (dang nhap doc pInfo, khong doc hai cot do) -> nam cho.
        final String xl = than(java, "static KetQua xuLyDatCapDo");
        Kiem.sai("LenhWeb KHONG UPDATE lastLv/xp (dang nhap khong doc hai cot do)", java.contains("UPDATE tob_char SET lastLv"));
        Kiem.dung("(tien de) dang nhap lay cap/exp tu pInfo: CharNapDuLieu doc data[4]/data[5]",
                doc("src/real/CharNapDuLieu.java").contains("self.lastLV = Short.parseShort(data[5]);"));
        Kiem.dung("xuLy: offline -> CHO_DANG_NHAP, sau khi chac tob_char co nhan vat",
                xl.contains("return new KetQua(CHO_DANG_NHAP, \"offline") && xl.contains("if (!coNhanVat(charId))"));
        Kiem.dung("xuLy: dang CHO_DANG_NHAP ma chua vao game thi giu nguyen, khong ghi gi",
                xl.contains("if (trangThaiCu == CHO_DANG_NHAP)") && xl.contains("return new KetQua(CHO_DANG_NHAP, \"\");"));
        Kiem.dung("vong doc lay ca CHO va CHO_DANG_NHAP (de cong khi nguoi choi vua vao game)",
                // Commit a8c6644 (phan trang hang doi: "khong de 50 lenh offline dau tien chan ca
                // hang doi") them dieu kien con tro "AND id > ?" giua IN va ORDER BY.
                java.contains("WHERE trang_thai IN (?, ?) AND id > ? ORDER BY id")
                && java.contains("ps.setInt(1, CHO);") && java.contains("ps.setInt(2, CHO_DANG_NHAP);"));
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
