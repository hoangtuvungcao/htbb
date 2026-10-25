import com.htbb.pokemon.hd.HudBoTri;
import javax.microedition.lcdui.KhungCoDinh;

/**
 * Kiểm CỠ CHẠM của HUD cảm ứng bản APK (com.htbb.pokemon.hd.HudBoTri, thuần Java) trên nhiều màn / dpi:
 * mọi nút >= 44 dp, cách nhau >= 8 dp, nằm trong vùng an toàn, không đè cần điều khiển, hàng nút trên không đè thẻ
 * bản đồ nhỏ - kể cả khi người chơi phóng nhỏ cụm nút (Chỉnh nút 0,7) hay phóng to (1,6).
 *
 *   S=client_apk/pokemon/src/main/java; O=$(mktemp -d)
 *   javac -d $O $S/javax/microedition/lcdui/KhungCoDinh.java $S/com/htbb/pokemon/hd/HudBoTri.java client_jar/tools/KiemCoCham.java
 *   java -cp $O KiemCoCham      # mã thoát 1 = hỏng
 */
public final class KiemCoCham {
    static int dat, hong;

    public static void main(String[] a) {
        // {rộng, cao, dpi, tai thỏ trái px}
        int[][] may = {
            {1920, 1080, 420, 0}, {2340, 1080, 420, 90}, {2400, 1080, 420, 100}, {2400, 1080, 480, 100}, {3200, 1440, 560, 120},
            {1600, 720, 280, 60}, {2560, 1080, 400, 0}, {2048, 1536, 320, 0}, {2560, 1600, 320, 0}, {1280, 800, 213, 0},
            {1280, 720, 320, 0}, {2772, 1240, 480, 110}, {1520, 720, 320, 70}};
        float[][] gs = {{1f, 1f, 1f, 1f}, {0.7f, 0.7f, 0.7f, 0.7f}, {1.6f, 1.6f, 1.6f, 1.6f}, {1f, 1.3f, 0.8f, 1f}};
        float[] g0 = {0, 0, 0, 0};
        for (int m = 0; m < may.length; m++) {
            KhungCoDinh k = KhungCoDinh.tinh(may[m][0], may[m][1]);
            int[] an = k.anToanLogic(may[m][3], 0, may[m][3] > 0 ? 48 : 0, 0);
            int bo = Math.min(may[m][0], may[m][1]) / 50;   // bo góc 2%
            int[] an2 = k.anToanLogic(Math.max(may[m][3], bo), 0, Math.max(may[m][3] > 0 ? 48 : 0, bo), 0);
            an = an2;
            float l = an[0] + 6, t = an[1] + 4, r = k.logicW - an[2] - 6, b = k.logicH - an[3] - 6;
            float dpL = (may[m][2] / 160f) / k.tiLe;
            float kd = Math.max(0.8f, Math.min(1.3f, (may[m][2] / k.tiLe) / 140f));
            for (int s = 0; s < gs.length; s++) {
                HudBoTri.dpLogic = dpL;
                HudBoTri.boTri(l, t, r, b, kd, gs[s], g0, g0);
                String loi = HudBoTri.kiem();
                String ten = may[m][0] + "x" + may[m][1] + "@" + may[m][2] + "dpi (logic " + k.logicW + "x" + k.logicH + ") co " + gs[s][1];
                if (loi.length() == 0) {
                    dat++;
                } else {
                    hong++;
                    System.out.println("HONG " + ten + ":\n" + loi);
                }
                // in kích thước thật của nút nhỏ nhất (dp) cho người đọc
                if (s == 0) {
                    float nho = 1e9f;
                    for (int n = 0; n < HudBoTri.N_MINIMAP; n++) if (!HudBoTri.an(n)) nho = Math.min(nho, 2 * HudBoTri.NR[n] / dpL);
                    System.out.println(ten + ": nut nho nhat " + Math.round(nho) + " dp");
                }
            }
        }
        System.out.println("KiemCoCham: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }
}
