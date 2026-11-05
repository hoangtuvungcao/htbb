import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.HoTongNhiemVu;
import com.htbb.pokemon.KhoaBanDo;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.NhiemVuMau;
import com.htbb.pokemon.ThongSoLauDai;
import com.htbb.pokemon.XeHangDiTheo;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.HoTongNhiemVuGame;
import real.LauDaiNhanVat;
import real.MucTieuNhiemVuGame;
import real.NewCharQuest;
import real.NhiemVuEngine;
import real.NhiemVuLauDaiGame;

/**
 * BAO LOI ZALO 02-04/11/2026 (kazuhas4 / ruby / rongngoo - Đào Ngọc Thịnh):
 * <ol>
 *   <li>"bấm nhầm xoá" nhiem vu (cua ai / buoc cot truyen) roi khong thay / nhan lai duoc: buoc chinh khong huy duoc khi bat
 *       lau dai (Char.duocHuyChinh), cua ai khong huy (MucTieuNhiemVuGame.khiGoiCuaAi), nhan lai dung buoc da tru ve khong
 *       tru them / khong bi chan (NhiemVuLauDaiGame.kiemNhanChinh + HoSoLauDai.veDaTra);</li>
 *   <li>giao hang o Kecleon: xe van tieu THUONG cua nguoi chua mo Dịch trạm giao duoc o Kecleon
 *       (HoTongNhiemVu.giaoDuocTaiKecleon); Dịch trạm da mo -&gt; bao ro, mo cua hang;</li>
 *   <li>Ponyta "quần": xe khong di lai vong cu / doan quay lai (XeHangDiTheo.tat), vet an toan nhieu luong;</li>
 *   <li>"hết vé chương, sang tuần mới": ve chuong lam day moi ngay 05:00 (NhiemVuLauDai.capNhatVeNgay).</li>
 * </ol>
 */
public final class KiemBaoLoi0411 {

    private KiemBaoLoi0411() {
    }

    static final int ID = 987_604_11;

    public static void chay() {
        Kiem.nhom("Bao loi 02-04/11/2026: xoa nham nhiem vu, giao hang Kecleon, Ponyta quan, het ve chuong");
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (gemCu == null) {
            real.Map.gemTemplate = new real.GemTemplate[0]; // Char(null) can (nhu KiemCap140)
        }
        try {
            veChuong();
            nhanLaiKhongTruVe();
            khongHuyChinh();
            cuaAiKhongHuy();
            giaoKecleon();
            xeKhongQuan();
            vetNhieuLuong();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemBaoLoi0411 khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
        }
    }

    // ------------------------------------------------------------------ 4. ve chuong

    static void veChuong() {
        final int toiDa = ThongSoLauDai.VE_CHUONG_TOI_DA;
        Kiem.dung("du lieu: ve_chuong.toi_da > 0 (" + toiDa + ")", toiDa > 0);
        int[] v = NhiemVuLauDai.capNhatVeNgay(0, 20000, 20001);
        Kiem.bang("het ve hom qua -> sang ngay moi lam day", toiDa, v[0]);
        Kiem.bang("ngay moi ghi lai", 20001, v[1]);
        v = NhiemVuLauDai.capNhatVeNgay(0, 20001, 20001);
        Kiem.bang("cung ngay: van het ve (khong lam day lien tuc)", 0, v[0]);
        v = NhiemVuLauDai.capNhatVeNgay(0, 0, 20001);
        Kiem.bang("ho so cu (chua co 'vn', vd kazuhas4 vc=0): lam day ngay lan dau", toiDa, v[0]);
        v = NhiemVuLauDai.capNhatVeNgay(toiDa + 3, 20000, 20005);
        Kiem.bang("ve GM tang vuot tran: khong bi cat", toiDa + 3, v[0]);
        final long ms = 1793750400000L; // 2026-11-04 ~ 16:00 +07
        Kiem.bang("ngayVe: 05:00 la moc doi ngay", NhiemVuLauDai.ngayVe(ms - 3600000L * 2) + 0, NhiemVuLauDai.ngayVe(ms));
        Kiem.dung("ngayVe: sau 24 gio la ngay ke", NhiemVuLauDai.ngayVe(ms + 86400000L) == NhiemVuLauDai.ngayVe(ms) + 1);
        final String chu = NhiemVuLauDai.kiemChinh(14, 50, 0, 0, true, 0);
        Kiem.dung("het ve: bao 'sáng mai', khong con 'tuần' / 'thứ Hai': " + chu, chu != null && chu.contains("05:00 sáng mai")
                && !chu.contains("tuần") && !chu.contains("thứ Hai"));
        final HoSoLauDai h = HoSoLauDai.giaiMa("vc=1;vt=2965;vn=20761;vq=48");
        Kiem.bang("ho so: doc 'vn'", 20761, h.veNgayCuoi);
        Kiem.bang("ho so: doc 'vq'", 48, h.veDaTra);
        final String ma = h.maHoa();
        Kiem.dung("ho so: ghi lai 'vn' / 'vq' / giu 'vt' (ban cu quay lui): " + ma, ma.contains("vn=20761") && ma.contains("vq=48")
                && ma.contains("vt=2965"));
    }

    static data.InfoQuestTemplate chuong(final int id, final int lv, final boolean canVe) {
        final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
        t.id = (short) id;
        t.type = 3;
        t.lv = (short) lv;
        t.name = "Ông ngủ giữa đường";
        t.main_sub = 0;
        t.canVe = canVe;
        return t;
    }

    /** rongngoo: C07.1 (buoc 1 chuong, can ve) - nhan, bi dua ve "chua nhan" (huy / goi cu), nhan lai: khong tru them. */
    static void nhanLaiKhongTruVe() {
        final boolean batCu = LauDai.bat();
        LauDai.dat(true);
        final HoSoLauDai h = new HoSoLauDai();
        try {
            final Char p = new Char(null);
            p.charDBID = ID;
            p.isBot = -1;
            p.lvDetail.lv = 50;
            p.mainQuest = new NewCharQuest(true);
            p.mainQuest.id_quest = 47;
            p.mainQuest.main_sub = 0;
            p.mainQuest.state_quest = NewCharQuest.UN_RECEIVE;
            h.veChuong = 1;
            h.veNgayCuoi = NhiemVuLauDai.ngayVe(com.htbb.pokemon.DongHo.bay());
            LauDaiNhanVat.datKiem(ID, h);
            final data.InfoQuestTemplate c71 = chuong(47, 14, true);
            Kiem.bang("C07.1 con 1 ve: nhan duoc", null, NhiemVuLauDaiGame.kiemNhanChinh(p, c71));
            Kiem.bang("tru 1 ve", 0, h.veChuong);
            Kiem.bang("ghi buoc da tra ve (id + 1)", 48, h.veDaTra);
            p.mainQuest.state_quest = NewCharQuest.UN_RECEIVE; // huy (ban cu) / nap lai: buoc ve "chua nhan"
            Kiem.bang("nhan LAI C07.1 khi het ve: duoc (da tra ve buoc nay)", null, NhiemVuLauDaiGame.kiemNhanChinh(p, c71));
            Kiem.bang("nhan lai: khong tru them ve", 0, h.veChuong);
            p.mainQuest.id_quest = 48;
            p.mainQuest.state_quest = NewCharQuest.UN_RECEIVE;
            Kiem.bang("buoc 2 cung chuong (khong can ve): nhan duoc", null, NhiemVuLauDaiGame.kiemNhanChinh(p, chuong(48, 14, false)));
            p.mainQuest.id_quest = 53;
            p.mainQuest.state_quest = NewCharQuest.UN_RECEIVE;
            Kiem.dung("chuong moi (C08.1) het ve trong ngay: chan", NhiemVuLauDaiGame.kiemNhanChinh(p, chuong(53, 16, true)) != null);
            h.veNgayCuoi -= 1; // sang ngay moi (05:00)
            Kiem.bang("sang ngay moi: C08.1 nhan duoc (khong doi tuan)", null, NhiemVuLauDaiGame.kiemNhanChinh(p, chuong(53, 16, true)));
            Kiem.bang("ngay moi: lam day roi tru 1", ThongSoLauDai.VE_CHUONG_TOI_DA - 1, h.veChuong);
        } finally {
            LauDaiNhanVat.xoaKiem(ID);
            LauDai.dat(batCu);
        }
    }

    // ------------------------------------------------------------------ 1. xoa nham

    static void khongHuyChinh() {
        final boolean batCu = LauDai.bat();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (gemCu == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
        t.id = 37;
        t.type = 0;
        t.name = "Machamp và ô kính trống";
        t.main_sub = 0;
        t.idItemgift = new short[]{32000};
        t.potiongift = new short[]{32000};
        t.npotiongift = new short[]{32000};
        final data.InfoQuestTemplate cu = real.Map.allMainQuest.get(Short.valueOf((short) 37));
        try {
            real.Map.allMainQuest.put(Short.valueOf((short) 37), t);
            LauDai.dat(true);
            Kiem.sai("bat lau dai: buoc cot truyen khong huy duoc", Char.duocHuyChinh());
            final Char p = new Char(null);
            final NewCharQuest q = new NewCharQuest(true);
            q.id_quest = 37;
            q.main_sub = 0;
            q.state_quest = NewCharQuest.WORKING;
            q.monsterKilled = new short[]{9};
            q.nItem = new short[]{0};
            p.mainQuest = q;
            try {
                p.OnQuest((byte) 2, (short) 37, (byte) 0); // client 0.9.30 nut "Huỷ nhiệm vụ" (bam nham)
            } catch (final RuntimeException e) {
                // gui goi / CSDL khong co trong test
            }
            Kiem.bang("bam Huy buoc chinh: van dang lam (khong ve 'chua nhan' o NPC xa)", Integer.valueOf(NewCharQuest.WORKING),
                    Integer.valueOf(p.mainQuest.state_quest));
            Kiem.bang("bam Huy buoc chinh: tien do giu nguyen", Integer.valueOf(9), Integer.valueOf(p.mainQuest.monsterKilled[0]));
            Kiem.dung("cau bao khong huy", Char.KHONG_HUY_CHINH.contains("không huỷ được"));
            LauDai.dat(false);
            Kiem.dung("tat lau dai: luat cu (huy duoc)", Char.duocHuyChinh());
        } finally {
            if (cu == null) {
                real.Map.allMainQuest.remove(Short.valueOf((short) 37));
            } else {
                real.Map.allMainQuest.put(Short.valueOf((short) 37), cu);
            }
            real.Map.gemTemplate = gemCu;
            LauDai.dat(batCu);
        }
    }

    static void cuaAiKhongHuy() {
        final boolean batCu = LauDai.bat();
        LauDai.dat(true);
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = "CA-05.2";
        m.loai = "cua_ai";
        m.ten = "Cửa ải: 30 Gastly ở Hang động";
        m.kieu = "giet";
        m.mucTieuId = "92";
        m.soLuong = 30;
        m.cap = 24;
        final ConcurrentHashMap<String, long[]> td = new ConcurrentHashMap<String, long[]>();
        final long[] v = new long[]{12L, 0L, 0L, 0L};
        td.put(m.ma, v);
        try {
            NhiemVuEngine.datKiem(Collections.singletonList(m), ID, td);
            final Char p = new Char(null);
            p.charDBID = ID;
            p.isBot = -1;
            p.lvDetail.lv = 30;
            final int id = MucTieuNhiemVuGame.idCuaAi(m.ma);
            Kiem.dung("id ao cua ai CA-05.2", MucTieuNhiemVuGame.laIdCuaAi(id));
            MucTieuNhiemVuGame.khiGoiCuaAi(p, 2, id); // Huy (client cu / bam nham)
            Kiem.dung("bam Huy cua ai: dong tien do con nguyen", td.get(m.ma) == v && v[0] == 12L && v[1] == 0L);
        } finally {
            NhiemVuEngine.xoaKiem(ID);
            LauDai.dat(batCu);
        }
    }

    // ------------------------------------------------------------------ 2. giao hang Kecleon

    static void giaoKecleon() {
        Kiem.dung("xe nhiem vu: Kecleon nhan (Dich tram mo hay khong)", HoTongNhiemVu.giaoDuocTaiKecleon(true, true)
                && HoTongNhiemVu.giaoDuocTaiKecleon(true, false));
        Kiem.dung("rongngoo cap 50, xe THUONG, Dich tram (Đường Victory, cap 69 + CA-11.3) con khoa: Kecleon nhan",
                HoTongNhiemVu.giaoDuocTaiKecleon(false, false));
        Kiem.sai("xe thuong, Dich tram da mo: van giao o Dich tram", HoTongNhiemVu.giaoDuocTaiKecleon(false, true));
        Kiem.dung("bao xe thuong -> Dich tram", HoTongNhiemVu.BAO_XE_THUONG_DICH_TRAM.contains("Dịch trạm"));
        Kiem.dung("bao trong nha -> ra ngoai", HoTongNhiemVu.BAO_XE_NGOAI_NHA.contains("không vào nhà"));

        final java.util.Map<Integer, KhoaBanDo.Luat> cuLuat = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        final java.util.Map<Integer, KhoaBanDo.Luat> luat = new HashMap<Integer, KhoaBanDo.Luat>();
        luat.put(118, new KhoaBanDo.Luat(118, 69, "CA-11.3", "Cửa ải: Đường Victory", true, ""));
        KhoaBanDo.datBang(luat);
        final boolean batCu = LauDai.bat();
        LauDai.dat(true);
        final ConcurrentHashMap<String, long[]> td = new ConcurrentHashMap<String, long[]>();
        try {
            NhiemVuEngine.datKiem(Collections.<NhiemVuMau>emptyList(), ID, td);
            final Char p = new Char(null);
            p.charDBID = ID;
            p.isBot = -1;
            p.charname = "kiemkecleon";
            final real.Map lang = new real.Map();
            lang.mapIDLoadMap = 0;
            p.map = lang;
            p.mapID = 0;
            p.lvDetail.lv = 50;
            Kiem.sai("cap 50: Dich tram khoa", HoTongNhiemVuGameMo.mo(p));
            p.lvDetail.lv = 75;
            real.KhoaBanDoGame.datMien(ID, 0);
            Kiem.sai("cap 75 chua xong CA-11.3: Dich tram van khoa", HoTongNhiemVuGameMo.mo(p));
            td.put("CA-11.3", new long[]{1L, 2L, 0L, 0L});
            Kiem.dung("cap 75 + CA-11.3: Dich tram mo", HoTongNhiemVuGameMo.mo(p));
            Kiem.sai("khong co xe: Kecleon mo cua hang nhu thuong", HoTongNhiemVuGame.traTaiKecleon(p));
        } finally {
            NhiemVuEngine.xoaKiem(ID);
            KhoaBanDo.datBang(cuLuat);
            LauDai.dat(batCu);
        }
    }

    /** Goi ham goi-noi-bo dichTramMo qua phan chieu (package real). */
    static final class HoTongNhiemVuGameMo {
        static boolean mo(final Char p) {
            try {
                final java.lang.reflect.Method m = HoTongNhiemVuGame.class.getDeclaredMethod("dichTramMo", Char.class);
                m.setAccessible(true);
                return ((Boolean) m.invoke(null, p)).booleanValue();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ------------------------------------------------------------------ 3. Ponyta quan

    static final XeHangDiTheo.DiDuoc TRONG = new XeHangDiTheo.DiDuoc() {
        @Override
        public boolean duoc(final int x, final int y) {
            return x >= 0 && y >= 0 && x < 2000 && y < 2000;
        }
    };

    static void xeKhongQuan() {
        // xe o (400,400); nguoi choi chay sang phai toi 560 (ngoai tam 80 px - xe dung), roi quay lai dung ben TRAI xe (340)
        final XeHangDiTheo.Vet v = new XeHangDiTheo.Vet();
        for (int x = 400; x <= 560; x += 8) {
            XeHangDiTheo.ghi(v, x, 400);
        }
        for (int x = 560; x >= 340; x -= 8) {
            XeHangDiTheo.ghi(v, x, 400);
        }
        final int[] r = XeHangDiTheo.buoc(v, 400, 400, 340, 400, 30, TRONG);
        Kiem.dung("nguoi choi quay lai ben trai xe: xe di ve phia nguoi choi (x < 400), khong chay lai doan cu sang phai: x=" + r[0],
                r[0] < 400);
        Kiem.dung("cat vet: chi con doan moi", v.soDiem() <= 3);
        // di vong quanh xe roi dung canh: xe khong di het vong
        final XeHangDiTheo.Vet w = new XeHangDiTheo.Vet();
        final int[][] vong = {{400, 400}, {400, 300}, {500, 300}, {500, 500}, {300, 500}, {300, 420}};
        for (int k = 0; k + 1 < vong.length; k++) {
            final int[] a = vong[k], b = vong[k + 1];
            for (int s = 0; s <= 100; s += 8) {
                final int x = a[0] + Integer.signum(b[0] - a[0]) * Math.min(s, Math.abs(b[0] - a[0]));
                final int y = a[1] + Integer.signum(b[1] - a[1]) * Math.min(s, Math.abs(b[1] - a[1]));
                XeHangDiTheo.ghi(w, x, y);
            }
        }
        int[] c = {400, 400};
        for (int k = 0; k < 6; k++) {
            c = XeHangDiTheo.buoc(w, c[0], c[1], 300, 420, 30, TRONG);
        }
        Kiem.dung("di vong roi dung cach xe ~100 px: xe toi canh nguoi choi (<= 16 px) trong 6 nhip: " + c[0] + "," + c[1],
                Math.abs(c[0] - 300) <= 16 && Math.abs(c[1] - 420) <= 16);
        // re qua goc tuong: diem moi nhat khong toi thang duoc -> van di theo vet (khong cat sai)
        final XeHangDiTheo.DiDuoc tuong = new XeHangDiTheo.DiDuoc() {
            @Override
            public boolean duoc(final int x, final int y) {
                return TRONG.duoc(x, y) && !(x >= 420 && x < 500 && y >= 300 && y < 400);
            }
        };
        final XeHangDiTheo.Vet g = new XeHangDiTheo.Vet();
        for (int y = 400; y <= 420; y += 8) {
            XeHangDiTheo.ghi(g, 400, y);
        }
        for (int x = 400; x <= 520; x += 8) {
            XeHangDiTheo.ghi(g, x, 420);
        }
        for (int y = 420; y >= 340; y -= 8) {
            XeHangDiTheo.ghi(g, 520, y);
        }
        int[] d = {400, 400};
        for (int k = 0; k < 12; k++) {
            d = XeHangDiTheo.buoc(g, d[0], d[1], 520, 340, 30, tuong);
            Kiem.dung("re goc: xe khong vao o chan (" + d[0] + "," + d[1] + ")", tuong.duoc(d[0], d[1]));
        }
        Kiem.dung("re goc: xe toi canh nguoi choi: " + d[0] + "," + d[1], Math.abs(d[0] - 520) <= 16 && Math.abs(d[1] - 340) <= 16);
    }

    static void vetNhieuLuong() throws Exception {
        final XeHangDiTheo.Vet v = new XeHangDiTheo.Vet();
        final Throwable[] loi = new Throwable[1];
        final Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 20000; i++) {
                        XeHangDiTheo.ghi(v, 400 + (i % 200) * 8, 400 + (i / 200 % 2) * 8);
                    }
                } catch (final Throwable e) {
                    loi[0] = e;
                }
            }
        });
        final Thread b = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int[] c = {400, 400};
                    for (int i = 0; i < 20000; i++) {
                        c = XeHangDiTheo.buoc(v, c[0], c[1], 1000, 400, 30, TRONG);
                    }
                } catch (final Throwable e) {
                    loi[0] = e;
                }
            }
        });
        a.start();
        b.start();
        a.join();
        b.join();
        Kiem.bang("hai luong cung ghi / di vet (luong van tieu + vong ban do): khong loi", null, loi[0] == null ? null : String.valueOf(loi[0]));
        Kiem.dung("vet khong vuot tran", v.soDiem() <= XeHangDiTheo.TOI_DA);
    }
}
