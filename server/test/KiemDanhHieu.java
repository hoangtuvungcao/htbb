import com.htbb.pokemon.DanhHieuLogic;
import com.htbb.pokemon.DanhHieuLogic.Mau;
import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.DanhHieuGame;

/**
 * DANH HIEU (0.9.7): mo khoa tu bo dem / trang thai song, an "???" va hien sau khi mo, het han + gia han, chi MOT
 * danh hieu dang deo cho chi so, tran o (danh_hieu.tran_phan_nghin) + tran cung, goi tin -110, va du lieu
 * sql/them_danh_hieu.sql (idempotent, chi so trong ngan sach, ten khop danh hieu cu / thuong nhiem vu).
 */
public final class KiemDanhHieu {

    private KiemDanhHieu() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    static int idKe = 70000;

    public static void chay() {
        Kiem.nhom("Danh hieu - mo khoa, an / hien, het han, mot danh hieu dang deo, tran chi so, du lieu SQL");
        logic();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        try {
            if (real.Map.gemTemplate == null) {
                real.Map.gemTemplate = new real.GemTemplate[0];
            }
            game();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("game: khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            com.htbb.pokemon.LauDai.dat(false);
            DanhHieuGame.datMau(new ArrayList<Mau>(), null);
        }
        goiTin();
        duLieu();
        moc();
    }

    // ------------------------------------------------------------------ logic thuan

    static Mau mau(final int id, final String dk, final String ts, final long sl, final int an, final int han,
            final String cs, final int pn) {
        return new Mau(id, "k" + id, "Danh hiệu " + id, "Mô tả " + id, an == 1 ? "Gợi ý" : "", 1, "sao", cs, pn, dk, ts, sl,
                an, han, "thanh_tuu", id);
    }

    /** Ngu canh gia lap. */
    static final class Gia implements DanhHieuLogic.NguCanh {
        int cap, dexPt, dexLoai, bac = -1, hang, gym;
        final Map<String, Long> dem = new HashMap<String, Long>();
        final Map<String, Integer> tap = new HashMap<String, Integer>();
        final Set<String> xong = new HashSet<String>(), suKien = new HashSet<String>(), sinhThai = new HashSet<String>(),
                nv = new HashSet<String>();

        public int cap() {
            return cap;
        }

        public int pokedexPhanTram() {
            return dexPt;
        }

        public int pokedexLoai() {
            return dexLoai;
        }

        public boolean xongNhiemVu(final String ma) {
            return xong.contains(ma);
        }

        public int pvpBac() {
            return bac;
        }

        public int hangPvp() {
            return hang;
        }

        public boolean suKienBat(final String ma) {
            return suKien.contains(ma);
        }

        public boolean sinhThai(final String ten) {
            return sinhThai.contains(ten);
        }

        public boolean nhiemVu(final String ten) {
            return nv.contains(ten);
        }

        public int gymHuyHieu() {
            return gym;
        }

        public long dem(final String khoa) {
            final Long v = dem.get(khoa);
            return v == null ? 0L : v.longValue();
        }

        public int tap(final String khoa) {
            final Integer v = tap.get(khoa);
            return v == null ? 0 : v.intValue();
        }
    }

    static void logic() {
        final Gia c = new Gia();
        final Mau cap30 = mau(1, "cap", "", 30, 0, 0, "ne_tranh", 5);
        c.cap = 29;
        Kiem.sai("cap 29 chua du danh hieu cap 30", DanhHieuLogic.du(cap30, c));
        c.cap = 30;
        Kiem.dung("cap 30 du danh hieu cap 30", DanhHieuLogic.du(cap30, c));
        final Mau dex = mau(2, "pokedex_phan_tram", "", 25, 0, 0, "", 0);
        c.dexPt = 24;
        Kiem.sai("Pokedex 24% chua du 25%", DanhHieuLogic.du(dex, c));
        c.dexPt = 25;
        Kiem.dung("Pokedex 25% du", DanhHieuLogic.du(dex, c));
        final Mau boss = mau(3, "boss_the_gioi", "", 1, 0, 0, "", 0);
        Kiem.bang("khoa bo dem boss (moi tham so)", "boss_the_gioi", boss.khoaDem());
        Kiem.bang("boss: kieu su kien", "boss_the_gioi", boss.kieuSuKien());
        final Mau bossX = mau(4, "boss_the_gioi", "77", 3, 0, 0, "", 0);
        Kiem.bang("khoa bo dem boss theo id", "boss_the_gioi:77", bossX.khoaDem());
        c.dem.put("boss_the_gioi", Long.valueOf(1));
        Kiem.dung("lan dau ha boss: du (first kill)", DanhHieuLogic.du(boss, c));
        final Mau tap = mau(5, "tap:den_ban_do", "", 50, 1, 0, "", 0);
        Kiem.bang("tap: kieu su kien", "den_ban_do", tap.kieuSuKien());
        c.tap.put("tap:den_ban_do", Integer.valueOf(49));
        Kiem.sai("49 ban do chua du 50", DanhHieuLogic.du(tap, c));
        c.tap.put("tap:den_ban_do", Integer.valueOf(50));
        Kiem.dung("50 ban do du", DanhHieuLogic.du(tap, c));
        final Mau trao = mau(6, "trao", "", 1, 2, 0, "", 0);
        Kiem.sai("dieu kien trao khong bao gio tu mo", DanhHieuLogic.du(trao, c));
        Kiem.dung("mau trao la song (khong nghe su kien)", trao.kieuSuKien().isEmpty());
        final Mau bac = mau(7, "pvp_bac", "", 2, 0, 43200, "", 0);
        c.bac = 1;
        Kiem.sai("bac Great chua du Ultra", DanhHieuLogic.du(bac, c));
        c.bac = 3;
        Kiem.dung("bac Master du Ultra", DanhHieuLogic.du(bac, c));
        final Mau top = mau(8, "top_pvp", "", 10, 0, 10080, "", 0);
        c.hang = 11;
        Kiem.sai("hang 11 khong vao top 10", DanhHieuLogic.du(top, c));
        c.hang = 10;
        Kiem.dung("hang 10 vao top 10", DanhHieuLogic.du(top, c));
        c.hang = 0;
        Kiem.sai("ngoai bang (hang 0) khong vao top", DanhHieuLogic.du(top, c));
        final Mau gym = mau(9, "gym_huy_hieu", "", 8, 0, 0, "", 0);
        c.gym = 8;
        Kiem.dung("8 huy hieu Gym du", DanhHieuLogic.du(gym, c));
        final Mau nv = mau(10, "nhiem_vu", "Bạn của Clefairy", 1, 0, 0, "", 0);
        Kiem.sai("chua nhan thuong nhiem vu", DanhHieuLogic.du(nv, c));
        c.nv.add("Bạn của Clefairy");
        Kiem.dung("da nhan thuong nhiem vu theo ten", DanhHieuLogic.du(nv, c));
        final Mau sk = mau(11, "su_kien", "halloween", 1, 0, 43200, "", 0);
        c.suKien.add("halloween");
        Kiem.dung("su kien dang bat -> du", DanhHieuLogic.du(sk, c));

        // an / hien
        Kiem.sai("danh hieu an chua co: KHONG hien ten", DanhHieuLogic.hienTen(tap, false, false));
        Kiem.dung("danh hieu an dang co: hien ten", DanhHieuLogic.hienTen(tap, true, false));
        Kiem.dung("danh hieu an da tung mo (het han): van hien ten", DanhHieuLogic.hienTen(tap, false, true));
        Kiem.sai("an = 2 (chi khi co): khong vao bang khi chua co", DanhHieuLogic.coTrongBang(trao, false));
        Kiem.dung("an = 2 dang co: vao bang", DanhHieuLogic.coTrongBang(trao, true));
        c.tap.put("tap:den_ban_do", Integer.valueOf(12));
        final DanhHieuLogic.Dong d = DanhHieuLogic.dong(tap, c, false, false, false, 0L, false, 0L, 10);
        Kiem.bang("dong an: ten ???", "???", d.ten);
        Kiem.bang("dong an: mo ta ???", "???", d.moTa);
        Kiem.bang("dong an: khong lo tien do", Long.valueOf(0), Long.valueOf(d.tienDo));
        Kiem.bang("dong an: van co goi y", "Gợi ý", d.goiY);
        Kiem.dung("dong an: co co an", d.an);
        final DanhHieuLogic.Dong d2 = DanhHieuLogic.dong(tap, c, true, true, true, 0L, true, 0L, 10);
        Kiem.bang("dong an da mo: ten that", "Danh hiệu 5", d2.ten);
        Kiem.bang("dong dang deo", Byte.valueOf(DanhHieuLogic.DANG_DEO), Byte.valueOf(d2.trangThai));
        c.cap = 12;
        final DanhHieuLogic.Dong d3 = DanhHieuLogic.dong(cap30, c, false, false, false, 0L, false, 0L, 10);
        Kiem.bang("tien do cap 12/30", "12/30", d3.tienDo + "/" + d3.can);
        Kiem.bang("chi so hien +0,5% Ne tranh", "+0,5% Né tránh", d3.chiSo);

        // het han
        final long bay = 1_000_000_000L;
        Kiem.sai("vinh vien (0) khong het han", DanhHieuLogic.hetHan(0L, bay));
        Kiem.dung("qua han -> het han", DanhHieuLogic.hetHan(bay - 1, bay));
        Kiem.sai("con han -> chua het", DanhHieuLogic.hetHan(bay + 60000, bay));
        Kiem.bang("phut con lai vinh vien = -1", Integer.valueOf(-1), Integer.valueOf(DanhHieuLogic.phutConLai(0L, bay)));
        Kiem.bang("phut con lai 90 phut", Integer.valueOf(90), Integer.valueOf(DanhHieuLogic.phutConLai(bay + 90 * 60000L, bay)));
        Kiem.dung("con < nua han: gia han", DanhHieuLogic.canGiaHan(bay + 10 * 60000L, 60, bay));
        Kiem.sai("con > nua han: chua gia han", DanhHieuLogic.canGiaHan(bay + 50 * 60000L, 60, bay));
        Kiem.bang("chu con lai 3 ngay 4 gio", "3 ngày 4 giờ", DanhHieuLogic.chuConLai(3 * 1440 + 4 * 60));
        Kiem.bang("chu con lai 12 phut", "12 phút", DanhHieuLogic.chuConLai(12));

        // tran chi so
        Kiem.bang("hieu luc 10 trong tran 10", Integer.valueOf(10), Integer.valueOf(DanhHieuLogic.hieuLuc(10, 10, 50, 0)));
        Kiem.bang("hieu luc 40 bi kep ve tran o 10", Integer.valueOf(10), Integer.valueOf(DanhHieuLogic.hieuLuc(40, 10, 50, 0)));
        Kiem.bang("cau hinh tran 999 bi kep cung 30", Integer.valueOf(30), Integer.valueOf(DanhHieuLogic.hieuLuc(999, 999, 50, 0)));
        Kiem.bang("tran tong trang tri con 4 -> 4", Integer.valueOf(4), Integer.valueOf(DanhHieuLogic.hieuLuc(10, 10, 50, 46)));
        Kiem.bang("tran tong da het -> 0", Integer.valueOf(0), Integer.valueOf(DanhHieuLogic.hieuLuc(10, 10, 50, 60)));
        Kiem.bang("so am -> 0", Integer.valueOf(0), Integer.valueOf(DanhHieuLogic.hieuLuc(-5, 10, 50, 0)));
        Kiem.bang("khac dong -> 0", Integer.valueOf(0), Integer.valueOf(DanhHieuLogic.phanNghin(cap30, "chi_mang", 10, 50, 0)));
        Kiem.bang("khong deo -> 0", Integer.valueOf(0), Integer.valueOf(DanhHieuLogic.phanNghin(null, "ne_tranh", 10, 50, 0)));
        Kiem.bang("phan tram 7 -> 0,7%", "0,7%", DanhHieuLogic.phanTram(7));
        Kiem.bang("phan tram 10 -> 1%", "1%", DanhHieuLogic.phanTram(10));
    }

    // ------------------------------------------------------------------ game (nhan vat gia, khong CSDL)

    static Char nhanVat() {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                GOI.add(m);
            }
        });
        p.getSession().banClient = DanhHieuLogic.BAN_CLIENT;
        p.charname = "kiemdh" + idKe;
        p.charDBID = idKe++;
        p.lvDetail.lv = 20;
        p.isBot = -1;
        p.allDanhHieu = new java.util.Vector<data.DanhHieu>();
        p.currentDanhHieu = null;
        p.idEffDanhHieu = -1;
        return p;
    }

    static void game() {
        final List<Mau> ds = new ArrayList<Mau>();
        ds.add(new Mau(9712, "ca_50", "Cần thủ nghiệp dư", "Câu cá 50 lần.", "", 0, "giot", "khang_trang_thai", 5, "cau_ca", "",
                50, 0, 0, "thanh_tuu", 1));
        ds.add(new Mau(9738, "guc_100", "Kẻ không bỏ cuộc", "Gục 100 lần.", "Ngã nhiều lần...", 0, "tim", "hp", 5, "guc", "",
                100, 1, 0, "an", 2));
        ds.add(new Mau(9739, "map_3", "Kẻ lang thang", "Đến 3 bản đồ.", "Cứ đi...", 1, "loc", "ne_tranh", 40, "tap:den_ban_do", "",
                3, 1, 0, "an", 3));
        ds.add(new Mau(9709, "boss_1", "Kẻ thách thức trùm", "Hạ trùm.", "", 0, "no", "chi_mang", 5, "boss_the_gioi", "",
                1, 0, 0, "boss", 4));
        ds.add(new Mau(9725, "nc_7", "Thợ rèn +7", "+7.", "", 1, "thep", "hp", 7, "thanh_tuu_nang_cap", "", 7, 0, 0, "thanh_tuu", 5));
        ds.add(new Mau(9718, "mua", "Great Ball mùa này", "Bậc mùa.", "", 1, "khien", "ne_tranh", 7, "cau_ca", "x", 1, 0, 60,
                "xep_hang", 6));
        ds.add(new Mau(8767, "cu_8767", "Anh hùng", "Trao.", "", 1, "sao", "chi_mang", 5, "trao", "", 1, 2, 0, "cu", 7));
        DanhHieuGame.datMau(ds, new String[]{"Thường", "Hiếm", "Sử thi", "Huyền thoại"});
        final Char p = nhanVat();
        GOI.clear();

        // bo dem: 49 lan cau chua mo, lan 50 mo
        for (int i = 0; i < 49; i++) {
            DanhHieuGame.suKien(p, "cau_ca", "0", 1);
        }
        Kiem.sai("49 lan cau: chua co danh hieu", co(p, 9712));
        DanhHieuGame.suKien(p, "cau_ca", "129", 1);
        Kiem.dung("lan cau thu 50: mo Can thu nghiep du", co(p, 9712));
        Kiem.bang("danh hieu dau tien tu deo (kho cu Char.addDanhHieu)", Integer.valueOf(9712), Integer.valueOf(p.idEffDanhHieu));
        Kiem.bang("mot danh hieu moi chua xem (cham do)", Integer.valueOf(1), Integer.valueOf(DanhHieuGame.soMoi(p)));
        Kiem.dung("bo dem cho ghi CSDL", DanhHieuGame.soBan(p) > 0);
        Kiem.dung("gui thong bao mo khoa (-104 / hop thoai) + so moi (-110)", GOI.size() >= 2);
        // tham so khong khop: mau "cau_ca" tham so "x" khong tang boi tham so khac
        Kiem.sai("mau tham so 'x' khong mo boi tham so khac", co(p, 9718));
        DanhHieuGame.suKien(p, "cau_ca", "x", 1);
        Kiem.dung("mau tham so 'x' mo khi dung tham so", co(p, 9718));

        // tap: chi dem gia tri KHAC NHAU
        DanhHieuGame.suKien(p, "den_ban_do", "1", 1);
        DanhHieuGame.suKien(p, "den_ban_do", "1", 1);
        DanhHieuGame.suKien(p, "den_ban_do", "2", 1);
        Kiem.sai("2 ban do khac nhau (3 lan vao) chua du 3", co(p, 9739));
        final List<DanhHieuLogic.Dong> b0 = DanhHieuGame.bang(p);
        final DanhHieuLogic.Dong an0 = tim(b0, 9739);
        Kiem.bang("danh hieu an chua mo: ten ??? trong bang", "???", an0 == null ? null : an0.ten);
        Kiem.dung("danh hieu cu (an = 2) chua co: khong trong bang", tim(b0, 8767) == null);
        DanhHieuGame.suKien(p, "den_ban_do", "3", 1);
        Kiem.dung("3 ban do khac nhau: mo Ke lang thang (an)", co(p, 9739));
        final DanhHieuLogic.Dong an1 = tim(DanhHieuGame.bang(p), 9739);
        Kiem.bang("danh hieu an sau khi mo: hien ten that", "Kẻ lang thang", an1 == null ? null : an1.ten);

        // muc (datMuc): max, khong cong don
        DanhHieuGame.datMuc(p, "thanh_tuu_nang_cap", 5);
        DanhHieuGame.datMuc(p, "thanh_tuu_nang_cap", 3);
        Kiem.sai("+5 roi +3: chua du +7 (muc lay max, khong cong)", co(p, 9725));
        DanhHieuGame.datMuc(p, "thanh_tuu_nang_cap", 7);
        Kiem.dung("+7: mo Tho ren +7", co(p, 9725));

        // chi so: chi danh hieu DANG DEO, kep tran o
        com.htbb.pokemon.LauDai.dat(true);
        final boolean chiSoBat = real.ChiSoTrangBiGame.bat();
        DanhHieuGame.deo(p, 9712);
        Kiem.bang("dang deo 9712", Integer.valueOf(9712), Integer.valueOf(p.idEffDanhHieu));
        if (chiSoBat) {
            Kiem.bang("deo Can thu (khang 5): khang = 5", Integer.valueOf(5),
                    Integer.valueOf(DanhHieuGame.phanNghin(p, DanhHieuLogic.KHANG_TRANG_THAI)));
            Kiem.bang("deo Can thu: ne = 0 (danh hieu khac dang co KHONG cong)", Integer.valueOf(0),
                    Integer.valueOf(DanhHieuGame.phanNghin(p, DanhHieuLogic.NE_TRANH)));
            DanhHieuGame.deo(p, 9739);
            Kiem.bang("deo Ke lang thang (40 trong mau) bi kep ve tran o 10", Integer.valueOf(10),
                    Integer.valueOf(DanhHieuGame.phanNghin(p, DanhHieuLogic.NE_TRANH)));
            Kiem.bang("doi danh hieu: dong cu het tac dung", Integer.valueOf(0),
                    Integer.valueOf(DanhHieuGame.phanNghin(p, DanhHieuLogic.KHANG_TRANG_THAI)));
            int tong = 0;
            for (final String ma : DanhHieuLogic.MA_CHI_SO) {
                tong += DanhHieuGame.phanNghin(p, ma);
            }
            Kiem.bang("tong chi so moi dong khi so huu nhieu danh hieu = 1 danh hieu (10)", Integer.valueOf(10), Integer.valueOf(tong));
        } else {
            Kiem.dung("(chi_so.bat tat tren may kiem: bo qua so)", true);
        }
        DanhHieuGame.thao(p);
        Kiem.bang("thao: khong deo", Integer.valueOf(-1), Integer.valueOf(p.idEffDanhHieu));
        Kiem.bang("thao: chi so 0", Integer.valueOf(0), Integer.valueOf(DanhHieuGame.phanNghin(p, DanhHieuLogic.NE_TRANH)));
        Kiem.sai("khong deo duoc danh hieu chua co", DanhHieuGame.deo(p, 9738));
        Kiem.sai("khong deo duoc danh hieu trao chua co", DanhHieuGame.deo(p, 8767));

        // het han: danh hieu co han (60 phut) qua han -> khong con, khong deo, khong chi so
        DanhHieuGame.deo(p, 9718);
        for (final data.DanhHieu d : p.allDanhHieu) {
            if (d.ideff == 9718) {
                Kiem.dung("danh hieu co han co timeExpire", d.timeExpire > System.currentTimeMillis());
                d.timeExpire = System.currentTimeMillis() - 1000L;
            }
        }
        Kiem.sai("qua han: khong con trong bang 'dang co'", co(p, 9718));
        Kiem.dung("qua han: dangDeo = null", DanhHieuGame.dangDeo(p) == null);
        Kiem.bang("qua han: chi so 0", Integer.valueOf(0), Integer.valueOf(DanhHieuGame.phanNghin(p, DanhHieuLogic.NE_TRANH)));
        Kiem.sai("qua han: khong deo lai duoc", DanhHieuGame.deo(p, 9718));
        // gap lai dieu kien: trao lai = moi lai, han moi
        DanhHieuGame.suKien(p, "cau_ca", "x", 1);
        Kiem.dung("dat lai dieu kien sau het han: co lai (han moi)", co(p, 9718));

        // trao tay (danh hieu cu): chi khi co
        DanhHieuGame.trao(p, DanhHieuGame.mau(8767), "kiem");
        Kiem.dung("trao tay danh hieu cu: vao bang", tim(DanhHieuGame.bang(p), 8767) != null);
        Kiem.bang("ten danh hieu cu doc tu mau", "Anh hùng", data.DanhHieu.getName(8767));

        // bot / nhan vat khong DB id: khong lam gi
        final Char bot = nhanVat();
        bot.isBot = 1;
        DanhHieuGame.suKien(bot, "cau_ca", "0", 100);
        Kiem.sai("bot khong nhan danh hieu", co(bot, 9712));
        DanhHieuGame.xoaTrangThai(p);
        DanhHieuGame.datMau(new ArrayList<Mau>(), null);
    }

    static boolean co(final Char p, final int id) {
        final long bay = System.currentTimeMillis();
        for (final data.DanhHieu d : p.allDanhHieu) {
            if (d.ideff == id && !DanhHieuLogic.hetHan(d.timeExpire, bay)) {
                return true;
            }
        }
        return false;
    }

    static DanhHieuLogic.Dong tim(final List<DanhHieuLogic.Dong> ds, final int id) {
        for (final DanhHieuLogic.Dong d : ds) {
            if (d.id == id) {
                return d;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ goi tin

    static void goiTin() {
        try {
            final List<DanhHieuLogic.Dong> ds = new ArrayList<DanhHieuLogic.Dong>();
            final DanhHieuLogic.Dong d = new DanhHieuLogic.Dong();
            d.id = 9700;
            d.ten = "Huấn luyện viên tập sự";
            d.moTa = "Đạt cấp 10.";
            d.goiY = "";
            d.doHiem = 0;
            d.hinh = "sao";
            d.trangThai = DanhHieuLogic.DANG_DEO;
            d.tienDo = 10;
            d.can = 10;
            d.phutConLai = -1;
            d.chiSo = "+0,5% HP tối đa";
            d.nguon = "thanh_tuu";
            d.moi = true;
            ds.add(d);
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            DanhHieuLogic.ghiDanhSach(new DataOutputStream(b), true, 9700, new String[]{"Thường", "Hiếm"}, ds);
            final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b.toByteArray()));
            Kiem.bang("goi: loai danh sach", Integer.valueOf(DanhHieuLogic.G_DANH_SACH), Integer.valueOf(r.readByte()));
            Kiem.bang("goi: mo bang", Integer.valueOf(1), Integer.valueOf(r.readByte()));
            Kiem.bang("goi: dang deo", Integer.valueOf(9700), Integer.valueOf(r.readShort()));
            final int nh = r.readByte();
            Kiem.bang("goi: 2 ten do hiem", Integer.valueOf(2), Integer.valueOf(nh));
            r.readUTF();
            r.readUTF();
            Kiem.bang("goi: 1 dong", Integer.valueOf(1), Integer.valueOf(r.readShort()));
            Kiem.bang("goi: id", Integer.valueOf(9700), Integer.valueOf(r.readShort()));
            Kiem.bang("goi: ten co dau", "Huấn luyện viên tập sự", r.readUTF());
            r.readUTF();
            r.readUTF();
            r.readByte();
            Kiem.bang("goi: hinh", "sao", r.readUTF());
            Kiem.bang("goi: trang thai", Integer.valueOf(DanhHieuLogic.DANG_DEO), Integer.valueOf(r.readByte()));
            r.readByte();
            Kiem.bang("goi: tien do", Integer.valueOf(10), Integer.valueOf(r.readInt()));
            r.readInt();
            Kiem.bang("goi: vinh vien -1", Integer.valueOf(-1), Integer.valueOf(r.readInt()));
            Kiem.bang("goi: chi so", "+0,5% HP tối đa", r.readUTF());
            r.readUTF();
            Kiem.bang("goi: moi", Integer.valueOf(1), Integer.valueOf(r.readByte()));
            Kiem.bang("goi: het byte", Integer.valueOf(0), Integer.valueOf(r.available()));
            final ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            DanhHieuLogic.ghiNhan(new DataOutputStream(b2), 123, 9740, "Người đi khắp thế gian", 3);
            final DataInputStream r2 = new DataInputStream(new ByteArrayInputStream(b2.toByteArray()));
            Kiem.bang("nhan: loai", Integer.valueOf(DanhHieuLogic.G_NHAN), Integer.valueOf(r2.readByte()));
            Kiem.bang("nhan: id nhan vat", Integer.valueOf(123), Integer.valueOf(r2.readShort()));
            Kiem.bang("nhan: id danh hieu", Integer.valueOf(9740), Integer.valueOf(r2.readShort()));
            Kiem.bang("nhan: ten", "Người đi khắp thế gian", r2.readUTF());
            Kiem.bang("nhan: do hiem", Integer.valueOf(3), Integer.valueOf(r2.readByte()));
            Kiem.bang("lenh khong trung -106 (the dat ten pet) / -100..-105", Boolean.TRUE,
                    Boolean.valueOf(DanhHieuLogic.LENH < -106 && DanhHieuLogic.LENH_KHACH < -106));
        } catch (final Exception e) {
            Kiem.dung("goi tin khong loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ du lieu sql/them_danh_hieu.sql

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static final Pattern DONG = Pattern.compile("^\\((\\d+),'([^']*)','((?:[^']|'')*)','((?:[^']|'')*)','((?:[^']|'')*)',(\\d+),'([a-z_]*)','([a-z_]*)',(\\d+),'([a-z_:]*)','((?:[^']|'')*)',(\\d+),(\\d),(\\d+),'([a-z_]*)',(\\d+),1\\)[,\\n]?");

    static void duLieu() {
        final String s = DayDu.phanNangCap("them_danh_hieu.sql");
        Kiem.dung("doc duoc sql/them_danh_hieu.sql", !s.isEmpty());
        if (s.isEmpty()) {
            return;
        }
        Kiem.dung("4 bang CREATE TABLE IF NOT EXISTS", s.split("CREATE TABLE IF NOT EXISTS").length - 1 == 4);
        Kiem.sai("khong DROP TABLE", s.contains("DROP TABLE"));
        Kiem.sai("khong INSERT ... SELECT ... ON DUPLICATE KEY UPDATE", Pattern.compile("INSERT[^;]*SELECT[^;]*ON DUPLICATE", Pattern.DOTALL).matcher(s).find());
        Kiem.sai("ON DUPLICATE KEY khong ghi de chi_so / phan_nghin / bat (chu game chinh)",
                s.contains("`chi_so`=VALUES") || s.contains("`phan_nghin`=VALUES") || s.contains("`bat`=VALUES"));
        int n = 0, moi = 0, an = 0, vuot = 0, saiMa = 0, saiDk = 0, nv = 0, cu = 0, coHan = 0;
        final Set<String> ma = new HashSet<String>();
        final Set<Integer> id = new HashSet<Integer>();
        final Set<String> tenNv = new HashSet<String>();
        final Map<Integer, Integer> hiem = new HashMap<Integer, Integer>();
        boolean trung = false, lechCu = false, anThieuGoiY = false, hoaCoDau = false;
        final Set<String> dkHopLe = new HashSet<String>(java.util.Arrays.asList("cap", "pokedex_phan_tram", "pokedex_loai", "xong_nhiem_vu",
                "pvp_bac", "top_pvp", "su_kien", "sinh_thai", "nhiem_vu", "gym_huy_hieu", "trao", "tap:den_ban_do", "boss_the_gioi",
                "cau_ca", "thang_pvp", "thanh_tuu_nang_cap", "tang_hang", "che_tao", "kham_ngoc", "giup_tan_thu", "dang_nhap",
                "tham_gia_gym", "pokedex_shiny", "guc", "giet", "ha_alpha"));
        for (final String dong : s.split("\n")) {
            final Matcher m = DONG.matcher(dong);
            if (!m.find()) {
                continue;
            }
            n++;
            final int i = Integer.parseInt(m.group(1));
            trung |= !id.add(Integer.valueOf(i)) | !ma.add(m.group(2));
            final int pn = Integer.parseInt(m.group(9));
            vuot += pn > DanhHieuLogic.TRAN_MAC_DINH ? 1 : 0;
            saiMa += DanhHieuLogic.laMaChiSo(m.group(8)) ? 0 : 1;
            saiDk += dkHopLe.contains(m.group(10)) ? 0 : 1;
            final int a = Integer.parseInt(m.group(13));
            an += a == 1 ? 1 : 0;
            anThieuGoiY |= a == 1 && m.group(5).isEmpty() && i < 10000;
            coHan += Integer.parseInt(m.group(14)) > 0 ? 1 : 0;
            if (i >= 9700 && i < 10000) {
                moi++;
                hiem.merge(Integer.valueOf(Integer.parseInt(m.group(6))), 1, Integer::sum);
            }
            if ("nhiem_vu".equals(m.group(10))) {
                nv++;
                tenNv.add(m.group(11).replace("''", "'"));
            }
            for (final String chu : new String[]{m.group(3), m.group(4), m.group(5)}) {
                for (int k = 0; k < chu.length(); k++) {
                    final char ch = chu.charAt(k);
                    // font client J2ME khong ve chu HOA co dau U+1EA0..U+1EF8 (KiemChuHoaCoDau)
                    hoaCoDau |= ch >= '\u1EA0' && ch <= '\u1EF8' && Character.isUpperCase(ch);
                }
            }
            if ("cu".equals(m.group(15))) {
                cu++;
                lechCu |= !m.group(3).replace("''", "'").equals(data.DanhHieu.getName(i));
            }
        }
        Kiem.dung("doc duoc > 300 dong danh hieu (" + n + ")", n > 300);
        Kiem.dung("30..50 danh hieu thanh tuu moi (" + moi + ")", moi >= 30 && moi <= 50);
        Kiem.dung("du 4 do hiem o danh hieu moi " + hiem, hiem.size() == 4);
        Kiem.dung("co danh hieu an (" + an + ")", an >= 5);
        Kiem.dung("co danh hieu co han (xep hang / su kien) (" + coHan + ")", coHan >= 5);
        Kiem.sai("id / ma khong trung", trung);
        Kiem.bang("moi phan_nghin <= 10 (ngan sach 1%)", Integer.valueOf(0), Integer.valueOf(vuot));
        Kiem.bang("moi chi_so la ma hop le", Integer.valueOf(0), Integer.valueOf(saiMa));
        Kiem.bang("moi dieu_kien la kieu da biet", Integer.valueOf(0), Integer.valueOf(saiDk));
        Kiem.sai("danh hieu an (thanh tuu) nao cung co goi y", anThieuGoiY);
        Kiem.sai("ten / mo ta / goi y khong co chu HOA co dau (font J2ME)", hoaCoDau);
        Kiem.sai("ten danh hieu cu khop data.DanhHieu.getName (giftcode)", lechCu);
        Kiem.dung("danh muc gom danh hieu cu (" + cu + ")", cu >= 40);
        for (final String maMua : com.htbb.pokemon.XepHangPvp.MA_DANH_HIEU_MUA) {
            Kiem.dung("danh hieu thuong mua co trong SQL: " + maMua, maMua.isEmpty() || s.contains("'" + maMua + "'"));
        }
        Kiem.bang("goi -110 chi cho client >= 907 (ban 906 da phat hanh khong co bo xu ly)", Integer.valueOf(907),
                Integer.valueOf(DanhHieuLogic.BAN_CLIENT));
        // moi "danh_hieu:<ten>" thuong nhiem vu co mot dong nhiem_vu cung ten
        // bo dong data_trang_phuc (do-trang-phuc): nguon "danh_hieu:<ma>" la DIEU KIEN mo trang tri theo ma danh hieu
        // (KiemDoTrangPhuc kiem ma co trong data_danh_hieu), khong phai thuong nhiem vu
        final String nvSql = DayDu.sql().replaceAll("(?m)^INSERT INTO `data_trang_phuc` .*$", "");
        final Matcher t = Pattern.compile("danh_hieu:([^;|']+)").matcher(nvSql);
        final Set<String> can = new HashSet<String>();
        while (t.find()) {
            can.add(t.group(1).trim());
        }
        can.removeAll(tenNv);
        Kiem.dung("moi danh hieu thuong nhiem vu (" + nv + ") co trong danh muc, thieu: " + can, !nvSql.isEmpty() && can.isEmpty());
        Kiem.dung("ten danh hieu nhiem vu <= 64 (cot tham_so)", tenNv.stream().allMatch(x -> x.length() <= 64));
    }

    // ------------------------------------------------------------------ diem moc trong ma

    static void moc() {
        final String nv = doc("src/real/NhiemVuEngine.java");
        Kiem.dung("NhiemVuEngine.suKien goi DanhHieuGame.suKien", nv.contains("DanhHieuGame.suKien(p, kieu, thamSo, n);"));
        Kiem.dung("NhiemVuEngine.datMuc goi DanhHieuGame.datMuc", nv.contains("DanhHieuGame.datMuc(p, kieu, giaTri);"));
        Kiem.dung("thuong nhiem vu danh_hieu: trao danh hieu deo duoc", nv.contains("DanhHieuGame.khiNhanTheoTen(p, v)"));
        Kiem.dung("cau ca ban diem moc cau_ca", doc("src/com/htbb/pokemon/HeSinhThaiMenu.java").contains("NhiemVuEngine.suKien(p, \"cau_ca\""));
        Kiem.dung("guc ban diem moc guc", doc("src/real/HoiSinhGame.java").contains("NhiemVuEngine.suKien(p, \"guc\""));
        Kiem.dung("doi ban do ban den_ban_do", doc("src/real/MapXuLyLenh.java").contains("\"den_ban_do\""));
        Kiem.dung("lenh -111 vao DanhHieuGame.xuLy", doc("src/real/MapXuLyLenh.java").contains("case -111:"));
        Kiem.dung("ne tranh cong danh hieu dang deo", doc("src/real/Char.java").contains("DanhHieuLogic.NE_TRANH"));
        Kiem.dung("chi mang cong danh hieu dang deo", doc("src/real/ChiSoTrangBiGame.java").contains("DanhHieuLogic.CHI_MANG"));
        Kiem.dung("khang trang thai cong danh hieu dang deo", doc("src/real/ChiSoTrangBiGame.java").contains("DanhHieuLogic.KHANG_TRANG_THAI"));
        Kiem.dung("HP cong TRUOC kepPhanTram", doc("src/real/CharChiSo.java").contains("DanhHieuLogic.HP"));
        Kiem.dung("nhan danh hieu noi chuoi -90..-102 cho nguoi gan", doc("src/real/TienHoaPokemon.java").contains("DanhHieuGame.noiDuoi"));
        Kiem.dung("deo danh hieu sinh thai qua DanhHieuGame (dat currentDanhHieu)",
                doc("src/com/htbb/pokemon/HeSinhThaiMenu.java").contains("real.DanhHieuGame.deo(p, HeSinhThai.ID_EFF_DANH_HIEU_DAU + i)"));
    }
}
