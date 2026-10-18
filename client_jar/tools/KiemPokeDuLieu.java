import game.pokemon.AmThanh;
import game.pokemon.ChuPokemon;
import game.pokemon.HePoke;
import game.pokemon.PokeDongTac;
import game.pokemon.PokeDuLieu;
import game.pokemon.TenMapPokemon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiem thu thuan Java (JDK) cho phan Pokemon cua ban jar:
 *  1. PokeDuLieu (ban J2ME) doc MOI Resources/poke/*_dl.bytes cua HTBB, doi chieu tung truong voi
 *     mot bo doc doc lap (DataInputStream) va kiem moi khung nam trong atlas _anh.bytes;
 *  2. moi res/pk/*.dl doc duoc, anh dong tac ton tai va dung kich thuoc (soKhung*rong x 4*cao);
 *  3. (tuy chon) ChuPokemon / TenMapPokemon cua jar cho KET QUA GIONG HET ban server
 *     com.htbb.pokemon.ChuPokemon.doiLuon / TenMapPokemon.doi (regex) tren tap chu kiem.
 *
 *   java -cp &lt;lop jar&gt;:&lt;midp.jar&gt;:res[:lop server] KiemPokeDuLieu &lt;client_pc_ios/Assets (hoac client_pc_ios/ClientUnity/Assets cu)&gt; res [nguon java ...]
 */
public class KiemPokeDuLieu {
    static int loi = 0;

    static void sai(String s) {
        loi++;
        if (loi < 30) {
            System.out.println("SAI: " + s);
        }
    }

    public static void main(String[] a) throws Exception {
        File poke = new File(a[0], "Resources/poke");
        File res = new File(a[1]);
        int soDl = 0;
        String[] ten = poke.list();
        Arrays.sort(ten);
        for (String t : ten) {
            if (!t.endsWith("_dl.bytes")) {
                continue;
            }
            soDl++;
            byte[] b = Files.readAllBytes(new File(poke, t).toPath());
            PokeDuLieu dl = PokeDuLieu.doc(b);
            if (dl == null) {
                sai(t + ": PokeDuLieu.doc tra ve null");
                continue;
            }
            soSanhDocLap(t, b, dl);
            int[] wh = kichThuocPng(Files.readAllBytes(new File(poke, t.replace("_dl", "_anh")).toPath()));
            for (int i = 0; i < dl.dongTac.length; i++) {
                PokeDongTac d = dl.dongTac[i];
                if (d.xAtlas + d.soKhung * d.rongKhung > wh[0] || d.yHang(3) + d.caoKhung > wh[1]) {
                    sai(t + " dong tac " + i + " ra ngoai atlas " + wh[0] + "x" + wh[1]);
                }
                int f = d.khungTai(PokeDuLieu.doiTick(1000000), PokeDuLieu.laDongTacLap(i));
                if (f < 0 || f >= d.soKhung || !PokeDuLieu.laDongTacLap(i) && f != d.soKhung - 1) {
                    sai(t + " khungTai sai");
                }
            }
            // dl hong (cat cut) phai ra null, khong nem ngoai le
            if (PokeDuLieu.doc(Arrays.copyOf(b, 10)) != null) {
                sai(t + ": tep cat cut van doc duoc");
            }
        }
        System.out.println("Unity _dl: " + soDl + " tep doc dung");

        File pk = new File(res, "pk");
        int soJar = 0;
        for (String t : pk.list()) {
            if (!t.endsWith(".dl")) {
                continue;
            }
            soJar++;
            String khoa = t.substring(0, t.length() - 3);
            PokeDuLieu dl = PokeDuLieu.doc(Files.readAllBytes(new File(pk, t).toPath()));
            if (dl == null || dl.dongTac.length != 6) {
                sai(t + " khong doc duoc");
                continue;
            }
            for (int i = 0; i < 6; i++) {
                PokeDongTac d = dl.dongTac[i];
                File png = new File(pk, khoa + "_" + dl.anhCua(i) + ".png");
                if (!png.exists()) {
                    sai(png + " thieu");
                    continue;
                }
                int[] wh = kichThuocPng(Files.readAllBytes(png.toPath()));
                if (wh[0] != d.soKhung * d.rongKhung || wh[1] != 4 * d.caoKhung) {
                    sai(png + " kich thuoc " + wh[0] + "x" + wh[1]);
                }
            }
            if (!new File(pk, khoa + "_c.png").exists()) {
                sai(khoa + " thieu chan dung");
            }
        }
        System.out.println("jar .dl: " + soJar + " tep, anh dong tac khop kich thuoc");
        try {
            DongGoiPokeJ2me.loaiGan = DongGoiPokeJ2me.loaiGan(DongGoiPokeJ2me.thuMucDocs(new File(a[0])));
        } catch (java.io.IOException e) {
            DongGoiPokeJ2me.loaiGan = null;
        }
        soSanhHinh(poke, pk, DongGoiPokeJ2me.DAY_DU);
        kiemHieuUng(poke, pk, DongGoiPokeJ2me.DAY_DU.toiDaHu);
        File nhe = new File(res.getAbsoluteFile().getParentFile(), "res_nhe/pk");
        if (nhe.isDirectory()) {
            System.out.println("--- ban nhe " + nhe);
            soSanhHinh(poke, nhe, DongGoiPokeJ2me.NHE);
            kiemHieuUng(poke, nhe, DongGoiPokeJ2me.NHE.toiDaHu);
            if (!new File(nhe, "bong.dl").exists() || !new File(nhe, "bong_c.png").exists()) {
                sai("ban nhe thieu bong den");
            }
        }

        kiemAmThanh(res);
        kiemHoCau(new File(a[0]));
        kiemFilePackDaLuong();
        kiemHe();
        kiemChu(a);
        System.out.println(loi == 0 ? "OK" : ("HONG: " + loi + " loi"));
        System.exit(loi == 0 ? 0 : 1);
    }

    /**
     * Ve tung khung giu lai cua ban Unity (atlas goc) va ban jar (da cat, bot khung) theo CUNG cong
     * thuc PokeBody (diem chan tai goc toa do) va so tung pixel: cat/bot khung khong duoc lam lech hinh.
     */
    static void soSanhHinh(File poke, File pk, DongGoiPokeJ2me.Cau cauVao) throws Exception {
        int soKhung = 0;
        DongGoiPokeJ2me.Cau cau = cauVao;
        for (String t : pk.list()) {
            cau = cauVao;
            if (!t.endsWith(".dl")) {
                continue;
            }
            String khoa = t.substring(0, t.length() - 3);
            if (khoa.equals("bong")) {
                continue;   // bong den: hinh doi mau, khong so pixel
            }
            PokeDuLieu goc = PokeDuLieu.doc(Files.readAllBytes(new File(poke, khoa + "_dl.bytes").toPath()));
            PokeDuLieu moi = PokeDuLieu.doc(Files.readAllBytes(new File(pk, t).toPath()));
            int caoGoc = goc.dongTac[0].caoKhung / 2 + goc.dongTac[0].dyChan[0];
            if (moi.caoTrenGoc != caoGoc) {
                sai(khoa + " caoTrenGoc " + moi.caoTrenGoc + " != " + caoGoc);
            }
            java.awt.image.BufferedImage atlas = javax.imageio.ImageIO.read(new File(poke, khoa + "_anh.bytes"));
            // ban day du: loai chi la quai (khong nhin gan) dong goi kieu XA (it khung) - xem DongGoiPokeJ2me
            String gocKhoa = khoa.endsWith("s") ? khoa.substring(0, khoa.length() - 1) : khoa;
            if (cau == DongGoiPokeJ2me.DAY_DU && DongGoiPokeJ2me.loaiGan != null && !DongGoiPokeJ2me.loaiGan.contains(gocKhoa)) {
                cau = DongGoiPokeJ2me.XA;
            }
            for (int i = 0; i < 6; i++) {
                PokeDongTac g = goc.dongTac[i], m = moi.dongTac[i];
                g = goc.dongTac[cau.nguon[i]];
                java.awt.image.BufferedImage anh = javax.imageio.ImageIO.read(new File(pk, khoa + "_" + moi.anhCua(i) + ".png"));
                int[] giu = DongGoiPokeJ2me.chonKhung(g.soKhung, cau.toiDa[i], PokeDuLieu.laDongTacLap(i));
                if (giu.length != m.soKhung) {
                    sai(khoa + " so khung");
                    continue;
                }
                for (int d = 0; d < 4; d++) {
                    for (int k = 0; k < giu.length; k++) {
                        soKhung++;
                        // goc: o (x - rong/2 - dx, y - cao/2 - dy), x = y = 0
                        int gx = -g.rongKhung / 2 - g.dxChan[d], gy = -g.caoKhung / 2 - g.dyChan[d];
                        int mx = -m.rongKhung / 2 - m.dxChan[d], my = -m.caoKhung / 2 - m.dyChan[d];
                        int sx = g.xAtlas + giu[k] * g.rongKhung, sy = g.yHang(d);
                        int dem = 0;
                        for (int y = 0; y < g.caoKhung; y++) {
                            for (int x = 0; x < g.rongKhung; x++) {
                                boolean coG = (atlas.getRGB(sx + x, sy + y) >>> 24) >= 128;
                                int ux = gx + x - mx, uy = gy + y - my;   // toa do trong khung jar
                                boolean trong = ux >= 0 && uy >= 0 && ux < m.rongKhung && uy < m.caoKhung;
                                int cm = trong ? anh.getRGB(k * m.rongKhung + ux, d * m.caoKhung + uy) : 0;
                                boolean coM = (cm >>> 24) >= 128;
                                if (coG != coM || coG && (cm & 0xFFFFFF) != (atlas.getRGB(sx + x, sy + y) & 0xFFFFFF)) {
                                    dem++;
                                }
                            }
                        }
                        if (dem > 0) {
                            sai(khoa + " dong tac " + i + " huong " + d + " khung " + k + ": " + dem + " pixel lech");
                        }
                    }
                }
            }
        }
        System.out.println("so hinh Unity <-> jar: " + soKhung + " khung (dong tac x huong), tung pixel");
    }

    /**
     * Hieu ung chieu: moi dong (lop, chieu) cua Unity hu_bang.bytes phai co trong hu.bin, anh dung kich
     * thuoc, va moi khung giu lai ve tai (dx, dy) trung khung Unity ve tai tam (alpha + mau gan dung,
     * PNG bang mau co the luong tu hoa).
     */
    /**
     * Ho cau ca: client (game.pokemon.SinhThai.nhanHo / diemCau) phai tim DUNG cac diem cua server
     * (HTBB docs/pokemon/diem_cau_ca.csv, sinh tu BanDoSinhThai) tren moi ban do HTBB/cMap_pokemon.
     */
    /**
     * FilePack nhieu luong (loi cu: luong nap Res.LoadData va luong ve dung chung FilePack.instance ->
     * NullPointerException / sai goi): 4 luong cung init -> loadFile -> reset hai goi khac nhau, moi luong
     * phai luon thay dung goi cua minh.
     */
    static void kiemFilePackDaLuong() throws Exception {
        final String[] goi = {"/font.sh", "/main.sh"};
        final String[] dau = new String[2];
        for (int i = 0; i < 2; i++) {
            game.model.FilePack.init(goi[i]);
            game.model.FilePack fp = game.model.FilePack.hienTai();
            if (fp == null || fp.fname == null || fp.fname.length == 0) {
                System.out.println("filepack: bo qua (khong mo duoc " + goi[i] + ")");
                game.model.FilePack.reset();
                return;
            }
            dau[i] = fp.fname[0];
            game.model.FilePack.reset();
        }
        final int[] loiLuong = {0};
        Thread[] t = new Thread[4];
        for (int k = 0; k < t.length; k++) {
            final int g = k % 2;
            t[k] = new Thread() {
                public void run() {
                    for (int n = 0; n < 150; n++) {
                        try {
                            game.model.FilePack.init(goi[g]);
                            Thread.yield();
                            game.model.FilePack fp = game.model.FilePack.hienTai();
                            if (fp == null || fp.fname == null || !dau[g].equals(fp.fname[0]) || fp.loadFile(dau[g]) == null) {
                                synchronized (loiLuong) {
                                    loiLuong[0]++;
                                }
                            }
                            game.model.FilePack.reset();
                        } catch (Throwable e) {
                            synchronized (loiLuong) {
                                loiLuong[0]++;
                            }
                        }
                    }
                }
            };
            t[k].start();
        }
        for (int k = 0; k < t.length; k++) {
            t[k].join();
        }
        if (loiLuong[0] > 0) {
            sai("FilePack da luong: " + loiLuong[0] + " lan doc sai goi / loi");
        }
        System.out.println("filepack da luong: 4 luong x 150 lan init/loadFile/reset, loi " + loiLuong[0]);
    }

    static void kiemHoCau(File assets) throws Exception {
        File docs = DongGoiPokeJ2me.thuMucDocs(assets);
        File csv = new File(docs, "diem_cau_ca.csv");
        File cmap = new File(docs, "../../server/cMap_pokemon");
        if (!csv.isFile() || !cmap.isDirectory()) {
            System.out.println("ho cau ca: bo qua (khong thay " + csv + " / " + cmap + ")");
            return;
        }
        java.util.Map<Integer, java.util.TreeSet<String>> server = new java.util.TreeMap<>();
        for (String d : Files.readAllLines(csv.toPath(), StandardCharsets.UTF_8)) {
            if (d.startsWith("#") || d.startsWith("map_id") || d.trim().isEmpty()) {
                continue;
            }
            String[] c = d.split(",");
            int m = Integer.parseInt(c[0].trim());
            server.computeIfAbsent(m, k -> new java.util.TreeSet<>()).add(c[c.length - 2].trim() + "," + c[c.length - 1].trim());
        }
        int soMap = 0, soDiem = 0;
        String[] ds = cmap.list();
        Arrays.sort(ds);
        for (String f : ds) {
            if (!f.matches("\\d+")) {
                continue;
            }
            int id = Integer.parseInt(f);
            if (game.pokemon.SinhThai.mapKhongDong(id)) {
                continue;   // ban do khong co he sinh thai (client cung khong tim ho)
            }
            byte[] b = Files.readAllBytes(new File(cmap, f).toPath());
            if (b.length < 2) {
                continue;
            }
            int w = b[0] & 255, h = b[1] & 255;
            if (b.length < 2 + w * h) {
                continue;
            }
            String bo = id < 110 ? "t" : (id > 200 ? "t_thanh" : "t_hang");
            File ft = new File(cmap, bo + ".type");
            byte[] kt = ft.isFile() ? Files.readAllBytes(ft.toPath()) : null;
            short[] m = new short[w * h];
            int[] kieu = new int[w * h];
            for (int i = 0; i < w * h; i++) {
                m[i] = (short) (b[2 + i] & 255);
                kieu[i] = kt != null && m[i] < kt.length ? kt[m[i]] : 0;
            }
            int[] nhan = game.pokemon.SinhThai.nhanHo(game.pokemon.SinhThai.luoiNuoc(m, id), w, h);
            java.util.Vector v = game.pokemon.SinhThai.diemCau(nhan, kt == null ? null : game.pokemon.SinhThai.luoiDiDuoc(m, kieu), w, h, 4);
            java.util.TreeSet<String> client = new java.util.TreeSet<>();
            for (int i = 0; i < v.size(); i++) {
                int[] d = (int[]) v.elementAt(i);
                client.add(d[1] + "," + d[2]);
            }
            java.util.TreeSet<String> sv = server.getOrDefault(id, new java.util.TreeSet<>());
            if (!client.equals(sv)) {
                sai("ho cau ca map " + id + ": client " + client + " server " + sv);
            }
            soMap++;
            soDiem += client.size();
        }
        System.out.println("ho cau ca: " + soMap + " ban do, " + soDiem + " diem cau khop server (diem_cau_ca.csv)");
    }

    static void kiemHieuUng(File poke, File pk, int toiDa) throws Exception {
        List<String> dong = Files.readAllLines(new File(poke, "hu_bang.bytes").toPath(), StandardCharsets.UTF_8);
        game.pokemon.HieuUngBang[] bang = game.pokemon.HieuUngBang.doc(Files.readAllBytes(new File(pk, "hu.bin").toPath()));
        if (bang == null) {
            sai("hu.bin khong doc duoc");
            return;
        }
        int soChieu = 0, soKhung = 0, lechMau = 0;
        long tongPx = 0;
        for (String d : dong) {
            d = d.trim();
            if (d.isEmpty() || d.charAt(0) == '#') {
                continue;
            }
            String[] c = d.split(",");
            soChieu++;
            int lop = Integer.parseInt(c[0]), chieu = Integer.parseInt(c[1]);
            game.pokemon.HieuUngBang h = null;
            for (game.pokemon.HieuUngBang x : bang) {
                if (x.lop == lop && x.chieu == chieu) {
                    h = x;
                }
            }
            if (h == null) {
                sai("thieu hieu ung lop " + lop + " chieu " + chieu);
                continue;
            }
            if (!h.ten.equals(c[2]) || h.kieuVe != c[8].charAt(0)) {
                sai("hieu ung " + lop + "," + chieu + " sai ten/kieu");
            }
            File png = new File(pk, h.ten + ".png");
            if (!png.exists()) {
                sai(png + " thieu");
                continue;
            }
            java.awt.image.BufferedImage anh = javax.imageio.ImageIO.read(png);
            int[] wh = h.kichThuocAnh();
            if (anh.getWidth() < wh[0] || anh.getHeight() < wh[1]) {
                sai(png + " nho hon " + wh[0] + "x" + wh[1]);
                continue;
            }
            int rong = Integer.parseInt(c[3]), cao = Integer.parseInt(c[4]), n = Integer.parseInt(c[5]);
            int soHuong = Integer.parseInt(c[6]), soCot = Integer.parseInt(c[7]), tick = Integer.parseInt(c[9]);
            int[] giu = DongGoiPokeJ2me.chonKhung(n, toiDa, false);
            if (h.soKhung != giu.length || h.soKhung > toiDa) {
                sai("hieu ung " + h.ten + " so khung " + h.soKhung);
                continue;
            }
            // tong thoi gian giu nguyen (sai so lam tron <= 1 tick moi khung)
            if (Math.abs(h.soKhung * h.tickMoiKhung - n * tick) > h.soKhung) {
                sai("hieu ung " + h.ten + " thoi luong " + h.soKhung * h.tickMoiKhung + " != " + n * tick);
            }
            java.awt.image.BufferedImage goc = javax.imageio.ImageIO.read(new File(poke, h.ten + ".bytes"));
            for (int dir = 0; dir < (soHuong >= 4 ? 4 : 1); dir++) {
                for (int k = 0; k < giu.length; k++) {
                    soKhung++;
                    int f = giu[k];
                    int gx = soHuong >= 4 ? f * rong : f % soCot * rong, gy = soHuong >= 4 ? dir * cao : f / soCot * cao;
                    int[] ng = h.nguon(k, dir);
                    for (int y = 0; y < cao; y++) {
                        for (int x = 0; x < rong; x++) {
                            int cg = goc.getRGB(gx + x, gy + y);
                            // toa do trong khung jar: Unity ve goc khung tai -rong/2, jar tai dx
                            int ux = x - rong / 2 - h.dx, uy = y - cao / 2 - h.dy;
                            boolean trong = ux >= 0 && uy >= 0 && ux < h.rong && uy < h.cao;
                            int cm = trong ? anh.getRGB(ng[0] + ux, ng[1] + uy) : 0;
                            int ag = cg >>> 24, am = cm >>> 24;
                            if (ag < 8) {
                                ag = 0;
                            }
                            tongPx++;
                            if (Math.abs(ag - am) > 40 || ag > 0 && DongGoiPokeJ2me.kc(cg | 0xFF000000, cm | 0xFF000000) > 3 * 48 * 48) {
                                lechMau++;
                            }
                        }
                    }
                }
            }
        }
        if (soChieu != bang.length) {
            sai("hu.bin co " + bang.length + " dong, Unity " + soChieu);
        }
        // luong tu hoa: cho phep it pixel lech mau (<0.5%)
        if (lechMau * 200L > tongPx) {
            sai("hieu ung lech mau qua nhieu: " + lechMau + "/" + tongPx);
        }
        System.out.println("hieu ung: " + soChieu + " chieu co du trong hu.bin, " + soKhung + " khung so pixel, lech "
                + lechMau + "/" + tongPx);
    }

    /** Bo doc doc lap theo dac ta muc 7.1. */
    static void soSanhDocLap(String t, byte[] b, PokeDuLieu dl) throws Exception {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
        int pb = in.readUnsignedByte();
        int n = in.readUnsignedByte();
        if (pb != dl.phienBan || n != dl.dongTac.length || n != 6) {
            sai(t + " dau tep");
            return;
        }
        for (int i = 0; i < n; i++) {
            PokeDongTac d = dl.dongTac[i];
            int w = in.readUnsignedShort(), h = in.readUnsignedShort(), k = in.readUnsignedByte();
            if (w != d.rongKhung || h != d.caoKhung || k != d.soKhung) {
                sai(t + " dong tac " + i + " kich thuoc");
            }
            int tong = 0;
            for (int j = 0; j < k; j++) {
                int tl = in.readUnsignedByte();
                tong += Math.max(1, tl);
                if (Math.max(1, tl) != d.thoiLuong[j]) {
                    sai(t + " thoi luong");
                }
            }
            if (tong != d.tongThoiLuong) {
                sai(t + " tong thoi luong");
            }
            int y = in.readUnsignedShort();
            int x = pb >= 2 ? in.readUnsignedShort() : 0;
            if (x != d.xAtlas || y != d.yAtlas) {
                sai(t + " xAtlas/yAtlas");
            }
            for (int h2 = 0; h2 < 4; h2++) {
                if (in.readByte() != d.dxChan[h2]) {
                    sai(t + " dxChan");
                }
            }
            for (int h2 = 0; h2 < 4; h2++) {
                if (in.readByte() != d.dyChan[h2]) {
                    sai(t + " dyChan");
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if ((in.readUnsignedByte() != 0) != dl.coThat[i]) {
                sai(t + " coThat");
            }
        }
        if (in.available() != 0) {
            sai(t + " con byte thua");
        }
    }

    static int[] kichThuocPng(byte[] b) {
        return new int[]{((b[16] & 0xFF) << 24) | ((b[17] & 0xFF) << 16) | ((b[18] & 0xFF) << 8) | (b[19] & 0xFF),
            ((b[20] & 0xFF) << 24) | ((b[21] & 0xFF) << 16) | ((b[22] & 0xFF) << 8) | (b[23] & 0xFF)};
    }

    /** So ChuPokemon/TenMapPokemon cua jar voi ban server (neu co tren classpath). */
    static void kiemChu(String[] a) throws Exception {
        Set<String> tap = new LinkedHashSet<>();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                Files.readAllBytes(new File(a[1], "pk/chu.bin").toPath())));
        int n = in.readUnsignedShort();
        for (int i = 0; i < n; i++) {
            String k = in.readUTF();
            in.readUTF();
            if (k.startsWith("=")) {
                k = k.substring(1);
            }
            tap.add(k);
            tap.add(k.toUpperCase());
            tap.add(Character.toUpperCase(k.charAt(0)) + k.substring(1));
            tap.add("Gặp " + k + " ở " + k + "!");
            tap.add("x" + k + " " + k + "_ 1" + k + "2 (" + k + ")");
        }
        // moi chuoi viet trong nguon game (chu nguoi choi thay)
        Pattern p = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");
        for (int i = 2; i < a.length; i++) {
            List<File> ds = new ArrayList<>();
            liet(new File(a[i]), ds);
            for (File f : ds) {
                Matcher m = p.matcher(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
                while (m.find()) {
                    tap.add(m.group(1));
                }
            }
        }
        int soChu = 0;
        for (String s : tap) {
            String jar = ChuPokemon.doiLuon(s);
            if (!ChuPokemon.doiLuon(jar).equals(jar)) {
                // tu dien khong lap: doi lan hai phai giu nguyen (Font goi D o ca do rong lan ve)
                if (!s.equals(jar)) {
                    System.out.println("  (doi 2 lan khac) " + s + " -> " + jar + " -> " + ChuPokemon.doiLuon(jar));
                }
            }
            soChu++;
        }
        Class<?> sv;
        try {
            sv = Class.forName("com.htbb.pokemon.ChuPokemon");
        } catch (ClassNotFoundException e) {
            System.out.println("ChuPokemon: " + soChu + " chuoi chay khong loi (khong co ban server de doi chieu)");
            return;
        }
        Method doi = sv.getMethod("doiLuon", String.class);
        int khac = 0;
        for (String s : tap) {
            String x = (String) doi.invoke(null, s);
            String y = ChuPokemon.doiLuon(s);
            if (!x.equals(y)) {
                khac++;
                sai("ChuPokemon khac server: [" + s + "] server=[" + x + "] jar=[" + y + "]");
            }
        }
        System.out.println("ChuPokemon: " + soChu + " chuoi, khac ban server: " + khac);
        Method doiMap = Class.forName("com.htbb.pokemon.TenMapPokemon").getMethod("doi", String.class);
        int soMap = 0;
        for (String[] d : TenMapPokemon.BANG) {
            for (String s : new String[]{d[0], " " + d[0] + " ", d[0].toUpperCase(), "Đông " + d[0], "bắc " + d[0], "Tây  " + d[0], "Nam", d[1]}) {
                soMap++;
                String x = (String) doiMap.invoke(null, s);
                if (!x.equals(TenMapPokemon.doi(s))) {
                    sai("TenMapPokemon [" + s + "] server=[" + x + "] jar=[" + TenMapPokemon.doi(s) + "]");
                }
            }
        }
        System.out.println("TenMapPokemon: " + soMap + " chuoi khop ban server");
        // vung Hoenn (nam 2): bang ten theo id (server com.htbb.pokemon.TenMapPokemon.HOENN_*)
        Class<?> tmS = Class.forName("com.htbb.pokemon.TenMapPokemon");
        int[] idS = (int[]) tmS.getField("HOENN_ID").get(null);
        String[] tenS = (String[]) tmS.getField("HOENN_TEN").get(null);
        if (!java.util.Arrays.equals(idS, TenMapPokemon.HOENN_ID) || !java.util.Arrays.equals(tenS, TenMapPokemon.HOENN_TEN)) {
            sai("TenMapPokemon.HOENN_ID/HOENN_TEN khac ban server");
        }
        System.out.println("TenMapPokemon Hoenn: " + idS.length + " ban do khop ban server");
    }

    static void liet(File f, List<File> ds) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                liet(c, ds);
            }
        } else if (f.getName().endsWith(".java")) {
            ds.add(f);
        }
    }

    /**
     * Am thanh: moi tieng AmThanh.TEN co /am/s_*.wav (PCM 8 bit mono), moi bai AmThanh.TEN_NHAC co
     * /am/n_*.mid (MThd); tong ban thuong &lt;= 400 KB; ban nhe (res_nhe/am) chi WAV, &lt;= 60 KB.
     */
    static void kiemAmThanh(File res) throws Exception {
        File am = new File(res, "am");
        long tong = 0;
        for (String t : AmThanh.TEN) {
            File f = new File(am, "s_" + t + ".wav");
            if (!f.exists()) {
                sai("thieu am thanh " + f);
                continue;
            }
            byte[] b = Files.readAllBytes(f.toPath());
            tong += b.length;
            if (b.length < 44 || b[0] != 'R' || b[8] != 'W' || b[22] != 1 || b[34] != 8) {
                sai(f + ": khong phai WAV PCM 8 bit mono");
            }
        }
        for (String t : AmThanh.TEN_NHAC) {
            File f = new File(am, "n_" + t + ".mid");
            if (!f.exists()) {
                sai("thieu nhac " + f);
                continue;
            }
            byte[] b = Files.readAllBytes(f.toPath());
            tong += b.length;
            if (b.length < 22 || b[0] != 'M' || b[1] != 'T' || b[2] != 'h' || b[3] != 'd') {
                sai(f + ": khong phai MIDI");
            }
        }
        if (tong > 400 * 1024) {
            sai("am thanh ban thuong qua 400 KB: " + tong);
        }
        if (AmThanh.TEN.length != AmThanh.UU_TIEN.length) {
            sai("AmThanh.UU_TIEN lech TEN");
        }
        System.out.println("am thanh: " + AmThanh.TEN.length + " tieng + " + AmThanh.TEN_NHAC.length + " bai nhac, " + tong + " byte");
        File nhe = new File(res.getAbsoluteFile().getParentFile(), "res_nhe/am");
        if (nhe.isDirectory()) {
            long t2 = 0;
            for (File f : nhe.listFiles()) {
                t2 += f.length();
                if (!f.getName().endsWith(".wav")) {
                    sai("ban nhe co tep khong phai tieng hieu ung: " + f);
                }
            }
            if (t2 > 60 * 1024) {
                sai("am thanh ban nhe qua 60 KB: " + t2);
            }
            System.out.println("am thanh ban nhe: " + t2 + " byte");
        }
    }

    /**
     * HePoke (client) khop bang khac he server (com.htbb.pokemon.HePokemon.heSoTrongGame) neu lop
     * server co tren classpath; luon kiem vai cap da biet.
     */
    static void kiemHe() {
        int[][] mau = {{1, 4, -1, 150}, {2, 1, -1, 150}, {3, 8, -1, 30}, {1, 2, -1, 70}, {4, 4, 7, 50},
            {5, 14, 9, 200}, {0, 13, -1, 30}, {3, 2, 9, 200}, {6, 0, -1, 150}};
        for (int[] m : mau) {
            int v = HePoke.heSo(m[0], m[1], m[2]);
            if (v != m[3]) {
                sai("HePoke.heSo(" + m[0] + "," + m[1] + "," + m[2] + ") = " + v + " != " + m[3]);
            }
        }
        if (HePoke.he1(6) != HePoke.LUA || HePoke.he2(6) != HePoke.BAY || HePoke.he1(94) != HePoke.MA || HePoke.he2(25) != -1) {
            sai("HePoke.he1/he2 sai (Charizard/Gengar/Pikachu)");
        }
        try {
            Method m = Class.forName("com.htbb.pokemon.HePokemon").getMethod("heSoTrongGame", int.class, int.class, int.class);
            int so = 0;
            for (int a = 0; a < 18; a++) {
                for (int b = 0; b < 18; b++) {
                    for (int c = -1; c < 18; c++) {
                        int sv = (Integer) m.invoke(null, a, b, c);
                        if (sv != HePoke.heSo(a, b, c)) {
                            sai("khac he lech server: " + a + " -> " + b + "/" + c);
                        }
                        so++;
                    }
                }
            }
            System.out.println("HePoke khop server: " + so + " cap");
        } catch (ClassNotFoundException e) {
            System.out.println("HePoke: khong co lop server tren classpath, chi kiem mau");
        } catch (Exception e) {
            sai("HePoke: " + e);
        }
    }
}
