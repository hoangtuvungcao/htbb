import com.htbb.pokemon.HoTongNhiemVu;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuMau;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.HoTongNhiemVuGame;
import real.NhiemVuEngine;

/**
 * BAO LOI 31/10/2026 (kazuhas4 cap 49 o Tây Fuchsia, N-3.36 "Hộ tống Gói hàng từ Fuchsia tới Kecleon an toàn (0/1)"): "đến
 * bản đồ rồi mà không có hướng dẫn". Kiem (com.htbb.pokemon.HoTongNhiemVu / real.HoTongNhiemVuGame):
 * <ul>
 *   <li>Tổng tiêu đầu hien muc nhan hang cho nguoi dang lam buoc ho_tong ca khi myCountry != inCountry va het luot; nhan
 *       duoc ngoai gio / duoi cap 40; mot luot mien phi / ngay; nguoi khong co nhiem vu giu luat cu;</li>
 *   <li>Char.doResponseTieu (giao, khong bi cuop) dua buoc ho_tong len 1/1 (truoc day khop "" voi muc_tieu_id "1" -&gt; 0/1);</li>
 *   <li>chu So tay / luc nhan: noi nhan (Tổng tiêu đầu, lang, o) + noi giao (Kecleon tạp hoá / Dịch trạm);</li>
 *   <li>noi day: menu -50, xu ly chon muc, Kecleon giao hang, khoi SQL sua_ho_tong.</li>
 * </ul>
 */
public final class KiemHoTong {

    private KiemHoTong() {
    }

    static final int ID = 987_654_321;

    public static void chay() {
        Kiem.nhom("Ho tong: noi nhan / noi giao / dem buoc (bao loi kazuhas4 N-3.36 31/10/2026)");
        try {
            luat();
            nhanVat();
            maNguon();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static void luat() {
        Kiem.dung("dang lam ho_tong, khac nuoc, het luot: hien muc nhan", HoTongNhiemVu.hienMucNhan(0, 0, 1, true));
        Kiem.dung("khong nhiem vu, con luot, khac nuoc: an (luat cu)", !HoTongNhiemVu.hienMucNhan(2, 0, 1, false));
        Kiem.dung("khong nhiem vu, con luot, dung nuoc: hien", HoTongNhiemVu.hienMucNhan(2, 1, 1, false));
        Kiem.dung("khong nhiem vu, het luot: an", !HoTongNhiemVu.hienMucNhan(0, 1, 1, false));
        Kiem.dung("dang lam: nhan duoc luc 23h, cap 12, khac nuoc, o Pewter",
                HoTongNhiemVu.lyDoKhongNhan(true, 23, 12, false, 0, true, false) == null);
        Kiem.dung("dang lam nhung o Fuchsia: bao ve lang co Tổng tiêu đầu",
                String.valueOf(HoTongNhiemVu.lyDoKhongNhan(true, 12, 49, true, 8, true, false)).contains("Tổng tiêu đầu"));
        Kiem.dung("khong nhiem vu: ngoai 9h-21h bao gio", String.valueOf(HoTongNhiemVu.lyDoKhongNhan(true, 23, 49, true, 0, false, false)).contains("9h"));
        Kiem.dung("khong nhiem vu: cap 30 bao cap 40", String.valueOf(HoTongNhiemVu.lyDoKhongNhan(true, 12, 30, true, 70, false, false)).contains("40"));
        Kiem.dung("khong nhiem vu: khac nuoc co cau bao (truoc day im lang)", HoTongNhiemVu.lyDoKhongNhan(true, 12, 49, false, 80, false, false) != null);
        Kiem.dung("khong nhiem vu, dung dieu kien: nhan duoc", HoTongNhiemVu.lyDoKhongNhan(true, 12, 49, true, 0, false, false) == null);
        Kiem.dung("bao tri: tu choi ca nguoi dang lam", HoTongNhiemVu.lyDoKhongNhan(false, 12, 49, true, 0, true, false) != null);
        Kiem.dung("luot mien phi: het luot + dang lam + hom nay chua cap", HoTongNhiemVu.mienPhi(0, true, -1, 100));
        Kiem.dung("luot mien phi: mot lan / ngay", !HoTongNhiemVu.mienPhi(0, true, 100, 100));
        Kiem.dung("luot mien phi: con luot thi khong cap", !HoTongNhiemVu.mienPhi(2, true, -1, 100));
        Kiem.dung("luot mien phi: khong nhiem vu thi khong cap", !HoTongNhiemVu.mienPhi(0, false, -1, 100));
        Kiem.bang("chi so xe hang cap 12 (nhiem vu N-1.36)", 0, HoTongNhiemVu.chiSoCap(12, 6));
        Kiem.bang("chi so xe hang cap 75", 3, HoTongNhiemVu.chiSoCap(75, 6));
        Kiem.bang("chi so xe hang cap 100 (truoc day 6 -> tran mang)", 5, HoTongNhiemVu.chiSoCap(100, 6));
        final String c = HoTongNhiemVu.chiDan("Pewter", new int[]{25, 11}, new int[]{55, 40}, "Đường Victory", false, "");
        Kiem.dung("chi dan: noi nhan Tổng tiêu đầu ở Pewter (ô 25,11): " + c, c.contains("Tổng tiêu đầu ở Pewter (ô 25,11)"));
        Kiem.dung("chi dan: noi giao Kecleon tạp hoá (ô 55,40) / Dịch trạm Đường Victory",
                c.contains("Kecleon tạp hoá ở Pewter (ô 55,40)") && c.contains("Dịch trạm ở Đường Victory (ô 55,20)"));
        final String x = HoTongNhiemVu.chiDan("Pewter", new int[]{25, 11}, new int[]{55, 40}, "Đường Victory", true, "còn khoảng 3 ô");
        Kiem.dung("chi dan khi dang co xe: chi con buoc giao + chi duong", x.startsWith("Đang có xe hàng.") && !x.contains("Nhận hàng:")
                && x.contains("còn khoảng 3 ô"));
    }

    static NhiemVuMau mau() {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = "N-3.36";
        m.loai = "ngay";
        m.ten = "Hộ tống hàng Kecleon";
        m.kieu = "ho_tong";
        m.mucTieuId = "1"; // Gói hàng - dung du lieu that
        m.soLuong = 1;
        m.cap = 40;
        m.mucTieuText = "Nhận Gói hàng ở Tổng tiêu đầu (làng khởi đầu), hộ tống tới Kecleon tạp hoá an toàn";
        m.hoiThoaiDangLam = "Blaziken: Lửa lên nào! Hàng vẫn đang trên đường. Đừng để bị cướp!";
        return m;
    }

    static void nhanVat() {
        final boolean batCu = LauDai.bat();
        final boolean moCu = real.Map.openvantieu;
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        LauDai.dat(true);
        real.Map.openvantieu = true;
        HoTongNhiemVuGame.xoaMienPhi();
        final NhiemVuMau m = mau();
        final ConcurrentHashMap<String, long[]> td = new ConcurrentHashMap<String, long[]>();
        try {
            final Char p = new Char(null);
            p.charDBID = ID;
            p.isBot = -1;
            p.charname = "kiemhotong";
            p.lvDetail.lv = 49;
            p.myCountry = 0;
            p.inCountry = 1; // Pokemon: nhan vat co the dung o ban sao vung khac
            p.luot_van_tieu = 0;
            p.langDungVung = Boolean.TRUE; // nhan vat tao tu ban sua-tuyen: Kanto -> Pewter (LangKhoiDau, khong doc CSDL)
            final real.Map lang = new real.Map();
            lang.mapIDLoadMap = 0; // Pewter
            p.map = lang;
            p.mapID = 0;
            p.x = 40 * 16 + 8;
            p.y = 30 * 16 + 8;
            NhiemVuEngine.datKiem(Collections.singletonList(m), ID, td);

            // chua nhan nhiem vu: luat cu (het luot + khac nuoc -> an)
            Kiem.sai("chua nhan N-3.36: khong hien muc nhan hang", HoTongNhiemVuGame.hienMucNhan(p));
            Kiem.dung("chua nhan N-3.36, 23h, khac nuoc: tu choi co ly do", HoTongNhiemVuGame.lyDoKhongNhan(p, 23) != null);

            // da nhan buoc (Sổ tay): v = {tien do, trang thai, ky, bat dau}
            final long[] v = new long[]{0L, 0L, 0L, 0L};
            td.put(m.ma, v);
            Kiem.dung("dang lam N-3.36: nhan ra buoc ho_tong", HoTongNhiemVuGame.buocDangLam(p) == m);
            Kiem.dung("dang lam, myCountry 0 != inCountry 1, het luot: hien muc nhan hang", HoTongNhiemVuGame.hienMucNhan(p));
            Kiem.dung("chu muc: hang nhiem vu", HoTongNhiemVuGame.chuMucNhan(p).contains("hàng nhiệm vụ"));
            Kiem.dung("dang lam: nhan duoc luc 23h du khac nuoc", HoTongNhiemVuGame.lyDoKhongNhan(p, 23) == null);
            p.lvDetail.lv = 12;
            Kiem.dung("dang lam: nhan duoc o cap 12 (N-1.36)", HoTongNhiemVuGame.lyDoKhongNhan(p, 12) == null);
            p.lvDetail.lv = 49;
            Kiem.dung("het luot: cap mot luot mien phi", HoTongNhiemVuGame.capMienPhi(p));
            Kiem.bang("luot sau khi cap", 1, (int) p.luot_van_tieu);
            p.luot_van_tieu = 0;
            Kiem.sai("luot mien phi chi mot lan / ngay", HoTongNhiemVuGame.capMienPhi(p));

            // chu So tay "Đang làm" + luc nhan: noi nhan + noi giao
            final String chu = NhiemVuEngine.chuDangLam(m, 0) + HoTongNhiemVuGame.duoi(p, m);
            Kiem.dung("So tay: noi nhan Tổng tiêu đầu ở Pewter + o: " + chu, chu.contains("Tổng tiêu đầu ở Pewter (ô "));
            Kiem.dung("So tay: noi giao Kecleon tạp hoá ở Pewter (ô 55,40 tu cMap/0) + Dịch trạm",
                    chu.contains("Kecleon tạp hoá ở Pewter (ô 55,40)") && chu.contains("Dịch trạm"));
            Kiem.dung("So tay: chi duong tren ban do lang (o / huong)", chu.contains("ô về phía"));
            Kiem.sai("So tay: khong lap o hai lan", chu.contains("(ô 29,20)), chọn") || chu.contains("Tây Bắc (ô 29,20)"));
            Kiem.dung("So tay: chu muc tieu moi khong con 'từ Fuchsia'", !chu.contains("từ Fuchsia"));
            p.map = new real.Map();
            p.map.mapIDLoadMap = 8; // Fuchsia (noi nguoi choi dung trong bao loi)
            final String oFuchsia = HoTongNhiemVuGame.chiDan(p);
            Kiem.dung("dung o Fuchsia: bao ve lang nha: " + oFuchsia, oFuchsia.contains("hãy về Pewter") && oFuchsia.contains("Tổng tiêu đầu ở Pewter"));
            p.map = lang;
            Kiem.bang("buoc khac kieu: khong them duoi", "", HoTongNhiemVuGame.duoi(p, new NhiemVuMau()));

            // giao: bi cuop khong tinh, giao dung tinh 1/1
            p.doResponseTieu(p, true, 0, 49);
            Kiem.bang("bi cuop (isThief): khong tinh", 0L, v[0]);
            p.doResponseTieu(p, false, 0, 49);
            Kiem.bang("giao xe hang (doResponseTieu): N-3.36 len 1/1", 1L, v[0]);
            Kiem.bang("giao xe hang: trang thai xong cho nhan thuong", 1L, v[1]);
            Kiem.sai("xong roi: khong con dang lam (khong con mien phi / Kecleon)", HoTongNhiemVuGame.dangLam(p));
            p.luot_van_tieu = 3;
            Kiem.sai("xong roi, con luot, khac nuoc: luat cu (an)", HoTongNhiemVuGame.hienMucNhan(p));
        } finally {
            NhiemVuEngine.xoaKiem(ID);
            HoTongNhiemVuGame.xoaMienPhi();
            LauDai.dat(batCu);
            real.Map.openvantieu = moCu;
            real.Map.gemTemplate = gemCu;
        }
        final NhiemVuMau k = mau();
        Kiem.dung("NhiemVuMau.khop: ho_tong muc_tieu_id '1' (Gói hàng) khop su kien giao (tham so rong)", k.khop("ho_tong", ""));
    }

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    static void maNguon() throws Exception {
        Kiem.dung("RequestNPCInfoHandler -50: muc nhan hang theo HoTongNhiemVuGame.hienMucNhan (bo dieu kien nuoc cung)",
                doc("src/real/cmd/RequestNPCInfoHandler.java").contains("real.HoTongNhiemVuGame.hienMucNhan(p)")
                && !doc("src/real/cmd/RequestNPCInfoHandler.java").contains("p.luot_van_tieu > 0 && p.myCountry == p.inCountry"));
        Kiem.dung("RealController / MapMenuNpc: chon muc nhan hang -> HoTongNhiemVuGame.nhanHang",
                doc("src/real/RealController.java").contains("HoTongNhiemVuGame.nhanHang(player, idMenu, idOptionMenu);")
                && doc("src/real/MapMenuNpc.java").contains("HoTongNhiemVuGame.nhanHang(player, idMenu, idOptionMenu);"));
        Kiem.dung("NpcDichVu: Kecleon tạp hoá giao hang nhiem vu", doc("src/real/NpcDichVu.java").contains("HoTongNhiemVuGame.traTaiKecleon(p)"));
        Kiem.dung("Char.doInitMonter: chi so cap kep + xe o ban sao inCountry",
                doc("src/real/Char.java").contains("HoTongNhiemVu.chiSoCap(p.lvDetail.lv, Char.HP_MONSTER_VANTIEU.length)")
                && doc("src/real/Char.java").contains("p.x, p.y, p.inCountry);"));
        Kiem.dung("NhiemVuEngine: So tay + luc nhan kem chi dan", doc("src/real/NhiemVuEngine.java").contains("chuDangLam(m, v[0]) + HoTongNhiemVuGame.duoi(p, m)")
                && doc("src/real/NhiemVuEngine.java").contains("+ HoTongNhiemVuGame.duoi(p, m); // sua-ho-tong"));
        final String sql = doc("../sql/htbb.sql");
        final int i = sql.indexOf("-- >>> sua_ho_tong.sql");
        Kiem.dung("sql/htbb.sql: khoi sua_ho_tong.sql", i > 0);
        final String khoi = i > 0 ? sql.substring(i) : "";
        int n = 0;
        for (final String ma : new String[]{"N-1.36", "N-2.36", "N-3.36", "N-4.36", "N-5.36", "TV-10.3", "HT-MEW.5", "C73.2"}) {
            n += khoi.contains("WHERE `ma` = '" + ma + "' AND `muc_tieu_text` = 'Hộ tống ") ? 1 : 0;
        }
        Kiem.bang("sua_ho_tong.sql: 8 buoc ho_tong engine (UPDATE theo chu cu, chay lai duoc)", 8, n);
        Kiem.dung("sua_ho_tong.sql: N-3.36 ghi Tổng tiêu đầu + Kecleon tạp hoá",
                khoi.contains("`muc_tieu_text` = 'Nhận Gói hàng ở Tổng tiêu đầu (làng khởi đầu), hộ tống tới Kecleon tạp hoá an toàn'"));
    }
}
