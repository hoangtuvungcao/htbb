package real;

import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * Ban do PHONG RIENG (0.9.7): ban sao chia khu cua mot ban do co san (nen + cong + tile cua {@code mapLoad}), moi khu
 * la mot phong cua mot to doi Pho ban / mot tran Cup (com.htbb.pokemon.PhongRieng). Khac Map thuong:
 * <ul>
 *   <li>KHONG nap quai tu tep map/map&lt;nap&gt; - quai do real.PhoBanGame trieu hoi theo tang (khu nao chi co quai cua
 *       phong do);</li>
 *   <li>khong phai bai luyen (isMapTrain = false): khong boss / quai rong cua bai luyen, khong cuop tieu.</li>
 * </ul>
 * Dang ky luc khoi dong o RealController.initMapHTBB qua real.PhongRiengGame.taoBanDo.
 */
public class MapPhongRieng extends Map {

    public MapPhongRieng(final int id, final int mapLoad, final int soKhu) {
        super(id, -1, 1, mapLoad, soKhu);
    }

    /** Chi doc kich thuoc ban do (MapNapTaiNguyen.loadMap goc tao quai cho moi khu - phong rieng tu trieu hoi). */
    @Override
    public void loadMap(final String filename, final int magic_physic) {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(filename));
            final int w = dis.readUnsignedByte();
            final int h = dis.readUnsignedByte();
            this.mapWidth = w * 16;
            this.mapHeight = h * 16;
        } catch (final Exception e) {
            System.out.println("[PHONG RIENG] khong doc duoc " + filename + ": " + e);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (final Exception ignored) {
                }
            }
        }
    }

    @Override
    public boolean isMapTrain() {
        return false;
    }
}
