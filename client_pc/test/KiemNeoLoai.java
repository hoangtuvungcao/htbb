import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;

/**
 * KIỂM NEO TRANG PHỤC MỌI LOÀI ĐÃ MỞ (loai-bo-sung, headless, đường vẽ bản PC):
 * <ul>
 * <li>nạp gói tải theo nhu cầu THẬT server/map/loai/&lt;NNNN&gt;.goi + &lt;NNNN&gt;s.goi vào KhoSprite (loài trong jar dùng
 *     jar) - đúng đường client đi;</li>
 * <li>mỗi loài 1..251 x (thường, sắc màu nếu có gói) x 6 động tác x MỌI khung x 4 hướng: có neo PMD + neo trang phục
 *     (NeoTrangPhuc từ điểm ảnh khung), giữa sọ trong đầu, cổ dưới đỉnh sọ;</li>
 * <li>CĂN SỐ (dung sai 1 px gốc): mũ (trang phục 1) giữa mũ lệch giữa sọ &lt;= 1, đáy mũ lệch đỉnh sọ &lt;= 1; khăn giữa cổ
 *     &lt;= 1, đỉnh khăn lệch cổ &lt;= 1; điểm neo THẺ BẠN ĐỒNG HÀNH (ô phụ kiện của Pokémon đi theo: đỉnh đầu PMD / điểm
 *     cổ) cách điểm ảnh đặc gần nhất của thân &lt;= 1 px; bản sắc màu cùng neo với bản thường;</li>
 * <li>ghi bảng neo từng loài du_lieu/loai/neo_loai.csv (đứng, khung 0, 4 hướng) và bảng soát ảnh (phóng 3x) vào thư mục
 *     ra: mỗi loài một hàng, 4 hướng x (trang phục 1, thẻ 1 ở cỡ pet 60%) + điểm neo tô màu.</li>
 * </ul>
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemNeoLoai.java
 *   java -Djava.awt.headless=true -Dhtbb.duLieu=$(mktemp -d) -cp client_pc/out/build/cls:client_pc/out/build/res:$O \
 *        KiemNeoLoai [thư mục ảnh soát]          # chạy ở gốc repo; thoát 1 = có lỗi
 */
public class KiemNeoLoai {
    static int dat, hong, soHongIn;
    static final java.util.TreeMap<String, Integer> LOAI = new java.util.TreeMap<String, Integer>();
    static final int NEN = 0xFF2C3446, O = 192, CY = 120;
    static final String[] TEN_DT = {"dung", "chay", "danh_gan", "danh_xa", "trung_don", "guc_nga"};
    static final String[] TEN_HUONG = {"xuong", "len", "trai", "phai"};
    /** Lệch lớn nhất đo được (px gốc): mũ giữa, mũ đáy, khăn giữa, khăn đỉnh, thẻ đỉnh, thẻ cổ, shiny khác thường. */
    static final double[] LECH_MAX = new double[8];
    static final String[] TEN_LECH = {"mu giua", "mu day", "khan giua", "khan dinh", "the dinh dau", "the co", "shiny/thuong",
        "mu cham dau"};
    static long soKhung, soMu, soKhan, soThe, soAnMu, soAnKhan;
    static PrintWriter tatCaHong;
    static long soKhacNguon;

    /** Hai khung cùng cỡ, cùng điểm chân, cùng bóng (alpha) từng điểm ảnh. */
    static boolean cungBong(int[][] a, int[][] b) {
        if (a == null || b == null || a[0][0] != b[0][0] || a[0][1] != b[0][1] || a[0][2] != b[0][2] || a[0][3] != b[0][3]) {
            return false;
        }
        for (int i = 0; i < a[1].length; i++) {
            if ((a[1][i] >>> 24 >= 128) != (b[1][i] >>> 24 >= 128)) {
                return false;
            }
        }
        return true;
    }

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
            return;
        }
        hong++;
        if (soHongIn++ < 60) {
            System.out.println("HONG: " + ten);
        }
        if (tatCaHong != null) {
            tatCaHong.println(ten);
        }
        String l = ten.replaceAll("#[0-9]+s? [a-z_]+ k[0-9]+ [a-z]+", "").replaceAll("-?[0-9]+(\\.[0-9]+)?", "N");
        LOAI.put(l, LOAI.containsKey(l) ? LOAI.get(l) + 1 : 1);
    }

    static void lech(int i, double v) {
        LECH_MAX[i] = Math.max(LECH_MAX[i], Math.abs(v));
    }

    public static void main(String[] a) throws Exception {
        File ra = new File(a.length > 0 ? a[0] : "/tmp/neo_loai");
        ra.mkdirs();
        tatCaHong = new PrintWriter(new File(ra, "hong.txt"), "UTF-8");
        File thu = new File("server/map/loai");
        game.pokemon.KhoSprite.NGAN_SACH = 64 << 20;
        game.pokemon.KhoSprite.RAM_TOI_DA = 64 << 20;
        game.pokemon.PokeBody.ToiDaPixel = 40000000;
        // loai-bo-sung: MOI loai da mo (du_lieu/loai/bang_loai.csv cot bat = 1: Gen 1-3 + Vung hoang da Gen 4-9)
        int[] daMo = loaiDaMo();
        boolean[] coShiny = new boolean[1026];
        int soGoi = 0;
        for (int d : daMo) {
            String k = game.pokemon.PokeBody.tenKhoa(d, false);
            File f = new File(thu, k + ".goi"), fs = new File(thu, k + "s.goi");
            if (!game.pokemon.PokeBody.coTrongJar(d) && f.isFile()) {
                kiem(game.pokemon.KhoSprite.luu(d, false, Files.readAllBytes(f.toPath())), "#" + d + " nạp gói");
                soGoi++;
            }
            if (fs.isFile()) {
                coShiny[d] = true;
                if (!game.pokemon.PokeBody.coTrongJar(d, true)) {
                    kiem(game.pokemon.KhoSprite.luu(d, true, Files.readAllBytes(fs.toPath())), "#" + d + "s nạp gói shiny");
                    soGoi++;
                }
            }
        }
        game.pokemon.KhoSprite.choXin = false;   // không có máy chủ
        game.pokemon.PhuKien.nap();
        kiem(game.pokemon.TrangPhucVe.nap(), "nạp atlas trang phục");
        PrintWriter csv = new PrintWriter(new File("du_lieu/loai/neo_loai.csv"), "UTF-8");
        csv.println("dex,huong,pmd_dau_x,pmd_dau_y,pmd_dinh_y,pmd_tay_x,pmd_tay_y,pmd_tam_x,pmd_tam_y,dinh_y,dau_x,dau_trai,dau_phai,"
                + "mat_y,co_x,co_y,lung_x,lung_y,eo_y,day_y,mu_sau");
        int[] neo = new int[8], tp = new int[game.pokemon.NeoTrangPhuc.SO];
        int soShiny = 0;
        for (int dex : daMo) {
            kiem(game.pokemon.PokeBody.coSprite(dex, false) && !"bong".equals(game.pokemon.PokeBody.lay(dex, false).khoa),
                    "#" + dex + " có sprite thật");
            for (int sh = 0; sh < 2; sh++) {
                boolean shiny = sh == 1;
                if (shiny && !coShiny[dex]) {
                    continue;
                }
                if (shiny) {
                    soShiny++;
                    game.pokemon.PokeBody pb = game.pokemon.PokeBody.lay(dex, true);
                    kiem(pb != null && pb.khoa.endsWith("s"), "#" + dex + "s nạp bản sắc màu thật");
                }
                for (int dt = 0; dt < 6; dt++) {
                    int[] ticks = tickKhung(dex, shiny, dt);
                    for (int fi = 0; fi < ticks.length; fi++) {
                        for (int dir = 0; dir < 4; dir++) {
                            mot(dex, shiny, dt, fi, ticks[fi], dir, neo, tp, csv);
                        }
                    }
                }
            }
        }
        csv.close();
        veBang(ra, coShiny, daMo);
        tatCaHong.close();
        for (java.util.Map.Entry<String, Integer> e : LOAI.entrySet()) {
            System.out.println("LOAI HONG " + e.getValue() + " x " + e.getKey());
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < LECH_MAX.length; i++) {
            b.append(i > 0 ? ", " : "").append(TEN_LECH[i]).append(' ').append(LECH_MAX[i]);
        }
        System.out.println("KiemNeoLoai: " + daMo.length + " loai da mo (" + soShiny + " co ban sac mau), " + soGoi + " goi tai, " + soKhung
                + " khung x huong; mu " + soMu + " (an " + soAnMu + "), khan " + soKhan + " (an " + soAnKhan + "), the " + soThe
                + "; shiny khac khung nguon voi ban thuong (jar day du / goi XA, khong so) " + soKhacNguon);
        System.out.println("lech lon nhat (px goc): " + b);
        System.out.println("KiemNeoLoai: dat " + dat + ", hong " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }

    /** Dex co bat = 1 trong du_lieu/loai/bang_loai.csv (sinh_loai.py), tang dan. */
    static int[] loaiDaMo() throws Exception {
        java.util.List<String> d = Files.readAllLines(new File("du_lieu/loai/bang_loai.csv").toPath(), java.nio.charset.StandardCharsets.UTF_8);
        String[] h = d.get(0).split(",");
        int cBat = java.util.Arrays.asList(h).indexOf("bat");
        java.util.List<Integer> kq = new java.util.ArrayList<Integer>();
        for (int i = 1; i < d.size(); i++) {
            String[] p = d.get(i).split(",");
            if (p.length > cBat && "1".equals(p[cBat])) {
                kq.add(Integer.valueOf(p[0]));
            }
        }
        int[] a = new int[kq.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = kq.get(i).intValue();
        }
        return a;
    }

    static int[] tickKhung(int dex, boolean shiny, int dt) {
        int[] tt = game.pokemon.PokeBody.thongTinDongTac(dex, shiny, dt);
        if (tt == null) {
            return new int[0];
        }
        int[] r = new int[tt[0]];
        int n = 0, sum = 0;
        for (int f = 0; f < tt[0]; f++) {
            int tick = (sum * 25 + 59) / 60;
            sum += tt[1 + f];
            if (tick * 60 / 25 < sum) {
                r[n++] = tick;
            }
        }
        int[] q = new int[n];
        System.arraycopy(r, 0, q, 0, n);
        return q;
    }

    static boolean dac(int[] px, int w, int h, int x, int y) {
        return x >= 0 && y >= 0 && x < w && y < h && (px[y * w + x] >>> 24) >= 128;
    }

    /** Khoảng cách (Chebyshev, px) từ (x, y) tới điểm ảnh đặc gần nhất của khung; 99 nếu không có trong 8 px. */
    static int cachThan(int[] px, int w, int h, int x, int y) {
        for (int r = 0; r <= 8; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if ((Math.abs(dx) == r || Math.abs(dy) == r) && dac(px, w, h, x + dx, y + dy)) {
                        return r;
                    }
                }
            }
        }
        return 99;
    }

    static void mot(int dex, boolean shiny, int dt, int fi, int tick, int dir, int[] neo, int[] tp, PrintWriter csv) {
        String ten = "#" + dex + (shiny ? "s" : "") + " " + TEN_DT[dt] + " k" + fi + " " + TEN_HUONG[dir];
        boolean coNeo = game.pokemon.PokeBody.neo(dex, shiny, dir, dt, tick, 100, neo);
        boolean coTp = game.pokemon.PokeBody.neoTrangPhuc(dex, shiny, dir, dt, tick, 100, tp);
        kiem(coNeo, ten + ": có neo PMD");
        kiem(coTp, ten + ": có neo trang phục");
        if (!coNeo || !coTp) {
            return;
        }
        soKhung++;
        int[] n = neo.clone(), t = tp.clone();
        kiem(t[game.pokemon.NeoTrangPhuc.DAU_TRAI] <= t[game.pokemon.NeoTrangPhuc.DAU_X]
                && t[game.pokemon.NeoTrangPhuc.DAU_X] <= t[game.pokemon.NeoTrangPhuc.DAU_PHAI], ten + ": giữa sọ trong đầu");
        kiem(t[game.pokemon.NeoTrangPhuc.CO_Y] > t[game.pokemon.NeoTrangPhuc.DINH_Y], ten + ": cổ dưới đỉnh sọ");
        int[][] k = game.pokemon.PokeBody.khungDangVe(dex, shiny, dir, dt, tick);
        kiem(k != null, ten + ": đọc được khung");
        if (k == null) {
            return;
        }
        int w = k[0][0], h = k[0][1], cx = k[0][2], cy = k[0][3];
        int[] px = k[1];
        // sọ: hàng đỉnh sọ có điểm đặc trong bề ngang đầu (đỉnh sọ là hàng của thân, không lơ lửng)
        boolean coDinh = false;
        for (int x = t[game.pokemon.NeoTrangPhuc.DAU_TRAI]; x <= t[game.pokemon.NeoTrangPhuc.DAU_PHAI] && !coDinh; x++) {
            coDinh = dac(px, w, h, cx + x, cy + t[game.pokemon.NeoTrangPhuc.DINH_Y]);
        }
        // khung ghi đè tay AN_MU (không vẽ mũ): đỉnh sọ không dùng
        kiem(coDinh || game.pokemon.NeoTrangPhuc.anMu(dex, dt, dir), ten + ": hàng đỉnh sọ là điểm ảnh của đầu");
        // thẻ bạn đồng hành (ô phụ kiện Pokémon đi theo): neo đỉnh đầu + cổ SAU khi bám thân (PokeBody.neoThe, đúng đường vẽ)
        int[] th = new int[5];
        kiem(game.pokemon.PokeBody.neoThe(dex, shiny, dir, dt, tick, 100, th), ten + ": có neo thẻ");
        int cDinh = cachThan(px, w, h, cx + th[0], cy + th[1]);
        lech(4, cDinh);
        kiem(cDinh <= 1, ten + ": neo thẻ đỉnh đầu cách thân " + cDinh + " <= 1");
        if (th[4] != 0) {
            int cCo = cachThan(px, w, h, cx + th[2], cy + th[3]);
            lech(5, cCo);
            kiem(cCo <= 1, ten + ": neo thẻ cổ cách thân " + cCo + " <= 1");
            // bám thân chỉ kéo tối đa BAM_THAN_TOI_DA px so với điểm PMD (không nhảy sang chỗ khác)
            int[] co0 = diemCo(n);
            if (co0 != null) {
                kiem(Math.abs(co0[0] - th[2]) <= game.pokemon.PokeBody.BAM_THAN_TOI_DA
                        && Math.abs(co0[1] - th[3]) <= game.pokemon.PokeBody.BAM_THAN_TOI_DA, ten + ": cổ bám thân gần điểm PMD");
            }
        }
        soThe++;
        if (shiny) {
            // bản sắc màu chỉ đổi màu: neo trang phục phải trùng bản thường (cùng khung)
            int[] t0 = new int[game.pokemon.NeoTrangPhuc.SO];
            int[][] k0 = game.pokemon.PokeBody.khungDangVe(dex, false, dir, dt, tick);
            // chỉ so khi CÙNG khung (cùng bóng điểm ảnh): loài trong jar đóng gói bản thường đủ khung, gói shiny cấu hình XA
            // (đánh xa dùng chung ảnh đánh gần) - khác khung nguồn thì không so
            if (!cungBong(k0, k)) {
                soKhacNguon++;
            } else if (game.pokemon.PokeBody.neoTrangPhuc(dex, false, dir, dt, tick, 100, t0)) {
                int m = 0;
                for (int i = 0; i < t0.length; i++) {
                    if (t0[i] != game.pokemon.NeoTrangPhuc.KHONG && t[i] != game.pokemon.NeoTrangPhuc.KHONG) {
                        m = Math.max(m, Math.abs(t0[i] - t[i]));
                    }
                }
                lech(6, m);
                kiem(m <= 1, ten + ": neo bản sắc màu lệch bản thường " + m + " <= 1");
            }
        }
        // trang phục 1 (mũ lưỡi trai + khăn): đo hộp điểm đặc mảnh thật sự vẽ
        game.pokemon.NgoaiHinhCua nh = new game.pokemon.NgoaiHinhCua();
        nh.tpSo = 1;
        game.pokemon.TrangPhucVe.soHopVe = 0;
        game.pokemon.TrangPhucVe.ghiHop = true;
        ve(dex, shiny, nh, dir, dt, tick, 1);
        game.pokemon.TrangPhucVe.ghiHop = false;
        boolean coMu = false, coKhan = false;
        for (int i = 0; i < game.pokemon.TrangPhucVe.soHopVe; i++) {
            int b = i * 7;
            int X = game.pokemon.TrangPhucVe.hopVe[b], Y = game.pokemon.TrangPhucVe.hopVe[b + 1];
            int W = game.pokemon.TrangPhucVe.hopVe[b + 2], H = game.pokemon.TrangPhucVe.hopVe[b + 3];
            int o = game.pokemon.TrangPhucVe.hopVe[b + 4];
            double giua = X + W / 2.0;
            if (o == game.pokemon.NgoaiHinhCua.MU) {
                coMu = true;
                soMu++;
                double lg = giua - (O / 2 + t[game.pokemon.NeoTrangPhuc.DAU_X] + 0.5);
                int ld = (Y + H - 1) - (CY + t[game.pokemon.NeoTrangPhuc.DINH_Y]);
                lech(0, lg);
                lech(1, ld);
                // mũ CHẠM đầu: hàng đáy mũ có cột mà ngay dưới (<= 1 px) là điểm ảnh thân - không lơ lửng
                int tiep = 99;
                for (int x = X; x < X + W; x++) {
                    for (int dy = 0; dy <= 3 && dy < tiep; dy++) {
                        if (dac(px, w, h, x - O / 2 + cx, (Y + H - 1) + dy - CY + cy)) {
                            tiep = dy;
                        }
                    }
                }
                lech(7, tiep);
                kiem(tiep <= 1, ten + ": đáy mũ chạm đầu (hở " + tiep + " <= 1)");
                kiem(Math.abs(lg) <= 1.0, ten + ": giữa mũ lệch giữa sọ " + lg + " <= 1");
                kiem(Math.abs(ld) <= 1, ten + ": đáy mũ lệch đỉnh sọ " + ld + " <= 1");
            } else if (o == game.pokemon.NgoaiHinhCua.KHAN) {
                coKhan = true;
                soKhan++;
                double lg = giua - (O / 2 + t[game.pokemon.NeoTrangPhuc.CO_X] + 0.5);
                int ld = Y - (CY + t[game.pokemon.NeoTrangPhuc.CO_Y] - 1);
                lech(2, lg);
                lech(3, ld);
                kiem(Math.abs(lg) <= 1.0, ten + ": giữa khăn lệch cổ " + lg + " <= 1");
                kiem(Math.abs(ld) <= 1, ten + ": đỉnh khăn lệch cổ " + ld + " <= 1");
            }
        }
        if (!coMu) {
            soAnMu++;   // mũ to hơn đầu / loài cấm ô: ẩn (thà không vẽ còn hơn lệch) - không phải lỗi
        }
        if (!coKhan) {
            soAnKhan++;
        }
        if (!shiny && dt == 0 && fi == 0) {
            int K = game.pokemon.NeoTrangPhuc.KHONG;
            StringBuilder s = new StringBuilder().append(dex).append(',').append(TEN_HUONG[dir]);
            for (int i = 0; i < 7; i++) {
                s.append(',').append(n[i]);
            }
            int[] cot = {game.pokemon.NeoTrangPhuc.DINH_Y, game.pokemon.NeoTrangPhuc.DAU_X, game.pokemon.NeoTrangPhuc.DAU_TRAI,
                game.pokemon.NeoTrangPhuc.DAU_PHAI, game.pokemon.NeoTrangPhuc.MAT_Y, game.pokemon.NeoTrangPhuc.CO_X,
                game.pokemon.NeoTrangPhuc.CO_Y, game.pokemon.NeoTrangPhuc.LUNG_X, game.pokemon.NeoTrangPhuc.LUNG_Y,
                game.pokemon.NeoTrangPhuc.EO_Y, game.pokemon.NeoTrangPhuc.DAY_Y, game.pokemon.NeoTrangPhuc.MU_SAU};
            for (int i = 0; i < cot.length; i++) {
                s.append(',');
                if (t[cot[i]] != K) {
                    s.append(t[cot[i]]);
                }
            }
            csv.println(s);
        }
    }

    /** Điểm cổ giống TrangPhucVe.diemCo (k = 1). */
    static int[] diemCo(int[] neo) {
        int px = neo[0] + (neo[5] - neo[0]) / 2;
        int py = (neo[1] + 1 + neo[2] / 2) / 2;
        if (py < neo[2] + 4) {
            py = neo[2] + 4;
        }
        if (py > neo[6] + 2) {
            py = neo[6] + 2;
        }
        if (py > -3) {
            return null;
        }
        return new int[]{px, py};
    }

    /** Vẽ dex (+ ngoại hình n) vào ô O x O hệ số k, chân ở (O/2, CY). */
    static BufferedImage ve(int dex, boolean shiny, game.pokemon.NgoaiHinhCua n, int dir, int dt, int tick, int k) {
        BufferedImage b = new BufferedImage(O * k, O * k, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = b.createGraphics();
        g2.setColor(new java.awt.Color(NEN, true));
        g2.fillRect(0, 0, b.getWidth(), b.getHeight());
        g2.scale(k, k);
        Graphics g = new Graphics(g2, O, O, k);
        int x = O / 2, y = CY;
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veSau(g, n, dex, shiny, x, y, y, dir, dt, tick);
        }
        game.pokemon.PokeBody.paint(g, dex, shiny, x, y, dir, dt, tick, 100, 0, 0);
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veTruoc(g, n, dex, shiny, x, y, dir, dt, tick);
        }
        g2.dispose();
        return b;
    }

    /** Pokémon đi theo cỡ pet (60%) đeo thẻ bạn đồng hành số {@code the}. */
    static BufferedImage veThe(int dex, boolean shiny, int dir, int the) {
        BufferedImage b = new BufferedImage(O, O, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = b.createGraphics();
        g2.setColor(new java.awt.Color(NEN, true));
        g2.fillRect(0, 0, O, O);
        Graphics g = new Graphics(g2, O, O, 1);
        game.pokemon.PokeBody.paint(g, dex, shiny, O / 2, CY, dir, game.pokemon.PokeDuLieu.DUNG, 0, 100);
        game.pokemon.TrangPhucVe.veThe(g, the, dex, shiny, O / 2, CY, dir, game.pokemon.PokeDuLieu.DUNG, 0, 100);
        g2.dispose();
        return b;
    }

    /**
     * Bảng soát (phóng 3x, mỗi tệp 16 loài): mỗi loài một hàng: 4 hướng trang phục 1 | 4 hướng thẻ 1 (đi theo) | bản sắc
     * màu đứng xuống. Điểm đỏ = đỉnh sọ / giữa sọ (NeoTrangPhuc), xanh = cổ, vàng = neo thẻ đỉnh đầu PMD.
     */
    static void veBang(File ra, boolean[] coShiny, int[] daMo) throws Exception {
        int s = 80, z = 3, cot = 9;
        int[] tp = new int[game.pokemon.NeoTrangPhuc.SO], neo = new int[8];
        for (int i0 = 0; i0 < daMo.length; i0 += 16) {
            int i1 = Math.min(daMo.length, i0 + 16);
            int tu = daMo[i0], den = daMo[i1 - 1];
            BufferedImage tam = new BufferedImage(cot * s * z, (i1 - i0) * s * z, BufferedImage.TYPE_INT_RGB);
            Graphics2D gt = tam.createGraphics();
            gt.setColor(new java.awt.Color(NEN));
            gt.fillRect(0, 0, tam.getWidth(), tam.getHeight());
            for (int ii = i0; ii < i1; ii++) {
                int dex = daMo[ii];
                int hang = ii - i0;
                for (int c = 0; c < cot; c++) {
                    boolean shiny = c == 8;
                    if (shiny && !coShiny[dex]) {
                        continue;
                    }
                    int dir = c < 8 ? c % 4 : 0;
                    BufferedImage o;
                    if (c < 4 || shiny) {
                        game.pokemon.NgoaiHinhCua nh = new game.pokemon.NgoaiHinhCua();
                        nh.tpSo = 1;
                        o = ve(dex, shiny, nh, dir, 0, 0, 1);
                    } else {
                        o = veThe(dex, false, dir, 1);
                    }
                    if (c < 4 && game.pokemon.PokeBody.neoTrangPhuc(dex, false, dir, 0, 0, 100, tp)) {
                        cham(o, O / 2 + tp[game.pokemon.NeoTrangPhuc.DAU_X], CY + tp[game.pokemon.NeoTrangPhuc.DINH_Y] - 3, 0xFFFF3030);
                        cham(o, O / 2 + tp[game.pokemon.NeoTrangPhuc.CO_X], CY + tp[game.pokemon.NeoTrangPhuc.CO_Y], 0xFF30C0FF);
                    } else if (c >= 4 && c < 8 && game.pokemon.PokeBody.neo(dex, false, dir, 0, 0, 100, neo)) {
                        cham(o, O / 2 + neo[0], CY + neo[2] - 3, 0xFFFFE040);
                    }
                    gt.drawImage(o.getSubimage((O - s) / 2, CY - s + 16, s, s), c * s * z, hang * s * z, s * z, s * z, null);
                }
                gt.setColor(java.awt.Color.WHITE);
                gt.drawString("#" + dex, 4, hang * s * z + 14);
            }
            gt.dispose();
            ImageIO.write(tam, "png", new File(ra, String.format("neo_%04d_%04d.png", tu, den)));
        }
    }

    /** Chấm 1 px (đánh dấu điểm neo ngay trên điểm đo, không che sprite). */
    static void cham(BufferedImage b, int x, int y, int mau) {
        if (x >= 0 && y >= 0 && x < b.getWidth() && y < b.getHeight()) {
            b.setRGB(x, y, mau);
        }
    }
}
