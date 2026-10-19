import com.htbb.pokemon.GoiThoiTiet;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HeSinhThai;
import com.htbb.pokemon.SinhThaiTheGioi;
import com.htbb.pokemon.ThoiTietLich;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thoi tiet theo khung gio, anh huong co tran, phu mau mua, goi -118 (de xuat 8): com.htbb.pokemon.ThoiTietLich,
 * GoiThoiTiet, sql/them_thoi_tiet_mua.sql.
 */
public final class KiemThoiTiet {

    private KiemThoiTiet() {
    }

    public static void chay() {
        lich();
        anhHuong();
        mauMua();
        goi();
        sql();
        noiGame();
    }

    static int ngay(final Calendar c) {
        return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }

    static void lich() {
        Kiem.nhom("Thoi tiet: lich 4 khung gio / ngay theo vung (tat dinh)");
        Kiem.bang("khung 0h = 0", 0, ThoiTietLich.khung(0));
        Kiem.bang("khung 5h = 0", 0, ThoiTietLich.khung(5));
        Kiem.bang("khung 6h = 1", 1, ThoiTietLich.khung(6));
        Kiem.bang("khung 13h = 2", 2, ThoiTietLich.khung(13));
        Kiem.bang("khung 23h = 3", 3, ThoiTietLich.khung(23));
        Kiem.bang("4 khung x 6 gio = 24 gio", 24, ThoiTietLich.SO_KHUNG * ThoiTietLich.GIO_KHUNG);
        Kiem.bang("ngay sau 31/12/2026 = 1/1/2027", 20270101, ThoiTietLich.ngaySau(20261231));
        Kiem.bang("ngay sau 28/02/2028 = 29/02 (nam nhuan)", 20280229, ThoiTietLich.ngaySau(20280228));
        Kiem.bang("ngay sau 28/02/2027 = 01/03", 20270301, ThoiTietLich.ngaySau(20270228));
        Kiem.bang("cung (ngay, khung, vung) = cung thoi tiet", ThoiTietLich.thoiTiet(20261019, 2, SinhThaiTheGioi.V_HO, null),
                ThoiTietLich.thoiTiet(20261019, 2, SinhThaiTheGioi.V_HO, null));

        final int nV = SinhThaiTheGioi.MA_VUNG.length;
        final int[][] dem = new int[nV][SinhThaiTheGioi.SO_THOI_TIET];
        int doiKhung = 0, tongChuyen = 0, ngoaiKhoang = 0;
        final Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2027, 0, 1);
        for (int d = 0; d < 365; d++) {
            final int n = ngay(c);
            for (int v = 0; v < nV; v++) {
                int truoc = -1;
                for (int k = 0; k < ThoiTietLich.SO_KHUNG; k++) {
                    final int tt = ThoiTietLich.thoiTiet(n, k, v, null);
                    if (tt < 0 || tt >= SinhThaiTheGioi.SO_THOI_TIET) {
                        ngoaiKhoang++;
                        continue;
                    }
                    dem[v][tt]++;
                    if (v == SinhThaiTheGioi.V_DONG_CO && k > 0) {
                        tongChuyen++;
                        doiKhung += tt != truoc ? 1 : 0;
                    }
                    truoc = tt;
                }
            }
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        final int moiVung = 365 * ThoiTietLich.SO_KHUNG;
        Kiem.bang("moi khung ra thoi tiet hop le", 0, ngoaiKhoang);
        Kiem.bang("hang dong: luon quang", moiVung, dem[SinhThaiTheGioi.V_HANG][SinhThaiTheGioi.QUANG]);
        Kiem.bang("trong nha: luon quang", moiVung, dem[SinhThaiTheGioi.V_TRONG_NHA][SinhThaiTheGioi.QUANG]);
        Kiem.bang("dau truong: luon quang", moiVung, dem[SinhThaiTheGioi.V_DAU_TRUONG][SinhThaiTheGioi.QUANG]);
        Kiem.dung("vung tuyet: tuyet > 50% khung (" + dem[SinhThaiTheGioi.V_TUYET][SinhThaiTheGioi.TUYET] + ")",
                dem[SinhThaiTheGioi.V_TUYET][SinhThaiTheGioi.TUYET] > moiVung / 2);
        Kiem.dung("bien: co gio manh (" + dem[SinhThaiTheGioi.V_BIEN][SinhThaiTheGioi.GIO] + " khung)",
                dem[SinhThaiTheGioi.V_BIEN][SinhThaiTheGioi.GIO] > 60);
        Kiem.bang("sa mac: khong gio manh (da co bao cat)", 0, dem[SinhThaiTheGioi.V_SA_MAC][SinhThaiTheGioi.GIO]);
        Kiem.bang("ho: khong bao gio bao cat", 0, dem[SinhThaiTheGioi.V_HO][SinhThaiTheGioi.BAO_CAT]);
        Kiem.bang("dong co: khong tuyet (khong co su kien)", 0, dem[SinhThaiTheGioi.V_DONG_CO][SinhThaiTheGioi.TUYET]);
        // giu khung truoc 55%: ti le doi khung trong khoang 20..60% (troi khong nhay loan, nhung co doi trong ngay)
        final int pt = doiKhung * 100 / Math.max(1, tongChuyen);
        Kiem.dung("dong co: doi thoi tiet giua hai khung lien tiep " + pt + "% (20..60)", pt >= 20 && pt <= 60);
        // du bao: khung ke tiep khop thoi tiet that cua khung do (qua ngay)
        final int[] db = ThoiTietLich.duBao(20261019, 3, SinhThaiTheGioi.V_BIEN, null, 2);
        Kiem.bang("du bao khung sau khung 3 = khung 0 ngay sau", ThoiTietLich.thoiTiet(20261020, 0, SinhThaiTheGioi.V_BIEN, null), db[0]);
        Kiem.bang("du bao 2 khung = khung 1 ngay sau", ThoiTietLich.thoiTiet(20261020, 1, SinhThaiTheGioi.V_BIEN, null), db[1]);
        // su kien: Halloween them suong mu (ngoai troi), khong chiu vao hang
        final Set<String> hl = new HashSet<String>(Arrays.asList("halloween"));
        final int[] cong = ThoiTietLich.congSuKien(ThoiTietLich.SU_KIEN_MAC_DINH, hl, SinhThaiTheGioi.V_DUONG);
        Kiem.dung("Halloween: cong trong so suong mu", cong != null && cong[SinhThaiTheGioi.SUONG_MU] > 0);
        Kiem.bang("Halloween: hang van luon quang", SinhThaiTheGioi.QUANG,
                ThoiTietLich.thoiTiet(20261031, 2, SinhThaiTheGioi.V_HANG, ThoiTietLich.congSuKien(ThoiTietLich.SU_KIEN_MAC_DINH, hl,
                        SinhThaiTheGioi.V_HANG)));
        int smThuong = 0, smHl = 0;
        for (int d = 1; d <= 28; d++) {
            for (int k = 0; k < 4; k++) {
                smThuong += ThoiTietLich.thoiTiet(20261000 + d, k, SinhThaiTheGioi.V_DUONG, null) == SinhThaiTheGioi.SUONG_MU ? 1 : 0;
                smHl += ThoiTietLich.thoiTiet(20261000 + d, k, SinhThaiTheGioi.V_DUONG, cong) == SinhThaiTheGioi.SUONG_MU ? 1 : 0;
            }
        }
        Kiem.dung("Halloween: duong nhieu suong mu hon (" + smHl + " > " + smThuong + ")", smHl > smThuong);
        final int[] noel = ThoiTietLich.trongSo(SinhThaiTheGioi.V_THI_TRAN, 12,
                ThoiTietLich.congSuKien(ThoiTietLich.SU_KIEN_MAC_DINH, new HashSet<String>(Arrays.asList("noel")), SinhThaiTheGioi.V_THI_TRAN));
        Kiem.dung("Noel thang 12: thi tran co tuyet", noel[SinhThaiTheGioi.TUYET] > 0);
        final int[] khongTuyet = ThoiTietLich.trongSo(SinhThaiTheGioi.V_SA_MAC, 6,
                ThoiTietLich.congSuKien(ThoiTietLich.SU_KIEN_MAC_DINH, new HashSet<String>(Arrays.asList("noel")), SinhThaiTheGioi.V_SA_MAC));
        Kiem.bang("su kien khong tao tuyet o sa mac giua nam", 0, khongTuyet[SinhThaiTheGioi.TUYET]);
        final ThoiTietLich.SuKienThoiTiet lo = new ThoiTietLich.SuKienThoiTiet("x", -1, SinhThaiTheGioi.MUA, 999, 0, 0);
        Kiem.bang("trong so su kien kep <= 60", ThoiTietLich.TRAN_TRONG_SO_SU_KIEN, lo.trongSo);
    }

    static void anhHuong() {
        Kiem.nhom("Thoi tiet: anh huong chieu / sinh quai nho, co tran");
        ThoiTietLich.datBang(null);
        Kiem.bang("mua: Nuoc +10%", 110, ThoiTietLich.satThuong(HePokemon.NUOC, SinhThaiTheGioi.MUA));
        Kiem.bang("mua: Lua -10%", 90, ThoiTietLich.satThuong(HePokemon.LUA, SinhThaiTheGioi.MUA));
        Kiem.bang("nang: Lua +10%", 110, ThoiTietLich.satThuong(HePokemon.LUA, SinhThaiTheGioi.NANG));
        Kiem.bang("gio: Bay +10%", 110, ThoiTietLich.satThuong(HePokemon.BAY, SinhThaiTheGioi.GIO));
        Kiem.bang("gio: Rong khong doi sat thuong", 100, ThoiTietLich.satThuong(HePokemon.RONG, SinhThaiTheGioi.GIO));
        Kiem.dung("gio: Rong sinh nhieu", ThoiTietLich.heHop(HePokemon.RONG, SinhThaiTheGioi.GIO));
        Kiem.bang("SinhThaiTheGioi dung chung bang (mua Nuoc)", 110, SinhThaiTheGioi.satThuongPhanTram(HePokemon.NUOC, SinhThaiTheGioi.MUA));
        int lech = 0;
        for (int he = 0; he < HePokemon.SO_HE; he++) {
            for (int tt = 0; tt < SinhThaiTheGioi.SO_THOI_TIET; tt++) {
                lech = Math.max(lech, Math.abs(ThoiTietLich.satThuong(he, tt) - 100));
            }
        }
        Kiem.dung("moi he x moi thoi tiet trong +-10% (" + lech + ")", lech <= ThoiTietLich.TRAN_SAT_THUONG_PHAN_TRAM);
        // CSDL ghi qua tran: van kep
        ThoiTietLich.datBang(new ThoiTietLich.AnhHuong[]{new ThoiTietLich.AnhHuong(SinhThaiTheGioi.MUA, new int[]{HePokemon.NUOC},
                new int[]{HePokemon.LUA}, new int[]{HePokemon.NUOC}, 80, "")});
        Kiem.bang("CSDL ghi 80%: kep +10%", 110, ThoiTietLich.satThuong(HePokemon.NUOC, SinhThaiTheGioi.MUA));
        Kiem.bang("CSDL ghi 80%: kep -10%", 90, ThoiTietLich.satThuong(HePokemon.LUA, SinhThaiTheGioi.MUA));
        Kiem.bang("dong thieu giu mac dinh (nang Lua +10%)", 110, ThoiTietLich.satThuong(HePokemon.LUA, SinhThaiTheGioi.NANG));
        // he vua tang vua giam (CSDL sai): cong lai, van trong tran
        ThoiTietLich.datBang(new ThoiTietLich.AnhHuong[]{new ThoiTietLich.AnhHuong(SinhThaiTheGioi.GIO, new int[]{HePokemon.BAY},
                new int[]{HePokemon.BAY}, new int[0], 10, "")});
        Kiem.bang("he vua tang vua giam = 100", 100, ThoiTietLich.satThuong(HePokemon.BAY, SinhThaiTheGioi.GIO));
        ThoiTietLich.datBang(null);
        Kiem.bang("he so sinh kep <= 3", Math.min(3, HeSinhThai.HE_SO_SINH_HOP_THOI_TIET),
                ThoiTietLich.heSoSinh(HePokemon.NUOC, -1, SinhThaiTheGioi.MUA));
        Kiem.bang("he so sinh loai khong hop = 1", 1, ThoiTietLich.heSoSinh(HePokemon.LUA, -1, SinhThaiTheGioi.MUA));
        final String g = ThoiTietLich.goiY(SinhThaiTheGioi.MUA);
        Kiem.dung("goi y mua co Nuoc +10% va Lua (" + g + ")", g.contains("Nước +10%") && g.contains("Lửa −10%"));
        Kiem.dung("goi y quang khong co %", !ThoiTietLich.goiY(SinhThaiTheGioi.QUANG).contains("%"));
        Kiem.bang("7 ten thoi tiet", 7, SinhThaiTheGioi.TEN_THOI_TIET.length);
        Kiem.bang("hieu ung -39 cho gio = la roi (client cu)", 1, SinhThaiTheGioi.HIEU_UNG_CLIENT[SinhThaiTheGioi.GIO]);
        Kiem.bang("thoi tiet theo ngay cu khong ra gio (giu ket qua cu)", 0, demGioNgayCu());
    }

    static int demGioNgayCu() {
        int n = 0;
        for (int d = 1; d <= 28; d++) {
            for (int v = 0; v < SinhThaiTheGioi.MA_VUNG.length; v++) {
                n += SinhThaiTheGioi.thoiTiet(20270100 + d, v) == SinhThaiTheGioi.GIO ? 1 : 0;
            }
        }
        return n;
    }

    static void mauMua() {
        Kiem.nhom("Phu mau mua / su kien");
        Kiem.bang("thang 3 = xuan", ThoiTietLich.MAU_XUAN, ThoiTietLich.mauMua(SinhThaiTheGioi.V_DUONG, 3, 0));
        Kiem.bang("thang 6 = ha", ThoiTietLich.MAU_HA, ThoiTietLich.mauMua(SinhThaiTheGioi.V_DUONG, 6, 0));
        Kiem.bang("thang 9 = thu", ThoiTietLich.MAU_THU, ThoiTietLich.mauMua(SinhThaiTheGioi.V_DUONG, 9, 0));
        Kiem.bang("thang 1 = dong", ThoiTietLich.MAU_DONG, ThoiTietLich.mauMua(SinhThaiTheGioi.V_DUONG, 1, 0));
        Kiem.bang("trong nha: khong phu", ThoiTietLich.MAU_KHONG, ThoiTietLich.mauMua(SinhThaiTheGioi.V_TRONG_NHA, 10, ThoiTietLich.MAU_HALLOWEEN));
        Kiem.bang("hang: khong phu", ThoiTietLich.MAU_KHONG, ThoiTietLich.mauMua(SinhThaiTheGioi.V_HANG, 10, 0));
        final Set<String> hl = new HashSet<String>(Arrays.asList("halloween", "he2017"));
        Kiem.bang("Halloween thang mua thu", ThoiTietLich.MAU_HALLOWEEN, ThoiTietLich.mauMua(SinhThaiTheGioi.V_RUNG, 10,
                ThoiTietLich.mauSuKien(ThoiTietLich.SU_KIEN_MAC_DINH, hl)));
        Kiem.bang("Tet uu tien hon Noel (60 > 50)", ThoiTietLich.MAU_TET, ThoiTietLich.mauSuKien(ThoiTietLich.SU_KIEN_MAC_DINH,
                new HashSet<String>(Arrays.asList("noel", "tet2017"))));
        Kiem.bang("khong su kien = khong mau su kien", ThoiTietLich.MAU_KHONG, ThoiTietLich.mauSuKien(ThoiTietLich.SU_KIEN_MAC_DINH,
                new HashSet<String>()));
        Kiem.bang("so ten mau = SO_MAU", ThoiTietLich.SO_MAU, ThoiTietLich.TEN_MAU.length);
    }

    static void goi() {
        Kiem.nhom("Goi -118 THOI_TIET (client >= 908)");
        try {
            final GoiThoiTiet.TrangThai t = new GoiThoiTiet.TrangThai();
            t.thoiTiet = SinhThaiTheGioi.MUA;
            t.ten = "Mưa";
            t.goiY = ThoiTietLich.goiY(SinhThaiTheGioi.MUA);
            t.giayConLai = 3600;
            t.tiepTheo = SinhThaiTheGioi.GIO;
            t.mauMua = ThoiTietLich.MAU_HALLOWEEN;
            t.tenMauMua = ThoiTietLich.TEN_MAU[t.mauMua];
            t.congDong = 2;
            t.tenDong = "Gastly";
            t.giayCongDong = 1200;
            t.coTrenMap = true;
            t.goiYCongDong = "Dòng Gastly";
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            GoiThoiTiet.ghi(new DataOutputStream(bo), t);
            final GoiThoiTiet.TrangThai r = GoiThoiTiet.doc(new DataInputStream(new ByteArrayInputStream(bo.toByteArray())));
            Kiem.bang("ghi roi doc lai: khoa trang thai", t.khoa(), r.khoa());
            Kiem.bang("ghi roi doc lai: giay con lai", 3600, r.giayConLai);
            Kiem.bang("ghi roi doc lai: giay cong dong", 1200, r.giayCongDong);
            Kiem.bang("ghi roi doc lai: ten mau", t.tenMauMua, r.tenMauMua);
            Kiem.bang("lenh -118", -118, GoiThoiTiet.LENH);
            Kiem.bang("chi client >= 908 (0.9.7 = 907 da phat hanh khong co -118)", 908, GoiThoiTiet.BAN_CLIENT);
            // client doc cung thu tu truong (game.pokemon.ThoiTiet.docGoi)
            final String cl = doc("../client_jar/src/game/pokemon/ThoiTiet.java");
            Kiem.dung("client co LENH = -118", cl.contains("LENH = -118"));
            final int a = cl.indexOf("static void docGoi");
            final String than = a < 0 ? "" : cl.substring(a, Math.min(cl.length(), a + 1600));
            final String[] thu = {"readUnsignedByte", "readByte", "readUTF", "readUTF", "readInt", "readByte", "readByte", "readUTF",
                "readUnsignedByte", "readUTF", "readInt", "readUnsignedByte", "readUTF"};
            final Matcher m = Pattern.compile("read(UnsignedByte|Byte|UTF|Int)\\(").matcher(than);
            final StringBuilder b = new StringBuilder();
            while (m.find()) {
                b.append(b.length() == 0 ? "" : ",").append("read").append(m.group(1));
            }
            Kiem.bang("client doc dung thu tu truong goi -118", String.join(",", thu), b.toString());
            // -118 chua ai dung (server + client)
            Kiem.dung("server chi GoiThoiTiet dung -118", demChuoi("src", "-118") <= 3);
        } catch (final Exception e) {
            Kiem.dung("goi -118: " + e, false);
        }
    }

    static int demChuoi(final String goc, final String chuoi) {
        final int[] n = {0};
        try {
            Files.walk(Paths.get(goc)).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    final String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                    if (s.contains(chuoi) && !p.toString().endsWith("GoiThoiTiet.java") && !p.toString().endsWith("ThoiTietGame.java")) {
                        n[0]++;
                    }
                } catch (final Exception ignored) {
                }
            });
        } catch (final Exception e) {
            return 99;
        }
        return n[0];
    }

    static void sql() {
        Kiem.nhom("sql/them_thoi_tiet_mua.sql (idempotent, khop mac dinh)");
        final String s = doc("../sql/them_thoi_tiet_mua.sql");
        Kiem.dung("doc duoc tep", s.length() > 1000);
        Kiem.sai("khong DROP TABLE", s.contains("DROP TABLE"));
        Kiem.sai("khong INSERT ... SELECT ... ON DUPLICATE", Pattern.compile("INSERT[^;]*SELECT[^;]*ON DUPLICATE", Pattern.DOTALL).matcher(s).find());
        Kiem.sai("khong ON DUPLICATE col=col", Pattern.compile("`(\\w+)`=`\\1`").matcher(s).find());
        Kiem.dung("CREATE TABLE IF NOT EXISTS du 5 bang", s.split("CREATE TABLE IF NOT EXISTS").length - 1 == 5);
        Kiem.sai("khong utf8mb4 (MariaDB 10.3 utf8)", s.contains("utf8mb4"));
        // data_thoi_tiet khop ThoiTietLich.MAC_DINH
        final Matcher m = Pattern.compile("\\((\\d),'(\\w+)','([^']*)','([\\d,]*)','([\\d,]*)','([\\d,]*)',(\\d+),'([^']*)'\\)").matcher(s);
        int n = 0, lech = 0;
        while (m.find()) {
            n++;
            final ThoiTietLich.AnhHuong a = ThoiTietLich.MAC_DINH[Integer.parseInt(m.group(1))];
            lech += csv(a.heTang).equals(m.group(4)) && csv(a.heGiam).equals(m.group(5)) && csv(a.heSinh).equals(m.group(6))
                    && a.phanTram == Integer.parseInt(m.group(7)) && a.goiY.equals(m.group(8))
                    && SinhThaiTheGioi.TEN_THOI_TIET[a.thoiTiet].equals(m.group(3)) ? 0 : 1;
        }
        Kiem.bang("data_thoi_tiet 7 dong", 7, n);
        Kiem.bang("data_thoi_tiet khop ThoiTietLich.MAC_DINH", 0, lech);
        final Matcher k = Pattern.compile("\\('(\\w+)',(-?\\d+),(\\d),(\\d+),(\\d),(\\d+)\\)").matcher(s);
        int nk = 0, lk = 0;
        while (k.find()) {
            final ThoiTietLich.SuKienThoiTiet x = ThoiTietLich.SU_KIEN_MAC_DINH[nk++];
            lk += x.maSuKien.equals(k.group(1)) && x.vung == Integer.parseInt(k.group(2)) && x.thoiTiet == Integer.parseInt(k.group(3))
                    && x.trongSo == Integer.parseInt(k.group(4)) && x.mauMua == Integer.parseInt(k.group(5))
                    && x.uuTien == Integer.parseInt(k.group(6)) ? 0 : 1;
        }
        Kiem.bang("data_thoi_tiet_su_kien khop mac dinh", ThoiTietLich.SU_KIEN_MAC_DINH.length + "/0", nk + "/" + lk);
        Kiem.dung("tep co trong sql/README.md", doc("../sql/README.md").contains("them_thoi_tiet_mua.sql"));
    }

    static String csv(final int[] a) {
        final StringBuilder b = new StringBuilder();
        for (final int x : a) {
            b.append(b.length() == 0 ? "" : ",").append(x);
        }
        return b.toString();
    }

    static void noiGame() {
        Kiem.nhom("Thoi tiet / mua: diem moc trong game");
        final String hst = doc("src/com/htbb/pokemon/HeSinhThaiGame.java");
        Kiem.dung("thoiTietVung dung lich khung gio", hst.contains("ThoiTietGame.thoiTietVung(vung)"));
        Kiem.dung("sat thuong kep qua ThoiTietLich.satThuong", hst.contains("ThoiTietLich.satThuong(he, thoiTietMap(self.mapID))"));
        Kiem.dung("client >= 908 nhan -118 thay -39", hst.contains("ThoiTietGame.gui(c, ep)"));
        final String gs = doc("../client_jar/src/game/render/screen/GameScr.java");
        Kiem.dung("GameScr ve thoi tiet / phu mua truoc phu dem", gs.indexOf("game.pokemon.ThoiTietVe.veTheGioi(g)") >= 0
                && gs.indexOf("game.pokemon.ThoiTietVe.veTheGioi(g)") < gs.indexOf("game.pokemon.SinhThai.veDem(g)"));
        Kiem.dung("GameScr ve bieu tuong thoi tiet HUD", gs.contains("game.pokemon.ThoiTiet.veHud(g)"));
        final String mh = doc("../client_jar/src/game/networklogic/MessageHandler.java");
        Kiem.dung("MessageHandler xu ly -118", mh.contains("game.pokemon.ThoiTiet.LENH"));
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
