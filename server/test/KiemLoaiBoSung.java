import com.htbb.dulieu.BangSql;
import com.htbb.pokemon.ChonLoaiHoangDa;
import com.htbb.pokemon.ChuPokemon;
import com.htbb.pokemon.GoiLoai;
import com.htbb.pokemon.LoaiDuLieu;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Bo sung 1.025 loai (loai-bo-sung): toan ven du lieu / tai nguyen va tuong thich goi.
 *
 *   1. Du lieu (sql/htbb.sql, khoi them_loai_bo_sung.sql): MOI 1.025 loai co mo ta tieng Viet (1-2 cau, <= 255,
 *      co dau, khong tu cam ChuPokemon.TU_CAM); moi loai DA MO co goi sprite (server/map/loai/NNNN.goi) co khoi neo phu
 *      kien (.dl doc duoc bang dinh dang jar, co 'N'); data_loai_sprite.shiny = 1 <=> co tep NNNNs.goi dung dau (dex,
 *      shiny = 1) + ghi cong shiny; data_loai_sprite.neo = 1 cho moi loai co goi; loai khong co sprite (trang_thai 0)
 *      khong co dong noi song o dot dang mo (khong xuat hien ngoai troi).
 *   2. Logic: ChonLoaiHoangDa.shiny (ti le, he so su kien, loai khong co ban sac mau khong bao gio shiny); loai
 *      khong sprite khong bao gio duoc chon; ti le thuc nghiem ~ 1/4096 va x5 gio vang.
 *   3. Goi: -125 ghi shiny = 1 cho quai shiny; client cu (< loai.ban_client 908) van thay loai mau (khong nhan -125,
 *      khong shiny); -126 kieu 0 shiny = 1 doc dung ten goi "NNNNs.goi"; duoi the Pokedex (-121 kieu 5) chi them CUOI goi.
 */
public final class KiemLoaiBoSung {

    static final File GOI = new File("map/loai");

    public static void chay() {
        Kiem.nhom("KiemLoaiBoSung: mo ta 1.025 loai, shiny, neo phu kien, goi -125/-126 tuong thich");
        try {
            duLieu();
            moLoai();
            logic();
            goi();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("KiemLoaiBoSung chay het khong loi: " + e, false);
        }
    }

    static Map<Integer, Integer> cot(final BangSql b, final String c) {
        final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        if (b != null && b.coCot(c)) {
            for (int r = 0; r < b.soDong(); r++) {
                m.put(b.so(r, "dex"), b.so(r, c));
            }
        }
        return m;
    }

    static void duLieu() throws Exception {
        final BangSql l = DayDu.bang("data_loai");
        final BangSql s = DayDu.bang("data_loai_sprite");
        Kiem.dung("htbb.sql co data_loai + data_loai_sprite", l != null && s != null);
        if (l == null || s == null) {
            return;
        }
        Kiem.dung("data_loai_sprite co cot shiny / neo / kich_thuoc_shiny / ghi_cong_shiny",
                s.coCot("shiny") && s.coCot("neo") && s.coCot("kich_thuoc_shiny") && s.coCot("ghi_cong_shiny"));
        int coMoTa = 0, moTaLoi = 0, moTaCam = 0;
        final List<String> loi = new ArrayList<String>();
        final Map<Integer, Integer> bat = new HashMap<Integer, Integer>();
        final java.util.Set<String> trung = new java.util.HashSet<String>();
        int soTrung = 0;
        for (int r = 0; r < l.soDong(); r++) {
            final int dex = l.so(r, "dex");
            bat.put(dex, l.so(r, "bat"));
            final String m = l.chu(r, "mo_ta");
            if (m.isEmpty()) {
                continue;
            }
            coMoTa++;
            if (!trung.add(m)) {
                soTrung++;
            }
            final int cau = m.split("[.!?…](\\s|$)", -1).length - 1;
            if (m.length() > 255 || cau < 1 || cau > 2 || !m.matches("(?s).*[à-ỹ].*") || m.indexOf('\n') >= 0) {
                moTaLoi++;
                if (loi.size() < 10) {
                    loi.add("#" + dex + " (" + m.length() + " ky tu, " + cau + " cau)");
                }
            }
            if (!ChuPokemon.tuCamTrong(m).isEmpty()) {
                moTaCam++;
                if (loi.size() < 10) {
                    loi.add("#" + dex + " tu cam " + ChuPokemon.tuCamTrong(m));
                }
            }
        }
        for (final String x : loi) {
            System.out.println("        " + x);
        }
        Kiem.bang("so loai co mo ta Pokedex (1.025)", Integer.valueOf(1025), Integer.valueOf(coMoTa));
        Kiem.bang("mo ta 1-2 cau, <= 255 ky tu, co dau", Integer.valueOf(0), Integer.valueOf(moTaLoi));
        Kiem.bang("mo ta khong co tu cam (thuat ngu)", Integer.valueOf(0), Integer.valueOf(moTaCam));
        Kiem.bang("mo ta khong trung nhau", Integer.valueOf(0), Integer.valueOf(soTrung));

        final Map<Integer, Integer> trangThai = cot(s, "trang_thai"), shiny = cot(s, "shiny"), neo = cot(s, "neo");
        int daMo = 0, coGoi = 0, coNeo = 0, soShiny = 0, shinyLech = 0, neoLech = 0, goiHong = 0, khongSpriteMo = 0;
        final Map<Integer, String> gcShiny = new HashMap<Integer, String>();
        for (int r = 0; r < s.soDong(); r++) {
            gcShiny.put(s.so(r, "dex"), s.chu(r, "ghi_cong_shiny"));
        }
        for (int dex = 1; dex <= LoaiDuLieu.SO_LOAI; dex++) {
            final boolean mo = bat.containsKey(dex) && bat.get(dex) != 0;
            final File f = new File(GOI, GoiLoai.tenGoi(dex)), fs = new File(GOI, GoiLoai.tenGoi(dex, true));
            if (mo) {
                daMo++;
                if (trangThai.getOrDefault(dex, 0) <= 0) {
                    khongSpriteMo++;
                }
            }
            if (f.isFile()) {
                coGoi++;
                final byte[] b = Files.readAllBytes(f.toPath());
                if (GoiLoai.docGoi(b, dex) == null || GoiLoai.shinyCuaGoi(b) != 0) {
                    goiHong++;
                } else if (coKhoiNeo(b, PokeKhoa.k(dex, false))) {
                    coNeo++;
                }
                if (neo.getOrDefault(dex, 0) != 1) {
                    neoLech++;
                }
            } else if (mo) {
                goiHong++;
                System.out.println("        loai da mo #" + dex + " thieu goi " + f);
            }
            final boolean coTepShiny = fs.isFile();
            if (coTepShiny) {
                soShiny++;
                final byte[] b = Files.readAllBytes(fs.toPath());
                if (GoiLoai.docGoi(b, dex) == null || GoiLoai.shinyCuaGoi(b) != 1 || !coKhoiNeo(b, PokeKhoa.k(dex, true))) {
                    goiHong++;
                    System.out.println("        goi shiny hong / thieu neo: " + fs);
                }
                if (gcShiny.getOrDefault(dex, "").isEmpty()) {
                    shinyLech++;
                }
            }
            if ((shiny.getOrDefault(dex, 0) == 1) != coTepShiny) {
                shinyLech++;
                System.out.println("        data_loai_sprite.shiny #" + dex + " = " + shiny.get(dex) + " nhung tep shiny "
                        + (coTepShiny ? "co" : "khong co"));
            }
        }
        Kiem.bang("moi loai da mo (" + daMo + ") co goi sprite", Integer.valueOf(daMo), Integer.valueOf(Math.min(daMo, coGoi)));
        Kiem.bang("moi goi co khoi neo phu kien", Integer.valueOf(coGoi), Integer.valueOf(coNeo));
        Kiem.bang("goi hong / thieu", Integer.valueOf(0), Integer.valueOf(goiHong));
        Kiem.bang("data_loai_sprite.neo khop goi", Integer.valueOf(0), Integer.valueOf(neoLech));
        Kiem.bang("data_loai_sprite.shiny khop tep NNNNs.goi + co ghi cong", Integer.valueOf(0), Integer.valueOf(shinyLech));
        Kiem.dung("co ban sac mau cho phan lon loai da mo (" + soShiny + ")", soShiny >= 200);
        Kiem.bang("loai da mo khong co sprite", Integer.valueOf(0), Integer.valueOf(khongSpriteMo));
        // loai KHONG co sprite (64 loai PMDCollab chua ve): khong co noi song o dot dang mo, khong co goi
        final BangSql v = DayDu.bang("data_loai_vung");
        int vungKhongHinh = 0;
        for (int r = 0; v != null && r < v.soDong(); r++) {
            if (trangThai.getOrDefault(v.so(r, "dex"), 0) <= 0) {
                vungKhongHinh++;
            }
        }
        Kiem.bang("loai khong co sprite khong co noi song (khong xuat hien ngoai troi)", Integer.valueOf(0),
                Integer.valueOf(vungKhongHinh));
        int khongHinh = 0, khongHinhCoGoi = 0;
        for (final Map.Entry<Integer, Integer> e : trangThai.entrySet()) {
            if (e.getValue() <= 0) {
                khongHinh++;
                if (new File(GOI, GoiLoai.tenGoi(e.getKey())).isFile()) {
                    khongHinhCoGoi++;
                }
            }
        }
        // 64 loai PMDCollab chua co sprite hoan chinh: 22 ban nhap / dang khac PMDCollab, 42 sprite tran dau Smogon Sprite
        // Project / Black-White goc doi sang 4 huong (du_lieu/loai/sprite_ngoai.csv) -> moi loai deu co hinh trong game
        Kiem.bang("so loai chua co sprite", Integer.valueOf(0), Integer.valueOf(khongHinh));
        final BangSql spr = DayDu.bang("data_loai_sprite");
        int smogon = 0, bw = 0;
        for (int r = 0; spr != null && r < spr.soDong(); r++) {
            final String ng = spr.chu(r, "nguon_sprite");
            if ("smogon".equals(ng) && spr.so(r, "ban_nhap") == 1) {
                smogon++;
            } else if ("bw_goc".equals(ng) && spr.so(r, "ban_nhap") == 1) {
                bw++;
            }
        }
        Kiem.bang("nguon_sprite smogon (Gen 6-9, ban_nhap = 1)", Integer.valueOf(29), Integer.valueOf(smogon));
        Kiem.bang("nguon_sprite bw_goc (Gen 5, ban_nhap = 1)", Integer.valueOf(13), Integer.valueOf(bw));
        final String gc = new String(Files.readAllBytes(new File("map/loai/GHI_CONG.txt").toPath()), "UTF-8");
        Kiem.dung("GHI_CONG ghi Smogon Sprite Project + nguoi ve (KingOfThe-X-Roads, leParagon)", gc.contains("Smogon Sprite Project")
                && gc.contains("KingOfThe-X-Roads") && gc.contains("leParagon") && gc.contains("Black / White"));
        Kiem.bang("loai chua co sprite khong co goi gia", Integer.valueOf(0), Integer.valueOf(khongHinhCoGoi));
        int chanDung = 0;
        for (int dex = 1; dex <= LoaiDuLieu.SO_LOAI; dex++) {
            if (new File(GOI, GoiLoai.tenGoi(dex)).isFile() || new File("../web/public/pokemon/loai/" + dex + ".png").isFile()
                    || new File("../web/public/pokemon/" + dex + ".png").isFile()) {
                chanDung++;
            }
        }
        Kiem.bang("moi loai co chan dung Pokedex (goi hoac anh tinh PMDCollab)", Integer.valueOf(1025), Integer.valueOf(chanDung));
    }

    // ------------------------------------------------------------------ mo loai: Gen 3 Hoenn + Vung hoang da

    static void moLoai() throws Exception {
        final BangSql l = DayDu.bang("data_loai"), s = DayDu.bang("data_loai_sprite"), v = DayDu.bang("data_loai_vung");
        final BangSql xv = DayDu.bang("data_loai_xoay_vong"), lich = DayDu.bang("data_loai_xoay_vong_lich");
        final BangSql bd = DayDu.bang("data_loai_xoay_vong_map"), hst = DayDu.bang("data_hst_vung");
        Kiem.dung("co bang xoay vong (lich, ban do, loai)", xv != null && lich != null && bd != null);
        if (l == null || s == null || v == null || xv == null || lich == null || bd == null) {
            return;
        }
        final Map<Integer, Integer> tt = cot(s, "trang_thai");
        final Map<Integer, int[]> info = new HashMap<Integer, int[]>();   // {the_he, bac, nhom, giai_doan, bat}
        for (int r = 0; r < l.soDong(); r++) {
            info.put(l.so(r, "dex"), new int[]{l.so(r, "the_he"), l.so(r, "bac"), l.so(r, "nhom"), l.so(r, "giai_doan"), l.so(r, "bat")});
        }
        final int[] moTheoThe = new int[10];
        int moKhongHinh = 0;
        for (final Map.Entry<Integer, int[]> e : info.entrySet()) {
            if (e.getValue()[4] == 1) {
                moTheoThe[e.getValue()[0]]++;
                if (tt.getOrDefault(e.getKey(), 0) <= 0) {
                    moKhongHinh++;
                }
            }
        }
        System.out.println("        da mo theo the he: " + java.util.Arrays.toString(moTheoThe));
        Kiem.bang("Gen 3 mo du 135 loai (dot B)", Integer.valueOf(135), Integer.valueOf(moTheoThe[3]));
        Kiem.bang("khong loai nao da mo ma khong co sprite", Integer.valueOf(0), Integer.valueOf(moKhongHinh));
        // bang noi song (moi khu vuc) + xoay vong: chi loai CO SPRITE (co goi / jar), khong T6, khong huyen thoai
        int vungLoi = 0, hoenn = 0, hoennNgoaiDai = 0;
        final java.util.Set<Integer> loaiHoenn = new java.util.HashSet<Integer>();
        for (int r = 0; r < v.soDong(); r++) {
            final int dex = v.so(r, "dex");
            final int[] x = info.get(dex);
            if (tt.getOrDefault(dex, 0) <= 0 || x == null || x[1] >= 6 || x[4] != 1
                    || !new File(GOI, GoiLoai.tenGoi(dex)).isFile() && !new File("../client_jar/res/pk/" + PokeKhoa.k(dex, false) + ".dl").isFile()) {
                vungLoi++;
            }
            if ("Hoenn".equals(v.chu(r, "khu_vuc"))) {
                hoenn++;
                loaiHoenn.add(dex);
                if (v.so(r, "cap_min") < 83 || v.so(r, "cap_max") > 100 || v.so(r, "cap_min") > v.so(r, "cap_max")) {
                    hoennNgoaiDai++;
                }
            }
        }
        Kiem.bang("data_loai_vung chi loai co sprite, da mo, khong T6", Integer.valueOf(0), Integer.valueOf(vungLoi));
        Kiem.dung("Hoenn co noi song Gen 3 (" + loaiHoenn.size() + " loai, " + hoenn + " dong)", loaiHoenn.size() >= 100);
        Kiem.bang("noi song Hoenn trong dai cap ban do Hoenn 83-100", Integer.valueOf(0), Integer.valueOf(hoennNgoaiDai));
        int xvLoi = 0, xvCap = 0, thapLoi = 0, soThap = 0;
        final Map<Integer, java.util.Set<Integer>> theoKy = new HashMap<Integer, java.util.Set<Integer>>();
        for (int r = 0; r < xv.soDong(); r++) {
            final int dex = xv.so(r, "dex"), ky = xv.so(r, "chu_ky");
            final int[] x = info.get(dex);
            theoKy.computeIfAbsent(ky, k -> new java.util.TreeSet<Integer>()).add(dex);
            if (x == null || tt.getOrDefault(dex, 0) <= 0 || !new File(GOI, GoiLoai.tenGoi(dex)).isFile() && !new File(
                    "../client_jar/res/pk/" + PokeKhoa.k(dex, false) + ".dl").isFile() || x[1] < 1 || x[1] > 4 || x[2] != 0
                    || x[3] < 1 || x[4] != 1 || x[0] < 4 || x[0] != 4 + ky || xv.so(r, "bac_gap") != x[1]) {
                xvLoi++;
            }
            if (xv.so(r, "nhom") == 0 && (xv.so(r, "cap_min") < 50 || xv.so(r, "cap_max") > 85)) {
                xvCap++;
            }
            if (xv.so(r, "nhom") == 1) {
                soThap++;
                // cap thap: CHI dang dau (giai doan 1), T1-T3, dai cap 5-40
                if (x == null || x[3] != 1 || x[1] > 3 || xv.so(r, "cap_min") < 5 || xv.so(r, "cap_max") > 40) {
                    thapLoi++;
                }
            }
        }
        Kiem.bang("xoay vong: chi loai co sprite, T1-T4, nhom 0 (khong huyen thoai / huyen ao / UB / Nghich ly), dung the he chu ky",
                Integer.valueOf(0), Integer.valueOf(xvLoi));
        Kiem.bang("xoay vong: dai cap 50-85", Integer.valueOf(0), Integer.valueOf(xvCap));
        Kiem.dung("xoay vong cap thap co dong (" + soThap + ")", soThap > 0);
        Kiem.bang("xoay vong cap thap: chi dang dau, T1-T3, cap 5-40", Integer.valueOf(0), Integer.valueOf(thapLoi));
        Kiem.bang("lich xoay vong 6 tuan (Sinnoh .. Paldea)", Integer.valueOf(6), Integer.valueOf(lich.soDong()));
        Kiem.bang("moi chu ky deu co loai", Integer.valueOf(6), Integer.valueOf(theoKy.size()));
        final java.util.Set<Integer> mapHst = new java.util.HashSet<Integer>();
        for (int r = 0; hst != null && r < hst.soDong(); r++) {
            mapHst.add(hst.so(r, "map_id"));
        }
        int mapLoi = 0, mapThap = 0;
        for (int r = 0; r < bd.soDong(); r++) {
            if (!mapHst.contains(bd.so(r, "map_id")) || !new File("map/map" + bd.so(r, "map_id")).isFile()) {
                mapLoi++;
            }
            if (bd.so(r, "nhom") == 1) {
                mapThap++;
            }
        }
        Kiem.dung("3-4 ban do xoay vong cap thap (" + mapThap + ")", mapThap >= 3 && mapThap <= 4);
        Kiem.dung("ban do xoay vong co that (map + vung sinh thai): " + bd.soDong(), bd.soDong() >= 4 && mapLoi == 0);
        // xac dinh: cung ngay -> cung chu ky; thu Hai doi chu ky; truoc ngay goc van hop le
        final long goc = java.time.LocalDate.parse(lich.chu(0, "ngay_goc")).toEpochDay();
        Kiem.bang("ngay goc (thu Hai) = chu ky 0", Integer.valueOf(0), Integer.valueOf(LoaiDuLieu.chuKy(goc, goc, 6)));
        Kiem.bang("Chu nhat cung tuan = chu ky 0", Integer.valueOf(0), Integer.valueOf(LoaiDuLieu.chuKy(goc + 6, goc, 6)));
        Kiem.bang("thu Hai tuan sau = chu ky 1", Integer.valueOf(1), Integer.valueOf(LoaiDuLieu.chuKy(goc + 7, goc, 6)));
        Kiem.bang("6 tuan sau quay lai chu ky 0", Integer.valueOf(0), Integer.valueOf(LoaiDuLieu.chuKy(goc + 42, goc, 6)));
        Kiem.bang("tuan truoc ngay goc = chu ky 5", Integer.valueOf(5), Integer.valueOf(LoaiDuLieu.chuKy(goc - 1, goc, 6)));
        Kiem.bang("khong co lich = -1", Integer.valueOf(-1), Integer.valueOf(LoaiDuLieu.chuKy(goc, goc, 0)));
        Kiem.dung("ngay goc la thu Hai", java.time.LocalDate.ofEpochDay(goc).getDayOfWeek() == java.time.DayOfWeek.MONDAY);
        // TRAN: ti le moi bac gap duoc KHONG vuot ti le cau hinh khi them ung vien xoay vong (chi them loai, khong them luot)
        final List<LoaiDuLieu.Loai> ds = new ArrayList<LoaiDuLieu.Loai>();
        final List<LoaiDuLieu.Vung> vg = new ArrayList<LoaiDuLieu.Vung>();
        final LoaiDuLieu.Loai a = loai(500, 1, 2), b = loai(501, 3, 2), c = loai(502, 4, 2), e5 = loai(503, 5, 2);
        ds.add(a);
        ds.add(b);
        ds.add(c);
        ds.add(e5);
        vg.add(new LoaiDuLieu.Vung(500, "Johto", 3, 1, 100, 100, 0, -1, 1));
        final LoaiDuLieu d = new LoaiDuLieu(ds, Collections.<LoaiDuLieu.TienHoa>emptyList(), vg, Collections.<Integer, String>emptyMap());
        final List<int[]> dong = new ArrayList<int[]>();
        dong.add(new int[]{0, 0, 501, 3, 50, 85, 100, 3});
        dong.add(new int[]{0, 0, 502, 3, 50, 85, 100, 4});
        dong.add(new int[]{1, 0, 503, 3, 50, 85, 100, 5});
        dong.add(new int[]{0, 1, 501, 3, 5, 40, 100, 3});
        final List<LoaiDuLieu.TuanXoay> lt = new ArrayList<LoaiDuLieu.TuanXoay>();
        lt.add(new LoaiDuLieu.TuanXoay(0, "A", "", 4));
        lt.add(new LoaiDuLieu.TuanXoay(1, "B", "", 5));
        final Map<Integer, Integer> nhomBd = new HashMap<Integer, Integer>();
        nhomBd.put(Integer.valueOf(20), Integer.valueOf(0));
        nhomBd.put(Integer.valueOf(5), Integer.valueOf(1));
        d.datXoayVong(lt, goc, nhomBd, dong);
        Kiem.dung("ban do 20 la ban do xoay vong", d.laBanDoXoay(20) && !d.laBanDoXoay(21));
        Kiem.bang("nhom ban do: 20 -> 0, 5 -> 1, 21 -> -1", "0/1/-1", d.nhomBanDoXoay(20) + "/" + d.nhomBanDoXoay(5) + "/" + d.nhomBanDoXoay(21));
        Kiem.bang("chu ky 0 nhom 0 vung 3 co 2 loai", Integer.valueOf(2), Integer.valueOf(d.xoayVong(0, 0, 3).size()));
        Kiem.bang("chu ky 0 nhom 1 vung 3 co 1 loai", Integer.valueOf(1), Integer.valueOf(d.xoayVong(0, 1, 3).size()));
        final String[] bn = real.LoaiQuaiGame.chuBanner(d, 0);
        Kiem.dung("banner doi tuan: ten tuan + so loai", bn != null && "Vùng hoang dã: A".equals(bn[0]) && bn[1].contains("2 loài"));
        Kiem.dung("banner: chu ky ngoai lich -> khong co", real.LoaiQuaiGame.chuBanner(d, 7) == null);
        final String nap = new String(Files.readAllBytes(new File("src/real/CharNapDuLieu.java").toPath()), "UTF-8");
        Kiem.dung("banner khi dang nhap dau tuan (CharNapDuLieu goi LoaiQuaiGame.khiDangNhap)", nap.contains("LoaiQuaiGame.khiDangNhap(self);"));
        final ChonLoaiHoangDa.ThamSo ts = new ChonLoaiHoangDa.ThamSo();
        final Random r1 = new Random(11), r2 = new Random(11);
        final int lan = 600_000;
        final int[] khong = new int[7], co = new int[7];
        for (int i = 0; i < lan; i++) {
            khong[ChonLoaiHoangDa.chon(d, "Johto", 3, 60, 12, false, -1, 0, ts, r1).bac]++;
            co[ChonLoaiHoangDa.chon(d, "Johto", 3, 60, 12, false, -1, 0, ts, r2, d.xoayVong(0, 0, 3)).bac]++;
        }
        final double p3 = 1.0 / ts.gapT3, p4 = 1.0 / ts.gapT4;
        Kiem.dung("khong xoay vong: khong co T3/T4 (vung khong co ung vien)", khong[3] == 0 && khong[4] == 0);
        Kiem.dung("xoay vong: T3 gap " + co[3] + " <= tran " + (int) (lan * p3 * 1.1), co[3] > 0 && co[3] <= lan * p3 * 1.1);
        Kiem.dung("xoay vong: T4 gap " + co[4] + " <= tran " + (int) (lan * p4 * 1.2), co[4] > 0 && co[4] <= lan * p4 * 1.2);
        Kiem.dung("ti le doi loai T1-T2 giu nguyen (" + khong[1] + " / " + co[1] + ")", Math.abs(khong[1] - co[1]) < lan * 0.005);
        Kiem.bang("T5 trong chu ky khac khong lot sang", Integer.valueOf(0), Integer.valueOf(co[5]));
        // dong T5 / T6 trong xoay vong (neu co ai them tay) cung bi chan o bo loc bac
        Kiem.bang("bo chon khong bao gio tra T6", Integer.valueOf(0), Integer.valueOf(co[6] + khong[6]));
        // 0.9.8: bieu ngu tuan (SU_KIEN) - tieu de + mo ta + nhac luat hoang da; chu ky -1 / khong lich -> khong tin
        final String[] tin = real.LoaiQuaiGame.chuBanner(d, 1);
        Kiem.dung("banner tuan: 'Vung hoang da: B' + nhac chi bat con hoang da", tin != null && tin[0].equals("Vùng hoang dã: B")
                && tin[1].contains("chỉ bắt được con hoang dã"));
        Kiem.dung("banner tuan: chu ky -1 -> khong gui", real.LoaiQuaiGame.chuBanner(d, -1) == null);
        Kiem.dung("banner: chua toi ngay goc thi khong co tuan", lqTuan());
        final String lq;
        try {
            lq = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/LoaiQuaiGame.java")), java.nio.charset.StandardCharsets.UTF_8);
        } catch (final java.io.IOException ex) {
            throw new RuntimeException(ex);
        }
        Kiem.dung("lich xoay vong chua chay truoc ngay goc (chuKyHienTai = -1)", lq.contains("hom < d.ngayGocXoay() ? -1"));
        Kiem.dung("xoay vong van qua gioi han khu hoang da (GIU) truoc khi tung shiny",
                lq.indexOf("ts, R, xoay);") > 0 && lq.indexOf("ts, R, xoay);") < lq.indexOf("kq = ChonLoaiHoangDa.GIU;"));
    }

    static boolean lqTuan() {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/LoaiQuaiGame.java")),
                    java.nio.charset.StandardCharsets.UTF_8).contains("hom < d.ngayGocXoay() ? Long.MIN_VALUE");
        } catch (final java.io.IOException e) {
            return false;
        }
    }

    /** Ten khoa loai giong client (PokeBody.tenKhoa). */
    static final class PokeKhoa {
        static String k(final int dex, final boolean shiny) {
            return String.format("%04d", dex) + (shiny ? "s" : "");
        }
    }

    /** Tep .dl cua goi co khoi neo 'N' ngay sau phan dinh dang jar (doc bang dinh dang PokeDuLieu ban 2). */
    static boolean coKhoiNeo(final byte[] goi, final String k) throws Exception {
        for (final GoiLoai.Tep t : GoiLoai.docGoi(goi, 0)) {
            if (t.ten.equals(k + ".dl")) {
                return viTriNeo(t.du) >= 0;
            }
        }
        return false;
    }

    /** Vi tri byte 'N' cua khoi neo trong .dl (-1 = khong co). */
    static int viTriNeo(final byte[] dl) throws Exception {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(dl));
        if (in.readUnsignedByte() != 2) {
            return -1;
        }
        final int n = in.readUnsignedByte();
        int p = 2, neo = 0;
        for (int i = 0; i < n; i++) {
            in.readUnsignedShort();
            in.readUnsignedShort();
            final int sk = in.readUnsignedByte();
            in.skipBytes(sk + 4 + 8);
            p += 4 + 1 + sk + 4 + 8;
            neo += 4 * sk * 8;   // 4 huong x so khung x 8 byte (TinhNeoPoke)
        }
        p += 14;
        // khoi neo du do dai: 'N' + moi dong tac 4 huong x so khung x 8 byte, het tep
        return p < dl.length && dl[p] == 'N' && p + 1 + neo == dl.length ? p : -1;
    }

    static void logic() {
        final List<LoaiDuLieu.Loai> ds = new ArrayList<LoaiDuLieu.Loai>();
        final LoaiDuLieu.Loai a = loai(10, 1, 2), b = loai(11, 1, 2), c = loai(12, 1, 0);
        a.coShiny = true;
        ds.add(a);
        ds.add(b);
        ds.add(c);
        final List<LoaiDuLieu.Vung> vung = new ArrayList<LoaiDuLieu.Vung>();
        vung.add(new LoaiDuLieu.Vung(10, "Kanto", 1, 1, 100, 100, 0, -1, 1));
        vung.add(new LoaiDuLieu.Vung(11, "Kanto", 1, 1, 100, 100, 0, -1, 1));
        vung.add(new LoaiDuLieu.Vung(12, "Kanto", 1, 1, 100, 100000, 0, -1, 1));
        final LoaiDuLieu d = new LoaiDuLieu(ds, Collections.<LoaiDuLieu.TienHoa>emptyList(), vung,
                Collections.<Integer, String>emptyMap());
        Kiem.dung("shiny: roll 0 / 4096 -> shiny (loai co ban sac mau)", ChonLoaiHoangDa.shiny(d, 10, 0, 4096, 1));
        Kiem.sai("shiny: roll 1 / 4096 x1 -> khong", ChonLoaiHoangDa.shiny(d, 10, 1, 4096, 1));
        Kiem.dung("shiny: roll 4 / 4096 x5 (gio vang ngay cong dong) -> shiny", ChonLoaiHoangDa.shiny(d, 10, 4, 4096, 5));
        Kiem.sai("shiny: roll 5 / 4096 x5 -> khong", ChonLoaiHoangDa.shiny(d, 10, 5, 4096, 5));
        Kiem.sai("shiny: loai KHONG co ban sac mau khong bao gio shiny", ChonLoaiHoangDa.shiny(d, 11, 0, 4096, 10));
        Kiem.sai("shiny: dex 0 khong shiny", ChonLoaiHoangDa.shiny(d, 0, 0, 4096, 10));
        final Random r = new Random(7);
        int n1 = 0, n5 = 0;
        final int lan = 2_000_000;
        for (int i = 0; i < lan; i++) {
            if (ChonLoaiHoangDa.shiny(d, 10, r.nextInt(4096), 4096, 1)) {
                n1++;
            }
            if (ChonLoaiHoangDa.shiny(d, 10, r.nextInt(4096), 4096, 5)) {
                n5++;
            }
        }
        final double k1 = lan / 4096.0;
        Kiem.dung("ti le shiny thuc nghiem ~ 1/4096 (" + n1 + " / ky vong " + (int) k1 + ")", Math.abs(n1 - k1) < k1 * 0.15);
        Kiem.dung("gio vang x5 (" + n5 + " ~ " + (int) (5 * k1) + ")", Math.abs(n5 - 5 * k1) < 5 * k1 * 0.08);
        // loai khong sprite (12, trong so rat lon) khong bao gio duoc chon
        final ChonLoaiHoangDa.ThamSo ts = new ChonLoaiHoangDa.ThamSo();
        ts.doiThuongPhanTram = 100;
        int chon12 = 0, chonKhac = 0;
        for (int i = 0; i < 20000; i++) {
            final ChonLoaiHoangDa.KetQua kq = ChonLoaiHoangDa.chon(d, "Kanto", 1, 10, 12, false, -1, 0, ts, r);
            if (kq.dex == 12) {
                chon12++;
            } else if (kq.dex > 0) {
                chonKhac++;
            }
        }
        Kiem.bang("loai khong co sprite khong bao gio duoc chon", Integer.valueOf(0), Integer.valueOf(chon12));
        Kiem.dung("loai co sprite van duoc chon (" + chonKhac + ")", chonKhac > 1000);
        Kiem.bang("mo ta mac dinh rong", "", d.moTa(11));
        Kiem.dung("coShiny theo co cua loai", d.coShiny(10) && !d.coShiny(11));
    }

    static LoaiDuLieu.Loai loai(final int dex, final int bac, final int sprite) {
        return new LoaiDuLieu.Loai(dex, "L" + dex, 1, 0, -1, new int[]{50, 50, 50, 50, 50, 50}, 0, dex, 0, 1, bac, 400,
                true, false, true, sprite, true, sprite > 0, false);
    }

    static void goi() throws Exception {
        // -125: quai shiny -> byte shiny = 1; quai thuong -> 0
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final List<int[]> ds = new ArrayList<int[]>();
        ds.add(new int[]{5, 25, 1, 2});
        ds.add(new int[]{6, 19, 0, 1});
        GoiLoai.ghiQuaiCon(new DataOutputStream(bo), ds);
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
        Kiem.bang("-125 n", Integer.valueOf(2), Integer.valueOf(in.readUnsignedByte()));
        Kiem.bang("-125 quai 1 id", Integer.valueOf(5), Integer.valueOf(in.readShort()));
        Kiem.bang("-125 quai 1 dex", Integer.valueOf(25), Integer.valueOf(in.readShort()));
        Kiem.bang("-125 quai 1 shiny", Integer.valueOf(1), Integer.valueOf(in.readUnsignedByte()));
        in.readUnsignedByte();
        in.readShort();
        in.readShort();
        Kiem.bang("-125 quai 2 shiny", Integer.valueOf(0), Integer.valueOf(in.readUnsignedByte()));
        Kiem.bang("-125 do dai khong doi (6 byte / quai)", Integer.valueOf(1 + 2 * 6), Integer.valueOf(bo.size()));
        // client cu (<= 907): khong nhan -125 -> thay loai mau (khong co loai rieng / shiny rieng); 908 thay loai rieng
        Kiem.bang("client 907 thay loai mau", Integer.valueOf(16), Integer.valueOf(real.LoaiQuaiGame.dexHienThi(25, 16, 907, 908)));
        Kiem.bang("client PC Unity (-1) thay loai mau", Integer.valueOf(16), Integer.valueOf(real.LoaiQuaiGame.dexHienThi(25, 16, -1, 908)));
        Kiem.bang("client 908 thay loai rieng", Integer.valueOf(25), Integer.valueOf(real.LoaiQuaiGame.dexHienThi(25, 16, 908, 908)));
        Kiem.sai("shinyChoNguoi(null) = false (khong client)", real.LoaiQuaiGame.shinyChoNguoi(null, null));
        // ten goi
        Kiem.bang("ten goi thuong", "0016.goi", GoiLoai.tenGoi(16, false));
        Kiem.bang("ten goi shiny", "0016s.goi", GoiLoai.tenGoi(16, true));
        Kiem.bang("ten goi shiny 4 so", "1025s.goi", GoiLoai.tenGoi(1025, true));
        // -126 kieu 0 khuc shiny: dau khuc giu dung bo cuc cu (kieu, dex, shiny, tong, viTri, len)
        final ByteArrayOutputStream bk = new ByteArrayOutputStream();
        GoiLoai.ghiKhucSprite(new DataOutputStream(bk), 16, 1, new byte[10], 0);
        final DataInputStream ik = new DataInputStream(new ByteArrayInputStream(bk.toByteArray()));
        ik.readUnsignedByte();
        Kiem.bang("-126 khuc dex", Integer.valueOf(16), Integer.valueOf(ik.readShort()));
        Kiem.bang("-126 khuc shiny", Integer.valueOf(1), Integer.valueOf(ik.readUnsignedByte()));
        Kiem.bang("-126 khuc tong", Integer.valueOf(10), Integer.valueOf(ik.readInt()));
        // duoi the Pokedex: client 908 cu (truoc bo sung) doc toi chi so goc roi dung - duoi chi o CUOI goi
        final String nguon = new String(Files.readAllBytes(new File("src/real/BatPokemonGame.java").toPath()), "UTF-8");
        final int a = nguon.indexOf("static void guiLoai(");
        final int goc = nguon.indexOf("o.writeByte(Math.min(255, g[i]));", a);
        final int moTa = nguon.indexOf("o.writeUTF(catUtf(ld.moTa(dex)", a);
        final int gui = nguon.indexOf("p.sendMessage(m);", a);
        Kiem.dung("duoi mo ta + co shiny ghi SAU chi so goc, truoc khi gui", a > 0 && goc > a && moTa > goc && gui > moTa);
        final String client = new String(Files.readAllBytes(new File("../client_jar/src/game/pokemon/BatPokemon.java").toPath()), "UTF-8");
        Kiem.dung("client doc duoi tuy chon (r.available())", client.contains("if (r.available() >= 2) {")
                && client.contains("l.moTa = r.readUTF();"));
    }
}
