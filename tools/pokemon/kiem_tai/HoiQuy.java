import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * BO HOI QUY NGUOI CHOI THAT (cong truoc khi len production, xem docs/kien-truc.md + memory "quy trinh kiem truoc khi
 * len"). Moi kich ban dung tai khoan MOI dang ky bang DUONG THAT trong game (goi 39 "#dk2": xin cau do -> tra loi),
 * nhan vat MOI, KHONG GM. Bat bien (BatBien) chay kem moi bot suot kich ban.
 *
 * Pha CHUAN BI (tach rieng, ghi ro tung muc trong bao cao): chi cap thu nguoi choi KIEM DUOC (cap do = EXP cay duoc,
 * xu = ban quai / thuong, luong khoa = qua su kien / giftcode, tuoi tai khoan = cho), qua mot tai khoan GM rieng
 * (hqgm1, "slv") hoac CSDL khi nhan vat da thoat. Moi lan cap ghi vao CHUAN_BI.
 *
 *   java -cp <bot>:<lop server> -Ddb=htbb_db -Ddbhost=127.0.0.1 -Ddbport=3393 -Dnoi=<monorepo> -Dlog=<game.log> \
 *        -Dra=<bao_cao.md> HoiQuy <host> <cong game> [kich ban ... | all | nhanh]
 *   -Dhonloan=N   so phut bot hon loan (mac dinh 3, 0 = tat)
 *   -Dkhoidonglai="<lenh>"  lenh khoi dong lai game server (kiem luu sau khoi dong lai); trong = bo qua kich ban do
 *   -Dban=917     so ban client gia lap (mac dinh 918 = client 0.9.18)
 * Thoat 0 khi khong co FAIL (DA_BIET khong tinh la FAIL), 1 khi co.
 */
public final class HoiQuy {

    private HoiQuy() {
    }

    // ================================================================== ket qua
    static final class KetQua {
        final String kichBan, muc, trangThai, bang;

        KetQua(String kichBan, String muc, String trangThai, String bang) {
            this.kichBan = kichBan;
            this.muc = muc;
            this.trangThai = trangThai;
            this.bang = bang;
        }
    }

    static final List<KetQua> KQ = Collections.synchronizedList(new ArrayList<>());
    static final List<String> CHUAN_BI = Collections.synchronizedList(new ArrayList<>());
    static final Map<String, long[]> THOI_GIAN = Collections.synchronizedMap(new LinkedHashMap<>());
    static final ThreadLocal<String> KB_HIEN = ThreadLocal.withInitial(() -> "?");

    /**
     * Loi DA BIET thuoc nhanh khac (khong sua o day): tien to muc -> nhanh. FAIL cua muc nay ghi "DA_BIET – chờ <nhánh>".
     */
    static final Map<String, String> DA_BIET = new LinkedHashMap<>();

    static {
        DA_BIET.put("shop_dac_biet.nhan_luong_khoa", "hien-luong-khoa-shop");
        DA_BIET.put("toc_do.client", "sua-slow-motion");
    }

    static void ket(String muc, Boolean ok, String bang) {
        String kb = KB_HIEN.get();
        String tt = ok == null ? "BO_QUA" : ok ? "PASS" : "FAIL";
        if (Boolean.FALSE.equals(ok)) {
            for (Map.Entry<String, String> e : DA_BIET.entrySet()) {
                if (muc.startsWith(e.getKey())) { tt = "DA_BIET – chờ " + e.getValue(); break; }
            }
        }
        String b = bang == null ? "" : bang.replace('\n', ' ');
        if (b.length() > 700) b = b.substring(0, 700) + "…";
        KQ.add(new KetQua(kb, muc, tt, b));
        System.out.printf("%-8s %-22s %-40s %s%n", tt.startsWith("DA_BIET") ? "DA_BIET" : tt, kb, muc, b);
        System.out.flush();
    }

    static void chuanBi(String viec) {
        CHUAN_BI.add(KB_HIEN.get() + ": " + viec);
        System.out.println("  [CHUAN BI] " + viec);
    }

    static void ghi(String s) {
        System.out.println("  [" + KB_HIEN.get() + "] " + s);
    }

    // ================================================================== tai khoan that
    static final String MAT_KHAU = "123456";
    static final String CHU = "bghkqrvwxz"; // khong nguyen am: khong ghep thanh tu cam / ten quan tri
    static int demTk = (int) (System.currentTimeMillis() / 1000 % 100000);
    static final Map<String, Integer> CHAR_DB = new ConcurrentHashMap<>();

    static synchronized String tenMoi(String tienTo) {
        StringBuilder s = new StringBuilder(tienTo);
        int v = demTk++;
        for (int i = 0; i < 6; i++) { s.append(CHU.charAt(v % CHU.length())); v /= CHU.length(); }
        return s.toString();
    }

    /** {loai, ...}: [0]="0" cau do (q), "1" thanh cong, "2" loi (cac thong bao). Doc goi 39 "#dk2" tu moc. */
    static String[] dk2(BotChucNang.Bot b, int tu) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 39 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                if (!"#dk2".equals(r.readUTF())) continue;
                int loai = r.readByte();
                if (loai == 0) return new String[]{"0", r.readUTF()};
                if (loai == 1) return new String[]{"1", r.readUTF(), r.readUTF()};
                int n = r.readByte();
                List<String> l = new ArrayList<>();
                l.add("2");
                for (int k = 0; k < n; k++) { l.add(r.readUTF() + ": " + r.readUTF()); }
                l.add("cau moi: " + r.readUTF());
                return l.toArray(new String[0]);
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    /** Giai cau do "Bốn cộng 2 bằng mấy?" (so viet bang chu hoac so; cong / tru / nhan). */
    static int giai(String q) {
        String s = BotChucNang.khongDau(q).replace("?", " ").replace(",", " ");
        String[] so = {"khong", "mot", "hai", "ba", "bon", "nam", "sau", "bay", "tam", "chin", "muoi"};
        List<Integer> v = new ArrayList<>();
        String phep = "+";
        for (String w : s.split("\\s+")) {
            if (w.matches("\\d+")) v.add(Integer.parseInt(w));
            else {
                for (int i = 0; i < so.length; i++) if (w.equals(so[i])) { v.add(i); break; }
                if (w.equals("tru")) phep = "-";
                if (w.equals("nhan") || w.equals("x")) phep = "*";
                if (w.equals("cong")) phep = "+";
            }
        }
        if (v.size() < 2) return 0;
        int a = v.get(0), c = v.get(1);
        return phep.equals("-") ? a - c : phep.equals("*") ? a * c : a + c;
    }

    /**
     * Dang ky tai khoan MOI bang duong trong game (y nhu client: may moi = ma may moi). Tra ve {ten, loi}: loi null =
     * thanh cong. {@code may} = ma may (chung ma may = cung thiet bi).
     */
    static String[] dangKy(String ten, String may) throws Exception {
        try (BotChucNang.Bot b = new BotChucNang.Bot(ten)) {
            b.ketNoi();
            int m = b.moc();
            b.gui(39, "#dk2", (byte) 0, may);
            b.cho(x -> dk2(x, m) != null, 6000);
            String[] q = dk2(b, m);
            if (q == null || !"0".equals(q[0])) return new String[]{ten, "khong nhan cau do: " + (q == null ? b.chuTu(m) : String.join(" | ", q))};
            Thread.sleep(2200);
            int m2 = b.moc();
            b.gui(39, "#dk2", (byte) 1, ten, MAT_KHAU, MAT_KHAU, "", String.valueOf(giai(q[1])), may);
            b.cho(x -> dk2(x, m2) != null, 8000);
            String[] k = dk2(b, m2);
            if (k == null) return new String[]{ten, "khong tra loi: " + b.chuTu(m2)};
            if (!"1".equals(k[0])) return new String[]{ten, String.join(" | ", k)};
            return new String[]{ten, null};
        }
    }

    /** Dang ky + dang nhap + tao nhan vat + vao game, gan bat bien. lop 0..4, vung 0 Kanto / 1 Johto. */
    static BotChucNang.Bot nguoiChoiMoi(String tienTo, int lop, int vung) throws Exception {
        String ten = tenMoi(tienTo);
        String[] k = dangKy(ten, "hq-may-" + ten);
        // ten ghep ngau nhien co the dinh tu cam (vd "xxx") -> bo loc ten tu choi dung luat: thu ten ke tiep
        for (int lan = 0; lan < 5 && k[1] != null && k[1].contains("tenTuCam"); lan++) {
            ten = tenMoi(tienTo);
            k = dangKy(ten, "hq-may-" + ten);
        }
        if (k[1] != null) throw new IllegalStateException("dang ky " + ten + ": " + k[1]);
        if (KICH_HOAT) kichHoat(ten);
        BotChucNang.Bot b = new BotChucNang.Bot(ten);
        BatBien.gan(b);
        String l = b.dangNhap(MAT_KHAU);
        if (l != null) throw new IllegalStateException(ten + " dang nhap: " + l);
        l = b.vaoGame(lop, vung);
        if (l != null) throw new IllegalStateException(ten + " vao game: " + l);
        CHAR_DB.put(ten, b.charDb);
        b.bom(800);
        napTheGioi(b);
        return b;
    }

    /** -Dkichhoat=0: khong kich hoat tai khoan moi (kiem dung luat tai khoan chua kich hoat). */
    static final boolean KICH_HOAT = !"0".equals(System.getProperty("kichhoat", "1"));

    /**
     * CHUAN BI: kich hoat tai khoan (= nguoi choi kich hoat tren web; open test mo kich hoat cho moi tai khoan -
     * vps/van_hanh). Lam TRUOC lan dang nhap dau (may chu nho trang thai kich hoat 30 giay).
     */
    static void kichHoat(String user) {
        if (BotChucNang.DB.isEmpty()) return;
        chuanBi("kich hoat " + user + " (5h_active 1 nam, = kich hoat tren web)");
        BotChucNang.sql("INSERT INTO 5h_active (userID, username, time_end) SELECT id, username, '" + (System.currentTimeMillis() / 1000 + 86400L * 365)
                + "' FROM account.team_user WHERE username = '" + user + "'");
    }

    /** Dang nhap lai tai khoan da co (relog), gan bat bien moi. */
    static BotChucNang.Bot vaoLai(String user) throws Exception {
        Exception cuoi = null;
        for (int lan = 0; lan < 4; lan++) {
            BotChucNang.Bot b = new BotChucNang.Bot(user);
            BatBien.gan(b);
            try {
                String l = b.dangNhap(MAT_KHAU);
                if (l == null) l = b.vaoGame(0);
                if (l == null) { b.bom(1000); return b; }
                cuoi = new IllegalStateException(user + ": " + l);
            } catch (IOException e) {
                cuoi = e;
            }
            b.close();
            Thread.sleep(2500);
        }
        throw cuoi;
    }

    /** Thoat (dong ket noi) roi doi server luu nhan vat. */
    static void thoat(BotChucNang.Bot b) throws InterruptedException {
        BatBien.kiemKet(b);
        b.close();
        Thread.sleep(2500);
    }

    // ================================================================== chuan bi (GM rieng / CSDL)
    static volatile BotChucNang.Bot gm;
    /** GM chuan bi: mac dinh kt040 (tai khoan bot GM cua staging, vps/staging/lam-moi.sh; co trong sv.adminAccounts). */
    static final String TEN_GM = System.getProperty("gmTk", "kt040");

    /** Tai khoan GM CHI de cap thu trong pha chuan bi (ten nhan vat phai co trong sv.adminAccounts). */
    static synchronized BotChucNang.Bot gm() throws Exception {
        if (gm != null && gm.conSong()) return gm;
        if (BotChucNang.sql1("SELECT id FROM account.team_user WHERE username='" + TEN_GM + "'") == null) {
            BotChucNang.sql("INSERT INTO account.team_user (username,password,phone,regdate,ban,provider,fromgame) VALUES ('" + TEN_GM
                    + "',PASSWORD('" + MAT_KHAU + "'),'',DATE_SUB(NOW(), INTERVAL 30 DAY),0,0,0)");
            BotChucNang.sql("INSERT INTO 5h_active (userID,username,time_end) SELECT id,username,'1894945812' FROM account.team_user WHERE username='" + TEN_GM + "'");
        }
        BotChucNang.Bot g = new BotChucNang.Bot(TEN_GM);
        String l = g.dangNhap(MAT_KHAU);
        if (l == null) l = g.vaoGame(0);
        if (l != null) throw new IllegalStateException("GM chuan bi " + TEN_GM + ": " + l);
        KichBanHuongDan.roiSan(g);
        gm = g;
        return g;
    }

    /** CHUAN BI: dat cap {@code lv} (= EXP nguoi choi cay duoc; slv cong du diem len cap qua ham chung). */
    static void datCap(BotChucNang.Bot b, int lv) throws Exception {
        chuanBi("cap " + b.ten + " -> " + lv + " (GM slv = EXP cay duoc; diem len cap cong qua ham chung)");
        // Cong kiem 25/10/2026 (giao_dich "Bạn cần đạt cấp 20"): slv cua GM vua dang nhap (nhan vat GM khong o san 105 ->
        // khong co buoc roiSan cho) khong an, chuan bi hong IM LANG roi kich ban bao loi luat cap. Nay KIEM cap that sau
        // slv; chua toi thi doi + gui lai, lan cuoi dang nhap lai GM; van khong toi = loi CHUAN BI ro rang (khong phai loi game).
        String ly = "";
        for (int lan = 0; lan < 3; lan++) {
            BatBien.of(b).choPhep("cap", 6000);
            BotChucNang.Bot g = gm();
            synchronized (g) {
                g.bom(lan == 0 ? 1500 : 3000);
                int mg = g.moc();
                g.gm("slv " + b.ten + " " + lv + " 0", 1200);
                ly = g.chuTu(mg);
            }
            b.bom(1000);
            b.lamMoi();
            if (b.lv >= lv) return;
            if (lan == 1) {
                synchronized (HoiQuy.class) {
                    if (gm != null) { try { gm.close(); } catch (Exception ignored) { } gm = null; }
                }
            }
        }
        throw new IllegalStateException("CHUAN BI hong: GM " + TEN_GM + " 'slv " + b.ten + " " + lv + "' khong an (cap van " + b.lv
                + ") - nhan vat GM co ten trong sv.adminAccounts? | GM thay: " + ly);
    }

    /** CHUAN BI (nhan vat da thoat): cong xu / luong khoa tren CSDL = thu kiem duoc khi choi. */
    static void capTien(String user, long xu, int lk) {
        Integer id = CHAR_DB.get(user);
        chuanBi("+" + xu + " xu, +" + lk + " luong khoa cho " + user + " (CSDL khi da thoat; = ban quai / qua su kien)");
        BotChucNang.sql("UPDATE tob_char SET gold = gold + " + xu + ", luonglock = luonglock + " + lk + " WHERE id = " + id);
    }

    /** CHUAN BI (nhan vat da thoat): luong (= nap that / qua), chi dung cho kich ban shop luong. */
    static void capLuong(String user, int luong) {
        Integer id = CHAR_DB.get(user);
        chuanBi("+" + luong + " luong cho " + user + " (CSDL khi da thoat; = nap tien)");
        BotChucNang.sql("UPDATE tob_char SET luong = luong + " + luong + " WHERE id = " + id);
    }

    /** CHUAN BI: tuoi tai khoan 30 ngay + 15 gio choi (chong clone mo giao dich / cho = cho doi that). */
    static void duTuoi(BotChucNang.Bot b) {
        chuanBi("tuoi tai khoan " + b.user + " 30 ngay + 15 gio choi (= cho doi; mo giao dich / cho)");
        BotChucNang.duTuoiChongClone(b);
    }

    /** CHUAN BI: giftcode rieng cho kich ban (nguoi choi nhap bang duong that -31/7). */
    static String giftcode(String phanThuong) {
        String ma = "hq" + tenMoi("");
        chuanBi("giftcode " + ma + " = " + phanThuong + " (nguoi choi tu nhap)");
        BotChucNang.sql("INSERT INTO tob_giftcode (ma, phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat, ghi_chu) VALUES ('"
                + ma + "', '" + phanThuong + "', NULL, 5, 0, 1, 1, 'hoi quy')");
        return ma;
    }

    // ================================================================== cau hinh tam (tra lai cuoi)
    static final Map<String, String> CAU_HINH_CU = new LinkedHashMap<>();

    static void datCauHinh(String khoa, String gt) {
        if (!CAU_HINH_CU.containsKey(khoa)) CAU_HINH_CU.put(khoa, BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = '" + khoa + "'"));
        BotChucNang.sql("INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES ('" + khoa + "', '" + gt
                + "', 'hoi_quy', NOW()) ON DUPLICATE KEY UPDATE gia_tri = '" + gt + "', sua_luc = NOW()");
    }

    static void traCauHinh() {
        for (Map.Entry<String, String> e : CAU_HINH_CU.entrySet()) {
            if (e.getValue() == null) BotChucNang.sql("DELETE FROM cau_hinh WHERE khoa = '" + e.getKey() + "'");
            else BotChucNang.sql("UPDATE cau_hinh SET gia_tri = '" + e.getValue() + "' WHERE khoa = '" + e.getKey() + "'");
        }
        CAU_HINH_CU.clear();
    }

    // ================================================================== nhat ky server
    static File tepLog;
    static long viTri;
    static final Pattern LOI = Pattern.compile("Exception|\\sE\\s|SEVERE|at [a-z]+\\.[A-Za-z]+\\.");

    /** Dong loi moi trong log server tu lan doc truoc. */
    static synchronized List<String> loiLogMoi() {
        List<String> kq = new ArrayList<>();
        if (tepLog == null || !tepLog.isFile()) return kq;
        try (RandomAccessFile r = new RandomAccessFile(tepLog, "r")) {
            if (r.length() < viTri) viTri = 0;
            r.seek(viTri);
            String l;
            while ((l = r.readLine()) != null) {
                String u = new String(l.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                if (!u.contains("Exception") && !u.contains(" E ")) continue;
                if (u.contains("[MANG]") || u.contains("RegionVantienTran") || u.contains("Connection reset") || u.contains("Broken pipe")) continue;
                kq.add(u.length() > 240 ? u.substring(0, 240) : u);
            }
            viTri = r.getFilePointer();
        } catch (IOException ignored) {
        }
        return kq;
    }

    // ================================================================== du lieu the gioi
    static volatile boolean daNapTheGioi;
    static final Map<Integer, String> TEN_MAP = new HashMap<>();
    static final Map<Integer, String> TEN_QUAI = new HashMap<>();

    static synchronized void napTheGioi(BotChucNang.Bot b) throws IOException {
        if (daNapTheGioi || b.viTriMap == null) return;
        BotXuyenSuot.napTheGioi(b);
        for (String d : BotChucNang.sql("SELECT map_id, ten FROM data_map_ten")) {
            String[] f = d.split("\t");
            if (f.length >= 2) try { TEN_MAP.put(Integer.parseInt(f[0]), f[1]); } catch (NumberFormatException ignored) { }
        }
        TEN_QUAI.putAll(BotXuyenSuot.TEN_QUAI);
        daNapTheGioi = true;
    }

    // ================================================================== kich ban
    interface KichBan {
        void chay() throws Exception;
    }

    static final LinkedHashMap<String, KichBan> KB = new LinkedHashMap<>();
    static final Map<String, String> MO_TA = new HashMap<>();
    static final Set<String> CHAM = new HashSet<>();

    static void dk(String ten, String moTa, KichBan k) {
        KB.put(ten, k);
        MO_TA.put(ten, moTa);
    }

    static void dkCham(String ten, String moTa, KichBan k) {
        dk(ten, moTa, k);
        CHAM.add(ten);
    }

    static void chay1(String ten) {
        KichBan k = KB.get(ten);
        KB_HIEN.set(ten);
        if (k == null) { ket(ten, null, "khong co kich ban"); return; }
        System.out.println("---- " + ten + ": " + MO_TA.get(ten));
        long bd = System.currentTimeMillis();
        loiLogMoi();
        int vp0 = BatBien.VI_PHAM.size();
        try {
            k.chay();
        } catch (Throwable e) {
            ket(ten + ".ngoai_le", false, e.toString());
            e.printStackTrace(System.out);
        }
        List<String> loi = loiLogMoi();
        ket(ten + ".log_server_sach", loi.isEmpty(), loi.isEmpty() ? "" : loi.size() + " dong: " + String.join(" || ", loi.subList(0, Math.min(4, loi.size()))));
        List<String> vp;
        if (BatBien.VI_PHAM.size() > vp0) BatBien.boSungNguonTruXu(vp0);
        synchronized (BatBien.VI_PHAM) { vp = new ArrayList<>(BatBien.VI_PHAM.subList(vp0, BatBien.VI_PHAM.size())); }
        ket(ten + ".bat_bien", vp.isEmpty(), vp.isEmpty() ? "" : vp.size() + ": " + String.join(" || ", vp.subList(0, Math.min(5, vp.size()))));
        THOI_GIAN.put(ten, new long[]{bd, System.currentTimeMillis()});
    }

    // ================================================================== main
    public static void main(String[] a) throws Exception {
        if (System.getProperty("ban") == null) System.setProperty("ban", "918");
        BotChucNang.HOST = a[0];
        BotChucNang.PORT = Integer.parseInt(a[1]);
        BotChucNang.TIEN_TO = "hq";
        String log = System.getProperty("log", "");
        if (!log.isEmpty()) { tepLog = new File(log); viTri = tepLog.isFile() ? tepLog.length() : 0; }
        KichBanHoiQuy.dangKy();
        KichBanCap140.dangKy();
        List<String> chon = new ArrayList<>();
        // goi kieu BotChucNang (hoi_quy.conf kieu cn): <host> <cong> <tien to> [kich ban ...]; tien to chi de tuong thich
        int dau = 2;
        if (a.length > 2 && !KB.containsKey(a[2]) && !a[2].equals("all") && !a[2].equals("nhanh") && !a[2].equals("hon_loan")) dau = 3;
        for (int i = dau; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty() || chon.contains("all")) chon = new ArrayList<>(KB.keySet());
        else if (chon.contains("nhanh")) { chon = new ArrayList<>(KB.keySet()); chon.removeAll(CHAM); }
        long bd = System.currentTimeMillis();
        int honLoan = Integer.getInteger("honloan", 3);
        try {
            KB_HIEN.set("chuan_bi");
            if (!BotChucNang.DB.isEmpty()) {
                // han muc dang ky theo mang (cau_hinh dang_ky.toi_da_gio/ngay): ca bo chay tu MOT IP. Kich ban dk_han_muc
                // kiem rieng han muc that; o day mo rong de cac kich ban khac co tai khoan moi. Tra lai cuoi.
                datCauHinh("dang_ky.toi_da_gio", "1000");
                datCauHinh("dang_ky.toi_da_ngay", "10000");
                chuanBi("cau_hinh dang_ky.toi_da_gio/ngay = 1000/10000 (tran danh muc) trong luc chay (bo chay tu mot IP), tra lai cuoi");
                Thread.sleep(6000); // CauHinhTrungTam nap lai <= 5 giay
            }
            for (String t : chon) {
                if (t.equals("hon_loan")) continue;
                chay1(t);
            }
            if (honLoan > 0 && (chon.contains("hon_loan") || a.length <= dau || a[dau].equals("all"))) {
                KB_HIEN.set("hon_loan");
                MO_TA.put("hon_loan", "bot hon loan " + honLoan + " phut");
                KB.putIfAbsent("hon_loan", () -> BotHonLoan.chay(honLoan));
                chay1("hon_loan");
            }
        } finally {
            KB_HIEN.set("don_dep");
            traCauHinh();
            if (gm != null) gm.close();
        }
        long tong = (System.currentTimeMillis() - bd) / 1000;
        int pass = 0, fail = 0, bo = 0, biet = 0;
        synchronized (KQ) {
            for (KetQua k : KQ) {
                if (k.trangThai.equals("PASS")) pass++;
                else if (k.trangThai.equals("FAIL")) fail++;
                else if (k.trangThai.startsWith("DA_BIET")) biet++;
                else bo++;
            }
        }
        System.out.printf("=== HOI QUY: PASS %d, FAIL %d, DA_BIET %d, BO QUA %d (%d giay) ===%n", pass, fail, biet, bo, tong);
        // dong tong ket chung voi BotChucNang (chay_hoi_quy.sh doc dong nay): DA_BIET tinh vao BO QUA (cho nhanh khac)
        System.out.printf("=== TONG: PASS %d, FAIL %d, BO QUA %d (%ds) ===%n", pass, fail, bo + biet, tong);
        String ra = System.getProperty("ra");
        if (ra != null) baoCao(new File(ra), pass, fail, biet, bo, tong);
        System.exit(fail == 0 ? 0 : 1);
    }

    static void baoCao(File f, int pass, int fail, int biet, int bo, long tong) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("# Hồi quy người chơi thật — ").append(new Date()).append("\n\n");
        s.append(String.format("PASS %d · FAIL %d · ĐÃ BIẾT %d · BỎ QUA %d · %d giây (%s:%d)%n%n", pass, fail, biet, bo, tong, BotChucNang.HOST, BotChucNang.PORT));
        s.append("## Kịch bản\n\n| Kịch bản | Mô tả | Thời gian | PASS | FAIL | Đã biết |\n|---|---|---|---|---|---|\n");
        for (String k : THOI_GIAN.keySet()) {
            int p = 0, fl = 0, b = 0;
            synchronized (KQ) {
                for (KetQua r : KQ) if (r.kichBan.equals(k)) { if (r.trangThai.equals("PASS")) p++; else if (r.trangThai.equals("FAIL")) fl++; else if (r.trangThai.startsWith("DA_BIET")) b++; }
            }
            long[] t = THOI_GIAN.get(k);
            s.append(String.format("| %s | %s | %ds | %d | %d | %d |%n", k, String.valueOf(MO_TA.get(k)).replace("|", "/"), (t[1] - t[0]) / 1000, p, fl, b));
        }
        s.append("\n## Không đạt / đã biết\n\n| Kịch bản | Mục | Trạng thái | Bằng chứng |\n|---|---|---|---|\n");
        synchronized (KQ) {
            for (KetQua r : KQ) if (!r.trangThai.equals("PASS") && !r.trangThai.equals("BO_QUA"))
                s.append("| ").append(r.kichBan).append(" | ").append(r.muc).append(" | ").append(r.trangThai).append(" | `").append(r.bang.replace("|", "/").replace("`", "'")).append("` |\n");
        }
        s.append("\n## Chuẩn bị (ngoài đường người chơi, chỉ cấp thứ người chơi kiếm được)\n\n");
        synchronized (CHUAN_BI) { for (String c : CHUAN_BI) s.append("- ").append(c).append('\n'); }
        s.append("\n## Toàn bộ mục\n\n| Kịch bản | Mục | Trạng thái | Bằng chứng |\n|---|---|---|---|\n");
        synchronized (KQ) {
            for (KetQua r : KQ) s.append("| ").append(r.kichBan).append(" | ").append(r.muc).append(" | ").append(r.trangThai).append(" | ")
                    .append(r.bang.length() > 200 ? r.bang.substring(0, 200).replace("|", "/") + "…" : r.bang.replace("|", "/")).append(" |\n");
        }
        Files.write(f.toPath(), s.toString().getBytes(StandardCharsets.UTF_8));
    }
}
