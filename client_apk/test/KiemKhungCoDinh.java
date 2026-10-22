import com.htbb.pokemon.hd.HdPhongToan;
import javax.microedition.lcdui.KhungCoDinh;

/**
 * Kiểm thử thuần Java (không Android, không máy) cho CAMERA CỐ ĐỊNH của bản APK (KhungCoDinh) và toán phóng cửa sổ
 * jar (HdPhongToan):
 *  - phủ kín (cover), không viền; logic <= 640x360 mỗi chiều -> vùng nhìn thế giới <= 320x180 (20:9 = 20 x 9 ô,
 *    4:3 = 15 x 11,25 ô);
 *  - chạm màn thật -> logic khứ hồi đúng từng điểm, mép màn kẹp đúng;
 *  - vùng an toàn (tai thỏ) đổi đúng sang px logic;
 *  - cửa sổ phóng (HdPhong) nằm trọn trong khung, canh giữa; hai lớp (cửa sổ + hộp thoại) không làm hoạt hình
 *    mở chạy lại mỗi khung (lỗi popup xác nhận mua lệch); chạm đổi ngược đúng.
 *
 *   S=client_apk/pokemon/src/main/java; O=$(mktemp -d)
 *   javac -d $O $S/javax/microedition/lcdui/KhungCoDinh.java $S/com/htbb/pokemon/hd/HdPhongToan.java client_apk/test/KiemKhungCoDinh.java
 *   java -cp $O KiemKhungCoDinh      # mã thoát 1 = hỏng
 */
public final class KiemKhungCoDinh {
    static int dat, hong;

    static void bang(String mo, Object mong, Object that) {
        boolean ok = mong == null ? that == null : mong.equals(that);
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG " + mo + ": mong " + mong + " thuc te " + that);
        }
    }

    static void dung(String mo, boolean v) {
        bang(mo, Boolean.TRUE, Boolean.valueOf(v));
    }

    public static void main(String[] a) {
        kiemHeSo();
        kiemCham();
        kiemAnToan();
        kiemTheGioi();
        kiemPhong();
        System.out.println("KiemKhungCoDinh: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }

    /** Phủ kín: tiLe = max(w/640, h/360), logic = ceil(w/tiLe) x ceil(h/tiLe) <= 640 x 360, không viền. */
    static void heSo(int w, int h, float tiLe, int lw, int lh) {
        KhungCoDinh k = KhungCoDinh.tinh(w, h);
        String t = w + "x" + h;
        bang(t + " tiLe", Float.valueOf(tiLe), Float.valueOf(Math.round(k.tiLe * 1000) / 1000f));
        bang(t + " logicW", Integer.valueOf(lw), Integer.valueOf(k.logicW));
        bang(t + " logicH", Integer.valueOf(lh), Integer.valueOf(k.logicH));
        bang(t + " khong vien X", Integer.valueOf(0), Integer.valueOf(k.lechX));
        bang(t + " khong vien Y", Integer.valueOf(0), Integer.valueOf(k.lechY));
        dung(t + " phu kin ngang", k.logicW * k.tiLe >= w - 0.01f);
        dung(t + " phu kin doc", k.logicH * k.tiLe >= h - 0.01f);
        dung(t + " tran mep < 1 px logic", (k.logicW - 1) * k.tiLe < w && (k.logicH - 1) * k.tiLe < h);
        // công bằng: không thấy hơn tham chiếu 16:9 ở bất kỳ chiều nào
        dung(t + " vung nhin <= " + KhungCoDinh.NHIN_W + "x" + KhungCoDinh.NHIN_H + " (" + KhungCoDinh.nhinW(k.logicW) + "x"
                + KhungCoDinh.nhinH(k.logicH) + ")", KhungCoDinh.nhinW(k.logicW) <= KhungCoDinh.NHIN_W && KhungCoDinh.nhinH(k.logicH) <= KhungCoDinh.NHIN_H);
        // một chiều luôn đúng bằng tham chiếu (lấp đầy chiều đó)
        dung(t + " mot chieu = tham chieu", k.logicW == 640 || k.logicH == 360);
    }

    static void kiemHeSo() {
        heSo(1280, 720, 2f, 640, 360);
        heSo(1920, 1080, 3f, 640, 360);
        heSo(2560, 1440, 4f, 640, 360);
        heSo(2400, 1080, 3.75f, 640, 288);       // 20:9 -> 20 x 9 ô
        heSo(1600, 720, 2.5f, 640, 288);
        heSo(2340, 1080, 3.656f, 640, 296);      // 19.5:9
        heSo(3200, 1440, 5f, 640, 288);
        heSo(3440, 1440, 5.375f, 640, 268);      // 21:9
        heSo(1280, 800, 2.222f, 576, 360);       // 16:10 -> cắt ngang
        heSo(2560, 1600, 4.444f, 576, 360);
        heSo(2048, 1536, 4.267f, 480, 360);      // 4:3 -> 15 x 11,25 ô
        heSo(2772, 1240, 4.331f, 640, 287);
        heSo(854, 480, 1.334f, 640, 360);
        heSo(640, 360, 1f, 640, 360);
        // 0.9.7: camera "cao hơn" NHIN_THEM 1,15 -> vùng nhìn 368 x 207 px thế giới (23 x ~13 ô) thay 320 x 180
        bang("vung nhin the gioi rong", Integer.valueOf(368), Integer.valueOf(KhungCoDinh.nhinW(KhungCoDinh.LOGIC_W)));
        bang("vung nhin the gioi cao", Integer.valueOf(207), Integer.valueOf(KhungCoDinh.nhinH(KhungCoDinh.LOGIC_H)));
        bang("23 o ngang", Integer.valueOf(23), Integer.valueOf(KhungCoDinh.NHIN_W / 16));
        bang("20:9 -> 10 o doc", Integer.valueOf(10), Integer.valueOf(KhungCoDinh.nhinH(KhungCoDinh.tinh(2400, 1080).logicH) / 16));
        dung("thay them ~15% moi chieu", Math.abs(KhungCoDinh.NHIN_W / 320f - 1.15f) < 0.01f && Math.abs(KhungCoDinh.NHIN_H / 180f - 1.15f) < 0.01f);
    }

    static void kiemCham() {
        int[][] man = {{2400, 1080}, {1600, 720}, {1280, 800}, {854, 480}, {960, 540}, {2772, 1240}, {2048, 1536}, {3440, 1440}};
        for (int m = 0; m < man.length; m++) {
            KhungCoDinh k = KhungCoDinh.tinh(man[m][0], man[m][1]);
            boolean ok = true;
            for (int p = 0; p < k.logicW; p += 7) {
                float that = (p + 0.5f) * k.tiLe;
                if (that < man[m][0] && k.logicXKep(that) != p) ok = false;
            }
            for (int p = 0; p < k.logicH; p += 5) {
                float that = (p + 0.5f) * k.tiLe;
                if (that < man[m][1] && k.logicYKep(that) != p) ok = false;
            }
            String t = man[m][0] + "x" + man[m][1];
            dung(t + " cham khu hoi dung", ok);
            bang(t + " mep trai -> 0", Integer.valueOf(0), Integer.valueOf(k.logicXKep(0)));
            // mép phải / dưới: hàng logic cuối (hàng tràn < 1 px logic có thể không có điểm ảnh thật nào)
            dung(t + " mep phai -> hang cuoi", k.logicXKep(man[m][0] - 1) >= k.logicW - 2);
            dung(t + " mep duoi -> hang cuoi", k.logicYKep(man[m][1] - 1) >= k.logicH - 2);
            dung(t + " ca man la khung", k.trongKhung(0, 0) && k.trongKhung(man[m][0] - 1, man[m][1] - 1));
        }
    }

    static void kiemAnToan() {
        int[] l = KhungCoDinh.tinh(2400, 1080).anToanLogic(100, 0, 48, 0);
        bang("tai tho 100px / 3,75 -> 27 logic", Integer.valueOf(27), Integer.valueOf(l[0]));
        bang("bo goc phai 48px -> 13 logic", Integer.valueOf(13), Integer.valueOf(l[2]));
        l = KhungCoDinh.tinh(1600, 720).anToanLogic(200, 0, 0, 30);
        bang("tai tho 200px / 2,5 -> 80 logic", Integer.valueOf(80), Integer.valueOf(l[0]));
        bang("thanh duoi 30px / 2,5 -> 12 logic", Integer.valueOf(12), Integer.valueOf(l[3]));
        l = KhungCoDinh.tinh(1280, 720).anToanLogic(0, 0, 0, 0);
        bang("khong tai tho", Integer.valueOf(0), Integer.valueOf(l[0] + l[1] + l[2] + l[3]));
    }

    static void kiemTheGioi() {
        bang("cham 0 -> 0", Integer.valueOf(0), Integer.valueOf(KhungCoDinh.theGioi(0)));
        // chạm logic p -> thế giới floor(p / THE_GIOI_Z), THE_GIOI_Z = 2 / 1,15
        bang("cham 639 -> 367", Integer.valueOf(367), Integer.valueOf(KhungCoDinh.theGioi(639)));
        bang("cham 320 -> 184", Integer.valueOf(184), Integer.valueOf(KhungCoDinh.theGioi(320)));
        // khứ hồi: điểm thế giới w vẽ ở logic w * Z -> chạm vào đó trả lại đúng w
        boolean kh = true;
        for (int w = 0; w < 368; w++) {
            int p = (int) Math.floor((w + 0.5f) * KhungCoDinh.THE_GIOI_Z);
            if (KhungCoDinh.theGioi(p) != w && KhungCoDinh.theGioi(p) != w - 1) kh = false;
        }
        dung("cham -> the gioi khu hoi (sai <= 1 px)", kh);
        bang("cham -1 -> -1 (lam tron xuong)", Integer.valueOf(-1), Integer.valueOf(KhungCoDinh.theGioi(-1)));
    }

    static void kiemPhong() {
        int W = 640, H = 360, band = 2 * 13 + 2;
        // cửa sổ jar 145x144 giữa màn (WindowInfoScr), hệ số khung 3
        float x = 320 - 72, y = 180 - 74, w = 145, h = 144;
        float zz = HdPhongToan.heSo(W, H, band, y, w, h, 2.2f, 3, false);
        dung("cua so duoc phong (> 1)", zz > 1.01f);
        float[] bd = HdPhongToan.bienDoi(W, H, band, x, y, w, h, zz, 3, 1f, false);
        float l = x * bd[0] + bd[1], t = y * bd[0] + bd[2], r = (x + w) * bd[0] + bd[1], b = (y + h) * bd[0] + bd[2];
        dung("the phong trong khung ngang", l >= 0 && r <= W);
        dung("the phong tren thanh phim mem", t >= 0 && b <= H - band);
        dung("the canh giua ngang", Math.abs((l + r) / 2 - W / 2f) <= 1);
        // chạm ngược: tâm thẻ trên màn -> tâm cửa sổ jar
        int cx = (int) ((l + r) / 2), cy = (int) ((t + b) / 2);
        int jx = HdPhongToan.doi(cx, bd[0], bd[1], cy, H, band), jy = HdPhongToan.doi(cy, bd[0], bd[2], cy, H, band);
        dung("cham nguoc ve tam cua so", Math.abs(jx - (x + w / 2)) <= 1 && Math.abs(jy - (y + h / 2)) <= 1);
        bang("thanh phim mem khong doi", Integer.valueOf(100), Integer.valueOf(HdPhongToan.doi(100, bd[0], bd[1], H - 5, H, band)));
        // hai lớp vẽ xen kẽ mỗi khung (cửa sổ + hộp thoại xác nhận): hoạt hình mở KHÔNG được chạy lại
        HdPhongToan.Lop man = new HdPhongToan.Lop(), hop = new HdPhongToan.Lop();
        Object cuaSo = "cua so", xacNhan = "xac nhan";
        long t0 = 1000000;
        man.vao(cuaSo, t0);
        hop.vao(xacNhan, t0);
        man.tdW = 150;
        boolean moLai = false;
        for (long now = t0 + 16; now < t0 + 600; now += 16) {
            moLai |= man.vao(cuaSo, now);
            moLai |= hop.vao(xacNhan, now);
        }
        dung("hai lop xen ke khong mo lai", !moLai);
        bang("cua so mo xong (e = 1)", Float.valueOf(1f), Float.valueOf(man.doMo(t0 + 600)));
        bang("hop thoai mo xong (e = 1)", Float.valueOf(1f), Float.valueOf(hop.doMo(t0 + 600)));
        bang("khung do cua so giu nguyen", Float.valueOf(150f), Float.valueOf(man.tdW));
        float[] mo = HdPhongToan.bienDoi(W, H, band, x, y, w, h, zz, 3, man.doMo(t0 + 600), false);
        dung("mo xong: khong lech 14px / khong 92%", mo[0] == bd[0] && mo[1] == bd[1] && mo[2] == bd[2]);
        dung("lop dong lau roi mo lai = hoat hinh moi", man.vao(cuaSo, t0 + 600 + HdPhongToan.HET_MS + 50));
    }
}
