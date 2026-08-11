import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Chuan bi bang sprite skin thu cuoi: tu mot anh PNG da tach nen (moi o mot frame,
 * xep thanh luoi) ra BANG CHUAN de tools/TaoEffThuCuoi.java va tools/CatSpriteSheet.java
 * an duoc — moi HANG la mot huong, cac o deu nhau, frame dat sat day o va can giua.
 *
 * BON VIEC, va ly do tung viec (deu la loi da gap that khi lam 5 skin):
 *
 * 1) DO KHE O 1x, CAT PIXEL O 4x (--phong). Bang tach nen tu dong thuong da duoc
 *    phong to (4x) va de lai vien mem: hang chuc nghin diem alpha trung gian. O co
 *    kich thuoc 4x thi mot cot khe chua vai chuc diem quang -> do khe o 4x luon ra
 *    "ca hang la mot o". Thu nho mat na ve 1x thi quang chi con 1 diem, khe hien ro.
 *    Nhung PIXEL thi van cat o 4x: dich cuoi cao 44px, thu tu 132px xuong 44px min
 *    hon nhieu so voi keo 33px len 44px.
 *
 * 2) LAM CUNG KENH TRONG SUOT. Sprite trong game khong co alpha trung gian. De vien
 *    mem thi khung bao tung frame phinh ra theo quang, va CatSpriteSheet.locNen voi
 *    --nguong=-1 tinh moi diem alpha >= 16 la co hinh nen cac o lai dinh vao nhau.
 *
 * 3) CHIA HANG DEU, DO KHE THEO CHIEU NGANG. Giua hai hang thuong khong co dong trong
 *    nao (tai/rau con o hang duoi cham day o hang tren) nen do khe theo chieu doc la
 *    vo ich — nhung bang sprite luon ke o vuong deu nhau nen chia deu lai dung. Trong
 *    mot hang thi PHAI do khe: frame nhin ngang rong hon frame nhin truoc, chia deu
 *    se chat mat dau va duoi con thu.
 *
 * 4) TACH O BONG DO. Mot o det, xam (thuong o goc tren phai) khong phai frame.
 *    TaoEffThuCuoi tu ve bong theo hang so cua no nen o nay chi de doi chieu.
 *
 *   javac -d out tools/ChuanBiBangSprite.java
 *   java -cp out ChuanBiBangSprite <anh.png> <ten> [--phong=4] [--hang=3] [--moihuong=6]
 *        [--ra=map/sprite_moi] [--alpha=96] [--nhieu=8] [--khe=2] [--mongcot=2]
 *        [--bong=x,y,w,h] [--thu]
 *
 *   --phong     anh nguon dang phong to bao nhieu lan (mat na do khe se thu nho lai)
 *   --hang      so hang cua luoi (mac dinh 3: xuong / len / ngang)
 *   --moihuong  so frame moi huong; 0 = khong kiem tra
 *   --alpha     nguong lam cung kenh trong suot (0..255)
 *   --nhieu     dom (tinh o 1x) nho hon bao nhieu diem thi coi la bui, xoa
 *   --khe       so cot trong lien tiep (o 1x) de tach hai frame
 *   --mongcot   cot (o 1x) co it hon bao nhieu diem thi coi la khe
 *   --bong      vung o bong do, toa do 1x; khong dat thi tu do tim
 *   --giu4x     giu nguyen do phan giai anh nguon. MAC DINH la thu ve dung luoi pixel
 *               1x bang MAU AP DAO cua tung khoi phong x phong (khong noi suy) — nho
 *               vay vien pixel dut khoat, khong bi nhoe. Chi dat co nay khi anh nguon
 *               that su la pixel-art o do phan giai that, khong phai ban phong to.
 *   --chiadeu   chia deu dai noi dung cua tung hang thanh --moihuong o, thay vi do khe.
 *               Dung khi frame nhin ngang rong hon frame nhin truoc: do khe phai xoa
 *               cot mong nen an mat chan va duoi con thu, con chia deu thi khong.
 *   --vun       xoa manh lien thong nho hon bao nhieu % vung lon nhat trong moi frame
 *               (mac dinh 20; 0 = khong xoa). De bo mieng cua con ben canh bi lem vao.
 *   --lay       chon frame theo huong: --lay=<xuong>|<len>|<ngang>, moi nhom la danh
 *               sach chi so o doc tu <ten>_bang.png
 *   --socot     so o cua tung hang, vd --socot=4,4,4,3 (bang xep khong deu)
 *   --thu       chi in ket qua do tim, khong ghi file
 */
public class ChuanBiBangSprite {

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java ChuanBiBangSprite <anh.png> <ten> [--phong=4] [--hang=3] "
                    + "[--moihuong=6] [--ra=map/sprite_moi] [--alpha=96] [--nhieu=8] [--khe=2] "
                    + "[--mongcot=2] [--bong=x,y,w,h] [--thu]");
            return;
        }
        final File nguon = new File(args[0]);
        final String ten = args[1];
        String ra = "map/sprite_moi";
        int phong = 4, soHang = 3, moiHuong = 6, nguongAlpha = 96, nhieu = 8, khe = 2, mongCot = 2;
        int[] vungBong = null;
        int[] soCot = null;
        int vunTiLe = 20;
        String lay = null;
        boolean chayThu = false, chiaDeu = false, ve1x = true;
        for (int i = 2; i < args.length; i++) {
            final String a = args[i];
            if (a.startsWith("--ra=")) ra = a.substring(5);
            else if (a.startsWith("--phong=")) phong = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--hang=")) soHang = Integer.parseInt(a.substring(7));
            else if (a.startsWith("--moihuong=")) moiHuong = Integer.parseInt(a.substring(11));
            else if (a.startsWith("--alpha=")) nguongAlpha = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--nhieu=")) nhieu = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--khe=")) khe = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--mongcot=")) mongCot = Integer.parseInt(a.substring(10));
            else if (a.equals("--thu")) chayThu = true;
            else if (a.equals("--chiadeu")) chiaDeu = true;
            else if (a.equals("--giu4x")) ve1x = false;
            else if (a.startsWith("--socot=")) {
                final String[] p = a.substring(8).split(",");
                soCot = new int[p.length];
                for (int k = 0; k < p.length; k++) soCot[k] = Integer.parseInt(p[k].trim());
            } else if (a.startsWith("--lay=")) lay = a.substring(6);
            else if (a.startsWith("--bong=")) {
                final String[] p = a.substring(7).split(",");
                vungBong = new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                        Integer.parseInt(p[2]), Integer.parseInt(p[3])};
            }
        }
        if (phong < 1) phong = 1;

        final BufferedImage goc = ImageIO.read(nguon);
        if (goc == null) {
            System.out.println("Khong doc duoc anh: " + nguon);
            return;
        }
        final int W = goc.getWidth(), H = goc.getHeight();
        final int w = W / phong, h = H / phong;
        if (w < 8 || h < 8) {
            System.out.println("Anh qua nho so voi --phong=" + phong);
            return;
        }

        // ---- 1) Anh 4x da lam cung kenh trong suot ----
        final BufferedImage sach = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        int soMo = 0;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                final int c = goc.getRGB(x, y);
                final int al = (c >>> 24) & 0xFF;
                if (al > 0 && al < 250) soMo++;
                if (al >= nguongAlpha) sach.setRGB(x, y, c | 0xFF000000);
            }
        }

        // ---- 2) Mat na o 1x: o 1x co hinh khi >= 1/4 khoi phong to co hinh ----
        final boolean[][] nen = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int dem = 0;
                for (int dy = 0; dy < phong; dy++) {
                    for (int dx = 0; dx < phong; dx++) {
                        final int sx = x * phong + dx, sy = y * phong + dy;
                        if (sx < W && sy < H && ((sach.getRGB(sx, sy) >>> 24) & 0xFF) != 0) dem++;
                    }
                }
                nen[y][x] = dem * 4 < phong * phong;
            }
        }
        System.out.println("== " + nguon.getName() + " " + W + "x" + H + "  (mat na " + w + "x" + h
                + " o 1x, " + soMo + " diem alpha trung gian, cat cung o " + nguongAlpha + ")");
        System.out.println("   xoa " + donDom(nen, nhieu) + " dom bui (< " + nhieu + " diem o 1x)");

        // ---- 3) Chia hang, do khe theo chieu ngang trong tung hang ----
        int wLuoi = w;
        if (vungBong != null && vungBong[0] > w / 2 && vungBong[1] < h / 2) wLuoi = vungBong[0];

        // Bien HANG: cung kieu nhu vach cot — bat dau tu chia deu roi truot ve dong co it
        // diem nhat quanh do. Cac hang cao khong bang nhau (hang nhin ngang thap hon hang
        // dung) nen chia cung se chat ngang than con thu o o dau moi hang.
        final int[] bienHang = new int[soHang + 1];
        bienHang[0] = 0;
        bienHang[soHang] = h;
        final int buocHang = h / soHang;
        for (int r = 1; r < soHang; r++) {
            final int dich = h * r / soHang;
            int tot = dich, itNhat = Integer.MAX_VALUE;
            for (int y = Math.max(1, dich - buocHang * 35 / 100);
                 y <= Math.min(h - 1, dich + buocHang * 35 / 100); y++) {
                int s = 0;
                for (int x = 0; x < w; x++) if (!nen[y][x]) s++;
                if (s < itNhat || (s == itNhat && Math.abs(y - dich) < Math.abs(tot - dich))) {
                    itNhat = s;
                    tot = y;
                }
            }
            bienHang[r] = tot;
        }
        final StringBuilder bh = new StringBuilder("   bien hang:");
        for (int r = 0; r <= soHang; r++) bh.append(' ').append(bienHang[r]);
        System.out.println(bh);
        // Bieu do tung hang de doc bang mat khi phai chinh tham so
        for (int r = 0; r < soHang; r++) {
            final StringBuilder sb = new StringBuilder("   hang " + r + ": ");
            for (int x = 0; x < w; x++) {
                int s = 0;
                for (int y = bienHang[r]; y < bienHang[r + 1]; y++) if (!nen[y][x]) s++;
                sb.append(s == 0 ? '.' : (s <= mongCot ? ',' : (s < 10 ? '-' : '#')));
            }
            System.out.println(sb);
        }

        final List<int[]> o = new ArrayList<>();
        final List<Integer> demTheoHang = new ArrayList<>();
        final List<int[]> bong = new ArrayList<>();
        for (int r = 0; r < soHang; r++) {
            final int y1 = bienHang[r], y2 = bienHang[r + 1] - 1;
            // Cot qua mong = khe con sot bui: xoa luon, khong thi ca hang bi gop lam mot.
            for (int x = 0; x < wLuoi; x++) {
                int s = 0;
                for (int y = y1; y <= y2; y++) if (!nen[y][x]) s++;
                if (s > 0 && s <= mongCot) for (int y = y1; y <= y2; y++) nen[y][x] = true;
            }
            int dem = 0;
            final List<int[]> khoi = new ArrayList<>();
            for (final int[] cot : cotTrong(nen, y1, y2, khe, wLuoi)) {
                final int[] bb = khungBao(nen, cot[0], Math.min(cot[1], wLuoi - 1), y1, y2);
                if (bb == null) continue;
                final int ow = bb[2] - bb[0] + 1, oh = bb[3] - bb[1] + 1;
                if (vungBong == null && ow > oh * 18 / 10 && nhatMau(sach, bb, phong)) {
                    System.out.println("   hang " + r + ": o " + ow + "x" + oh + " tai ("
                            + bb[0] + "," + bb[1] + ") det va xam -> BONG DO");
                    bong.add(bb);
                    continue;
                }
                khoi.add(bb);
            }
            final int cotHang = (soCot != null && r < soCot.length) ? soCot[r]
                    : (chiaDeu ? moiHuong : 0);
            if (cotHang > 0 && !khoi.isEmpty()) {
                // Chia deu dai noi dung cua RIENG hang nay thanh cotHang o. Moi hang co
                // buoc o rieng (hang nhin ngang rong hon hang nhin truoc, va so o moi hang
                // khong bang nhau) nen khong duoc dung mot buoc chung cho ca bang. Cach
                // nay khong an mat chan/duoi con thu nhu kieu xoa cot mong de do khe.
                int x0 = Integer.MAX_VALUE, x1 = -1;
                for (final int[] b : khoi) {
                    x0 = Math.min(x0, b[0]);
                    x1 = Math.max(x1, b[2]);
                }
                final int rong = x1 - x0 + 1;
                // Vach cat: bat dau tu vi tri chia deu, roi TRUOT VE DAY KHE gan nhat.
                // Bang sprite xep khong deu (frame nhin ngang rong hon frame nhin truoc)
                // nen chia cung se cat vao giua con thu; nhung khe giua hai con luon la
                // cho co it diem nhat, tim day khe quanh vi tri chia deu thi cat dung.
                final int[] vach = new int[cotHang + 1];
                vach[0] = x0;
                vach[cotHang] = x1 + 1;
                final int buoc = rong / cotHang;
                final int timQuanh = Math.max(2, buoc * 35 / 100);
                for (int i = 1; i < cotHang; i++) {
                    final int dich = x0 + rong * i / cotHang;
                    int tot = dich, itNhat = Integer.MAX_VALUE;
                    for (int x = Math.max(x0 + 1, dich - timQuanh);
                         x <= Math.min(x1 - 1, dich + timQuanh); x++) {
                        int s = 0;
                        for (int y = y1; y <= y2; y++) if (!nen[y][x]) s++;
                        // Cung so diem thi lay cho gan vi tri chia deu nhat
                        if (s < itNhat || (s == itNhat && Math.abs(x - dich) < Math.abs(tot - dich))) {
                            itNhat = s;
                            tot = x;
                        }
                    }
                    vach[i] = tot;
                }
                for (int i = 0; i < cotHang; i++) {
                    final int cx1 = vach[i], cx2 = vach[i + 1] - 1;
                    final int[] bb = khungBao(nen, cx1, cx2, y1, y2);
                    if (bb == null) {
                        System.out.println("   hang " + r + " o " + i + " (x " + cx1 + ".." + cx2
                                + ") trong rong");
                        continue;
                    }
                    o.add(bb);
                    dem++;
                }
                System.out.println("   hang " + r + ": dai noi dung x " + x0 + ".." + x1
                        + " -> " + cotHang + " o, buoc " + String.format("%.1f", rong / (double) cotHang) + "px");
            } else {
                o.addAll(khoi);
                dem = khoi.size();
            }
            demTheoHang.add(dem);
        }
        if (vungBong != null) {
            final int[] bb = khungBao(nen, Math.max(0, vungBong[0]),
                    Math.min(w - 1, vungBong[0] + vungBong[2] - 1),
                    Math.max(0, vungBong[1]), Math.min(h - 1, vungBong[1] + vungBong[3] - 1));
            if (bb != null) bong.add(bb);
        }

        System.out.print("   so frame moi hang:");
        for (int i = 0; i < demTheoHang.size(); i++) System.out.print(" h" + i + "=" + demTheoHang.get(i));
        System.out.println("  (tong " + o.size() + ")");
        if (o.isEmpty()) {
            System.out.println("   Khong cat duoc frame nao. Thu doi --alpha / --nhieu / --khe / --mongcot.");
            return;
        }

        // ---- Cat pixel o 4x tu khung 1x, roi bo sat vien lai theo alpha that ----
        final List<BufferedImage> frame = new ArrayList<>();
        for (final int[] b : o) {
            BufferedImage f = catTheoKhung1x(sach, b, phong);
            // Ve dung luoi pixel goc TRUOC khi don manh vun, de nguong "manh nho" tinh
            // theo so diem that cua sprite chu khong theo so diem da phong to.
            if (ve1x) f = ve1x(f, phong);
            if (vunTiLe > 0) f = boManhVun(f, vunTiLe);
            frame.add(f);
        }
        int wo = 0, ho = 0;
        for (final BufferedImage f : frame) {
            wo = Math.max(wo, f.getWidth());
            ho = Math.max(ho, f.getHeight());
        }
        System.out.println("   o lon nhat " + wo + "x" + ho + " (o 4x)");

        if (chayThu) {
            System.out.println("   (--thu) khong ghi file.");
            return;
        }
        new File(ra).mkdirs();
        for (int i = 0; i < bong.size(); i++) {
            final BufferedImage b = catTheoKhung1x(sach, bong.get(i), phong);
            ImageIO.write(b, "png", new File(ra + "/" + ten + "_bong" + (i == 0 ? "" : i) + ".png"));
            System.out.println("   -> bong do " + b.getWidth() + "x" + b.getHeight() + " ghi ra "
                    + ten + "_bong" + (i == 0 ? "" : i) + ".png");
        }
        veBangDanhSo(frame, ra + "/" + ten + "_bang.png");
        System.out.println("   -> " + ra + "/" + ten + "_bang.png (co danh so, chi de xem)");

        if (lay != null) {
            // Chon frame theo tung huong: --lay=<nhom xuong>|<nhom len>|<nhom ngang>, moi
            // nhom la danh sach chi so o trong <ten>_bang.png. Can den vi bang sprite xep
            // khong deu: frame cua ba huong khong nam thanh hang rieng, phai chi dinh tay
            // sau khi doc bang danh so.
            final String[] nhom = lay.split("\\|");
            final List<List<BufferedImage>> chon = new ArrayList<>();
            int moiNhom = -1;
            for (final String nh : nhom) {
                final List<BufferedImage> ds = new ArrayList<>();
                for (final String s : nh.split(",")) {
                    final int idx = Integer.parseInt(s.trim());
                    if (idx < 0 || idx >= frame.size()) {
                        System.out.println("   --lay: chi so " + idx + " ngoai khoang 0.."
                                + (frame.size() - 1));
                        return;
                    }
                    ds.add(frame.get(idx));
                }
                if (moiNhom < 0) moiNhom = ds.size();
                else if (moiNhom != ds.size()) {
                    System.out.println("   --lay: cac nhom phai co cung so frame ("
                            + moiNhom + " vs " + ds.size() + ")");
                    return;
                }
                chon.add(ds);
            }
            int cw = 0, chh = 0;
            for (final List<BufferedImage> ds : chon) {
                for (final BufferedImage f : ds) {
                    cw = Math.max(cw, f.getWidth());
                    chh = Math.max(chh, f.getHeight());
                }
            }
            // Le 8px quanh moi o: TaoEffThuCuoi do hang/cot bang khoang trong, ma frame cao
            // nhat lai vua khit chieu cao o -> khong co le thi no dinh vao hang tren/duoi
            // va ca bang bi coi la mot hang.
            final int LE = 8;
            cw += LE;
            chh += LE;
            final BufferedImage bangLay = new BufferedImage(cw * moiNhom, chh * chon.size(),
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics2D gl = bangLay.createGraphics();
            for (int r = 0; r < chon.size(); r++) {
                for (int i = 0; i < moiNhom; i++) {
                    final BufferedImage f = chon.get(r).get(i);
                    gl.drawImage(f, i * cw + (cw - f.getWidth()) / 2,
                            r * chh + (chh - f.getHeight()) - LE / 2, null);
                }
            }
            gl.dispose();
            ImageIO.write(bangLay, "png", new File(ra + "/" + ten + ".png"));
            System.out.println("   -> " + ra + "/" + ten + ".png  " + bangLay.getWidth() + "x"
                    + bangLay.getHeight() + " (" + chon.size() + " hang x " + moiNhom
                    + " frame, o " + cw + "x" + chh + ")");
            return;
        }

        if (moiHuong > 0 && frame.size() % moiHuong != 0) {
            System.out.println("   CANH BAO: " + frame.size() + " frame khong chia het cho "
                    + moiHuong + " frame/huong -> chua ghi bang chuan. Xem " + ten + "_bang.png"
                    + " de biet o nao bi gop hay bi tach doi, roi chinh --khe / --mongcot / --nhieu.");
            return;
        }

        // Bang chuan: o deu nhau, frame dat SAT DAY o va can giua. TaoEffThuCuoi lay moc
        // doc chung ca hang nen moi frame phai dung tren cung mot vach dat, khong thi con
        // thu se giat len giat xuong giua cac frame.
        final int cot = (moiHuong > 0) ? moiHuong : frame.size();
        final int soDong = (frame.size() + cot - 1) / cot;
        final BufferedImage bang = new BufferedImage(wo * cot, ho * soDong, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = bang.createGraphics();
        for (int i = 0; i < frame.size(); i++) {
            final BufferedImage f = frame.get(i);
            g.drawImage(f, (i % cot) * wo + (wo - f.getWidth()) / 2,
                    (i / cot) * ho + (ho - f.getHeight()), null);
        }
        g.dispose();
        ImageIO.write(bang, "png", new File(ra + "/" + ten + ".png"));
        System.out.println("   -> " + ra + "/" + ten + ".png  " + bang.getWidth() + "x"
                + bang.getHeight() + " (" + soDong + " hang x " + cot + " frame, o " + wo + "x" + ho + ")");
    }

    /**
     * Bo cac manh vun cua o ben canh: giu lai cac vung lien thong lon (>= tiLe % vung lon
     * nhat), xoa nhung manh nho hon.
     *
     * Can den vi vach cat chay giua hai con thu dinh nhau nen frame nao cung lem mot
     * mieng cua con ben canh (mot soi tai, mot goc canh). Than con thu la mot vung lien
     * thong lon, con manh lem thi nho — nen loc theo do lon la sach.
     */
    private static BufferedImage boManhVun(final BufferedImage f, final int tiLe) {
        final int w = f.getWidth(), h = f.getHeight();
        final int[][] nhan = new int[h][w];
        final List<Integer> coVung = new ArrayList<>();
        coVung.add(0);
        int soVung = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((f.getRGB(x, y) >>> 24) & 0xFF) == 0 || nhan[y][x] != 0) continue;
                soVung++;
                int n = 0;
                final Deque<int[]> q = new ArrayDeque<>();
                q.add(new int[]{x, y});
                nhan[y][x] = soVung;
                while (!q.isEmpty()) {
                    final int[] p = q.poll();
                    n++;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            final int nx = p[0] + dx, ny = p[1] + dy;
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                            if (nhan[ny][nx] != 0 || ((f.getRGB(nx, ny) >>> 24) & 0xFF) == 0) continue;
                            nhan[ny][nx] = soVung;
                            q.add(new int[]{nx, ny});
                        }
                    }
                }
                coVung.add(n);
            }
        }
        int lonNhat = 0;
        for (int i = 1; i < coVung.size(); i++) lonNhat = Math.max(lonNhat, coVung.get(i));
        if (lonNhat == 0) return f;
        final BufferedImage ra = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int v = nhan[y][x];
                if (v > 0 && coVung.get(v) * 100 >= lonNhat * tiLe) ra.setRGB(x, y, f.getRGB(x, y));
            }
        }
        // Bo sat vien lai sau khi xoa manh vun
        int x1 = w, y1 = h, x2 = -1, y2 = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((ra.getRGB(x, y) >>> 24) & 0xFF) == 0) continue;
                if (x < x1) x1 = x;
                if (x > x2) x2 = x;
                if (y < y1) y1 = y;
                if (y > y2) y2 = y;
            }
        }
        if (x2 < x1 || y2 < y1) return ra;
        return ra.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    /**
     * Thu anh ve dung LUOI PIXEL GOC (1x) bang cach lay MAU AP DAO cua tung khoi phong x
     * phong, khong noi suy.
     *
     * Vi sao khong dung getScaledInstance/AREA_AVERAGING: bang sprite nay da bi phong 4x
     * bang thuat toan lam muot, nen vien pixel bi nhoe san. Neu lai thu nho bang noi suy
     * (va nhat la thu ve chieu cao le nhu 44px = 0.75x luoi goc) thi nhoe them mot lan
     * nua — do la ly do con thu nhin "mo mo" so voi pixel-art goc. Lay mau ap dao cua
     * moi khoi 4x4 tra ve gan dung mau pixel goc, vien lai dut khoat.
     *
     * Alpha: khoi nao co >= nua so diem dac thi coi la dac, con lai la trong. Sprite trong
     * game khong co alpha trung gian nen khong duoc de lai vien mo.
     */
    private static BufferedImage ve1x(final BufferedImage f, final int phong) {
        if (phong <= 1) {
            return f;
        }
        final int w = Math.max(1, f.getWidth() / phong), h = Math.max(1, f.getHeight() / phong);
        final BufferedImage ra = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final java.util.HashMap<Integer, Integer> dem = new java.util.HashMap<Integer, Integer>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dem.clear();
                int soDac = 0, tong = 0;
                for (int dy = 0; dy < phong; dy++) {
                    for (int dx = 0; dx < phong; dx++) {
                        final int sx = x * phong + dx, sy = y * phong + dy;
                        if (sx >= f.getWidth() || sy >= f.getHeight()) {
                            continue;
                        }
                        tong++;
                        final int c = f.getRGB(sx, sy);
                        if (((c >>> 24) & 0xFF) == 0) {
                            continue;
                        }
                        soDac++;
                        final int mau = c | 0xFF000000;
                        final Integer cu = dem.get(mau);
                        dem.put(mau, cu == null ? 1 : cu + 1);
                    }
                }
                if (tong == 0 || soDac * 2 < tong || dem.isEmpty()) {
                    continue;
                }
                int mauTot = 0, nhieuNhat = -1;
                for (final java.util.Map.Entry<Integer, Integer> e : dem.entrySet()) {
                    if (e.getValue() > nhieuNhat) {
                        nhieuNhat = e.getValue();
                        mauTot = e.getKey();
                    }
                }
                ra.setRGB(x, y, mauTot);
            }
        }
        return ra;
    }

    /** Cat vung 4x tuong ung khung 1x, roi bo sat vien theo alpha that. */
    private static BufferedImage catTheoKhung1x(final BufferedImage sach, final int[] b, final int phong) {
        final int X1 = Math.max(0, b[0] * phong), Y1 = Math.max(0, b[1] * phong);
        final int X2 = Math.min(sach.getWidth() - 1, (b[2] + 1) * phong - 1);
        final int Y2 = Math.min(sach.getHeight() - 1, (b[3] + 1) * phong - 1);
        int tx1 = X2, tx2 = X1, ty1 = Y2, ty2 = Y1;
        for (int y = Y1; y <= Y2; y++) {
            for (int x = X1; x <= X2; x++) {
                if (((sach.getRGB(x, y) >>> 24) & 0xFF) == 0) continue;
                if (x < tx1) tx1 = x;
                if (x > tx2) tx2 = x;
                if (y < ty1) ty1 = y;
                if (y > ty2) ty2 = y;
            }
        }
        if (tx2 < tx1 || ty2 < ty1) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage ra = new BufferedImage(tx2 - tx1 + 1, ty2 - ty1 + 1, BufferedImage.TYPE_INT_ARGB);
        for (int y = ty1; y <= ty2; y++) {
            for (int x = tx1; x <= tx2; x++) ra.setRGB(x - tx1, y - ty1, sach.getRGB(x, y));
        }
        return ra;
    }

    /** Xoa cac dom nho hon toiThieu diem tren mat na 1x. Tra ve so dom da xoa. */
    private static int donDom(final boolean[][] nen, final int toiThieu) {
        final int h = nen.length, w = nen[0].length;
        final boolean[][] xong = new boolean[h][w];
        int xoa = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (nen[y][x] || xong[y][x]) continue;
                final List<int[]> dom = new ArrayList<>();
                final Deque<int[]> q = new ArrayDeque<>();
                q.add(new int[]{x, y});
                xong[y][x] = true;
                while (!q.isEmpty()) {
                    final int[] p = q.poll();
                    dom.add(p);
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            final int nx = p[0] + dx, ny = p[1] + dy;
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                            if (xong[ny][nx] || nen[ny][nx]) continue;
                            xong[ny][nx] = true;
                            q.add(new int[]{nx, ny});
                        }
                    }
                }
                if (dom.size() < toiThieu) {
                    for (final int[] p : dom) nen[p[1]][p[0]] = true;
                    xoa++;
                }
            }
        }
        return xoa;
    }

    /** Vung nay xam nhat mau (bong do) hay co mau (con thu)? Doc tren anh 4x. */
    private static boolean nhatMau(final BufferedImage sach, final int[] b, final int phong) {
        long lech = 0;
        int n = 0;
        for (int y = b[1] * phong; y <= (b[3] + 1) * phong - 1 && y < sach.getHeight(); y++) {
            for (int x = b[0] * phong; x <= (b[2] + 1) * phong - 1 && x < sach.getWidth(); x++) {
                final int c = sach.getRGB(x, y);
                if (((c >>> 24) & 0xFF) == 0) continue;
                final int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, bl = c & 0xFF;
                lech += Math.max(r, Math.max(g, bl)) - Math.min(r, Math.min(g, bl));
                n++;
            }
        }
        return n > 0 && lech / n < 22;
    }

    private static void veBangDanhSo(final List<BufferedImage> frame, final String duong) throws Exception {
        final int COT = 6, PAD = 6;
        int wo = 1, ho = 1;
        for (final BufferedImage f : frame) {
            wo = Math.max(wo, f.getWidth());
            ho = Math.max(ho, f.getHeight());
        }
        final double z = Math.min(1.0, 960.0 / (wo * COT));
        final int cw = (int) (wo * z) + PAD, ch = (int) (ho * z) + PAD + 14;
        final int dong = Math.max(1, (frame.size() + COT - 1) / COT);
        final BufferedImage ra = new BufferedImage(cw * COT, ch * dong, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ra.createGraphics();
        g.setColor(new Color(0x30, 0x30, 0x30));
        g.fillRect(0, 0, ra.getWidth(), ra.getHeight());
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (int i = 0; i < frame.size(); i++) {
            final BufferedImage f = frame.get(i);
            final int fw = Math.max(1, (int) (f.getWidth() * z)), fh = Math.max(1, (int) (f.getHeight() * z));
            g.drawImage(f.getScaledInstance(fw, fh, Image.SCALE_AREA_AVERAGING),
                    (i % COT) * cw + PAD / 2, (i / COT) * ch + 14, null);
            g.setColor(Color.YELLOW);
            g.drawString(String.valueOf(i), (i % COT) * cw + 4, (i / COT) * ch + 12);
        }
        g.dispose();
        ImageIO.write(ra, "png", new File(duong));
    }

    private static List<int[]> cotTrong(final boolean[][] nen, final int y1, final int y2,
                                         final int khe, final int wLuoi) {
        final List<int[]> ds = new ArrayList<>();
        int dau = -1, trong = 0;
        for (int x = 0; x < wLuoi; x++) {
            boolean co = false;
            for (int y = y1; y <= y2; y++) {
                if (!nen[y][x]) { co = true; break; }
            }
            if (co) {
                if (dau < 0) dau = x;
                trong = 0;
            } else if (dau >= 0 && ++trong >= khe) {
                ds.add(new int[]{dau, x - trong});
                dau = -1;
            }
        }
        if (dau >= 0) ds.add(new int[]{dau, wLuoi - 1});
        return ds;
    }

    private static int[] khungBao(final boolean[][] nen, final int x1, final int x2,
                                   final int y1, final int y2) {
        int tx1 = x2, tx2 = x1, ty1 = y2, ty2 = y1;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (nen[y][x]) continue;
                if (x < tx1) tx1 = x;
                if (x > tx2) tx2 = x;
                if (y < ty1) ty1 = y;
                if (y > ty2) ty2 = y;
            }
        }
        if (tx2 < tx1 || ty2 < ty1) return null;
        if (tx2 - tx1 + 1 < 6 || ty2 - ty1 + 1 < 6) return null;
        return new int[]{tx1, ty1, tx2, ty2};
    }
}
