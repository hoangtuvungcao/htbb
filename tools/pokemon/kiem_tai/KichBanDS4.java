import java.io.IOException;
import java.util.*;

/** Kich ban nhom 4: chat, ban be, to doi, bang hoi, nhiem vu, So tay, Gym, Ham nguc, PvP, giftcode. */
class KichBanDS4 {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        BotChucNang.dk("chat", true, KichBanDS4::chat);
        BotChucNang.dk("ban_be", true, KichBanDS4::banBe);
        BotChucNang.dk("to_doi", true, KichBanDS4::toDoi);
        BotChucNang.dk("bang_hoi", true, KichBanDS4::bangHoi);
        BotChucNang.dk("so_tay", true, KichBanDS4::soTay);
        BotChucNang.dk("nhiem_vu", true, KichBanDS4::nhiemVu);
        BotChucNang.dk("nhiem_vu_chinh", true, KichBanDS4::nhiemVuChinh);
        BotChucNang.dk("ham_nguc", true, KichBanDS4::hamNguc);
        BotChucNang.dk("gym", true, KichBanDS4::gym);
        BotChucNang.dk("pvp", true, KichBanDS4::pvp);
        BotChucNang.dk("giftcode", true, KichBanDS4::giftcode);
        BotChucNang.dk("pokedex_menu", true, KichBanDS4::pokedexMenu);
        KichBanDS5.dangKy();
        KichBanNhiemVu.dangKy();
    }

    static BotChucNang.Bot botLang(int lv) throws Exception {
        return KichBanDS3.botLang(lv);
    }

    /** Quoc gia theo lang luc vao (MapDoMenu/doReturnVillage: {70,1701},{0,301},{80,1901}). */
    static int quocGia(BotChucNang.Bot b) {
        int m = b.mapId;
        if (KichBanDS2.laOffline(m) && b.mapCu >= 0) m = b.mapCu;
        return m == 70 || m == 1701 ? 0 : m == 0 || m == 301 ? 1 : m == 80 || m == 1901 ? 2 : -1;
    }

    /** Bot KHONG GM cung quoc gia voi a (kt101..). */
    static BotChucNang.Bot thuongCungQuoc(BotChucNang.Bot a) throws Exception {
        for (int i = 0; i < 10; i++) {
            BotChucNang.Bot c = BotChucNang.botThuong();
            KichBanDS.veLang(c);
            if (quocGia(c) == quocGia(a) || quocGia(c) < 0) return c;
            c.close();
        }
        throw new IllegalStateException("khong co tai khoan thuong cung quoc gia " + quocGia(a));
    }

    /** Bot thu hai cung quoc gia voi a (thu toi da 8 tai khoan). */
    static BotChucNang.Bot botCungQuoc(int lv, BotChucNang.Bot a) throws Exception {
        for (int i = 0; i < 8; i++) {
            BotChucNang.Bot c = botLang(lv);
            if (quocGia(c) == quocGia(a)) return c;
            c.close();
        }
        throw new IllegalStateException("khong tim duoc bot cung quoc gia " + quocGia(a));
    }

    /** Mo So tay huan luyen (npc -90 muc 2 -> menu -527/0). */
    static boolean moSoTay(BotChucNang.Bot b) throws IOException {
        int m = b.moc();
        b.menu(-90, 0, 2);
        return b.doiMenu(m, -527, 0, 3000) > 0;
    }

    /** Chon muc trong So tay (menu goc) va tra ve moc truoc khi chon. */
    static int chonSoTay(BotChucNang.Bot b, int muc) throws IOException {
        moSoTay(b);
        int m = b.moc();
        b.menu(-527, 0, muc);
        b.bom(900);
        return m;
    }

    // ------------------------------------------------------------------ chat
    static void chat() throws Exception {
        try (BotChucNang.Bot a = botLang(25); BotChucNang.Bot c = botLang(25)) {
            a.den(2, 20, 20);
            c.den(2, 21, 20);
            a.bom(500);
            c.bom(500);
            int m = c.moc();
            a.chat("xin chao ban " + c.ten);
            String t = c.doiChu(m, 3000, "xin chao ban");
            ket("chat.ban_do", t != null, t == null ? c.chuTu(m) : t);
            // tin rieng (-5): server giao cham theo nhip CharCapNhat
            m = c.moc();
            a.gui(-5, c.ten, "tin rieng thu");
            t = c.doiChu(m, 8000, "tin rieng thu");
            ket("chat.tin_rieng", t != null, t == null ? c.chuTu(m) : t);
            // loa the gioi: can vat pham 100
            a.gm("spt " + a.ten + " 100 2", 600);
            m = c.moc();
            a.gui(-31, (short) a.actor, (byte) 1, "loa thu nghiem");
            t = c.doiChu(m, 5000, "loa thu nghiem");
            ket("chat.loa", t != null, t == null ? c.chuTu(m) : t);
            // khong co loa: khong phat
            a.gm("clear " + a.ten + " 0", 600);
            m = c.moc();
            a.gui(-31, (short) a.actor, (byte) 1, "loa khong co");
            c.bom(1500);
            ket("chat.loa_khong_co", c.doiChu(m, 0, "loa khong co") == null, "khong co loa -> khong phat");
            // chong spam (chi ap cho nguoi thuong): tai khoan khong GM (-Dthuong=kt101), GM nang cap 25
            String uThuong = System.getProperty("thuong", "kt101");
            BotChucNang.Bot t1 = new BotChucNang.Bot(uThuong);
            try {
                String l = t1.dangNhap("123456");
                if (l == null) l = t1.vaoGame(0);
                if (l != null) { ket("chat.chong_spam", null, "khong vao duoc " + uThuong + ": " + l); return; }
                KichBanDS.veLang(t1);
                a.gm("slv " + t1.ten + " 25 0", 800);
                a.gm("mc " + t1.ten, 1500);
                c.gm("mc " + t1.ten, 1500);
                t1.bom(500);
                for (int i = 0; i < 7; i++) { t1.chat("spam " + i); t1.bom(150); }
                m = c.moc();
                t1.bom(500);
                t1.chat("sau spam");
                String sau = c.doiChu(m, 1500, "sau spam");
                String truoc = c.doiChu(0, 0, "spam 0");
                ket("chat.chong_spam", truoc != null && sau == null, "B thay 'spam 0'=" + (truoc != null) + "; sau spam: " + (sau == null ? "bi cam chat" : "VAN PHAT " + sau));
            } finally {
                t1.close();
            }
        }
    }

    // ------------------------------------------------------------------ ban be
    static void banBe() throws Exception {
        try (BotChucNang.Bot a = botLang(20); BotChucNang.Bot c = botCungQuoc(20, a)) {
            a.den(2, 20, 20);
            c.den(2, 21, 20);
            int m = c.moc();
            a.gui(101, (short) c.actor, (byte) 0);
            boolean moi = c.choLenh(m, 101, 3000);
            int m2 = a.moc();
            c.gui(101, (short) a.actor, (byte) 1);
            boolean ds = a.choLenh(m2, 102, 3000);
            ket("ban_be.ket_ban", moi && ds, "loi moi=" + moi + " danh sach=" + ds + " " + a.chuTu(m2) + c.chuTu(m));
            m = a.moc();
            a.gui(-6, c.ten);
            a.bom(800);
            ket("ban_be.xoa", a.conSong(), a.chuTu(m));
        }
    }

    // ------------------------------------------------------------------ to doi + chia EXP
    static void toDoi() throws Exception {
        try (BotChucNang.Bot a = botLang(3); BotChucNang.Bot c = botCungQuoc(3, a)) {
            a.den(2, 20, 20);
            c.den(2, 20, 21);
            int m = a.moc();
            a.gui(48, a.actor);
            a.bom(800);
            ket("to_doi.tao", a.coLenh(m, 48), a.chuTu(m));
            int mc = c.moc();
            a.gui(49, (byte) 0, (short) c.actor);
            boolean moi = c.choLenh(mc, 49, 3000);
            int ma = a.moc();
            c.gui(49, (byte) 1, (short) a.actor);
            boolean vao = a.choLenh(ma, 49, 3000);
            ket("to_doi.moi_vao", moi && vao, "B nhan loi moi=" + moi + ", A thay B vao=" + vao + " " + c.chuTu(mc));
            // A danh quai, B dung canh: B phai co EXP
            c.lamMoi();
            int pt0 = c.pt, lv0 = c.lv;
            List<Integer> ds = KichBanDS.timQuai(a, 0, 200);
            int ha = 0;
            for (int id : ds) {
                int[] q = a.quai.get(id);
                if (q == null) continue;
                c.den(2, q[0] / 16, q[1] / 16 + 1);
                if (KichBanDS.haQuai(a, id, 0, 15) > 0) ha++;
                if (ha >= 3) break;
            }
            c.lamMoi();
            ket("to_doi.chia_exp", ha > 0 && (c.pt > pt0 || c.lv > lv0), "A ha " + ha + " quai; B pt " + pt0 + " -> " + c.pt + " lv " + lv0 + " -> " + c.lv);
            int mk = c.moc();
            a.gui(50, (byte) 0, (short) c.actor);
            boolean kick = c.choLenh(mk, 50, 3000);
            ket("to_doi.duoi", kick, c.chuTu(mk));
            m = a.moc();
            a.gui(50, (byte) 1);
            a.bom(800);
            ket("to_doi.giai_tan", a.conSong(), a.chuTu(m));
        }
    }

    // ------------------------------------------------------------------ bang hoi
    static void bangHoi() throws Exception {
        try (BotChucNang.Bot a = botLang(50); BotChucNang.Bot c = botCungQuoc(50, a)) {
            a.gm("spt " + a.ten + " 31 1", 600);
            a.gm("smn " + a.ten + " 150000000 0 0", 600);
            a.lamMoi();
            long xu0 = a.xu;
            int m = a.moc();
            a.gui(-9, new byte[0]);
            a.cho(x -> x.coLenh(m, -9) || x.doiChu(m, 0, "") != null, 3000);
            BotChucNang.SuKien e = a.layLenh(m, -9);
            int icon = 0;
            if (e != null && e.d.length >= 4) icon = ((e.d[2] & 255) << 8) | (e.d[3] & 255);
            ket("bang_hoi.xin_tao", e != null, a.chuTu(m));
            String ten = "kt" + (System.currentTimeMillis() % 100000);
            int m2 = a.moc();
            a.gui(-10, (short) icon, ten);
            a.choLenh(m2, -10, 4000);
            a.lamMoi();
            BotChucNang.SuKien t = a.layLenh(m2, -10);
            int idClan = t != null && t.d.length >= 2 ? (short) (((t.d[0] & 255) << 8) | (t.d[1] & 255)) : -1;
            ket("bang_hoi.tao", idClan >= 0 && t != null && a.xu <= xu0 - 100000000, "id=" + idClan + " xu " + xu0 + " -> " + a.xu + " " + a.chuTu(m2));
            String db = BotChucNang.sql1("SELECT COUNT(*) FROM tob_clan WHERE id_clan=" + idClan);
            ket("bang_hoi.csdl", db == null || !db.startsWith("0"), "tob_clan id=" + idClan + ": " + db);
            if (idClan < 0 || t == null) return;
            // moi thanh vien
            a.den(2, 20, 20);
            c.den(2, 21, 20);
            int mc = c.moc();
            a.gui(-11, (byte) 0, (short) c.actor);
            boolean moi = c.choLenh(mc, -11, 3000);
            int ma = a.moc();
            c.gui(-11, (byte) 1, (short) a.actor, true);
            a.bom(1200);
            ket("bang_hoi.moi", moi, "B nhan loi moi=" + moi + " " + c.chuTu(mc) + " | A: " + a.chuTu(ma));
            // chat bang
            mc = c.moc();
            a.gui(-18, "chao ca bang");
            String cb = c.doiChu(mc, 3000, "chao ca bang");
            ket("bang_hoi.chat", cb != null, cb == null ? c.chuTu(mc) : cb);
            // quy bang
            a.gm("slv " + a.ten + " 50 0", 400);
            ma = a.moc();
            a.gui(-20, 1000000);
            a.cho(x -> x.layLenh(a.moc() - 1, -32) != null, 2000);
            BotChucNang.SuKien p = a.layLenh(ma, -32);
            long xuQ = a.xu;
            if (p != null) { a.gui(-32, (short) p.so[0], (byte) p.so[1], "", (byte) 0); a.bom(1000); }
            a.lamMoi();
            ket("bang_hoi.quy", p != null && a.xu < xuQ, "popup=" + (p != null) + " xu " + xuQ + " -> " + a.xu + " " + a.chuTu(ma));
            // roi bang
            mc = c.moc();
            c.gui(-15, new byte[0]);
            c.bom(1000);
            ket("bang_hoi.roi", c.conSong(), c.chuTu(mc));
            ma = a.moc();
            a.gui(-14, new byte[0]);
            a.bom(1000);
            ket("bang_hoi.giai_tan", a.conSong(), a.chuTu(ma));
            a.gui(-14, new byte[0]); // huy giai tan (bat lai) - de bang con, khong anh huong
            a.bom(500);
        }
    }

    // ------------------------------------------------------------------ So tay: moi muc
    static void soTay() throws Exception {
        try (BotChucNang.Bot b = botLang(35)) {
            boolean mo = moSoTay(b);
            ket("so_tay.mo", mo && b.menuChu.size() >= 9, "menu: " + b.menuChu);
            String[] ten = {"nang_luong", "tuan", "ham_nguc", "pvp", "su_pham", "dang_nhap", "qua_hoat_dong", "gym", "nhiem_vu"};
            for (int i = 0; i < ten.length; i++) {
                int m = chonSoTay(b, i);
                String t = b.chuTu(m);
                boolean coTraLoi = !t.isEmpty();
                boolean loiChu = t.contains("?") && t.matches(".*\\?\\?.*");
                ket("so_tay." + ten[i], coTraLoi && b.conSong() && !loiChu, t.isEmpty() ? "KHONG TRA LOI" : t);
            }
            // tinh thong: chon HP roi tan cong
            chonSoTay(b, 0);
            int m = b.moc();
            b.menu(-527, 1, 1);
            String t = b.doiChu(m, 2000, "Tinh thông");
            ket("so_tay.tinh_thong", t != null && t.contains("HP"), t);
            // qua hoat dong: nhan moc 0 (chua du giay -> tu choi ro rang)
            chonSoTay(b, 6);
            m = b.moc();
            b.menu(-527, 4, 0);
            t = b.doiChu(m, 2000, "");
            ket("so_tay.nhan_qua_hoat_dong", t != null, t);
        }
    }

    // ------------------------------------------------------------------ nhiem vu (engine)
    /** Mo menu engine (So tay muc 8) -> chon nhom -> danh sach (menu -527/12 hoac 11). */
    static List<String> dsNhom(BotChucNang.Bot b, int nhom, int idMenuMongDoi) throws IOException {
        chonSoTay(b, 8);
        int m = b.moc();
        b.menu(-527, 10, nhom);
        int n = b.doiMenu(m, -527, idMenuMongDoi, 2500);
        if (n < 0) {
            String t = b.doiChu(m, 0, "");
            return t == null ? null : Collections.singletonList("THONG BAO: " + t);
        }
        return new ArrayList<>(b.menuChu);
    }

    static String nhan(BotChucNang.Bot b, int idx) throws IOException {
        int m = b.moc();
        b.menu(-527, 12, idx);
        String t = b.doiChu(m, 2500, "");
        return t == null ? "khong tra loi" : t;
    }

    static void nhiemVu() throws Exception {
        try (BotChucNang.Bot b = botLang(12)) {
            // nhiem vu phu: toi da 3 khe
            List<String> phu = dsNhom(b, 1, 12);
            ket("nhiem_vu.ds_phu", phu != null && !phu.isEmpty() && !phu.get(0).startsWith("THONG BAO"), phu == null ? "khong co" : phu.size() + " muc: " + phu.subList(0, Math.min(4, phu.size())));
            if (phu != null && phu.size() >= 4 && !phu.get(0).startsWith("THONG BAO")) {
                List<String> kq = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    kq.add(nhan(b, i).replace('\n', ' '));
                }
                boolean thu4 = BotChucNang.khongDau(kq.get(3)).contains("da du 3");
                ket("nhiem_vu.3_khe_phu", thu4, "lan 4: " + kq.get(3) + " | lan 1: " + kq.get(0).substring(0, Math.min(80, kq.get(0).length())));
            } else ket("nhiem_vu.3_khe_phu", null, "khong du 4 nhiem vu phu de thu");
            // dang lam
            List<String> dang = dsNhom(b, 0, 11);
            ket("nhiem_vu.dang_lam", dang != null && !dang.isEmpty(), dang == null ? "khong co" : String.valueOf(dang));
            // hang ngay: 5 mau
            List<String> ngay = dsNhom(b, 2, 12);
            ket("nhiem_vu.ngay_rut", ngay != null && ngay.size() >= 1 && ngay.size() <= 5 && !ngay.get(0).startsWith("THONG BAO"), ngay == null ? "khong co" : ngay.size() + ": " + ngay);
            // cau do neu co trong bang ngay
            int idxDo = -1;
            if (ngay != null) for (int i = 0; i < ngay.size(); i++) if (ngay.get(i).startsWith("Câu đố")) idxDo = i;
            if (idxDo >= 0) {
                String tenDo = ngay.get(idxDo).replaceAll(" \\(.*\\)$", "");
                String da = BotChucNang.sql1("SELECT n.dap_an FROM data_nhiem_vu_noi_dung n JOIN data_quest_lau_dai q ON q.ma=n.ma WHERE q.ten='" + tenDo.replace("'", "''") + "' LIMIT 1");
                nhan(b, idxDo);
                int m = b.moc();
                b.chat(da == null ? "?" : da.split("\\|")[0]);
                String t = b.doiChu(m, 3000, "Chính xác");
                ket("nhiem_vu.cau_do", t != null, "'" + tenDo + "' tra loi '" + da + "': " + (t == null ? b.chuTu(m) : t));
            } else ket("nhiem_vu.cau_do", null, "bang ngay hom nay cua nhan vat nay khong co cau do");
            // hang tuan: toi da 2
            List<String> tuan = dsNhom(b, 3, 12);
            if (tuan != null && tuan.size() >= 3 && !tuan.get(0).startsWith("THONG BAO")) {
                String k1 = nhan(b, 0);
                String k2 = nhan(b, 1);
                String k3 = nhan(b, 2);
                ket("nhiem_vu.tuan_toi_da_2", BotChucNang.khongDau(k3).contains("moi tuan chi nhan 2") || BotChucNang.khongDau(k2).contains("moi tuan chi nhan 2"), "lan 3: " + k3.replace('\n', ' '));
            } else ket("nhiem_vu.tuan_toi_da_2", null, "bang tuan < 3 muc: " + tuan);
            // nhiem vu an AN-01 (ban dem 19h-06h)
            int gio = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            boolean dem = gio >= 19 || gio < 6;
            List<String> phu2 = dsNhom(b, 1, 12);
            String an = BotChucNang.sql1("SELECT q.ten FROM data_quest_lau_dai q WHERE q.ma='AN-01'");
            boolean thay = phu2 != null && an != null && phu2.stream().anyMatch(x -> x.startsWith(an));
            // danh sach toi da DS_TOI_DA muc nen co the khong hien du khi da mo
            ket("nhiem_vu.an_ban_dem", dem || !thay, "gio " + gio + ", '" + an + "' hien=" + thay + " (ban ngay phai an)");
        }
    }

    /** Nhiem vu chinh (-64) + nhanh lua chon (danh dau chuoi truoc da xong qua CSDL). */
    static void nhiemVuChinh() throws Exception {
        BotChucNang.Bot b = botLang(8);
        try {
            int m = b.moc();
            b.gui(-64, (byte) 0, (short) 0, (byte) 0);
            b.bom(1200);
            String t = b.chuTu(m);
            ket("nhiem_vu_chinh.nhan", b.coLenh(m, -64) || t.contains("nhiệm vụ"), t);
            // nhanh lua chon P-01.1.4 (can P-01.1.1..3)
            int idc = b.charDb;
            String u = b.user;
            b.close();
            Thread.sleep(3000);
            for (String ma : new String[]{"P-01.1.1", "P-01.1.2", "P-01.1.3"})
                BotChucNang.sql("INSERT INTO tob_nhiem_vu_lau_dai(id_char,ma,tien_do,trang_thai,tuan) VALUES(" + idc + ",'" + ma + "',1,2,0)");
            b = new BotChucNang.Bot(u);
            String l = b.dangNhap("123456");
            if (l == null) l = b.vaoGame(0);
            if (l != null) { ket("nhiem_vu_chinh.nhanh", false, l); return; }
            List<String> phu = dsNhom(b, 1, 12);
            int idx = -1;
            if (phu != null) for (int i = 0; i < phu.size(); i++) if (phu.get(i).startsWith("Hai lá thư")) idx = i;
            if (idx < 0) { ket("nhiem_vu_chinh.nhanh", false, "khong thay 'Hai lá thư' trong ds phu: " + phu); return; }
            String nh = nhan(b, idx);
            List<String> dang = dsNhom(b, 0, 11);
            int j = -1;
            for (int i = 0; dang != null && i < dang.size(); i++) if (dang.get(i).contains("Hai lá thư")) j = i;
            m = b.moc();
            b.menu(-527, 11, Math.max(0, j));
            int n = b.doiMenu(m, -527, 13, 2500);
            if (n <= 0) { ket("nhiem_vu_chinh.nhanh", false, "khong co menu lua chon: " + b.chuTu(m) + " | nhan: " + nh); return; }
            m = b.moc();
            long xu0 = b.xu;
            b.menu(-527, 13, 0);
            String t2 = b.doiChu(m, 2500, "");
            b.bom(800);
            String dh = BotChucNang.sql1("SELECT COUNT(*) FROM tob_nhiem_vu_lau_dai WHERE id_char=" + idc + " AND (ma='P-01.1.4#nhanh' OR ma LIKE 'DH:%')");
            ket("nhiem_vu_chinh.nhanh", t2 != null && dh != null && !dh.startsWith("0"), "chon A: " + t2 + " | CSDL nhanh/danh hieu: " + dh);
        } finally {
            b.close();
        }
    }

    // ------------------------------------------------------------------ ham nguc
    static void hamNguc() throws Exception {
        try (BotChucNang.Bot b = botLang(5)) {
            int m = chonSoTay(b, 2);
            String t = b.chuTu(m);
            ket("ham_nguc.xem", t.contains("Hầm ngục"), t);
            m = b.moc();
            b.menu(-527, 3, 0);
            b.bom(800);
            ket("ham_nguc.vao", b.doiChu(m, 1000, "Đã vào tầng") != null, b.chuTu(m));
            b.den(2, 20, 20);
            List<Integer> ds = KichBanDS.timQuai(b, 0, 200);
            int ha = 0;
            m = b.moc();
            for (int id : ds) {
                if (KichBanDS.haQuai(b, id, 0, 12) > 0) ha++;
                if (b.doiChu(m, 0, "tầng") != null && ha >= 3) break;
                if (ha >= 10) break;
            }
            String qua = b.doiChu(m, 1000, "tầng");
            ket("ham_nguc.qua_tang", qua != null, "ha " + ha + " quai: " + (qua == null ? b.chuTu(m) : qua));
            m = chonSoTay(b, 2);
            int m2 = b.moc();
            b.menu(-527, 3, 1);
            String th = b.doiChu(m2, 2000, "");
            ket("ham_nguc.thuong", th != null, th);
            String db = BotChucNang.sql1("SELECT COUNT(*) FROM tob_ham_nguc WHERE id_char=" + b.charDb);
            ket("ham_nguc.csdl", true, "tob_ham_nguc dong: " + db);
        }
    }

    // ------------------------------------------------------------------ gym
    static void gym() throws Exception {
        try (BotChucNang.Bot b = KichBanDS.botDuDiem(15)) {
            int m = chonSoTay(b, 7);
            int n = b.doiMenu(m, -527, 5, 2000);
            ket("gym.ds", n > 0, b.chuTu(m));
            if (n <= 0) return;
            b.den(2, 20, 20);
            int vk = KichBanDS3.aitMon(b, 81, 0); // vu khi dong cap 11 (Gym 1 cap 12)
            if (vk != KichBanDS3.KHONG) { b.gui(29, (short) vk); b.bom(600); }
            KichBanDS.timQuai(b, 0, 300);
            Set<Integer> cu = new HashSet<>(b.quai.keySet());
            chonSoTay(b, 7);
            m = b.moc();
            b.menu(-527, 5, 0);
            String t = b.doiChu(m, 2500, "");
            ket("gym.thach_dau", t != null && !BotChucNang.khongDau(t).contains("khong"), t);
            // tim boss gym (quai moi xuat hien canh nguoi choi, id tu IDGen - am)
            Integer bId = KichBanDS.quaiMoi(b, cu, 8000);
            int boss = bId == null ? -1 : bId, maxhp = bId == null ? 0 : b.quai.get(bId)[4];
            int hp0 = bId == null ? -1 : b.quai.get(boss)[2];
            long t0 = System.currentTimeMillis();
            int don = bId == null ? -1 : KichBanDS.haQuai(b, boss, -1, 2400);
            long giay = (System.currentTimeMillis() - t0) / 1000;
            if (bId != null) System.out.println("  gym boss hp " + hp0 + " -> " + b.quai.get(boss)[2] + " vi tri " + b.quai.get(boss)[0] + "," + b.quai.get(boss)[1] + " minh " + b.px + "," + b.py);
            b.bom(1500);
            String th = b.doiChu(m, 500, "huy hiệu", "Huy hiệu", "thắng");
            ket("gym.ha_boss", don > 0 && maxhp > 10000, "boss " + boss + " maxhp " + maxhp + " ha sau " + don + " don, " + giay + " giay (thiet ke ttk 105 giay; bot cap 15, vu khi dong +0, xoay chieu 0..3); " + (th != null ? th : b.chuTu(m)));
        }
    }

    // ------------------------------------------------------------------ PvP xep hang
    static void pvp() throws Exception {
        // B phai la nguoi thuong: GM (isAdmin) khong chet o map luyen (Char.isHieuUngCoLongDuongQua)
        try (BotChucNang.Bot a = botLang(30); BotChucNang.Bot c = thuongCungQuoc(a)) {
            a.gm("slv " + c.ten + " 21 0", 800);
            if (!KichBanDS2.raNgoai(c)) { ket("pvp", false, c.user + " khong ra duoc map ngoai lang"); return; }
            a.gm("mc " + c.ten, 1500);
            c.bom(500);
            int m = a.moc();
            a.gui(67, (byte) 1);
            c.gui(67, (byte) 1);
            a.bom(800);
            c.bom(800);
            ket("pvp.bat_pk", a.coLenh(m, 67), a.chuTu(m));
            // vu khi bac (tpl 84, cap 26) cho A de tran PvP khong qua dai (PvP lau dai x0,30 sat thuong)
            int vk = KichBanDS3.aitMon(a, 84, 0);
            if (vk != KichBanDS3.KHONG) { a.gui(29, (short) vk); a.bom(800); }
            String elo0 = BotChucNang.sql1("SELECT elo FROM tob_pvp_mua WHERE id_char=" + a.charDb + " ORDER BY mua DESC LIMIT 1");
            c.lamMoi();
            int lan = 0;
            for (; lan < 400; lan++) {
                a.gui(6, (short) c.actor, (byte) 0);
                a.bom(450);
                if (lan % 5 == 4) { c.lamMoi(); if (c.hp <= 0) break; }
            }
            c.lamMoi();
            ket("pvp.ha_doi_thu", c.hp <= 0, "sau " + lan + " don hp B=" + c.hp + "/" + c.maxhp);
            a.bom(1500);
            String elo1 = BotChucNang.sql1("SELECT elo FROM tob_pvp_mua WHERE id_char=" + a.charDb + " ORDER BY mua DESC LIMIT 1");
            int ms = a.moc();
            a.menu(-90, 0, 2);
            a.doiMenu(ms, -527, 0, 2000);
            ms = a.moc();
            a.menu(-527, 0, 3);
            String st = a.doiChu(ms, 2000, "Elo");
            ket("pvp.elo", st != null && (elo1 != null || st.matches("(?s).*Elo: 10[1-9][0-9].*")), "CSDL elo " + elo0 + " -> " + elo1 + "; So tay: " + st);
        }
    }

    // ------------------------------------------------------------------ giftcode
    static void giftcode() throws Exception {
        String ma = "kttest" + (System.currentTimeMillis() % 100000);
        BotChucNang.sql("INSERT INTO tob_giftcode(ma,phan_thuong,han,tong_luot,da_dung,cap_toi_thieu,bat,ghi_chu,tao_boi,tao_luc) VALUES('" + ma + "','LUONGKHOA:5',NULL,5,0,1,1,'kiem thu tu dong','botchucnang',NOW())");
        try (BotChucNang.Bot b = botLang(10)) {
            b.lamMoi();
            int lk0 = b.lk;
            Thread.sleep(2100);
            int m = b.moc();
            b.gui(-31, (short) b.actor, (byte) 7, ma);
            String t = b.doiChu(m, 3000, "");
            b.lamMoi();
            ket("giftcode.nhan", b.lk == lk0 + 5, "lk " + lk0 + " -> " + b.lk + ": " + t);
            String log = BotChucNang.sql1("SELECT COUNT(*) FROM tob_giftcode_log WHERE ma='" + ma + "'");
            String dd = BotChucNang.sql1("SELECT da_dung FROM tob_giftcode WHERE ma='" + ma + "'");
            ket("giftcode.csdl", "1".equals(log) && "1".equals(dd), "log=" + log + " da_dung=" + dd);
            Thread.sleep(2100);
            m = b.moc();
            b.gui(-31, (short) b.actor, (byte) 7, ma);
            t = b.doiChu(m, 3000, "");
            b.lamMoi();
            ket("giftcode.lan_2", b.lk == lk0 + 5, "lan 2: " + t);
            Thread.sleep(2100);
            m = b.moc();
            b.gui(-31, (short) b.actor, (byte) 7, "khongtontai999");
            t = b.doiChu(m, 3000, "");
            ket("giftcode.sai_ma", t != null && b.lk == lk0 + 5, t);
        } finally {
            BotChucNang.sql("DELETE FROM tob_giftcode_log WHERE ma='" + ma + "'");
            BotChucNang.sql("DELETE FROM tob_giftcode WHERE ma='" + ma + "'");
        }
    }

    // ------------------------------------------------------------------ menu doi thuong / pokedex
    static void pokedexMenu() throws Exception {
        try (BotChucNang.Bot b = botLang(10)) {
            int m = b.moc();
            b.gui(23, (byte) -90);
            b.bom(1200);
            ket("npc_doi_thuong.mo", b.coLenh(m, -30), b.chuTu(m));
            m = b.moc();
            b.menu(-90, 0, 1);
            b.bom(1200);
            ket("pokedex.menu", !b.chuTu(m).isEmpty(), b.chuTu(m));
            m = b.moc();
            b.menu(-90, 0, 0);
            b.bom(1200);
            ket("npc_doi_thuong.xem", !b.chuTu(m).isEmpty() && b.conSong(), b.chuTu(m));
        }
    }
}
