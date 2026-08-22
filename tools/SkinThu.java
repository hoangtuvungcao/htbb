import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Dung bo anh cho SKIN LINH THU tu mot bang sprite ve tay / ve bang AI.
 *
 * Mot skin can DUNG BA thu, tool nay sinh ca ba:
 *
 *   1. map/skinthu/&lt;n&gt;.png + map/skinthu/&lt;n&gt;
 *      Con thu chay tren ban do. Anh la mot atlas 256x256, file khong duoi la du lieu
 *      frame theo dinh dang Client/DataSkillEff.loadData. Server phuc vu ca hai duoi id
 *      SkinLinhThu.ID_ANH_BAN_DO + n (xem DataGame).
 *
 *   2. map/skinthu/avt&lt;n&gt;.png
 *      Dai DUNG 3 FRAME xep doc, hien trong bang "Trang bi thu". Server gui thang byte
 *      anh nen khong can id.
 *
 *   3. map/icon/&lt;idIcon&gt;.png
 *      Icon 16x16 cua vat pham skin trong hanh trang (cot xstart cua data_item).
 *
 * ------------------------------------------------------------------------------------
 * BO CUC 21 FRAME — cho de lam sai nhat
 * ------------------------------------------------------------------------------------
 * Client khong doc so frame tu file; no tinh chi so frame bang cong thuc co dinh
 * (Client/Char.getFrameHorse):
 *
 *     huong DOWN        -> Fhorse
 *     huong UP          -> Fhorse + 7
 *     huong LEFT/RIGHT  -> Fhorse + 14
 *
 * Nen file PHAI co it nhat 21 frame, chia ba khoi 7 frame. Trong moi khoi, Fhorse chay
 * theo hai bang cua Char:
 *
 *     dung yen  : Fhorse = 0, 1           (STANDFRAME = {0 x8, 1 x4})
 *     di chuyen : Fhorse = 2, 3, 4        (horseMove = {2,2,2,3,3,4,4,4})
 *
 * HAI TAP NAY KHONG GIAO NHAU, va do chinh la y nghia cua 5 o:
 *
 *     o 0, 1     = hai tu the DUNG YEN. Phai chon hai tu the GIONG NHAU gan het, chi
 *                  khac chut it (kieu con thu dang tho). Hai tu the khac xa nhau thi
 *                  con thu dung im ma cu giat qua giat lai.
 *     o 2, 3, 4  = ba tu the DI, xep theo dung THU TU BUOC CHAN. Client chay vong
 *                  2 -> 3 -> 4 -> 2 lien tuc, moi tick mot buoc, nen xep lung tung la
 *                  dang di nhin giat cuc.
 *
 * (Truoc day cho nay ghi nham "dung yen: Fhorse = 0..3", dan den xep 5 o theo thu tu
 * trong bang ma khong tach tu the dung khoi tu the di — ket qua la ca hai hoat anh deu
 * khong lien mach. Ba nhanh quyet dinh Fhorse nam o Client/Char.cs:2108-2137 va
 * :2154-2241, doc lai o do neu con nghi ngo.)
 *
 * Vay moi khoi can 5 anh that o slot 0..4. Slot 5 va 6 khong bao gio duoc dung nhung
 * PHAI ton tai, vi paintTopHorse/paintBottomHorse thoat ngay khi Frame >= listFrame
 * .size() — thieu la con thu bien mat dung tick do, nhin nhu nhay giat. Ta cho slot
 * 5, 6 dung lai anh cua slot 0, 1 (thu cuoi Poro trong game lam dung nhu vay).
 *
 * So frame phai la BOI cua 21: Char.updateHorse lay listFrame.size() / 21 lam so vong
 * hoat anh. Ta ghi dung 21 nen so vong = 1, hoat anh hoan toan do Fhorse quyet dinh.
 *
 * ------------------------------------------------------------------------------------
 * LOP VE (onTop) — cung la cho de sai
 * ------------------------------------------------------------------------------------
 * Client/FrameEff co hai ham tao va ham dang dung bi DAO TEN THAM SO: part co onTop==0
 * lai roi vao listPartTop, part co onTop!=0 roi vao listPartBottom. Trong Char.paint
 * thu tu goi la:
 *
 *     (huong DOWN / LEFT / RIGHT)  ve than nhan vat  ->  paintTopHorse  ->  paintBottomHorse
 *     (huong UP)                   paintTopHorse  ->  paintBottomHorse  ->  ve than nhan vat
 *
 * tuc la onTop==0 ve truoc, onTop!=0 ve sau. Vi vay:
 *
 *     bong do  onTop = 0  -> ve truoc, nam duoi cung
 *     con thu  onTop = 1  -> ve sau
 *
 * Voi huong DOWN/LEFT/RIGHT thi nhan vat ve truoc ca hai, nen con thu che chan nguoi
 * ngoi — dung, vi nhin tu truoc/ngang thi than con thu o truoc chan nguoi. Voi huong UP
 * thi nhan vat ve sau cung nen lung nguoi che con thu — cung dung.
 *
 * ------------------------------------------------------------------------------------
 * CHIEU CAO — quyet dinh nguoi ngoi dung hay ngoi lo lung
 * ------------------------------------------------------------------------------------
 * Nguoi ngoi tren thu duoc nhac len bang Char.dy_horse_tool[indexdxy], la bang CO DINH
 * o server (khong doi duoc theo tung con thu). SkinLinhThu gui indexdxy = 0, tuc dong
 * {-20,-15,-15,-15}. De nguoi ngoi dung lung thu, con thu phai cao xap xi con Poro —
 * con dang dung chinh bang do. Do tu map/d_effect/56: DOWN 39px, UP 40px, NGANG 42px,
 * day con thu tho xuong 7px duoi chan nhan vat. Tool nay thu nho MOI HUONG ve dung ba
 * so do (xem CAO_DICH / THO_XUONG), nen bang sprite to nho bao nhieu cung ra dung co.
 *
 * ------------------------------------------------------------------------------------
 * HUONG NHIN CUA ANH
 * ------------------------------------------------------------------------------------
 * Client lat ngang khi di sang phai: paintTopHorse(..., (dir == RIGHT) ? 2 : 0). Nen
 * anh luu trong atlas phai nhin ve BEN TRAI. Bang sprite nao ve nhin phai thi them
 * --lat de tool lat lai.
 *
 * ------------------------------------------------------------------------------------
 * CACH DUNG
 * ------------------------------------------------------------------------------------
 *   javac -d out_tools tools/SkinThu.java
 *
 *   # Buoc 1: xem bang sprite co may hang, may o, o nao la huong nao
 *   java -cp out_tools SkinThu xem <bang.png>
 *   java -cp out_tools SkinThu xem <bang.png> --gop=6
 *
 *   # Buoc 2: dung bo anh
 *   java -cp out_tools SkinThu tao <bang.png> <n> <idIcon> \
 *        --xuong=a,b,c,d,e --len=a,b,c,d,e --ngang=a,b,c,d,e [--gop=N] [--lat]
 *
 * Tham so:
 *   --gop=N           gop them: hai manh cach nhau <= N px nua thi coi la cung mot con.
 *                     Tang len khi mot con bi tach lam nhieu o, giam khi hai con bi dinh.
 *   --xuong/--len/--ngang   DANH SACH UNG VIEN cua ba huong (chi so o 0-based, dem theo
 *                     thu tu in ra o buoc 1). Khong phai thu tu frame: cu liet ke het
 *                     cac o thuoc huong do, cang nhieu cang tot, tool tu chon ra 5 va
 *                     tu xep vao dung slot (xem chonTuThe).
 *   --lat             bang sprite ve con thu nhin sang PHAI, lat lai thanh nhin trai.
 *   --alpha=32        nguong kenh trong suot de coi la co hinh (0..255).
 *
 * Nhan dang huong khi xem bang: thay ro hai mat + than doi dien = XUONG (nhin tu truoc),
 * thay lung / phia sau dau = LEN, than nam ngang mot ben = NGANG.
 */
public class SkinThu {

    // ---- Bo cuc frame ma client quy dinh, KHONG duoc doi ----
    /** So slot frame moi huong (Char.getFrameHorse cong 0 / 7 / 14). */
    private static final int SLOT_MOI_HUONG = 7;
    /** So anh that moi huong: slot 0..4 duoc dung, slot 5,6 lap lai slot 0,1. */
    private static final int ANH_MOI_HUONG = 5;
    private static final int SO_HUONG = 3;
    private static final int SO_FRAME = SLOT_MOI_HUONG * SO_HUONG;          // 21
    private static final int SO_ANH_THAN = ANH_MOI_HUONG * SO_HUONG;        // 15

    // ---- Atlas: toa do anh con luu bang byte KHONG DAU nen atlas toi da 256x256 ----
    private static final int ATLAS = 256;
    /** Chi so anh con cua bong do: o cuoi cung cua luoi. */
    private static final int ANH_BONG = SO_ANH_THAN;                        // 15

    // ---- Co con thu, do tu map/d_effect/56 (Poro) de nguoi ngoi dung lung ----
    /** Chieu cao dich cua tung huong: xuong, len, ngang. */
    private static final int[] CAO_DICH = {39, 40, 42};
    /** Day con thu tho xuong duoi chan nhan vat bao nhieu px, tung huong. */
    private static final int[] THO_XUONG = {7, 7, 7};

    /** Hai tu the dung yen lech hon muc nay thi thoi, dung mot tu the lap lai. */
    private static final double NGUONG_DUNG = 0.12;
    /** Bo ba tu the di lech hon muc nay thi rut ve hai tu the A-B-A. */
    private static final double NGUONG_DI = 0.28;

    /** Khoang cach toi da (px) tu than de mot manh roi van duoc coi la cua con thu. */
    private static final int GAN_THAN = 4;

    /** Toi thieu de mot vung duoc coi la hinh that, khong phai bui hay chu. */
    private static final int TOI_THIEU = 8;

    private static final String[] TEN_HUONG = {"xuong", "len", "ngang"};

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            inCachDung();
            return;
        }
        final String lenh = args[0];
        final File bang = new File(args[1]);
        final BufferedImage goc = ImageIO.read(bang);
        if (goc == null) {
            System.out.println("Khong doc duoc anh: " + bang);
            return;
        }

        int alpha = 32;
        int pcHang = 22, pcCot = 12, pcCao = 100;
        int[] soCot = null;
        List<int[]> hangTay = null;
        boolean lat = false;
        final int[][] chon = new int[SO_HUONG][];
        for (int i = 2; i < args.length; i++) {
            final String a = args[i];
            if (a.startsWith("--alpha=")) alpha = Integer.parseInt(a.substring(8));
            else if (a.equals("--lat")) lat = true;
            else if (a.startsWith("--pchang=")) pcHang = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--pccot=")) pcCot = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--socot=")) soCot = docSo(a.substring(8));
            else if (a.startsWith("--cao=")) pcCao = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--hang=")) hangTay = docKhoang(a.substring(7));
            else if (a.startsWith("--xuong=")) chon[0] = docSo(a.substring(8));
            else if (a.startsWith("--len=")) chon[1] = docSo(a.substring(6));
            else if (a.startsWith("--ngang=")) chon[2] = docSo(a.substring(8));
        }

        System.out.println("Bang sprite: " + bang.getName() + "  " + goc.getWidth() + "x" + goc.getHeight());
        final boolean[][] coHinh = locNen(goc, alpha);
        final List<int[]> o = timO(coHinh, pcHang, pcCot, soCot, hangTay);
        if (o.isEmpty()) {
            System.out.println("Khong tim thay o nao. Thu ha --alpha.");
            return;
        }

        if ("xem".equals(lenh)) {
            xem(goc, coHinh, o, bang);
            return;
        }
        if (!"tao".equals(lenh)) {
            inCachDung();
            return;
        }
        if (args.length < 4) {
            System.out.println("Thieu tham so: tao <bang.png> <n> <idIcon> --xuong= --len= --ngang=");
            return;
        }
        final int n = Integer.parseInt(args[2]);
        final int idIcon = Integer.parseInt(args[3]);
        for (int h = 0; h < SO_HUONG; h++) {
            if (chon[h] == null || chon[h].length < 1) {
                System.out.println("--" + TEN_HUONG[h] + " phai co it nhat mot chi so o.");
                return;
            }
            for (final int idx : chon[h]) {
                if (idx < 0 || idx >= o.size()) {
                    System.out.println("--" + TEN_HUONG[h] + " co chi so " + idx
                            + " ngoai khoang 0.." + (o.size() - 1) + ".");
                    return;
                }
            }
        }
        tao(goc, coHinh, o, chon, n, idIcon, lat, pcCao);
    }

    private static void inCachDung() {
        System.out.println("Dung:");
        System.out.println("  java -cp out_tools SkinThu xem <bang.png> [--socot=5,5,5] [--pchang=22] [--pccot=12] [--alpha=32]");
        System.out.println("  java -cp out_tools SkinThu tao <bang.png> <n> <idIcon>"
                + " --xuong=a,b,c,d,e --len=a,b,c,d,e --ngang=a,b,c,d,e [--socot=...] [--lat]");
    }

    /** Doc danh sach khoang dang "y1-y2,y3-y4". */
    private static List<int[]> docKhoang(final String s) {
        final List<int[]> ds = new ArrayList<>();
        for (final String p : s.split(",")) {
            final String[] hai = p.trim().split("-");
            ds.add(new int[]{Integer.parseInt(hai[0].trim()), Integer.parseInt(hai[1].trim())});
        }
        return ds;
    }

    private static int[] docSo(final String s) {
        final String[] p = s.split(",");
        final int[] r = new int[p.length];
        for (int i = 0; i < p.length; i++) r[i] = Integer.parseInt(p[i].trim());
        return r;
    }

    // ================================================================================
    //                              DO TIM O TRONG BANG
    // ================================================================================

    /**
     * true = diem co hinh. Chi xet KENH TRONG SUOT, khong xet do sang.
     *
     * Cac bang sprite o day co nen trong suot va con thu thi mau toi, nen neu loc theo
     * do sang thi mat sach phan toi cua than con thu — do la ly do skin nhin "mat net".
     */
    private static boolean[][] locNen(final BufferedImage img, final int alpha) {
        final boolean[][] m = new boolean[img.getHeight()][img.getWidth()];
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                m[y][x] = ((img.getRGB(x, y) >>> 24) & 0xFF) >= alpha;
            }
        }
        return m;
    }

    /**
     * Tat ca cac o trong bang, moi o la {x1, y1, x2, y2} da sat vien, xep tu tren
     * xuong duoi roi trai qua phai.
     *
     * Hai cach do thong thuong deu KHONG dung duoc voi may bang nay, da thu:
     *   - Cat theo hang/cot trong hoan toan: khong co hang nao trong ca, vi duoi va
     *     canh cua con hang tren cham vao con hang duoi.
     *   - Vung lien thong: cac con ve dinh nhau nen ca bang ra mot vung duy nhat.
     *
     * Cach dung o day: chieu so diem co hinh xuong truc y roi cat o cho THUA, nguong
     * la pcHang phan tram cua cho day nhat. Ranh giua hai hang van con vai diem (duoi,
     * canh) nhung it hon han giua than con thu, nen nguong tuong doi tach duoc. Trong
     * moi hang thi lam tuong tu voi truc x, hoac chia deu neu da biet so o (--socot) —
     * chia deu chac an hon khi hai con dung sat khong co khe.
     */
    private static List<int[]> timO(final boolean[][] m, final int pcHang, final int pcCot,
                                    final int[] soCot, final List<int[]> hangTay) {
        final List<int[]> ket = new ArrayList<>();
        List<int[]> hang = catThua(chieuY(m, 0, m[0].length - 1), pcHang, TOI_THIEU);
        if (hangTay != null && !hangTay.isEmpty()) {
            hang = new ArrayList<>();
            for (final int[] k : hangTay) {
                hang.add(new int[]{Math.max(0, k[0]), Math.min(m.length - 1, k[1])});
            }
        }
        for (int h = 0; h < hang.size(); h++) {
            final int y1 = hang.get(h)[0], y2 = hang.get(h)[1];
            final List<int[]> cot = (soCot != null && h < soCot.length && soCot[h] > 0)
                    ? chiaDeu(m, y1, y2, soCot[h])
                    : catThua(chieuX(m, y1, y2), pcCot, TOI_THIEU);
            for (final int[] c : cot) {
                final int[] k = satVien(m, c[0], c[1], y1, y2);
                if (k != null) ket.add(k);
            }
        }
        return ket;
    }

    /** So diem co hinh cua tung hang trong khoang cot x1..x2. */
    private static int[] chieuY(final boolean[][] m, final int x1, final int x2) {
        final int[] p = new int[m.length];
        for (int y = 0; y < m.length; y++) {
            for (int x = x1; x <= x2; x++) if (m[y][x]) p[y]++;
        }
        return p;
    }

    /** So diem co hinh cua tung cot trong khoang hang y1..y2. */
    private static int[] chieuX(final boolean[][] m, final int y1, final int y2) {
        final int[] p = new int[m[0].length];
        for (int x = 0; x < m[0].length; x++) {
            for (int y = y1; y <= y2; y++) if (m[y][x]) p[x]++;
        }
        return p;
    }

    /**
     * Cat mot bieu do chieu thanh cac doan "day", bo cac doan "thua".
     *
     * Nguong = pc phan tram cua gia tri lon nhat. Dat pc = 0 la quay ve cach cu (chi
     * cat cho bang 0).
     */
    private static List<int[]> catThua(final int[] p, final int pc, final int toiThieu) {
        int max = 0;
        for (final int v : p) max = Math.max(max, v);
        final int nguong = max * pc / 100;
        final List<int[]> ds = new ArrayList<>();
        int dau = -1;
        for (int i = 0; i <= p.length; i++) {
            final boolean day = i < p.length && p[i] > nguong;
            if (day && dau < 0) dau = i;
            if (!day && dau >= 0) {
                if (i - dau >= toiThieu) ds.add(new int[]{dau, i - 1});
                dau = -1;
            }
        }
        return ds;
    }

    /** Chia deu phan co hinh cua hang thanh k o — chac an voi bang ke luoi deu. */
    private static List<int[]> chiaDeu(final boolean[][] m, final int y1, final int y2, final int k) {
        final List<int[]> ds = new ArrayList<>();
        final int[] p = chieuX(m, y1, y2);
        int x1 = -1, x2 = -1;
        for (int x = 0; x < p.length; x++) {
            if (p[x] <= 0) continue;
            if (x1 < 0) x1 = x;
            x2 = x;
        }
        if (x1 < 0) return ds;
        final int rong = x2 - x1 + 1;
        for (int i = 0; i < k; i++) {
            ds.add(new int[]{x1 + rong * i / k, x1 + rong * (i + 1) / k - 1});
        }
        return ds;
    }

    /** Khung sat vien cua vung, hoac null neu qua nho. */
    private static int[] satVien(final boolean[][] m, final int x1, final int x2,
                                 final int y1, final int y2) {
        int ax = x2, bx = x1, ay = y2, by = y1;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (!m[y][x]) continue;
                if (x < ax) ax = x;
                if (x > bx) bx = x;
                if (y < ay) ay = y;
                if (y > by) by = y;
            }
        }
        if (bx - ax + 1 < TOI_THIEU || by - ay + 1 < TOI_THIEU) return null;
        return new int[]{ax, ay, bx, by};
    }

    // ================================================================================
    //                                   XEM BANG
    // ================================================================================

    /** In danh sach o va ghi mot ban sao co danh so de nguoi dung chon huong. */
    private static void xem(final BufferedImage goc, final boolean[][] coHinh,
                            final List<int[]> o, final File bang) throws Exception {
        System.out.println("Do duoc " + o.size() + " o:");
        for (int i = 0; i < o.size(); i++) {
            final int[] k = o.get(i);
            System.out.printf("  o %2d: x %3d..%3d  y %3d..%3d   %3dx%-3d%n",
                    i, k[0], k[2], k[1], k[3], k[2] - k[0] + 1, k[3] - k[1] + 1);
        }
        final BufferedImage ra = new BufferedImage(goc.getWidth(), goc.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ra.createGraphics();
        g.setColor(new Color(0x30, 0x30, 0x38));
        g.fillRect(0, 0, ra.getWidth(), ra.getHeight());
        g.drawImage(goc, 0, 0, null);
        for (int i = 0; i < o.size(); i++) {
            final int[] k = o.get(i);
            g.setColor(new Color(0x00, 0xC8, 0xFF));
            g.drawRect(k[0], k[1], k[2] - k[0], k[3] - k[1]);
            g.setColor(Color.YELLOW);
            g.drawString(String.valueOf(i), k[0] + 1, k[1] + 11);
        }
        g.dispose();
        final File out = new File("map/skinthu_goc");
        out.mkdirs();
        final String ten = bang.getName().replaceAll("\\.png$", "") + "_so.png";
        ImageIO.write(ra, "png", new File(out, ten));
        System.out.println("-> map/skinthu_goc/" + ten + " (co danh so, chi de xem)");
        bangRoi(goc, coHinh, o, new File(out, ten.replace("_so.png", "_roi.png")));
        System.out.println("-> map/skinthu_goc/" + ten.replace("_so.png", "_roi.png")
                + " (tung o cat roi, de nhan huong)");
    }

    /**
     * Xep tung o da cat roi thanh mot bang, moi o mot khung co danh so.
     *
     * Nhin bang goc rat kho doan o nao thuoc huong nao vi cac con de len nhau; cat roi
     * ra roi xep deu thi thay ngay tung tu the.
     */
    private static void bangRoi(final BufferedImage goc, final boolean[][] m,
                                final List<int[]> o, final File ra) throws Exception {
        final int cot = Math.min(6, Math.max(1, o.size()));
        final int dong = (o.size() + cot - 1) / cot;
        int wMax = 1, hMax = 1;
        for (final int[] k : o) {
            wMax = Math.max(wMax, k[2] - k[0] + 1);
            hMax = Math.max(hMax, k[3] - k[1] + 1);
        }
        final int oW = wMax + 8, oH = hMax + 18;
        final BufferedImage im = new BufferedImage(cot * oW, dong * oH, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = im.createGraphics();
        g.setColor(new Color(0x28, 0x28, 0x30));
        g.fillRect(0, 0, im.getWidth(), im.getHeight());
        for (int i = 0; i < o.size(); i++) {
            final int[] k = o.get(i);
            final int ox = (i % cot) * oW, oy = (i / cot) * oH;
            g.setColor(new Color(0x40, 0x40, 0x4A));
            g.drawRect(ox, oy, oW - 1, oH - 1);
            final BufferedImage f = cat(goc, m, k, 0);
            g.drawImage(f, ox + (oW - f.getWidth()) / 2, oy + 16 + (hMax - f.getHeight()), null);
            // (giu nguyen co goc: bang sprite da lon san, phong them chi lam file to)
            g.setColor(Color.YELLOW);
            g.drawString(String.valueOf(i), ox + 3, oy + 12);
        }
        g.dispose();
        ImageIO.write(im, "png", ra);
    }

    // ================================================================================
    //                                  DUNG BO ANH
    // ================================================================================

    private static void tao(final BufferedImage goc, final boolean[][] coHinh,
                            final List<int[]> o, final int[][] chon,
                            final int n, final int idIcon, final boolean lat,
                            final int pcCao) throws Exception {
        // Thu nho TUNG FRAME ve dung chieu cao dich cua huong do, khong dung chung mot
        // ti le cho ca nhom.
        //
        // Dung chung ti le nghe co ly hon (giu ti le that giua cac tu the) nhung khong
        // hop voi may bang nay: 5 tu the cua mot huong duoc ve o nam hang khac nhau,
        // moi hang mot co, chenh nhau toi 29%. Giu nguyen chenh lech do thi con thu
        // phinh to thu nho theo tung buoc chan — nhin nhu dang thoi bong. Chenh lech o
        // day la do nguoi ve, khong phai do tu the, nen chuan hoa tung frame moi dung.
        final BufferedImage[] than = new BufferedImage[SO_ANH_THAN];
        for (int h = 0; h < SO_HUONG; h++) {
            final int caoDich = Math.max(4, CAO_DICH[h] * pcCao / 100);
            // Cat het ung vien cua huong nay ra truoc, roi moi chon 5 cai hop nhau nhat.
            final BufferedImage[] ungVien = new BufferedImage[chon[h].length];
            final StringBuilder co = new StringBuilder();
            for (int f = 0; f < chon[h].length; f++) {
                BufferedImage im = cat(goc, coHinh, o.get(chon[h][f]), caoDich);
                if (lat) im = latNgang(im);
                ungVien[f] = im;
                co.append(String.format(" o%d(%dx%d)", chon[h][f], im.getWidth(), im.getHeight()));
            }
            System.out.printf("  huong %-5s cao %2dpx, %d ung vien:%s%n",
                    TEN_HUONG[h], caoDich, ungVien.length, co);
            final int[] xep = chonTuThe(ungVien, TEN_HUONG[h]);
            for (int f = 0; f < ANH_MOI_HUONG; f++) {
                than[h * ANH_MOI_HUONG + f] = ungVien[xep[f]];
            }
        }

        final BufferedImage bong = veBong();
        final BufferedImage[] tatCa = new BufferedImage[SO_ANH_THAN + 1];
        System.arraycopy(than, 0, tatCa, 0, SO_ANH_THAN);
        tatCa[ANH_BONG] = bong;

        final int[][] vung = xepAtlas(tatCa);
        if (vung == null) {
            System.out.println("LOI: khong xep vua " + tatCa.length + " anh vao atlas "
                    + ATLAS + "x" + ATLAS + ". Giam CAO_DICH.");
            return;
        }
        final BufferedImage atlas = new BufferedImage(ATLAS, ATLAS, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = atlas.createGraphics();
        for (int i = 0; i < tatCa.length; i++) {
            g.drawImage(tatCa[i], vung[i][0], vung[i][1], null);
        }
        g.dispose();

        final File dir = new File("map/skinthu");
        dir.mkdirs();
        ImageIO.write(atlas, "png", new File(dir, n + ".png"));
        // Canh cho nam frame cua moi huong chong khit nhau, tinh rieng tung huong.
        final int[] dxTheoAnh = new int[SO_ANH_THAN];
        for (int h = 0; h < SO_HUONG; h++) {
            final BufferedImage[] nhom = new BufferedImage[ANH_MOI_HUONG];
            System.arraycopy(than, h * ANH_MOI_HUONG, nhom, 0, ANH_MOI_HUONG);
            final int[] dx = canhChongKhit(nhom);
            System.arraycopy(dx, 0, dxTheoAnh, h * ANH_MOI_HUONG, ANH_MOI_HUONG);
            final StringBuilder s = new StringBuilder();
            for (final int v : dx) s.append(String.format(" %4d", v));
            System.out.printf("  dx %-5s:%s%n", TEN_HUONG[h], s);
        }
        ghiDuLieuFrame(new File(dir, String.valueOf(n)), vung, than, bong, dxTheoAnh);

        ghiAvatar(new File(dir, "avt" + n + ".png"), than);
        ghiIcon(new File("map/icon/" + idIcon + ".png"), than[0]);
        ghiKiemTra(new File("map/skinthu_goc/" + n + "_kiemtra.png"), than, bong);

        System.out.println("Xong skin " + n + ":");
        System.out.println("  map/skinthu/" + n + ".png       (atlas " + ATLAS + "x" + ATLAS
                + ", " + (SO_ANH_THAN + 1) + " anh con)");
        System.out.println("  map/skinthu/" + n + "            (" + SO_FRAME + " frame)");
        System.out.println("  map/skinthu/avt" + n + ".png     (3 frame, bang Trang bi thu)");
        System.out.println("  map/icon/" + idIcon + ".png      (icon vat pham)");
    }

    /**
     * Xep cac anh vao atlas theo kieu ke tang: xep tu trai qua phai, het chieu ngang
     * thi xuong tang moi cao bang anh cao nhat cua tang truoc.
     *
     * Khong dung luoi o co dinh vi tu the NGANG cua vai con dai hon han tu the dung
     * (con Chien Lang: 66px so voi 40px), o vuong phai lay theo canh dai nhat nen se
     * khong du 16 o trong 256x256. Ke tang thi anh nao rong bao nhieu chiem bay nhieu.
     *
     * Tra ve mang {x, y, w, h} cho tung anh, hoac null neu khong xep vua. Toa do va
     * kich thuoc deu phai <= 255 vi file du lieu luu chung bang byte khong dau.
     *
     * Thu tu xep khong quan trong voi client — no doc toa do tu file du lieu — nhung
     * giu nguyen thu tu dau vao cho de doi chieu khi mo atlas ra xem.
     */
    private static int[][] xepAtlas(final BufferedImage[] anh) {
        final int[][] vung = new int[anh.length][];
        int x = 0, y = 0, caoTang = 0;
        for (int i = 0; i < anh.length; i++) {
            final int w = anh[i].getWidth(), h = anh[i].getHeight();
            if (w > ATLAS || h > ATLAS) return null;
            if (x + w > ATLAS) {
                x = 0;
                y += caoTang;
                caoTang = 0;
            }
            if (y + h > ATLAS) return null;
            vung[i] = new int[]{x, y, w, h};
            x += w;
            caoTang = Math.max(caoTang, h);
        }
        return vung;
    }

    /**
     * Cat o ra khoi bang, bo het diem khong thuoc o, thu nho theo ti le chung.
     *
     * Ti le 1 thi tra ve y nguyen, khong cho di qua bo loc thu nho: sprite pixel-art
     * ma qua bo loc trung binh vung la vien bi lam mem, nhin mo ngay ca khi kich thuoc
     * khong doi.
     */
    private static BufferedImage cat(final BufferedImage goc, final boolean[][] m,
                                     final int[] k, final int caoDich) {
        final int w0 = k[2] - k[0] + 1, h0 = k[3] - k[1] + 1;
        boolean[][] o = new boolean[h0][w0];
        for (int y = 0; y < h0; y++) {
            for (int x = 0; x < w0; x++) o[y][x] = m[k[1] + y][k[0] + x];
        }
        o = giuThanChinh(o);
        final int[] v = satVienO(o);
        if (v == null) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final int w = v[2] - v[0] + 1, h = v[3] - v[1] + 1;
        final BufferedImage cat = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (o[v[1] + y][v[0] + x]) {
                    cat.setRGB(x, y, goc.getRGB(k[0] + v[0] + x, k[1] + v[1] + y));
                }
            }
        }
        // Ti le tinh SAU khi da bo manh roi va sat vien lai, khong tinh theo o goc:
        // o goc con om ca bong do va khoang trong nen chieu cao sai.
        if (caoDich <= 0) return cat;
        final double tiLe = (double) caoDich / h;
        final int rong = Math.max(1, (int) Math.round(w * tiLe));
        final int cao = Math.max(1, caoDich);
        if (rong == w && cao == h) return cat;

        final BufferedImage nho = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = nho.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(cat, 0, 0, rong, cao, null);
        g.dispose();
        // Don lai kenh trong suot: duoi 40 cho trong han, tren 200 cho dac han. Khong
        // don thi vien bi mo nhoe kieu anh chup, khac han sprite goc cua game.
        for (int y = 0; y < cao; y++) {
            for (int x = 0; x < rong; x++) {
                final int p = nho.getRGB(x, y);
                final int a = (p >>> 24) & 0xFF;
                if (a < 40) nho.setRGB(x, y, 0);
                else if (a > 200) nho.setRGB(x, y, p | 0xFF000000);
            }
        }
        return nho;
    }

    /**
     * Bo cac manh roi khong thuoc con thu: bong do ve san, manh yen roi, vet but.
     *
     * Bang sprite hay co mot bong do hoac mot manh trang tri nam ngay duoi con thu.
     * Cat theo o thi chung lot vao cung, lam khung bao cao them va con thu bi day len
     * lech khoi chan.
     *
     * Loc theo VI TRI chu khong theo dien tich: giu dam to nhat, roi giu them dam nao
     * CHONG THEO CHIEU DOC voi dam to nhat. Bong do va manh trang tri nam tach han
     * phia duoi nen khong chong; con chom long, duoi, chan bi vien toi lam dut thi nam
     * ngang hang voi than nen van giu duoc. Loc theo dien tich khong an: bong do cua
     * con gau truc to bang mot phan tu than, qua moi nguong hop ly.
     */
    private static boolean[][] giuThanChinh(final boolean[][] o) {
        final int h = o.length, w = o[0].length;
        final int[][] nhan = new int[h][w];
        final List<Integer> dienTich = new ArrayList<>();
        final List<int[]> khung = new ArrayList<>();
        dienTich.add(0);
        khung.add(null);
        final int[] ngan = new int[h * w];
        for (int y0 = 0; y0 < h; y0++) {
            for (int x0 = 0; x0 < w; x0++) {
                if (!o[y0][x0] || nhan[y0][x0] != 0) continue;
                final int id = dienTich.size();
                int dem = 0, dinh = 0;
                int ax = x0, bx = x0, ay = y0, by = y0;
                ngan[dinh++] = y0 * w + x0;
                nhan[y0][x0] = id;
                while (dinh > 0) {
                    final int p = ngan[--dinh];
                    final int y = p / w, x = p % w;
                    dem++;
                    if (x < ax) ax = x;
                    if (x > bx) bx = x;
                    if (y < ay) ay = y;
                    if (y > by) by = y;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            final int ny = y + dy, nx = x + dx;
                            if (ny < 0 || ny >= h || nx < 0 || nx >= w) continue;
                            if (!o[ny][nx] || nhan[ny][nx] != 0) continue;
                            nhan[ny][nx] = id;
                            ngan[dinh++] = ny * w + nx;
                        }
                    }
                }
                dienTich.add(dem);
                khung.add(new int[]{ax, ay, bx, by});
            }
        }
        if (dienTich.size() <= 2) return o;

        int toNhat = 1;
        for (int i = 2; i < dienTich.size(); i++) {
            if (dienTich.get(i) > dienTich.get(toNhat)) toNhat = i;
        }
        final boolean[] giu = new boolean[dienTich.size()];
        giu[toNhat] = true;
        // Giu dam nao co diem cach than khong qua GAN_THAN px. Chom long, tai, duoi bi
        // vien toi lam dut chi cach than vai diem; con bong do va manh yen ve roi thi
        // nam cach han ra nen bi bo. Do bang khoang cach that (loang tu than ra) chu
        // khong bang khung bao: khung bao cua mot cai duoi dai co the trum ca bong do.
        final int[] xa = loangTuThan(nhan, toNhat, GAN_THAN);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int id = nhan[y][x];
                if (id > 0 && xa[y * w + x] >= 0) giu[id] = true;
            }
        }
        // ... nhung khong giu dam nao THO XUONG DUOI CHAN con thu. Nhieu bang sprite ve
        // bong do hoac mot manh yen nam ngay sat duoi chan, gan den muc phep do khoang
        // cach o tren van nhan. Cai gia phai tra khong nho: khung bao cao them nen con
        // thu bi thu nho lai va bi day len khoi mat dat — nhin ro nhat o tu the dung
        // yen, con thu lo lung tren vach dat con manh rac thi cham dat.
        final int[] than = khung.get(toNhat);
        for (int i = 1; i < dienTich.size(); i++) {
            if (i != toNhat && giu[i] && khung.get(i)[3] > than[3] + 2) {
                giu[i] = false;
            }
        }

        final boolean[][] r = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) r[y][x] = o[y][x] && giu[nhan[y][x]];
        }
        return r;
    }

    /**
     * Loang tu dam {@code than} ra toi da {@code toiDa} buoc, tra ve mang khoang cach
     * (-1 = xa hon toiDa). Dung de biet dam nao nam sat than.
     */
    private static int[] loangTuThan(final int[][] nhan, final int than, final int toiDa) {
        final int h = nhan.length, w = nhan[0].length;
        final int[] d = new int[h * w];
        java.util.Arrays.fill(d, -1);
        final int[] hangDoi = new int[h * w];
        int dau = 0, cuoi = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (nhan[y][x] == than) {
                    d[y * w + x] = 0;
                    hangDoi[cuoi++] = y * w + x;
                }
            }
        }
        while (dau < cuoi) {
            final int p = hangDoi[dau++];
            if (d[p] >= toiDa) continue;
            final int y = p / w, x = p % w;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    final int ny = y + dy, nx = x + dx;
                    if (ny < 0 || ny >= h || nx < 0 || nx >= w) continue;
                    final int q = ny * w + nx;
                    if (d[q] >= 0) continue;
                    d[q] = d[p] + 1;
                    hangDoi[cuoi++] = q;
                }
            }
        }
        return d;
    }

    /** Khung bao sat vien cua mot mat na o, hoac null neu rong. */
    private static int[] satVienO(final boolean[][] o) {
        int ax = Integer.MAX_VALUE, ay = Integer.MAX_VALUE, bx = -1, by = -1;
        for (int y = 0; y < o.length; y++) {
            for (int x = 0; x < o[y].length; x++) {
                if (!o[y][x]) continue;
                if (x < ax) ax = x;
                if (x > bx) bx = x;
                if (y < ay) ay = y;
                if (y > by) by = y;
            }
        }
        return bx < 0 ? null : new int[]{ax, ay, bx, by};
    }

    private static BufferedImage latNgang(final BufferedImage im) {
        final BufferedImage r = new BufferedImage(im.getWidth(), im.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                r.setRGB(im.getWidth() - 1 - x, y, im.getRGB(x, y));
            }
        }
        return r;
    }

    /**
     * Bong do duoi chan con thu.
     *
     * Thu cuoi "tool" khong duoc client ve bong ho: Char.paint chi ve Res.imgShadow
     * trong nhanh useHorse, ma SkinLinhThu gui userHorse = 0 de tat nhanh do. Nen bong
     * phai nam san trong sprite — con Rong Lon trong game cung lam vay.
     */
    private static BufferedImage veBong() {
        final int w = 30, h = 12;
        final BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(0, 0, w, h);
        g.dispose();
        return im;
    }

    // ================================================================================
    //                          FILE DU LIEU FRAME (21 FRAME)
    // ================================================================================

    /**
     * Ghi file du lieu frame dung dinh dang Client/DataSkillEff.loadData:
     *
     *   byte   so anh con
     *     moi anh con: 5 byte KHONG DAU = id, x, y, w, h   (nen moi so phai <= 255)
     *   short  so frame
     *     moi frame: byte so part, roi moi part = short dx, short dy,
     *                byte id anh con, byte flip, byte onTop
     *   byte   so buoc sequence, roi moi buoc = short chi so frame
     *   byte   indexStartSkill
     *   byte n0 + n0 byte   -> frameChar[0]
     *   byte n1 + n1 byte   -> frameChar[1]
     *   byte n3 + n3 byte   -> frameChar[3]
     *
     * sequence va frameChar de trong: hai thu do danh cho hieu ung ky nang, thu cuoi
     * khong dung (con Poro trong game cung de trong).
     */
    private static void ghiDuLieuFrame(final File ra, final int[][] vung,
                                       final BufferedImage[] than, final BufferedImage bong,
                                       final int[] dxTheoAnh)
            throws Exception {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(bo);

        out.writeByte(vung.length);
        for (int i = 0; i < vung.length; i++) {
            for (final int v : vung[i]) {
                // Bon so nay ghi bang BYTE, client doc lai bang readUnsignedByte. Qua 255
                // la writeByte cat mat bit cao va client doc ra mot so khac han — anh con
                // ve sai vung, khong bao loi gi ca. Chan som cho de tim.
                if (v < 0 || v > 255) {
                    throw new IllegalStateException("Anh con " + i + " co so " + v
                            + " ngoai khoang 0..255, khong ghi bang byte duoc. Giam --cao.");
                }
            }
            out.writeByte(i);              // id anh con
            out.writeByte(vung[i][0]);     // x trong atlas
            out.writeByte(vung[i][1]);     // y trong atlas
            out.writeByte(vung[i][2]);     // rong
            out.writeByte(vung[i][3]);     // cao
        }

        out.writeShort(SO_FRAME);
        for (int h = 0; h < SO_HUONG; h++) {
            for (int slot = 0; slot < SLOT_MOI_HUONG; slot++) {
                // Slot 5, 6 khong bao gio duoc dung (Fhorse chi chay 0..4) nhung phai
                // ton tai, khong thi paintTopHorse thoat som va con thu bien mat.
                final int f = (slot < ANH_MOI_HUONG) ? slot : slot - ANH_MOI_HUONG;
                final int idAnh = h * ANH_MOI_HUONG + f;
                final BufferedImage im = than[idAnh];
                final int dxThan = dxTheoAnh[idAnh];
                final int dyThan = -(im.getHeight() - THO_XUONG[h]);

                out.writeByte(2);                                  // 2 part: bong + than
                // Bong: onTop = 0 -> ve truoc, nam duoi cung.
                out.writeShort(-(bong.getWidth() / 2));
                out.writeShort(-(bong.getHeight() / 2));
                out.writeByte(ANH_BONG);
                out.writeByte(0);                                  // flip
                out.writeByte(0);                                  // onTop
                // Than con thu: onTop = 1 -> ve sau bong.
                out.writeShort(dxThan);
                out.writeShort(dyThan);
                out.writeByte(idAnh);
                out.writeByte(0);
                out.writeByte(1);
            }
        }

        out.writeByte(0);   // so buoc sequence
        out.writeByte(0);   // indexStartSkill
        out.writeByte(0);   // frameChar[0]
        out.writeByte(0);   // frameChar[1]
        out.writeByte(0);   // frameChar[3]
        out.flush();

        try (FileOutputStream f = new FileOutputStream(ra)) {
            f.write(bo.toByteArray());
        }
    }

    /**
     * Chon va xep 5 tu the cho MOT huong tu mot danh sach ung vien.
     *
     * Day la cho quyet dinh hoat anh nhin muot hay giat, hon ca chuyen canh toa do.
     *
     * Cac bang sprite nay khong phai bo frame cua mot hoat anh — chung la nam buc ve
     * roi rac, moi buc mot co, mot dang. Do thu: cac tu the trong cung mot nhom lech
     * hinh nhau 14% den 48%, be ngang chenh nhau toi 48%. Cu lay theo thu tu trong bang
     * thi con thu doi ca kich thuoc lan hinh dang moi khi doi frame — do la cai "khong
     * lien mach" ma nguoi choi thay.
     *
     * Nen o day khong lay theo thu tu nua ma CHON THEO DO GIONG NHAU:
     *
     *   dung yen (o 0, 1)  hai tu the giong nhau nhat. Neu doi giong nhat van lech qua
     *                      NGUONG_DUNG thi dung MOT tu the lap lai hai lan — con thu
     *                      dung im hoan toan con hon dung im ma cu giat.
     *   di       (o 2,3,4) bo ba co do lech lon nhat nho nhat. Client chay vong
     *                      2->3->4->2 nen tong ba canh khong doi du xep kieu gi, chi
     *                      can chon dung bo ba. Neu bo ba tot nhat van lech qua
     *                      NGUONG_DI thi rut ve hai tu the (A, B, A) — buoc di hai
     *                      nhip gon gang hon ba nhip so le.
     *
     * Tra ve 5 chi so (tro vao {@code ungVien}), co the trung nhau.
     */
    private static int[] chonTuThe(final BufferedImage[] ungVien, final String tenHuong) {
        final int n = ungVien.length;
        final double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                d[i][j] = d[j][i] = khacNhau(ungVien[i], ungVien[j]);
            }
        }

        // --- hai o dung yen ---
        int a = 0, b = 0;
        double tot = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (d[i][j] < tot) {
                    tot = d[i][j];
                    a = i;
                    b = j;
                }
            }
        }
        if (n < 2 || tot > NGUONG_DUNG) {
            a = b = medoid(d);   // lap lai mot tu the: dung im han
            tot = 0;
        }

        // --- ba o di ---
        int p = -1, q = -1, r = -1;
        double totDi = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    final double xau = Math.max(d[i][j], Math.max(d[j][k], d[i][k]));
                    if (xau < totDi) {
                        totDi = xau;
                        p = i;
                        q = j;
                        r = k;
                    }
                }
            }
        }
        String kieuDi = "3 tu the";
        if (p < 0 || totDi > NGUONG_DI) {
            // Rut ve hai tu the giong nhau nhat, chay A - B - A.
            int x = 0, y = 0;
            double t2 = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (d[i][j] < t2) {
                        t2 = d[i][j];
                        x = i;
                        y = j;
                    }
                }
            }
            if (n < 2) {
                x = y = 0;
            }
            p = x;
            q = y;
            r = x;
            totDi = t2;
            kieuDi = "2 tu the (A-B-A)";
        }

        System.out.printf("  chon %-5s: dung [%d,%d] lech %.0f%%  |  di [%d,%d,%d] lech %.0f%% (%s)%n",
                tenHuong, a, b, tot * 100, p, q, r, totDi * 100, kieuDi);
        return new int[]{a, b, p, q, r};
    }

    /** Tu the "trung tam" nhat: tong do lech toi cac tu the khac nho nhat. */
    private static int medoid(final double[][] d) {
        int tot = 0;
        double nhoNhat = Double.MAX_VALUE;
        for (int i = 0; i < d.length; i++) {
            double s = 0;
            for (int j = 0; j < d.length; j++) s += d[i][j];
            if (s < nhoNhat) {
                nhoNhat = s;
                tot = i;
            }
        }
        return tot;
    }

    /**
     * Ti le diem anh KHONG trung nhau giua hai tu the, sau khi da canh chong khit nhat.
     * 0 = trung khit, 1 = khong dinh nhau ti nao.
     */
    private static double khacNhau(final BufferedImage x, final BufferedImage y) {
        final int d = doLechTot(x, y);
        final boolean[][] a = mask(x), b = mask(y);
        int giao = 0, hop = 0;
        final int h = Math.max(a.length, b.length);
        for (int yy = 0; yy < h; yy++) {
            for (int xx = -y.getWidth(); xx < x.getWidth() + y.getWidth(); xx++) {
                final boolean pa = lay(a, xx, yy);
                final boolean pb = lay(b, xx - d, yy);
                if (pa && pb) giao++;
                if (pa || pb) hop++;
            }
        }
        return hop == 0 ? 0 : 1.0 - (double) giao / hop;
    }

    private static boolean lay(final boolean[][] m, final int x, final int y) {
        return y >= 0 && y < m.length && x >= 0 && x < m[0].length && m[y][x];
    }

    /**
     * Tinh dx cho tung frame cua MOT huong, sao cho ca nam frame chong khit len nhau
     * nhat. Tra ve mang dx dung thu tu frame.
     *
     * Day la cho quyet dinh hoat anh co lien mach hay khong.
     *
     * Cach hien nhien — canh giua theo khung bao, dx = -rong/2 — thi SAI: khung bao doi
     * theo cai duoi, cai tai, cai canh tho ra o tung tu the, nen moi frame lech tam mot
     * chut, do duoc 4 den 6 px giua hai frame lien tiep. Client chay vong 2->3->4->2 moi
     * tick nen con thu truot ngang qua lai lien tuc, nhin nhu bi giat.
     *
     * Canh theo trong tam vung chan cung khong an: cac tu the trong bang khac nhau qua
     * nhieu (co tu the chum chan, co tu the nhay) nen vung chan moi frame moi khac, do
     * lai con lech hon ca cach cu (11 px).
     *
     * Cach dung o day la do thang cai can do: thu tung do lech, chon do lech lam frame
     * chong khit voi frame mau nhat (dem so diem ca hai cung dac). Phan than to nhat
     * quyet dinh ket qua nen than dung yen, chi cho chan va duoi dong day — dung y nghia
     * "buoc di". Khong can doan dau la chan, dau la duoi.
     *
     * Frame mau la frame co nhieu diem dac nhat (tu the day du nhat), khong phai frame
     * dau: frame dau co the la mot tu the khuyet, keo ca loat lech theo.
     */
    private static int[] canhChongKhit(final BufferedImage[] anh) {
        int mau = 0;
        for (int i = 1; i < anh.length; i++) {
            if (soDiemDac(anh[i]) > soDiemDac(anh[mau])) {
                mau = i;
            }
        }
        final int dxMau = -(anh[mau].getWidth() / 2);
        final int[] dx = new int[anh.length];
        for (int i = 0; i < anh.length; i++) {
            dx[i] = (i == mau) ? dxMau : dxMau + doLechTot(anh[mau], anh[i]);
        }
        return dx;
    }

    private static int soDiemDac(final BufferedImage im) {
        int n = 0;
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                if (((im.getRGB(x, y) >>> 24) & 0xFF) >= 128) n++;
            }
        }
        return n;
    }

    /**
     * Do lech ngang lam {@code im} chong khit voi {@code mau} nhat. Hai anh da cung
     * chieu cao va cung can day nen chi phai do mot chieu.
     */
    private static int doLechTot(final BufferedImage mau, final BufferedImage im) {
        final int h = mau.getHeight();
        final boolean[][] a = mask(mau), b = mask(im);
        final int wa = mau.getWidth(), wb = im.getWidth();
        int tot = 0;
        long diemTot = -1;
        for (int d = -wb; d <= wa; d++) {
            long diem = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < wb; x++) {
                    final int xa = x + d;
                    if (xa >= 0 && xa < wa && b[y][x] && a[y][xa]) diem++;
                }
            }
            // Hoa thi lay do lech nho hon: giu con thu gan cho cu, khoi bi keo lech han.
            if (diem > diemTot || (diem == diemTot && Math.abs(d) < Math.abs(tot))) {
                diemTot = diem;
                tot = d;
            }
        }
        return tot;
    }

    private static boolean[][] mask(final BufferedImage im) {
        final boolean[][] m = new boolean[im.getHeight()][im.getWidth()];
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                m[y][x] = ((im.getRGB(x, y) >>> 24) & 0xFF) >= 128;
            }
        }
        return m;
    }

    // ================================================================================
    //                    ANH 3 FRAME CHO BANG "TRANG BI THU"
    // ================================================================================

    /**
     * Dai DUNG 3 FRAME xep doc.
     *
     * Bang "Trang bi thu" ben client chay hoat anh bang mot bang frame co dinh
     * (WindowInfoScr / MenuWindows / ShopNew, ba cho giong nhau):
     *     arrFrame = { {0,1,2}, {0,1} }
     *     idFrame  = arrFrame[(numFrame != 3) ? 1 : 0][countFrame]
     * Gui so frame khac 3 thi client CHI ve frame 0 va 1 — con thu gan nhu dung im va
     * cac frame con lai khong bao gio duoc ve. Gui dung 3 thi duoc ca ba tu the.
     *
     * Lay ba tu the huong XUONG (nhin tu truoc) o slot 0, 2, 4.
     */
    private static void ghiAvatar(final File ra, final BufferedImage[] than) throws Exception {
        final int[] lay = {0, 2, 4};
        int w = 0, h = 0;
        for (final int i : lay) {
            w = Math.max(w, than[i].getWidth());
            h = Math.max(h, than[i].getHeight());
        }
        final BufferedImage im = new BufferedImage(w, h * lay.length, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = im.createGraphics();
        for (int i = 0; i < lay.length; i++) {
            final BufferedImage f = than[lay[i]];
            // Can giua ngang, sat day o: bang Trang bi thu ve theo goc duoi.
            g.drawImage(f, (w - f.getWidth()) / 2, (i + 1) * h - f.getHeight(), null);
        }
        g.dispose();
        ImageIO.write(im, "png", ra);
    }

    /**
     * Anh kiem tra: ve dung 21 frame nhu client se ghep, moi huong mot hang.
     *
     * Cot 0..6 la slot Fhorse. Cot 0-3 la luc dung yen, cot 2-4 la luc di chuyen, cot
     * 5-6 khong bao gio duoc ve (lap lai 0,1). Vach do la muc chan nhan vat: chan con
     * thu phai cham vach, phan tho xuong duoi vach chinh la THO_XUONG.
     */
    private static void ghiKiemTra(final File ra, final BufferedImage[] than,
                                   final BufferedImage bong) throws Exception {
        final int oW = 80, oH = 76, le = 22;
        final BufferedImage im = new BufferedImage(SLOT_MOI_HUONG * oW + le, SO_HUONG * oH + 16,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = im.createGraphics();
        g.setColor(new Color(0x28, 0x28, 0x30));
        g.fillRect(0, 0, im.getWidth(), im.getHeight());
        for (int s = 0; s < SLOT_MOI_HUONG; s++) {
            g.setColor(s < ANH_MOI_HUONG ? Color.YELLOW : new Color(0x70, 0x70, 0x70));
            g.drawString("F" + s + (s < ANH_MOI_HUONG ? "" : " (lap)"), le + s * oW + 4, 12);
        }
        for (int h = 0; h < SO_HUONG; h++) {
            final int oy = 16 + h * oH;
            g.setColor(Color.YELLOW);
            g.drawString(TEN_HUONG[h].substring(0, 2), 2, oy + oH / 2);
            for (int s = 0; s < SLOT_MOI_HUONG; s++) {
                final int f = (s < ANH_MOI_HUONG) ? s : s - ANH_MOI_HUONG;
                final BufferedImage b = than[h * ANH_MOI_HUONG + f];
                final int ox = le + s * oW;
                final int chan = oy + oH - 16;                 // muc chan nhan vat
                g.setColor(new Color(0x3A, 0x3A, 0x44));
                g.drawRect(ox, oy, oW - 1, oH - 1);
                g.drawImage(bong, ox + oW / 2 - bong.getWidth() / 2,
                        chan - bong.getHeight() / 2, null);
                g.drawImage(b, ox + oW / 2 - b.getWidth() / 2,
                        chan - (b.getHeight() - THO_XUONG[h]), null);
                g.setColor(new Color(0xC0, 0x40, 0x40));
                g.drawLine(ox + 2, chan, ox + oW - 3, chan);
            }
        }
        g.dispose();
        ra.getParentFile().mkdirs();
        ImageIO.write(im, "png", ra);
    }

    /** Icon 16x16 cua vat pham skin: thu nho tu tu the dung yen huong xuong. */
    private static void ghiIcon(final File ra, final BufferedImage nguon) throws Exception {
        final int cd = 16;
        final double tiLe = Math.min((double) cd / nguon.getWidth(), (double) cd / nguon.getHeight());
        final int w = Math.max(1, (int) Math.round(nguon.getWidth() * tiLe));
        final int h = Math.max(1, (int) Math.round(nguon.getHeight() * tiLe));
        final BufferedImage im = new BufferedImage(cd, cd, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(nguon, (cd - w) / 2, (cd - h) / 2, w, h, null);
        g.dispose();
        ra.getParentFile().mkdirs();
        ImageIO.write(im, "png", ra);
    }
}
