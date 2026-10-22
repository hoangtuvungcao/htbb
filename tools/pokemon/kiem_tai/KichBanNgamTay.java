import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot NGAM TAY PvP (0.9.8: real.BanHuongGame) - hai bot dau nhau bang phat ban theo huong, tren MAY CHU THU
 * RIENG (GM: slv / mto). Tai khoan <tienTo>NNN / 123456, ten nhan vat trong sv.adminAccounts.
 *   nt_che_do   bot 908 vao Dau truong 202 -> may chu gui -113 loai 1 (PvP = 1) + loai 2 (bang chieu)
 *   nt_ban      A, B bat co PK (binh 14 / 15) o Pallet (map 1); A keo ngam thang vao B -> B bi trung (goi 6 A -> B,
 *               HP giam); ban nguoc -> khong trung; hai phat lien nhau -> phat 2 con hoi chieu
 *   nt_khoa     A (908) gui lenh 6 khoa muc tieu -> bi tu choi (chu "Kéo chiêu"), B khong mat HP
 *   nt_cu       C (client 907) gui lenh 6 vao B dung huong ngang -> doi thanh ban 4 phia (trung) + nhac cap nhat 0.9.8;
 *               C khong nhan goi -113
 *   nt_tu_dong  A (dang PK) xin bat Tu dong danh -> bi tu choi ("Tự động đánh không dùng trong trận giữa người chơi")
 *   nt_pve      bot KHONG PK ("Ngam thu cong" o PvE): ban -113 vao Pokemon hoang da gan nhat -> trung (goi 9 / 90 / 30
 *               nhu duong khoa muc tieu cu; -Dnt.doiChieu=true in them phan hoi cua lenh 9 de so)
 *
 *   javac -d /tmp/bot tools/pokemon/kiem_tai/*.java
 *   java -cp /tmp/bot -Dban=908 -Dtu=1 -Dden=4 KichBanNgamTay 127.0.0.1 27741 nt [nt_...]
 */
public final class KichBanNgamTay {

    private KichBanNgamTay() {
    }

    /** Goi 6 (danh nguoi) tu moc {@code tu}: {attacker, attacked, skill, dam, hpLeft}; null = khong co. */
    static int[] goi6(BotChucNang.Bot b, int tu, int bi) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 6 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                int a = r.readShort(), c = r.readShort(), s = r.readByte(), dam = r.readInt(), hp = r.readInt();
                if (bi < 0 || c == bi) return new int[]{a, c, s, dam, hp};
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /** Goi -113 loai {@code loai} tu moc: du lieu; null = khong co. */
    static byte[] goi113(BotChucNang.Bot b, int tu, int loai) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -113 && e.d != null && e.d.length > 0 && e.d[0] == loai) return e.d;
        }
        return null;
    }

    static String chu(BotChucNang.Bot b, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.chu != null && (e.cmd == 27 || e.cmd == -104 || e.cmd == -100)) s.append(e.chu).append(" | ");
            if (e.cmd == -100 && e.d != null) {
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    r.readByte();
                    r.readByte();
                    r.readInt();
                    r.readInt();
                    r.readShort();
                    r.readByte();
                    s.append("-100: ").append(r.readUTF()).append(" | ");
                } catch (Exception ignored) {
                }
            }
        }
        return s.toString();
    }

    /** Bot vao game voi so ban client {@code ban} (BotChucNang.BAN la chung, client cu can rieng). */
    static BotChucNang.Bot botBan(int lop, int ban) throws Exception {
        BotChucNang.Bot b = new BotChucNang.Bot(BotChucNang.tkMoi());
        b.ketNoi();
        int m = b.moc();
        b.gui(1, b.user, "123456", "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "botnt-" + b.user, ban);
        b.cho(x -> x.coLenh(m, 13) || x.coLenh(m, 2) || x.dut, 8000);
        if (!b.coLenh(m, 13)) throw new IllegalStateException(b.user + " dang nhap: " + b.chuTu(m));
        String l = b.vaoGame(lop);
        if (l != null) throw new IllegalStateException(b.user + " " + l);
        b.gm("slv " + b.ten + " 40 0", 800);
        return b;
    }

    static int[] viTri(BotChucNang.Bot b) {
        return new int[]{b.px, b.py};
    }

    /** Dua bot vao Dau truong 202 o (tx, ty); tra ve vi tri pixel may chu bao. */
    static boolean vao202(BotChucNang.Bot b, int tx, int ty) throws Exception {
        boolean ok = b.den(202, tx, ty);
        b.bom(1200);
        return ok;
    }

    static BotChucNang.Bot A, B;
    static final int MAP = 1, TX = 30, TY = 30;

    /** Bat co PK bang binh co (14 / 15, GM spt) - hai phe khac nhau danh duoc nhau ngoai vung an toan. */
    static void coPk(BotChucNang.Bot b, int binh) throws Exception {
        b.gm("spt " + b.ten + " " + binh + " 1", 800);
        b.gui(22, (byte) binh);
        b.bom(800);
    }

    static void chuanBi() throws Exception {
        if (A != null) return;
        A = botBan(2, 908);
        B = botBan(2, 908);
        A.den(MAP, TX, TY);
        B.den(MAP, TX + 3, TY);
        coPk(A, 14);
        coPk(B, 15);
        A.bom(800);
        B.bom(800);
    }

    /** Huong x1000 tu a toi b (nguoi choi keo ngam vao doi thu). */
    static short[] huong(int[] a, int[] b, boolean nguoc) {
        double dx = b[0] - a[0], dy = b[1] - a[1];
        double l = Math.sqrt(dx * dx + dy * dy);
        if (l < 1) {
            dx = 1;
            dy = 0;
            l = 1;
        }
        int k = nguoc ? -1000 : 1000;
        return new short[]{(short) Math.round(dx * k / l), (short) Math.round(dy * k / l)};
    }

    static void cheDo() throws Exception {
        BotChucNang.Bot b = botBan(2, 908);
        int m = b.moc();
        boolean vao = vao202(b, 20, 22);
        b.bom(1500);
        byte[] cd = goi113(b, m, 1);
        byte[] bang = goi113(b, 0, 2);
        BotChucNang.ket("nt.che_do.vao_202", vao, "map " + b.mapId);
        BotChucNang.ket("nt.che_do.pvp", cd != null && cd.length > 1 && cd[1] == 1, cd == null ? "khong co -113 loai 1" : "pvp=" + cd[1]);
        BotChucNang.ket("nt.che_do.bang_chieu", bang != null && bang.length > 2 && bang[1] >= 15, bang == null ? "khong co -113 loai 2" : "so chieu " + bang[1]);
        b.close();
    }

    static void ban() throws Exception {
        chuanBi();
        A.bom(500);
        B.bom(500);
        int[] pa = viTri(A), pb = viTri(B);
        int ma = A.moc(), mb = B.moc();
        short[] h = huong(pa, pb, false), hn = huong(pa, pb, true);
        A.gui(-113, (byte) 0, (byte) 0, (short) pa[0], (short) pa[1], h[0], h[1]);
        B.bom(1500);
        A.bom(200);
        int[] t = goi6(B, mb, B.actor);
        BotChucNang.ket("nt.ban.trung", t != null && t[0] == A.actor && t[3] > 0, "A " + pa[0] + "," + pa[1] + " B " + pb[0] + "," + pb[1]
                + (t == null ? " khong goi 6" : " dam=" + t[3] + " hpCon=" + t[4]) + " " + chu(A, ma));
        A.bom(2500); // het hoi chieu
        int mb2 = B.moc();
        A.gui(-113, (byte) 0, (byte) 0, (short) pa[0], (short) pa[1], hn[0], hn[1]);
        B.bom(1500);
        BotChucNang.ket("nt.ban.nguoc_truot", goi6(B, mb2, B.actor) == null, "ban nguoc chieu");
        int mb3;
        A.bom(2500);
        mb3 = B.moc();
        A.gui(-113, (byte) 0, (byte) 0, (short) pa[0], (short) pa[1], h[0], h[1]);
        A.gui(-113, (byte) 0, (byte) 0, (short) pa[0], (short) pa[1], h[0], h[1]);
        B.bom(1500);
        int dem = 0;
        for (int i = mb3; i < B.su.size(); i++) if (B.su.get(i).cmd == 6) dem++;
        BotChucNang.ket("nt.ban.hoi_chieu", dem <= 1, "hai phat lien nhau: " + dem + " lan trung (phat 2 con hoi chieu)");
    }

    static void khoa() throws Exception {
        chuanBi();
        A.bom(2500);
        int ma = A.moc(), mb = B.moc();
        A.gui(6, (short) B.actor, (byte) 0);
        A.bom(1200);
        B.bom(300);
        String c = chu(A, ma);
        BotChucNang.ket("nt.khoa.tu_choi", c.contains("Kéo chiêu") || c.contains("kéo chiêu"), c);
        BotChucNang.ket("nt.khoa.khong_sat_thuong", goi6(B, mb, B.actor) == null, "B khong mat HP");
    }

    static void cu() throws Exception {
        chuanBi();
        BotChucNang.Bot c = botBan(2, 907);
        int m = c.moc();
        c.den(MAP, TX, TY); // cung o voi A: cung hang ngang voi B
        coPk(c, 14);
        c.bom(1000);
        if (c.py != B.py) c.gui(4, (short) c.px, (short) B.py);
        c.bom(500);
        int mb = B.moc(), mc = c.moc();
        c.gui(6, (short) B.actor, (byte) 0);
        c.bom(1500);
        B.bom(300);
        int[] t = goi6(B, mb, B.actor);
        String ch = chu(c, mc) + chu(c, m);
        BotChucNang.ket("nt.cu.ban_4_phia", t != null && t[0] == c.actor, t == null ? "khong trung " + c.px + "," + c.py : "dam=" + t[3]);
        BotChucNang.ket("nt.cu.nhac_cap_nhat", ch.contains("0.9.8"), ch);
        BotChucNang.ket("nt.cu.khong_goi_moi", goi113(c, 0, 0) == null && goi113(c, 0, 1) == null && goi113(c, 0, 2) == null, "client 907 khong nhan -113");
        c.close();
    }

    static void tuDong() throws Exception {
        chuanBi();
        int m = A.moc();
        A.gui(-100, (byte) 1, (short) 240, (byte) 0, (byte) 2);
        A.bom(1500);
        String c = chu(A, m);
        BotChucNang.ket("nt.tu_dong.tu_choi", c.contains("Tự động đánh không dùng trong trận giữa người chơi"), c);
    }

    static void pve() throws Exception {
        BotChucNang.Bot b = botBan(2, 908);
        b.den(MAP, TX, TY);
        b.bom(2500);
        int[] best = null;
        int bid = -1;
        long bd = Long.MAX_VALUE;
        for (java.util.Map.Entry<Integer, int[]> e : b.quai.entrySet()) {
            int[] q = e.getValue();
            long d = (long) (q[0] - b.px) * (q[0] - b.px) + (long) (q[1] - b.py) * (q[1] - b.py);
            if (d < bd) {
                bd = d;
                best = q;
                bid = e.getKey();
            }
        }
        if (best == null) {
            BotChucNang.ket("nt.pve.co_quai", null, "khong thay Pokemon hoang da quanh " + b.px + "," + b.py);
            b.close();
            return;
        }
        // lai gan quai (GM mto toi o cua quai, lech 2 o)
        b.den(MAP, best[0] / 16 - 2, best[1] / 16);
        b.bom(1200);
        int[] q = b.quai.get(bid);
        int m = b.moc();
        short[] h = huong(new int[]{b.px, b.py}, new int[]{q[0], q[1]}, false);
        b.gui(-113, (byte) 1, (byte) 1);   // bat Ngam thu cong (thong ke)
        b.gui(-113, (byte) 0, (byte) 0, (short) b.px, (short) b.py, h[0], h[1]);
        b.bom(1500);
        boolean trung = false;
        for (int i = m; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            // goi danh quai (9) / quai guc (90) / EXP (30): cung phan hoi nhu duong khoa muc tieu cu (lenh 9)
            if ((e.cmd == 9 || e.cmd == 106 || e.cmd == 90 || e.cmd == 30) && e.d != null) trung = true;
        }
        if (Boolean.getBoolean("nt.doiChieu")) {   // doi chieu: duong khoa muc tieu cu (lenh 9) co danh duoc quai nay khong
            b.bom(2500);
            int m9 = b.moc();
            b.gui(9, (short) bid, (byte) 0);
            b.bom(1500);
            StringBuilder c = new StringBuilder();
            for (int i = m9; i < b.su.size(); i++) c.append(b.su.get(i).cmd).append(' ');
            System.out.println("DOI CHIEU lenh 9 -> " + c);
        }
        BotChucNang.ket("nt.pve.trung_quai", trung, "quai " + bid + " o " + q[0] + "," + q[1] + " bot " + b.px + "," + b.py + " lenh=" + b.demLenh);
        b.close();
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("nt_pve", true, KichBanNgamTay::pve);
        BotChucNang.dk("nt_che_do", true, KichBanNgamTay::cheDo);
        BotChucNang.dk("nt_ban", true, KichBanNgamTay::ban);
        BotChucNang.dk("nt_khoa", true, KichBanNgamTay::khoa);
        BotChucNang.dk("nt_cu", true, KichBanNgamTay::cu);
        BotChucNang.dk("nt_tu_dong", true, KichBanNgamTay::tuDong);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("nt_che_do", "nt_ban", "nt_khoa", "nt_cu", "nt_tu_dong", "nt_pve"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
