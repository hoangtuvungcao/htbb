package real;

import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.ThongSoLauDai;
import com.htbb.pokemon.TrangThaiPokemon;
import data.Database;
import data.DatabaseLauDai;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vat pham tieu hao / do giu moi (GD2b, docs/thiet-ke-game.md + 9.1): data_item type 28 id
 * 1201..1260 (tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_vat_pham_lau_dai.sql) + bang data_vat_pham_lau_dai (id -> hieu_ung lay
 * NGUYEN chuoi cua doi noi dung, vd "tc:20,giay:90"). Bo doc hieu ung la DU LIEU: them vat pham moi
 * chi can them dong, khong sua code, mien la dung cac khoa da ho tro duoi day. Chi chay khi LauDai.bat().
 *
 * Khoa ho tro: hp, mp, mp_doi (%); go:<1..5|tat_ca>, mien:<giay>; hoi_sinh:<%HP>; gio_treo:<gio> (Ve treo,
 * TuDongGame.dungVeTreo);
 * tc / thu / chi_mang / boss / toc_danh / chinh_xac + giay (buff co han); xua:<cap>, phut (xua quai);
 * exp_phut + tru_nang_luong_phut (Keo Hiem, 2/tuan); tu_dung:<hp50|mp20|trang_thai|bong|doc|te|ngu|dong_bang>
 * (qua mong: dung tay hoac TU DUNG khi du dieu kien, LauDaiJob); o:phien_da (do giu: bam dung = mang/thao,
 * hieu ung exp / chi_mang / hut / hoi_hp_3s / don_thuong / song_sot / chan_tien_hoa); bua:<k1-k2>
 * (bua bao ho that, NangCapLauDaiGame); nhom 'hop' + mo_hop:<id> (hop dot kich 1050/1051 -> NoiDungLauDai.moHop). toc_danh: giam hoi chieu (Char.calculateAttrib + goi 35 cho client);
 * chinh_xac: don danh quai khong truot nen ap thanh xuyen giap diem/3 % (xuyenGiap); het han -> nhip() tinh lai.
 * toc_danh_ngau_nhien (Mong vuot nhanh): CHUA noi vao cong thuc - xem docs/thiet-ke-game.md "Con lai".
 * Moi phan hoi qua DungVatPham (thong bao nhanh -104 / hop thoai 37).
 */
public final class VatPhamDungLauDai {

    /** id_item -> {ma, nhom, hieu_ung, gioi_han, capTu, capDen}. */
    static volatile java.util.Map<Integer, Object[]> BANG = java.util.Collections.emptyMap();

    /** Buff co han: charDBID -> (khoa -> {het han ms, gia tri}). */
    private static final ConcurrentHashMap<Integer, HashMap<String, long[]>> BUFF = new ConcurrentHashMap<Integer, HashMap<String, long[]>>();

    static final String[] KHOA_BUFF = {"tc", "thu", "chi_mang", "boss", "toc_danh", "chinh_xac", "xua"};

    /** hieu_ung "gio_treo:N" (Ve treo N gio, id com.htbb.pokemon.TuDong.ID_VE_TREO): +N gio kho Tu dong danh. */
    public static final String HIEU_UNG_GIO_TREO = "gio_treo";

    private VatPhamDungLauDai() {
    }

    /** Nap bang luc khoi dong (TeamServer, bat co). */
    public static void nap() {
        BANG = DatabaseLauDai.napVatPhamLauDai();
        System.out.println("[LAU DAI] vat pham tieu hao / do giu moi: " + BANG.size() + " mau");
    }

    /** Id vat pham dau tien (nho nhat) co khoa hieu ung {@code khoa} (vd "dat_ten"), 0 neu chua co dong. */
    public static int idTheoHieuUng(final String khoa) {
        int id = 0;
        for (final java.util.Map.Entry<Integer, Object[]> e : BANG.entrySet()) {
            if (hieuUng((String) e.getValue()[2]).containsKey(khoa) && (id == 0 || e.getKey().intValue() < id)) {
                id = e.getKey().intValue();
            }
        }
        return id;
    }

    /** Phan tich "a:1,b:2" -> map. */
    static HashMap<String, String> hieuUng(final String s) {
        final HashMap<String, String> m = new HashMap<String, String>();
        if (s == null) {
            return m;
        }
        for (final String phan : s.split(",")) {
            final int i = phan.indexOf(':');
            if (i > 0) {
                m.put(phan.substring(0, i).trim(), phan.substring(i + 1).trim());
            }
        }
        return m;
    }

    static int so(final HashMap<String, String> m, final String k, final int macDinh) {
        try {
            return m.containsKey(k) ? Integer.parseInt(m.get(k)) : macDinh;
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    // ------------------------------------------------------------------ dung tay

    /** Map.doUseItem. @return true neu la vat pham cua bang nay (da xu ly, ke ca tu choi). */
    public static boolean dung(final Char p, final Item item) {
        if (!LauDai.bat() || p == null || item == null || item.getTemplate() == null) {
            return false;
        }
        if (TangTocGame.dung(p, item)) {
            return true; // ve EXP / bua the luc / uy thac (tran chung TangToc)
        }
        final Object[] d = BANG.get(Integer.valueOf(item.getTemplate().id));
        if (d == null) {
            return false;
        }
        final String nhom = (String) d[1];
        final HashMap<String, String> hu = hieuUng((String) d[2]);
        if (p.lvDetail.lv < ((Integer) d[4]).intValue()) {
            DungVatPham.loi(p, "Cần đạt cấp " + d[4] + " để dùng " + DungVatPham.ten(item) + ".");
            return true;
        }
        if ("bua".equals(nhom)) {
            DungVatPham.loi(p, "Bùa bảo hộ tự dùng khi nâng cấp thất bại (đặt ngọc bảo hiểm hoặc chọn bảo hộ).");
            return true;
        }
        if ("hop".equals(nhom)) {
            moHop(p, item);
            return true;
        }
        if (hu.containsKey("o")) {
            doiDoGiu(p, item);
            return true;
        }
        if (hu.containsKey(DatTenPetGame.HIEU_UNG)) {
            DatTenPetGame.batDau(p, item); // the dat ten Pokemon: menu chon pet -> o nhap ten (the tru khi dat xong)
            return true;
        }
        if (hu.containsKey(HIEU_UNG_GIO_TREO)) {
            // Ve treo N gio: cong N gio vao kho gio Tu dong danh (TuDongGame tu tru ve / bao loi)
            TuDongGame.dungVeTreo(p, item, so(hu, HIEU_UNG_GIO_TREO, com.htbb.pokemon.TuDong.GIO_MOI_VE_TREO));
            return true;
        }
        final String loi = kiemDung(p, hu);
        if (loi != null) {
            DungVatPham.loi(p, loi);
            return true;
        }
        if (hu.containsKey("exp_phut") && !keoHiemDuoc(p)) {
            DungVatPham.loi(p, "Kẹo hiếm: tối đa 2 viên mỗi tuần.");
            return true;
        }
        if (hu.containsKey("exp_phut")) {
            final String tg = TangTocGame.kiemKeo(p); // chung tran diem tang toc ngay/tuan, khong dung o cap toi da
            if (tg != null) {
                DungVatPham.loi(p, tg);
                return true;
            }
        }
        if (hu.containsKey("hoi_sinh")) {
            final String hs = HoiSinhGame.chanDungTuTui(p); // cam / cho / hoi chieu cua co che hoi sinh
            if (hs != null) {
                DungVatPham.loi(p, hs);
                return true;
            }
        }
        // tru vat pham TRUOC (CODING_CONVENTION muc 5); [chong-vat-pham] chong: chi tru 1 don vi
        if (!com.htbb.pokemon.ChongVatPham.tieuMot(p, item)) {
            return true;
        }
        apDung(p, hu);
        if (hu.containsKey("hoi_sinh")) {
            HoiSinhGame.daHoiSinh(p);
        }
        if (hu.containsKey("exp_phut")) {
            TangTocGame.ghiKeo(p);
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        try {
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "dung " + d[0] + " dbid" + item.dbid, "vatphamlaudai");
        } catch (final Exception ignored) {
        }
        final String mt = moTa(hu);
        DungVatPham.bao(p, "Đã dùng " + DungVatPham.ten(item) + (mt.isEmpty() ? "." : ": " + mt + "."));
        return true;
    }

    /** Mo ta ngan hieu ung cho thong bao "Da dung X: ...". */
    static String moTa(final HashMap<String, String> hu) {
        final StringBuilder sb = new StringBuilder();
        if (hu.containsKey("hoi_sinh")) {
            noi(sb, "hồi sinh với " + so(hu, "hoi_sinh", 30) + "% HP");
        }
        if (hu.containsKey("hp")) {
            noi(sb, "+" + so(hu, "hp", 0) + "% HP");
        }
        if (hu.containsKey("mp")) {
            noi(sb, "+" + so(hu, "mp", 0) + "% PP");
        }
        if (hu.containsKey("mp_doi")) {
            noi(sb, "+" + so(hu, "mp_doi", 0) + "% PP cho cả đội");
        }
        if (hu.containsKey("go")) {
            noi(sb, "giải trạng thái" + (so(hu, "mien", 0) > 0 ? ", miễn " + so(hu, "mien", 0) + " giây" : ""));
        }
        final int giay = so(hu, "giay", so(hu, "phut", 0) * 60);
        final String trong = giay >= 120 && giay % 60 == 0 ? " trong " + giay / 60 + " phút" : " trong " + giay + " giây";
        if (hu.containsKey("tc")) {
            noi(sb, "+" + so(hu, "tc", 0) + "% tấn công" + trong);
        }
        if (hu.containsKey("thu")) {
            noi(sb, "+" + so(hu, "thu", 0) + "% phòng thủ" + trong);
        }
        if (hu.containsKey("chi_mang")) {
            noi(sb, "+" + so(hu, "chi_mang", 0) + "% chí mạng" + trong);
        }
        if (hu.containsKey("boss")) {
            noi(sb, "+" + so(hu, "boss", 0) + "% sát thương boss" + trong);
        }
        if (hu.containsKey("toc_danh")) {
            noi(sb, "+" + so(hu, "toc_danh", 0) + "% tốc đánh" + trong);
        }
        if (hu.containsKey("chinh_xac")) {
            noi(sb, "+" + Math.max(1, so(hu, "chinh_xac", 0) / 3) + "% xuyên giáp" + trong);
        }
        if (hu.containsKey("xua")) {
            noi(sb, "quái yếu hơn " + so(hu, "xua", 0) + " cấp không tấn công" + trong);
        }
        if (hu.containsKey("exp_phut")) {
            noi(sb, "+EXP bằng " + so(hu, "exp_phut", 30) + " phút đánh quái");
        }
        return sb.toString();
    }

    static void noi(final StringBuilder sb, final String s) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(s);
    }

    /** Ly do khong dung duoc (truoc khi tru vat pham), null neu dung duoc. */
    static String kiemDung(final Char p, final HashMap<String, String> hu) {
        if (hu.containsKey("hoi_sinh") && p.hp > 0) {
            return "Chỉ dùng được khi đã gục.";
        }
        if (!hu.containsKey("hoi_sinh") && p.hp <= 0) {
            return "Không dùng được khi đã gục.";
        }
        // Chi hoi HP / PP (qua Oran, Sitrus, Leppa...): day roi thi khong tru vat pham.
        final boolean chiHoi = !hu.containsKey("go") && !hu.containsKey("exp_phut") && !coBuff(hu);
        if (chiHoi && hu.containsKey("hp") && !hu.containsKey("mp") && p.hp >= p.maxhp) {
            return "HP đã đầy, chưa cần dùng.";
        }
        if (chiHoi && hu.containsKey("mp") && !hu.containsKey("hp") && p.mp >= p.maxmp) {
            return "PP đã đầy, chưa cần dùng.";
        }
        if (hu.containsKey("go") && !hu.containsKey("hp")) {
            final TrangThaiPokemon t = TrangThaiGame.lay(p);
            if (t == null || !t.dangCo(com.htbb.pokemon.DongHo.bay())) {
                return "Bạn không bị trạng thái nào.";
            }
        }
        return null;
    }

    static boolean coBuff(final HashMap<String, String> hu) {
        for (final String k : KHOA_BUFF) {
            if (hu.containsKey(k)) {
                return true;
            }
        }
        return hu.containsKey("mp_doi");
    }

    /** Ap mot chuoi hieu ung (vat pham da tru). */
    static void apDung(final Char p, final HashMap<String, String> hu) {
        final long bay = com.htbb.pokemon.DongHo.bay();
        if (hu.containsKey("hoi_sinh")) {
            p.hp = Math.max(1, p.maxhp * so(hu, "hoi_sinh", 30) / 100);
            try {
                p.desTroy();
            } catch (final Exception ignored) {
            }
            guiHp(p);
        }
        if (hu.containsKey("hp")) {
            p.hp = Math.min(p.maxhp, p.hp + p.maxhp * so(hu, "hp", 0) / 100);
            guiHp(p);
        }
        if (hu.containsKey("mp")) {
            p.mp = Math.min(p.maxmp, p.mp + p.maxmp * so(hu, "mp", 0) / 100);
            guiHp(p);
        }
        if (hu.containsKey("mp_doi")) {
            for (final Char c : ExpLauDai.thanhVienGan(p)) {
                c.mp = Math.min(c.maxmp, c.mp + c.maxmp * so(hu, "mp_doi", 0) / 100);
                guiHp(c);
            }
        }
        if (hu.containsKey("go")) {
            TrangThaiGame.go(p, "tat_ca".equals(hu.get("go")) ? 0 : so(hu, "go", 0), so(hu, "mien", 0) * 1000L);
        }
        final int giay = so(hu, "giay", so(hu, "phut", 0) * 60);
        boolean tinhLai = false;
        for (final String k : KHOA_BUFF) {
            if (hu.containsKey(k) && giay > 0) {
                datBuff(p, k, so(hu, k, 0), bay + giay * 1000L);
                tinhLai |= laBuffChiSo(k);
            }
        }
        if (tinhLai) {
            tinhLaiChiSo(p);
        }
        if (hu.containsKey("exp_phut")) {
            keoHiem(p, so(hu, "exp_phut", 30), so(hu, "tru_nang_luong_phut", 30));
        }
    }

    /** Hop dot kich (1050/1051, nhom 'hop'): tru hop TRUOC roi mo theo data_noi_dung_roi nguon mo_hop. */
    static void moHop(final Char p, final Item item) {
        if (p.countSlotInventory() < 2) {
            DungVatPham.loi(p, "Hành trang cần 2 ô trống để mở hộp.");
            return;
        }
        if (!com.htbb.pokemon.ChongVatPham.tieuMot(p, item)) { // [chong-vat-pham] chong: chi tru 1 hop
            return;
        }
        if (item.dbid != 0 && !p.iItems.contains(item)) {
            try {
                Database.instance.deleteItem(item.dbid);
            } catch (final Exception ignored) {
            }
        }
        final String kq = NoiDungLauDai.moHop(p, item.getTemplate().id);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        p.sendMessage(MessageCreator.createCharGemItem(p));
        try {
            Database.instance.saveOrtherLog("tob_log_other_item", p.charname, "mo hop " + item.getTemplate().id + " dbid" + item.dbid
                    + ": " + kq, "vatphamlaudai");
        } catch (final Exception ignored) {
        }
        DungVatPham.bao(p, kq.isEmpty() ? "Hộp trống." : "Mở " + DungVatPham.ten(item) + ": " + kq + ".");
    }

    static void guiHp(final Char p) {
        try {
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            p.sendToNearPlayer(MessageCreator.createNew_HMP_Message(p, 0));
        } catch (final Exception ignored) {
        }
    }

    static void bao(final Char p, final String s) {
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
    }

    // ------------------------------------------------------------------ Keo Hiem

    static boolean keoHiemDuoc(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return false;
        }
        synchronized (h) {
            final int tuan = NhiemVuLauDai.tuan(com.htbb.pokemon.DongHo.bay());
            if (h.khTuan != tuan) {
                h.khTuan = tuan;
                h.khSo = 0;
            }
            return h.khSo < 2;
        }
    }

    /** EXP = {@code phut} phut danh quai o cap hien tai; tru {@code truPhut} phut nang luong 100% (khong vuot tran ngay). */
    static void keoHiem(final Char p, final int phut, final int truPhut) {
        // khoi tao ngay moi (cau hinh + thuong thanh vien) truoc khi tru phut 100%
        final HoSoLauDai h = LauDaiNhanVat.chuanBiNgay(p);
        if (h == null) {
            return;
        }
        synchronized (h) {
            h.khSo++;
            com.htbb.pokemon.NangLuong.ghiNhan(h, com.htbb.pokemon.DongHo.bay(), ThongSoLauDai.NANG_LUONG_PHUT_NGAY,
                    ThongSoLauDai.NANG_LUONG_PHUT_TICH_TOI_DA);
            h.nlMsDung += truPhut * 60000L;
            h.ban = true;
        }
        final long exp = ThongSoLauDai.expMoiGio(LauDai.kepCap(p.lvDetail.lv)) * phut / 60L;
        try {
            Map.addXPForChar(p, exp, false, "keo hiem");
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ buff co han

    /** Buff doi chi so luu san (calculateAttrib): toc danh -> hoi chieu, chinh xac -> xuyen giap. */
    static boolean laBuffChiSo(final String k) {
        return "toc_danh".equals(k) || "chinh_xac".equals(k);
    }

    /** Tinh lai chi so + gui bang hoi chieu moi (client tu dem hoi chieu theo goi 35) va thong tin nhan vat. */
    static void tinhLaiChiSo(final Char p) {
        try {
            p.calculateAttrib();
            p.sendMessage(MessageCreator.createSkillInfoMessage(p));
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
        } catch (final Exception ignored) {
        }
    }

    /** % xuyen giap tu X Chinh xac (diem chinh xac / 3), 0 neu khong co. */
    public static int xuyenGiap(final Char p) {
        final int cx = buff(p, "chinh_xac");
        return cx <= 0 ? 0 : Math.max(1, cx / 3);
    }

    /** nhip(): buff doi chi so vua het han -> xoa + tinh lai (khong thi hoi chieu / xuyen giap giu mai). */
    static void hetHanBuffChiSo(final Char p) {
        final HashMap<String, long[]> m = BUFF.get(Integer.valueOf(p.charDBID));
        if (m == null) {
            return;
        }
        boolean het = false;
        synchronized (m) {
            final long bay = com.htbb.pokemon.DongHo.bay();
            final java.util.Iterator<Entry<String, long[]>> it = m.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String, long[]> e = it.next();
                if (laBuffChiSo(e.getKey()) && e.getValue()[0] <= bay) {
                    it.remove();
                    het = true;
                }
            }
        }
        if (het) {
            tinhLaiChiSo(p);
            DungVatPham.baoNhe(p, "Hết hiệu lực vật phẩm tăng cường.");
        }
    }

    static void datBuff(final Char p, final String k, final int giaTri, final long het) {
        final HashMap<String, long[]> m = BUFF.computeIfAbsent(Integer.valueOf(p.charDBID), x -> new HashMap<String, long[]>());
        synchronized (m) {
            m.put(k, new long[]{het, giaTri});
        }
    }

    /** Gia tri buff {@code k} con hieu luc (0 neu khong co). */
    public static int buff(final Char p, final String k) {
        if (!LauDai.bat() || p == null) {
            return 0;
        }
        final HashMap<String, long[]> m = BUFF.get(Integer.valueOf(p.charDBID));
        if (m == null) {
            return 0;
        }
        synchronized (m) {
            final long[] v = m.get(k);
            return v != null && v[0] > com.htbb.pokemon.DongHo.bay() ? (int) v[1] : 0;
        }
    }

    // ------------------------------------------------------------------ do giu

    /** Bam dung do giu: mang (thay mon dang mang) hoac thao neu dang mang chinh mon do. */
    static void doiDoGiu(final Char p, final Item item) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            DungVatPham.loi(p, "Tạm thời không dùng được, thử lại sau.");
            return;
        }
        final int id = item.getTemplate().id;
        synchronized (h) {
            h.doGiu = h.doGiu == id ? 0 : id;
            h.ban = true;
        }
        DungVatPham.bao(p, h.doGiu == id ? "Đang mang: " + DungVatPham.ten(item) + "." : "Đã cất " + DungVatPham.ten(item) + ".");
        try {
            p.calculateAttrib();
        } catch (final Exception ignored) {
        }
    }

    /** Hieu ung cua do giu dang mang (con trong tui), map rong neu khong co. */
    static HashMap<String, String> doGiu(final Char p) {
        final HoSoLauDai h = LauDai.bat() ? LauDaiNhanVat.lay(p) : null;
        if (h == null || h.doGiu == 0) {
            return new HashMap<String, String>();
        }
        final Object[] d = BANG.get(Integer.valueOf(h.doGiu));
        if (d == null || demItem(p, h.doGiu) <= 0) {
            return new HashMap<String, String>();
        }
        return hieuUng((String) d[2]);
    }

    /** Gia tri hieu ung {@code k} cua do giu (0 neu khong mang). */
    public static int doGiuSo(final Char p, final String k) {
        if (!LauDai.bat() || p == null) {
            return 0;
        }
        return so(doGiu(p), k, 0);
    }

    // ------------------------------------------------------------------ diem goi tu chien dau

    /** Char.havecrit: cong them diem chi mang (thang 300 nhu ban cu: 1% = 3). */
    public static int congChiMang(final Char p) {
        return (buff(p, "chi_mang") + doGiuSo(p, "chi_mang")) * 3;
    }

    /** % tan cong cong them (X Tan cong). */
    public static int congTanCong(final Char p) {
        return buff(p, "tc");
    }

    /** % EXP do giu (Trung may man), cong vao tran buff. */
    public static int congExp(final Char p) {
        return doGiuSo(p, "exp");
    }

    /** Quai danh nguoi: giam theo X Phong thu; Bang do tap trung (song sot 10%, hoi 120 giay). */
    public static int satThuongNhan(final LiveActor muc, final int ahp) {
        if (!LauDai.bat() || !(muc instanceof Char)) {
            return ahp;
        }
        final Char c = (Char) muc;
        int d = ahp;
        final int thu = buff(c, "thu");
        if (thu > 0) {
            d = ChiSoTrangBiGame.bat() ? ChiSoTrangBiGame.giam(c, d, Math.min(500, thu * 10)) // tran 50% + san
                    : d * (100 - Math.min(80, thu)) / 100;
        }
        if (d >= c.hp && c.hp > 1) {
            final int sot = doGiuSo(c, "song_sot");
            if (sot > 0 && buff(c, "hoi_song_sot") == 0 && Map.random(100) < sot) {
                datBuff(c, "hoi_song_sot", 1, com.htbb.pokemon.DongHo.bay() + doGiuSo(c, "hoi") * 1000L);
                d = c.hp - 1;
            }
        }
        return Math.max(0, d);
    }

    /** Sat thuong vao boss (Thuoc san trum +30%). */
    public static int vaoBoss(final Char p, final int dam) {
        final int b = buff(p, "boss");
        return b > 0 ? (int) Math.min(Integer.MAX_VALUE, (long) dam * (100 + b) / 100L) : dam;
    }

    /** Sau moi don trung (GoiLauDaiGame.don): Chuong vo so hut %. */
    public static void sauDonDanh(final Char p, final int dam) {
        if (ChiSoTrangBiGame.bat()) {
            return; // da gop vao hut HP chung (ChiSoTrangBiGame.hutSauDon, tran 10% / PvP 5%)
        }
        final int hut = doGiuSo(p, "hut");
        if (hut > 0 && dam > 0 && p.hp > 0 && p.hp < p.maxhp) {
            p.hp = Math.min(p.maxhp, p.hp + Math.max(1, dam * hut / 100));
        }
    }

    /** % sat thuong don thuong (idSkill 0) them - Dai luc si. */
    public static int donThuong(final Char p) {
        return doGiuSo(p, "don_thuong");
    }

    /** Xua quai: quai thap hon >= xua cap khong chu dong danh. */
    public static boolean xuaQuai(final Char p, final Monster mt) {
        final int x = buff(p, "xua");
        return x > 0 && mt != null && !mt.isBoss && p.lvDetail.lv - mt.level >= x;
    }

    /** Da vinh cuu: chan tien hoa. */
    public static boolean chanTienHoa(final Char p) {
        return doGiuSo(p, "chan_tien_hoa") > 0;
    }

    // ------------------------------------------------------------------ tu dung (moi 2-3 giay, LauDaiJob)

    public static void nhip() {
        if (!LauDai.bat()) {
            return;
        }
        for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
            final Char p;
            try {
                p = CharManager.instance.vChars.elementAt(i);
            } catch (final Exception e) {
                break;
            }
            if (p == null || p.isBot != -1 || p.hp <= 0) {
                continue;
            }
            try {
                TangTocGame.nhip(p); // EXP uy thac luyen tap (tinh luc dang nhap)
                hetHanBuffChiSo(p);
                final int hoi = doGiuSo(p, "hoi_hp_3s");
                if (hoi > 0 && p.hp < p.maxhp) {
                    p.hp = Math.min(p.maxhp, p.hp + Math.max(1, p.maxhp * hoi / 100));
                }
                tuDungQua(p);
            } catch (final Exception ignored) {
                // vong khac
            }
        }
    }

    /** Qua mong trong tui tu dung khi du dieu kien (moi lan toi da 1 qua). */
    static void tuDungQua(final Char p) {
        final TrangThaiPokemon t = TrangThaiGame.lay(p);
        final long bay = com.htbb.pokemon.DongHo.bay();
        final int tt = t != null && t.dangCo(bay) ? t.loai : 0;
        for (final Entry<Integer, Object[]> e : BANG.entrySet()) {
            final HashMap<String, String> hu = hieuUng((String) e.getValue()[2]);
            final String dk = hu.get("tu_dung");
            if (dk == null) {
                continue;
            }
            final boolean du;
            if ("hp50".equals(dk)) {
                du = p.hp * 2 < p.maxhp;
            } else if ("mp20".equals(dk)) {
                du = p.mp * 5 < p.maxmp;
            } else if ("trang_thai".equals(dk)) {
                du = tt != 0;
            } else {
                du = tt != 0 && tt == ttTheoTen(dk);
            }
            if (!du) {
                continue;
            }
            final Item it = timItem(p, e.getKey().intValue());
            if (it != null && com.htbb.pokemon.ChongVatPham.tieuMot(p, it)) { // [chong-vat-pham] chong: tru 1
                apDung(p, hu);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                DungVatPham.baoNhe(p, "Tự dùng " + DungVatPham.ten(it) + ".");
                return;
            }
        }
    }

    static int ttTheoTen(final String s) {
        if ("bong".equals(s)) {
            return TrangThaiPokemon.BONG;
        }
        if ("doc".equals(s)) {
            return TrangThaiPokemon.DOC;
        }
        if ("te".equals(s)) {
            return TrangThaiPokemon.TE;
        }
        if ("ngu".equals(s)) {
            return TrangThaiPokemon.NGU;
        }
        if ("dong_bang".equals(s)) {
            return TrangThaiPokemon.BANG;
        }
        return -1;
    }

    // ------------------------------------------------------------------ tien ich tui

    /** [chong-vat-pham] Tong SO LUONG theo template (chong tinh du so luong, bo mon dang rao ban). */
    public static int demItem(final Char p, final int idTemplate) {
        return com.htbb.pokemon.ChongVatPham.dem(p.iItems, idTemplate);
    }

    static Item timItem(final Char p, final int idTemplate) {
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.getTemplate() != null && it.getTemplate().id == idTemplate && !it.isSelling) {
                    return it;
                }
            }
        }
        return null;
    }

    /**
     * Lay ra {@code n} don vi theo template (tat ca hoac khong). [chong-vat-pham] chong giam so luong, chong het thi lay
     * ra khoi tui (tra id). @return true neu da lay du.
     */
    public static boolean truItem(final Char p, final int idTemplate, final int n) {
        return com.htbb.pokemon.ChongVatPham.tieu(p, idTemplate, n);
    }

    /** id bua bao ho that cho muc +k (0 neu khong co bua nao hop). */
    public static int buaChoMuc(final int k) {
        for (final Entry<Integer, Object[]> e : BANG.entrySet()) {
            if (!"bua".equals(e.getValue()[1])) {
                continue;
            }
            final String b = hieuUng((String) e.getValue()[2]).get("bua");
            if (b == null || b.indexOf('-') < 0) {
                continue;
            }
            final int tu = Integer.parseInt(b.substring(0, b.indexOf('-')));
            final int den = Integer.parseInt(b.substring(b.indexOf('-') + 1));
            if (k >= tu && k <= den) {
                return e.getKey().intValue();
            }
        }
        return 0;
    }

    static int capQuai(final Monster m) {
        return m == null ? 0 : CapDoLauDai.CAP_BANG > 0 ? m.level : 0;
    }
}
