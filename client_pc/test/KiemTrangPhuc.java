import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM TRANG PHỤC 0.9.8 (headless, không máy chủ, đường vẽ bản PC):
 * <ul>
 * <li>atlas res/pk/trang_phuc.png + .bin: đọc được, mọi mảnh của mọi trang phục có đủ 3 hướng, vùng ảnh nằm trong atlas,
 *     hào quang 4 khung, skin / thẻ có dữ liệu; khung độ hiếm res/pk/khung_hiem.png đủ 6 bậc;</li>
 * <li>đuôi gói -102 (0x54, 1, tp, hq, skin, thẻ) đọc đúng, gói cũ không đuôi vẫn đọc; gói -116 kiểu 0 (tủ đồ) đọc đúng;</li>
 * <li>KIỂM VẼ: 5 dòng khởi đầu x 3 dạng (Charmander 4/5/6, Bulbasaur 1/2/3, Squirtle 7/8/9, Eevee 133/134/136, Pichu
 *     172/25/26) x mọi trang phục x 4 hướng x 11 cỡ chuẩn (ChupTrongGame.CO -> hệ số NenPc): mũ ngồi trên đỉnh đầu (đáy mũ
 *     chồng đầu &lt;= 3k px, không xuống tới điểm mặt PMD), đồ cổ giữa đỉnh đầu và tâm thân, áo choàng / cánh bám cổ và
 *     đúng lớp theo hướng (sau thân, trước khi quay lưng), đồ cầm ở tay, không mảnh nào ra xa hộp sprite quá lề; mảnh vẽ
 *     ở đúng chỗ ghi (điểm ảnh thật khác ảnh không trang phục); hào quang dưới chân.</li>
 * </ul>
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemTrangPhuc.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTrangPhuc [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemTrangPhuc {
    static int dat, hong;
    static int soHongIn, soHongKhac;
    static final java.util.TreeMap<String, Integer> LOAI = new java.util.TreeMap<String, Integer>();

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            if (soHongIn++ < 80) {
                System.out.println("HONG: " + ten);
            }
            String loai = ten.replaceAll("#[0-9]+ [a-z_]+ k[0-9]+ h[0-9] tp[0-9]+", "").replaceAll("-?[0-9]+(\\.[0-9]+)?", "N");
            LOAI.put(loai, LOAI.containsKey(loai) ? LOAI.get(loai) + 1 : 1);
            if (ten.indexOf("điểm ảnh") < 0 && soHongKhac++ < 80) {
                System.out.println("HONG*: " + ten);
            }
        }
    }

    static final int[][] DONG = {{4, 5, 6}, {1, 2, 3}, {7, 8, 9}, {133, 134, 136}, {172, 25, 26}};
    static final String[] TEN_DONG = {"Charmander", "Bulbasaur", "Squirtle", "Eevee", "Pichu"};
    static final int[][] CO = {{1920, 1080}, {2340, 1080}, {2400, 1080}, {2520, 1080}, {2560, 1600}, {2048, 1536},
        {1280, 720}, {1920, 1080}, {2560, 1080}, {1920, 1200}, {1024, 768}};
    static final int NEN = 0xFF2C3446;
    static final int CY = 120;   // điểm chân (O/2, CY): khung đánh / ngã thân lệch xuống dưới chân tới ~20 px
    static final int O = 192;   // ô vẽ (px logic), chân ở (O/2, O-16) - đủ rộng cho khung đánh / ngã

    public static void main(String[] a) throws Exception {
        File ra = new File(a.length > 0 ? a[0] : "/tmp/kiem_trang_phuc");
        ra.mkdirs();
        kiemDuLieu();
        kiemGoi();
        kiemVe(ra);
        for (java.util.Map.Entry<String, Integer> e : LOAI.entrySet()) {
            System.out.println("LOAI HONG " + e.getValue() + " x " + e.getKey());
        }
        System.out.println("KiemTrangPhuc: dat " + dat + ", hong " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------ dữ liệu
    static void kiemDuLieu() throws Exception {
        kiem(game.pokemon.TrangPhucVe.nap(), "nạp atlas trang_phuc.png + .bin");
        int W = game.pokemon.TrangPhucVe.rongAtlas(), H = game.pokemon.TrangPhucVe.caoAtlas();
        kiem(W > 0 && H > 0, "atlas có cỡ");
        int soTp = 0;
        for (int so = 1; so < 256; so++) {
            int[] ds = game.pokemon.TrangPhucVe.manhCua(so);
            if (ds == null) {
                continue;
            }
            soTp++;
            kiem(ds.length >= 1 && ds.length <= 4, "trang phục " + so + " có 1..4 mảnh");
            boolean[] oDa = new boolean[8];
            for (int i = 0; i < ds.length; i++) {
                for (int h = 0; h < 3; h++) {
                    game.pokemon.TrangPhucVe.Manh m = game.pokemon.TrangPhucVe.manh(ds[i], h);
                    kiem(m != null, "trang phục " + so + " mảnh " + ds[i] + " hướng " + h);
                    if (m == null) {
                        continue;
                    }
                    kiem(m.x + m.w <= W && m.y + m.h <= H && m.w > 2 && m.h > 2, "mảnh " + ds[i] + " trong atlas");
                    kiem(m.ax >= 0 && m.ax < m.w && m.ay >= 0 && m.ay <= m.h, "mảnh " + ds[i] + " điểm neo trong ảnh");
                    if (h == 0) {
                        kiem(!oDa[m.o], "trang phục " + so + ": mỗi ô một mảnh (ô " + m.o + ")");
                        oDa[m.o] = true;
                    }
                }
            }
        }
        kiem(soTp >= 30, "có >= 30 trang phục (" + soTp + ")");
        int soHq = 0;
        for (int so = 1; so < 256; so++) {
            int[][] k = game.pokemon.TrangPhucVe.khungHaoQuang(so);
            if (k == null) {
                continue;
            }
            soHq++;
            kiem(k.length == 4, "hào quang " + so + " 4 khung");
            for (int f = 0; f < k.length; f++) {
                kiem(k[f][0] + k[f][2] <= W && k[f][1] + k[f][3] <= H, "hào quang " + so + " khung " + f + " trong atlas");
            }
        }
        kiem(soHq >= 10, "có >= 10 hào quang (" + soHq + ")");
        int soSkin = 0, soThe = 0;
        for (int so = 1; so < 256; so++) {
            if (game.pokemon.TrangPhucVe.skin(so) != null) {
                soSkin++;
            }
            int[] t = game.pokemon.TrangPhucVe.the(so);
            if (t != null) {
                soThe++;
                kiem(game.pokemon.TrangPhucVe.manh(t[0], 0) != null, "thẻ " + so + " có mảnh");
            }
        }
        kiem(soSkin >= 6 && soThe >= 6, "skin thú (" + soSkin + ") / thẻ bạn đồng hành (" + soThe + ")");
        byte[] k = game.pokemon.PokeBody.docTaiNguyen("/pk/khung_hiem.png");
        kiem(k != null, "có khung_hiem.png");
        BufferedImage kh = ImageIO.read(new java.io.ByteArrayInputStream(k));
        kiem(kh.getWidth() == 40 && kh.getHeight() == 120, "khung độ hiếm 40x120 (6 bậc x 2 khung)");
        int[] map = {0, 3, 2, 1, 6, 4, 5};
        int[] mong = {0, 1, 2, 3, 4, 5, 5};
        for (int i = 0; i < map.length; i++) {
            kiem(game.pokemon.IconVatPham.bacKhung(map[i]) == mong[i], "colorName " + map[i] + " -> bậc khung " + mong[i]);
        }
    }

    // ------------------------------------------------------------------ gói
    static void kiemGoi() throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(1);
        o.writeByte(0);      // không ô nào
        o.writeByte(255);
        o.writeByte(0);
        o.writeByte(0);
        o.writeByte(0);
        o.writeByte(0);
        byte[] cu = bo.toByteArray();
        java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(cu));
        game.pokemon.NgoaiHinhCua n = game.pokemon.NgoaiHinhCua.doc(r);
        n.docDuoi(r);
        kiem(n.tpSo == 0 && n.haoQuang == 0, "-102 cũ (không đuôi): không trang phục");
        o.writeByte(0x54);
        o.writeByte(1);
        o.writeByte(11);
        o.writeByte(2);
        o.writeByte(4);
        o.writeByte(3);
        r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(bo.toByteArray()));
        n = game.pokemon.NgoaiHinhCua.doc(r);
        n.docDuoi(r);
        kiem(n.tpSo == 11 && n.haoQuang == 2 && n.skinThu == 4 && n.theBan == 3, "-102 đuôi 0.9.8 đọc đúng");
        kiem(game.pokemon.TrangPhucVe.tpThu(n) == 104, "skin thú đổi màu -> mã PokeBody 104");
        n.skinThu = 1;
        kiem(game.pokemon.TrangPhucVe.tpThu(n) == 0, "Yên cương cơ bản không đổi màu thú");
        game.pokemon.ManTuDo.napThu(goiTuDo());
        kiem(game.pokemon.ManTuDo.o.length == 4, "-116 tủ đồ: 4 ô");
        kiem(game.pokemon.ManTuDo.MON.size() == 3, "-116 tủ đồ: 3 món");
        kiem("Trang phục".equals(game.pokemon.ManTuDo.o[0].ten) && game.pokemon.ManTuDo.o[0].pnHieuLuc == 15, "ô 0 ngân sách 1,5%");
        game.pokemon.ManTuDo.Mon m = (game.pokemon.ManTuDo.Mon) game.pokemon.ManTuDo.MON.elementAt(2);
        kiem(m.hanhDong == game.pokemon.ManTuDo.HD_MUA, "món cửa hàng: hành động mua");
        // thử đồ: bản sao, bản thật không đổi
        game.pokemon.NgoaiHinhCua that = new game.pokemon.NgoaiHinhCua();
        game.pokemon.ManTuDo.thu((game.pokemon.ManTuDo.Mon) game.pokemon.ManTuDo.MON.elementAt(0));
        game.pokemon.NgoaiHinhCua thu = game.pokemon.ManTuDo.ngoaiHinhThu(that);
        kiem(thu != that && thu.tpSo == 1 && that.tpSo == 0, "thử đồ: bản sao mặc, bản thật giữ nguyên");
        game.pokemon.ManTuDo.boThu();
        kiem(game.pokemon.ManTuDo.ngoaiHinhThu(that) == that, "bỏ thử: về bản thật");
    }

    /** Gói -116 kiểu 0 giả (sau byte kiểu) - dùng chung cho ChupTrongGame. */
    public static byte[] goiTuDo() throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        String[][] oo = {{"trang_phuc", "Trang phục", "15"}, {"hao_quang", "Hào quang", "10"}, {"skin_thu", "Skin thú cưỡi", "5"},
            {"the_dong_hanh", "Thẻ bạn đồng hành", "10"}};
        o.writeByte(oo.length);
        for (int i = 0; i < oo.length; i++) {
            o.writeUTF(oo[i][0]);
            o.writeUTF(oo[i][1]);
            o.writeShort(Integer.parseInt(oo[i][2]));
            o.writeShort(Integer.parseInt(oo[i][2]));
            o.writeByte(2);
            o.writeUTF("hp");
            o.writeUTF("HP");
            o.writeUTF("tan_cong");
            o.writeUTF("Tấn công");
            o.writeByte(0);
            o.writeUTF(i == 0 ? "tp_tan_thu" : "");
            o.writeInt(i == 0 ? 3600 : 0);
        }
        o.writeShort(35);
        o.writeShort(50);
        o.writeShort(10);
        Object[][] mon = {
            {"tp_tan_thu", 0, 1, "Áo huấn luyện viên tân thủ", 0, 15628, 3, "Miễn phí", "", 0, "Mũ lưỡi trai đỏ và khăn trắng."},
            {"tp_halloween", 0, 11, "Trang phục Halloween", 3, 15638, 0, "Sự kiện Halloween", "Hạ quái 120/500", 0, "Mũ phù thuỷ, áo choàng tím."},
            {"tp_hiep_si", 0, 23, "Trang phục Hiệp sĩ", 3, 15650, 0, "Cửa hàng: 250 lượng", "", 3, "Mũ giáp lông đỏ và áo choàng."},
        };
        o.writeShort(mon.length);
        for (int i = 0; i < mon.length; i++) {
            Object[] m = mon[i];
            o.writeUTF((String) m[0]);
            o.writeByte((Integer) m[1]);
            o.writeByte((Integer) m[2]);
            o.writeUTF((String) m[3]);
            o.writeByte((Integer) m[4]);
            o.writeShort((Integer) m[5]);
            o.writeByte((Integer) m[6]);
            o.writeUTF((String) m[7]);
            o.writeUTF((String) m[8]);
            o.writeByte((Integer) m[9]);
            o.writeUTF((String) m[10]);
        }
        return bo.toByteArray();
    }

    // ------------------------------------------------------------------ vẽ

    static int[] NEO_TAM = new int[8];

    /** Vẽ dex + ngoại hình n vào ảnh ô O x O (hệ số k), chân ở (O/2, O-16). Trả ảnh. */
    static BufferedImage ve(int dex, game.pokemon.NgoaiHinhCua n, int dir, int k) {
        BufferedImage b = new BufferedImage(O * k, O * k, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = b.createGraphics();
        g2.setColor(new java.awt.Color(NEN, true));
        g2.fillRect(0, 0, b.getWidth(), b.getHeight());
        g2.scale(k, k);
        Graphics g = new Graphics(g2, O, O, k);
        int x = O / 2, y = CY;
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veSau(g, n, dex, false, x, y, y, dir, game.pokemon.PokeDuLieu.DUNG, 0);
        }
        game.pokemon.PokeBody.paint(g, dex, false, x, y, dir, game.pokemon.PokeDuLieu.DUNG, 0, 100, 0, 0);
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veTruoc(g, n, dex, false, x, y, dir, game.pokemon.PokeDuLieu.DUNG, 0);
        }
        g2.dispose();
        return b;
    }

    static int[] hop(BufferedImage b, int k) {
        int x0 = 9999, y0 = 9999, x1 = -1, y1 = -1;
        for (int y = 0; y < b.getHeight(); y++) {
            for (int x = 0; x < b.getWidth(); x++) {
                if (b.getRGB(x, y) != NEN) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        return x1 < 0 ? null : new int[]{x0 / k, y0 / k, x1 / k, y1 / k};
    }

    static final int[] TP = new int[game.pokemon.NeoTrangPhuc.SO];
    static final String[] TEN_DT = {"dung", "chay", "danh_gan", "danh_xa", "trung_don", "guc_nga"};
    /** Số đo lệch (cho báo cáo): [dòng][hướng] tổng |dx| , max |dx| , n. */
    static final double[][][] LECH = new double[5][4][3];

    /** Danh sách tick đại diện mỗi khung (khung ngắn hơn một nhịp game không bao giờ hiện: bỏ). */
    static int[] tickKhung(int dex, int dt) {
        int[] tt = game.pokemon.PokeBody.thongTinDongTac(dex, false, dt);
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

    static void kiemVe(File ra) throws Exception {
        game.pokemon.PhuKien.nap();
        int soTp = 0;
        int[] dsTp = new int[256];
        for (int so = 1; so < 256; so++) {
            if (game.pokemon.TrangPhucVe.manhCua(so) != null) {
                dsTp[soTp++] = so;
            }
        }
        // ---- 1. hình học trên MỌI khung của mọi động tác (px gốc; mọi cỡ vẽ cùng toạ độ logic)
        int soKhung = 0, soVe = 0, soBo = 0;
        for (int d = 0; d < DONG.length; d++) {
            for (int st = 0; st < 3; st++) {
                int dex = DONG[d][st];
                kiem(game.pokemon.PokeBody.coSprite(dex, false), "có sprite #" + dex);
                for (int dt = 0; dt < 6; dt++) {
                    int[] ticks = tickKhung(dex, dt);
                    for (int fi = 0; fi < ticks.length; fi++) {
                        int tick = ticks[fi];
                        for (int dir = 0; dir < 4; dir++) {
                            BufferedImage tron = ve(dex, null, dir, 1, dt, tick);
                            int[] bb = hop(tron, 1);
                            if (!game.pokemon.PokeBody.neoTrangPhuc(dex, false, dir, dt, tick, 100, TP)) {
                                kiem(false, "#" + dex + " " + TEN_DT[dt] + " có neo trang phục");
                                continue;
                            }
                            soKhung++;
                            int[] neo = TP.clone();
                            String ten0 = "#" + dex + " " + TEN_DT[dt] + " k" + fi + " h" + dir;
                            kiem(neo[game.pokemon.NeoTrangPhuc.DAU_TRAI] <= neo[game.pokemon.NeoTrangPhuc.DAU_X]
                                    && neo[game.pokemon.NeoTrangPhuc.DAU_X] <= neo[game.pokemon.NeoTrangPhuc.DAU_PHAI], ten0 + ": giữa sọ trong đầu");
                            kiem(neo[game.pokemon.NeoTrangPhuc.CO_Y] > neo[game.pokemon.NeoTrangPhuc.DINH_Y], ten0 + ": cổ dưới đỉnh sọ");
                            for (int t = 0; t < soTp; t++) {
                                game.pokemon.NgoaiHinhCua n = new game.pokemon.NgoaiHinhCua();
                                n.tpSo = dsTp[t];
                                game.pokemon.TrangPhucVe.soHopVe = 0;
                                game.pokemon.TrangPhucVe.ghiHop = true;
                                BufferedImage anh = ve(dex, n, dir, 1, dt, tick);
                                game.pokemon.TrangPhucVe.ghiHop = false;
                                int nVe = game.pokemon.TrangPhucVe.soHopVe;
                                int[] ds = game.pokemon.TrangPhucVe.manhCua(dsTp[t]);
                                soVe += nVe;
                                soBo += ds.length - nVe;
                                String ten = ten0 + " tp" + dsTp[t];
                                kiem(nVe >= 1 || dt >= 2, ten + ": vẽ ít nhất 1 mảnh (đứng / chạy; khung đánh / ngã có thể ẩn mảnh không vừa)");
                                for (int i = 0; i < nVe; i++) {
                                    int[] h = new int[7];
                                    System.arraycopy(game.pokemon.TrangPhucVe.hopVe, i * 7, h, 0, 7);
                                    kiemManh(ten, h, neo, bb, dir, d);
                                    if (h[5] == 1) {
                                        kiem(khacAnh(anh, tron, h, 1), ten + " mảnh " + h[6] + ": thấy được đúng chỗ");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("hình học: " + soKhung + " khung (5 dòng x 3 dạng x 6 động tác x mọi khung x 4 hướng), mảnh vẽ " + soVe
                + ", bỏ (mũ to hơn đầu / loài cấm ô) " + soBo);
        for (int d = 0; d < 5; d++) {
            for (int dir = 0; dir < 4; dir++) {
                double[] v = LECH[d][dir];
                System.out.printf("lech sau: %-10s huong %d: n=%d |dx| tb %.2f max %.1f%n", TEN_DONG[d], dir, (int) v[2],
                        v[0] / Math.max(1, v[2]), v[1]);
            }
        }
        // ---- 2. 11 cỡ chuẩn: vẽ thật ở hệ số NenPc của cỡ đó (đứng + chạy, mọi khung) - mảnh lớp trước thấy đúng chỗ, trong khung
        java.util.Set<Integer> heSoDa = new java.util.TreeSet<Integer>();
        int soPx = 0;
        for (int c = 0; c < CO.length; c++) {
            NenPc.tinhKichThuoc(CO[c][0], CO[c][1]);
            int k = Math.max(1, NenPc.heSo);
            heSoDa.add(k);
            for (int d = 0; d < DONG.length; d++) {
                for (int st = 0; st < 3; st++) {
                    int dex = DONG[d][st];
                    for (int dt = 0; dt < 2; dt++) {
                        int[] ticks = tickKhung(dex, dt);
                        for (int fi = 0; fi < ticks.length; fi += (c % 3 == 0 ? 1 : 2)) {
                            for (int dir = 0; dir < 4; dir++) {
                                BufferedImage tron = ve(dex, null, dir, k, dt, ticks[fi]);
                                for (int t = 0; t < soTp; t += 3) {
                                    game.pokemon.NgoaiHinhCua n = new game.pokemon.NgoaiHinhCua();
                                    n.tpSo = dsTp[t];
                                    game.pokemon.TrangPhucVe.soHopVe = 0;
                                    game.pokemon.TrangPhucVe.ghiHop = true;
                                    BufferedImage anh = ve(dex, n, dir, k, dt, ticks[fi]);
                                    game.pokemon.TrangPhucVe.ghiHop = false;
                                    for (int i = 0; i < game.pokemon.TrangPhucVe.soHopVe; i++) {
                                        int[] h = new int[7];
                                        System.arraycopy(game.pokemon.TrangPhucVe.hopVe, i * 7, h, 0, 7);
                                        String ten = TEN_CO_TEN(c) + " #" + dex + " " + TEN_DT[dt] + " h" + dir + " tp" + dsTp[t];
                                        kiem(h[0] >= 0 && h[1] >= 0 && (h[0] + h[2]) * k <= anh.getWidth() && (h[1] + h[3]) * k <= anh.getHeight(),
                                                ten + ": mảnh trong khung vẽ (không bị cắt)");
                                        if (h[5] == 1) {
                                            soPx++;
                                            kiem(khacAnh(anh, tron, h, k), ten + " mảnh " + h[6] + ": điểm ảnh đúng chỗ ở hệ số " + k);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("11 cỡ: so điểm ảnh " + soPx + ", hệ số " + heSoDa);
        veBoSuuTap(ra, dsTp, soTp);
    }

    /** Vẽ dex + ngoại hình n (động tác dt, tick) vào ô O x O hệ số k, chân ở (O/2, O-16). */
    static BufferedImage ve(int dex, game.pokemon.NgoaiHinhCua n, int dir, int k, int dt, int tick) {
        BufferedImage b = new BufferedImage(O * k, O * k, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = b.createGraphics();
        g2.setColor(new java.awt.Color(NEN, true));
        g2.fillRect(0, 0, b.getWidth(), b.getHeight());
        g2.scale(k, k);
        Graphics g = new Graphics(g2, O, O, k);
        int x = O / 2, y = CY;
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veSau(g, n, dex, false, x, y, y, dir, dt, tick);
        }
        game.pokemon.PokeBody.paint(g, dex, false, x, y, dir, dt, tick, 100, 0, 0);
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veTruoc(g, n, dex, false, x, y, dir, dt, tick);
        }
        g2.dispose();
        return b;
    }

    static void veBoSuuTap(File ra, int[] dsTp, int soTp) throws Exception {
        // a) mỗi dòng: 3 dạng x 4 hướng (hàng) x mọi trang phục (cột), đứng yên, KHÔNG hào quang, không cắt
        int s = 96, z = 2;
        for (int d = 0; d < DONG.length; d++) {
            BufferedImage tam = new BufferedImage(soTp * s * z, 12 * s * z, BufferedImage.TYPE_INT_RGB);
            Graphics2D gt = tam.createGraphics();
            gt.setColor(new java.awt.Color(NEN));
            gt.fillRect(0, 0, tam.getWidth(), tam.getHeight());
            for (int st = 0; st < 3; st++) {
                for (int dir = 0; dir < 4; dir++) {
                    for (int t = 0; t < soTp; t++) {
                        game.pokemon.NgoaiHinhCua n = new game.pokemon.NgoaiHinhCua();
                        n.tpSo = dsTp[t];
                        BufferedImage o = ve(DONG[d][st], n, dir, 1, 0, 0);
                        gt.drawImage(o.getSubimage((O - s) / 2, CY - s + 24, s, s), t * s * z, (st * 4 + dir) * s * z, s * z, s * z, null);
                    }
                }
            }
            gt.dispose();
            ImageIO.write(tam, "png", new File(ra, "render_" + TEN_DONG[d].toLowerCase() + ".png"));
        }
        // b) dải từng khung: mỗi dòng, mỗi dạng, động tác chạy + đánh gần + đánh xa x 4 hướng, 2 trang phục
        int[] hai = {1, 11};
        for (int d = 0; d < DONG.length; d++) {
            java.util.List<BufferedImage> hang = new java.util.ArrayList<BufferedImage>();
            int cotMax = 1;
            for (int st = 0; st < 3; st++) {
                for (int dt = 1; dt <= 3; dt++) {
                    int[] ticks = tickKhung(DONG[d][st], dt);
                    for (int tp : hai) {
                        for (int dir = 0; dir < 4; dir++) {
                            BufferedImage r = new BufferedImage(Math.max(1, ticks.length) * s * z, s * z, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g = r.createGraphics();
                            for (int fi = 0; fi < ticks.length; fi++) {
                                game.pokemon.NgoaiHinhCua n = new game.pokemon.NgoaiHinhCua();
                                n.tpSo = tp;
                                BufferedImage o = ve(DONG[d][st], n, dir, 1, dt, ticks[fi]);
                                g.drawImage(o.getSubimage((O - s) / 2, CY - s + 24, s, s), fi * s * z, 0, s * z, s * z, null);
                            }
                            g.dispose();
                            cotMax = Math.max(cotMax, ticks.length);
                            hang.add(r);
                        }
                    }
                }
            }
            BufferedImage tam = new BufferedImage(cotMax * s * z, hang.size() * s * z, BufferedImage.TYPE_INT_RGB);
            Graphics2D gt = tam.createGraphics();
            gt.setColor(new java.awt.Color(NEN));
            gt.fillRect(0, 0, tam.getWidth(), tam.getHeight());
            for (int i = 0; i < hang.size(); i++) {
                gt.drawImage(hang.get(i), 0, i * s * z, null);
            }
            gt.dispose();
            ImageIO.write(tam, "png", new File(ra, "render_khung_" + TEN_DONG[d].toLowerCase() + ".png"));
        }
        // c) hào quang riêng (mỗi dòng dạng cuối, 13 hào quang) + skin thú / thẻ bạn đồng hành
        BufferedImage hq = new BufferedImage(13 * s * z, 5 * s * z, BufferedImage.TYPE_INT_RGB);
        Graphics2D gh = hq.createGraphics();
        for (int d = 0; d < DONG.length; d++) {
            for (int a = 1; a <= 13; a++) {
                game.pokemon.NgoaiHinhCua n = new game.pokemon.NgoaiHinhCua();
                n.haoQuang = a;
                BufferedImage o = ve(DONG[d][2], n, 0, 1, 0, 0);
                gh.drawImage(o.getSubimage((O - s) / 2, CY - s + 24, s, s), (a - 1) * s * z, d * s * z, s * z, s * z, null);
            }
        }
        gh.dispose();
        ImageIO.write(hq, "png", new File(ra, "render_hao_quang.png"));
        BufferedImage sk = new BufferedImage(8 * s * z, 5 * s * z, BufferedImage.TYPE_INT_RGB);
        Graphics2D gs = sk.createGraphics();
        for (int d = 0; d < DONG.length; d++) {
            for (int s2 = 1; s2 <= 8; s2++) {
                BufferedImage b = new BufferedImage(O, O, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = b.createGraphics();
                g2.setColor(new java.awt.Color(NEN, true));
                g2.fillRect(0, 0, O, O);
                Graphics g = new Graphics(g2, O, O, 1);
                game.pokemon.PokeBody.paint(g, DONG[d][2], false, O / 2 - 8, CY, 0, game.pokemon.PokeDuLieu.DUNG, 0, 100, 0, 100 + s2);
                game.pokemon.PokeBody.paint(g, DONG[d][0], false, O / 2 + 22, CY, 0, game.pokemon.PokeDuLieu.DUNG, 0, 60);
                game.pokemon.TrangPhucVe.veThe(g, s2, DONG[d][0], false, O / 2 + 22, CY, 0, game.pokemon.PokeDuLieu.DUNG, 0, 60);
                g2.dispose();
                gs.drawImage(b.getSubimage((O - s) / 2, CY - s + 24, s, s), (s2 - 1) * s * z, d * s * z, s * z, s * z, null);
            }
        }
        gs.dispose();
        ImageIO.write(sk, "png", new File(ra, "render_skin_thu.png"));
    }

    static String TEN_CO_TEN(int c) {
        String[] t = {"dt16x9", "dt19.5x9", "dt20x9", "dt21x9", "tablet16x10", "tablet4x3", "pc720", "pc1080", "pc21x9", "pc16x10", "pc4x3"};
        return t[c];
    }

    /**
     * Kiểm CHẶT một mảnh: h = {X, Y, W, H (hộp điểm đặc), o, lop, id} (px gốc, chân ở O/2, O-16); neo = NeoTrangPhuc.
     * Dung sai 1 px gốc.
     */
    static void kiemManh(String ten, int[] h, int[] neo, int[] bb, int dir, int dong) {
        int x = O / 2, y = CY;
        int X = h[0], Y = h[1], W = h[2], H = h[3], o = h[4], lop = h[5], id = h[6];
        double giua = X + W / 2.0;
        String t = ten + " mảnh " + id + " (ô " + o + ")";
        int dinh = y + neo[game.pokemon.NeoTrangPhuc.DINH_Y];
        int matTren = neo[game.pokemon.NeoTrangPhuc.MAT_TREN];
        kiem(X >= 0 && Y >= 0 && X + W <= O && Y + H <= O, t + ": trong khung vẽ (không bị cắt) " + X + "," + Y + " " + W + "x" + H);
        switch (o) {
            case 0: {
                double c = x + neo[game.pokemon.NeoTrangPhuc.DAU_X] + 0.5;
                boolean trangTri = id == 21 || id == 22;   // hoa cài / nơ tim: cố ý lệch một bên (dx)
                if (!trangTri) {
                    double lech = Math.abs(giua - c);
                    LECH[dong][dir][0] += lech;
                    LECH[dong][dir][1] = Math.max(LECH[dong][dir][1], lech);
                    LECH[dong][dir][2]++;
                    kiem(lech <= 1.0, t + ": giữa mũ " + giua + " lệch giữa sọ " + c + " <= 1 px");
                    int rongDau = neo[game.pokemon.NeoTrangPhuc.DAU_PHAI] - neo[game.pokemon.NeoTrangPhuc.DAU_TRAI] + 1;
                    kiem(W <= rongDau + 2, t + ": rộng " + W + " <= đầu " + rongDau + " + 2");
                }
                int day = Y + H - 1;
                if (id == 19 && matTren != game.pokemon.NeoTrangPhuc.KHONG) {
                    kiem(day <= y + matTren - 1 && day >= y + matTren - 4, t + ": kính trán ngay trên mắt (đáy " + day + ", mắt " + (y + matTren) + ")");
                } else {
                    kiem(day == dinh, t + ": đáy mũ " + day + " chạm đỉnh sọ " + dinh + " (chồng 1 hàng, không hở / lún)");
                }
                if (matTren != game.pokemon.NeoTrangPhuc.KHONG) {
                    kiem(day < y + matTren, t + ": không che mắt (đáy " + day + " < mắt " + (y + matTren) + ")");
                }
                kiem(lop == 1, t + ": mũ lớp trước");
                break;
            }
            case 1: {
                double c = x + neo[game.pokemon.NeoTrangPhuc.CO_X] + 0.5;
                kiem(Math.abs(giua - c) <= 1.0, t + ": khăn giữa cổ (lệch " + (giua - c) + ")");
                kiem(Y == y + neo[game.pokemon.NeoTrangPhuc.CO_Y] - 1, t + ": khăn đỉnh ở cổ");
                kiem(lop == (dir == 0 ? 1 : 0), t + ": lớp khăn theo hướng");
                if (matTren != game.pokemon.NeoTrangPhuc.KHONG) {
                    kiem(Y > y + matTren, t + ": khăn dưới mắt");
                }
                break;
            }
            case 4: {
                double c = x + neo[dir == 1 ? game.pokemon.NeoTrangPhuc.CO_X : game.pokemon.NeoTrangPhuc.LUNG_X] + 0.5;
                kiem(Math.abs(giua - c) <= 1.0, t + ": đồ lưng giữa vai (lệch " + (giua - c) + ")");
                kiem(Y == y + neo[game.pokemon.NeoTrangPhuc.LUNG_Y] - 1, t + ": đồ lưng đỉnh ở vai");
                kiem(Y < y + neo[game.pokemon.NeoTrangPhuc.EO_Y], t + ": đồ lưng trên eo (không ở chân)");
                if (W * 1 > 0 && h[2] > 0) {
                    // không thò xuống dưới mặt đất (điểm chân) trừ khi đã ở cỡ nhỏ nhất (mảnh gốc dài hơn thân)
                    kiem(Y + H - 1 <= y + neo[game.pokemon.NeoTrangPhuc.DAY_Y] - 1, t + ": đồ lưng không thò xuống dưới thân (đáy " + (Y + H - 1) + ", đáy thân " + (y + neo[game.pokemon.NeoTrangPhuc.DAY_Y]) + ")");
                }
                kiem(lop == (dir == 1 ? 1 : 0), t + ": đồ lưng sau thân (nhìn xuống / ngang), trước khi quay lưng");
                break;
            }
            case 3: {
                kiem(Math.abs(giua - (X + W / 2.0)) < 0.01, t);
                break;
            }
            default:
                break;
        }
    }

    /** Có điểm ảnh khác ảnh không trang phục trong hộp mảnh (mảnh thật sự được vẽ chỗ đã ghi). */
    static boolean khacAnh(BufferedImage a, BufferedImage b, int[] h, int k) {
        for (int y = h[1] * k; y < (h[1] + h[3]) * k; y++) {
            for (int x = h[0] * k; x < (h[0] + h[2]) * k; x++) {
                if (x >= 0 && y >= 0 && x < a.getWidth() && y < a.getHeight() && a.getRGB(x, y) != b.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
