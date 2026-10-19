import com.htbb.pokemon.BanDoSinhThai;
import com.htbb.pokemon.CauCaPhien;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 * 0.9.6.1 cau ca: nhip server (cho ngau nhien, khung keo can, ti le khong duoc gi, nghi giua hai lan, giam theo gio),
 * chong spam tha cau, chi cau sat ho (server kiem vi tri), noi lenh chat, client tha phao xuong NUOC (khong xuong dat).
 */
public final class KiemCauCaNhip {
    private KiemCauCaNhip() {
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        Kiem.nhom("Cau ca 0.9.6.1: nhip, khung keo can, ti le trong, tran gio");
        final CauCaPhien.SoLieu s = CauCaPhien.MAC_DINH;
        CauCaPhien.xoaHet();

        // ---- thoi gian cho: trong [6, 15] s (ho lon +3 s), phan bo deu (seed co dinh)
        final Random r = new Random(42);
        long min = Long.MAX_VALUE, max = 0, tong = 0;
        long minLon = Long.MAX_VALUE, maxLon = 0;
        for (int i = 0; i < 20000; i++) {
            final long c = CauCaPhien.cho(s, false, r);
            min = Math.min(min, c);
            max = Math.max(max, c);
            tong += c;
            final long cl = CauCaPhien.cho(s, true, r);
            minLon = Math.min(minLon, cl);
            maxLon = Math.max(maxLon, cl);
        }
        Kiem.dung("cho ca can >= 6 s (" + min + ")", min >= 6000);
        Kiem.dung("cho ca can <= 15 s (" + max + ")", max <= 15000);
        Kiem.dung("cho trung binh ~10,5 s", Math.abs(tong / 20000 - 10500) < 200);
        Kiem.dung("ho lon cho lau hon: [9, 18] s", minLon >= 9000 && maxLon <= 18000 && maxLon > 15000);

        // ---- mot luot day du: tha -> bao ca can -> keo dung luc
        long t0 = 1_000_000_000L;
        CauCaPhien.Phien p = CauCaPhien.tha(7, 1, 10, 10, 0, false, true, t0, s, new Random(1));
        Kiem.dung("tha cau tao phien", p != null && p.canLuc >= t0 + 6000 && p.canLuc <= t0 + 15000);
        Kiem.dung("tha lan 2 khi dang cau = tu choi (chong spam)",
                CauCaPhien.tha(7, 1, 10, 10, 0, false, true, t0 + 10, s, new Random(2)) == null);
        Kiem.dung("truoc luc ca can: nhip khong bao", CauCaPhien.denLucCan(p.canLuc - 1).isEmpty());
        Kiem.bang("keo truoc khi ca can = SOM (ca thoat)", Integer.valueOf(CauCaPhien.SOM),
                Integer.valueOf(CauCaPhien.phanLoai(p, p.canLuc - 1, s)));
        final long bao = p.canLuc + 150; // nhip 200 ms: bao tre toi 200 ms, khung tinh tu luc bao that
        Kiem.bang("nhip bao ca can dung 1 lan", Integer.valueOf(1), Integer.valueOf(CauCaPhien.denLucCan(bao).size()));
        Kiem.dung("nhip sau khong bao lai", CauCaPhien.denLucCan(bao + 100).isEmpty());
        Kiem.bang("keo ngay luc bao = DUNG", Integer.valueOf(CauCaPhien.DUNG), Integer.valueOf(CauCaPhien.phanLoai(p, bao, s)));
        Kiem.bang("keo sau 1,5 s + bu tre 0,7 s = DUNG", Integer.valueOf(CauCaPhien.DUNG),
                Integer.valueOf(CauCaPhien.phanLoai(p, bao + 2200, s)));
        Kiem.bang("keo sau 2,201 s = MUON", Integer.valueOf(CauCaPhien.MUON), Integer.valueOf(CauCaPhien.phanLoai(p, bao + 2201, s)));
        Kiem.bang("keo dung luc: ket qua DUNG va ket thuc phien", Integer.valueOf(CauCaPhien.DUNG),
                Integer.valueOf(CauCaPhien.keo(7, bao + 800, s)));
        Kiem.dung("het phien sau keo", CauCaPhien.dang(7) == null);
        Kiem.bang("keo lan 2 (khong co phien) = KHONG_CO", Integer.valueOf(CauCaPhien.KHONG_CO),
                Integer.valueOf(CauCaPhien.keo(7, bao + 900, s)));
        // nghi giua hai lan
        Kiem.dung("ngay sau khi xong: phai nghi 3 s", CauCaPhien.conNghi(7, bao + 900, s) > 2800);
        Kiem.bang("sau 3 s: tha duoc", Long.valueOf(0), Long.valueOf(CauCaPhien.conNghi(7, bao + 800 + 3000, s)));

        // ---- keo som: ca thoat, ket thuc phien
        p = CauCaPhien.tha(8, 1, 10, 10, 0, false, true, t0, s, new Random(3));
        Kiem.bang("keo som ket thuc phien = SOM", Integer.valueOf(CauCaPhien.SOM), Integer.valueOf(CauCaPhien.keo(8, t0 + 1000, s)));
        Kiem.dung("keo som: het phien (phai tha lai, mat moi)", CauCaPhien.dang(8) == null);

        // ---- khong keo: het khung = ca thoat (nhip go phien)
        p = CauCaPhien.tha(9, 1, 10, 10, 0, false, true, t0, s, new Random(4));
        CauCaPhien.denLucCan(p.canLuc);
        Kiem.dung("trong khung: chua go", CauCaPhien.quaKhung(p.canLuc + 2200, s).isEmpty() && CauCaPhien.dang(9) != null);
        Kiem.bang("qua khung: go 1 phien", Integer.valueOf(1), Integer.valueOf(CauCaPhien.quaKhung(p.canLuc + 2201, s).size()));
        Kiem.dung("qua khung: het phien + tinh nghi", CauCaPhien.dang(9) == null && CauCaPhien.conNghi(9, p.canLuc + 2300, s) > 0);
        Kiem.bang("keo sau khi ca da thoat = KHONG_CO (khong tra thuong)", Integer.valueOf(CauCaPhien.KHONG_CO),
                Integer.valueOf(CauCaPhien.keo(9, p.canLuc + 2300, s)));

        // ---- huy (roi cho)
        CauCaPhien.tha(10, 1, 10, 10, 0, false, true, t0, s, new Random(5));
        Kiem.dung("huy phien", CauCaPhien.huy(10, t0 + 10) != null && CauCaPhien.dang(10) == null);

        // ---- ti le khong duoc gi: 30% goc, tang 2%/lan sau 30 lan trong gio, tran 75%
        Kiem.bang("ti le trong goc 30%", Integer.valueOf(30), Integer.valueOf(CauCaPhien.tiLeTrong(s, 0)));
        Kiem.bang("29 lan trong gio: van 30%", Integer.valueOf(30), Integer.valueOf(CauCaPhien.tiLeTrong(s, 29)));
        Kiem.bang("lan thu 31 trong gio: 32%", Integer.valueOf(32), Integer.valueOf(CauCaPhien.tiLeTrong(s, 30)));
        Kiem.bang("40 lan: 52%", Integer.valueOf(52), Integer.valueOf(CauCaPhien.tiLeTrong(s, 40)));
        Kiem.bang("tran 75%", Integer.valueOf(75), Integer.valueOf(CauCaPhien.tiLeTrong(s, 1000)));
        final Random rr = new Random(20261019L);
        int trong = 0;
        final int n = 100000;
        for (int i = 0; i < n; i++) {
            trong += CauCaPhien.trong(s, 0, rr) ? 1 : 0;
        }
        Kiem.dung("phan bo trong (seed): 30% +- 1% (" + (trong * 1000L / n) / 10.0 + "%)", Math.abs(trong * 100.0 / n - 30) < 1.0);
        // dem theo gio dong ho
        final long gio = 3600000L * 500000L;
        for (int i = 0; i < 31; i++) {
            CauCaPhien.ghiCauDuoc(11, gio + i * 1000L);
        }
        Kiem.bang("dem cau duoc trong gio", Integer.valueOf(31), Integer.valueOf(CauCaPhien.demTrongGio(11, gio + 40000)));
        Kiem.bang("sang gio moi: dem lai tu 0", Integer.valueOf(0), Integer.valueOf(CauCaPhien.demTrongGio(11, gio + 3600000L)));

        // ---- tran nhip: mot gio cau lien tuc (cho trung binh 10,5 s + phan ung + nghi 3 s) <= ~260 luot,
        //      the luc 100/ngay (2/lan) = 50 luot/ngay la tran cung
        final long chuKy = 10500 + 800 + s.hoiMs;
        Kiem.dung("toi da ~260 luot / gio (chu ky " + chuKy + " ms)", 3600000L / chuKy <= 260);
        Kiem.dung("the luc gioi han 50 lan/ngay", com.htbb.pokemon.HeSinhThai.THE_LUC_NGAY / com.htbb.pokemon.HeSinhThai.TL_CAU_CA == 50);

        // ---- so lieu mac dinh khop danh_muc.json (bang cau_hinh)
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        final String[][] k = {{CauCaPhien.K_CHO_MIN, "6"}, {CauCaPhien.K_CHO_MAX, "15"}, {CauCaPhien.K_CHO_HO_LON, "3"},
            {CauCaPhien.K_CUA_SO, "1500"}, {CauCaPhien.K_TRE, "700"}, {CauCaPhien.K_TRONG, "30"}, {CauCaPhien.K_HOI, "3"},
            {CauCaPhien.K_GIAM_TU, "30"}, {CauCaPhien.K_GIAM_MOI, "2"}, {CauCaPhien.K_TRONG_TOI_DA, "75"}};
        for (final String[] kv : k) {
            Kiem.dung("danh_muc.json co " + kv[0] + " = " + kv[1],
                    dm.contains("\"khoa\": \"" + kv[0] + "\"") && dm.matches("(?s).*\"khoa\": \"" + kv[0].replace(".", "\\.")
                            + "\"[^}]*\"macDinh\": \"" + kv[1] + "\".*"));
            Kiem.bang("CauHinhTrungTam doc mac dinh " + kv[0], Long.valueOf(kv[1]),
                    Long.valueOf(com.htbb.cauhinh.CauHinhTrungTam.so(kv[0], -1)));
        }
        final String sql = doc("../sql/sua_096_1_logic.sql");
        Kiem.dung("sua_096_1_logic.sql co khoi [cau-ca-nhip]", sql.contains("[cau-ca-nhip]") && sql.contains("[/cau-ca-nhip]"));
        Kiem.dung("sua_096_1_logic.sql dang ky 10 khoa cau_ca.*", demChuoi(sql, "('cau_ca.") >= 10);

        // ---- server chi cho cau sat ho (chong gian: khong tin client)
        final int w = 20, h = 20;
        final boolean[] nuoc = new boolean[w * h];
        for (int y = 8; y < 12; y++) {
            for (int x = 8; x < 12; x++) {
                nuoc[y * w + x] = true;
            }
        }
        final int[] nhan = BanDoSinhThai.nhanHo(nuoc, w, h);
        Kiem.dung("ho 4x4 giua map la ho", nhan[9 * w + 9] >= 0);
        Kiem.dung("dung sat bo (cach 1 o): duoc cau", BanDoSinhThai.hoGan(nhan, w, h, 7, 9, 2) >= 0);
        Kiem.dung("cach 2 o: duoc cau", BanDoSinhThai.hoGan(nhan, w, h, 6, 9, 2) >= 0);
        Kiem.dung("cach 3 o (tren dat): server tu choi", BanDoSinhThai.hoGan(nhan, w, h, 5, 9, 2) < 0);
        Kiem.dung("xa ho: server tu choi", BanDoSinhThai.hoGan(nhan, w, h, 1, 1, 2) < 0);

        // ---- noi lenh + nguon
        final String game = doc("src/com/htbb/pokemon/HeSinhThaiGame.java");
        final String menu = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.dung("chat cauca2 -> thaCau(p, true)", game.contains("\"cauca2\".equals(c)") && game.contains("HeSinhThaiMenu.thaCau(p, true)"));
        Kiem.dung("chat keocan -> keoCan", game.contains("\"keocan\".equals(c)") && game.contains("HeSinhThaiMenu.keoCan(p)"));
        Kiem.dung("nhip cau ca 200 ms", game.contains("nhipCauCa, 5000, 200, TimeUnit.MILLISECONDS"));
        Kiem.dung("thaCau kiem ho gan (server) truoc khi tru moi", menu.indexOf("BanDoSinhThai.hoGan") >= 0
                && menu.indexOf("BanDoSinhThai.hoGan") < menu.indexOf("truXu(p, moi, \"moi cau\")"));
        Kiem.dung("bao ca can / ca thoat / khong duoc gi", menu.contains("\"Cá cắn câu! Kéo cần ngay!\"")
                && menu.contains("cá đã thoát mất…") && menu.contains("\"Không câu được gì. Thể lực còn \""));
        Kiem.dung("nhip: roi cho (> 2 o) / doi map = huy", menu.contains("Bạn đã rời chỗ câu"));

        // ---- client: phao chi xuong o NUOC ho, xa nuoc thi bao ngan, nut / Enter = tha hoac keo
        final String st = doc("../client_jar/src/game/pokemon/SinhThai.java");
        final String hd = doc("../client_jar/src/game/pokemon/HanhDong.java");
        Kiem.dung("client: phao = tam o nuoc ho gan nhat (oNuocGan)", st.contains("public static int[] oNuocGan(")
                && st.contains("phaoX = o[0] * 16 + 8;") && !st.contains("cauX + dx * 34"));
        Kiem.dung("client: xa mat nuoc bao 'Hãy đứng gần mặt nước để câu cá'", st.contains("\"Hãy đứng gần mặt nước để câu cá\""));
        Kiem.dung("client: nut cau gui cauca2 / keocan", hd.contains("chat(\"cauca2\")") && hd.contains("chat(\"keocan\")"));
        Kiem.dung("client: ban do nho khong con o vuong toi moi diem cau", !st.contains("g.fillRect(x - 2, y - 3, 6, 6)"));
        CauCaPhien.xoaHet();
    }

    static int demChuoi(final String s, final String t) {
        int n = 0;
        for (int i = s.indexOf(t); i >= 0; i = s.indexOf(t, i + 1)) {
            n++;
        }
        return n;
    }
}
