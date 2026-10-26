import java.awt.image.BufferedImage;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM KÉO THẢ CHIÊU 0.9.22 (game.pokemon.KeoChieu - màn Nhân vật › Kỹ năng, + cần ngắm game.pokemon.NgamTay) - client
 * thật headless, khung vẽ thật (KiemChamHud.ve), sự kiện con trỏ thật (Canvas.pointerPressed / Dragged / Released) ở cỡ
 * PC và cỡ APK (phép đổi APK: ChupTrongGame.datCoApk - cỡ chạm 44 dp theo mật độ):
 *  1) chạm ô nhanh (không kéo) / rung dưới ngưỡng: KHÔNG kéo, chạm vẫn mở bảng gán (ChonChieuNhanh);
 *  2) kéo ô 1 -> ô 3: bóng = con trỏ - chỗ nắm (x1,15) ở MỌI khung, ô đích sáng đúng (giữa ô, mép trái / phải, khe giữa
 *     hai ô = ô gần hơn), thả = ĐỔI CHỖ ngay (RMS), không mở bảng gán;
 *  3) thả xa ngoài hàng ô = không đổi gì (trượt về), không kẹt; huỷ giữa chừng (KeoChieu.huy - APK ACTION_CANCEL) = không
 *     đổi, cú chạm sau vẫn chạy;
 *  4) danh sách -> ô: kéo HÌNH chiêu đã học vào ô = gán; chiêu đã ở ô khác = đổi chỗ (không trùng); nhấn giữ hàng = nhấc;
 *  5) vuốt dọc trên hàng (không phải hình) = cuộn, KHÔNG kéo;
 *  6) rời tab giữa lúc kéo = hết trạng thái kéo;
 *  7) ngắm tay: vùng chết (rung < vungChet không đổi hướng), nhấn lệch tâm nút không nhảy hướng, núm theo ngón (giữ độ
 *     lệch), vùng huỷ có trễ, bắn đúng hướng vạch đang vẽ.
 *
 *   sh client_pc/build.sh && O=$(mktemp -d)
 *   javac -cp client_pc/out/build/cls -sourcepath server/src -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemChamHud.java \
 *     client_pc/test/KiemKeoTha.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemKeoTha <thư mục ảnh>
 * Mã thoát 1 = có lỗi.
 */
public class KiemKeoTha {
    static int dat, hong;
    static String co = "";

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + co + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static void nhan(int x, int y) throws Exception {
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerPressed", x, y));
        KiemChamHud.ve();
    }

    static void keo(int x, int y) throws Exception {
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerDragged", x, y));
        KiemChamHud.ve();
    }

    static void tha(int x, int y) throws Exception {
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerReleased", x, y));
        KiemChamHud.ve();
        ngu(120);   // nhịp game xử lý cú thả (VungCham)
        KiemChamHud.ve();
    }

    static game.model.QuickSlot[] bo() {
        return game.model.MainChar.quickSlot[game.render.screen.GameScr.indexTabSlot];
    }

    /** Nội dung ô: "s<chiêu>" / "p<thuốc>" / "-". */
    static String nd(int o) {
        game.model.QuickSlot q = bo()[o];
        if (q == null || q.quickslotType < 0) return "-";
        return q.quickslotType == game.model.QuickSlot.TYPE_SKILL ? "s" + q.skillType : "p" + q.potionType;
    }

    static String hang() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5; i++) b.append(nd(i)).append(i < 4 ? "," : "");
        return b.toString();
    }

    static void dongBang() throws Exception {
        ChupTrongGame.chay(() -> {
            if (game.render.Canvas.currentDialog != null) game.render.Canvas.endDlg();
        });
        KiemChamHud.ve();
    }

    static void moTab() throws Exception {
        ChupTrongGame.chay(() -> {
            game.pokemon.ManNhanVat.mo(2);
        });
        KiemChamHud.ve();
        ngu(60);
        KiemChamHud.ve();
    }

    /** Kéo theo nhiều bước từ (x0, y0) tới (x1, y1); kiểm bóng mỗi khung = con trỏ - chỗ nắm x TO. */
    static int[] buocKeo(int x0, int y0, int x1, int y1, int n, int lx, int ly, String ten) throws Exception {
        int sai = 0, khung = 0;
        for (int k = 1; k <= n; k++) {
            int x = x0 + (x1 - x0) * k / n, y = y0 + (y1 - y0) * k / n;
            keo(x, y);
            int[] b = game.pokemon.KeoChieu.BONG_VE;
            if (game.pokemon.KeoChieu.dangKeo()) {
                khung++;
                int tl = game.pokemon.KeoChieu.TO;
                if (b == null || b[3] != x || b[4] != y || b[0] != x - lx * tl / 100 || b[1] != y - ly * tl / 100) sai++;
            }
        }
        kiem(khung > 0 && sai == 0, ten + ": bong = con tro - cho nam moi khung (" + sai + " sai / " + khung + " khung)");
        return new int[]{x1, y1};
    }

    static void kiemCo(String ten, java.io.File ra) throws Exception {
        co = ten;
        KiemChamHud.co = ten;
        game.pokemon.KeoChieu.datLai();
        moTab();
        int[][] o = new int[5][];
        for (int i = 0; i < 5; i++) o[i] = game.pokemon.KeoChieu.dichCua(i);
        boolean du = true;
        for (int i = 0; i < 5; i++) du &= o[i] != null;
        kiem(du, "hang o nhanh dang ky du 5 o dich");
        if (!du) return;
        int s = o[0][2], slop = game.pokemon.KeoChieu.slop();
        String h0 = hang();
        // (1) chạm ô 1: không kéo, mở bảng gán
        int cx = o[0][0] + s / 2, cy = o[0][1] + s / 2;
        nhan(cx, cy);
        ngu(60);
        KiemChamHud.ve();
        kiem(game.pokemon.KeoChieu.trangThai() == game.pokemon.KeoChieu.CHO, "nhan o co chieu: cho nguong (chua keo)");
        tha(cx, cy);
        kiem(!game.pokemon.KeoChieu.dangKeo() && game.pokemon.ChonChieuNhanh.dangMo(), "cham o (khong keo): mo bang gan nhu cu");
        kiem(hang().equals(h0), "cham o: khong doi o");
        dongBang();
        // (1b) rung dưới ngưỡng
        nhan(cx, cy);
        keo(cx + slop - 2, cy);
        kiem(!game.pokemon.KeoChieu.dangKeo(), "rung " + (slop - 2) + " px < nguong " + slop + ": chua keo");
        tha(cx + slop - 2, cy);
        kiem(game.pokemon.ChonChieuNhanh.dangMo() && hang().equals(h0), "rung duoi nguong: van la cham (mo bang gan)");
        dongBang();
        // (2) kéo ô 1 -> ô 3, nắm lệch tâm
        int bx = o[0][0] + s / 4, by = o[0][1] + s / 3;
        nhan(bx, by);
        keo(bx + slop + 1, by);
        kiem(game.pokemon.KeoChieu.dangKeo(), "keo qua nguong: bat dau keo");
        int[] cho = game.pokemon.KeoChieu.cho();
        kiem(cho[0] == s / 4 && cho[1] == s / 3, "giu cho nam (" + cho[0] + "," + cho[1] + " / " + s / 4 + "," + s / 3 + ")");
        int tx = o[2][0] + s / 2, ty = o[2][1] + s / 2;
        buocKeo(bx + slop + 1, by, tx, ty, 8, cho[0], cho[1], "o1->o3");
        kiem(game.pokemon.KeoChieu.dich() == 2, "giua o 3: dich o 3 (" + game.pokemon.KeoChieu.dich() + ")");
        BufferedImage anh = ChupTrongGame.ve(NenPc.thatW, NenPc.thatH);
        javax.imageio.ImageIO.write(anh, "png", new java.io.File(ra, "keo_o_" + ten + ".png"));
        // mép ô: trong ô 3 sát mép trái / phải; khe giữa ô 3 và ô 4 -> ô gần hơn
        keo(o[2][0], ty);
        kiem(game.pokemon.KeoChieu.dich() == 2, "mep trai o 3: dich o 3 (" + game.pokemon.KeoChieu.dich() + ")");
        keo(o[2][0] + s - 1, ty);
        kiem(game.pokemon.KeoChieu.dich() == 2, "mep phai o 3: dich o 3 (" + game.pokemon.KeoChieu.dich() + ")");
        int khe = o[3][0] - (o[2][0] + s);
        keo(o[2][0] + s + Math.max(0, khe / 2 - 1), ty);
        kiem(game.pokemon.KeoChieu.dich() == 2, "khe (nua trai) -> o 3 (" + game.pokemon.KeoChieu.dich() + ", khe " + khe + ")");
        keo(o[3][0] - Math.max(0, khe / 2 - 1), ty);
        kiem(game.pokemon.KeoChieu.dich() == 3, "khe (nua phai) -> o 4 (" + game.pokemon.KeoChieu.dich() + ")");
        keo(tx, ty - s / 2 - 2);
        kiem(game.pokemon.KeoChieu.dich() == 2, "tren o 3 mot chut: van hut vao o 3 (" + game.pokemon.KeoChieu.dich() + ")");
        keo(tx, ty);
        String a0 = nd(0), a2 = nd(2);
        tha(tx, ty);
        kiem(nd(0).equals(a2) && nd(2).equals(a0), "tha o 3: DOI CHO o 1 <-> o 3 (" + h0 + " -> " + hang() + ")");
        kiem(!game.pokemon.KeoChieu.dangKeo() && !game.pokemon.ChonChieuNhanh.dangMo(), "tha: het keo, khong mo bang gan");
        ngu(game.pokemon.KeoChieu.VE_MS + 40);
        KiemChamHud.ve();
        // (3) thả xa ngoài: không đổi
        String h1 = hang();
        cx = o[1][0] + s / 2;
        cy = o[1][1] + s / 2;
        nhan(cx, cy);
        keo(cx + slop + 2, cy + slop + 2);
        int fx = Math.min(game.render.Canvas.w - 5, cx + s * 2), fy = Math.min(game.render.Canvas.h - 5, cy + s * 4);
        keo(fx, fy);
        kiem(game.pokemon.KeoChieu.dangKeo() && game.pokemon.KeoChieu.dich() == -1, "keo xa: khong co dich");
        tha(fx, fy);
        kiem(hang().equals(h1) && game.pokemon.KeoChieu.trangThai() == game.pokemon.KeoChieu.NGHI, "tha ngoai: khong doi, het keo");
        KiemChamHud.ve();
        kiem(game.pokemon.KeoChieu.BONG_VE == null, "tha ngoai: bong truot ve (khong theo con tro nua)");
        ngu(game.pokemon.KeoChieu.VE_MS + 40);
        // huỷ giữa chừng (APK ACTION_CANCEL)
        nhan(cx, cy);
        keo(cx + slop + 3, cy);
        keo(o[3][0] + s / 2, o[3][1] + s / 2);
        kiem(game.pokemon.KeoChieu.dich() == 3, "keo o 2 toi o 4: dich o 4 (tt " + game.pokemon.KeoChieu.trangThai() + " dich " + game.pokemon.KeoChieu.dich() + " o2 " + nd(1) + ")");
        ChupTrongGame.chay(game.pokemon.KeoChieu::huy);
        KiemChamHud.ve();
        kiem(!game.pokemon.KeoChieu.dangKeo(), "huy: het keo ngay");
        tha(o[3][0] + s / 2, o[3][1] + s / 2);
        kiem(hang().equals(h1) && !game.pokemon.ChonChieuNhanh.dangMo(), "huy roi tha: khong doi, khong mo bang");
        ngu(game.pokemon.KeoChieu.VE_MS + 40);
        KiemChamHud.cham(o[1][0] + s / 2, o[1][1] + s / 2);
        kiem(game.pokemon.ChonChieuNhanh.dangMo(), "sau huy: cham o van mo bang gan (khong ket)");
        dongBang();
        // (4) danh sách -> ô
        KiemChamHud.ve();
        int sk = -1, skTrong = -1;
        for (int i = 0; i < 16; i++) {
            int[] v = game.pokemon.KeoChieu.chieuCua(i);
            if (v == null || v[2] <= 0) continue;
            boolean tren = false;
            for (int k = 0; k < 5; k++) tren |= nd(k).equals("s" + i);
            if (tren && sk < 0) sk = i;
            if (!tren && skTrong < 0) skTrong = i;
        }
        kiem(sk >= 0, "danh sach: co chieu da hoc dang tren hang o (keo duoc)");
        if (skTrong < 0 && sk >= 0) {
            // mọi chiêu thấy được đều đã ở hàng ô: bỏ trống ô của một chiêu để có chiêu "chưa gán"
            for (int k = 4; k >= 0 && skTrong < 0; k--) {
                game.model.QuickSlot q = bo()[k];
                if (q != null && q.quickslotType == game.model.QuickSlot.TYPE_SKILL && q.skillType != sk
                        && game.pokemon.KeoChieu.chieuCua(q.skillType) != null && game.pokemon.KeoChieu.chieuCua(q.skillType)[2] > 0) {
                    skTrong = q.skillType;
                    q.setIsNothing();
                }
            }
            KiemChamHud.ve();
        }
        kiem(skTrong >= 0, "danh sach: co chieu da hoc chua gan (keo vao o)");
        if (skTrong >= 0) {
            int[] v = game.pokemon.KeoChieu.chieuCua(skTrong);
            int ix = v[0] + v[2] / 2, iy = v[1] + v[2] / 2;
            nhan(ix, iy);
            keo(ix, iy - slop - 2);
            kiem(game.pokemon.KeoChieu.dangKeo(), "keo hinh chieu " + skTrong + " (len): bat dau keo, khong cuon");
            buocKeo(ix, iy - slop - 2, o[3][0] + s / 2, o[3][1] + s / 2, 6, v[2] / 2, v[2] / 2, "ds->o4");
            anh = ChupTrongGame.ve(NenPc.thatW, NenPc.thatH);
            javax.imageio.ImageIO.write(anh, "png", new java.io.File(ra, "keo_ds_" + ten + ".png"));
            tha(o[3][0] + s / 2, o[3][1] + s / 2);
            kiem(nd(3).equals("s" + skTrong), "tha chieu " + skTrong + " vao o 4: gan ngay (" + hang() + ")");
            ngu(game.pokemon.KeoChieu.VE_MS + 40);
        }
        if (sk >= 0) {
            int c = -1;
            for (int k = 0; k < 5; k++) if (nd(k).equals("s" + sk)) c = k;
            int dd = c == 0 ? 4 : 0;
            String truocD = nd(dd);
            KiemChamHud.ve();
            int[] v = game.pokemon.KeoChieu.chieuCua(sk);
            int ix = v[0] + v[2] / 2, iy = v[1] + v[2] / 2;
            nhan(ix, iy);
            keo(ix + slop + 2, iy);
            keo(o[dd][0] + s / 2, o[dd][1] + s / 2);
            tha(o[dd][0] + s / 2, o[dd][1] + s / 2);
            int dem = 0;
            for (int k = 0; k < 5; k++) if (nd(k).equals("s" + sk)) dem++;
            kiem(nd(dd).equals("s" + sk) && nd(c).equals(truocD) && dem == 1,
                    "chieu " + sk + " da o o " + (c + 1) + " -> o " + (dd + 1) + ": doi cho, khong trung (" + hang() + ")");
            ngu(game.pokemon.KeoChieu.VE_MS + 40);
        }
        // (5) vuốt dọc trên hàng (không phải hình) = không kéo
        KiemChamHud.ve();
        int[] v = sk >= 0 ? game.pokemon.KeoChieu.chieuCua(sk) : null;
        if (v != null) {
            int rx = v[3] + v[5] * 2 / 3, ry = v[4] + v[6] / 2;
            String h2 = hang();
            nhan(rx, ry);
            for (int k = 1; k <= 6; k++) keo(rx, ry - k * 8);
            kiem(!game.pokemon.KeoChieu.dangKeo(), "vuot doc tren hang: khong keo chieu");
            tha(rx, ry - 48);
            kiem(hang().equals(h2) && !game.pokemon.KeoChieu.dangKeo(), "vuot doc: khong doi o");
            // nhấn giữ hàng = nhấc
            KiemChamHud.ve();
            v = game.pokemon.KeoChieu.chieuCua(sk);
            if (v != null) {
                rx = v[3] + v[5] * 2 / 3;
                ry = v[4] + v[6] / 2;
                nhan(rx, ry);
                ngu(game.pokemon.KeoChieu.GIU_MS + 60);
                KiemChamHud.ve();
                kiem(game.pokemon.KeoChieu.dangKeo(), "nhan giu hang " + game.pokemon.KeoChieu.GIU_MS + " ms: nhac chieu (tt " + game.pokemon.KeoChieu.trangThai() + ")");
                // (6) rời tab giữa lúc kéo
                ChupTrongGame.chay(() -> game.pokemon.ManNhanVat.mo(0));
                KiemChamHud.ve();
                ngu(game.pokemon.KeoChieu.CU_MS + 100);
                tha(rx, ry);
                moTab();
                kiem(!game.pokemon.KeoChieu.dangKeo() && hang().equals(h2), "roi tab giua luc keo: het keo, khong doi");
            }
        }
        dongBang();
    }

    /** (7) ngắm tay (logic thuần, toạ độ nút giả). */
    static void kiemNgam() throws Exception {
        co = "ngam";
        game.pokemon.NgamTay.datLai();
        ChupTrongGame.chay(() -> {
            game.pokemon.NgamTay.chuot = false;
            game.pokemon.NgamTay.HUD_RIENG = true;   // như APK: HUD giữ cú chạm (không thì nhịp game thấy "không nhấn" = thả)
            game.pokemon.NgamTay.mayChu = true;   // đang ngắm (không thì nhịp game huỷ)
            game.pokemon.NgamTay.pvp = true;
            // nhấn lệch tâm nút 12 px sang phải: chưa có hướng
            game.pokemon.NgamTay.batDauKeo(1, 500, 300, 30, 512, 300);
            game.pokemon.NgamTay.keo(512, 300);
        });
        int[] c = game.pokemon.NgamTay.can();
        kiem(c != null && c[6] == 0, "nhan lech tam nut: chua co huong (khong nhay)");
        kiem(c != null && c[3] == 500 && c[4] == 300, "num o tam nut luc nhan (giu do lech)");
        int vc = game.pokemon.NgamTay.vungChet();
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.keo(512 + vc - 1, 300));
        c = game.pokemon.NgamTay.can();
        kiem(c[6] == 0, "rung < vung chet " + vc + ": chua co huong");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.keo(512, 300 - vc - 4));
        c = game.pokemon.NgamTay.can();
        kiem(c[6] == 1 && c[3] == 500 && c[4] == 300 - vc - 4, "qua vung chet: co huong, num theo ngon 1:1");
        // thả khi chưa ra khỏi nút: bắn ĐÚNG hướng đang vẽ (lên), không phải hướng quay
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.tha(512, 300 - vc - 4));
        kiem(game.pokemon.NgamTay.KQ_THA == 1, "tha trong nut nhung da co huong: ban theo huong ve (" + game.pokemon.NgamTay.KQ_THA + ")");
        // vùng huỷ có trễ
        ChupTrongGame.chay(() -> {
            game.pokemon.NgamTay.batDauKeo(1, 500, 300, 30, 500, 300);
            game.pokemon.NgamTay.keo(400, 300);
        });
        c = game.pokemon.NgamTay.can();
        kiem(c[3] == 500 - c[2] && c[4] == 300, "num kep trong can (" + c[3] + " / " + (500 - c[2]) + ")");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.keo(500 - 25, 300));
        kiem(!game.pokemon.NgamTay.vungHuy(), "ve gan nut (25 px, > 2/3 r): chua huy");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.keo(500 - 15, 300));
        kiem(game.pokemon.NgamTay.vungHuy(), "ve sat nut (15 px): huy");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.keo(500 - 25, 300));
        kiem(game.pokemon.NgamTay.vungHuy(), "ra 25 px (< r): van huy (tre - khong nhay)");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.keo(500 - 31, 300));
        kiem(!game.pokemon.NgamTay.vungHuy(), "ra ngoai nut: het huy");
        ChupTrongGame.chay(() -> game.pokemon.NgamTay.huy());
        kiem(!game.pokemon.NgamTay.dangGiu() && game.pokemon.NgamTay.can() == null, "huy: het ngam");
        game.pokemon.NgamTay.datLai();
        game.pokemon.NgamTay.HUD_RIENG = false;
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_keo_tha");
        ra.mkdirs();
        game.pokemon.Bang.dungHieuUng = true;
        System.setProperty("htbb.duLieu", new java.io.File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ngu(500);
        KiemChamHud.dungCanh(false);
        kiemNgam();
        String locCo = System.getProperty("chup.co");
        String[] pc = {"pc720", "pc1080", "pc4x3", "dt20x9"};
        for (String t : pc) {
            if (locCo != null && !("," + locCo + ",").contains("," + t + ",")) continue;
            int i = java.util.Arrays.asList(ChupTrongGame.TEN_CO).indexOf(t);
            int W = ChupTrongGame.CO[i][0], H = ChupTrongGame.CO[i][1], dpi = ChupTrongGame.CO[i][2];
            NenPc.tinhKichThuoc(W, H);
            float sc = NenPc.hienW / (float) NenPc.rong;
            game.pokemon.Bang.anTrai = 0;
            game.pokemon.Bang.anPhai = 0;
            game.pokemon.VungCham.MIN = dpi > 0 ? (int) Math.ceil(44 * (dpi / 160f) / sc) : (int) Math.ceil(32 / sc);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            try {
                kiemCo(t, ra);
            } catch (Throwable e) {
                e.printStackTrace();
                kiem(false, "nem loi: " + e);
            }
        }
        String[] apk = {"apk1080x1920d3", "apk720x1600d2", "apk1080x2400d3.25", "apk1600x2560tab"};
        for (String t : apk) {
            if (locCo != null && !("," + locCo + ",").contains("," + t + ",")) continue;
            int i = java.util.Arrays.asList(ChupTrongGame.TEN_CO_APK).indexOf(t);
            int[] c = ChupTrongGame.CO_APK[i];
            NenPc.tinhKichThuoc(c[0], c[1]);
            ChupTrongGame.datCoApk(c[0], c[1], c[2], c[3]);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            try {
                kiemCo(t, ra);
            } catch (Throwable e) {
                e.printStackTrace();
                kiem(false, "nem loi: " + e);
            }
        }
        System.out.println("KiemKeoTha: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
