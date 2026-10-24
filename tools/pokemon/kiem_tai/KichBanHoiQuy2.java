import java.io.*;
import java.util.*;

/** Kich ban hoi quy nguoi choi that (phan 2): kinh te, thu, xa hoi, PvP, khoi dong lai. Xem KichBanHoiQuy. */
final class KichBanHoiQuy2 {

    private KichBanHoiQuy2() {
    }

    static void ket(String muc, Boolean ok, String bang) {
        HoiQuy.ket(muc, ok, bang);
    }

    static void dangKy() {
        HoiQuy.dk("ve_dich_chuyen", "Ve dich chuyen: mua o Kecleon bang xu, dung ngoai lang -> ve lang, hoi chieu, khong tru hai ve", KichBanHoiQuy2::veDichChuyen);
        HoiQuy.dk("thu_cuoi", "Thu / thu cuoi: mua Sao goi Ponyta (luong), dung tu tui, cuoi / xuong, khoa, tha", KichBanHoiQuy2::thuCuoi);
        HoiQuy.dk("giao_dich", "giao dich hai nguoi choi: thuoc + xu bao toan, huy giua chung khong mat", KichBanHoiQuy2::giaoDich);
        HoiQuy.dk("chat_ban_be", "chat ban do, tin rieng", KichBanHoiQuy2::chat);
        HoiQuy.dk("pvp_an_toan", "PvP: trong lang khong ai mat HP", KichBanHoiQuy2::pvpAnToan);
        HoiQuy.dk("toc_do_client", "(loi 24/10: cham nhu slow-motion, giat lui khi danh tay) - kiem client cua nhanh sua-slow-motion", KichBanHoiQuy2::tocDoClient);
        HoiQuy.dk("khoi_dong_lai", "luu qua khoi dong lai may chu (-Dkhoidonglai): cap, tien, vat pham, huong dan khong moi lai", KichBanHoiQuy2::khoiDongLai);
    }

    // ================================================================== danh quai bang di bo (khong GM)
    /** Di bo toi canh quai roi danh (lenh 9, xoay chieu) toi khi chet; tra ve so don hoac -1. */
    static int haQuaiDiBo(BotChucNang.Bot b, int id, int maxDon) throws IOException {
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        KichBanHuongDan.dongBoViTri(b);
        for (int lan = 1; lan <= maxDon; lan++) {
            int[] q = b.quai.get(id);
            if (q != null && bd != null && (Math.abs(q[0] - b.px) > 40 || Math.abs(q[1] - b.py) > 40) && lan % 5 == 1) {
                int[] o = bd.oGan(q[0] / 16, q[1] / 16, 2);
                if (o != null) b.diDuong(bd, o[0], o[1]);
            }
            b.gui(9, (short) id, (byte) KichBanGymLang.chieuCo(b, lan));
            b.bom(280);
            if (lan % 4 == 0) {
                b.quai.remove(id);
                b.gui(7, (short) id);
                b.bom(250);
                if (!b.quai.containsKey(id)) { b.bom(400); if (!b.quai.containsKey(id)) return lan; } // quai da roi ban do (chet / boss rut)
            }
            int[] q2 = b.quai.get(id);
            if (q2 != null && q2[2] == 0) return lan;
            if (lan % 15 == 0) { b.lamMoi(); if (b.hp > 0 && b.hp < b.maxhp / 3) { b.gui(22, (byte) 1); b.bom(200); } }
            if (b.dut || b.hp <= 0 && lan > 3) return -1;
        }
        return -1;
    }

    /** Dem tong vat pham "nhin thay": thuoc (o < 163 co ten trong data_potion) + do + ngoc. */
    static Map<String, Integer> tui(BotChucNang.Bot b) {
        Map<String, Integer> m = new TreeMap<>();
        for (Map.Entry<Integer, Integer> e : b.thuoc.entrySet()) if (e.getValue() > 0) m.put("thuoc" + e.getKey(), e.getValue());
        for (int[] d : b.doDung) m.merge("do" + d[1], 1, Integer::sum);
        for (int[] g : b.ngoc.values()) m.merge("ngoc" + g[0] + (g[2] == 1 ? "k" : ""), g[1], Integer::sum);
        return m;
    }

    static Map<String, Integer> khac(Map<String, Integer> a, Map<String, Integer> c) {
        Map<String, Integer> d = new TreeMap<>();
        Set<String> k = new TreeSet<>(a.keySet());
        k.addAll(c.keySet());
        for (String x : k) { int v = c.getOrDefault(x, 0) - a.getOrDefault(x, 0); if (v != 0) d.put(x, v); }
        return d;
    }

    /** O thuoc {@code o} co hien o client khong (data_potion co ten, o < so o client 0.9.17 = 163). */
    static boolean oThuocHien(int o) {
        if (o >= 163) return false;
        String t = BotChucNang.sql1("SELECT name FROM data_potion WHERE id=" + o);
        return t != null && !t.trim().isEmpty() && !t.startsWith("LOI SQL");
    }

    // ================================================================== shop dac biet
    static void shopDacBiet() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("sd", 0, 0);
        String u = b.user;
        KichBanHoiQuy.boQuaHuongDan(b);
        HoiQuy.thoat(b);
        HoiQuy.capTien(u, 120000, 0);
        HoiQuy.capLuong(u, 30);
        String ma = HoiQuy.giftcode("LUONGKHOA:30"); // giftcode.gia_tri_toi_da = 30
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            BatBien v = BatBien.of(c);
            v.choPhep("luong", 3000);
            c.lamMoi();
            String t = KichBanGiftcode.nhap(c, ma, "thanh cong", "kich hoat", "khong");
            c.lamMoi();
            ket("shop_dac_biet.giftcode_luong_khoa", c.lk >= 30, t + " | lk " + c.lk);
            // {id shop, loai tien 0 luong / 1 xu / 2 luong khoa, gia}
            int[][] mua = {{2, 2, 20}, {8, 1, 50000}, {14, 0, 10}};
            String[] tenTien = {"luong", "xu", "luong khoa"};
            for (int[] mu : mua) {
                c.lamMoi();
                long xu0 = c.xu;
                int l0 = c.luong, k0 = c.lk;
                Map<String, Integer> t0 = tui(c);
                int m = c.moc();
                c.gui(85, (byte) mu[0]);
                c.bom(1800);
                c.lamMoi();
                Map<String, Integer> d = khac(t0, tui(c));
                long dXu = xu0 - c.xu;
                int dL = l0 - c.luong, dK = k0 - c.lk;
                long[] tru = {dL, dXu, dK};
                boolean dungLoai = tru[mu[1]] == mu[2];
                boolean chiMot = true;
                for (int i = 0; i < 3; i++) if (i != mu[1] && tru[i] != 0) chiMot = false;
                String chu = c.chuTu(m);
                ket("shop_dac_biet.tru_dung_tien.id" + mu[0], dungLoai && chiMot, "mong -" + mu[2] + " " + tenTien[mu[1]] + "; that: luong -" + dL + ", xu -" + dXu + ", lk -" + dK + " | " + chu);
                boolean coHien = false;
                List<String> an = new ArrayList<>();
                for (Map.Entry<String, Integer> e : d.entrySet()) {
                    if (e.getValue() <= 0) continue;
                    if (e.getKey().startsWith("thuoc")) {
                        int o = Integer.parseInt(e.getKey().substring(5));
                        if (oThuocHien(o)) coHien = true; else an.add("o thuoc " + o);
                    } else coHien = true;
                }
                ket("shop_dac_biet.vao_o_hien.id" + mu[0], coHien && an.isEmpty(), "tui doi: " + d + (an.isEmpty() ? "" : " | O AN: " + an));
                // nhan tien trong cau bao (loi 24/10: luong khoa hien thanh luong)
                if (mu[1] == 2) ket("shop_dac_biet.nhan_luong_khoa.id" + mu[0], !chu.contains("lượng") || chu.contains("lượng khoá") || chu.contains("lượng khóa"), chu);
            }
            // khong du tien: khong tru, khong vat pham
            c.lamMoi();
            Map<String, Integer> t1 = tui(c);
            int l1 = c.luong;
            for (int i = 0; i < 5; i++) { c.gui(85, (byte) 14); c.bom(400); }
            c.bom(1000);
            c.lamMoi();
            int duoc = (l1 - c.luong) / 10;
            Map<String, Integer> d2 = khac(t1, tui(c));
            int nhan = 0;
            for (int v2 : d2.values()) if (v2 > 0) nhan += v2;
            ket("shop_dac_biet.het_tien_khong_tru_oan", c.luong >= 0 && nhan >= duoc && duoc <= l1 / 10, "luong " + l1 + " -> " + c.luong + ", mua duoc " + duoc + ", tui +" + d2);
        }
    }

    // ================================================================== giftcode
    static void giftcode() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("gc", 0, 0);
        String u = b.user;
        KichBanHoiQuy.boQuaHuongDan(b);
        HoiQuy.thoat(b);
        HoiQuy.capTien(u, 400000, 0);
        String ma = HoiQuy.giftcode("ITEM:714:2,LUONGKHOA:5");
        String ma2 = HoiQuy.giftcode("ITEM:714:2");
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.lamMoi();
            int lk0 = c.lk;
            Map<String, Integer> t0 = tui(c);
            String t = KichBanGiftcode.nhap(c, ma, "thanh cong", "kich hoat", "khong");
            c.lamMoi();
            ket("giftcode.nhan", t.contains("thành công") && c.lk == lk0 + 5, t + " | tui " + khac(t0, tui(c)) + " lk " + lk0 + "->" + c.lk);
            String t2 = KichBanGiftcode.nhap(c, ma, "da nhap", "thanh cong");
            ket("giftcode.nhap_lai", t2.contains("đã nhập"), t2);
            // lam day tui bang cach MUA do o tiem (duong that), roi nhap ma2: khong mat qua
            if (!KichBanDS2.vaoNha(c, 103)) { ket("giftcode.vao_tiem", false, "map " + c.mapId); return; }
            int truoc = c.doDung.size(), lan = 0;
            while (lan++ < 60) {
                int n = c.doDung.size();
                c.gui(24, (byte) 1, (byte) 3, (short) 79, (short) 1, (byte) 0);
                c.bom(350);
                if (lan % 5 == 0) { c.lamMoi(); if (c.doDung.size() == n) break; }
            }
            c.lamMoi();
            int day = c.doDung.size();
            int dem0 = 0;
            for (int[] d : c.doDung) if (d[1] == 714) dem0++;
            String t3 = KichBanGiftcode.nhap(c, ma2, "thanh cong", "hanh trang", "o trong", "khong");
            c.lamMoi();
            int dem1 = 0;
            for (int[] d : c.doDung) if (d[1] == 714) dem1++;
            String dd = BotChucNang.sql1("SELECT da_dung FROM tob_giftcode WHERE ma='" + ma2 + "'");
            ket("giftcode.tui_day_khong_mat", dem1 - dem0 == 2 || "0".equals(dd), "tui " + truoc + "->" + day + " mon; " + t3 + " | 714: " + dem0 + "->" + dem1 + " da_dung " + dd);
            KichBanDS2.raNgoai(c);
        }
    }

    // ================================================================== ve dich chuyen
    static void veDichChuyen() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("vd", 0, 0);
        String u = b.user;
        KichBanHoiQuy.boQuaHuongDan(b);
        HoiQuy.thoat(b);
        HoiQuy.capTien(u, 50000, 0);
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            if (!KichBanDS2.vaoNha(c, 103)) { ket("ve_dc.vao_tiem", false, "map " + c.mapId); return; }
            c.lamMoi();
            long xu0 = c.xu;
            int ve0 = c.soThuoc(162);
            int m = c.moc();
            c.gui(23, (byte) 1);
            c.doiMenu(m, 1, -1, 3000);
            int i = c.timMuc("Vé dịch chuyển");
            ket("ve_dc.kecleon_co_muc", i >= 0, c.menuChu.toString() + " | " + c.chuTu(m));
            if (i < 0) return;
            int m2 = c.moc();
            c.menu(1, c.menuCuoi[1], i);
            int n = c.doiMenu(m2, -591, -1, 3000);
            if (n <= 0) { ket("ve_dc.menu_mua", false, c.chuTu(m2)); return; }
            int m3 = c.moc();
            c.menu(-591, c.menuCuoi[1], 0);
            c.bom(1500);
            c.lamMoi();
            ket("ve_dc.mua_1", c.soThuoc(162) == ve0 + 1 && c.xu < xu0, "ve " + ve0 + "->" + c.soThuoc(162) + " xu " + xu0 + "->" + c.xu + " | " + c.chuTu(m3));
            KichBanDS2.raNgoai(c);
            if (!KichBanGymLang.raPallet(c)) { ket("ve_dc.ra_pallet", false, "map " + c.mapId); return; }
            int ve1 = c.soThuoc(162);
            int m4 = c.moc();
            c.gui(22, (byte) 162);
            int k = c.doiMenu(m4, -590, 0, 3000);
            ket("ve_dc.dung_mo_menu", k == 3, c.menuChu + " | " + c.chuTu(m4));
            if (k <= 0) return;
            int m5 = c.moc();
            c.menu(-590, 0, 0);
            c.cho(x -> x.coLenh(m5, 12), 5000);
            if (c.coLenh(m5, 12)) { c.gui(12, (short) -500, (short) 0, (short) 0); c.bom(1200); }
            c.lamMoi();
            ket("ve_dc.ve_lang", KichBanDS.laLang(c.mapId) && c.soThuoc(162) == ve1 - 1, "map " + c.mapId + " ve " + ve1 + "->" + c.soThuoc(162) + " | " + c.chuTu(m5));
            // bam lai ngay: hoi chieu, khong tru
            int ve2 = c.soThuoc(162);
            int m6 = c.moc();
            c.gui(22, (byte) 162);
            c.bom(1500);
            c.lamMoi();
            ket("ve_dc.hoi_chieu", c.soThuoc(162) == ve2, "ve " + ve2 + "->" + c.soThuoc(162) + " | " + c.chuTu(m6));
        }
    }

    // ================================================================== thu cuoi
    static void thuCuoi() throws Exception {
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("tc", 0, 0);
        String u = b.user;
        KichBanHoiQuy.boQuaHuongDan(b);
        HoiQuy.thoat(b);
        HoiQuy.capLuong(u, 310);
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.lamMoi();
            int nThu = c.thu.size(), l0 = c.luong;
            Map<String, Integer> t0 = tui(c);
            int m = c.moc();
            c.gui(85, (byte) 45); // Sao goi Tauros (300 luong) -> thu cuoi vinh vien (ThuCuoiTuTui)
            c.bom(1500);
            c.lamMoi();
            Map<String, Integer> d = khac(t0, tui(c));
            ket("thu_cuoi.mua_sao", l0 - c.luong == 300 && !d.isEmpty(), "luong " + l0 + "->" + c.luong + " tui " + d + " | " + c.chuTu(m));
            // Sao goi Ponyta (10 luong, trang tri): cuoi tam, khong thanh thu
            int m0 = c.moc();
            c.gui(85, (byte) 11);
            c.bom(1200);
            c.gui(22, (byte) 30);
            c.bom(1200);
            ket("thu_cuoi.sao_ponyta_cuoi_tam", c.conSong() && !c.chuTu(m0).contains("Không thể"), c.chuTu(m0));
            c.gui(108, (byte) 0); // xuong Ponyta truoc khi cuoi thu khac
            c.bom(1000);
            // dung Sao tu tui (lenh 22, o thuoc moi)
            int o = -1;
            for (Map.Entry<String, Integer> e : d.entrySet()) if (e.getKey().startsWith("thuoc") && e.getValue() > 0) o = Integer.parseInt(e.getKey().substring(5));
            if (o >= 0) {
                int m2 = c.moc();
                c.gui(22, (byte) o);
                c.bom(1500);
                c.lamMoi();
                ket("thu_cuoi.dung_sao_co_thu", c.thu.size() == nThu + 1, "thu " + nThu + "->" + c.thu.size() + " " + c.tenThu.values() + " | " + c.chuTu(m2));
            } else ket("thu_cuoi.dung_sao_co_thu", false, "khong thay o vat pham moi: " + d);
            if (c.thu.isEmpty()) return;
            int idThu = c.thu.get(c.thu.size() - 1)[0];
            int m3 = c.moc();
            c.gui(-45, (byte) 0, (short) idThu);
            c.bom(1000);
            BotChucNang.SuKien mn = null;
            for (int i = m3; i < c.su.size(); i++) if (c.su.get(i).cmd == -30 && c.su.get(i).so[0] == -800) mn = c.su.get(i);
            if (mn != null) { c.menu(-800, mn.so[1], 0); c.bom(1000); }
            ket("thu_cuoi.cuoi", c.coLenh(m3, 3) || c.coLenh(m3, 15), c.chuTu(m3));
            int m4 = c.moc();
            c.gui(108, (byte) 0);
            c.bom(1000);
            ket("thu_cuoi.xuong", c.conSong(), c.chuTu(m4));
        }
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.lamMoi();
            ket("thu_cuoi.luu_qua_dang_nhap_lai", !c.thu.isEmpty(), "thu sau dang nhap lai: " + c.tenThu.values());
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== giao dich
    static void giaoDich() throws Exception {
        BotChucNang.Bot a = HoiQuy.nguoiChoiMoi("ga", 0, 0);
        BotChucNang.Bot c = HoiQuy.nguoiChoiMoi("gb", 0, 0);
        try {
            KichBanHoiQuy.boQuaHuongDan(a);
            KichBanHoiQuy.boQuaHuongDan(c);
            HoiQuy.datCap(a, 20); // giao dich / mua ban xu voi nguoi khac: tu cap 20
            HoiQuy.datCap(c, 20);
            HoiQuy.duTuoi(a);
            HoiQuy.duTuoi(c);
            // hai nguoi cung ban do: lang co nhieu ban sao (0 / 301...) -> cung ra dong Pallet (map 1) roi di bo sat nhau
            if (a.mapId != c.mapId) { KichBanGymLang.raPallet(a); KichBanGymLang.raPallet(c); }
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(a.mapLoad);
            KichBanHuongDan.dongBoViTri(a);
            KichBanHuongDan.dongBoViTri(c);
            if (a.mapId == c.mapId && bd != null) {
                int[] o = bd.oGan(c.px / 16 + 1, c.py / 16, 3);
                if (o != null) a.diDuong(bd, o[0], o[1]);
            }
            a.bom(800);
            c.bom(800);
            a.lamMoi();
            c.lamMoi();
            int a0 = a.soThuoc(1), c0 = c.soThuoc(1);
            long xa = a.xu, xc = c.xu;
            int m = c.moc();
            a.gui(66, (byte) 0, (short) c.actor);
            boolean moi = c.cho(x -> x.coLenh(m, 66), 3000);
            c.gui(66, (byte) 1, (short) a.actor);
            a.bom(600);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 3);
            a.bom(600);
            c.bom(300);
            a.gui(66, (byte) 4, (short) 0);
            c.gui(66, (byte) 4, (short) 0);
            a.bom(600);
            c.bom(600);
            a.gui(66, (byte) 5);
            c.gui(66, (byte) 5);
            a.bom(1500);
            c.bom(1500);
            a.lamMoi();
            c.lamMoi();
            ket("giao_dich.moi", moi, "map A " + a.mapId + " B " + c.mapId + " | " + c.chuTu(m) + " | A: " + a.chuTu(m));
            ket("giao_dich.bao_toan_thuoc", a.soThuoc(1) + c.soThuoc(1) == a0 + c0, "A " + a0 + "->" + a.soThuoc(1) + " B " + c0 + "->" + c.soThuoc(1));
            ket("giao_dich.chuyen_dung", a.soThuoc(1) == a0 - 3 && c.soThuoc(1) == c0 + 3, "A " + a0 + "->" + a.soThuoc(1) + " B " + c0 + "->" + c.soThuoc(1) + " | " + a.chuTu(m));
            ket("giao_dich.xu_khong_doi", a.xu == xa && c.xu == xc, "xu A " + xa + "->" + a.xu + " B " + xc + "->" + c.xu);
            // ngat ket noi giua giao dich: khong mat, khong nhan doi
            a.bom(61000);
            int a1 = a.soThuoc(1), c1 = c.soThuoc(1);
            int m2 = c.moc();
            a.gui(66, (byte) 0, (short) c.actor);
            c.cho(x -> x.coLenh(m2, 66), 3000);
            c.gui(66, (byte) 1, (short) a.actor);
            a.bom(500);
            a.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 2);
            a.bom(300);
            String ua = a.user;
            a.close(); // rot mang giua chung
            c.bom(2500);
            BotChucNang.Bot a2 = HoiQuy.vaoLai(ua);
            a2.lamMoi();
            c.lamMoi();
            ket("giao_dich.rot_mang_bao_toan", a2.soThuoc(1) + c.soThuoc(1) == a1 + c1 && a2.soThuoc(1) == a1, "A " + a1 + "->" + a2.soThuoc(1) + " B " + c1 + "->" + c.soThuoc(1));
            a = a2;
        } finally {
            HoiQuy.thoat(a);
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== chat
    static void chat() throws Exception {
        BotChucNang.Bot a = HoiQuy.nguoiChoiMoi("ch", 0, 0);
        BotChucNang.Bot c = HoiQuy.nguoiChoiMoi("ci", 0, 0);
        try {
            KichBanHoiQuy.boQuaHuongDan(a);
            KichBanHoiQuy.boQuaHuongDan(c);
            a.bom(500);
            int m = c.moc();
            a.chat("xin chao " + c.ten);
            String t = c.doiChu(m, 3000, "xin chao");
            ket("chat.ban_do", a.mapId != c.mapId || t != null, "map " + a.mapId + "/" + c.mapId + " | " + (t == null ? c.chuTu(m) : t));
            int m2 = c.moc();
            a.gui(-124, (byte) 1, (byte) 4, c.ten, "tin rieng hoi quy");
            String t2 = c.doiChu(m2, 8000, "tin rieng hoi quy");
            ket("chat.tin_rieng", t2 != null, t2 == null ? c.chuTu(m2) + " | A: " + a.chuTu(m2) : t2);
        } finally {
            HoiQuy.thoat(a);
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== PvP an toan
    static void pvpAnToan() throws Exception {
        BotChucNang.Bot a = HoiQuy.nguoiChoiMoi("pa", 0, 0);
        BotChucNang.Bot c = HoiQuy.nguoiChoiMoi("pb", 0, 0);
        try {
            KichBanHoiQuy.boQuaHuongDan(a);
            KichBanHoiQuy.boQuaHuongDan(c);
            // lang co nhieu ban sao (0 / 301): ra Pallet roi ve lang toi khi hai nguoi cung ban sao
            for (int lan = 0; lan < 4 && a.mapId != c.mapId; lan++) {
                KichBanGymLang.raPallet(c);
                KichBanHuongDan.quaCong(c, 0);
            }
            if (a.mapId != c.mapId) { ket("pvp_an_toan.trong_lang", null, "khong vao duoc cung ban sao lang: " + a.mapId + "/" + c.mapId); return; }
            c.lamMoi();
            int hpDung = c.hp;
            c.bom(4000);
            c.lamMoi();
            ket("pvp_an_toan.dung_yen_khong_mat_mau", c.hp >= hpDung, "dung yen 4 giay o map " + c.mapId + ": hp " + hpDung + "->" + c.hp + " | " + c.chuTu(Math.max(0, c.moc() - 30)));
            int hp0 = c.hp;
            for (int i = 0; i < 6; i++) { a.gui(6, (short) c.actor, (byte) 0); a.bom(400); }
            c.bom(800);
            c.lamMoi();
            ket("pvp_an_toan.trong_lang", c.hp >= hp0, "hp B " + hp0 + "->" + c.hp + " (map " + a.mapId + "/" + c.mapId + ")");
        } finally {
            HoiQuy.thoat(a);
            HoiQuy.thoat(c);
        }
    }

    // ================================================================== toc do client (nhanh khac)
    /**
     * Toc do game = thoi gian that + khong giat lui khi danh tay la phan CLIENT (vong lap khung) cua nhanh sua-slow-motion
     * (test client_pc/test/KiemTocDoGame.java). Chua gop vao cay nay -> FAIL "DA_BIET – chờ sua-slow-motion"; da gop thi
     * chay_hoi_quy.sh CHUP=1 chay no cung cac kiem client khac. Phia may chu: di_bo.toc_do_that_khong_keo_lui.
     */
    static void tocDoClient() {
        File t = new File(BotChucNang.NOI, "client_pc/test/KiemTocDoGame.java");
        ket("toc_do.client.kiem_co_trong_cay", t.isFile(), t.isFile() ? "co " + t : "chua co KiemTocDoGame (nhanh sua-slow-motion chua gop)");
    }

    // ================================================================== khoi dong lai
    static void khoiDongLai() throws Exception {
        String lenh = System.getProperty("khoidonglai", "");
        if (lenh.isEmpty()) { ket("khoi_dong_lai", null, "khong co -Dkhoidonglai"); return; }
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("kd", 0, 0);
        String u = b.user;
        KichBanHoiQuy.boQuaHuongDan(b);
        KichBanGymLang.raPallet(b);
        b.lamMoi();
        String truoc = "lv" + b.lv + " xu" + b.xu + " lk" + b.lk + " thuoc" + b.soThuoc(1) + " do" + b.doDung.size() + " map" + b.mapId;
        b.close();
        Thread.sleep(3000);
        HoiQuy.ghi("khoi dong lai may chu: " + lenh);
        Process p = new ProcessBuilder("sh", "-c", lenh).inheritIO().start();
        p.waitFor();
        boolean len = false;
        for (int i = 0; i < 120 && !len; i++) {
            try (java.net.Socket s = new java.net.Socket()) {
                s.connect(new java.net.InetSocketAddress(BotChucNang.HOST, BotChucNang.PORT), 1000);
                len = true;
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
        ket("khoi_dong_lai.len_lai", len, lenh);
        if (!len) return;
        Thread.sleep(3000);
        HoiQuy.viTri = HoiQuy.tepLog == null ? 0 : HoiQuy.tepLog.length();
        try (BotChucNang.Bot c = HoiQuy.vaoLai(u)) {
            c.lamMoi();
            c.bom(2500);
            String sau = "lv" + c.lv + " xu" + c.xu + " lk" + c.lk + " thuoc" + c.soThuoc(1) + " do" + c.doDung.size() + " map" + c.mapId;
            ket("khoi_dong_lai.giu_du_lieu", truoc.equals(sau), truoc + " => " + sau);
            int moi = 0;
            for (BotChucNang.SuKien e : c.su) if (e.cmd == -30 && e.so != null && e.so[1] == KichBanHuongDan.MENU_MOI && e.so[0] == KichBanHuongDan.NPC_SO_TAY) moi++;
            ket("khoi_dong_lai.khong_moi_huong_dan", moi == 0, "menu moi: " + moi);
            HoiQuy.thoat(c);
        }
    }
}
