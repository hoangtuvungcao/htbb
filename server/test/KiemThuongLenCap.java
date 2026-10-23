import com.htbb.pokemon.ThuongLenCap;
import io.Message;
import io.Session;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import real.Char;
import real.LevelDetail;

/**
 * Bao loi nguoi choi 23/10/2026 (client 0.9.13 / 0.9.14): "len lv khong co diem tiem nang - lam nhiem vu len cap / nhan
 * goi qua len cap la khong nhan duoc". Kiem: moi duong cong EXP (danh quai, nhiem vu, goi qua / su kien, huong dan tan
 * thu bo qua, nhay nhieu cap, GM) di qua MOT khoi len cap, thuong du tung cap, dung mot lan (goi lai / dang nhap lai
 * khong cong lan hai), loi o lenh phu khong lam mat diem; bu diem luc nap (cong thuc, chi cong, khong am, lap lai = 0).
 */
public final class KiemThuongLenCap {

    private KiemThuongLenCap() {
    }

    static final List<Message> GOI = new ArrayList<Message>();
    /** Lenh lam session nem loi (mo phong mot lenh phu hong giua khoi len cap), Integer.MIN_VALUE = khong. */
    static int lenhHong = Integer.MIN_VALUE;

    public static void chay() {
        Kiem.nhom("Thuong len cap: moi duong cong EXP + bu diem luc nap");
        final boolean coCu = com.htbb.pokemon.LauDai.bat();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        try {
            if (real.Map.gemTemplate == null) {
                real.Map.gemTemplate = new real.GemTemplate[0];
            }
            com.htbb.pokemon.LauDai.dat(false);
            cacDuong();
            com.htbb.pokemon.LauDai.dat(true);
            cacDuongLauDai();
            huanLuyen();
            boQuaHuongDan();
            buKhiNap();
            congThuc();
            maNguon();
        } catch (final Throwable t) {
            t.printStackTrace();
            Kiem.dung("khong nem loi: " + t, false);
        } finally {
            lenhHong = Integer.MIN_VALUE;
            real.Map.gemTemplate = gemCu;
            com.htbb.pokemon.LauDai.dat(coCu);
        }
    }

    static Char nhanVat(final int cap) {
        final Char p = new Char(null);
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                if (m.command == lenhHong) {
                    throw new IllegalStateException("gia lap loi lenh " + lenhHong);
                }
                GOI.add(m);
            }
        });
        p.getSession().banClient = 914;
        p.charClass = 1;
        p.charDBID = 0; // khong CSDL: bo qua ho so lau dai / khoa ban do
        p.charname = "kiem_len_cap";
        final byte[] g = data.Database.basicPoint[1];
        p.strength = (short) (g[0] + cap - 1);
        p.agitity = (short) (g[1] + cap - 1);
        p.spirit = (short) (g[2] + cap - 1);
        p.health = (short) (g[3] + cap - 1);
        p.luck = (short) (g[4] + cap - 1);
        p.basepoint = (short) (5 * (cap - 1));
        p.skillpoint = (short) (cap - 1);
        p.lvDetail.setExpNew(LevelDetail.expMain[cap - 1]);
        p.lastLV = (short) cap;
        return p;
    }

    /** EXP can de tu dau cap hien tai len dung dau cap {@code den}. */
    static long expToi(final Char p, final int den) {
        return LevelDetail.expMain[den - 1] - p.lvDetail.getExp();
    }

    static boolean coChu(final String s) {
        final byte[] k = s.getBytes(StandardCharsets.UTF_8);
        for (final Message m : GOI) {
            final byte[] b = m.toByteArray();
            outer:
            for (int i = 0; i + k.length <= b.length; i++) {
                for (int j = 0; j < k.length; j++) {
                    if (b[i + j] != k[j]) {
                        continue outer;
                    }
                }
                return true;
            }
        }
        return false;
    }

    static int dem(final int lenh) {
        int n = 0;
        for (final Message m : GOI) {
            if (m.command == lenh) {
                n++;
            }
        }
        return n;
    }

    interface Duong {
        void cong(Char p, long exp) throws Exception;
    }

    static void mot(final String ten, final Duong d) throws Exception {
        final Char p = nhanVat(1);
        GOI.clear();
        d.cong(p, expToi(p, 2) + 5L);
        Kiem.bang(ten + ": len cap 2", 2, p.lvDetail.lv);
        Kiem.bang(ten + ": +5 diem tiem nang", 5, (int) p.basepoint);
        Kiem.bang(ten + ": +1 diem ky nang", 1, (int) p.skillpoint);
        Kiem.bang(ten + ": +1 suc manh", data.Database.basicPoint[1][0] + 1, (int) p.strength);
        Kiem.bang(ten + ": lastLV = 2 (da thuong)", 2, (int) p.lastLV);
        Kiem.dung(ten + ": goi 32 (bp/sp) toi client", dem(32) >= 1);
        Kiem.dung(ten + ": the \"Lên cấp 2! +5 điểm tiềm năng\"", coChu("Lên cấp 2! +5 điểm tiềm năng"));
    }

    static void cacDuong() throws Exception {
        mot("danh quai (addXPForChar)", (p, e) -> real.Map.addXPForChar(p, e, false, "map doAttackMonster2"));
        mot("nhiem vu (NhiemVuEngine.traThuong -> addXPForChar)", (p, e) -> real.Map.addXPForChar(p, e, false, "nhiem vu A-001"));
        mot("nhiem vu chinh (InfoQuestTemplate.addGift -> addXpCharEvent)", (p, e) -> {
            final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
            t.exp = (int) e;
            t.idItemgift = new short[]{32000, 32000, 32000, 32000, 32000, 0};
            t.addGift(p);
        });
        mot("goi qua / su kien (addXpCharEvent)", (p, e) -> real.Map.addXpCharEvent(p, e, false, "doTuiQuaNoel"));
        mot("web / AdminPanel (adminpanel)", (p, e) -> real.Map.addXPForChar(p, e, true, "adminpanel:kiem"));

        // nhay nhieu cap mot lan: du diem TUNG cap
        Char p = nhanVat(1);
        GOI.clear();
        real.Map.addXpCharEvent(p, expToi(p, 6) + 5L, false, "doHopQuaHaloween2016");
        Kiem.bang("nhay 1 -> 6 mot lan: cap 6", 6, p.lvDetail.lv);
        Kiem.bang("nhay 1 -> 6: +25 diem tiem nang (5 cap x 5)", 25, (int) p.basepoint);
        Kiem.bang("nhay 1 -> 6: +5 diem ky nang", 5, (int) p.skillpoint);
        Kiem.bang("nhay 1 -> 6: +5 moi chi so", data.Database.basicPoint[1][4] + 5, (int) p.luck);
        Kiem.dung("nhay 1 -> 6: the \"Lên cấp 6! +25 điểm tiềm năng, +5 điểm kỹ năng\"",
                coChu("Lên cấp 6! +25 điểm tiềm năng, +5 điểm kỹ năng"));

        // dung mot lan: goi lai thuong / cong EXP khong len cap -> khong cong them
        final int bp = p.basepoint;
        final int sp = p.skillpoint;
        GOI.clear();
        Kiem.bang("goi lai thuongLenCap: 0 cap moi", 0, real.Map.thuongLenCap(p, "kiem goi lai"));
        real.Map.addXPForChar(p, 10L, false, "map doAttackMonster2");
        Kiem.bang("goi lai / cong them EXP khong len cap: tiem nang giu nguyen", bp, (int) p.basepoint);
        Kiem.bang("goi lai / cong them EXP khong len cap: ky nang giu nguyen", sp, (int) p.skillpoint);
        Kiem.sai("goi lai: khong co the len cap moi", coChu("Lên cấp"));

        // len lai cap cu sau khi bi ha cap (lastLV giu moc cao nhat): khong cong lan hai
        p = nhanVat(5);
        p.lvDetail.setExpNew(LevelDetail.expMain[2]); // ha ve cap 3 (vd lenh quan tri giam cap - giu lastLV)
        final int bp5 = p.basepoint;
        real.Map.addXPForChar(p, expToi(p, 5) + 5L, false, "map doAttackMonster2");
        Kiem.bang("len lai cap 5 sau khi ha cap: khong cong lan hai", bp5, (int) p.basepoint);
        real.Map.addXPForChar(p, expToi(p, 6) + 5L, false, "map doAttackMonster2");
        Kiem.bang("vuot moc cu (5 -> 6): chi cong cap moi", bp5 + 5, (int) p.basepoint);

        // lenh phu hong giua khoi len cap (truoc day: addXpCharEvent nuot loi -> mat diem)
        p = nhanVat(1);
        GOI.clear();
        lenhHong = 33;
        real.Map.addXpCharEvent(p, expToi(p, 3) + 5L, false, "InfoQuestTemplate");
        lenhHong = Integer.MIN_VALUE;
        Kiem.bang("goi 33 hong (lenh phu): van +10 diem tiem nang", 10, (int) p.basepoint);
        Kiem.bang("goi 33 hong: lastLV = 3", 3, (int) p.lastLV);
        Kiem.dung("goi 33 hong: van co the len cap", coChu("Lên cấp 3!"));

        // no cu (cap da len ma chua thuong, vd ban cu hong giua chung): lan cong EXP ke tiep tu bu
        p = nhanVat(1);
        p.lvDetail.setExpNew(LevelDetail.expMain[3]); // cap 4 nhung lastLV = 1
        real.Map.addXPForChar(p, 10L, false, "map doAttackMonster2");
        Kiem.bang("cap 4 chua thuong, cong EXP (khong len cap): tu bu +15 tiem nang", 15, (int) p.basepoint);
        Kiem.bang("tu bu: lastLV = 4", 4, (int) p.lastLV);
    }

    /** sv.lau_dai bat (bang EXP + nhanh congTheoTran): cung mot khoi len cap. */
    static void cacDuongLauDai() throws Exception {
        final Char p = nhanVat(1);
        GOI.clear();
        real.Map.addXPForChar(p, expToi(p, 4) + 5L, false, "nhiem vu A-002");
        Kiem.bang("lau dai: nhiem vu 1 -> 4 +15 tiem nang", 15, (int) p.basepoint);
        Kiem.bang("lau dai: +3 ky nang", 3, (int) p.skillpoint);
    }

    /** Diem huan luyen (luat moi, production): the "+X Điểm huấn luyện" cho moi cap, tong suy tu lastLV. */
    @SuppressWarnings("unchecked")
    static void huanLuyen() throws Exception {
        final java.lang.reflect.Field fd = com.htbb.pokemon.HuanLuyen.class.getDeclaredField("DONG");
        final java.lang.reflect.Field ft = com.htbb.pokemon.HuanLuyen.class.getDeclaredField("TANG");
        final java.lang.reflect.Field ftt = real.HocChieuGame.class.getDeclaredField("TT");
        fd.setAccessible(true);
        ft.setAccessible(true);
        ftt.setAccessible(true);
        final Object dongCu = fd.get(null);
        final Object tangCu = ft.get(null);
        final java.util.Map<Integer, Object> tt = (java.util.Map<Integer, Object>) ftt.get(null);
        try {
            final List<com.htbb.pokemon.HuanLuyen.Dong> d = new ArrayList<com.htbb.pokemon.HuanLuyen.Dong>();
            d.add(new com.htbb.pokemon.HuanLuyen.Dong(0, "hp", "HP", "", 80, 40, 3, 2, 0));
            com.htbb.pokemon.HuanLuyen.datDong(d);
            final java.util.Map<Integer, int[]> tang = new java.util.HashMap<Integer, int[]>();
            for (int c = 0; c < 5; c++) {
                tang.put(c, new int[]{1, 1, 1, 1, 1});
            }
            com.htbb.pokemon.HuanLuyen.datTangTruong(tang);
            final Char p = nhanVat(1);
            p.charDBID = 990790;
            p.isBot = -1;
            final Class<?> c = Class.forName("real.HocChieuGame$TrangThai");
            final java.lang.reflect.Constructor<?> k = c.getDeclaredConstructor();
            k.setAccessible(true);
            final Object t = k.newInstance();
            final java.lang.reflect.Field pb = c.getDeclaredField("phanBo");
            pb.setAccessible(true);
            pb.set(t, new int[1]);
            tt.put(990790, t);
            Kiem.dung("huan luyen: nhan vat theo Diem huan luyen", real.HocChieuGame.dangHuanLuyen(p));
            GOI.clear();
            real.Map.addXPForChar(p, expToi(p, 6) + 5L, false, "nhiem vu A-003");
            Kiem.bang("huan luyen: nhiem vu 1 -> 6, tong 2 x 5 = 10", 10, real.HocChieuGame.tongDiem(p));
            Kiem.bang("huan luyen: lastLV = 6", 6, (int) p.lastLV);
            Kiem.dung("huan luyen: the \"Lên cấp 6! +10 Điểm huấn luyện\"", coChu("Lên cấp 6! +10 Điểm huấn luyện"));
            Kiem.dung("huan luyen: the chi duong \"Còn 10 điểm\"", coChu("Còn 10 điểm"));
            GOI.clear();
            Kiem.bang("huan luyen: goi lai khong cong them", 0, real.Map.thuongLenCap(p, "kiem"));
            Kiem.bang("huan luyen: tong van 10", 10, real.HocChieuGame.tongDiem(p));
        } finally {
            tt.remove(990790);
            fd.set(null, dongCu);
            ft.set(null, tangCu);
        }
    }

    /** HuongDanTanThuGame.boQua tra moi buoc con lai qua InfoQuestTemplate.addGift (nhieu lan EXP lien tiep). */
    static void boQuaHuongDan() throws Exception {
        final Char p = nhanVat(1);
        GOI.clear();
        long tong = 0L;
        final int moiBuoc = (int) Math.max(1L, (expToi(p, 6) + 20L) / 4L + 1L);
        for (int i = 0; i < 4; i++) {
            final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
            t.exp = moiBuoc;
            t.idItemgift = new short[]{32000, 32000, 32000, 32000, 32000, 0};
            t.addGift(p);
            tong += t.exp;
        }
        final int cap = p.lvDetail.lv;
        Kiem.dung("bo qua huong dan (4 buoc EXP): len >= cap 6", cap >= 6);
        Kiem.bang("bo qua huong dan: tiem nang = 5 x (cap - 1)", 5 * (cap - 1), (int) p.basepoint);
        Kiem.bang("bo qua huong dan: ky nang = cap - 1", cap - 1, (int) p.skillpoint);
    }

    static void buKhiNap() {
        // cap da len luc nap (initPinfo) ma chua thuong: +5/+1 moi chi so/+1 ky nang tung cap
        Char p = nhanVat(3);
        p.dbBasic = "x";
        p.isBot = -1;
        p.charDBID = 990777;
        p.lvDetail.setExpNew(LevelDetail.expMain[5]);
        p.lastLV = 6;
        p.capChuaThuongKhiNap = 3;
        int[] kq = real.ThuongLenCapNap.khiNap(p);
        Kiem.bang("nap: 3 cap chua thuong -> +30 tiem nang (15 tu do + 15 chi so)", 30, kq[0]);
        Kiem.bang("nap: basepoint 10 -> 25", 25, (int) p.basepoint);
        Kiem.bang("nap: suc manh +3", data.Database.basicPoint[1][0] + 5, (int) p.strength);
        Kiem.bang("nap: ky nang 2 -> 5", 5, (int) p.skillpoint);
        Kiem.bang("nap: dat lai so cap chua thuong", 0, p.capChuaThuongKhiNap);
        kq = real.ThuongLenCapNap.khiNap(p);
        Kiem.bang("nap lai (dang nhap lai): khong cong lan hai (tiem nang)", 0, kq[0]);
        Kiem.bang("nap lai: khong cong lan hai (ky nang)", 0, kq[1]);
        Kiem.bang("nap lai: basepoint giu 25", 25, (int) p.basepoint);

        // da mat diem tu truoc (lastLV dung, diem thieu): bu dung phan thieu vao diem tu do
        p = nhanVat(10);
        p.dbBasic = "x";
        p.isBot = -1;
        p.charDBID = 990778;
        p.basepoint = 0; // mat 45 diem tu do
        p.skillpoint = 2; // mat 7 diem ky nang
        kq = real.ThuongLenCapNap.khiNap(p);
        Kiem.bang("bu: thieu 45 tiem nang", 45, kq[0]);
        // hoc chieu theo loai bat (co du lieu learnset): diem ky nang tinh lai tu lastLV trong HocChieuGame.khiNap
        Kiem.bang("bu: thieu 7 ky nang (luat cu; hoc chieu theo loai: 0, tinh lai sau)", real.HocChieuGame.batChieu() ? 0 : 7, kq[1]);
        Kiem.bang("bu: basepoint = 45", 45, (int) p.basepoint);
        kq = real.ThuongLenCapNap.khiNap(p);
        Kiem.bang("bu lan 2: 0 (idempotent)", 0, kq[0] + kq[1]);

        // da cong / co them (vat pham, do cu): khong bao gio tru
        p = nhanVat(10);
        p.dbBasic = "x";
        p.isBot = -1;
        p.charDBID = 990779;
        p.basepoint = 300;
        p.skillpoint = 40;
        kq = real.ThuongLenCapNap.khiNap(p);
        Kiem.bang("du / thua diem: khong cong", 0, kq[0] + kq[1]);
        Kiem.bang("du / thua diem: khong tru", 300, (int) p.basepoint);
        Kiem.bang("du / thua ky nang: khong tru", 40, (int) p.skillpoint);

        // bot / linh thue / chua co chi so luu: bo qua
        p = nhanVat(10);
        p.basepoint = 0;
        p.isBot = 1;
        p.dbBasic = "x";
        p.charDBID = 990780;
        Kiem.bang("bot: khong bu", 0, real.ThuongLenCapNap.khiNap(p)[0]);
    }

    static void congThuc() {
        final int[] g = {30, 20, 10, 20, 10};
        Kiem.bang("tong tiem nang cap 1 = 0", 0, ThuongLenCap.tongTiemNang(1));
        Kiem.bang("tong tiem nang cap 20 = 190 (= getBasicPoint(20) + 5 x 19)", 190, ThuongLenCap.tongTiemNang(20));
        Kiem.bang("tong tiem nang khop chot CharNapDuLieu (allBasicPoint)",
                new LevelDetail().getBasicPoint(20) + 19 * 5, ThuongLenCap.tongTiemNang(20));
        Kiem.bang("cap 20 du diem: thieu 0", 0, ThuongLenCap.thieuTiemNang(20, new int[]{49, 39, 29, 39, 29}, g, 95));
        Kiem.bang("cap 20 mat 95 tu do: thieu 95", 95, ThuongLenCap.thieuTiemNang(20, new int[]{49, 39, 29, 39, 29}, g, 0));
        Kiem.bang("da cong het vao suc manh: thieu 0", 0, ThuongLenCap.thieuTiemNang(20, new int[]{144, 39, 29, 39, 29}, g, 0));
        Kiem.bang("thua diem: khong am", 0, ThuongLenCap.thieuTiemNang(20, new int[]{500, 39, 29, 39, 29}, g, 999));
        Kiem.bang("thieu chi so (null): 0", 0, ThuongLenCap.thieuTiemNang(20, null, g, 0));
        final byte[] chieu = new byte[15];
        chieu[0] = 3; // cap 1 tang + 2 diem
        chieu[2] = 4;
        chieu[5] = -1; // hoc o NPC chua cong: khong ton diem
        Kiem.bang("ky nang da tieu = 2 + 4", 6, ThuongLenCap.daTieuKyNang(chieu));
        Kiem.bang("cap 10: thieu ky nang 9 - 6 - 1 = 2", 2, ThuongLenCap.thieuKyNang(10, chieu, 1));
        Kiem.bang("ky nang thua: khong am", 0, ThuongLenCap.thieuKyNang(10, chieu, 50));
    }

    static void maNguon() throws Exception {
        final String xl = doc("src/real/MapXuLyLenh.java");
        Kiem.bang("addXpCharEvent + addXPForChar goi chung khiLenCap", 2, demChu(xl, "khiLenCap(p, currentLevel, currentXP, dxp, pos);"));
        Kiem.bang("MOT cho cong diem (HocChieuGame.congLenCap) trong MapXuLyLenh", 1, demChu(xl, "HocChieuGame.congLenCap("));
        Kiem.sai("het ban sao khoi goi 33 thu hai", demChu(xl, "new Message(33)") > 1);
        final String chat = doc("src/real/MapChat.java");
        Kiem.dung("GM slv: nang cap qua thuongLenCap (truoc dat lastLV khong cong diem)",
                chat.contains("Map.thuongLenCap(p, \"gm slv \"") && !chat.contains("p.lastLV = (short) p.lvDetail.lv;"));
        final String nap = doc("src/real/CharNapDuLieu.java");
        Kiem.dung("nap: ghi so cap chua thuong thay vi nang lastLV im lang", nap.contains("self.capChuaThuongKhiNap = self.lvDetail.lv - "));
        Kiem.dung("nap: bu diem TRUOC chot chong sua chi so",
                nap.indexOf("ThuongLenCapNap.khiNap(self);") > 0
                        && nap.indexOf("ThuongLenCapNap.khiNap(self);") < nap.indexOf("final int allBasicPoint = self.lvDetail.getBasicPoint(self.lastLV)"));
        final String ld = doc("src/data/LenhWeb.java");
        Kiem.dung("web dat cap online van qua addXPForChar", ld.contains("Map.addXPForChar(p, expMoi - expCu, true, NGUON"));
    }

    static int demChu(final String s, final String k) {
        int n = 0;
        for (int i = s.indexOf(k); i >= 0; i = s.indexOf(k, i + k.length())) {
            n++;
        }
        return n;
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
    }
}
