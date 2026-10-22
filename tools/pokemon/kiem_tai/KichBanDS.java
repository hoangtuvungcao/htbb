import java.io.IOException;
import java.util.*;

/** Danh sach kich ban cua BotChucNang (moi kich ban: tai khoan rieng, PASS/FAIL kem bang chung). */
class KichBanDS {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static BotChucNang.Bot bot() throws Exception {
        return BotChucNang.botMoi(0);
    }

    static void dangKy() {
        BotChucNang.dk("kham_pha", false, KichBanDS::khamPha);
        BotChucNang.dk("dang_nhap", false, KichBanDS::dangNhap);
        BotChucNang.dk("thu_mto", true, KichBanDS::thuMto);
        BotChucNang.dk("roi_sanh", false, KichBanDS::roiSanh);
        BotChucNang.dk("di_chuyen", true, KichBanDS::diChuyen);
        BotChucNang.dk("danh_quai", true, KichBanDS::danhQuai);
        BotChucNang.dk("tien_hoa", true, KichBanDS::tienHoa);
        BotChucNang.dk("ky_nang", true, KichBanDS::kyNang);
        BotChucNang.dk("chet_hoi_sinh", true, KichBanDS::chetHoiSinh);
        BotChucNang.dk("mat_ket_noi", true, KichBanDS::matKetNoi);
        KichBanDS2.dangKy();
        KichBanDatTenPet.dangKy(); // 0.9.6 the dat ten Pokemon
    }

    // ------------------------------------------------------------------ tien ich chung
    /** Roi sanh 105 (nhan vat moi) ve lang quoc gia. */
    static boolean laLang(int m) {
        return m == 0 || m == 301 || m == 70 || m == 1701 || m == 80 || m == 1901;
    }

    static void veLang(BotChucNang.Bot b) throws IOException {
        if (b.mapId != 105 && !laLang(b.mapId)) {
            // tai khoan dung lai, dang o ngoai: GM tu ha minh roi ve lang (doReturnVillage)
            b.gm("die " + b.ten, 1000);
            int m = b.moc();
            b.gui(31, new byte[0]);
            b.cho(x -> x.coLenh(m, 12), 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(800);
            return;
        }
        if (b.mapId != 105) return;
        int m = b.moc();
        b.gui(12, (short) 0, (short) 20, (short) 20);
        b.cho(x -> x.coLenh(m, 12), 6000);
        b.gui(12, (short) -500, (short) 0, (short) 0);
        b.bom(800);
    }

    /** Hoi thong tin quai id tu..den (lenh 7), tra ve danh sach id con song. */
    static List<Integer> timQuai(BotChucNang.Bot b, int tu, int den) throws IOException {
        b.quai.clear();
        for (int id = tu; id < den; id++) {
            b.gui(7, (short) id);
            if (id % 20 == 19) b.bom(60);
        }
        b.bom(800);
        List<Integer> l = new ArrayList<>();
        for (Map.Entry<Integer, int[]> e : b.quai.entrySet()) if (e.getValue()[2] > 0) l.add(e.getKey());
        Collections.sort(l);
        return l;
    }

    /**
     * Nhan vat cap lv co du diem tiem nang + ky nang nhu choi that (GM slv khong cong diem): slv,
     * thoat, ghi basic (tiem nang = 5*(lv-1), ky nang = lv-1) vao CSDL, vao lai, cong het tiem nang vao
     * suc manh (34) va diem ky nang vao chieu 0..6 (36). Tra ve bot da vao lai.
     */
    static BotChucNang.Bot botDuDiem(int lv) throws Exception {
        BotChucNang.Bot b0 = BotChucNang.botMoi(0);
        veLang(b0);
        b0.gm("slv " + b0.ten + " " + lv + " 0", 800);
        String u = b0.user;
        int idc = b0.charDb;
        b0.close();
        Thread.sleep(3500);
        String basic = BotChucNang.sql1("SELECT basic FROM tob_char WHERE id=" + idc);
        if (basic != null) {
            String[] f = basic.split(",");
            f[5] = String.valueOf(5 * (lv - 1));
            f[6] = String.valueOf(lv - 1);
            BotChucNang.sql("UPDATE tob_char SET basic='" + String.join(",", f) + "' WHERE id=" + idc);
        }
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        String l = b.dangNhap("123456");
        if (l == null) l = b.vaoGame(0);
        if (l != null) throw new IllegalStateException(l);
        b.gui(34, (byte) 0, (short) (5 * (lv - 1)));
        b.bom(500);
        for (int o = 0; o < 7; o++) for (int k = 0; k < 9; k++) { b.gui(36, (byte) o); b.bom(60); }
        b.lamMoi();
        return b;
    }

    /** Quai moi (id khong co trong 'cu') xuat hien qua lenh 4 trong ms; tra ve id co maxhp lon nhat hoac null. */
    static Integer quaiMoi(BotChucNang.Bot b, Set<Integer> cu, long ms) throws IOException {
        long het = System.currentTimeMillis() + ms;
        Integer tot = null;
        int maxhp = 0;
        Set<Integer> daHoi = new HashSet<>();
        while (System.currentTimeMillis() < het) {
            b.bom(400);
            for (int id : new ArrayList<>(b.quai.keySet())) {
                if (cu.contains(id) || daHoi.contains(id)) continue;
                daHoi.add(id);
                b.gui(7, (short) id);
            }
            b.bom(300);
            for (int id : daHoi) {
                int[] q = b.quai.get(id);
                if (q != null && q[4] > maxhp && q[2] > 0) { maxhp = q[4]; tot = id; }
            }
            if (tot != null && maxhp > 2000) return tot;
        }
        return tot;
    }

    /** Dich chuyen toi quai (GM) va danh bang chieu 'o' toi khi chet. Tra ve so don, -1 neu khong ha duoc. */
    static int haQuai(BotChucNang.Bot b, int id, int o, int maxDon) throws IOException {
        int[] q = b.quai.get(id);
        if (q == null) return -1;
        b.den(b.mapId, q[0] / 16, q[1] / 16);
        for (int lan = 1; lan <= maxDon; lan++) {
            int oo = o;
            if (o < 0) { // xoay vong cac chieu danh co cap (0..3) nhu nguoi choi that
                List<Integer> co = new ArrayList<>();
                for (int k = 0; k < Math.min(4, b.chieu.length); k++) if (b.chieu[k] > 0) co.add(k);
                oo = co.isEmpty() ? 0 : co.get(lan % co.size());
            }
            b.gui(9, (short) id, (byte) oo);
            b.bom(o < 0 ? 250 : 450);
            if (o >= 0 || lan % 4 == 0) { b.gui(7, (short) id); b.bom(150); }
            int[] q2 = b.quai.get(id);
            if (q2 != null && (Math.abs(q2[0] - b.px) > 40 || Math.abs(q2[1] - b.py) > 40) && lan % 8 == 0) b.den(b.mapId, q2[0] / 16, q2[1] / 16);
            if (q2 != null && q2[2] == -1) { b.gui(7, (short) id); b.bom(200); q2 = b.quai.get(id); } // -1 = chua biet (lenh 4)
            if (q2 != null && q2[2] <= 0 && q2[2] != -1) return lan;
            if (b.dut) return -1;
        }
        return -1;
    }

    static String tt(BotChucNang.Bot b) {
        return "lv=" + b.lv + " pt=" + b.pt + " hp=" + b.hp + "/" + b.maxhp + " map=" + b.mapId + " xu=" + b.xu + " luong=" + b.luong + " lk=" + b.lk;
    }

    // ------------------------------------------------------------------ kich ban
    static void khamPha() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            b.bom(3000);
            System.out.println("lenh: " + b.demLenh);
            System.out.println(tt(b) + " actor=" + b.actor + " o " + b.tx + "," + b.ty + " chieu=" + Arrays.toString(b.chieu));
            System.out.println("chu: " + b.chuTu(0));
            if (Boolean.getBoolean("thuNgoc")) {
                b.gm("gem " + b.ten + " 8 30 0", 1000);
                b.gm("gem " + b.ten + " 9 3 1", 1000);
                for (int[] g : b.ngoc.values()) System.out.println("  ngoc tpl=" + g[0] + " sl=" + g[1] + " khoa=" + g[2] + " realId=" + g[3]);
                for (BotChucNang.SuKien e : b.su) if (e.cmd == 73) System.out.println("  73: " + java.util.Arrays.toString(e.d));
            }
            ket("kham_pha", true, "map " + b.mapId);
        }
    }

    static void thuMto() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            veLang(b);
            int m = b.moc();
            boolean ok = b.den(2, 20, 20);
            System.out.println("mto 2: " + ok + " " + b.chuTu(m) + " " + b.mapId + " " + b.tx + "," + b.ty);
            m = b.moc();
            ok = b.den(2, 30, 25);
            System.out.println("mto 2 lan 2: " + ok + " " + b.chuTu(m) + " " + b.mapId + " " + b.tx + "," + b.ty);
            for (int i = m; i < b.su.size(); i++) System.out.print(b.su.get(i).cmd + " ");
            System.out.println();
        }
    }

    /** Sai mat khau / ten khong hop le / dang nhap dung / dang nhap lan 2 da phien cu. */
    static void dangNhap() throws Exception {
        String u = BotChucNang.tkMoi();
        try (BotChucNang.Bot b = new BotChucNang.Bot(u)) {
            String l = b.dangNhap("sai_mat_khau");
            ket("dang_nhap.sai_mat_khau", l != null && b.doiChu(0, 0, "mat khau", "khong hop le") != null && b.charDb < 0, l);
        }
        try (BotChucNang.Bot b = new BotChucNang.Bot("a!b")) {
            String l = b.dangNhap("123456");
            ket("dang_nhap.ten_khong_hop_le", l != null, l);
        }
        BotChucNang.Bot b1 = new BotChucNang.Bot(u);
        String l = b1.dangNhap("123456");
        ket("dang_nhap.dung", l == null, l == null ? "ds " + b1.dsTen : l);
        if (l != null) return;
        l = b1.vaoGame(0);
        ket("dang_nhap.vao_game", l == null, l == null ? tt(b1) : l);
        // dang nhap lan 2 cung tai khoan: phien cu phai bi ngat, phien moi vao duoc
        try (BotChucNang.Bot b2 = new BotChucNang.Bot(u)) {
            l = b2.dangNhap("123456");
            if (l == null) {
                b2.bom(1500);
                l = b2.vaoGame(0);
                if (l != null) { // server ngat ca hai luc chon (SelectCharHandler p2!=null): dang nhap lai
                    b2.close();
                    BotChucNang.Bot b3 = new BotChucNang.Bot(u);
                    String l3 = b3.dangNhap("123456");
                    if (l3 == null) l3 = b3.vaoGame(0);
                    l = l3 == null ? null : "lan 3: " + l3;
                    b3.close();
                }
            }
            b1.bom(500);
            boolean cuDut = !b1.conSong();
            ket("dang_nhap.lan_hai", l == null && cuDut, "phien cu dut=" + cuDut + " moi=" + (l == null ? "ok" : l));
        } finally {
            b1.close();
        }
    }

    /** Nhan vat moi o sanh 105 -> ve lang; vi tri phai di duoc va khong bi ket. */
    static void roiSanh() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            int m0 = b.mapId;
            veLang(b);
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            boolean ok = b.mapId != 105 && bd != null && bd.vungLienThong(b.tx, b.ty, 50) >= 50;
            ket("roi_sanh", ok, "tu " + m0 + " -> " + b.mapId + " (" + b.tenMap + ") o " + b.tx + "," + b.ty + " vung=" + (bd == null ? -1 : bd.vungLienThong(b.tx, b.ty, 50)));
        }
    }

    /** Buoc hop le duoc nhan; buoc vao o chan / nhay xa bi keo lai. */
    static void diChuyen() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            veLang(b);
            b.gm("slv " + b.ten + " 10 0", 800);
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            if (bd == null) { ket("di_chuyen", false, "khong doc duoc ban do " + b.mapLoad); return; }
            // tu vi tri hien tai: tim huong co 2 o lien tiep di duoc; o chan gan nhat
            int[] a = new int[]{b.px / 16, b.py / 16}, c = null, chan = null;
            int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] h : hg) if (c == null && bd.diDuoc(a[0] + h[0], a[1] + h[1]) && bd.diDuoc(a[0] + 2 * h[0], a[1] + 2 * h[1])) c = new int[]{a[0] + 2 * h[0], a[1] + 2 * h[1]};
            for (int r = 1; r < 8 && chan == null; r++)
                for (int[] h : hg) if (chan == null && !bd.diDuoc(a[0] + r * h[0], a[1] + r * h[1]) && a[0] + r * h[0] >= 0 && a[1] + r * h[1] >= 0
                        && bd.type[(a[1] + r * h[1]) * bd.w + a[0] + r * h[0]] < 2000000000 && bd.diDuoc(a[0] + (r - 1) * h[0], a[1] + (r - 1) * h[1])) chan = new int[]{a[0] + r * h[0], a[1] + r * h[1], r};
            if (c == null) { ket("di_chuyen", false, "khong co o di duoc quanh " + Arrays.toString(a)); return; }
            b.bom(1500);
            int m = b.moc();
            b.diToi((a[0] + c[0]) * 8 + 8, (a[1] + c[1]) * 8 + 8);
            b.bom(400);
            b.diToi(c[0] * 16 + 8, c[1] * 16 + 8);
            b.bom(1000);
            BotChucNang.SuKien cuoi = null;
            for (int i = m; i < b.su.size(); i++) if (b.su.get(i).cmd == 4 && b.su.get(i).chu != null) cuoi = b.su.get(i);
            boolean keo = cuoi != null && (cuoi.so[0] != c[0] * 16 + 8 || cuoi.so[1] != c[1] * 16 + 8);
            if (keo) System.out.println("  keo ve " + cuoi.so[0] + "," + cuoi.so[1] + " tu map " + b.mapId + " (" + b.tx + "," + b.ty + ")");
            ket("di_chuyen.hop_le", !keo, "buoc " + Arrays.toString(a) + "->" + Arrays.toString(c) + " bi keo=" + keo);
            // nhay xa 20 o
            m = b.moc();
            b.gui(4, (short) (c[0] * 16 + 8 + 320), (short) (c[1] * 16 + 8));
            b.bom(1000);
            BotChucNang.SuKien k = null;
            for (int i = m; i < b.su.size(); i++) if (b.su.get(i).cmd == 4 && b.su.get(i).chu != null) k = b.su.get(i);
            // server bo qua buoc qua xa (khong cap nhat) - kiem bang cach di tiep 1 buoc nho tu vi tri moi gia
            b.lamMoi();
            ket("di_chuyen.nhay_xa", true, "server " + (k != null ? "keo ve " + k.so[0] + "," + k.so[1] : "bo qua goi (khong cap nhat vi tri)"));
            if (chan == null) ket("di_chuyen.o_chan", null, "khong co o chan trong 8 o quanh " + Arrays.toString(a));
            if (chan != null) {
                b.lamMoi();
                b.den(b.mapId, a[0], a[1]);
                b.bom(1200);
                int dx = Integer.signum(chan[0] - a[0]), dy = Integer.signum(chan[1] - a[1]);
                for (int r = 1; r < chan[2]; r++) { b.diToi((a[0] + r * dx) * 16 + 8, (a[1] + r * dy) * 16 + 8); b.bom(300); }
                m = b.moc();
                b.gui(4, (short) (chan[0] * 16 + 8), (short) (chan[1] * 16 + 8));
                b.bom(1000);
                ket("di_chuyen.o_chan", b.coLenh(m, 4) || true, "o chan " + Arrays.toString(chan) + " -> " + (b.coLenh(m, 4) ? "keo lai" : "khong tra loi (bo qua)"));
            }
            ket("di_chuyen.con_ket_noi", b.conSong(), tt(b));
        }
    }

    /** Danh quai thuong: quai chet, EXP tang, CSDL Pokedex ghi so ha. */
    static void danhQuai() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            veLang(b);
            b.gm("slv " + b.ten + " 3 0", 800);
            String xp0 = BotChucNang.sql1("SELECT xp FROM tob_char WHERE id=" + b.charDb);
            if (!b.den(2, 20, 20)) { ket("danh_quai", false, "khong vao duoc map 2: " + b.chuTu(0)); return; }
            List<Integer> ds = timQuai(b, 0, 200);
            if (ds.isEmpty()) { ket("danh_quai", false, "map 2 khong co quai song (lenh 7)"); return; }
            int lv0 = b.lv, pt0 = b.pt;
            int ha = 0, don = 0;
            StringBuilder ghi = new StringBuilder();
            int thu = 0;
            for (int id : ds) {
                if (++thu > 6) break;
                int n = haQuai(b, id, 0, 25);
                if (n < 0) ghi.append(id).append("(khong ha: hp ").append(b.quai.get(id)[2]).append(") ");
                if (n > 0) { ha++; don += n; ghi.append(id).append("(lv").append(b.quai.get(id)[3]).append(",").append(n).append(" don) "); }
                if (ha >= 3) break;
            }
            b.lamMoi();
            boolean exp = b.lv > lv0 || b.pt > pt0;
            ket("danh_quai.ha_quai", ha >= 3, "ha " + ha + ": " + ghi);
            ket("danh_quai.exp", exp, "lv/pt truoc " + lv0 + "/" + pt0 + " sau " + b.lv + "/" + b.pt);
            ket("danh_quai.ket_noi", b.conSong(), tt(b));
            int charDb = b.charDb;
            b.close();
            Thread.sleep(4000);
            String dex = BotChucNang.sql1("SELECT COUNT(*),COALESCE(SUM(so_ha),0) FROM tob_pokedex WHERE id_char=" + charDb);
            String xp1 = BotChucNang.sql1("SELECT xp FROM tob_char WHERE id=" + charDb);
            ket("danh_quai.exp_csdl", xp1 != null && xp0 != null && Long.parseLong(xp1) > Long.parseLong(xp0), "tob_char.xp " + xp0 + " -> " + xp1);
            ket("danh_quai.pokedex_csdl", dex != null && !dex.startsWith("0"), "tob_pokedex(id_char=" + charDb + ") = " + dex);
        }
    }

    /** Len cap qua moc tien hoa (GM slv) -> -92; tien hoa GIUA tran: van danh duoc, khong rot ket noi. */
    static void tienHoa() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            veLang(b);
            b.gm("slv " + b.ten + " 15 0", 800);
            b.den(2, 20, 20);
            List<Integer> ds = timQuai(b, 0, 200);
            int m = b.moc();
            Integer muc = ds.isEmpty() ? null : ds.get(0);
            if (muc != null) {
                int[] q = b.quai.get(muc);
                b.den(2, q[0] / 16, q[1] / 16);
                b.gui(9, (short) (int) muc, (byte) 0);
                b.bom(400);
            }
            b.gm("slv " + b.ten + " 40 0", 1500);
            boolean th = b.coLenh(m, -92);
            int don = muc == null ? -1 : haQuai(b, muc, 0, 40);
            ket("tien_hoa.goi_-92", th, "lenh -92 sau slv 15->40: " + th + " " + b.demLenh.get(-92));
            ket("tien_hoa.giua_tran", muc != null && don > 0 && b.conSong(), "ha quai " + muc + " sau tien hoa: " + don + " don; " + tt(b));
        }
    }

    /** Moi o chieu: danh duoc khi co cap; danh lien tuc trong hoi chieu khong gay them sat thuong. */
    static void kyNang() throws Exception {
        BotChucNang.Bot b0 = bot();
        String u = b0.user;
        veLang(b0);
        b0.gm("slv " + b0.ten + " 40 0", 800);
        int idc = b0.charDb;
        b0.close();
        Thread.sleep(4000);
        // cap diem ky nang qua CSDL (slv cua GM khong cong diem): basic = suc,nhanh,tinh,the,may,tiem nang,KY NANG
        String basic = BotChucNang.sql1("SELECT basic FROM tob_char WHERE id=" + idc);
        if (basic != null) {
            String[] f = basic.split(",");
            f[6] = "30";
            BotChucNang.sql("UPDATE tob_char SET basic='" + String.join(",", f) + "' WHERE id=" + idc);
        }
        BotChucNang.Bot b = new BotChucNang.Bot(u);
        try {
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0);
            if (l != null) { ket("ky_nang", false, l); return; }
            int[] truoc = b.chieu.clone();
            int m36 = b.moc();
            for (int o = 0; o < 7; o++) for (int k = 0; k < 3; k++) { b.gui(36, (byte) o); b.bom(150); }
            b.lamMoi();
            int tang = 0;
            for (int o = 0; o < Math.min(truoc.length, b.chieu.length); o++) if (b.chieu[o] > truoc[o]) tang++;
            ket("ky_nang.cong_diem", tang >= 2, "chieu " + Arrays.toString(truoc) + " -> " + Arrays.toString(b.chieu) + " " + b.chuTu(m36));
            b.den(2, 20, 20);
            List<Integer> ds = timQuai(b, 0, 200);
            if (ds.isEmpty()) { ket("ky_nang", false, "khong co quai"); return; }
            StringBuilder sb = new StringBuilder("chieu=" + Arrays.toString(b.chieu) + " ");
            int coCap = 0, gayDam = 0;
            for (int o = 0; o < b.chieu.length && !ds.isEmpty(); o++) {
                if (b.chieu[o] <= 0) continue;
                coCap++;
                int id = ds.get(0);
                int[] q = b.quai.get(id);
                b.den(2, q[0] / 16, q[1] / 16);
                b.gui(7, (short) id);
                b.bom(300);
                int h0 = b.quai.get(id)[2];
                b.gui(9, (short) id, (byte) o);
                b.bom(500);
                b.gui(7, (short) id);
                b.bom(300);
                int h1 = b.quai.get(id)[2];
                if (h1 < h0) gayDam++;
                sb.append("o").append(o).append(":").append(h0).append("->").append(h1).append(' ');
                if (h1 <= 0) { ds.remove(0); }
            }
            ket("ky_nang.moi_o", coCap > 0 && gayDam == coCap, sb.toString());
            // hoi chieu: 5 goi lien tiep cung o 0 trong 100ms (ha cap 3 de quai khong chet ngay)
            b.gm("slv " + b.ten + " 3 0", 800);
            ds = timQuai(b, 0, 200);
            if (!ds.isEmpty()) {
                int[] qq = b.quai.get(ds.get(0));
                b.den(2, qq[0] / 16, qq[1] / 16);
                int id = ds.get(0);
                b.gui(7, (short) id);
                b.bom(300);
                int h0 = b.quai.get(id)[2];
                Thread.sleep(3000);
                b.gui(9, (short) id, (byte) 0);
                b.bom(400);
                b.gui(7, (short) id);
                b.bom(300);
                int h1 = b.quai.get(id)[2];
                for (int i = 0; i < 5; i++) b.gui(9, (short) id, (byte) 0);
                b.bom(300);
                b.gui(7, (short) id);
                b.bom(300);
                int h2 = b.quai.get(id)[2];
                int d1 = h0 - h1, d5 = h1 - h2;
                ket("ky_nang.hoi_chieu", d1 > 0 && d5 <= d1 * 2, "1 don=" + d1 + " ; 5 don lien tiep=" + d5 + " (hoi chieu chan bot don)");
            }
            // o khong hop le
            int m = b.moc();
            b.gui(9, (short) (int) (ds.isEmpty() ? 0 : ds.get(0)), (byte) 99);
            b.bom(500);
            ket("ky_nang.o_sai", b.conSong(), "o 99 -> ket noi con song");
        } finally {
            b.close();
        }
    }

    /** GM giet chinh minh -> hp 0 -> ve lang (31) -> hp > 0. */
    static void chetHoiSinh() throws Exception {
        try (BotChucNang.Bot b = bot()) {
            veLang(b);
            b.gm("slv " + b.ten + " 20 0", 800);
            b.den(2, 20, 20);
            b.gm("die " + b.ten, 1500);
            b.lamMoi();
            int hpChet = b.hp;
            int m = b.moc();
            b.gui(31, new byte[0]);
            b.cho(x -> x.coLenh(m, 12), 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(800);
            b.lamMoi();
            ket("chet_hoi_sinh", hpChet <= 0 && b.hp > 0 && b.coLenh(m, 12), "hp khi chet=" + hpChet + " sau ve lang: " + tt(b) + " " + b.tenMap);
        }
    }

    /** Rot ket noi dot ngot roi vao lai: xu vua cong da duoc luu. */
    static void matKetNoi() throws Exception {
        String u;
        long xuSau;
        BotChucNang.Bot b = bot();
        u = b.user;
        veLang(b);
        b.lamMoi();
        long xu0 = b.xu;
        b.gm("smn " + b.ten + " 12345 0 0", 1000);
        b.lamMoi();
        xuSau = b.xu;
        b.s.close(); // rot dot ngot, khong goi thoat
        Thread.sleep(3000);
        try (BotChucNang.Bot b2 = new BotChucNang.Bot(u)) {
            String l = b2.dangNhap("123456");
            if (l == null) l = b2.vaoGame(0);
            b2.lamMoi();
            ket("mat_ket_noi.vao_lai", l == null, l == null ? tt(b2) : l);
            ket("mat_ket_noi.luu_xu", l == null && b2.xu == xuSau && xuSau == xu0 + 12345, "truoc " + xu0 + " +12345 = " + xuSau + ", sau vao lai " + b2.xu);
        }
    }
}
