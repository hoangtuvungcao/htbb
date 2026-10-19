import com.htbb.pokemon.hd.HdPhongToan;
import javax.microedition.lcdui.KhungCoDinh;

/**
 * Kiểm thử thuần Java (không Android, không máy) cho CAMERA CỐ ĐỊNH của bản APK (KhungCoDinh) và toán phóng cửa sổ
 * jar (HdPhongToan):
 *  - logic luôn 640x360, vùng nhìn thế giới 320x180, hệ số nguyên khi được, viền canh giữa;
 *  - chạm màn thật -> logic khứ hồi đúng từng điểm, chạm vào viền kẹp mép;
 *  - vùng an toàn (tai thỏ) nằm trong viền thì không chừa;
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

    static void heSo(int w, int h, float tiLe, boolean nguyen, int lx, int ly) {
        KhungCoDinh k = KhungCoDinh.tinh(w, h);
        bang(w + "x" + h + " tiLe", Float.valueOf(tiLe), Float.valueOf(Math.round(k.tiLe * 1000) / 1000f));
        bang(w + "x" + h + " nguyen", Boolean.valueOf(nguyen), Boolean.valueOf(k.nguyen));
        bang(w + "x" + h + " lechX", Integer.valueOf(lx), Integer.valueOf(k.lechX));
        bang(w + "x" + h + " lechY", Integer.valueOf(ly), Integer.valueOf(k.lechY));
        dung(w + "x" + h + " khung vua man", k.khungW <= w && k.khungH <= h && k.lechX >= 0 && k.lechY >= 0);
        dung(w + "x" + h + " ti le 16:9 giu nguyen", Math.abs(k.khungW * 360 - k.khungH * 640) <= 640);
    }

    static void kiemHeSo() {
        heSo(1280, 720, 2f, true, 0, 0);
        heSo(1920, 1080, 3f, true, 0, 0);
        heSo(2560, 1440, 4f, true, 0, 0);
        heSo(2400, 1080, 3f, true, 240, 0);      // điện thoại 20:9 -> pillarbox
        heSo(1600, 720, 2f, true, 160, 0);
        heSo(2340, 1080, 3f, true, 210, 0);
        heSo(3200, 1440, 4f, true, 320, 0);
        heSo(1280, 800, 2f, true, 0, 40);        // máy tính bảng 16:10 -> letterbox
        heSo(2048, 1536, 3f, true, 64, 228);     // 4:3: 3 / 3,2 >= 0,8 -> nguyên
        heSo(2772, 1240, 3f, true, 426, 80);
        heSo(854, 480, 1.333f, false, 0, 0);     // 1 / 1,33 < 0,8 -> hệ số lẻ
        heSo(960, 540, 1.5f, false, 0, 0);
        heSo(640, 360, 1f, true, 0, 0);
        bang("vung nhin the gioi rong", Integer.valueOf(320), Integer.valueOf(KhungCoDinh.nhinW(KhungCoDinh.LOGIC_W)));
        bang("vung nhin the gioi cao", Integer.valueOf(180), Integer.valueOf(KhungCoDinh.nhinH(KhungCoDinh.LOGIC_H)));
        bang("20 o ngang", Integer.valueOf(20), Integer.valueOf(KhungCoDinh.NHIN_W / 16));
    }

    static void kiemCham() {
        int[][] man = {{2400, 1080}, {1600, 720}, {1280, 800}, {854, 480}, {960, 540}, {2772, 1240}};
        for (int m = 0; m < man.length; m++) {
            KhungCoDinh k = KhungCoDinh.tinh(man[m][0], man[m][1]);
            boolean ok = true;
            for (int p = 0; p < 640; p += 7) {
                float that = k.lechX + (p + 0.5f) * k.tiLe;
                if (k.logicXKep(that) != p) ok = false;
            }
            for (int p = 0; p < 360; p += 5) {
                float that = k.lechY + (p + 0.5f) * k.tiLe;
                if (k.logicYKep(that) != p) ok = false;
            }
            dung(man[m][0] + "x" + man[m][1] + " cham khu hoi dung", ok);
            bang(man[m][0] + "x" + man[m][1] + " cham vien trai kep 0", Integer.valueOf(0), Integer.valueOf(k.logicXKep(0)));
            bang(man[m][0] + "x" + man[m][1] + " cham vien phai kep 639", Integer.valueOf(639), Integer.valueOf(k.logicXKep(man[m][0] - 1)));
            dung(man[m][0] + "x" + man[m][1] + " vien khong thuoc khung", k.lechX == 0 || !k.trongKhung(k.lechX - 1, man[m][1] / 2f));
        }
    }

    static void kiemAnToan() {
        int[] l = KhungCoDinh.tinh(2400, 1080).anToanLogic(100, 0, 48, 0);
        bang("tai tho 100px nam trong vien 240px", Integer.valueOf(0), Integer.valueOf(l[0]));
        bang("bo goc phai trong vien", Integer.valueOf(0), Integer.valueOf(l[2]));
        l = KhungCoDinh.tinh(1600, 720).anToanLogic(200, 0, 0, 30);
        bang("tai tho 200px lan vao khung 40px -> 20 logic", Integer.valueOf(20), Integer.valueOf(l[0]));
        bang("thanh duoi 30px x2 -> 15 logic", Integer.valueOf(15), Integer.valueOf(l[3]));
        l = KhungCoDinh.tinh(1280, 720).anToanLogic(0, 0, 0, 0);
        bang("khong tai tho", Integer.valueOf(0), Integer.valueOf(l[0] + l[1] + l[2] + l[3]));
    }

    static void kiemTheGioi() {
        bang("cham 0 -> 0", Integer.valueOf(0), Integer.valueOf(KhungCoDinh.theGioi(0)));
        bang("cham 639 -> 319", Integer.valueOf(319), Integer.valueOf(KhungCoDinh.theGioi(639)));
        bang("cham 320 -> 160", Integer.valueOf(160), Integer.valueOf(KhungCoDinh.theGioi(320)));
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
