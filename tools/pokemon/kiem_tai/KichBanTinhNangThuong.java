import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TINH NANG NGUOI CHOI THUONG (29/10/2026): cac tinh nang ma truoc day bot chi kiem bang GM (duoc mien luat) hoac chua kiem,
 * nay do NGUOI CHOI THUONG (tai khoan kt101.., KHONG GM, nhan vat moi moi lan) tu choi bang goi tin nhu client. GM (kt0xx)
 * CHI dung canh bang lenh nham ten / nham quai (smn, spt, hoangda, mc) - moi hanh dong duoc kiem deu do nguoi choi gui.
 *
 * Moi case kiem bang TRANG THAI (goi tin + CSDL sau khi thoat), khong chi chu thong bao: xu / bong / so con / Pokedex khong
 * nhan doi, khong mat, khong ket; dang nhap lai van giu; may chu con tra loi; khong Exception moi trong log (-Dlog).
 *
 *   java -cp <bot> -Ddb=htbb_db -Ddbport=3610 -Dlog=<game.log> -Dban=915 -Dtu=1 -Dden=40 -DtuThuong=101 -DdenThuong=199 \
 *        KichBanTinhNangThuong 127.0.0.1 18611 kt [tn_...]
 */
public final class KichBanTinhNangThuong {

    private KichBanTinhNangThuong() {
    }

    static void ket(String ten, Boolean ok, String bang) {
        BotChucNang.ket(ten, ok, bang);
    }

    static void lenhGm(String lenh) throws Exception {
        KichBanBatThuong.lenhGm(lenh);
    }

    static BotChucNang.Bot gm() throws Exception {
        return KichBanBatThuong.gm();
    }

    static void onDinh(BotChucNang.Bot b) throws IOException {
        KichBanBatThuong.onDinh(b);
    }

    // ================================================================== -121 (Bat Pokemon / Hop PC)
    /** Goi -121 kieu {@code kieu} moi nhat sau moc. */
    static byte[] goi121(BotChucNang.Bot b, int tu, int kieu) {
        return KichBanThuPhuc.goi121(b, tu, kieu);
    }

    /** Trang thai bat (-121 kieu 0 -> 0): {bong thu tu 1, batNgay, soCon, sucChua}; null = khong co. */
    static int[] trangThai(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(-121, (byte) 0, (byte) 1);
        b.cho(x -> goi121(x, m, 0) != null, 4000);
        byte[] d = goi121(b, m, 0);
        if (d == null) return null;
        DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
        r.readByte();
        r.readByte();
        int n = r.readByte();
        int bong1 = -1;
        for (int i = 0; i < n; i++) {
            int tt = r.readByte();
            r.readUTF();
            int so = r.readShort();
            r.readShort();
            if (tt == 1) bong1 = so;
        }
        int batNgay = r.readShort();
        r.readShort();
        int soCon = r.readShort();
        int suc = r.readShort();
        return new int[]{bong1, batNgay, soCon, suc};
    }

    /** Hop PC CSDL (tob_hop_pc.du_lieu): so con (";c<id>="), so Poke Ball (";b_<ma>=" dau tien), keo. */
    static String hopCsdl(BotChucNang.Bot b) {
        return BotChucNang.sql1("SELECT du_lieu FROM tob_hop_pc WHERE id_char = " + b.charDb);
    }

    static int demCon(String du) {
        if (du == null) return -1;
        Matcher m = Pattern.compile(";c\\d+=").matcher(du);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    static int soTrong(String du, String khoa) {
        if (du == null) return -1;
        Matcher m = Pattern.compile(";" + Pattern.quote(khoa) + "=(\\d+)").matcher(du);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    static long soBatPokedex(BotChucNang.Bot b) {
        String v = BotChucNang.sql1("SELECT COALESCE(SUM(so_bat),0) FROM tob_pokedex WHERE id_char=" + b.charDb);
        return v == null || !v.trim().matches("\\d+") ? -1 : Long.parseLong(v.trim());
    }

    /** Mo Chansey (NPC 0) -> "Poké Ball & Hộp PC" -> menu bong (-581). Tra chu muc mua bong dau tien, null = khong mo duoc. */
    static String moMenuBong(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.gui(23, (byte) 0);
        int n = b.doiMenu(m, 0, -1, 4000);
        int i = b.timMuc("Hộp PC");
        if (n <= 0 || i < 0) return null;
        int m2 = b.moc();
        b.menu(b.menuCuoi[0], b.menuCuoi[1], i);
        if (b.doiMenu(m2, -581, 0, 4000) <= 0) return null;
        return b.menuChu.isEmpty() ? null : b.menuChu.get(0);
    }

    static long giaTrongChu(String chu) {
        Matcher m = Pattern.compile("\\(([0-9.,]+)\\s*(k|K|xu)?").matcher(chu == null ? "" : chu);
        if (!m.find()) return -1;
        String so = m.group(1).replace(".", "").replace(",", "");
        try {
            long v = Long.parseLong(so);
            return "k".equalsIgnoreCase(m.group(2)) ? v * 1000 : v;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ================================================================== di bo ra ban do co quai
    /** Nguoi choi thuong di bo tu nha / lang ra mot ban do ngoai co quai song (khong mto). */
    static List<Integer> raBanDoCoQuai(BotChucNang.Bot b) throws IOException {
        java.util.Set<Integer> daThu = new java.util.HashSet<>();
        for (int lan = 0; lan < 6; lan++) {
            if (KichBanDS2.laOffline(b.mapId)) {
                if (!KichBanDS2.raNgoai(b)) break;
                onDinh(b);
                continue;
            }
            if (!KichBanDS.laLang(b.mapId) && b.mapId != 106) {
                List<Integer> ds = KichBanDS.timQuai(b, 0, 250);
                if (!ds.isEmpty()) return ds;
            }
            daThu.add(b.mapId);
            if (!quaCongKhac(b, daThu)) break;
            onDinh(b);
        }
        return new ArrayList<>();
    }

    /** Di bo qua mot cong ngoai troi toi map chua thu (khong phai ben tau 106) - nguoi choi thuong, khong mto. */
    static boolean quaCongKhac(BotChucNang.Bot b, java.util.Set<Integer> daThu) throws IOException {
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        if (bd == null || b.viTriMap == null) return false;
        int nhom = KichBanDS2.nhomCua(b, b.mapId);
        if (nhom < 0) return false;
        int mapTruoc = b.mapId;
        java.util.Set<Integer> daXet = new java.util.HashSet<>();
        for (int[] g : bd.cong) {
            if (KichBanDS2.laOffline(g[2]) || g[2] >= b.viTriMap[nhom].length) continue;
            short[] loc = b.viTriMap[nhom][g[2]];
            if (loc[0] < 0 || loc[0] == 106 || daThu.contains((int) loc[0]) || !daXet.add((int) loc[0])) continue;
            int[] dung = bd.oTruocCong(g[0], g[1]);
            if (dung == null) continue;
            b.px = b.px <= 0 ? b.tx * 16 + 8 : b.px;
            if (!b.diDuong(bd, dung[0], dung[1])) continue;
            int m = b.moc();
            b.gui(12, loc[0], loc[1], loc[2]);
            b.cho(x -> x.coLenh(m, 12) || x.dut, 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(800);
            if (b.mapId != mapTruoc) {
                b.px = b.tx * 16 + 8;
                b.py = b.ty * 16 + 8;
                return true;
            }
        }
        return false;
    }

    /** Di bo toi gan quai id (trong 3 o); true = toi duoc. */
    static boolean toiGanQuai(BotChucNang.Bot b, int id) throws IOException {
        int[] q = b.quai.get(id);
        if (q == null) return false;
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        if (bd == null) return false;
        int[] o = bd.oGan(q[0] / 16, q[1] / 16, 3);
        if (o == null) return false;
        b.px = b.px <= 0 ? b.tx * 16 + 8 : b.px;
        return b.diDuong(bd, o[0], o[1]) || Math.abs(b.px - q[0]) <= 140 && Math.abs(b.py - q[1]) <= 140;
    }

    /** Danh quai id cho yeu (HP <= 35%) bang chieu thuong roi nem bong 1 toi khi bat / bo chay. Tra {kq, so lan nem}. */
    /**
     * Danh cho yeu (<= 35% HP) roi nem toi khi bat / bo chay / bi tu choi. {ket qua, so bong da TRU}: ket qua 1 bat, 2 bo
     * chay, 0 het luot, 3 tu choi, -1 quai chet / mat, -2 may chu khong tra loi.
     *
     * <p>Cong kiem 31/10/2026 (can bang class: don thuong manh hon): truoc day sau MOI lan truot bot danh them mot don "giu
     * giao chien" -> ha chet Pidgey 35% HP ("Không thấy Pokémon đó") va dem MOI lan gui la mot bong. Nay nhu client that:
     * giao chien (BatPokemonGame.khiDanh) giu 30 giay tu don trung cuoi - chi danh lai khi da qua 20 giay; chi DEM bong khi
     * may chu tra -121 ket qua 0/1/2 (truoc truBong moi duong bo qua deu KHONG tra loi va KHONG tru bong: nhip nem 1,5 giay,
     * BoNgocGame.coClient); khong tra loi -> cho het nhip nem roi nem lai (toi da 2 lan).
     */
    static int[] danhVaNem(BotChucNang.Bot b, int id, StringBuilder ghi) throws IOException {
        int[] q;
        long danhCuoi = 0;
        for (int lan = 0; lan < 60; lan++) {
            b.gui(7, (short) id);
            b.bom(200);
            q = b.quai.get(id);
            if (q == null || q[2] == 0) return new int[]{-1, 0};
            if (q[2] > 0 && q[4] > 0 && q[2] * 100 <= q[4] * 35) break;
            b.gui(9, (short) id, (byte) 0);
            danhCuoi = System.currentTimeMillis();
            b.bom(450);
        }
        q = b.quai.get(id);
        if (q == null || q[2] <= 0) return new int[]{-1, 0};
        int nem = 0, khongTraLoi = 0;
        for (int k = 0; k < 8; k++) {
            if (danhCuoi > 0 && System.currentTimeMillis() - danhCuoi > 20000L) {
                b.gui(9, (short) id, (byte) 0); // giao chien sap het (30 giay): danh lai mot don nhu nguoi choi
                danhCuoi = System.currentTimeMillis();
                b.bom(400);
                q = b.quai.get(id);
                if (q == null || q[2] <= 0) { ghi.append("[quai chet khi danh giu giao chien] "); return new int[]{-1, nem}; }
            }
            int m = b.moc();
            b.gui(-121, (byte) 1, (short) id, (byte) 1);
            b.cho(x -> goi121(x, m, 1) != null, 3000);
            byte[] d = goi121(b, m, 1);
            if (d == null) {
                ghi.append("[nem khong ket qua (khong tru bong): ").append(b.chuTu(m)).append("] ");
                if (++khongTraLoi > 2) return new int[]{-2, nem};
                b.bom(1700); // nhip nem
                continue;
            }
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            r.readByte();
            r.readShort();
            r.readShort();
            r.readByte();
            int kq = r.readByte();
            r.readByte();
            int dex = r.readShort();
            r.readByte();
            int tl = r.readShort();
            String chu = r.readUTF();
            ghi.append("[").append(dex).append(' ').append(tl).append("‰ ").append(chu).append("] ");
            if (kq == 3) return new int[]{3, nem}; // tu choi: khong tru bong
            nem++; // 0 truot / 1 bat / 2 bo chay: da tru mot bong
            if (kq == 1 || kq == 2) return new int[]{kq, nem};
            if (nem >= 6) break;
            b.bom(1700); // nhip nem 1,5 giay
        }
        return new int[]{0, nem};
    }

    // ================================================================== 1. bat Pokemon dau-cuoi
    static void batPokemon() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot p = KichBanBatThuong.nguoiTrongNha();
        try {
            // --- mua bong o Chansey: tru DUNG gia ghi tren menu, +5 bong
            int[] t0 = trangThai(p);
            ket("tn_bat.trang_thai", t0 != null, t0 == null ? "khong co goi -121/0" : "bong " + t0[0] + ", hop " + t0[2] + "/" + t0[3]);
            if (t0 == null) return;
            KichBanBatThuong.datXu(p, 20000);
            String muc = moMenuBong(p);
            long gia = giaTrongChu(muc);
            if (muc == null || gia <= 0) { ket("tn_bat.mua_bong", false, "khong mo / doc duoc menu bong: " + muc + " " + p.chuTu(0)); return; }
            long xu0 = p.xu;
            int mb = p.moc();
            p.menu(-581, 0, 0);
            p.doiChu(mb, 4000, "Mua ");
            onDinh(p);
            int[] t1 = trangThai(p);
            ket("tn_bat.mua_bong", t1 != null && t1[0] == t0[0] + 5 && xu0 - p.xu == gia,
                    "menu \"" + muc + "\": xu " + xu0 + " -> " + p.xu + " (gia " + gia + "), bong " + t0[0] + " -> " + (t1 == null ? "?" : t1[0]));
            int bong = t1 == null ? t0[0] : t1[0];
            // bam mua 2 lan lien (bam dup): tru dung 2 lan, +10
            long xu1 = p.xu;
            p.menu(-581, 0, 0);
            p.menu(-581, 0, 0);
            p.bom(800);
            onDinh(p);
            int[] t2 = trangThai(p);
            int bongSau = t2 == null ? -1 : t2[0];
            ket("tn_bat.mua_dup", t2 != null && (xu1 - p.xu) == gia * ((bongSau - bong) / 5) && (bongSau - bong) % 5 == 0 && bongSau > bong,
                    "2 goi mua: xu -" + (xu1 - p.xu) + ", bong " + bong + " -> " + bongSau);
            bong = bongSau;
            // --- di bo ra ban do co quai
            List<Integer> ds = raBanDoCoQuai(p);
            if (ds.isEmpty()) { ket("tn_bat.ra_dong", false, "khong ra duoc ban do co quai, dang o map " + p.mapId); return; }
            ket("tn_bat.ra_dong", true, "map " + p.mapId + " (" + p.tenMap + "), " + ds.size() + " quai");
            // quai THUONG: tu choi, khong tru bong
            Integer thuong = null;
            for (int id : ds) if (!p.hoangDa.containsKey(id)) { thuong = id; break; }
            if (thuong != null && toiGanQuai(p, thuong)) {
                int m = p.moc();
                p.gui(-121, (byte) 1, (short) (int) thuong, (byte) 1);
                String c = p.doiChu(m, 3000, "thuần hoá", "giao chiến", "quá xa");
                int[] tt = trangThai(p);
                ket("tn_bat.quai_thuong", c != null && tt != null && tt[0] == bong, "tra loi: " + c + ", bong " + bong + " -> " + (tt == null ? "?" : tt[0]));
                p.bom(1600);
            }
            // --- GM chi DUNG CANH: toi canh nguoi choi (mc), bien quai gan thanh Pokemon hoang da
            BotChucNang.Bot g = gm();
            g.gm("mc " + p.ten, 1500);
            g.gui(12, (short) -500, (short) 0, (short) 0);
            g.bom(800);
            StringBuilder ghi = new StringBuilder();
            int[] kq = {-1, 0};
            int daNem = 0;
            long dex0 = soBatPokedex(p);
            ds = KichBanDS.timQuai(p, 0, 250);
            for (int i = 0; i < ds.size() && i < 8 && kq[0] != 1; i++) {
                int id = ds.get(i);
                if (!p.hoangDa.containsKey(id)) {
                    g.gm("hoangda " + id + " 16 1", 900);
                    p.cho(x -> x.hoangDa.containsKey(id), 3000);
                    if (!p.hoangDa.containsKey(id)) { ghi.append("[khong bien ").append(id).append("] "); continue; }
                }
                if (!toiGanQuai(p, id)) { ghi.append("[khong toi gan ").append(id).append("] "); continue; }
                kq = danhVaNem(p, id, ghi);
                daNem += kq[1];
            }
            ket("tn_bat.bat", kq[0] == 1, ghi.toString());
            int[] t3 = trangThai(p);
            ket("tn_bat.bong_tru_dung", t3 != null && t3[0] == bong - daNem, "bong " + bong + " - " + daNem + " lan nem = " + (t3 == null ? "?" : t3[0]));
            if (kq[0] != 1) return;
            ket("tn_bat.hop_pc", t3 != null && t3[2] == t0[2] + 1, "Hop PC " + t0[2] + " -> " + (t3 == null ? "?" : t3[2]));
            // Pokedex "Da bat" (goi -121 kieu 2) co loai 16
            int m = p.moc();
            p.gui(-121, (byte) 2);
            p.cho(x -> goi121(x, m, 2) != null, 3000);
            byte[] dx = goi121(p, m, 2);
            int daBat = -1;
            boolean co16 = false;
            if (dx != null) {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(dx));
                r.readByte();
                int n = r.readShort();
                for (int i = 0; i < n; i++) {
                    int dex = r.readShort();
                    int tt = r.readByte();
                    if (dex == 16 && tt >= 2) co16 = true;
                }
                daBat = r.readShort();
            }
            ket("tn_bat.pokedex_da_bat", co16 && daBat >= 1, "goi Pokedex: Pidgey trang thai bat=" + co16 + ", so loai da bat " + daBat);
            // --- thoat, CSDL, vao lai: giu nguyen
            int bongCuoi = t3[0], conCuoi = t3[2];
            BotChucNang.Bot p2 = KichBanBatThuong.vaoLai(p);
            p = p2;
            String du = hopCsdl(p);
            long dex1 = soBatPokedex(p);
            ket("tn_bat.csdl", demCon(du) == conCuoi && soTrong(du, "b_poke") == bongCuoi && dex1 == dex0 + 1,
                    "tob_hop_pc: " + demCon(du) + " con, b_poke=" + soTrong(du, "b_poke") + " (mong " + conCuoi + " / " + bongCuoi + "); tob_pokedex.so_bat " + dex0 + " -> " + dex1
                            + " | " + (du == null ? "null" : du.length() > 160 ? du.substring(0, 160) : du));
            int[] t4 = trangThai(p);
            ket("tn_bat.vao_lai", t4 != null && t4[0] == bongCuoi && t4[2] == conCuoi, "sau dang nhap lai: bong " + (t4 == null ? "?" : t4[0]) + ", hop " + (t4 == null ? "?" : t4[2]));
            // --- Hop PC: theo, tha (ve tu nhien +keo), id khong co
            List<int[]> hop = KichBanThuPhuc.hop(p);
            if (hop != null && !hop.isEmpty()) {
                int idCon = hop.get(0)[0];
                int mt = p.moc();
                p.gui(-121, (byte) 4, (byte) 0, idCon);
                String c = p.doiChu(mt, 3000, "đi theo");
                ket("tn_bat.theo", c != null, c == null ? p.chuTu(mt) : c);
                String du0 = hopCsdl(p);
                int mt2 = p.moc();
                p.gui(-121, (byte) 4, (byte) 3, idCon);
                String c2 = p.doiChu(mt2, 3000, "Đã thả");
                p.bom(500);
                int mt3 = p.moc();
                p.gui(-121, (byte) 4, (byte) 3, idCon); // tha lan 2 cung id: khong con
                String c3 = p.doiChu(mt3, 3000, "không còn");
                int[] t5 = trangThai(p);
                ket("tn_bat.tha", c2 != null && c3 != null && t5 != null && t5[2] == conCuoi - 1,
                        "tha: " + c2 + " | tha lai: " + c3 + " | hop " + conCuoi + " -> " + (t5 == null ? "?" : t5[2]));
                p = KichBanBatThuong.vaoLai(p);
                String du1 = hopCsdl(p);
                ket("tn_bat.tha_csdl", demCon(du1) == conCuoi - 1 && soTrong(du1, "dt") == 0,
                        "CSDL sau tha: " + demCon(du1) + " con, dang theo " + soTrong(du1, "dt") + " (truoc " + demCon(du0) + ")");
            }
        } finally {
            p.close();
        }
        ket("tn_bat.may_chu", KichBanBatThuong.mayChuSong(), "nguoi choi khac van vao game + lam moi");
        KichBanBatThuong.kiemLog("tn_bat");
    }

    // ================================================================== 2. nha tre & trung
    static final int NPC_HST = -528;

    /** Mo menu he sinh thai (chat "hst" nhu nut client) roi chon muc goc chua {@code muc}; tra so muc menu moi, -1 = loi. */
    static int moHst(BotChucNang.Bot b, String muc, int idMenu) throws IOException {
        int m = b.moc();
        b.chat("hst");
        if (b.doiMenu(m, NPC_HST, 0, 4000) <= 0) return -1;
        int i = b.timMuc(muc);
        if (i < 0) return -1;
        int m2 = b.moc();
        b.menu(NPC_HST, 0, i);
        return b.doiMenu(m2, NPC_HST, idMenu, 4000);
    }

    /** Chon muc op o menu he sinh thai idMenu, doi menu idMoi (-1 = khong doi menu, chi doi chu thong bao). */
    static String chonHst(BotChucNang.Bot b, int idMenu, int op, int idMoi, String... chu) throws IOException {
        int m = b.moc();
        b.menu(NPC_HST, idMenu, op);
        if (idMoi >= 0) return b.doiMenu(m, NPC_HST, idMoi, 4000) > 0 ? String.join(" ; ", b.menuChu) : null;
        return b.doiChu(m, 4000, chu);
    }

    static String hstCsdl(BotChucNang.Bot b) {
        return BotChucNang.sql1("SELECT du_lieu FROM tob_hst_nhan_vat WHERE id_char = " + b.charDb);
    }

    /** Truong "nt=" cua ho so: {nhaTre1, nhaTre2, buoc, deHomNay, ngayPhi, demKhongShiny}; null = chua co. */
    static long[] nhaTreCsdl(String du) {
        if (du == null) return null;
        Matcher m = Pattern.compile("(?:^|[;|&\\n])nt[=:]([-0-9,]+)").matcher(du);
        if (!m.find()) return null;
        String[] f = m.group(1).split(",");
        long[] r = new long[6];
        for (int i = 0; i < 6 && i < f.length; i++) r[i] = Long.parseLong(f[i].trim());
        return r;
    }

    /** Thoat, doi may chu ghi + bo ho so sinh thai khoi bo nho, sua CSDL (dung canh), dang nhap lai. */
    static BotChucNang.Bot suaHoSoRoiVao(BotChucNang.Bot b, java.util.function.UnaryOperator<String> sua) throws Exception {
        String u = b.user;
        int id = b.charDb;
        b.close();
        // luuTatCa (nhip 15 giay) ghi roi bo ho so nguoi da thoat; doi du lieu dung yen 2 nhip
        String truoc = null;
        for (int i = 0; i < 12; i++) {
            Thread.sleep(5000);
            String nay = BotChucNang.sql1("SELECT MD5(du_lieu) FROM tob_hst_nhan_vat WHERE id_char = " + id);
            if (nay != null && nay.equals(truoc) && i >= 5) break;
            truoc = nay;
        }
        String du = BotChucNang.sql1("SELECT du_lieu FROM tob_hst_nhan_vat WHERE id_char = " + id);
        if (du != null) {
            String moi = sua.apply(du);
            BotChucNang.sql("UPDATE tob_hst_nhan_vat SET du_lieu = '" + moi.replace("\\", "\\\\").replace("'", "''") + "' WHERE id_char = " + id);
        }
        BotChucNang.Bot c = new BotChucNang.Bot(u);
        String l = c.dangNhap("123456");
        if (l == null) l = KichBanBatThuong.vaoGameNhanh(c);
        if (l != null) throw new IllegalStateException(u + ": " + l);
        onDinh(c);
        return c;
    }

    /** Ha mot quai (nguoi choi danh bang chieu thuong); true = quai chet. */
    static boolean haQuai(BotChucNang.Bot b) throws IOException {
        List<Integer> ds = KichBanDS.timQuai(b, 0, 250);
        for (int i = 0; i < ds.size() && i < 6; i++) {
            int id = ds.get(i);
            if (!toiGanQuai(b, id)) continue;
            for (int lan = 0; lan < 80; lan++) {
                b.gui(9, (short) id, (byte) 0);
                b.bom(450);
                b.gui(7, (short) id);
                b.bom(150);
                int[] q = b.quai.get(id);
                if (q == null || q[2] == 0) return true;
            }
        }
        return false;
    }

    static int dem(BotChucNang.Bot b, int tpl) {
        int n = 0;
        n += b.soDo(tpl); // cong so luong chong type 28 (ChongVatPham), khong dem o
        return n;
    }

    static void nhaTre() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot p = KichBanBatThuong.nguoi();
        try {
            // bao loi 30/10/2026 (J2ME "đây là khi ấn trả phí hôm nay"): nguoi choi CHUA CO pet bam Tra phi chi thay
            // "Chưa gửi pet nào." - nay chu noi pet dong hanh la gi + cach co, khong tru xu.
            int n0 = moHst(p, "Nhà trẻ", 6);
            String nhanK = n0 > 0 ? String.join(" ; ", p.menuChu) : p.chuTu(0);
            ket("tn_nha_tre.khong_pet_nhan", n0 > 0 && nhanK.contains("gửi pet trước") && nhanK.contains("Nhận pet đồng hành đầu tiên"), nhanK);
            if (n0 > 0) {
                long xuK = p.xu;
                String k1 = chonHst(p, 6, 3, -1, "pet đồng hành");
                onDinh(p);
                ket("tn_nha_tre.khong_pet_tra_phi", k1 != null && k1.contains("Chưa gửi pet") && k1.contains("Chợ sinh thái") && p.xu == xuK,
                        k1 + " | xu " + xuK + " -> " + p.xu);
                String k2 = chonHst(p, 6, 1, -1, "pet đồng hành");
                ket("tn_nha_tre.khong_pet_gui", k2 != null && k2.contains("không phải Pokémon chiến đấu"), String.valueOf(k2));
            }
            // dung canh: cap + xu (GM dat cho nguoi choi); pet thi NGUOI CHOI tu nhan (khong GM tang - PetDongHanh 30/10/2026)
            lenhGm("slv " + p.ten + " 12 0"); // danh quai nhanh hon (buoc = so quai ha, khong phu thuoc cap)
            KichBanBatThuong.datXu(p, 50000);
            onDinh(p);
            // nhan pet dong hanh mien phi (1 lan): Nha tre muc 5 -> chon Hoothoot
            moHst(p, "Nhà trẻ", 6);
            String dsLoai = chonHst(p, 6, 5, 32);
            ket("tn_nha_tre.chon_loai", dsLoai != null && dsLoai.contains("Hoothoot") && dsLoai.contains("Zubat") && dsLoai.contains("Cleffa"),
                    String.valueOf(dsLoai));
            long xuNhan = p.xu;
            String nh = dsLoai == null ? null : chonHst(p, 32, 0, -1, "Đã nhận Hoothoot");
            onDinh(p);
            ket("tn_nha_tre.co_pet", nh != null && dem(p, 689) == 1 && p.xu == xuNhan, nh + " | Hoothoot trong tui: " + dem(p, 689)
                    + " | xu " + xuNhan + " -> " + p.xu);
            ket("tn_nha_tre.csdl_nhan", "689".equals(BotChucNang.sql1("SELECT id_item FROM tob_pet_dong_hanh WHERE id_char = " + p.charDb)),
                    "tob_pet_dong_hanh: " + BotChucNang.sql1("SELECT CONCAT(id_item, ',', nguon) FROM tob_pet_dong_hanh WHERE id_char = " + p.charDb));
            // bam lai bang chon cu (Zubat) + muc nhan lan 2: tu choi, khong them pet
            String nh2 = chonHst(p, 32, 1, -1, "đã nhận pet đồng hành");
            moHst(p, "Nhà trẻ", 6);
            String nhan2 = p.menuChu.size() > 5 ? p.menuChu.get(5) : String.join(" ; ", p.menuChu);
            String nh3 = chonHst(p, 6, 5, -1, "đã nhận pet đồng hành");
            onDinh(p);
            ket("tn_nha_tre.nhan_lan_2", nh2 != null && nh3 != null && nhan2.contains("đã nhận") && dem(p, 689) == 1 && dem(p, 704) == 0,
                    nh2 + " | " + nhan2 + " | " + nh3 + " | Hoothoot " + dem(p, 689) + ", Zubat " + dem(p, 704));
            // mua them 1 Zubat bang xu: tru DUNG gia ghi tren menu
            moHst(p, "Nhà trẻ", 6);
            String mucMua = p.menuChu.size() > 6 ? p.menuChu.get(6) : "";
            long giaPet = giaTrongChu(mucMua);
            long xuMua = p.xu;
            String dsMua = chonHst(p, 6, 6, 33);
            String mu = dsMua == null ? null : chonHst(p, 33, 1, -1, "Đã mua", "Cần");
            onDinh(p);
            ket("tn_nha_tre.mua_pet", mu != null && mu.contains("Đã mua Zubat") && giaPet > 0 && xuMua - p.xu == giaPet && dem(p, 704) == 1,
                    "\"" + mucMua + "\" -> " + mu + " | xu " + xuMua + " -> " + p.xu + " | Zubat " + dem(p, 704));
            int n = moHst(p, "Nhà trẻ", 6);
            ket("tn_nha_tre.mo", n > 0, n > 0 ? String.join(" ; ", p.menuChu) : p.chuTu(0));
            if (n <= 0) return;
            // tra phi khi chua gui: tu choi, khong tru
            long xu0 = p.xu;
            String c = chonHst(p, 6, 3, -1, "Chưa gửi", "phí");
            onDinh(p);
            ket("tn_nha_tre.phi_chua_gui", c != null && c.contains("Chưa gửi") && p.xu == xu0, c + " | xu " + xu0 + " -> " + p.xu);
            // gui Hoothoot
            String ds = chonHst(p, 6, 1, 7);
            String g = ds == null ? null : chonHst(p, 7, 0, -1, "Đã gửi");
            ket("tn_nha_tre.gui", g != null && g.contains("Hoothoot"), "danh sach: " + ds + " | " + g);
            // tra phi: tru DUNG phi ghi tren menu
            moHst(p, "Nhà trẻ", 6);
            String mucPhi = p.menuChu.size() > 3 ? p.menuChu.get(3) : "";
            long phi = giaTrongChu(mucPhi);
            long xu1 = p.xu;
            String tp = chonHst(p, 6, 3, -1, "Đã trả phí", "Cần");
            onDinh(p);
            ket("tn_nha_tre.tra_phi", tp != null && tp.contains("Đã trả phí") && phi > 0 && xu1 - p.xu == phi,
                    "\"" + mucPhi + "\" -> " + tp + " | xu " + xu1 + " -> " + p.xu);
            // tra lan 2 cung ngay: khong tru them
            long xu2 = p.xu;
            moHst(p, "Nhà trẻ", 6);
            String tp2 = chonHst(p, 6, 3, -1, "Hôm nay đã trả", "Đã trả");
            onDinh(p);
            ket("tn_nha_tre.phi_mot_lan", p.xu == xu2, tp2 + " | xu " + xu2 + " -> " + p.xu);
            long[] nt;
            // dung canh: buoc nha tre = 299 (CSDL, luc thoat) -> ha 1 quai la de trung
            p = suaHoSoRoiVao(p, du -> du.replaceFirst("(nt[=:])(-?\\d+),(-?\\d+),(-?\\d+),", "$1$2,$3,299,"));
            nt = nhaTreCsdl(hstCsdl(p));
            ket("tn_nha_tre.csdl_gui", nt != null && nt[0] == 163 && nt[2] == 299, "nt=" + (nt == null ? "null" : Arrays.toString(nt)) );
            // dang nhap lai: van khong nhan duoc pet mien phi lan 2 (co CSDL, khong phai bo nho)
            moHst(p, "Nhà trẻ", 6);
            String vl = p.menuChu.size() > 5 ? p.menuChu.get(5) : String.join(" ; ", p.menuChu);
            int pVl = dem(p, 689) + dem(p, 704) + dem(p, 699);
            String nhVl = chonHst(p, 6, 5, -1, "đã nhận pet đồng hành");
            onDinh(p);
            ket("tn_nha_tre.nhan_vao_lai", vl.contains("đã nhận") && nhVl != null && dem(p, 689) + dem(p, 704) + dem(p, 699) == pVl,
                    vl + " | " + nhVl + " | pet " + pVl + " -> " + (dem(p, 689) + dem(p, 704) + dem(p, 699)));
            List<Integer> q = raBanDoCoQuai(p);
            if (q.isEmpty()) { ket("tn_nha_tre.de_trung", false, "khong ra duoc ban do co quai"); return; }
            int md = p.moc();
            boolean ha = haQuai(p);
            String de = p.doiChu(md, 4000, "đẻ trứng");
            ket("tn_nha_tre.de_trung", ha && de != null, "ha quai " + ha + ": " + (de == null ? p.chuTu(md) : de));
            // xem trung: IV trong khoang hoang da (0..15, dot bien +-3), dung loai
            int nm = moHst(p, "Nhà trẻ", 6);
            String trung = nm > 0 ? chonHst(p, 6, 4, 8) : null;
            Matcher iv = Pattern.compile("Hoothoot\\S* IV (\\d+)/(\\d+)/(\\d+)").matcher(trung == null ? "" : trung);
            boolean ivOk = iv.find();
            if (ivOk) for (int i = 1; i <= 3; i++) ivOk &= Integer.parseInt(iv.group(i)) <= 18;
            ket("tn_nha_tre.trung_iv", ivOk, "trung: " + trung);
            // nha tre sau de: buoc ve 0, de hom nay 1, dem khong shiny +1 (hoac 0 neu shiny)
            String nhan0 = null;
            moHst(p, "Nhà trẻ", 6);
            nhan0 = p.menuChu.isEmpty() ? "" : p.menuChu.get(0);
            ket("tn_nha_tre.dem", nhan0.contains("(0/300") || nhan0.contains("(1/300") , nhan0);
            // dung canh: trung con 1 buoc la no (CSDL), ha 1 quai -> "sap no"
            int[] buocNo = {400};
            p = suaHoSoRoiVao(p, du -> du.replaceFirst("(163\\.\\d+\\.\\d+\\.\\d+\\.[01]\\.)(\\d+)", "$1" + (buocNo[0] - 1)));
            String duT = hstCsdl(p);
            ket("tn_nha_tre.csdl_trung", duT != null && duT.contains(".399"), duT == null ? "null" : duT.length() > 200 ? duT.substring(0, 200) : duT);
            moHst(p, "Nhà trẻ", 6);
            String trungTruoc = chonHst(p, 6, 4, 8);
            q = raBanDoCoQuai(p);
            int ms = p.moc();
            boolean ha2 = !q.isEmpty() && haQuai(p);
            String sap = p.doiChu(ms, 4000, "sắp nở");
            ket("tn_nha_tre.sap_no", ha2 && sap != null, "truoc: " + trungTruoc + " | ha quai " + ha2 + " (map " + p.mapId + ", " + q.size() + " quai): " + (sap == null ? p.chuTu(ms) : sap));
            // no trung: +1 pet Hoothoot (689 / 690), trung bien mat; no dup -> khong them
            onDinh(p);
            int pet0 = dem(p, 689) + dem(p, 690);
            moHst(p, "Nhà trẻ", 6);
            chonHst(p, 6, 4, 8);
            chonHst(p, 8, 0, 9);
            int mn = p.moc();
            p.menu(NPC_HST, 9, 0);
            p.menu(NPC_HST, 9, 0); // bam dup "Cho trung no"
            String no = p.doiChu(mn, 4000, "Trứng nở");
            p.bom(800);
            onDinh(p);
            int pet1 = dem(p, 689) + dem(p, 690);
            ket("tn_nha_tre.no", no != null && pet1 == pet0 + 1, no + " | pet Hoothoot " + pet0 + " -> " + pet1);
            p = KichBanBatThuong.vaoLai(p);
            moHst(p, "Nhà trẻ", 6);
            String xemTrung = p.menuChu.size() > 4 ? p.menuChu.get(4) : String.join(" ; ", p.menuChu);
            ket("tn_nha_tre.no_vao_lai", xemTrung.contains("(0/"), "sau vao lai: " + xemTrung);
            // ho so ghi CSDL theo nhip 15 giay (HeSinhThaiGame.nhipCham): doi toi 25 giay
            String du2 = null;
            boolean conTrung = true;
            for (int i = 0; i < 10 && conTrung; i++) {
                if (i > 0) Thread.sleep(2500);
                du2 = hstCsdl(p);
                conTrung = du2 != null && Pattern.compile("163\\.\\d+\\.\\d+\\.\\d+\\.[01]\\.\\d+").matcher(du2).find();
            }
            long[] nt2 = nhaTreCsdl(du2);
            ket("tn_nha_tre.no_csdl", !conTrung && (dem(p, 689) + dem(p, 690)) == pet1, "con trung trong ho so: " + conTrung + ", pet trong tui sau vao lai "
                    + (dem(p, 689) + dem(p, 690)) + ", nt=" + (nt2 == null ? "null" : Arrays.toString(nt2)));
            // vut het pet dang gui (khong con so huu) roi ha quai du buoc: nha tre KHONG duoc de trung tu pet khong con
            for (int[] d : new ArrayList<>(p.doDung)) {
                if (d[1] == 689 || d[1] == 690) {
                    p.gui(61, (short) d[0]);
                    p.bom(300);
                    p.gui(61, (short) d[0], (byte) 1);
                    p.bom(300);
                }
            }
            onDinh(p);
            int conPet = dem(p, 689) + dem(p, 690);
            p = suaHoSoRoiVao(p, du -> du.replaceFirst("(nt[=:])(-?\\d+),(-?\\d+),(-?\\d+),", "$1$2,$3,299,"));
            q = raBanDoCoQuai(p);
            int mk = p.moc();
            boolean ha3 = !q.isEmpty() && haQuai(p);
            String de2 = p.doiChu(mk, 4000, "đẻ trứng");
            long[] nt3 = nhaTreCsdl(hstCsdl(p));
            ket("tn_nha_tre.khong_con_pet", conPet == 0 && ha3 && de2 == null, "pet con " + conPet + ", ha quai " + ha3 + ": "
                    + (de2 == null ? "khong de trung" : de2) + ", nt=" + (nt3 == null ? "null" : Arrays.toString(nt3)));
        } finally {
            p.close();
        }
        ket("tn_nha_tre.may_chu", KichBanBatThuong.mayChuSong(), "nguoi choi khac van vao game + lam moi");
        KichBanBatThuong.kiemLog("tn_nha_tre");
    }

    // ================================================================== 3. doi khu
    /** Chon khu op (menu -34); tra {co goi 12 (da doi), chu thong bao}. */
    static Object[] chonKhu(BotChucNang.Bot b, int op) throws IOException {
        int m = b.moc();
        b.menu(-34, 0, op);
        b.cho(x -> x.coLenh(m, 12) || x.doiChu(m, 0, "") != null, 4000);
        boolean doi = b.coLenh(m, 12);
        if (doi) {
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(600);
            b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8;
        }
        String c = b.doiChu(m, 300, "");
        return new Object[]{doi, c};
    }

    static void doiKhu() throws Exception {
        KichBanBatThuong.loiLogMoi();
        // ban do co nhieu khu chi o vung Johto (Cherrygrove 15, Violet 20, ...), xa lang khoi dau: nhan vat Johto cap 40,
        // DUNG CANH dat vi tri luu (tob_char.pInfo, luc da thoat) tai Cherrygrove = di bo / di tau toi do
        BotChucNang.Bot p0 = new BotChucNang.Bot(BotChucNang.tkThuong());
        BotChucNang.xoaNhanVatCu(p0.user);
        String l = p0.dangNhap("123456");
        if (l == null) l = p0.vaoGame(0, 1);
        if (l != null) throw new IllegalStateException(p0.user + " " + l);
        BotChucNang.duTuoiChongClone(p0);
        KichBanDS.veLang(p0);
        // cap 99 (khong phai 40): nhan vat moi dung 2 x 21 giay o bien khu Cherrygrove giua quai cap ~40. Sau can bang class
        // 31/10/2026 (ChiSoDong, mau / thu chuan hoa) nhan vat cap 40 tay trang bi quai danh guc giua cac buoc -> moi buoc khu
        // bao "Bạn đã bị hạ gục" (cong kiem 31/10). Nguoi choi that o day da co do / thuoc; cap cao chi giu canh song, khong
        // doi luat khu (menu, bien, trung khu, hoi chieu 20 giay kiem nhu cu). Mo ban do: tob_ban_do_mo moc 40 nhu cu.
        lenhGm("slv " + p0.ten + " 99 0");
        p0.bom(500);
        int idc = p0.charDb;
        p0.close();
        Thread.sleep(3000);
        KichBanKetTau.doiTruong(idc, "pInfo", new int[]{8, 9, 10}, new String[]{"15", "" + (20 * 16 + 8), "" + (20 * 16 + 8)});
        // Cherrygrove mo khi xong cua ai CA-07.2 (cap 40): dung canh = moc mien tru cap 40 (tob_ban_do_mo) nhu da qua cua ai
        BotChucNang.sql("REPLACE INTO tob_ban_do_mo (id_char, moc_mien, nguon, luc) VALUES (" + idc + ", 40, 'bot', NOW())");
        BotChucNang.Bot p = KichBanBatThuong.vaoLai(p0);
        p.px = p.tx * 16 + 8;
        p.py = p.ty * 16 + 8;
        try {
            // nguoi choi thuong mo menu khu bang cham bien Khu (NPC -34, lenh 23) - chat "khu" chi danh cho GM
            int m = p.moc();
            p.gui(23, (byte) -34);
            int n = p.doiMenu(m, -34, 0, 4000);
            ket("tn_khu.menu", n >= 2, "menu khu: " + String.join(" ; ", p.menuChu) + " (map " + p.mapId + ")");
            if (n < 2) return;
            // khu ngoai danh sach: khong doi, khong rot
            Object[] r = chonKhu(p, 99);
            ket("tn_khu.sai", !(Boolean) r[0] && p.conSong(), "chon khu 100: doi=" + r[0] + " " + r[1]);
            // tim bien khu: xa bien -> may chu bao o
            int mapDau = p.mapId;
            r = chonKhu(p, 1);
            String chu = (String) r[1];
            Matcher o = Pattern.compile("ô (\\d+),(\\d+)").matcher(chu == null ? "" : chu);
            if ((Boolean) r[0]) {
                ket("tn_khu.xa_bien", null, "dang dung gan bien khu, da doi ngay");
                r = chonKhu(p, 0); // ve khu 1 de bat dau tu khu 1 (hoi chieu)
            } else if (o.find()) {
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(p.mapLoad);
                int[] dich = bd.oGan(Integer.parseInt(o.group(1)), Integer.parseInt(o.group(2)), 3);
                boolean toi = dich != null && p.diDuong(bd, dich[0], dich[1]);
                ket("tn_khu.xa_bien", toi, "\"" + chu + "\" -> di bo toi " + (dich == null ? "?" : dich[0] + "," + dich[1]) + ": " + toi);
                if (!toi) return;
            } else {
                ket("tn_khu.xa_bien", false, "khong doi va khong bao o bien khu: " + chu);
                return;
            }
            Thread.sleep(21000);
            // chon dung khu dang dung (khu 1): bao, KHONG tinh hoi chieu
            r = chonKhu(p, 0);
            ket("tn_khu.cung_khu", !(Boolean) r[0] && r[1] != null && ((String) r[1]).contains("đang ở tại khu"), "chon khu 1 khi dang o khu 1: " + r[1]);
            // ngay sau do doi sang khu 2: phai duoc (chua doi lan nao)
            r = chonKhu(p, 1);
            boolean doi2 = (Boolean) r[0];
            ket("tn_khu.doi", doi2 && p.mapId == mapDau, "khu 2 ngay sau lan chon trung khu: doi=" + doi2 + " map " + p.mapId + " " + r[1]);
            // doi lai ngay: hoi chieu 20 giay, bao so giay, khong doi
            r = chonKhu(p, 0);
            ket("tn_khu.hoi_chieu", !(Boolean) r[0] && r[1] != null && ((String) r[1]).contains("giây"), "doi lai ngay: " + r[1]);
            // het hoi chieu: doi duoc
            Thread.sleep(21000);
            r = chonKhu(p, 0);
            if (!(Boolean) r[0] && r[1] != null && ((String) r[1]).contains("hạ gục")) {
                // quai Cherrygrove danh guc nhan vat dung cho 21 giay o bien khu: may chu chan moi thao tac khi guc (dung luat,
                // KiemChanKhiGuc) - khong phai loi hoi chieu khu, khong kiem duoc buoc nay lan nay
                ket("tn_khu.het_hoi_chieu", null, "bi quai danh guc trong luc cho het hoi chieu: " + r[1]);
            } else {
                ket("tn_khu.het_hoi_chieu", (Boolean) r[0], "sau 21 giay: doi=" + r[0] + " " + r[1]);
            }
            // vao lai: vao map hop le, khong ket
            p = KichBanBatThuong.vaoLai(p);
            ket("tn_khu.vao_lai", p.mapId >= 0 && p.hp > 0 || p.mapId >= 0, "map " + p.mapId + " hp " + p.hp);
        } finally {
            p.close();
        }
        KichBanBatThuong.kiemLog("tn_khu");
    }

    // ================================================================== 4. ngay moi / tuan moi (dong ho ao)
    /** So tay -> muc "Về làng"; tra {da ve lang (goi 12 toi lang), chu}. */
    static Object[] veLangSoTay(BotChucNang.Bot b) throws IOException {
        KichBanDS4.moSoTay(b);
        int i = b.timMuc("Về làng");
        String nhan = i >= 0 ? b.menuChu.get(i) : String.join(" ; ", b.menuChu);
        if (i < 0) return new Object[]{false, "khong co muc Ve lang: " + nhan};
        int m = b.moc();
        b.menu(-527, 0, i);
        b.cho(x -> x.coLenh(m, 12) || x.doiChu(m, 0, "") != null, 5000);
        if (b.coLenh(m, 12)) {
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1000);
            b.px = b.tx * 16 + 8;
            b.py = b.ty * 16 + 8;
        }
        String c = b.doiChu(m, 600, "");
        return new Object[]{b.coLenh(m, 12) && KichBanDS.laLang(b.mapId), "\"" + nhan + "\" -> map " + b.mapId + " " + c};
    }

    static void ngayMoi() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot p = KichBanBatThuong.nguoi();
        try {
            lenhGm("slv " + p.ten + " 30 0");
            onDinh(p);
            // --- nhiem vu tuan: toi da 2 / tuan
            List<String> tuan = KichBanDS4.dsNhom(p, 3, 12);
            boolean coTuan = tuan != null && tuan.size() >= 3 && !tuan.get(0).startsWith("THONG BAO");
            String k3 = null;
            if (coTuan) {
                KichBanDS4.nhan(p, 0);
                KichBanDS4.nhan(p, 1);
                KichBanDS4.dsNhom(p, 3, 12);
                k3 = KichBanDS4.nhan(p, 2);
                ket("tn_ngay_moi.tuan_toi_da_2", BotChucNang.khongDau(k3).contains("chi nhan 2"), "lan 3: " + k3.replace('\n', ' '));
            } else ket("tn_ngay_moi.tuan_toi_da_2", null, "bang tuan < 3 muc: " + tuan);
            // --- ve lang mien phi: 1 luot / ngay
            List<Integer> q = raBanDoCoQuai(p);
            Object[] v1 = q.isEmpty() ? new Object[]{false, "khong ra duoc khoi lang"} : veLangSoTay(p);
            ket("tn_ngay_moi.ve_lang_1", (Boolean) v1[0], (String) v1[1]);
            q = raBanDoCoQuai(p);
            int mapNgoai = p.mapId;
            Thread.sleep(61000);
            Object[] v2 = veLangSoTay(p);
            ket("tn_ngay_moi.ve_lang_het_luot", !(Boolean) v2[0] && p.mapId == mapNgoai && !((String) v2[1]).contains("đang hồi"), (String) v2[1]);
            // --- GM tua dong ho ao (may thu) toi thu Hai 05:01 tuan sau
            BotChucNang.Bot g = gm();
            Long ao = KichBanPhoBanCup.gioAo(g);
            if (ao == null) { ket("tn_ngay_moi.tua", false, "khong doc duoc dong ho ao (sv.cho_phep_thu_nghiem=1?)"); return; }
            long dich = KichBanPhoBanCup.thuHaiSau(ao) - 59 * 60000L;
            g.gm("tua " + Math.max(1, (dich - ao) / 60000L + 1) + "m", 2500);
            Long ao2 = KichBanPhoBanCup.gioAo(g);
            ket("tn_ngay_moi.tua", ao2 != null && ao2 >= dich, "gio ao " + new java.util.Date(ao) + " -> " + (ao2 == null ? "?" : new java.util.Date(ao2)));
            Thread.sleep(61000); // hoi chieu 60 giay chung cua Ve lang (gio that), khong lien quan luot ngay
            // --- sau moc: ve lang mien phi co lai, nhiem vu tuan nhan tiep duoc
            Object[] v3 = veLangSoTay(p);
            ket("tn_ngay_moi.ve_lang_ngay_moi", (Boolean) v3[0], (String) v3[1]);
            if (coTuan) {
                List<String> tuan2 = KichBanDS4.dsNhom(p, 3, 12);
                String k4 = tuan2 == null || tuan2.isEmpty() ? "khong co bang tuan" : KichBanDS4.nhan(p, Math.min(2, tuan2.size() - 1));
                ket("tn_ngay_moi.tuan_moi", !BotChucNang.khongDau(k4).contains("chi nhan 2"), "bang tuan moi " + tuan2 + " | nhan: " + k4.replace('\n', ' '));
            }
            // --- dang nhap lai sau moc: van giu (khong dat lai lan nua, khong mat)
            p = KichBanBatThuong.vaoLai(p);
            q = raBanDoCoQuai(p);
            Thread.sleep(61000);
            Object[] v4 = veLangSoTay(p);
            ket("tn_ngay_moi.ve_lang_vao_lai", !(Boolean) v4[0] && !((String) v4[1]).contains("đang hồi"), "luot ngay moi da dung, vao lai khong duoc them: " + v4[1]);
        } finally {
            p.close();
        }
        KichBanBatThuong.kiemLog("tn_ngay_moi");
    }

    // ================================================================== 5. Quan ly thu: ve mo o (nguoi choi thuong)
    static void veOThu() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot p = KichBanBatThuong.nguoi();
        try {
            // dat luong DUNG 100 (GM smn chenh lech), doi toi khi nguoi choi thay
            for (int lan = 0; lan < 3 && p.luong != 100; lan++) {
                onDinh(p);
                if (p.luong != 100) lenhGm("smn " + p.ten + " 0 " + (100 - p.luong) + " 0");
                p.cho(x -> x.luong == 100, 6000);
            }
            onDinh(p);
            p.gui(-119, (byte) 0);
            p.bom(800);
            onDinh(p);
            int[] o0 = KichBanQaToanGame.oThu(p);
            ket("tn_o_thu.mo", o0 != null, o0 == null ? "khong co phan quan ly thu trong goi 16/2" : "o " + o0[0] + ", ve " + o0[1]);
            if (o0 == null) return;
            // mua 1 ve: luong tru dung 1 lan
            int l0 = p.luong;
            int m = p.moc();
            p.gui(-119, (byte) 4);
            p.bom(1200);
            onDinh(p);
            int[] o1 = KichBanQaToanGame.oThu(p);
            int gia = l0 - p.luong;
            ket("tn_o_thu.mua", o1 != null && o1[1] == o0[1] + 1 && gia > 0, "ve " + o0[1] + " -> " + (o1 == null ? "?" : o1[1]) + ", luong " + l0 + " -> " + p.luong + " " + p.chuTu(m));
            // het luong (dat luong = gia - 1): mua bi tu choi, khong tru, khong them ve
            if (gia > 0 && o1 != null) {
                for (int lan = 0; lan < 3 && p.luong != gia - 1; lan++) {
                    lenhGm("smn " + p.ten + " 0 " + (gia - 1 - p.luong) + " 0");
                    p.cho(x -> x.luong == gia - 1, 6000);
                    onDinh(p);
                }
                onDinh(p);
                int l1 = p.luong;
                m = p.moc();
                p.gui(-119, (byte) 4);
                p.bom(1200);
                onDinh(p);
                int[] o2 = KichBanQaToanGame.oThu(p);
                ket("tn_o_thu.thieu_luong", p.luong == l1 && o2 != null && o2[1] == o1[1], "luong " + l1 + " (gia " + gia + ") -> " + p.luong + ", ve " + o1[1] + " -> " + (o2 == null ? "?" : o2[1]) + " " + p.chuTu(m));
            }
            // dung 1 ve, bam dup: o +1 dung mot lan
            m = p.moc();
            p.gui(-119, (byte) 3);
            p.gui(-119, (byte) 3);
            p.bom(1500);
            onDinh(p);
            int[] o3 = KichBanQaToanGame.oThu(p);
            ket("tn_o_thu.dung_dup", o1 != null && o3 != null && o3[0] == o1[0] + 1 && o3[1] == o1[1] - 1, "o " + (o1 == null ? "?" : o1[0]) + " -> " + (o3 == null ? "?" : o3[0])
                    + ", ve -> " + (o3 == null ? "?" : o3[1]) + " " + p.chuTu(m));
            int suc = o3 == null ? -1 : o3[0], luongSau = p.luong;
            p = KichBanBatThuong.vaoLai(p);
            p.gui(-119, (byte) 0);
            p.bom(1000);
            onDinh(p);
            int[] o4 = KichBanQaToanGame.oThu(p);
            ket("tn_o_thu.vao_lai", o4 != null && o4[0] == suc && p.luong == luongSau, "o " + (o4 == null ? "?" : o4[0]) + " (mong " + suc + "), luong " + p.luong + " (mong " + luongSau + ")");
        } finally {
            p.close();
        }
        KichBanBatThuong.kiemLog("tn_o_thu");
    }

    // ================================================================== 6. cho nguoi choi: hai nguoi choi thuong mua ban
    static void choHaiNguoi() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot a = KichBanBatThuong.nguoiTrongNha();
        BotChucNang.Bot c = KichBanBatThuong.nguoiTrongNha();
        try {
            lenhGm("slv " + a.ten + " 20 0");
            lenhGm("slv " + c.ten + " 20 0");
            KichBanBatThuong.datXu(a, 5000);
            int id = KichBanBatThuong.muaVuKhi(a, KichBanBatThuong.VU_KHI);
            if (id == KichBanDS3.KHONG) { ket("tn_cho.dang_ban", false, "A khong mua duoc vu khi: " + a.chuTu(0)); return; }
            final int gia = 10000;
            int npc = 11, shop = 0, m = a.moc();
            for (npc = 11; npc <= 20; npc++) {
                m = a.moc();
                final int m92 = m;
                a.gui(92, true, (byte) npc, (byte) shop, (short) id, gia, (byte) 0);
                a.cho(x -> x.layLenh(m92, -31) != null || x.coLenh(m92, 92), 3000);
                if (a.layLenh(m, -31) != null) {
                    a.gui(-31, (short) -32047, (byte) 0, "");
                    a.bom(1000);
                }
                if (a.coLenh(m, 92)) break;
                if (a.doiChu(m, 0, "chỉ bán") == null) break;
            }
            boolean daBan = a.coLenh(m, 92);
            ket("tn_cho.dang_ban", daBan, "npc " + npc + ": " + a.chuTu(m));
            if (!daBan) return;
            onDinh(a);
            long xuA = a.xu;
            KichBanBatThuong.datXu(c, gia); // so du VUA DUNG bang gia
            m = c.moc();
            c.gui(94, (byte) 0, (byte) npc, (byte) shop);
            c.bom(800);
            c.gui(94, (byte) 1, (byte) npc, (byte) shop, (short) a.actor);
            c.bom(800);
            int m2 = c.moc();
            c.gui(95, (byte) npc, (byte) shop, (short) a.actor, (short) id, (byte) 0);
            c.cho(x -> x.layLenh(m2, -32) != null, 3000);
            BotChucNang.SuKien pp = c.layLenh(m2, -32);
            if (pp != null) {
                c.gui(-32, (short) pp.so[0], (byte) pp.so[1], "", (byte) 1);
                c.gui(-32, (short) pp.so[0], (byte) pp.so[1], "", (byte) 1); // xac nhan dup
                c.bom(1500);
            }
            // bam mua lai mon da ban (tin khong con)
            c.gui(95, (byte) npc, (byte) shop, (short) a.actor, (short) id, (byte) 0);
            c.bom(1200);
            BotChucNang.SuKien pp2 = null;
            for (int i = m2; i < c.su.size(); i++) if (c.su.get(i).cmd == -32 && c.su.get(i) != pp) pp2 = c.su.get(i);
            if (pp2 != null) { c.gui(-32, (short) pp2.so[0], (byte) pp2.so[1], "", (byte) 1); c.bom(1200); }
            a.bom(500);
            onDinh(a);
            onDinh(c);
            int coDo = dem(c, KichBanBatThuong.VU_KHI);
            boolean aConDo = a.timDo(d -> d[0] == id) != null;
            ket("tn_cho.mua_vua_du", coDo == 1 && c.xu == 0 && !aConDo, "C co " + coDo + " mon, xu C " + gia + " -> " + c.xu + "; A con mon=" + aConDo + " " + c.chuTu(m2));
            long nhan = a.xu - xuA;
            ket("tn_cho.nguoi_ban_nhan", nhan > 0 && nhan <= gia && nhan >= gia * 80 / 100, "A nhan " + nhan + "/" + gia + " (sau thue)");
            // CSDL sau khi ca hai thoat
            int idA = a.charDb, idC = c.charDb;
            long xa = a.xu, xc = c.xu;
            a.close();
            c.close();
            Thread.sleep(4000);
            long dbA = Long.parseLong(BotChucNang.sql1("SELECT gold FROM tob_char WHERE id=" + idA).trim());
            long dbC = Long.parseLong(BotChucNang.sql1("SELECT gold FROM tob_char WHERE id=" + idC).trim());
            ket("tn_cho.csdl", dbA == xa && dbC == xc, "tob_char.gold A " + dbA + " (game " + xa + "), C " + dbC + " (game " + xc + ")");
            a = KichBanBatThuong.vaoLai(a);
            c = KichBanBatThuong.vaoLai(c);
            ket("tn_cho.vao_lai", dem(c, KichBanBatThuong.VU_KHI) == 1 && a.timDo(d -> d[1] == KichBanBatThuong.VU_KHI) == null,
                    "sau vao lai: C co " + dem(c, KichBanBatThuong.VU_KHI) + " mon, A co " + dem(a, KichBanBatThuong.VU_KHI));
        } finally {
            a.close();
            c.close();
        }
        KichBanBatThuong.kiemLog("tn_cho");
    }

    // ================================================================== 7. bang hoi: nguoi choi thuong tao / moi / chat / roi
    static int taoBang(BotChucNang.Bot a, String ten) throws IOException {
        int m = a.moc();
        a.gui(-9, new byte[0]);
        a.cho(x -> x.coLenh(m, -9) || x.doiChu(m, 0, "") != null, 4000);
        BotChucNang.SuKien e = a.layLenh(m, -9);
        if (e == null || e.d.length < 4) return -2;
        int icon = ((e.d[2] & 255) << 8) | (e.d[3] & 255);
        int m2 = a.moc();
        a.gui(-10, (short) icon, ten);
        a.cho(x -> x.coLenh(m2, -10) || x.doiChu(m2, 0, "") != null, 5000);
        BotChucNang.SuKien t = a.layLenh(m2, -10);
        return t != null && t.d.length >= 2 ? (short) (((t.d[0] & 255) << 8) | (t.d[1] & 255)) : -1;
    }

    static void bangHoi() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot a = KichBanBatThuong.nguoi();
        BotChucNang.Bot c = KichBanBatThuong.nguoi();
        try {
            final long GIA = 100_000_000L;
            lenhGm("slv " + a.ten + " 50 0");
            lenhGm("spt " + a.ten + " 31 1");
            lenhGm("slv " + c.ten + " 50 0"); // thanh vien doi phai tu cap 50
            KichBanBatThuong.datXu(a, GIA); // VUA DUNG gia lap doi
            String ten = "tn" + (System.currentTimeMillis() % 1000000);
            int id = taoBang(a, ten);
            onDinh(a);
            String db = id < 0 ? null : BotChucNang.sql1("SELECT CONCAT(clanName,'|',charMaster) FROM tob_clan WHERE id_clan=" + id);
            ket("tn_bang.tao", id >= 0 && a.xu == 0 && db != null && db.startsWith(ten + "|"), "id " + id + ", xu " + GIA + " -> " + a.xu + ", huy hieu " + a.soThuoc(31)
                    + ", tob_clan: " + db + " | " + a.chuTu(0).replaceAll(".*\\| (\\[[^|]*\\| ){0,3}$", ""));
            if (id < 0) return;
            // tao lan 2 khi da co doi (them xu): tu choi, khong tru
            KichBanBatThuong.datXu(a, GIA);
            int m = a.moc();
            a.gui(-9, new byte[0]);
            String tc = a.doiChu(m, 3000, "đang ở trong đội", "Không thể");
            onDinh(a);
            ket("tn_bang.tao_lai", tc != null && a.xu == GIA, tc + " | xu " + a.xu);
            // moi C, C dong y
            int mc = c.moc();
            a.gui(-11, (byte) 0, (short) c.actor);
            boolean moi = c.choLenh(mc, -11, 4000);
            c.gui(-11, (byte) 1, (short) a.actor, true);
            c.bom(1500);
            a.bom(500);
            String tv = BotChucNang.sql1("SELECT member FROM tob_clan WHERE id_clan=" + id);
            ket("tn_bang.moi", moi && tv != null && tv.contains(c.ten), "C nhan loi moi=" + moi + ", member CSDL: " + tv);
            // C chat bang -> A nhan
            int mca = a.moc();
            c.gui(-18, "xin chao doi");
            String cb = a.doiChu(mca, 4000, "xin chao doi");
            ket("tn_bang.chat", cb != null, cb == null ? a.chuTu(mca) : cb);
            // C roi doi: phi roi doi thanh vien 500.000 xu (90% vao quy) -> dat VUA DUNG; CSDL khong con ten
            KichBanBatThuong.datXu(c, 500000);
            c.gui(-15, new byte[0]);
            c.bom(1500);
            a.bom(1500);
            Thread.sleep(2000);
            String tv2 = BotChucNang.sql1("SELECT member FROM tob_clan WHERE id_clan=" + id);
            onDinh(c);
            ket("tn_bang.roi", tv2 != null && !tv2.contains(c.ten) && c.xu == 0, "member sau khi C roi: " + tv2 + ", xu C 500000 -> " + c.xu);
            c = KichBanBatThuong.vaoLai(c);
            int mc2 = c.moc(), ma2 = a.moc();
            a.gui(-11, (byte) 0, (short) c.actor);
            boolean moi2 = c.choLenh(mc2, -11, 4000);
            String cho = a.doiChu(ma2, 1500, "chỉ có thể tham gia");
            ket("tn_bang.roi_moi_lai", moi2 || cho != null, "sau khi roi + vao lai: C nhan loi moi=" + moi2 + " / A duoc bao thoi gian cho: " + cho);
            // A vao lai: van la chu doi
            a = KichBanBatThuong.vaoLai(a);
            String chu = BotChucNang.sql1("SELECT charMaster FROM tob_clan WHERE id_clan=" + id);
            ket("tn_bang.vao_lai", chu != null && chu.trim().equals(a.ten) && a.xu == GIA, "chu doi " + chu + ", xu A " + a.xu);
        } finally {
            a.close();
            c.close();
        }
        ket("tn_bang.may_chu", KichBanBatThuong.mayChuSong(), "nguoi choi khac van vao game + lam moi");
        KichBanBatThuong.kiemLog("tn_bang");
    }

    // ================================================================== 8. trao doi Pokemon giua hai nguoi choi thuong
    /** Goi -121 kieu 6 (trao doi) moi nhat sau moc co byte trang thai {@code tt}; null = khong co. */
    static byte[] goiTraoDoi(BotChucNang.Bot b, int tu, int tt) {
        byte[] r = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -121 && e.d != null && e.d.length > 1 && e.d[0] == 6 && e.d[1] == tt) r = e.d;
        }
        return r;
    }

    /** Hop PC (CSDL) moi chi mot con loai dex; id con = 1. Viet khi nhan vat da thoat va may chu da bo hop khoi bo nho. */
    static String hopMotCon(int idChar, int dex) {
        return "v=1;ik=2;mr=0;dt=0;ng=20261001;bn=0;nn=0;tb=1;dn=0;cx=0;cl=0;dc=0;td=0;tc=0;mn=0;bg=0;b_poke=5;c1=" + dex
                + ".5.0.20261001.poke.700000000.3." + idChar + ".0";
    }

    static void ganHop(int idChar, int dex) {
        BotChucNang.sql("DELETE FROM tob_hop_pc WHERE id_char = " + idChar);
        BotChucNang.sql("INSERT INTO tob_hop_pc (id_char, du_lieu) VALUES (" + idChar + ", '" + hopMotCon(idChar, dex) + "')");
    }

    /** Danh sach dex trong Hop PC (CSDL). */
    static List<Integer> dexHop(String du) {
        List<Integer> l = new ArrayList<>();
        if (du == null) return l;
        Matcher m = Pattern.compile(";c\\d+=(\\d+)\\.").matcher(du);
        while (m.find()) l.add(Integer.parseInt(m.group(1)));
        return l;
    }

    static void traoDoi() throws Exception {
        KichBanBatThuong.loiLogMoi();
        BotChucNang.Bot a0 = KichBanBatThuong.nguoi();
        // lang khoi dau co ban sao (Pewter 0 / Dong Pewter 301): hai ben phai cung mot ban do -> lay nguoi choi moi toi khi trung
        BotChucNang.Bot c0 = KichBanBatThuong.nguoi();
        for (int i = 0; i < 6 && c0.mapId != a0.mapId; i++) {
            c0.close();
            c0 = KichBanBatThuong.nguoi();
        }
        lenhGm("slv " + a0.ten + " 25 0"); // trao doi: cap 10 (trao_doi.cap_toi_thieu) + chong clone cap 20
        lenhGm("slv " + c0.ten + " 25 0");
        KichBanBatThuong.datXu(a0, 200000);
        KichBanBatThuong.datXu(c0, 200000);
        int idA = a0.charDb, idC = c0.charDb;
        // Hop PC nap luoi tu CSDL o lan -121 dau tien: ghi truoc lan do (dang nhap lai thi hai ben co the roi vao hai ban sao lang)
        // dung canh: A co 1 Pidgey (16), C co 1 Rattata (19) - Hop PC cu (ngay bat 01/10, chua tung trao doi)
        ganHop(idA, 16);
        ganHop(idC, 19);
        BotChucNang.Bot a = a0;
        BotChucNang.Bot c = c0;
        try {
            int[] ta = trangThai(a), tc = trangThai(c);
            ket("tn_trao_doi.chuan_bi", ta != null && ta[2] == 1 && tc != null && tc[2] == 1, "Hop PC A " + (ta == null ? "?" : ta[2]) + ", C " + (tc == null ? "?" : tc[2])
                    + "; A map " + a.mapId + " o " + a.px / 16 + "," + a.py / 16 + ", C map " + c.mapId + " o " + c.px / 16 + "," + c.py / 16);
            // C di bo lai canh A (trao doi can dung gan nhau)
            BotChucNang.BanDo bdc = BotChucNang.BanDo.nap(c.mapLoad);
            int[] ke = bdc == null ? null : bdc.oGan(a.px / 16 + 1, a.py / 16, 4);
            boolean gan = ke != null && c.diDuong(bdc, ke[0], ke[1]);
            onDinh(c);
            // moi -> dong y
            int mc = c.moc(), mam = a.moc();
            a.gui(-121, (byte) 6, (byte) 0, (short) c.actor);
            c.cho(x -> goiTraoDoi(x, mc, 1) != null, 4000);
            a.bom(600);
            byte[] dong = goiTraoDoi(a, mam, 0);
            ket("tn_trao_doi.loi_moi", goiTraoDoi(c, mc, 1) != null, "C di bo canh A: " + gan + "; C nhan loi moi: " + (goiTraoDoi(c, mc, 1) != null) + " | A: " + a.chuTu(mam)
                    + (dong == null ? "" : " | DONG: " + new String(dong, 2, dong.length - 2, java.nio.charset.StandardCharsets.UTF_8)));
            if (goiTraoDoi(c, mc, 1) == null) return;
            int ma = a.moc();
            c.gui(-121, (byte) 6, (byte) 1, (byte) 1);
            a.cho(x -> goiTraoDoi(x, ma, 2) != null, 4000);
            ket("tn_trao_doi.phien", goiTraoDoi(a, ma, 2) != null, "A mo cua so trao doi: " + (goiTraoDoi(a, ma, 2) != null) + " " + a.chuTu(ma));
            // dat con (id 1 moi ben), khoa
            a.gui(-121, (byte) 6, (byte) 2, (byte) 1, 1);
            c.gui(-121, (byte) 6, (byte) 2, (byte) 1, 1);
            a.bom(500);
            a.gui(-121, (byte) 6, (byte) 3);
            c.gui(-121, (byte) 6, (byte) 3);
            a.bom(800);
            c.bom(300);
            // dang trong trao doi: A tha / cho theo con dang dua -> tu choi
            int mt = a.moc();
            a.gui(-121, (byte) 4, (byte) 3, 1);
            String tuChoi = a.doiChu(mt, 3000, "trao đổi");
            int[] ta2 = trangThai(a);
            ket("tn_trao_doi.tha_khi_dua", tuChoi != null && ta2 != null && ta2[2] == 1, "tha con dang dua: " + tuChoi + ", hop A " + (ta2 == null ? "?" : ta2[2]));
            // xac nhan: A bam dup, C mot lan
            onDinh(a);
            onDinh(c);
            long xa0 = a.xu, xc0 = c.xu;
            int mx = a.moc(), mxc = c.moc();
            a.gui(-121, (byte) 6, (byte) 4);
            a.gui(-121, (byte) 6, (byte) 4);
            c.gui(-121, (byte) 6, (byte) 4);
            a.cho(x -> goiTraoDoi(x, mx, 3) != null, 5000);
            c.cho(x -> goiTraoDoi(x, mxc, 3) != null, 5000);
            onDinh(a);
            onDinh(c);
            String duA = hopCsdl(a), duC = hopCsdl(c);
            int[] ta3 = trangThai(a), tc3 = trangThai(c);
            ket("tn_trao_doi.xong", goiTraoDoi(a, mx, 3) != null && goiTraoDoi(c, mxc, 3) != null && ta3 != null && ta3[2] == 1 && tc3 != null && tc3[2] == 1,
                    "A xong=" + (goiTraoDoi(a, mx, 3) != null) + " C xong=" + (goiTraoDoi(c, mxc, 3) != null) + ", hop A " + (ta3 == null ? "?" : ta3[2]) + " C " + (tc3 == null ? "?" : tc3[2])
                            + " | " + a.chuTu(mx).replaceAll("^(.{300}).*$", "$1"));
            ket("tn_trao_doi.phi", a.xu <= xa0 && c.xu <= xc0 && a.xu >= 0 && c.xu >= 0, "xu A " + xa0 + " -> " + a.xu + ", C " + xc0 + " -> " + c.xu);
            // trao lai ngay con vua nhan: bi chan (khoa 24 gio / nhip giua hai lan trao doi), khong mo phien moi
            int mc2 = c.moc();
            a.gui(-121, (byte) 6, (byte) 0, (short) c.actor);
            c.cho(x -> goiTraoDoi(x, mc2, 1) != null, 4000);
            a.bom(300);
            int ma2 = a.moc();
            c.gui(-121, (byte) 6, (byte) 1, (byte) 1);
            a.cho(x -> goiTraoDoi(x, ma2, 2) != null, 4000);
            List<int[]> hopA = KichBanThuPhuc.hop(a);
            int idMoi = hopA == null || hopA.isEmpty() ? -1 : hopA.get(0)[0];
            int md = a.moc();
            a.gui(-121, (byte) 6, (byte) 2, (byte) 1, idMoi);
            a.bom(1200);
            String khoaGio = null;
            for (int i = md; i < a.su.size(); i++) {
                BotChucNang.SuKien e = a.su.get(i);
                if (e.cmd == -121 && e.d != null && e.d.length > 2 && e.d[0] == 6) {
                    String t = new String(e.d, java.nio.charset.StandardCharsets.UTF_8);
                    if (t.contains("giờ nữa") || t.contains("Vừa trao đổi")) khoaGio = t.replaceAll("[^\\p{L}\\p{N} ,.:]", "");
                }
            }
            ket("tn_trao_doi.khoa_sau_trao", khoaGio != null, "dua lai con vua nhan (id " + idMoi + "): " + (khoaGio == null ? a.chuTu(md) : khoaGio.trim()));
            a.gui(-121, (byte) 6, (byte) 5);
            a.bom(500);
            // CSDL sau khi thoat: A co Rattata, C co Pidgey, dung 1 con moi ben (khong nhan ban)
            a.close();
            c.close();
            Thread.sleep(4000);
            duA = BotChucNang.sql1("SELECT du_lieu FROM tob_hop_pc WHERE id_char = " + idA);
            duC = BotChucNang.sql1("SELECT du_lieu FROM tob_hop_pc WHERE id_char = " + idC);
            String log = BotChucNang.sql1("SELECT COUNT(*) FROM tob_log_trao_doi_pokemon WHERE (id_a = " + idA + " AND id_b = " + idC + ") OR (id_a = " + idC + " AND id_b = " + idA + ")");
            ket("tn_trao_doi.csdl", dexHop(duA).equals(Arrays.asList(19)) && dexHop(duC).equals(Arrays.asList(16)),
                    "A " + dexHop(duA) + ", C " + dexHop(duC) + ", nhat ky " + log);
            long dexA = Long.parseLong(BotChucNang.sql1("SELECT COUNT(*) FROM tob_pokedex WHERE id_char=" + idA + " AND so_bat > 0").trim());
            ket("tn_trao_doi.pokedex", dexA >= 1, "A Pokedex da bat (nhan qua trao doi): " + dexA + " loai");
        } finally {
            a.close();
            c.close();
        }
        ket("tn_trao_doi.may_chu", KichBanBatThuong.mayChuSong(), "nguoi choi khac van vao game + lam moi");
        KichBanBatThuong.kiemLog("tn_trao_doi");
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

    static final List<String> MAC_DINH = new ArrayList<>();

    static void dangKy() {
        dk("tn_bat", KichBanTinhNangThuong::batPokemon);
        dk("tn_nha_tre", KichBanTinhNangThuong::nhaTre);
        dk("tn_khu", KichBanTinhNangThuong::doiKhu);
        dk("tn_ngay_moi", KichBanTinhNangThuong::ngayMoi);
        dk("tn_o_thu", KichBanTinhNangThuong::veOThu);
        dk("tn_cho", KichBanTinhNangThuong::choHaiNguoi);
        dk("tn_bang", KichBanTinhNangThuong::bangHoi);
        dk("tn_trao_doi", KichBanTinhNangThuong::traoDoi);
        MAC_DINH.addAll(Arrays.asList("tn_bat", "tn_trao_doi", "tn_nha_tre", "tn_khu", "tn_o_thu", "tn_cho", "tn_bang", "tn_ngay_moi"));
    }

    public static void main(String[] a) throws Exception {
        String log = System.getProperty("log", "");
        if (!log.isEmpty()) {
            KichBanBatThuong.tepLog = new File(log);
            KichBanBatThuong.viTri = KichBanBatThuong.tepLog.isFile() ? KichBanBatThuong.tepLog.length() : 0;
        }
        dangKy();
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(MAC_DINH);
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        try {
            BotChucNang.main(b);
        } finally {
            if (KichBanBatThuong.gm != null) KichBanBatThuong.gm.close();
        }
    }
}
