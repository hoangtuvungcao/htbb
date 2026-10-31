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
 * Dong (class 0..4 = Charmander / Bulbasaur / Eevee / Squirtle / Pichu): chu so cuoi ten nhanh, vd -Dnhanh=bo0,bo1,bo2,bo3,bo4
 * (can bang 29/10/2026: hai bot cu deu dong Lua - noi dung Nuoc danh x1,5 - khong noi duoc dong nao bi chan).
 */
final class KichBanCap140 {

    private KichBanCap140() {
    }

    static void ket(String muc, Boolean ok, String bang) {
        HoiQuy.ket(muc, ok, bang);
    }

    static void dangKy() {
        HoiQuy.dk("cap1_40_luu", "buoc nhiem vu dia diem da nhan: thoat + vao lai van dang lam", KichBanCap140::luuBuocVung);
        HoiQuy.dk("duong_thu", "in duong di giua cac ban do (-Dthu=tu:den,...) + ban do co NPC loai (-DthuNpc=24,25)", KichBanCap140::duongThu);
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

    /** Chan doan: duong di / ban do co NPC tren the gioi that (bot GM chi doc goi 105, khong lam gi). */
    static void duongThu() throws Exception {
        BotChucNang.Bot gmb = HoiQuy.gm();
        HoiQuy.napTheGioi(gmb);
        napTen();
        for (String c : System.getProperty("thu", "").split(",")) {
            if (!c.contains(":")) continue;
            int tu = Integer.parseInt(c.split(":")[0]), den = Integer.parseInt(c.split(":")[1]);
            List<int[]> d = BotXuyenSuot.duong(tu, den);
            StringBuilder s = new StringBuilder();
            if (d != null) for (int[] e : d) s.append(" ->").append(e[1]);
            System.out.println("DUONG " + tu + " -> " + den + ": " + (d == null ? "KHONG" : d.size() + " cong" + s) + " | MAP_THU co den: " + BotXuyenSuot.MAP_THU.contains(den)
                    + " load " + BotXuyenSuot.LOAD.get(den) + " hang " + BotXuyenSuot.hang(den) + " canh " + BotXuyenSuot.canh(den).size());
        }
        for (String v : System.getProperty("vaoMap", "").split(",")) {
            if (v.isEmpty()) continue;
            int den = Integer.parseInt(v.trim());
            for (int mid : BotXuyenSuot.LOAD.keySet())
                for (int[] e : BotXuyenSuot.canh(mid)) if (BotXuyenSuot.cungBanDo(e[1], den)) System.out.println("CONG VAO " + den + ": tu map " + mid + " cong " + e[0] + " -> " + e[1]);
            System.out.println("NPC cua load " + BotXuyenSuot.LOAD.get(den) + ": " + KichBanHuongDan.npcCMap(BotXuyenSuot.LOAD.getOrDefault(den, -1)).stream().map(java.util.Arrays::toString).collect(java.util.stream.Collectors.joining(" ")));
        }
        for (String n : System.getProperty("thuNpc", "").split(",")) {
            if (n.isEmpty()) continue;
            int npc = Integer.parseInt(n.trim());
            for (int mid : mapTim()) {
                Integer l = BotXuyenSuot.LOAD.get(mid);
                if (l != null && KichBanHuongDan.oNpc(l, npc) != null) System.out.println("NPC " + npc + " o map " + mid + " (load " + l + ", MAP_THU " + BotXuyenSuot.MAP_THU.contains(mid) + ") duong tu 0: " + (BotXuyenSuot.duong(0, mid) == null ? "KHONG" : BotXuyenSuot.duong(0, mid).size()));
            }
        }
        HoiQuy.thoat(gmb);
    }

    /**
     * Ban do de tim NPC / dich: ban sao 0 cua moi nhom goi 105 (MAP_THU) + ban do chuan (id = load) khong nam trong nhom nao.
     * Truoc 30/10/2026 chi MAP_THU: lang Cao nguyen Indigo (201, MapTown) khong o nhom nao -> "khong ban do nao co NPC
     * loai 24" (Sableye) / 25 (Heracross giu Gym) du nguoi choi di bo toi duoc.
     */
    static volatile Set<Integer> MAP_TIM;

    static Set<Integer> mapTim() {
        Set<Integer> s = MAP_TIM;
        if (s != null) return s;
        s = new TreeSet<>(BotXuyenSuot.MAP_THU);
        for (Map.Entry<Integer, Integer> e : BotXuyenSuot.LOAD.entrySet())
            if (e.getKey().equals(e.getValue()) && BotXuyenSuot.hang(e.getKey()) < 0) s.add(e.getKey());
        MAP_TIM = s;
        return s;
    }

    /** Ban do trong lang khoi dau phe 0 (vao bang cua o lang 0 / 301): KiemSuaTuyen.NHA_LANG cot 0. */
    static final Set<Integer> NHA_LANG_0 = new HashSet<>(Arrays.asList(29, 102, 103, 104, 108, 109));

    static final int CAP_DICH = Integer.getInteger("capDich", 40);
    /**
     * Luot phu kin nhiem vu phu (-DuuTienPhu=1): nhu nguoi choi thich lam het viec phu o moi thi tran - nhiem vu phu dang lam
     * di TRUOC nhiem vu chinh (phu chi moi trong dai 10 cap: len cap nhanh qua la mat). Chinh van lam khi phu ket / het phu.
     */
    static final boolean UU_TIEN_PHU = Boolean.getBoolean("uuTienPhu") || "1".equals(System.getProperty("uuTienPhu"));
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
        // ban do ngoai nhom goi 105 (21 Dong co phia tay - chi qua Arcanine): quai cua no cung vao QUAI_MAP (truoc: "khong
        // biet quai 'Scyther'" o #253 d250)
        for (int mid : mapTim()) {
            if (BotXuyenSuot.MAP_THU.contains(mid)) continue;
            Integer l = BotXuyenSuot.LOAD.get(mid);
            if (l == null) continue;
            for (int t : BotXuyenSuot.luoiQuai(l)) BotXuyenSuot.QUAI_MAP.computeIfAbsent(t, z -> new TreeSet<>()).add(mid);
        }
    }

    /** Ban do (mapId ban sao 0) co ten {@code ten} ma di toi duoc tu {@code tu}, gan nhat; -1 = khong co. */
    static int mapTheoTen(int tu, String ten) {
        if (ten == null) return -1;
        Integer load = LOAD_THEO_TEN.get(ten.trim());
        if (load == null) return -1;
        int tot = -1, ds = Integer.MAX_VALUE;
        for (int mid : mapTim()) {
            Integer l = BotXuyenSuot.LOAD.get(mid);
            if (l == null || !l.equals(load)) continue;
            List<int[]> d = BotXuyenSuot.duongUocTinh(tu, mid);
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

    /** Dong (class) theo chu so cuoi ten nhanh ("bo3" -> 3); khong co = 0 (Charmander, nhu truoc). */
    static int lopCua(String nhanh) {
        char c = nhanh.isEmpty() ? 'x' : nhanh.charAt(nhanh.length() - 1);
        return c >= '0' && c <= '4' ? c - '0' : 0;
    }

    /**
     * Diem huan luyen (client 0.9.8+ nut "Huấn luyện", goi -122): phan 1 diem moi lan, xoay Tấn công / HP / Phòng thủ, toi
     * khi may chu tu choi (het diem / tran dong), roi diem ky nang vao chieu 0..3. Dung chung bot choi va do suc (truoc
     * 29/10/2026 do suc goi lenh tiem nang cu 34 ma che do huan luyen bo qua -> nhan vat do suc KHONG co diem nao).
     * @return dong ghi (null = khong phan diem nao).
     */
    static String congDiemHuanLuyen(BotChucNang.Bot b, int toiDa) throws IOException {
        int[] thuTu = {1, 0, 2};
        int daCong = 0;
        String ghi = null;
        for (int lan = 0; lan < toiDa; lan++) {
            int m = b.moc();
            b.gui(-122, (byte) 1, (byte) 1, (byte) thuTu[lan % 3], (short) 1);
            b.bom(250);
            String t = b.chuTu(m);
            if (t.contains("DUNG LOI") || t.contains("không") || t.contains("Không") || t.contains("Hết")) {
                if (daCong == 0 && lan == 0 && !t.contains("đủ") && !t.contains("điểm")) ghi = "huan luyen: " + t;
                break;
            }
            daCong++;
        }
        for (int o = 0; o < 4; o++) { b.gui(36, (byte) o); b.bom(120); } // diem ky nang (chieu)
        b.lamMoi();
        return daCong > 0 ? "phan " + daCong + " diem huan luyen (Tấn công/HP/Phòng thủ)" : ghi;
    }

    // ================================================================== mot nguoi choi
    static class NguoiChoi extends BotXuyenSuot.Dong { // khong final: tools/pokemon/hlv_ai/HlvAiNguoiChoi ke thua
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

        /** "lam" (lam huong dan) hoac "bo" (bo qua) - ten nhanh bo chu so dong o cuoi. */
        final String kieu;

        NguoiChoi(String nhanh) {
            super(lopCua(nhanh), null, Long.MAX_VALUE);
            this.nhanh = nhanh;
            this.kieu = nhanh.startsWith("lam") ? "lam" : "bo";
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
                // ngoac cho moi vong for: truoc 30/10/2026 "else if" sau "chinh=" gan vao if TRONG vong for (dangling else) ->
                // phu= / ket= / gd= / moc= / tn= / so= khong bao gio doc: moi lan chay lai bot di lai vong tinh nang ~10 phut
                if (l.startsWith("user=")) user = l.substring(5);
                else if (l.startsWith("phut=")) phutCongDon = Double.parseDouble(l.substring(5));
                else if (l.startsWith("ngayAo=")) soNgayAo = Integer.parseInt(l.substring(7));
                else if (l.startsWith("tg=")) dongThoiGian.add(l.substring(3));
                else if (l.startsWith("cap=")) { String[] f = l.substring(4).split(":"); capLuc.put(Integer.parseInt(f[0]), Long.parseLong(f[1])); }
                else if (l.startsWith("chinh=")) { for (String x : l.substring(6).split(",")) if (!x.isEmpty()) chinhDaXong.add(Integer.parseInt(x)); }
                else if (l.startsWith("phu=")) { for (String y : l.substring(4).split(",")) if (!y.isEmpty()) phuDaXong.add(Integer.parseInt(y)); }
                else if (l.startsWith("ket=")) ketChi.add(l.substring(4));
                else if (l.startsWith("gd=")) daGiaoDich = l.endsWith("1");
                else if (l.startsWith("moc=")) { for (String z : l.substring(4).split(",")) if (!z.isEmpty()) mocTinhNang.add(Integer.parseInt(z)); }
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
            ngheDon(); // don quai trung minh: in khi guc
            KichBanHuongDan.roiSan(b);
            b.lamMoi();
            capTruoc = b.lv;
            ptTruoc = b.pt;
            lanVaoLai = System.currentTimeMillis();
            congDiem(); // diem huan luyen con du (vao lai / lan dau che do huan luyen)
            napDangMac();
            ghi("vao game map " + b.mapId + " (" + b.tenMap + ") xu " + b.xu + " HP " + b.hp + "/" + b.maxhp);
            // guc truoc khi rot / tat JVM: vao lai van guc (may chu giu HP 0) - nguoi choi that thay hop hoi sinh ngay;
            // truoc 31/10/2026 bot di tiep khi dang guc toi cong ("bạn đã bị hạ gục")
            if (b.hp <= 0 && b.maxhp > 0) { ghi("vao game dang guc -> hoi sinh"); hoiSinh(); }
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
                BotChucNang.Bot b0 = HoiQuy.nguoiChoiMoi(kieu.equals("lam") ? "cl" : "cb", lop, 0);
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
            if (kieu.equals("bo")) {
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
            ket("cap1_40." + nhanh + ".huong_dan_10_buoc", buoc.size() == KichBanHuongDan.SO_BUOC && buoc.stream().noneMatch(x -> x.contains("<")), String.join(" | ", buoc));
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
                    for (int mid : mapTim()) {
                        Integer l = BotXuyenSuot.LOAD.get(mid);
                        if (l == null || KichBanHuongDan.oNpc(l, npc) == null) continue;
                        List<int[]> d = BotXuyenSuot.duong(b.mapId, mid);
                        // cum Johto / khu xa: lang ve duoc qua Arcanine "Về làng" (toi() lo), du khong co duong di bo
                        boolean quaArc = d == null && KichBanDS.laLang(mid) && (BotXuyenSuot.Dong.CUM_JOHTO.contains(b.mapId) || BotXuyenSuot.Dong.KHU_XA_ARCANINE.contains(b.mapId));
                        // khong co duong tu day nhung co tu lang: toi() ve lang (Arcanine / So tay) roi di tiep
                        boolean quaLang = d == null && !quaArc && !BotXuyenSuot.cungBanDo(b.mapId, 0) && BotXuyenSuot.duong(0, mid) != null;
                        int s = quaArc ? 1000 : quaLang ? 2000 + BotXuyenSuot.duong(0, mid).size() : d == null ? Integer.MAX_VALUE : d.size() - (KichBanDS.laLang(mid) ? 1 : 0);
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
        int boSuBoss;

        /**
         * Thong bao may chu "<boss> đã xuất hiện ở <ban do>!" / "... đã rời <ban do>. Hẹn lần sau!" (ThongBao.bossXuatHien /
         * bossBienMat): nguoi choi that tranh ban do co boss lang thang (danh lan 220 px, 27.000 tan cong) - chan 3 gio / mo lai.
         */
        void xemBoss() {
            int n = b.su.size();
            if (boSuBoss > n) boSuBoss = 0;
            for (int i = boSuBoss; i < n; i++) {
                String c = b.su.get(i).chu;
                if (c == null) continue;
                Matcher co = Pattern.compile("đã xuất hiện ở ([^!|]+)!").matcher(c);
                Matcher roi = Pattern.compile("đã rời ([^.|]+)\\. Hẹn").matcher(c);
                String ten = co.find() ? co.group(1).trim() : roi.find() ? roi.group(1).trim() : null;
                if (ten == null) continue;
                Integer load = LOAD_THEO_TEN.get(ten);
                if (load == null) continue;
                boolean xuatHien = c.contains("đã xuất hiện");
                for (int mid : mapTim()) {
                    if (!load.equals(BotXuyenSuot.LOAD.get(mid)) || KichBanDS.laLang(mid)) continue;
                    if (xuatHien) BotXuyenSuot.MAP_CHAN.put(mid, System.currentTimeMillis() + 3 * 3600000L);
                    else BotXuyenSuot.MAP_CHAN.remove(mid);
                }
                ghi("boss " + (xuatHien ? "o " : "roi ") + ten + " (load " + load + ") -> " + (xuatHien ? "tranh ban do" : "mo lai"));
            }
            boSuBoss = n;
        }

        void vong() throws Exception {
            BotXuyenSuot.quetLog();
            b.lamMoi();
            xemBoss();
            // trong nha / khu khong co cong di bo (Bao tang 100 sau Arcanine): ra cua nhu nguoi choi truoc khi tim duong
            if (b.mapId >= 0 && !BotXuyenSuot.LOAD.containsKey(b.mapId)) {
                int truoc = b.mapId;
                KichBanDS2.raNgoai(b);
                if (b.mapId != truoc) ghi("ra khoi map " + truoc + " -> " + b.mapId);
            }
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
            // guc >= 3 lan / 20 phut: nhu nguoi choi - luyen o bai ngang cap 15 phut (kiem xu mua vu khi / len cap) roi quay lai
            if (gucGanDay(20 * 60000L) >= 3 && System.currentTimeMillis() - lanLuyenAnToan > 25 * 60000L) {
                lanLuyenAnToan = System.currentTimeMillis();
                ghi("guc " + gucGanDay(20 * 60000L) + " lan / 20 phut -> luyen bai ngang cap 15 phut");
                muaThuoc();
                luyen(15);
                return;
            }
            hop95(0);
            docNv();
            if (traXong()) return;
            if (nhanMoi()) return;
            muaThuoc();
            macDoTot();
            nangVuKhi();
            muaGiapIndigo();
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
                if (conXong) {
                    if (t.contains("Hành trang đã đầy") || t.contains("hành trang đầy")) donTui(); // nguoi choi ban bot do roi tra lai
                    ketLai("tra '" + q.ten + "' #" + q.id + "/" + q.ms + " khong duoc: " + t);
                    continue;
                }
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
                    if (q.ms == 0 && (bt.contains("ve chuong") || bt.contains("mo sau"))) {
                        // nguoi choi het ve chuong lam nhiem vu phu trong luc cho (truoc 30/10/2026 bot tua ngay ngay)
                        if (coViecPhu()) { ghi("chinh '" + q.ten + "' cho ve chuong -> lam nhiem vu phu truoc"); continue; }
                        choNgayMoi("nhan '" + q.ten + "': " + t);
                        return true;
                    }
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

        /** Con nhiem vu phu dang lam / co the nhan (chua bo) de lam trong luc chinh cho ve chuong. */
        boolean coViecPhu() {
            for (KichBanTienDoNhiemVu.Dong x : l2) if (x.ms == 1 && !phuBo.contains(x.id)) return true;
            for (KichBanTienDoNhiemVu.Dong x : l0) if (x.ms == 1 && !phuBo.contains(x.id)) return true;
            return false;
        }

        // ---------------------------------------------------------------- lam muc tieu
        boolean lamDangLam() throws Exception {
            dangNangCap = l2.stream().anyMatch(x -> (x.muc + " " + x.moTa).contains("Nâng 1 món trang bị"));
            List<KichBanTienDoNhiemVu.Dong> thuTu = new ArrayList<>();
            for (int ms : UU_TIEN_PHU ? new int[]{1, 2, 0} : new int[]{0, 2, 1}) for (KichBanTienDoNhiemVu.Dong q : l2) if (q.ms == ms) thuTu.add(q);
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
            boChanKhiMoBanDo();
            if (capMapKhoa != b.lv) { mapKhoa.clear(); capMapKhoa = b.lv; }
            int tot = -1, diem = Integer.MAX_VALUE;
            for (int mid : BotXuyenSuot.MAP_THU) {
                Integer l = BotXuyenSuot.LOAD.get(mid);
                if (l == null || KichBanDS2.laOffline(mid) || mapKhoa.contains(mid)) continue;
                for (int t : BotXuyenSuot.luoiQuai(l)) {
                    Integer c = BotXuyenSuot.CAP_QUAI.get(t);
                    if (c == null || c - b.lv < -3 || c - b.lv > 3) continue;
                    List<int[]> d = BotXuyenSuot.duongUocTinh(b.mapId, mid);
                    if (d == null) continue;
                    int sc = d.size() * 2 + Math.abs(c - b.lv);
                    if (sc < diem) { diem = sc; tot = mid; }
                }
            }
            return tot;
        }

        /**
         * Bai luyen CAO NHAT di toi duoc (chua bi chan o cap nay): quai cao nhat <= cap minh + 4. Dung khi da vuot moi bai
         * ngang cap (nhiem vu "chênh không quá 5 cấp (hoặc ở bãi luyện cao nhất bạn đã mở)", luyen khi khong con bai ngang cap).
         */
        int mapCaoNhat() {
            int tot = -1, capTot = -1, dsTot = Integer.MAX_VALUE;
            for (int mid : BotXuyenSuot.MAP_THU) {
                Integer l = BotXuyenSuot.LOAD.get(mid);
                if (l == null || KichBanDS2.laOffline(mid) || mapKhoa.contains(mid)) continue;
                int top = -1;
                for (int t : BotXuyenSuot.luoiQuai(l)) { Integer c = BotXuyenSuot.CAP_QUAI.get(t); if (c != null && c <= b.lv + 4) top = Math.max(top, c); }
                if (top < 0) continue;
                List<int[]> d = BotXuyenSuot.duongUocTinh(b.mapId, mid);
                if (d == null) continue;
                if (top > capTot || top == capTot && d.size() < dsTot) { tot = mid; capTot = top; dsTot = d.size(); }
            }
            return tot;
        }

        @Override
        void luyen(int phut) throws Exception {
            int mid = mapLuyen();
            if (mid < 0) mid = mapCaoNhat(); // da vuot moi bai ngang cap: luyen o bai cao nhat da mo
            if (mid < 0) { ketLai("khong con ban do luyen mo cho cap " + b.lv + " (da khoa: " + mapKhoa + ")"); b.bom(5000); return; }
            if (!toi(mid)) { mapKhoa.add(mid); hop95(0); return; }
            // do can bang kinh te thuoc (thiet ke: thuoc <= 12% thu nhap): binh / quai, xu / quai, guc khi luyen
            long t0 = System.currentTimeMillis(), xu0 = b.xu;
            int binh0 = b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3), so0 = ttkSo, chet0 = soChet;
            long ttk0 = ttkMs;
            danh(-1, 40, phut * 60000L);
            int ha = ttkSo - so0;
            if (ha > 0) ghi(String.format(Locale.ROOT, "LUYEN map %d: %d quai / %.1f phut, TTK %.1fs, BINH %.2f/quai, XU %+d, guc %d, cap %d maxhp %d",
                    mid, ha, (System.currentTimeMillis() - t0) / 60000.0, (ttkMs - ttk0) / 1000.0 / ha,
                    Math.max(0, binh0 - (b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3))) / (double) ha, b.xu - xu0, soChet - chet0, b.lv, b.maxhp));
        }
        final Map<Integer, Integer> lanToiDich = new HashMap<>();

        /**
         * Ban do ghi trong chu muc tieu: ten ban do (data_map_ten) xuat hien trong chu - uu tien ten ngay truoc "(ô x,y" ,
         * roi sau " ở " / " trên " / "Đến ", roi ten dai nhat. Bo qua ten trong phan "Trả:" / "Nhận thưởng ở".
         */
        String tenBanDoTrong(String chu) {
            // goi y cua may chu "(hãy tới X (ô a,b)..." la ban do that cua diem (vd #57: "trên Cerulean, chân Núi Mặt Trăng
            // (ô 53,49)" - diem o Cerulean, khong phai Nui Mat Trang)
            Matcher mh = Pattern.compile("hãy tới ([^(),]+?) \\(ô").matcher(chu);
            if (mh.find() && LOAD_THEO_TEN.containsKey(mh.group(1).trim())) return mh.group(1).trim();
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
            // huong dan HD.01..10 (id 901..910) do dang (vd ket o buoc 2 luc tao nhan vat roi choi tiep): lam theo kich ban huong
            // dan (cau ca o ao lang, mac do...) - lamMot chung khong biet "Thả câu 1 lần" (lamp4 ket o HD.09)
            if (q.ms == 0 && q.id >= 0 && q.id < KichBanHuongDan.SO_BUOC) { // -64 dung chi so chuoi chinh: HD = #0..#9
                int m64 = b.moc();
                b.gui(-64, (byte) 1, (short) -1, (byte) 0);
                b.cho(x -> KichBanTienDoNhiemVu.ds(x, m64, 2) != null, 3000);
                KichBanHuongDan.Nv nv = KichBanHuongDan.chinh(b, m64, 2); // buoc DANG LAM (nhom 2), danh sach moi
                if (nv == null) nv = KichBanHuongDan.chinh(b, m64, 0);
                if (nv != null && nv.id == q.id) {
                    String ly = KichBanHuongDan.lamBuoc(b, nv, nv.id);
                    return ly == null ? null : "huong dan '" + q.ten + "': " + ly;
                }
            }
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
                Integer nha = tenBd == null ? null : LOAD_THEO_TEN.get(tenBd.trim());
                if (dich < 0 && nha != null && NHA_LANG_0.contains(nha)) {
                    // ban do trong lang (nha / khu noi bo cua lang khoi dau, vd 29 "Rừng Ilex (cạnh làng khởi đầu)"): ve lang
                    // roi vao cua nhu nguoi choi (truoc 30/10/2026 bot d210 ket "khong biet ban do 'Rừng Ilex'" o C34.4)
                    if (b.mapId != nha) {
                        KichBanCap140TinhNang.veLangDiBo(this);
                        if (!KichBanDS2.vaoNha(b, nha) || b.mapId != nha) return "khong vao duoc " + tenBd + " (" + nha + ") tu lang " + b.mapId;
                    }
                } else {
                    if (dich < 0) return "khong biet ban do '" + tenBd + "'";
                    if (!toi(dich)) return "khong toi duoc " + tenBd;
                }
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                if (nha != null && b.mapId == nha && b.mapLoad != nha) {
                    ghi("trong nha " + nha + ": mapLoad " + b.mapLoad + " -> dung luoi " + nha + " (o " + b.tx + "," + b.ty + ")");
                    bd = BotChucNang.BanDo.nap(nha);
                }
                int x = Integer.parseInt(mo.group(1)), y = Integer.parseInt(mo.group(2));
                int[] o = bd == null ? null : bd.oGan(x, y, 2);
                KichBanHuongDan.dongBoViTri(b);
                if (o == null || !b.diDuong(bd, o[0], o[1])) return "khong di bo toi o " + x + "," + y + " o " + tenBd + " (map " + b.mapId + " load " + b.mapLoad + " tu o " + b.px / 16 + "," + b.py / 16 + (o == null ? ", khong co o gan" : "") + ")";
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
                // chu moi: "(hoặc ở bãi luyện cao nhất bạn đã mở)" -> khong con bai ngang cap thi ra bai cao nhat, danh moi quai
                boolean baiCao = false;
                if (dich < 0 && chu.contains("bãi luyện cao nhất")) { dich = mapCaoNhat(); baiCao = dich >= 0; }
                if (dich >= 0 && !toi(dich)) { mapKhoa.add(dich); hop95(0); return "khong toi duoc ban do luyen " + dich + " (thu ban do khac)"; }
                lechDuoi = baiCao ? 999 : lech;
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
                    for (int mid : co) { List<int[]> d = BotXuyenSuot.duongUocTinh(b.mapId, mid); if (d != null && d.size() < ds) { ds = d.size(); dich = mid; } }
                }
                if (dich < 0 && co != null && co.stream().anyMatch(BotXuyenSuot::dangChan)) {
                    // ban do cua quai dang khoa (cua ai / cap): nhan cua ai nhu nguoi choi doc hop "Để vào X cần xong cửa ải"
                    hop95(0);
                    nhanCuaAiNeuKhoa();
                    return "ban do cua " + HoiQuy.TEN_QUAI.get(tpl) + " dang khoa (cua ai / cap)";
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
        final Map<Integer, Integer> nangDaDat = new HashMap<>();
        /** Dang co buoc "Nâng 1 món trang bị lên +N": mon da nang do dang giu trong tui (khong ban, khong mac). */
        boolean dangNangCap;

        /**
         * Buoc noi chuyen co hanh dong kiem that (nhanh sua-buoc-hanh-dong, kiem_hanh_dong=1): chu muc tieu
         * "Mặc 1 món trang bị" / "Nâng 1 món trang bị lên +2 ở chỗ Machamp" / "Hạ 1 Skorupi vào ban đêm (19:00–5:59) ở Saffron"
         * / "Hạ 26 Gloom trong 8 phút ở Fuchsia". null = khong phai buoc hanh dong; "-" = da lam; chuoi khac = ly do ket.
         */
        String buocHanhDong(KichBanTienDoNhiemVu.Dong q, String chu, String tenBd) throws Exception {
            int lan = lanHanhDong.merge(q.id * 10 + q.ms, 1, Integer::sum);
            // buoc dia diem khong co cong di bo (Bao tang Pewter, Duong Victory, Nui Silver...): chu goi y "nhờ Arcanine gác
            // cổng (hãy tới X)" - nguoi choi ve lang, noi chuyen Arcanine (NPC loai 4), chon "Đến X". Truoc 29/10/2026 bot
            // ket o #36 'Bảo tàng thiên thạch' (ca 5 dong, cap 26-31).
            // buoc "Đến X ... (hãy tới X)" khong co o: ca ban do la vung dich (data_nhiem_vu_khu_vuc 0..255) - toi X, buoc vai o
            Matcher mt = Pattern.compile("\\(hãy tới ([^)]+)\\)").matcher(chu);
            if (!chu.contains("Arcanine") && mt.find() && LOAD_THEO_TEN.containsKey(mt.group(1).trim())) {
                if (lan > 6) return "toi " + mt.group(1) + " 6 lan van chua xong";
                int dich = mapTheoTen(b.mapId, mt.group(1).trim());
                if (dich < 0) return "khong co duong toi " + mt.group(1);
                if (!toi(dich)) return "khong toi duoc " + mt.group(1);
                BotChucNang.BanDo bdo = BotChucNang.BanDo.nap(b.mapLoad);
                if (bdo != null) {
                    KichBanHuongDan.dongBoViTri(b);
                    for (int k = 0; k < 3; k++) {
                        int[] o = bdo.oGan(b.px / 16 + (k % 2 == 0 ? 2 : -2), b.py / 16 + 1, 3);
                        if (o != null) b.diDuong(bdo, o[0], o[1]);
                    }
                }
                ghi("hanh dong '" + q.ten + "': toi " + mt.group(1) + " (map " + b.mapId + ")");
                b.bom(1500);
                return "-";
            }
            Matcher ma = Pattern.compile("Arcanine gác cổng[^(]*\\(hãy tới ([^)]+)\\)").matcher(chu);
            if (ma.find()) {
                if (lan > 6) return "Arcanine khong dua toi " + ma.group(1) + " sau 6 lan";
                String noi = ma.group(1).trim();
                KichBanCap140TinhNang.veLangDiBo(this);
                String l = KichBanHuongDan.toiNpc(b, 4);
                if (l != null) return "khong toi Arcanine gac cong: " + l;
                int m = b.moc();
                b.gui(23, (byte) 4);
                if (b.doiMenu(m, 4, 0, 3000) <= 0) return "Arcanine khong mo menu: " + b.chuTu(m);
                int i = b.timMuc(noi);
                if (i < 0) return "menu Arcanine khong co '" + noi + "': " + b.menuChu;
                int m2 = b.moc();
                int truoc = b.mapId;
                b.menu(4, 0, i);
                b.cho(x -> x.coLenh(m2, 12), 6000);
                if (b.coLenh(m2, 12)) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(1500); }
                b.px = b.tx * 16 + 8;
                b.py = b.ty * 16 + 8;
                ghi("hanh dong '" + q.ten + "': Arcanine -> " + noi + " map " + truoc + " -> " + b.mapId + " | " + b.chuTu(m2));
                // buoc dia diem xet khi DI CHUYEN (KhuVucNhiemVuGame.khiDiChuyen): buoc vai o nhu nguoi choi vua vao
                BotChucNang.BanDo bdo = BotChucNang.BanDo.nap(b.mapLoad);
                if (bdo != null) {
                    KichBanHuongDan.dongBoViTri(b);
                    for (int k = 0; k < 3; k++) {
                        int[] o = bdo.oGan(b.px / 16 + (k % 2 == 0 ? 2 : -2), b.py / 16 + 1, 3);
                        if (o != null) b.diDuong(bdo, o[0], o[1]);
                    }
                }
                b.bom(2000);
                return "-";
            }
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
                // tien do "(+3/+6)" tang -> dem lai (nang tung bac, that bai roi ngau nhien la binh thuong)
                Matcher mt2 = Pattern.compile("\\(\\+(\\d+)/\\+\\d+\\)").matcher(chu);
                int daDat = mt2.find() ? Integer.parseInt(mt2.group(1)) : 0;
                if (daDat > nangDaDat.getOrDefault(q.id * 10 + q.ms, -1)) { nangDaDat.put(q.id * 10 + q.ms, daDat); lanHanhDong.put(q.id * 10 + q.ms, 1); lan = 1; }
                dangNangCap = true;
                if (lan > 12) return "nang cap khong xong sau 12 lan: " + chu;
                KichBanCap140TinhNang.veLangDiBo(this);
                String l = KichBanHuongDan.toiNpc(b, 2);
                if (l != null) return "khong toi Machamp: " + l;
                b.lamMoi();
                // o nang cap nhan mon trong HANH TRANG (may chu tim trong iItems; mon dang mac -> im lang, bot "khong tra
                // loi" 12 lan o #43 'Rèn thêm hai lần búa' ca 5 dong): thao mon ra, nang, mac lai - nhu nguoi choi
                int idNang = KichBanDS3.KHONG; // id do la short, co the am
                boolean daThao = false;
                final int canPlus = Integer.parseInt(mn.group(1));
                int[] dangNang = b.timDo(x -> x[2] > 0 && x[2] < canPlus); // mon da nang do dang (giu nang tiep mot mon)
                if (dangNang != null) idNang = dangNang[0];
                else if (!b.dangMac.isEmpty()) {
                    // thao ra: mon vao tui voi id MOI (may chu cap lai id) - tim lai theo mau mon
                    final int tplMac = b.dangMacTpl.isEmpty() ? -1 : b.dangMacTpl.get(0);
                    Set<Integer> truocThao = new HashSet<>();
                    for (int[] x : b.doDung) truocThao.add(x[0]);
                    b.gui(30, (short) (int) b.dangMac.get(0)); b.bom(800); b.lamMoi();
                    int[] moi = b.timDo(x -> !truocThao.contains(x[0]) && (tplMac < 0 || x[1] == tplMac));
                    if (moi == null) moi = b.timDo(x -> !truocThao.contains(x[0]));
                    if (moi != null) { idNang = moi[0]; daThao = true; }
                } else if (!b.doDung.isEmpty()) idNang = b.doDung.get(0)[0];
                if (idNang == KichBanDS3.KHONG && KichBanDS2.vaoNha(b, 103)) {
                    // khong biet mon dang mac (vao lai game khong gui lai goi 15) va tui trong: mua mon re nhat o tiem (500 xu)
                    int idm = KichBanDS3.muaVuKhi(b, BotXuyenSuot.vuKhiShop(lop, 1));
                    KichBanDS2.raNgoai(b);
                    if (idm != KichBanDS3.KHONG) { idNang = idm; ghi("mua vu khi re de nang cap (tui trong)"); }
                    l = KichBanHuongDan.toiNpc(b, 2);
                    if (l != null) return "khong toi Machamp: " + l;
                }
                if (idNang == KichBanDS3.KHONG) return "khong co mon nao de nang";
                // nang trong xuong (nha 103 co Machamp; may chu cho phep nang trong nha - NpcTinhBanDo.oNoiDichVu): dung
                // canh Machamp ngoai lang hay lech vi tri -> doFinishImbue im lang ("khong tra loi")
                boolean trongNha = KichBanDS2.vaoNha(b, 103);
                String t = KichBanDS3.moNangCap(b, idNang, false);
                b.gui(77, (byte) -1); b.bom(200);
                if (trongNha) KichBanDS2.raNgoai(b);
                // khong mac lai giua chung (mac vao thi id doi, lan sau khong tim ra mon dang nang); dat muc thi mac lai
                b.lamMoi();
                final int idx = idNang;
                int[] sau = b.timDo(x -> x[0] == idx);
                if (sau != null && sau[2] >= canPlus) {
                    b.gui(29, (short) idNang); b.bom(800); b.lamMoi();
                    // vu khi dung bac cap con trong tui (da thao ra de nang thu): mac lai
                    final int tplVk = BotXuyenSuot.vuKhiShop(lop, b.lv);
                    int[] vk = b.timDo(x -> x[1] == tplVk);
                    if (vk != null && sau[1] != tplVk) { b.gui(29, (short) vk[0]); b.bom(800); b.lamMoi(); }
                }
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
                if (dich < 0) {
                    Integer ld = tenBd == null ? null : LOAD_THEO_TEN.get(tenBd.trim());
                    StringBuilder ly = new StringBuilder();
                    if (ld != null) for (int mid : mapTim()) if (ld.equals(BotXuyenSuot.LOAD.get(mid)))
                        ly.append(" map ").append(mid).append(" chan=").append(BotXuyenSuot.dangChan(mid)).append(" duong=").append(BotXuyenSuot.duongUocTinh(b.mapId, mid) != null);
                    return "khong co ban do co " + tenQ + " (ban do '" + tenBd + "' load " + ld + ":" + ly + ", chan10=" + BotXuyenSuot.dangChan(10) + ", dang chan " + BotXuyenSuot.MAP_CHAN.keySet().stream().filter(BotXuyenSuot::dangChan).collect(java.util.stream.Collectors.toList()) + ")";
                }
                if (!toi(dich)) { hop95(0); nhanCuaAiNeuKhoa(); return "khong toi duoc " + tenBd; }
                int can = Integer.parseInt(mh.group(1));
                boolean khungGio = Pattern.compile("\\(\\d+:\\d+").matcher(chu).find();
                long t0 = System.currentTimeMillis();
                int chet0 = soChet;
                long ttk0 = ttkMs; int ttkSo0 = ttkSo, ttkCap0 = ttkCap;
                int binh0 = b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3);
                int ha = danh(tpl, Math.max(1, can), (can > 1 ? 7 : 2) * 60000L);
                double phut = Math.max(0.1, (System.currentTimeMillis() - t0) / 60000.0);
                Matcher mtg = Pattern.compile("trong (\\d+) phút").matcher(chu);
                // toc do ha that cua nhan vat (do can bang buoc ha trong N phut: can / N con/phut)
                String toc = mtg.find() ? String.format(Locale.ROOT, " | TOC DO %.2f con/phut, can %.2f (%d/%s phut), cap %d, maxhp %d, guc %d",
                        ha / phut, can / Double.parseDouble(mtg.group(1)), can, mtg.group(1), b.lv, b.maxhp, soChet - chet0) : "";
                // TTK (design: ~4/5/6 giay quai thuong o cap 1-30/31-60/61-100 voi do +6) - mau cho can bang
                if (ttkSo > ttkSo0) toc += String.format(Locale.ROOT, " | TTK %.1fs (cap %d, cap quai %.0f, %d con, vu khi +%d, guc %d)",
                        (ttkMs - ttk0) / 1000.0 / (ttkSo - ttkSo0), b.lv, (ttkCap - ttkCap0) / (double) (ttkSo - ttkSo0), ttkSo - ttkSo0, vuKhiPlus(), soChet - chet0)
                        + String.format(Locale.ROOT, " BINH %.2f/quai", Math.max(0, binh0 - (b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3))) / (double) (ttkSo - ttkSo0));
                ghi("hanh dong '" + q.ten + "': ha " + ha + " " + tenQ + " (lan " + lan + ")" + toc);
                if (khungGio) choGio("'" + q.ten + "' can dung khung gio: " + chu);
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
            // 1 gio moi lan (khung gio 3 tieng): nhieu dong cung tua 3h thi buoc 6h co the khong bao gio roi vao khung
            // (bo3 / bo4 ket #64 'Hoàng hôn trên cầu' 17 lan)
            BotXuyenSuot.tua("1h", nhanh + ": " + ly);
            if (BotXuyenSuot.lanTuaCuoi != truoc) ghi("CHO 1 GIO (tua 1h) vi " + ly);
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
        final Map<String, Long> gymThuLuc = new HashMap<>();

        String gymCuaAi(KichBanTienDoNhiemVu.Dong q, String chu) throws Exception {
            // thua thi luyen them 2 cap hoac (khong con cho luyen ngang cap) thu lai sau 15 phut, mua thuoc tot hon
            if (b.lv < gymThuCap.getOrDefault(q.ten, 0) + 2 && System.currentTimeMillis() - gymThuLuc.getOrDefault(q.ten, 0L) < 15 * 60000L) { luyen(4); return null; }
            gymThuCap.put(q.ten, b.lv);
            gymThuLuc.put(q.ten, System.currentTimeMillis());
            // ve lang TRUOC roi moi mua thuoc: truoc 30/10/2026 mua o cum Johto (khong co shop) that bai -> bo Thuong vao
            // Gym Celadon voi 0 binh thuoc (thua o 37, boss con 17%)
            KichBanCap140TinhNang.veLangDiBo(this);
            lanMuaThuoc = 0;
            muaThuoc();
            // thach dau o lang (Quay doi thuong o lang; boss danh duoc trong lang tu 24/10/2026): truoc day bot ra bai luyen
            // nen vua danh Gym vua bi quai hoang da danh - so lan guc o Gym khong phan anh suc boss
            if (KichBanDS2.laOffline(b.mapId)) KichBanDS2.raNgoai(b);
            // nguoi choi ve lang hoi day o Chansey (mien phi) roi moi thach dau - truoc day bot danh Gym ngay giua bai quai voi
            // 60% HP (dong Nuoc thua Celadon 4 lan, vao tran voi ~5.000 / 7.851 HP)
            KichBanCap140TinhNang.veLangDiBo(this);
            hoiChansey();
            b.lamMoi();
            hoiMau("truoc tran Gym"); // khong vao tran 1 dau 1 khi con it mau
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
            // nhieu boss Gym cung ban do (nguoi khac cung thach dau): boss CUA MINH hien ngay canh minh -> chon con moi gan nhat
            int gan = Integer.MAX_VALUE;
            for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) {
                int[] qq = e.getValue();
                if (cu.contains(e.getKey()) || qq[4] < 2000 || qq[2] <= 0) continue;
                int d = Math.abs(qq[0] - b.px) + Math.abs(qq[1] - b.py);
                if (d < gan) { gan = d; boss = e.getKey(); }
            }
            if (boss == null && b.chuTu(m2).contains("đang thách đấu")) {
                // tran cu con (boss dung o cho cu): quai maxhp lon nhat dang biet
                int mx = 0;
                for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) if (e.getValue()[4] > mx && e.getValue()[2] > 0) { mx = e.getValue()[4]; boss = e.getKey(); }
                if (mx < 2000) { boss = null; b.bom(30000); return "dang co tran Gym cu (boss o cho khac) - cho het 10 phut"; }
            }
            if (boss == null) return "khong thay boss Gym: " + b.chuTu(m2);
            long t0 = System.currentTimeMillis();
            int don = danhBoss(boss, q.ten, m2);
            String kq = b.doiChu(m2, 3000, "Chiến thắng", "thua", "hết giờ", "Hết giờ");
            boolean thang = kq != null && kq.contains("Chiến thắng");
            ghi("Gym '" + q.ten + "' cap " + b.lv + ": " + (thang ? "THANG" : "thua") + " " + don + " don " + (System.currentTimeMillis() - t0) / 1000 + "s | " + kq);
            gymKetQua.add(q.ten + " cap " + b.lv + ": " + (thang ? "THANG" : "thua") + " sau " + (System.currentTimeMillis() - t0) / 1000 + "s");
            if (thang) soGymThang++;
            b.lamMoi();
            if (b.hp <= 0) hoiSinh();
            return null;
        }

        /**
         * Danh boss Gym nhu nguoi choi: lai gan, danh lien tuc (lenh 9, xoay chieu), uong thuoc tot nhat khi HP < 50%. Ghi
         * HP boss / minh moi 60 giay (do sat thuong / giay de biet thua vi thieu suc hay vi bot). Tra so don, -1 = khong ha.
         */
        int danhBoss(int boss, String ten, int mocTran) throws IOException {
            BotXuyenSuot.gymBatDau(b.user);
            try {
                return danhBossTran(boss, ten, mocTran);
            } finally {
                BotXuyenSuot.gymXong(b.user);
            }
        }

        int danhBossTran(int boss, String ten, int mocTran) throws IOException {
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            KichBanHuongDan.dongBoViTri(b);
            long bd0 = System.currentTimeMillis(), moc = bd0;
            int hp0 = -1;
            long[] nho = {mocTran, 0, 0};
            for (int lan = 1; lan <= 3000 && System.currentTimeMillis() - bd0 < 620000L; lan++) {
                // Boss guc: may chu gui 90 (actorDie) roi go boss - lenh 7 khong con tra loi nen HP cu (0,2-0,5%) DUNG YEN.
                // Truoc 29/10/2026 bot chi xem HP lenh 7 -> danh vao khong khi toi het 10 phut ("boss ket o 0,3% HP" o bao
                // cao can bang: that ra boss da guc o phut 2-3, "Chiến thắng" da co tu luc do).
                if (lan % 4 == 1 && (KichBanGymLang.co90(b, mocTran, boss) || KichBanDoSuc.coChu(b, mocTran, "Chiến thắng"))) {
                    ghi("Gym " + ten + ": boss guc sau " + (System.currentTimeMillis() - bd0) / 1000 + "s");
                    return lan;
                }
                if (b.hp > 0 && b.hp < b.maxhp / 2) uongThuoc(); // HP song tu goi 10
                KichBanDoSuc.ne(b, bd, boss, nho); // ne chieu lon bao truoc (-96) nhu nguoi choi nhin vong do
                int[] q = b.quai.get(boss);
                if (q != null && bd != null && System.currentTimeMillis() > nho[1] && (Math.abs(q[0] - b.px) > 40 || Math.abs(q[1] - b.py) > 40) && lan % 5 == 1) {
                    int[] o = bd.oGan(q[0] / 16, q[1] / 16, 2);
                    if (o != null) b.diDuong(bd, o[0], o[1]);
                }
                int[] qb = b.quai.get(boss);
                b.gui(9, (short) boss, (byte) BotXuyenSuot.chieuTheoHe(b, lop, qb == null ? -1 : qb[5], lan));
                b.bom(280);
                if (lan % 10 == 0) {
                    b.gui(7, (short) boss);
                    b.lamMoi();
                    q = b.quai.get(boss);
                    if (q != null && hp0 < 0 && q[2] > 0) hp0 = q[2];
                    if (q == null || q[2] == 0) return lan;
                    if (b.hp > 0 && b.hp < b.maxhp / 2) for (int t : new int[]{3, 2, 1}) if (b.soThuoc(t) > 0) { b.gui(22, (byte) t); b.bom(200); break; }
                    if (System.currentTimeMillis() - moc > 60000L) {
                        moc = System.currentTimeMillis();
                        long giay = (moc - bd0) / 1000;
                        ghi("Gym " + ten + " " + giay + "s: boss " + q[2] + "/" + q[4] + " (" + (hp0 > 0 ? (hp0 - q[2]) / Math.max(1, giay) : 0) + " sat thuong/giay), minh "
                                + b.hp + "/" + b.maxhp + ", thuoc " + b.soThuoc(1) + "/" + b.soThuoc(2) + "/" + b.soThuoc(3));
                    }
                    if (b.hp <= 0) {
                        long giay = (System.currentTimeMillis() - bd0) / 1000;
                        loi("Gym " + ten + ": guc o cap " + b.lv + " sau " + giay + "s, boss con " + q[2] + "/" + q[4] + " (" + (hp0 > 0 ? (hp0 - q[2]) / Math.max(1, giay) : 0)
                                + " st/giay; can " + q[4] / 600 + " st/giay de ha trong 10 phut); do: vu khi shop, xu " + b.xu);
                        return -1;
                    }
                }
                if (b.dut) return -1;
            }
            return -1;
        }

        /**
         * Diem huan luyen (client 0.9.8+ nut "Huấn luyện", goi -122): phan 1 diem moi lan, xoay Tấn công / HP / Phòng thủ,
         * toi khi may chu tu choi (het diem / tran dong). Thay lenh cong tiem nang cu (34) ma che do huan luyen bo qua.
         */
        @Override
        void congDiem() throws IOException {
            String t = congDiemHuanLuyen(b, 60);
            if (t != null) ghi(t);
        }

        @Override
        boolean veLangKhac() throws Exception {
            return KichBanCap140TinhNang.dungVe(this);
        }

        /**
         * Do dang mac (nhu nguoi choi nhin o Trang bi) luc vao game: may chu luu tob_char.equip khi thoat, nen sau khi vao lai
         * doc o day la dung. Truoc 30/10/2026 bot chi biet mon dang mac qua goi 15 (khong gui lai khi vao game): vao lai la
         * "khong cam gi" -> mua vu khi re hon, mac vao (thao vu khi tot ra tui) roi "ban mon cu" ban luon vu khi tot (d290 cap
         * 85: vu khi cap 66 -> cap 21 -> cap 1, guc lien tuc).
         */
        void napDangMac() {
            capDaMac.clear();
            String eq = BotChucNang.sql1("SELECT equip FROM tob_char WHERE id=" + b.charDb);
            if (eq == null) return;
            for (String it : eq.split(">")) {
                String[] f = it.split("\\|")[0].split(",");
                if (f.length < 3) continue;
                try {
                    int tpl = Integer.parseInt(f[1].trim());
                    int[] tt = ttDo(tpl);
                    if (tt != null && tt[0] < 28) capDaMac.merge(tt[0], tt[1], Math::max);
                } catch (NumberFormatException ignored) { }
            }
            Integer vk = capDaMac.get(loaiVuKhi());
            if (vk != null) vuKhiMac = BotXuyenSuot.vuKhiShop(lop, vk);
            ghi("dang mac (theo o): " + capDaMac);
        }

        int loaiVuKhi() {
            int[] t = ttDo(BotXuyenSuot.vuKhiShop(lop, 1));
            return t == null ? -1 : t[0];
        }

        long lanNangVk, lanIndigo;

        /**
         * Giap / phu kien cap 64-79 (bo "chuyên gia" / "tinh anh", tpl 467..506) chi ban o Bronzong ban giap (NPC 27) Cao
         * nguyen Indigo (201) / nha Indigo (260): tiem Pewter het o cap 59. Nguoi choi that cap 64+ len Indigo mua; truoc
         * 31/10/2026 bot cap 72 / 85 van mac giap cap 59 (d290 guc o map 28 vi Galvantula danh 27% HP).
         */
        void muaGiapIndigo() throws Exception {
            if (b.lv < 64 || System.currentTimeMillis() - lanIndigo < 30 * 60000L) return;
            List<int[]> mua = new ArrayList<>();
            long con = b.xu - DU_TRU_XU - (long) MUC * GIA_THUOC[3];
            for (int type : O_GIAP) {
                String r = BotChucNang.sql1("SELECT CONCAT(id,',',level,',',price) FROM data_item WHERE id BETWEEN 467 AND 506 AND clazz=-1 AND type=" + type
                        + " AND level<=" + b.lv + " ORDER BY level DESC, price LIMIT 1");
                if (r == null || !r.contains(",")) continue;
                String[] f = r.trim().split(",");
                int tpl = Integer.parseInt(f[0]), cap = Integer.parseInt(f[1]), gia = Integer.parseInt(f[2]);
                if (capDaMac.getOrDefault(type, 0) >= cap || giapMac.getOrDefault(type, 0) >= cap) continue;
                mua.add(new int[]{tpl, cap, gia, type});
            }
            mua.sort((x, y) -> x[2] - y[2]); // re truoc: nhieu o nhat voi so xu dang co
            List<int[]> duTien = new ArrayList<>();
            for (int[] g : mua) if (g[2] <= con) { duTien.add(g); con -= g[2]; }
            if (duTien.isEmpty()) return;
            lanIndigo = System.currentTimeMillis();
            if (!toi(201)) { ghi("giap Indigo: khong toi duoc Cao nguyen Indigo (201)"); return; }
            String l = KichBanHuongDan.toiNpc(b, 27);
            boolean nha = false;
            if (l != null && KichBanDS2.vaoNha(b, 260)) { nha = true; l = KichBanHuongDan.toiNpc(b, 27); }
            if (l != null) { ghi("giap Indigo: " + l); if (nha) KichBanDS2.raNgoai(b); return; }
            int m = b.moc();
            b.gui(23, (byte) 27, (byte) lop);
            if (b.doiMenu(m, 27, 0, 3000) > 0) { b.menu(27, 0, 0); final int mm = m; b.cho(x -> x.coLenh(mm, 23), 3000); }
            long xu0 = b.xu;
            StringBuilder kq = new StringBuilder();
            for (int[] g : duTien) {
                final int tpl = g[0];
                Set<Integer> cu = new HashSet<>();
                b.lamMoi();
                for (int[] x : b.doDung) cu.add(x[0]);
                int m2 = b.moc();
                b.gui(24, (byte) 1, (byte) 3, (short) tpl, (short) 1, (byte) lop);
                b.bom(1000);
                b.lamMoi();
                int[] moi = b.timDo(x -> x[1] == tpl && !cu.contains(x[0]));
                giapMac.put(g[3], g[1]);
                if (moi == null) { kq.append(" tpl ").append(tpl).append(" KHONG MUA DUOC: ").append(b.chuTu(m2)); continue; }
                b.gui(29, (short) moi[0]);
                b.bom(700);
                capDaMac.put(g[3], g[1]);
                kq.append(" tpl ").append(tpl).append("(o ").append(g[3]).append(", cap ").append(g[1]).append(")");
            }
            b.lamMoi();
            if (b.xu < xu0) xuTieu += xu0 - b.xu;
            ghi("GIAP INDIGO: xu " + xu0 + " -> " + b.xu + ":" + kq);
            if (nha) KichBanDS2.raNgoai(b);
        }

        /** +N cua vu khi dang cam (goi 15), -1 = chua biet. */
        int vuKhiPlus() {
            int loai = loaiVuKhi();
            for (int i = 0; i < b.dangMac.size() && i < b.dangMacTpl.size() && i < b.dangMacPlus.size(); i++) {
                int[] t = ttDo(b.dangMacTpl.get(i));
                if (t != null && t[0] == loai) return b.dangMacPlus.get(i);
            }
            return -1;
        }

        /** +N vu khi nguoi choi that thuong co o cap nay: +2 o cap 24, +3 / 36, +4 / 48, +5 / 60, +6 tu 72. */
        static int plusMucTieu(int lv) {
            return Math.max(0, Math.min(6, lv / 12));
        }

        /**
         * Nang vu khi dang cam o Machamp (nha 103) nhu nguoi choi that dung da luyen kim roi tu quai: thao ra, nang tung bac
         * (dung bua bao ho neu co), mac lai. Yeu cau chu game 30/10/2026: nhan vat doan phai manh nhu nguoi choi that cung cap
         * (truoc day bot chi nang khi nhiem vu bat buoc "Nâng ... +N" - moi bot cam vu khi +0).
         */
        void nangVuKhi() throws Exception {
            if (b.lv < 20 || dangNangCap || System.currentTimeMillis() - lanNangVk < 20 * 60000L) return;
            int loai = loaiVuKhi();
            int k = -1;
            for (int i = 0; i < b.dangMac.size() && i < b.dangMacTpl.size() && i < b.dangMacPlus.size(); i++) {
                int[] t = ttDo(b.dangMacTpl.get(i));
                if (t != null && t[0] == loai) { k = i; break; }
            }
            if (k < 0) return; // chua biet vu khi dang cam (goi 15 chua toi - sau lan qua cong dau)
            int muc = plusMucTieu(b.lv), dau = b.dangMacPlus.get(k);
            if (dau >= muc) return;
            lanNangVk = System.currentTimeMillis();
            final int idVk = b.dangMac.get(k), tplVk = b.dangMacTpl.get(k);
            KichBanCap140TinhNang.veLangDiBo(this);
            if (!KichBanDS2.vaoNha(b, 103)) return;
            Set<Integer> truoc = new HashSet<>();
            b.lamMoi();
            for (int[] x : b.doDung) truoc.add(x[0]);
            // may chu khong cho thao do cam tay ("hãy cầm món khác để thay"): mua vu khi re nhat (tiem cung nha 103), cam vao
            // de doi - vu khi tot ra tui (nhu nguoi choi), nang xong cam lai; vu khi re de lai / ban o lan don tui sau
            int m30 = b.moc();
            int re = KichBanDS3.muaVuKhi(b, BotXuyenSuot.vuKhiShop(lop, 1));
            if (re == KichBanDS3.KHONG) { ghi("nang vu khi: khong mua duoc vu khi re de doi | " + b.chuTu(m30)); KichBanDS2.raNgoai(b); return; }
            b.gui(29, (short) re);
            b.bom(800);
            b.lamMoi();
            int[] vk = b.timDo(x -> x[1] == tplVk && !truoc.contains(x[0]));
            if (vk == null) vk = b.timDo(x -> x[1] == tplVk);
            if (vk == null) {
                ghi("nang vu khi: khong thay vu khi trong tui sau khi thao (id " + idVk + " tpl " + tplVk + ", mac " + b.dangMac + " " + b.dangMacTpl + " " + b.dangMacPlus
                        + ", tui " + b.doDung.size() + ") | " + b.chuTu(m30));
                KichBanDS2.raNgoai(b);
                return;
            }
            final int idN = vk[0];
            int lan = 0, plus = vk[2];
            String t = "";
            long xu0 = b.xu;
            while (plus < muc && lan < 25) {
                lan++;
                t = KichBanDS3.moNangCap(b, idN, true);
                b.gui(77, (byte) -1); b.bom(200);
                b.lamMoi();
                int[] sau = b.timDo(x -> x[0] == idN);
                if (sau == null) { ghi("nang vu khi: mat mon sau khi nang (" + t + ")"); break; }
                plus = sau[2];
                if (t.contains("het da") || t.contains("cần") && !t.contains("Thành công") && !t.contains("Thất bại")) break;
            }
            b.gui(29, (short) idN);
            b.bom(800);
            b.lamMoi();
            if (b.xu < xu0) xuTieu += xu0 - b.xu;
            ghi("NANG VU KHI tpl " + tplVk + ": +" + dau + " -> +" + plus + " (muc +" + muc + ", " + lan + " lan, xu " + xu0 + " -> " + b.xu + ") | " + t);
            KichBanDS2.raNgoai(b);
        }

        // ---------------------------------------------------------------- mac do roi tot hon
        /** o trang bi (khoa gan dung) -> cap mon da mac: vu khi theo dong (tpl 79.. moi lop 7 bac), mon khac theo tpl. */
        final Map<Integer, Integer> capDaMac = new HashMap<>();

        /** o trang bi = data_item.type (moi o mot mon); vat pham dung (type >= 28) theo tpl nhu cu. */
        static int oDo(int tpl) {
            int[] t = ttDo(tpl);
            return t != null && t[0] < 28 ? t[0] : 1000 + tpl;
        }

        static final Map<Integer, int[]> TT_DO = new java.util.concurrent.ConcurrentHashMap<>();

        /** {type, level, clazz} cua mau vat pham (data_item), null = khong co. */
        static int[] ttDo(int tpl) {
            if (TT_DO.isEmpty()) {
                synchronized (TT_DO) {
                    if (TT_DO.isEmpty()) for (String d : BotChucNang.sql("SELECT id, type, level, clazz FROM data_item")) {
                        String[] f = d.split("\t");
                        if (f.length < 4) continue;
                        try { TT_DO.put(Integer.parseInt(f[0].trim()), new int[]{Integer.parseInt(f[1].trim()), Integer.parseInt(f[2].trim()), Integer.parseInt(f[3].trim())}); } catch (NumberFormatException ignored) { }
                    }
                }
            }
            return TT_DO.get(tpl);
        }

        /** Mon trong tui la trang bi TOT HON mon dang mac cung o (dung duoc: dung dong, du cap) - khong ban. */
        boolean totHonDangMac(int[] x) {
            int[] t = ttDo(x[1]);
            if (t == null || t[0] >= 28) return false;
            if (t[2] >= 0 && t[2] != lop) return false;
            return t[1] <= b.lv && t[1] > capDaMac.getOrDefault(t[0], 0);
        }

        void macDoTot() throws IOException {
            List<int[]> ds = new ArrayList<>(b.doDung);
            ds.sort((x, y) -> Integer.compare(y.length > 3 ? y[3] : 0, x.length > 3 ? x[3] : 0));
            for (int[] d : ds) {
                if (d.length < 4 || d[3] > b.lv || d[3] <= 0) continue;
                int o = oDo(d[1]);
                int[] tt = ttDo(d[1]);
                if (tt != null && tt[0] < 28 && tt[2] >= 0 && tt[2] != lop) continue; // trang bi dong khac
                if (dangNangCap && d[2] > 0) continue; // mon dang nang do dang (buoc "Nâng +N"): de trong tui
                if (capDaMac.getOrDefault(o, 0) >= d[3]) continue; // chi mac mon cap CAO HON mon da mac cung o
                int m = b.moc();
                b.gui(29, (short) d[0]);
                b.bom(500);
                String t = b.chuTu(m);
                capDaMac.put(o, d[3]);
                if (!t.isEmpty()) ghi("mac do tpl " + d[1] + " cap " + d[3] + ": " + t);
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

        long lanMuaThuoc, lanLuyenAnToan;
        int vuKhiMac = -1;
        static final int[] GIA_THUOC = {0, 2, 4, 7}; // data_potion 1..3 (sua_gia_thuoc.sql 01/11/2026)

        /**
         * Tiem (nha 103) nhu nguoi choi that, theo thu tu uu tien (can bang 29/10/2026 - bot cu mua vu khi bang het xu roi di
         * luyen KHONG con binh nao, guc lien tiep; ket qua do bot chu khong do game):
         *   1. du TRU THUOC: luon giu >= DU_TRU binh loai theo cap (mua toi MUC binh, toi da 60% xu);
         *   2. vu khi dung bac cap (BotXuyenSuot.vuKhiShop theo dong) khi mua xong van con DU_TRU_XU (~5 binh tot);
         *   3. giap / phu kien bac cap (moi o mot mon, re truoc) bang tien thua sau khi giu du tru + tiet kiem vu khi bac sau.
         * Ve tiem khi con < DU_TRU binh hoac co mon moi de mua (cach nhau >= 3 phut).
         */
        static final int DU_TRU = 6, MUC = 12;
        /** Xu giu lai khi mua vu khi (~5 binh 1000 HP): nguoi that mua vu khi ngay khi du tien, con lai mua thuoc. */
        static final long DU_TRU_XU = 4000;
        final Map<Integer, Integer> giapMac = new HashMap<>(); // type -> cap mon da mac (mua o tiem)

        /**
         * Binh nguoi choi that mua: hoi nhieu nhat moi lan uong ma tui tien mua duoc >= 5 binh. Tiem ban moi loai o moi cap
         * (khong khoa cap): Thuoc hoi mau cuc manh 1000 HP / 800 xu (1,25 HP/xu) tot hon ca Sieu thuoc 300 / 500 (0,6) lan
         * Thuoc thuong 80 / 100. Truoc 29/10/2026 bot mua Sieu thuoc o cap 10-29 - so binh / HP thuoc it hon nguoi that.
         */
        int loaiThuoc() {
            long ngan = b.xu * 6 / 10;
            return ngan >= 5L * GIA_THUOC[3] ? 3 : ngan >= 5L * GIA_THUOC[2] ? 2 : 1;
        }

        static long gia(int tpl) {
            String g = BotChucNang.sql1("SELECT price FROM data_item WHERE id=" + tpl);
            try { return g == null ? Long.MAX_VALUE : Long.parseLong(g.trim()); } catch (NumberFormatException e) { return Long.MAX_VALUE; }
        }

        /** Mon giap / phu kien tiem cap cao nhat <= cap minh cho o {@code type}: {tpl, cap, gia} hoac null. */
        int[] giapTiem(int type) {
            String r = BotChucNang.sql1("SELECT CONCAT(id,',',level,',',price) FROM data_item WHERE type=" + type + " AND clazz=-1 AND id<174 AND price>=500 AND level<=" + b.lv
                    + " ORDER BY level DESC, id LIMIT 1");
            if (r == null || !r.contains(",")) return null;
            String[] f = r.trim().split(",");
            return new int[]{Integer.parseInt(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2])};
        }

        static final int[] O_GIAP = {0, 1, 2, 8, 9, 10, 11};

        /**
         * Vu khi tiem tot nhat mua NGAY duoc (gia + du tru <= xu) va hon vu khi dang cam; khong co thi bac dung cap (de danh).
         * Truoc 30/10/2026 bot chi nham bac dung cap: Lua / Thuong cap 36 de danh 40.300 xu cho bac 36 (co 32.000) va van cam
         * vu khi cap 21 - nguoi that mua bac 31 (28.800) ngay.
         */
        int vuKhiMuaDuoc() {
            int dich = BotXuyenSuot.vuKhiShop(lop, b.lv);
            int capCam = 0;
            for (int t : new ArrayList<>(b.dangMacTpl)) {
                boolean vk = t >= 79 + 7 * lop && t < 86 + 7 * lop || t >= 174 + 8 * lop && t < 182 + 8 * lop;
                if (!vk) continue;
                String l = BotChucNang.sql1("SELECT level FROM data_item WHERE id=" + t);
                try { if (l != null) capCam = Math.max(capCam, Integer.parseInt(l.trim())); } catch (NumberFormatException ignored) { }
            }
            capCam = Math.max(capCam, capDaMac.getOrDefault(loaiVuKhi(), 0));
            for (int lv = b.lv; lv >= 1; lv -= 5) {
                int t = BotXuyenSuot.vuKhiShop(lop, lv);
                String l = BotChucNang.sql1("SELECT level FROM data_item WHERE id=" + t);
                int c;
                try { c = l == null ? 0 : Integer.parseInt(l.trim()); } catch (NumberFormatException e) { c = 0; }
                if (c <= capCam) break;
                if (t != vuKhiMac && b.xu >= gia(t) + DU_TRU_XU) return t;
            }
            return dich;
        }

        @Override
        void muaThuoc() throws Exception {
            int tplVk = vuKhiMuaDuoc();
            int loai = loaiThuoc();
            long giaVk = tplVk != vuKhiMac ? gia(tplVk) : Long.MAX_VALUE;
            int co = b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3);
            boolean canVk = tplVk != vuKhiMac && b.xu >= giaVk + DU_TRU_XU;
            int[] giap = null;
            if (!canVk && tplVk == vuKhiMac) {
                long thua = b.xu - (long) MUC * GIA_THUOC[loai];
                for (int type : O_GIAP) {
                    int[] g = giapTiem(type);
                    // capDaMac (doc tu trang bi dang mac luc vao game): sau vao lai khong mua lai mon da mac (d290 mua 2 lan 45.000 xu)
                    if (g == null || giapMac.getOrDefault(type, 0) >= g[1] || capDaMac.getOrDefault(type, 0) >= g[1] || g[2] > thua) continue;
                    if (giap == null || g[2] < giap[2]) { giap = g; giap = new int[]{g[0], g[1], g[2], type}; }
                }
            }
            if (!canVk && giap == null && co >= DU_TRU || System.currentTimeMillis() - lanMuaThuoc < 3 * 60000L || b.xu < 300) return;
            lanMuaThuoc = System.currentTimeMillis();
            long xu0 = b.xu;
            if (!KichBanDS.laLang(b.mapId)) {
                int tot = -1, ds = 999;
                for (int c : new int[]{0, 70, 80, 301, 1701, 1901}) { List<int[]> d = BotXuyenSuot.duong(b.mapId, c); if (d != null && d.size() < ds) { ds = d.size(); tot = c; } }
                if ((tot < 0 || !toi(tot)) && !KichBanCap140TinhNang.veLangSoTay(this)) { ghi("khong ve duoc lang de mua thuoc"); return; }
            }
            if (!KichBanDS2.vaoNha(b, 103)) { ketLai("khong vao duoc nha tiem 103 o lang " + b.mapId); return; }
            if (canVk) {
                long x1 = b.xu;
                int id = KichBanDS3.muaVuKhi(b, tplVk);
                if (id != KichBanDS3.KHONG) {
                    b.gui(29, (short) id);
                    b.bom(800);
                    b.lamMoi();
                    ghi("mua + mac vu khi tpl " + tplVk + " (xu " + x1 + " -> " + b.xu + ")");
                    int[] tt = ttDo(tplVk);
                    if (tt != null) capDaMac.put(tt[0], tt[1]);
                }
                vuKhiMac = tplVk; // khong mua duoc (het hang): khong thu lai cung bac
            }
            // don hanh trang nhu nguoi choi: ban do cu (cap <= cap minh - 6) - tui day thi mua thuoc moi loai cung bi chan
            // (moi loai thuoc chiem mot o; choi 5 dong 30/10: "Không mua được ... hành trang đầy" khi co 32.000 xu)
            {
                int ban = 0;
                long xb = b.xu;
                for (int[] x : new ArrayList<>(b.doDung)) {
                    // khong ban: mon da nang (+N, dang lam buoc "Nâng +N") / mon tot hon mon dang mac cung o
                    if (ban >= 20 || x.length < 4 || x[3] <= 0 || x[3] > b.lv - 6 || x[1] == tplVk || x[2] > 0 || totHonDangMac(x)) continue;
                    b.gui(28, (short) x[0]);
                    b.bom(250);
                    ban++;
                }
                if (ban > 0) { b.lamMoi(); ghi("ban " + ban + " mon cu (xu " + xb + " -> " + b.xu + ", tui con " + b.doDung.size() + ")"); }
            }
            loai = loaiThuoc(); // sau khi mua vu khi
            // vu khi chua dung bac cap: chi giu du tru thuoc, de danh xu mua vu khi (nguoi choi uu tien vu khi; bot cu mua 20
            // binh 800 xu moi lan, o cap 33 van cam vu khi cap 16 va guc moi 5 phut o Rung Viridian)
            int mucThuoc = tplVk != vuKhiMac ? DU_TRU : MUC;
            final int nhaRen = b.mapId; // nha Machamp (103 / 73 / 83): Kecleon o nha - 1, Bronzong o nha + 1
            if (b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3) < mucThuoc) {
                int sl = (int) Math.min(mucThuoc - (b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3)), b.xu * 6 / 10 / GIA_THUOC[loai]);
                if (sl > 0 && sangNha(nhaRen - 1, "Kecleon")) {
                    int t0 = b.soThuoc(loai);
                    int m = b.moc();
                    b.gui(24, (byte) 1, (byte) 4, (short) loai, (short) sl);
                    b.bom(1200);
                    b.lamMoi();
                    ghi("mua " + sl + " thuoc " + loai + ": " + t0 + " -> " + b.soThuoc(loai) + " (xu " + xu0 + " -> " + b.xu + ")");
                    if (b.soThuoc(loai) < t0 + sl) loi("mua " + sl + " thuoc " + loai + " (gia " + GIA_THUOC[loai] + ", co " + xu0 + " xu) khong duoc: " + b.chuTu(m));
                }
            }
            // Vé dịch chuyển du phong (het luot Ve lang mien phi o ban do xa khong co duong di bo): gia ~ 56 xu x cap
            if (b.soThuoc(162) < 2 && b.xu > DU_TRU_XU + 3L * 60 * b.lv) KichBanCap140TinhNang.muaVe(this, 2);
            if (giap != null && b.xu - giap[2] >= DU_TRU_XU && sangNha(nhaRen + 1, "Bronzong")) {
                final int tpl = giap[0];
                Set<Integer> cu = new HashSet<>();
                for (int[] x : b.doDung) cu.add(x[0]);
                long x1 = b.xu;
                moTiemGiap();
                b.gui(24, (byte) 1, (byte) 3, (short) tpl, (short) 1, (byte) lop); // tab dong minh (nhu client chon dong o tiem giap)
                b.bom(1000);
                b.lamMoi();
                int[] moi = b.timDo(x -> x[1] == tpl && !cu.contains(x[0]));
                giapMac.put(giap[3], giap[1]); // mua hong (het hang) cung khong thu lai bac nay
                if (moi != null) {
                    b.gui(29, (short) moi[0]);
                    b.bom(700);
                    b.lamMoi();
                    capDaMac.put(oDo(tpl), giap[1]);
                    ghi("mua + mac giap tpl " + tpl + " (o " + giap[3] + ", cap " + giap[1] + ") xu " + x1 + " -> " + b.xu);
                }
            }
            if (b.xu < xu0) xuTieu += xu0 - b.xu;
            KichBanDS2.raNgoai(b);
        }

        /**
         * Sang nha NPC canh tiem Machamp: giap o Bronzong (Pewter 103 -> 104, 73 -> 74, 83 -> 84), thuoc o Kecleon (103 -> 102,
         * 73 -> 72, 83 -> 82). Tu 31/10 (giap) / 01/11/2026 (thuoc) may chu chi ban mon cua NPC dang dung canh (goi 24): bot
         * cu mua ca hai trong nha 103 (chi co Machamp).
         */
        boolean sangNha(int nha, String ai) throws Exception {
            if (b.mapId == nha) return true;
            KichBanDS2.raNgoai(b);
            if (KichBanDS2.vaoNha(b, nha) && b.mapId == nha) return true;
            ghi("khong vao duoc nha " + ai + " " + nha + " (dang o " + b.mapId + ")");
            if (KichBanDS2.laOffline(b.mapId)) KichBanDS2.raNgoai(b);
            return false;
        }

        /** Mo cua so giap cua Bronzong (goi 23 loai 3 + lop, chon "Trang bị vật lý") nhu client. */
        void moTiemGiap() throws IOException {
            int m = b.moc();
            b.gui(23, (byte) 3, (byte) lop);
            if (b.doiMenu(m, 3, 0, 3000) > 0) { b.menu(3, 0, 1); final int mm = m; b.cho(x -> x.coLenh(mm, 23), 3000); }
        }

        /**
         * Tui day (may chu: "Hành trang đã đầy... để nhận thưởng"): vao tiem ban moi mon trang bi trong tui khong khoa, tru
         * vu khi dung bac cap va mon da nang (+N) - nhu nguoi choi don tui.
         */
        void donTui() throws Exception {
            if (!KichBanDS.laLang(b.mapId)) KichBanCap140TinhNang.veLangDiBo(this);
            if (!KichBanDS2.vaoNha(b, 103)) return;
            b.lamMoi();
            final int tplVk = BotXuyenSuot.vuKhiShop(lop, b.lv);
            int ban = 0;
            long x0 = b.xu;
            for (int[] x : new ArrayList<>(b.doDung)) {
                // do cu da nang (+N) / khoa cung ban khi da qua 6 cap (choi 5 dong 30/10: tui 28 o day vu khi cu da nang o
                // buoc "Nâng +N" + 8 Huy chuong Gym -> khong mua duoc thuoc, vao Gym Celadon voi 0 binh)
                boolean cu = x.length >= 4 && x[3] > 0 && x[3] <= b.lv - 6;
                if (x.length < 5 || x[1] == tplVk || x[3] <= 0 || !cu && (x[4] != 0 || x[2] > 0) || totHonDangMac(x) || x[2] > 0 && dangNangCap) continue;
                b.gui(28, (short) x[0]);
                b.bom(250);
                ban++;
            }
            b.lamMoi();
            // ve / bua dung ngay (Vé EXP 1251, Bùa thể lực 1255 - moi cai mot o, khong chong): nguoi choi dung khi tui chat
            String dung = "";
            for (int[] x : new ArrayList<>(b.doDung)) {
                if (x[1] != 1251 && x[1] != 1255) continue;
                int m = b.moc();
                b.gui(29, (short) x[0]);
                b.bom(400);
                if (dung.isEmpty()) dung = " | dung " + x[1] + ": " + b.chuTu(m);
            }
            b.lamMoi();
            ghi("tui day -> ban " + ban + " mon (xu " + x0 + " -> " + b.xu + ", tui con " + b.doDung.size() + ")" + dung);
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
