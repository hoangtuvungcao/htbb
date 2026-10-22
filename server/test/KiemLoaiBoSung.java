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
 *   1. Du lieu (sql/htbb_day_du.sql, khoi them_loai_bo_sung.sql): MOI 1.025 loai co mo ta tieng Viet (1-2 cau, <= 255,
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
        Kiem.dung("htbb_day_du co data_loai + data_loai_sprite", l != null && s != null);
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
        // 64 loai PMDCollab chua co sprite dung duoc; #879 Copperajah da co sprite (ban PMDCollab chua xong, Walk/Idle) dong
        // san trong jar tu truoc -> 63 loai chua co hinh trong game
        Kiem.bang("so loai chua co sprite (chi chan dung Pokedex)", Integer.valueOf(63), Integer.valueOf(khongHinh));
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
