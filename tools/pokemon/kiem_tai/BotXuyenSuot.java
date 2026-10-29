import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * Bot CHOI XUYEN SUOT (kiem thu chap nhan): moi dong khoi dau (lop 0..4) mot tai khoan + nhan vat MOI, choi
 * nhu nguoi that (KHONG GM): di bo qua cong, theo nhiem vu chinh (-64), ha quai / nhat do nhiem vu, nhan
 * nhiem vu phu/ngay/tuan qua So tay, thach dau Gym khi du cap, cong diem tiem nang/ky nang, dung thuoc,
 * hoi sinh. Mot bot dieu phoi GM ("dieuphoi") tua dong ho ao (lenh "tua", com.htbb.pokemon.DongHo - chi
 * may thu) khi cac dong bi chan boi moc ngay / ve chuong / reset ngay-tuan.
 *
 * Sau moi buoc kiem: khong co loi moi trong nhat ky server (log/nhat_ky/may_chu-*.log dong " E "), cap/EXP
 * khong giam, khong ket (khong tien trien N phut -> ghi ly do + thu cach khac), CSDL nhiem vu khop.
 *
 * Chay (o HTBB/, may thu sv.cho_phep_thu_nghiem=1; nen -Dhtbb.thu.nhanExp=N khi chay tang toc):
 *   javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/BotChucNang.java tools/pokemon/kiem_tai/KichBanDS*.java tools/pokemon/kiem_tai/BotXuyenSuot.java
 *   java -cp /tmp/bot -Ddb=htbb_db_xs -Dnoi=. -Dlog=<thu muc log server> -Dphut=120 -Ddong=0,1,2,3,4 \
 *        BotXuyenSuot 127.0.0.1 19179 xs [dieuphoi]
 * Ket qua: docs/kien-truc.md (-Dra=<tep markdown>).
 */
public class BotXuyenSuot {

    // ======================================================================== the gioi (du lieu tinh)
    static final Map<Integer, Integer> LOAD = new HashMap<>();          // mapId -> load
    static final Map<Integer, Set<Integer>> QUAI_MAP = new HashMap<>();  // template -> mapId (ban sao 0)
    static final Map<Integer, Integer> CAP_QUAI = new HashMap<>();       // template -> level
    static final Map<Integer, String> TEN_QUAI = new HashMap<>();
    static volatile short[][][] VI_TRI;
    static volatile short[][] NHOM;
    static volatile short[] ID_ARR;
    static final Set<Integer> MAP_THU = new TreeSet<>();                 // ban sao 0 cua moi nhom
    static String NOI;

    static void napTheGioi(BotChucNang.Bot b) throws IOException {
        NOI = BotChucNang.NOI;
        VI_TRI = b.viTriMap;
        NHOM = b.maIdNhom;
        ID_ARR = b.idArr;
        // mapId -> load tu RealController + Hoenn
        String rc = new String(Files.readAllBytes(BotChucNang.nguon("server/src/real/RealController.java").toPath()), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("mapList\\.put\\(new Integer\\((\\d+)\\), (?:m = )?new (\\w+)\\((\\d+), ([-\\d]+), ([-\\d]+), ([-\\d]+)").matcher(rc);
        while (m.find()) LOAD.put(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(6)));
        File fh = new File(BotChucNang.GOC, "server/src/com/htbb/pokemon/BanDoHoenn.java");
        if (fh.isFile()) {
            Matcher h = Pattern.compile("them\\((\\d+), \"").matcher(new String(Files.readAllBytes(fh.toPath()), StandardCharsets.UTF_8));
            while (h.find()) { int id = Integer.parseInt(h.group(1)); LOAD.put(id, id); }
        }
        for (short[] g : NHOM) if (g.length > 0 && g[0] >= 0) MAP_THU.add((int) g[0]);
        for (String d : BotChucNang.sql("SELECT id, level, name, he, IFNULL(he2, -1) FROM data_monster")) {
            String[] f = d.split("\t");
            if (f.length < 3) continue;
            CAP_QUAI.put(Integer.parseInt(f[0]), Integer.parseInt(f[1]));
            TEN_QUAI.put(Integer.parseInt(f[0]), f[2]);
            if (f.length >= 5) try { HE_QUAI.put(Integer.parseInt(f[0]), new int[]{Integer.parseInt(f[3].trim()), Integer.parseInt(f[4].trim())}); } catch (NumberFormatException ignored) { }
        }
        for (int mid : MAP_THU) {
            Integer l = LOAD.get(mid);
            if (l == null) continue;
            for (int t : luoiQuai(l)) QUAI_MAP.computeIfAbsent(t, z -> new TreeSet<>()).add(mid);
        }
    }

    /** template quai -> {he1, he2} (data_monster). */
    static final Map<Integer, int[]> HE_QUAI = new ConcurrentHashMap<>();

    /**
     * O chieu nguoi choi bam vao quai mau {@code tpl}: nhu nguoi choi nhin bieu tuong khac he - chi xoay cac chieu danh da
     * hoc (o 0..3; o 4 la buff / bi dong o nhieu dong) co he so khac he cao nhat (>= 85% chieu tot nhat), xen chieu khac
     * khi chi co mot. Truoc 29/10/2026 bot xoay mu 4 o dau (vd Charmander dung chieu Thuong vao quai Co thay vi chieu Lua).
     */
    static int chieuTheoHe(BotChucNang.Bot b, int lop, int tpl, int lan) {
        int[] he = HE_QUAI.get(tpl);
        int n = Math.min(4, b.chieu.length);
        List<Integer> co = new ArrayList<>();
        int[] gt = new int[n];
        int tot = 0;
        for (int k = 0; k < n; k++) {
            if (b.chieu[k] <= 0) continue;
            int hc = com.htbb.pokemon.ChieuPokemon.heChieu(lop, k);
            int v = 100;
            if (he != null && hc >= 0) v = com.htbb.pokemon.HePokemon.heSoTrongGame(hc, he[0], he[1]);
            gt[k] = v;
            tot = Math.max(tot, v);
            co.add(k);
        }
        if (co.isEmpty()) return 0;
        List<Integer> chon = new ArrayList<>();
        for (int k : co) if (gt[k] * 100 >= tot * 85) chon.add(k);
        if (chon.isEmpty()) chon = co;
        if (chon.size() == 1 && co.size() > 1 && lan % 2 == 1) { // mot chieu tot: xen chieu khac khi no dang hoi
            List<Integer> khac = new ArrayList<>(co);
            khac.remove(chon.get(0));
            return khac.get(Math.floorMod(lan / 2, khac.size()));
        }
        return chon.get(Math.floorMod(lan, chon.size()));
    }

    static Set<Integer> luoiQuai(int load) {
        Set<Integer> s = new TreeSet<>();
        try {
            File f = new File(NOI, "server/map/map" + load);
            if (!f.isFile()) f = new File(NOI, "server/map/map" + load);
            if (!f.isFile()) return s;
            byte[] b = Files.readAllBytes(f.toPath());
            int w = b[0] & 255, h = b[1] & 255;
            for (int i = 0; i < w * h && 2 + i < b.length; i++) {
                int v = b[2 + i] & 255;
                if (v > 0 && v != 255) s.add(load == 17 && v > 81 ? v + 3 : v);
            }
        } catch (IOException ignored) {
        }
        return s;
    }

    static final Map<Integer, List<int[]>> O_QUAI = new ConcurrentHashMap<>(); // load -> {id quai (= chi so o luoi), template}

    /** Moi o co quai trong luoi sinh: id quai server = so thu tu o co quai theo hang (MapNapTaiNguyen.loadMap). */
    static List<int[]> oQuai(int load) {
        return O_QUAI.computeIfAbsent(load, l -> {
            List<int[]> r = new ArrayList<>();
            try {
                File f = new File(NOI, "server/map/map" + l);
                if (!f.isFile()) f = new File(NOI, "server/map/map" + l);
                if (!f.isFile()) return r;
                byte[] b = Files.readAllBytes(f.toPath());
                int w = b[0] & 255, h = b[1] & 255;
                int id = 0;
                for (int i = 0; i < w * h && 2 + i < b.length; i++) {
                    int v = b[2 + i] & 255;
                    if (v > 0 && v != 255) r.add(new int[]{id++, l == 17 && v > 81 ? v + 3 : v, i % w, i / w});
                }
            } catch (IOException ignored) {
            }
            return r;
        });
    }

    /** Hoi thong tin (lenh 7) cac quai o luoi map hien tai (loc theo template t, -1 = tat ca); tra ve id con song. */
    static List<Integer> timQuaiLuoi(BotChucNang.Bot b, int t) throws IOException {
        List<Integer> l = new ArrayList<>();
        int n = 0;
        int px = b.px / 16, py = b.py / 16;
        for (int[] o : oQuai(b.mapLoad)) {
            if (t >= 0 && o[1] != t) continue;
            if (Math.abs(o[2] - px) > 14 || Math.abs(o[3] - py) > 14) continue; // server chi tra quai o gan
            b.quai.remove(o[0]);
            b.gui(7, (short) o[0]);
            if (++n % 25 == 0) b.bom(80);
            if (n >= 150) break;
        }
        b.bom(700);
        for (int[] o : oQuai(b.mapLoad)) {
            int[] q = b.quai.get(o[0]);
            if (q != null && q[2] > 0 && (t < 0 || o[1] == t)) l.add(o[0]);
        }
        return l;
    }

    static int nhom(int mapId) {
        if (NHOM == null) return -1;
        for (int i = 0; i < NHOM.length; i++) for (short s : NHOM[i]) if (s == mapId) return ID_ARR[i];
        return -1;
    }

    static int cot(int mapId) { // ban sao thu may trong nhom
        for (short[] g : NHOM) for (int k = 0; k < g.length; k++) if (g[k] == mapId) return k;
        return 0;
    }

    /** Canh: tu mapId -> (gate index -> dich mapId). */
    static List<int[]> canh(int mapId) {
        List<int[]> l = new ArrayList<>();
        Integer load = LOAD.get(mapId);
        if (load == null) return l;
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(load);
        int n = nhom(mapId), k = cot(mapId);
        if (bd == null || n < 0 || VI_TRI == null || n >= VI_TRI.length) return l;
        for (int gi = 0; gi < bd.cong.size(); gi++) {
            int[] g = bd.cong.get(gi);
            if (KichBanDS2.laOffline(g[2]) || g[2] >= VI_TRI[n].length) continue;
            short[] loc = VI_TRI[n][g[2]];
            int kk = Math.min(k, loc.length / 3 - 1);
            if (loc[kk * 3] < 0) kk = 0;
            if (loc[kk * 3] < 0) continue;
            l.add(new int[]{gi, loc[kk * 3], loc[kk * 3 + 1], loc[kk * 3 + 2]});
        }
        return l;
    }

    /** Chi so nhom (hang trong bang 105) cua map; -1 neu khong co. Cac ban sao cung nhom = cung ban do. */
    static int hang(int mapId) {
        if (NHOM == null) return -1;
        for (int i = 0; i < NHOM.length; i++) for (short x : NHOM[i]) if (x == mapId) return i;
        return -1;
    }

    static boolean cungBanDo(int a, int b2) {
        if (a == b2) return true;
        int ha = hang(a);
        return ha >= 0 && ha == hang(b2);
    }

    /** Duong qua cong ngan nhat (BFS so map) toi den HOAC mot ban sao cua den. {gate, dich, x, y} hoac null. */
    static List<int[]> duong(int tu, int den) {
        if (cungBanDo(tu, den)) return new ArrayList<>();
        Map<Integer, int[]> truoc = new HashMap<>();
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(tu);
        truoc.put(tu, null);
        while (!q.isEmpty()) {
            int c = q.poll();
            for (int[] e : canh(c)) {
                if (truoc.containsKey(e[1])) continue;
                truoc.put(e[1], new int[]{c, e[0], e[1], e[2], e[3]});
                if (cungBanDo(e[1], den)) {
                    LinkedList<int[]> r = new LinkedList<>();
                    for (int x = e[1]; truoc.get(x) != null; x = truoc.get(x)[0]) r.addFirst(Arrays.copyOfRange(truoc.get(x), 1, 5));
                    return r;
                }
                q.add(e[1]);
            }
        }
        return null;
    }

    // ======================================================================== dieu phoi dong ho
    static volatile BotChucNang.Bot dieuPhoi;
    static final Object KHOA_DH = new Object();
    static volatile long lanTuaCuoi = 0;
    static volatile long tongTuaPhut = 0;
    static final List<String> NHAT_KY_TUA = Collections.synchronizedList(new ArrayList<>());

    /** Tua dong ho ao (moi lan toi da 1 lan / 20 giay cho tat ca cac dong). */
    static void tua(String thamSo, String lyDo) {
        synchronized (KHOA_DH) {
            if (dieuPhoi == null || System.currentTimeMillis() - lanTuaCuoi < 20000) return;
            try {
                int m = dieuPhoi.moc();
                dieuPhoi.chat("tua " + thamSo);
                String t = dieuPhoi.doiChu(m, 3000, "tua", "Đã tua", "giờ ảo");
                lanTuaCuoi = System.currentTimeMillis();
                long phut = thamSo.endsWith("d") ? Long.parseLong(thamSo.replace("d", "")) * 1440 : thamSo.endsWith("h") ? Long.parseLong(thamSo.replace("h", "")) * 60 : 0;
                tongTuaPhut += phut;
                NHAT_KY_TUA.add(new Date() + " tua " + thamSo + " (" + lyDo + "): " + t);
                System.out.println("[DIEU PHOI] tua " + thamSo + " vi " + lyDo + " -> " + t);
            } catch (Exception e) {
                System.out.println("[DIEU PHOI] loi tua: " + e);
            }
        }
    }

    // ======================================================================== nhat ky loi server
    static File tepLog;
    static long viTriLog;
    static final List<String> LOI_SERVER = Collections.synchronizedList(new ArrayList<>());

    static synchronized void quetLog() {
        if (tepLog == null) {
            File d = new File(System.getProperty("log", "log"), "nhat_ky");
            File[] fs = d.listFiles((x, n) -> n.startsWith("may_chu-"));
            if (fs == null || fs.length == 0) return;
            Arrays.sort(fs);
            tepLog = fs[fs.length - 1];
            viTriLog = tepLog.length();
            return;
        }
        try (RandomAccessFile r = new RandomAccessFile(tepLog, "r")) {
            if (r.length() < viTriLog) viTriLog = 0;
            r.seek(viTriLog);
            String l;
            while ((l = r.readLine()) != null) {
                String u = new String(l.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                if (u.length() > 19 && u.charAt(19) == 'E' && u.contains(" E ")) LOI_SERVER.add(u.length() > 220 ? u.substring(0, 220) : u);
            }
            viTriLog = r.getFilePointer();
        } catch (IOException ignored) {
        }
    }

    // ======================================================================== mot dong choi
    /**
     * Vu khi cua hang dung bac cap cho dong (class) {@code lop} o cap {@code lv}: cap 1..35 = 79 + 7*lop + bac (bac 5 cap),
     * cap 36..75 = 174 + 8*lop + bac (Than lua / Hat giong / Khan lua / Nuoc than / Nam cham vang 36 .. tinh anh 71).
     * Truoc 29/10/2026 bot (va do suc) dung 174 cho MOI dong o cap >= 36 va dong 0 (lua) cho do suc - sai voi 4 dong kia.
     */
    static int vuKhiShop(int lop, int lv) {
        if (lv >= 36) return 174 + 8 * lop + Math.min(7, (lv - 36) / 5);
        return 79 + 7 * lop + Math.min(6, Math.max(0, (lv - 1) / 5));
    }

    static class Dong implements Runnable {
        final int lop;
        final String user;
        BotChucNang.Bot b;
        final long hetGio;
        // thong ke
        int soVaoLai, capDau, capCao, soNvChinh, soNvPhu, soGym, soTienHoa, soChet, soHaQuai, soBuoc, soDoiMap;
        final Set<String> huyHieu = new TreeSet<>();
        final List<String> ket = new ArrayList<>();      // ly do ket (lap lai = 1 dong)
        final List<String> loi = new ArrayList<>();      // vi pham kiem tra
        final List<String> nhatKy = new ArrayList<>();
        long batDau = System.currentTimeMillis();
        long lanTienTrien = System.currentTimeMillis();
        int capTruoc = 0;
        long ptTruoc = 0;
        String nvChinh = "";            // ten nhiem vu chinh dang lam
        int nvChinhId = -1, nvChinhType = -1, nvChinhLv = 0;
        final List<int[]> mucTieu = new ArrayList<>(); // {template, da, tong} hoac {-1, da, tong} (type 4)
        final Set<Integer> daToi = new HashSet<>();
        boolean xong;
        /** danh(-1): chi danh quai cap >= cap minh - lechDuoi (nhiem vu "chenh khong qua N cap"); mac dinh khong gioi han. */
        int lechDuoi = 999;

        Dong(int lop, String user, long hetGio) {
            this.lop = lop;
            this.user = user;
            this.hetGio = hetGio;
        }

        void ghi(String s) {
            String d = String.format("[%s %s lv%d map%d] %s", user, new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()), b == null ? 0 : b.lv, b == null ? -1 : b.mapId, s);
            System.out.println(d);
            synchronized (nhatKy) { nhatKy.add(d); if (nhatKy.size() > 400) nhatKy.remove(0); }
        }

        void ketLyDo(String s) {
            if (!ket.contains(s)) ket.add(s);
            ghi("KET: " + s);
        }

        @Override
        public void run() {
            try {
                vao();
                capDau = b.lv;
                while (System.currentTimeMillis() < hetGio && !xong) {
                    try {
                        vongChinh();
                    } catch (java.net.SocketException | EOFException e) {
                        // server tu khoi dong lai (lich su kien / bao tri): vao lai nhu nguoi choi
                        soVaoLai++;
                        ghi("mat ket noi (" + e + ") -> cho server, vao lai");
                        vaoLai();
                    }
                }
            } catch (Throwable e) {
                loi.add("ngoai le bot: " + e);
                ghi("NGOAI LE " + e);
                e.printStackTrace(System.out);
            } finally {
                if (b != null) b.close();
            }
        }

        void vao() throws Exception {
            String l = null;
            for (int lan = 0; lan < 40; lan++) { // server co the dang khoi dong lai
                b = new BotChucNang.Bot(user);
                try {
                    l = b.dangNhap("123456");
                    if (l == null) l = b.vaoGame(lop);
                } catch (IOException e) {
                    l = e.toString();
                }
                if (l == null) break;
                b.close();
                Thread.sleep(6000);
            }
            if (l != null) throw new IllegalStateException("vao game: " + l);
            ngheDon();
            KichBanDS.veLang(b);
            b.lamMoi();
            capTruoc = b.lv;
            // huy hieu da co (ho so dai han tob_char_lau_dai.du_lieu: gh = bit Gym da ha) - khong danh lai Gym cu
            String hs = BotChucNang.sql1("SELECT du_lieu FROM tob_char_lau_dai WHERE id_char=" + b.charDb);
            if (hs != null) {
                Matcher gm = Pattern.compile("(?:^|;)gh=(\\d+)").matcher(hs);
                if (gm.find()) {
                    long bit = Long.parseLong(gm.group(1));
                    List<String> ten = BotChucNang.sql("SELECT huy_hieu FROM data_noi_dung_gym ORDER BY stt");
                    for (int i = 0; i < ten.size(); i++) if ((bit >> i & 1) == 1) huyHieu.add(ten.get(i));
                }
            }
            ghi("vao game lop " + lop + " map " + b.mapId + " (" + b.tenMap + ") chieu " + Arrays.toString(b.chieu));
        }

        void vaoLai() throws Exception {
            b.close();
            Thread.sleep(3000);
            vao();
        }

        // ---------------------------------------------------------------- kiem tra sau moi buoc
        void kiem() throws Exception {
            soBuoc++;
            if (b.dut || !b.conSong()) {
                loi.add("rot ket noi o buoc " + soBuoc + " (map " + b.mapId + ")");
                ghi("rot ket noi -> vao lai");
                vaoLai();
            }
            if (b.lv < capTruoc) loi.add("cap GIAM " + capTruoc + " -> " + b.lv);
            if (b.lv > capTruoc || b.pt != ptTruoc) lanTienTrien = System.currentTimeMillis();
            if (b.lv > capTruoc) { ghi("len cap " + b.lv); congDiem(); }
            capTruoc = b.lv;
            ptTruoc = b.pt;
            capCao = Math.max(capCao, b.lv);
            if (b.hp <= 0) hoiSinh();
        }

        void congDiem() throws IOException {
            // tiem nang -> suc manh; ky nang -> chieu 0..3 (o co cap > 0 hoac chieu ke tiep)
            b.lamMoi();
            b.gui(34, (byte) 0, (short) 5);
            b.bom(200);
            for (int o = 0; o < 4; o++) { b.gui(36, (byte) o); b.bom(120); }
            b.lamMoi();
        }

        /** Don quai trung minh gan day (goi 10): {luc ms, mau quai, sat thuong, hp con} - in khi guc (can bang cap 26/10/2026). */
        final java.util.ArrayDeque<int[]> donGan = new java.util.ArrayDeque<>();
        long donGoc = System.currentTimeMillis();

        void ngheDon() {
            final BotChucNang.Bot bb = b;
            final java.util.function.Consumer<BotChucNang.SuKien> cu = bb.ngheNhan;
            bb.ngheNhan = e -> {
                if (cu != null) cu.accept(e);
                if (e.cmd != 10 || e.d == null || e.d.length < 12) return;
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    int mq = r.readShort(), tg = r.readShort(), dmg = r.readInt(), hp = r.readInt();
                    if (tg != bb.actor) return;
                    int[] q = bb.quai.get(mq);
                    synchronized (donGan) {
                        donGan.addLast(new int[]{(int) (System.currentTimeMillis() - donGoc), q == null ? -1 : q[5], dmg, hp});
                        while (donGan.size() > 12) donGan.removeFirst();
                    }
                } catch (IOException ignored) {
                }
            };
        }

        String tomTatDon() {
            StringBuilder sb = new StringBuilder();
            synchronized (donGan) {
                long cuoi = donGan.isEmpty() ? 0 : donGan.peekLast()[0];
                for (int[] d : donGan) sb.append(String.format("%.1fs:%s-%d(con %d) ", (d[0] - cuoi) / 1000.0, TEN_QUAI.getOrDefault(d[1], "?"), d[2], d[3]));
                donGan.clear();
            }
            return sb.toString();
        }

        void hoiSinh() throws Exception {
            soChet++;
            ghi("chet -> ve lang | maxhp " + b.maxhp + " | don cuoi: " + tomTatDon());
            int m = b.moc();
            b.gui(31, new byte[0]);
            b.cho(x -> x.coLenh(m, 12), 6000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(800);
            b.lamMoi();
            hoiMau("sau khi ve lang");
        }

        /**
         * Nguoi choi binh thuong: ve lang voi it mau thi uong thuoc truoc khi ra ngoai (truoc day bot ra ngay voi ~5% HP, guc
         * lien tiep o cung buoc - bao cao can bang cap 26/10/2026). Toi da 8 binh, dung khi >= 70% HP / het thuoc.
         */
        void hoiMau(String vi) throws IOException {
            int uong = 0, hp0 = b.hp;
            for (int lan = 0; lan < 8 && b.hp > 0 && b.hp < b.maxhp * 7 / 10; lan++) {
                int t = b.soThuoc(3) > 0 ? 3 : b.soThuoc(2) > 0 ? 2 : b.soThuoc(1) > 0 ? 1 : -1;
                if (t < 0) break;
                b.gui(22, (byte) t);
                b.bom(700);
                b.lamMoi();
                uong++;
            }
            if (uong > 0) ghi("hoi mau " + vi + ": " + uong + " binh, HP " + hp0 + " -> " + b.hp + "/" + b.maxhp);
        }

        long thuocLanCuoi = 0;
        int vuKhiDangMac = -1;

        /** Het thuoc hoi mau: ve lang, vao nha, mua 20 binh (shop NPC lenh 24), ra lai. */
        void muaThuoc() throws Exception {
            boolean canVk = vuKhiShop(lop, b.lv) != vuKhiDangMac;
            if (!canVk && b.soThuoc(1) + b.soThuoc(2) + b.soThuoc(3) >= 4 || System.currentTimeMillis() - thuocLanCuoi < 5 * 60000L || b.xu < 2000) return;
            thuocLanCuoi = System.currentTimeMillis();
            int lang = b.mapId;
            if (!KichBanDS.laLang(lang)) {
                int[] cac = {70, 0, 80, 1701, 301, 1901};
                int tot = -1, ds = 999;
                for (int c : cac) { List<int[]> d = duong(b.mapId, c); if (d != null && d.size() < ds) { ds = d.size(); tot = c; } }
                if (tot < 0 || !toi(tot)) { ghi("khong ve duoc lang de mua thuoc"); return; }
            }
            int lv = b.lv >= 30 ? 3 : b.lv >= 10 ? 2 : 1;
            if (!KichBanDS2.vaoNha(b, 103)) { ghi("khong vao duoc nha shop"); return; }
            int t0 = b.soThuoc(lv);
            b.gui(24, (byte) 1, (byte) 4, (short) lv, (short) 20);
            b.bom(1200);
            b.lamMoi();
            ghi("mua thuoc " + lv + ": " + t0 + " -> " + b.soThuoc(lv) + " (xu " + b.xu + ")");
            // vu khi shop theo bac cap (79 + 7*lop: cap 1,6,11,...,31) - nguoi choi that mua do o tiem
            int tpl = vuKhiShop(lop, b.lv);
            if (tpl != vuKhiDangMac) {
                int id = KichBanDS3.muaVuKhi(b, tpl);
                if (id != KichBanDS3.KHONG) {
                    b.gui(29, (short) id);
                    b.bom(800);
                    if (b.timDo(x -> x[0] == id) == null) { vuKhiDangMac = tpl; ghi("mac vu khi moi tpl " + tpl); }
                }
            }
            KichBanDS2.raNgoai(b);
        }

        /** HP < 50%: uong binh tot nhat (toi da 3 binh lien, dung khi >= 50% / het thuoc) - nhu nguoi choi bam thuoc. */
        void uongThuoc() throws IOException {
            for (int lan = 0; lan < 3 && b.hp > 0 && b.hp < b.maxhp / 2; lan++) {
                int t = b.soThuoc(3) > 0 ? 3 : b.soThuoc(2) > 0 ? 2 : b.soThuoc(1) > 0 ? 1 : -1;
                if (t < 0) return;
                b.gui(22, (byte) t);
                b.bom(300);
                b.lamMoi();
            }
        }

        // ---------------------------------------------------------------- di chuyen giua cac map
        boolean toi(int dich) throws Exception {
            if (cungBanDo(b.mapId, dich)) return true;
            int tu = b.mapId;
            if (KichBanDS2.laOffline(tu) || !LOAD.containsKey(tu)) { KichBanDS2.raNgoai(b); tu = b.mapId; }
            List<int[]> d = duong(tu, dich);
            if (d == null) { ketLyDo("khong co duong tu map " + tu + " toi " + dich); return false; }
            for (int[] e : d) {
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                if (bd == null || e[0] >= bd.cong.size()) { ketLyDo("ban do " + b.mapLoad + " khong co cong " + e[0]); return false; }
                int[] g = bd.cong.get(e[0]);
                int[] dung = bd.oTruocCong(g[0], g[1]);
                b.px = b.px <= 0 ? b.tx * 16 + 8 : b.px;
                if (dung == null || !b.diDuong(bd, dung[0], dung[1])) {
                    // vi tri bot chua dong bo (moi vao map): dung o vao
                    b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8;
                    if (dung == null || !b.diDuong(bd, dung[0], dung[1])) { ketLyDo("khong di bo toi cong " + e[0] + " map " + b.mapId); return false; }
                }
                int truoc = b.mapId, m = b.moc();
                b.gui(12, (short) e[1], (short) e[2], (short) e[3]);
                b.cho(x -> x.coLenh(m, 12) || x.dut, 6000);
                if (b.dut || !b.conSong()) { loi.add("BI NGAT khi qua cong " + truoc + "->" + e[1]); vaoLai(); return false; }
                b.gui(12, (short) -500, (short) 0, (short) 0);
                b.bom(600);
                b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8;
                soDoiMap++;
                // nguoi choi that uong thuoc khi di duong voi it mau (bao cao can bang 29/10/2026: bot di qua 3-4 ban do quai
                // voi 20% HP roi guc giua duong - ket qua do bot, khong phai do game)
                b.lamMoi();
                if (b.hp <= 0) { hoiSinh(); return false; }
                uongThuoc();
                if (b.mapId == truoc) {
                    String t = b.chuTu(m);
                    ketLyDo("cong " + truoc + "->" + e[1] + " khong cho qua: " + (t.length() > 160 ? t.substring(0, 160) : t));
                    return false;
                }
                if (b.mapId != e[1]) ghi("qua cong xin " + e[1] + " -> toi " + b.mapId);
            }
            return cungBanDo(b.mapId, dich);
        }

        // ---------------------------------------------------------------- danh quai tai map hien tai
        /** Ha toi da n quai (template t, -1 = bat ky hop cap) trong map hien tai; tra ve so ha. */
        int danh(int t, int n, long ms) throws Exception {
            long het = System.currentTimeMillis() + ms;
            int ha = 0;
            while (ha < n && System.currentTimeMillis() < het && !b.dut) {
                List<Integer> ds = timQuaiLuoi(b, t);
                Integer chon = null;
                double tot = 1e18;
                for (int id : ds) {
                    int[] q = b.quai.get(id);
                    if (q == null || q[2] <= 0) continue;
                    if (t < 0 && (q[3] > b.lv + 4 || q[3] < b.lv - lechDuoi)) continue;
                    double kc = Math.hypot(q[0] - b.px, q[1] - b.py);
                    if (kc < tot) { tot = kc; chon = id; }
                }
                if (chon == null) {
                    // di toi o sinh gan nhat cua loai can (chua toi gan day) roi hoi lai
                    int[] toi = null;
                    double kc = 1e18;
                    for (int[] o : oQuai(b.mapLoad)) {
                        if (t >= 0 && o[1] != t) continue;
                        if (t < 0) { Integer c = CAP_QUAI.get(o[1]); if (c == null || c > b.lv + 4 || c < b.lv - lechDuoi) continue; }
                        if (daToi.contains(o[2] / 8 * 1000 + o[3] / 8)) continue;
                        double d = Math.hypot(o[2] - b.px / 16, o[3] - b.py / 16);
                        if (d < kc) { kc = d; toi = o; }
                    }
                    if (toi == null) { daToi.clear(); b.bom(2500); continue; }
                    daToi.add(toi[2] / 8 * 1000 + toi[3] / 8);
                    BotChucNang.BanDo bd0 = BotChucNang.BanDo.nap(b.mapLoad);
                    int[] o = bd0 == null ? null : bd0.oGan(toi[2], toi[3], 3);
                    if (o != null && !b.diDuong(bd0, o[0], o[1])) { b.px = b.tx * 16 + 8; b.py = b.ty * 16 + 8; }
                    continue;
                }
                int[] q = b.quai.get(chon);
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                if (bd != null) {
                    int[] o = bd.oGan(q[0] / 16, q[1] / 16, 2);
                    if (o != null) b.diDuong(bd, o[0], o[1]);
                }
                int m = b.moc();
                int don = 0;
                for (; don < 120 && !b.dut; don++) {
                    int[] qt = b.quai.get(chon);
                    b.gui(9, (short) (int) chon, (byte) chieuTheoHe(b, lop, qt == null ? -1 : qt[5], don));
                    b.bom(260);
                    if (don % 3 == 2) { b.gui(7, (short) (int) chon); b.bom(150); }
                    int[] q2 = b.quai.get(chon);
                    if (q2 != null && q2[2] == 0) break;
                    if (q2 != null && Math.hypot(q2[0] - b.px, q2[1] - b.py) > 60 && bd != null && don % 6 == 5) {
                        int[] o = bd.oGan(q2[0] / 16, q2[1] / 16, 2);
                        if (o != null) b.diDuong(bd, o[0], o[1]);
                    }
                    if (b.hp > 0 && b.hp < b.maxhp / 2) uongThuoc(); // HP song tu goi 10 (quai danh minh)
                    if (don % 10 == 9) { b.lamMoi(); uongThuoc(); if (b.hp <= 0) break; }
                }
                int[] q3 = b.quai.get(chon);
                if (q3 != null && q3[2] == 0) { ha++; soHaQuai++; nhatDo(m); }
                kiem();
                if (b.hp <= 0) { hoiSinh(); return ha; }
            }
            return ha;
        }

        /** Nhat do roi (lenh 17: danh sach roi) gan nguoi choi. */
        void nhatDo(int tu) throws IOException {
            for (int i = tu; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd != 17 || e.d == null) continue;
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    r.readShort(); r.readShort(); r.readByte(); r.readInt(); r.readByte();
                    int n = r.readByte();
                    for (int k = 0; k < n; k++) {
                        int cat = r.readByte(), tpl = r.readShort(), id = r.readShort(), x = r.readShort(), y = r.readShort();
                        if (cat == 3) b.gui(18, (short) id);
                        else if (cat == 4) b.gui(19, (short) id);
                        else if (cat == 6 || cat == 7) b.gui(-41, (byte) cat, (short) id);
                        else if (cat == 14) b.gui(-65, (short) id);
                    }
                } catch (IOException ignored) {
                }
            }
            b.bom(200);
        }

        /** Map tot nhat de luyen o cap hien tai (quai trong [lv-2, lv+3]) - gan nhat theo so cong. */
        int mapLuyen() {
            int tot = -1, diem = Integer.MAX_VALUE;
            for (int mid : MAP_THU) {
                Integer l = LOAD.get(mid);
                if (l == null || KichBanDS2.laOffline(mid)) continue;
                for (int t : luoiQuai(l)) {
                    Integer c = CAP_QUAI.get(t);
                    if (c == null) continue;
                    int lech = c - b.lv;
                    if (lech < -3 || lech > 3) continue;
                    List<int[]> d = duong(b.mapId, mid);
                    if (d == null) continue;
                    int s = d.size() * 2 + Math.abs(lech);
                    if (s < diem) { diem = s; tot = mid; }
                }
            }
            return tot;
        }

        void luyen(int phut) throws Exception {
            int mid = mapLuyen();
            if (mid < 0) { ketLyDo("khong tim duoc map luyen cho cap " + b.lv); b.bom(5000); return; }
            if (!toi(mid)) return;
            danh(-1, 40, phut * 60000L);
        }

        // ---------------------------------------------------------------- nhiem vu chinh (-64)
        /** Doc danh sach nhiem vu moi nhat (lenh -64 type 0/1/2) tu luot su kien. */
        List<Object[]> dsNv(int loai) {
            for (int i = b.su.size() - 1; i >= 0; i--) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd != -64 || e.d == null || e.d.length < 2 || e.d[0] != loai) continue;
                List<Object[]> l = new ArrayList<>();
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    r.readByte();
                    int n = r.readUnsignedByte();
                    for (int k = 0; k < n; k++) {
                        int id = r.readShort(), ms = r.readByte();
                        String ten = r.readUTF();
                        Object[] q = new Object[]{id, ms, ten, -1, null};
                        if (loai == 0) { r.readByte(); r.readUTF(); q[3] = (int) r.readByte(); r.readByte(); }
                        else if (loai == 1) { r.readByte(); r.readUTF(); r.readUTF(); r.readUTF(); }
                        else {
                            int ty = r.readByte();
                            q[3] = ty;
                            r.readUTF(); r.readByte(); r.readByte(); r.readUTF();
                            List<int[]> mt = new ArrayList<>();
                            if (ty == 0 || ty == 2) {
                                int c = r.readByte();
                                for (int j = 0; j < c; j++) mt.add(new int[]{r.readShort(), r.readShort(), r.readShort()});
                            } else if (ty == 4) {
                                mt.add(new int[]{-1, r.readShort(), r.readShort()});
                            }
                            q[4] = mt;
                        }
                        l.add(q);
                    }
                } catch (IOException ignored) {
                }
                return l;
            }
            return null;
        }

        /** Nhan/tra lien tiep cac nhiem vu noi chuyen/giao (type 3/1) - nguoi that bam lien tuc, khong di luyen. */
        String nvChinh() throws Exception {
            String t = "";
            for (int lan = 0; lan < 8; lan++) {
                int truoc = soNvChinh;
                t = nvChinhMot();
                if ((nvChinhType == 3 || nvChinhType == 1) || soNvChinh > truoc && nvChinhType != 0 && nvChinhType != 2 && nvChinhType != 4) continue;
                break;
            }
            return t;
        }

        String nvChinhMot() throws Exception {
            // 1) tra nhiem vu da xong. Bao loi 25/10 (bot-cap1-40): truoc day gui id 0 (ca nhan lan tra) - may chu chi nhan
            // dung id nhiem vu chinh dang moi / dang lam (Char.xuLyNhiemVu), nen bot KHONG BAO GIO nhan/tra duoc nhiem vu chinh
            // nao (cot_truyen "nv chinh 0" van PASS). Nay: lay id that tu goi -64 (nhom 1 = xong, nhom 0 = co the nhan).
            int m = b.moc();
            b.gui(-64, (byte) 1, (short) -1, (byte) 0); // id sai co y: may chu chi gui lai 3 danh sach (tuChoiNhiemVu(null))
            b.bom(900);
            List<Object[]> xong = dsNv(1);
            Object[] qx = null;
            if (xong != null) for (Object[] q : xong) if ((int) q[1] == 0) qx = q;
            m = b.moc();
            if (qx != null) { b.gui(-64, (byte) 1, (short) (int) qx[0], (byte) 0); b.bom(1200); }
            String t = b.chuTu(m);
            if (qx != null && (t.contains("hoàn thành") || t.contains("Hoàn thành") || t.contains("Nhận được") || t.contains("nhận thưởng"))) {
                soNvChinh++;
                lanTienTrien = System.currentTimeMillis();
                ghi("TRA nhiem vu chinh: " + t.replace('\n', ' ').substring(0, Math.min(140, t.length())));
            }
            // 2) nhan nhiem vu chinh moi (dung id; noi chuyen type 3: client nghe het loi thoai la gui thang "tra")
            List<Object[]> moi = dsNv(0);
            Object[] qm = null;
            if (moi != null) for (Object[] q : moi) if ((int) q[1] == 0) qm = q;
            m = b.moc();
            if (qm != null) {
                boolean noiChuyen = (int) qm[3] == 3;
                b.gui(-64, (byte) (noiChuyen ? 1 : 0), (short) (int) qm[0], (byte) 0);
                b.bom(1200);
                if (noiChuyen && b.chuTu(m).contains("hoàn thành")) { soNvChinh++; lanTienTrien = System.currentTimeMillis(); }
            } else {
                b.gui(-64, (byte) 1, (short) -1, (byte) 0);
                b.bom(900);
            }
            t = b.chuTu(m);
            List<Object[]> dang = dsNv(2);
            mucTieu.clear();
            nvChinhType = -1;
            if (dang != null) for (Object[] q : dang) if ((int) q[1] == 0) {
                nvChinhId = (int) q[0];
                nvChinh = (String) q[2];
                nvChinhType = (int) q[3];
                @SuppressWarnings("unchecked") List<int[]> mt = (List<int[]>) q[4];
                if (mt != null) mucTieu.addAll(mt);
            }
            return t;
        }

        /**
         * Quai roi vat {@code vat} cua nhiem vu thu thap {@code idNv} (data_quest_moi: idItemget / idMonskill CSV cung vi tri;
         * {@code k} = vi tri muc tieu trong goi -64). -1 = khong co quai roi (buoc hanh dong) / khong doc duoc.
         */
        static int quaiRoiVat(int idNv, int vat, int k) {
            String d = BotChucNang.sql1("SELECT idItemget, idMonskill FROM data_quest_moi WHERE id = " + idNv);
            if (d == null || d.startsWith("LOI SQL")) return -1;
            String[] f = d.split("\t");
            if (f.length < 2) return -1;
            String[] vp = f[0].split(","), qu = f[1].split(",");
            int vt = -1;
            for (int i = 0; i < vp.length; i++) if (vp[i].trim().equals(String.valueOf(vat))) { vt = i; break; }
            if (vt < 0) vt = k;
            if (vt < 0 || vt >= qu.length) return -1;
            try {
                int q = Integer.parseInt(qu[vt].trim());
                return q >= 0 && q < 32000 && TEN_QUAI.containsKey(q) ? q : -1;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        /** Lam muc tieu nhiem vu chinh dang co. Tra ve false neu khong co gi de lam. */
        boolean lamNvChinh() throws Exception {
            if (nvChinhType == 3 || nvChinhType == 1) return false; // noi chuyen / giao: tra ngay o buoc tra
            if (mucTieu.isEmpty()) return false;
            for (int k = 0; k < mucTieu.size(); k++) {
                int[] mt = mucTieu.get(k);
                if (mt[1] >= mt[2]) continue;
                int t = mt[0];
                if (t < 0) { // ha quai lech cap
                    luyen(4);
                    return true;
                }
                if (nvChinhType == 2) {
                    // Nhiem vu THU THAP (type 2): goi -64 gui {VAT PHAM, co, can} - KHONG phai id quai (MessageCreator
                    // createMsgCharQuest). Cong kiem 25/10/2026: bot coi vat 53 (dau tap "Dùng 1 bình thuốc" cua huong dan)
                    // la quai 53 -> "khong co duong toi map co quai 53". Quai roi vat = data_quest_moi.idMonskill (cung vi tri
                    // voi idItemget); khong co quai (32000) = buoc HANH DONG cua huong dan tan thu (dung thuoc / cong diem /
                    // mac do / tha cau) -> lam nhu nguoi choi (KichBanHuongDan.hanhDong).
                    int quai = quaiRoiVat(nvChinhId, t, k);
                    if (quai < 0) {
                        int m = b.moc();
                        String l = KichBanHuongDan.hanhDong(b, t, m);
                        if (l != null) { ketLyDo("nhiem vu '" + nvChinh + "' (vat " + t + ", khong quai roi): " + l); return false; }
                        ghi("nv '" + nvChinh + "' buoc hanh dong vat " + t + " xong");
                        lanTienTrien = System.currentTimeMillis();
                        return true;
                    }
                    t = quai;
                }
                Set<Integer> maps = QUAI_MAP.get(t);
                if (maps == null || maps.isEmpty()) { ketLyDo("nhiem vu '" + nvChinh + "': quai " + t + " (" + TEN_QUAI.get(t) + ") khong co tren map nao"); return false; }
                int tot = -1, ds = Integer.MAX_VALUE;
                for (int mid : maps) { List<int[]> d = duong(b.mapId, mid); if (d != null && d.size() < ds) { ds = d.size(); tot = mid; } }
                if (tot < 0) { ketLyDo("nhiem vu '" + nvChinh + "': khong co duong toi map co quai " + t); return false; }
                Integer cq = CAP_QUAI.get(t);
                if (cq != null && cq > b.lv + 6) { ghi("quai " + t + " cap " + cq + " qua cao, luyen truoc"); luyen(4); return true; }
                if (!toi(tot)) return false;
                int truoc = mt[1];
                int ha = danh(t, Math.min(10, mt[2] - mt[1]), 4 * 60000L);
                ghi("nv '" + nvChinh + "' ha " + ha + " " + TEN_QUAI.get(t) + " (" + truoc + "/" + mt[2] + ")");
                return true;
            }
            return false;
        }

        // ---------------------------------------------------------------- So tay: Gym, nhiem vu phu/ngay
        int gymDaThu = 0;
        final Map<String, Integer> gymThuLuc = new HashMap<>(); // ten Gym -> cap luc thu (thua thi luyen them 3 cap)
        final List<String> gymKetQua = new ArrayList<>();
        long gymLanCuoi = 0;

        void gym() throws Exception {
            if (System.currentTimeMillis() - gymLanCuoi < 3 * 60000L) return;
            gymLanCuoi = System.currentTimeMillis();
            int m = KichBanDS4.chonSoTay(b, 7);
            if (b.doiMenu(m, -527, 5, 2000) <= 0) return;
            List<String> ds = new ArrayList<>(b.menuChu);
            for (int i = 0; i < ds.size(); i++) {
                Matcher mm = Pattern.compile("cấp (\\d+)").matcher(ds.get(i));
                int cap = mm.find() ? Integer.parseInt(mm.group(1)) : 999;
                String ten = ds.get(i).replaceAll(" - cấp.*", "");
                if (huyHieu.contains(ten) || b.lv < cap + 4 || b.lv < gymThuLuc.getOrDefault(ten, 0) + 3) continue;
                gymThuLuc.put(ten, b.lv);
                // ra map ngoai lang (boss trieu hoi canh nguoi choi)
                if (KichBanDS.laLang(b.mapId) || KichBanDS2.laOffline(b.mapId)) { int ml = mapLuyen(); if (ml >= 0) toi(ml); }
                KichBanDS.timQuai(b, 0, 260);
                Set<Integer> cu = new HashSet<>(b.quai.keySet());
                KichBanDS4.chonSoTay(b, 7);
                int m2 = b.moc();
                b.menu(-527, 5, i);
                String t = b.doiChu(m2, 2500, "");
                ghi("Gym '" + ten + "': " + t);
                if (t == null || !t.contains("xuất hiện")) {
                    if (t != null && (t.contains("tuần") || t.contains("đã có"))) huyHieu.add(ten);
                    continue;
                }
                Integer boss = KichBanDS.quaiMoi(b, cu, 8000);
                if (boss == null) { ketLyDo("Gym " + ten + ": khong thay boss"); return; }
                long t0 = System.currentTimeMillis();
                int don = KichBanDS.haQuai(b, boss, -1, 2600);
                String kq = b.doiChu(m2, 3000, "Chiến thắng", "Đánh lại Gym thành công", "thua", "hết giờ", "Hết giờ");
                ghi("Gym '" + ten + "' ket qua: " + kq + " (" + don + " don, " + (System.currentTimeMillis() - t0) / 1000 + " giay)");
                gymKetQua.add(ten + " cap " + b.lv + ": " + (kq != null && kq.contains("Chiến thắng") ? "THANG" : "thua") + " sau " + (System.currentTimeMillis() - t0) / 1000 + " giay, " + don + " don");
                if (kq != null && (kq.contains("Chiến thắng") || kq.contains("Đánh lại"))) { huyHieu.add(ten); if (kq.contains("Chiến thắng")) soGym++; lanTienTrien = System.currentTimeMillis(); }
                return; // mot Gym moi lan
            }
        }

        long phuLanCuoi = 0;

        void nvPhu() throws Exception {
            if (System.currentTimeMillis() - phuLanCuoi < 4 * 60000L) return;
            phuLanCuoi = System.currentTimeMillis();
            // nhan thuong nhiem vu da xong
            List<String> dang = KichBanDS4.dsNhom(b, 0, 11);
            if (dang != null) for (int i = 0; i < dang.size(); i++) if (dang.get(i).contains("(nhận") || dang.get(i).contains("nhận thưởng")) {
                int m = b.moc();
                b.menu(-527, 11, i);
                String t = b.doiChu(m, 2000, "");
                if (t != null && !t.contains("Đã nhận thưởng")) { soNvPhu++; ghi("thuong nhiem vu phu/ngay: " + t.replace('\n', ' ')); }
                KichBanDS4.dsNhom(b, 0, 11);
            }
            for (int nhom : new int[]{2, 1, 3}) {
                List<String> ds = KichBanDS4.dsNhom(b, nhom, 12);
                if (ds == null || ds.isEmpty() || ds.get(0).startsWith("THONG BAO")) continue;
                String t = KichBanDS4.nhan(b, 0);
                if (t.contains("Câu đố") || t.contains("đố")) {
                    String da = BotChucNang.sql1("SELECT n.dap_an FROM data_nhiem_vu_noi_dung n JOIN data_quest_lau_dai q ON q.ma=n.ma WHERE q.ten='" + ds.get(0).replaceAll(" \\(.*\\)$", "").replace("'", "''") + "' LIMIT 1");
                    if (da != null && !da.isEmpty()) { b.chat(da.split("\\|")[0]); b.bom(800); }
                }
            }
        }

        // ---------------------------------------------------------------- vong chinh
        void vongChinh() throws Exception {
            int vong = 0;
            while (System.currentTimeMillis() < hetGio && !xong) {
                vong++;
                quetLog();
                b.lamMoi();
                kiem();
                String t = nvChinh();
                if (vong % 3 == 0) nvPhu();
                muaThuoc();
                if (vong % 2 == 0) gym();
                boolean lam = lamNvChinh();
                if (!lam) {
                    String bt = BotChucNang.khongDau(t);
                    if (bt.contains("ve chuong") || bt.contains("mo sau") || bt.contains("ngay") && bt.contains("mo")) {
                        tua("1d", user + ": " + t.replace('\n', ' '));
                    }
                    if (bt.contains("can dat cap") || nvChinhType < 0 || nvChinhType == 4) luyen(3);
                    else luyen(2);
                }
                if (b.lv >= 100 && soGym >= 16) { xong = true; ghi("HOAN THANH: cap 100 + 16 huy hieu"); }
                if (System.currentTimeMillis() - lanTienTrien > 12 * 60000L) {
                    ketLyDo("12 phut khong tien trien (nv '" + nvChinh + "' type " + nvChinhType + ", map " + b.mapId + ", cap " + b.lv + ")");
                    tua("1d", "dong " + user + " ket");
                    lanTienTrien = System.currentTimeMillis();
                }
            }
        }
    }

    // ======================================================================== main
    public static void main(String[] a) throws Exception {
        BotChucNang.HOST = a[0];
        BotChucNang.PORT = Integer.parseInt(a[1]);
        BotChucNang.TIEN_TO = a[2];
        String tkDieuPhoi = a.length > 3 ? a[3] : "dieuphoi";
        long hetGio = System.currentTimeMillis() + Long.getLong("phut", 60L) * 60000L;
        int[] lop = Arrays.stream(System.getProperty("dong", "0,1,2,3,4").split(",")).mapToInt(Integer::parseInt).toArray();
        int tu = Integer.getInteger("tu", 1);
        quetLog();
        // dieu phoi (GM): tua dong ho
        BotChucNang.Bot dp = new BotChucNang.Bot(tkDieuPhoi);
        String l = dp.dangNhap("123456");
        if (l == null) l = dp.vaoGame(0);
        if (l != null) System.out.println("CANH BAO: dieu phoi khong vao duoc (" + l + ") - khong tua dong ho duoc");
        else { dieuPhoi = dp; KichBanDS.veLang(dp); dp.chat("tua"); dp.bom(1200); System.out.println("[DIEU PHOI] " + dp.chuTu(0).replaceAll(".*\\[37\\]", "")); }
        if (dp.viTriMap == null) throw new IllegalStateException("khong co bang vi tri 105");
        napTheGioi(dp);
        System.out.println("the gioi: " + LOAD.size() + " map, " + MAP_THU.size() + " nhom, " + QUAI_MAP.size() + " loai quai co cho sinh");
        List<Dong> ds = new ArrayList<>();
        ExecutorService ex = Executors.newFixedThreadPool(lop.length);
        for (int i = 0; i < lop.length; i++) {
            Dong d = new Dong(lop[i], String.format("%s%03d", BotChucNang.TIEN_TO, tu + i), hetGio);
            ds.add(d);
            ex.submit(d);
            Thread.sleep(3000);
        }
        // giu ket noi dieu phoi song + quet log
        while (!ex.isTerminated() && System.currentTimeMillis() < hetGio + 120000) {
            synchronized (KHOA_DH) {
                if (dieuPhoi != null) { dieuPhoi.bom(200); if (!dieuPhoi.conSong()) { System.out.println("[DIEU PHOI] rot ket noi -> vao lai"); dieuPhoi.close(); dieuPhoi = null; } }
                if (dieuPhoi == null) {
                    BotChucNang.Bot x = new BotChucNang.Bot(tkDieuPhoi);
                    try {
                        String l2 = x.dangNhap("123456");
                        if (l2 == null) l2 = x.vaoGame(0);
                        if (l2 == null) { KichBanDS.veLang(x); dieuPhoi = x; } else x.close();
                    } catch (IOException e) { x.close(); }
                }
            }
            quetLog();
            Thread.sleep(5000);
            boolean het = true;
            for (Dong d : ds) if (d.b != null && !d.b.dut && System.currentTimeMillis() < hetGio) het = false;
            if (het && System.currentTimeMillis() >= hetGio) break;
            ex.shutdown();
            ex.awaitTermination(1, TimeUnit.SECONDS);
        }
        ex.shutdownNow();
        quetLog();
        baoCao(ds);
        System.exit(0);
    }

    static final String[] TEN_LOP = {"lop 0", "lop 1", "lop 2", "lop 3", "lop 4"};

    static void baoCao(List<Dong> ds) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("| Dòng | Tài khoản | Vào lại (server khởi động lại) | Cấp đầu → cao nhất | NV chính trả | NV phụ/ngày | Gym | Huy hiệu | Hạ quái | Đổi map | Chết | Thời gian thật | Kẹt | Lỗi kiểm |\n|---|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");
        for (Dong d : ds) {
            sb.append(String.format("| %d | %s | %d | %d → %d | %d | %d | %d | %s | %d | %d | %d | %d phút | %d | %d |%n", d.lop, d.user, d.soVaoLai, d.capDau, d.capCao,
                    d.soNvChinh, d.soNvPhu, d.soGym, String.join(", ", d.huyHieu), d.soHaQuai, d.soDoiMap, d.soChet,
                    (System.currentTimeMillis() - d.batDau) / 60000, d.ket.size(), d.loi.size()));
        }
        sb.append("\nĐồng hồ ảo đã tua: ").append(tongTuaPhut / 1440).append(" ngày (").append(NHAT_KY_TUA.size()).append(" lần).\n");
        sb.append("\nLỗi server (dòng E trong nhật ký) trong lúc chạy: ").append(LOI_SERVER.size()).append("\n");
        for (String s : new LinkedHashSet<>(LOI_SERVER)) sb.append("- `").append(s.replace('`', '\'')).append("`\n");
        for (Dong d : ds) {
            sb.append("\n### Dòng ").append(d.lop).append(" (").append(d.user).append(")\n");
            for (String k : d.gymKetQua) sb.append("- Gym: ").append(k).append('\n');
            for (String k : d.ket) sb.append("- KẸT: ").append(k).append('\n');
            for (String k : d.loi) sb.append("- LỖI: ").append(k).append('\n');
        }
        System.out.println(sb);
        String ra = System.getProperty("ra");
        if (ra != null) Files.write(new File(ra).toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        String nk = System.getProperty("nhatKy");
        if (nk != null) {
            StringBuilder s2 = new StringBuilder();
            for (Dong d : ds) synchronized (d.nhatKy) { for (String x : d.nhatKy) s2.append(x).append('\n'); }
            for (String x : NHAT_KY_TUA) s2.append(x).append('\n');
            Files.write(new File(nk).toPath(), s2.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
