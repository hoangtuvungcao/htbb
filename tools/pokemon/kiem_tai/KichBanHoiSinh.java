import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot GUC / HOI SINH (com.htbb.pokemon.HoiSinh, real.HoiSinhGame, lenh -101). Chay tren MAY CHU THU RIENG
 * (can GM: slv / mto / die / smn / spt). Moi kich ban: guc that (GM die) roi chon tung cach:
 *   tan_thu   cap 10: hop thoai co "tan thu", cho 5 giay roi hoi tai cho mien phi, khong mat EXP
 *   thuoc     cap 40: Thuoc hoi sinh (potions 119) -> song lai ~30% HP, mat 1 thuoc
 *   xu        cap 40: tra xu (phi theo cap), lan guc thu 2 trong 30 phut phi x1,5 (sau hoi chieu)
 *   trung_tam cap 40: ve Trung tam (lang nha), khong tut cap, co dem nguoc Met moi (-75 id 47)
 *   hoenn     cap 90 o Hoenn (Route 110): ve Trung tam -> o lai Hoenn (khong ve Kanto)
 *   pvp       dau truong 202: moi lua chon tai cho bi khoa (ly do PvP), ve Trung tam van duoc
 *   cu        client cu (khong gui -101): 1 giay sau guc nhan menu -803, chon "Ve Trung tam"
 *   dong_doi  2 bot cung doi: A guc, chon cho dong doi; B nhan menu -804, dung Thuoc hoi sinh cuu A
 *   tu_dong   bat Tu dong danh (-100) roi guc: tu dong dung + tu ve Trung tam (khong treo vo han)
 *
 *   javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/*.java
 *   java -cp /tmp/bot -Ddbport=3577 -Ddb=htbb_db -Dtu=41 -Dden=60 KichBanHoiSinh 127.0.0.1 19237 kt [hs_ten...]
 * (tai khoan GM: ten nhan vat trong sv.adminAccounts VA pInfo[16]=1 trong tob_char)
 */
public final class KichBanHoiSinh {

    private KichBanHoiSinh() {
    }

    static final class Hop {
        String tieuDe, moTa;
        final List<int[]> dong = new ArrayList<>(); // {ma, bamDuoc, cho}
        final List<String> chu = new ArrayList<>();

        int[] tim(int ma) {
            for (int[] d : dong) if (d[0] == ma) return d;
            return null;
        }

        String chuCua(int ma) {
            for (int i = 0; i < dong.size(); i++) if (dong.get(i)[0] == ma) return chu.get(i);
            return null;
        }

        @Override
        public String toString() {
            return tieuDe + " | " + moTa + " | " + String.join(" ; ", chu);
        }
    }

    /** Goi -101 MO cuoi cung tu moc. null = khong co. */
    static Hop hop(BotChucNang.Bot b, int tu) {
        Hop h = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -101 || e.d == null || e.d.length == 0) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                if (r.readByte() != 0) continue;
                Hop x = new Hop();
                x.tieuDe = r.readUTF();
                x.moTa = r.readUTF();
                int n = r.readUnsignedByte();
                for (int k = 0; k < n; k++) {
                    int ma = r.readByte();
                    String ten = r.readUTF(), ct = r.readUTF();
                    int duoc = r.readByte();
                    String khoa = r.readUTF();
                    int cho = r.readShort();
                    x.dong.add(new int[]{ma, duoc, cho});
                    x.chu.add(ma + ":" + ten + " [" + ct + "]" + (duoc == 0 ? " KHOA(" + khoa + ")" : "") + (cho > 0 ? " cho" + cho : ""));
                }
                h = x;
            } catch (Exception ignored) {
            }
        }
        return h;
    }

    static Hop xin(BotChucNang.Bot b) throws Exception {
        int m = b.moc();
        b.gui(-101, (byte) 0);
        b.cho(x -> hop(x, m) != null, 4000);
        return hop(b, m);
    }

    static void chon(BotChucNang.Bot b, int ma) throws Exception {
        b.gui(-101, (byte) 1, (byte) ma);
        b.bom(1500);
    }

    static BotChucNang.Bot bot(int lv, int map, int x, int y) throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        KichBanDS.veLang(b);
        b.gm("slv " + b.ten + " " + lv + " 0", 800);
        if (map >= 0) b.den(map, x, y);
        b.lamMoi();
        return b;
    }

    static void guc(BotChucNang.Bot b) throws Exception {
        b.gm("die " + b.ten, 1200);
        b.lamMoi();
    }

    static String tt(BotChucNang.Bot b) {
        return "map=" + b.mapId + " hp=" + b.hp + "/" + b.maxhp + " lv=" + b.lv + " xu=" + b.xu;
    }

    // ------------------------------------------------------------------ kich ban

    static void tanThu() throws Exception {
        try (BotChucNang.Bot b = bot(10, 2, 20, 20)) {
            guc(b);
            Hop h = xin(b);
            BotChucNang.ket("hs.tan_thu.hop", h != null && h.tim(0) != null && h.tim(3) == null, String.valueOf(h));
            BotChucNang.ket("hs.tan_thu.dem_nguoc", h != null && h.tim(0) != null && h.tim(0)[2] > 0, String.valueOf(h));
            chon(b, 0);
            b.lamMoi();
            boolean conGuc = b.hp <= 0;
            Thread.sleep(5200);
            chon(b, 0);
            b.lamMoi();
            BotChucNang.ket("hs.tan_thu.mien_phi", conGuc && b.hp > 0 && b.mapId == 2, "truoc 5s con guc=" + conGuc + " sau: " + tt(b));
        }
    }

    static void thuoc() throws Exception {
        try (BotChucNang.Bot b = bot(40, 2, 20, 20)) {
            b.gm("spt " + b.ten + " 119 2", 800);
            b.lamMoi();
            int t0 = b.soThuoc(119);
            guc(b);
            Thread.sleep(5200);
            Hop h = xin(b);
            chon(b, 1);
            b.lamMoi();
            BotChucNang.ket("hs.thuoc", b.hp > 0 && b.hp <= b.maxhp * 31 / 100 + 1 && b.soThuoc(119) == t0 - 1 && b.mapId == 2,
                    "thuoc " + t0 + " -> " + b.soThuoc(119) + " " + tt(b) + " hop=" + h);
        }
    }

    static long phiTrongHop(Hop h, int ma) {
        String c = h == null ? null : h.chuCua(ma);
        if (c == null) return -1;
        int i = c.indexOf('['), j = c.indexOf(" xu", i);
        if (i < 0 || j < 0) return -1;
        return Long.parseLong(c.substring(i + 1, j).replace(".", "").trim());
    }

    static void xu() throws Exception {
        try (BotChucNang.Bot b = bot(40, 2, 20, 20)) {
            b.gm("smn " + b.ten + " 50000000 0 0", 800);
            b.lamMoi();
            guc(b);
            Thread.sleep(5200);
            Hop h1 = xin(b);
            long p1 = phiTrongHop(h1, 3);
            long xu0 = b.xu;
            chon(b, 3);
            b.lamMoi();
            BotChucNang.ket("hs.xu.lan1", b.hp > 0 && p1 > 0 && xu0 - b.xu == p1, "phi=" + p1 + " xu " + xu0 + " -> " + b.xu + " " + tt(b));
            guc(b);
            Hop h2 = xin(b);
            long p2 = phiTrongHop(h2, 3);
            BotChucNang.ket("hs.xu.lan2_x1.5", p2 == p1 * 3 / 2 && h2.moTa.contains("1,5"), "lan2 phi=" + p2 + " | " + h2);
            BotChucNang.ket("hs.xu.hoi_chieu", h2 != null && h2.tim(3) != null && h2.tim(3)[2] > 40, String.valueOf(h2));
            chon(b, 5);
            b.lamMoi();
        }
    }

    static void trungTam() throws Exception {
        try (BotChucNang.Bot b = bot(40, 2, 20, 20)) {
            b.gm("slv " + b.ten + " 40 50", 800); // giua cap: co tien do de tru
            b.lamMoi();
            int lv0 = b.lv;
            guc(b);
            int m = b.moc();
            chon(b, 5);
            b.cho(x -> x.coLenh(m, 12), 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1500);
            b.lamMoi();
            boolean met = false;
            for (int i = m; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -75 && e.d != null && e.d.length > 3 && ((e.d[1] << 8) | (e.d[2] & 255)) == 47) met = true;
            }
            BotChucNang.ket("hs.trung_tam", b.hp > 0 && KichBanDS.laLang(b.mapId) && b.lv == lv0,
                    tt(b) + " lv " + lv0 + " -> " + b.lv + " " + b.chuTu(m));
            BotChucNang.ket("hs.trung_tam.met_moi", met, "dem nguoc -75 id 47");
            BotChucNang.ket("hs.trung_tam.thong_bao", b.doiChu(m, 500, "Trung tâm Pokémon") != null, b.chuTu(m));
            BotChucNang.ket("hs.trung_tam.mat_exp", b.doiChu(m, 500, " EXP.") != null && b.lv == lv0, b.chuTu(m));
        }
    }

    static void hoenn() throws Exception {
        try (BotChucNang.Bot b = bot(90, 48, 25, 45)) {
            boolean oHoenn = b.mapId == 48;
            guc(b);
            int m = b.moc();
            chon(b, 5);
            b.cho(x -> x.coLenh(m, 12), 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1500);
            b.lamMoi();
            BotChucNang.ket("hs.hoenn", oHoenn && b.hp > 0 && (b.mapId == 47 || b.mapId == 49), "truoc o 48=" + oHoenn + " sau: " + tt(b) + " " + b.tenMap);
        }
    }

    static void pvp() throws Exception {
        try (BotChucNang.Bot b = bot(40, 202, 20, 20)) {
            boolean vao = b.mapId == 202;
            guc(b);
            Thread.sleep(5200);
            Hop h = xin(b);
            boolean khoa = h != null && h.tim(1) != null && h.tim(1)[1] == 0 && h.tim(3) != null && h.tim(3)[1] == 0
                    && h.chuCua(1).contains("PvP") && h.tim(5)[1] == 1;
            BotChucNang.ket("hs.pvp", vao ? khoa : null, vao ? String.valueOf(h) : "khong vao duoc map 202: " + tt(b));
            chon(b, 1);
            b.lamMoi();
            BotChucNang.ket("hs.pvp.may_chu_chan", vao ? b.hp <= 0 : null, tt(b));
            chon(b, 5);
        }
    }

    static void cu() throws Exception {
        try (BotChucNang.Bot b = bot(40, 2, 20, 20)) {
            // client cu (jar 2.7.6, khong biet -101): bam "Hoi sinh" trong hop thoai cua client -> popup -1
            guc(b);
            int m = b.moc();
            b.gui(-32, (short) b.actor, (byte) -1, "", (byte) 1);
            int n = b.doiMenu(m, -803, 0, 3000);
            int vt = b.timMuc("Trung tâm");
            BotChucNang.ket("hs.cu.menu", n > 0 && vt >= 0 && b.hp <= 0, b.chuTu(m));
            int m2 = b.moc();
            b.menu(-803, 0, vt);
            b.cho(x -> x.coLenh(m2, 12), 5000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1200);
            b.lamMoi();
            BotChucNang.ket("hs.cu.ve", b.hp > 0 && KichBanDS.laLang(b.mapId), tt(b));
            // popup cu "hoi sinh tai cho" (id 1, OK) khong con hoi sinh 5% gio thu nhap
            b.den(2, 20, 20);
            guc(b);
            long xu0 = b.xu;
            int m3 = b.moc();
            b.gui(-32, (short) b.actor, (byte) 1, "", (byte) 1);
            b.bom(1500);
            b.lamMoi();
            BotChucNang.ket("hs.cu.popup_ok", b.hp <= 0 && b.xu == xu0 && b.coLenh(m3, -30), tt(b) + " " + b.chuTu(m3));
            b.gui(31, new byte[0]); // "Ve lang" cua client cu = ve Trung tam
            b.bom(2000);
            b.lamMoi();
            BotChucNang.ket("hs.cu.ve_lang_31", b.hp > 0, tt(b));
        }
    }

    static void dongDoi() throws Exception {
        try (BotChucNang.Bot a = KichBanDS4.botLang(40); BotChucNang.Bot c = KichBanDS4.botCungQuoc(40, a)) {
            a.gm("slv " + a.ten + " 40 0", 800);
            c.gm("slv " + c.ten + " 40 0", 800);
            a.den(2, 20, 20);
            c.den(2, 20, 21);
            c.gm("spt " + c.ten + " 119 1", 800);
            a.gui(48, a.actor);
            a.bom(800);
            a.gui(49, (byte) 0, (short) c.actor);
            c.bom(1000);
            c.gui(49, (byte) 1, (short) a.actor);
            a.bom(1000);
            guc(a);
            Thread.sleep(500);
            Hop h = xin(a);
            BotChucNang.ket("hs.dong_doi.lua_chon", h != null && h.tim(6) != null && h.tim(6)[1] == 1, String.valueOf(h));
            int mc = c.moc();
            chon(a, 6);
            int n = c.doiMenu(mc, -804, 0, 3000);
            BotChucNang.ket("hs.dong_doi.moi", n > 0, c.chuTu(mc));
            c.menu(-804, 0, 0);
            c.bom(800);
            a.bom(800);
            a.lamMoi();
            c.lamMoi();
            BotChucNang.ket("hs.dong_doi.cuu", a.hp > 0 && c.soThuoc(119) == 0, "A " + tt(a) + " B thuoc=" + c.soThuoc(119));
        }
    }

    static void tuDong() throws Exception {
        try (BotChucNang.Bot b = bot(40, 2, 20, 20)) {
            int m = b.moc();
            b.gui(-100, (byte) 1, (short) 240, (byte) 0); // bat, khong tu hoi sinh
            b.bom(1500);
            boolean bat = false;
            for (int i = m; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -100 && e.d != null && e.d.length > 1 && e.d[1] == 1) bat = true;
            }
            int m2 = b.moc();
            guc(b);
            b.cho(x -> x.coLenh(m2, 12), 7000);
            b.gui(12, (short) -500, (short) 0, (short) 0);
            b.bom(1200);
            b.lamMoi();
            boolean tat = false;
            for (int i = m2; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -100 && e.d != null && e.d.length > 1 && e.d[1] == 0) tat = true;
            }
            BotChucNang.ket("hs.tu_dong.dung_ve", bat ? tat && b.hp > 0 && KichBanDS.laLang(b.mapId) : null,
                    "bat=" + bat + " tat=" + tat + " " + tt(b) + " " + b.chuTu(m2));
            // bat lai voi Tu hoi sinh + co thuoc: tu dung thuoc tai cho, tu dong chay tiep
            b.den(2, 20, 20);
            b.gm("spt " + b.ten + " 119 1", 800);
            b.gui(-100, (byte) 1, (short) 240, (byte) 1);
            b.bom(1500);
            Thread.sleep(61000); // hoi chieu tai cho tu lan truoc khong anh huong (lan truoc ve Trung tam)
            int m3 = b.moc();
            guc(b);
            b.bom(6500);
            b.lamMoi();
            boolean vanBat = true;
            for (int i = m3; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -100 && e.d != null && e.d.length > 1 && e.d[1] == 0) vanBat = false;
            }
            BotChucNang.ket("hs.tu_dong.tu_hoi_sinh", b.hp > 0 && b.mapId == 2 && b.soThuoc(119) == 0 && vanBat, tt(b) + " vanBat=" + vanBat + " " + b.chuTu(m3));
            b.gui(-100, (byte) 2);
            b.bom(500);
        }
    }

    /** So tay -> Cua hang: dong Thuoc hoi sinh gia theo cap, hop xac nhan ghi dung so xu, tru dung so do. */
    static void mua() throws Exception {
        try (BotChucNang.Bot b = bot(70, -1, 0, 0)) {
            b.gm("smn " + b.ten + " 50000000 0 0", 800);
            b.lamMoi();
            KichBanDS4.moSoTay(b);
            int m = b.moc();
            int vt = -1;
            for (int i = 0; i < b.menuChu.size(); i++) if (b.menuChu.get(i).startsWith("Cửa hàng")) vt = i;
            b.menu(-527, 0, vt);
            b.doiMenu(m, -527, 20, 3000);
            int dong = b.timMuc("Thuốc hồi sinh");
            String chuDong = dong >= 0 ? b.menuChu.get(dong) : "";
            long giaHien = dong >= 0 ? Long.parseLong(chuDong.replaceAll(".* - ([0-9.]+) xu/viên.*", "$1").replace(".", "")) : -1;
            int m2 = b.moc();
            b.menu(-527, 20, dong);
            int n = b.doiMenu(m2, -805, -1, 3000);
            String xn = n > 0 ? b.menuChu.get(0) : "";
            long giaXn = n > 0 ? Long.parseLong(xn.replaceAll(".*trả ([0-9.]+) xu.*", "$1").replace(".", "")) : -2;
            BotChucNang.ket("hs.mua.hien_gia", giaHien > 407 && giaHien == giaXn, "dong=" + chuDong + " | xac nhan=" + xn);
            long xu0 = b.xu;
            int t0 = b.soThuoc(119);
            int capMenu = b.menuCuoi == null ? 0 : b.menuCuoi[1];
            b.menu(-805, capMenu, 0);
            b.bom(1200);
            b.lamMoi();
            BotChucNang.ket("hs.mua.tru_dung", xu0 - b.xu == giaXn && b.soThuoc(119) == t0 + 1, "xu " + xu0 + " -> " + b.xu + " (hien " + giaXn + "), thuoc " + t0 + " -> " + b.soThuoc(119));
            // cua so thuoc NPC kieu cu (goi mua 119) bi tu choi, khong tru tien
            long xu1 = b.xu;
            b.gui(24, (byte) 1, (byte) 4, (short) 119, (short) 1);
            b.bom(1200);
            b.lamMoi();
            BotChucNang.ket("hs.mua.cua_so_cu_chan", b.xu == xu1 && b.soThuoc(119) == t0 + 1, "xu " + xu1 + " -> " + b.xu);
        }
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("hs_tan_thu", true, KichBanHoiSinh::tanThu);
        BotChucNang.dk("hs_thuoc", true, KichBanHoiSinh::thuoc);
        BotChucNang.dk("hs_xu", true, KichBanHoiSinh::xu);
        BotChucNang.dk("hs_trung_tam", true, KichBanHoiSinh::trungTam);
        BotChucNang.dk("hs_hoenn", true, KichBanHoiSinh::hoenn);
        BotChucNang.dk("hs_pvp", true, KichBanHoiSinh::pvp);
        BotChucNang.dk("hs_cu", true, KichBanHoiSinh::cu);
        BotChucNang.dk("hs_dong_doi", true, KichBanHoiSinh::dongDoi);
        BotChucNang.dk("hs_tu_dong", true, KichBanHoiSinh::tuDong);
        BotChucNang.dk("hs_mua", true, KichBanHoiSinh::mua);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) {
            chon.addAll(List.of("hs_tan_thu", "hs_thuoc", "hs_xu", "hs_trung_tam", "hs_hoenn", "hs_pvp", "hs_cu", "hs_dong_doi", "hs_tu_dong", "hs_mua"));
        }
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
