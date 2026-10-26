import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * DO SUC (can bang cap 15-40, bao cao bot-cap1-40 26/10/2026 "từ lv20 chết liên tục"): do suc nguoi choi KHONG GM o cap L
 * voi do cua hang dung bac cap (+0) so voi quai cung cap, de chinh duong chi so quai / gia do co so lieu that.
 *
 * Moi muc (cap L, bo do): tai khoan moi (dang ky that, khong GM), bo qua huong dan; CHUAN BI bang GM rieng: slv L, +xu de mua
 * do (= xu kiem duoc), +60 thuoc. Nguoi choi tu mua o tiem (lenh 24 nhu client) vu khi bac cap L (bo 0) hoac vu khi + moi o
 * giap bac cap L (bo 1), mac vao, cong diem nhu nguoi choi (KichBanCap140: tiem nang -> suc manh, ky nang -> chieu 0..3).
 * Moc mien khoa ban do 100 (tob_ban_do_mo, = da xong cac cua ai) de toi thang bai quai cung cap.
 * Danh 15 con mau quai cap gan L nhat (<= L) bang vong danh cua bot choi that (BotXuyenSuot.Dong.danh: chieu xoay vong,
 * uong thuoc khi < 50% HP). Ghi: HP / tan cong / thu, giay moi con, don quai trung (goi 10 -> minh): trung binh / lon nhat,
 * so don quai can de ha minh (HP / don TB), so lan guc, thuoc da uong, xu moi con.
 *
 * -DdoSucCap=15,20,25,30,35,40  -DdoSucBo=0,1  -DdoSucSo=15  -DdoSucRa=<tep tsv>
 */
final class KichBanDoSuc {

    private KichBanDoSuc() {
    }

    static void dangKy() {
        HoiQuy.dkCham("do_suc", "do suc nguoi choi khong GM voi do shop dung bac so voi quai cung cap (can bang cap 15-40)", KichBanDoSuc::chay);
    }

    static int[] so(String k, String md) {
        String[] f = System.getProperty(k, md).split(",");
        int[] r = new int[f.length];
        for (int i = 0; i < f.length; i++) r[i] = Integer.parseInt(f[i].trim());
        return r;
    }

    static final List<String> DONG = Collections.synchronizedList(new ArrayList<>());

    static void chay() throws Exception {
        BotChucNang.Bot g = HoiQuy.gm();
        HoiQuy.napTheGioi(g);
        BotXuyenSuot.napTheGioi(g);
        List<Thread> ts = new ArrayList<>();
        for (int L : so("doSucCap", "15,20,25,30,35,40")) {
            for (int bo : so("doSucVk", "0")) { // 0 = vu khi dung bac cap L; N = vu khi cua hang cap N (cham bac)
                Thread t = new Thread(() -> {
                    try {
                        do1(L, bo);
                    } catch (Throwable e) {
                        DONG.add(L + "\t" + bo + "\tLOI " + e);
                        e.printStackTrace(System.out);
                    }
                });
                t.start();
                ts.add(t);
                Thread.sleep(4000);
            }
        }
        for (Thread t : ts) t.join();
        List<String> d = new ArrayList<>(DONG);
        Collections.sort(d, Comparator.comparingInt((String x) -> Integer.parseInt(x.split("\t")[0])).thenComparing(x -> x.split("\t")[1]));
        StringBuilder s = new StringBuilder("cap\tbo\tmaxhp\ttan_cong\tthu_vl\tthu_pt\tquai\tcap_quai\tha\tgiay_moi_con\tdon_trung\tdon_tb\tdon_max\tdon_de_guc\tguc\tthuoc\txu_moi_con\n");
        for (String x : d) s.append(x).append('\n');
        System.out.println(s);
        String ra = System.getProperty("doSucRa");
        if (ra != null) Files.write(new File(ra).toPath(), s.toString().getBytes(StandardCharsets.UTF_8));
        HoiQuy.ket("do_suc.xong", !d.isEmpty(), d.size() + " dong");
    }

    static void do1(int L, int bo) throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("ds", 0, 0);
        KichBanHoiQuy.boQuaHuongDan(b);
        HoiQuy.datCap(b, L);
        // CHUAN BI: moc mien tru khoa ban do 100 (= nguoi choi da xong cac cua ai toi cap L) - toi thang bai quai cung cap
        String user = b.user;
        int idc = b.charDb;
        HoiQuy.thoat(b);
        HoiQuy.chuanBi("moc mien khoa ban do 100 cho " + user + " (= da xong cua ai toi cap " + L + ")");
        BotChucNang.sql("UPDATE tob_ban_do_mo SET moc_mien = 100 WHERE id_char = " + idc);
        b = HoiQuy.vaoLai(user);
        KichBanDS.veLang(b);
        b.lamMoi();
        BotChucNang.Bot g = HoiQuy.gm();
        synchronized (g) {
            g.gm("smn " + b.ten + " 3000000 0 0", 1200);
            g.gm("spt " + b.ten + " 2 60", 800);
        }
        b.lamMoi();
        BotXuyenSuot.Dong d = new BotXuyenSuot.Dong(0, b.user, System.currentTimeMillis() + 3600000L);
        d.b = b;
        d.capTruoc = b.lv;
        for (int i = 1; i < L; i++) d.congDiem();
        // ve lang co tiem, mua do
        if (!KichBanDS.laLang(b.mapId)) {
            int tot = -1, ds = 999;
            for (int c : new int[]{0, 70, 80, 301, 1701, 1901}) { List<int[]> du = BotXuyenSuot.duong(b.mapId, c); if (du != null && du.size() < ds) { ds = du.size(); tot = c; } }
            if (tot >= 0) d.toi(tot);
        }
        List<String> mua = new ArrayList<>();
        final int capVk = bo > 0 ? bo : L;
        if (KichBanDS2.vaoNha(b, 103)) {
            List<Integer> loai = new ArrayList<>(Collections.singletonList(3));
            if (Boolean.getBoolean("doSucGiap")) loai.addAll(Arrays.asList(0, 1, 2, 8, 9, 10, 11));
            for (int type : loai) {
                List<String> ids = type == 3
                        ? Collections.singletonList(String.valueOf(capVk >= 36 ? 174 : 79 + Math.min(6, Math.max(0, (capVk - 1) / 5))))
                        : BotChucNang.sql("SELECT id FROM data_item WHERE type=" + type + " AND clazz=-1 AND level<=" + L
                                + " AND id<174 AND price>=500 AND level=(SELECT MAX(level) FROM data_item WHERE type=" + type + " AND clazz=-1 AND id<174 AND level<=" + L + " AND price>=500) ORDER BY id");
                for (String sid : ids) {
                    int tpl = Integer.parseInt(sid.trim());
                    Set<Integer> cu = new HashSet<>();
                    for (int[] x : b.doDung) cu.add(x[0]);
                    b.gui(23, (byte) 2);
                    b.bom(500);
                    b.gui(24, (byte) 1, (byte) 3, (short) tpl, (short) 1, (byte) 0);
                    b.bom(1000);
                    b.lamMoi();
                    int[] moi = b.timDo(x -> x[1] == tpl && !cu.contains(x[0]));
                    if (moi == null) { mua.add(tpl + ":khong_mua"); continue; }
                    b.gui(29, (short) moi[0]);
                    b.bom(700);
                    b.lamMoi();
                    boolean mac = b.timDo(x -> x[0] == moi[0]) == null;
                    mua.add(tpl + (mac ? ":mac" : ":khong_mac"));
                    if (mac) break;
                }
            }
            KichBanDS2.raNgoai(b);
        }
        b.lamMoi();
        // mau quai: cap gan L nhat (<= L, >= L-8) co cho sinh ma DI TOI DUOC (ban do goc uu tien)
        List<int[]> ung = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : BotXuyenSuot.CAP_QUAI.entrySet()) {
            int c = e.getValue();
            if (c > L || c < L - 8 || !BotXuyenSuot.QUAI_MAP.containsKey(e.getKey())) continue;
            ung.add(new int[]{e.getKey(), c});
        }
        ung.sort((x, y) -> y[1] - x[1]);
        int tpl = -1, capQ = -1, map = -1;
        for (int[] u : ung) {
            int ds = 999;
            for (int m : BotXuyenSuot.QUAI_MAP.get(u[0])) { List<int[]> du = BotXuyenSuot.duong(b.mapId, m); int dd = du == null ? 999 : du.size() + (m >= 300 ? 3 : 0); if (dd < ds) { ds = dd; map = m; } }
            if (ds < 999) { tpl = u[0]; capQ = u[1]; break; }
            map = -1;
        }
        final int mapEp = Integer.getInteger("doSucMap", -1);
        if (mapEp >= 0) { map = mapEp; tpl = -1; capQ = -1; }
        long[] don = new long[3]; // so, tong, max
        final int actor = b.actor;
        java.util.function.Consumer<BotChucNang.SuKien> cu = b.ngheNhan;
        b.ngheNhan = e -> {
            if (cu != null) cu.accept(e);
            if (e.cmd == 10 && e.d != null && e.d.length >= 12) {
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    r.readShort();
                    int tg = r.readShort();
                    int dmg = r.readInt();
                    if (tg == actor && dmg > 0) { synchronized (don) { don[0]++; don[1] += dmg; don[2] = Math.max(don[2], dmg); } }
                } catch (IOException ignored) {
                }
            }
        };
        int ha = 0, thuoc0 = b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3);
        long xu0 = b.xu, bd = System.currentTimeMillis();
        int guc0 = d.soChet;
        boolean toi = false;
        for (int lan = 0; lan < 5 && map >= 0 && !toi; lan++) { // cong luc dau chua dong bo vi tri: thu lai
            toi = d.toi(map);
            if (!toi) { b.bom(1500); b.lamMoi(); b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8; }
        }
        if (toi) {
            bd = System.currentTimeMillis();
            ha = d.danh(tpl, Integer.getInteger("doSucSo", 15), 8 * 60000L);
        }
        double giay = (System.currentTimeMillis() - bd) / 1000.0;
        b.lamMoi();
        int thuoc = thuoc0 - (b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3));
        double tb = don[0] == 0 ? 0 : (double) don[1] / don[0];
        String dong = String.format(Locale.ROOT, "%d\t%d\t%d\t%d\t%d\t%d\t%s\t%d\t%d\t%.1f\t%d\t%.0f\t%d\t%.1f\t%d\t%d\t%.0f",
                L, bo, b.maxhp, b.tanCong, b.thuVl, b.thuPt, HoiQuy.TEN_QUAI.getOrDefault(tpl, "?") + "@" + map, capQ, ha,
                ha == 0 ? 0 : giay / ha, don[0], tb, don[2], tb == 0 ? 0 : b.maxhp / tb, d.soChet - guc0, thuoc,
                ha == 0 ? 0 : (double) (b.xu - xu0) / ha);
        System.out.println("DO SUC " + dong + " | mua " + mua);
        DONG.add(dong);
        HoiQuy.thoat(b);
    }
}
