import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Kich ban bot TU DONG DUNG CHIEU DA GAN (0.9.24, chu game: "jar bat auto khong dung cac chieu da gan a?"). Tai khoan
 * THUONG (khong GM), may chu tu_dong.ke_hoach=1 nhu production. Bot dong vai client J2ME (platf nokia, ban 923):
 *   1) tao nhan vat moi, thoat; CSDL thu: hoc chieu 1, 2, 3 (cap 1) + dat len bai quai (-Dmap/-Dx/-Dy);
 *   2) vao lai, gan chieu 1 + 3 vao trang 1 (goi -103 loai 1 nhu RMS.saveQuickSlot), KHONG gan chieu 2;
 *   3) bat Tu dong (-100 loai 1), xin ke hoach (-100 loai 4) ~0,6 giay / lan va danh dung chieu may chu chon (goi 9),
 *      dem chieu: kieu "cu" (client 0.9.21-0.9.23: goi loai 4 tron) va "moi" (kem danh sach chieu gan [1, 3]).
 * DAT khi: co danh, moi chieu may chu chon thuoc {1, 3, 0 (danh thuong)}, khong bao gio chieu 2.
 *
 *   javac -encoding UTF-8 -d $B tools/pokemon/kiem_tai/*.java
 *   java -cp $B:server/lib/mysql-connector-java-5.1.49.jar -Ddbport=3611 -Ddb=htbb_db -Dban=923 -Dtu=1 -Dden=9 \
 *     KichBanTuDongChieu 127.0.0.1 19911 tdc [cu|moi ...]
 */
public final class KichBanTuDongChieu {

    private KichBanTuDongChieu() {
    }

    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else hong++;
        System.out.println((ok ? "PASS " : "FAIL ") + ten);
    }

    /** Ma 12 bit mot o trang chieu (client TrangChieu.ma): kieu (quickslotType + 1) 3 bit | gia tri 8 bit | buff 1 bit. */
    static long o(int i, int chieu) {
        return ((long) (2 << 9 | (chieu & 255) << 1)) << (12 * i);
    }

    /** CSDL thu: hoc chieu 1, 2, 3 (cap 1) + dat nhan vat len bai quai (-Dmap, mac dinh 1 Pallet). */
    static void chuanBiNhanVat(int db) {
        int map = Integer.getInteger("map", 1), x = Integer.getInteger("x", 0), y = Integer.getInteger("y", 0);
        if (x <= 0) {
            // o di duoc giua ban do (vung lien thong lon), tranh o cong
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(map);
            if (bd != null) {
                int[] g = bd.oGan(bd.w / 2, bd.h / 2, Math.max(bd.w, bd.h));
                for (int r = 0; g != null && bd.vungLienThong(g[0], g[1], 200) < 200 && r < 40; r++) g = bd.oGan(bd.w / 2 + r, bd.h / 2 + r, 30);
                if (g != null) {
                    x = g[0] * 16 + 8;
                    y = g[1] * 16 + 8;
                }
            }
        }
        List<String> sk = BotChucNang.sql("SELECT skill, xp, lastLv, map, x, y FROM tob_char WHERE id = " + db);
        System.out.println("nhan vat " + db + " truoc: " + sk);
        String chieu = sk.isEmpty() ? "" : sk.get(0).split("\t")[0];
        String[] a = chieu.split(",");
        for (int i = 1; i <= 3 && i < a.length; i++) a[i] = "1";
        String moi = String.join(",", a);
        // vi tri that nam o pInfo[8..10] (map, x, y - real.CharNapDuLieu)
        String pi = BotChucNang.sql("SELECT pInfo FROM tob_char WHERE id = " + db).get(0);
        String[] p = pi.split(",");
        p[8] = String.valueOf(map);
        p[9] = String.valueOf(x);
        p[10] = String.valueOf(y);
        StringBuilder q = new StringBuilder("UPDATE tob_char SET skill = '" + moi + "', map = " + map + ", pInfo = '" + String.join(",", p) + "'");
        q.append(" WHERE id = ").append(db);
        BotChucNang.sql(q.toString());
        System.out.println("nhan vat " + db + " sau: " + BotChucNang.sql("SELECT skill, map, x, y FROM tob_char WHERE id = " + db));
    }

    static Map<Integer, Integer> chay(String kieu) throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        int db = b.charDb;
        b.close();
        Thread.sleep(1500);
        chuanBiNhanVat(db);
        b = new BotChucNang.Bot(b.user);
        String l = b.dangNhap("123456");
        if (l != null) throw new IllegalStateException("dang nhap lai: " + l);
        l = b.vaoGame(0);
        if (l != null) throw new IllegalStateException(l);
        System.out.println("vao map " + b.mapId + " @" + b.px + "," + b.py + " cap " + b.lv + " chieu " + java.util.Arrays.toString(b.chieu));
        // gan chieu 1 (o phim 1) + 3 (o phim 3) vao trang 1 - nhu J2ME "Cho vao phim tat" -> RMS.saveQuickSlot
        b.gui(-103, (byte) 1, (byte) 0, o(0, 1) | o(1, 3), 0L, 0L);
        b.bom(500);
        // bat Tu dong: ban kinh 240, khong tu hoi sinh, giao thuc 2 + cai dat
        b.gui(-100, (byte) 1, (short) 240, (byte) 0, (byte) 2, (byte) 50, (byte) 30, (byte) 1, (byte) 1, (byte) 0);
        b.bom(800);
        System.out.println("bat Tu dong: " + trangThai(b, 0));
        Map<Integer, Integer> dem = new TreeMap<>();
        int ke = 0;
        long het = System.currentTimeMillis() + Integer.getInteger("giay", 25) * 1000L;
        int tu = b.moc();
        while (System.currentTimeMillis() < het && !b.dut) {
            if ("moi".equals(kieu)) {
                b.gui(-100, (byte) 4, (byte) 1, (byte) 0, (byte) 2, (byte) 1, (byte) 3);
            } else {
                b.gui(-100, (byte) 4);
            }
            b.bom(600);
            int[] k = null;
            for (int i = tu; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd != -100 || e.d == null || e.d.length < 2 || e.d[0] != 1) continue;
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                k = new int[]{r.readByte(), r.readShort(), r.readShort(), r.readShort(), r.readShort()};
                ke++;
                if (ke <= 3 || ke % 10 == 0) System.out.println("ke hoach: hd=" + k[0] + " id=" + k[1] + " so=" + k[2] + " @" + k[3] + "," + k[4] + " '" + r.readUTF() + "'");
            }
            tu = b.su.size();
            if (k == null) continue;
            if (k[0] == 1) {
                if (Math.abs(b.px - k[3]) > 40 || Math.abs(b.py - k[4]) > 40) {
                    int dx = Integer.signum(k[3] - b.px) * Math.min(40, Math.abs(k[3] - b.px));
                    int dy = Integer.signum(k[4] - b.py) * Math.min(40, Math.abs(k[4] - b.py));
                    b.diToi(b.px + dx, b.py + dy);
                }
                if (k[2] >= 0) {
                    dem.merge(k[2], 1, Integer::sum);
                    b.gui(9, (short) k[1], (byte) k[2]);
                }
            } else if (k[0] == 4 && (Math.abs(b.px - k[3]) > 16 || Math.abs(b.py - k[4]) > 16)) {
                b.diToi(k[3], k[4]);
            }
        }
        System.out.println("[" + kieu + "] so ke hoach " + ke + ", chieu may chu chon (chieu=lan): " + dem);
        b.gui(-100, (byte) 2);
        b.bom(300);
        b.close();
        return dem;
    }

    /** Cau trang thai -100 byte 0 cuoi cung (bat, thong bao) + chu tren dau. */
    static String trangThai(BotChucNang.Bot b, int tu) {
        String kq = "";
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -100 || e.d == null || e.d.length < 2 || e.d[0] != 0) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                int bat = r.readByte();
                r.readInt();
                r.readInt();
                r.readShort();
                int co = r.readByte();
                kq = "bat=" + bat + " co=" + co + " '" + r.readUTF() + "'";
            } catch (Exception ex) {
                kq = "goi ngan";
            }
        }
        return kq + " | " + b.chuTu(Math.max(0, b.su.size() - 8));
    }

    /**
     * Do cam tay HONG (31/10/2026, chu game: "do vat cam tay bi hong, thoat game vao lai thi lai treo binh thuong"):
     * CSDL thu cho nhan vat cam "Than lua so cap" (mau 79, lop 0) do ben 0/0. Vao game, bat Tu dong -> may chu tu choi
     * kem ly do Tho ren; thoat, vao lai -> VAN hong (khong sua mien phi) -> van tu choi. @return {lan 1 tu choi, lan 2}.
     */
    static boolean[] hong() throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        int db = b.charDb;
        b.close();
        Thread.sleep(1500);
        chuanBiNhanVat(db);
        String eq = BotChucNang.sql("SELECT equip FROM tob_char WHERE id = " + db).get(0);
        // vu khi mac dinh cua nhan vat moi (CharTrangBi.setItemWearing: mau 79 lop 0) -> do ben 0/0
        String[] mon = eq.split(">");
        boolean co = false;
        for (int i = 0; i < mon.length; i++) {
            String[] hai = mon[i].split("\\|", 2);
            String[] f = hai[0].split(",");
            if (f.length > 4 && "79".equals(f[1])) {
                f[3] = "0";
                f[4] = "0";
                mon[i] = String.join(",", f) + "|" + hai[1];
                co = true;
            }
        }
        if (!co) throw new IllegalStateException("khong thay vu khi mau 79 trong equip: " + eq);
        BotChucNang.sql("UPDATE tob_char SET equip = '" + String.join(">", mon) + "' WHERE id = " + db);
        boolean[] kq = new boolean[2];
        for (int lan = 0; lan < 2; lan++) {
            b = new BotChucNang.Bot(b.user);
            String l = b.dangNhap("123456");
            if (l != null) throw new IllegalStateException("dang nhap: " + l);
            l = b.vaoGame(0);
            if (l != null) throw new IllegalStateException(l);
            int tu = b.moc();
            b.gui(-100, (byte) 1, (short) 240, (byte) 0, (byte) 2, (byte) 50, (byte) 30, (byte) 1, (byte) 1, (byte) 0);
            b.bom(1200);
            String tt = trangThai(b, tu);
            System.out.println("[hong] lan " + (lan + 1) + " (map " + b.mapId + "): " + tt.substring(0, Math.min(200, tt.length())));
            kq[lan] = tt.startsWith("bat=0") && tt.contains("Thợ rèn");
            b.gui(-100, (byte) 2);
            b.bom(300);
            b.close();
            Thread.sleep(2500); // may chu luu nhan vat khi thoat
            List<String> e2 = BotChucNang.sql("SELECT equip FROM tob_char WHERE id = " + db);
            String luu = e2.get(0);
            int i = luu.indexOf(",79,");
            System.out.println("[hong] equip sau thoat: " + (i < 0 ? "khong thay mau 79" : luu.substring(i, Math.min(luu.length(), i + 20))));
        }
        return kq;
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.HOST = a[0];
        BotChucNang.PORT = Integer.parseInt(a[1]);
        BotChucNang.TIEN_TO = a[2];
        List<String> ds = new ArrayList<>();
        for (int i = 3; i < a.length; i++) ds.add(a[i]);
        if (ds.isEmpty()) {
            ds.add("cu");
            ds.add("moi");
        }
        for (String k : ds) {
            if ("hong".equals(k)) {
                boolean[] h = hong();
                kiem(h[0], "hong: bat Tu dong khi do cam tay hong -> tu choi + noi Tho ren");
                kiem(h[1], "hong: thoat vao lai (chua sua) -> VAN hong, van tu choi (khong sua mien phi)");
                continue;
            }
            Map<Integer, Integer> dem = chay(k);
            int tong = 0;
            for (int v : dem.values()) tong += v;
            kiem(tong > 0, k + ": Tu dong co danh (" + tong + " don)");
            kiem(!dem.containsKey(2), k + ": khong bao gio chieu 2 (khong gan): " + dem);
            boolean chiGan = true;
            for (int s : dem.keySet()) chiGan &= s == 1 || s == 3 || s == 0;
            kiem(chiGan, k + ": chi chieu da gan 1 / 3 hoac danh thuong 0: " + dem);
        }
        System.out.println("=== TONG: PASS " + dat + ", FAIL " + hong + ", BO QUA 0");
        System.exit(hong == 0 ? 0 : 1);
    }
}
