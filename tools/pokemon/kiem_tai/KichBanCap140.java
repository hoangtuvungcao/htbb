import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

/**
 * CHOI TU NHIEN CAP 1 -> 40 (nhom cap1_40, bao loi 25/10/2026 "chơi từ lv1 đến 40 và làm all nhiệm vụ có lỗi gì không?").
 *
 * Tai khoan MOI dang ky bang duong that trong game (HoiQuy.nguoiChoiMoi, #dk2), KHONG GM, KHONG sua CSDL sau khi tao. Hai
 * nguoi choi chay song song: "lam" (chon Làm huong dan, 10 buoc) va "bo" (Bỏ qua huong dan). Moi buoc quyet dinh CHI tu cai
 * game noi voi nguoi choi: 3 danh sach -64 (co the nhan / xong / dang lam) kem duoi goi muc tieu - noi tra - ly do khoa
 * (client >= 915), hop 95 (cua ai), thong bao 37/-26. Khong doc data_quest_moi de choi (chi de doi chieu trong bao cao).
 *
 *   chinh   : nhan dung id, di toi NPC giao (cMap), lam muc tieu (ha quai / nhat do roi / ha lech cap / den vung "ô x,y"),
 *             ve NPC tra. Bi chan cap -> cay; het ve chuong / chuong chua mo -> tua dong ho ao (CHI may thu c40).
 *   phu     : moi luc mot nhiem vu phu -64 (main_sub 1) cap thap nhat.
 *   cua ai  : ban do khoa -> nhan qua hop 95 / Quay doi thuong -> Nhiem vu cua toi -> Thu thach cua ai; ha quai dung ban do;
 *             buoc Gym -> muc Thach dau Gym o Quay doi thuong; nhan thuong bang -64 nhom 1 id cua ai.
 *   tinh nang (moi bac cap): mua thuoc + vu khi bang xu kiem duoc, mac do roi, cong diem, Ve lang mien phi, ...
 *             (KichBanCap140Tinh nang: chay tung cai o moc cap mo).
 *   kiem    : BatBien gan moi phien; log server sach; moi 30 phut thoat + vao lai (luu); moi buoc ket > 15 phut ghi lai
 *             trang thai day du (id nhiem vu, map, o, 3 danh sach) vao muc "ket".
 *
 * Dong ho: nang luong 90 phut danh quai / ngay + ve chuong 2 / tuan -> nguoi choi that cho sang hom sau. Bot dieu phoi GM
 * (kt040, -DgmTk) go "tua 1d" tren MAY THU (sv.cho_phep_thu_nghiem=1) - khong dong toi nhan vat nguoi choi. Moi lan tua ghi
 * vao dong thoi gian.
 *
 * Tiep tuc (checkpoint): -Dcp=<thu muc>: luu tai khoan + dong thoi gian; chay lai cung -Dcp thi dang nhap lai tai khoan cu
 * (trang thai that cua nhan vat la checkpoint). -DgioCap140=N gio toi da moi nguoi choi, -DcapDich=40, -Dnhanh=lam,bo.
 */
final class KichBanCap140 {

    private KichBanCap140() {
    }

    static void ket(String muc, Boolean ok, String bang) {
        HoiQuy.ket(muc, ok, bang);
    }

    static void dangKy() {
        HoiQuy.dk("cap1_40_luu", "buoc nhiem vu dia diem da nhan: thoat + vao lai van dang lam", KichBanCap140::luuBuocVung);
        HoiQuy.dkCham("cap1_40", "choi tu nhien cap 1 -> " + CAP_DICH + " (khong GM): moi nhiem vu chinh/phu/cua ai, Gym, tinh nang theo cap", KichBanCap140::chay);
    }

    /**
     * Buoc dia diem (type 3 "Ra đồng cỏ Pallet, đứng đúng chỗ..."): da nhan (dang lam) -> thoat + vao lai -> van DANG LAM,
     * khong quay ve "co the nhan" (lan chay 25/10: vao lai thay nhan lai #14 'Nơi ngôi sao rơi').
     */
    static void luuBuocVung() throws Exception {
        BotChucNang.Bot gmb = HoiQuy.gm();
        HoiQuy.napTheGioi(gmb);
        napTen();
        NguoiChoi p = new NguoiChoi("luu");
        BotChucNang.Bot b0 = HoiQuy.nguoiChoiMoi("cv", 0, 0);
        p.user = b0.user;
        KichBanHoiQuy.boQuaHuongDan(b0);
        HoiQuy.thoat(b0);
        p.batDauThat = System.currentTimeMillis();
        p.vao();
        for (int moc : new int[]{5, 10, 15, 20, 25, 30, 35, 40}) p.mocTinhNang.add(moc);
        KichBanTienDoNhiemVu.Dong dang = null;
        long het = System.currentTimeMillis() + 10 * 60000L;
        while (dang == null && System.currentTimeMillis() < het) {
            p.docNv();
            for (KichBanTienDoNhiemVu.Dong q : p.l2) if (q.ms == 0 && q.loai == 3 && (q.muc + q.moTa).contains("ô ")) dang = q;
            if (dang == null) p.vong();
        }
        ket("cap1_40_luu.co_buoc_vung", dang != null, String.valueOf(dang));
        HoiQuy.thoat(p.b);
        if (dang == null) return;
        Thread.sleep(3000);
        try (BotChucNang.Bot c = HoiQuy.vaoLai(p.user)) {
            int m = c.moc();
            c.gui(-64, (byte) 1, (short) -1, (byte) 0);
            c.cho(x -> KichBanTienDoNhiemVu.ds(x, m, 2) != null, 3000);
            List<KichBanTienDoNhiemVu.Dong> l0 = KichBanTienDoNhiemVu.ds(c, m, 0), l2 = KichBanTienDoNhiemVu.ds(c, m, 2);
            final int id = dang.id;
            boolean conDang = l2 != null && l2.stream().anyMatch(q -> q.id == id && q.ms == 0);
            boolean veNhan = l0 != null && l0.stream().anyMatch(q -> q.id == id && q.ms == 0);
            ket("cap1_40_luu.vao_lai_van_dang_lam", conDang && !veNhan, "#" + id + " dang_lam=" + conDang + " co_the_nhan=" + veNhan
                    + " | CSDL " + BotChucNang.sql1("SELECT isfinish FROM tob_char_quest WHERE id_char=" + c.charDb));
            HoiQuy.thoat(c);
        }
    }

    static final int CAP_DICH = Integer.getInteger("capDich", 40);
    static volatile int SO_NGUOI_CHOI = 0;
    static final double GIO = Double.parseDouble(System.getProperty("gioCap140", "10"));
    static final File CP = new File(System.getProperty("cp", System.getProperty("java.io.tmpdir") + "/cap140"));
    /** Phut danh quai / "ngay" truoc khi cho sang ngay moi (nang luong 100% = 90 phut). */
    static final int PHUT_NGAY = Integer.getInteger("phutNgay", 85);

    // ================================================================== ten -> id
    static final Map<String, Integer> LOAD_THEO_TEN = new HashMap<>();
    static final Map<Integer, List<int[]>> NPC_CUA_LOAD = new HashMap<>(); // load -> {x, y, loai}

    static synchronized void napTen() {
        if (!LOAD_THEO_TEN.isEmpty()) return;
        for (Map.Entry<Integer, String> e : HoiQuy.TEN_MAP.entrySet()) LOAD_THEO_TEN.putIfAbsent(e.getValue().trim(), e.getKey());
        for (int mid : BotXuyenSuot.MAP_THU) {
            Integer l = BotXuyenSuot.LOAD.get(mid);
            if (l != null) NPC_CUA_LOAD.computeIfAbsent(l, KichBanHuongDan::npcCMap);
        }
    }

    /** Ban do (mapId ban sao 0) co ten {@code ten} ma di toi duoc tu {@code tu}, gan nhat; -1 = khong co. */
    static int mapTheoTen(int tu, String ten) {
        if (ten == null) return -1;
        Integer load = LOAD_THEO_TEN.get(ten.trim());
        if (load == null) return -1;
        int tot = -1, ds = Integer.MAX_VALUE;
        for (int mid : BotXuyenSuot.MAP_THU) {
            Integer l = BotXuyenSuot.LOAD.get(mid);
            if (l == null || !l.equals(load)) continue;
            List<int[]> d = BotXuyenSuot.duong(tu, mid);
            if (d != null && d.size() < ds) { ds = d.size(); tot = mid; }
        }
        return tot;
    }

    /** Mau quai theo ten, uu tien loai sinh o ban do nap {@code load}. */
    static int quaiTheoTen(String ten, int load) {
        int bat = -1;
        for (Map.Entry<Integer, String> e : HoiQuy.TEN_QUAI.entrySet()) {
            if (!e.getValue().equalsIgnoreCase(ten.trim())) continue;
            Set<Integer> maps = BotXuyenSuot.QUAI_MAP.get(e.getKey());
            if (maps == null) continue;
            for (int m : maps) if (Objects.equals(BotXuyenSuot.LOAD.get(m), load)) return e.getKey();
            if (bat < 0) bat = e.getKey();
        }
        return bat;
    }

    // ================================================================== bao cao chung
    static final List<String> LOI = Collections.synchronizedList(new ArrayList<>());      // loi game (nghi la bug)
    static final List<String> CHU_NV = Collections.synchronizedList(new ArrayList<>());   // van de chu nhiem vu

    static void loi(String s) {
        synchronized (LOI) { if (!LOI.contains(s)) LOI.add(s); }
        System.out.println("  [LOI GAME] " + s);
    }

    static void chuNv(String s) {
        synchronized (CHU_NV) { if (!CHU_NV.contains(s)) CHU_NV.add(s); }
        System.out.println("  [CHU NV] " + s);
    }

    // ================================================================== chay
    static void chay() throws Exception {
        CP.mkdirs();
        BotChucNang.Bot dp = HoiQuy.gm(); // CHI tua dong ho ao (may thu); khong dung toi nhan vat nguoi choi
        HoiQuy.napTheGioi(dp);
        napTen();
        BotXuyenSuot.dieuPhoi = dp;
        List<NguoiChoi> ds = new ArrayList<>();
        for (String n : System.getProperty("nhanh", "lam,bo").split(",")) ds.add(new NguoiChoi(n.trim()));
        SO_NGUOI_CHOI = ds.size();
        List<Thread> th = new ArrayList<>();
        for (NguoiChoi p : ds) {
            Thread t = new Thread(() -> {
                HoiQuy.KB_HIEN.set("cap1_40");
                p.chayAnToan();
            }, "cap140-" + p.nhanh);
            th.add(t);
            t.start();
            Thread.sleep(20000);
        }
        // giu bot dieu phoi song (doc goi) trong luc cho
        while (th.stream().anyMatch(Thread::isAlive)) {
            synchronized (BotXuyenSuot.KHOA_DH) {
                if (BotXuyenSuot.dieuPhoi != null) {
                    BotXuyenSuot.dieuPhoi.bom(200);
                    BotXuyenSuot.dieuPhoi.su.clear();
                    if (!BotXuyenSuot.dieuPhoi.conSong()) {
                        HoiQuy.gm = null;
                        try { BotXuyenSuot.dieuPhoi = HoiQuy.gm(); } catch (Exception e) { BotXuyenSuot.dieuPhoi = null; }
                    }
                }
            }
            Thread.sleep(5000);
        }
        for (NguoiChoi p : ds) p.ketQua();
        ket("cap1_40.khong_loi_game", LOI.isEmpty(), LOI.size() + ": " + String.join(" || ", LOI.subList(0, Math.min(8, LOI.size()))));
        ket("cap1_40.chu_nhiem_vu", CHU_NV.isEmpty(), CHU_NV.size() + ": " + String.join(" || ", CHU_NV.subList(0, Math.min(8, CHU_NV.size()))));
        baoCao(ds);
    }

    // ================================================================== mot nguoi choi
    static final class NguoiChoi extends BotXuyenSuot.Dong {
        final String nhanh;
        final File tepCp;
        final List<String> dongThoiGian = new ArrayList<>(); // "cap|phut that|ngay ao|nv chinh|phu|cua ai|xu|chet"
        final List<String> ketChi = new ArrayList<>();
        final Map<Integer, Long> capLuc = new TreeMap<>();
        long batDauThat;
        double phutCongDon; // phut that da choi o cac lan chay truoc (checkpoint)
        int soPhu, soCuaAi, soNgayAo, soLanVaoLai, soGymThang;
        long xuKiem, xuTieu, xuTruoc = -1;
        int capChan = 0;               // "Cần đạt cấp N" cua nhiem vu chinh
        final Set<Integer> phuBo = new HashSet<>();  // nhiem vu phu khong lam duoc (ghi ly do)
        final Map<String, Integer> lanKet = new HashMap<>();
        long phutDanhNgay;             // ms danh quai tu lan tua cuoi
        long lanVaoLai = System.currentTimeMillis();
        String nvChinhTen = "";
        int nvChinhIdCuoi = -1;
        final Set<Integer> chinhDaXong = new TreeSet<>();
        final Set<Integer> phuDaXong = new TreeSet<>();
        String trangThai = "";
        boolean coBong, daGiaoDich;
        int soBat;
        final Map<String, String> tinhNangKq = new LinkedHashMap<>();
        final Set<Integer> mocTinhNang = new TreeSet<>();

        void tinhNang(String ten, String kq) {
            tinhNangKq.put(ten + " [cấp " + (b == null ? 0 : b.lv) + "]", kq);
            ghi("TINH NANG " + ten + ": " + kq);
        }

        NguoiChoi(String nhanh) {
            super(0, null, Long.MAX_VALUE);
            this.nhanh = nhanh;
            this.tepCp = new File(CP, "cap140_" + nhanh + ".txt");
        }

        String user;

        @Override
        void ghi(String s) {
            String d = String.format("[%s %s %s lv%d map%d] %s", nhanh, user, new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()),
                    b == null ? 0 : b.lv, b == null ? -1 : b.mapId, s);
            System.out.println(d);
            synchronized (nhatKy) { nhatKy.add(d); if (nhatKy.size() > 600) nhatKy.remove(0); }
        }

        // ---------------------------------------------------------------- checkpoint
        void docCp() throws IOException {
            if (!tepCp.isFile()) return;
            for (String l : Files.readAllLines(tepCp.toPath(), StandardCharsets.UTF_8)) {
                if (l.startsWith("user=")) user = l.substring(5);
                else if (l.startsWith("phut=")) phutCongDon = Double.parseDouble(l.substring(5));
                else if (l.startsWith("ngayAo=")) soNgayAo = Integer.parseInt(l.substring(7));
                else if (l.startsWith("tg=")) dongThoiGian.add(l.substring(3));
                else if (l.startsWith("cap=")) { String[] f = l.substring(4).split(":"); capLuc.put(Integer.parseInt(f[0]), Long.parseLong(f[1])); }
                else if (l.startsWith("chinh=")) for (String x : l.substring(6).split(",")) if (!x.isEmpty()) chinhDaXong.add(Integer.parseInt(x));
                else if (l.startsWith("phu=")) for (String y : l.substring(4).split(",")) if (!y.isEmpty()) phuDaXong.add(Integer.parseInt(y));
                else if (l.startsWith("ket=")) ketChi.add(l.substring(4));
                else if (l.startsWith("gd=")) daGiaoDich = l.endsWith("1");
                else if (l.startsWith("moc=")) for (String z : l.substring(4).split(",")) if (!z.isEmpty()) mocTinhNang.add(Integer.parseInt(z));
                else if (l.startsWith("tn=")) { String[] f = l.substring(3).split("\t", 2); if (f.length == 2) tinhNangKq.put(f[0], f[1]); }
                else if (l.startsWith("so=")) {
                    String[] f = l.substring(3).split(",");
                    soCuaAi = Integer.parseInt(f[0]); soChet = Integer.parseInt(f[1]); xuKiem = Long.parseLong(f[2]); xuTieu = Long.parseLong(f[3]);
                    soGymThang = Integer.parseInt(f[4]); soLanVaoLai = Integer.parseInt(f[5]);
                }
            }
        }

        synchronized void luuCp() {
            StringBuilder s = new StringBuilder();
            s.append("user=").append(user).append('\n');
            s.append("phut=").append(String.format(Locale.ROOT, "%.1f", phutThat())).append('\n');
            s.append("ngayAo=").append(soNgayAo).append('\n');
            s.append("so=").append(soCuaAi).append(',').append(soChet).append(',').append(xuKiem).append(',').append(xuTieu).append(',').append(soGymThang).append(',').append(soLanVaoLai).append('\n');
            for (Map.Entry<Integer, Long> e : capLuc.entrySet()) s.append("cap=").append(e.getKey()).append(':').append(e.getValue()).append('\n');
            s.append("chinh=").append(chinhDaXong.toString().replaceAll("[\\[\\] ]", "")).append('\n');
            s.append("phu=").append(phuDaXong.toString().replaceAll("[\\[\\] ]", "")).append('\n');
            for (String t : dongThoiGian) s.append("tg=").append(t).append('\n');
            s.append("gd=").append(daGiaoDich ? 1 : 0).append('\n');
            s.append("moc=").append(mocTinhNang.toString().replaceAll("[\\[\\] ]", "")).append('\n');
            for (Map.Entry<String, String> e : tinhNangKq.entrySet()) s.append("tn=").append(e.getKey().replace('\t', ' ').replace('\n', ' ')).append('\t').append(e.getValue().replace('\n', ' ')).append('\n');
            for (String t : ketChi) s.append("ket=").append(t.replace('\n', ' ')).append('\n');
            try {
                Files.write(tepCp.toPath(), s.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                ghi("khong luu checkpoint: " + e);
            }
        }

        double phutThat() {
            return phutCongDon + (System.currentTimeMillis() - batDauThat) / 60000.0;
        }

        // ---------------------------------------------------------------- vao game
        @Override
        void vao() throws Exception {
            Exception cuoi = null;
            for (int lan = 0; lan < 40; lan++) {
                try {
                    b = HoiQuy.vaoLai(user);
                    break;
                } catch (Exception e) {
                    cuoi = e;
                    Thread.sleep(6000);
                }
            }
            if (b == null || b.dut) throw cuoi == null ? new IllegalStateException("khong vao lai duoc " + user) : cuoi;
            HoiQuy.napTheGioi(b);
            KichBanHuongDan.roiSan(b);
            b.lamMoi();
            capTruoc = b.lv;
            ptTruoc = b.pt;
            lanVaoLai = System.currentTimeMillis();
            ghi("vao game map " + b.mapId + " (" + b.tenMap + ") xu " + b.xu);
        }

        void chayAnToan() {
            try {
                chay();
            } catch (Throwable e) {
                loi.add("ngoai le bot: " + e);
                ghi("NGOAI LE " + e);
                e.printStackTrace(System.out);
            } finally {
                luuCp();
                if (b != null) try { HoiQuy.thoat(b); } catch (Exception ignored) { }
            }
        }

        void chay() throws Exception {
            docCp();
            batDauThat = System.currentTimeMillis();
            if (user == null) {
                // tai khoan MOI bang duong dang ky that; nhan vat moi
                BotChucNang.Bot b0 = HoiQuy.nguoiChoiMoi(nhanh.equals("lam") ? "cl" : "cb", 0, 0);
                user = b0.user;
                b = b0;
                luuCp();
                huongDan();
                HoiQuy.thoat(b);
                b = null;
            } else {
                ghi("TIEP TUC tu checkpoint: " + tepCp + " (" + String.format(Locale.ROOT, "%.0f", phutCongDon) + " phut da choi)");
            }
            vao();
            capDau = b.lv;
            long hetGio = System.currentTimeMillis() + (long) (GIO * 3600000L - phutCongDon * 60000L);
            while (System.currentTimeMillis() < hetGio && !xong) {
                try {
                    vong();
                } catch (java.net.SocketException | EOFException e) {
                    soVaoLai++;
                    ghi("mat ket noi (" + e + ") -> vao lai");
                    b.close();
                    Thread.sleep(3000);
                    vao();
                }
                if (b.lv >= CAP_DICH && chinhHetCap()) { xong = true; ghi("XONG: cap " + b.lv + ", het nhiem vu chinh trong khoang cap"); }
            }
        }

        /** Nhiem vu chinh dang moi / dang lam co cap > CAP_DICH (tuc moi nhiem vu chinh <= 40 da xong). */
        boolean chinhHetCap() {
            return capChan > CAP_DICH;
        }

        // ---------------------------------------------------------------- huong dan
        void huongDan() throws Exception {
            int m = b.moc();
            KichBanHuongDan.roiSan(b);
            boolean moi = b.doiMenu(m, KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 3000) == 2;
            ket("cap1_40." + nhanh + ".moi_huong_dan", moi, b.chuTu(m));
            if (nhanh.equals("bo")) {
                KichBanHoiQuy.boQuaHuongDan(b);
                ghi("Bo qua huong dan -> cap " + b.lv + " xu " + b.xu);
                return;
            }
            b.menu(KichBanHuongDan.NPC_SO_TAY, KichBanHuongDan.MENU_MOI, 0);
            b.bom(1000);
            KichBanHuongDan.Nv q = KichBanHuongDan.chinh(b, 0, 0);
            List<String> buoc = new ArrayList<>();
            for (int k = 0; k < KichBanHuongDan.SO_BUOC && q != null; k++) {
                String ly = KichBanHuongDan.lamBuoc(b, q, k);
                buoc.add(q.ten + (ly == null ? "" : " <" + ly + ">"));
                if (ly != null) { ketLai("huong dan buoc " + (k + 1) + " '" + q.ten + "': " + ly); break; }
                KichBanHuongDan.Nv sau = KichBanHuongDan.chinh(b, 0, 0);
                if (sau == null || sau.id != q.id + 1) break;
                q = sau;
            }
            ghi("huong dan: " + String.join(" | ", buoc));
            ket("cap1_40.lam.huong_dan_10_buoc", buoc.size() == KichBanHuongDan.SO_BUOC && buoc.stream().noneMatch(x -> x.contains("<")), String.join(" | ", buoc));
        }

        // ---------------------------------------------------------------- doc nhiem vu
        List<KichBanTienDoNhiemVu.Dong> l0, l1, l2;

        /** Lam moi 3 danh sach (goi tra id sai -> may chu gui lai ca 3, Char.tuChoiNhiemVu(null)). */
        void docNv() throws IOException {
            int m = b.moc();
            b.gui(-64, (byte) 1, (short) -1, (byte) 0);
            b.cho(x -> KichBanTienDoNhiemVu.ds(x, m, 2) != null, 3000);
            l0 = KichBanTienDoNhiemVu.ds(b, m, 0);
            l1 = KichBanTienDoNhiemVu.ds(b, m, 1);
            l2 = KichBanTienDoNhiemVu.ds(b, m, 2);
            if (l0 == null) l0 = new ArrayList<>();
            if (l1 == null) l1 = new ArrayList<>();
            if (l2 == null) l2 = new ArrayList<>();
        }

        /** Thoat nhu nguoi choi tat game (KHONG chay BatBien.kiemKet: no ve lang / dung luot Ve lang mien phi). */
        void thoatGiuNguyen() throws InterruptedException {
            b.close();
            Thread.sleep(2500);
        }

        String dsChu() {
            return "co_the_nhan=" + l0 + " ; xong=" + l1 + " ; dang_lam=" + l2;
        }

        // ---------------------------------------------------------------- di toi NPC
        /** Di toi NPC loai {@code npc}; {@code tra} = chu "Machamp (Pewter)" (ban do trong ngoac). null = toi noi. */
        String toiNpc(int npc, String tra) throws Exception {
            if (npc < 0) return null;
            if (KichBanDS2.laOffline(b.mapId)) KichBanDS2.raNgoai(b);
            if (KichBanHuongDan.oNpc(b.mapLoad, npc) == null) {
                String tenBd = null;
                Matcher mm = Pattern.compile("\\(([^)]+)\\)\\s*$").matcher(tra == null ? "" : tra.trim());
                if (mm.find()) tenBd = mm.group(1);
                int dich = -1;
                if (tenBd != null) dich = mapTheoTen(b.mapId, tenBd);
                if (dich < 0 || KichBanHuongDan.oNpc(BotXuyenSuot.LOAD.getOrDefault(dich, -1), npc) == null) {
                    // ban do khong ghi / ghi sai: NPC loai nay o ban do gan nhat (lang ve truoc)
                    int ds = Integer.MAX_VALUE;
                    for (int mid : BotXuyenSuot.MAP_THU) {
                        Integer l = BotXuyenSuot.LOAD.get(mid);
                        if (l == null || KichBanHuongDan.oNpc(l, npc) == null) continue;
                        List<int[]> d = BotXuyenSuot.duong(b.mapId, mid);
                        int s = d == null ? Integer.MAX_VALUE : d.size() - (KichBanDS.laLang(mid) ? 1 : 0);
                        if (s < ds) { ds = s; dich = mid; }
                    }
                    if (tenBd != null && dich >= 0) chuNv("NPC loai " + npc + " ghi '" + tra + "' nhung khong co o ban do '" + tenBd + "' (co o " + HoiQuy.TEN_MAP.get(BotXuyenSuot.LOAD.get(dich)) + ")");
                }
                if (dich < 0) return "khong ban do nao co NPC loai " + npc;
                if (!toi(dich)) return "khong di toi ban do " + dich + " co NPC " + npc;
            }
            return KichBanHuongDan.toiNpc(b, npc);
        }

        // ---------------------------------------------------------------- vong choi
        void vong() throws Exception {
            BotXuyenSuot.quetLog();
            b.lamMoi();
            kiem();
            ghiCap();
            if (b.su.size() > 20000) b.su.subList(0, 15000).clear(); // gioi han bo nho (moc cu khong con dung)
            if (System.currentTimeMillis() - lanVaoLai > 30 * 60000L) { vaoLaiCheckpoint(); return; }
            for (int moc : new int[]{5, 10, 15, 20, 25, 30, 35, 40}) {
                if (b.lv >= moc && mocTinhNang.add(moc)) {
                    luuCp();
                    if (moc < 40 && b.lv >= moc + 5) continue; // nhay qua nhieu moc cung luc: chi chay moc cao nhat
                    KichBanCap140TinhNang.chay(this);
                    return;
                }
            }
            hop95(0);
            docNv();
            if (traXong()) return;
            if (nhanMoi()) return;
            muaThuoc();
            macDoTot();
            if (lamDangLam()) return;
            nvPhu();
            if (capChan > b.lv) { luyen(4); return; }
            String bt = BotChucNang.khongDau(trangThai);
            if (bt.contains("ve chuong") || bt.contains("mo sau")) { choNgayMoi("chinh tuyen: " + trangThai); return; }
            if (l0.isEmpty() && l2.isEmpty() && l1.isEmpty()) ketLai("khong co nhiem vu nao (chinh/phu/cua ai) o cap " + b.lv + " | " + trangThai);
            luyen(3);
        }

        void ghiCap() {
            if (!capLuc.containsKey(b.lv)) {
                capLuc.put(b.lv, Math.round(phutThat()));
                String d = String.format(Locale.ROOT, "%d|%.0f|%d|%d|%d|%d|%d|%d|%d", b.lv, phutThat(), soNgayAo, chinhDaXong.size(), phuDaXong.size(), soCuaAi, b.xu, xuTieu, soChet);
                dongThoiGian.add(d);
                ghi("MOC CAP " + b.lv + ": " + d);
                luuCp();
            }
        }

        void vaoLaiCheckpoint() throws Exception {
            soLanVaoLai++;
            int lv = b.lv;
            long xu = b.xu;
            int map = b.mapId;
            luuCp();
            thoatGiuNguyen();
            vao();
            if (b.lv != lv || b.xu != xu) loi("vao lai mat du lieu: cap " + lv + "->" + b.lv + " xu " + xu + "->" + b.xu + " (map " + map + "->" + b.mapId + ")");
            ghi("checkpoint: thoat + vao lai (lan " + soLanVaoLai + ")");
        }

        /** Ghi mot cho ket (dem lan); > 3 lan cung ly do -> vao ketChi kem trang thai day du. */
        void ketLai(String ly) {
            int n = lanKet.merge(ly, 1, Integer::sum);
            ghi("KET (" + n + "): " + ly);
            if (n == 3) {
                String s = ly + " || cap " + (b == null ? 0 : b.lv) + " map " + (b == null ? -1 : b.mapId) + " o " + (b == null ? "" : b.px / 16 + "," + b.py / 16)
                        + " || " + (l0 == null ? "" : dsChu());
                ketChi.add(s);
                ketLyDo(ly);
            }
        }

        // ---------------------------------------------------------------- hop 95 (cua ai)
        int moc95 = 0;

        void hop95(int tu) throws IOException {
            hop95(tu, false);
        }

        /** Hop 95: Dong y (nhan cua ai / xem tien do). "Thách đấu Trưởng Gym … ngay?" chi Dong y khi dang di danh Gym. */
        void hop95(int tu, boolean nhanGym) throws IOException {
            for (int i = Math.max(tu, moc95); i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -32 && e.so != null && e.so[1] == 95) {
                    boolean gym = e.chu.contains("Thách đấu Trưởng Gym") && e.chu.contains("ngay?");
                    ghi("hop 95" + (gym && !nhanGym ? " (Gym: de sau)" : "") + ": " + e.chu.replace('\n', ' '));
                    b.gui(-32, (short) e.so[0], (byte) 95, "", (byte) (gym && !nhanGym ? 0 : 1));
                    b.bom(1200);
                }
            }
            moc95 = b.su.size();
        }

        // ---------------------------------------------------------------- tra nhiem vu xong
        boolean traXong() throws Exception {
            for (KichBanTienDoNhiemVu.Dong q : l1) {
                long xu0 = b.xu;
                int lv0 = b.lv;
                if (q.ms == 0 || q.ms == 1) {
                    String l = toiNpc(q.npc, q.tra);
                    if (l != null) { ketLai("tra '" + q.ten + "' #" + q.id + ": " + l + " (chu tra: " + q.tra + ")"); continue; }
                }
                int m = b.moc();
                b.gui(-64, (byte) 1, (short) q.id, (byte) q.ms);
                b.bom(1500);
                docNv();
                boolean conXong = l1.stream().anyMatch(x -> x.id == q.id && x.ms == q.ms);
                String t = b.chuTu(m);
                if (conXong) { ketLai("tra '" + q.ten + "' #" + q.id + "/" + q.ms + " khong duoc: " + t); continue; }
                b.lamMoi();
                if (q.ms == 0) { chinhDaXong.add(q.id); nvChinhIdCuoi = q.id; soNvChinh++; }
                else if (q.ms == 1) { phuDaXong.add(q.id); soPhu++; }
                else soCuaAi++;
                lanTienTrien = System.currentTimeMillis();
                ghi("TRA " + (q.ms == 0 ? "chinh" : q.ms == 1 ? "phu" : "cua ai") + " #" + q.id + " '" + q.ten + "' xu +" + (b.xu - xu0) + " cap " + lv0 + "->" + b.lv);
                if (q.ms == 0 && b.xu == xu0 && b.lv == lv0 && !t.contains("Nhận") && !t.contains("EXP") && !t.contains("exp")) ghi("  (tra khong thay thuong: " + t + ")");
                return true;
            }
            return false;
        }

        // ---------------------------------------------------------------- nhan nhiem vu moi
        boolean nhanMoi() throws Exception {
            boolean coPhuDang = l2.stream().anyMatch(x -> x.ms == 1);
            for (KichBanTienDoNhiemVu.Dong q : l0) {
                if (q.ms == 1 && (coPhuDang || phuBo.contains(q.id))) continue;
                if (q.ms == 0 && capChan > b.lv) continue;
                String l = toiNpc(q.npc, q.tra);
                if (l != null) { ketLai("nhan '" + q.ten + "' #" + q.id + ": " + l); if (q.ms == 1) phuBo.add(q.id); continue; }
                int m = b.moc();
                boolean noiChuyen = q.loai == 3;
                b.gui(-64, (byte) (noiChuyen ? 1 : 0), (short) q.id, (byte) q.ms);
                b.bom(1500);
                String t = b.chuTu(m);
                docNv();
                boolean conMoi = l0.stream().anyMatch(x -> x.id == q.id && x.ms == q.ms);
                if (q.ms == 0) trangThai = t;
                if (conMoi) {
                    Matcher mc = Pattern.compile("Cần đạt cấp (\\d+)").matcher(t);
                    if (q.ms == 0 && mc.find()) { capChan = Integer.parseInt(mc.group(1)); ghi("chinh '" + q.ten + "' can cap " + capChan); continue; }
                    String bt = BotChucNang.khongDau(t);
                    if (q.ms == 0 && (bt.contains("ve chuong") || bt.contains("mo sau"))) { choNgayMoi("nhan '" + q.ten + "': " + t); return true; }
                    if (q.ms == 1) { phuBo.add(q.id); ghi("phu '" + q.ten + "' khong nhan duoc: " + t); continue; }
                    ketLai("nhan '" + q.ten + "' #" + q.id + " khong duoc: " + t);
                    continue;
                }
                if (q.ms == 0) { capChan = 0; nvChinhTen = q.ten; }
                if (noiChuyen && !l2.stream().anyMatch(x -> x.id == q.id)) {
                    if (q.ms == 0) { chinhDaXong.add(q.id); soNvChinh++; } else if (q.ms == 1) { phuDaXong.add(q.id); soPhu++; }
                    ghi("NOI CHUYEN xong " + (q.ms == 0 ? "chinh" : "phu") + " #" + q.id + " '" + q.ten + "'");
                } else ghi("NHAN " + (q.ms == 0 ? "chinh" : "phu") + " #" + q.id + " '" + q.ten + "' t" + q.loai);
                lanTienTrien = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        // ---------------------------------------------------------------- lam muc tieu
        boolean lamDangLam() throws Exception {
            List<KichBanTienDoNhiemVu.Dong> thuTu = new ArrayList<>();
            for (int ms : new int[]{0, 2, 1}) for (KichBanTienDoNhiemVu.Dong q : l2) if (q.ms == ms) thuTu.add(q);
            for (KichBanTienDoNhiemVu.Dong q : thuTu) {
                String chu = (q.muc.isEmpty() ? q.moTa : q.muc + " " + q.moTa);
                String kq = lamMot(q, chu);
                if (kq == null) return true;
                if (!kq.isEmpty()) {
                    ketLai((q.ms == 0 ? "chinh" : q.ms == 1 ? "phu" : "cua ai") + " '" + q.ten + "' #" + q.id + ": " + kq + " | muc: " + chu + (q.khoa.isEmpty() ? "" : " | khoa: " + q.khoa));
                    if (q.ms == 1 && lanKet.getOrDefault(kq, 0) >= 3) huyPhu(q);
                }
            }
            return false;
        }

        void huyPhu(KichBanTienDoNhiemVu.Dong q) throws IOException {
            phuBo.add(q.id);
            b.gui(-64, (byte) 2, (short) q.id, (byte) 1);
            b.bom(1000);
            ghi("huy phu '" + q.ten + "' (khong lam duoc)");
        }

        final Set<Integer> daGhiChu = new HashSet<>();
        /** Ban do luyen da bi chan (khoa cap / cua ai) o cap hien tai - mapLuyen bo qua; xoa khi len cap. */
        final Set<Integer> mapKhoa = new HashSet<>();
        int capMapKhoa;

        @Override
        int mapLuyen() {
            if (capMapKhoa != b.lv) { mapKhoa.clear(); capMapKhoa = b.lv; }
            int tot = -1, diem = Integer.MAX_VALUE;
            for (int mid : BotXuyenSuot.MAP_THU) {
                Integer l = BotXuyenSuot.LOAD.get(mid);
                if (l == null || KichBanDS2.laOffline(mid) || mapKhoa.contains(mid)) continue;
                for (int t : BotXuyenSuot.luoiQuai(l)) {
                    Integer c = BotXuyenSuot.CAP_QUAI.get(t);
                    if (c == null || c - b.lv < -3 || c - b.lv > 3) continue;
                    List<int[]> d = BotXuyenSuot.duong(b.mapId, mid);
                    if (d == null) continue;
                    int sc = d.size() * 2 + Math.abs(c - b.lv);
                    if (sc < diem) { diem = sc; tot = mid; }
                }
            }
            return tot;
        }

        @Override
        void luyen(int phut) throws Exception {
            int mid = mapLuyen();
            if (mid < 0) { ketLai("khong con ban do luyen mo cho cap " + b.lv + " (da khoa: " + mapKhoa + ")"); b.bom(5000); return; }
            if (!toi(mid)) { mapKhoa.add(mid); hop95(0); return; }
            danh(-1, 40, phut * 60000L);
        }
        final Map<Integer, Integer> lanToiDich = new HashMap<>();

        /**
         * Ban do ghi trong chu muc tieu: ten ban do (data_map_ten) xuat hien trong chu - uu tien ten ngay truoc "(ô x,y" ,
         * roi sau " ở " / " trên " / "Đến ", roi ten dai nhat. Bo qua ten trong phan "Trả:" / "Nhận thưởng ở".
         */
        String tenBanDoTrong(String chu) {
            String c = chu;
            for (String cat : new String[]{" · Trả", "Trả:", "Nhận thưởng ở", "Nhận thưởng"}) { int k = c.indexOf(cat); if (k > 0) c = c.substring(0, k); }
            String tot = null;
            int diem = -1;
            for (String ten : LOAD_THEO_TEN.keySet()) {
                if (ten.length() < 3) continue;
                int k = c.indexOf(ten);
                if (k < 0) continue;
                // ten phai la tu tron (khong phai mot phan cua tu khac)
                int e = k + ten.length();
                if (e < c.length() && Character.isLetter(c.charAt(e))) continue;
                int d = ten.length();
                String sau = c.substring(e);
                String truoc = c.substring(0, k);
                if (sau.matches("^\\s*\\(ô.*") || sau.matches("^,? ?\\(?ô \\d.*")) d += 1000;
                if (truoc.endsWith(" ở ") || truoc.endsWith(" trên ") || truoc.endsWith("Đến ") || truoc.endsWith("đồng cỏ ")) d += 500;
                if (d > diem) { diem = d; tot = ten; }
            }
            return tot;
        }

        /** null = da lam gi do; "" = khong co gi de lam (cho tra); chuoi = ly do ket. */
        String lamMot(KichBanTienDoNhiemVu.Dong q, String chu) throws Exception {
            String tenBd = tenBanDoTrong(chu);
            if (daGhiChu.add(q.id * 10 + q.ms)) ghi("muc tieu #" + q.id + "/" + q.ms + " '" + q.ten + "' t" + q.loai + ": " + chu + " -> ban do '" + tenBd + "'");
            if (q.loai == 1) return "";
            if (q.loai == 3 && !Pattern.compile("ô (\\d+)[,;] ?(\\d+)").matcher(chu).find()) {
                String hd = buocHanhDong(q, chu, tenBd);
                if (hd != null) return hd.equals("-") ? null : hd;
            }
            if (q.loai == 3) {
                Matcher mo = Pattern.compile("ô (\\d+)[,;] ?(\\d+)").matcher(chu);
                if (!mo.find()) {
                    if (q.ms == 2 && chu.contains("Gym")) return gymCuaAi(q, chu);
                    return BotChucNang.khongDau(chu).contains("noi chuyen") ? "" : "buoc noi chuyen dang lam khong ghi o dich: " + chu;
                }
                int dich = tenBd == null ? b.mapId : mapTheoTen(b.mapId, tenBd);
                if (dich < 0) return "khong biet ban do '" + tenBd + "'";
                if (!toi(dich)) return "khong toi duoc " + tenBd;
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                int x = Integer.parseInt(mo.group(1)), y = Integer.parseInt(mo.group(2));
                int[] o = bd == null ? null : bd.oGan(x, y, 2);
                KichBanHuongDan.dongBoViTri(b);
                if (o == null || !b.diDuong(bd, o[0], o[1])) return "khong di bo toi o " + x + "," + y + " o " + tenBd;
                b.bom(1500);
                int lan = lanToiDich.merge(q.id * 10 + q.ms, 1, Integer::sum);
                ghi("toi dich '" + q.ten + "' o " + x + "," + y + " map " + b.mapId + " (lan " + lan + ")");
                if (lan >= 3) {
                    if (lan == 3) loi("dung o dich " + x + "," + y + " (" + tenBd + ", map " + b.mapId + ") 3 lan ma '" + q.ten + "' #" + q.id + " khong xong: " + chu);
                    if (lan > 6) return "da dung o dich nhung khong xong";
                    // thu dung dung o (khong lan can) + di qua lai trong vung
                    KichBanHuongDan.dongBoViTri(b);
                    if (bd.diDuoc(x, y)) b.diDuong(bd, x, y);
                    b.bom(1500);
                }
                return null;
            }
            if (q.loai == 4) {
                // "chênh không quá N cấp": quai ban do ghi trong chu (neu co) co the da thap hon cap minh > N -> khong tinh.
                // Nguoi choi doc chu "chênh không quá 5 cấp" -> tim ban do quai ngang cap (mapLuyen: lech <= 3).
                Matcher mc = Pattern.compile("chênh không quá (\\d+) cấp").matcher(chu);
                int lech = mc.find() ? Integer.parseInt(mc.group(1)) : 5;
                int dich = tenBd == null ? -1 : mapTheoTen(b.mapId, tenBd);
                if (dich >= 0) {
                    boolean coNgangCap = false;
                    for (int t : BotXuyenSuot.luoiQuai(BotXuyenSuot.LOAD.getOrDefault(dich, -1))) { Integer c = BotXuyenSuot.CAP_QUAI.get(t); if (c != null && Math.abs(c - b.lv) <= lech) coNgangCap = true; }
                    if (!coNgangCap) { chuNv("'" + q.ten + "': chu ghi " + tenBd + " nhung quai o do lech > " + lech + " cap voi cap " + b.lv + " (khong tinh)"); dich = -1; }
                }
                if (dich < 0) dich = mapLuyen();
                if (dich >= 0 && !toi(dich)) { mapKhoa.add(dich); hop95(0); return "khong toi duoc ban do luyen " + dich + " (thu ban do khac)"; }
                lechDuoi = lech;
                int ha;
                try { ha = danh(-1, 10, 3 * 60000L); } finally { lechDuoi = 999; }
                ghi("'" + q.ten + "' ha lech cap " + ha);
                return ha > 0 ? null : "khong ha duoc quai nao o " + b.mapId;
            }
            for (int[] t : q.td) {
                if (t[1] >= t[2]) continue;
                int tpl;
                if (q.loai == 2) {
                    // "Thu 5 Gói hàng (rơi từ Litwick) ở Cerulean" (scontent) hoac "Hạ Caterpie ở Pallet, nhặt 4 Mảnh đá" (muc)
                    Matcher mr = Pattern.compile("rơi từ ([^)]+)\\)").matcher(chu);
                    Matcher mh = Pattern.compile("Hạ (?:\\d+ )?([^,·.]+?) ở ").matcher(chu);
                    String tenQ = mr.find() ? mr.group(1) : mh.find() ? mh.group(1) : null;
                    if (tenQ == null) return "thu thap khong ghi quai roi: " + chu;
                    int load = tenBd == null ? -1 : LOAD_THEO_TEN.getOrDefault(tenBd, -1);
                    tpl = quaiTheoTen(tenQ, load);
                    if (tpl < 0) return "khong biet quai '" + tenQ + "'";
                } else tpl = t[0];
                int dich = tenBd == null ? -1 : mapTheoTen(b.mapId, tenBd);
                Set<Integer> co = BotXuyenSuot.QUAI_MAP.get(tpl);
                final int dichF = dich;
                if (dich >= 0 && co != null && co.stream().noneMatch(x -> Objects.equals(BotXuyenSuot.LOAD.get(x), BotXuyenSuot.LOAD.get(dichF))))
                    chuNv("'" + q.ten + "': " + HoiQuy.TEN_QUAI.get(tpl) + " ghi o " + tenBd + " nhung khong sinh o do");
                if (dich < 0 && co != null) {
                    int ds = Integer.MAX_VALUE;
                    for (int mid : co) { List<int[]> d = BotXuyenSuot.duong(b.mapId, mid); if (d != null && d.size() < ds) { ds = d.size(); dich = mid; } }
                }
                if (dich < 0) return "khong biet ban do cua quai " + tpl + " (" + HoiQuy.TEN_QUAI.get(tpl) + ")";
                Integer cq = BotXuyenSuot.CAP_QUAI.get(tpl);
                if (cq != null && cq > b.lv + 5) { ghi("quai " + HoiQuy.TEN_QUAI.get(tpl) + " cap " + cq + " > cap minh " + b.lv + " + 5: luyen truoc"); luyen(4); return null; }
                if (!toi(dich)) {
                    hop95(0);
                    nhanCuaAiNeuKhoa();
                    return "khong toi duoc ban do " + tenBd + " (" + dich + ")";
                }
                // thu thap: so lan HA khong bang so vat pham (roi theo ti le) -> moi luot ha 10 con roi doc lai tien do
                int ha = danh(tpl, q.loai == 2 ? 10 : Math.min(10, t[2] - t[1]), 4 * 60000L);
                ghi("'" + q.ten + "' ha " + ha + " " + HoiQuy.TEN_QUAI.get(tpl) + " (" + t[1] + "/" + t[2] + ")");
                if (ha == 0) return "khong ha duoc " + HoiQuy.TEN_QUAI.get(tpl) + " o " + b.mapId;
                return null;
            }
            return "";
        }

        final Map<Integer, Integer> lanHanhDong = new HashMap<>();

        /**
         * Buoc noi chuyen co hanh dong kiem that (nhanh sua-buoc-hanh-dong, kiem_hanh_dong=1): chu muc tieu
         * "Mặc 1 món trang bị" / "Nâng 1 món trang bị lên +2 ở chỗ Machamp" / "Hạ 1 Skorupi vào ban đêm (19:00–5:59) ở Saffron"
         * / "Hạ 26 Gloom trong 8 phút ở Fuchsia". null = khong phai buoc hanh dong; "-" = da lam; chuoi khac = ly do ket.
         */
        String buocHanhDong(KichBanTienDoNhiemVu.Dong q, String chu, String tenBd) throws Exception {
            int lan = lanHanhDong.merge(q.id * 10 + q.ms, 1, Integer::sum);
            if (chu.contains("Mặc 1 món") || chu.contains("Mặc ") && chu.contains("trang bị") && !chu.contains("bậc")) {
                b.lamMoi();
                int[] d = b.doDung.isEmpty() ? null : b.doDung.get(0);
                if (d == null && !b.dangMac.isEmpty()) {
                    int id = b.dangMac.get(0); // thao ra roi mac lai (chu: "Tháo ra mặc lại một món cũng được tính")
                    b.gui(30, (short) id); b.bom(800); b.lamMoi();
                    d = b.timDo(x -> x[0] == id);
                }
                if (d == null) return "khong co mon nao de mac";
                int m = b.moc();
                b.gui(29, (short) d[0]);
                b.bom(1200);
                ghi("hanh dong '" + q.ten + "': mac tpl " + d[1] + " -> " + b.chuTu(m));
                return lan > 4 ? "mac do khong tinh: " + b.chuTu(m) : "-";
            }
            Matcher mn = Pattern.compile("Nâng 1 món trang bị lên \\+(\\d+)").matcher(chu);
            if (mn.find()) {
                if (lan > 12) return "nang cap khong xong sau 12 lan: " + chu;
                KichBanCap140TinhNang.veLangDiBo(this);
                String l = KichBanHuongDan.toiNpc(b, 2);
                if (l != null) return "khong toi Machamp: " + l;
                b.lamMoi();
                if (b.dangMac.isEmpty()) return "khong mac mon nao de nang";
                String t = KichBanDS3.moNangCap(b, b.dangMac.get(0), false);
                ghi("hanh dong '" + q.ten + "': nang cap -> " + t);
                if (t.contains("het da")) {
                    loi("buoc '" + q.ten + "' #" + q.id + " can nang +" + mn.group(1) + " nhung khong co da luyen kim (nguoi choi cap " + b.lv + " lay o dau?)");
                    return "het da luyen kim de nang cap";
                }
                return "-";
            }
            Matcher mh = Pattern.compile("Hạ (\\d+) ([^(]+?) (?:vào|lúc|trong|ở)").matcher(chu);
            if (mh.find() && !chu.contains("Pokémon hoang dã")) {
                String tenQ = mh.group(2).trim();
                int load = tenBd == null ? -1 : LOAD_THEO_TEN.getOrDefault(tenBd, -1);
                int tpl = quaiTheoTen(tenQ, load);
                if (tpl < 0) return "khong biet quai '" + tenQ + "'";
                int dich = tenBd == null ? -1 : mapTheoTen(b.mapId, tenBd);
                if (dich < 0) { Set<Integer> co = BotXuyenSuot.QUAI_MAP.get(tpl); if (co != null) for (int x : co) { if (BotXuyenSuot.duong(b.mapId, x) != null) { dich = x; break; } } }
                if (dich < 0) return "khong co ban do co " + tenQ;
                if (!toi(dich)) { hop95(0); nhanCuaAiNeuKhoa(); return "khong toi duoc " + tenBd; }
                int can = Integer.parseInt(mh.group(1));
                boolean khungGio = Pattern.compile("\\(\\d+:\\d+").matcher(chu).find();
                int ha = danh(tpl, Math.max(1, can), (can > 1 ? 7 : 2) * 60000L);
                ghi("hanh dong '" + q.ten + "': ha " + ha + " " + tenQ + " (lan " + lan + ")");
                if (khungGio && lan % 2 == 0) choGio("'" + q.ten + "' can dung khung gio: " + chu);
                if (lan > 14) return "ha " + tenQ + " " + lan + " lan van khong xong: " + chu;
                return "-";
            }
            return null;
        }

        /** Khung gio trong game (ngay/dem): cho 3 gio ao (may thu: tua). */
        void choGio(String ly) {
            if (System.currentTimeMillis() - lanTuaCuoi < 25000) return;
            lanTuaCuoi = System.currentTimeMillis();
            long truoc = BotXuyenSuot.lanTuaCuoi;
            BotXuyenSuot.tua("3h", nhanh + ": " + ly);
            if (BotXuyenSuot.lanTuaCuoi != truoc) ghi("CHO 3 GIO (tua 3h) vi " + ly);
        }

        /** Ban do khoa: nhan cua ai qua Quay doi thuong -> Nhiem vu cua toi -> Thu thach cua ai (neu hop 95 khong hien). */
        long lanNhanCuaAi;

        void nhanCuaAiNeuKhoa() throws IOException {
            if (System.currentTimeMillis() - lanNhanCuaAi < 3 * 60000L) return;
            lanNhanCuaAi = System.currentTimeMillis();
            int m = b.moc();
            if (!KichBanDS4.chonQuay(b, "Nhiệm vụ của tôi")) { ghi("Quay doi thuong khong co 'Nhiệm vụ của tôi'"); return; }
            b.doiMenu(m, -527, 10, 3000);
            int i = b.timMuc("Thử thách cửa ải");
            if (i < 0) { ghi("khong co muc 'Thử thách cửa ải': " + b.menuChu); return; }
            int m2 = b.moc();
            b.menu(-527, 10, i);
            b.bom(1500);
            if (b.doiMenu(m2, -527, 12, 1500) > 0 && !b.menuChu.isEmpty()) {
                int m3 = b.moc();
                b.menu(-527, 12, 0);
                b.bom(1500);
                ghi("nhan cua ai '" + b.menuChu.get(0) + "': " + b.chuTu(m3));
            } else ghi("thu thach cua ai: " + b.chuTu(m2));
            hop95(m2);
        }

        // ---------------------------------------------------------------- Gym cua ai
        final Map<String, Integer> gymThuCap = new HashMap<>();

        String gymCuaAi(KichBanTienDoNhiemVu.Dong q, String chu) throws Exception {
            if (b.lv < gymThuCap.getOrDefault(q.ten, 0) + 2) { luyen(4); return null; }
            gymThuCap.put(q.ten, b.lv);
            if (KichBanDS.laLang(b.mapId) || KichBanDS2.laOffline(b.mapId)) { int ml = mapLuyen(); if (ml >= 0) toi(ml); }
            KichBanDS.timQuai(b, 0, 260);
            Set<Integer> cu = new HashSet<>(b.quai.keySet());
            int m = b.moc();
            b.gui(23, (byte) -90);
            if (b.doiMenu(m, -90, 0, 3000) <= 0) return "khong mo duoc Quay doi thuong";
            int i = b.timMuc("Thách đấu Gym");
            if (i < 0) return "Quay doi thuong khong co muc Thach dau Gym (chu: " + chu + ") menu " + b.menuChu;
            int m2 = b.moc();
            b.menu(-90, 0, i);
            b.bom(1500);
            hop95(m2, true);
            String t = b.doiChu(m2, 3000, "xuất hiện", "cấp", "tuần", "đã", "đang thách đấu");
            ghi("Gym cua ai: " + t);
            Integer boss = KichBanDS.quaiMoi(b, cu, 8000);
            if (boss == null && b.chuTu(m2).contains("đang thách đấu")) {
                // tran cu con (boss dung o cho cu): quai maxhp lon nhat dang biet
                int mx = 0;
                for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) if (e.getValue()[4] > mx && e.getValue()[2] > 0) { mx = e.getValue()[4]; boss = e.getKey(); }
                if (mx < 2000) { boss = null; b.bom(30000); return "dang co tran Gym cu (boss o cho khac) - cho het 10 phut"; }
            }
            if (boss == null) return "khong thay boss Gym: " + b.chuTu(m2);
            long t0 = System.currentTimeMillis();
            int don = KichBanHoiQuy2.haQuaiDiBo(b, boss, 2600);
            String kq = b.doiChu(m2, 3000, "Chiến thắng", "thua", "hết giờ", "Hết giờ");
            boolean thang = kq != null && kq.contains("Chiến thắng");
            ghi("Gym '" + q.ten + "' cap " + b.lv + ": " + (thang ? "THANG" : "thua") + " " + don + " don " + (System.currentTimeMillis() - t0) / 1000 + "s | " + kq);
            gymKetQua.add(q.ten + " cap " + b.lv + ": " + (thang ? "THANG" : "thua") + " sau " + (System.currentTimeMillis() - t0) / 1000 + "s");
            if (thang) soGymThang++;
            b.lamMoi();
            if (b.hp <= 0) hoiSinh();
            return null;
        }

        // ---------------------------------------------------------------- mac do roi tot hon
        final Set<Integer> daMac = new HashSet<>();

        void macDoTot() throws IOException {
            // do trong tui co cap <= cap minh: mac (may chu tu doi cho cung o); chi mac khi cap do cao hon do dang mac cung tpl nhom
            for (int[] d : new ArrayList<>(b.doDung)) {
                if (d.length < 4 || d[3] > b.lv || d[3] <= 0 || !daMac.add(d[0])) continue; // moi mon trong tui thu mac MOT lan
                int m = b.moc();
                b.gui(29, (short) d[0]);
                b.bom(500);
                String t = b.chuTu(m);
                if (!t.isEmpty() && !t.contains("không")) ghi("mac do tpl " + d[1] + " cap " + d[3] + ": " + t);
            }
        }

        // ---------------------------------------------------------------- dong ho (cho ngay moi)
        long lanTuaCuoi;

        void choNgayMoi(String ly) {
            if (System.currentTimeMillis() - lanTuaCuoi < 30000) { b.bom(3000); return; }
            lanTuaCuoi = System.currentTimeMillis();
            long truoc = BotXuyenSuot.lanTuaCuoi;
            BotXuyenSuot.tua("1d", nhanh + ": " + ly);
            if (BotXuyenSuot.lanTuaCuoi != truoc) { soNgayAo++; phutDanhNgay = 0; ghi("CHO NGAY MOI (tua 1d) vi " + ly); }
        }

        @Override
        int danh(int t, int n, long ms) throws Exception {
            long bd = System.currentTimeMillis();
            long xu0 = b.xu;
            int ha = super.danh(t, n, ms);
            phutDanhNgay += System.currentTimeMillis() - bd;
            if (b.xu > xu0) xuKiem += b.xu - xu0;
            if (coBong && soBat < 3 && !b.hoangDa.isEmpty() && KichBanCap140TinhNang.batPokemon(this)) soBat++;
            if (phutDanhNgay > PHUT_NGAY * 60000L) choNgayMoi("het nang luong 100% (" + PHUT_NGAY + " phut danh quai)");
            return ha;
        }

        long lanMuaThuoc;
        int vuKhiMac = -1;
        static final int[] GIA_THUOC = {0, 100, 500, 800};

        /** Het thuoc: ve lang, vao tiem (nha 103), mua so binh vua tui tien (toi da 40% xu), vu khi theo bac cap. */
        @Override
        void muaThuoc() throws Exception {
            int bac = Math.min(6, Math.max(0, (b.lv - 1) / 5));
            int tplVk = 79 + 7 * lop + bac;
            boolean canVk = tplVk != vuKhiMac && b.xu > 3000;
            int co = b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3);
            if (!canVk && co >= 6 || System.currentTimeMillis() - lanMuaThuoc < 5 * 60000L || b.xu < 300) return;
            lanMuaThuoc = System.currentTimeMillis();
            long xu0 = b.xu;
            if (!KichBanDS.laLang(b.mapId)) {
                int tot = -1, ds = 999;
                for (int c : new int[]{0, 70, 80, 301, 1701, 1901}) { List<int[]> d = BotXuyenSuot.duong(b.mapId, c); if (d != null && d.size() < ds) { ds = d.size(); tot = c; } }
                if (tot < 0 || !toi(tot)) { ghi("khong ve duoc lang de mua thuoc"); return; }
            }
            if (!KichBanDS2.vaoNha(b, 103)) { ketLai("khong vao duoc nha tiem 103 o lang " + b.mapId); return; }
            int loai = b.lv >= 30 ? 3 : b.lv >= 10 ? 2 : 1;
            long ngan = b.xu * 4 / 10;
            if (ngan / GIA_THUOC[loai] < 5) loai = 1;
            int sl = (int) Math.min(20, ngan / GIA_THUOC[loai]);
            if (sl > 0) {
                int t0 = b.soThuoc(loai);
                int m = b.moc();
                b.gui(24, (byte) 1, (byte) 4, (short) loai, (short) sl);
                b.bom(1200);
                b.lamMoi();
                ghi("mua " + sl + " thuoc " + loai + ": " + t0 + " -> " + b.soThuoc(loai) + " (xu " + xu0 + " -> " + b.xu + ")");
                if (b.soThuoc(loai) < t0 + sl) loi("mua " + sl + " thuoc " + loai + " (gia " + GIA_THUOC[loai] + ", co " + xu0 + " xu) khong duoc: " + b.chuTu(m));
            }
            if (canVk && b.xu > 3000) {
                long x1 = b.xu;
                int id = KichBanDS3.muaVuKhi(b, tplVk);
                if (id != KichBanDS3.KHONG) {
                    b.gui(29, (short) id);
                    b.bom(800);
                    b.lamMoi();
                    vuKhiMac = tplVk;
                    ghi("mua + mac vu khi tpl " + tplVk + " (xu " + x1 + " -> " + b.xu + ")");
                } else vuKhiMac = tplVk; // khong mua duoc (thieu xu / het hang): khong thu lai cung bac
            }
            if (b.xu < xu0) xuTieu += xu0 - b.xu;
            KichBanDS2.raNgoai(b);
        }

        // ---------------------------------------------------------------- ket qua
        void ketQua() {
            String p = "cap1_40." + nhanh;
            int capCuoi = b == null ? capCao : Math.max(capCao, b.lv);
            ket(p + ".dat_cap_" + CAP_DICH, capCuoi >= CAP_DICH, "cap " + capDau + " -> " + capCuoi + " sau " + String.format(Locale.ROOT, "%.0f", phutThat()) + " phut that, " + soNgayAo + " ngay ao");
            ket(p + ".nhiem_vu_chinh", soNvChinh > 0 && xong, chinhDaXong.size() + " nhiem vu chinh xong (cuoi #" + nvChinhIdCuoi + "), dang '" + nvChinhTen + "', chan cap " + capChan);
            ket(p + ".nhiem_vu_phu", soPhu > 0, phuDaXong.size() + " phu, bo " + phuBo);
            ket(p + ".cua_ai", soCuaAi > 0 || capCuoi < 4, soCuaAi + " buoc cua ai; Gym thang " + soGymThang + " " + gymKetQua);
            ket(p + ".bat_pokemon", soBat > 0, soBat + " con (co bong: " + coBong + ")");
            ket(p + ".tinh_nang", !tinhNangKq.isEmpty(), tinhNangKq.size() + " muc da thu");
            ket(p + ".khong_ket", ketChi.isEmpty(), ketChi.size() + ": " + String.join(" || ", ketChi.subList(0, Math.min(4, ketChi.size()))));
            ket(p + ".khong_loi_bot", loi.isEmpty(), String.join(" || ", loi));
        }
    }

    // ================================================================== bao cao
    static void baoCao(List<NguoiChoi> ds) throws IOException {
        StringBuilder s = new StringBuilder("# Chơi tự nhiên cấp 1 → " + CAP_DICH + " — " + new Date() + "\n\n");
        for (NguoiChoi p : ds) {
            s.append("## Nhánh ").append(p.nhanh).append(" (").append(p.user).append(")\n\n");
            s.append("| Cấp | Phút thật | Ngày ảo | NV chính | NV phụ | Cửa ải | Xu | Xu đã tiêu | Chết |\n|---|---|---|---|---|---|---|---|---|\n");
            for (String d : p.dongThoiGian) s.append("| ").append(d.replace("|", " | ")).append(" |\n");
            s.append("\n### Tính năng đã dùng\n\n| Tính năng | Kết quả |\n|---|---|\n");
            for (Map.Entry<String, String> e : p.tinhNangKq.entrySet()) s.append("| ").append(e.getKey().replace("|", "/")).append(" | ").append(e.getValue().replace("|", "/")).append(" |\n");
            s.append("\nGym: ").append(p.gymKetQua).append("\n\nKẹt:\n");
            for (String k : p.ketChi) s.append("- ").append(k).append('\n');
            for (String k : p.loi) s.append("- LỖI BOT: ").append(k).append('\n');
            s.append('\n');
        }
        s.append("## Lỗi game\n\n");
        for (String l : LOI) s.append("- ").append(l).append('\n');
        s.append("\n## Chữ nhiệm vụ\n\n");
        for (String l : CHU_NV) s.append("- ").append(l).append('\n');
        s.append("\n## Lỗi server trong log\n\n");
        for (String l : new LinkedHashSet<>(BotXuyenSuot.LOI_SERVER)) s.append("- `").append(l.replace('`', '\'')).append("`\n");
        s.append("\nĐồng hồ ảo: ").append(BotXuyenSuot.NHAT_KY_TUA.size()).append(" lần tua\n");
        File f = new File(CP, "bao_cao_cap140.md");
        Files.write(f.toPath(), s.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println(s);
    }
}
