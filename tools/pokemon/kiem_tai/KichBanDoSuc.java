import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

/**
 * DO SUC (can bang cap 15-65, bao cao bot-cap1-40 26/10/2026 "từ lv20 chết liên tục"): do suc nguoi choi KHONG GM o cap L
 * voi do cua hang dung bac cap (+0) so voi quai cung cap / truong Gym, de chinh chi so quai / gia do co so lieu that.
 *
 * Moi muc (cap L, dong lop, ban do hoac Gym): tai khoan moi (dang ky that, khong GM, dong {@code lop} = class 0..4
 * Charmander / Bulbasaur / Eevee / Squirtle / Pichu), bo qua huong dan; CHUAN BI bang GM rieng: slv L, +xu de mua do
 * (= xu kiem duoc), +N binh thuoc loai theo cap (= nguoi choi mua du tru). Nguoi choi tu mua o tiem (lenh 24 nhu client)
 * vu khi DUNG DONG bac cap L (BotXuyenSuot.vuKhiShop) + (mac dinh) giap / phu kien bac cap L, mac vao, cong diem nhu
 * nguoi choi. Moc mien khoa ban do 100 (tob_ban_do_mo, = da xong cac cua ai) de toi thang bai quai.
 *
 *   quai : danh 15 con o ban do (-DdoSucMap, -1 = quai cap gan L nhat) bang vong danh cua bot choi that
 *          (BotXuyenSuot.Dong.danh: chieu xoay vong, uong thuoc khi < 50% HP). Ghi: HP / tan cong / thu, giay moi con, don
 *          quai trung minh (goi 10): trung binh / lon nhat, so don quai de ha minh, so lan guc, thuoc da uong, xu moi con.
 *   gym  : -DdoSucGym=2,3: thach dau Gym stt o lang (So tay -> Thách đấu Gym), danh nhu nguoi choi toi khi thang / guc /
 *          het 10 phut. Ghi: ket qua, giay, % HP boss con (theo goi 10 / lenh 7), guc, thuoc, giay luc may chu gui 90
 *          (boss guc) va luc co "Chiến thắng" - kiem "boss ket o 0,2-0,5% HP toi het gio" (bao cao can bang 29/10/2026).
 *
 * -DdoSucCap=15,20,25  -DdoSucLop=0,1,2,3,4  -DdoSucMap=8,9  -DdoSucGym=2,3  -DdoSucGiap=1  -DdoSucThuoc=12  -DdoSucLoaiThuoc=3  -DdoSucSo=15
 * -DdoSucSongSong=6  -DdoSucRa=<tep tsv>
 */
final class KichBanDoSuc {

    private KichBanDoSuc() {
    }

    static void dangKy() {
        HoiQuy.dkCham("do_suc", "do suc nguoi choi khong GM voi do shop dung bac so voi quai / Gym cung cap, moi dong (can bang cap 15-65)", KichBanDoSuc::chay);
    }

    static int[] so(String k, String md) {
        String v = System.getProperty(k, md).trim();
        if (v.isEmpty()) return new int[0];
        String[] f = v.split(",");
        int[] r = new int[f.length];
        for (int i = 0; i < f.length; i++) r[i] = Integer.parseInt(f[i].trim());
        return r;
    }

    static final List<String> DONG = Collections.synchronizedList(new ArrayList<>());
    /** -DdoSucNe=0: khong ne chieu lon bao truoc (do truong hop xau nhat). */
    static final boolean NE = !"0".equals(System.getProperty("doSucNe", "1"));

    /** Co su kien chu chua {@code chu} tu moc {@code tu} (khong cat 600 ky tu nhu chuTu). */
    static boolean coChu(BotChucNang.Bot b, int tu, String chu) {
        for (int i = Math.max(0, tu); i < b.su.size(); i++) { String c = b.su.get(i).chu; if (c != null && c.contains(chu)) return true; }
        return false;
    }

    /**
     * Ne chieu lon da bao truoc (goi -96: vong tron ban kinh r quanh muc tieu, no sau ms) nhu nguoi choi nhin vong do:
     * di ra ngoai vong (r + 2,5 o) theo huong tu tam ra minh. {@code nho[0]} = chi so su kien da xet, {@code nho[1]} = het
     * han ne (ms, khong lai gan boss truoc luc nay). @return true neu vua ne.
     */
    static boolean ne(BotChucNang.Bot b, BotChucNang.BanDo bd, int boss, long[] nho) throws IOException {
        boolean da = false;
        for (int i = (int) nho[0]; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -96 || e.d == null || e.d.length < 18) continue;
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
            int id = r.readShort();
            r.readShort(); r.readByte();
            int hinh = r.readByte(), x = r.readShort(), y = r.readShort(), rr = r.readShort();
            r.readShort(); r.readShort();
            int ms = r.readUnsignedShort();
            if (id != boss || hinh != 0 || bd == null) continue;
            double dx = b.px - x, dy = b.py - y, d = Math.hypot(dx, dy);
            if (d > rr + 8) continue; // da o ngoai vong
            if (d < 1) { dx = 1; dy = 0; d = 1; }
            int tx = (int) Math.round((x + dx / d * (rr + 40)) / 16.0), ty = (int) Math.round((y + dy / d * (rr + 40)) / 16.0);
            int[] o = bd.oGan(tx, ty, 2);
            if (o == null) o = bd.oGan((int) Math.round((x - dx / d * (rr + 40)) / 16.0), (int) Math.round((y - dy / d * (rr + 40)) / 16.0), 2);
            if (o != null) { b.diDuong(bd, o[0], o[1]); da = true; nho[1] = System.currentTimeMillis() + ms + 300; nho[2]++; }
        }
        nho[0] = b.su.size();
        return da;
    }

    static final String TIEU_DE_QUAI = "loai\tcap\tlop\tmaxhp\ttan_cong\tthu_vl\tthu_pt\tquai@map\tcap_quai\tha\tgiay_moi_con\tdon_trung\tdon_tb\tdon_max\tdon_de_guc\tguc\tthuoc\txu_moi_con\tmua";
    static final String TIEU_DE_GYM = "loai\tcap\tlop\tmaxhp\ttan_cong\tthu_vl\tthu_pt\tgym\tket\tgiay\tboss_con_pt\tst_moi_giay\tdon_trung\tdon_tb\tdon_max\tguc\tthuoc\tgiay_90\tgiay_thang\tmua";

    static void chay() throws Exception {
        BotChucNang.Bot g = HoiQuy.gm();
        HoiQuy.napTheGioi(g);
        BotXuyenSuot.napTheGioi(g);
        if (Boolean.getBoolean("doSucDuong")) { // chan doan: ban do co quai di bo toi duoc tu moi lang
            for (String c : System.getProperty("doSucCanh", "").split(",")) {
                if (c.isEmpty()) continue;
                StringBuilder sb = new StringBuilder("  canh " + c + " (load " + BotXuyenSuot.LOAD.get(Integer.parseInt(c.trim())) + "):");
                for (int[] e : BotXuyenSuot.canh(Integer.parseInt(c.trim()))) sb.append(' ').append(e[1]);
                System.out.println(sb);
            }
            for (int tu : new int[]{0, 70, 80, 301, 1701, 1901}) {
                StringBuilder sb = new StringBuilder("  tu " + tu + ":");
                for (int mid : BotXuyenSuot.MAP_THU) {
                    Integer l = BotXuyenSuot.LOAD.get(mid);
                    if (l == null || BotXuyenSuot.luoiQuai(l).isEmpty()) continue;
                    List<int[]> du = BotXuyenSuot.duong(tu, mid);
                    if (du != null) sb.append(' ').append(mid).append('/').append(l);
                }
                for (int lang : new int[]{0, 70, 80, 301, 1701, 1901}) sb.append(" ->lang").append(lang).append(BotXuyenSuot.duong(tu, lang) == null ? ":khong" : ":co");
                for (String dich : System.getProperty("doSucDuongToi", "").split(",")) {
                    if (dich.isEmpty()) continue;
                    List<int[]> du = BotXuyenSuot.duong(tu, Integer.parseInt(dich.trim()));
                    StringBuilder qua = new StringBuilder();
                    if (du != null) for (int[] e : du) qua.append(e[1]).append(' ');
                    sb.append(" ->").append(dich).append(du == null ? ":khong" : ":co(" + qua.toString().trim() + ")");
                }
                System.out.println(sb);
            }
            return;
        }
        List<Callable<Void>> viec = new ArrayList<>();
        int[] gyms = so("doSucGym", "");
        for (int lop : so("doSucLop", "0")) {
            for (int L : so("doSucCap", "15,20,25,30,35,40")) {
                if (gyms.length > 0) {
                    for (int stt : gyms) viec.add(() -> { chay1(() -> doGym(L, lop, stt), "gym" + stt + "\t" + L + "\t" + lop); return null; });
                } else {
                    for (int map : so("doSucMap", "-1")) viec.add(() -> { chay1(() -> do1(L, lop, map), "quai\t" + L + "\t" + lop); return null; });
                }
            }
        }
        ExecutorService ex = Executors.newFixedThreadPool(Math.max(1, Integer.getInteger("doSucSongSong", 6)));
        List<Future<Void>> fs = new ArrayList<>();
        for (Callable<Void> c : viec) {
            fs.add(ex.submit(c));
            Thread.sleep(gyms.length > 0 ? 25000 : 5000); // Gym: boss cua minh la boss MOI hien canh minh - khong trieu hoi cung luc
        }
        for (Future<Void> f : fs) f.get();
        ex.shutdown();
        List<String> d = new ArrayList<>(DONG);
        Collections.sort(d, Comparator.comparing((String x) -> x.split("\t")[0]).thenComparingInt(x -> Integer.parseInt(x.split("\t")[1]))
                .thenComparingInt(x -> Integer.parseInt(x.split("\t")[2])).thenComparing(x -> x.split("\t")[7]));
        StringBuilder s = new StringBuilder((gyms.length > 0 ? TIEU_DE_GYM : TIEU_DE_QUAI) + "\n");
        for (String x : d) s.append(x).append('\n');
        System.out.println(s);
        String ra = System.getProperty("doSucRa");
        if (ra != null) Files.write(new File(ra).toPath(), s.toString().getBytes(StandardCharsets.UTF_8));
        HoiQuy.ket("do_suc.xong", !d.isEmpty(), d.size() + " dong");
    }

    interface Viec {
        void chay() throws Exception;
    }

    static void chay1(Viec v, String nhan) {
        try {
            v.chay();
        } catch (Throwable e) {
            DONG.add(nhan + "\tLOI " + e);
            e.printStackTrace(System.out);
        }
    }

    /** Nguoi choi moi dong {@code lop} o cap L, mien khoa ban do, xu mua do, thuoc du tru; mua + mac do tiem. */
    static BotChucNang.Bot chuanBi(int L, int lop, BotXuyenSuot.Dong[] ra, List<String> mua) throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi(System.getProperty("doSucTienTo", "ds"), lop, Integer.getInteger("doSucVung", 0)); // vung 1 = Johto (bai cap 43+ chi di bo toi tu lang Johto) // tien to rieng moi tien trinh (ten khong trung)
        KichBanHoiQuy.boQuaHuongDan(b);
        HoiQuy.datCap(b, L);
        String user = b.user;
        int idc = b.charDb;
        HoiQuy.thoat(b);
        HoiQuy.chuanBi("moc mien khoa ban do 100 cho " + user + " (= da xong cua ai toi cap " + L + ")");
        BotChucNang.sql("UPDATE tob_ban_do_mo SET moc_mien = 100 WHERE id_char = " + idc);
        b = HoiQuy.vaoLai(user);
        KichBanDS.veLang(b);
        b.lamMoi();
        int loaiThuoc = Integer.getInteger("doSucLoaiThuoc", 3); // nguoi choi that mua binh 1000 HP / 800 xu (tot nhat moi xu, khong khoa cap)
        BotChucNang.Bot g = HoiQuy.gm();
        synchronized (g) {
            g.gm("smn " + b.ten + " 3000000 0 0", 1200);
            g.gm("spt " + b.ten + " " + loaiThuoc + " " + Integer.getInteger("doSucThuoc", 12), 800); // ~ xu du tru cua nguoi choi cap 19-25 (8-11k)
        }
        b.lamMoi();
        BotXuyenSuot.Dong d = new BotXuyenSuot.Dong(lop, b.user, System.currentTimeMillis() + 3600000L);
        d.b = b;
        d.capTruoc = b.lv;
        d.ngheDon();
        String cd = KichBanCap140.congDiemHuanLuyen(b, 400); // diem cua L-1 lan len cap, nhu bot choi that
        for (int i = 0; i < 9 * L; i++) { b.gui(36, (byte) (i % 4)); b.bom(40); }
        b.lamMoi();
        mua.add("diem:" + cd);
        if (!KichBanDS.laLang(b.mapId)) {
            int tot = -1, ds = 999;
            for (int c : new int[]{0, 70, 80, 301, 1701, 1901}) { List<int[]> du = BotXuyenSuot.duong(b.mapId, c); if (du != null && du.size() < ds) { ds = du.size(); tot = c; } }
            if (tot >= 0) d.toi(tot);
        }
        if (KichBanDS2.vaoNha(b, 103)) {
            List<Integer> loai = new ArrayList<>(Collections.singletonList(3));
            if (!"0".equals(System.getProperty("doSucGiap", "1"))) loai.addAll(Arrays.asList(0, 1, 2, 8, 9, 10, 11));
            for (int type : loai) {
                // giap / phu kien ban o Bronzong nha ben (104) - may chu chi ban mon cua NPC dang dung canh (31/10/2026)
                if (type != 3 && b.mapId == 103) { KichBanDS2.raNgoai(b); if (!KichBanDS2.vaoNha(b, 104)) { mua.add("khong_vao_104"); break; } b.gui(23, (byte) 3, (byte) lop); b.bom(800); }
                List<String> ids = type == 3
                        ? Collections.singletonList(String.valueOf(BotXuyenSuot.vuKhiShop(lop, L)))
                        : BotChucNang.sql("SELECT id FROM data_item WHERE type=" + type + " AND clazz=-1 AND level<=" + L
                                + " AND id<174 AND price>=500 AND level=(SELECT MAX(level) FROM data_item WHERE type=" + type + " AND clazz=-1 AND id<174 AND level<=" + L + " AND price>=500) ORDER BY id");
                for (String sid : ids) {
                    int tpl = Integer.parseInt(sid.trim());
                    Set<Integer> cu = new HashSet<>();
                    for (int[] x : b.doDung) cu.add(x[0]);
                    b.gui(23, (byte) 2);
                    b.bom(500);
                    // byte cuoi = dong (tab lop o tiem giap, client gui WindowInfoScr.idClass): giap mua voi 0 thanh "do cua Dong
                    // Charmander" - 4 dong kia khong mac duoc (do suc truoc 29/10/2026 chi dong 0 co giap)
                    b.gui(24, (byte) 1, (byte) 3, (short) tpl, (short) 1, (byte) lop);
                    b.bom(1000);
                    b.lamMoi();
                    int[] moi = b.timDo(x -> x[1] == tpl && !cu.contains(x[0]));
                    if (moi == null) { mua.add(tpl + ":khong_mua"); continue; }
                    int mm = b.moc();
                    b.gui(29, (short) moi[0]);
                    b.bom(700);
                    b.lamMoi();
                    boolean mac = b.timDo(x -> x[0] == moi[0]) == null;
                    mua.add(tpl + (mac ? ":mac" : ":khong_mac(" + b.chuTu(mm).replace('\t', ' ') + ")"));
                    if (mac) break;
                }
            }
            KichBanDS2.raNgoai(b);
        }
        b.lamMoi();
        ra[0] = d;
        return b;
    }

    /** Dem don quai trung minh (goi 10): {so, tong, max}. */
    static long[] demDon(BotChucNang.Bot b) {
        long[] don = new long[3];
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
        return don;
    }

    static int soThuoc(BotChucNang.Bot b) {
        return b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3);
    }

    static void do1(int L, int lop, int mapEp) throws Exception {
        List<String> mua = new ArrayList<>();
        BotXuyenSuot.Dong[] dd = new BotXuyenSuot.Dong[1];
        BotChucNang.Bot b = chuanBi(L, lop, dd, mua);
        BotXuyenSuot.Dong d = dd[0];
        // mau quai: cap gan L nhat (<= L, >= L-8) co cho sinh ma DI TOI DUOC (ban do goc uu tien)
        List<int[]> ung = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : BotXuyenSuot.CAP_QUAI.entrySet()) {
            int c = e.getValue();
            if (c > L || c < L - 8 || !BotXuyenSuot.QUAI_MAP.containsKey(e.getKey())) continue;
            ung.add(new int[]{e.getKey(), c});
        }
        ung.sort((x, y) -> y[1] - x[1]);
        int tpl = -1, capQ = -1, map = -1;
        // bai cap cao (Johto) chi co duong cong tu ban sao lang 301 / 1701 / 1901 (chan doan -DdoSucDuong): toi lang do truoc
        int tuLang = b.mapId;
        for (int lang : new int[]{301, 1701, 1901}) {
            if (BotXuyenSuot.duong(b.mapId, lang) != null) { tuLang = lang; break; }
        }
        final int tuL = tuLang;
        for (int[] u : ung) {
            int ds = 999;
            for (int m : BotXuyenSuot.QUAI_MAP.get(u[0])) { List<int[]> du = BotXuyenSuot.duong(tuL, m); int dd2 = du == null ? 999 : du.size() + (m >= 300 ? 3 : 0); if (dd2 < ds) { ds = dd2; map = m; } }
            if (ds < 999) { tpl = u[0]; capQ = u[1]; break; }
            map = -1;
        }
        if (mapEp >= 0) { map = mapEp; tpl = -1; capQ = -1; }
        if (map < 0) System.out.println("  do suc L" + L + " lop" + lop + ": khong co bai quai di toi duoc tu map " + b.mapId + " (" + ung.size() + " mau cap " + (L - 8) + ".." + L + ")");
        // ban sao lang (0 / 301 ...) co do thi cong khac nhau: bai Johto chi co duong tu mot so ban sao - vao lai toi da 8
        // lan cho toi ban sao co duong (nguoi choi that: vao lai / doi khu)
        for (int lan = 0; lan < 8 && map >= 0 && BotXuyenSuot.duong(b.mapId, map) == null; lan++) {
            String u = b.user;
            HoiQuy.thoat(b);
            b = HoiQuy.vaoLai(u);
            KichBanDS.veLang(b);
            b.lamMoi();
            d.b = b;
            d.ngheDon();
            System.out.println("  do suc L" + L + " lop" + lop + ": vao lai -> map " + b.mapId + (BotXuyenSuot.duong(b.mapId, map) == null ? " (van khong co duong toi " + map + ")" : " (co duong)"));
        }
        long[] don = demDon(b);
        int ha = 0, thuoc0 = soThuoc(b);
        long xu0 = b.xu, bd = System.currentTimeMillis();
        int guc0 = d.soChet;
        boolean toi = false;
        for (int lan = 0; lan < 5 && map >= 0 && !toi; lan++) { // cong luc dau chua dong bo vi tri: thu lai
            if (BotXuyenSuot.duong(b.mapId, map) == null && tuL != b.mapId) d.toi(tuL);
            toi = d.toi(map);
            if (!toi) { b.bom(1500); b.lamMoi(); b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8; }
        }
        if (toi) {
            bd = System.currentTimeMillis();
            synchronized (don) { don[0] = 0; don[1] = 0; don[2] = 0; }
            guc0 = d.soChet;
            b.lamMoi();
            thuoc0 = soThuoc(b); // thuoc / xu tinh tu luc toi bai (khong tinh duong di)
            xu0 = b.xu;
            if (mapEp >= 0) d.lechDuoi = 999;
            ha = d.danh(tpl, Integer.getInteger("doSucSo", 15), Integer.getInteger("doSucPhut", 8) * 60000L);
        }
        double giay = (System.currentTimeMillis() - bd) / 1000.0;
        b.lamMoi();
        int thuoc = thuoc0 - soThuoc(b);
        double tb = don[0] == 0 ? 0 : (double) don[1] / don[0];
        String dong = String.format(Locale.ROOT, "quai\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%d\t%d\t%.1f\t%d\t%.0f\t%d\t%.1f\t%d\t%d\t%.0f\t%s",
                L, lop, b.maxhp, b.tanCong, b.thuVl, b.thuPt, (tpl < 0 ? "*" : HoiQuy.TEN_QUAI.getOrDefault(tpl, "?")) + "@" + map + (toi ? "" : "(khong toi)"), capQ, ha,
                ha == 0 ? 0 : giay / ha, don[0], tb, don[2], tb == 0 ? 0 : b.maxhp / tb, d.soChet - guc0, thuoc,
                ha == 0 ? 0 : (double) (b.xu - xu0) / ha, mua);
        System.out.println("DO SUC " + dong);
        DONG.add(dong);
        HoiQuy.thoat(b);
    }

    /** Thach dau Gym {@code stt} o lang va danh nhu nguoi choi. */
    static void doGym(int L, int lop, int stt) throws Exception {
        List<String> mua = new ArrayList<>();
        BotXuyenSuot.Dong[] dd = new BotXuyenSuot.Dong[1];
        BotChucNang.Bot b = chuanBi(L, lop, dd, mua);
        BotXuyenSuot.Dong d = dd[0];
        KichBanDS.veLang(b);
        if (KichBanDS2.laOffline(b.mapId)) KichBanDS2.raNgoai(b);
        String tenBoss = BotChucNang.sql1("SELECT ten_boss FROM data_noi_dung_gym WHERE stt=" + stt);
        String loaiBoss = tenBoss == null ? "?" : tenBoss.substring(tenBoss.lastIndexOf(':') + 1).trim();
        b.lamMoi();
        d.hoiMau("truoc Gym");
        // dung cho rieng trong lang (moi bot mot cho): boss Gym hien canh nguoi thach dau - nhieu bot dung cung o vao lang
        // thi bot nham boss cua nhau (do suc 29/10/2026: "guc90_khong_thang")
        BotChucNang.BanDo bdl = BotChucNang.BanDo.nap(b.mapLoad);
        if (bdl != null) {
            KichBanHuongDan.dongBoViTri(b);
            Random rd = new Random();
            for (int lan = 0; lan < 6; lan++) {
                int[] o = bdl.oGan(b.px / 16 + rd.nextInt(17) - 8, b.py / 16 + rd.nextInt(13) - 6, 2);
                if (o != null && b.diDuong(bdl, o[0], o[1])) break;
            }
        }
        KichBanDS.timQuai(b, 0, 300);
        Set<Integer> cu = new HashSet<>(b.quai.keySet());
        int m0 = KichBanDS4.chonSoTay(b, 7);
        b.doiMenu(m0, -527, 5, 2500);
        int i = b.timMuc(loaiBoss);
        int m = b.moc();
        b.menu(-527, 5, Math.max(0, i));
        String t = b.doiChu(m, 2500, "");
        Integer boss = KichBanDS.quaiMoi(b, cu, 8000);
        int gan = Integer.MAX_VALUE;
        for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) {
            int[] qq = e.getValue();
            if (cu.contains(e.getKey()) || qq[4] < 2000 || qq[2] <= 0) continue;
            int dd2 = Math.abs(qq[0] - (b.px + 48)) + Math.abs(qq[1] - b.py); // may chu trieu hoi o (x + 48, y)
            if (dd2 < gan) { gan = dd2; boss = e.getKey(); }
        }
        long[] don = demDon(b);
        int thuoc0 = soThuoc(b), guc0 = d.soChet;
        String ket = "khong_boss";
        long bd0 = System.currentTimeMillis();
        double g90 = -1, gThang = -1;
        long[] neDem = {0};
        int hpBoss = -1, maxBoss = -1;
        if (boss != null) {
            final int id = boss;
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            KichBanHuongDan.dongBoViTri(b);
            ket = "het_gio";
            long[] nho = {m, 0, 0};
            for (int lan = 1; lan <= 4000 && System.currentTimeMillis() - bd0 < 615000L && !b.dut; lan++) {
                if (g90 < 0 && KichBanGymLang.co90(b, m, id)) g90 = (System.currentTimeMillis() - bd0) / 1000.0;
                if (gThang < 0 && coChu(b, m, "Chiến thắng")) gThang = (System.currentTimeMillis() - bd0) / 1000.0;
                if (gThang >= 0) { ket = "THANG"; b.bom(1500); if (g90 < 0 && KichBanGymLang.co90(b, m, id)) g90 = (System.currentTimeMillis() - bd0) / 1000.0; break; }
                if (g90 >= 0 && System.currentTimeMillis() - bd0 > g90 * 1000 + 8000) { ket = "guc90_khong_thang"; break; }
                if (NE) ne(b, bd, id, nho);
                neDem[0] = nho[2];
                int[] q = b.quai.get(id);
                if (q != null && bd != null && System.currentTimeMillis() > nho[1] && (Math.abs(q[0] - b.px) > 40 || Math.abs(q[1] - b.py) > 40) && lan % 5 == 1) {
                    int[] o = bd.oGan(q[0] / 16, q[1] / 16, 2);
                    if (o != null) b.diDuong(bd, o[0], o[1]);
                }
                int[] qb = b.quai.get(id);
                b.gui(9, (short) id, (byte) BotXuyenSuot.chieuTheoHe(b, lop, qb == null ? -1 : qb[5], lan));
                b.bom(280);
                if (b.hp > 0 && b.hp < b.maxhp / 2) d.uongThuoc();
                if (lan % 10 == 0) {
                    b.gui(7, (short) id);
                    b.lamMoi();
                    q = b.quai.get(id);
                    if (q != null && q[2] >= 0) { hpBoss = q[2]; maxBoss = q[4]; }
                    if (lan % 200 == 0) System.out.println("  gym" + stt + " L" + L + " lop" + lop + " " + (System.currentTimeMillis() - bd0) / 1000 + "s boss " + hpBoss + "/" + maxBoss + " minh " + b.hp + "/" + b.maxhp + " thuoc " + soThuoc(b));
                    if (b.hp <= 0) { ket = "guc"; break; }
                }
            }
        }
        double giay = (System.currentTimeMillis() - bd0) / 1000.0;
        b.lamMoi();
        if (b.hp <= 0) d.hoiSinh();
        double tb = don[0] == 0 ? 0 : (double) don[1] / don[0];
        double con = maxBoss > 0 ? 100.0 * (ket.equals("THANG") ? 0 : hpBoss) / maxBoss : -1;
        String dong = String.format(Locale.ROOT, "gym\t%d\t%d\t%d\t%d\t%d\t%d\t%s\t%s\t%.0f\t%.1f\t%.0f\t%d\t%.0f\t%d\t%d\t%d\t%.0f\t%.0f\t%s",
                L, lop, b.maxhp, b.tanCong, b.thuVl, b.thuPt, stt + ":" + loaiBoss, ket, giay, con,
                maxBoss > 0 ? (maxBoss - (ket.equals("THANG") ? 0 : Math.max(0, hpBoss))) / Math.max(1, giay) : 0, don[0], tb, don[2], d.soChet - guc0,
                thuoc0 - soThuoc(b), g90, gThang, "ne " + neDem[0] + " " + mua + " | " + t.replace('\t', ' '));
        System.out.println("DO SUC " + dong);
        DONG.add(dong);
        HoiQuy.thoat(b);
    }
}
