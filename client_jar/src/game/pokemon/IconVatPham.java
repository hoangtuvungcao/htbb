package game.pokemon;

import game.model.ImageIcon;
import game.render.Canvas;
import game.render.screen.GameData;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;

/**
 * ICON VẬT PHẨM THẬT (0.9.5) cho hành trang, cửa hàng, giao dịch, thưởng nhiệm vụ, ô trang bị: một đường duy nhất
 * {@link #ve} thay các ô "giọt nước / túi / Poké Ball" chung.
 *
 * Nguồn icon: máy chủ gửi ảnh theo id (lệnh -51; id 14000+n = icon PMD map/icon_pokemon, 5500+ thuốc, 6500+ ngọc,
 * 7500+ thú, 8000+ vật phẩm nhiệm vụ - server IconPokemon.doiAnh). Client:
 *  - nhớ trong RAM (GameData), nhớ trên ĐĨA (bản PC / APK: kho RMS "ic&lt;id&gt;") - lần sau mở game không phải chờ máy chủ;
 *  - đọc đĩa + giải mã PNG trên LUỒNG NỀN (không giật khung), chưa có thì mới hỏi máy chủ;
 *  - đang tải: ô sáng nhấp nháy nhẹ (không còn Poké Ball quay); thiếu thật (máy chủ không có ảnh): biểu tượng loại.
 */
public final class IconVatPham {
    private IconVatPham() {
    }

    // ------------------------------------------------------------------ luồng nền: đĩa -> giải mã -> RAM, không có thì hỏi máy chủ
    private static final Vector HANG = new Vector();
    private static Thread luong;
    /** kho đĩa bật (PC / APK: chữ thật có - máy J2ME bộ nhớ RMS nhỏ thì không) */
    public static boolean coDia() {
        return ChuUI.bo != null;
    }

    /** GameData.getImgIcon tạo ô mới: nạp nền. */
    public static void tai(ImageIcon ic) {
        if (!coDia()) {
            game.networklogic.GameService.gI().doGetImgIcon(ic.id);
            return;
        }
        synchronized (HANG) {
            HANG.addElement(ic);
            HANG.notifyAll();
        }
        if (luong == null) {
            luong = new Thread(new Runnable() {
                public void run() {
                    vong();
                }
            });
            try {
                luong.setPriority(Thread.MIN_PRIORITY + 1);
            } catch (Throwable e) {
                // bỏ qua
            }
            luong.start();
        }
    }

    static void vong() {
        while (true) {
            ImageIcon ic;
            synchronized (HANG) {
                while (HANG.isEmpty()) {
                    try {
                        HANG.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                ic = (ImageIcon) HANG.elementAt(0);
                HANG.removeElementAt(0);
            }
            byte[] b = doc(ic.id);
            Image img = null;
            if (b != null) {
                try {
                    img = Image.createImage(b, 0, b.length);
                } catch (Throwable e) {
                    img = null;
                }
            }
            if (img != null && ic.isLoad) {
                ic.img = img;
                ic.isLoad = false;
            } else if (ic.isLoad) {
                try {
                    game.networklogic.GameService.gI().doGetImgIcon(ic.id);
                } catch (Throwable e) {
                    // chưa nối máy chủ
                }
            }
        }
    }

    /** Máy chủ gửi ảnh (lệnh -51, luồng mạng): ghi đĩa. */
    public static void luu(short id, byte[] b) {
        if (!coDia() || b == null || b.length == 0 || b.length > 60000) {
            return;
        }
        try {
            RecordStore r = RecordStore.openRecordStore("ic" + id, true);
            if (r.getNumRecords() > 0) {
                r.setRecord(1, b, 0, b.length);
            } else {
                r.addRecord(b, 0, b.length);
            }
            r.closeRecordStore();
        } catch (Throwable e) {
            // đầy bộ nhớ: bỏ qua, lần sau hỏi máy chủ
        }
    }

    static byte[] doc(short id) {
        try {
            RecordStore r = RecordStore.openRecordStore("ic" + id, false);
            byte[] b = r.getRecord(1);
            r.closeRecordStore();
            return b;
        } catch (Throwable e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ vẽ
    /** Loại dự phòng (khi thiếu ảnh thật): BieuTuong.* */
    public static final int DP_THUOC = BieuTuong.THUOC, DP_NGOC = BieuTuong.DA, DP_DO = BieuTuong.QUA, DP_NV = BieuTuong.NHIEM_VU;

    private static Image goi;
    private static boolean daNapGoi;

    /** Tấm icon PMD của client (nạp một lần). */
    static Image goi() {
        if (!daNapGoi) {
            daNapGoi = true;
            try {
                goi = Image.createImage("/pk_icon.png");
            } catch (Throwable e) {
                goi = null;
            }
        }
        return goi;
    }

    /** Vẽ ô o của gói, phóng nguyên/nửa lần cho vừa ~72 % ô s (điểm ảnh sắc nét). */
    static boolean veGoi(Graphics g, int o, int cx, int cy, int s) {
        Image a = goi();
        if (a == null || o >= IconBang.SO) {
            return false;
        }
        int sx = (o % IconBang.COT) * IconBang.O, sy = (o / IconBang.COT) * IconBang.O;
        float z = Math.max(1f, ((int) (s * 0.74f / IconBang.O * 2 + 0.5f)) / 2f);   // CLDC: không có Math.round
        if (z > 1.01f && PhongVe.co()) {
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            int kx = g.getClipX(), ky = g.getClipY(), kw = g.getClipWidth(), kh = g.getClipHeight();
            PhongVe.bo.bat(g, z);
            g.drawRegion(a, sx, sy, IconBang.O, IconBang.O, 0, (int) ((cx + tx) / z), (int) ((cy + ty) / z), Graphics.HCENTER | Graphics.VCENTER);
            PhongVe.bo.tat(g);
            g.translate(tx, ty);
            g.setClip(kx, ky, kw, kh);
        } else {
            g.drawRegion(a, sx, sy, IconBang.O, IconBang.O, 0, cx, cy, Graphics.HCENTER | Graphics.VCENTER);
        }
        return true;
    }

    /** Cửa sổ cũ (GameData.paintIcon, cỡ gốc 16 px): có trong gói thì vẽ, true. */
    public static boolean veGoiId(Graphics g, short id, int cx, int cy) {
        int o = IconBang.o(id);
        return o >= 0 && veGoi(g, o, cx, cy, 16);
    }

    /** Sau bao lâu không có ảnh thì coi là thiếu (vẽ biểu tượng loại). */
    static final long CHO_THIEU = 6000;

    /**
     * Vẽ icon id canh giữa (cx, cy) vừa ô cạnh s (phóng nguyên lần nếu ô to); duPhong = BieuTuong khi thiếu ảnh.
     * Trả true nếu đã vẽ ảnh thật.
     */
    public static boolean ve(Graphics g, short id, int cx, int cy, int s, int duPhong) {
        // 0.9.5: icon PMD có trong gói client (res/pk_icon.png) -> vẽ ngay, không phụ thuộc máy chủ
        int o = IconBang.o(id);
        if (o >= 0 && veGoi(g, o, cx, cy, s)) {
            return true;
        }
        ImageIcon ic = GameData.getImgIcon(id);
        if (ic != null && !ic.isLoad && ic.img != null) {
            float z = s >= 26 ? Math.min(3f, s / 22f) : 1f;
            Bang.veAnhIcon(g, id, cx, cy, z);
            return true;
        }
        // chưa có / thiếu ảnh thật: biểu tượng loại (không bao giờ là ô trống)
        int k = s * 3 / 5;
        BieuTuong.ve(g, duPhong, cx - k / 2, cy - k / 2, k, ChuDe.CHU_NHAT, ChuDe.NEN_PHU);
        return false;
    }

    // ------------------------------------------------------------------ 0.9.8 khung độ hiếm PNG (res/pk/khung_hiem.png)

    /** Bậc khung: 0 Thường, 1 Hiếm, 2 Quý, 3 Sử thi, 4 Huyền thoại, 5 Trang trí. */
    public static final int SO_KHUNG = 6;
    private static boolean khungNap;
    private static Image khungAnh;
    private static final Image[] KHUNG = new Image[SO_KHUNG * 2 * 4];

    /** colorName của đồ -> bậc khung (-1 = không khung): 0 Thường, 3 Hiếm, 2 Quý, 1 Sử thi, 6 Huyền thoại, 4/5 trang trí. */
    public static int bacKhung(int colorName) {
        switch (colorName) {
            case 0:
                return 0;
            case 3:
                return 1;
            case 2:
                return 2;
            case 1:
                return 3;
            case 6:
                return 4;
            case 4:
            case 5:
                return 5;
            default:
                return -1;
        }
    }

    static Image anhKhung(int bac, int f, int k) {
        if (!khungNap) {
            khungNap = true;
            try {
                byte[] b = PokeBody.docTaiNguyen(PokeBody.THU_MUC + "khung_hiem.png");
                khungAnh = b == null ? null : Image.createImage(b, 0, b.length);
            } catch (Throwable e) {
                khungAnh = null;
            }
        }
        if (khungAnh == null || bac < 0 || bac >= SO_KHUNG || khungAnh.getHeight() < (bac + 1) * 20) {
            return null;
        }
        int i = (bac * 2 + f) * 4 + k - 1;
        if (KHUNG[i] != null) {
            return KHUNG[i];
        }
        int[] p = new int[400];
        khungAnh.getRGB(p, 0, 20, f * 20, bac * 20, 20, 20);
        int W = 20 * k;
        int[] q = new int[W * W];
        for (int y = 0; y < W; y++) {
            for (int x = 0; x < W; x++) {
                q[y * W + x] = p[(y / k) * 20 + x / k];
            }
        }
        try {
            KHUNG[i] = Image.createRGBImage(q, W, W, true);
        } catch (Throwable e) {
            return null;
        }
        return KHUNG[i];
    }

    /**
     * Khung độ hiếm pixel quanh ô vật phẩm tâm (cx, cy) cạnh s (phóng nguyên lần, Sử thi trở lên lấp lánh 2 nhịp).
     * false = không vẽ được (thiếu ảnh / ô quá nhỏ) - nơi gọi vẽ kiểu cũ.
     */
    public static boolean veKhungHiem(Graphics g, int colorName, int cx, int cy, int s) {
        return veKhungHiemTheoBac(g, bacKhung(colorName), cx, cy, s);
    }

    public static boolean veKhungHiemTheoBac(Graphics g, int bac, int cx, int cy, int s) {
        if (bac < 0 || s < 18) {
            return false;
        }
        int k = Math.max(1, Math.min(4, (s + 4) / 20));
        int f = bac >= 3 && ((Canvas.gameTick / 10) & 1) == 1 ? 1 : 0;
        Image img = anhKhung(bac, f, k);
        if (img == null) {
            return false;
        }
        g.drawImage(img, cx - 10 * k, cy - 10 * k, Graphics.TOP | Graphics.LEFT);
        return true;
    }
}
