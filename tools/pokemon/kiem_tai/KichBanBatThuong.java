import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * THAO TAC BAT THUONG (nhom hoi quy bat_thuong, 25/10/2026): nguoi choi THAT (tai khoan thuong kt101.., KHONG GM, nhan
 * vat moi) lam nhung viec la ma nguoi choi van lam - bam dup / bam lien tuc, tui day, rut mang giua chung, giao dich doi do
 * sau khi doi phuong da khoa, ban do dang mac, hanh dong khi da guc, goi tin sai (id la / so luong am / goi cut), dang
 * nhap lai lien tuc. GM (tai khoan kt0xx) CHI dung chuan bi canh qua lenh nham ten (smn / spt / gem / die) - moi kiem tra
 * do nhan vat thuong tu gui goi.
 *
 * Moi kiem tra so BAT BIEN, khong so thong bao:
 *   - tien / thuoc / so mon BAO TOAN (khong nhan doi, khong am, khong mat khong ly do), doi chieu CSDL (tob_char.gold)
 *     sau khi thoat;
 *   - khong ket trang thai (dang nhap lai duoc, vao map hop le, con song sau "Ve lang");
 *   - may chu van tra loi (bot khac dang nhap + lam moi duoc);
 *   - KHONG co Exception moi trong log may chu (-Dlog=<game.log>) trong luc kich ban chay.
 * Doi TRANG THAI (cho / onDinh) chu khong doi thoi gian co dinh: moi cho deu co han rong (8-15 giay).
 *
 *   java -cp <bot>:<lop server> -Ddb=htbb_db -Ddbport=3560 -Dlog=<game.log> -Dban=915 -Dtu=1 -Dden=40 -DtuThuong=101 \
 *        -DdenThuong=199 KichBanBatThuong 127.0.0.1 18561 kt [bt_...]
 */
public final class KichBanBatThuong {

    private KichBanBatThuong() {
    }

    static final int GIA_THUOC_1 = 100;   // Binh thuoc 1 o Kecleon (data_potion) - kiem lai bang lan mua dau
    static final int VU_KHI = 79;         // Than lua so cap (shop vu khi NPC 2, 500 xu, ban lai 25 xu)

    // ================================================================== GM chuan bi
    static BotChucNang.Bot gm;

    static synchronized BotChucNang.Bot gm() throws Exception {
        if (gm != null && gm.conSong()) return gm;
        if (gm != null) gm.close();
        gm = BotChucNang.botMoi(0);
        return gm;
    }

    /** Lenh GM nham ten nguoi choi thuong (chuan bi canh). */
    static void lenhGm(String lenh) throws Exception {
        BotChucNang.Bot g = gm();
        g.gm(lenh, 600);
    }

    // ================================================================== nguoi choi thuong
    /** Nhan vat moi, tai khoan thuong (khong GM), da roi san ve lang quoc gia. */
    static BotChucNang.Bot nguoi() throws Exception {
        BotChucNang.Bot b = BotChucNang.botThuong();
        KichBanDS.veLang(b);
        b.px = b.tx * 16 + 8;
        b.py = b.ty * 16 + 8;
        onDinh(b);
        return b;
    }

    /** Nguoi choi thuong da di bo vao nha 103 (OfflineMap: Kecleon + shop vu khi + tho ren). */
    static BotChucNang.Bot nguoiTrongNha() throws Exception {
        BotChucNang.Bot b = nguoi();
        if (!KichBanDS2.vaoNha(b, 103)) throw new IllegalStateException(b.user + " khong vao duoc nha tu map " + b.mapId);
        onDinh(b);
        return b;
    }

    /** Lam moi toi khi xu + thuoc + so mon dung yen 2 lan lien (may chu da xu ly het goi truoc do). */
    static void onDinh(BotChucNang.Bot b) throws IOException {
        String truoc = null;
        for (int i = 0; i < 12; i++) {
            lamMoi(b);
            if (b.dut) return;
            String nay = b.xu + "|" + b.luong + "|" + b.lk + "|" + b.thuoc + "|" + b.doDung.size() + "/" + b.tongMon() + "|" + b.soMac + "|" + b.ngoc.size();
            if (nay.equals(truoc)) return;
            truoc = nay;
        }
    }

    /**
     * Lam moi (88) va doi DUNG goi tui (16) tra ve - 88 chay tren luong phien nhu mua / ban / dung (24/28/29/22) nen
     * khi 16 toi thi moi goi gui truoc do da xu ly xong. Nhanh hon BotChucNang.lamMoi (bom co dinh 700 ms).
     */
    static void lamMoi(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(88, new byte[0]);
        b.cho(x -> x.coLenh(m, 16) || x.dut, 5000);
        b.bom(150);
    }

    /** Dat xu dung bang {@code x} (GM smn chenh lech), doi bot thay. */
    static boolean datXu(BotChucNang.Bot b, long x) throws Exception {
        onDinh(b);
        long d = x - b.xu;
        if (d != 0) lenhGm("smn " + b.ten + " " + d + " 0 0");
        b.cho(z -> z.xu == x, 8000);
        onDinh(b);
        return b.xu == x;
    }

    static boolean datThuoc(BotChucNang.Bot b, int id, int sl) throws Exception {
        onDinh(b);
        int d = sl - b.soThuoc(id);
        if (d != 0) lenhGm("spt " + b.ten + " " + id + " " + d);
        b.cho(z -> z.soThuoc(id) == sl, 8000);
        onDinh(b);
        return b.soThuoc(id) == sl;
    }

    /** Mo Kecleon (NPC 1) -> "Mua thuoc" nhu client. */
    static boolean moKecleon(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(23, (byte) 1);
        b.cho(x -> x.coLenh(m, 23) || x.coLenh(m, -30), 5000);
        if (!b.coLenh(m, 23) && b.coLenh(m, -30)) {
            int i = b.timMuc("Mua thuốc");
            if (i >= 0) b.menu(b.menuCuoi[0], b.menuCuoi[1], i);
            b.cho(x -> x.coLenh(m, 23), 5000);
        }
        return b.coLenh(m, 23);
    }

    /** Mua 1 vu khi tpl o shop NPC 2; tra id moi hoac KHONG. */
    static int muaVuKhi(BotChucNang.Bot b, int tpl) throws IOException {
        int id = KichBanDS3.muaVuKhi(b, tpl);
        onDinh(b);
        return id;
    }

    /** Dong ket noi, doi may chu luu, dang nhap lai cung tai khoan (khong xoa nhan vat). */
    static BotChucNang.Bot vaoLai(BotChucNang.Bot cu) throws Exception {
        String u = cu.user;
        cu.close();
        Exception cuoi = null;
        for (int lan = 0; lan < 8; lan++) {
            Thread.sleep(lan == 0 ? 2500 : 3000);
            BotChucNang.Bot b = new BotChucNang.Bot(u);
            try {
                String l = b.dangNhap("123456");
                if (l == null) l = vaoGameNhanh(b);
                if (l == null) { onDinh(b); return b; }
                cuoi = new IllegalStateException(u + ": " + l);
            } catch (IOException e) {
                cuoi = e;
            }
            b.close();
        }
        throw cuoi;
    }

    /**
     * Vao game voi nhan vat SAN CO: chon nhan vat (13), doi map (12) roi bao da nap map (12/-500) ngay - nhu client.
     * (BotChucNang.vaoGame doi them actor tu goi 3 - goi nay chi den SAU -500 nen moi lan vao mat tron 10 giay.)
     */
    static String vaoGameNhanh(BotChucNang.Bot b) throws IOException {
        if (b.dsNv.stream().noneMatch(v -> v[2] == 1)) return b.vaoGame(0);
        b.charDb = b.dsNv.stream().filter(v -> v[2] == 1).findFirst().get()[0];
        int m = b.moc();
        b.gui(13, (byte) 0, b.charDb);
        if (!b.cho(x -> x.coLenh(m, 12), 10000)) return "chon nhan vat: khong nhan map. " + b.chuTu(m);
        b.gui(12, (short) -500, (short) 0, (short) 0);
        b.cho(x -> x.actor >= 0, 8000); // goi 3 (actor) co luc khong toi - nhu BotChucNang.vaoGame: khong coi la loi
        b.bom(800);
        return null;
    }

    static long xuCsdl(BotChucNang.Bot b) {
        String v = BotChucNang.sql1("SELECT gold FROM tob_char WHERE id = " + b.charDb);
        return v == null || !v.trim().matches("-?\\d+") ? Long.MIN_VALUE : Long.parseLong(v.trim());
    }

    /** Thuoc id trong CSDL (tob_char.potion: o 0 la xu, o i la so binh loai i). */
    static int thuocCsdl(BotChucNang.Bot b, int id) {
        String v = BotChucNang.sql1("SELECT potion FROM tob_char WHERE id = " + b.charDb);
        if (v == null) return Integer.MIN_VALUE;
        String[] f = v.trim().split(",");
        return id < f.length && f[id].matches("-?\\d+") ? Integer.parseInt(f[id]) : Integer.MIN_VALUE;
    }

    /** May chu con tra loi: mot nguoi choi khac dang nhap + lam moi duoc. */
    static boolean mayChuSong() {
        try (BotChucNang.Bot t = new BotChucNang.Bot(BotChucNang.tkThuong())) {
            String l = t.dangNhap("123456");
            if (l == null) l = t.vaoGame(0);
            if (l != null) return false;
            int m = t.moc();
            t.gui(88, new byte[0]);
            return t.cho(x -> x.coLenh(m, 16), 10000);
        } catch (Exception e) {
            return false;
        }
    }

    // ================================================================== log may chu
    static File tepLog;
    static long viTri;

    /** Dong Exception moi trong log may chu tu lan doc truoc (bo loi mang binh thuong khi rut day). */
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
                if (u.contains("[NHAT] loi lap lai")) continue; // tong ket lap lai cua loi da bao truoc do
                if (u.contains("[MANG]") || u.contains("Connection reset") || u.contains("Broken pipe") || u.contains("RegionVantienTran")) continue;
                kq.add(u.length() > 200 ? u.substring(0, 200) : u);
            }
            viTri = r.getFilePointer();
        } catch (IOException ignored) {
        }
        return kq;
    }

    static void kiemLog(String ten) {
        if (tepLog == null) { BotChucNang.ket(ten + ".log_sach", null, "khong co -Dlog"); return; }
        List<String> l = loiLogMoi();
        BotChucNang.ket(ten + ".log_sach", l.isEmpty(), l.isEmpty() ? "khong co Exception moi" : l.size() + " dong: " + String.join(" || ", l.subList(0, Math.min(4, l.size()))));
    }

    /**
     * Sau doan gui goi SAI co chu dich: Exception do doc goi cut / id la la NHAT KY (may chu da bat, khong doi trang
     * thai - trang thai duoc kiem rieng) -> BO QUA kem dong log, khong lam do cong kiem; khong co = PASS.
     */
    static void ghiLogGoiSai(String ten) {
        if (tepLog == null) return;
        List<String> l = loiLogMoi();
        BotChucNang.ket(ten + ".log_goi_sai", l.isEmpty() ? Boolean.TRUE : null,
                l.isEmpty() ? "goi sai khong de lai Exception" : "ghi nhan " + l.size() + " dong Exception do goi sai (da bat, trang thai kiem rieng): "
                        + String.join(" || ", l.subList(0, Math.min(3, l.size()))));
    }

    static void ket(String ten, boolean ok, String bang) {
        BotChucNang.ket(ten, ok, bang);
    }

    // ================================================================== 1. mua: bam lien tuc / vua du tien / goi sai
    static void muaNhanh() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoiTrongNha();
        try {
            boolean mo = moKecleon(b);
            datThuoc(b, 1, 0);
            // gia that cua 1 binh (lan mua binh thuong)
            datXu(b, 10000);
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 1);
            b.cho(x -> x.soThuoc(1) == 1, 8000);
            onDinh(b);
            long gia = 10000 - b.xu;
            ket("bt_mua.gia_mot_binh", mo && b.soThuoc(1) == 1 && gia > 0, "mo Kecleon=" + mo + ", thuoc1=" + b.soThuoc(1) + ", gia=" + gia);
            if (gia <= 0) return;
            // VUA DU tien cho 10 binh, bam 14 lan lien (khong doi) -> dung 10 binh, xu = 0, khong am
            datThuoc(b, 1, 0);
            datXu(b, gia * 10);
            for (int i = 0; i < 14; i++) b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 1);
            b.cho(x -> x.soThuoc(1) >= 10 && x.xu == 0, 12000);
            onDinh(b);
            ket("bt_mua.bam_lien_vua_du", b.soThuoc(1) == 10 && b.xu == 0, "14 goi mua x1 voi xu = 10 binh: thuoc1=" + b.soThuoc(1) + " xu=" + b.xu);
            // THIEU 1 xu: khong duoc cap, khong tru
            datXu(b, gia - 1);
            int t0 = b.soThuoc(1);
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 1);
            onDinh(b);
            ket("bt_mua.thieu_mot_xu", b.soThuoc(1) == t0 && b.xu == gia - 1, "xu " + (gia - 1) + " -> " + b.xu + ", thuoc1 " + t0 + " -> " + b.soThuoc(1));
            // so luong 0 / am / id thuoc la / loai la / goi cut / n am -> khong doi gi
            datXu(b, 100000);
            Map<Integer, Integer> th0 = new LinkedHashMap<>(b.thuoc);
            int nDo = b.doDung.size(), tong0 = b.tongMon(); // tong mon: dup vao chong type 28 khong tang so O
            List<byte[]> sai = Arrays.asList(
                    BotChucNang.goi((byte) 1, (byte) 4, (short) 1, (short) 0),
                    BotChucNang.goi((byte) 1, (byte) 4, (short) 1, (short) -5),
                    BotChucNang.goi((byte) 1, (byte) 4, (short) 1, (short) -32768),
                    BotChucNang.goi((byte) 1, (byte) 4, (short) -1, (short) 3),
                    BotChucNang.goi((byte) 1, (byte) 4, (short) 30000, (short) 3),
                    BotChucNang.goi((byte) 1, (byte) 7, (short) 1, (short) 3),
                    BotChucNang.goi((byte) 1, (byte) 3, (short) VU_KHI, (short) 1, (byte) 99),
                    BotChucNang.goi((byte) 1, (byte) 3, (short) 1034, (short) 1, (byte) 0),
                    BotChucNang.goi((byte) -1),
                    BotChucNang.goi((byte) 5, (byte) 4, (short) 1),
                    new byte[0]);
            for (byte[] g : sai) b.gui(24, g);
            onDinh(b);
            ghiLogGoiSai("bt_mua");
            boolean song = b.conSong();
            if (!song) b = vaoLai(b);
            ket("bt_mua.goi_sai", b.xu == 100000 && b.thuoc.equals(th0) && b.doDung.size() == nDo && b.tongMon() == tong0,
                    sai.size() + " goi 24 sai (sl 0/-5/-32768, id -1/30000, loai 7, kind 99, mon khong ban, n=-1, cut): xu 100000 -> " + b.xu
                            + ", thuoc " + th0 + " -> " + b.thuoc + ", mon " + nDo + " -> " + b.doDung.size() + " (tong " + tong0 + " -> " + b.tongMon() + "), ket noi " + (song ? "con" : "bi dong (dang nhap lai duoc)"));
            // so luong khong lo: chi toi da 999 / o, tien tru dung so binh nhan
            datThuoc(b, 1, 0);
            datXu(b, 5000000);
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 32767);
            b.cho(x -> x.soThuoc(1) > 0, 8000);
            onDinh(b);
            ket("bt_mua.so_luong_lon", b.soThuoc(1) <= 999 && b.soThuoc(1) > 0 && 5000000 - b.xu == gia * b.soThuoc(1),
                    "mua 32767: nhan " + b.soThuoc(1) + " binh, tru " + (5000000 - b.xu) + " xu (= " + gia + " x so binh?)");
            // CSDL sau khi thoat khop voi trong game
            long xuGame = b.xu;
            int thGame = b.soThuoc(1);
            b = vaoLai(b);
            ket("bt_mua.csdl", xuCsdl(b) == xuGame && b.xu == xuGame && b.soThuoc(1) == thGame && thuocCsdl(b, 1) == thGame,
                    "game xu=" + xuGame + " thuoc1=" + thGame + " | CSDL gold=" + xuCsdl(b) + " potion[1]=" + thuocCsdl(b, 1) + " | vao lai xu=" + b.xu + " thuoc1=" + b.soThuoc(1));
        } finally {
            b.close();
        }
        kiemLog("bt_mua");
    }

    // ================================================================== 2. ban: bam dup / ban do dang mac / id la
    static void banNhanh() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoiTrongNha();
        try {
            datXu(b, 20000);
            b = vaoLai(b); // luu CSDL: so vu khi khoi dau (vu khi tan thu dang mac cung la mau VU_KHI)
            final int vk0 = vuKhiCsdl(b);
            if (!KichBanDS2.laOffline(b.mapId)) KichBanDS2.vaoNha(b, 103);
            int id1 = muaVuKhi(b, VU_KHI);
            int id2 = muaVuKhi(b, VU_KHI);
            if (id1 == KichBanDS3.KHONG || id2 == KichBanDS3.KHONG) { ket("bt_ban.mua", false, "khong mua duoc vu khi: " + id1 + "," + id2 + " " + b.chuTu(0)); return; }
            long xu0 = b.xu;
            int n0 = b.doDung.size();
            for (int i = 0; i < 8; i++) b.gui(28, (short) id1);
            b.cho(x -> x.timDo(d -> d[0] == id1) == null, 8000);
            onDinh(b);
            long cong = b.xu - xu0;
            ket("bt_ban.bam_dup", b.timDo(d -> d[0] == id1) == null && b.doDung.size() == n0 - 1 && cong > 0 && cong <= 500,
                    "8 goi ban cung id: mon " + n0 + " -> " + b.doDung.size() + ", xu +" + cong + " (chi duoc cong 1 lan)");
            // mac id2 roi ban chinh id2 -> khong ban duoc (van mac), xu khong doi
            final int macGoc = b.soMac; // so mon dang mac khi vu khi con trong tui
            int m = b.moc();
            b.gui(29, (short) id2);
            b.cho(x -> x.timDo(d -> d[0] == id2) == null, 8000);
            onDinh(b);
            boolean daMac = b.timDo(d -> d[0] == id2) == null && b.soMac > 0;
            long xu1 = b.xu;
            int mac1 = b.soMac;
            for (int i = 0; i < 3; i++) b.gui(28, (short) id2);
            onDinh(b);
            ket("bt_ban.do_dang_mac", daMac && b.xu == xu1 && b.soMac == mac1, "mac=" + daMac + " so mac " + mac1 + " -> " + b.soMac + ", xu " + xu1 + " -> " + b.xu + " " + b.chuTu(m));
            // bam mac/thao lien tuc 9 lan -> vu khi van ton tai DUNG 1 ban (trong tui hoac dang mac), ca truoc va sau vao lai
            for (int i = 0; i < 9; i++) b.gui(29, (short) id2);
            onDinh(b);
            int tui1 = demTpl(b, VU_KHI);
            b = vaoLai(b);
            int tui2 = demTpl(b, VU_KHI), csdl = vuKhiCsdl(b);
            ket("bt_ban.mac_thao_lien", tui1 == tui2 && csdl == vk0 + 1,
                    "sau 9 goi 29: vu khi trong tui " + tui1 + "; vao lai: trong tui " + tui2 + ", CSDL (tui + dang mac) " + csdl + " ban (phai = " + (vk0 + 1) + ": " + vk0 + " khoi dau + 2 mua - 1 ban)");
            // id khong co / am
            long xu2 = b.xu;
            int n2 = b.doDung.size(), tong2 = b.tongMon();
            b.gui(28, (short) -1);
            b.gui(28, (short) 32767);
            b.gui(28, new byte[0]);
            b.gui(29, (short) 12345);
            onDinh(b);
            if (!b.conSong()) b = vaoLai(b);
            ket("bt_ban.id_la", b.xu == xu2 && b.doDung.size() == n2 && b.tongMon() == tong2, "ban/dung id la: xu " + xu2 + " -> " + b.xu + ", mon " + n2 + " -> " + b.doDung.size() + " (tong " + tong2 + " -> " + b.tongMon() + ")");
        } finally {
            b.close();
        }
        kiemLog("bt_ban");
    }

    // ================================================================== 3. tui day
    static void tuiDay() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoiTrongNha();
        try {
            datXu(b, 3000000);
            int idMac = muaVuKhi(b, VU_KHI);
            if (idMac != KichBanDS3.KHONG) {
                b.gui(29, (short) idMac);
                b.cho(x -> x.timDo(d -> d[0] == idMac) == null, 8000);
                onDinh(b);
            }
            // mua tung lo 20 vu khi toi khi so mon ngung tang (vong lap mua dung o tui day)
            moShopVuKhi(b);
            int truoc = -1;
            for (int lan = 0; lan < 15 && b.doDung.size() != truoc; lan++) {
                truoc = b.doDung.size();
                Object[] f = new Object[1 + 20 * 4];
                f[0] = (byte) 20;
                for (int i = 0; i < 20; i++) { f[1 + i * 4] = (byte) 3; f[2 + i * 4] = (short) VU_KHI; f[3 + i * 4] = (short) 1; f[4 + i * 4] = (byte) 0; }
                b.gui(24, f);
                onDinh(b);
            }
            int day = b.doDung.size();
            long xu0 = b.xu;
            ket("bt_tui.lam_day", day > 5 && day == truoc, "tui dung o " + day + " mon (xu con " + xu0 + ")");
            // mua them vu khi + thuoc loai moi khi day -> khong nhan, KHONG tru tien
            Map<Integer, Integer> th0 = new LinkedHashMap<>(b.thuoc);
            b.gui(24, (byte) 1, (byte) 3, (short) VU_KHI, (short) 1, (byte) 0);
            b.gui(24, (byte) 1, (byte) 4, (short) 2, (short) 5);
            onDinh(b);
            ket("bt_tui.mua_khi_day", b.xu == xu0 && b.doDung.size() == day && b.thuoc.equals(th0),
                    "xu " + xu0 + " -> " + b.xu + ", mon " + day + " -> " + b.doDung.size() + ", thuoc " + th0 + " -> " + b.thuoc);
            // thao vu khi dang mac khi tui day -> mon KHONG duoc bien mat
            int tui0 = b.doDung.size();
            b.gui(29, (short) idMac);
            onDinh(b);
            ket("bt_tui.thao_khi_day", idMac != KichBanDS3.KHONG && b.doDung.size() == tui0 && b.timDo(d -> d[0] == idMac) == null,
                    "thao mon dang mac khi tui day: tui " + tui0 + " -> " + b.doDung.size() + ", vu khi " + (b.timDo(d -> d[0] == idMac) == null ? "van mac" : "VAO TUI (vuot suc chua)") + " " );
            // vao lai: xu + so vu khi (tui + dang mac, CSDL) giu nguyen - mon dang mac khong bien mat / nhan doi
            long xu1 = b.xu;
            int tui = b.doDung.size();
            int ky = demTpl(b, VU_KHI) + (idMac != KichBanDS3.KHONG && b.timDo(d -> d[0] == idMac) == null ? 1 : 0);
            b = vaoLai(b);
            ket("bt_tui.vao_lai", b.xu == xu1 && b.doDung.size() == tui && xuCsdl(b) == xu1 && vuKhiCsdl(b) == ky,
                    "xu " + xu1 + " -> " + b.xu + " (CSDL " + xuCsdl(b) + "), tui " + tui + " -> " + b.doDung.size() + ", vu khi tui+mac " + ky + " -> CSDL " + vuKhiCsdl(b));
        } finally {
            b.close();
        }
        kiemLog("bt_tui");
    }

    static void moShopVuKhi(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(23, (byte) 2);
        if (b.doiMenu(m, 2, 0, 5000) > 0) { b.menu(2, 0, 0); b.cho(x -> x.coLenh(m, 23), 5000); }
    }

    // ================================================================== 4. rut mang giua luc mua / ban / doi map
    static void dutGiuaChung() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoiTrongNha();
        try {
            moKecleon(b);
            datThuoc(b, 1, 0);
            datXu(b, 100000);
            StringBuilder sb = new StringBuilder();
            boolean ok = true;
            long xu = b.xu;
            int th = b.soThuoc(1);
            for (int lan = 0; lan < 4; lan++) {
                // mua 3 binh (gia 100) roi rut mang NGAY (khong doc tra loi)
                b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 3);
                if (lan % 2 == 1) b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 3);
                b = vaoLai(b);
                long dXu = xu - b.xu;
                int dTh = b.soThuoc(1) - th;
                boolean khop = dXu == (long) GIA_THUOC_1 * dTh && b.xu >= 0 && xuCsdl(b) == b.xu;
                ok &= khop;
                sb.append("lan").append(lan).append(": -").append(dXu).append(" xu / +").append(dTh).append(" binh").append(khop ? "" : " LECH").append("; ");
                xu = b.xu;
                th = b.soThuoc(1);
                if (!KichBanDS.laLang(b.mapId) && b.mapId != 105 && !KichBanDS2.laOffline(b.mapId)) break;
                if (!KichBanDS2.laOffline(b.mapId)) KichBanDS2.vaoNha(b, 103);
                moKecleon(b);
            }
            ket("bt_dut.mua", ok, sb.toString());
            // ban vu khi roi rut mang: mon mat <=> xu cong
            if (!KichBanDS2.laOffline(b.mapId)) KichBanDS2.vaoNha(b, 103);
            int id = muaVuKhi(b, VU_KHI);
            if (id != KichBanDS3.KHONG) {
                long x0 = b.xu;
                int n0 = b.doDung.size();
                b.gui(28, (short) id);
                b.gui(28, (short) id);
                b = vaoLai(b);
                boolean mat = b.doDung.size() == n0 - 1;
                long cong = b.xu - x0;
                ket("bt_dut.ban", mat ? cong > 0 && cong <= 500 : cong == 0 && b.doDung.size() == n0, "mon " + n0 + " -> " + b.doDung.size() + ", xu +" + cong);
            } else ket("bt_dut.ban", false, "khong mua duoc vu khi (map " + b.mapId + ")");
            // doi map roi rut mang: dang nhap lai vao map hop le, di lai duoc
            if (!KichBanDS2.laOffline(b.mapId)) KichBanDS2.vaoNha(b, 103);
            int nhaMap = b.mapId;
            boolean daGui = guiCongRa(b, false);
            b = vaoLai(b);
            ket("bt_dut.doi_map", daGui && b.mapId >= 0 && b.tenMap.length() > 0 && b.hp > 0,
                    "rut mang ngay sau goi qua cong ra khoi nha " + nhaMap + ": vao lai map " + b.mapId + " (" + b.tenMap + ") hp " + b.hp);
            // spam cong: 8 goi qua cong lien tiep -> o mot map hop le, van choi tiep duoc
            if (!KichBanDS2.laOffline(b.mapId)) KichBanDS2.vaoNha(b, 103);
            int nha2 = b.mapId;
            boolean gui2 = guiCongRa(b, true);
            b.bom(3000);
            boolean song = b.conSong();
            if (!song) b = vaoLai(b);
            else { b.gui(12, (short) -500, (short) 0, (short) 0); onDinh(b); }
            ket("bt_dut.spam_cong", gui2 && b.mapId >= 0 && b.mapId != nha2 || gui2 && !song,
                    "8 goi cong ra tu nha " + nha2 + ": map " + b.mapId + " (" + b.tenMap + "), ket noi " + (song ? "con" : "bi dong"));
            // cong gia toi map xa (khong co cong) -> khong duoc dua toi do
            int truoc = b.mapId;
            b.gui(12, (short) 50, (short) 10, (short) 10);
            b.bom(3000);
            if (!b.conSong()) b = vaoLai(b);
            onDinh(b);
            ket("bt_dut.cong_gia", b.mapId != 50, "goi 12 toi map 50 tu map " + truoc + " -> dang o " + b.mapId);
        } finally {
            b.close();
        }
        ket("bt_dut.may_chu_song", mayChuSong(), "nguoi choi khac dang nhap + lam moi duoc");
        kiemLog("bt_dut");
    }

    /** Di toi cong ra cua nha (map offline) roi gui 12; {@code spam} = gui 8 lan; khong doc tra loi. */
    static boolean guiCongRa(BotChucNang.Bot b, boolean spam) throws IOException {
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        if (bd == null || b.mapCu < 0) return false;
        for (int[] g : bd.cong) {
            int[] dung = bd.oTruocCong(g[0], g[1]);
            b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8;
            if (dung == null || !b.diDuong(bd, dung[0], dung[1])) continue;
            for (int i = 0; i < (spam ? 8 : 1); i++) b.gui(12, (short) b.mapCu, (short) g[3], (short) g[4]);
            return true;
        }
        return false;
    }

    // ================================================================== 5. dang nhap lai lien tuc / hai phien
    static void dangNhapBao() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoi();
        String u = b.user;
        datXu(b, 77777);
        datThuoc(b, 1, 33);
        b.close();
        Thread.sleep(2500);
        int vao = 0, loi = 0;
        // 8 lan: ket noi + dang nhap roi dong ngay (co lan vao game nua chung)
        for (int i = 0; i < 8; i++) {
            BotChucNang.Bot t = new BotChucNang.Bot(u);
            try {
                String l = t.dangNhap("123456");
                if (l == null && i % 2 == 0) { t.gui(13, (byte) 0, t.dsNv.isEmpty() ? 0 : t.dsNv.get(0)[0]); vao++; }
                if (l != null) loi++;
            } catch (IOException e) {
                loi++;
            } finally {
                t.close();
            }
        }
        // hai phien cung luc cung tai khoan
        BotChucNang.Bot p1 = new BotChucNang.Bot(u), p2 = new BotChucNang.Bot(u);
        String l1 = p1.dangNhap("123456");
        if (l1 == null) l1 = p1.vaoGame(0);
        String l2 = p2.dangNhap("123456");
        if (l2 == null) l2 = p2.vaoGame(0);
        p1.bom(2000);
        boolean haiCungSong = p1.conSong() && p2.conSong() && l1 == null && l2 == null;
        p1.close();
        p2.close();
        BotChucNang.Bot c = vaoLai(new BotChucNang.Bot(u));
        try {
            ket("bt_dang_nhap.bao", c.xu == 77777 && c.soThuoc(1) == 33 && xuCsdl(c) == 77777,
                    "8 lan dang nhap/dong (" + vao + " lan vao game nua chung, " + loi + " loi) + 2 phien: xu " + c.xu + " (CSDL " + xuCsdl(c) + "), thuoc1 " + c.soThuoc(1));
            ket("bt_dang_nhap.hai_phien", !haiCungSong, "hai phien cung tai khoan: " + (haiCungSong ? "CA HAI cung song" : "chi mot phien giu duoc") + " (l1=" + l1 + ", l2=" + l2 + ")");
            ket("bt_dang_nhap.choi_tiep", c.hp > 0 && c.mapId >= 0 && c.conSong(), "map " + c.mapId + " hp " + c.hp);
        } finally {
            c.close();
        }
        ket("bt_dang_nhap.may_chu_song", mayChuSong(), "");
        kiemLog("bt_dang_nhap");
    }

    // ================================================================== 6. giao dich: doi do sau khi doi phuong khoa / rut mang / xac nhan dup
    /**
     * Hai nguoi choi thuong cung lang, dung canh nhau. Chong clone (ChongCloneLogic: cap toi thieu, tuoi tai khoan,
     * gio choi) chan giao dich cua nhan vat cap 1 - DUNG thiet ke; chuan bi = GM slv cap 20 (tuoi / gio choi da lui
     * tren CSDL o BotChucNang.duTuoiChongClone).
     */
    static BotChucNang.Bot[] haiNguoi() throws Exception {
        // lang khoi dau ngau nhien (vd Pewter 0 / Dong Pewter 301): giu cac bot da tao, ghep hai bot cung map + quoc gia
        List<BotChucNang.Bot> ds = new ArrayList<>();
        try {
            for (int i = 0; i < 6; i++) {
                BotChucNang.Bot c = nguoi();
                lenhGm("slv " + c.ten + " 20 0");
                c.bom(300);
                for (BotChucNang.Bot a : ds) {
                    if (KichBanDS4.quocGia(c) == KichBanDS4.quocGia(a) && c.mapId == a.mapId && lamGan(a, c)) {
                        ds.remove(a);
                        return new BotChucNang.Bot[]{a, c};
                    }
                }
                System.out.println("  [gd] chua ghep duoc " + c.ten + " (qg " + KichBanDS4.quocGia(c) + " map " + c.mapId + ")");
                ds.add(c);
            }
        } finally {
            for (BotChucNang.Bot b : ds) b.close();
        }
        throw new IllegalStateException("khong co hai nguoi choi thuong cung lang");
    }

    /** c di bo toi canh a (may chu chi cho giao dich voi nguoi trong danh sach "gan" - nearChars). */
    static boolean lamGan(BotChucNang.Bot a, BotChucNang.Bot c) throws IOException {
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(c.mapLoad);
        if (bd == null) return false;
        for (int r = 1; r <= 4; r++) {
            int[] o = bd.oGan(a.px / 16 + r, a.py / 16, 2);
            if (o != null && c.diDuong(bd, o[0], o[1])) break;
        }
        int[] oa = bd.oGan(a.px / 16, a.py / 16, 1);
        if (oa != null) a.diToi(oa[0] * 16 + 8, oa[1] * 16 + 8);
        a.bom(800);
        c.bom(800);
        return Math.abs(c.px - a.px) <= 160 && Math.abs(c.py - a.py) <= 160;
    }

    /** a moi c, c nhan; true neu ca hai nhan 66/1. */
    static boolean moGiaoDich(BotChucNang.Bot a, BotChucNang.Bot c) throws IOException {
        int ma = a.moc(), mc = c.moc();
        a.gui(66, (byte) 0, (short) c.actor);
        boolean ok = c.cho(x -> x.coLenh(mc, 66), 8000);
        if (ok) {
            c.gui(66, (byte) 1, (short) a.actor);
            ok = a.cho(x -> giaoDich(x, ma, 1), 8000) && c.cho(x -> giaoDich(x, mc, 1), 8000);
        }
        if (!ok) a.bom(500);
        if (!ok) System.out.println("  [gd] khong mo duoc: " + a.ten + " map " + a.mapId + " o " + a.px / 16 + "," + a.py / 16 + " qg " + KichBanDS4.quocGia(a)
                + " -> " + c.ten + " (actor " + c.actor + ") map " + c.mapId + " o " + c.px / 16 + "," + c.py / 16 + " qg " + KichBanDS4.quocGia(c)
                + " | A thay " + a.nguoi.keySet() + " C thay " + c.nguoi.keySet() + " | A: " + a.chuTu(ma) + lenhTu(a, ma) + " | C: " + c.chuTu(mc) + lenhTu(c, mc));
        return ok;
    }

    static String lenhTu(BotChucNang.Bot b, int tu) {
        StringBuilder sb = new StringBuilder(" cmd[");
        for (int i = tu; i < b.su.size(); i++) sb.append(b.su.get(i).cmd).append(' ');
        return sb.append(']').toString();
    }

    static boolean giaoDich(BotChucNang.Bot b, int tu, int loai) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == 66 && e.so != null && e.so[0] == loai) return true;
        }
        return false;
    }

    static void giaoDichDoi() throws Exception {
        loiLogMoi();
        BotChucNang.Bot[] hai = haiNguoi();
        BotChucNang.Bot a = hai[0], c = hai[1];
        try {
            ketBan(a, c);
            datThuoc(a, 1, 20);
            datThuoc(a, 4, 0);
            datThuoc(c, 1, 0);
            datThuoc(c, 4, 20);
            // (a) A dua 10 thuoc 1, C dua 10 thuoc 4; C KHOA + XAC NHAN; A rut thuoc ra, chi de 1, roi khoa + xac nhan
            final long luMoiA = System.currentTimeMillis();
            boolean mo = moGiaoDich(a, c);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 10);
            c.gui(66, (byte) 2, (byte) 1, (byte) 4, (short) 10);
            a.bom(800);
            c.bom(800);
            int mc = c.moc();
            c.gui(66, (byte) 4);
            c.bom(600);
            c.gui(66, (byte) 5);
            c.bom(800);
            a.gui(66, (byte) 2, (byte) 2, (byte) 1);
            a.bom(600);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 1);
            a.bom(600);
            a.gui(66, (byte) 4);
            a.bom(600);
            a.gui(66, (byte) 5);
            a.bom(1500);
            c.bom(500);
            onDinh(a);
            onDinh(c);
            boolean bao = a.soThuoc(1) + c.soThuoc(1) == 20 && a.soThuoc(4) + c.soThuoc(4) == 20;
            // C da khoa voi 10 thuoc 1 tren ban: hoac giao dich bi huy / khong xong, hoac C nhan du 10 - KHONG duoc nhan 1
            boolean cBiLua = c.soThuoc(4) < 20 && c.soThuoc(1) < 10;
            ket("bt_gd.doi_sau_khi_khoa", mo && bao && !cBiLua,
                    "mo=" + mo + "; sau: A thuoc1=" + a.soThuoc(1) + " thuoc4=" + a.soThuoc(4) + ", C thuoc1=" + c.soThuoc(1) + " thuoc4=" + c.soThuoc(4)
                            + (cBiLua ? " -> C mat " + (20 - c.soThuoc(4)) + " thuoc4 ma chi nhan " + c.soThuoc(1) + " thuoc1 (doi phuong rut do SAU khi C da khoa)" : "")
                            + " " + c.chuTu(mc));
            c.gui(66, (byte) 3);
            a.gui(66, (byte) 3);
            onDinh(a);
            onDinh(c);
            // (b) xac nhan dup: ca hai khoa roi bam xac nhan 5 lan lien -> chuyen dung 1 lan (C moi: A dang bi gioi han 60 giay)
            datThuoc(a, 1, 20);
            datThuoc(a, 4, 0);
            datThuoc(c, 1, 0);
            datThuoc(c, 4, 0);
            boolean mo2 = moGiaoDich(c, a);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 7);
            a.bom(600);
            a.gui(66, (byte) 4);
            c.gui(66, (byte) 4);
            a.bom(600);
            c.bom(600);
            for (int i = 0; i < 5; i++) { a.gui(66, (byte) 5); c.gui(66, (byte) 5); }
            a.bom(1500);
            c.bom(1500);
            onDinh(a);
            onDinh(c);
            ket("bt_gd.xac_nhan_dup", mo2 && a.soThuoc(1) == 13 && c.soThuoc(1) == 7, "mo=" + mo2 + "; A " + a.soThuoc(1) + "/20 con, C nhan " + c.soThuoc(1) + " (dung 7)");
            // (c) rut mang giua giao dich: A da bo 5 + khoa + xac nhan, C khoa roi rut mang -> khong mat, khong nhan doi
            a.bom(Math.max(0, 61500 - (System.currentTimeMillis() - luMoiA))); // gioi han 1 loi moi / 60 giay / nguoi moi
            int a1 = a.soThuoc(1), c1 = c.soThuoc(1);
            boolean mo3 = moGiaoDich(a, c);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 5);
            a.bom(600);
            a.gui(66, (byte) 4);
            a.bom(300);
            a.gui(66, (byte) 5);
            c.gui(66, (byte) 4);
            c.bom(100);
            c = vaoLai(c);
            onDinh(a);
            int tong = a.soThuoc(1) + c.soThuoc(1);
            ket("bt_gd.rut_mang", mo3 && tong == a1 + c1 && (a.soThuoc(1) == a1 || a.soThuoc(1) == a1 - 5),
                    "mo=" + mo3 + "; A " + a1 + " -> " + a.soThuoc(1) + ", C " + c1 + " -> " + c.soThuoc(1) + " (tong " + (a1 + c1) + " -> " + tong + ")");
            // (d) sau rut mang: A khong ket o trang thai giao dich - mua / dung vat pham binh thuong
            int ma = a.moc();
            a.gui(22, (byte) 1);
            a.bom(800);
            String ket = a.chuTu(ma);
            ket("bt_gd.khong_ket", a.conSong() && !khongDau(ket).contains("giao dich"), ket);
            // thoat ca hai: CSDL khop
            int aG = a.soThuoc(1), cG = c.soThuoc(1);
            a = vaoLai(a);
            c = vaoLai(c);
            ket("bt_gd.csdl", thuocCsdl(a, 1) == a.soThuoc(1) && thuocCsdl(c, 1) == c.soThuoc(1) && a.soThuoc(1) + c.soThuoc(1) <= aG + cG,
                    "A " + aG + " -> " + a.soThuoc(1) + " (CSDL " + thuocCsdl(a, 1) + "), C " + cG + " -> " + c.soThuoc(1) + " (CSDL " + thuocCsdl(c, 1) + ")");
        } finally {
            a.close();
            c.close();
        }
        kiemLog("bt_gd");
    }

    /**
     * So ban vu khi VU_KHI (gia 500) trong CSDL - hanh trang (inven) + dang mac (equip) - sau khi thoat. Ban ghi mon:
     * "<o>,<tpl>,<plus>,<gia>,...". (Goi 15 chi cho so o trang bi, khong cho biet mon nao dang mac.)
     */
    static int vuKhiCsdl(BotChucNang.Bot b) {
        String v = BotChucNang.sql1("SELECT CONCAT(IFNULL(inven,''), '#', IFNULL(equip,'')) FROM tob_char WHERE id = " + b.charDb);
        if (v == null) return -1;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("," + VU_KHI + ",-?\\d+,500,").matcher(v);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    static int demTpl(BotChucNang.Bot b, int tpl) {
        int n = 0;
        n += b.soDo(tpl); // cong so luong chong type 28 (ChongVatPham), khong dem o
        return n;
    }

    /** Ket ban (101): "dong y" khong co loi moi = ep ket ban; dong y lap = trung danh sach. */
    static void ketBan(BotChucNang.Bot a, BotChucNang.Bot c) throws IOException {
        int mc = c.moc(), ma = a.moc();
        for (int i = 0; i < 3; i++) a.gui(101, (short) c.actor, (byte) 1); // A tu "dong y" ma C chua he moi
        a.bom(1200);
        c.bom(300);
        boolean ep = c.doiChu(mc, 0, "đồng ý kết bạn") != null;
        ket("bt_ket_ban.khong_loi_moi", !ep, "A gui 101 ok=1 (khong co loi moi) x3: C " + (ep ? "BI THEM BAN: " + c.chuTu(mc) : "khong bi them"));
        // luong that: C moi A, A dong y -> thanh ban; A dong y lan nua -> khong them trung
        mc = c.moc();
        int ma2 = a.moc();
        c.gui(101, (short) a.actor, (byte) 0);
        boolean nhan = a.cho(x -> x.coLenh(ma2, 101), 8000);
        a.gui(101, (short) c.actor, (byte) 1);
        boolean dongY = c.doiChu(mc, 8000, "đồng ý kết bạn") != null;
        int mc2 = c.moc();
        a.gui(101, (short) c.actor, (byte) 1);
        a.bom(1000);
        c.bom(300);
        boolean lap = c.doiChu(mc2, 0, "đồng ý kết bạn") != null;
        ket("bt_ket_ban.luong_that", nhan && dongY && !lap, "C moi -> A nhan loi moi=" + nhan + ", A dong y -> C bao=" + dongY + ", A dong y lan 2 -> " + (lap ? "THEM LAN NUA" : "bo qua"));
    }

    static String khongDau(String s) {
        return BotChucNang.khongDau(s);
    }

    // ================================================================== 7. nang cap: ban da giua chung / rut mang
    static void nangCapBatThuong() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoiTrongNha();
        try {
            datXu(b, 2000000);
            int id = muaVuKhi(b, VU_KHI);
            if (id == KichBanDS3.KHONG) { ket("bt_nc", false, "khong mua duoc vu khi"); return; }
            lenhGm("gem " + b.ten + " 8 1 0");
            b.cho(x -> x.soNgoc(8) >= 1, 8000);
            onDinh(b);
            // dat vu khi + da luyen kim vao o, BAN da di, roi bam Dap
            int lkd = KichBanDS3.realIdNgoc(b, 8);
            b.gui(77, (byte) -1);
            int m = b.moc();
            b.gui(71, (byte) 0);
            boolean mo = b.doiMenu(m, -505, -1, 5000) >= 0;
            if (mo) { b.menu(-505, 0, 2); b.cho(x -> x.coLenh(m, 71), 5000); }
            b.gui(75, (short) id);
            b.gui(76, (short) lkd, (byte) 0);
            b.bom(500);
            b.gui(78, (short) lkd, (byte) 0);
            b.cho(x -> x.soNgoc(8) == 0, 5000);
            onDinh(b);
            long xu0 = b.xu;
            int[] d0 = b.timDo(x -> x[0] == id);
            int plus0 = d0 == null ? -9 : d0[2];
            int m2 = b.moc();
            b.gui(77, (byte) 1);
            b.bom(1500);
            onDinh(b);
            int[] d1 = b.timDo(x -> x[0] == id);
            int plus1 = d1 == null ? -9 : d1[2];
            String traLoi = b.chuTu(m2);
            ket("bt_nc.ban_da_giua_chung", lkd != KichBanDS3.KHONG && b.soNgoc(8) == 0 && plus1 == plus0 && b.xu == xu0 && traLoi.contains("cần"),
                    "mo o=" + mo + ", da sau khi ban=" + b.soNgoc(8) + "; Dap: +" + plus0 + " -> +" + plus1 + ", xu " + xu0 + " -> " + b.xu + " " + b.chuTu(m2));
            // bam Dap 6 lan lien voi 2 da: so lan len cap + tru xu khop so da da dung
            lenhGm("gem " + b.ten + " 8 2 0");
            b.cho(x -> x.soNgoc(8) >= 2, 8000);
            datXu(b, 2000000);
            int da0 = b.soNgoc(8);
            int lkd2 = KichBanDS3.realIdNgoc(b, 8);
            b.gui(77, (byte) -1);
            int m3 = b.moc();
            b.gui(71, (byte) 0);
            if (b.doiMenu(m3, -505, -1, 5000) >= 0) { b.menu(-505, 0, 2); b.cho(x -> x.coLenh(m3, 71), 5000); }
            b.gui(75, (short) id);
            b.gui(76, (short) lkd2, (byte) 0);
            b.bom(400);
            for (int i = 0; i < 6; i++) b.gui(77, (byte) 1);
            b.bom(2000);
            onDinh(b);
            int daDung = da0 - b.soNgoc(8);
            long tru = 2000000 - b.xu;
            ket("bt_nc.dap_lien", daDung >= 0 && daDung <= da0 && (daDung == 0) == (tru == 0) && b.xu >= 0,
                    "6 goi Dap lien voi " + da0 + " da: dung " + daDung + " da, tru " + tru + " xu, vu khi +" + (b.timDo(x -> x[0] == id) == null ? "?" : b.timDo(x -> x[0] == id)[2]));
            // rut mang ngay sau Dap: CSDL - tru xu <=> mat da
            lenhGm("gem " + b.ten + " 8 1 0");
            b.cho(x -> x.soNgoc(8) >= 1, 8000);
            onDinh(b);
            long x2 = b.xu;
            int da2 = b.soNgoc(8);
            int lkd3 = KichBanDS3.realIdNgoc(b, 8);
            b.gui(77, (byte) -1);
            int m4 = b.moc();
            b.gui(71, (byte) 0);
            if (b.doiMenu(m4, -505, -1, 5000) >= 0) { b.menu(-505, 0, 2); b.cho(x -> x.coLenh(m4, 71), 5000); }
            b.gui(75, (short) id);
            b.gui(76, (short) lkd3, (byte) 0);
            b.gui(77, (byte) 1);
            b = vaoLai(b);
            boolean truXu = b.xu < x2, matDa = b.soNgoc(8) < da2;
            ket("bt_nc.rut_mang", truXu == matDa && b.xu >= 0 && xuCsdl(b) == b.xu, "xu " + x2 + " -> " + b.xu + " (CSDL " + xuCsdl(b) + "), da " + da2 + " -> " + b.soNgoc(8));
        } finally {
            b.close();
        }
        kiemLog("bt_nc");
    }

    // ================================================================== 8. hanh dong khi da guc
    static void khiGuc() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoi();
        BotChucNang.Bot c = null;
        try {
            datXu(b, 50000);
            datThuoc(b, 1, 5);
            int map0 = b.mapId;
            lenhGm("die " + b.ten);
            b.cho(x -> x.hp <= 0, 8000);
            onDinh(b);
            boolean guc = b.hp <= 0;
            long xu0 = b.xu;
            int th0 = b.soThuoc(1);
            int m = b.moc();
            // khi guc: mua thuoc (khong o shop), dung thuoc, qua cong la, moi giao dich, doi khu
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 3);
            b.gui(22, (byte) 1);
            b.gui(29, (short) -1);
            b.chat("khu");
            b.bom(800);
            b.menu(-34, 0, 1);
            b.bom(1500);
            onDinh(b);
            ket("bt_guc.khong_hoi_mau", !guc || b.hp <= 0, "guc=" + guc + ", hp sau khi dung thuoc " + b.hp + " " + b.chuTu(m));
            ket("bt_guc.bao_toan", b.xu <= xu0 && b.soThuoc(1) <= th0 + 3 && b.xu + (long) GIA_THUOC_1 * (b.soThuoc(1) - th0) <= xu0 + 0,
                    "xu " + xu0 + " -> " + b.xu + ", thuoc1 " + th0 + " -> " + b.soThuoc(1));
            // ve lang (31) -> song lai o lang, ket noi tot
            int m2 = b.moc();
            b.gui(31, new byte[0]);
            b.cho(x -> x.coLenh(m2, 12), 8000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            onDinh(b);
            ket("bt_guc.ve_lang", guc && b.hp > 0 && b.coLenh(m2, 12), "map " + map0 + " -> " + b.mapId + " (" + b.tenMap + "), hp " + b.hp);
            // 31 khi DANG SONG (khong dung ve) -> khong duoc dich chuyen mien phi
            int map1 = b.mapId;
            int m3 = b.moc();
            b.gui(31, new byte[0]);
            b.bom(2500);
            ket("bt_guc.ve_lang_khi_song", !b.coLenh(m3, 12), "goi 31 khi con song: " + (b.coLenh(m3, 12) ? "BI DICH CHUYEN toi " + b.mapId : "dung yen o " + map1));
            // vao lai sau khi guc giua chung (rut mang luc dang guc)
            lenhGm("die " + b.ten);
            b.cho(x -> x.hp <= 0, 8000);
            b = vaoLai(b);
            int m4 = b.moc();
            if (b.hp <= 0) { b.gui(31, new byte[0]); b.cho(x -> x.coLenh(m4, 12), 8000); b.gui(12, (short) -500, (short) 0, (short) 0); onDinh(b); }
            ket("bt_guc.rut_mang_khi_guc", b.hp > 0 && b.mapId >= 0, "vao lai: map " + b.mapId + " hp " + b.hp);
        } finally {
            b.close();
            if (c != null) c.close();
        }
        kiemLog("bt_guc");
    }

    // ================================================================== 9. goi tin sai / cut / id la cho nhieu lenh
    static void goiSai() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = nguoiTrongNha();
        int dong = 0;
        try {
            datXu(b, 100000);
            datThuoc(b, 1, 10);
            onDinh(b);
            long xu0 = b.xu;
            int luong0 = b.luong, lk0 = b.lk;
            Map<Integer, Integer> th0 = new LinkedHashMap<>(b.thuoc);
            int nDo = b.doDung.size(), tongDo = b.tongMon(), nNgoc = b.ngoc.size(); // tong: dup vao chong khong tang so O
            Random r = new Random(20261025L);
            int[] lenh = {22, 24, 28, 29, 61, 66, 68, 69, 71, 74, 75, 76, 77, 78, 85, 92, 94, 95, 108, 110, -21, -23, -30, -34, -35, -36, -45, -52, -62, -68, -69, -107, -119, -120};
            int n = 0;
            for (int cmd : lenh) {
                List<byte[]> mau = Arrays.asList(new byte[0],
                        BotChucNang.goi((byte) -1, (short) -1, (short) -1, -1),
                        BotChucNang.goi((byte) 127, (short) 32767, (short) -32768, Integer.MAX_VALUE),
                        ngauNhien(r, 1 + r.nextInt(12)));
                for (byte[] g : mau) {
                    if (b.dut || !b.conSong()) { dong++; b = vaoLai(b); if (!KichBanDS2.laOffline(b.mapId)) KichBanDS2.vaoNha(b, 103); }
                    b.gui(cmd, g);
                    n++;
                }
                b.bom(150);
            }
            b.bom(1500);
            if (!b.conSong()) { dong++; b = vaoLai(b); }
            onDinh(b);
            ghiLogGoiSai("bt_goi_sai");
            ket("bt_goi_sai.khong_loi", b.xu <= xu0 && b.luong <= luong0 && b.lk <= lk0 && b.doDung.size() <= nDo && b.tongMon() <= tongDo && b.ngoc.size() <= nNgoc && khongTang(th0, b.thuoc),
                    n + " goi sai / cut tren " + lenh.length + " lenh: xu " + xu0 + " -> " + b.xu + ", luong " + luong0 + " -> " + b.luong + ", lk " + lk0 + " -> " + b.lk
                            + ", mon " + nDo + " -> " + b.doDung.size() + " (tong " + tongDo + " -> " + b.tongMon() + "), ngoc " + nNgoc + " -> " + b.ngoc.size() + ", thuoc " + th0 + " -> " + b.thuoc + " (bi dong ket noi " + dong + " lan)");
            ket("bt_goi_sai.xu_khong_am", b.xu >= 0 && xuCsdlSauThoat(b) >= 0, "xu " + b.xu);
        } finally {
            b.close();
        }
        ket("bt_goi_sai.may_chu_song", mayChuSong(), "");
        kiemLog("bt_goi_sai");
    }

    static long xuCsdlSauThoat(BotChucNang.Bot b) throws Exception {
        BotChucNang.Bot c = vaoLai(b);
        long x = xuCsdl(c);
        c.close();
        return x;
    }

    static boolean khongTang(Map<Integer, Integer> truoc, Map<Integer, Integer> sau) {
        for (Map.Entry<Integer, Integer> e : sau.entrySet()) if (e.getValue() > truoc.getOrDefault(e.getKey(), 0)) return false;
        return true;
    }

    static byte[] ngauNhien(Random r, int n) {
        byte[] b = new byte[n];
        r.nextBytes(b);
        return b;
    }

    // ================================================================== 10. thu (mail): doc thu khong xoa thu nguoi khac
    static void docThu() throws Exception {
        loiLogMoi();
        BotChucNang.Bot b = BotChucNang.botThuong();
        String u = b.user, ten = b.ten;
        b.close();
        Thread.sleep(2500);
        // thu dang cho cua NGUOI KHAC co id nho (0..3 = chi so thu ma nguoi doc gui len): tao mot thu o id trong (neu co)
        for (int id = 1; id <= 3; id++) {
            if (BotChucNang.sql1("SELECT id FROM tob_inbox WHERE id = " + id) == null) {
                BotChucNang.sql("INSERT INTO tob_inbox (id, charname, sender, info, dateSend) VALUES (" + id + ", 'bt_nan_nhan', 'He thong', 'thu cua nguoi khac', NOW())");
                break;
            }
        }
        // nguoi choi co 4 thu cho san (dang nhap -> may chu gui hop thu -5)
        for (int i = 0; i < 4; i++)
            BotChucNang.sql("INSERT INTO tob_inbox (charname, sender, info, dateSend) VALUES ('" + ten + "', 'bt', 'thu " + i + "', NOW())");
        List<String> truoc = BotChucNang.sql("SELECT id FROM tob_inbox WHERE id <= 3 AND charname <> '" + ten + "' ORDER BY id");
        BotChucNang.Bot c = new BotChucNang.Bot(u);
        try {
            String l = c.dangNhap("123456");
            if (l == null) l = vaoGameNhanh(c);
            if (l != null) { ket("bt_thu.doc_thu", false, l); return; }
            onDinh(c);
            if (truoc.isEmpty()) { BotChucNang.ket("bt_thu.doc_thu", null, "khong co thu nguoi khac o id 0..3 de doi chieu"); return; }
            // doc tung thu (client gui 110 + chi so thu trong hop)
            for (int i = 0; i < 4; i++) c.gui(110, (byte) i);
            onDinh(c);
            c.bom(1000);
            List<String> sau = BotChucNang.sql("SELECT id FROM tob_inbox WHERE id <= 3 AND charname <> '" + ten + "' ORDER BY id");
            ket("bt_thu.doc_thu", sau.equals(truoc), "doc 4 thu cua minh (chi so 0..3): thu nguoi khac id " + truoc + " -> con " + sau);
        } finally {
            c.close();
            BotChucNang.sql("DELETE FROM tob_inbox WHERE charname = 'bt_nan_nhan'");
        }
        kiemLog("bt_thu");
    }

    // ================================================================== 11. tien / vat pham: tru TRUOC trao (29/10/2026)
    /** So ban VU_KHI trong CSDL ca hanh trang + dang mac + KHO (bag) - mot mon chi duoc nam o MOT noi. */
    static int vuKhiCsdlKho(BotChucNang.Bot b) {
        String v = BotChucNang.sql1("SELECT CONCAT(IFNULL(inven,''), '#', IFNULL(equip,''), '#', IFNULL(bag,'')) FROM tob_char WHERE id = " + b.charDb);
        if (v == null) return -1;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("," + VU_KHI + ",-?\\d+,500,").matcher(v);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    /** Mo kho do (NPC 9 Kangaskhan; trong nha = ban do offline nen o dau cung mo duoc) -> goi 23 kieu 2. */
    static boolean moKho(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(23, (byte) 9);
        int n = b.doiMenu(m, 9, 0, 3000);
        int mk = b.moc();
        if (n > 0 && b.timMuc("Mở kho") >= 0) b.menu(9, 0, b.timMuc("Mở kho"));
        return b.cho(x -> x.coLenh(mk, 23) || x.coLenh(m, 23), 4000);
    }

    /**
     * TIEN / VAT PHAM: tru TRUOC trao (29/10/2026, ma-tran-bot "Lo hong uu tien" #2 #5 #6 #9 #14). MOT nguoi choi thuong P
     * (trong nha 103 = ban do offline: Kecleon, tho ren, kho do, gian hang) lam lien bon phan de giu thoi gian nhom <= 15 phut:
     * <ol>
     *   <li>{@code bt_thu_ban}: 2 thu cuoi (chep tu thu GM "poro 1" qua CSDL truoc khi dang nhap) DA KHOA (-119/2), ban NPC
     *       (-800 chon 1) x3 -> khong mat con nao; mo khoa 1 con, ban 1 lan -> mat dung 1 (CSDL tob_animal khop);</li>
     *   <li>{@code bt_ep}: xu VUA DU mot lan ghep ngoc (5000), 3 lenh ghep 5 vien (100%) xen 3 lenh mua thuoc -> ghep <= 1 lan,
     *       xu tieu = 5000 x lan + 100 x binh, ngoc dung = 5 x lan;</li>
     *   <li>{@code bt_kho}: 31 goi 68/69 xen ke don dap cung MOT vu khi (68 luong phien, 69 truoc day luong ban do);</li>
     *   <li>{@code bt_cho}: A rao vu khi 10.000 xu; P thieu 1 xu xac nhan -> khong mua; P vua du xac nhan -32 x3 xen mua thuoc
     *       -> mua dung 1 lan, P tieu = gia, A nhan = gia - thue.</li>
     * </ol>
     * Cuoi: thoat CA HAI, doi may chu luu (kick sau ~5 giay), doi chieu CSDL: vu khi P = khoi dau + 2 (kho + mua cho), A =
     * khoi dau (da ban), xu khop, khong am.
     */
    static void tienVatPham() throws Exception {
        loiLogMoi();
        BotChucNang.Bot g = gm();
        g.gm("poro 1", 1500);
        String nguon = BotChucNang.sql1("SELECT id FROM tob_animal WHERE owner = " + g.charDb + " AND del = 0 ORDER BY id DESC LIMIT 1");
        BotChucNang.Bot b0 = BotChucNang.botThuong();
        String u = b0.user;
        int cid = b0.charDb;
        b0.close();
        boolean coThu = nguon != null && nguon.trim().matches("\\d+");
        Thread.sleep(2500);
        if (coThu)
            for (int i = 0; i < 2; i++)
                BotChucNang.sql("INSERT INTO tob_animal (owner, place, att, id_img, lv, name, texpire, isHoaHinh, timeHoaHinh) SELECT " + cid
                        + ", 0, att, id_img, lv, name, texpire, isHoaHinh, timeHoaHinh FROM tob_animal WHERE id = " + nguon.trim());
        BotChucNang.Bot b = vaoLai(new BotChucNang.Bot(u));
        BotChucNang.Bot a = null;
        try {
            KichBanDS.veLang(b);
            if (!KichBanDS2.vaoNha(b, 103)) { ket("bt_tien", false, "khong vao duoc nha (map " + b.mapId + ")"); return; }
            onDinh(b);
            lenhGm("slv " + b.ten + " 20 0");
            final int goc = vuKhiCsdlKho(b); // vu khi khoi dau (nhan vat moi mac san mot Than lua so cap)

            // ---- 1. ban thu cho NPC: con da khoa
            String demSql = "SELECT COUNT(*) FROM tob_animal WHERE owner = " + cid + " AND del = 0";
            int n0 = b.thu.size();
            if (!coThu || n0 < 2) {
                BotChucNang.ket("bt_thu_ban", coThu ? Boolean.FALSE : null, "khong dung duoc canh: GM poro=" + coThu + ", thu cua nguoi choi " + n0);
            } else {
                for (int[] t : new ArrayList<>(b.thu)) b.gui(-119, (byte) 2, (byte) 0, (short) t[0], (byte) 1);
                b.bom(800);
                int m = b.moc();
                for (int i = 0; i < 3; i++) { b.menu(-800, 0, 1); b.bom(300); }
                b.bom(800);
                onDinh(b);
                int n1 = b.thu.size();
                String db1 = BotChucNang.sql1(demSql);
                ket("bt_thu_ban.da_khoa", n1 == n0 && ("" + n0).equals(db1 == null ? "" : db1.trim()),
                        n0 + " thu da khoa, -800 ban x3: trong game " + n0 + " -> " + n1 + ", CSDL " + db1 + " " + b.chuTu(m));
                if (!b.thu.isEmpty()) {
                    b.gui(-119, (byte) 2, (byte) 0, (short) b.thu.get(0)[0], (byte) 0);
                    b.bom(600);
                    b.menu(-800, 0, 1);
                    b.bom(1000);
                    onDinh(b);
                    String db2 = BotChucNang.sql1(demSql);
                    ket("bt_thu_ban.mo_khoa", b.thu.size() == n1 - 1 && ("" + (n1 - 1)).equals(db2 == null ? "" : db2.trim()),
                            "mo khoa 1 con, ban 1 lan: " + n1 + " -> " + b.thu.size() + " (CSDL " + db2 + ")");
                }
            }

            // ---- 2. ghep ngoc voi xu vua du + lenh tieu xu xen giua
            lenhGm("gem " + b.ten + " 60 15 0");
            b.cho(x -> x.soNgoc(60) >= 15, 8000);
            datThuoc(b, 1, 0);
            boolean ke = moKecleon(b);
            datXu(b, 5000);
            int g60 = b.soNgoc(60), g61 = b.soNgoc(61);
            int rid = KichBanDS3.realIdNgoc(b, 60);
            for (int i = 0; i < 3; i++) {
                b.gui(-36, (byte) 1, (byte) 5, (short) rid, (short) rid, (short) rid, (short) rid, (short) rid);
                b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 1);
            }
            b.bom(1500);
            onDinh(b);
            int lan = b.soNgoc(61) - g61, dung = g60 - b.soNgoc(60), binh = b.soThuoc(1);
            long tieu = 5000 - b.xu;
            ket("bt_ep.vua_du", rid != KichBanDS3.KHONG && g60 >= 15 && lan >= 0 && lan <= 1 && dung == 5 * lan && b.xu >= 0
                            && tieu == 5000L * lan + (long) GIA_THUOC_1 * binh,
                    "kecleon=" + ke + "; 3 lenh ghep + 3 lenh mua voi 5000 xu: ghep " + lan + " lan, ngoc60 " + g60 + " -> " + b.soNgoc(60) + ", binh " + binh
                            + ", xu 5000 -> " + b.xu + " (tieu " + tieu + ")");

            // ---- 3. kho do: 68/69 xen ke don dap cung mot mon
            datXu(b, 2000);
            int id = muaVuKhi(b, VU_KHI);
            boolean mo = id != KichBanDS3.KHONG && moKho(b);
            if (mo) {
                for (int i = 0; i < 15; i++) { b.gui(68, (short) id); b.gui(69, (short) id); }
                b.gui(68, (short) id);
                b.bom(1500);
                onDinh(b);
            }
            final int tuiKho = demTpl(b, VU_KHI);

            // ---- 4. cho ky gui: A rao, P thieu 1 xu / vua du + xac nhan x3
            a = nguoiTrongNha();
            lenhGm("slv " + a.ten + " 20 0");
            final int gocA = vuKhiCsdl(a);
            datXu(a, 50000);
            int idA = muaVuKhi(a, VU_KHI);
            final int gia = 10000;
            int npc = 11, shop = 0, m = a.moc();
            for (npc = 11; idA != KichBanDS3.KHONG && npc <= 20; npc++) {
                m = a.moc();
                final int m92 = m;
                a.gui(92, true, (byte) npc, (byte) shop, (short) idA, gia, (byte) 0);
                a.cho(x -> x.layLenh(m92, -31) != null || x.coLenh(m92, 92), 3000);
                if (a.layLenh(m, -31) != null) { a.gui(-31, (short) -32047, (byte) 0, ""); a.bom(1000); }
                if (a.coLenh(m, 92)) break;
                if (a.doiChu(m, 0, "chỉ bán") == null) break;
            }
            boolean daRao = idA != KichBanDS3.KHONG && a.coLenh(m, 92);
            if (!daRao) ket("bt_cho.dang_ban", false, "A mua=" + (idA != KichBanDS3.KHONG) + " " + a.chuTu(m));
            onDinh(a);
            final int npcB = npc;
            final int tpl0 = demTpl(b, VU_KHI);
            datXu(b, gia - 1);
            long xa0 = a.xu;
            BotChucNang.SuKien p1 = daRao ? choHoi(b, npcB, shop, a, idA) : null;
            if (p1 != null) b.gui(-32, (short) p1.so[0], (byte) p1.so[1], "", (byte) 1);
            b.bom(1200);
            onDinh(b);
            onDinh(a);
            ket("bt_cho.thieu_mot_xu", daRao && b.xu == gia - 1 && a.xu == xa0 && demTpl(b, VU_KHI) == tpl0,
                    "hoi=" + (p1 != null) + "; P " + (gia - 1) + " -> " + b.xu + ", A " + xa0 + " -> " + a.xu + ", P vu khi " + tpl0 + " -> " + demTpl(b, VU_KHI));
            datThuoc(b, 1, 0);
            moKecleon(b);
            datXu(b, gia);
            xa0 = a.xu;
            BotChucNang.SuKien p = daRao ? choHoi(b, npcB, shop, a, idA) : null;
            if (p != null) {
                for (int i = 0; i < 3; i++) {
                    b.gui(-32, (short) p.so[0], (byte) p.so[1], "", (byte) 1);
                    b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 1);
                }
            }
            b.bom(1500);
            a.bom(300);
            onDinh(b);
            onDinh(a);
            int binh2 = b.soThuoc(1);
            boolean coMon = demTpl(b, VU_KHI) == tpl0 + 1;
            long tieu2 = gia - b.xu - (long) GIA_THUOC_1 * binh2;
            long nhan = a.xu - xa0;
            ket("bt_cho.vua_du", p != null && b.xu >= 0 && demTpl(b, VU_KHI) <= tpl0 + 1 && (coMon ? tieu2 == gia && nhan > 0 && nhan <= gia : tieu2 == 0 && nhan == 0),
                    "P co mon=" + coMon + ", P tieu cho mon " + tieu2 + " (+" + binh2 + " binh), A nhan " + nhan + "/" + gia + " (sau thue), P xu " + b.xu);

            // ---- thoat ca hai, doi may chu luu, doi chieu CSDL
            long xaG = a.xu, xbG = b.xu;
            a.close();
            b.close();
            Thread.sleep(7500); // may chu kick + luu khi thoat sau ~5 giay (CharVongLap: now - timeExit > 5000)
            int vb = vuKhiCsdlKho(b), va = vuKhiCsdlKho(a);
            long xaD = xuCsdl(a), xbD = xuCsdl(b);
            ket("bt_kho.don_dap", mo && vb == goc + (coMon ? 2 : 1) && tuiKho <= 1,
                    "mo kho=" + mo + "; 31 goi 68/69 xen ke cung mon: tui " + tuiKho + ", sau khi thoat CSDL (tui+mac+kho) " + vb + " (dung khoi dau " + goc
                            + " + 1 kho" + (coMon ? " + 1 mua cho" : "") + ")");
            ket("bt_cho.csdl", coMon && va == gocA && xaD == xaG && xbD == xbG && xbD >= 0 && xaD >= 0,
                    "vu khi A CSDL " + va + " (khoi dau " + gocA + ", da ban); xu A " + xaG + " (CSDL " + xaD + "), P " + xbG + " (CSDL " + xbD + ")");
        } finally {
            b.close();
            if (a != null) a.close();
        }
        kiemLog("bt_tien");
    }

    /** C mo gian hang cua A, hoi mua mon id (95) -> cho popup -32. */
    static BotChucNang.SuKien choHoi(BotChucNang.Bot c, int npc, int shop, BotChucNang.Bot a, int id) throws IOException {
        int m = c.moc();
        c.gui(94, (byte) 0, (byte) npc, (byte) shop);
        c.bom(400);
        c.gui(94, (byte) 1, (byte) npc, (byte) shop, (short) a.actor);
        c.bom(400);
        c.gui(95, (byte) npc, (byte) shop, (short) a.actor, (short) id, (byte) 0);
        c.cho(x -> x.layLenh(m, -32) != null, 4000);
        return c.layLenh(m, -32);
    }

    // ================================================================== dang ky + main
    static void dk(String ten, BotChucNang.KichBan k) {
        BotChucNang.dk(ten, false, () -> {
            long t = System.currentTimeMillis();
            try {
                k.chay();
            } finally {
                System.out.println("  [thoi gian] " + ten + " " + (System.currentTimeMillis() - t) / 1000 + " s");
            }
        });
    }

    static void dangKy() {
        dk("bt_mua", KichBanBatThuong::muaNhanh);
        dk("bt_ban", KichBanBatThuong::banNhanh);
        dk("bt_tui", KichBanBatThuong::tuiDay);
        dk("bt_dut", KichBanBatThuong::dutGiuaChung);
        dk("bt_dang_nhap", KichBanBatThuong::dangNhapBao);
        dk("bt_gd", KichBanBatThuong::giaoDichDoi);
        dk("bt_nc", KichBanBatThuong::nangCapBatThuong);
        dk("bt_guc", KichBanBatThuong::khiGuc);
        dk("bt_goi_sai", KichBanBatThuong::goiSai);
        dk("bt_thu", KichBanBatThuong::docThu);
        dk("bt_tien", KichBanBatThuong::tienVatPham);
    }

    public static void main(String[] a) throws Exception {
        String log = System.getProperty("log", "");
        if (!log.isEmpty()) { tepLog = new File(log); viTri = tepLog.isFile() ? tepLog.length() : 0; }
        dangKy();
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(Arrays.asList("bt_mua", "bt_ban", "bt_tui", "bt_dut", "bt_dang_nhap", "bt_gd", "bt_nc", "bt_guc", "bt_goi_sai", "bt_thu", "bt_tien"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        try {
            BotChucNang.main(b);
        } finally {
            if (gm != null) gm.close();
        }
    }
}
