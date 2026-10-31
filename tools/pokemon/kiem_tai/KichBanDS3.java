import java.io.IOException;
import java.util.*;

/** Kich ban nhom 3: vat pham, trang bi, nang cap, ngoc, shop, giao dich, cho, thu cuoi. */
class KichBanDS3 {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("thuoc", true, KichBanDS3::thuoc);
        BotChucNang.dk("shop_npc", true, KichBanDS3::shopNpc);
        BotChucNang.dk("shop_luong", true, KichBanDS3::shopLuong);
        BotChucNang.dk("trang_bi", true, KichBanDS3::trangBi);
        BotChucNang.dk("nang_cap", true, KichBanDS3::nangCap);
        BotChucNang.dk("ngoc", true, KichBanDS3::ngoc);
        BotChucNang.dk("phan_ra", true, KichBanDS3::phanRa);
        BotChucNang.dk("giao_dich", true, KichBanDS3::giaoDich);
        BotChucNang.dk("cho", true, KichBanDS3::cho);
        BotChucNang.dk("thu_cuoi", true, KichBanDS3::thuCuoi);
        KichBanDS4.dangKy();
    }

    /** Khong co mon (id vat pham la short, co the am). */
    static final int KHONG = Integer.MIN_VALUE;

    static BotChucNang.Bot botLang(int lv) throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        KichBanDS.veLang(b);
        if (lv > 1) b.gm("slv " + b.ten + " " + lv + " 0", 800);
        b.lamMoi();
        if (b.xu < 20000000) b.gm("smn " + b.ten + " " + (20000000 - b.xu) + " 0 0", 600); // tai khoan dung lai
        b.lamMoi();
        return b;
    }

    /** Bot o trong nha (OfflineMap) - shop NPC chi xu ly o day. */
    static BotChucNang.Bot botNha(int lv) throws Exception {
        return botNha(lv, 103);
    }

    /** Bot o lang, vao nha {@code nha} (Pewter: 102 Kecleon - thuoc, 103 Machamp - vu khi, 104 Bronzong - giap). */
    static BotChucNang.Bot botNha(int lv, int nha) throws Exception {
        BotChucNang.Bot b = botLang(lv);
        if (!KichBanDS2.vaoNha(b, nha)) System.out.println("  (khong vao duoc nha " + nha + " tu map " + b.mapId + ")");
        return b;
    }

    /** Mua 1 mon tu shop vu khi (NPC 2) - tra ve id mon moi trong tui hoac -1. */
    static int muaVuKhi(BotChucNang.Bot b, int tpl) throws IOException {
        // client chuan (HLV AI): may chu chi ban khi dung canh NPC ren (NpcTinhBanDo.banTaiCho, BAN_KINH_O) - di toi NPC
        // nhu nguoi choi, khong thi bi ghi "hackbitem" trong nhat ky gian lan
        if (BotChucNang.Bot.CHUAN_CLIENT && KichBanHuongDan.oNpc(b.mapLoad, 2) != null) {
            String loi = KichBanHuongDan.toiNpc(b, 2);
            if (loi != null) return KHONG;
        }
        int m = b.moc();
        b.gui(23, (byte) 2);
        int n = b.doiMenu(m, 2, 0, 3000);
        if (n > 0) { b.menu(2, 0, 0); final int mm = m; b.cho(x -> x.coLenh(mm, 23), 3000); }
        Set<Integer> cu = new HashSet<>();
        for (int[] d : b.doDung) cu.add(d[0]);
        b.gui(24, (byte) 1, (byte) 3, (short) tpl, (short) 1, (byte) 0);
        b.bom(1200);
        int[] moi = b.timDo(d -> d[1] == tpl && !cu.contains(d[0]));
        return moi == null ? KHONG : moi[0];
    }

    /**
     * GM ait: them mon tpl (chi trong RAM) - tra ve id moi hoac -1. Vat pham type 28 xep chong (ChongVatPham): mon moi
     * don vao chong san co (khong co id moi) -&gt; tra id chong vua TANG so luong.
     */
    static int aitMon(BotChucNang.Bot b, int tpl, int clazz) throws IOException {
        Map<Integer, Integer> cu = new HashMap<>();
        for (int[] d : b.doDung) cu.put(d[0], BotChucNang.Bot.soO(d));
        b.gm("ait " + b.ten + " " + tpl + " " + clazz + " 0 0", 1000);
        int[] moi = b.timDo(d -> d[1] == tpl && !cu.containsKey(d[0]));
        if (moi == null) moi = b.timDo(d -> d[1] == tpl && BotChucNang.Bot.soO(d) > cu.getOrDefault(d[0], Integer.MAX_VALUE));
        return moi == null ? KHONG : moi[0];
    }

    // ------------------------------------------------------------------ thuoc
    static void thuoc() throws Exception {
        try (BotChucNang.Bot b = botLang(20)) {
            b.gm("spt " + b.ten + " 1 20", 600);
            b.gm("spt " + b.ten + " 4 20", 600);
            b.gm("spt " + b.ten + " 19 2", 600);
            b.lamMoi();
            int h0 = b.soThuoc(1), m0 = b.soThuoc(4);
            // lam mat mau: xuong map quai dung yen cho quai danh
            b.den(2, 20, 20);
            b.gm("slv " + b.ten + " 25 0", 600); // slv giu nguyen hp -> hp < maxhp moi
            b.lamMoi();
            boolean thieu = b.hp < b.maxhp;
            for (int i = 0; i < 3; i++) { b.gui(22, (byte) 1); b.bom(400); }
            for (int i = 0; i < 3; i++) { b.gui(22, (byte) 4); b.bom(400); }
            b.lamMoi();
            ket("thuoc.hp", b.soThuoc(1) < h0 || !thieu, "thuoc 1: " + h0 + " -> " + b.soThuoc(1) + " (hp " + b.hp + "/" + b.maxhp + ", truoc do thieu mau=" + thieu + ")");
            ket("thuoc.mp", b.soThuoc(4) <= m0, "thuoc 4: " + m0 + " -> " + b.soThuoc(4));
            int m = b.moc();
            int mapTruoc = b.mapId;
            b.gui(22, (byte) 19);
            b.bom(10500); // client: dung yen 10 giay (Char.comeHome) roi gui lenh 31
            b.gui(31, new byte[0]);
            b.cho(x -> x.coLenh(m, 12), 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(800);
            b.lamMoi();
            ket("thuoc.ve_thanh", b.coLenh(m, 12) && b.mapId != mapTruoc, "map " + mapTruoc + " -> " + b.mapId + " (" + b.tenMap + "), con " + b.soThuoc(19) + " " + b.chuTu(m));
            // bo thuoc ra dat: xoa ca chong
            b.gui(-21, (short) 1);
            b.bom(600);
            b.lamMoi();
            ket("thuoc.bo_ra_dat", b.soThuoc(1) == 0, "thuoc 1 sau -21: " + b.soThuoc(1));
            // dung thuoc khong co: khong lam gi, khong ngat
            b.gui(22, (byte) 2);
            b.bom(400);
            ket("thuoc.khong_co", b.conSong(), "dung thuoc 2 khi khong co -> ket noi con song");
        }
    }

    // ------------------------------------------------------------------ shop NPC
    static void shopNpc() throws Exception {
        // 01/11/2026: goi 24 thuoc chi mua canh Chansey / Kecleon -> mo shop thuoc trong nha Kecleon 102, vu khi sang nha 103
        try (BotChucNang.Bot b = botNha(10, 102)) {
            int m = b.moc();
            final int m23 = m;
            b.gui(23, (byte) 1);
            // 0.9.15+: Kecleon mo menu truoc ("Mua thuốc, vật phẩm" / "Vé dịch chuyển") -> chon muc dau -> cua so thuoc (23)
            b.cho(x -> x.coLenh(m23, 23) || x.coLenh(m23, -30), 3000);
            if (!b.coLenh(m, 23) && b.coLenh(m, -30)) {
                int i = b.timMuc("Mua thuốc");
                if (i >= 0) b.menu(b.menuCuoi[0], b.menuCuoi[1], i);
                b.cho(x -> x.coLenh(m23, 23), 3000);
            }
            ket("shop_npc.mo_shop_thuoc", b.coLenh(m, 23), b.chuTu(m));
            long xu0 = b.xu;
            int t0 = b.soThuoc(1);
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 5);
            b.bom(1200);
            b.lamMoi();
            ket("shop_npc.mua_thuoc", b.soThuoc(1) == t0 + 5 && b.xu < xu0, "thuoc 1: " + t0 + " -> " + b.soThuoc(1) + ", xu " + xu0 + " -> " + b.xu);
            long xu1 = b.xu;
            KichBanDS2.raNgoai(b);
            if (!KichBanDS2.vaoNha(b, 103)) System.out.println("  (khong sang duoc nha Machamp 103 tu map " + b.mapId + ")");
            int id = muaVuKhi(b, 79);
            b.lamMoi();
            ket("shop_npc.mua_vu_khi", id != KHONG && b.xu < xu1, "id moi=" + id + ", xu " + xu1 + " -> " + b.xu);
            // mon khong ban o NPC (tpl 1034 Bui sao): phai bi tu choi
            long xu2 = b.xu;
            int nTui = b.doDung.size();
            b.gui(24, (byte) 1, (byte) 3, (short) 1034, (short) 1, (byte) 0);
            b.bom(1000);
            b.lamMoi();
            ket("shop_npc.mon_khong_ban", b.doDung.size() == nTui && b.xu == xu2, "tui " + nTui + " -> " + b.doDung.size() + ", xu " + xu2 + " -> " + b.xu);
            if (id != KHONG) {
                long xu3 = b.xu;
                m = b.moc();
                b.gui(28, (short) id);
                b.bom(1000);
                b.lamMoi();
                ket("shop_npc.ban_do", b.timDo(d -> d[0] == id) == null && b.xu > xu3, "ban id " + id + ": xu " + xu3 + " -> " + b.xu + " " + b.chuTu(m));
            }
            // thieu xu: mua mon dat khi xu = 0
            b.gm("smn " + b.ten + " " + (-b.xu) + " 0 0", 800);
            b.lamMoi();
            int t1 = b.soThuoc(1);
            b.gui(24, (byte) 1, (byte) 4, (short) 3, (short) 50);
            b.bom(800);
            b.lamMoi();
            ket("shop_npc.thieu_xu", b.xu >= 0 && b.soThuoc(3) == 0, "xu=" + b.xu + " thuoc3=" + b.soThuoc(3) + " (khong duoc cap)");
        }
    }

    // ------------------------------------------------------------------ shop luong (85)
    static void shopLuong() throws Exception {
        try (BotChucNang.Bot b = botLang(10)) {
            // mon 2 tra bang LUONG KHOA (data_shop_gia_lau_dai.tien_moi=2); nhan vat moi khong co luong khoa -> GM cap
            if (b.lk < 5000) { b.gm("smn " + b.ten + " 0 0 " + (5000 - b.lk), 600); b.lamMoi(); }
            int lk0 = b.lk, l0 = b.luong;
            int t0 = b.soThuoc(2) + b.soThuoc(8);
            int m = b.moc();
            b.gui(85, (byte) 2); // 200 Sieu thuoc hoi mau: luong khoa (data_shop_gia_lau_dai)
            b.bom(1500);
            b.lamMoi();
            String chu = b.chuTu(m);
            ket("shop_luong.mua", b.lk < lk0 || b.luong < l0, "lk " + lk0 + " -> " + b.lk + ", luong " + l0 + " -> " + b.luong + " " + chu);
            ket("shop_luong.tien_dung_loai", b.luong == l0 || b.lk == lk0, "chi tru mot loai tien: lk -" + (lk0 - b.lk) + " luong -" + (l0 - b.luong));
            // gioi han tuan: ve gio vang 100 (id 0) 1/tuan
            b.gui(85, (byte) 0);
            b.bom(1200);
            b.lamMoi();
            int lk1 = b.lk, l1 = b.luong;
            m = b.moc();
            b.gui(85, (byte) 0);
            b.bom(1200);
            b.lamMoi();
            ket("shop_luong.gioi_han_tuan", b.lk == lk1 && b.luong == l1, "lan 2: lk " + lk1 + " -> " + b.lk + ", luong " + l1 + " -> " + b.luong + " " + b.chuTu(m));
        }
    }

    // ------------------------------------------------------------------ trang bi
    static void trangBi() throws Exception {
        try (BotChucNang.Bot b = botNha(12)) {
            int id1 = muaVuKhi(b, 79);
            int id2 = muaVuKhi(b, 80);
            if (id1 == KHONG || id2 == KHONG) { ket("trang_bi", false, "khong mua duoc vu khi " + id1 + "," + id2 + " " + b.chuTu(0)); return; }
            int m = b.moc();
            b.gui(29, (short) id1);
            b.bom(1200);
            boolean mac1 = b.timDo(d -> d[0] == id1) == null && b.coLenh(m, 15);
            ket("trang_bi.mac", mac1, "mac id " + id1 + ": con trong tui=" + (b.timDo(d -> d[0] == id1) != null) + " so mac=" + b.soMac);
            m = b.moc();
            b.gui(29, (short) id2);
            b.bom(1200);
            boolean doi = b.timDo(d -> d[0] == id2) == null && b.timDo(d -> d[0] == id1) != null;
            ket("trang_bi.doi_mon", doi, "mac id " + id2 + " -> id " + id1 + " ve tui=" + (b.timDo(d -> d[0] == id1) != null));
            // vu khi lop khac (86 = lop 1): khong mac duoc - GM (isAdmin) duoc bo qua kiem tra nay
            // (Map.doUseItem), nen thu bang tai khoan thuong
            try (BotChucNang.Bot t = BotChucNang.botThuong()) {
                KichBanDS.veLang(t);
                KichBanDS2.vaoNha(t, 103);
                t.lamMoi();
                int id3 = muaVuKhi(t, 86);
                if (id3 != KHONG) {
                    int mt = t.moc();
                    t.gui(29, (short) id3);
                    t.bom(1000);
                    ket("trang_bi.sai_lop", t.timDo(d -> d[0] == id3) != null, t.user + ": vu khi lop 1 van trong tui " + t.chuTu(mt));
                } else ket("trang_bi.sai_lop", null, t.user + " khong mua duoc vu khi lop 1 (map " + t.mapId + ")");
            }
            // vu khi cap cao (85 = cap 31) voi nhan vat cap 12
            int id4 = muaVuKhi(b, 85);
            if (id4 != KHONG) {
                b.gui(29, (short) id4);
                b.bom(1000);
                ket("trang_bi.cap_thap", b.timDo(d -> d[0] == id4) != null, "vu khi cap 31 van trong tui");
            } else ket("trang_bi.cap_thap", null, "khong mua duoc 85");
            // moi o trang bi khac: lay mau trong data_item (cap <= 12, clazz -1 hoac 0, mau thuong)
            int daMac = 0, thu = 0;
            StringBuilder sb = new StringBuilder();
            for (int type : new int[]{0, 1, 2, 8, 9, 10, 11, 12}) {
                String tpl = BotChucNang.sql1("SELECT id FROM data_item WHERE type=" + type + " AND level<=12 AND level>=1 AND (clazz=-1 OR clazz=0) AND price>0 AND gender IN (0,1) ORDER BY level DESC LIMIT 1");
                if (tpl == null) continue;
                thu++;
                int id = aitMon(b, Integer.parseInt(tpl), 5);
                if (id == KHONG) id = aitMon(b, Integer.parseInt(tpl), 0);
                if (id == KHONG) { sb.append("type").append(type).append(":ait loi "); continue; }
                final int idf = id;
                int truoc = b.soMac;
                b.gui(29, (short) id);
                b.bom(900);
                if (b.timDo(d -> d[0] == idf) == null) { daMac++; sb.append("type").append(type).append(":ok "); }
                else sb.append("type").append(type).append("(tpl ").append(tpl).append("):khong mac ");
            }
            ket("trang_bi.moi_o", thu > 0 && daMac == thu, daMac + "/" + thu + " o: " + sb + " so mac=" + b.soMac);
            ket("trang_bi.ket_noi", b.conSong(), BotChucNang.class.getSimpleName());
        }
    }

    // ------------------------------------------------------------------ nang cap +1..+15
    static int realIdNgoc(BotChucNang.Bot b, int tpl) {
        for (int[] g : b.ngoc.values()) if (g[0] == tpl && g[1] > 0 && g[2] == 0) return g[3];
        for (int[] g : b.ngoc.values()) if (g[0] == tpl && g[1] > 0) return g[3];
        return KHONG;
    }

    static String moNangCap(BotChucNang.Bot b, int id, boolean bua) throws IOException {
        b.gui(77, (byte) -1); // dong o nang cap cu (server giu mon + da sau moi lan thu o nhanh lau dai)
        b.bom(150);
        int m = b.moc();
        b.gui(71, (byte) 0);
        if (b.doiMenu(m, -505, -1, 2500) < 0) return "khong mo duoc o nang cap: " + b.chuTu(m);
        b.menu(-505, 0, 2); // ca da thuong lan da khoa
        b.cho(x -> x.coLenh(m, 71), 2000);
        b.gui(75, (short) id);
        int lkd = realIdNgoc(b, 8);
        if (lkd == KHONG) return "het da luyen kim";
        b.gui(76, (short) lkd, (byte) 0);
        if (bua) {
            int bh = realIdNgoc(b, 0);
            if (bh != KHONG) b.gui(76, (short) bh, (byte) 0);
        }
        b.bom(300);
        int m2 = b.moc();
        b.gui(77, (byte) 1);
        String t = b.doiChu(m2, 3000, "NANG CAP", "cần", "can ", "Thành công", "Thất bại");
        b.lamMoi(); // 77 chi gui 21 + 16/0: lay lai tui (16/1) de thay +N moi
        if (t != null) return t;
        final int idf = id;
        return "khong tra loi (map " + b.mapId + (KichBanDS2.laOffline(b.mapId) ? " nha" : "") + ", mon " + id
                + (b.timDo(x -> x[0] == idf) == null ? " KHONG trong tui" : " trong tui") + ", da " + lkd + ", goi tu luc mo: " + b.chuTu(m) + ")";
    }

    static void nangCap() throws Exception {
        try (BotChucNang.Bot b = botNha(30)) {
            int id = muaVuKhi(b, 79);
            if (id == KHONG) { ket("nang_cap", false, "khong mua duoc vu khi"); return; }
            b.gm("smn " + b.ten + " 50000000 0 20000", 800);
            b.gm("gem " + b.ten + " 8 300 0", 800);
            b.gm("gem " + b.ten + " 0 20 0", 800);
            // Bui sao (nguyen lieu +11..+15 bac so cap, 1 / lan thu): 25 tung het o +14 khi xui (cong kiem 31/10/2026: 40 lan,
            // 25 that bai) -> 45
            for (int i = 0; i < 45; i++) aitMon(b, 1034, 5);
            b.lamMoi();
            int plusMax = 0, lan = 0, tc = 0, tb = 0, rot = 0;
            boolean pityTang = false, sai = false;
            int lastRate = -1, lastK = -1;
            StringBuilder lg = new StringBuilder();
            String loiCuoi = null;
            while (lan < 220) {
                int[] d = b.timDo(x -> x[0] == id);
                if (d == null) { loiCuoi = "mon bien mat khoi tui"; break; }
                int plus = d[2];
                if (plus >= 15) break;
                boolean bua = plus >= 6; // tu +7 dung bua
                long xu0 = b.xu;
                int lk0 = b.lk;
                String t = moNangCap(b, id, bua);
                lan++;
                b.bom(400);
                int[] d2 = b.timDo(x -> x[0] == id);
                int plus2 = d2 == null ? -1 : d2[2];
                java.util.regex.Matcher mm = java.util.regex.Pattern.compile("([+]\\d+) \\(tỉ lệ (\\d+)%").matcher(t);
                int rate = mm.find() ? Integer.parseInt(mm.group(2)) : -1;
                if (t.contains("Thành công")) {
                    tc++;
                    if (plus2 != plus + 1) { sai = true; lg.append("[TC nhung plus ").append(plus).append("->").append(plus2).append("] "); }
                } else if (t.contains("Thất bại")) {
                    tb++;
                    if (plus2 < plus) rot++;
                    if (bua && plus2 < plus) { sai = true; lg.append("[co bua van rot +").append(plus).append("->").append(plus2).append("] "); }
                    if (!bua && plus >= 6 && plus2 >= plus) { /* +7 tro len khong bua phai rot */ }
                    if (lastK == plus + 1 && rate > lastRate && lastRate >= 0) pityTang = true;
                } else if (plus2 == plus + 1) {
                    tc++; // tin ket qua bi tin khac (vd "Danh hiệu mới: ... +15") chen truoc: mon da len cap = thanh cong
                } else { loiCuoi = t; break; }
                if (rate >= 0) { lastRate = rate; lastK = plus + 1; }
                if (t.contains("Thành công")) lastK = -1;
                plusMax = Math.max(plusMax, plus2);
                if (lan <= 3 || plus2 >= 12) lg.append(plus).append("->").append(plus2).append("(").append(rate).append("%) ");
            }
            ket("nang_cap.len_15", plusMax >= 15, "dat +" + plusMax + " sau " + lan + " lan (TC " + tc + ", TB " + tb + ")" + (loiCuoi != null ? " dung vi: " + loiCuoi : ""));
            ket("nang_cap.bua_bao_ho", !sai, sai ? lg.toString() : "co bua tu +7: khong rot cap; " + lg);
            ket("nang_cap.pity", tb == 0 || pityTang, "ti le tang sau that bai lien tiep: " + pityTang);
            // khong bua tu +7: lan that bai phai rot 1 cap (thu tren mon moi +6 bang may man) - TRU moc neo 0.9.8
            // (bo-ngoc-bat: da dat +7 / +10 / +13 thi that bai o dung moc khong rot duoi moc)
            int id2 = muaVuKhi(b, 79);
            int lanRot = 0, lanTb = 0, lanNeo = 0;
            boolean neoSai = false;
            for (int i = 0; i < 80 && id2 != KHONG; i++) {
                int[] d = b.timDo(x -> x[0] == id2);
                if (d == null) break;
                if (d[2] >= 10) break;
                String t = moNangCap(b, id2, false);
                b.bom(300);
                int[] dd = b.timDo(x -> x[0] == id2);
                if (t.contains("Thất bại") && d[2] >= 6) {
                    if (d[2] == 7 || d[2] == 10 || d[2] == 13) {
                        lanNeo++;
                        neoSai |= dd == null || dd[2] < d[2];
                    } else {
                        lanTb++;
                        if (dd != null && dd[2] == d[2] - 1) lanRot++;
                    }
                }
                if (!t.contains("Thành công") && !t.contains("Thất bại")) break;
            }
            ket("nang_cap.rot_khong_bua", lanTb == 0 && lanNeo == 0 ? null : lanRot == lanTb && !neoSai, "that bai tu +7 khong bua (ngoai moc neo): " + lanTb
                    + " lan, rot cap " + lanRot + " lan; that bai o moc neo +7/+10/+13: " + lanNeo + " lan, " + (neoSai ? "ROT duoi moc" : "giu moc"));
            // het xu: thong bao yeu cau, khong tru gi
            b.gm("smn " + b.ten + " " + (-b.xu) + " 0 0", 800);
            b.lamMoi();
            int nd = b.soNgoc(8);
            String t = id2 != KHONG ? moNangCap(b, id2, false) : "";
            b.lamMoi();
            ket("nang_cap.thieu_xu", t.contains("cần") && b.soNgoc(8) == nd, t + " | da " + nd + " -> " + b.soNgoc(8));
        }
    }

    // ------------------------------------------------------------------ ngoc
    static void ngoc() throws Exception {
        try (BotChucNang.Bot b = botNha(30)) {
            b.gm("smn " + b.ten + " 20000000 0 0", 800);
            b.lamMoi();
            long xu0 = b.xu;
            int n0 = b.soNgoc(5);
            b.gui(74, (short) 2, (short) 5, (short) 5); // mua 2 da may man 1
            b.bom(1200);
            b.lamMoi();
            ket("ngoc.mua", b.soNgoc(5) == n0 + 2 && b.xu < xu0, "da may man 1: " + n0 + " -> " + b.soNgoc(5) + ", xu " + xu0 + " -> " + b.xu);
            int rid = realIdNgoc(b, 5);
            long xu1 = b.xu;
            if (rid != KHONG) {
                int khoa = 0;
                for (int[] g : b.ngoc.values()) if (g[3] == rid) khoa = g[2];
                b.gui(78, (short) rid, (byte) khoa);
                b.bom(1000);
                b.lamMoi();
                ket("ngoc.ban", b.soNgoc(5) == 0 && b.xu > xu1, "ban ca chong: con " + b.soNgoc(5) + ", xu " + xu1 + " -> " + b.xu);
            } else ket("ngoc.ban", false, "khong thay realId da 5");
            // ghep 5 ngoc kham cap 1 (12) -> cap 2 (16) ti le 100%
            b.gm("gem " + b.ten + " 60 5 0", 800); // mui duc 1 (ep=1, GemTemplate) -> mui duc 2
            b.lamMoi();
            int g12 = b.soNgoc(60), g16 = b.soNgoc(61);
            int r12 = realIdNgoc(b, 60);
            int m = b.moc();
            b.gui(-36, (byte) 0);
            b.bom(500);
            b.gui(-36, (byte) 1, (byte) 5, (short) r12, (short) r12, (short) r12, (short) r12, (short) r12);
            b.bom(1500);
            b.lamMoi();
            ket("ngoc.ghep", b.soNgoc(61) == g16 + 1 && b.soNgoc(60) == g12 - 5, "ngoc 60: " + g12 + " -> " + b.soNgoc(60) + ", ngoc 61: " + g16 + " -> " + b.soNgoc(61) + " " + b.chuTu(m));
            // kham: duc lo + kham ngoc vao vu khi
            int id = muaVuKhi(b, 79);
            b.gm("gem " + b.ten + " 60 1 0", 600);
            b.gm("gem " + b.ten + " 29 1 0", 600);
            b.lamMoi();
            m = b.moc();
            b.gui(-34, (byte) 0);
            if (b.doiMenu(m, -506, -1, 2500) >= 0) {
                b.menu(-506, 0, 0);
                b.bom(600);
                b.gui(75, (short) id);
                b.gui(76, (short) realIdNgoc(b, 60), (byte) 0);
                b.bom(300);
                int m2 = b.moc();
                b.gui(-35, (byte) 1);
                String t = b.doiChu(m2, 3000, "KHAM", "");
                ket("ngoc.duc_lo", t != null && !t.toLowerCase().contains("không"), t);
                m = b.moc();
                b.gui(-34, (byte) 0);
                b.doiMenu(m, -506, -1, 2500);
                b.menu(-506, 0, 0);
                b.bom(600);
                b.gui(76, (short) realIdNgoc(b, 29), (byte) 0); // mon van dang chon tu lan duc lo (75 lan 2 = bo chon)
                b.bom(300);
                m2 = b.moc();
                b.gui(-35, (byte) 1);
                t = b.doiChu(m2, 3000, "KHAM", "");
                if (t != null && t.contains("Không tìm thấy")) {
                    b.gui(75, (short) id);
                    b.gui(76, (short) realIdNgoc(b, 29), (byte) 0);
                    b.bom(300);
                    m2 = b.moc();
                    b.gui(-35, (byte) 1);
                    t = b.doiChu(m2, 3000, "KHAM", "");
                }
                ket("ngoc.kham", t != null && !t.toLowerCase().contains("không"), t);
            } else ket("ngoc.duc_lo", false, "khong mo menu -506: " + b.chuTu(m));
        }
    }

    // ------------------------------------------------------------------ phan ra / nghien
    static void phanRa() throws Exception {
        try (BotChucNang.Bot b = botNha(20)) {
            int id = muaVuKhi(b, 79);
            int n0 = b.ngoc.size();
            int m = b.moc();
            b.gui(-68, (byte) 4, (short) id);
            b.bom(1500);
            b.lamMoi();
            boolean mat = b.timDo(d -> d[0] == id) == null;
            ket("phan_ra.do_shop", !mat, "do mua shop (trang, khong roi tu quai) khong duoc nghien: con trong tui=" + !mat + " " + b.chuTu(m));
            // do roi tu quai: ha quai lay do
            b.den(2, 20, 20);
            List<Integer> ds = KichBanDS.timQuai(b, 0, 200);
            int doTruoc = b.doDung.size();
            for (int i = 0; i < Math.min(15, ds.size()); i++) {
                int q = ds.get(i);
                KichBanDS.haQuai(b, q, 0, 10);
            }
            b.lamMoi();
            ket("phan_ra.roi_do", true, "sau 15 quai: tui " + doTruoc + " -> " + b.doDung.size() + " mon (chi ghi nhan)");
            ket("phan_ra.ket_noi", b.conSong(), "");
        }
    }

    // ------------------------------------------------------------------ giao dich (chi thuoc)
    static void giaoDich() throws Exception {
        try (BotChucNang.Bot a = botLang(20); BotChucNang.Bot c = KichBanDS4.botCungQuoc(20, a)) {
            a.gm("spt " + a.ten + " 1 30", 600);
            a.lamMoi();
            c.lamMoi();
            // dua ve cung map + cung cho
            a.den(2, 20, 20);
            c.den(2, 21, 20);
            a.bom(500);
            c.bom(500);
            int a0 = a.soThuoc(1), c0 = c.soThuoc(1);
            int m = c.moc();
            a.gui(66, (byte) 0, (short) c.actor);
            boolean moi = c.cho(x -> x.coLenh(m, 66), 3000);
            c.gui(66, (byte) 1, (short) a.actor);
            a.bom(600);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 10);
            a.bom(600);
            c.bom(300);
            a.gui(66, (byte) 4, (short) 0);
            c.gui(66, (byte) 4, (short) 0);
            a.bom(600);
            c.bom(600);
            a.gui(66, (byte) 5);
            c.gui(66, (byte) 5);
            a.bom(1200);
            c.bom(1200);
            a.lamMoi();
            c.lamMoi();
            ket("giao_dich.loi_moi", moi, c.chuTu(m) + " | A: " + a.chuTu(0).replaceAll(".*\\[12\\]", ""));
            ket("giao_dich.thuoc", a.soThuoc(1) == a0 - 10 && c.soThuoc(1) == c0 + 10, "A thuoc1 " + a0 + " -> " + a.soThuoc(1) + ", B " + c0 + " -> " + c.soThuoc(1));
            // huy giua chung (server: 1 loi moi / 60 giay)
            a.bom(61000);
            c.bom(100);
            int a1 = a.soThuoc(1);
            a.gui(66, (byte) 0, (short) c.actor);
            c.bom(500);
            c.gui(66, (byte) 1, (short) a.actor);
            a.bom(500);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 5);
            a.bom(300);
            c.gui(66, (byte) 3, (short) a.actor);
            a.bom(800);
            a.lamMoi();
            ket("giao_dich.huy", a.soThuoc(1) == a1, "huy giua chung: A con " + a.soThuoc(1) + " (truoc " + a1 + ")");
            ket("giao_dich.ket_noi", a.conSong() && c.conSong(), "");
        }
    }

    // ------------------------------------------------------------------ cho (gian hang NPC 11..20)
    static void cho() throws Exception {
        try (BotChucNang.Bot a = botNha(20); BotChucNang.Bot c = KichBanDS4.botCungQuoc(20, a)) {
            KichBanDS2.vaoNha(c, 103);
            int mMua = a.moc();
            int id = muaVuKhi(a, 80);
            if (id == KHONG) { ket("cho", false, a.user + " khong mua duoc do (map " + a.mapId + ", tui " + a.doDung.size() + ", xu " + a.xu + ") " + a.chuTu(mMua)); return; }
            int npc = 11, shop = 0, m = a.moc();
            for (npc = 11; npc <= 20; npc++) {
                m = a.moc();
                final int m92 = m;
                a.gui(92, true, (byte) npc, (byte) shop, (short) id, 10000, (byte) 0);
                a.cho(x -> x.layLenh(m92, -31) != null || x.coLenh(m92, 92), 3000);
                if (a.layLenh(m, -31) != null) {
                    a.gui(-31, (short) -32047, (byte) 0, "");
                    a.bom(1000);
                }
                if (a.coLenh(m, 92)) break;
                if (a.doiChu(m, 0, "chỉ bán") == null) break;
            }
            boolean daBan = a.coLenh(m, 92);
            ket("cho.dang_ban", daBan, a.chuTu(m));
            if (!daBan) return;
            long xuA = a.xu, xuC = c.xu;
            m = c.moc();
            c.gui(94, (byte) 0, (byte) npc, (byte) shop);
            c.bom(800);
            c.gui(94, (byte) 1, (byte) npc, (byte) shop, (short) a.actor);
            c.bom(800);
            int m2 = c.moc();
            c.gui(95, (byte) npc, (byte) shop, (short) a.actor, (short) id, (byte) 0);
            c.cho(x -> x.layLenh(m2, -32) != null, 3000);
            BotChucNang.SuKien p = c.layLenh(m2, -32);
            if (p != null) {
                c.gui(-32, (short) p.so[0], (byte) p.so[1], "", (byte) 1);
                c.bom(1500);
            }
            a.bom(500);
            a.lamMoi();
            c.lamMoi();
            boolean coDo = c.timDo(d -> d[1] == 80) != null;
            ket("cho.mua", coDo && c.xu < xuC, "B co do=" + coDo + " xu B " + xuC + " -> " + c.xu + ", A " + xuA + " -> " + a.xu + " " + c.chuTu(m));
            ket("cho.thue", a.xu > xuA && a.xu - xuA < 10000, "A nhan " + (a.xu - xuA) + "/10000 (sau thue)");
        }
    }

    // ------------------------------------------------------------------ thu cuoi / vat nuoi
    static void thuCuoi() throws Exception {
        BotChucNang.Bot b0 = botLang(30);
        String u = b0.user;
        int nDo = b0.doDung.size();
        b0.gm("ngua 1", 1500);
        b0.gm("poro 1", 1500);
        b0.lamMoi();
        String doMoi = "tui " + nDo + " -> " + b0.doDung.size();
        b0.close();
        Thread.sleep(3000);
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        try {
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0);
            if (l != null) { ket("thu_cuoi", false, l); return; }
            if (b.thu.isEmpty()) {
                ket("thu_cuoi.co_thu", false, "GM 'ngua 1'/'poro 1' khong tao thu (16/2 rong sau dang nhap lai), " + doMoi);
                return;
            }
            ket("thu_cuoi.co_thu", true, b.thu.size() + " thu: " + b.tenThu.values());
            int idThu = b.thu.get(0)[0];
            int m = b.moc();
            b.gui(-45, (byte) 0, (short) idThu);
            b.bom(800);
            BotChucNang.SuKien mn = null;
            for (int i = m; i < b.su.size(); i++) if (b.su.get(i).cmd == -30 && b.su.get(i).so[0] == -800) mn = b.su.get(i);
            if (mn != null) { b.menu(-800, mn.so[1], 0); b.bom(800); }
            boolean cuoi = b.thu.stream().noneMatch(t -> t[0] == idThu) || b.coLenh(m, 3);
            ket("thu_cuoi.cuoi", cuoi, "thu " + idThu + " (" + b.tenThu.get(idThu) + ") " + b.chuTu(m));
            m = b.moc();
            b.gui(108, (byte) 0);
            b.bom(800);
            ket("thu_cuoi.xuong", b.conSong(), b.chuTu(m));
        } finally {
            b.close();
        }
    }
}
